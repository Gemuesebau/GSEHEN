package de.hgu.gsehen.gui.controller;

import de.hgu.gsehen.Gsehen;
import de.hgu.gsehen.event.FarmDataChanged;
import de.hgu.gsehen.event.GsehenEventListener;
import de.hgu.gsehen.gui.GeoPoint;
import de.hgu.gsehen.gui.GeoPolygon;
import de.hgu.gsehen.gui.PolygonData;
import de.hgu.gsehen.gui.view.NodeGestures;
import de.hgu.gsehen.model.Drawable;
import de.hgu.gsehen.model.DrawableParent;
import de.hgu.gsehen.model.Farm;
import de.hgu.gsehen.model.Field;
import de.hgu.gsehen.model.Plot;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;

/**
 * The GSEHEN Main-Controller.
 *
 * @author CWI
 */
@SuppressWarnings({"checkstyle:commentsindentation", "rawtypes"})
public class MainController implements Initializable, ChangeListener,
    GsehenEventListener<FarmDataChanged> {
  {
    Gsehen gsehenInstance = Gsehen.getInstance();
    //gsehenInstance.setMainController(this);
    gsehenInstance.registerForEvent(FarmDataChanged.class, this);
  }

  @SuppressWarnings("unused")
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
  private BorderPane farmViewBorderPane;
  @FXML
  private HBox farmViewTopHBox;
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
  private Pane farmViewPane;
  private SubScene subScene;

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
  @SuppressWarnings("unchecked")
  @FXML
  protected void enterFarmView() {
    farmViewPane = new Pane();
    farmViewPane.setStyle("-fx-background-color: #394c77;"); // nur zur �bersicht.

    subScene = new SubScene(farmViewPane, 950, 400, true, SceneAntialiasing.BALANCED);
    farmViewBorderPane.setCenter(subScene);
    farmLabel.getScene().widthProperty().addListener(this);
    farmLabel.getScene().heightProperty().addListener(this);

    int width = (int) (farmViewPane.getWidth() * 0.75); // 75% from parent
    int height = (int) (farmViewPane.getHeight() * 0.75); // 75% from parent

    canvas.setWidth(width);
    canvas.setHeight(height);

    gc = canvas.getGraphicsContext2D();
    
    // TODO rausrefactorn. Muss auch durch die handle(FarmDataChanged) hervorgerufen werden können.
    // s.u. .....
    setTransformation(gc, width, height, polygons);
    drawShapes(gc, polygons);

    if (!farmViewPane.getChildren().contains(canvas)) {
      farmViewPane.getChildren().add(canvas);
      canvas.layoutXProperty()
          .bind(farmViewPane.widthProperty().subtract(canvas.widthProperty()).divide(2));
      canvas.layoutYProperty()
          .bind(farmViewPane.heightProperty().subtract(canvas.heightProperty()).divide(2));
      this.zoom();
    }

    // create sample nodes which can be dragged
    nodeGestures = new NodeGestures(canvas);
    canvas.addEventFilter(MouseEvent.MOUSE_PRESSED, nodeGestures.getOnMousePressedEventHandler());
    canvas.addEventFilter(MouseEvent.MOUSE_DRAGGED, nodeGestures.getOnMouseDraggedEventHandler());

    farmPieChart = pieChart;
    farmPieChart.setTitle("Anbau");
    farmPieChart.setLegendSide(Side.RIGHT);
    if (!farmViewTopHBox.getChildren().contains(farmPieChart)) {
      farmViewTopHBox.getChildren().addAll(farmPieChart);
    }

    // TODO Bislang nur ein kleiner Test. Generell aber nicht verkehrt, wenn man die GeoPoints
    // mittels Label anzeigen lassen wuerde.
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

  /**
   * Zoom for canvas in the farmViewPane.
   */
  public void zoom() {

    farmViewPane.setOnScroll(event -> {
      double zoomFactor = 1.3;
      double deltaY = event.getDeltaY();

      if (deltaY < 0) {
        zoomFactor = 2.0 - zoomFactor;
      }

      Scale newScale = new Scale();
      newScale.setPivotX(event.getX());
      newScale.setPivotY(event.getY());
      newScale.setX(canvas.getScaleX() * zoomFactor);
      newScale.setY(canvas.getScaleY() * zoomFactor);

      canvas.getTransforms().add(newScale);

      event.consume();
    });
  }

  /**
   * ChangeListener: Resizing the SubScene in the FarmView.
   */
  public void changed(ObservableValue observable, Object oldValue, Object newValue) {
    double width = farmLabel.getScene().getWidth();
    double height = farmLabel.getScene().getHeight();
    if (observable.equals(farmLabel.getScene().widthProperty())) {
      double scale = width / 1200;
      subScene.setWidth(950 * scale);
    } else if (observable.equals(farmLabel.getScene().heightProperty())) {
      double scale = height / 800;
      subScene.setHeight(400 * scale);
    }
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
      result.add(drawable.getPolygon());
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

//  /**
//   * Called by others to tell an instance of this class that a new object has been created.
//   *
//   * @param object the newly created object, e.g. a Farm, Field, or Plot
//   */
//  public void objectAdded(NamedPolygonHolder object) {
//    LOGGER.info("Neues Objekt: " + object.getClass().getSimpleName() + " '" + object.getName()
//        + "' mit Polygon " + object.getPolygon().getGeoPoints());
//  }

  @Override
  public void initialize(URL arg0, ResourceBundle arg1) {
    // FIXME klären woher dies kommt und ob/wie es ausimplementiert werden muss
  }

  @Override
  public void handle(FarmDataChanged event) {
    List<Farm> farms = event.getFarms();
    Drawable[] farmsArray = new Drawable[farms.size()];
    int i = 0;
    for (Farm farm : farms) {
      farmsArray[i++] = farm;
    }
    @SuppressWarnings("unused")
    GeoPolygon[] polygons2 = extractPolygons(farmsArray); // TODO verarbeiten!!
  }
}
