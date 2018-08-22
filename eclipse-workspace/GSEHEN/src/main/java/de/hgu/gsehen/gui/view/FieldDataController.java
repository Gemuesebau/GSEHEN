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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
// import java.util.logging.Logger;
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
  private int layer;
  private HBox buttonBox;

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

    HBox soilBox = new HBox();
    soilBox.getChildren().addAll(soilProfile, soilChoiceBox);

    createSoil = new Button("Bodentyp ertsellen");
    createSoil.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent e) {
        pane.getChildren().clear();

        Text soilNameLabel = new Text("Name des Profils: ");
        soilNameLabel.setFont(Font.font("Arial", 14));
        TextField soilProfileName = new TextField("");

        HBox nameBox = new HBox();
        nameBox.getChildren().addAll(soilNameLabel, soilProfileName);

        layer = 1;

        Text depth = new Text("Schicht #" + layer);
        depth.setFont(Font.font("Arial", FontWeight.BOLD, 18));

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

        HBox soilBox = new HBox();
        soilBox.getChildren().addAll(soil, soilChoiceBox);

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

        HBox awcBox = new HBox();
        awcBox.getChildren().addAll(soilAwcLabel, soilAwc);

        VBox topBox = new VBox(25);
        topBox.setPadding(new Insets(20, 20, 20, 20));
        topBox.getChildren().addAll(nameBox, depth, soilBox, awcBox);
        pane.setTop(topBox);

        TextField depthStart = new TextField("0");
        Text depthStartLabel = new Text("Tiefe (min.): ");
        depthStartLabel.setFont(Font.font("Arial", 14));
        depthStart.textProperty().addListener(new ChangeListener<String>() {
          @Override
          public void changed(ObservableValue<? extends String> observable, String oldValue,
              String newValue) {
            if (newValue != null) {
              if (!newValue.matches("\\d{0,7}([\\.]\\d{0,4})?")) {
                depthStart.setText(oldValue);
              }
            }
          }
        });

        HBox depthStartBox = new HBox();
        depthStartBox.getChildren().addAll(depthStartLabel, depthStart);

        TextField depthEnd = new TextField("25");
        Text depthEndLabel = new Text("Tiefe (max.): ");
        depthEndLabel.setFont(Font.font("Arial", 14));
        depthEnd.textProperty().addListener(new ChangeListener<String>() {
          @Override
          public void changed(ObservableValue<? extends String> observable, String oldValue,
              String newValue) {
            if (newValue != null) {
              if (!newValue.matches("\\d{0,7}([\\.]\\d{0,4})?")) {
                depthEnd.setText(oldValue);
              }
            }
          }
        });

        Set<Soil> soilSet = new HashSet<Soil>();
        Set<SoilProfileDepth> spdSet = new HashSet<SoilProfileDepth>();

        soilProfileItem = new SoilProfile();
        VBox leftBox = new VBox(25);

        Button setSoil = new Button("Schicht abschließen");
        setSoil.setOnAction(new EventHandler<ActionEvent>() {
          @Override
          public void handle(ActionEvent arg0) {

            if (Double.parseDouble(depthStart.getText()) > Double.parseDouble(depthEnd.getText())) {
              Text error = new Text("Ein Minimum kann nicht größer, als ein Maximum sein!");
              error.setFont(Font.font("Verdana", 20));
              error.setFill(Color.RED);
              buttonBox.getChildren().clear();
              buttonBox.getChildren().addAll(back, save, error);
            } else if (soilChoiceBox.getValue() != null && depthStart.getText() != null
                && depthEnd.getText() != null && soilProfileName.getText() != null) {
              Soil soil = new Soil();
              soil.setName(soilChoiceBox.getValue().getName());
              soil.setAvailableWaterCapacity(Double.parseDouble(soilAwc.getText()));

              SoilProfileDepth spd = new SoilProfileDepth();
              spd.setDepthStart(Double.parseDouble(depthStart.getText()));
              spd.setDepthEnd(Double.parseDouble(depthEnd.getText()));

              soilSet.add(soil);
              spdSet.add(spd);

              soilChoiceBox.setValue(null);
              soilAwc.setText(null);
              depthStart.setText(String.valueOf(spd.getDepthEnd()));
              depthEnd.setText(String.valueOf(spd.getDepthEnd() + 25));

              Text createdSoil = new Text("Schicht #" + layer + ": \n" + "Bodentyp: "
                  + soil.getName() + ", Wasserhaltekapazität: " + soil.getAvailableWaterCapacity()
                  + ", Tiefe(min.):" + spd.getDepthStart() + ", Tiefe(max.): " + spd.getDepthEnd()
                  + "\n\n");
              createdSoil.setFont(Font.font("Arial", FontWeight.BOLD, 14));
              leftBox.getChildren().add(createdSoil);
              depth.setText("Schicht #" + (layer += 1));
            } else {
              Text error = new Text("Es wurden nicht alle Felder ausgefüllt!");
              error.setFont(Font.font("Verdana", 20));
              error.setFill(Color.RED);
              buttonBox.getChildren().clear();
              buttonBox.getChildren().addAll(back, save, error);
            }
          }
        });

        HBox depthEndBox = new HBox();
        depthEndBox.getChildren().addAll(depthEndLabel, depthEnd);

        leftBox.setPadding(new Insets(20, 20, 20, 20));
        leftBox.getChildren().addAll(depthStartBox, depthEndBox, setSoil);
        pane.setLeft(leftBox);

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
    leftBox.getChildren().addAll(soilBox, createSoil);
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
