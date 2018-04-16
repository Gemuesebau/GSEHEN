package de.hgu.gsehen.gui.controller;

import de.hgu.gsehen.gui.GeoPoint;
import de.hgu.gsehen.gui.GeoPolygon;
import de.hgu.gsehen.gui.PolygonData;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.stage.Stage;

/**
 * The GSEHEN Main-Controller.
 * 
 * @author CWI
 *
 */
public class MainController {

  // Views
  @FXML
  private Accordion accordion;
  @FXML
  private TabPane tabPane;
  @FXML
  private Tab mapViewTab;
  @FXML
  private Tab farmViewTab;
  @FXML
  private Tab fieldViewTab;
  @FXML
  private Tab fieldPlotViewTab;
  @FXML
  private Tab contactViewTab;
  @FXML
  private HBox farmViewTopHBox;
  @FXML
  private Pane farmViewPane;
  @FXML
  private PieChart farmPieChart;
  @FXML
  private Label farmLabel;

  // Help-Menu
  @FXML
  private MenuItem contactMenuItem;
  @FXML
  private Button contactBack;
  @FXML
  private MenuItem aboutUsMenuItem;

  // Hides the Accordion and the tabs in the TabPane.
  @FXML
  protected void openContactView(ActionEvent o) {
    accordion.setVisible(false);
    tabPane.getTabs().clear();
    tabPane.getTabs().add(contactViewTab);
  }

  // Returns to Main-Menu.
  @FXML
  protected void backFromContactView(ActionEvent b) {
    accordion.setVisible(true);
    tabPane.getTabs().clear();
    tabPane.getTabs().add(mapViewTab);
    tabPane.getTabs().add(farmViewTab);
    tabPane.getTabs().add(fieldViewTab);
    tabPane.getTabs().add(fieldPlotViewTab);
  }

  // Opens a new Stage.
  @FXML
  protected void about(ActionEvent a) {
    Stage stage = new Stage();
    Scene scene = new Scene(new VBox(), 400, 400);
    stage.setTitle("About us");
    stage.setScene(scene);
    stage.show();
  }

  // TODO Aktuell hardcoded Zeugs (Polygon(!) und PieChart(?)).
  @FXML
  protected void enterFarmView(Event d) {
    int width = (int) (farmViewPane.getWidth() * 0.95); // 95% from parent
    int height = (int) (farmViewPane.getHeight() * 0.95); // 95% from parent
    Canvas canvas = new Canvas(width, height);
    GeoPolygon[] polygons = {
        new GeoPolygon(new GeoPoint(52.2, 10.5), new GeoPoint(52.5, 10.5),
            new GeoPoint(52.4, 10.1)),
        new GeoPolygon(new GeoPoint(53.2, 10.5), new GeoPoint(53.5, 10.5),
            new GeoPoint(53.4, 10.1)),
        new GeoPolygon(new GeoPoint(52.2, 11.5), new GeoPoint(52.5, 11.5),
            new GeoPoint(52.4, 11.1))};
    GraphicsContext gc = canvas.getGraphicsContext2D();
    setTransformation(gc, width, height, polygons);
    drawShapes(gc, polygons);
    farmViewPane.getChildren().addAll(canvas);

    // TODO Ist das sinnvoll, oder wird's dadurch zu voll?
    ObservableList<PieChart.Data> pieChartData =
        FXCollections.observableArrayList(new PieChart.Data("Bananen", 13),
            new PieChart.Data("Weizen", 25), new PieChart.Data("Kartoffeln", 10),
            new PieChart.Data("frei", 22), new PieChart.Data("Mais", 30));
    PieChart pieChart = new PieChart(pieChartData);
    farmPieChart = pieChart;
    farmPieChart.setTitle("Anbau");
    farmPieChart.setLegendSide(Side.RIGHT);
    farmViewTopHBox.getChildren().addAll(farmPieChart);

    // TODO Bislang nur ein kleiner Test. Generell aber nicht verkehrt, wenn man die GeoPoints
    // mittels Label anzeigen lassen würde.
    for (GeoPolygon polygon : polygons) {
      String labelText = "";
      for (GeoPoint geoPoint : polygon.getGeoPoints()) {
        labelText += "GeoPoint Lat: " + geoPoint.getLat() + "; GeoPoint Lng: " + geoPoint.getLng()
            + "\n" + "\n";
      }
      farmLabel.setText(labelText);
    }
    farmLabel.setWrapText(true);
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
