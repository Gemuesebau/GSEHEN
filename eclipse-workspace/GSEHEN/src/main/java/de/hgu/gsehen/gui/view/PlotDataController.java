package de.hgu.gsehen.gui.view;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXSlider;
import com.jfoenix.controls.JFXTabPane;
import com.jfoenix.controls.JFXTextField;
import de.hgu.gsehen.Gsehen;
import de.hgu.gsehen.evapotranspiration.DayData;
import de.hgu.gsehen.event.FarmDataChanged;
import de.hgu.gsehen.event.GsehenEventListener;
import de.hgu.gsehen.gui.CombinedBarAndLineChart;
import de.hgu.gsehen.gui.CropPhase;
import de.hgu.gsehen.gui.GeoPoint;
import de.hgu.gsehen.gui.GsehenGuiElements;
import de.hgu.gsehen.model.Crop;
import de.hgu.gsehen.model.CropDevelopmentStatus;
import de.hgu.gsehen.model.CropRootingZone;
import de.hgu.gsehen.model.Drawable;
import de.hgu.gsehen.model.Field;
import de.hgu.gsehen.model.ManualData;
import de.hgu.gsehen.model.ManualWaterSupply;
import de.hgu.gsehen.model.Plot;
import de.hgu.gsehen.util.DateUtil;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.StringConverter;

public class PlotDataController implements GsehenEventListener<FarmDataChanged> {
  private static PlotDataController instance;
  private static final String FARM_TREE_VIEW_ID = "#farmTreeView";
  protected final ResourceBundle mainBundle;

  private List<Crop> cropList = new ArrayList<>();

  private Field field;
  private Plot plot;

  private Gsehen gsehenInstance;
  private BorderPane pane;
  private TreeTableView<Drawable> treeTableView;
  private int currentItem;
  private JFXTabPane tabPane;
  private TableView<CropPhase> cropTable;
  private List<CropPhase> cropPhases;
  private Date todayDate;
  private JFXComboBox<String> startType;

  private Text nameLabel;
  private Text areaLabel;
  private Text locationLabel;
  private Text rootingZoneLabel;
  private Text soilStartLabel;
  private Text soilStartValueLabel;
  private Text cropStartLabel;

  private JFXTextField name;
  private JFXTextField rootingZone;
  private JFXComboBox<Crop> cropChoiceBox;
  private JFXSlider scalingFactor;
  private Text area;
  private Hyperlink location;
  private DatePicker soilStart;
  private DatePicker cropStart;
  private JFXSlider soilStartValue;
  private Button harvest;
  private Button watering;
  private Button save;

  private boolean isActive = true;
  private HBox bottomBox;
  private Text error;

  private SortedList<Data<Date, Number>> precBarDataList;
  private SortedList<Data<Date, Number>> irriBarDataList;
  private SortedList<Data<Date, Number>> caswDataList;
  private SortedList<Data<Date, Number>> twbDataList;
  private ObservableList<Data<Date, Number>> precBarData;
  private ObservableList<Data<Date, Number>> irriBarData;
  private ObservableList<Data<Date, Number>> caswData;
  private ObservableList<Data<Date, Number>> twbData;
  private CombinedBarAndLineChart barLineChart;
  private SwingNode chartPanel;

  {
    instance = this;

    gsehenInstance = Gsehen.getInstance();
    cropList = gsehenInstance.getCrops();

    gsehenInstance.registerForEvent(FarmDataChanged.class, this);

    mainBundle = ResourceBundle.getBundle("i18n.main", gsehenInstance.getSelectedLocale());
  }

