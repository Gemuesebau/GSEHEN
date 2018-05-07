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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.ContextMenuEvent;
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
public class MainController_new implements ChangeListener, GsehenEventListener<FarmDataChanged> {
  private Gsehen gsehenInstance;

  {
    gsehenInstance = Gsehen.getInstance();
    // gsehenInstance.setMainController(this);
    gsehenInstance.registerForEvent(FarmDataChanged.class, this);
  }

  private static final Logger LOGGER = Logger.getLogger(MainController_new.class.getName());

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
  @FXML
  private Button show;

  // Help-Menu
  @FXML
  private MenuItem contactMenuItem;
  @FXML
  private Button contactBack;
  @FXML
  private MenuItem aboutUsMenuItem;

  private Canvas canvas = new Canvas();
  private ObservableList<PieChart.Data> pieChartData =
      FXCollections.observableArrayList(new PieChart.Data("Bananen", 13),
          new PieChart.Data("Weizen", 25), new PieChart.Data("Kartoffeln", 10),
          new PieChart.Data("frei", 22), new PieChart.Data("Mais", 30));
  private PieChart pieChart = new PieChart(pieChartData);
  private Pane farmViewPane = new Pane();
  private SubScene subScene;
  private NodeGestures nodeGestures;
  private Drawable[] farmsArray;
  private Drawable[] flatDrawables;
  private List<Farm> farms;
  private GraphicsContext gc;
  private String labelText;
  private static int height;
  private static int width;

  // Hides the Accordion and the tabs in the TabPane.
  @FXML
  private void openContactView(ActionEvent o) {
    accordion.setVisible(false);
    tabPane.getTabs().clear();
    tabPane.getTabs().add(contactViewTab);
  }

  // Returns to Main-Menu.
  @FXML
  private void backFromContactView(ActionEvent b) {
    accordion.setVisible(true);
    tabPane.getTabs().clear();
    tabPane.getTabs().addAll(mapViewTab, farmViewTab, fieldViewTab, fieldPlotViewTab);
  }

  // Opens a new Stage.
  @FXML
  private void about(ActionEvent a) {
    Stage stage = new Stage();
    Scene scene = new Scene(new VBox(), 400, 400);
    stage.setTitle("About us");
    stage.setScene(scene);
    stage.show();
  }

