package de.hgu.gsehen.gui.view;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;

/**
 * Listeners for making the nodes draggable via left mouse button. Considers if parent is zoomed.
 */
public class NodeGestures {


  private DragContext nodeDragContext = new DragContext();

  Canvas canvas;

  public NodeGestures(Canvas canvas2) {
    this.canvas = canvas2;

  }

  public EventHandler<MouseEvent> getOnMousePressedEventHandler() {
    return onMousePressedEventHandler;
  }

  public EventHandler<MouseEvent> getOnMouseDraggedEventHandler() {
    return onMouseDraggedEventHandler;
  }

  private EventHandler<MouseEvent> onMousePressedEventHandler = new EventHandler<MouseEvent>() {

    public void handle(MouseEvent event) {

      // left mouse button => dragging
      if (!event.isPrimaryButtonDown()) {
        return;
      }

      nodeDragContext.mouseAnchorX = event.getSceneX();
      nodeDragContext.mouseAnchorY = event.getSceneY();

      Node node = (Node) event.getSource();

      nodeDragContext.translateAnchorX = node.getTranslateX();
      nodeDragContext.translateAnchorY = node.getTranslateY();

    }

  };

  private EventHandler<MouseEvent> onMouseDraggedEventHandler = new EventHandler<MouseEvent>() {
    public void handle(MouseEvent event) {

      // left mouse button => dragging
      if (!event.isPrimaryButtonDown()) {
        return;
      }

      double scaleX = canvas.getScaleX();
      double scaleY = canvas.getScaleX();

      Node node = (Node) event.getSource();

      node.setTranslateX(nodeDragContext.translateAnchorX
          + ((event.getSceneX() - nodeDragContext.mouseAnchorX) / scaleX));
      node.setTranslateY(nodeDragContext.translateAnchorY
          + ((event.getSceneY() - nodeDragContext.mouseAnchorY) / scaleY));

      event.consume();

    }
  };
}
