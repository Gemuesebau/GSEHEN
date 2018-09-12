package de.hgu.gsehen.gsbalance;

import static de.hgu.gsehen.util.TextResourceUtil.JS_RESOURCE_FOLDER;
import static de.hgu.gsehen.util.TextResourceUtil.evaluateJsResource;

import de.hgu.gsehen.Gsehen;
import de.hgu.gsehen.evapotranspiration.DayData;
import de.hgu.gsehen.model.WeatherDataSource;
import de.hgu.gsehen.util.DateUtil;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.Invocable;
import javax.script.ScriptEngine;

public class DayDataCalculation {
  private Gsehen gsehenInstance;

  {
    gsehenInstance = Gsehen.getInstance();
  }

  private static final Logger LOGGER = Logger.getLogger(DayDataCalculation.class.getName());
  private static final String WEATHER_DATA_JS = JS_RESOURCE_FOLDER + "/weatherData.js";

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
}
