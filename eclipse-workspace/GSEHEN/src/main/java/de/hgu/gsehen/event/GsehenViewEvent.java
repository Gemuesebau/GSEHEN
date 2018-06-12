package de.hgu.gsehen.event;

import de.hgu.gsehen.gui.GeoPoint;
import de.hgu.gsehen.util.Pair;

public class GsehenViewEvent extends GsehenEvent {

  private Pair<GeoPoint> viewport;

  public Pair<GeoPoint> getViewport() {
    return viewport;
  }

  public void setViewport(Pair<GeoPoint> viewport) {
    this.viewport = viewport;
  }
}
