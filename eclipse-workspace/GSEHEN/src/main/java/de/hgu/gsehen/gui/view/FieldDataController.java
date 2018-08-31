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
import java.util.Locale;
import java.util.ResourceBundle;
// import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ScrollPane;
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
import javafx.scene.text.FontPosture;
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
  protected static final ResourceBundle mainBundle =
      ResourceBundle.getBundle("i18n.main", Locale.GERMAN);
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

  private ChoiceBox<SoilProfile> currentSoilBox;
  private SoilProfile sp;
  private Button createSoil;
  private SoilProfile soilProfileItem;
  private Button save;
  private Button back;
  private HBox buttonBox;
  private List<Text> layorList;
  private int index;
  private VBox center;
  private Button saveField;

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
    GridPane grid = new GridPane();

    grid.setPadding(new Insets(20, 20, 20, 20));
    grid.setHgap(15);
    grid.setVgap(15);
    grid.setGridLinesVisible(false);

    ColumnConstraints column1 = new ColumnConstraints(200, 100, 300);
    ColumnConstraints column2 = new ColumnConstraints(200, 100, 100);
    column1.setHgrow(Priority.ALWAYS);
    column2.setHgrow(Priority.ALWAYS);
    RowConstraints rowEmpty = new RowConstraints();

    grid.getColumnConstraints().addAll(column1, column2);
    grid.getRowConstraints().add(0, rowEmpty);
    grid.getRowConstraints().add(1, rowEmpty);

    nameLabel = new Text(mainBundle.getString("fieldview.name"));
    nameLabel.setFont(Font.font("Arial", 14));
    name = new TextField("");

    areaLabel = new Text(mainBundle.getString("fieldview.area"));
    areaLabel.setFont(Font.font("Arial", 14));
    area = new Text("");
    area.setFont(Font.font("Arial", FontWeight.BOLD, 14));

    Text soilProfile = new Text(mainBundle.getString("fieldview.soilprofile"));
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

    currentSoilBox = new ChoiceBox<SoilProfile>();
    if (!soilList.isEmpty()) {
      for (SoilProfile s : soilList) {
        currentSoilBox.getItems().add(s);
      }
      currentSoilBox.setConverter(new StringConverter<SoilProfile>() {

        @Override
        public String toString(SoilProfile object) {
          return object.getName();
        }

        @Override
        public SoilProfile fromString(String string) {
          return currentSoilBox.getItems().stream().filter(ap -> ap.getName().equals(string))
              .findFirst().orElse(null);
        }
      });
    }

    Button editProfile = new Button(mainBundle.getString("fieldview.editprofile"));
    editProfile.setOnAction(new EventHandler<ActionEvent>() {

      @Override
      public void handle(ActionEvent e) {
        if (currentSoilBox.getValue() != null) {
          pane.getChildren().clear();
          treeTableView.setVisible(false);

          Text soilNameLabel = new Text(mainBundle.getString("fieldview.profilename"));
          soilNameLabel.setFont(Font.font("Arial", 14));
          TextField soilProfileName = new TextField(currentSoilBox.getValue().getName());
          soilProfileName.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue,
                String newValue) {
              currentSoilBox.getValue().setName(soilProfileName.getText());
            }
          });

          HBox nameBox = new HBox();
          nameBox.getChildren().addAll(soilNameLabel, soilProfileName);

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

          int row = 0;

          for (int i = 0; i < currentSoilBox.getValue().getSoilType().size(); i++) {
            Text layor = new Text(mainBundle.getString("fieldview.layor") + (i + 1));
            layor.setFont(Font.font("Arial", FontWeight.BOLD, 14));

            Text soil = new Text(mainBundle.getString("fieldview.soiltype"));
            soil.setFont(Font.font("Arial", 14));

            Soil s = new Soil();
            List<Soil> soils = s.soils();

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

            Soil curSoil = currentSoilBox.getValue().getSoilType().get(i);
            Text soilAwc = new Text();
            for (Soil setSoil : soilChoiceBox.getItems()) {
              if (setSoil.getName().equals(curSoil.getName())) {
                soilChoiceBox.getSelectionModel().select(setSoil);
                soilAwc.setText(String.valueOf(setSoil.getAvailableWaterCapacity()));
              }
            }

            Text soilAwcLabel = new Text(mainBundle.getString("fieldview.soilawc"));
            soilAwcLabel.setFont(Font.font("Arial", 14));

            int in = i;
            ChangeListener<Soil> changeListener = new ChangeListener<Soil>() {
              @Override
              public void changed(ObservableValue<? extends Soil> observable, //
                  Soil oldValue, Soil newValue) {
                if (newValue != null) {
                  soilAwc.setText(
                      String.valueOf(soilChoiceBox.getValue().getAvailableWaterCapacity()));
                  currentSoilBox.getValue().getSoilType().get(in)
                      .setName(soilChoiceBox.getValue().getName());
                  currentSoilBox.getValue().getSoilType().get(in).setAvailableWaterCapacity(
                      soilChoiceBox.getValue().getAvailableWaterCapacity());
                }
              }
            };
            soilChoiceBox.getSelectionModel().selectedItemProperty().addListener(changeListener);

            VBox topBox = new VBox(25);
            topBox.setPadding(new Insets(20, 20, 20, 20));
            topBox.getChildren().addAll(nameBox);
            pane.setTop(topBox);

            TextField depth = new TextField(
                String.valueOf(currentSoilBox.getValue().getProfileDepth().get(i).getDepth()));
            Text depthLabel = new Text(mainBundle.getString("fieldview.depth"));
            depthLabel.setFont(Font.font("Arial", 14));
            depth.textProperty().addListener(new ChangeListener<String>() {
              @Override
              public void changed(ObservableValue<? extends String> observable, String oldValue,
                  String newValue) {
                if (newValue != null) {
                  if (!newValue.matches("\\d{0,7}([\\.]\\d{0,4})?")) {
                    depth.setText(oldValue);
                  } else {
                    currentSoilBox.getValue().getProfileDepth().get(in)
                        .setDepth(Double.valueOf(newValue));
                  }
                }
              }
            });
            // Set Nodes Vertical & Horizontal Alignment
            GridPane.setHalignment(layor, HPos.LEFT);
            GridPane.setHalignment(soil, HPos.LEFT);
            GridPane.setHalignment(soilChoiceBox, HPos.LEFT);
            GridPane.setHalignment(soilAwcLabel, HPos.LEFT);
            GridPane.setHalignment(soilAwc, HPos.LEFT);
            GridPane.setHalignment(depthLabel, HPos.LEFT);
            GridPane.setHalignment(depth, HPos.LEFT);

            // Set Row & Column Index for Nodes
            GridPane.setConstraints(layor, 0, row);
            row += 1;
            GridPane.setConstraints(soil, 0, row);
            GridPane.setConstraints(soilChoiceBox, 1, row);
            row += 1;
            GridPane.setConstraints(soilAwcLabel, 0, row);
            GridPane.setConstraints(soilAwc, 1, row);
            row += 1;
            GridPane.setConstraints(depthLabel, 0, row);
            GridPane.setConstraints(depth, 1, row);
            row += 1;

            center.getChildren().addAll(layor, soil, soilChoiceBox, soilAwcLabel, soilAwc,
                depthLabel, depth);
          }

          ScrollPane scrollPane = new ScrollPane();
          scrollPane.setContent(center);
          scrollPane.setPannable(true);
          pane.setCenter(scrollPane);

          back = new Button(mainBundle.getString("fieldview.editend"));
          back.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
              pane.getChildren().clear();
              treeTableView.setVisible(true);
              gsehenInstance.sendFarmDataChanged(field, null);
            }
          });
          pane.setBottom(back);
        }
      }
    });

    createSoil = new Button(mainBundle.getString("fieldview.createprofile"));
    createSoil.setOnAction(new EventHandler<ActionEvent>() {

      @Override
      public void handle(ActionEvent e) {
        // CREATE SOILPROFILE ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        pane.getChildren().clear();
        treeTableView.setVisible(false);

        Text soilNameLabel = new Text(mainBundle.getString("fieldview.profilename"));
        soilNameLabel.setFont(Font.font("Arial", 14));
        TextField soilProfileName = new TextField("");

        HBox nameBox = new HBox();
        nameBox.getChildren().addAll(soilNameLabel, soilProfileName);

        layorList = new ArrayList<Text>();

        Text layorText = new Text(mainBundle.getString("fieldview.layor") + (layorList.size() + 1));
        layorText.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        Text soil = new Text(mainBundle.getString("fieldview.soiltype"));
        soil.setFont(Font.font("Arial", 14));

        Soil s = new Soil();
        List<Soil> soils = s.soils();

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

        Text soilAwcLabel = new Text(mainBundle.getString("fieldview.soilawc"));
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
        Text depthLabel = new Text(mainBundle.getString("fieldview.depth"));
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

        Button setSoil = new Button(mainBundle.getString("fieldview.setsoil"));
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

              Text createdSoil =
                  new Text(mainBundle.getString("fieldview.layor") + (layorList.size() + 1) + ": \n"
                      + mainBundle.getString("fieldview.soiltype") + soil.getName() + ";\n"
                      + mainBundle.getString("fieldview.awc") + soil.getAvailableWaterCapacity()
                      + ";\n" + mainBundle.getString("fieldview.depth") + spd.getDepth() + "\n\n");
              createdSoil.setFont(Font.font("Arial", FontPosture.ITALIC, 14));
              GridPane.setHalignment(createdSoil, HPos.LEFT);
              GridPane.setConstraints(createdSoil, 0, 4 + layorList.size() + 1);
              layorList.add(createdSoil);

              layorText.setText(mainBundle.getString("fieldview.layor") + (layorList.size() + 1));

              Button delSoil = new Button(mainBundle.getString("fieldview.delsoil"));
              delSoil.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent arg0) {
                  soilSet.remove(soil);
                  spdSet.remove(spd);
                  layorList.remove(createdSoil);
                  center.getChildren().removeAll(createdSoil, delSoil);
                  depth.setText(mainBundle.getString("fieldview.layor") + (layorList.size() - 1));
                  layorText
                      .setText(mainBundle.getString("fieldview.layor") + (layorList.size() + 1));

                  for (Text t : layorList) {
                    t.setText(t.getText().substring(0, 9) + index + t.getText().substring(10));
                    index += 1;
                  }
                }
              });
              GridPane.setHalignment(delSoil, HPos.LEFT);
              GridPane.setConstraints(delSoil, 1, 4 + layorList.size());

              center.getChildren().addAll(createdSoil, delSoil);
              depth.setText(mainBundle.getString("fieldview.layor") + (layorList.size() + 1));
            } else {
              Text error = new Text(mainBundle.getString("fieldview.error"));
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
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(center);
        scrollPane.setPannable(true);
        pane.setCenter(scrollPane);
        // CREATE SOILPROFILE END ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        back = new Button(mainBundle.getString("fieldview.back"));
        back.setOnAction(new EventHandler<ActionEvent>() {
          @Override
          public void handle(ActionEvent arg0) {
            pane.getChildren().clear();
            treeTableView.setVisible(true);
            gsehenInstance.sendFarmDataChanged(field, null);
          }
        });

        buttonBox = new HBox();

        save = new Button(mainBundle.getString("menu.file.save"));
        save.setOnAction(new EventHandler<ActionEvent>() {
          @Override
          public void handle(ActionEvent arg0) {
            pane.getChildren().clear();
            treeTableView.setVisible(true);
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

    saveField = new Button(mainBundle.getString("menu.file.save"));
    saveField.setOnAction(new EventHandler<ActionEvent>() {

      @Override
      public void handle(ActionEvent e) {
        field.setName(name.getText());
        field.setArea(Double.valueOf(area.getText()));
        for (SoilProfile sp : soilList) {
          if (sp == currentSoilBox.getValue()) {
            field.setSoilProfile(sp);
          }
        }
        gsehenInstance.sendFarmDataChanged(field, null);
      }
    });

    GridPane.setHalignment(nameLabel, HPos.LEFT);
    GridPane.setHalignment(name, HPos.LEFT);
    GridPane.setHalignment(areaLabel, HPos.LEFT);
    GridPane.setHalignment(area, HPos.LEFT);
    GridPane.setHalignment(soilProfile, HPos.LEFT);
    GridPane.setHalignment(currentSoilBox, HPos.LEFT);
    GridPane.setHalignment(editProfile, HPos.LEFT);
    GridPane.setHalignment(createSoil, HPos.LEFT);
    GridPane.setHalignment(saveField, HPos.LEFT);

    GridPane.setConstraints(nameLabel, 0, 0);
    GridPane.setConstraints(name, 1, 0);
    GridPane.setConstraints(areaLabel, 0, 1);
    GridPane.setConstraints(area, 1, 1);
    GridPane.setConstraints(soilProfile, 0, 2);
    GridPane.setConstraints(currentSoilBox, 1, 2);
    GridPane.setConstraints(editProfile, 2, 2);
    GridPane.setConstraints(createSoil, 0, 3);
    GridPane.setConstraints(saveField, 0, 4);

    grid.getChildren().addAll(nameLabel, name, areaLabel, area, soilProfile, currentSoilBox,
        editProfile, createSoil, saveField);

    pane.setTop(grid);
    // TOP END ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    // BOTTOM ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    center = new VBox();
    center.setPadding(new Insets(10));
    center.setSpacing(8);
    // BOTTOM END ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

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

                  area.setText(String.valueOf(field.getPolygon().calculateArea()));

                  for (SoilProfile soPr : soilList) {
                    if (field.getSoilProfile() != null
                        && soPr.getName().equals(field.getSoilProfile().getName())) {
                      currentSoilBox.getSelectionModel().select(soPr);
                    }
                  }
                  if (field.getSoilProfile() != null) {
                    getCurrentProfile();
                  } else {
                    currentSoilBox.getSelectionModel().clearSelection();
                    pane.setBottom(null);
                  }
                } else {
                  pane.setVisible(false);
                }
              }
            }
          }
        });
  }

  /**
   * Shows current SoilProfile.
   */
  public void getCurrentProfile() {
    center.getChildren().clear();
    pane.setBottom(null);
    sp = field.getSoilProfile();
    int index = 1;
    Text setSoil =
        new Text(mainBundle.getString("fieldview.currentsoil") + " (" + sp.getName() + "):" + "\n");
    setSoil.setFont(Font.font("Arial", FontWeight.BOLD, 14));
    center.getChildren().add(setSoil);
    for (Soil soil : sp.getSoilType()) {
      Text createdSoil = new Text(mainBundle.getString("fieldview.layor") + (index) + ": \n"
          + mainBundle.getString("fieldview.soiltype") + soil.getName() + ";\n"
          + mainBundle.getString("fieldview.awc") + soil.getAvailableWaterCapacity() + ";\n"
          + mainBundle.getString("fieldview.depth") + sp.getProfileDepth().get(index - 1).getDepth()
          + "\n\n");
      center.getChildren().add(createdSoil);
      createdSoil.setFont(Font.font("Arial", FontPosture.ITALIC, 14));
      index++;
    }
    ScrollPane scrollPane = new ScrollPane();
    scrollPane.setContent(center);
    scrollPane.setPannable(true);
    pane.setBottom(scrollPane);
  }
}
