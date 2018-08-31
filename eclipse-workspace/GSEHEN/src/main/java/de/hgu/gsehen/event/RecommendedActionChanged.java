package de.hgu.gsehen.event;

import de.hgu.gsehen.model.Plot;

public class RecommendedActionChanged extends GsehenEvent {
  private Plot plot;

  public Plot getPlot() {
    return plot;
  }

  public void setPlot(Plot plot) {
    this.plot = plot;
  }
}
