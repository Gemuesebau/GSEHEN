package de.hgu.gsehen.util;

import static de.hgu.gsehen.util.MessageUtil.logException;
import static de.hgu.gsehen.util.MessageUtil.logMessage;
import static de.hgu.gsehen.util.TextFileUtil.evaluateJavaScriptFile;

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
  private Gsehen gsehenInstance = Gsehen.getInstance();

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
   * Recalculates today's day data, for all registered weather data sources.
   */
  @SuppressWarnings({ "checkstyle:rightcurly" })
  public void recalculateDayData() {
    final Date today = DateUtil.truncToDay(new Date());
    for (WeatherDataSource weatherDataSource : gsehenInstance.getWeatherDataSources()) {
      List<DayData> dayData = null;
      final String pluginJsFileName = weatherDataSource.getPluginJsFileName();
      Exception detDaydataException = null;
      try {
        dayData = PluginUtil.getPlugin(pluginJsFileName, WeatherDataPlugin.class)
            .determineDayData(weatherDataSource, today /* currently unused */);
      } catch (Exception e) {
        detDaydataException = e;
        logException(LOGGER, Level.SEVERE, e, "wd.plugin.error.det.daydata", pluginJsFileName);
      }
      logMessage(LOGGER, Level.INFO, "wd.import.result", weatherDataSource.getName(),
          dayData == null ? 1 : 0);
      if (dayData == null) {
        if (detDaydataException != null) {
          throw new RuntimeException(detDaydataException);
        } else {
          throw new RuntimeException("day data null");
        }
      }
      dayData.sort((a, b) -> a.getDate().compareTo(b.getDate()));
      CollectionUtil.eliminateDuplicates(dayData, d -> DateUtil.truncToDay(d.getDate()));
      gsehenInstance.sendDayDataChanged(dayData, weatherDataSource, null);
    }
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
