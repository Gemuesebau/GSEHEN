package de.hgu.gsehen.evapotranspiration;

import de.hgu.gsehen.Gsehen;
import de.hgu.gsehen.gui.GeoPoint;
import de.hgu.gsehen.gui.GeoPolygon;
import de.hgu.gsehen.gui.PolygonData;
import java.sql.SQLException;
import javafx.application.Application;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;
import org.junit.BeforeClass;
import org.junit.jupiter.api.Test;

class PolygonTest {

  @BeforeClass
  public static void before() throws SQLException, InterruptedException {
    Thread thread = new Thread("JavaFX Init Thread") {
      public void run() {
        Application.launch(Gsehen.class);
      }
    };
    thread.setDaemon(true);
    thread.start();
    Thread.sleep(5000);
  }

  @Test
  void test() {
    int width = 300;
    int height = 300;
    Canvas canvas = new Canvas(width, height);
    GraphicsContext gc = canvas.getGraphicsContext2D();
    GeoPolygon[] polygons = {
        new GeoPolygon(new GeoPoint(52.2, 10.5), new GeoPoint(52.5, 10.5),
            new GeoPoint(52.4, 10.1)),
        new GeoPolygon(new GeoPoint(53.2, 10.5), new GeoPoint(53.5, 10.5),
            new GeoPoint(53.4, 10.1)),
        new GeoPolygon(new GeoPoint(52.2, 11.5), new GeoPoint(52.5, 11.5),
            new GeoPoint(52.4, 11.1))};
    setTransformation(gc, width, height, polygons);
    drawShapes(gc, polygons);
  }

  private void drawShapes(GraphicsContext gc, GeoPolygon... polygons) {
    gc.setStroke(Color.WHITE);
    gc.setFill(Color.WHEAT);
    for (GeoPolygon polygon : polygons) {
      PolygonData polygonData = polygon.getPolygonData();
      gc.fillPolygon(polygonData.getPointsX(), polygonData.getPointsY(),
          polygonData.getPointsCount());
    }
  }

  private void setTransformation(GraphicsContext gc, int widthPx, int heightPx,
      GeoPolygon... polygons) {
    if (polygons == null || polygons.length == 0) {
      throw new IllegalArgumentException("at least one polygon must be given");
    }
    GeoPolygon g = polygons[0];
    double minX = g.getMinX();
    double maxX = g.getMaxX();
    double minY = g.getMinY();
    double maxY = g.getMaxY();
    for (int i = 1; i < polygons.length; i++) {
      g = polygons[i];
      double compare = g.getMinX();
      if (compare < minX) {
        minX = compare;
      }
      compare = g.getMaxX();
      if (compare < maxX) {
        maxX = compare;
      }
      compare = g.getMinY();
      if (compare < maxX) {
        minY = compare;
      }
      compare = g.getMaxY();
      if (compare < maxX) {
        maxY = compare;
      }
    }
    setTransformation(gc, widthPx, heightPx, minX, maxX, minY, maxY);
  }

  @SuppressWarnings({"checkstyle:rightcurly"})
  private void setTransformation(GraphicsContext gc, int widthPx, int heightPx, double minX,
      double maxX, double minY, double maxY) {
    double rangeX = maxX - minX;
    double rangeY = maxY - minY;
    Affine affineTransformation = new Affine();
    double ratioX = rangeX / widthPx;
    double ratioY = rangeY / heightPx;
    affineTransformation.appendTranslation(minX, minY);
    affineTransformation.appendScale(ratioX, ratioY);
    try {
      affineTransformation.invert();
    } catch (NonInvertibleTransformException e) {
      throw new RuntimeException(e);
    }
    gc.setTransform(affineTransformation);
  }
}
