package de.hgu.gsehen.evapotranspiration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.hgu.gsehen.gui.GeoPoint;
import de.hgu.gsehen.gui.GeoPolygon;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;
import org.junit.jupiter.api.Test;

class PolygonTest {

  @Test
  void test() {
    GeoPolygon[] polygons = {
        new GeoPolygon(new GeoPoint(52.2, 10.5), new GeoPoint(52.5, 10.5),
            new GeoPoint(52.4, 10.1)),
        new GeoPolygon(new GeoPoint(53.2, 10.5), new GeoPoint(53.5, 10.5),
            new GeoPoint(53.4, 10.1)),
        new GeoPolygon(new GeoPoint(52.2, 11.5), new GeoPoint(52.5, 11.5),
            new GeoPoint(52.4, 11.1))};
    setTransformation(polygons);
  }

  private void setTransformation(GeoPolygon... polygons) {
    if (polygons == null || polygons.length == 0) {
      throw new IllegalArgumentException("at least one polygon must be given");
    }
    GeoPolygon g = polygons[0];
    double minX = g.getMinX();
    double maxX = g.getMaxX();
    double minY = g.getMinY();
    double maxY = g.getMaxY();
    for (int i = 0; i < polygons.length; i++) {
      g = polygons[i];
      double compare = g.getMinX();
      if (compare < minX) {
        minX = compare;
      }
      compare = g.getMaxX();
      if (compare > maxX) {
        maxX = compare;
      }
      compare = g.getMinY();
      if (compare < maxX) {
        minY = compare;
      }
      compare = g.getMaxY();
      if (compare > maxY) {
        maxY = compare;
      }
    }
    assertEquals(10.1, minX, 0.1);
    assertEquals(52.2, minY, 0.1);
    assertEquals(11.5, maxX, 0.1);
    assertEquals(53.5, maxY, 0.1);
    setTransformation(minX, maxX, minY, maxY);
  }

  @SuppressWarnings({"checkstyle:rightcurly"})
  private void setTransformation(double minX, double maxX, double minY, double maxY) {
    double rangeX = maxX - minX;
    double rangeY = maxY - minY;
    Affine affineTransformation = new Affine();
    double ratioX = rangeX / 300;
    double ratioY = rangeY / 300;
    affineTransformation.appendTranslation(minX, minY);
    affineTransformation.appendScale(ratioX, ratioY);
    try {
      affineTransformation.invert();
    } catch (NonInvertibleTransformException e) {
      throw new RuntimeException(e);
    }
  }
}
