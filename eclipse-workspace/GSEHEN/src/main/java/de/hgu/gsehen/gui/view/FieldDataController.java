package de.hgu.gsehen.gui.view;

import static de.hgu.gsehen.util.JavaFxUtil.noneIsEmpty;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTabPane;
import com.jfoenix.controls.JFXTextField;
import de.hgu.gsehen.Gsehen;
import de.hgu.gsehen.event.FarmDataChanged;
import de.hgu.gsehen.event.GsehenEventListener;
import de.hgu.gsehen.model.Drawable;
import de.hgu.gsehen.model.Field;
import de.hgu.gsehen.model.Soil;
import de.hgu.gsehen.model.SoilManualData;
import de.hgu.gsehen.model.SoilProfile;
import de.hgu.gsehen.model.SoilProfileDepth;
import de.hgu.gsehen.model.WeatherDataSource;
import de.hgu.gsehen.util.DBUtil;
import de.hgu.gsehen.util.PluginUtil;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
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
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class FieldDataController extends Application
    implements GsehenEventListener<FarmDataChanged> {
  private static final String FARM_TREE_VIEW_ID = "#farmTreeView";
  protected final ResourceBundle mainBundle;
  private WeatherDataSource selectedWeatherDataSource;
  private SoilManualData soilManualData;

  private List<SoilProfile> soilProfileList;
  private List<WeatherDataSource> weatherDataSourceList;

  private TreeItem<Drawable> selectedItem;
  private int currentItem;
  private Field field;

  private Gsehen gsehenInstance;
  private BorderPane pane;
  private TreeTableView<Drawable> treeTableView;
  private JFXTabPane tabPane;
  private Tab mapViewTab;
  private Tab fieldViewTab;
  private Tab plotViewTab;
  private Tab logViewTab;

  private Text nameLabel;
  private Text areaLabel;

  private JFXTextField name;
  private Text area;

  private JFXComboBox<SoilProfile> currentSoilBox;
  private JFXComboBox<WeatherDataSource> weatherData;
  private SoilProfile sp;
  private WeatherDataSource wds;
  private Button createSoil;
  private Button save;
  private Button back;
  private HBox buttonBox;
  private List<Text> layerList;
  private int index;
  private Button saveField;

  private JFXTextField weatherDataSourceName;
  private JFXComboBox<String> weatherDataPluginJsFileName;
  private CheckBox weatherDataManualImport;
  private CheckBox weatherDataAutomaticImport;
  private JFXTextField weatherDataAutomaticImportIntervalSeconds;
  private JFXTextField weatherDataSourceLocationLat;
  private JFXTextField weatherDataSourceLocationLng;
  private JFXTextField weatherDataSourceMetersAbove;

  private JFXTextField soilDepth;
  private JFXTextField soilManualKc;
  private JFXTextField soilManualZone;
  private JFXTextField soilManualRain;
  private JFXTextField soilManualPause;
  private Text dateError = new Text();

  {
    gsehenInstance = Gsehen.getInstance();
    soilProfileList = gsehenInstance.getSoilProfiles();
    weatherDataSourceList = gsehenInstance.getWeatherDataSources();

    final Locale selLocale = gsehenInstance.getSelectedLocale();
    mainBundle = ResourceBundle.getBundle("i18n.main", selLocale);

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

  @Override
  public void handle(FarmDataChanged event) {
    createFieldView();
  }

  @SuppressWarnings("unchecked")
  private void createFieldView() {
    pane.setVisible(false);
    index = 1;

    // TOP (The FieldView, you will see first)
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

    // Name
    nameLabel = new Text(mainBundle.getString("fieldview.name"));
    nameLabel.setFont(Font.font("Arial", 14));
    name = new JFXTextField("");
    name.setPrefSize(150, 25);

    // m²
    areaLabel = new Text(mainBundle.getString("fieldview.area"));
    areaLabel.setFont(Font.font("Arial", 14));
    area = new Text("");
    area.setFont(Font.font("Arial", FontWeight.BOLD, 14));

    // Bodenprofil
    Text soilProfile = new Text(mainBundle.getString("fieldview.soilprofile"));
    soilProfile.setFont(Font.font("Arial", 14));
    currentSoilBox = new JFXComboBox<SoilProfile>();
    currentSoilBox.setPrefSize(200, 25);
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
    editProfile.setId("glass-grey");
    editProfile.setPrefSize(200, 25);
    editProfile.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent e) {
        editSoilProfile();
      }
    });

    createSoil = new Button(mainBundle.getString("fieldview.createprofile"));
    createSoil.setId("glass-grey");
    createSoil.setPrefSize(150, 25);
    createSoil.setOnAction(new EventHandler<ActionEvent>() {

      @Override
      public void handle(ActionEvent e) {
        createSoilProfile();
      }
    });

    VBox soilBox = new VBox();
    soilBox.setSpacing(10);
    soilBox.getChildren().addAll(editProfile, createSoil);

    // Wetterdatenquelle
    Text weatherDataSource = new Text(mainBundle.getString("fieldview.weatherdatasource") + ":");
    weatherDataSource.setFont(Font.font("Arial", 14));
    weatherData = new JFXComboBox<WeatherDataSource>();
    weatherData.setPrefSize(150, 25);
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
    editWds.setId("glass-grey");
    editWds.setPrefSize(200, 25);
    editWds.setOnAction(new EventHandler<ActionEvent>() {

      @Override
      public void handle(ActionEvent e) {
        if (weatherData.getValue() != null) {
          createWeatherDataSource();
          setWeatherDataTexts();
        }
      }
    });

    Button createWds = new Button(mainBundle.getString("fieldview.createwds"));
    createWds.setId("glass-grey");
    createWds.setPrefSize(200, 25);
    createWds.setOnAction(new EventHandler<ActionEvent>() {

      @Override
      public void handle(ActionEvent e) {
        createWeatherDataSource();
      }
    });

    VBox weatherBox = new VBox();
    weatherBox.setSpacing(10);
    weatherBox.getChildren().addAll(editWds, createWds);

    // Speichern
    saveField = new Button(mainBundle.getString("button.accept"));
    saveField.setId("glass-grey");
    saveField.setPrefSize(150, 25);
    Text nameError = new Text(mainBundle.getString("fieldview.nameerror"));
    nameError.setFont(Font.font("Verdana", 14));
    nameError.setFill(Color.RED);
    saveField.setOnAction(new EventHandler<ActionEvent>() {

      @Override
      public void handle(ActionEvent e) {
        if (name.getText().isEmpty()) {
          GridPane.setConstraints(nameError, 2, 0);
          grid.getChildren().add(nameError);
        } else {
          field.setName(name.getText());
          field.setArea(gsehenInstance.parseDouble(area.getText()));
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
          tabPane.getSelectionModel().select(1);
          treeTableView.getSelectionModel().clearSelection();
          treeTableView.getSelectionModel().select(currentItem);
        }
      }
    });

    GridPane.setConstraints(nameLabel, 0, 0);
    GridPane.setConstraints(name, 1, 0);
    GridPane.setConstraints(areaLabel, 0, 1);
    GridPane.setConstraints(area, 1, 1);
    GridPane.setConstraints(soilProfile, 0, 2);
    GridPane.setConstraints(currentSoilBox, 1, 2);
    GridPane.setConstraints(soilBox, 2, 2);
    GridPane.setConstraints(weatherDataSource, 0, 3);
    GridPane.setConstraints(weatherData, 1, 3);
    GridPane.setConstraints(weatherBox, 2, 3);
    GridPane.setConstraints(saveField, 0, 5);

    grid.getChildren().addAll(nameLabel, name, areaLabel, area, soilProfile, soilBox,
        weatherDataSource, weatherBox, currentSoilBox, weatherData, saveField);

    pane.setTop(grid);
    // TOP END ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    tabPane = gsehenInstance.getMainController().getJfxTabPane();
    mapViewTab = gsehenInstance.getMainController().getMapViewTab();
    fieldViewTab = gsehenInstance.getMainController().getFieldViewTab();
    plotViewTab = gsehenInstance.getMainController().getPlotViewTab();
    logViewTab = gsehenInstance.getMainController().getLogViewTab();

    // Actions that will happen, if you click a 'field' in the TreeTableView
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

                  area.setText(
                      gsehenInstance.formatDoubleOneDecimal(field.getPolygon().calculateArea()));

                  for (WeatherDataSource wds : weatherDataSourceList) {
                    final WeatherDataSource weatherDataSource = gsehenInstance
                        .getWeatherDataSourceForUuid(field.getWeatherDataSourceUuid());
                    if (weatherDataSource != null
                        && wds.getName().equals(weatherDataSource.getName())) {
                      weatherData.getSelectionModel().select(wds);
                    }
                  }

                  SoilProfile fieldSoilProfile = gsehenInstance
                      .getSoilProfileForUuid(field.getSoilProfileUuid());
                  for (SoilProfile soPr : soilProfileList) {
                    if (fieldSoilProfile != null
                        && soPr.getName().equals(fieldSoilProfile.getName())) {
                      currentSoilBox.getSelectionModel().select(soPr);
                    }
                  }
                  if (fieldSoilProfile != null) {
                    getCurrentSoilProfile();
                  } else {
                    currentSoilBox.getSelectionModel().clearSelection();
                    pane.setBottom(null);
                  }
                  if (grid.getChildren().contains(nameError)) {
                    grid.getChildren().remove(nameError);
                  }
                } else {
                  pane.setVisible(false);
                }
              }
            }
          }
        });
    currentItem = treeTableView.getSelectionModel().getSelectedIndex();
  }

  /**
   * Shows current SoilProfile at bottom of the 'MainView'.
   */
  private void getCurrentSoilProfile() {
    pane.setCenter(null);
    sp = gsehenInstance.getSoilProfileForUuid(field.getSoilProfileUuid());
    int index = 1;

    // TODO
    GridPane grid = new GridPane();

    grid.setPadding(new Insets(20, 0, 0, 20));
    grid.setVgap(5);
    grid.setGridLinesVisible(false);

    ColumnConstraints column1 = new ColumnConstraints(200, 100, 300);
    ColumnConstraints column2 = new ColumnConstraints(200, 100, 100);
    column1.setHgrow(Priority.ALWAYS);
    column2.setHgrow(Priority.ALWAYS);
    RowConstraints rowEmpty = new RowConstraints();

    grid.getColumnConstraints().addAll(column1, column2);
    grid.getRowConstraints().add(0, rowEmpty);
    grid.getRowConstraints().add(1, rowEmpty);

    Text setSoilProfile = new Text(
        mainBundle.getString("fieldview.currentsoil") + " (" + sp.getName() + "):" + "\n");
    setSoilProfile.setFont(Font.font("Arial", FontWeight.BOLD, 14));

    Text setKcLabel = new Text(mainBundle.getString("fieldview.manualkc"));
    setKcLabel.setFont(Font.font("Arial", 14));

    Text setKc = new Text();
    String kc = "";
    if (sp.getSoilManualData().getSoilKc() != null) {
      kc = String.valueOf(sp.getSoilManualData().getSoilKc());
      setKc.setText(kc);
    }

    Text setZoneLabel = new Text(mainBundle.getString("fieldview.manualzone"));
    setZoneLabel.setFont(Font.font("Arial", 14));

    Text setZone = new Text();
    String zone = "";
    if (sp.getSoilManualData().getSoilZone() != null) {
      zone = String.valueOf(sp.getSoilManualData().getSoilZone());
      setZone.setText(zone);
    }

    Text setRainLabel = new Text(mainBundle.getString("fieldview.manualrain"));
    setRainLabel.setFont(Font.font("Arial", 14));

    Text setRain = new Text();
    String rain = "";
    if (sp.getSoilManualData().getRainMax() != null) {
      rain = String.valueOf(sp.getSoilManualData().getRainMax());
      setRain.setText(rain);
    }

    Text setPauseLabel = new Text(mainBundle.getString("fieldview.manualpause"));
    setPauseLabel.setFont(Font.font("Arial", 14));

    Text setPause = new Text();
    String pause = "";
    if (sp.getSoilManualData().getDaysPause() != null) {
      pause = String.valueOf(sp.getSoilManualData().getDaysPause());
      setPause.setText(pause);
    }

    GridPane.setConstraints(setSoilProfile, 0, 0);
    GridPane.setConstraints(setKcLabel, 0, 1);
    GridPane.setConstraints(setKc, 1, 1);
    GridPane.setConstraints(setZoneLabel, 0, 2);
    GridPane.setConstraints(setZone, 1, 2);
    GridPane.setConstraints(setRainLabel, 0, 3);
    GridPane.setConstraints(setRain, 1, 3);
    GridPane.setConstraints(setPauseLabel, 0, 4);
    GridPane.setConstraints(setPause, 1, 4);

    grid.getChildren().addAll(setSoilProfile, setKcLabel, setZoneLabel, setRainLabel, setPauseLabel,
        setKc, setZone, setRain, setPause);

    int col = 5;
    for (Soil soil : sp.getSoilType()) {
      Text createdSoilLayer = new Text(
          "\n" + mainBundle.getString("fieldview.layer") + (index) + ":");
      Text createdSoil = new Text(mainBundle.getString("fieldview.soiltype"));
      Text soilName = new Text(soil.getName());
      Text createdSoilWater = new Text(mainBundle.getString("fieldview.awc"));
      Text water = new Text(String.valueOf(soil.getAvailableWaterCapacity()));
      Text createdSoilDepth = new Text(mainBundle.getString("fieldview.depth"));
      Text depth = new Text(String.valueOf(sp.getProfileDepth().get(index - 1).getDepth()));
      GridPane.setConstraints(createdSoilLayer, 0, col);
      col++;
      GridPane.setConstraints(createdSoil, 0, col);
      GridPane.setConstraints(soilName, 1, col);
      col++;
      GridPane.setConstraints(createdSoilWater, 0, col);
      GridPane.setConstraints(water, 1, col);
      col++;
      GridPane.setConstraints(createdSoilDepth, 0, col);
      GridPane.setConstraints(depth, 1, col);
      col++;
      grid.getChildren().addAll(createdSoilLayer, createdSoil, soilName, createdSoilWater, water,
          createdSoilDepth, depth);
      createdSoilLayer.setFont(Font.font("Arial", FontPosture.ITALIC, 14));
      createdSoil.setFont(Font.font("Arial", FontPosture.ITALIC, 14));
      soilName.setFont(Font.font("Arial", FontPosture.ITALIC, 14));
      createdSoilWater.setFont(Font.font("Arial", FontPosture.ITALIC, 14));
      water.setFont(Font.font("Arial", FontPosture.ITALIC, 14));
      createdSoilDepth.setFont(Font.font("Arial", FontPosture.ITALIC, 14));
      depth.setFont(Font.font("Arial", FontPosture.ITALIC, 14));
      index++;
    }
    ScrollPane scrollPane = new ScrollPane();
    scrollPane.setContent(grid);
    scrollPane.setPannable(true);
    pane.setCenter(scrollPane);
  }

  /**
   * Creates a WeatherDataSource.
   */
  private void createWeatherDataSource() {
    pane.getChildren().clear();
    treeTableView.setVisible(false);
    tabPane.getTabs().removeAll(mapViewTab, plotViewTab, logViewTab);

    // GridPane - Center Section
    GridPane top = new GridPane();

    // GridPane Configuration (Padding, Gaps, etc.)
    top.setPadding(new Insets(20, 20, 20, 20));
    top.setHgap(15);
    top.setVgap(15);
    top.setGridLinesVisible(false);

    // Name
    Text weatherDataLabel = new Text(mainBundle.getString("fieldview.weatherdatalabel"));
    weatherDataLabel.setFont(Font.font("Arial", 14));
    weatherDataSourceName = new JFXTextField();

    GridPane.setConstraints(weatherDataLabel, 0, 0);
    GridPane.setConstraints(weatherDataSourceName, 1, 0);

    top.getChildren().addAll(weatherDataLabel, weatherDataSourceName);

    pane.setTop(top);

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

    // Plug-in
    Text weatherDataPluginJsFileNameLabel = new Text(
        mainBundle.getString("fieldview.weatherdatapluginjsfilenamelabel"));
    weatherDataPluginJsFileNameLabel.setFont(Font.font("Arial", 14));
    weatherDataPluginJsFileName = new JFXComboBox<String>();
    weatherDataPluginJsFileName.getItems().addAll(PluginUtil.getPluginJsFileNames());

    // Manual Import?
    Text weatherDataManualImportLabel = new Text(
        mainBundle.getString("fieldview.weatherdatamanualimportlabel"));
    weatherDataManualImportLabel.setFont(Font.font("Arial", 14));
    weatherDataManualImport = new CheckBox();

    // Automatic Import?
    Text weatherDataAutomaticImportLabel = new Text(
        mainBundle.getString("fieldview.weatherdataautomaticimportlabel"));
    weatherDataAutomaticImportLabel.setFont(Font.font("Arial", 14));
    weatherDataAutomaticImport = new CheckBox();

    // Automatic Import interval
    Text weatherDataAutomaticImportIntervalSecondsLabel = new Text(
        mainBundle.getString("fieldview.weatherdataautomaticimportintervalsecondslabel"));
    weatherDataAutomaticImportIntervalSecondsLabel.setFont(Font.font("Arial", 14));
    weatherDataAutomaticImportIntervalSeconds = new JFXTextField();
    weatherDataAutomaticImportIntervalSeconds.textProperty()
        .addListener(new ChangeListener<String>() {
          @Override
          public void changed(ObservableValue<? extends String> observable, String oldValue,
              String newValue) {
            if (!newValue.trim().isEmpty() && !gsehenInstance.isParseable(newValue)) {
              weatherDataAutomaticImportIntervalSeconds.setText(oldValue);
            }
          }
        });

    // Latitude
    Text weatherDataSourceLocationLatLabel = new Text(
        mainBundle.getString("fieldview.locationlat"));
    weatherDataSourceLocationLatLabel.setFont(Font.font("Arial", 14));
    weatherDataSourceLocationLat = new JFXTextField();
    weatherDataSourceLocationLat.textProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observable, String oldValue,
          String newValue) {
        if (newValue != null) {
          if (!gsehenInstance.isParseable(newValue)) {
            weatherDataSourceLocationLat.setText(oldValue);
          }
        }
      }
    });
    NumberFormat formatter = NumberFormat.getInstance(gsehenInstance.getSelectedLocale());
    Text weatherDataSourceLocationLatExample = new Text(
        mainBundle.getString("fieldview.example") + " " + formatter.format(
            gsehenInstance.parseDouble(mainBundle.getString("fieldview.locationlatvalue"))));
    weatherDataSourceLocationLatExample.setFont(Font.font("Arial", FontPosture.ITALIC, 12));

    // Longitude
    Text weatherDataSourceLocationLngLabel = new Text(
        mainBundle.getString("fieldview.locationlng"));
    weatherDataSourceLocationLngLabel.setFont(Font.font("Arial", 14));
    weatherDataSourceLocationLng = new JFXTextField();
    weatherDataSourceLocationLng.textProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observable, String oldValue,
          String newValue) {
        if (newValue != null) {
          if (!gsehenInstance.isParseable(newValue)) {
            weatherDataSourceLocationLng.setText(oldValue);
          }
        }
      }
    });
    Text weatherDataSourceLocationLngExample = new Text(
        mainBundle.getString("fieldview.example") + " " + formatter.format(
            gsehenInstance.parseDouble(mainBundle.getString("fieldview.locationlngvalue"))));
    weatherDataSourceLocationLngExample.setFont(Font.font("Arial", FontPosture.ITALIC, 12));

    // Standort (Meter ü. NN)
    Text weatherDataSourceMetersAboveLabel = new Text(
        mainBundle.getString("fieldview.metersabove"));
    weatherDataSourceMetersAboveLabel.setFont(Font.font("Arial", 14));
    weatherDataSourceMetersAbove = new JFXTextField();
    weatherDataSourceMetersAbove.textProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observable, String oldValue,
          String newValue) {
        if (newValue != null) {
          if (!gsehenInstance.isParseable(newValue)) {
            weatherDataSourceMetersAbove.setText(oldValue);
          }
        }
      }
    });

    // Set Row & Column Index for Nodes
    GridPane.setConstraints(weatherDataPluginJsFileNameLabel, 0, 0);
    GridPane.setConstraints(weatherDataPluginJsFileName, 1, 0);
    GridPane.setConstraints(weatherDataManualImportLabel, 0, 1);
    GridPane.setConstraints(weatherDataManualImport, 1, 1);
    GridPane.setConstraints(weatherDataAutomaticImportLabel, 0, 2);
    GridPane.setConstraints(weatherDataAutomaticImport, 1, 2);
    GridPane.setConstraints(weatherDataAutomaticImportIntervalSecondsLabel, 0, 3);
    GridPane.setConstraints(weatherDataAutomaticImportIntervalSeconds, 1, 3);
    GridPane.setConstraints(weatherDataSourceLocationLatLabel, 0, 4);
    GridPane.setConstraints(weatherDataSourceLocationLat, 1, 4);
    GridPane.setConstraints(weatherDataSourceLocationLatExample, 2, 4);
    GridPane.setConstraints(weatherDataSourceLocationLngLabel, 0, 5);
    GridPane.setConstraints(weatherDataSourceLocationLng, 1, 5);
    GridPane.setConstraints(weatherDataSourceLocationLngExample, 2, 5);
    GridPane.setConstraints(weatherDataSourceMetersAboveLabel, 0, 6);
    GridPane.setConstraints(weatherDataSourceMetersAbove, 1, 6);

    // Add nodes
    center.getChildren().addAll(weatherDataPluginJsFileNameLabel, weatherDataPluginJsFileName,
        weatherDataManualImportLabel, weatherDataManualImport, weatherDataAutomaticImportLabel,
        weatherDataAutomaticImport, weatherDataAutomaticImportIntervalSecondsLabel,
        weatherDataAutomaticImportIntervalSeconds, weatherDataSourceLocationLatLabel,
        weatherDataSourceLocationLat, weatherDataSourceLocationLatExample,
        weatherDataSourceLocationLngLabel, weatherDataSourceLocationLng,
        weatherDataSourceLocationLngExample, weatherDataSourceMetersAboveLabel,
        weatherDataSourceMetersAbove);

    ScrollPane scrollPane = new ScrollPane();
    scrollPane.setContent(center);
    scrollPane.setPannable(true);
    pane.setCenter(scrollPane);

    back = new Button(mainBundle.getString("fieldview.back"));
    back.setId("glass-grey");
    back.setPrefSize(150, 25);
    back.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent arg0) {
        pane.getChildren().clear();
        treeTableView.setVisible(true);
        tabPane.getTabs().clear();
        tabPane.getTabs().addAll(mapViewTab, fieldViewTab, plotViewTab, logViewTab);
        gsehenInstance.sendFarmDataChanged(field, null);
        tabPane.getSelectionModel().select(1);
        treeTableView.getSelectionModel().clearSelection();
        treeTableView.getSelectionModel().select(currentItem);
      }
    });

    save = new Button(mainBundle.getString("button.accept"));
    save.setId("glass-grey");
    save.setPrefSize(150, 25);
    save.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent arg0) {
        if (noneIsEmpty(
            new TextField[] { weatherDataSourceName, weatherDataAutomaticImportIntervalSeconds })) {
          try {
            setWeatherDataAndValues();

            pane.getChildren().clear();
            treeTableView.setVisible(true);
            tabPane.getTabs().clear();
            tabPane.getTabs().addAll(mapViewTab, fieldViewTab, plotViewTab, logViewTab);
            gsehenInstance.sendFarmDataChanged(field, null);
            tabPane.getSelectionModel().select(1);
            treeTableView.getSelectionModel().clearSelection();
            treeTableView.getSelectionModel().select(currentItem);
          } catch (IllegalArgumentException iae) {
            if (center.getChildren().contains(dateError)) {
              center.getChildren().remove(dateError);
            }
            dateError.setText(
                mainBundle.getString("fieldview.dateerror") + "\"" + iae.getMessage() + "\"");
            dateError.setFont(Font.font("Verdana", 14));
            dateError.setFill(Color.RED);
            GridPane.setHalignment(dateError, HPos.LEFT);
            GridPane.setConstraints(dateError, 3, 2);
            center.getChildren().add(dateError);
          }
        } else {
          Text error = new Text(mainBundle.getString("fieldview.error"));
          error.setFont(Font.font("Verdana", 14));
          error.setFill(Color.RED);
          buttonBox.getChildren().clear();
          buttonBox.getChildren().addAll(back, save, error);
        }
      }
    });

    buttonBox = new HBox();
    buttonBox.setSpacing(10);
    buttonBox.getChildren().addAll(back, save);

    VBox bottomBox = new VBox(25);
    bottomBox.setSpacing(10);
    bottomBox.setPadding(new Insets(20, 20, 20, 20));
    bottomBox.getChildren().addAll(buttonBox);
    pane.setBottom(bottomBox);
  }

  private void setWeatherDataAndValues() {
    if (selectedWeatherDataSource != null) {
      wds = selectedWeatherDataSource;
    } else {
      wds = new WeatherDataSource(DBUtil.generateUuid());
      weatherDataSourceList.add(wds);
    }
    wds.setName(weatherDataSourceName.getText());
    wds.setPluginJsFileName(weatherDataPluginJsFileName.getValue());
    wds.setManualImportActive(weatherDataManualImport.isSelected());
    wds.setAutomaticImportActive(weatherDataAutomaticImport.isSelected());
    wds.setAutomaticImportFrequencySeconds(
        gsehenInstance.parseDouble(weatherDataAutomaticImportIntervalSeconds.getText()));
    wds.setLocationLat(gsehenInstance.parseDouble(weatherDataSourceLocationLat.getText()));
    wds.setLocationLng(gsehenInstance.parseDouble(weatherDataSourceLocationLng.getText()));
    wds.setLocationMetersAboveSeaLevel(
        gsehenInstance.parseDouble(weatherDataSourceMetersAbove.getText()));
  }

  /**
   * Fills JFXTextFields with correct values ("Wetterdatenquelle bearbeiten").
   */
  private void setWeatherDataTexts() {
    selectedWeatherDataSource = weatherData.getSelectionModel().getSelectedItem();
    if (selectedWeatherDataSource != null) {
      weatherDataSourceName.setText(selectedWeatherDataSource.getName());
      weatherDataPluginJsFileName.setValue(selectedWeatherDataSource.getPluginJsFileName());
      weatherDataManualImport.setSelected(selectedWeatherDataSource.isManualImportActive());
      weatherDataAutomaticImport.setSelected(selectedWeatherDataSource.isAutomaticImportActive());
      weatherDataAutomaticImportIntervalSeconds.setText(gsehenInstance
          .formatDoubleTwoDecimal(selectedWeatherDataSource.getAutomaticImportFrequencySeconds()));
      weatherDataSourceLocationLat.setText(
          gsehenInstance.formatDoubleTwoDecimal(selectedWeatherDataSource.getLocationLat()));
      weatherDataSourceLocationLng.setText(
          gsehenInstance.formatDoubleTwoDecimal(selectedWeatherDataSource.getLocationLng()));
      weatherDataSourceMetersAbove.setText(gsehenInstance
          .formatDoubleTwoDecimal(selectedWeatherDataSource.getLocationMetersAboveSeaLevel()));
    }
  }

  /**
   * Creates a new SoilProfile.
   */
  private void createSoilProfile() {
    pane.getChildren().clear();
    treeTableView.setVisible(false);
    tabPane.getTabs().removeAll(mapViewTab, plotViewTab, logViewTab);

    // GridPane - Center Section
    GridPane top = new GridPane();

    // GridPane Configuration (Padding, Gaps, etc.)
    top.setPadding(new Insets(20, 20, 20, 20));
    top.setHgap(15);
    top.setVgap(15);
    top.setGridLinesVisible(false);

    // Set Column and Row Constraints
    ColumnConstraints col1 = new ColumnConstraints(200, 100, 300);
    ColumnConstraints col2 = new ColumnConstraints(200, 100, 100);
    col1.setHgrow(Priority.ALWAYS);
    col2.setHgrow(Priority.ALWAYS);
    RowConstraints rowEmpty = new RowConstraints();

    // Add Constraints to Columns & Rows
    top.getColumnConstraints().addAll(col1, col2);
    top.getRowConstraints().add(0, rowEmpty);
    top.getRowConstraints().add(1, rowEmpty);

    // Name
    Text soilNameLabel = new Text(mainBundle.getString("fieldview.profilename"));
    soilNameLabel.setFont(Font.font("Arial", 14));

    // kc-Wert
    Text soilManualKcLabel = new Text(mainBundle.getString("fieldview.manualkc"));
    soilManualKcLabel.setFont(Font.font("Arial", 14));
    soilManualKc = new JFXTextField("");

    // Bilanzierungstiefe (in cm)
    Text soilManualZoneLabel = new Text(mainBundle.getString("fieldview.manualzone"));
    soilManualZoneLabel.setFont(Font.font("Arial", 14));
    soilManualZone = new JFXTextField("");

    // Schwelle des Regenereignis (in mm)
    Text soilManualRainLabel = new Text(mainBundle.getString("fieldview.manualrain"));
    soilManualRainLabel.setFont(Font.font("Arial", 14));
    soilManualRain = new JFXTextField("");

    // Bewässerungspause (in Tagen)
    Text soilManualPauseLabel = new Text(mainBundle.getString("fieldview.manualpause"));
    soilManualPauseLabel.setFont(Font.font("Arial", 14));
    JFXTextField soilManualPause = new JFXTextField("");

    JFXTextField soilProfileName = new JFXTextField("");

    // Set Row & Column Index for Nodes
    GridPane.setConstraints(soilNameLabel, 0, 0);
    GridPane.setConstraints(soilProfileName, 1, 0);
    GridPane.setConstraints(soilManualKcLabel, 0, 1);
    GridPane.setConstraints(soilManualKc, 1, 1);
    GridPane.setConstraints(soilManualZoneLabel, 0, 2);
    GridPane.setConstraints(soilManualZone, 1, 2);
    GridPane.setConstraints(soilManualRainLabel, 0, 3);
    GridPane.setConstraints(soilManualRain, 1, 3);
    GridPane.setConstraints(soilManualPauseLabel, 0, 4);
    GridPane.setConstraints(soilManualPause, 1, 4);

    top.getChildren().addAll(soilNameLabel, soilProfileName, soilManualKcLabel, soilManualKc,
        soilManualZoneLabel, soilManualZone, soilManualRainLabel, soilManualRain,
        soilManualPauseLabel, soilManualPause);
    pane.setTop(top);

    layerList = new ArrayList<Text>();

    // "Schicht #XY"
    Text layerText = new Text(mainBundle.getString("fieldview.layer") + (layerList.size() + 1));
    layerText.setFont(Font.font("Arial", FontWeight.BOLD, 18));

    // Bodentyp
    Text soil = new Text(mainBundle.getString("fieldview.soiltype"));
    soil.setFont(Font.font("Arial", 14));

    Soil s = new Soil();
    List<Soil> soils = s.soils();

    JFXComboBox<Soil> soilChoiceBox = new JFXComboBox<Soil>();
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

    // Wasserhaltekapazität
    Text soilAwcLabel = new Text(mainBundle.getString("fieldview.soilawc"));
    soilAwcLabel.setFont(Font.font("Arial", 14));
    JFXTextField soilAwc = new JFXTextField();

    // Sets the 'soilAwc', if the ChoiceBox-Value changed
    ChangeListener<Soil> changeListener = new ChangeListener<Soil>() {
      @Override
      public void changed(ObservableValue<? extends Soil> observable, //
          Soil oldValue, Soil newValue) {
        if (newValue != null) {
          soilAwc.setText(gsehenInstance
              .formatDoubleOneDecimal(soilChoiceBox.getValue().getAvailableWaterCapacity()));
        }
      }
    };
    soilChoiceBox.getSelectionModel().selectedItemProperty().addListener(changeListener);

    // Tiefe
    JFXTextField depth = new JFXTextField("25");
    Text depthLabel = new Text(mainBundle.getString("fieldview.depth"));
    depthLabel.setFont(Font.font("Arial", 14));
    depth.textProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observable, String oldValue,
          String newValue) {
        if (newValue != null) {
          if (!newValue.trim().isEmpty() && !gsehenInstance.isParseable(newValue)) {
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
    RowConstraints emptyRow = new RowConstraints();

    // Add Constraints to Columns & Rows
    center.getColumnConstraints().addAll(column1, column2);
    center.getRowConstraints().add(0, emptyRow);
    center.getRowConstraints().add(1, emptyRow);

    List<Soil> soilList = new ArrayList<Soil>();
    List<SoilProfileDepth> soilDepthList = new ArrayList<SoilProfileDepth>();

    // Schicht abschließen
    Button setSoil = new Button(mainBundle.getString("fieldview.setsoil"));
    setSoil.setId("glass-grey");
    setSoil.setPrefSize(200, 25);
    setSoil.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent arg0) {
        if (soilChoiceBox.getValue() != null && !depth.getText().isEmpty()
            && !soilAwc.getText().isEmpty() && !soilChoiceBox.getValue().getName().isEmpty()) {
          Soil soil = new Soil();
          soil.setName(soilChoiceBox.getValue().getName());
          soil.setAvailableWaterCapacity(gsehenInstance.parseDouble(soilAwc.getText()));

          SoilProfileDepth spd = new SoilProfileDepth();
          spd.setDepth(gsehenInstance.parseDouble(depth.getText()));

          soilList.add(soil);
          soilDepthList.add(spd);

          soilChoiceBox.setValue(null);
          soilAwc.setText(null);
          depth.setText(gsehenInstance.formatDoubleOneDecimal(spd.getDepth()));

          Text createdSoil = new Text(
              mainBundle.getString("fieldview.layer") + (layerList.size() + 1) + ": \n"
                  + mainBundle.getString("fieldview.soiltype") + soil.getName() + ";\n"
                  + mainBundle.getString("fieldview.awc") + soil.getAvailableWaterCapacity() + ";\n"
                  + mainBundle.getString("fieldview.depth") + spd.getDepth() + "\n\n");
          createdSoil.setFont(Font.font("Arial", FontPosture.ITALIC, 14));
          GridPane.setHalignment(createdSoil, HPos.LEFT);
          GridPane.setConstraints(createdSoil, 0, 4 + layerList.size() + 1);
          layerList.add(createdSoil);

          layerText.setText(mainBundle.getString("fieldview.layer") + (layerList.size() + 1));

          // Deletes a soil layer.
          Button delSoil = new Button(mainBundle.getString("fieldview.delsoil"));
          delSoil.setId("glass-grey");
          delSoil.setPrefSize(150, 25);
          delSoil.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
              soilList.remove(soil);
              soilDepthList.remove(spd);
              layerList.remove(createdSoil);
              center.getChildren().removeAll(createdSoil, delSoil);
              depth.setText(mainBundle.getString("fieldview.layer") + (layerList.size() - 1));
              layerText.setText(mainBundle.getString("fieldview.layer") + (layerList.size() + 1));

              for (Text t : layerList) {
                t.setText(t.getText().substring(0, 9) + index + t.getText().substring(10));
                index += 1;
              }
            }
          });
          GridPane.setHalignment(delSoil, HPos.LEFT);
          GridPane.setConstraints(delSoil, 1, 4 + layerList.size());

          center.getChildren().addAll(createdSoil, delSoil);
          depth.setText(mainBundle.getString("fieldview.layer") + (layerList.size() + 1));
        } else {
          Text error = new Text(mainBundle.getString("fieldview.error"));
          error.setFont(Font.font("Verdana", 14));
          error.setFill(Color.RED);
          buttonBox.getChildren().clear();
          buttonBox.getChildren().addAll(back, save, error);
        }
      }
    });

    // Set Row & Column Index for Nodes
    GridPane.setConstraints(layerText, 0, 0, 2, 1);
    GridPane.setConstraints(soil, 0, 1);
    GridPane.setConstraints(soilChoiceBox, 1, 1);
    GridPane.setConstraints(soilAwcLabel, 0, 2);
    GridPane.setConstraints(soilAwc, 1, 2);
    GridPane.setConstraints(depthLabel, 0, 3);
    GridPane.setConstraints(depth, 1, 3);
    GridPane.setConstraints(setSoil, 0, 4);

    center.getChildren().addAll(layerText, soil, soilChoiceBox, soilAwcLabel, soilAwc, depthLabel,
        depth, setSoil);
    ScrollPane scrollPane = new ScrollPane();
    scrollPane.setContent(center);
    scrollPane.setPannable(true);
    pane.setCenter(scrollPane);
    // CREATE SOILPROFILE END ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    back = new Button(mainBundle.getString("fieldview.back"));
    back.setId("glass-grey");
    back.setPrefSize(150, 25);
    back.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent arg0) {
        pane.getChildren().clear();
        treeTableView.setVisible(true);
        tabPane.getTabs().clear();
        tabPane.getTabs().addAll(mapViewTab, fieldViewTab, plotViewTab, logViewTab);
        gsehenInstance.sendFarmDataChanged(field, null);
        tabPane.getSelectionModel().select(1);
        treeTableView.getSelectionModel().clearSelection();
        treeTableView.getSelectionModel().select(currentItem);
      }
    });

    save = new Button(mainBundle.getString("button.accept"));
    save.setId("glass-grey");
    save.setPrefSize(150, 25);
    save.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent arg0) {
        if (!soilProfileName.getText().isEmpty() && !soilList.isEmpty()) {
          pane.getChildren().clear();
          treeTableView.setVisible(true);
          tabPane.getTabs().clear();
          tabPane.getTabs().addAll(mapViewTab, fieldViewTab, plotViewTab, logViewTab);

          SoilProfile soilProfileItem = new SoilProfile(DBUtil.generateUuid());

          soilProfileItem.setSoilType(soilList);
          soilProfileItem.setProfileDepth(soilDepthList);

          if (!soilManualKc.getText().isEmpty() && !soilManualZone.getText().isEmpty()
              && !soilManualRain.getText().isEmpty() && !soilManualPause.getText().isEmpty()) {
            soilManualData = new SoilManualData(gsehenInstance.parseDouble(soilManualKc.getText()),
                Integer.valueOf(soilManualZone.getText()),
                gsehenInstance.parseDouble(soilManualRain.getText()),
                Integer.valueOf(soilManualPause.getText()));
          } else {
            soilManualData = new SoilManualData(null, null, null, null);
          }
          soilProfileItem.setSoilManualData(soilManualData);

          soilProfileItem.setName(soilProfileName.getText());
          soilProfileList.add(soilProfileItem);
          pane.getChildren().clear();
          gsehenInstance.sendFarmDataChanged(field, null);
          tabPane.getSelectionModel().select(1);
          treeTableView.getSelectionModel().clearSelection();
          treeTableView.getSelectionModel().select(currentItem);
        } else {
          Text noNameOrSoil = new Text(mainBundle.getString("fieldview.nonameorsoil"));
          noNameOrSoil.setFont(Font.font("Verdana", 14));
          noNameOrSoil.setFill(Color.RED);
          buttonBox.getChildren().clear();
          buttonBox.getChildren().addAll(back, save, noNameOrSoil);
        }
      }
    });

    buttonBox = new HBox();
    buttonBox.setSpacing(10.0);
    buttonBox.getChildren().addAll(back, save);

    VBox bottomBox = new VBox(25);
    bottomBox.setSpacing(10);
    bottomBox.setPadding(new Insets(20, 20, 20, 20));
    bottomBox.getChildren().addAll(buttonBox);
    pane.setBottom(bottomBox);
  }

  /**
   * Edits a SoilProfile.
   */
  private void editSoilProfile() {
    if (currentSoilBox.getValue() != null) {
      pane.getChildren().clear();
      treeTableView.setVisible(false);
      tabPane.getTabs().removeAll(mapViewTab, plotViewTab, logViewTab);

      // GridPane - Center Section
      GridPane top = new GridPane();

      // GridPane Configuration (Padding, Gaps, etc.)
      top.setPadding(new Insets(20, 20, 20, 20));
      top.setHgap(15);
      top.setVgap(15);
      top.setGridLinesVisible(false);

      // Set Column and Row Constraints
      ColumnConstraints col1 = new ColumnConstraints(200, 100, 300);
      ColumnConstraints col2 = new ColumnConstraints(200, 100, 100);
      col1.setHgrow(Priority.ALWAYS);
      col2.setHgrow(Priority.ALWAYS);
      RowConstraints rowEmpty = new RowConstraints();

      // Add Constraints to Columns & Rows
      top.getColumnConstraints().addAll(col1, col2);
      top.getRowConstraints().add(0, rowEmpty);
      top.getRowConstraints().add(1, rowEmpty);

      SoilProfile currentSoilProfile = currentSoilBox.getValue();

      // Name
      Text soilNameLabel = new Text(mainBundle.getString("fieldview.profilename"));
      soilNameLabel.setFont(Font.font("Arial", 14));
      JFXTextField soilProfileName = new JFXTextField(currentSoilProfile.getName());
      soilProfileName.textProperty().addListener(new ChangeListener<String>() {
        @Override
        public void changed(ObservableValue<? extends String> observable, String oldValue,
            String newValue) {
          if (!newValue.isEmpty()) {
            currentSoilProfile.setName(soilProfileName.getText());
          }
        }
      });

      // kc-Wert
      Text soilManualKcLabel = new Text(mainBundle.getString("fieldview.manualkc"));
      soilManualKcLabel.setFont(Font.font("Arial", 14));
      if (currentSoilBox.getValue().getSoilManualData().getSoilKc() != null) {
        soilManualKc = new JFXTextField(String.valueOf(gsehenInstance
            .formatDoubleOneDecimal(currentSoilProfile.getSoilManualData().getSoilKc())));
      } else {
        soilManualKc = new JFXTextField();
      }
      soilManualKc.textProperty().addListener(new ChangeListener<String>() {
        @Override
        public void changed(ObservableValue<? extends String> observable, String oldValue,
            String newValue) {
          if (!newValue.isEmpty()) {
            if (!newValue.trim().isEmpty() && !gsehenInstance.isParseable(newValue)) {
              soilManualKc.setText(oldValue);
            } else {
              currentSoilProfile.getSoilManualData()
                  .setSoilKc(gsehenInstance.parseDouble(newValue));
            }
          }
        }
      });

      // Bilanzierungstiefe (in cm)
      Text soilManualZoneLabel = new Text(mainBundle.getString("fieldview.manualzone"));
      soilManualZoneLabel.setFont(Font.font("Arial", 14));
      if (currentSoilProfile.getSoilManualData().getSoilZone() != null) {
        soilManualZone = new JFXTextField(
            String.valueOf(currentSoilProfile.getSoilManualData().getSoilZone()));
      } else {
        soilManualZone = new JFXTextField();
      }
      soilManualZone.textProperty().addListener(new ChangeListener<String>() {
        @Override
        public void changed(ObservableValue<? extends String> observable, String oldValue,
            String newValue) {
          if (!newValue.isEmpty()) {
            if (!newValue.trim().isEmpty() && !gsehenInstance.isParseable(newValue)) {
              soilManualZone.setText(oldValue);
            } else {
              currentSoilProfile.getSoilManualData().setSoilZone(Integer.valueOf(newValue));
            }
          }
        }
      });

      // Schwelle des Regenereignis (in mm)
      Text soilManualRainLabel = new Text(mainBundle.getString("fieldview.manualrain"));
      soilManualRainLabel.setFont(Font.font("Arial", 14));
      if (currentSoilBox.getValue().getSoilManualData().getRainMax() != null) {
        soilManualRain = new JFXTextField(String.valueOf(gsehenInstance
            .formatDoubleOneDecimal(currentSoilProfile.getSoilManualData().getRainMax())));
      } else {
        soilManualRain = new JFXTextField();
      }
      soilManualRain.textProperty().addListener(new ChangeListener<String>() {
        @Override
        public void changed(ObservableValue<? extends String> observable, String oldValue,
            String newValue) {
          if (!newValue.isEmpty()) {
            if (!newValue.trim().isEmpty() && !gsehenInstance.isParseable(newValue)) {
              soilManualRain.setText(oldValue);
            } else {
              currentSoilProfile.getSoilManualData()
                  .setRainMax(gsehenInstance.parseDouble(newValue));
            }
          }
        }
      });

      // Bewässerungspause (in Tagen)
      Text soilManualPauseLabel = new Text(mainBundle.getString("fieldview.manualpause"));
      soilManualPauseLabel.setFont(Font.font("Arial", 14));
      if (currentSoilProfile.getSoilManualData().getDaysPause() != null) {
        soilManualPause = new JFXTextField(
            String.valueOf(currentSoilProfile.getSoilManualData().getDaysPause()));
      } else {
        soilManualPause = new JFXTextField();
      }
      soilManualPause.textProperty().addListener(new ChangeListener<String>() {
        @Override
        public void changed(ObservableValue<? extends String> observable, String oldValue,
            String newValue) {
          if (!newValue.isEmpty()) {
            if (!newValue.trim().isEmpty() && !gsehenInstance.isParseable(newValue)) {
              soilManualPause.setText(oldValue);
            } else {
              currentSoilProfile.getSoilManualData().setDaysPause(Integer.valueOf(newValue));
            }
          }
        }
      });

      // Set Row & Column Index for Nodes
      GridPane.setConstraints(soilNameLabel, 0, 0);
      GridPane.setConstraints(soilProfileName, 1, 0);
      GridPane.setConstraints(soilManualKcLabel, 0, 1);
      GridPane.setConstraints(soilManualKc, 1, 1);
      GridPane.setConstraints(soilManualZoneLabel, 0, 2);
      GridPane.setConstraints(soilManualZone, 1, 2);
      GridPane.setConstraints(soilManualRainLabel, 0, 3);
      GridPane.setConstraints(soilManualRain, 1, 3);
      GridPane.setConstraints(soilManualPauseLabel, 0, 4);
      GridPane.setConstraints(soilManualPause, 1, 4);

      top.getChildren().addAll(soilNameLabel, soilProfileName, soilManualKcLabel, soilManualKc,
          soilManualZoneLabel, soilManualZone, soilManualRainLabel, soilManualRain,
          soilManualPauseLabel, soilManualPause);
      pane.setTop(top);

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
      RowConstraints emptyRow = new RowConstraints();

      // Add Constraints to Columns & Rows
      center.getColumnConstraints().addAll(column1, column2);
      center.getRowConstraints().add(0, emptyRow);
      center.getRowConstraints().add(1, emptyRow);

      int row = 0;

      // Each layer the SoilProfile has
      for (int i = 0; i < currentSoilProfile.getSoilType().size(); i++) {
        // "Schicht #XY"
        Text layer = new Text(mainBundle.getString("fieldview.layer") + (i + 1));
        layer.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        // Bodentyp
        Text soil = new Text(mainBundle.getString("fieldview.soiltype"));
        soil.setFont(Font.font("Arial", 14));

        Soil s = new Soil();
        List<Soil> soils = s.soils();

        JFXComboBox<Soil> soilChoiceBox = new JFXComboBox<Soil>();
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

        Soil curSoil = currentSoilProfile.getSoilType().get(i);
        Text soilAwc = new Text();
        for (Soil setSoil : soilChoiceBox.getItems()) {
          if (setSoil.getName().equals(curSoil.getName())) {
            soilChoiceBox.getSelectionModel().select(setSoil);
            soilAwc.setText(
                gsehenInstance.formatDoubleOneDecimal(setSoil.getAvailableWaterCapacity()));
          }
        }

        // Wasserhaltekapazität
        Text soilAwcLabel = new Text(mainBundle.getString("fieldview.soilawc"));
        soilAwcLabel.setFont(Font.font("Arial", 14));

        int in = i;
        ChangeListener<Soil> changeListener = new ChangeListener<Soil>() {
          @Override
          public void changed(ObservableValue<? extends Soil> observable, //
              Soil oldValue, Soil newValue) {
            if (newValue != null) {
              soilAwc.setText(gsehenInstance
                  .formatDoubleOneDecimal(soilChoiceBox.getValue().getAvailableWaterCapacity()));
              currentSoilProfile.getSoilType().get(in).setName(soilChoiceBox.getValue().getName());
              currentSoilProfile.getSoilType().get(in)
                  .setAvailableWaterCapacity(soilChoiceBox.getValue().getAvailableWaterCapacity());
            }
          }
        };
        soilChoiceBox.getSelectionModel().selectedItemProperty().addListener(changeListener);

        // Tiefe
        soilDepth = new JFXTextField(gsehenInstance
            .formatDoubleOneDecimal(currentSoilProfile.getProfileDepth().get(i).getDepth()));
        Text depthLabel = new Text(mainBundle.getString("fieldview.depth"));
        depthLabel.setFont(Font.font("Arial", 14));
        soilDepth.textProperty().addListener(new ChangeListener<String>() {
          @Override
          public void changed(ObservableValue<? extends String> observable, String oldValue,
              String newValue) {
            if (!newValue.isEmpty()) {
              if (!newValue.trim().isEmpty() && !gsehenInstance.isParseable(newValue)) {
                soilDepth.setText(oldValue);
              } else {
                currentSoilProfile.getProfileDepth().get(in)
                    .setDepth(gsehenInstance.parseDouble(newValue));
              }
            }
          }
        });

        // Set Row & Column Index for Nodes
        GridPane.setConstraints(layer, 0, row);
        row += 1;
        GridPane.setConstraints(soil, 0, row);
        GridPane.setConstraints(soilChoiceBox, 1, row);
        row += 1;
        GridPane.setConstraints(soilAwcLabel, 0, row);
        GridPane.setConstraints(soilAwc, 1, row);
        row += 1;
        GridPane.setConstraints(depthLabel, 0, row);
        GridPane.setConstraints(soilDepth, 1, row);
        row += 1;

        center.getChildren().addAll(layer, soil, soilChoiceBox, soilAwcLabel, soilAwc, depthLabel,
            soilDepth);
      }

      ScrollPane scrollPane = new ScrollPane();
      scrollPane.setContent(center);
      scrollPane.setPannable(true);
      pane.setCenter(scrollPane);

      // Bearbeitung abschließen
      back = new Button(mainBundle.getString("fieldview.editend"));
      back.setId("glass-grey");
      back.setPrefSize(200, 25);
      back.setOnAction(new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent arg0) {
          if ((!soilProfileName.getText().isEmpty() && soilDepth != null
              && !soilDepth.getText().isEmpty())
              || (!soilManualKc.getText().isEmpty() && !soilManualZone.getText().isEmpty()
                  && !soilManualRain.getText().isEmpty() && !soilManualPause.getText().isEmpty())) {
            pane.getChildren().clear();
            treeTableView.setVisible(true);
            tabPane.getTabs().clear();
            tabPane.getTabs().addAll(mapViewTab, fieldViewTab, plotViewTab, logViewTab);
            gsehenInstance.sendFarmDataChanged(field, null);
            tabPane.getSelectionModel().select(1);
            treeTableView.getSelectionModel().clearSelection();
            treeTableView.getSelectionModel().select(currentItem);
          } else {
            Text profileChangeError = new Text(mainBundle.getString("fieldview.error"));
            profileChangeError.setFont(Font.font("Verdana", 14));
            profileChangeError.setFill(Color.RED);
            HBox bottom = new HBox();
            bottom.setSpacing(10);
            bottom.getChildren().addAll(back, profileChangeError);
            pane.setBottom(bottom);
          }
        }
      });
      pane.setBottom(back);
    }
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
  }

}
