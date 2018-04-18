package de.hgu.gsehen.evapotranspiration;

import static java.lang.Math.log;

public class UtilityFunctions {
  /**
   * Method to convert wind speed measurements to a height of 2 m above ground level.
   *
   * @param v Windspeed in m/s
   * @param hw Height of the anemometer in m
   * @return
   */
  private static double convertWindSpeed2m(double v, double hw) {
    return ((v * 4.87) / log(67.8 * hw - 5.42));
  }
}