  @SuppressWarnings("unchecked")
  @FXML
  private void enterFarmView() {
    farmViewPane = new Pane();
    farmViewPane.setStyle("-fx-background-color: #394c77;"); // nur zur Übersicht.

    subScene = new SubScene(farmViewPane, 950, 400, true, SceneAntialiasing.BALANCED);
    farmViewBorderPane.setCenter(subScene);
    farmLabel.getScene().widthProperty().addListener(this);
    farmLabel.getScene().heightProperty().addListener(this);

    canvas.layoutXProperty()
        .bind(farmViewPane.widthProperty().subtract(canvas.widthProperty()).divide(2));
    canvas.layoutYProperty()
        .bind(farmViewPane.heightProperty().subtract(canvas.heightProperty()).divide(2));

    // create sample nodes which can be dragged
    nodeGestures = new NodeGestures(canvas);
    canvas.addEventFilter(MouseEvent.MOUSE_PRESSED, nodeGestures.getOnMousePressedEventHandler());
    canvas.addEventFilter(MouseEvent.MOUSE_DRAGGED, nodeGestures.getOnMouseDraggedEventHandler());

    farmViewPane.setOnScroll(e -> {
      double zoomFactor = 1.3;
      double deltaY = e.getDeltaY();

      if (deltaY < 0) {
        zoomFactor = 2.0 - zoomFactor;
      }

      Scale newScale = new Scale();
      newScale.setPivotX(e.getX());
      newScale.setPivotY(e.getY());
      newScale.setX(canvas.getScaleX() * zoomFactor);
      newScale.setY(canvas.getScaleY() * zoomFactor);

      canvas.getTransforms().add(newScale);

      e.consume();
    });

    // Create ContextMenu
    ContextMenu contextMenu = new ContextMenu();
    MenuItem item1 = new MenuItem("Reset");
    item1.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        redraw();
      }
    });

    // Add MenuItem to ContextMenu
    contextMenu.getItems().addAll(item1);

    // When user right-click on SubScene
    farmViewPane.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
      @Override
      public void handle(ContextMenuEvent event) {
        contextMenu.show(farmViewPane, event.getScreenX(), event.getScreenY());
      }
    });

    farmPieChart = pieChart;
    farmPieChart.setTitle("Anbau");
    farmPieChart.setLegendSide(Side.RIGHT);
    if (!farmViewTopHBox.getChildren().contains(farmPieChart)) {
      farmViewTopHBox.getChildren().addAll(farmPieChart);
    }

    if (flatDrawables != null) {
      for (Drawable drawable : flatDrawables) {
        for (Farm farm : farms) {
          labelText = farm.getClass().getSimpleName() + ": '" + farm.getName() + "' \n" + "\n";
        }
        for (GeoPoint geoPoint : drawable.getPolygon().getGeoPoints()) {
          labelText += "GeoPoint Lat: " + geoPoint.getLat() + "; GeoPoint Lng: " + geoPoint.getLng()
              + "\n" + "\n";
        }
        farmLabel.setText(labelText);
      }
      farmLabel.setWrapText(true);
    }
  }

  private void redraw() {
    int width = (int) (farmViewPane.getWidth() * 0.75); // 75% from parent
    int height = (int) (farmViewPane.getHeight() * 0.75); // 75% from parent

    canvas.setWidth(width);
    canvas.setHeight(height);

    gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    setTransformation(gc, width, height, flatDrawables);
    LOGGER.log(Level.CONFIG, "redraw(): calling 'drawShapes'");
    drawShapes(gc, flatDrawables);

    farmViewPane.getChildren().add(canvas);
  }

  private Drawable[] extractPolygons(Drawable... drawables) {
    List<Drawable> result = new ArrayList<>();
    extractPolygonsImpl(result, drawables);
    return result.toArray(new Drawable[0]);
  }

  private void extractPolygonsImpl(List<Drawable> result, Drawable... drawables) {
    for (Drawable drawable : drawables) {
      result.add(drawable);
      if (drawable instanceof DrawableParent) {
        ((DrawableParent) drawable)
            .forAllChildDrawables(drawableChild -> extractPolygonsImpl(result, drawableChild));
      }
    }
  }

  private void drawShapes(GraphicsContext gc, Drawable... drawables) {
    gc.setStroke(Color.WHITE);
    LOGGER.log(Level.CONFIG, "Starting to draw polygons ...");
    for (Drawable drawable : drawables) {
      if (drawable instanceof Farm) {
        gc.setFill(Color.BLACK);
      } else if (drawable instanceof Field) {
        gc.setFill(Color.BLUE);
      } else if (drawable instanceof Plot) {
        gc.setFill(Color.ORANGE); // TODO depending on water balance!!
      } else {
        gc.setFill(Color.WHEAT);
      }
      PolygonData polygonData = drawable.getPolygon().getPolygonData();
      gc.fillPolygon(polygonData.getPointsX(), polygonData.getPointsY(),
          polygonData.getPointsCount());
      LOGGER.log(Level.CONFIG, "Polygon drawn: " + drawable.getPolygon().getGeoPoints());
      // LOGGER.info("Polygon drawn: " + polygon.getGeoPoints());
    }
  }

  private void setTransformation(GraphicsContext gc, int widthPx, int heightPx,
      Drawable... polygons) {
    if (polygons == null || polygons.length == 0) {
      throw new IllegalArgumentException("at least one polygon must be given");
    }
    GeoPolygon g = polygons[0].getPolygon();
    double minX = g.getMinX();
    double maxX = g.getMaxX();
    double minY = g.getMinY();
    double maxY = g.getMaxY();
    for (int i = 1; i < polygons.length; i++) {
      g = polygons[i].getPolygon();
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

  // /**
  // * Called by others to tell an instance of this class that a new object has been created.
  // *
  // * @param object the newly created object, e.g. a Farm, Field, or Plot
  // */
  // public void objectAdded(NamedPolygonHolder object) {
  // LOGGER.info("Neues Objekt: " + object.getClass().getSimpleName() + " '" + object.getName()
  // + "' mit Polygon " + object.getPolygon().getGeoPoints());
  // }

  @Override
  public void handle(FarmDataChanged event) {
    farms = event.getFarms();
    farmsArray = new Drawable[farms.size()];
    int i = 0;
    for (Farm farm : farms) {
      farmsArray[i++] = farm;
    }

    width = (int) (farmViewPane.getWidth() * 0.75); // 75% from parent
    height = (int) (farmViewPane.getHeight() * 0.75); // 75% from parent

    canvas.setWidth(width);
    canvas.setHeight(height);

    flatDrawables = extractPolygons(farmsArray);
    gc = canvas.getGraphicsContext2D();
    setTransformation(gc, width, height, flatDrawables);
    LOGGER.log(Level.CONFIG, "handle(): calling 'drawShapes'");
    drawShapes(gc, flatDrawables);

    farmViewPane.getChildren().add(canvas);
  }

  /**
   * Loads the user-created data (farms, fields, plots, ..)
   */
  public void loadUserData() {
    gsehenInstance.loadUserData();
  }

  /**
   * Saves the user-created data (farms, fields, plots, ..)
   */
  public void saveUserData() {
    gsehenInstance.saveUserData();
  }

  @Override
  public void changed(ObservableValue observable, Object arg1, Object arg2) {
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
}
