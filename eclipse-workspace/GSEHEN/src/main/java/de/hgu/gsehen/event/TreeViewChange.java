package de.hgu.gsehen.event;

import de.hgu.gsehen.Gsehen;
import de.hgu.gsehen.model.Farm;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.util.converter.DefaultStringConverter;

public class TreeViewChange extends TextFieldTreeCell<String> {
  protected static final ResourceBundle mainBundle =
      ResourceBundle.getBundle("i18n.main", Locale.GERMAN);

  private TreeView<String> treeView = Gsehen.getInstance().getFarmTreeView();
  private List<Farm> farmList = Gsehen.getInstance().getFarmsList();
  private ContextMenu menu = new ContextMenu();
  private TreeItem<String> trash;
  private boolean remove = false;
  private boolean rename = false;

  /**
   * Constructor.
   */
  public TreeViewChange() {
    super(new DefaultStringConverter());

    MenuItem renameItem = new MenuItem(mainBundle.getString("treeview.rename"));
    menu.getItems().add(renameItem);
    renameItem.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent arg0) {
        startEdit();
        rename = true;
      }
    });

    MenuItem deleteItem = new MenuItem(mainBundle.getString("treeview.remove"));
    menu.getItems().add(deleteItem);
    deleteItem.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent e) {
        trash = (TreeItem<String>) Gsehen.getInstance().getFarmTreeView().getSelectionModel()
            .getSelectedItem();
        remove = trash.getParent().getChildren().remove(trash);
      }
    });
  }

  @Override
  public void updateItem(String item, boolean empty) {
    super.updateItem(item, empty);

    if (!isEditing()) {
      setContextMenu(menu);
    }
    if (rename) {
      for (int i = 0; i < treeView.getRoot().getChildren().size(); i++) {
        farmList.get(i).setName(treeView.getRoot().getChildren().get(i).getValue());
        for (int j = 0; j < treeView.getRoot().getChildren().get(i).getChildren().size(); j++) {
          farmList.get(i).getFields().get(j)
              .setName(treeView.getRoot().getChildren().get(i).getChildren().get(j).getValue());
          for (int k = 0; k < treeView.getRoot().getChildren().get(i).getChildren().get(j)
              .getChildren().size(); k++) {
            farmList.get(i).getFields().get(j).getPlots().get(k).setName(treeView.getRoot()
                .getChildren().get(i).getChildren().get(j).getChildren().get(k).getValue());
          }
        }
      }
    } else if (remove) {
      for (int i = 0; i < farmList.size(); i++) {
        if (trash.getValue().equals(farmList.get(i).getName())) {
          System.out.println("Farm");
          farmList.remove(i);
        } else {
          for (int j = 0; j < farmList.get(i).getFields().size(); j++) {
            if (trash.getValue().equals(farmList.get(i).getFields().get(j).getName())) {
              System.out.println("Field");
              farmList.get(i).getFields().remove(j);
            } else {
              for (int k = 0; k < farmList.get(i).getFields().get(j).getPlots().size(); k++) {
                if (trash.getValue()
                    .equals(farmList.get(i).getFields().get(j).getPlots().get(k).getName())) {
                  System.out.println("Plot");
                  farmList.get(i).getFields().get(j).getPlots().remove(j);
                }
              }
            }
          }
        }
      }
    }
  }
}
