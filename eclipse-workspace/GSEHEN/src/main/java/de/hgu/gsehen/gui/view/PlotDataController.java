package de.hgu.gsehen.gui.view;

import de.hgu.gsehen.Gsehen;
import de.hgu.gsehen.event.FarmDataChanged;
import de.hgu.gsehen.event.GsehenEventListener;
import de.hgu.gsehen.gui.GsehenTreeTable;
import de.hgu.gsehen.model.Crop;
import de.hgu.gsehen.model.Drawable;
import de.hgu.gsehen.model.Farm;
import de.hgu.gsehen.model.Field;
import de.hgu.gsehen.model.Plot;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class PlotDataController implements GsehenEventListener<FarmDataChanged> {
  private Gsehen gsehenInstance;
  private GsehenTreeTable gsehenTreeTable;
  private BorderPane pane;
  private TreeTableView<Drawable> treeTableView;

  {
    gsehenInstance = Gsehen.getInstance();
    gsehenInstance.registerForEvent(FarmDataChanged.class, this);
    gsehenTreeTable = GsehenTreeTable.getInstance();
  }

  /**
   * Constructs a new field data controller associated with the given BorderPane.
   *
   * @param pane - the associated BorderPane.
   */
  public PlotDataController(Gsehen application, BorderPane pane) {
    this.gsehenInstance = application;
    this.pane = pane;
  }

  @Override
  public void handle(FarmDataChanged event) {

    // TOP ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    Text name = new Text("Name:");
    name.setFont(Font.font("Arial", FontWeight.BOLD, 14));

    Text geopolygon = new Text("GeoPolygon:");
    geopolygon.setFont(Font.font("Arial", FontWeight.BOLD, 14));

    Text location = new Text("Location:");
    location.setFont(Font.font("Arial", FontWeight.BOLD, 14));

    VBox topBox = new VBox(25);
    topBox.setPadding(new Insets(20, 20, 20, 20));
    topBox.getChildren().addAll(name, geopolygon, location);
    pane.setTop(topBox);
    // TOP END ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    // LEFT ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    Text crop = new Text("Bestellung:\t\t");
    crop.setFont(Font.font("Arial", FontWeight.BOLD, 14));
    ObservableList<String> cropList =
        FXCollections.observableArrayList("Apfel", "Birne", "Löwenzahn", "Tomate", "Zwiebel");
    ComboBox<String> cropCombo = new ComboBox<String>(cropList);
    cropCombo.valueProperty().addListener(new ChangeListener<String>() {
      @SuppressWarnings("rawtypes")
      @Override
      public void changed(ObservableValue ov, String t, String t1) {
        System.out.println(ov);
        System.out.println(t);
        System.out.println(t1);

        Drawable obj = null;
        Crop crop = new Crop();
        crop.setName(cropCombo.getValue());

        for (Farm farm : gsehenInstance.getFarmsList()) {
          for (Field field : farm.getFields()) {
            for (Plot plot : field.getPlots()) {
              obj = plot;
              for (int i = 0; i < treeTableView.getSelectionModel().getSelectedItems()
                  .size(); i++) {
                if (treeTableView.getSelectionModel().getSelectedItems().size() == 1) {
                  if (plot.getName().equals(
                      treeTableView.getSelectionModel().getSelectedItem().getValue().getName())) {
                    plot.setCrop(crop);
                  }
                } else {
                  if (plot.getName().equals(treeTableView.getSelectionModel().getSelectedItems()
                      .get(i).getValue().getName())) {
                    plot.setCrop(crop);
                  }
                }
              }
            }
          }
        }

        gsehenInstance.sendFarmDataChanged(obj, null);
      }
    });

    HBox leftBox = new HBox();
    leftBox.setPadding(new Insets(20, 20, 20, 20));
    leftBox.getChildren().addAll(crop, cropCombo);
    pane.setLeft(leftBox);
    // LEFT END ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    treeTableView = gsehenTreeTable.getFarmTreeView();
    treeTableView.getSelectionModel().selectedItemProperty()
        .addListener(new ChangeListener<Object>() {

          @SuppressWarnings("unchecked")
          @Override
          public void changed(ObservableValue<?> observable, Object oldVal, Object newVal) {
            TreeItem<Drawable> selectedItem = (TreeItem<Drawable>) newVal;
            if (selectedItem.getValue().getClass().getSimpleName().equals("Plot")) {
              name.setText("Name:\t\t\t" + selectedItem.getValue().getName());
              geopolygon
                  .setText("GeoPolygon:\t\t" + selectedItem.getValue().getPolygon().getGeoPoints());
              // location.setText("Location: " + selectedItem.getValue().);
            }
          }
        });
  }

}
