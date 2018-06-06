package de.hgu.gsehen.gui.view;

import de.hgu.gsehen.Gsehen;
import de.hgu.gsehen.event.FarmDataChanged;
import de.hgu.gsehen.event.GsehenEventListener;
import de.hgu.gsehen.model.Crop;
import de.hgu.gsehen.model.Drawable;
import de.hgu.gsehen.model.Farm;
import de.hgu.gsehen.model.Field;
import de.hgu.gsehen.model.Plot;
import java.util.logging.Logger;
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
  private static final String FARM_TREE_VIEW_ID = "#farmTreeView";
  private static final Logger LOGGER = Logger.getLogger(Gsehen.class.getName());

  private static PlotDataController instance;
  private Gsehen gsehenInstance;
  private BorderPane pane;
  private TreeTableView<Drawable> treeTableView;

  private Text name;
  private Text geopolygon;
  private Text location;

  {
    instance = this;
    gsehenInstance = Gsehen.getInstance();
    gsehenInstance.registerForEvent(FarmDataChanged.class, this);
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

  @SuppressWarnings("unchecked")
  @Override
  public void handle(FarmDataChanged event) {
    // TOP ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    name = new Text("Name:");
    name.setFont(Font.font("Arial", FontWeight.BOLD, 14));

    geopolygon = new Text("GeoPolygon:");
    geopolygon.setFont(Font.font("Arial", FontWeight.BOLD, 14));

    location = new Text("Location:");
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
      public void changed(ObservableValue ov, String oldValue, String newValue) {
        Drawable obj = null;
        Crop crop = new Crop();
        crop.setName(cropCombo.getValue());

        for (Farm farm : gsehenInstance.getFarmsList()) {
          for (Field field : farm.getFields()) {
            for (Plot plot : field.getPlots()) {
              obj = plot;
              // TODO - Testen, wieso immer ein Item mehr selektiert wird!
              for (int i = 0; i < treeTableView.getSelectionModel().getSelectedItems().size()
                  - 1; i++) {
                if (treeTableView.getSelectionModel().getSelectedItems().size() == 2) {
                  if (plot.getName().equals(
                      treeTableView.getSelectionModel().getSelectedItem().getValue().getName())) {
                    plot.setCrop(crop);
                    LOGGER.info("'" + crop.getName() + "' was set as crop in" + obj);
                  }
                } else {
                  if (plot.getName().equals(treeTableView.getSelectionModel().getSelectedItems()
                      .get(i).getValue().getName())) {
                    plot.setCrop(crop);
                    LOGGER.info("'" + crop.getName() + "' was set as crop in" + obj);
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

    treeTableView =
        (TreeTableView<Drawable>) Gsehen.getInstance().getScene().lookup(FARM_TREE_VIEW_ID);
    treeTableView.getSelectionModel().selectedItemProperty()
        .addListener(new ChangeListener<Object>() {
          @Override
          public void changed(ObservableValue<?> observable, Object oldVal, Object newVal) {
            TreeItem<Drawable> selectedItem = treeTableView.getSelectionModel().getSelectedItem();
            // TODO: Nullpointer! Könnte was mit dem Problem von oben zu tun haben!?
            if (selectedItem.getValue().getClass().getSimpleName().equals("Plot")) {
              name.setText("Name:\t\t\t" + selectedItem.getValue().getName());
              geopolygon
                  .setText("GeoPolygon:\t\t" + selectedItem.getValue().getPolygon().getGeoPoints());
              // location.setText("Location: " + selectedItem.getValue().);
            }
          }
        });
  }

  public static PlotDataController getInstance() {
    return instance;
  }

}
