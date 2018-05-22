package de.hgu.gsehen.gui;

import de.hgu.gsehen.Gsehen;
import de.hgu.gsehen.model.Farm;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import javafx.util.Duration;

public class GsehenTreeTable {
  private Timeline scrolltimeline = new Timeline();
  private double scrollDirection = 0;
  private TreeTableView<Map<String, Object>> farmTreeView;
  private TreeItem<Map<String, Object>> farmItem;
  private TreeItem<Map<String, Object>> fieldItem;
  @SuppressWarnings("unused")
  private TreeItem<Map<String, Object>> plotItem;
  private TreeItem<Map<String, Object>> trash;
  private TreeItem<Map<String, Object>> item;
  private TreeItem<Map<String, Object>> rootItem;
  private List<Farm> farmsList = new ArrayList<>();

  /**
   * Scrollbar in the TreeTableView.
   */
  public void setupScrolling() {
    farmTreeView = Gsehen.getInstance().getFarmTreeView();

    scrolltimeline.setCycleCount(Timeline.INDEFINITE);
    scrolltimeline.getKeyFrames()
        .add(new KeyFrame(Duration.millis(20), "Scoll", (ActionEvent e) -> {
          dragScroll();
        }));
    farmTreeView.setOnDragExited(event -> {
      if (event.getY() > 0) {
        scrollDirection = 1.0 / farmTreeView.getExpandedItemCount();
      } else {
        scrollDirection = -1.0 / farmTreeView.getExpandedItemCount();
      }
      scrolltimeline.play();
    });
    farmTreeView.setOnDragEntered(event -> {
      scrolltimeline.stop();
    });
    farmTreeView.setOnDragDone(event -> {
      scrolltimeline.stop();
    });

  }

  private void dragScroll() {
    ScrollBar sb = getVerticalScrollbar();
    if (sb != null) {
      double newValue = sb.getValue() + scrollDirection;
      newValue = Math.min(newValue, 1.0);
      newValue = Math.max(newValue, 0.0);
      sb.setValue(newValue);
    }
  }

  private ScrollBar getVerticalScrollbar() {
    ScrollBar result = null;
    for (Node n : farmTreeView.lookupAll(".scroll-bar")) {
      if (n instanceof ScrollBar) {
        ScrollBar bar = (ScrollBar) n;
        if (bar.getOrientation().equals(Orientation.VERTICAL)) {
          result = bar;
        }
      }
    }
    return result;
  }

  /**
   * Fills the TreeView with Farms, Fields and Plots.
   */
  public void fillTreeView() {
    farmsList = Gsehen.getInstance().getFarmsList();
    rootItem = Gsehen.getInstance().getRootItem();

    for (int i = 0; i < farmsList.size(); i++) {
      farmItem = createItem(rootItem, farmsList.get(i).getName(),
          farmsList.get(i).getClass().getSimpleName());

      for (int j = 0; j < farmsList.get(i).getFields().size(); j++) {
        fieldItem = createItem(farmItem, farmsList.get(i).getFields().get(j).getName(),
            farmsList.get(i).getFields().get(j).getClass().getSimpleName());

        for (int k = 0; k < farmsList.get(i).getFields().get(j).getPlots().size(); k++) {
          plotItem =
              createItem(fieldItem, farmsList.get(i).getFields().get(j).getPlots().get(k).getName(),
                  farmsList.get(i).getFields().get(j).getPlots().get(k).getClass().getSimpleName());
        }
      }
    }
  }

  private TreeItem<Map<String, Object>> createItem(TreeItem<Map<String, Object>> parent,
      String name, String type) {
    item = new TreeItem<>();
    Map<String, Object> value = new HashMap<>();
    value.put("name", name);
    value.put("type", type);
    item.setValue(value);
    parent.getChildren().add(item);
    item.setExpanded(true);
    return item;
  }

  /**
   * Removes an item (and his childs) from the TreeTableView.
   */
  public void removeItem() {
    trash = Gsehen.getInstance().getTrash();

    for (int i = 0; i < farmsList.size(); i++) {
      if (trash.getValue().containsValue(farmsList.get(i).getName())) {
        farmsList.remove(i);
      } else {
        for (int j = 0; j < farmsList.get(i).getFields().size(); j++) {
          if (trash.getValue().containsValue(farmsList.get(i).getFields().get(j).getName())) {
            farmsList.get(i).getFields().remove(j);
          } else {
            for (int k = 0; k < farmsList.get(i).getFields().get(j).getPlots().size(); k++) {
              if (trash.getValue()
                  .containsValue(farmsList.get(i).getFields().get(j).getPlots().get(k).getName())) {
                farmsList.get(i).getFields().get(j).getPlots().remove(j);
              }
            }
          }
        }
      }
    }
  }
}
