package de.hgu.gsehen.event;

import de.hgu.gsehen.gui.GeoPoint;
import de.hgu.gsehen.model.Farm;
import de.hgu.gsehen.util.Pair;

import java.util.List;

public class FarmDataChanged extends GsehenEvent {
  private List<Farm> farms;
  private Pair<GeoPoint> viewport;

  public List<Farm> getFarms() {
    return farms;
  }

  public void setFarms(List<Farm> farms) {
    this.farms = farms;
  }

  public Pair<GeoPoint> getViewport() {
    return viewport;
  }

  public void setViewport(Pair<GeoPoint> viewport) {
    this.viewport = viewport;
  }
}
