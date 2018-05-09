package de.hgu.gsehen.gui.controller;

import de.hgu.gsehen.Gsehen;
import de.hgu.gsehen.event.FarmDataChanged;
import de.hgu.gsehen.event.GsehenEventListener;
import de.hgu.gsehen.gui.GeoPoint;
import de.hgu.gsehen.gui.GeoPolygon;
import de.hgu.gsehen.gui.PolygonData;
import de.hgu.gsehen.model.Drawable;
import de.hgu.gsehen.model.DrawableParent;
import de.hgu.gsehen.model.Farm;
import de.hgu.gsehen.model.Field;
import de.hgu.gsehen.model.Plot;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.scene.transform.Transform;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

/**
 * The GSEHEN Main-Controller.
 *
 * @author CWI
 */
@SuppressWarnings({"checkstyle:commentsindentation"})
public class MainController implements GsehenEventListener<FarmDataChanged> {
  private Gsehen gsehenInstance;

  {
    gsehenInstance = Gsehen.getInstance();
    // gsehenInstance.setMainController(this);
    //gsehenInstance.registerForEvent(FarmDataChanged.class, this);
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
  private Tab aboutViewTab;
  @FXML
  private WebView contactWebView;
  @FXML
  private WebView aboutWebView;
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
  private MenuItem aboutUsMenuItem;

  private Boolean change = false;
  private Canvas canvas = new Canvas();
  private ObservableList<PieChart.Data> pieChartData =
      FXCollections.observableArrayList(new PieChart.Data("Bananen", 13),
          new PieChart.Data("Weizen", 25), new PieChart.Data("Kartoffeln", 10),
          new PieChart.Data("frei", 22), new PieChart.Data("Mais", 30));
  private PieChart pieChart = new PieChart(pieChartData);
  private BorderPane imageBorderPane = new BorderPane();
  private ImageView farmImageView = new ImageView();
  private WritableImage canvasImage;
  private Drawable[] farmsArray;
  private Drawable[] flatDrawables;
  private List<Farm> farms;
  private GraphicsContext gc;
  private String labelText;
  private HBox zoom;
  private Slider zoomLvl;
  private Slider horScroll;
  private Slider verScroll;
  private static int height;
  private static int width;
  private static double initx;
  private static double inity;
  private static double offSetX;
  private static double offSetY;
  private static double zoomlvl;

  @FXML
  private void about(ActionEvent a) {
    accordion.setVisible(false);
    tabPane.getTabs().clear();
    tabPane.getTabs().add(aboutViewTab);
    WebEngine engine = aboutWebView.getEngine();
    engine.load(
        "https://www.hs-geisenheim.de/forschung/institute/gemuesebau/ueberblick-institut-fuer-gemuesebau/bewaesserung/ble-gsehen/");

    // if the URL does not contain "https://www.hs-geisenheim.de" skip back
    engine.locationProperty().addListener((observable, oldValue, newValue) -> {
      if (!newValue.contains("https://www.hs-geisenheim.de")) {
        Platform.runLater(() -> {
          engine.load(
              "https://www.hs-geisenheim.de/forschung/institute/gemuesebau/ueberblick-institut-fuer-gemuesebau/bewaesserung/ble-gsehen/");
        });
      }
    });
  }

  @FXML
  private void openContactView(ActionEvent o) {
    accordion.setVisible(false);
    tabPane.getTabs().clear();
    tabPane.getTabs().add(contactViewTab);
    WebEngine engine = contactWebView.getEngine();
    engine.load("https://www.hs-geisenheim.de/personen/person/231/");

    // if the URL does not contain "https://www.hs-geisenheim.de" skip back
    engine.locationProperty().addListener((observable, oldValue, newValue) -> {
      if (!newValue.contains("https://www.hs-geisenheim.de")) {
        Platform.runLater(() -> {
          engine.load("https://www.hs-geisenheim.de/personen/person/231/");
        });
      }
    });
  }

  // Returns to Main-Menu.
  @FXML
  private void backToMainView(ActionEvent b) {
    accordion.setVisible(true);
    tabPane.getTabs().clear();
    tabPane.getTabs().addAll(mapViewTab, farmViewTab, fieldViewTab, fieldPlotViewTab);
  }

  // // Opens a new Stage.
  // @FXML
  // private void about(ActionEvent a) {
  // Stage stage = new Stage();
  // Scene scene = new Scene(new VBox(), 400, 400);
  // stage.setTitle("About us");
  // stage.setScene(scene);
  // stage.show();
  // }

  @FXML
  private void enterFarmView() {
    farmViewBorderPane.setCenter(imageBorderPane);
    farmViewBorderPane.widthProperty().addListener(observable -> redraw());
    farmViewBorderPane.heightProperty().addListener(observable -> redraw());

    if (canvasImage != null) {
      farmImageView.setImage(canvasImage);
    }

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

    zoom = new HBox(10);
    zoom.setAlignment(Pos.CENTER);

    zoomLvl = new Slider();
    zoomLvl.setMax(4);
    zoomLvl.setMin(1);
    zoomLvl.setMaxWidth(400);
    zoomLvl.setMinWidth(400);
    Label hint = new Label("Zoom Level");
    Label value = new Label("1.0");

    zoom.getChildren().addAll(hint, zoomLvl, value);

    horScroll = new Slider();
    horScroll.setMin(0);
    horScroll.setMax(width);
    verScroll = new Slider();
    verScroll.setMin(0);
    verScroll.setMax(height);

    horScroll.valueProperty().addListener(e -> {
      offSetX = horScroll.getValue();
      zoomlvl = zoomLvl.getValue();
      double newValue = (double) ((int) (zoomlvl * 10)) / 10;
      value.setText(newValue + "");
      if (offSetX < (width / newValue) / 2) {
        offSetX = (width / newValue) / 2;
      }
      if (offSetX > width - ((width / newValue) / 2)) {
        offSetX = width - ((width / newValue) / 2);
      }

      farmImageView.setViewport(new Rectangle2D(offSetX - ((width / newValue) / 2),
          offSetY - ((height / newValue) / 2), width / newValue, height / newValue));
    });

    verScroll.valueProperty().addListener(e -> {
      offSetY = height - verScroll.getValue();
      zoomlvl = zoomLvl.getValue();
      double newValue = (double) ((int) (zoomlvl * 10)) / 10;
      value.setText(newValue + "");
      if (offSetY < (height / newValue) / 2) {
        offSetY = (height / newValue) / 2;
      }
      if (offSetY > height - ((height / newValue) / 2)) {
        offSetY = height - ((height / newValue) / 2);
      }
      farmImageView.setViewport(new Rectangle2D(offSetX - ((width / newValue) / 2),
          offSetY - ((height / newValue) / 2), width / newValue, height / newValue));
    });

    imageBorderPane.prefWidthProperty().bind(farmLabel.getScene().widthProperty().divide(1.6));
    imageBorderPane.prefHeightProperty().bind(farmLabel.getScene().heightProperty().divide(1.6));
    imageBorderPane.setCenter(farmImageView);
    imageBorderPane.setBottom(zoom);

    zoomLvl.valueProperty().addListener(e -> {
      zoomlvl = zoomLvl.getValue();
      double newValue = (double) ((int) (zoomlvl * 10)) / 10;
      value.setText(newValue + "");
      if (offSetX < (width / newValue) / 2) {
        offSetX = (width / newValue) / 2;
      }
      if (offSetX > width - ((width / newValue) / 2)) {
        offSetX = width - ((width / newValue) / 2);
      }
      if (offSetY < (height / newValue) / 2) {
        offSetY = (height / newValue) / 2;
      }
      if (offSetY > height - ((height / newValue) / 2)) {
        offSetY = height - ((height / newValue) / 2);
      }
      horScroll.setValue(offSetX);
      verScroll.setValue(height - offSetY);
      farmImageView.setViewport(new Rectangle2D(offSetX - ((width / newValue) / 2),
          offSetY - ((height / newValue) / 2), width / newValue, height / newValue));
    });
    imageBorderPane.setCursor(Cursor.OPEN_HAND);
    farmImageView.setOnMousePressed(e -> {
      initx = e.getSceneX();
      inity = e.getSceneY();
      imageBorderPane.setCursor(Cursor.CLOSED_HAND);
    });
    farmImageView.setOnMouseReleased(e -> {
      imageBorderPane.setCursor(Cursor.OPEN_HAND);
    });
    farmImageView.setOnMouseDragged(e -> {
      horScroll.setValue(horScroll.getValue() + (initx - e.getSceneX()));
      verScroll.setValue(verScroll.getValue() - (inity - e.getSceneY()));
      initx = e.getSceneX();
      inity = e.getSceneY();
    });
    farmImageView.setOnScroll(e -> {
      double zoomValue = zoomLvl.getValue();
      double deltaY = e.getDeltaY();
      if (deltaY > 0) {
        zoomLvl.setValue(zoomValue += 0.1);
      } else {
        zoomLvl.setValue(zoomValue -= 0.1);
      }
    });
  }

  private void redraw() {
    width = (int) (imageBorderPane.getPrefWidth());
    height = (int) (imageBorderPane.getPrefHeight());

    farmImageView.setFitWidth(width);
    farmImageView.setFitHeight(height);

    canvas.setWidth(width);
    canvas.setHeight(height);

    gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    setTransformation(gc, width, height, flatDrawables);
    LOGGER.log(Level.CONFIG, "redraw(): calling 'drawShapes'");
    drawShapes(gc, flatDrawables);

    farmImageView.setImage(canvasImage);
  }

  /**
   * Converts a canvas in a picture.
   * 
   * @param canvas - The Canvas.
   * @param pixelScale == 1.0.
   * @return - Picture of the canvas.
   */
  public static WritableImage pixelScaleAwareCanvasSnapshot(Canvas canvas, double pixelScale) {
    WritableImage writableImage =
        new WritableImage((int) Math.rint((pixelScale * canvas.getWidth()) * 1.5),
            (int) Math.rint((pixelScale * canvas.getHeight()) * 1.5));
    SnapshotParameters spa = new SnapshotParameters();
    spa.setTransform(Transform.scale(pixelScale, pixelScale));
    return canvas.snapshot(spa, writableImage);
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

    farmImageView.setFitWidth(950);
    farmImageView.setFitHeight(400);
    farmImageView.setPreserveRatio(true);

    width = (int) (farmImageView.getFitWidth());
    height = (int) (farmImageView.getFitHeight());

    canvas.setWidth(width);
    canvas.setHeight(height);

    if (flatDrawables == null) {
      flatDrawables = extractPolygons(farmsArray);
    } else {
      flatDrawables = extractPolygons(farmsArray);
      change = true;
    }
    gc = canvas.getGraphicsContext2D();
    setTransformation(gc, width, height, flatDrawables);
    LOGGER.log(Level.CONFIG, "handle(): calling 'drawShapes'");
    drawShapes(gc, flatDrawables);

    canvasImage = pixelScaleAwareCanvasSnapshot(canvas, 1.0);
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

  /**
   * Save & Exit Event.
   */
  public void exit() {
    if (change) {
      Button button1 = new Button("Ja");
      button1.setStyle("-fx-font: 14 arial;");
      Button button2 = new Button("Nein");
      button2.setStyle("-fx-font: 14 arial;");
      Button button3 = new Button("Abbrechen");
      button3.setStyle("-fx-font: 14 arial;");

      button1.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent e) {
          gsehenInstance.saveUserData();
          Platform.exit();
          System.exit(0);
        }
      });

      button2.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent e) {
          Platform.exit();
          System.exit(0);
        }
      });

      Stage stage = new Stage();

      button3.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent e) {
          stage.close();
        }
      });

      Label label = new Label("Wollen Sie Ihre Daten speichern,\nbevor Sie das Programm beenden?");
      label.setStyle("-fx-font: 16 arial;");
      HBox horBox = new HBox();
      horBox.setSpacing(10);
      horBox.getChildren().addAll(button1, button2, button3);

      BorderPane borderPane = new BorderPane();
      borderPane.setTop(label);
      borderPane.setBottom(horBox);

      Scene scene = new Scene(borderPane, 260, 100);
      stage.setTitle("Speichern & beenden?");
      stage.setScene(scene);
      stage.show();
    } else {
      Platform.exit();
    }
  }
}
