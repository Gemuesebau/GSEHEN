package de.hgu.gsehen.zoomtest;

import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Scale;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class ScalingDemo extends Application {
  /**
   * ZoomDemo.
   */
  public void start(Stage primaryStage) throws Exception {
    WebView webView = new WebView();
    Slider slider = new Slider(0.5, 2, 1);
    ZoomingPane zoomingPane = new ZoomingPane(webView);
    zoomingPane.zoomFactorProperty().bind(slider.valueProperty());
    primaryStage.setScene(new Scene(new BorderPane(zoomingPane, null, null, slider, null)));
    webView.getEngine().load("http://www.google.com");
    primaryStage.setWidth(800);
    primaryStage.setHeight(600);
    primaryStage.show();
  }

  private class ZoomingPane extends Pane {
    Node content;
    private DoubleProperty zoomFactor = new SimpleDoubleProperty(1);

    private ZoomingPane(Node content) {
      this.content = content;
      getChildren().add(content);
      Scale scale = new Scale(1, 1);
      content.getTransforms().add(scale);

      zoomFactor.addListener(new ChangeListener<Number>() {
        public void changed(ObservableValue<? extends Number> observable, Number oldValue,
            Number newValue) {
          scale.setX(newValue.doubleValue());
          scale.setY(newValue.doubleValue());
          requestLayout();
        }
      });
    }

    protected void layoutChildren() {
      Pos pos = Pos.TOP_LEFT;
      double width = getWidth();
      double height = getHeight();
      double top = getInsets().getTop();
      double right = getInsets().getRight();
      double left = getInsets().getLeft();
      double bottom = getInsets().getBottom();
      double contentWidth = (width - left - right) / zoomFactor.get();
      double contentHeight = (height - top - bottom) / zoomFactor.get();
      layoutInArea(content, left, top, contentWidth, contentHeight, 0, null, pos.getHpos(),
          pos.getVpos());
    }

    public final DoubleProperty zoomFactorProperty() {
      return zoomFactor;
    }
  }

  public static void main(String[] args) {
    launch(args);
  }
}
