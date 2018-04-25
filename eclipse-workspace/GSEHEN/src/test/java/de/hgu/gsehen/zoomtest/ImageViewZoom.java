package de.hgu.gsehen.zoomtest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ImageViewZoom extends Application {
  static double initx;
  static double inity;
  static int height;
  static int width;
  public static String path;
  static Scene initialScene, View;
  static double offSetX, offSetY, zoomlvl;

  @Override
  public void start(Stage s) {
    s.setResizable(false);
    GridPane grid = new GridPane();
    grid.setHgap(20);
    grid.setVgap(20);
    grid.setAlignment(Pos.CENTER);

    Label hint = new Label("Select Your Image");
    TextField URL = new TextField();
    URL.setEditable(false);
    URL.setPrefWidth(350);

    Button browse = new Button("Browse");
    FileChooser fc = new FileChooser();
    ExtensionFilter png = new ExtensionFilter("png", "*.png");
    ExtensionFilter jpg = new ExtensionFilter("jpg", "*.jpg");
    fc.getExtensionFilters().addAll(png, jpg);
    browse.setOnAction(e -> {
      URL.setText(fc.showOpenDialog(s).getAbsolutePath());
    });

    Button open = new Button("Open");
    open.setOnAction(e -> {
      path = URL.getText();
      initView();
      s.setScene(View);
    });

    grid.add(hint, 0, 0);
    grid.add(URL, 1, 0);
    grid.add(browse, 2, 0);
    grid.add(open, 2, 1);

    initialScene = new Scene(grid, 600, 100);
    s.setScene(initialScene);
    s.show();
  }

  public static void initView() {
    VBox root = new VBox(20);
    root.setAlignment(Pos.CENTER);

    Label title = new Label(path.substring(path.lastIndexOf("\\") + 1));
    Image source = null;
    try {
      source = new Image(new FileInputStream(path));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }

    ImageView image = new ImageView(source);
    double ratio = source.getWidth() / source.getHeight();

    if (500 / ratio < 500) {
      width = 500;
      height = (int) (500 / ratio);
    } else if (500 * ratio < 500) {
      height = 500;
      width = (int) (500 * ratio);
    } else {
      height = 500;
      width = 500;
    }
    image.setPreserveRatio(false);
    image.setFitWidth(width);
    image.setFitHeight(height);
    height = (int) source.getHeight();
    width = (int) source.getWidth();
    System.out.println("height = " + height + "\nwidth = " + width);
    HBox zoom = new HBox(10);
    zoom.setAlignment(Pos.CENTER);

    Slider zoomLvl = new Slider();
    zoomLvl.setMax(4);
    zoomLvl.setMin(1);
    zoomLvl.setMaxWidth(200);
    zoomLvl.setMinWidth(200);
    Label hint = new Label("Zoom Level");
    Label value = new Label("1.0");

    offSetX = width / 2;
    offSetY = height / 2;



    zoom.getChildren().addAll(hint, zoomLvl, value);

    Slider Hscroll = new Slider();
    Hscroll.setMin(0);
    Hscroll.setMax(width);
    Hscroll.setMaxWidth(image.getFitWidth());
    Hscroll.setMinWidth(image.getFitWidth());
    Hscroll.setTranslateY(-20);
    Slider Vscroll = new Slider();
    Vscroll.setMin(0);
    Vscroll.setMax(height);
    Vscroll.setMaxHeight(image.getFitHeight());
    Vscroll.setMinHeight(image.getFitHeight());
    Vscroll.setOrientation(Orientation.VERTICAL);
    Vscroll.setTranslateX(-20);


    BorderPane imageView = new BorderPane();
    BorderPane.setAlignment(Hscroll, Pos.CENTER);
    BorderPane.setAlignment(Vscroll, Pos.CENTER_LEFT);
    Hscroll.valueProperty().addListener(e -> {
      offSetX = Hscroll.getValue();
      zoomlvl = zoomLvl.getValue();
      double newValue = (double) ((int) (zoomlvl * 10)) / 10;
      value.setText(newValue + "");
      if (offSetX < (width / newValue) / 2) {
        offSetX = (width / newValue) / 2;
      }
      if (offSetX > width - ((width / newValue) / 2)) {
        offSetX = width - ((width / newValue) / 2);
      }

      image.setViewport(new Rectangle2D(offSetX - ((width / newValue) / 2),
          offSetY - ((height / newValue) / 2), width / newValue, height / newValue));
    });
    Vscroll.valueProperty().addListener(e -> {
      offSetY = height - Vscroll.getValue();
      zoomlvl = zoomLvl.getValue();
      double newValue = (double) ((int) (zoomlvl * 10)) / 10;
      value.setText(newValue + "");
      if (offSetY < (height / newValue) / 2) {
        offSetY = (height / newValue) / 2;
      }
      if (offSetY > height - ((height / newValue) / 2)) {
        offSetY = height - ((height / newValue) / 2);
      }
      image.setViewport(new Rectangle2D(offSetX - ((width / newValue) / 2),
          offSetY - ((height / newValue) / 2), width / newValue, height / newValue));
    });
    imageView.setCenter(image);
    imageView.setTop(Hscroll);
    imageView.setRight(Vscroll);
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
      Hscroll.setValue(offSetX);
      Vscroll.setValue(height - offSetY);
      image.setViewport(new Rectangle2D(offSetX - ((width / newValue) / 2),
          offSetY - ((height / newValue) / 2), width / newValue, height / newValue));
    });
    imageView.setCursor(Cursor.OPEN_HAND);
    image.setOnMousePressed(e -> {
      initx = e.getSceneX();
      inity = e.getSceneY();
      imageView.setCursor(Cursor.CLOSED_HAND);
    });
    image.setOnMouseReleased(e -> {
      imageView.setCursor(Cursor.OPEN_HAND);
    });
    image.setOnMouseDragged(e -> {
      Hscroll.setValue(Hscroll.getValue() + (initx - e.getSceneX()));
      Vscroll.setValue(Vscroll.getValue() - (inity - e.getSceneY()));
      initx = e.getSceneX();
      inity = e.getSceneY();
    });
    root.getChildren().addAll(title, imageView, zoom);

    View = new Scene(root, (image.getFitWidth()) + 70, (image.getFitHeight()) + 150);
  }

  public static void main(String[] args) {
    launch(args);
  }
}