  /**
   * Constructs a new plot data controller associated with the given BorderPane.
   *
   * @param application the Gsehen application singleton reference
   * @param pane
   *          - the associated BorderPane.
   */
  public PlotDataController(Gsehen application, BorderPane pane) {
    this.gsehenInstance = application;
    this.pane = pane;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  public void handle(FarmDataChanged event) {
    pane.setVisible(false);

    // CENTER (From "Name" to "Bewässerungsfaktor")
    // Name
    nameLabel = GsehenGuiElements.text(mainBundle.getString("fieldview.name"));
    name = new JFXTextField("");

    // m²
    areaLabel = GsehenGuiElements.text(mainBundle.getString("fieldview.area"));
    area = GsehenGuiElements.text("");

    // Lage
    locationLabel = GsehenGuiElements.text(mainBundle.getString("plotview.location"));
    location = new Hyperlink("");
    location.setFont(Font.font("Arial", 14));

    // Max. durchw. Zone
    rootingZoneLabel = GsehenGuiElements.text(mainBundle.getString("plotview.rootingzone"));
    rootingZone = new JFXTextField("");
    rootingZone.textProperty().addListener((obs, oldVal, newVal) -> {
      if (newVal != null) {
        if (!newVal.trim().isEmpty() && !gsehenInstance.isParseable(newVal)) {
          rootingZone.setText(oldVal);
        }
      }
    });

    startType = new JFXComboBox<String>();
    startType.getItems().addAll(mainBundle.getString("plotview.directstart"),
        mainBundle.getString("plotview.delayedstart"));
    startType.setPrefSize(200, 25);

    // Start der Inkulturnahme
    cropStartLabel = GsehenGuiElements.text(mainBundle.getString("plotview.cropstart"));
    cropStart = GsehenGuiElements.datepicker();

    // Start der Bodenwasserbilanz
    soilStartLabel = GsehenGuiElements.text(mainBundle.getString("plotview.soilstart"));
    soilStart = GsehenGuiElements.datepicker();

    // Startwert der Wasserbilanz
    soilStartValueLabel = GsehenGuiElements.text(mainBundle.getString("plotview.soilstartvalue"));
    soilStartValue = new JFXSlider();
    soilStartValue.setMin(0.0);
    soilStartValue.setMax(100.0);
    soilStartValue.setValue(100.0);
    soilStartValue.setShowTickLabels(true);
    soilStartValue.setShowTickMarks(true);
    soilStartValue.setMajorTickUnit(25.0);
    soilStartValue.setBlockIncrement(5.0);
    soilStartValue.valueProperty()
        .addListener((obs, oldVal, newVal) -> soilStartValue.setValue(newVal.intValue()));

    // Kultur
    cropChoiceBox = new JFXComboBox<Crop>();
    cropChoiceBox.setPrefSize(200, 25);
    if (!cropList.isEmpty()) {
      for (Crop c : cropList) {
        cropChoiceBox.getItems().add(c);
      }
    } else {
      cropChoiceBox.getItems().addAll(new Crop(mainBundle.getString("plotview.empty"), true, 0.0,
          0.0, 0.0, 0.0, 0, 0, 0, 0, "", "", "", "", 0, 0, 0, 0, ""));
    }
    cropChoiceBox.setConverter(new StringConverter<Crop>() {
      @Override
      public String toString(Crop crop) {
        return gsehenInstance.localizeCropText(crop.getName());
      }

      @Override
      public Crop fromString(String string) {
        return null;
      }
    });

    // Beschreibung (Accordion)
    TitledPane descriptionPane = new TitledPane();
    descriptionPane.setText(mainBundle.getString("plotview.description"));
    HBox descriptionBox = new HBox();
    Text descriptionText = GsehenGuiElements.text("");
    descriptionText.wrappingWidthProperty().bind(pane.widthProperty().divide(3.5));
    descriptionBox.setPadding(new Insets(5, 5, 5, 5));
    descriptionBox.getChildren().add(descriptionText);
    descriptionPane.setContent(descriptionBox);
    Accordion descriptionAccordion = new Accordion();
    descriptionAccordion.getPanes().add(descriptionPane);

    cropChoiceBox.getSelectionModel().selectedItemProperty().addListener(
        (obs, oldVal, newVal) -> {
          if (newVal != null && newVal != oldVal) {
            Tooltip tooltip = new Tooltip(
                gsehenInstance.localizeCropText(cropChoiceBox.getValue().getDescription()));
            tooltip.setStyle("-fx-font-style: italic; -fx-background-color: #ffffff; "
                + "-fx-text-fill: #000000; -fx-font-size: 9pt;");
            cropChoiceBox.setTooltip(tooltip);
            descriptionText
              .setText(gsehenInstance.localizeCropText(cropChoiceBox.getValue().getDescription()));

            if (!newVal.equals(plot.getCrop())) {
              plot.setCropDevelopmentStatus(new CropDevelopmentStatus(
                  newVal.getPhase1(), newVal.getPhase2(),
                  newVal.getPhase3(), newVal.getPhase4()));
              plot.setCropRootingZone(new CropRootingZone(
                  newVal.getRootingZone1(), newVal.getRootingZone2(),
                  newVal.getRootingZone3(), newVal.getRootingZone4()));
              plot.setCrop(newVal);
            }
            setTableData();
          }
        });

    // TableView where you can set the duration of each crop phase and the rooting-zone of each crop
    cropTable = new TableView();

    // each TableColumn
    TableColumn phase = new TableColumn(mainBundle.getString("tableview.phase"));
    TableColumn description = new TableColumn(mainBundle.getString("tableview.description"));
    TableColumn today = new TableColumn(mainBundle.getString("tableview.currentphase"));
    TableColumn startDate = new TableColumn(mainBundle.getString("tableview.startdate"));
    TableColumn duration = new TableColumn(mainBundle.getString("tableview.duration"));
    TableColumn cropRootingZone = new TableColumn(mainBundle.getString("tableview.rootingzone"));

    cropTable.getColumns().addAll(phase, description, today, startDate, duration, cropRootingZone);

    // Configuration of each TableColumn
    phase.setMinWidth(20);
    phase.setStyle("-fx-alignment:top-center; -fx-font-style: italic");
    phase.setCellValueFactory(new PropertyValueFactory<CropPhase, Integer>("phase"));
    description.setMinWidth(125);
    description.setStyle("-fx-alignment:top-center; -fx-font-style: italic");
    description.setCellValueFactory(new PropertyValueFactory<CropPhase, String>("description"));
    today.setMinWidth(125);
    today.setStyle("-fx-alignment:top-right");
    today.setCellValueFactory(new PropertyValueFactory<CropPhase, String>("todayMarker"));
    startDate.setMinWidth(100);
    startDate.setStyle("-fx-alignment:top-center; -fx-font-style: italic");
    startDate.setCellValueFactory(new PropertyValueFactory<CropPhase, String>("cropStart"));
    duration.setMinWidth(125);
    duration.setStyle("-fx-alignment:top-center; -fx-font-weight: bold;");
    duration.setCellValueFactory(new PropertyValueFactory<CropPhase, String>("duration"));
    cropRootingZone.setMinWidth(200);
    cropRootingZone.setStyle("-fx-alignment:top-center; -fx-font-weight: bold;");
    cropRootingZone
      .setCellValueFactory(new PropertyValueFactory<CropPhase, String>("rootingZone"));

    // Sets each column not editable, except of 'duration' and 'cropRootingZone'
    cropTable.setEditable(true);
    phase.setEditable(false);
    description.setEditable(false);
    today.setEditable(false);
    startDate.setEditable(false);
    duration.setCellFactory(TextFieldTableCell.forTableColumn());
    // Sets the change for the right phase
    duration.setOnEditCommit(new EventHandler<CellEditEvent<CropPhase, String>>() {
      @Override
      public void handle(CellEditEvent<CropPhase, String> t) {
        CropDevelopmentStatus devPhase = plot.getCropDevelopmentStatus();
        ((CropPhase) t.getTableView().getItems().get(t.getTablePosition().getRow()))
        .setDuration(t.getNewValue());
        if (cropTable.getFocusModel().getFocusedIndex() == 0 && !t.getNewValue().isEmpty()) {
          devPhase.setPhase1(Integer.valueOf(t.getNewValue()));
        } else if (cropTable.getFocusModel().getFocusedIndex() == 1 && !t.getNewValue().isEmpty()) {
          devPhase.setPhase2(Integer.valueOf(t.getNewValue()));
        } else if (cropTable.getFocusModel().getFocusedIndex() == 2 && !t.getNewValue().isEmpty()) {
          devPhase.setPhase3(Integer.valueOf(t.getNewValue()));
        } else if (cropTable.getFocusModel().getFocusedIndex() == 3 && !t.getNewValue().isEmpty()) {
          devPhase.setPhase4(Integer.valueOf(t.getNewValue()));
        } else {
          ((CropPhase) t.getTableView().getItems().get(t.getTablePosition().getRow()))
          .setDuration(t.getOldValue());
        }
        setTableData();
      }
    });
    cropRootingZone.setCellFactory(TextFieldTableCell.forTableColumn());
    // Sets the change for the right rooting zone
    cropRootingZone.setOnEditCommit(new EventHandler<CellEditEvent<CropPhase, String>>() {
      @Override
      public void handle(CellEditEvent<CropPhase, String> t) {
        CropRootingZone devRoot = plot.getCropRootingZone();
        ((CropPhase) t.getTableView().getItems().get(t.getTablePosition().getRow()))
        .setRootingZone(t.getNewValue());
        if (cropTable.getFocusModel().getFocusedIndex() == 0) {
          devRoot.setRootingZone1(Integer.valueOf(t.getNewValue()));
        } else if (cropTable.getFocusModel().getFocusedIndex() == 1) {
          devRoot.setRootingZone2(Integer.valueOf(t.getNewValue()));
        } else if (cropTable.getFocusModel().getFocusedIndex() == 2) {
          devRoot.setRootingZone3(Integer.valueOf(t.getNewValue()));
        } else if (cropTable.getFocusModel().getFocusedIndex() == 3) {
          devRoot.setRootingZone4(Integer.valueOf(t.getNewValue()));
        }
      }
    });

    Pane tablePane = new Pane();
    cropTable.setMaxHeight(150);
    tablePane.setMaxHeight(150);
    tablePane.getChildren().add(cropTable);

    Button nextPhase = GsehenGuiElements.button(150);
    nextPhase.setText(mainBundle.getString("plotview.nextphase"));
    nextPhase.setOnAction(e -> setNextPhase());

    // Bewässerungsgrafik (Accordion)
    TitledPane wateringGraphicPane = new TitledPane();
    wateringGraphicPane.setText(mainBundle.getString("plotview.graphic"));

    precBarData = FXCollections.observableArrayList();
    irriBarData = FXCollections.observableArrayList();
    caswData = FXCollections.observableArrayList();
    twbData = FXCollections.observableArrayList();

    barLineChart = new CombinedBarAndLineChart();
    StackPane root = new StackPane();
    chartPanel = new SwingNode();
    root.getChildren().addAll(chartPanel);
    root.setPrefSize(800, 600);
    root.setPadding(new Insets(20, 20, 20, 20));

    ScrollPane wateringScrollPane = new ScrollPane();
    wateringScrollPane.setContent(root);
    wateringScrollPane.setPannable(true);

    wateringGraphicPane.setContent(wateringScrollPane);

    Accordion wateringGraphicAccordion = new Accordion();
    wateringGraphicAccordion.getPanes().add(wateringGraphicPane);

    StackPane wateringStackPane = new StackPane();
    wateringStackPane.getChildren().add(wateringGraphicAccordion);

    // Bewässerungsfaktor
    scalingFactor = new JFXSlider();
    scalingFactor.setMin(-100.0);
    scalingFactor.setMax(100.0);
    scalingFactor.setValue(0.0);
    scalingFactor.setShowTickLabels(true);
    scalingFactor.setShowTickMarks(true);
    scalingFactor.setMajorTickUnit(25.0);
    scalingFactor.setBlockIncrement(5.0);
    scalingFactor.valueProperty()
        .addListener((obs, oldVal, newVal) -> scalingFactor.setValue(newVal.intValue()));

    Text scalingValue = GsehenGuiElements.text("", FontWeight.BOLD);

    scalingFactor.valueProperty().addListener((obs, oldVal, newVal) -> {
      String factor = "";
      if (newVal.doubleValue() > 0) {
        factor = mainBundle.getString("plotview.wet");
      } else if (newVal.doubleValue() < 0) {
        factor = mainBundle.getString("plotview.dry");
      }
      scalingValue.setText(Math.abs(newVal.intValue()) + "% " + factor);
    });

    Text cropLabel = GsehenGuiElements.text(mainBundle.getString("plotview.crop"));
    Text scalingFactorLabel = GsehenGuiElements
        .text(mainBundle.getString("plotview.scalingfactor"));
    Text startTypeLabel = GsehenGuiElements.text(mainBundle.getString("plotview.starttype"));

    // Set Row & Column Index for Nodes
    GridPane.setConstraints(nameLabel, 0, 0);
    GridPane.setConstraints(name, 1, 0);
    GridPane.setConstraints(areaLabel, 0, 1);
    GridPane.setConstraints(area, 1, 1);
    GridPane.setConstraints(locationLabel, 0, 2);
    GridPane.setConstraints(location, 1, 2);
    GridPane.setConstraints(rootingZoneLabel, 0, 3);
    GridPane.setConstraints(rootingZone, 1, 3);
    GridPane.setConstraints(startTypeLabel, 0, 4);
    GridPane.setConstraints(startType, 1, 4);

    GridPane.setConstraints(cropLabel, 0, 8);
    GridPane.setConstraints(cropChoiceBox, 1, 8);
    GridPane.setConstraints(descriptionAccordion, 2, 8);
    GridPane.setConstraints(tablePane, 0, 9, 3, 1);
    GridPane.setConstraints(nextPhase, 0, 10);
    GridPane.setConstraints(wateringStackPane, 0, 12, 3, 1);
    GridPane.setConstraints(scalingFactorLabel, 0, 13);
    GridPane.setConstraints(scalingFactor, 1, 13, 2, 1);
    GridPane.setConstraints(scalingValue, 3, 13);

    // GridPane - Center Section
    GridPane centerGrid = GsehenGuiElements.gridPane(pane);
    centerGrid.getChildren().addAll(nameLabel, name, areaLabel, area, locationLabel, location,
        rootingZoneLabel, rootingZone, startTypeLabel, startType, cropLabel, cropChoiceBox,
        descriptionAccordion, tablePane, nextPhase, wateringStackPane, scalingFactorLabel,
        scalingFactor, scalingValue);

    startType.valueProperty().addListener((obs, oldVal, newVal) -> {
      if (startType.getValue().equals(mainBundle.getString("plotview.directstart"))) {
        GridPane.setConstraints(cropStartLabel, 0, 5);
        GridPane.setConstraints(cropStart, 1, 5);
        GridPane.setConstraints(soilStartValueLabel, 0, 6);
        GridPane.setConstraints(soilStartValue, 1, 6);
        centerGrid.getChildren().removeAll(cropStartLabel, cropStart, soilStartLabel, soilStart,
            soilStartValueLabel, soilStartValue);
        centerGrid.getChildren().addAll(cropStartLabel, cropStart, soilStartValueLabel,
            soilStartValue);
      } else {
        GridPane.setConstraints(soilStartLabel, 0, 5);
        GridPane.setConstraints(soilStart, 1, 5);
        GridPane.setConstraints(cropStartLabel, 0, 6);
        GridPane.setConstraints(cropStart, 1, 6);
        GridPane.setConstraints(soilStartValueLabel, 0, 7);
        GridPane.setConstraints(soilStartValue, 1, 7);
        centerGrid.getChildren().removeAll(cropStartLabel, cropStart, soilStartLabel, soilStart,
            soilStartValueLabel, soilStartValue);
        centerGrid.getChildren().addAll(soilStartLabel, soilStart, cropStartLabel, cropStart,
            soilStartValueLabel, soilStartValue);
      }
    });

    ScrollPane scrollPane = new ScrollPane();
    scrollPane.setContent(centerGrid);
    scrollPane.setPannable(true);

    pane.setCenter(scrollPane);
    // CENTER END ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    // Ernte
    harvest = GsehenGuiElements.button(100);
    harvest.setText(mainBundle.getString("plotview.harvest"));
    harvest.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent e) {
        isActive = false;
        plot.setCropEnd(DateUtil.truncToDay(new Date()));
        plot.setIsActive(isActive);
        gsehenInstance.sendFarmDataChanged(plot, null);
        gsehenInstance.saveUserData();
        tabPane.getSelectionModel().select(2);
        treeTableView.getSelectionModel().clearSelection();
        treeTableView.getSelectionModel().select(currentItem);
      }
    });

    // Plot manuell bewässern (creates a new view)
    watering = GsehenGuiElements.button(250);
    watering.setText(mainBundle.getString("plotview.watering"));
    watering.setOnAction(e -> wateringView());

    // Speichern
    save = GsehenGuiElements.button(200);
    save.setText(mainBundle.getString("button.accept"));
    save.setOnAction(e -> savePlot());
    bottomBox = new HBox();
    bottomBox.setPadding(new Insets(20, 20, 20, 20));
    bottomBox.setSpacing(10);
    bottomBox.getChildren().addAll(harvest, watering, save);

    pane.setBottom(bottomBox);

    tabPane = gsehenInstance.getMainController().getJfxTabPane();

    treeTableView = (TreeTableView<Drawable>) Gsehen.getInstance().getScene()
        .lookup(FARM_TREE_VIEW_ID);
    treeTableView.getSelectionModel().selectedItemProperty().addListener(
        (obs, oldVal, newVal) -> treeViewUpdate());
    currentItem = treeTableView.getSelectionModel().getSelectedIndex();
  }

  private void setNextPhase() {
    Date actualDate;
    Date nextDate;
    for (int i = 0; i < cropPhases.size() - 1; i++) {
      try {
        actualDate = new SimpleDateFormat("dd.MM.yyyy").parse(cropPhases.get(i).getCropStart());
        nextDate = new SimpleDateFormat("dd.MM.yyyy").parse(cropPhases.get(i + 1).getCropStart());

        if (todayDate.after(actualDate) && todayDate.before(nextDate)) {
          long days = todayDate.getTime() - actualDate.getTime();
          int duration = Integer.parseInt(cropPhases.get(i).getDuration().substring(0,
              cropPhases.get(i).getDuration().length() - 2));
          if (plot.getCrop().getPhase1() == duration) {
            plot.getCrop().setPhase1((int) TimeUnit.DAYS.convert(days, TimeUnit.MILLISECONDS));
          } else if (plot.getCrop().getPhase2() == duration) {
            plot.getCrop().setPhase2((int) TimeUnit.DAYS.convert(days, TimeUnit.MILLISECONDS));
          } else if (plot.getCrop().getPhase3() == duration) {
            plot.getCrop().setPhase3((int) TimeUnit.DAYS.convert(days, TimeUnit.MILLISECONDS));
          } else if (plot.getCrop().getPhase4() == duration) {
            plot.getCrop().setPhase4((int) TimeUnit.DAYS.convert(days, TimeUnit.MILLISECONDS));
          }
          gsehenInstance.sendFarmDataChanged(plot, null);
          tabPane.getSelectionModel().select(2);
          treeTableView.getSelectionModel().clearSelection();
          treeTableView.getSelectionModel().select(currentItem);
        }
      } catch (ParseException e) {
        e.printStackTrace();
      }
    }
  }

  private void setPrecBarChartData() {
    precBarDataList = createEventChartData(precBarData, mws -> mws.getPrecipitation());
  }

  private void setIrriBarChartData() {
    irriBarDataList = createEventChartData(irriBarData, mws -> mws.getIrrigation());
  }

  private void setCaswChartData() {
    caswDataList = createChartData(caswData, dayData -> dayData.getCurrentAvailableSoilWater());
  }

  private void setTwbChartData() {
    twbDataList = createChartData(twbData, dayData -> dayData.getCurrentTotalWaterBalance());
  }

  public void setupChartPanel() {
    chartPanel.setContent(
        barLineChart.scrollPane(precBarDataList, irriBarDataList, caswDataList, twbDataList));
  }

  private SortedList<Data<Date, Number>> createEventChartData(
      final ObservableList<Data<Date, Number>> dataList,
      final Function<ManualWaterSupply, Double> dataSource) {
    final SortedList<Data<Date, Number>> sortedList = new SortedList<Data<Date, Number>>(dataList,
        (data1, data2) -> data1.getXValue().compareTo(data2.getXValue()));
    dataList.clear();
    for (ManualWaterSupply manualWaterSupply : plot.getManualData().getManualWaterSupply()) {
      final Double data = dataSource.apply(manualWaterSupply);
      if (data != 0.0) {
        dataList.add(new XYChart.Data<Date, Number>(manualWaterSupply.getDate(), (Number)data));
      }
    }
    return sortedList;
  }

  private SortedList<Data<Date, Number>> createChartData(
      final ObservableList<Data<Date, Number>> dataList,
      final Function<DayData, Double> dataSource) {
    final SortedList<Data<Date, Number>> sortedList = new SortedList<Data<Date, Number>>(dataList,
        (data1, data2) -> data1.getXValue().compareTo(data2.getXValue()));
    dataList.clear();
    if (plot.getWaterBalance() != null && plot.getWaterBalance().getDailyBalances() != null) {
      for (DayData dayData : plot.getWaterBalance().getDailyBalances()) {
        dataList.add(new XYChart.Data<Date, Number>(dayData.getDate(),
            (Number)(dataSource.apply(dayData) * (-1))));
      }
    }
    return sortedList;
  }

  @SuppressWarnings("checkstyle:all")
  /**
   * Sets the data in the TableView.
   */
  private void setTableData() {
    cropTable.getItems().clear();
    cropPhases = new ArrayList<>();

    setPlotCropAndSoilStart();
    Date cropdate = plot.getCropStart();
    Date soildate = plot.getSoilStartDate();

    CropDevelopmentStatus devPhase = plot.getCropDevelopmentStatus();
    if (cropdate != null || soildate != null && devPhase != null) {
      todayDate = Date
          .from(java.time.LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());

      Date startDate = null;
      if (cropdate != null) {
        startDate = cropdate;
      }
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(startDate);

      List<String> bbchs = Arrays.asList(plot.getCrop().getBbch1(), plot.getCrop().getBbch2(),
          plot.getCrop().getBbch3(), plot.getCrop().getBbch4());
      CropRootingZone devRoot = plot.getCropRootingZone();
//      if (devRoot == null) {
//        devRoot = new CropRootingZone();
//        plot.setCropRootingZone(devRoot);
//      }
      List<Integer> rootingZones = Arrays.asList(devRoot.getRootingZone1(),
          devRoot.getRootingZone2(), devRoot.getRootingZone3(), devRoot.getRootingZone4());
//      if (devPhase == null) {
//        devPhase = new CropDevelopmentStatus();
//        plot.setCropDevelopmentStatus(devPhase);
//      }
      List<Integer> cropDurations = Arrays.asList(devPhase.getPhase1(), devPhase.getPhase2(),
          devPhase.getPhase3(), devPhase.getPhase4());

      int index = 0;
      for (Integer duration : cropDurations) {
        if (duration == null || duration == 0) {
          break;
        }
        final Integer currentPhaseDuration = cropDurations.get(index);
        final Date currentCalendarTime = calendar.getTime();
        calendar.add(Calendar.DAY_OF_YEAR, currentPhaseDuration);
        cropPhases
            .add(new CropPhase(index + 1, gsehenInstance.localizeCropText(bbchs.get(index)),
                DateUtil.between(todayDate, currentCalendarTime, calendar.getTime()) ? "\u25B6"
                    : "",
                gsehenInstance.formatDate(currentCalendarTime),
                gsehenInstance.formatDoubleOneDecimal(currentPhaseDuration),
                String.valueOf(rootingZones.get(index++))));
      }
      cropTable.getItems().addAll(FXCollections.observableArrayList(cropPhases));
    }
  }

  private void wateringView() {
    @SuppressWarnings("checkstyle:variabledeclarationusagedistance")
    ManualData displayManualData = plot.getManualData();

    // Datum (when the irrigation/precipitation should be booked)
    DatePicker date = GsehenGuiElements.datepicker();
    date.setValue(LocalDate.now());

    // mm/Liter
    JFXComboBox<String> unit = new JFXComboBox<>();
    unit.getItems().addAll("mm", "Liter");
    unit.getSelectionModel().select(0);

    // Bewässerung
    JFXTextField irrigation = new JFXTextField();
    irrigation.textProperty().addListener((obs, oldVal, newVal) -> {
      if (newVal != null) {
        if (!newVal.trim().isEmpty() && !gsehenInstance.isParseable(newVal)) {
          irrigation.setText(oldVal);
        }
      }
    });

    // Niederschlag
    JFXTextField precipitation = new JFXTextField();
    precipitation.textProperty().addListener((obs, oldVal, newVal) -> {
      if (newVal != null) {
        if (!newVal.trim().isEmpty() && !gsehenInstance.isParseable(newVal)) {
          precipitation.setText(oldVal);
        }
      }
    });

    irrigation.setText(gsehenInstance.formatDoubleTwoDecimal(0.0));
    precipitation.setText(gsehenInstance.formatDoubleOneDecimal(0.0));
    if (displayManualData != null) {
      Date compareWateringDate = Date
          .from(date.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
      for (ManualWaterSupply mws : displayManualData.getManualWaterSupply()) {
        if (compareWateringDate.equals(mws.getDate())) {
          irrigation
          .setText(String.valueOf(gsehenInstance.formatDoubleTwoDecimal(mws.getIrrigation())));
          precipitation.setText(
              String.valueOf(gsehenInstance.formatDoubleTwoDecimal(mws.getPrecipitation())));
        }
      }
    }

    unit.valueProperty().addListener((obs, oldVal, newVal) -> {
      if (displayManualData != null) {
        for (ManualWaterSupply mws : displayManualData.getManualWaterSupply()) {
          Date wateringDate = Date
              .from(date.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
          if ((wateringDate.compareTo(mws.getDate()) == 0)
              && unit.getSelectionModel().getSelectedItem().equals("Liter")) {
            irrigation
            .setText(gsehenInstance.formatDoubleTwoDecimal(mws.getIrrigation() * plot.getArea()));
            precipitation.setText(
                gsehenInstance.formatDoubleOneDecimal(mws.getPrecipitation() * plot.getArea()));
            break;
          } else if ((wateringDate.compareTo(mws.getDate()) == 0)
              && unit.getSelectionModel().getSelectedItem().equals("mm")) {
            irrigation.setText(gsehenInstance.formatDoubleTwoDecimal(mws.getIrrigation()));
            precipitation.setText(gsehenInstance.formatDoubleOneDecimal(mws.getPrecipitation()));
            break;
          }
        }
      }
    });

    date.valueProperty().addListener((obs, oldVal, newVal) -> {
      if (displayManualData != null) {
        // Books the irrigation/precipitation for the right day
        for (ManualWaterSupply mws : displayManualData.getManualWaterSupply()) {
          Date wateringDate = Date.from(newVal.atStartOfDay(ZoneId.systemDefault()).toInstant());
          if ((wateringDate.compareTo(mws.getDate()) == 0)
              && unit.getSelectionModel().getSelectedItem().equals("Liter")) {
            irrigation
            .setText(gsehenInstance.formatDoubleTwoDecimal(mws.getIrrigation() * plot.getArea()));
            precipitation.setText(
                gsehenInstance.formatDoubleOneDecimal(mws.getPrecipitation() * plot.getArea()));
            break;
          } else if ((wateringDate.compareTo(mws.getDate()) == 0)
              && unit.getSelectionModel().getSelectedItem().equals("mm")) {
            irrigation.setText(gsehenInstance.formatDoubleTwoDecimal(mws.getIrrigation()));
            precipitation.setText(gsehenInstance.formatDoubleOneDecimal(mws.getPrecipitation()));
            break;
          } else {
            irrigation.setText(gsehenInstance.formatDoubleTwoDecimal(0.0));
            precipitation.setText(gsehenInstance.formatDoubleOneDecimal(0.0));
          }
        }
      }
    });

    Text watering = GsehenGuiElements.text(mainBundle.getString("plotview.manual"),
        FontWeight.BOLD);
    Text dateLabel = GsehenGuiElements.text(mainBundle.getString("plotview.date"));
    Text unitLabel = GsehenGuiElements.text(mainBundle.getString("plotview.unit"));
    Text irrigationLabel = GsehenGuiElements.text(mainBundle.getString("plotview.irrigation"));
    Text precipitationLabel = GsehenGuiElements
        .text(mainBundle.getString("plotview.precipitation"));

    // Set Row & Column Index for Nodes
    GridPane.setConstraints(watering, 0, 0);
    GridPane.setConstraints(dateLabel, 0, 1);
    GridPane.setConstraints(date, 1, 1);
    GridPane.setConstraints(unitLabel, 0, 2);
    GridPane.setConstraints(unit, 1, 2);
    GridPane.setConstraints(irrigationLabel, 0, 3);
    GridPane.setConstraints(irrigation, 1, 3);
    GridPane.setConstraints(precipitationLabel, 0, 4);
    GridPane.setConstraints(precipitation, 1, 4);

    // GridPane - Center Section
    GridPane center = GsehenGuiElements.gridPane(pane);

    center.getChildren().addAll(watering, dateLabel, date, unitLabel, unit, irrigationLabel,
        irrigation, precipitationLabel, precipitation);

    JFXButton back = GsehenGuiElements.jfxButton(mainBundle.getString("plotview.hide"));
    back.setOnAction(new EventHandler<ActionEvent>() {

      @Override
      public void handle(ActionEvent arg0) {
        gsehenInstance.sendFarmDataChanged(plot, null);
        pane.setTop(null);
        tabPane.getSelectionModel().select(2);
        treeTableView.getSelectionModel().clearSelection();
        treeTableView.getSelectionModel().select(currentItem);
      }
    });

    // Bewässerung buchen
    JFXButton book = GsehenGuiElements.jfxButton(mainBundle.getString("plotview.book"));
    book.setOnAction(e -> {
      if (date.getValue() != null && !unit.getSelectionModel().isEmpty()
          && !irrigation.getText().isEmpty() && !precipitation.getText().isEmpty()) {
        LocalDate localDate = date.getValue();
        Date wateringDate = DateUtil
            .truncToDay(Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        String waterUnit = unit.getSelectionModel().getSelectedItem();

        updatePlotManualData(wateringDate,
            unit.getSelectionModel().getSelectedItem().equals("Liter"),
            irrigation, precipitation, waterUnit);

        gsehenInstance.sendFarmDataChanged(plot, null);
        gsehenInstance.sendManualDataChanged(field, plot, wateringDate, null);

        pane.setTop(null);
        tabPane.getSelectionModel().select(2);
        treeTableView.getSelectionModel().clearSelection();
        treeTableView.getSelectionModel().select(currentItem);
      } else {
        Text wateringError =
            GsehenGuiElements.text(mainBundle.getString("fieldview.form.values.missing"));
        wateringError.setFill(Color.RED);
        GridPane.setConstraints(wateringError, 0, 6);
        center.getChildren().add(wateringError);
      }
    });

    GridPane.setConstraints(back, 0, 5);
    GridPane.setConstraints(book, 1, 5);
    center.getChildren().addAll(back, book);

    VBox wateringVbox = new VBox();
    wateringVbox.getChildren().add(center);
    pane.setTop(wateringVbox);
  }

  private void updatePlotManualData(Date wateringDate, boolean isLiter, JFXTextField irrigation,
      JFXTextField precipitation, String waterUnit) {
    ManualData manualData = plot.getManualData();
    if (manualData == null) {
      plot.setManualData(new ManualData(Arrays.asList(
          parseSupply(wateringDate, isLiter, irrigation, precipitation, waterUnit)
      )));
    } else {
      boolean newDate = true;
      for (ManualWaterSupply mws : manualData.getManualWaterSupply()) {
        if (wateringDate.equals(mws.getDate())) {
          newDate = false;
          mws.setIrrigation(parseDouble(irrigation, isLiter));
          mws.setPrecipitation(parseDouble(precipitation, isLiter));
          break;
        }
      }
      if (newDate) {
        manualData.getManualWaterSupply()
            .add(parseSupply(wateringDate, isLiter, irrigation, precipitation, waterUnit));
      }
    }
  }

  private ManualWaterSupply parseSupply(Date wateringDate, boolean isLiter, JFXTextField irrigation,
      JFXTextField precipitation, String waterUnit) {
    return new ManualWaterSupply(wateringDate,
        parseDouble(irrigation, isLiter), parseDouble(precipitation, isLiter),
        waterUnit);
  }

  private double parseDouble(JFXTextField textField, boolean divideByPlotArea) {
    return parseDouble(textField) / (divideByPlotArea ? plot.getArea() : 1.0);
  }

  private double parseDouble(JFXTextField textField) {
    return gsehenInstance.parseDouble(textField.getText());
  }

  private void savePlot() {
    if (soilStart.getValue() != null && cropStart.getValue() != null
        || cropStart.getValue() != null) {
      if (!name.getText().isEmpty() && cropChoiceBox.getValue() != null) {
        try {
          plot.setName(name.getText());
          plot.setCrop(cropChoiceBox.getValue());

          setPlotCropAndSoilStart();

          if (!rootingZone.getText().equals("0") && !rootingZone.getText().isEmpty()) {
            plot.setRootingZone(Integer.valueOf(rootingZone.getText()));
          } else {
            plot.setRootingZone(null);
          }
          plot.setSoilStartValue(soilStartValue.getValue());
          plot.setIsActive(isActive);
          plot.setScalingFactor((scalingFactor.getValue() + 100) / 100);
        } finally {
          if (bottomBox.getChildren().contains(error)) {
            bottomBox.getChildren().remove(error);
          }
          gsehenInstance.sendFarmDataChanged(plot, null);
          tabPane.getSelectionModel().select(2);
          treeTableView.getSelectionModel().clearSelection();
          treeTableView.getSelectionModel().select(currentItem);
        }
      } else {
        Text plotError =
            GsehenGuiElements.text(mainBundle.getString("fieldview.form.values.missing"));
        plotError.setFill(Color.RED);
        bottomBox.getChildren().clear();
        bottomBox.getChildren().addAll(harvest, watering, save, plotError);
      }
    } else {
      error = GsehenGuiElements.text(mainBundle.getString("plotview.error"));
      error.setFill(Color.RED);
      bottomBox.getChildren().clear();
      bottomBox.getChildren().addAll(harvest, watering, save, error);
    }
  }

  private void setPlotCropAndSoilStart() {
    if (soilStart.getValue() != null && cropStart.getValue() != null) {
      LocalDate localDateSoil = soilStart.getValue();
      Date soilDate = Date
          .from(localDateSoil.atStartOfDay(ZoneId.systemDefault()).toInstant());
      plot.setSoilStartDate(soilDate);

      LocalDate localDateCrop = cropStart.getValue();
      Date cropDate = Date
          .from(localDateCrop.atStartOfDay(ZoneId.systemDefault()).toInstant());
      plot.setCropStart(cropDate);
    } else if (soilStart.getValue() == null && cropStart.getValue() != null) {
      LocalDate localDateCrop = cropStart.getValue();
      Date cropDate = Date
          .from(localDateCrop.atStartOfDay(ZoneId.systemDefault()).toInstant());
      plot.setCropStart(cropDate);
    }
  }

  private void treeViewUpdate() {
    bottomBox.getChildren().clear();
    bottomBox.getChildren().addAll(harvest, watering, save);
    cropStart.setValue(null);
    soilStart.setValue(null);
    for (int i = 0; i < treeTableView.getSelectionModel().getSelectedCells().size(); i++) {
      if (treeTableView.getSelectionModel().getSelectedCells().get(i) != null) {
        TreeItem<Drawable> selectedItem =
            treeTableView.getSelectionModel().getSelectedCells().get(i).getTreeItem();
        if (selectedItem != null
            && selectedItem.getValue().getClass().getSimpleName().equals("Plot")) {
          pane.setVisible(true);
          plot = (Plot) selectedItem.getValue();
          field = (Field) selectedItem.getParent().getValue();

          if (plot.getLocation() == null) {
            double lat = 0.0;
            double lng = 0.0;
            for (int y = 0; y < plot.getPolygon().getGeoPoints().size(); y++) {
              lat += plot.getPolygon().getGeoPoints().get(y).getLat();
              if (y == plot.getPolygon().getGeoPoints().size() - 1) {
                lat = lat / plot.getPolygon().getGeoPoints().size();
              }
            }
            for (int z = 0; z < plot.getPolygon().getGeoPoints().size(); z++) {
              lng += plot.getPolygon().getGeoPoints().get(z).getLng();
              if (z == plot.getPolygon().getGeoPoints().size() - 1) {
                lng = lng / plot.getPolygon().getGeoPoints().size();
              }
            }
            plot.setLocation(new GeoPoint(lat, lng));
          }

          name.setText(plot.getName());

          area.setText(gsehenInstance.formatDoubleOneDecimal(
              plot.getPolygon().calculateArea(plot.getPolygon().getGeoPoints())));

          if (plot.getLocation() != null) {
            location.setText(String.valueOf(
                plot.getLocation().getLat() + " (" + mainBundle.getString("plotview.lat") + ")\n")
                + String.valueOf(plot.getLocation().getLng() + " ("
                    + mainBundle.getString("plotview.lng") + ")"));
            location.setOnAction(new EventHandler<ActionEvent>() {
              @SuppressWarnings("static-access")
              @Override
              public void handle(ActionEvent event) {
                gsehenInstance.getInstance().getHostServices()
                    .showDocument("https://www.google.de/maps?ll=" + plot.getLocation().getLat()
                        + "," + plot.getLocation().getLng());
              }
            });
          }

          if (plot.getScalingFactor() != null) {
            scalingFactor.setValue((plot.getScalingFactor() * 100) - 100);
          }

          if (plot.getCrop() != null && cropList.size() != 0) {
            cropChoiceBox.getSelectionModel().select(plot.getCrop());
          }

          Date date = plot.getSoilStartDate();
          Date cropdate = plot.getCropStart();

          if (date != null && cropdate != null) {
            LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            soilStart.setValue(localDate);
            LocalDate cropDate = cropdate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            cropStart.setValue(cropDate);
            startType.getSelectionModel().select(mainBundle.getString("plotview.delayedstart"));
          } else if (date == null && cropdate != null) {
            LocalDate cropDate = cropdate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            cropStart.setValue(cropDate);
            startType.getSelectionModel().select(mainBundle.getString("plotview.directstart"));
          }

          if (plot.getRootingZone() != null) {
            rootingZone.setText(String.valueOf(plot.getRootingZone()));
          } else {
            rootingZone.setText(String.valueOf(0));
          }

          if (plot.getSoilStartValue() == null) {
            soilStartValue.setValue(100.0);
          } else {
            soilStartValue.setValue(plot.getSoilStartValue());
          }

          setTableData();

          if (plot.getManualData() != null) {
            setPrecBarChartData();
            setIrriBarChartData();
            setCaswChartData();
            setTwbChartData();
            setupChartPanel();
          }
        } else {
          pane.setVisible(false);
        }
      }
    }
  }

  public BorderPane getPane() {
    return pane;
  }

  public void setPane(BorderPane pane) {
    this.pane = pane;
  }

  public static PlotDataController getInstance() {
    return instance;
  }
}
