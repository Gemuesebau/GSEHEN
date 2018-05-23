package de.hgu.gsehen.model;

import de.hgu.gsehen.gui.GeoPolygon;

public interface Drawable {

  public void setNameAndPolygon(String name, GeoPolygon polygon);

  public String getName();

  public GeoPolygon getPolygon();

  public GeoPolygon getPolygonByName(String name);
}
