package de.hgu.gsehen.gui;

import de.hgu.gsehen.Gsehen;
import de.hgu.gsehen.model.Farm;
import de.hgu.gsehen.model.Field;
import de.hgu.gsehen.model.Plot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableColumn.CellEditEvent;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.util.Duration;

public class GsehenTreeTable {
  protected static final ResourceBundle mainBundle =
      ResourceBundle.getBundle("i18n.main", Locale.GERMAN);
  private static final DataFormat SERIALIZED_MIME_TYPE =
      new DataFormat("application/x-java-serialized-object");
  private static final String FARM_TREE_VIEW_ID = "#farmTreeView";
  private static final Logger LOGGER = Logger.getLogger(Gsehen.class.getName());

  private List<Farm> newFarmsList;
  private Farm farm;
  private Timeline scrolltimeline = new Timeline();
  private double scrollDirection = 0;
  private TreeTableView<Map<String, Object>> farmTreeView;

  private TreeTableColumn<Map<String, Object>, String> column;
  private TreeItem<Map<String, Object>> farmItem;
  private TreeItem<Map<String, Object>> fieldItem;
  @SuppressWarnings("unused")
  private TreeItem<Map<String, Object>> plotItem;
  private TreeItem<Map<String, Object>> trash;
  private TreeItem<Map<String, Object>> item;
  private TreeItem<Map<String, Object>> rootItem;
  private List<Farm> farmsList = new ArrayList<>();

  private ContextMenu menu = new ContextMenu();
  private MenuItem deleteItem;

  private static GsehenTreeTable instance;

  {
    instance = this;
  }

