package de.hgu.gsehen.model;

import de.hgu.gsehen.gui.GeoPolygon;
import java.util.UUID;


public abstract class Drawable {
  private final String uuid;

  public String getUuid() {
    return uuid;
  }

  public abstract void setNameAndPolygon(String name, GeoPolygon polygon);
  
  public abstract void setName(String name);

  public abstract String getName();

  public abstract GeoPolygon getPolygon();

  public Drawable() {
    uuid = UUID.randomUUID().toString();
  }
}
