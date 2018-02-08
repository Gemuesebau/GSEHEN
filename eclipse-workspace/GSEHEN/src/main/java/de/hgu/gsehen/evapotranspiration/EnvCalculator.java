package de.hgu.gsehen.evapotranspiration;

import static java.lang.Math.PI;
import static java.lang.Math.acos;
import static java.lang.Math.cos;
import static java.lang.Math.exp;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.tan;

import java.util.Calendar;

import org.junit.platform.commons.annotation.Testable;

public class EnvCalculator {
  private static final double ALPHA = 0.23;
  private static final double LAI = 0.7;
  private static final double RS = 70;
  private static final double RA = 208;
  private static final double GSC = 0.082;
  private static final double G = 0;

  /**
   * Main method to calculate the daily reference evapotranspiration as published by Allen et al.
   * All parameters are daily values. method changes object DayData et0
   * 
   * @author MO
   * @param dayData An Object of the DayData class
   * @param geoData An Object of the GeoData class
   */
  @Testable
  public static void calculateEt0(DayData dayData, GeoData geoData) {
    Integer yday = tellDoy(dayData);

    double psi = convertDegToRad(geoData);
    double solarRad = dayData.getGlobalRad(); // TODO object
    double latentVaporHeatFlux = calculateLatentVaporHeatFlux(dayData);
    double slope = calculateSlope(dayData);
    double distanceSun = calculateDistanceSun(yday);
    double sunDeclination = calculateSunDeclination(yday);
    double omegaS = calculateOmegaS(sunDeclination, psi);
    double posSunDur = calculatePosSunDur(omegaS);
    double exTerRad = calculateExTerRad(distanceSun, omegaS, psi, sunDeclination);
    double clearSkyRad = calculateClearSkyRad(exTerRad);
    double netShortRad = calculateNetShortRad(solarRad);
    double absTemp = calculateAbsTemp(dayData);
    double satVaP = calculateSatVaP(dayData);
    double actVaP = calculateActVaP(dayData, satVaP);
    double netLongRad = calculateNetLongRad(absTemp, actVaP, solarRad, clearSkyRad);
    double netRad = calculateNetRad(netShortRad, netLongRad);
    double airP = calculateAirP(geoData);
    double rho = calculateRho(dayData, airP);
    double gamma = calculateGamma(latentVaporHeatFlux, airP);
    double etFao = calculateEtFao(netRad, dayData, satVaP, actVaP, slope, gamma);

    dayData.setEt0(etFao);


  }


  /**
   * Method to convert the geo position degree into rad.
   * 
   * @author MO
   * @param geoData An object of the GeoData class
   * @return double geographical width position in rad aka. psi
   */
  private static double convertDegToRad(GeoData geoData) {
    double psi = geoData.getGeoWid() * PI / 180;
    return (psi);
  }


  /**
   * Method to calcualte the latent vapor heat flux from temperature mean.
   * 
   * @param dayData DayData class with mean temperature
   * @return double latent vapor heat flux L
   */
  private static double calculateLatentVaporHeatFlux(DayData dayData) {
    return (2.501 - 0.002361 * dayData.getTempMean());
  }

  /**
   * MEthod to calculate the slope of vapor saturation curve.
   * 
   * @param dayData DayData class with mean temperature
   * @return double s or delta
   */
  private static double calculateSlope(DayData dayData) {
    double meanT = dayData.getTempMean();
    return ((4098 * 0.6108 * exp((17.27 * meanT) / (237.3 + meanT))) / pow((237.3 + meanT), 2));
  }

  /*-
  s <- function(Temp) {
      (4098 * 0.6108 * exp((17.27 * Temp)/(237.3 + Temp)))/((237.3 + 
          Temp)^2)
  }
  */
  /**
   * Method to calculate the relative distance of the earth to the sun.
   * 
   * @param yday double day of the year 1-366
   * @return double returns the relative distance dr
   */
  private static double calculateDistanceSun(double yday) {
    return (1 + 0.033 * cos((2 * PI / 365) * yday));
  }

  /**
   * Method to calculate the declination of the sun.
   * 
   * @param yday double day of the year 1-366
   * @return double theta
   */
  private static double calculateSunDeclination(double yday) {
    return (0.409 * sin((2 * PI / 365) * yday - 1.39));
  }

