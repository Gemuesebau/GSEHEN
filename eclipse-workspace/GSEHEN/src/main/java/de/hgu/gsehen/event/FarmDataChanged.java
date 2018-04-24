package de.hgu.gsehen.event;

import de.hgu.gsehen.model.Farm;

import java.util.List;

public class FarmDataChanged extends GsehenEvent {

  private List<Farm> farms;

  public List<Farm> getFarms() {
    return farms;
  }

  public void setFarms(List<Farm> farms) {
    this.farms = farms;
  }
}
