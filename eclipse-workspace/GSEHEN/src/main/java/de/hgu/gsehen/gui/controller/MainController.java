package de.hgu.gsehen.gui.controller;

import de.hgu.gsehen.Gsehen;
import de.hgu.gsehen.event.FarmDataChanged;
import de.hgu.gsehen.event.GsehenEventListener;
import de.hgu.gsehen.gui.GeoPolygon;
import de.hgu.gsehen.gui.PolygonData;
import de.hgu.gsehen.model.Drawable;
import de.hgu.gsehen.model.DrawableParent;
import de.hgu.gsehen.model.Farm;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.scene.transform.Transform;
import javafx.stage.Stage;

/**
 * The GSEHEN Main-Controller.
 *
 * @author CWI
 */
@SuppressWarnings({"checkstyle:commentsindentation"})
public class MainController implements GsehenEventListener<FarmDataChanged> {
  {
    Gsehen gsehenInstance = Gsehen.getInstance();
    // gsehenInstance.setMainController(this);
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
  // private GeoPolygon[] polygons = extractPolygons(buildFarm());
  // TODO Ist das sinnvoll, oder wird's dadurch zu voll?
  private ObservableList<PieChart.Data> pieChartData =
      FXCollections.observableArrayList(new PieChart.Data("Bananen", 13),
          new PieChart.Data("Weizen", 25), new PieChart.Data("Kartoffeln", 10),
          new PieChart.Data("frei", 22), new PieChart.Data("Mais", 30));
  private PieChart pieChart = new PieChart(pieChartData);
  // private String labelText;
  private BorderPane imageView = new BorderPane();
  private ImageView farmImageView = new ImageView();
  private WritableImage canvasImage;
  private Drawable[] farmsArray;
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

  // TODO Mit den neuen Daten (18.04.) experimentieren.
  @FXML
  private void enterFarmView() {
    farmViewBorderPane.setCenter(imageView);
    drawCanvas();

    // farmPieChart = pieChart;
    // farmPieChart.setTitle("Anbau");
    // farmPieChart.setLegendSide(Side.RIGHT);
    // if (!farmViewTopHBox.getChildren().contains(farmPieChart)) {
    // farmViewTopHBox.getChildren().addAll(farmPieChart);
    // }

    // for (GeoPolygon polygon : polygons) {
    // labelText = "";
    // for (GeoPoint geoPoint : polygon.getGeoPoints()) {
    // labelText += "GeoPoint Lat: " + geoPoint.getLat() + "; GeoPoint Lng: " + geoPoint.getLng()
    // + "\n" + "\n";
    // }
    // farmLabel.setText(labelText);
    // }
    // farmLabel.setWrapText(true);
  }

  private void drawCanvas() {
    farmImageView.setImage(canvasImage);
    farmImageView.setPreserveRatio(true);
    farmImageView.setFitWidth(width);
    farmImageView.setFitHeight(height);

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

    imageView.setCenter(farmImageView);
    imageView.setBottom(zoom);

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
    imageView.setCursor(Cursor.OPEN_HAND);
    farmImageView.setOnMousePressed(e -> {
      initx = e.getSceneX();
      inity = e.getSceneY();
      imageView.setCursor(Cursor.CLOSED_HAND);
    });
    farmImageView.setOnMouseReleased(e -> {
      imageView.setCursor(Cursor.OPEN_HAND);
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

  /**
   * Converts a canvas in a picture.
   * 
   * @param canvas - The Canvas.
   * @param pixelScale == 1.0.
   * @return - Picture of the canvas.
   */
  public static WritableImage pixelScaleAwareCanvasSnapshot(Canvas canvas, double pixelScale) {
    WritableImage writableImage = new WritableImage((int) Math.rint(pixelScale * canvas.getWidth()),
        (int) Math.rint(pixelScale * canvas.getHeight()));
    SnapshotParameters spa = new SnapshotParameters();
    spa.setTransform(Transform.scale(pixelScale, pixelScale));
    return canvas.snapshot(spa, writableImage);
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
    List<Farm> farms = event.getFarms();
    farmsArray = new Drawable[farms.size()];
    int i = 0;
    for (Farm farm : farms) {
      farmsArray[i++] = farm;
    }

    farmImageView.setFitWidth(1200);
    farmImageView.setFitHeight(800);

    width = (int) (farmImageView.getFitWidth() * 0.75);
    height = (int) (farmImageView.getFitHeight() * 0.75);

    canvas.setWidth(width);
    canvas.setHeight(height);

    GeoPolygon[] polygons = extractPolygons(farmsArray);
    GraphicsContext gc = canvas.getGraphicsContext2D();
    setTransformation(gc, width, height, polygons);
    drawShapes(gc, polygons);

    canvasImage = pixelScaleAwareCanvasSnapshot(canvas, 1.0);
  }
}