  /**
   * Method to calculate hour arc on sunset.
   * 
   * @param theta declination of the sun
   * @param psi geo position in rad
   * @return
   */
  private static double calculateOmegaS(double theta, double psi) {
    return (acos(-tan(psi) * tan(theta)));
  }

  /**
   * Method to calculate the atronomicla possible sunshine duration.
   * 
   * @param omegas hour arc on sunset
   * @return double sunshine duration
   */
  private static double calculatePosSunDur(double omegaS) {
    return (24 / PI * omegaS);
  }

  /**
   * Method to calculate extra terrestrial radiation.
   * 
   * @param GSC constant
   * @param distanceSun dr relative distance earth to sun
   * @param omegas hour arc on sunset
   * @param psi geo postion in rad
   * @param sunDeclination theta declination of the sun
   * @return double Ra exTerRad
   */
  private static double calculateExTerRad(double distanceSun, double omegaS, double psi,
      double sunDeclination) {
    return ((24 * 60 / PI) * GSC * distanceSun
        * (omegaS * sin(psi) * sin(sunDeclination) + cos(psi) * cos(sunDeclination) * sin(omegaS)));
  }

  /*
   * Method to calculate global irradiation by cloudless sky angstroem coeff (0.25 + 0.5)*Ra
   * 
   * @param exTerRad extra terestrial radiation Ra
   * 
   * @return cloudless sky global irradiatoin Rs0
   */
  private static double calculateClearSkyRad(double exTerRad) {
    return (0.75 * exTerRad);
  }

  /**
   * Method to calculate short wave net radiation.
   * 
   * @param solarRad Rs measured solar radiation / global irritation
   * @return short wave net radiation Rns
   */
  private static double calculateNetShortRad(double solarRad) {
    return ((1 - ALPHA) * solarRad);
  }


  /**
   * Method to calculate the absolute Temperature in kelvin.
   * 
   * @param DayData class with meanTemp Temperature mean
   * @return absTemp Absolute temperature Tabs
   */
  private static double calculateAbsTemp(DayData dayData) {
    double meanTemp = dayData.getTempMean();
    return (273.16 + meanTemp);
  }

  /**
   * Template method to calculate saturation vapor pressure from Temp.
   * 
   * @see calculateSatVP
   * @param DayData class with meanTemp Temperature mean
   * @return satVp
   */
  private static double tempCalcSatVP(double temp) {

    return (0.6108 * exp((17.27 * temp) / (237.3 + temp)));
  }

  /**
   * Method to calculate saturation vapor pressure.
   * 
   * @param maxTemp Temperature maximum
   * @param minTemp Temperature minimum
   * @return es saturation vapor pressure
   */
  private static double calculateSatVaP(DayData dayData) {
    if (dayData.getTempMax() != 0 && dayData.getTempMax() != 0) { // FIXME this is not fail safe
                                                                  // b.c.
                                                                  // sane value
      double maxTemp = dayData.getTempMax();
      double minTemp = dayData.getTempMin();
      double maxVP = tempCalcSatVP(maxTemp);
      double minVP = tempCalcSatVP(minTemp);
      double meanVP = (maxVP + minVP) / 2;
      return (meanVP);
    } else {
      double meanTemp = dayData.getTempMean();
      return (tempCalcSatVP(meanTemp));
    }
  }

  /**
   * Method to calculate the actual vapor pressure.
   * 
   * @param dayData class containing airHumidityRel relative air humidity
   * @param satVP saturation vapor pressure @see calculateSatVP
   * @return actual Vapor pressure ea
   */
  private static double calculateActVaP(DayData dayData, double satVP) {
    double airHumidityRel = dayData.getAirHumidityRel();
    return (satVP * (airHumidityRel / 100));
  }


  /**
   * Method to calculate long wave net radiation.
   * 
   * @param absTemp absolute Temperature
   * @param actVP actual vapor pressure
   * @param solarRad sRad global radiation / is measured
   * @param clearSkyRad sRad0 clear sky global radiation
   * @return NetLongRad long wave net radiation Rnl
   */
  private static double calculateNetLongRad(double absTemp, double actVP, double solarRad,
      double clearSkyRad) {
    return (4.901e-09 * pow(absTemp, 4) * (0.34 - 0.14 * sqrt(actVP))
        * (1.35 * (solarRad / clearSkyRad) - 0.35));
  }

