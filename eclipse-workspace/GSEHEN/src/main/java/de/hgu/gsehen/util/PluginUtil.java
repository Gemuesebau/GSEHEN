package de.hgu.gsehen.util;

import static de.hgu.gsehen.util.MessageUtil.logException;
import static de.hgu.gsehen.util.MessageUtil.logMessage;
import static de.hgu.gsehen.util.TextResourceUtil.evaluateJsResource;

import de.hgu.gsehen.Gsehen;
import de.hgu.gsehen.evapotranspiration.DayData;
import de.hgu.gsehen.model.WeatherDataPlugin;
import de.hgu.gsehen.model.WeatherDataSource;
import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
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
   *
   * @param beforeImport logic to execute before import - is called with the WDS UUID as parameter
   */
  @SuppressWarnings({"checkstyle:rightcurly"})
  public void recalculateDayData(Consumer<String> beforeImport) {
    final Date today = DateUtil.truncToDay(new Date());
    for (WeatherDataSource weatherDataSource : gsehenInstance.getWeatherDataSources()) {
      List<DayData> dayData = null;
      final String pluginJsFileName = weatherDataSource.getPluginJsFileName();
      try {
        dayData = PluginUtil.getPlugin(
            pluginJsFileName,
            WeatherDataPlugin.class
        ).determineDayData(weatherDataSource, today, wds -> beforeImport.accept(wds.getUuid()));
      } catch (Exception e) {
        logException(LOGGER, Level.SEVERE, e, "wd.plugin.error.det.daydata", pluginJsFileName);
      }
      logMessage(LOGGER, Level.INFO, "wd.import.result", weatherDataSource.getName(),
          dayData == null ? 1 : 0);
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
