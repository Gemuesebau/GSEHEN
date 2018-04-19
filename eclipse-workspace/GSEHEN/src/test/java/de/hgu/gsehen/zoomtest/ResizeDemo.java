package de.hgu.gsehen.zoomtest;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Cylinder;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class ResizeDemo extends Application {

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage stage) {

    // SubScene
    final Group subSceneRoot = new Group();
    final SubScene subScene = new SubScene(subSceneRoot, 800, 800);
    subScene.setFill(Color.YELLOWGREEN);

    // SubScene's camera
    final PerspectiveCamera perspectiveCamera = new PerspectiveCamera(true);
    perspectiveCamera.setFarClip(25);
    perspectiveCamera.setNearClip(0.1);
    perspectiveCamera.setFieldOfView(44);

    subScene.setCamera(perspectiveCamera);

    // SubScene's light
    final PointLight pointLight = new PointLight(Color.WHITE);
    pointLight.setTranslateZ(-20000);

    // Viewing group: camera and headlight
    final Group viewingGroup = new Group(perspectiveCamera, pointLight);
    viewingGroup.setTranslateZ(-5);

    // 3D model
    final Cylinder cylinder = new Cylinder();

    subSceneRoot.getChildren().addAll(cylinder, viewingGroup);

    // SubScene's parent
    final BorderPane borderPane = new BorderPane();
    borderPane.setMinHeight(0);
    borderPane.setMinWidth(0);
    borderPane.setCenter(subScene);
    borderPane.setBackground(new Background(new BackgroundFill(Color.YELLOW, null, null)));

    final Scene scene = new Scene(borderPane, 900, 900, true);
    ChangeListener sceneBoundsListener = new ChangeListener() {
      @Override
      public void changed(ObservableValue observable, Object oldXY, Object newXY) {
        subScene.setWidth(scene.getWidth() - 100);
        subScene.setHeight(scene.getHeight() - 100);
      }
    };
    scene.widthProperty().addListener(sceneBoundsListener);
    scene.heightProperty().addListener(sceneBoundsListener);

    stage.setTitle("WindowResizing");
    stage.setScene(scene);
    stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
      @Override
      public void handle(WindowEvent event) {
        System.exit(0);
      }
    });
    stage.show();
  }
}
