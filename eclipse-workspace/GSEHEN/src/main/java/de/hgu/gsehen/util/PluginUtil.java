package de.hgu.gsehen.util;

import static de.hgu.gsehen.util.TextResourceUtil.evaluateJsResource;

import de.hgu.gsehen.Gsehen;
import de.hgu.gsehen.evapotranspiration.DayData;
import de.hgu.gsehen.gsbalance.DayDataCalculation;
import de.hgu.gsehen.model.WeatherDataSource;
import java.io.File;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.Invocable;
import javax.script.ScriptEngine;

public class PluginUtil {
  private Gsehen gsehenInstance;

  {
    gsehenInstance = Gsehen.getInstance();
  }

  private static final Logger LOGGER = Logger.getLogger(DayDataCalculation.class.getName());

  public static final String JS_RESOURCE_FOLDER = "/de/hgu/gsehen/js";
  public static final String PLUGINS_FOLDER = JS_RESOURCE_FOLDER + "/plugins";

  private static final String WEATHER_DATA_JS = JS_RESOURCE_FOLDER + "/weatherData.js";

  @SuppressWarnings("checkstyle:javadocmethod")
  public static String[] getPluginJsFileNames() {
    return CollectionUtil.mapArrayValues(
        new File(Gsehen.class.getResource(PLUGINS_FOLDER).getPath()).listFiles(),
        String.class, file -> file.getName()
    );
  }

  /**
   * Recalculates today's day data.
   */
  @SuppressWarnings({"checkstyle:rightcurly"})
  public void recalculateDayData() {
    final ScriptEngine engine = evaluateJsResource(WEATHER_DATA_JS);
    final Date today = DateUtil.truncToDay(new Date());

    for (WeatherDataSource weatherDataSource : gsehenInstance.getWeatherDataSources()) {
      DayData dayData = null;
      try {
        dayData = (DayData) ((Invocable) engine).invokeFunction("determineDayData",
            weatherDataSource, today);
      } catch (Exception e) {
        LOGGER.log(Level.SEVERE, "Error when running 'determineDayData' in " + WEATHER_DATA_JS, e);
      }
      LOGGER.log(Level.INFO, "Weather data import from '" + weatherDataSource.getName() + "' was "
          + (dayData == null ? "NOT " : "") + "successful");
      gsehenInstance.sendDayDataChanged(dayData, weatherDataSource, null);
    }
  }
}
