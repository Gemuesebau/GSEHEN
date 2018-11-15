package de.hgu.gsehen.gui;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class GeoPolygon {

  private static final String POLYGON_MUST_HAVE_AT_LEAST_ONE_POINT = 
      "the polygon must have at least one point";

  @Id
  @GeneratedValue
  private int id;
  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
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
   * @param geoPoints
   *          the points to be added to this polygon
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
      throw new IllegalArgumentException(POLYGON_MUST_HAVE_AT_LEAST_ONE_POINT);
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
      throw new IllegalArgumentException(POLYGON_MUST_HAVE_AT_LEAST_ONE_POINT);
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
      throw new IllegalArgumentException(POLYGON_MUST_HAVE_AT_LEAST_ONE_POINT);
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
      throw new IllegalArgumentException(POLYGON_MUST_HAVE_AT_LEAST_ONE_POINT);
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

  /**
   * Calcutes the area of an object via GeoPoints.
   * 
   * @return - The calculeted area.
   */
  public double calculateArea(List<GeoPoint> coordinates) {
    double area = 0.0;
    if (coordinates.size() > 2) {
      for (int i = 0; i < coordinates.size(); i++) {
        GeoPoint p1 = coordinates.get(i);
        GeoPoint p2;
        if (coordinates.get(i) == coordinates.get(coordinates.size() - 1)) {
          p2 = coordinates.get(0);
        } else {
          p2 = coordinates.get(i + 1);
        }
        area += Math.toRadians(p2.getLng() - p1.getLng())
            * (2 + Math.sin(Math.toRadians(p1.getLat())) + Math.sin(Math.toRadians(p2.getLat())));
      }

      area = area * 6378137.0 * 6378137.0 / 2.0;
    }
    return Math.abs(area);
  }

  // public double polygonArea(double[] X, double[] Y, int numPoints) {
  // double area = 0; // Accumulates area in the loop
  // int j = numPoints - 1; // The last vertex is the 'previous' one to the first
  //
  // for (int i = 0; i < numPoints; i++) {
  // area = area + (X[j] + X[i]) * (Y[j] - Y[i]);
  // j = i; // j is previous vertex to i
  // // System.out.println(area / 2);
  // }
  // return area / 2;
  // }

}
