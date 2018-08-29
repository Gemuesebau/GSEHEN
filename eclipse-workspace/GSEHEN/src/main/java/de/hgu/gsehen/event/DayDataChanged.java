package de.hgu.gsehen.event;

import de.hgu.gsehen.evapotranspiration.DayData;

public class DayDataChanged extends GsehenEvent {
  private DayData dayData;

  public DayData getDayData() {
    return dayData;
  }

  public void setDayData(DayData dayData) {
    this.dayData = dayData;
  }
}
