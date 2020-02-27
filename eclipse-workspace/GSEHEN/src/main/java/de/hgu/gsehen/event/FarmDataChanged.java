package de.hgu.gsehen.event;

import de.hgu.gsehen.model.Farm;
import de.hgu.gsehen.util.LoggingList;

public class FarmDataChanged extends GsehenViewEvent {
  private LoggingList<Farm> farms;

  public LoggingList<Farm> getFarms() {
    return farms;
  }

  public void setFarms(LoggingList<Farm> farms) {
    this.farms = farms;
  }
}
