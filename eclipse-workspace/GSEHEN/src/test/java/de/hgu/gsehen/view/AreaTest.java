package de.hgu.gsehen.view;

import java.util.List;

import org.junit.jupiter.api.Test;

import de.hgu.gsehen.gui.GeoPoint;
import de.hgu.gsehen.gui.GeoPolygon;

public class AreaTest {

  @Test
  void calculateMeters() {

    /*
     * #1
     */
    double lon1 = 7.962164212303151;
    double lat1 = 49.98440351548787;

    double lon2 = 7.960372496681202;
    double lat2 = 49.98376884011384;

    double lon3 = 7.960855294303883;
    double lat3 = 49.98322384044712;

    double lon4 = 7.962872315483082;
    double lat4 = 49.98385852301169;

    double oneTwo = measure(lat1, lon1, lat2, lon2);
    double twoThree = measure(lat2, lon2, lat3, lon3);
    double threeFour = measure(lat3, lon3, lat4, lon4);
    double fourOne = measure(lat4, lon4, lat1, lon1);

    System.out.println("m²: " + oneTwo + " * " + twoThree + " * " + threeFour + " * " + fourOne
        + " = " + oneTwo * twoThree * threeFour * fourOne);

    System.out.println("m²: " + ((oneTwo + threeFour) / 2) + " * " + ((twoThree + fourOne) / 2)
        + " = " + ((oneTwo + threeFour) / 2) * ((twoThree + fourOne) / 2));

    System.out.println("");

    /*
     * #2
     */
    double xPos1 = (lon1) * (6378137 * Math.PI / 180) * Math.cos(lat1 * Math.PI / 180);
    double yPos1 = (lat1) * (Math.toRadians(6378137));

    double xPos2 = (lon2) * (6378137 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180);
    double yPos2 = (lat2) * (Math.toRadians(6378137));

    double xPos3 = (lon3) * (6378137 * Math.PI / 180) * Math.cos(lat3 * Math.PI / 180);
    double yPos3 = (lat3) * (Math.toRadians(6378137));

    double xPos4 = (lon4) * (6378137 * Math.PI / 180) * Math.cos(lat4 * Math.PI / 180);
    double yPos4 = (lat4) * (Math.toRadians(6378137));

    double area = 0.5 * (xPos1 * yPos2 - xPos2 * yPos1 + xPos2 * yPos3 - xPos3 * yPos2
        + xPos3 * yPos4 - xPos4 * yPos3 + xPos4 * yPos1 - xPos1 * yPos4);
    System.out.println(area);

    System.out.println("");

    /*
     * #3
     */
    GeoPoint geoPoint1 = new GeoPoint(lat1, lon1);
    GeoPoint geoPoint2 = new GeoPoint(lat2, lon2);
    GeoPoint geoPoint3 = new GeoPoint(lat3, lon3);
    GeoPoint geoPoint4 = new GeoPoint(lat4, lon4);
    GeoPolygon geoPolygon = new GeoPolygon(geoPoint1, geoPoint2, geoPoint3, geoPoint4);

    System.out.println("Area: " + area(geoPolygon.getGeoPoints()));

    System.out.println("");

    /*
     * #4
     */
    System.out.println(calculatePolygonArea(geoPolygon.getGeoPoints()));

    System.out.println("");
  }

  // generally used geo measurement function
  double measure(double lat1, double lon1, double lat2, double lon2) {
    double R = 6378.137; // Radius of earth in KM
    double dLat = lat2 * Math.PI / 180 - lat1 * Math.PI / 180;
    double dLon = lon2 * Math.PI / 180 - lon1 * Math.PI / 180;
    double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(lat1 * Math.PI / 180)
        * Math.cos(lat2 * Math.PI / 180) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    double d = R * c;
    return d * 1000; // meters
  }

  double area(List<GeoPoint> vertices) {
    int n = vertices.size();
    double sum = 0;
    for (int i = 0; i < n; i++) {
      sum += vertices.get(i).getLat()
          * ((vertices.get((i + 1) % n)).getLng() - vertices.get((i + n - 1) % n).getLng());
      System.out.println(sum);
    }
    double area = 0.5 * Math.abs(sum);
    return area;
  }

  private static double calculatePolygonArea(List<GeoPoint> coordinates) {
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
        area += convertToRadian(p2.getLng() - p1.getLng())
            * (2 + Math.sin(convertToRadian(p1.getLat())) + Math.sin(convertToRadian(p2.getLat())));
        System.out.println(i + ": " + area);
      }
      area = area * 6378137 * 6378137 / 2;
    }
    return Math.abs(area);
  }

  private static double convertToRadian(double input) {
    return input * Math.PI / 180;
  }

}
