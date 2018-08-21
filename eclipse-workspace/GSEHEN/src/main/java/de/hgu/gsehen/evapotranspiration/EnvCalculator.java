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
  private static final double GSC = 0.082;
  /**
   * Flag for calculating Rs0.
   */
  private static final boolean OLDRS0 = false;
  /**
   * Soil heat flux is 0 for daily calculation step.
   */
  private static final double G = 0;
  /**
   * Flag for calculating gamma procedure.
   */
  private static final boolean OLDGAMMA = false;
  /**
   * Flag for calculating net longwave radiation.
   */
  private static final boolean OLDRNL = false;

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
    double solarRad = dayData.getGlobalRad();
    double latentVaporHeatFlux = calculateLatentVaporHeatFlux(dayData);
    double slope = calculateSlope(dayData);
    double distanceSun = calculateDistanceSun(yday);
    double sunDeclination = calculateSunDeclination(yday);
    double omegaS = calculateOmegaS(sunDeclination, psi);
    double exTerRad = calculateExTerRad(distanceSun, omegaS, psi, sunDeclination);
    double clearSkyRad = calculateClearSkyRad(exTerRad, geoData);
    double netShortRad = calculateNetShortRad(solarRad);
    AbsTemp absTemp = calculateAbsTemp(dayData);
    double satVaP = calculateSatVaP(dayData);
    double actVaP = calculateActVaP(dayData, satVaP);
    double netLongRad = calculateNetLongRad(absTemp, actVaP, solarRad, clearSkyRad);
    double netRad = calculateNetRad(netShortRad, netLongRad);
    double airP = calculateAirP(geoData);
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
   * Method to calculate the slope of vapor saturation curve.
   *
   * @param dayData DayData class with mean temperature
   * @return double s or delta
   */
  private static double calculateSlope(DayData dayData) {
    double meanT = dayData.getTempMean();
    return ((4098 * 0.6108 * exp((17.27 * meanT) / (237.3 + meanT))) / pow((237.3 + meanT), 2));
  }

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
  private static double calculateClearSkyRad(double exTerRad, GeoData geoData) {
    if (OLDRS0) {

      return (0.75 * exTerRad);
    } else {
      return ((0.75 + 2 * 10e-5 * geoData.getHeighAbvNn()) * exTerRad);
    }
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
   * @param dayData class with meanTemp Temperature mean
   * @return absTemp absolut temperature values of class absTemp as output
   */
  private static AbsTemp calculateAbsTemp(DayData dayData) {
    double meanTemp = dayData.getTempMean();
    double maxTemp = dayData.getTempMax();
    double minTemp = dayData.getTempMin();
    AbsTemp absTemp = new AbsTemp(0.0, 0.0, 0.0);
    absTemp.setAbsTempMean(273.16 + meanTemp);
    absTemp.setAbsTempMax(273.16 + maxTemp);
    absTemp.setAbsTempMin(273.16 + minTemp);
    return (absTemp);
  }

  /**
   * Template method to calculate saturation vapor pressure from Temp.
   *
   * @see calculateSatVP
   * @param DayData class with meanTemp Temperature mean
   * @return satVp
   */
  private static double tempCalcSatVaP(double temp) {

    return (0.6108 * exp((17.27 * temp) / (237.3 + temp)));
  }

  /**
   * Method to calculate mean saturation vapor pressure.
   *
   * @param maxTemp Temperature maximum
   * @param minTemp Temperature minimum
   * @return es saturation vapor pressure
   */
  private static double calculateSatVaP(DayData dayData) {
    if (dayData.getTempMax() != null && dayData.getTempMax() != null) {
      // b.c.
      // sane value
      double maxTemp = dayData.getTempMax();
      double minTemp = dayData.getTempMin();
      double maxVaP = tempCalcSatVaP(maxTemp);
      double minVaP = tempCalcSatVaP(minTemp);
      double meanVaP = (maxVaP + minVaP) / 2;
      return (meanVaP);
    } else {
      double meanTemp = dayData.getTempMean();
      return (tempCalcSatVaP(meanTemp));
    }
  }

  /**
   * Method to calculate the actual vapor pressure.
   *
   * @param dayData class containing airHumidityRelmean relative air humidity mean. Optional min and
   *        max TODO
   * @param satVP saturation vapor pressure @see calculateSatVP
   * @return actual Vapor pressure ea
   */
  private static double calculateActVaP(DayData dayData, double satVaP) {
    double airHumidityRel = dayData.getAirHumidityRelMean();

    if (dayData.getAirHumidityRelMin() != null && dayData.getAirHumidityRelMax() != null) {
      return ((tempCalcSatVaP(dayData.getTempMin()) * dayData.getAirHumidityRelMax() / 100
          + tempCalcSatVaP(dayData.getTempMax()) * dayData.getAirHumidityRelMin() / 100) / 2);
    } else {
      return (satVaP * airHumidityRel / 100);
    }
  }


  /**
   * Method to calculate long wave net radiation. Depends on flag OLDRNL. If OLDRNL long wave net
   * radiation is calculated with temperature mean, else it is calculated with temperature minimum
   * and maximum.
   *
   * @param absTemp absolute Temperature
   * @param absTempMax absolute Temperature maximum
   * @param absTempMin absolute Temperature minimum
   * @param actVP actual vapor pressure
   * @param solarRad sRad global radiation / is measured
   * @param clearSkyRad sRad0 clear sky global radiation
   * @return NetLongRad long wave net radiation Rnl
   */
  private static double calculateNetLongRad(AbsTemp absTemp, double actVaP, double solarRad,
      double clearSkyRad) {
    if (OLDRNL) {
      return (4.901e-09 * pow(absTemp.getAbsTempMean(), 4) * (0.34 - 0.14 * sqrt(actVaP))
          * (1.35 * (solarRad / clearSkyRad) - 0.35));
    } else {
      return (4.901e-09 * ((pow(absTemp.getAbsTempMax(), 4) + pow(absTemp.getAbsTempMin(), 4)) / 2)
          * (0.34 - 0.14 * sqrt(actVaP)) * (1.35 * (solarRad / clearSkyRad) - 0.35));
    }
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
   * Method to calculate the psychrometric constant. Depends on OLDGAMMA flag. No obvious
   * differences between calculations. If OLDGAMMA then gamma is calculated with
   * latenetVapourHeutFlux otherwise not.
   *
   * @param latentVaporHeatFlux latent vapor heat flux
   * @param airP air pressure
   * @return gamma psychrometric constant
   */
  private static double calculateGamma(double latentVaporHeatFlux, double airP) {

    double gammaOld = ((0.001013 * airP) / (0.622 * latentVaporHeatFlux));
    double gammaNew = 0.665 * pow(10, -3) * airP;
    if (OLDGAMMA) {
      return (gammaOld);
    } else {
      return (gammaNew);
    }
  }


  private static double calculateEtFao(double netRad, DayData dayData, double satVaP, double actVaP,
      double slope, double gamma) {
    double meanTemp = dayData.getTempMean();
    double u2 = dayData.getWindspeed2m();
    return ((0.408 * slope * (netRad - G)
        + gamma * (900 / (meanTemp + 273) * u2 * (satVaP - actVaP)))
        / (slope + gamma * (1 + 0.34 * u2)));
  }

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
