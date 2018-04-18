package de.hgu.gsehen.gui;

import java.util.ArrayList;
import java.util.List;

public class GeoPolygon {

  private List<GeoPoint> geoPoints;

  public GeoPolygon() {
    geoPoints = new ArrayList<>();
  }

  public void addGeoPoint(GeoPoint geoPoint) {
    geoPoints.add(geoPoint);
  }

  public void addGeoPointByCoords(double lat, double lng) {
    geoPoints.add(new GeoPoint(lat, lng));
  }

  /**
   * Constructs from a variable amount of GeoPoint instances.
   *
   * @param geoPoints the points to be added to this polygon
   */
  public GeoPolygon(GeoPoint... geoPoints) {
    this();
    for (GeoPoint geoPoint : geoPoints) {
      this.geoPoints.add(geoPoint);
    }
  }

  public List<GeoPoint> getGeoPoints() {
    return geoPoints;
  }

  public void setGeoPoints(List<GeoPoint> geoPoints) {
    this.geoPoints = geoPoints;
  }

  /**
   * Conversion-getter.
   *
   * @return this GeoPolygon's point data in a format suitable for Java2D
   */
  public PolygonData getPolygonData() {
    double[] pointsX = new double[geoPoints.size()];
    double[] pointsY = new double[geoPoints.size()];
    for (int i = 0; i < geoPoints.size(); i++) {
      GeoPoint geoPoint = geoPoints.get(i);
      pointsX[i] = geoPoint.getLng();
      pointsY[i] = geoPoint.getLat();
    }
    return new PolygonData(pointsX, pointsY);
  }

  /**
   * Calculates the minimum x value of all of this polygon's points.
   *
   * @return the calculated value
   */
  public double getMinX() {
    if (geoPoints.isEmpty()) {
      throw new IllegalArgumentException("the polygon must have at least one point");
    }
    double result = geoPoints.get(0).getLng();
    for (int i = 1; i < geoPoints.size(); i++) {
      double compare = geoPoints.get(i).getLng();
      if (compare < result) {
        result = compare;
      }
    }
    return result;
  }

  /**
   * Calculates the minimum y value of all of this polygon's points.
   *
   * @return the calculated value
   */
  public double getMinY() {
    if (geoPoints.isEmpty()) {
      throw new IllegalArgumentException("the polygon must have at least one point");
    }
    double result = geoPoints.get(0).getLat();
    for (int i = 1; i < geoPoints.size(); i++) {
      double compare = geoPoints.get(i).getLat();
      if (compare < result) {
        result = compare;
      }
    }
    return result;
  }

  /**
   * Calculates the maximum x value of all of this polygon's points.
   *
   * @return the calculated value
   */
  public double getMaxX() {
    if (geoPoints.isEmpty()) {
      throw new IllegalArgumentException("the polygon must have at least one point");
    }
    double result = geoPoints.get(0).getLng();
    for (int i = 1; i < geoPoints.size(); i++) {
      double compare = geoPoints.get(i).getLng();
      if (compare > result) {
        result = compare;
      }
    }
    return result;
  }

  /**
   * Calculates the maximum y value of all of this polygon's points.
   *
   * @return the calculated value
   */
  public double getMaxY() {
    if (geoPoints.isEmpty()) {
      throw new IllegalArgumentException("the polygon must have at least one point");
    }
    double result = geoPoints.get(0).getLat();
    for (int i = 1; i < geoPoints.size(); i++) {
      double compare = geoPoints.get(i).getLat();
      if (compare > result) {
        result = compare;
      }
    }
    return result;
  }
}
