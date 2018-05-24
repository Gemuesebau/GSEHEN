package de.hgu.gsehen.model;

import de.hgu.gsehen.gui.GeoPolygon;

public interface Drawable {

  public void setNameAndPolygon(String name, GeoPolygon polygon);
  
  public void setName(String name);

  public String getName();

  public GeoPolygon getPolygon();

}
