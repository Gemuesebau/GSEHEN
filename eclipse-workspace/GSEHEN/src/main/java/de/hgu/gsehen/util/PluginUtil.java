package de.hgu.gsehen.util;

import static de.hgu.gsehen.util.TextResourceUtil.evaluateJsResource;

import de.hgu.gsehen.Gsehen;
import de.hgu.gsehen.evapotranspiration.DayData;
import de.hgu.gsehen.model.WeatherDataPlugin;
import de.hgu.gsehen.model.WeatherDataSource;
import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.Invocable;
import javax.script.ScriptEngine;

public class PluginUtil {
  private Gsehen gsehenInstance;

  {
    gsehenInstance = Gsehen.getInstance();
  }

  private static final Logger LOGGER = Logger.getLogger(PluginUtil.class.getName());

  public static final String JS_RESOURCE_FOLDER = "/de/hgu/gsehen/js";
  public static final String PLUGINS_FOLDER = JS_RESOURCE_FOLDER + "/plugins";

  @SuppressWarnings("checkstyle:javadocmethod")
  public static String[] getPluginJsFileNames() {
    return CollectionUtil.mapArrayValues(
        new File(Gsehen.class.getResource(PLUGINS_FOLDER).getPath()).listFiles(
            (a, b) -> b.endsWith(".js")
            ),
        String.class, file -> file.getName()
    );
  }

  /**
   * Recalculates today's day data, for all registered weather data sources.
   */
  @SuppressWarnings({"checkstyle:rightcurly"})
  public void recalculateDayData() {
    final Date today = DateUtil.truncToDay(new Date());
    for (WeatherDataSource weatherDataSource : gsehenInstance.getWeatherDataSources()) {
      List<DayData> dayData = null;
      final String pluginJsFileName = weatherDataSource.getPluginJsFileName();
      try {
        dayData = PluginUtil.getPlugin(
            pluginJsFileName,
            WeatherDataPlugin.class
        ).determineDayData(weatherDataSource, today);
      } catch (Exception e) {
        LOGGER.log(Level.SEVERE, "Error when running 'determineDayData' in " + pluginJsFileName, e);
      }
      LOGGER.log(Level.INFO, "Weather data import from '" + weatherDataSource.getName() + "' was "
          + (dayData == null ? "NOT " : "") + "successful");
      gsehenInstance.sendDayDataChanged(dayData, weatherDataSource, null);
    }
  }

  @SuppressWarnings({"unchecked", "checkstyle:javadocmethod"})
  public static <T> T getPlugin(String pluginJsFileName, Class<T> pluginClass) {
    final ScriptEngine engine = evaluateJsResource(PLUGINS_FOLDER + "/" + pluginJsFileName);
    try {
      return (T) ((Invocable) engine).invokeFunction("getPlugin");
    } catch (Exception e) {
      final String errorMessage = "Error when getting plugin '" + pluginJsFileName + "'";
      LOGGER.log(Level.SEVERE, errorMessage, e);
      throw new RuntimeException(errorMessage, e);
    }
  }
}
