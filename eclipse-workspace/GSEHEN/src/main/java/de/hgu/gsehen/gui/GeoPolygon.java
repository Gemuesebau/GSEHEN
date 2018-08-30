package de.hgu.gsehen.gui;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
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
  public double calculateArea() {
    double distM = 0.0;
    for (int i = 1; i <= geoPoints.size(); i++) {
      double latMid = (getMinX() + getMaxX()) / 2.0;
      double meterLat =
          111132.954 - 559.822 * Math.cos(2.0 * latMid) + 1.175 * Math.cos(4.0 * latMid);
      double meterLon = (3.14159265359 / 180) * 6367449 * Math.cos(latMid);

      double deltaLat = Math.abs(getMinX() - getMaxX());
      double deltaLon = Math.abs(getMinY() - getMaxY());

      distM = Math.sqrt(Math.pow(deltaLat * meterLat, 2) + Math.pow(deltaLon * meterLon, 2));

      System.out.println(distM);
    }
    // return distM; // meters
    // TODO: Herausfinden, ob das Ergebnis stimmt.
    return 0.0;
  }
}
