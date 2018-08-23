package de.hgu.gsehen.gui.view;

import de.hgu.gsehen.Gsehen;
import de.hgu.gsehen.event.FarmDataChanged;
import de.hgu.gsehen.event.GsehenEventListener;
import de.hgu.gsehen.model.Drawable;
import de.hgu.gsehen.model.Field;
import de.hgu.gsehen.model.Soil;
import de.hgu.gsehen.model.SoilProfile;
import de.hgu.gsehen.model.SoilProfileDepth;
import java.util.ArrayList;
import java.util.List;
// import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.StringConverter;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.hibernate.Session;
import org.hibernate.query.Query;

public class FieldDataController implements GsehenEventListener<FarmDataChanged> {
  private static final String FARM_TREE_VIEW_ID = "#farmTreeView";
  // private static final Logger LOGGER = Logger.getLogger(Gsehen.class.getName());

  private List<SoilProfile> soilList = new ArrayList<>();

  private TreeItem<Drawable> selectedItem;
  private Field field;

  private Gsehen gsehenInstance;
  private BorderPane pane;
  private TreeTableView<Drawable> treeTableView;

  private Text nameLabel;
  private Text areaLabel;

  private TextField name;
  private Text area;

  private Button createSoil;
  private SoilProfile soilProfileItem;
  private Button save;
  private Button back;
  private HBox buttonBox;
  private List<Text> layorList;
  private int index;

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
    index = 1;

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
    topBox.setSpacing(5);
    topBox.getChildren().addAll(nameBox, locationBox);
    pane.setTop(topBox);
    // TOP END ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    // LEFT ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    Text soilProfile = new Text("Bodenprofil: ");
    soilProfile.setFont(Font.font("Arial", 14));

    EntityManagerFactory emf = Persistence.createEntityManagerFactory("GSEHEN");
    EntityManager em = emf.createEntityManager();
    if (soilList.isEmpty()) {
      try {
        Session session = em.unwrap(Session.class);
        Query<SoilProfile> query = session.createQuery("from SoilProfile");
        soilList = query.list();
      } finally {
        em.close();
      }
    }

    ChoiceBox<SoilProfile> soilChoiceBox = new ChoiceBox<SoilProfile>();
    if (!soilList.isEmpty()) {
      for (SoilProfile s : soilList) {
        soilChoiceBox.getItems().add(s);
      }
      soilChoiceBox.setConverter(new StringConverter<SoilProfile>() {

        @Override
        public String toString(SoilProfile object) {
          return object.getName();
        }

        @Override
        public SoilProfile fromString(String string) {
          return soilChoiceBox.getItems().stream().filter(ap -> ap.getName().equals(string))
              .findFirst().orElse(null);
        }
      });
    }

    HBox soilProfileBox = new HBox();
    soilProfileBox.getChildren().addAll(soilProfile, soilChoiceBox);

