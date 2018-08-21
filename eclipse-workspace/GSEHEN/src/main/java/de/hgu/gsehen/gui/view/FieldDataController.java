package de.hgu.gsehen.gui.view;

import de.hgu.gsehen.Gsehen;
import de.hgu.gsehen.event.FarmDataChanged;
import de.hgu.gsehen.event.GsehenEventListener;
import de.hgu.gsehen.model.Drawable;
import de.hgu.gsehen.model.Farm;
import de.hgu.gsehen.model.Field;
import de.hgu.gsehen.model.Soil;
import de.hgu.gsehen.model.SoilProfile;
import de.hgu.gsehen.model.SoilProfileDepth;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
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

  private Button createSoil;
  private SoilProfile soilProfile;

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
    pane.setVisible(false);

    // TOP ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    nameLabel = new Text("Name: ");
    nameLabel.setFont(Font.font("Arial", 14));
    name = new TextField("");

    HBox nameBox = new HBox();
    nameBox.getChildren().addAll(nameLabel, name);

    areaLabel = new Text("m²: ");
    areaLabel.setFont(Font.font("Arial", 14));
    area = new Text("");
    area.setFont(Font.font("Arial", FontWeight.BOLD, 14));

    HBox locationBox = new HBox();
    locationBox.getChildren().addAll(areaLabel, area);

    VBox topBox = new VBox(25);
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
    soilBox.getChildren().addAll(soil, soilChoiceBox);

    rootingZoneLabel = new Text("Maximal durchwurzelbare Zone: ");
    rootingZoneLabel.setFont(Font.font("Arial", 14));
    rootingZone = new TextField("");

    createSoil = new Button("Bodentyp ertsellen");
    createSoil.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent e) {
        pane.getChildren().clear();

        TextField vsStart = new TextField("0");
        TextField vsEnd = new TextField("25");
        TextField sStart = new TextField(vsEnd.getText());
        TextField sEnd = new TextField("50");
        TextField mdStart = new TextField(sEnd.getText());
        TextField mdEnd = new TextField("90");
        TextField dStart = new TextField(mdEnd.getText());
        TextField dEnd = new TextField("150");

        Text soilNameLabel = new Text("Name: ");
        soilNameLabel.setFont(Font.font("Arial", 14));
        TextField soilName = new TextField("");

        HBox nameBox = new HBox();
        nameBox.getChildren().addAll(soilNameLabel, soilName);

        Text descriptionLabel = new Text("Beschreibung: ");
        descriptionLabel.setFont(Font.font("Arial", 14));
        TextArea descriptionArea = new TextArea("");

        HBox descriptionBox = new HBox();
        descriptionBox.getChildren().addAll(descriptionLabel, descriptionArea);

        Text soilAwcLabel = new Text("Verfügbare Wasserkapazität: ");
        soilAwcLabel.setFont(Font.font("Arial", 14));
        Text soilAwc = new Text("0.0"); // TODO berechnen!
        soilAwc.setFont(Font.font("Arial", 14));

        HBox awcBox = new HBox();
        awcBox.getChildren().addAll(soilAwcLabel, soilAwc);

        VBox topBox = new VBox(25);
        topBox.setPadding(new Insets(20, 20, 20, 20));
        topBox.getChildren().addAll(nameBox, descriptionBox, awcBox);
        pane.setTop(topBox);

        Text depth = new Text("Tiefe");
        depth.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        Text vsStartLabel = new Text("Sehr flach (min.): ");
        vsStartLabel.setFont(Font.font("Arial", 14));
        vsStart.textProperty().addListener(new ChangeListener<String>() {
          @Override
          public void changed(ObservableValue<? extends String> observable, String oldValue,
              String newValue) {
            if (!newValue.matches("\\d{0,7}([\\.]\\d{0,4})?")
                || Double.parseDouble(newValue) > Double.parseDouble(vsEnd.getText())) {
              vsStart.setText(oldValue);
            }
          }
        });

        HBox vsStartBox = new HBox();
        vsStartBox.getChildren().addAll(vsStartLabel, vsStart);

        Text vsEndLabel = new Text("Sehr flach (max.): ");
        vsEndLabel.setFont(Font.font("Arial", 14));
        vsEnd.textProperty().addListener(new ChangeListener<String>() {
          @Override
          public void changed(ObservableValue<? extends String> observable, String oldValue,
              String newValue) {
            if (!newValue.matches("\\d{0,7}([\\.]\\d{0,4})?")
                || Double.parseDouble(newValue) < Double.parseDouble(vsEnd.getText())) {
              vsEnd.setText(oldValue);
            } else {
              sStart.setText(vsEnd.getText());
            }
          }
        });

        HBox vsEndBox = new HBox();
        vsEndBox.getChildren().addAll(vsEndLabel, vsEnd);

        Text sStartLabel = new Text("Flach (min.): ");
        sStartLabel.setFont(Font.font("Arial", 14));
        sStart.textProperty().addListener(new ChangeListener<String>() {
          @Override
          public void changed(ObservableValue<? extends String> observable, String oldValue,
              String newValue) {
            if (!newValue.matches("\\d{0,7}([\\.]\\d{0,4})?")) {
              sStart.setText(oldValue);
            } else {
              vsEnd.setText(sStart.getText());
            }
          }
        });

        HBox sStartBox = new HBox();
        sStartBox.getChildren().addAll(sStartLabel, sStart);

        Text sEndLabel = new Text("Flach (max.): ");
        sEndLabel.setFont(Font.font("Arial", 14));
        sEnd.textProperty().addListener(new ChangeListener<String>() {
          @Override
          public void changed(ObservableValue<? extends String> observable, String oldValue,
              String newValue) {
            if (!newValue.matches("\\d{0,7}([\\.]\\d{0,4})?")) {
              sEnd.setText(oldValue);
            } else {
              mdStart.setText(sEnd.getText());
            }
          }
        });

        HBox sEndBox = new HBox();
        sEndBox.getChildren().addAll(sEndLabel, sEnd);

        Text mdStartLabel = new Text("Mäßig tief (min.): ");
        mdStartLabel.setFont(Font.font("Arial", 14));
        mdStart.textProperty().addListener(new ChangeListener<String>() {
          @Override
          public void changed(ObservableValue<? extends String> observable, String oldValue,
              String newValue) {
            if (!newValue.matches("\\d{0,7}([\\.]\\d{0,4})?")) {
              mdStart.setText(oldValue);
            } else {
              sEnd.setText(mdStart.getText());
            }
          }
        });

        HBox mdStartBox = new HBox();
        mdStartBox.getChildren().addAll(mdStartLabel, mdStart);

        Text mdEndLabel = new Text("Mäßig tief (max.): ");
        mdEndLabel.setFont(Font.font("Arial", 14));
        mdEnd.textProperty().addListener(new ChangeListener<String>() {
          @Override
          public void changed(ObservableValue<? extends String> observable, String oldValue,
              String newValue) {
            if (!newValue.matches("\\d{0,7}([\\.]\\d{0,4})?")) {
              mdEnd.setText(oldValue);
            } else {
              dStart.setText(mdEnd.getText());
            }
          }
        });

        HBox mdEndBox = new HBox();
        mdEndBox.getChildren().addAll(mdEndLabel, mdEnd);

        Text dStartLabel = new Text("Tief (min.): ");
        dStartLabel.setFont(Font.font("Arial", 14));
        dStart.textProperty().addListener(new ChangeListener<String>() {
          @Override
          public void changed(ObservableValue<? extends String> observable, String oldValue,
              String newValue) {
            if (!newValue.matches("\\d{0,7}([\\.]\\d{0,4})?")) {
              dStart.setText(oldValue);
            } else {
              mdEnd.setText(dStart.getText());
            }
          }
        });

        HBox dStartBox = new HBox();
        dStartBox.getChildren().addAll(dStartLabel, dStart);

        Text dEndLabel = new Text("Tief (max.): ");
        dEndLabel.setFont(Font.font("Arial", 14));
        dStart.textProperty().addListener(new ChangeListener<String>() {
          @Override
          public void changed(ObservableValue<? extends String> observable, String oldValue,
              String newValue) {
            if (!newValue.matches("\\d{0,7}([\\.]\\d{0,4})?")
                || Double.parseDouble(newValue) < Double.parseDouble(dStart.getText())) {
              dStart.setText(oldValue);
            }
          }
        });

        HBox dEndBox = new HBox();
        dEndBox.getChildren().addAll(dEndLabel, dEnd);

        Text vdStartLabel = new Text("Sehr tief (min.): > \"Tief (max.)\"");
        vdStartLabel.setFont(Font.font("Arial", 14));

        HBox vdStartBox = new HBox();
        vdStartBox.getChildren().addAll(vdStartLabel);

        VBox leftBox = new VBox(25);
        leftBox.setPadding(new Insets(20, 20, 20, 20));
        leftBox.getChildren().addAll(depth, vsStartBox, vsEndBox, sStartBox, sEndBox);
        pane.setLeft(leftBox);

        Text placeholder = new Text("");

        VBox rightBox = new VBox(25);
        rightBox.setPadding(new Insets(20, 20, 20, 20));
        rightBox.getChildren().addAll(placeholder, mdStartBox, mdEndBox, dStartBox, dEndBox,
            vdStartBox);
        pane.setRight(rightBox);

        Button back = new Button("Zurück");
        back.setOnAction(new EventHandler<ActionEvent>() {
          @Override
          public void handle(ActionEvent arg0) {
            pane.getChildren().clear();
            gsehenInstance.sendFarmDataChanged(field, null);
          }
        });

        HBox buttonBox = new HBox();

        Button save = new Button("Speichern");
        save.setOnAction(new EventHandler<ActionEvent>() {
          @Override
          public void handle(ActionEvent arg0) {

            if (Double.parseDouble(dStart.getText()) > Double.parseDouble(dEnd.getText())
                || Double.parseDouble(mdStart.getText()) > Double.parseDouble(mdEnd.getText())
                || Double.parseDouble(vsStart.getText()) > Double.parseDouble(vsEnd.getText())
                || Double.parseDouble(sStart.getText()) > Double.parseDouble(sEnd.getText())) {
              Text error = new Text("Ein Minimum kann nicht größer, als ein Maximum sein!");
              error.setFont(Font.font("Verdana", 20));
              error.setFill(Color.RED);
              buttonBox.getChildren().clear();
              buttonBox.getChildren().addAll(back, save, error);
            } else {

              pane.getChildren().clear();

              Soil soil = new Soil();
              soil.setName(soilName.getText());
              soil.setDescription(descriptionArea.getText());
              soil.setAvailableWaterCapacity(Double.parseDouble(soilAwc.getText()));

              SoilProfileDepth spd1 = new SoilProfileDepth();
              spd1.setDepthStart(Double.parseDouble(vsStart.getText()));
              spd1.setDepthEnd(Double.parseDouble(vsEnd.getText()));

              SoilProfileDepth spd2 = new SoilProfileDepth();
              spd2.setDepthStart(Double.parseDouble(sStart.getText()));
              spd2.setDepthEnd(Double.parseDouble(sEnd.getText()));

              SoilProfileDepth spd3 = new SoilProfileDepth();
              spd3.setDepthStart(Double.parseDouble(mdStart.getText()));
              spd3.setDepthEnd(Double.parseDouble(mdEnd.getText()));

              SoilProfileDepth spd4 = new SoilProfileDepth();
              spd4.setDepthStart(Double.parseDouble(dStart.getText()));
              spd4.setDepthEnd(Double.parseDouble(dEnd.getText()));

              Set<Soil> soilSet = new HashSet<Soil>();
              soilSet.add(soil);
              Set<SoilProfileDepth> spdSet = new HashSet<SoilProfileDepth>();
              spdSet.add(spd1);
              spdSet.add(spd2);
              spdSet.add(spd3);
              spdSet.add(spd4);
              soilProfile = new SoilProfile();
              soilProfile.setSoilType(soilSet);
              soilProfile.setProfileDepth(spdSet);

              gsehenInstance.sendFarmDataChanged(field, null);
            }
          }
        });

        buttonBox.getChildren().addAll(back, save);

        VBox bottomBox = new VBox(25);
        bottomBox.setPadding(new Insets(20, 20, 20, 20));
        bottomBox.getChildren().addAll(buttonBox);
        pane.setBottom(bottomBox);

      }
    });

    HBox soilStartBox = new HBox();
    soilStartBox.getChildren().addAll(rootingZoneLabel, rootingZone);

    VBox leftBox = new VBox(50);
    leftBox.setPadding(new Insets(20, 20, 20, 20));
    leftBox.getChildren().addAll(soilBox, soilStartBox, createSoil);
    pane.setLeft(leftBox);
    // LEFT END ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    Button save = new Button("Speichern");
    save.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent e) {
        field.setName(name.getText());
        field.setArea(Double.valueOf(area.getText()));
        field.setSoilProfile(soilProfile);
        field.setRootingZone(Double.valueOf(rootingZone.getText()));

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