  /**
   * Adds the FarmTreeView.
   */
  @SuppressWarnings("unchecked")
  public void addFarmTreeView() {
    farmTreeView = (TreeTableView<Map<String, Object>>) Gsehen.getInstance().getScene()
        .lookup(FARM_TREE_VIEW_ID);
    rootItem = new TreeItem<>();
    farmTreeView.setRowFactory(this::rowFactory);
    addColumn(mainBundle.getString("treetableview.name"), "name");
    addColumn(mainBundle.getString("treetableview.type"), "type");

    deleteItem = new MenuItem(mainBundle.getString("treeview.remove"));
    menu.getItems().add(deleteItem);
    deleteItem.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent e) {
        trash = farmTreeView.getSelectionModel().getSelectedItem();
        removeItem();
        farmTreeView.getRoot().getChildren().clear();
        fillTreeView();
      }
    });

    farmTreeView.setRoot(rootItem);
    farmTreeView.setShowRoot(false);
    farmTreeView.setEditable(true);
    farmTreeView.setContextMenu(menu);
    fillTreeView();
    setupScrolling();
  }

  @SuppressWarnings("unchecked")
  private TreeTableRow<Map<String, Object>> rowFactory(TreeTableView<Map<String, Object>> view) {
    TreeTableRow<Map<String, Object>> row = new TreeTableRow<>();
    row.setOnDragDetected(event -> {
      if (!row.isEmpty()) {
        Dragboard db = row.startDragAndDrop(TransferMode.MOVE);
        db.setDragView(row.snapshot(null, null));
        ClipboardContent cc = new ClipboardContent();
        cc.put(SERIALIZED_MIME_TYPE, row.getIndex());
        db.setContent(cc);
        event.consume();
      }
    });

    row.setOnDragOver(event -> {
      Dragboard db = event.getDragboard();
      if (acceptable(db, row)) {
        event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
        event.consume();
      }
    });

    row.setOnDragDropped(event -> {
      Dragboard db = event.getDragboard();
      if (acceptable(db, row)) {
        int index = (Integer) db.getContent(SERIALIZED_MIME_TYPE);
        TreeItem<Map<String, Object>> item = farmTreeView.getTreeItem(index);

        Map<String, Object> itemMap = item.getValue();
        String itemType = "";
        for (String key : itemMap.keySet()) {
          itemType = (String) itemMap.get(key);
        }

        Map<String, Object> map = row.getTreeItem().getValue();
        String destinationType = "";
        for (String key : map.keySet()) {
          destinationType = (String) map.get(key);
        }

        if (itemType.equals("Plot") && destinationType.equals("Field")
            || itemType.equals("Field") && destinationType.equals("Farm")) {
          item.getParent().getChildren().remove(item);
          getTarget(row).getChildren().add(item);
          event.setDropCompleted(true);
          farmTreeView.getSelectionModel().select(item);
          event.consume();

          newFarmsList = new ArrayList<>();

          String farmName;
          GeoPolygon farmGeo;

          String fieldName;
          GeoPolygon fieldGeo;
          Field field;

          String plotName;
          GeoPolygon plotGeo;
          Plot plot;

          // Updates the farmList.
          for (int i = 0; i < farmTreeView.getRoot().getChildren().size(); i++) {

            farmName = (String) farmTreeView.getRoot().getChildren().get(i).getValue().entrySet()
                .iterator().next().getValue();
            farmGeo = farmsList.iterator().next().getPolygonByName(farmName);
            farm = new Farm(farmName, farmGeo);

            for (int j = 0; j < farmTreeView.getRoot().getChildren().get(i).getChildren()
                .size(); j++) {

              fieldName = (String) farmTreeView.getRoot().getChildren().get(i).getChildren().get(j)
                  .getValue().entrySet().iterator().next().getValue();
              fieldGeo = farmsList.iterator().next().getFields().iterator().next()
                  .getPolygonByName(fieldName);
              field = new Field(fieldName, fieldGeo);

              if (j == 0) {
                farm.setFields(field);
              } else {
                List<Field> fields = farm.getFields();
                fields.add(field);
              }

              for (int k = 0; k < farmTreeView.getRoot().getChildren().get(i).getChildren().get(j)
                  .getChildren().size(); k++) {

                plotName = (String) farmTreeView.getRoot().getChildren().get(i).getChildren().get(j)
                    .getChildren().get(k).getValue().entrySet().iterator().next().getValue();
                plotGeo = farmsList.iterator().next().getFields().iterator().next().getPlots()
                    .iterator().next().getPolygonByName(plotName);
                plot = new Plot(plotName, plotGeo);

                if (k == 0) {
                  field.setPlots(plot);
                } else {
                  List<Plot> plots = farm.getFields().get(j).getPlots();
                  plots.add(plot);
                }
              }
            }
            newFarmsList.add(farm);
          }
        } else {
          LOGGER.info(itemType + " can't be stack on " + destinationType);
        }
        farmsList.clear();
        farmsList.addAll(newFarmsList);
        Gsehen.getInstance().saveUserData();
      }
    });
    return row;
  }

  @SuppressWarnings("rawtypes")
  private boolean acceptable(Dragboard db, TreeTableRow<Map<String, Object>> row) {
    boolean result = false;
    if (db.hasContent(SERIALIZED_MIME_TYPE)) {
      int index = (Integer) db.getContent(SERIALIZED_MIME_TYPE);
      if (row.getIndex() != index) {
        TreeItem target = getTarget(row);
        TreeItem item = farmTreeView.getTreeItem(index);
        result = !isParent(item, target);
      }
    }
    return result;
  }

  @SuppressWarnings("rawtypes")
  private TreeItem getTarget(TreeTableRow<Map<String, Object>> row) {
    TreeItem target = farmTreeView.getRoot();
    if (!row.isEmpty()) {
      target = row.getTreeItem();
    }
    return target;
  }

  // prevent loops in the tree
  @SuppressWarnings("rawtypes")
  private boolean isParent(TreeItem parent, TreeItem child) {
    boolean result = false;
    while (!result && child != null) {
      result = child.getParent() == parent;
      child = child.getParent();
    }
    return result;
  }

  /**
   * Adds the columns to the TreeTableView.
   * 
   * @param label - Name of the column.
   * @param dataIndex - Content of the column.
   */
  public void addColumn(String label, String dataIndex) {
    column = new TreeTableColumn<>(label);

    column.setCellValueFactory(
        (TreeTableColumn.CellDataFeatures<Map<String, Object>, String> param) -> {
          ObservableValue<String> result = new ReadOnlyStringWrapper("");
          if (param.getValue().getValue() != null) {
            result = new ReadOnlyStringWrapper("" + param.getValue().getValue().get(dataIndex));
          }
          return result;
        });

    column.setCellFactory(TextFieldTreeTableCell.forTreeTableColumn());

    if (column.getText().equals(mainBundle.getString("treetableview.name"))) {
      column.setPrefWidth(200);
      column.setOnEditCommit(new EventHandler<CellEditEvent<Map<String, Object>, String>>() {
        @Override
        public void handle(CellEditEvent<Map<String, Object>, String> event) {
          // TODO: gleich heißende Objekte werden aktuell zusammen umbenannt.
          
          // TreeItem<Map<String, Object>> item =
          // farmTreeView.getSelectionModel().getSelectedItem();
          // item.getClass().getSimpleName().replaceAll("", event.getNewValue());
          // Gsehen.getInstance().saveUserData();
          
          for (int i = 0; i < farmTreeView.getRoot().getChildren().size(); i++) {
            if (farmsList.get(i).getName().equals(event.getOldValue())
                && event.getRowValue() == farmTreeView.getSelectionModel().getSelectedItem()) {
              farmsList.get(i).setName(event.getNewValue());
            }
            for (int j = 0; j < farmTreeView.getRoot().getChildren().get(i).getChildren()
                .size(); j++) {
              if (farmsList.get(i).getFields().get(j).getName().equals(event.getOldValue())
                  && event.getRowValue() == farmTreeView.getSelectionModel().getSelectedItem()) {
                farmsList.get(i).getFields().get(j).setName(event.getNewValue());
              }
              for (int k = 0; k < farmTreeView.getRoot().getChildren().get(i).getChildren().get(j)
                  .getChildren().size(); k++) {
                if (farmsList.get(i).getFields().get(j).getPlots().get(k).getName()
                    .equals(event.getOldValue())
                    && event.getRowValue() == farmTreeView.getSelectionModel().getSelectedItem()) {
                  farmsList.get(i).getFields().get(j).getPlots().get(k)
                      .setName(event.getNewValue());
                }
              }
            }
          }
          farmTreeView.getRoot().getChildren().clear();
          fillTreeView();
        }
      });
    } else {
      column.setPrefWidth(100);
      column.setEditable(false);
    }
    farmTreeView.getColumns().add(column);
  }

  /**
   * Scrollbar in the TreeTableView.
   */
  public void setupScrolling() {
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

    for (Farm farm : farmsList) {
      farmItem = createItem(rootItem, farm.getName(), farm.getClass().getSimpleName());

      if (farm.getFields() != null) {
        for (Field field : farm.getFields()) {
          fieldItem = createItem(farmItem, field.getName(), field.getClass().getSimpleName());

          if (field.getPlots() != null) {
            for (Plot plot : field.getPlots()) {
              plotItem = createItem(fieldItem, plot.getName(), plot.getClass().getSimpleName());
            }
          }
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

  public static GsehenTreeTable getInstance() {
    return instance;
  }

  public TreeTableView<Map<String, Object>> getFarmTreeView() {
    return farmTreeView;
  }

}
