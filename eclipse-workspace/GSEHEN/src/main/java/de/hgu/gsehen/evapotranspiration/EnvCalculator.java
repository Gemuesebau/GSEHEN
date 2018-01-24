package de.hgu.gsehen.evapotranspiration;

import static java.lang.Math.PI;

import org.junit.platform.commons.annotation.Testable;

public class EnvCalculator {
  public static final double PI2 = PI;

  @Testable
  /**
   * Method to calculate the daily reference evapotranspiration as published by Allen et al. All
   * parameters are daily values. method changes object DayData et0
   * 
   * @author MO
   * @param dayData An Object of the DayData class
   * @param geoData An Object of the GeoData class
   */
  public static void calculateEt0(DayData dayData, GeoData geoData) {
    dayData.setEt0(5);
  }
}
