package de.hgu.gsehen.gsbalance;

import de.hgu.gsehen.Gsehen;
import de.hgu.gsehen.evapotranspiration.DayData;
import de.hgu.gsehen.model.WeatherDataSource;
import de.hgu.gsehen.util.DateUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class DayDataCalculation {
  private Gsehen gsehenInstance;

  {
    gsehenInstance = Gsehen.getInstance();
  }

  private static final Logger LOGGER = Logger.getLogger(DayDataCalculation.class.getName());
  private static final String WEATHER_DATA_JS = "/de/hgu/gsehen/js/weatherData.js";

  public static InputStreamReader getReaderForUtf8(String resourceName) throws IOException {
    return new InputStreamReader(Gsehen.class.getResourceAsStream(resourceName), "utf-8");
  }

  /**
   * Reads the contents of a given utf-8-encoded resource as one String.
   *
   * @param resourceName
   *          the name of the resource to read
   * @return a String containing the given resource's contents
   * @throws IOException
   *           if the resource can't be read (as utf-8)
   */
  public static String getUtf8ResourceAsOneString(String resourceName) throws IOException {
    try (BufferedReader buffer = new BufferedReader(getReaderForUtf8(resourceName))) {
      return buffer.lines().collect(Collectors.joining("\n"));
    }
  }

  /**
   * Recalculates today's day data.
   */
  @SuppressWarnings({"checkstyle:rightcurly"})
  public void recalculateDayData() {
    final ScriptEngine engine = prepareScriptEngine();
    final Date today = DateUtil.truncToDay(new Date());
    for (WeatherDataSource weatherDataSource : gsehenInstance.getWeatherDataSources()) {
      DayData dayData = null;
      try {
        dayData = (DayData)((Invocable)engine)
            .invokeFunction("determineDayData", weatherDataSource, today);
      }
      catch (Exception e) {
        LOGGER.log(Level.SEVERE, "Error when running 'determineDayData' in " + WEATHER_DATA_JS, e);
      }
      LOGGER.log(Level.INFO, "Weather data import from '" + weatherDataSource.getName() + "' was "
          + (dayData == null ? "NOT " : "") + "successful");
      gsehenInstance.sendDayDataChanged(dayData, weatherDataSource, null);
    }
  }

  private ScriptEngine prepareScriptEngine() {
    final ScriptEngine engine = new ScriptEngineManager().getEngineByExtension("js");
    engine.put("LOGGER", LOGGER);
    try {
      engine.eval(getReaderForUtf8(WEATHER_DATA_JS));
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Can't evaluate " + WEATHER_DATA_JS, e);
    }
    return engine;
  }
}
