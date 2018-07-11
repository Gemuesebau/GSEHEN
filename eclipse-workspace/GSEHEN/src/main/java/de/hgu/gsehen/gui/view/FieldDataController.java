package de.hgu.gsehen.gui.view;

import de.hgu.gsehen.Gsehen;
import de.hgu.gsehen.event.FarmDataChanged;
import de.hgu.gsehen.event.GsehenEventListener;
import de.hgu.gsehen.model.Drawable;
import de.hgu.gsehen.model.Field;
import de.hgu.gsehen.model.Soil;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.StringConverter;

public class FieldDataController implements GsehenEventListener<FarmDataChanged> {
  private static final String FARM_TREE_VIEW_ID = "#farmTreeView";
  private static final Logger LOGGER = Logger.getLogger(Gsehen.class.getName());

  private TreeItem<Drawable> selectedItem;
  private Field field;

  private Gsehen gsehenInstance;
  private BorderPane pane;
  private TreeTableView<Drawable> treeTableView;

  private Text nameLabel;
  private Text areaLabel;
  private Text rootingZoneLabel;

  private TextField name;
  private Text area;
  private TextField rootingZone;

  {
    gsehenInstance = Gsehen.getInstance();
    gsehenInstance.registerForEvent(FarmDataChanged.class, this);
  }

  /**
   * Constructs a new field data controller associated with the given BorderPane.
   *
   * @param pane - the associated BorderPane.
   */
  public FieldDataController(Gsehen application, BorderPane pane) {
    this.gsehenInstance = application;
    this.pane = pane;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void handle(FarmDataChanged event) {
    /*
     * TODO: Alles noch sehr wild auf einen Haufen geworfen. Hier wird also noch aufgeräumt! 1)
     * Echte Daten erstellen. 2) Schauen, was sinnvoll ist und was nicht.
     */

    pane.setVisible(false);

    // TOP ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    nameLabel = new Text("Name: ");
    nameLabel.setFont(Font.font("Arial", 14));
    name = new TextField("");

    HBox nameBox = new HBox();
    // nameBox.setStyle("-fx-background-color: #d39494;"); // Nur zur Übersicht!
    nameBox.getChildren().addAll(nameLabel, name);

    areaLabel = new Text("m²: ");
    areaLabel.setFont(Font.font("Arial", 14));
    area = new Text("");
    area.setFont(Font.font("Arial", FontWeight.BOLD, 14));

    HBox locationBox = new HBox();
    // locationBox.setStyle("-fx-background-color: #acd293;"); // Nur zur Übersicht!
    locationBox.getChildren().addAll(areaLabel, area);

    VBox topBox = new VBox(25);
    // topBox.setStyle("-fx-background-color: #f4ec46;"); // Nur zur Übersicht!
    topBox.setPadding(new Insets(20, 20, 20, 20));
    topBox.getChildren().addAll(nameBox, locationBox);
    pane.setTop(topBox);
    // TOP END ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    // LEFT ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    Text soil = new Text("Bodentyp: ");
    soil.setFont(Font.font("Arial", 14));
    // Dummy-Liste
    ChoiceBox<Soil> soilChoiceBox = new ChoiceBox<Soil>();
    soilChoiceBox.getItems().addAll(new Soil("A"), new Soil("B"), new Soil("C"));
    soilChoiceBox.setConverter(new StringConverter<Soil>() {

      @Override
      public String toString(Soil object) {
        return object.getName();
      }

      @Override
      public Soil fromString(String string) {
        return soilChoiceBox.getItems().stream().filter(ap -> ap.getName().equals(string))
            .findFirst().orElse(null);
      }
    });

    HBox soilBox = new HBox();
    // cropBox.setStyle("-fx-background-color: #d39494;"); // Nur zur Übersicht!
    soilBox.getChildren().addAll(soil, soilChoiceBox);

    rootingZoneLabel = new Text("Maximal durchwurzelbare Zone: ");
    rootingZoneLabel.setFont(Font.font("Arial", 14));
    rootingZone = new TextField("");

    HBox soilStartBox = new HBox();
    // cropStartBox.setStyle("-fx-background-color: #acd293;"); // Nur zur
    // Übersicht!
    soilStartBox.getChildren().addAll(rootingZoneLabel, rootingZone);

    VBox leftBox = new VBox(50);
    // leftBox.setStyle("-fx-background-color: #f4ba46;"); // Nur zur Übersicht!
    leftBox.setPadding(new Insets(20, 20, 20, 20));
    leftBox.getChildren().addAll(soilBox, soilStartBox);
    pane.setLeft(leftBox);
    // LEFT END ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    Button save = new Button("Speichern");
    save.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent e) {
        // TODO
        // Name
        field.setName(name.getText());
        // Soil
        // field.setSoilProfile(soilProfile);
        // Rooting Zone
        // field.setRootingZone(Double.valueOf(rootingZone.getText()));

        gsehenInstance.sendFarmDataChanged(field, null);
      }
    });
    pane.setBottom(save);

    treeTableView =
        (TreeTableView<Drawable>) Gsehen.getInstance().getScene().lookup(FARM_TREE_VIEW_ID);
    treeTableView.getSelectionModel().selectedItemProperty()
        .addListener(new ChangeListener<Object>() {
          @Override
          public void changed(ObservableValue<?> observable, Object oldVal, Object newVal) {
            for (int i = 0; i < treeTableView.getSelectionModel().getSelectedCells().size(); i++) {
              if (treeTableView.getSelectionModel().getSelectedCells().get(i) != null) {
                selectedItem =
                    treeTableView.getSelectionModel().getSelectedCells().get(i).getTreeItem();
                if (selectedItem != null
                    && selectedItem.getValue().getClass().getSimpleName().equals("Field")) {
                  // TODO

                  pane.setVisible(true);
                  field = (Field) selectedItem.getValue();

                  name.setText(field.getName());

                  area.setText(String.valueOf(field.getArea()));

                  // soilChoiceBox.getSelectionModel().select(field.getSoilProfile().getSoilType());

                  rootingZone.setText(String.valueOf(field.getRootingZone()));
                } else {
                  pane.setVisible(false);
                }
              }
            }
          }
        });
  }
}
