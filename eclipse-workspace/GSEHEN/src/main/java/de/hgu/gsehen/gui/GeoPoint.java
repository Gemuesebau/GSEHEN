package de.hgu.gsehen.gui;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * This class represents a point in a geographic two-dimensional coordinate system.
 *
 * @author AT
 */
@Entity
public class GeoPoint {

  @Id
  @GeneratedValue
  private long id;
  
  private double lat;  // e.g. 52.2678302894808
  private double lng;  // e.g. 10.51279688354498

  public GeoPoint() {
    lat = 0;
    lng = 0;
  }

  public GeoPoint(double lat, double lng) {
    this.lat = lat;
    this.lng = lng;
  }

  public double getLat() {
    return lat;
  }

  public void setLat(double lat) {
    this.lat = lat;
  }

  public double getLng() {
    return lng;
  }

  public void setLng(double lng) {
    this.lng = lng;
  }

  @Override
  public String toString() {
    return "(" + lng + ", " + lat + ")";
  }
}
