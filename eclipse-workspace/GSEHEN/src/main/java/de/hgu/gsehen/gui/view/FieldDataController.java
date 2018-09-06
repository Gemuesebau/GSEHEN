package de.hgu.gsehen.gui.view;

import static de.hgu.gsehen.util.CollectionUtil.getKeyForValue;

import de.hgu.gsehen.Gsehen;
import de.hgu.gsehen.event.FarmDataChanged;
import de.hgu.gsehen.event.GsehenEventListener;
import de.hgu.gsehen.model.Drawable;
import de.hgu.gsehen.model.Field;
import de.hgu.gsehen.model.Soil;
import de.hgu.gsehen.model.SoilProfile;
import de.hgu.gsehen.model.SoilProfileDepth;
import de.hgu.gsehen.model.WeatherDataSource;
import de.hgu.gsehen.util.DBUtil;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.TreeMap;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Hyperlink;
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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class FieldDataController extends Application
    implements GsehenEventListener<FarmDataChanged> {
  private static final String FARM_TREE_VIEW_ID = "#farmTreeView";
  protected final ResourceBundle mainBundle;

  private List<SoilProfile> soilProfileList;
  private List<WeatherDataSource> weatherDataSourceList;
  private FileChooser filePath;

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
  private ChoiceBox<WeatherDataSource> weatherData;
  private SoilProfile sp;
  private WeatherDataSource wds;
  private WeatherDataSource wdsFile;
  private Button createSoil;
  private Button save;
  private Button back;
  private HBox buttonBox;
  private List<Text> layorList;
  private int index;
  private VBox center;
  private Button saveField;

  private TextField weatherDataName;
  private TextField interval;
  private TextField windspeed;
  private TextField dateFormat;
  private ChoiceBox<String> localeId;
  private TextField path;
  private TextField locationLat;
  private TextField locationLng;
  private TextField metersAbove;
  private TreeMap<String, String> javaLocaleMap;
  private Text dateError = new Text();

  private void fillJavaLocaleMap(final Locale selectedLocale) {
    javaLocaleMap = new TreeMap<String, String>();
    java.lang.reflect.Field[] fieldArray = Locale.class.getFields();
    for (int i = 0; i < fieldArray.length; i++) {
      if (fieldArray[i].getType().equals(Locale.class)) {
        String language;
        try {
          language = ((Locale) fieldArray[i].get(null)).getDisplayLanguage(selectedLocale);
        } catch (Exception e) {
          language = null;
        }
        if (language != null && language.length() > 0) {
          final String fieldName = fieldArray[i].getName();
          javaLocaleMap.put(language + " (" + fieldName + ")", fieldName);
        }
      }
    }
  }

  {
    gsehenInstance = Gsehen.getInstance();
    soilProfileList = gsehenInstance.getSoilProfiles();
    weatherDataSourceList = gsehenInstance.getWeatherDataSources();

    final Locale selLocale = gsehenInstance.getSelectedLocale();
    mainBundle = ResourceBundle.getBundle("i18n.main", selLocale);
    fillJavaLocaleMap(selLocale);

    gsehenInstance.registerForEvent(FarmDataChanged.class, this);
  }

  /**
   * Constructs a new field data controller associated with the given BorderPane.
   *
   * @param pane
   *          - the associated BorderPane.
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

    currentSoilBox = new ChoiceBox<SoilProfile>();
    if (!soilProfileList.isEmpty()) {
      for (SoilProfile s : soilProfileList) {
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
        // CREATE SOILPROFILE
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
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

        List<Soil> soilList = new ArrayList<Soil>();
        List<SoilProfileDepth> soilDepthList = new ArrayList<SoilProfileDepth>();

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

              soilList.add(soil);
              soilDepthList.add(spd);

              soilChoiceBox.setValue(null);
              soilAwc.setText(null);
              depth.setText(String.valueOf(spd.getDepth()));

              Text createdSoil = new Text(
                  mainBundle.getString("fieldview.layor") + (layorList.size() + 1) + ": \n"
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
                  soilList.remove(soil);
                  soilDepthList.remove(spd);
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
        // CREATE SOILPROFILE END
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        back = new Button(mainBundle.getString("fieldview.back"));
        back.setOnAction(new EventHandler<ActionEvent>() {
          @Override
          public void handle(ActionEvent arg0) {
            pane.getChildren().clear();
            treeTableView.setVisible(true);
            gsehenInstance.sendFarmDataChanged(field, null);
          }
        });

        save = new Button(mainBundle.getString("menu.file.save"));
        save.setOnAction(new EventHandler<ActionEvent>() {
          @Override
          public void handle(ActionEvent arg0) {
            pane.getChildren().clear();
            treeTableView.setVisible(true);
            SoilProfile soilProfileItem = new SoilProfile(DBUtil.generateUuid());
            soilProfileItem.setSoilType(soilList);
            soilProfileItem.setProfileDepth(soilDepthList);
            soilProfileItem.setName(soilProfileName.getText());
            soilProfileList.add(soilProfileItem);
            pane.getChildren().clear();
            gsehenInstance.sendFarmDataChanged(field, null);
          }
        });

        buttonBox = new HBox();
        buttonBox.getChildren().addAll(back, save);

        VBox bottomBox = new VBox(25);
        bottomBox.setPadding(new Insets(20, 20, 20, 20));
        bottomBox.getChildren().addAll(buttonBox);
        pane.setBottom(bottomBox);

      }
    });

    Text weatherDataSource = new Text(mainBundle.getString("fieldview.weatherdatasource"));
    weatherDataSource.setFont(Font.font("Arial", 14));

    weatherData = new ChoiceBox<WeatherDataSource>();
    if (!weatherDataSourceList.isEmpty()) {
      for (WeatherDataSource s : weatherDataSourceList) {
        weatherData.getItems().add(s);
      }
      weatherData.setConverter(new StringConverter<WeatherDataSource>() {

        @Override
        public String toString(WeatherDataSource object) {
          return object.getName();
        }

        @Override
        public WeatherDataSource fromString(String string) {
          return weatherData.getItems().stream().filter(ap -> ap.getName().equals(string))
              .findFirst().orElse(null);
        }
      });
    }

    Button editWds = new Button(mainBundle.getString("fieldview.editwds"));
    editWds.setOnAction(new EventHandler<ActionEvent>() {

      @Override
      public void handle(ActionEvent e) {
        setWeatherDataSource();
        setWeatherDataTexts();
      }
    });

    Button createWds = new Button(mainBundle.getString("fieldview.createwds"));
    createWds.setOnAction(new EventHandler<ActionEvent>() {

      @Override
      public void handle(ActionEvent e) {
        setWeatherDataSource();
      }
    });

    saveField = new Button(mainBundle.getString("menu.file.save"));
    saveField.setOnAction(new EventHandler<ActionEvent>() {

      @Override
      public void handle(ActionEvent e) {
        field.setName(name.getText());
        field.setArea(Double.valueOf(area.getText()));
        for (SoilProfile sp : soilProfileList) {
          if (sp == currentSoilBox.getValue()) {
            field.setSoilProfileUuid(sp.getUuid());
          }
        }
        for (WeatherDataSource wds : weatherDataSourceList) {
          if (wds == weatherData.getValue()) {
            field.setWeatherDataSourceUuid(wds.getUuid());
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
    GridPane.setHalignment(weatherDataSource, HPos.LEFT);
    GridPane.setHalignment(weatherData, HPos.LEFT);
    GridPane.setHalignment(editWds, HPos.LEFT);
    GridPane.setHalignment(createWds, HPos.LEFT);
    GridPane.setHalignment(saveField, HPos.LEFT);

    GridPane.setConstraints(nameLabel, 0, 0);
    GridPane.setConstraints(name, 1, 0);
    GridPane.setConstraints(areaLabel, 0, 1);
    GridPane.setConstraints(area, 1, 1);
    GridPane.setConstraints(soilProfile, 0, 2);
    GridPane.setConstraints(currentSoilBox, 1, 2);
    GridPane.setConstraints(editProfile, 2, 2);
    GridPane.setConstraints(weatherDataSource, 0, 3);
    GridPane.setConstraints(weatherData, 1, 3);
    GridPane.setConstraints(editWds, 2, 3);
    GridPane.setConstraints(createSoil, 0, 4);
    GridPane.setConstraints(createWds, 1, 4);
    GridPane.setConstraints(saveField, 0, 5);

    grid.getChildren().addAll(nameLabel, name, areaLabel, area, soilProfile, currentSoilBox,
        editProfile, weatherDataSource, weatherData, editWds, createSoil, createWds, saveField);

    pane.setTop(grid);
    // TOP END ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    // BOTTOM ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    center = new VBox();
    center.setPadding(new Insets(10));
    center.setSpacing(8);
    // BOTTOM END ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    treeTableView = (TreeTableView<Drawable>) Gsehen.getInstance().getScene()
        .lookup(FARM_TREE_VIEW_ID);
    treeTableView.getSelectionModel().selectedItemProperty()
        .addListener(new ChangeListener<Object>() {
          @Override
          public void changed(ObservableValue<?> observable, Object oldVal, Object newVal) {
            for (int i = 0; i < treeTableView.getSelectionModel().getSelectedCells().size(); i++) {
              if (treeTableView.getSelectionModel().getSelectedCells().get(i) != null) {
                selectedItem = treeTableView.getSelectionModel().getSelectedCells().get(i)
                    .getTreeItem();
                if (selectedItem != null
                    && selectedItem.getValue().getClass().getSimpleName().equals("Field")) {
                  pane.setVisible(true);
                  field = (Field) selectedItem.getValue();

                  name.setText(field.getName());

                  area.setText(String.valueOf(field.getPolygon().calculateArea()));

                  for (WeatherDataSource wds : weatherDataSourceList) {
                    final WeatherDataSource weatherDataSource =
                        gsehenInstance.getWeatherDataSourceForUuid(
                            field.getWeatherDataSourceUuid());
                    if (weatherDataSource != null
                        && wds.getName().equals(weatherDataSource.getName())) {
                      weatherData.getSelectionModel().select(wds);
                      wdsFile = wds;
                    }
                  }

                  SoilProfile fieldSoilProfile =
                      gsehenInstance.getSoilProfileForUuid(field.getSoilProfileUuid());
                  for (SoilProfile soPr : soilProfileList) {
                    if (fieldSoilProfile != null
                        && soPr.getName().equals(fieldSoilProfile.getName())) {
                      currentSoilBox.getSelectionModel().select(soPr);
                    }
                  }
                  if (fieldSoilProfile != null) {
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
    sp = gsehenInstance.getSoilProfileForUuid(field.getSoilProfileUuid());
    int index = 1;
    Text setSoil = new Text(
        mainBundle.getString("fieldview.currentsoil") + " (" + sp.getName() + "):" + "\n");
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

  /**
   * Creates a WeatherDataSource.
   */
  public void setWeatherDataSource() {
    pane.getChildren().clear();
    treeTableView.setVisible(false);

    Text weatherDataLabel = new Text(mainBundle.getString("fieldview.weatherdataname"));
    weatherDataLabel.setFont(Font.font("Arial", 14));
    weatherDataName = new TextField();

    HBox nameBox = new HBox();
    nameBox.getChildren().addAll(weatherDataLabel, weatherDataName);

    VBox topBox = new VBox(25);
    topBox.setPadding(new Insets(20, 20, 20, 20));
    topBox.getChildren().addAll(nameBox);
    pane.setTop(topBox);

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

    Text intervalLabel = new Text(mainBundle.getString("fieldview.interval"));
    intervalLabel.setFont(Font.font("Arial", 14));
    interval = new TextField();
    interval.textProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observable, String oldValue,
          String newValue) {
        if (!newValue.matches("\\d*")) {
          interval.setText(newValue.replaceAll("[^\\d]", ""));
        }
      }
    });

    Text windspeedLabel = new Text(mainBundle.getString("fieldview.windspeed"));
    windspeedLabel.setFont(Font.font("Arial", 14));
    windspeed = new TextField();
    windspeed.textProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observable, String oldValue,
          String newValue) {
        if (newValue != null) {
          if (!newValue.matches("\\d{0,7}([\\.]\\d{0,4})?")) {
            windspeed.setText(oldValue);
          }
        }
      }
    });

    Text dateFormatLabel = new Text(mainBundle.getString("fieldview.dateformat"));
    dateFormatLabel.setFont(Font.font("Arial", 14));
    dateFormat = new TextField();
    Hyperlink dateFormatExample = new Hyperlink(
        mainBundle.getString("fieldview.dateformatexample"));
    dateFormatExample.setFont(Font.font("Arial", FontPosture.ITALIC, 12));
    dateFormatExample.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        getHostServices().showDocument(
            "https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html");
      }
    });

    Text localeIdLabel = new Text(mainBundle.getString("fieldview.localeid"));
    localeIdLabel.setFont(Font.font("Arial", 14));
    localeId = new ChoiceBox<String>();
    localeId.getItems().addAll(javaLocaleMap.keySet());

    Text filePathLabel = new Text(mainBundle.getString("fieldview.filepath"));
    filePathLabel.setFont(Font.font("Arial", 14));
    path = new TextField();
    Button fileChooserButton = new Button(mainBundle.getString("fieldview.filechooserbutton"));
    fileChooserButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        Stage stage = new Stage();
        filePath = new FileChooser();
        filePath.setTitle(mainBundle.getString("fieldview.filechooser"));
        path.setText(filePath.showOpenDialog(stage).getAbsolutePath());
      }
    });

    Text locationLatLabel = new Text(mainBundle.getString("fieldview.locationlat"));
    locationLatLabel.setFont(Font.font("Arial", 14));
    locationLat = new TextField();
    locationLat.textProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observable, String oldValue,
          String newValue) {
        if (newValue != null) {
          if (!newValue.matches("\\d{0,7}([\\.]\\d{0,4})?")) {
            locationLat.setText(oldValue);
          }
        }
      }
    });
    Text locationLatExample = new Text(mainBundle.getString("fieldview.locationlatexample"));
    locationLatExample.setFont(Font.font("Arial", FontPosture.ITALIC, 12));

    Text locationLngLabel = new Text(mainBundle.getString("fieldview.locationlng"));
    locationLngLabel.setFont(Font.font("Arial", 14));
    locationLng = new TextField();
    locationLng.textProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observable, String oldValue,
          String newValue) {
        if (newValue != null) {
          if (!newValue.matches("\\d{0,7}([\\.]\\d{0,4})?")) {
            locationLng.setText(oldValue);
          }
        }
      }
    });
    Text locationLngExample = new Text(mainBundle.getString("fieldview.locationlngexample"));
    locationLngExample.setFont(Font.font("Arial", FontPosture.ITALIC, 12));

    Text metersAboveLabel = new Text(mainBundle.getString("fieldview.metersabove"));
    metersAboveLabel.setFont(Font.font("Arial", 14));
    metersAbove = new TextField();
    metersAbove.textProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observable, String oldValue,
          String newValue) {
        if (newValue != null) {
          if (!newValue.matches("\\d{0,7}([\\.]\\d{0,4})?")) {
            metersAbove.setText(oldValue);
          }
        }
      }
    });

    // Set Nodes Vertical & Horizontal Alignment
    GridPane.setHalignment(intervalLabel, HPos.LEFT);
    GridPane.setHalignment(interval, HPos.LEFT);
    GridPane.setHalignment(windspeedLabel, HPos.LEFT);
    GridPane.setHalignment(windspeed, HPos.LEFT);
    GridPane.setHalignment(dateFormatLabel, HPos.LEFT);
    GridPane.setHalignment(dateFormat, HPos.LEFT);
    GridPane.setHalignment(dateFormatExample, HPos.LEFT);
    GridPane.setHalignment(localeIdLabel, HPos.LEFT);
    GridPane.setHalignment(localeId, HPos.LEFT);
    GridPane.setHalignment(filePathLabel, HPos.LEFT);
    GridPane.setHalignment(path, HPos.LEFT);
    GridPane.setHalignment(fileChooserButton, HPos.LEFT);
    GridPane.setHalignment(locationLatLabel, HPos.LEFT);
    GridPane.setHalignment(locationLat, HPos.LEFT);
    GridPane.setHalignment(locationLatExample, HPos.LEFT);
    GridPane.setHalignment(locationLngLabel, HPos.LEFT);
    GridPane.setHalignment(locationLng, HPos.LEFT);
    GridPane.setHalignment(locationLngExample, HPos.LEFT);
    GridPane.setHalignment(metersAboveLabel, HPos.LEFT);
    GridPane.setHalignment(metersAbove, HPos.LEFT);

    // Set Row & Column Index for Nodes
    GridPane.setConstraints(intervalLabel, 0, 0);
    GridPane.setConstraints(interval, 1, 0);
    GridPane.setConstraints(windspeedLabel, 0, 1);
    GridPane.setConstraints(windspeed, 1, 1);
    GridPane.setConstraints(dateFormatLabel, 0, 2);
    GridPane.setConstraints(dateFormat, 1, 2);
    GridPane.setConstraints(dateFormatExample, 2, 2);
    GridPane.setConstraints(localeIdLabel, 0, 3);
    GridPane.setConstraints(localeId, 1, 3);
    GridPane.setConstraints(filePathLabel, 0, 4);
    GridPane.setConstraints(path, 1, 4);
    GridPane.setConstraints(fileChooserButton, 2, 4);
    GridPane.setConstraints(locationLatLabel, 0, 5);
    GridPane.setConstraints(locationLat, 1, 5);
    GridPane.setConstraints(locationLatExample, 2, 5);
    GridPane.setConstraints(locationLngLabel, 0, 6);
    GridPane.setConstraints(locationLng, 1, 6);
    GridPane.setConstraints(locationLngExample, 2, 6);
    GridPane.setConstraints(metersAboveLabel, 0, 7);
    GridPane.setConstraints(metersAbove, 1, 7);

    center.getChildren().addAll(intervalLabel, interval, windspeedLabel, windspeed, dateFormatLabel,
        dateFormat, dateFormatExample, localeIdLabel, localeId, filePathLabel, path,
        fileChooserButton, locationLatLabel, locationLat, locationLatExample, locationLngLabel,
        locationLng, locationLngExample, metersAboveLabel, metersAbove);

    ScrollPane scrollPane = new ScrollPane();
    scrollPane.setContent(center);
    scrollPane.setPannable(true);
    pane.setCenter(scrollPane);

    back = new Button(mainBundle.getString("fieldview.back"));
    back.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent arg0) {
        pane.getChildren().clear();
        treeTableView.setVisible(true);
        gsehenInstance.sendFarmDataChanged(field, null);
      }
    });

    save = new Button(mainBundle.getString("menu.file.save"));
    save.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent arg0) {
        if (!weatherDataName.getText().trim().isEmpty() && !interval.getText().trim().isEmpty()
            && !windspeed.getText().trim().isEmpty() && !dateFormat.getText().trim().isEmpty()
            && !localeId.getSelectionModel().isEmpty() && !path.getText().trim().isEmpty()
            && !locationLat.getText().trim().isEmpty() && !locationLng.getText().trim().isEmpty()
            && !metersAbove.getText().trim().isEmpty()) {
          try {
            @SuppressWarnings("unused")
            SimpleDateFormat sdf = new SimpleDateFormat(dateFormat.getText());

            pane.getChildren().clear();
            treeTableView.setVisible(true);
            wds = new WeatherDataSource(DBUtil.generateUuid());
            wds.setName(weatherDataName.getText());
            wds.setMeasIntervalSeconds(Integer.valueOf(interval.getText()));
            wds.setWindspeedMeasHeightMeters(Double.valueOf(windspeed.getText()));
            wds.setDateFormatString(dateFormat.getText());
            wds.setNumberLocaleId(javaLocaleMap.get(localeId.getValue()));
            wds.setDataFilePath(path.getText());
            wds.setLocationLat(Double.valueOf(locationLat.getText()));
            wds.setLocationLng(Double.valueOf(locationLng.getText()));
            wds.setLocationMetersAboveSeaLevel(Double.valueOf(metersAbove.getText()));

            weatherDataSourceList.add(wds);
            pane.getChildren().clear();
            gsehenInstance.sendFarmDataChanged(field, null);
          } catch (IllegalArgumentException iae) {
            if (center.getChildren().contains(dateError)) {
              center.getChildren().remove(dateError);
            }
            dateError.setText(
                mainBundle.getString("fieldview.dateerror") + "\"" + iae.getMessage() + "\"");
            dateError.setFont(Font.font("Verdana", 12));
            dateError.setFill(Color.RED);
            GridPane.setHalignment(dateError, HPos.LEFT);
            GridPane.setConstraints(dateError, 3, 2);
            center.getChildren().add(dateError);
          }
        } else {
          Text error = new Text(mainBundle.getString("fieldview.error"));
          error.setFont(Font.font("Verdana", 20));
          error.setFill(Color.RED);
          buttonBox.getChildren().clear();
          buttonBox.getChildren().addAll(back, save, error);
        }
      }
    });

    buttonBox = new HBox();
    buttonBox.getChildren().addAll(back, save);

    VBox bottomBox = new VBox(25);
    bottomBox.setPadding(new Insets(20, 20, 20, 20));
    bottomBox.getChildren().addAll(buttonBox);
    pane.setBottom(bottomBox);
  }

  /**
   * Fills TextFields with correct values.
   */
  public void setWeatherDataTexts() {
    final WeatherDataSource selectedWeatherDataSource = weatherData.getSelectionModel()
        .getSelectedItem();
    if (selectedWeatherDataSource != null) {
      weatherDataName.setText(selectedWeatherDataSource.getName());
      interval.setText(String.valueOf(selectedWeatherDataSource.getMeasIntervalSeconds()));
      windspeed.setText(String.valueOf(selectedWeatherDataSource.getWindspeedMeasHeightMeters()));
      dateFormat.setText(selectedWeatherDataSource.getDateFormatString());
      localeId.getSelectionModel()
          .select(getKeyForValue(selectedWeatherDataSource.getNumberLocaleId(), javaLocaleMap));
      path.setText(selectedWeatherDataSource.getDataFilePath());
      locationLat.setText(String.valueOf(selectedWeatherDataSource.getLocationLat()));
      locationLng.setText(String.valueOf(selectedWeatherDataSource.getLocationLng()));
      metersAbove
          .setText(String.valueOf(selectedWeatherDataSource.getLocationMetersAboveSeaLevel()));
      weatherDataSourceList.remove(wdsFile);
    }
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
  }
}