    createSoil = new Button("Bodentyp ertsellen");
    createSoil.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent e) {
        // CREATE SOILPROFILE ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        pane.getChildren().clear();

        Text soilNameLabel = new Text("Name des Profils: ");
        soilNameLabel.setFont(Font.font("Arial", 14));
        TextField soilProfileName = new TextField("");

        HBox nameBox = new HBox();
        nameBox.getChildren().addAll(soilNameLabel, soilProfileName);

        layorList = new ArrayList<Text>();

        Text layorText = new Text("Schicht #" + (layorList.size() + 1));
        layorText.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        Text soil = new Text("Bodentyp: ");
        soil.setFont(Font.font("Arial", 14));

        // TODO: Hardcoded.
        Soil sand = new Soil();
        sand.setName("Sand");
        sand.setAvailableWaterCapacity(8);
        Soil sandyLoam = new Soil();
        sandyLoam.setName("Sandiger Lehm");
        sandyLoam.setAvailableWaterCapacity(12);
        Soil loam = new Soil();
        loam.setName("Lehm");
        loam.setAvailableWaterCapacity(17);
        Soil clayLoam = new Soil();
        clayLoam.setName("Toniger Lehm");
        clayLoam.setAvailableWaterCapacity(18);
        Soil siltyClay = new Soil();
        siltyClay.setName("Schluffiger Ton");
        siltyClay.setAvailableWaterCapacity(20);
        Soil clay = new Soil();
        clay.setName("Ton");
        clay.setAvailableWaterCapacity(23);

        List<Soil> soils = new ArrayList<Soil>();
        soils.add(sand);
        soils.add(sandyLoam);
        soils.add(loam);
        soils.add(clayLoam);
        soils.add(siltyClay);
        soils.add(clay);

        ChoiceBox<Soil> soilChoiceBox = new ChoiceBox<Soil>();
        soilChoiceBox.getItems().addAll(soils);
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

        Text soilAwcLabel = new Text("Verfügbare Wasserhaltekapazität: ");
        soilAwcLabel.setFont(Font.font("Arial", 14));
        TextField soilAwc = new TextField();

        ChangeListener<Soil> changeListener = new ChangeListener<Soil>() {
          @Override
          public void changed(ObservableValue<? extends Soil> observable, //
              Soil oldValue, Soil newValue) {
            if (newValue != null) {
              soilAwc.setText(String.valueOf(soilChoiceBox.getValue().getAvailableWaterCapacity()));
            }
          }
        };
        soilChoiceBox.getSelectionModel().selectedItemProperty().addListener(changeListener);

        VBox topBox = new VBox(25);
        topBox.setPadding(new Insets(20, 20, 20, 20));
        topBox.getChildren().addAll(nameBox);
        pane.setTop(topBox);

        TextField depth = new TextField("25");
        Text depthLabel = new Text("Tiefe (in cm): ");
        depthLabel.setFont(Font.font("Arial", 14));
        depth.textProperty().addListener(new ChangeListener<String>() {
          @Override
          public void changed(ObservableValue<? extends String> observable, String oldValue,
              String newValue) {
            if (newValue != null) {
              if (!newValue.matches("\\d{0,7}([\\.]\\d{0,4})?")) {
                depth.setText(oldValue);
              }
            }
          }
        });

        // GridPane - Center Section
        GridPane center = new GridPane();

        // GridPane Configuration (Padding, Gaps, etc.)
        center.setPadding(new Insets(20, 20, 20, 20));
        center.setHgap(15);
        center.setVgap(15);
        center.setGridLinesVisible(false);

        // Set Column and Row Constraints
        ColumnConstraints column1 = new ColumnConstraints(200, 100, 300);
        ColumnConstraints column2 = new ColumnConstraints(200, 100, 100);
        column1.setHgrow(Priority.ALWAYS);
        column2.setHgrow(Priority.ALWAYS);
        RowConstraints rowEmpty = new RowConstraints();

        // Add Constraints to Columns & Rows
        center.getColumnConstraints().addAll(column1, column2);
        center.getRowConstraints().add(0, rowEmpty);
        center.getRowConstraints().add(1, rowEmpty);

        List<Soil> soilSet = new ArrayList<Soil>();
        List<SoilProfileDepth> spdSet = new ArrayList<SoilProfileDepth>();

        soilProfileItem = new SoilProfile();

        Button setSoil = new Button("Schicht abschließen");
        setSoil.setOnAction(new EventHandler<ActionEvent>() {
          @Override
          public void handle(ActionEvent arg0) {

            if (soilChoiceBox.getValue() != null && depth.getText() != null
                && soilProfileName.getText() != null) {
              Soil soil = new Soil();
              soil.setName(soilChoiceBox.getValue().getName());
              soil.setAvailableWaterCapacity(Double.parseDouble(soilAwc.getText()));

              SoilProfileDepth spd = new SoilProfileDepth();
              spd.setDepth(Double.parseDouble(depth.getText()));

              soilSet.add(soil);
              spdSet.add(spd);

              soilChoiceBox.setValue(null);
              soilAwc.setText(null);
              depth.setText(String.valueOf(spd.getDepth()));

              Text createdSoil = new Text(
                  "Schicht #" + (layorList.size() + 1) + ": \n" + "Bodentyp: " + soil.getName()
                      + "; Wasserhaltekapazität: " + soil.getAvailableWaterCapacity()
                      + "; Tiefe (in cm): " + spd.getDepth() + "\n\n");
              createdSoil.setFont(Font.font("Arial", FontWeight.BOLD, 14));
              GridPane.setHalignment(createdSoil, HPos.LEFT);
              GridPane.setConstraints(createdSoil, 0, 4 + layorList.size() + 1);
              layorList.add(createdSoil);

              Button delSoil = new Button("Schicht löschen");
              delSoil.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent arg0) {
                  // TODO: #xy nach dem Löschen und Layout (Text/Button) anpassen!
                  soilSet.remove(soil);
                  spdSet.remove(spd);
                  layorList.remove(createdSoil);
                  center.getChildren().removeAll(createdSoil, delSoil);
                  depth.setText("Schicht #" + (layorList.size() - 1));

                  for (Text t : layorList) {
                    t.setText(t.getText().substring(0, 9) + index + t.getText().substring(10));
                    index += 1;
                  }
                }
              });
              GridPane.setHalignment(delSoil, HPos.LEFT);
              GridPane.setConstraints(delSoil, 1, 4 + layorList.size());

              center.getChildren().addAll(createdSoil, delSoil);
              depth.setText("Schicht #" + (layorList.size() + 1));
            } else {
              Text error = new Text("Es wurden nicht alle Felder ausgefüllt!");
              error.setFont(Font.font("Verdana", 20));
              error.setFill(Color.RED);
              buttonBox.getChildren().clear();
              buttonBox.getChildren().addAll(back, save, error);
            }
          }
        });

        // Set Nodes Vertical & Horizontal Alignment
        GridPane.setHalignment(layorText, HPos.LEFT);
        GridPane.setHalignment(soil, HPos.LEFT);
        GridPane.setHalignment(soilChoiceBox, HPos.LEFT);
        GridPane.setHalignment(soilAwcLabel, HPos.LEFT);
        GridPane.setHalignment(soilAwc, HPos.LEFT);
        GridPane.setHalignment(depthLabel, HPos.LEFT);
        GridPane.setHalignment(depth, HPos.LEFT);
        GridPane.setHalignment(setSoil, HPos.LEFT);

        // Set Row & Column Index for Nodes
        GridPane.setConstraints(layorText, 0, 0, 2, 1);
        GridPane.setConstraints(soil, 0, 1);
        GridPane.setConstraints(soilChoiceBox, 1, 1);
        GridPane.setConstraints(soilAwcLabel, 0, 2);
        GridPane.setConstraints(soilAwc, 1, 2);
        GridPane.setConstraints(depthLabel, 0, 3);
        GridPane.setConstraints(depth, 1, 3);
        GridPane.setConstraints(setSoil, 0, 4);

        center.getChildren().addAll(layorText, soil, soilChoiceBox, soilAwcLabel, soilAwc,
            depthLabel, depth, setSoil);
        pane.setCenter(center);
        // CREATE SOILPROFILE END ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        back = new Button("Zurück");
        back.setOnAction(new EventHandler<ActionEvent>() {
          @Override
          public void handle(ActionEvent arg0) {
            pane.getChildren().clear();
            gsehenInstance.sendFarmDataChanged(field, null);
          }
        });

        buttonBox = new HBox();

        save = new Button("Speichern");
        save.setOnAction(new EventHandler<ActionEvent>() {
          @Override
          public void handle(ActionEvent arg0) {
            pane.getChildren().clear();
            soilProfileItem = new SoilProfile();
            soilProfileItem.setSoilType(soilSet);
            soilProfileItem.setProfileDepth(spdSet);
            soilProfileItem.setName(soilProfileName.getText());

            soilList.add(soilProfileItem);
            pane.getChildren().clear();
            gsehenInstance.sendFarmDataChanged(field, null);
          }
        });

        buttonBox.getChildren().addAll(back, save);

        VBox bottomBox = new VBox(25);
        bottomBox.setPadding(new Insets(20, 20, 20, 20));
        bottomBox.getChildren().addAll(buttonBox);
        pane.setBottom(bottomBox);

      }
    });

    VBox leftBox = new VBox(50);
    leftBox.setPadding(new Insets(20, 20, 20, 20));
    leftBox.getChildren().addAll(soilProfileBox, createSoil);
    pane.setLeft(leftBox);
    // LEFT END ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    Button saveField = new Button("Speichern");
    saveField.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent e) {
        field.setArea(Double.valueOf(area.getText()));
        for (SoilProfile sp : soilList) {
          if (sp == soilChoiceBox.getValue()) {
            field.setSoilProfile(sp);
          }
        }

        gsehenInstance.sendFarmDataChanged(field, null);
      }
    });
    pane.setBottom(saveField);

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
                  pane.setVisible(true);
                  field = (Field) selectedItem.getValue();

                  name.setText(field.getName());

                  area.setText(String.valueOf(field.getArea()));

                  for (SoilProfile soPr : soilList) {
                    if (field.getSoilProfile() != null
                        && soPr.getName().equals(field.getSoilProfile().getName())) {
                      soilChoiceBox.getSelectionModel().select(soPr);
                    }
                  }

                } else {
                  pane.setVisible(false);
                }
              }
            }
          }
        });
  }
}
