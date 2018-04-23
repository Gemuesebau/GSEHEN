package de.hgu.gsehen.gui.view;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.util.Duration;

public class AnimatedZoomOperator {

  private Timeline timeline;

  public AnimatedZoomOperator() {
    this.timeline = new Timeline(60);
  }

  /**
   * For zooming in the farmViewPane.
   * 
   * @param node = farmViewPane.
   * @param factor = zoomFactor.
   * @param x = event.getSceneX().
   * @param y = event.getSceneY().
   */
  public void zoom(Node node, double factor, double x, double y) {
    // determine scale
    double oldScale = node.getScaleX();
    double scale = oldScale * factor;
    double f = (scale / oldScale) - 1;

    // System.out.println(oldScale);
    // System.out.println(scale);
    // System.out.println(f);

    // determine offset that we will have to move the node
    Bounds bounds = node.localToScene(node.getBoundsInLocal());

    System.out.println(bounds);

    double dx = (x - (bounds.getWidth() / 2 + bounds.getMinX()));
    double dy = (y - (bounds.getHeight() / 2 + bounds.getMinY()));

    // timeline that scales and moves the node
    timeline.getKeyFrames().clear();
    timeline.getKeyFrames().addAll(
        new KeyFrame(Duration.millis(200),
            new KeyValue(node.translateXProperty(), node.getTranslateX() - f * dx)),
        new KeyFrame(Duration.millis(200),
            new KeyValue(node.translateYProperty(), node.getTranslateY() - f * dy)),
        new KeyFrame(Duration.millis(200), new KeyValue(node.scaleXProperty(), scale)),
        new KeyFrame(Duration.millis(200), new KeyValue(node.scaleYProperty(), scale)));

    timeline.play();
  }
}
