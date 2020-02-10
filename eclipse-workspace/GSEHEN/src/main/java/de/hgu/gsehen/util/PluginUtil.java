package de.hgu.gsehen.util;

import static de.hgu.gsehen.util.DateUtil.truncToDay;
import static de.hgu.gsehen.util.MessageUtil.logException;
import static de.hgu.gsehen.util.MessageUtil.logMessage;
import static de.hgu.gsehen.util.TextFileUtil.evaluateJavaScriptFile;

import de.hgu.gsehen.Gsehen;
import de.hgu.gsehen.evapotranspiration.DayData;
import de.hgu.gsehen.model.WeatherDataPlugin;
import de.hgu.gsehen.model.WeatherDataSource;
import java.io.File;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javax.script.Invocable;
import javax.script.ScriptEngine;

public class PluginUtil {
  private static final Logger LOGGER = Logger.getLogger(PluginUtil.class.getName());
  private static final String PLUGINS_FOLDER =
      new File(Gsehen.class.getResource("/de/hgu/gsehen/js/plugins").getPath()).getAbsolutePath();
  private static final String USER_PLUGINS_FOLDER = Gsehen.getPluginsFolder().getAbsolutePath();

  /**
   * Returns a String array of decorated plug-in names.
   *
   * <p>The plug-in names are the pure JavaScript file names (w/o their paths),
   * but each prefixed by two characters indicating whether a plug-in is "built-in", i.e., delivered
   * with the GSEHEN application installation package.</p>
   *
   * @return the marked plug-in file names array
   */
  public static String[] getPluginJsFileNames() {
    return CollectionUtil.mapArrayValues(String.class,
        file -> "" + (file.getParent().equals(PLUGINS_FOLDER) ? (char)9670 : ' ')
            + ' ' + file.getName(),
        getJsFiles(PLUGINS_FOLDER),
        getJsFiles(USER_PLUGINS_FOLDER)
    );
  }

  private static File[] getJsFiles(final String absoluteFolderName) {
    return new File(absoluteFolderName).listFiles((a, b) -> b.endsWith(".js"));
  }

  /**
   * Performs an automatic import, for all registered weather data sources.
   *
   * @param gsehenInstance the Gsehen singleton instance
   */
  @SuppressWarnings({ "checkstyle:rightcurly" })
  public static void automaticImport(Gsehen gsehenInstance) {
    for (WeatherDataSource weatherDataSource : gsehenInstance.getWeatherDataSources()) {
      automaticImport(gsehenInstance, weatherDataSource);
    }
  }

  private static void automaticImport(Gsehen gsehenInstance, WeatherDataSource weatherDataSource) {
    final String pluginJsFileName = weatherDataSource.getPluginJsFileName();
    try {
      Double nextCheckMillis = getPlugin(pluginJsFileName, WeatherDataPlugin.class)
          .getNextCheckMillis(weatherDataSource);
      if (nextCheckMillis == null) {
        if (weatherDataSource.isAutomaticImportActive()) {
          updateDayData(gsehenInstance, weatherDataSource);
        }
        nextCheckMillis = 1000.0 * 60.0 * 5.0; // after import, wait 5 minutes
      }
      Timeline automaticImporter =
          new Timeline(new KeyFrame(Duration.millis(nextCheckMillis), event -> {
            automaticImport(gsehenInstance, weatherDataSource);
          }));
      automaticImporter.setCycleCount(1);
      automaticImporter.play();
    } catch (Exception e) {
      logException(LOGGER, Level.SEVERE, e, "wd.plugin.error.det.daydata", pluginJsFileName);
    }
  }

  /**
   * Recalculates today's day data, for all registered weather data sources.
   *
   * @param gsehenInstance the Gsehen singleton instance
   */
  @SuppressWarnings({ "checkstyle:rightcurly" })
  public static void updateDayData(Gsehen gsehenInstance) {
    int detDaydataExceptionCount = 0;
    for (WeatherDataSource weatherDataSource : gsehenInstance.getWeatherDataSources()) {
      if (weatherDataSource.isManualImportActive()
          && updateDayData(gsehenInstance, weatherDataSource)) {
        detDaydataExceptionCount++;
      }
    }
    if (detDaydataExceptionCount > 0) {
      throw new RuntimeException(MessageFormat.format(
          gsehenInstance.getBundle().getString("wd.import.exception"),
          detDaydataExceptionCount,
          detDaydataExceptionCount == 1 ? 0 : 1
      ));
    }
  }

  private static boolean updateDayData(Gsehen gsehenInstance, WeatherDataSource weatherDataSource) {
    boolean threwException = false;
    List<DayData> dayData = null;
    final String pluginJsFileName = weatherDataSource.getPluginJsFileName();
    try {
      dayData = getPlugin(pluginJsFileName, WeatherDataPlugin.class)
          .determineDayData(weatherDataSource, truncToDay(new Date()) /* currently unused */);
    } catch (Exception e) {
      threwException = true;
      logException(LOGGER, Level.SEVERE, e, "wd.plugin.error.det.daydata", pluginJsFileName);
    }
    logMessage(LOGGER, Level.INFO, "wd.import.result", weatherDataSource.getName(),
        dayData == null ? 1 : 0);
    if (dayData != null) {
      dayData.sort((a, b) -> a.getDate().compareTo(b.getDate()));
      CollectionUtil.eliminateDuplicates(dayData, d -> truncToDay(d.getDate()));
      gsehenInstance.sendDayDataChanged(dayData, weatherDataSource, null);
    }
    return threwException;
  }

  @SuppressWarnings({"unchecked", "checkstyle:javadocmethod"})
  public static <T> T getPlugin(String pluginJsFileName, Class<T> pluginClass) {
    final ScriptEngine engine = evaluateJavaScriptFile(pluginJsFileName,
        PLUGINS_FOLDER, USER_PLUGINS_FOLDER);
    try {
      return (T)((Invocable)engine).invokeFunction("getPlugin");
    } catch (Exception e) {
      logException(LOGGER, Level.SEVERE, e, "get.plugin.error", pluginJsFileName);
      throw new RuntimeException("Error when getting plugin '" + pluginJsFileName + "'", e);
    }
  }
}
