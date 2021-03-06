package de.hgu.gsehen.gui.view;

import static de.hgu.gsehen.util.JavaFxUtil.noneIsEmpty;
import static de.hgu.gsehen.util.MessageUtil.logException;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTabPane;
import com.jfoenix.controls.JFXTextField;
import de.hgu.gsehen.Gsehen;
import de.hgu.gsehen.event.FarmDataChanged;
import de.hgu.gsehen.event.GsehenEventListener;
import de.hgu.gsehen.gui.GsehenGuiElements;
import de.hgu.gsehen.gui.util.ItemStringConverter;
import de.hgu.gsehen.model.Drawable;
import de.hgu.gsehen.model.Field;
import de.hgu.gsehen.model.Soil;
import de.hgu.gsehen.model.SoilManualData;
import de.hgu.gsehen.model.SoilProfile;
import de.hgu.gsehen.model.SoilProfileDepth;
import de.hgu.gsehen.model.WeatherDataConfigurator;
import de.hgu.gsehen.model.WeatherDataPlugin;
import de.hgu.gsehen.model.WeatherDataSource;
import de.hgu.gsehen.util.CollectionUtil;
import de.hgu.gsehen.util.DBUtil;
import de.hgu.gsehen.util.PluginUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class FieldDataController extends Application
    implements GsehenEventListener<FarmDataChanged>, WeatherDataConfigurator {
  private static final Logger LOGGER = Logger.getLogger(FieldDataController.class.getName());
  private static final String FARM_TREE_VIEW_ID = "#farmTreeView";
  protected final ResourceBundle mainBundle;
  private TreeMap<String, String> javaLocaleMap;
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
  private ConfigDialogElement<JFXComboBox<String>, String> pluginJsFileName;
  private ConfigDialogElement<CheckBox, Boolean> manualImport;
  private ConfigDialogElement<CheckBox, Boolean> automaticImport;
  private ConfigDialogElement<JFXTextField, Double> automaticImportIntervalSeconds;
  private ConfigDialogElement<JFXTextField, Double> locationLat;
  private ConfigDialogElement<JFXTextField, Double> locationLng;
  private ConfigDialogElement<JFXTextField, Double> metersAbove;

  private JFXTextField soilDepth;
  private JFXTextField soilManualKc;
  private JFXTextField soilManualZone;
  private JFXTextField soilManualRain;
  private JFXTextField soilManualPause;
  private int fixedNodesCount = -1;
  private int fixedItemsCount;
  private StackPane configStackPane;
  private GridPane configElementsParent;
  private WeatherDataPlugin weatherDataPlugin;

  {
    gsehenInstance = Gsehen.getInstance();

    Locale selectedLocale = gsehenInstance.getSelectedLocale();
    fillJavaLocaleMap(selectedLocale);
    mainBundle = ResourceBundle.getBundle("i18n.main", selectedLocale);

    soilProfileList = gsehenInstance.getSoilProfiles();
    weatherDataSourceList = gsehenInstance.getWeatherDataSources();

    gsehenInstance.registerForEvent(FarmDataChanged.class, this);
  }

  /**
   * Constructs a new field data controller associated with the given BorderPane.
   *
   * @param application the Gsehen application singleton reference
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
    // Name
    nameLabel = GsehenGuiElements.text(mainBundle.getString("fieldview.name"));

    name = new JFXTextField("");
    name.setPrefSize(150, 25);

    // m²
    areaLabel = GsehenGuiElements.text(mainBundle.getString("fieldview.area"));

    area = GsehenGuiElements.text("", FontWeight.BOLD);

    // Bodenprofil
    @SuppressWarnings("checkstyle:variabledeclarationusagedistance")
    Text soilProfile = GsehenGuiElements.text(mainBundle.getString("fieldview.soilprofile"));
    currentSoilBox = new JFXComboBox<SoilProfile>();
    currentSoilBox.setPrefSize(300, 25);
    if (!soilProfileList.isEmpty()) {
      for (SoilProfile s : soilProfileList) {
        currentSoilBox.getItems().add(s);
      }
      currentSoilBox.setConverter(new ItemStringConverter<SoilProfile>(currentSoilBox));
    }

    Button editProfile = GsehenGuiElements.button(200);
    editProfile.setText(mainBundle.getString("fieldview.editprofile"));
    editProfile.setOnAction(e -> editSoilProfile());

    createSoil = GsehenGuiElements.button(200);
    createSoil.setText(mainBundle.getString("fieldview.createprofile"));
    createSoil.setOnAction(e -> createSoilProfile());

    VBox soilBox = new VBox();
    soilBox.setSpacing(10);
    soilBox.getChildren().addAll(editProfile, createSoil);

    // Wetterdatenquelle
    @SuppressWarnings("checkstyle:variabledeclarationusagedistance")
    Text weatherDataSourceLabel = GsehenGuiElements
        .text(mainBundle.getString("fieldview.weatherdatasource") + ":");

    weatherData = new JFXComboBox<WeatherDataSource>();
    weatherData.setPrefSize(300, 25);
    if (!weatherDataSourceList.isEmpty()) {
      for (WeatherDataSource s : weatherDataSourceList) {
        weatherData.getItems().add(s);
      }
      weatherData.setConverter(new ItemStringConverter<WeatherDataSource>(weatherData));
    }

    Button editWds = GsehenGuiElements.button(200);
    editWds.setText(mainBundle.getString("fieldview.editwds"));
    editWds.setOnAction(e -> {
      if (weatherData.getValue() != null) {
        createWeatherDataSource();
        fillWeatherDataControls();
      }
    });

    Button createWds = GsehenGuiElements.button(200);
    createWds.setText(mainBundle.getString("fieldview.createwds"));
    createWds.setOnAction(e -> {
      createWeatherDataSource();
    });

    VBox weatherBox = new VBox();
    weatherBox.setSpacing(10);
    weatherBox.getChildren().addAll(editWds, createWds);

    // Speichern
    saveField = GsehenGuiElements.button(150);
    saveField.setText(mainBundle.getString("button.accept"));
    Text nameError = new Text(mainBundle.getString("fieldview.nameerror"));
    nameError.setFont(Font.font("Verdana", 14));
    nameError.setFill(Color.RED);

    GridPane grid = GsehenGuiElements.gridPane(pane);
    saveField.setOnAction(e -> {
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
    });

    GridPane.setConstraints(nameLabel, 0, 0);
    GridPane.setConstraints(name, 1, 0);
    GridPane.setConstraints(areaLabel, 0, 1);
    GridPane.setConstraints(area, 1, 1);
    GridPane.setConstraints(soilProfile, 0, 2);
    GridPane.setConstraints(currentSoilBox, 1, 2);
    GridPane.setConstraints(soilBox, 2, 2);
    GridPane.setConstraints(weatherDataSourceLabel, 0, 3);
    GridPane.setConstraints(weatherData, 1, 3);
    GridPane.setConstraints(weatherBox, 2, 3);
    GridPane.setConstraints(saveField, 0, 5);

    grid.getChildren().addAll(nameLabel, name, areaLabel, area, soilProfile, soilBox,
        weatherDataSourceLabel, weatherBox, currentSoilBox, weatherData, saveField);

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
    treeTableView.getSelectionModel().selectedItemProperty().addListener((o, oldVal, newVal) -> {
      for (int i = 0; i < treeTableView.getSelectionModel().getSelectedCells().size(); i++) {
        if (treeTableView.getSelectionModel().getSelectedCells().get(i) != null) {
          selectedItem = treeTableView.getSelectionModel().getSelectedCells().get(i).getTreeItem();
          if (selectedItem != null
              && selectedItem.getValue().getClass().getSimpleName().equals("Field")) {
            pane.setVisible(true);
            field = (Field) selectedItem.getValue();

            name.setText(field.getName());

            area.setText(gsehenInstance.formatDoubleOneDecimal(
                field.getPolygon().calculateArea(field.getPolygon().getGeoPoints())));
            
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

    Text setKc = new Text();
    String kc = "";
    if (sp.getSoilManualData().getSoilKc() != null) {
      kc = String.valueOf(sp.getSoilManualData().getSoilKc());
      setKc.setText(kc);
    }

    Text setZone = new Text();
    String zone = "";
    if (sp.getSoilManualData().getSoilZone() != null) {
      zone = String.valueOf(sp.getSoilManualData().getSoilZone());
      setZone.setText(zone);
    }

    Text setRain = new Text();
    String rain = "";
    if (sp.getSoilManualData().getRainMax() != null) {
      rain = String.valueOf(sp.getSoilManualData().getRainMax());
      setRain.setText(rain);
    }

    Text setPause = new Text();
    String pause = "";
    if (sp.getSoilManualData().getDaysPause() != null) {
      pause = String.valueOf(sp.getSoilManualData().getDaysPause());
      setPause.setText(pause);
    }

    Text soilProfileLabel = GsehenGuiElements.text(
        mainBundle.getString("fieldview.currentsoil") + " (" + sp.getName() + "):" + "\n",
        FontWeight.BOLD);
    Text setKcLabel = GsehenGuiElements.text(mainBundle.getString("fieldview.manualkc"));
    Text setZoneLabel = GsehenGuiElements.text(mainBundle.getString("fieldview.manualzone"));
    Text setRainLabel = GsehenGuiElements.text(mainBundle.getString("fieldview.manualrain"));
    Text setPauseLabel = GsehenGuiElements.text(mainBundle.getString("fieldview.manualpause"));

    GridPane.setConstraints(soilProfileLabel, 0, 0);
    GridPane.setConstraints(setKcLabel, 0, 1);
    GridPane.setConstraints(setKc, 1, 1);
    GridPane.setConstraints(setZoneLabel, 0, 2);
    GridPane.setConstraints(setZone, 1, 2);
    GridPane.setConstraints(setRainLabel, 0, 3);
    GridPane.setConstraints(setRain, 1, 3);
    GridPane.setConstraints(setPauseLabel, 0, 4);
    GridPane.setConstraints(setPause, 1, 4);

    GridPane grid = GsehenGuiElements.gridPane(pane);
    grid.getChildren().addAll(soilProfileLabel, setKcLabel, setZoneLabel, setRainLabel,
        setPauseLabel, setKc, setZone, setRain, setPause);

    int col = 5;
    for (Soil soil : sp.getSoilType()) {
      Text createdSoilLayer = new Text(
          "\n" + mainBundle.getString("fieldview.layer") + (index) + ":");
      GridPane.setConstraints(createdSoilLayer, 0, col);
      col++;

      Text createdSoil = new Text(mainBundle.getString("fieldview.soiltype"));
      GridPane.setConstraints(createdSoil, 0, col);

      Text soilName = new Text(soil.getName());
      GridPane.setConstraints(soilName, 1, col);
      col++;

      Text createdSoilWater = new Text(mainBundle.getString("fieldview.awc"));
      GridPane.setConstraints(createdSoilWater, 0, col);

      Text water = new Text(String.valueOf(soil.getAvailableWaterCapacity()));
      GridPane.setConstraints(water, 1, col);
      col++;

      Text createdSoilDepth = new Text(mainBundle.getString("fieldview.depth"));
      GridPane.setConstraints(createdSoilDepth, 0, col);

      Text depth = new Text(String.valueOf(sp.getProfileDepth().get(index - 1).getDepth()));
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

  private void fillButtonBox(Node... additionalElements) {
    final ObservableList<Node> children = buttonBox.getChildren();
    children.clear();
    children.addAll(back, save);
    children.addAll(additionalElements);
  }

  private void putErrorInButtonBox(final String messageKey) {
    setError(mainBundle.getString(messageKey));
  }

  /**
   * Creates the GUI for working with the details of a WeatherDataSource.
   *
   * <p>This method is usually called when a WDS is:
   * <ul>
   * <li>newly created</li>
   * <li>edited</li>
   * </ul>
   * </p>
   */
  private void createWeatherDataSource() {
    setWeatherDataPlugin(null);
    pane.getChildren().clear();
    treeTableView.setVisible(false);
    tabPane.getTabs().removeAll(mapViewTab, plotViewTab, logViewTab);
    createWeatherDataSourceNameSection();

    List<ConfigDialogElement<Node, Object>> weatherDataSourceConfigItems = createFixedNodes();
    fixedItemsCount = weatherDataSourceConfigItems.size();
    fixedNodesCount = countNodes(weatherDataSourceConfigItems);
    
    configureNodes(weatherDataSourceConfigItems, 0);
    configElementsParent = createGridPaneWithNodes(weatherDataSourceConfigItems);
    ScrollPane scrollPane = new ScrollPane();
    scrollPane.setContent(configElementsParent);
    scrollPane.setPannable(true);
    configStackPane = new StackPane(scrollPane);
    pane.setCenter(configStackPane);

    back = GsehenGuiElements.button(150);
    back.setText(mainBundle.getString("fieldview.back"));
    back.setOnAction(e -> recreateNormalGUI());

    save = GsehenGuiElements.button(150);
    save.setText(mainBundle.getString("button.accept"));
    save.setOnAction(
        arg -> CollectionUtil.testAndRun(
            () -> checkMandatoryBaseFields() && setAndPopulateWeatherDataSource(),
            () -> recreateNormalGUI()
        )
    );
    buttonBox = new HBox();
    buttonBox.setSpacing(10);
    reset();

    VBox bottomBox = new VBox(25);
    bottomBox.setSpacing(10);
    bottomBox.setPadding(new Insets(20, 20, 20, 20));
    bottomBox.getChildren().addAll(buttonBox);
    pane.setBottom(bottomBox);
  }

  private int countNodes(List<ConfigDialogElement<Node, Object>> weatherDataSourceConfigItems) {
    int count = 0;
    for (ConfigDialogElement<Node, Object> configDialogElement : weatherDataSourceConfigItems) {
      if (configDialogElement.getLabel() != null) {
        count++;
      }
      if (configDialogElement.getNode() != null) {
        count++;
      }
      if (configDialogElement.getExample() != null) {
        count++;
      }
    }
    return count;
  }

  private void createWeatherDataSourceNameSection() {
    Text weatherDataLabel = GsehenGuiElements
        .text(mainBundle.getString("fieldview.weatherdatalabel"));
    weatherDataSourceName = new JFXTextField();
    GridPane.setConstraints(weatherDataLabel, 0, 0);
    GridPane.setConstraints(weatherDataSourceName, 1, 0);
    GridPane top = GsehenGuiElements.gridPane(pane);
    top.getChildren().addAll(weatherDataLabel, weatherDataSourceName);
    pane.setTop(top);
  }

  private boolean checkMandatoryBaseFields() {
    final boolean bothSet = noneIsEmpty(
        new TextField[] { weatherDataSourceName, automaticImportIntervalSeconds.getNode() });
    if (!bothSet) {
      putErrorInButtonBox("fieldview.form.values.missing");
    }
    return bothSet;
  }

  @SuppressWarnings({"checkstyle:abbreviationaswordinname"})
  private void recreateNormalGUI() {
    fixedNodesCount = -1;
    pane.getChildren().clear();
    treeTableView.setVisible(true);
    tabPane.getTabs().clear();
    tabPane.getTabs().addAll(mapViewTab, fieldViewTab, plotViewTab, logViewTab);
    gsehenInstance.sendFarmDataChanged(field, null);
    tabPane.getSelectionModel().select(1);
    treeTableView.getSelectionModel().clearSelection();
    treeTableView.getSelectionModel().select(currentItem);
  }

  private List<ConfigDialogElement<Node, Object>> createFixedNodes() {
    List<ConfigDialogElement<Node, Object>> weatherDataSourceConfigItems = new ArrayList<>();
    pluginJsFileName = new ConfigDialogComboBox(
        GsehenGuiElements.text(mainBundle.getString("fieldview.weatherdatapluginjsfilenamelabel")),
        null, weatherDataSourceConfigItems,
        GsehenGuiElements.comboBox(PluginUtil.getPluginJsFileNames()),
        event -> setWeatherDataPlugin(((String)getComboBoxValue(event)).substring(2)));
    manualImport = new ConfigDialogCheckBox(
        GsehenGuiElements.text(mainBundle.getString("fieldview.weatherdatamanualimportlabel")),
        null, weatherDataSourceConfigItems);
    automaticImport = new ConfigDialogCheckBox(
        GsehenGuiElements.text(mainBundle.getString("fieldview.weatherdataautomaticimportlabel")),
        null, weatherDataSourceConfigItems);
    automaticImportIntervalSeconds = new ConfigDialogDoubleField(
        GsehenGuiElements
            .text(mainBundle.getString("fieldview.weatherdataautomaticimportintervalsecondslabel")),
        null, weatherDataSourceConfigItems, gsehenInstance);
    locationLat = new ConfigDialogDoubleField(
        GsehenGuiElements.text(mainBundle.getString("fieldview.locationlat")),
        GsehenGuiElements.text(mainBundle.getString("fieldview.example")
            + numberExample(mainBundle.getString("fieldview.locationlatvalue"))),
        weatherDataSourceConfigItems, gsehenInstance);
    locationLng = new ConfigDialogDoubleField(
        GsehenGuiElements.text(mainBundle.getString("fieldview.locationlng")),
        GsehenGuiElements.text(mainBundle.getString("fieldview.example")
            + numberExample(mainBundle.getString("fieldview.locationlngvalue"))),
        weatherDataSourceConfigItems, gsehenInstance);
    metersAbove = new ConfigDialogDoubleField(
        GsehenGuiElements.text(mainBundle.getString("fieldview.metersabove")), null,
        weatherDataSourceConfigItems, gsehenInstance);
    return weatherDataSourceConfigItems;
  }

  @SuppressWarnings("unchecked")
  private <T> T getComboBoxValue(ActionEvent actionEvent) {
    return ((JFXComboBox<T>)actionEvent.getSource()).getValue();
  }

  private String numberExample(final String string) {
    return " " + gsehenInstance.formatDoubleTwoDecimal(gsehenInstance.parseDouble(string));
  }

  private void configureNodes(List<ConfigDialogElement<Node, Object>> nodes, int startIndex) {
    int nodeIndex = 0;
    for (ConfigDialogElement<Node, Object> node : nodes) {
      if (nodeIndex >= startIndex) {
        GridPane.setConstraints(node.getLabel(), 0, nodeIndex);
        GridPane.setConstraints(node.getNode(), 1, nodeIndex);
        final Text example = node.getExample();
        if (example != null) {
          example.setFont(Font.font("Arial", FontPosture.ITALIC, 12));
          GridPane.setConstraints(example, 2, nodeIndex);
        }
      }
      nodeIndex++;
    }
  }

  private GridPane createGridPaneWithNodes(List<ConfigDialogElement<Node, Object>> nodes) {
    GridPane gridPane = GsehenGuiElements.gridPane(pane);
    ObservableList<Node> gridPaneChildren = gridPane.getChildren();
    for (ConfigDialogElement<Node, Object> node : nodes) {
      gridPaneChildren.add(node.getLabel());
      gridPaneChildren.add(node.getNode());
      final Text example = node.getExample();
      if (example != null) {
        gridPaneChildren.add(example);
      }
    }
    return gridPane;
  }

  /**
   * Sets the current weather data source and populates it from the GUI controls.
   *
   * @see checkMandatoryBaseFields
   * @return true if all values were applied successfully, false otherwise
   */
  @SuppressWarnings({"checkstyle:abbreviationaswordinname"})
  private boolean setAndPopulateWeatherDataSource() {
    WeatherDataSource currentWDS = wds;
    boolean addWDStoList = false;
    try {
      if (selectedWeatherDataSource != null) {
        wds = selectedWeatherDataSource;
      } else {
        wds = new WeatherDataSource(DBUtil.generateUuid());
        addWDStoList = true;
      }
      wds.setName(weatherDataSourceName.getText());
      wds.setPluginJsFileName(pluginJsFileName.getNodeValue().substring(2));
      wds.setManualImportActive(manualImport.getNodeValue());
      wds.setAutomaticImportActive(automaticImport.getNodeValue());
      wds.setAutomaticImportFrequencySeconds(automaticImportIntervalSeconds.getNodeValue());
      wds.setLocationLat(locationLat.getNodeValue());
      wds.setLocationLng(locationLng.getNodeValue());
      wds.setLocationMetersAboveSeaLevel(metersAbove.getNodeValue());
      processPluginSpecificConfiguration();
    } catch (Exception e) {
      logException(LOGGER, Level.FINE, e, "weather.data.source.parameter.failure");
      wds = currentWDS;
      return false;
    }
    if (addWDStoList) {
      weatherDataSourceList.add(wds);
    }
    return true;
  }

  private void fillWeatherDataControls() {
    selectedWeatherDataSource = weatherData.getSelectionModel().getSelectedItem();
    if (selectedWeatherDataSource != null) {
      weatherDataSourceName.setText(selectedWeatherDataSource.getName());
      final String selectedPluginJsFileName = selectedWeatherDataSource.getPluginJsFileName();
      pluginJsFileName.setNodeValue(CollectionUtil.findItem(pluginJsFileName.getNode().getItems(),
          item -> item.substring(2).equals(selectedPluginJsFileName)));
      manualImport.setNodeValue(selectedWeatherDataSource.isManualImportActive());
      automaticImport.setNodeValue(selectedWeatherDataSource.isAutomaticImportActive());
      automaticImportIntervalSeconds
          .setNodeValue(selectedWeatherDataSource.getAutomaticImportFrequencySeconds());
      locationLat.setNodeValue(selectedWeatherDataSource.getLocationLat());
      locationLng.setNodeValue(selectedWeatherDataSource.getLocationLng());
      metersAbove.setNodeValue(selectedWeatherDataSource.getLocationMetersAboveSeaLevel());
      setWeatherDataPlugin(selectedPluginJsFileName);
    }
  }

  private void processPluginSpecificConfiguration() {
    if (weatherDataPlugin == null) {
      return;
    }
    wds.setPluginConfigurationJSON(weatherDataPlugin.getSpecificConfigurationJSON());
  }

  private void setWeatherDataPlugin(String pluginJsFileName) {
    if (fixedNodesCount == -1) {
      return;
    }
    final ObservableList<Node> configNodes = getConfigNodes();
    for (int i = configNodes.size() - 1; i >= fixedNodesCount; i--) {
      configNodes.remove(i);
    }
    if (pluginJsFileName == null) {
      weatherDataPlugin = null;
    } else {
      weatherDataPlugin = PluginUtil.getPlugin(pluginJsFileName, WeatherDataPlugin.class);
    }
    if (weatherDataPlugin == null) {
      return;
    }
    weatherDataPlugin.createAndFillSpecificControls(
        selectedWeatherDataSource != null && samePluginJsFileName(pluginJsFileName)
          ? selectedWeatherDataSource.getPluginConfigurationJSON()
          : "{}", this
    );
  }

  private boolean samePluginJsFileName(String newPluginJsFileName) {
    String oldPluginJsFileName = selectedWeatherDataSource.getPluginJsFileName();
    if (newPluginJsFileName == null) {
      if (oldPluginJsFileName == null) {
        return true;
      }
    } else {
      if (newPluginJsFileName.equals(oldPluginJsFileName)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Creates a new SoilProfile.
   */
  private void createSoilProfile() {
    pane.getChildren().clear();
    treeTableView.setVisible(false);
    tabPane.getTabs().removeAll(mapViewTab, plotViewTab, logViewTab);

    // kc-Wert
    soilManualKc = new JFXTextField("");

    // Bilanzierungstiefe (in cm)
    soilManualZone = new JFXTextField("");

    // Schwelle des Regenereignis (in mm)
    soilManualRain = new JFXTextField("");

    // Bewässerungspause (in Tagen)
    Text soilManualPauseLabel = GsehenGuiElements
        .text(mainBundle.getString("fieldview.manualpause"));

    JFXTextField soilManualPause = new JFXTextField("");

    JFXTextField soilProfileName = new JFXTextField("");

    Text soilNameLabel = GsehenGuiElements.text(mainBundle.getString("fieldview.profilename"));
    Text soilManualKcLabel = GsehenGuiElements.text(mainBundle.getString("fieldview.manualkc"));
    Text soilManualZoneLabel = GsehenGuiElements.text(mainBundle.getString("fieldview.manualzone"));
    Text soilManualRainLabel = GsehenGuiElements.text(mainBundle.getString("fieldview.manualrain"));

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

    // GridPane - Top Section
    GridPane top = GsehenGuiElements.gridPane(pane);

    top.getChildren().addAll(soilNameLabel, soilProfileName, soilManualKcLabel, soilManualKc,
        soilManualZoneLabel, soilManualZone, soilManualRainLabel, soilManualRain,
        soilManualPauseLabel, soilManualPause);
    pane.setTop(top);

    layerList = new ArrayList<Text>();

    // "Schicht #XY"
    Text layerText = new Text(mainBundle.getString("fieldview.layer") + (layerList.size() + 1));
    layerText.setFont(Font.font("Arial", FontWeight.BOLD, 18));

    // Bodentyp
    Soil s = new Soil();
    List<Soil> soils = s.soils();

    JFXComboBox<Soil> soilChoiceBox = new JFXComboBox<Soil>();
    soilChoiceBox.getItems().addAll(soils);
    soilChoiceBox.setConverter(new ItemStringConverter<Soil>(soilChoiceBox));

    // Wasserhaltekapazität
    JFXTextField soilAwc = new JFXTextField();

    // Sets the 'soilAwc', if the ChoiceBox-Value changed
    ChangeListener<Soil> changeListener = (o, oldValue, newValue) -> {
      if (newValue != null) {
        soilAwc.setText(gsehenInstance
            .formatDoubleOneDecimal(soilChoiceBox.getValue().getAvailableWaterCapacity()));
      }
    };
    soilChoiceBox.getSelectionModel().selectedItemProperty().addListener(changeListener);

    // Tiefe
    JFXTextField depth = new JFXTextField("25");
    depth.textProperty().addListener((o, oldValue, newValue) -> {
      if (newValue != null) {
        if (!newValue.trim().isEmpty() && !gsehenInstance.isParseable(newValue)) {
          depth.setText(oldValue);
        }
      }
    });

    // GridPane - Center Section
    GridPane center = GsehenGuiElements.gridPane(pane);

    List<Soil> soilList = new ArrayList<Soil>();
    List<SoilProfileDepth> soilDepthList = new ArrayList<SoilProfileDepth>();

    // Schicht abschließen
    Button setSoil = GsehenGuiElements.button(200);
    setSoil.setText(mainBundle.getString("fieldview.setsoil"));
    setSoil.setOnAction(e -> {
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
        Button delSoil = GsehenGuiElements.button(150);
        delSoil.setText(mainBundle.getString("fieldview.delsoil"));
        delSoil.setOnAction(ae -> {
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
        });
        GridPane.setHalignment(delSoil, HPos.LEFT);
        GridPane.setConstraints(delSoil, 1, 4 + layerList.size());

        center.getChildren().addAll(createdSoil, delSoil);
        depth.setText(mainBundle.getString("fieldview.layer") + (layerList.size() + 1));
      } else {
        putErrorInButtonBox("fieldview.form.values.missing");
      }
    });

    Text soilLabel = GsehenGuiElements.text(mainBundle.getString("fieldview.soiltype"));
    Text soilAwcLabel = GsehenGuiElements.text(mainBundle.getString("fieldview.soilawc"));
    Text depthLabel = GsehenGuiElements.text(mainBundle.getString("fieldview.depth"));

    // Set Row & Column Index for Nodes
    GridPane.setConstraints(layerText, 0, 0, 2, 1);
    GridPane.setConstraints(soilLabel, 0, 1);
    GridPane.setConstraints(soilChoiceBox, 1, 1);
    GridPane.setConstraints(soilAwcLabel, 0, 2);
    GridPane.setConstraints(soilAwc, 1, 2);
    GridPane.setConstraints(depthLabel, 0, 3);
    GridPane.setConstraints(depth, 1, 3);
    GridPane.setConstraints(setSoil, 0, 4);

    center.getChildren().addAll(layerText, soilLabel, soilChoiceBox, soilAwcLabel, soilAwc,
        depthLabel, depth, setSoil);
    ScrollPane scrollPane = new ScrollPane();
    scrollPane.setContent(center);
    scrollPane.setPannable(true);
    pane.setCenter(scrollPane);
    // CREATE SOILPROFILE END ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    back = GsehenGuiElements.button(150);
    back.setText(mainBundle.getString("fieldview.back"));
    back.setOnAction(e -> {
      pane.getChildren().clear();
      treeTableView.setVisible(true);
      tabPane.getTabs().clear();
      tabPane.getTabs().addAll(mapViewTab, fieldViewTab, plotViewTab, logViewTab);
      gsehenInstance.sendFarmDataChanged(field, null);
      tabPane.getSelectionModel().select(1);
      treeTableView.getSelectionModel().clearSelection();
      treeTableView.getSelectionModel().select(currentItem);
    });

    save = GsehenGuiElements.button(150);
    save.setText(mainBundle.getString("button.accept"));
    save.setOnAction(e -> {
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
        putErrorInButtonBox("fieldview.nonameorsoil");
      }
    });

    buttonBox = new HBox();
    buttonBox.setSpacing(10.0);
    reset();

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

      SoilProfile currentSoilProfile = currentSoilBox.getValue();

      // Name
      JFXTextField soilProfileName = new JFXTextField(currentSoilProfile.getName());
      soilProfileName.textProperty().addListener((o, oldValue, newValue) -> {
        if (!newValue.isEmpty()) {
          currentSoilProfile.setName(soilProfileName.getText());
        }
      });

      // kc-Wert
      if (currentSoilBox.getValue().getSoilManualData().getSoilKc() != null) {
        soilManualKc = new JFXTextField(String.valueOf(gsehenInstance
            .formatDoubleOneDecimal(currentSoilProfile.getSoilManualData().getSoilKc())));
      } else {
        soilManualKc = new JFXTextField();
      }
      soilManualKc.textProperty().addListener((o, oldValue, newValue) -> {
        if (!newValue.isEmpty()) {
          if (!newValue.trim().isEmpty() && !gsehenInstance.isParseable(newValue)) {
            soilManualKc.setText(oldValue);
          } else {
            currentSoilProfile.getSoilManualData()
                .setSoilKc(gsehenInstance.parseDouble(newValue));
          }
        }
      });

      // Bilanzierungstiefe (in cm)
      if (currentSoilProfile.getSoilManualData().getSoilZone() != null) {
        soilManualZone = new JFXTextField(
            String.valueOf(currentSoilProfile.getSoilManualData().getSoilZone()));
      } else {
        soilManualZone = new JFXTextField();
      }
      soilManualZone.textProperty().addListener((o, oldValue, newValue) -> {
        if (!newValue.isEmpty()) {
          if (!newValue.trim().isEmpty() && !gsehenInstance.isParseable(newValue)) {
            soilManualZone.setText(oldValue);
          } else {
            currentSoilProfile.getSoilManualData().setSoilZone(Integer.valueOf(newValue));
          }
        }
      });

      // Schwelle des Regenereignis (in mm)
      if (currentSoilBox.getValue().getSoilManualData().getRainMax() != null) {
        soilManualRain = new JFXTextField(String.valueOf(gsehenInstance
            .formatDoubleOneDecimal(currentSoilProfile.getSoilManualData().getRainMax())));
      } else {
        soilManualRain = new JFXTextField();
      }
      soilManualRain.textProperty().addListener((o, oldValue, newValue) -> {
        if (!newValue.isEmpty()) {
          if (!newValue.trim().isEmpty() && !gsehenInstance.isParseable(newValue)) {
            soilManualRain.setText(oldValue);
          } else {
            currentSoilProfile.getSoilManualData()
                .setRainMax(gsehenInstance.parseDouble(newValue));
          }
        }
      });

      // Bewässerungspause (in Tagen)
      if (currentSoilProfile.getSoilManualData().getDaysPause() != null) {
        soilManualPause = new JFXTextField(
            String.valueOf(currentSoilProfile.getSoilManualData().getDaysPause()));
      } else {
        soilManualPause = new JFXTextField();
      }
      soilManualPause.textProperty().addListener((o, oldValue, newValue) -> {
        if (!newValue.isEmpty()) {
          if (!newValue.trim().isEmpty() && !gsehenInstance.isParseable(newValue)) {
            soilManualPause.setText(oldValue);
          } else {
            currentSoilProfile.getSoilManualData().setDaysPause(Integer.valueOf(newValue));
          }
        }
      });

      Text soilNameLabel = GsehenGuiElements.text(mainBundle.getString("fieldview.profilename"));
      Text soilManualKcLabel = GsehenGuiElements.text(mainBundle.getString("fieldview.manualkc"));
      Text soilManualZoneLabel = GsehenGuiElements
          .text(mainBundle.getString("fieldview.manualzone"));
      Text soilManualRainLabel = GsehenGuiElements
          .text(mainBundle.getString("fieldview.manualrain"));
      Text soilManualPauseLabel = GsehenGuiElements
          .text(mainBundle.getString("fieldview.manualpause"));

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

      // GridPane - Top Section
      GridPane top = GsehenGuiElements.gridPane(pane);

      top.getChildren().addAll(soilNameLabel, soilProfileName, soilManualKcLabel, soilManualKc,
          soilManualZoneLabel, soilManualZone, soilManualRainLabel, soilManualRain,
          soilManualPauseLabel, soilManualPause);
      pane.setTop(top);

      // GridPane - Center Section
      GridPane center = GsehenGuiElements.gridPane(pane);

      int row = 0;

      // Each layer the SoilProfile has
      for (int i = 0; i < currentSoilProfile.getSoilType().size(); i++) {
        // Bodentyp
        Soil s = new Soil();
        List<Soil> soils = s.soils();

        JFXComboBox<Soil> soilChoiceBox = new JFXComboBox<Soil>();
        soilChoiceBox.getItems().addAll(soils);
        soilChoiceBox.setConverter(new ItemStringConverter<Soil>(soilChoiceBox));

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
        int in = i;
        ChangeListener<Soil> changeListener = (o, oldValue, newValue) -> {
          if (newValue != null) {
            soilAwc.setText(gsehenInstance
                .formatDoubleOneDecimal(soilChoiceBox.getValue().getAvailableWaterCapacity()));
            currentSoilProfile.getSoilType().get(in).setName(soilChoiceBox.getValue().getName());
            currentSoilProfile.getSoilType().get(in)
                .setAvailableWaterCapacity(soilChoiceBox.getValue().getAvailableWaterCapacity());
          }
        };
        soilChoiceBox.getSelectionModel().selectedItemProperty().addListener(changeListener);

        // Tiefe
        soilDepth = new JFXTextField(gsehenInstance
            .formatDoubleOneDecimal(currentSoilProfile.getProfileDepth().get(i).getDepth()));

        soilDepth.textProperty().addListener((o, oldValue, newValue) -> {
          if (!newValue.isEmpty()) {
            if (!newValue.trim().isEmpty() && !gsehenInstance.isParseable(newValue)) {
              soilDepth.setText(oldValue);
            } else {
              currentSoilProfile.getProfileDepth().get(in)
                  .setDepth(gsehenInstance.parseDouble(newValue));
            }
          }
        });

        // "Schicht #XY"
        Text layer = GsehenGuiElements.text(mainBundle.getString("fieldview.layer") + (i + 1));
        Text soilLabel = GsehenGuiElements.text(mainBundle.getString("fieldview.soiltype"));

        // Set Row & Column Index for Nodes
        GridPane.setConstraints(layer, 0, row);
        row += 1;
        GridPane.setConstraints(soilLabel, 0, row);
        GridPane.setConstraints(soilChoiceBox, 1, row);
        row += 1;
        Text soilAwcLabel = GsehenGuiElements.text(mainBundle.getString("fieldview.soilawc"));
        GridPane.setConstraints(soilAwcLabel, 0, row);
        GridPane.setConstraints(soilAwc, 1, row);
        row += 1;
        Text depthLabel = GsehenGuiElements.text(mainBundle.getString("fieldview.depth"));
        GridPane.setConstraints(depthLabel, 0, row);
        GridPane.setConstraints(soilDepth, 1, row);
        row += 1;

        center.getChildren().addAll(layer, soilLabel, soilChoiceBox, soilAwcLabel, soilAwc,
            depthLabel, soilDepth);
      }

      ScrollPane scrollPane = new ScrollPane();
      scrollPane.setContent(center);
      scrollPane.setPannable(true);
      pane.setCenter(scrollPane);

      // Bearbeitung abschließen
      back = GsehenGuiElements.button(200);
      back.setText(mainBundle.getString("fieldview.editend"));
      back.setOnAction(e -> {
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
          Text profileChangeError = GsehenGuiElements
              .text(mainBundle.getString("fieldview.form.values.missing"));
          profileChangeError.setFill(Color.RED);
          HBox bottom = new HBox();
          bottom.setSpacing(10);
          bottom.getChildren().addAll(back, profileChangeError);
          pane.setBottom(bottom);
        }
      });
      pane.setBottom(back);
    }
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
  }

  @Override
  public ObservableList<Node> getConfigNodes() {
    return configElementsParent.getChildren();
  }

  @Override
  public int getFixedNodesCount() {
    return fixedNodesCount;
  }

  @Override
  public int getFixedItemsCount() {
    return fixedItemsCount;
  }

  @Override
  public Gsehen getInstance() {
    return gsehenInstance;
  }

  @Override
  public Locale getLocale() {
    return gsehenInstance.getSelectedLocale();
  }

  @Override
  public TreeMap<String, String> getJavaLocaleMap() {
    return javaLocaleMap;
  }

  @Override
  public StackPane getParentStackPane() {
    return configStackPane;
  }

  @Override
  public void setError(String errorMessage) {
    Text error = new Text(errorMessage);
    error.setFont(Font.font("Verdana", 14));
    error.setFill(Color.RED);
    fillButtonBox(error);
  }

  @Override
  public void reset() {
    fillButtonBox();
  }
}
