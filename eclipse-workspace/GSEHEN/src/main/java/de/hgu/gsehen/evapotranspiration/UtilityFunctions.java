package de.hgu.gsehen.evapotranspiration;

import static java.lang.Math.log;

import de.hgu.gsehen.Gsehen;
import de.hgu.gsehen.model.Plot;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UtilityFunctions {

  private static final Logger LOGGER = Logger.getLogger(Gsehen.class.getName());

  /**
   * Method to convert wind speed measurements to a height of 2 m above ground level.
   *
   * @param v Windspeed in m/s
   * @param hw Height of the anemometer in m
   * @return windspeed converted to 2 m above grund level
   */
  public static double convertWindSpeed2m(Double v, Double hw) {
    if (hw.equals(2.0)) {
      return (v);
    } else {
      return ((v * 4.87) / log(67.8 * hw - 5.42));
    }
  }

  /**
   * Method to get the start date of a plot to query the required weather data.
   * 
   * @param plot Plot, the plot in question.
   * @return Date, the start date of the plot.
   */
  public static Date determineDataStartDate(Plot plot) {
    final Date cropStart = plot.getCropStart();
    final Date soilStart = plot.getSoilStartDate();
    if (soilStart == null && cropStart != null) {
      return (cropStart);
    } else if (soilStart != null) {
      return (soilStart);
    } else {
      final String Error = "Für den Plot" + plot.getName() + plot.getUuid()
          + "wurde weder ein Boden- noch ein Kultur-Startdatum gesetzt.";
      LOGGER.log(Level.SEVERE, Error);
      return (null);
    }
  }
}
