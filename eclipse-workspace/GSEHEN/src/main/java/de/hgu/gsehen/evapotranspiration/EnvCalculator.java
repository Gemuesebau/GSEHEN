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
    dayData.setEt0(5);
    double solarRadiation = dayData.getGlobalRad();
    double u2 = dayData.getWindspeed2m();
    double latentVaporHeatFlux = calculateLatentVaporHeatFlux(dayData);
    double slope = calculateSlope(dayData);
    double distanceSun = calculateDistanceSun(yday);
    double sunDeclination = calculateSunDeclination(yday);
    double omegaS = calculateOmegaS(sunDeclination, psi);    
    double posSunDur = calculatePosSunDur(omegaS);
    double extRad = calculateExTerRad(dr, omegaS, psi, theta);

    
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
   * @param rA extra terestrial radiation
   * 
   * @return
   */
  private static double calculateRs0(double rA) {
    return (0.75 * rA);
  }

  /**
   * Method to calculate short wave net radiation
   * 
   * @param rS
   * @return short wave net radiation Rns
   */
  private static double calculateNetShortRad(double rS) {
    return ((1 - ALPHA) * rS);
  }


  /**
   * Method to calculate the absolute Temperature in kelvin
   * 
   * @param meanTemp Temperature mean
   * @return absTemp Absolute temperature Tabs
   */
  private static double calculateAbsTemp(double meanTemp) {
    return (273.16 + meanTemp);
  }

  /**
   * Method to calculate saturation vapor pressure from meanTemp
   * 
   * @param meanTemp Temperature mean
   * @return
   */
  private static double calculateSatVP(double meanTemp) {
    return (0.6108 * exp((17.27 * meanTemp) / (237.3 + meanTemp)));
  }

  /**
   * Method to calculate saturation vapor pressure
   * 
   * @param maxTemp Temperature maximum
   * @param minTemp Temperature minimum
   * @return es saturation vapor pressure
   */
  private static double calculateSatVP(double maxTemp, double minTemp) {
    double maxVP = 0.6108 * exp((17.27 * maxTemp) / (237.3 + maxTemp));
    double minVP = 0.6108 * exp((17.27 * maxTemp) / (237.3 + maxTemp));
    double meanVP = (maxVP + minVP) / 2;
    return (meanVP);
  }

  /**
   * Method to calculate the actual vapor pressure
   * 
   * @param airHumidityRel
   * @param satVP
   * @return actual Vapor pressure ea
   */
  private static double caculateActVP(double airHumidityRel, double satVP) {
    return (satVP * (airHumidityRel / 100));
  }


  /**
   * Method to calculate long wave net radiation
   * 
   * @param absTemp absolute Temperature
   * @param actVP actual vapor pressure
   * @param sRad global radiation / is measured
   * @param sRad0 clear sky global radiation
   * @return long wave net radiation Rnl
   */
  private static double calculateNetLongRad(double absTemp, double actVP, double sRad,
      double sRad0) {
    return (4.901e-09 * pow(absTemp, 4) * (0.34 - 0.14 * sqrt(actVP))
        * (1.35 * (sRad / sRad0) - 0.35));
  }

  /**
   * Method to calculate net radiation
   * 
   * @param netShortRad net short radiation
   * @param netLongRad net long radiation
   * @return netRad net radiation Rn
   */
  private double calculateNetRad(double netShortRad, double netLongRad) {
    return (netShortRad - netLongRad);
  }

  /*-
  soil heat flux is excluded -not needed
  G <- function(Rns) {
      0.2 * Rns
  }
  */

  /**
   * Method to calculate the air pressure for the location
   * 
   * @param geoData class containing heighAbvNn location high above normal null
   * @return AirP air pressure P
   */
  private static double calculateAirP(GeoData geoData) {

    return (101.3 * pow(((293 - 0.0065 * geoData.getHeighAbvNn()) / 293), 5.26));
  }



  /**
   * Method to calculate the air density by constant pressure
   * 
   * @param meanTemp
   * @param airP
   * @return
   */
  private static double calculateRho(double meanTemp, double airP) {

    return (airP / (1.01 * (meanTemp + 273) * 0.287)); // 273 or absTemp 273.16?
  }

  /**
   * Method to calculate the psychrometric constant
   * 
   * @param latentVaporHeatFlux
   * @param airP
   * @return gamma psychrometric constant
   */
  private static double calculateGamma(double latentVaporHeatFlux, double airP) {

    return ((0.001013 * airP) / (0.622 * latentVaporHeatFlux));
  }


  private static double calculateEtFao(double netRad, double G, double meanTemp, double u2,
      double satVP, double actVP, double slope, double gamma) {
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
