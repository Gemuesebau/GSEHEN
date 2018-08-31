package de.hgu.gsehen.event;

import de.hgu.gsehen.evapotranspiration.DayData;
import de.hgu.gsehen.model.WeatherDataSource;

public class DayDataChanged extends GsehenEvent {
  private DayData dayData;

  public DayData getDayData() {
    return dayData;
  }

  public void setDayData(DayData dayData) {
    this.dayData = dayData;
  }

  /**
   * Determines whether this event originates from the given weather data source.
   *
   * @param fieldWeatherDataSource the weather data source to check
   * @return true if this event originates from the given source, false otherwise
   */
  public boolean isFromWeatherDataSource(WeatherDataSource fieldWeatherDataSource) {
    // FIXME add persistent property "weatherDataSource", and implement this correctly,
    //  e.g. using a correct "equals" implementation!
    return true;
  }
}
