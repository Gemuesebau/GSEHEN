package de.hgu.gsehen.gui.controller;

import de.hgu.gsehen.Gsehen;
import de.hgu.gsehen.gui.GeoPoint;
import de.hgu.gsehen.gui.GeoPolygon;
import de.hgu.gsehen.gui.PolygonData;
import de.hgu.gsehen.gui.view.AnimatedZoomOperator;
import de.hgu.gsehen.gui.view.NodeGestures;
import de.hgu.gsehen.model.Drawable;
import de.hgu.gsehen.model.DrawableParent;
import de.hgu.gsehen.model.Farm;
import de.hgu.gsehen.model.Field;
import de.hgu.gsehen.model.NamedPolygonHolder;
import de.hgu.gsehen.model.Plot;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
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
  {
    Gsehen.getInstance().setMainController(this);
  }

  private static final Logger LOGGER = Logger.getLogger(MainController.class.getName());

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

  private Canvas canvas = new Canvas();
  private DoubleProperty scale;
  private NodeGestures nodeGestures;
  private GeoPolygon[] polygons = extractPolygons(buildFarm());
  private GraphicsContext gc;
  // TODO Ist das sinnvoll, oder wird's dadurch zu voll?
  private ObservableList<PieChart.Data> pieChartData =
      FXCollections.observableArrayList(new PieChart.Data("Bananen", 13),
          new PieChart.Data("Weizen", 25), new PieChart.Data("Kartoffeln", 10),
          new PieChart.Data("frei", 22), new PieChart.Data("Mais", 30));
  private PieChart pieChart = new PieChart(pieChartData);
  private String labelText;

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
    tabPane.getTabs().addAll(mapViewTab, farmViewTab, fieldViewTab, fieldPlotViewTab);
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

  // TODO Mit den neuen Daten (18.04.) experimentieren.
  @FXML
  protected void enterFarmView() {
    int width = (int) (farmViewPane.getWidth() * 0.75); // 75% from parent
    int height = (int) (farmViewPane.getHeight() * 0.75); // 75% from parent

    canvas.setWidth(width);
    canvas.setHeight(height);
    scale = new SimpleDoubleProperty(1.0);
    canvas.scaleXProperty().bindBidirectional(scale);
    canvas.scaleYProperty().bindBidirectional(scale);

    gc = canvas.getGraphicsContext2D();
    setTransformation(gc, width, height, polygons);
    drawShapes(gc, polygons);

    if (!farmViewPane.getChildren().contains(canvas)) {
      farmViewPane.getChildren().addAll(canvas);
    }

    // Create operator
    AnimatedZoomOperator zoomOperator = new AnimatedZoomOperator();

    // Listen to scroll events
    farmViewPane.setOnScroll(new EventHandler<ScrollEvent>() {
      @Override
      public void handle(ScrollEvent event) {
        double zoomFactor = 2.0;
        if (event.getDeltaY() <= 0) {
          // zoom out
          zoomFactor = 1 / zoomFactor;
        }
        zoomOperator.zoom(farmViewPane, zoomFactor, event.getSceneX(), event.getSceneY());
      }
    });

    // create sample nodes which can be dragged
    nodeGestures = new NodeGestures(canvas);
    canvas.addEventFilter(MouseEvent.MOUSE_PRESSED, nodeGestures.getOnMousePressedEventHandler());
    canvas.addEventFilter(MouseEvent.MOUSE_DRAGGED, nodeGestures.getOnMouseDraggedEventHandler());

    // TODO: Aktuell noch ein "Hack", damit Canvas hinter'm Rest bleibt.
    farmViewPane.toBack();

    farmPieChart = pieChart;
    farmPieChart.setTitle("Anbau");
    farmPieChart.setLegendSide(Side.RIGHT);
    if (!farmViewTopHBox.getChildren().contains(farmPieChart)) {
      farmViewTopHBox.getChildren().addAll(farmPieChart);
    }

    // TODO Bislang nur ein kleiner Test. Generell aber nicht verkehrt, wenn man die GeoPoints
    // mittels Label anzeigen lassen würde.
    for (GeoPolygon polygon : polygons) {
      labelText = "";
      for (GeoPoint geoPoint : polygon.getGeoPoints()) {
        labelText += "GeoPoint Lat: " + geoPoint.getLat() + "; GeoPoint Lng: " + geoPoint.getLng()
            + "\n" + "\n";
      }
      farmLabel.setText(labelText);
    }
    farmLabel.setWrapText(true);
  }

  @SuppressWarnings("checkstyle:linelength")
  private Farm buildFarm() {
    Farm farm = new Farm("Meine kleine Farm", new GeoPolygon(new GeoPoint(52.2, 10.5),
        new GeoPoint(52.5, 10.5), new GeoPoint(52.4, 10.1)));
    farm.setFields(
        new Field("Beilagenfeld",
            new GeoPolygon(new GeoPoint(52, 10), new GeoPoint(52, 11), new GeoPoint(54, 10),
                new GeoPoint(54, 11)),
            new Plot("Kartoffelacker",
                new GeoPolygon(new GeoPoint(52.2, 10.5), new GeoPoint(52.5, 10.5),
                    new GeoPoint(52.4, 10.1))),
            new Plot("Pastinakenfleckerl",
                new GeoPolygon(new GeoPoint(53.2, 10.5), new GeoPoint(53.5, 10.5),
                    new GeoPoint(53.4, 10.1)))),
        new Field("Buntesfeld",
            new GeoPolygon(new GeoPoint(52, 11), new GeoPoint(53, 12), new GeoPoint(52, 12)),
            new Plot("Erbsenkamp", new GeoPolygon(new GeoPoint(52.2, 11.5),
                new GeoPoint(52.5, 11.5), new GeoPoint(52.4, 11.1)))));
    return farm;
  }

  private GeoPolygon[] extractPolygons(Drawable... drawables) {
    List<GeoPolygon> result = new ArrayList<>();
    extractPolygonsImpl(result, drawables);
    return result.toArray(new GeoPolygon[0]);
  }

  private void extractPolygonsImpl(List<GeoPolygon> result, Drawable... drawables) {
    for (Drawable drawable : drawables) {
      result.addAll(drawable.getPolygons());
      if (drawable instanceof DrawableParent) {
        ((DrawableParent) drawable)
            .forAllChildDrawables(drawableChild -> extractPolygonsImpl(result, drawableChild));
      }
    }
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

  /**
   * Called by others to tell an instance of this class that a new object has been created.
   *
   * @param object the newly created object, e.g. a Farm, Field, or Plot
   */
  public void objectAdded(NamedPolygonHolder object) {
    object.getName();     // Bsp.
    object.getPolygons(); // Bsp.
    LOGGER.info("Neues Objekt: " + object.getClass().getSimpleName()
        + " '" + object.getName() + "' mit Polygon "
        + object.getPolygons().get(0).getGeoPoints());
  }
}
