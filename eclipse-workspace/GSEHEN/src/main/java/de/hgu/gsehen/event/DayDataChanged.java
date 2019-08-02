package de.hgu.gsehen.event;

import de.hgu.gsehen.evapotranspiration.DayData;
import de.hgu.gsehen.model.WeatherDataSource;
import java.util.List;

public class DayDataChanged extends GsehenEvent {
  private List<DayData> dayData;
  private WeatherDataSource weatherDataSource;

  public List<DayData> getDayData() {
    return dayData;
  }

  public void setDayData(List<DayData> dayData) {
    this.dayData = dayData;
  }

  public WeatherDataSource getWeatherDataSource() {
    return weatherDataSource;
  }

  public void setWeatherDataSource(WeatherDataSource weatherDataSource) {
    this.weatherDataSource = weatherDataSource;
  }

  /**
   * Determines whether this event originates from the given weather data source.
   *
   * @param weatherDataSourceUuid the weather data source to check
   * @return true if this event originates from the given source, false otherwise
   */
  public boolean isFromWeatherDataSource(String weatherDataSourceUuid) {
    return weatherDataSourceUuid != null
        && weatherDataSourceUuid.equals(weatherDataSource.getUuid());
  }
}
