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

  private Gsehen gsehenInstance;
  private BorderPane pane;
  private TreeTableView<Drawable> treeTableView;

  private Text nameLabel;
  private Text locationLabel;
  private Text cropStartLabel;
  private Text cropEndLabel;

  private Text name;
  private Text location;
  private Text cropStart;
  private Text cropEnd;

  {
    gsehenInstance = Gsehen.getInstance();
    gsehenInstance.registerForEvent(FarmDataChanged.class, this);
  }

  /**
   * Constructs a new plot data controller associated with the given BorderPane.
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
    nameLabel = new Text("Name:");
    nameLabel.setFont(Font.font("Arial", 14));
    name = new Text("");
    name.setFont(Font.font("Arial", FontWeight.BOLD, 14));

    HBox nameBox = new HBox();
    // nameBox.setStyle("-fx-background-color: #d39494;"); // Nur zur Übersicht!
    nameBox.getChildren().addAll(nameLabel, name);

    locationLabel = new Text("Location: ");
    locationLabel.setFont(Font.font("Arial", 14));
    location = new Text("");
    location.setFont(Font.font("Arial", FontWeight.BOLD, 14));

    HBox locationBox = new HBox();
    // locationBox.setStyle("-fx-background-color: #acd293;"); // Nur zur Übersicht!
    locationBox.getChildren().addAll(locationLabel, location);

    VBox topBox = new VBox(25);
    // topBox.setStyle("-fx-background-color: #f4ec46;"); // Nur zur Übersicht!
    topBox.setPadding(new Insets(20, 20, 20, 20));
    topBox.getChildren().addAll(nameBox, locationBox);
    pane.setTop(topBox);
    // TOP END ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    // LEFT ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    Text crop = new Text("Bestellung: ");
    crop.setFont(Font.font("Arial", 14));
    // Dummy-Liste
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
              for (int i = 0; i < treeTableView.getSelectionModel().getSelectedCells()
                  .size(); i++) {
                if (treeTableView.getSelectionModel().getSelectedCells().get(i) != null
                    && plot.getName().equals(treeTableView.getSelectionModel().getSelectedCells()
                        .get(i).getTreeItem().getValue().getName())) {
                  plot.setCrop(crop);
                  LOGGER.info("'" + crop.getName() + "' was set as crop in" + obj);
                }
              }
            }
          }
        }
        gsehenInstance.sendFarmDataChanged(obj, null);
      }
    });

    HBox cropBox = new HBox();
    // cropBox.setStyle("-fx-background-color: #d39494;"); // Nur zur Übersicht!
    cropBox.getChildren().addAll(crop, cropCombo);

    cropStartLabel = new Text("Start am:");
    cropStartLabel.setFont(Font.font("Arial", 14));
    cropStart = new Text("");
    cropStart.setFont(Font.font("Arial", FontWeight.BOLD, 14));

    HBox cropStartBox = new HBox();
    // cropStartBox.setStyle("-fx-background-color: #acd293;"); // Nur zur Übersicht!
    cropStartBox.getChildren().addAll(cropStartLabel, cropStart);

    cropEndLabel = new Text("Ende am:");
    cropEndLabel.setFont(Font.font("Arial", 14));
    cropEnd = new Text("");
    cropEnd.setFont(Font.font("Arial", FontWeight.BOLD, 14));

    HBox cropEndBox = new HBox();
    // cropEndBox.setStyle("-fx-background-color: #466bf4;"); // Nur zur Übersicht!
    cropEndBox.getChildren().addAll(cropEndLabel, cropEnd);

    VBox leftBox = new VBox(50);
    // leftBox.setStyle("-fx-background-color: #f4ba46;"); // Nur zur Übersicht!
    leftBox.setPadding(new Insets(20, 20, 20, 20));
    leftBox.getChildren().addAll(cropBox, cropStartBox, cropEndBox);
    pane.setLeft(leftBox);
    // LEFT END ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    treeTableView =
        (TreeTableView<Drawable>) Gsehen.getInstance().getScene().lookup(FARM_TREE_VIEW_ID);
    treeTableView.getSelectionModel().selectedItemProperty()
        .addListener(new ChangeListener<Object>() {
          @Override
          public void changed(ObservableValue<?> observable, Object oldVal, Object newVal) {
            for (int i = 0; i < treeTableView.getSelectionModel().getSelectedCells().size(); i++) {
              if (treeTableView.getSelectionModel().getSelectedCells().get(i) != null) {
                TreeItem<Drawable> selectedItem =
                    treeTableView.getSelectionModel().getSelectedCells().get(i).getTreeItem();
                if (selectedItem != null
                    && selectedItem.getValue().getClass().getSimpleName().equals("Plot")) {
                  pane.setVisible(true);
                  name.setText(selectedItem.getValue().getName());

                  // String geoPolygon = "";
                  // DecimalFormat decimal = new DecimalFormat("#.#####");
                  // for (double dx : selectedItem.getValue().getPolygon().getPolygonData()
                  // .getPointsX()) {
                  // for (double dy : selectedItem.getValue().getPolygon().getPolygonData()
                  // .getPointsY()) {
                  // geoPolygon += "[X: " + decimal.format(dx) + " / Y: " + decimal.format(dy)
                  // + "] \n\t\t\t\t";
                  // }
                  // }
                  // geopolygon.setText("GeoPolygon:\t\t" + geoPolygon);
                } else {
                  pane.setVisible(false);
                }
              }
            }
          }
        });
  }
}