  /**
   * Method to calculate net radiation.
   * 
   * @param netShortRad net short radiation
   * @param netLongRad net long radiation
   * @return netRad net radiation Rn
   */
  private static double calculateNetRad(double netShortRad, double netLongRad) {
    return (netShortRad - netLongRad);
  }

  /*-
  soil heat flux is excluded -not needed
  G <- function(Rns) {
      0.2 * Rns
  }
  */

  /**
   * Method to calculate the air pressure for the location.
   * 
   * @param geoData class containing heighAbvNn location high above normal null
   * @return AirP air pressure P
   */
  private static double calculateAirP(GeoData geoData) {

    return (101.3 * pow(((293 - 0.0065 * geoData.getHeighAbvNn()) / 293), 5.26));
  }



  /**
   * Method to calculate the air density at constant pressure.
   * 
   * @param dayData class containing meanTemp Temperature mean
   * @param airP @see calculateAirP
   * @return Rho air density at constant pressure
   */
  private static double calculateRho(DayData dayData, double airP) {
    double meanTemp = dayData.getTempMean();
    return (airP / (1.01 * (meanTemp + 273) * 0.287)); // 273 or absTemp 273.16?
  }

  /**
   * Method to calculate the psychrometric constant.
   * 
   * @param latentVaporHeatFlux
   * @param airP
   * @return gamma psychrometric constant
   */
  private static double calculateGamma(double latentVaporHeatFlux, double airP) {

    return ((0.001013 * airP) / (0.622 * latentVaporHeatFlux));
  }


  private static double calculateEtFao(double netRad, DayData dayData, double satVP, double actVP,
      double slope, double gamma) {
    double meanTemp = dayData.getTempMean();
    double u2 = dayData.getWindspeed2m();
    return ((0.408 * slope * (netRad - G) + gamma * (900 / (meanTemp + 273) * u2 * (satVP - actVP)))
        / (slope + gamma * (1 + 0.34 * u2)));
  }
  /*-
  
  
  }
  }
  Etfao.f <- function(Rn, G, Temp, u2, es, ea, s, gamma) {
      (0.408 * s * (Rn - G) + gamma * (900/(Temp + 273) * u2 * 
          (es - ea)))/(s + gamma * (1 + 0.34 * u2))
  }
  Latente <- L(Temp)
  Steigung <- s(Temp)
  Relatived <- dr(J)
  DekdS <- theta(J)
  Wbsu <- omegas(theta = DekdS, psi)
  N <- N.f(Wbsu)
  Exts <- Ra(Gsc = Gsc, dr = Relatived, omegas = Wbsu, psi = psi, 
      theta = DekdS)
  Gbu <- Rs0(Ra = Exts)
  Kns <- Rns(Rs = Rs, alpha = alpha)
  AT <- Tabs(Temp = Temp)
  Sd <- es(Temp = Temp,Tmin = Tmin, Tmax= Tmax)
  Ad <- ea(Hum = Hum, es = Sd)
  LNs <- Rnl(Tabs = AT, ea = Ad, Rs = Rs, Rs0 = Gbu)
  Strbil <- Rn(Rns = Kns, Rnl = LNs)
  Bws <- G(Rns = Kns)
  LuftD <- P(NN = NN)
  Ldich <- rho(Temp = Temp, P = LuftD)
  Psy <- gamma(L = Latente, P = LuftD)
  EtfaomG <- Etfao.f(Rn = Strbil, G = 0, Temp = Temp, u2 = u2, 
      es = Sd, ea = Ad, s = Steigung, gamma = Psy)
  FAO56 <- round(digits = 1, EtfaomG)
  */

  /**
   * Method to extract the day of the year 1-366 from a date from a DayData object.
   * 
   * @author MO
   * @param dayData DayData object
   * @return An Integer with the value of day of the year 1-366
   */
  private static Integer tellDoy(DayData dayData) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(dayData.getDate());
    Integer yday = cal.get(Calendar.DAY_OF_YEAR);
    return (yday);
  }
}
