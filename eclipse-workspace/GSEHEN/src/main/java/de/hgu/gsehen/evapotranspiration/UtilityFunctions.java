package de.hgu.gsehen.evapotranspiration;

import static de.hgu.gsehen.util.MessageUtil.logMessage;
import static java.lang.Math.log;

import de.hgu.gsehen.model.Plot;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UtilityFunctions {

  private static final Logger LOGGER = Logger.getLogger(UtilityFunctions.class.getName());

  /**
   * Returns the java.util.logging.Level for the given level name.
   *
   * @param logLevelName the name of a level, like SEVERE .. FINEST
   * @return the level for the given name
   */
  public static Level getLevelForName(String logLevelName) {
    try {
      return (Level) Level.class.getField(logLevelName).get(null);
    } catch (Exception e) {
      return Level.INFO;
    }
  }

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
      logMessage(LOGGER, Level.SEVERE, "utility.determine.data.startdate.error",
          plot.getName(), plot.getUuid());
      return (null);
    }
  }
}
