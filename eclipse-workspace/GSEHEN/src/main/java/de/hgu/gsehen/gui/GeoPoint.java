package de.hgu.gsehen.gui;

/**
 * This class represents a point in a geographic two-dimensional coordinate system.
 *
 * @author AT
 */
public class GeoPoint {
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
}
