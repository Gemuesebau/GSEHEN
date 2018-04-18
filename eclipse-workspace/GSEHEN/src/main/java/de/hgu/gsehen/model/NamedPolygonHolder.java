package de.hgu.gsehen.model;

import de.hgu.gsehen.gui.GeoPolygon;

import java.util.List;

public interface NamedPolygonHolder {

  public void setNameAndPolygon(String name, GeoPolygon polygon);

  public String getName();

  public List<GeoPolygon> getPolygons();
}
