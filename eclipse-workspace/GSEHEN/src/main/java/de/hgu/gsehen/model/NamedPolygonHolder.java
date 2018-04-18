package de.hgu.gsehen.model;

import de.hgu.gsehen.gui.GeoPolygon;

public interface NamedPolygonHolder {

  public void setNameAndPolygon(String name, GeoPolygon polygon);

  public String getName();

  public GeoPolygon getPolygon();
}
