package de.hgu.gsehen.gui.view;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXSlider;
import com.jfoenix.controls.JFXTabPane;
import com.jfoenix.controls.JFXTextField;
import com.sun.javafx.charts.Legend;
import de.hgu.gsehen.Gsehen;
import de.hgu.gsehen.evapotranspiration.DayData;
import de.hgu.gsehen.event.FarmDataChanged;
import de.hgu.gsehen.event.GsehenEventListener;
import de.hgu.gsehen.event.RecommendedActionChanged;
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
import java.text.DateFormat;
import java.text.DecimalFormat;
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

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
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
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class PlotDataController implements GsehenEventListener<FarmDataChanged> {
  private static final String FARM_TREE_VIEW_ID = "#farmTreeView";
  protected final ResourceBundle mainBundle;
  private GsehenGuiElements gsehenGuiElements;

  private List<Crop> cropList = new ArrayList<>();
  private CropDevelopmentStatus devPhase;
  private CropRootingZone devRoot;

  private TreeItem<Drawable> selectedItem;
  private int currentItem;
  private Field field;
  private Plot plot;

  private Gsehen gsehenInstance;
  private BorderPane pane;
  private TreeTableView<Drawable> treeTableView;
  private JFXTabPane tabPane;
  private TableView<CropPhase> cropTable;
  private List<CropPhase> cropPhases;
  private Date todayDate;
  private JFXComboBox<String> startType;
  private ManualData md;

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
  private Double waterLevel;
  private Double availableSoilWater;
  @SuppressWarnings("rawtypes")
  private XYChart.Series series;
  private BarChart<String, Number> chart;
  private NumberAxis axisY;
  private String pattern;
  private Double lat = 0.0;
  private Double lng = 0.0;

  {
    gsehenInstance = Gsehen.getInstance();
    cropList = gsehenInstance.getCrops();
    gsehenGuiElements = new GsehenGuiElements();

    DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT,
        gsehenInstance.getSelectedLocale());
    pattern = ((SimpleDateFormat) dateFormat).toPattern();

    gsehenInstance.registerForEvent(FarmDataChanged.class, this);

    gsehenInstance.registerForEvent(RecommendedActionChanged.class, event -> setChartData());

    mainBundle = ResourceBundle.getBundle("i18n.main", gsehenInstance.getSelectedLocale());
  }

  /**
   * Constructs a new plot data controller associated with the given BorderPane.
   *
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
    nameLabel = gsehenGuiElements.text(mainBundle.getString("fieldview.name"));
    name = new JFXTextField("");

    // m²
    areaLabel = gsehenGuiElements.text(mainBundle.getString("fieldview.area"));
    area = gsehenGuiElements.text("");

    // Lage
    locationLabel = gsehenGuiElements.text(mainBundle.getString("plotview.location"));
    location = new Hyperlink("");
    location.setFont(Font.font("Arial", 14));

    // Max. durchw. Zone
    rootingZoneLabel = gsehenGuiElements.text(mainBundle.getString("plotview.rootingzone"));
    rootingZone = new JFXTextField("");
    rootingZone.textProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observable, String oldValue,
          String newValue) {
        if (newValue != null) {
          if (!newValue.trim().isEmpty() && !gsehenInstance.isParseable(newValue)) {
            rootingZone.setText(oldValue);
          }
        }
      }
    });

    startType = new JFXComboBox<String>();
    startType.getItems().addAll(mainBundle.getString("plotview.directstart"),
        mainBundle.getString("plotview.delayedstart"));
    startType.setPrefSize(200, 25);

    // Start der Inkulturnahme
    cropStartLabel = gsehenGuiElements.text(mainBundle.getString("plotview.cropstart"));
    cropStart = gsehenGuiElements.datepicker();

    // Start der Bodenwasserbilanz
    soilStartLabel = gsehenGuiElements.text(mainBundle.getString("plotview.soilstart"));
    soilStart = gsehenGuiElements.datepicker();

    // Startwert der Wasserbilanz
    soilStartValueLabel = gsehenGuiElements.text(mainBundle.getString("plotview.soilstartvalue"));
    soilStartValue = new JFXSlider();
    soilStartValue.setMin(0.0);
    soilStartValue.setMax(100.0);
    soilStartValue.setValue(100.0);
    soilStartValue.setShowTickLabels(true);
    soilStartValue.setShowTickMarks(true);
    soilStartValue.setMajorTickUnit(25.0);
    soilStartValue.setBlockIncrement(5.0);
    soilStartValue.valueProperty()
        .addListener((obs, oldval, newVal) -> soilStartValue.setValue(newVal.intValue()));

    // Kultur
    cropChoiceBox = new JFXComboBox<Crop>();
    cropChoiceBox.setPrefSize(200, 25);
    if (!cropList.isEmpty()) {
      for (Crop c : cropList) {
        cropChoiceBox.getItems().add(c);
      }
    } else {
      cropChoiceBox.getItems().addAll(new Crop("Liste leer!", true, 0.0, 0.0, 0.0, 0.0, 0, 0, 0, 0,
          "", "", "", "", 0, 0, 0, 0, ""));
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
    Text descriptionText = gsehenGuiElements.text("");
    descriptionText.wrappingWidthProperty().bind(pane.widthProperty().divide(3.5));
    descriptionBox.setPadding(new Insets(5, 5, 5, 5));
    descriptionBox.getChildren().add(descriptionText);
    descriptionPane.setContent(descriptionBox);
    Accordion descriptionAccordion = new Accordion();
    descriptionAccordion.getPanes().add(descriptionPane);

    // devPhase is a helper-object where you can set the duration of each crop phase
    devPhase = new CropDevelopmentStatus();
    // devRoot is a helper-object where you can set the rooting-zone of each crop
    devRoot = new CropRootingZone();

    ChangeListener<Crop> changeListener = new ChangeListener<Crop>() {
      @Override
      public void changed(ObservableValue<? extends Crop> observable, //
          Crop oldValue, Crop newValue) {
        if (oldValue != newValue) {
          // A tooltip, that shows the current crop description
          Tooltip tooltip = new Tooltip(
              gsehenInstance.localizeCropText(cropChoiceBox.getValue().getDescription()));
          tooltip.setStyle("-fx-font-style: italic; -fx-background-color: #ffffff; "
              + "-fx-text-fill: #000000; -fx-font-size: 9pt;");
          cropChoiceBox.setTooltip(tooltip);

          // Sets the accordion-content
          descriptionText
              .setText(gsehenInstance.localizeCropText(cropChoiceBox.getValue().getDescription()));

          plot.setCrop(newValue);

          devPhase.setPhase1(plot.getCrop().getPhase1());
          devPhase.setPhase2(plot.getCrop().getPhase2());
          devPhase.setPhase3(plot.getCrop().getPhase3());
          devPhase.setPhase4(plot.getCrop().getPhase4());
          plot.setCropDevelopmentStatus(devPhase);

          devRoot.setRootingZone1(plot.getCrop().getRootingZone1());
          devRoot.setRootingZone2(plot.getCrop().getRootingZone2());
          devRoot.setRootingZone3(plot.getCrop().getRootingZone3());
          devRoot.setRootingZone4(plot.getCrop().getRootingZone4());
          plot.setCropRootingZone(devRoot);
          setTableData();
        }
      }
    };
    cropChoiceBox.getSelectionModel().selectedItemProperty().addListener(changeListener);

    // TableView where you can set the duration of each crop phase and the rooting-zone of each crop
    cropTable = new TableView();
    cropTable.setEditable(true);

    // Each TableCollumn
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
    duration.setCellValueFactory(new PropertyValueFactory<CropPhase, Integer>("duration"));
    cropRootingZone.setMinWidth(200);
    cropRootingZone.setStyle("-fx-alignment:top-center;  -fx-font-weight: bold;");
    cropRootingZone
        .setCellValueFactory(new PropertyValueFactory<CropPhase, Integer>("rootingZone"));

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

    Button nextPhase = gsehenGuiElements.button(150);
    nextPhase.setText(mainBundle.getString("plotview.nextphase"));
    nextPhase.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent e) {
        setNextPhase();
      }
    });

    // Balkendiagramm
    waterLevel = 0.0;
    CategoryAxis axisX = new CategoryAxis();
    axisY = new NumberAxis(0, 20, 1);
    chart = new BarChart<String, Number>(axisX, axisY);
    chart.setPrefWidth(30);
    chart.setTitle(mainBundle.getString("plotview.waterinsoil"));
    chart.setLegendSide(Side.RIGHT);
    axisY.setLabel("mm");

    series = new XYChart.Series();
    chart.getData().addAll(series);

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
        .addListener((obs, oldval, newVal) -> scalingFactor.setValue(newVal.intValue()));

    Text scalingValue = gsehenGuiElements.text("", FontWeight.BOLD);

    scalingFactor.valueProperty().addListener(new ChangeListener<Number>() {
      public void changed(ObservableValue<? extends Number> ov, Number oldVal, Number newVal) {
        String factor = "";
        if (newVal.doubleValue() > 0) {
          factor = mainBundle.getString("plotview.wet");
        } else if (newVal.doubleValue() < 0) {
          factor = mainBundle.getString("plotview.dry");
        }
        scalingValue.setText(Math.abs(newVal.intValue()) + "% " + factor);
      }
    });

    Text cropLabel = gsehenGuiElements.text(mainBundle.getString("plotview.crop"));
    Text scalingFactorLabel = gsehenGuiElements
        .text(mainBundle.getString("plotview.scalingfactor"));
    Text startTypeLabel = gsehenGuiElements.text(mainBundle.getString("plotview.starttype"));

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

    GridPane.setConstraints(cropLabel, 0, 7);
    GridPane.setConstraints(cropChoiceBox, 1, 7);
    GridPane.setConstraints(descriptionAccordion, 2, 7);
    GridPane.setConstraints(tablePane, 0, 8, 3, 1);
    GridPane.setConstraints(nextPhase, 0, 9);
    GridPane.setConstraints(chart, 0, 10, 3, 1);
    GridPane.setConstraints(scalingFactorLabel, 0, 11);
    GridPane.setConstraints(scalingFactor, 1, 11, 2, 1);
    GridPane.setConstraints(scalingValue, 3, 11);

    // GridPane - Center Section
    GridPane centerGrid = gsehenGuiElements.gridPane(pane);
    centerGrid.getChildren().addAll(nameLabel, name, areaLabel, area, locationLabel, location,
        rootingZoneLabel, rootingZone, startTypeLabel, startType, cropLabel, cropChoiceBox,
        descriptionAccordion, tablePane, nextPhase, chart, scalingFactorLabel, scalingFactor,
        scalingValue);

    startType.valueProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue ov, String t, String t1) {
        if (startType.getValue().equals(mainBundle.getString("plotview.directstart"))) {
          GridPane.setConstraints(cropStartLabel, 0, 5);
          GridPane.setConstraints(cropStart, 1, 5);
          centerGrid.getChildren().removeAll(soilStartLabel, soilStart, soilStartValueLabel,
              soilStartValue);
          centerGrid.getChildren().addAll(cropStartLabel, cropStart);
        } else {
          GridPane.setConstraints(soilStartLabel, 0, 5);
          GridPane.setConstraints(soilStart, 1, 5);
          GridPane.setConstraints(soilStartValueLabel, 0, 6);
          GridPane.setConstraints(soilStartValue, 1, 6);
          centerGrid.getChildren().removeAll(cropStartLabel, cropStart);
          centerGrid.getChildren().addAll(soilStartLabel, soilStart, soilStartValueLabel,
              soilStartValue);
        }
      }
    });

    ScrollPane scrollPane = new ScrollPane();
    scrollPane.setContent(centerGrid);
    scrollPane.setPannable(true);

    pane.setCenter(scrollPane);
    // CENTER END ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    // Ernte
    harvest = gsehenGuiElements.button(100);
    harvest.setText(mainBundle.getString("plotview.harvest"));
    harvest.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent e) {
        isActive = false;
        Date date = Calendar.getInstance().getTime();
        DateFormat formatter = new SimpleDateFormat(pattern);
        String enddate = formatter.format(date);
        Date cropEnd;
        try {
          cropEnd = formatter.parse(enddate);
          plot.setCropEnd(cropEnd);
          plot.setIsActive(isActive);
          gsehenInstance.sendFarmDataChanged(plot, null);
          gsehenInstance.saveUserData();
          tabPane.getSelectionModel().select(2);
          treeTableView.getSelectionModel().clearSelection();
          treeTableView.getSelectionModel().select(currentItem);
        } catch (ParseException e1) {
          e1.printStackTrace();
        }
      }
    });

    // Plot manuell bewässern (creates a new view)
    watering = gsehenGuiElements.button(250);
    watering.setText(mainBundle.getString("plotview.watering"));
    watering.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent e) {
        wateringView();
      }
    });

    // Speichern
    save = gsehenGuiElements.button(200);
    save.setText(mainBundle.getString("button.accept"));
    save.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent e) {
        savePlot();
      }
    });
    bottomBox = new HBox();
    bottomBox.setPadding(new Insets(20, 20, 20, 20));
    bottomBox.setSpacing(10);
    bottomBox.getChildren().addAll(harvest, watering, save);

    pane.setBottom(bottomBox);

    tabPane = gsehenInstance.getMainController().getJfxTabPane();

    // Actions that will happen, if you click a 'plot' in the TreeTableView
    treeTableView = (TreeTableView<Drawable>) Gsehen.getInstance().getScene()
        .lookup(FARM_TREE_VIEW_ID);
    treeTableView.getSelectionModel().selectedItemProperty()
        .addListener(new ChangeListener<Object>() {
          @Override
          public void changed(ObservableValue<?> observable, Object oldVal, Object newVal) {
            treeViewUpdate();
          }
        });
    currentItem = treeTableView.getSelectionModel().getSelectedIndex();
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  private void setChartData() {
    waterLevel = plot.getRecommendedAction().getAvailableWater();
    int daysToIrrigation = plot.getRecommendedAction().getProjectedDaysToIrrigation();

    if (!plot.getWaterBalance().equals(null)) {
      final List<DayData> dailyBalances = plot.getWaterBalance().getDailyBalances();
      if (!dailyBalances.isEmpty()) {
        int waterBalance = dailyBalances.size() - 1;
        availableSoilWater = dailyBalances.get(waterBalance).getCurrentAvailableSoilWater() * 1.1;
        axisY.setUpperBound(availableSoilWater);
      }
    }

    Legend legend = (Legend) chart.lookup(".chart-legend");
    Legend.LegendItem li = null;
    DecimalFormat df = new DecimalFormat("#.##");

    if (waterLevel != null) {
      XYChart.Data data = new XYChart.Data("waterLevel", waterLevel);
      series.getData().add(data);
      Node node = data.getNode();

      if (daysToIrrigation == 0) {
        node.setStyle("-fx-bar-fill: #ff0000;");
        li = new Legend.LegendItem(df.format(waterLevel) + " mm", new Rectangle(10, 4, Color.RED));
      } else if (daysToIrrigation == 1) {
        node.setStyle("-fx-bar-fill: #800080;");
        li = new Legend.LegendItem(df.format(waterLevel) + " mm",
            new Rectangle(10, 4, Color.PURPLE));
      } else {
        node.setStyle("-fx-bar-fill: #0000ff;");
        li = new Legend.LegendItem(df.format(waterLevel) + " mm", new Rectangle(10, 4, Color.BLUE));
      }

      legend.getItems().setAll(li);
    }
  }

  private void setNextPhase() {
    Date actualDate;
    Date nextDate;
    // for (CropPhase phase : cropPhases) {
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

  @SuppressWarnings("checkstyle:all")
  /**
   * Sets the data in the TableView.
   */
  private void setTableData() {
    cropTable.getItems().clear();

    Date cropdate = plot.getCropStart();
    Date soildate = plot.getSoilStartDate();

    if (cropdate != null || soildate != null && plot.getCropDevelopmentStatus() != null) {
      todayDate = Date
          .from(java.time.LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());

      Date startDate = null;
      if (cropdate != null) {
        startDate = cropdate;
      } else if (soildate != null) {
        startDate = soildate;
      }
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(startDate);

      List<Integer> durations = Arrays.asList(plot.getCrop().getPhase1(),
          plot.getCrop().getPhase2(), plot.getCrop().getPhase3(), plot.getCrop().getPhase4());
      List<String> bbchs = Arrays.asList(plot.getCrop().getBbch1(), plot.getCrop().getBbch2(),
          plot.getCrop().getBbch3(), plot.getCrop().getBbch4());
      List<Integer> rootingZones = Arrays.asList(devRoot.getRootingZone1(),
          devRoot.getRootingZone2(), devRoot.getRootingZone3(), devRoot.getRootingZone4());
      List<Integer> devCropDurations = Arrays.asList(devPhase.getPhase1(), devPhase.getPhase2(),
          devPhase.getPhase3(), devPhase.getPhase4());

      cropPhases = new ArrayList<>();
      int index = 0;
      for (Integer duration : durations) {
        if (duration == null || duration == 0) {
          break;
        }
        final Integer currentPhaseDuration = devCropDurations.get(index);
        final Date currentCalendarTime = calendar.getTime();
        calendar.add(Calendar.DAY_OF_YEAR, currentPhaseDuration);
        cropPhases
            .add(new CropPhase(index + 1, gsehenInstance.localizeCropText(bbchs.get(index)),
                DateUtil.between(todayDate, currentCalendarTime, calendar.getTime()) ? "\u25B6"
                    : "",
                gsehenInstance.formatDate(currentCalendarTime),
                gsehenInstance.formatDoubleOneDecimal(currentPhaseDuration),
                gsehenInstance.formatDoubleOneDecimal(rootingZones.get(index++))));
      }
      cropTable.getItems().addAll(FXCollections.observableArrayList(cropPhases));
    }
  }

  private void wateringView() {
    md = new ManualData();

    if (plot.getManualData() != null) {
      md = plot.getManualData();
    }

    JFXDialogLayout content = new JFXDialogLayout();
    content.setHeading(new Text(mainBundle.getString("plotview.manual")));
    StackPane stackPane = new StackPane();
    Scene scene = new Scene(stackPane, 300, 250);
    Stage stage = (Stage) gsehenInstance.getScene().getWindow();
    JFXDialog dialog = new JFXDialog(stackPane, content, JFXDialog.DialogTransition.CENTER);
    stage.setScene(scene);
    dialog.show();

    // Name of the plot
    Text nameLabel = gsehenGuiElements.text("", FontWeight.BOLD);

    pane.setTop(nameLabel);

    // Datum (when the irrigation/precipitation should be booked)
    DatePicker date = gsehenGuiElements.datepicker();
    date.setValue(LocalDate.now());

    // mm/Liter
    JFXComboBox<String> unit = new JFXComboBox<>();
    unit.getItems().addAll("mm", "Liter");
    unit.getSelectionModel().select(0);

    // Bewässerung
    JFXTextField irrigation = new JFXTextField();
    irrigation.textProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observable, String oldValue,
          String newValue) {
        if (newValue != null) {
          if (!newValue.trim().isEmpty() && !gsehenInstance.isParseable(newValue)) {
            irrigation.setText(oldValue);
          }
        }
      }
    });

    // Niederschlag
    JFXTextField precipitation = new JFXTextField();
    precipitation.textProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observable, String oldValue,
          String newValue) {
        if (newValue != null) {
          if (!newValue.trim().isEmpty() && !gsehenInstance.isParseable(newValue)) {
            precipitation.setText(oldValue);
          }
        }
      }
    });

    // Books the irrigation/precipitation for the right day
    for (ManualWaterSupply mws : md.getManualWaterSupply()) {
      Date wateringDate = Date
          .from(date.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
      if ((wateringDate.compareTo(mws.getDate()) == 0)) {
        irrigation.setText(gsehenInstance.formatDoubleTwoDecimal(mws.getIrrigation()));
        precipitation.setText(gsehenInstance.formatDoubleOneDecimal(mws.getPrecipitation()));
      } else {
        irrigation.setText(gsehenInstance.formatDoubleTwoDecimal(0.0));
        precipitation.setText(gsehenInstance.formatDoubleOneDecimal(0.0));
      }
    }

    date.valueProperty().addListener((ov, oldValue, newValue) -> {
      boolean newData = true;

      // Books the irrigation/precipitation for the right day
      for (ManualWaterSupply mws : md.getManualWaterSupply()) {
        Date wateringDate = Date.from(newValue.atStartOfDay(ZoneId.systemDefault()).toInstant());
        if ((wateringDate.compareTo(mws.getDate()) == 0)) {
          irrigation.setText(gsehenInstance.formatDoubleTwoDecimal(mws.getIrrigation()));
          precipitation.setText(gsehenInstance.formatDoubleOneDecimal(mws.getPrecipitation()));
          newData = false;
        } else if (newData) {
          irrigation.setText(gsehenInstance.formatDoubleTwoDecimal(0.0));
          precipitation.setText(gsehenInstance.formatDoubleOneDecimal(0.0));
        }
      }
    });

    Text dateLabel = gsehenGuiElements.text(mainBundle.getString("plotview.date"));
    Text unitLabel = gsehenGuiElements.text(mainBundle.getString("plotview.unit"));
    Text irrigationLabel = gsehenGuiElements.text(mainBundle.getString("plotview.irrigation"));
    Text precipitationLabel = gsehenGuiElements
        .text(mainBundle.getString("plotview.precipitation"));

    // Set Row & Column Index for Nodes
    GridPane.setConstraints(dateLabel, 0, 0);
    GridPane.setConstraints(date, 1, 0);
    GridPane.setConstraints(unitLabel, 0, 1);
    GridPane.setConstraints(unit, 1, 1);
    GridPane.setConstraints(irrigationLabel, 0, 2);
    GridPane.setConstraints(irrigation, 1, 2);
    GridPane.setConstraints(precipitationLabel, 0, 3);
    GridPane.setConstraints(precipitation, 1, 3);

    // GridPane - Center Section
    GridPane center = gsehenGuiElements.gridPane(pane);

    center.getChildren().addAll(dateLabel, date, unitLabel, unit, irrigationLabel, irrigation,
        precipitationLabel, precipitation);

    JFXButton back = gsehenGuiElements.jfxButton(mainBundle.getString("fieldview.back"));
    back.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent arg0) {
        gsehenInstance.sendFarmDataChanged(plot, null);
        dialog.close();
        stackPane.setVisible(false);
        stage.setScene(gsehenInstance.getScene());
        tabPane.getSelectionModel().select(2);
        treeTableView.getSelectionModel().clearSelection();
        treeTableView.getSelectionModel().select(currentItem);
      }
    });

    // Bewässerung buchen
    JFXButton book = gsehenGuiElements.jfxButton(mainBundle.getString("plotview.book"));
    book.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent arg0) {
        if (date.getValue() != null && !unit.getSelectionModel().isEmpty()
            && !irrigation.getText().isEmpty() && !precipitation.getText().isEmpty()) {
          LocalDate localDate = date.getValue();
          Date wateringDate = DateUtil
              .truncToDay(Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));

          if (plot.getManualData() == null) {
            ManualData manualData = new ManualData();
            List<ManualWaterSupply> mwsList = new ArrayList<ManualWaterSupply>();
            if (unit.getSelectionModel().getSelectedItem().equals("Liter")) {
              double irri = parseDouble(irrigation) / plot.getArea();
              double prec = parseDouble(precipitation) / plot.getArea();
              ManualWaterSupply mws = new ManualWaterSupply(wateringDate, irri, prec);
              mwsList.add(mws);
            } else {
              mwsList.add(parseSupply(wateringDate, irrigation, precipitation));
            }
            manualData.setManualWaterSupply(mwsList);
            plot.setManualData(manualData);
          } else {
            ManualData manualData = plot.getManualData();
            boolean newDate = true;
            for (ManualWaterSupply mws : manualData.getManualWaterSupply()) {
              if (wateringDate.equals(mws.getDate())) {
                newDate = false;
                if (unit.getSelectionModel().getSelectedItem().equals("Liter")) {
                  mws.setIrrigation(parseDouble(irrigation) / plot.getArea());
                  mws.setPrecipitation(parseDouble(precipitation) / plot.getArea());
                } else {
                  mws.setIrrigation(parseDouble(irrigation));
                  mws.setPrecipitation(parseDouble(precipitation));
                }
                break;
              }
            }
            if (newDate) {
              manualData.getManualWaterSupply()
                  .add(parseSupply(wateringDate, irrigation, precipitation));
            }
          }

          gsehenInstance.sendFarmDataChanged(plot, null);
          gsehenInstance.sendManualDataChanged(field, plot, wateringDate, null);
          dialog.close();
          stackPane.setVisible(false);
          stage.setScene(gsehenInstance.getScene());
          tabPane.getSelectionModel().select(2);
          treeTableView.getSelectionModel().clearSelection();
          treeTableView.getSelectionModel().select(currentItem);
        } else {
          Text wateringError = gsehenGuiElements.text(mainBundle.getString("fieldview.error"));
          wateringError.setFill(Color.RED);
          GridPane.setConstraints(wateringError, 0, 5);
          center.getChildren().add(wateringError);
        }
      }

      private ManualWaterSupply parseSupply(Date wateringDate, JFXTextField irrigation,
          JFXTextField precipitation) {
        return new ManualWaterSupply(wateringDate, parseDouble(irrigation),
            parseDouble(precipitation));
      }

      private Double parseDouble(JFXTextField textField) {
        return gsehenInstance.parseDouble(textField.getText());
      }
    });

    GridPane.setConstraints(back, 0, 4);
    GridPane.setConstraints(book, 1, 4);
    center.getChildren().addAll(back, book);
    content.setBody(center);
  }

  private void savePlot() {
    if (cropStart.getValue() != null || soilStart.getValue() != null) {
      if (!name.getText().isEmpty() && cropChoiceBox.getValue() != null) {
        try {
          plot.setName(name.getText());
          plot.setCrop(cropChoiceBox.getValue());

          if (cropStart.getValue() != null) {
            LocalDate localDateCrop = cropStart.getValue();
            Date cropDate = Date
                .from(localDateCrop.atStartOfDay(ZoneId.systemDefault()).toInstant());
            plot.setCropStart(cropDate);
          }
          if (soilStart.getValue() != null) {
            LocalDate localDateSoil = soilStart.getValue();
            Date soilDate = Date
                .from(localDateSoil.atStartOfDay(ZoneId.systemDefault()).toInstant());
            plot.setSoilStartDate(soilDate);
          }
          if (!rootingZone.getText().equals("0") && !rootingZone.getText().isEmpty()) {
            plot.setRootingZone(Integer.valueOf(rootingZone.getText()));
          } else {
            plot.setRootingZone(null);
          }
          if (devPhase.getPhase1() != null) {
            plot.getCrop().setPhase1(devPhase.getPhase1());
          }
          if (devPhase.getPhase2() != null) {
            plot.getCrop().setPhase2(devPhase.getPhase2());
          }
          if (devPhase.getPhase3() != null) {
            plot.getCrop().setPhase3(devPhase.getPhase3());
          }
          if (devPhase.getPhase4() != null) {
            plot.getCrop().setPhase4(devPhase.getPhase4());
          }
          if (devRoot.getRootingZone1() != null) {
            plot.getCrop().setRootingZone1(devRoot.getRootingZone1());
          }
          if (devRoot.getRootingZone2() != null) {
            plot.getCrop().setRootingZone2(devRoot.getRootingZone2());
          }
          if (devRoot.getRootingZone3() != null) {
            plot.getCrop().setRootingZone3(devRoot.getRootingZone3());
          }
          if (devRoot.getRootingZone4() != null) {
            plot.getCrop().setRootingZone4(devRoot.getRootingZone4());
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
        Text plotError = gsehenGuiElements.text(mainBundle.getString("fieldview.error"));
        plotError.setFill(Color.RED);
        bottomBox.getChildren().clear();
        bottomBox.getChildren().addAll(harvest, watering, save, plotError);
      }
    } else {
      error = gsehenGuiElements.text(mainBundle.getString("plotview.error"));
      error.setFill(Color.RED);
      bottomBox.getChildren().clear();
      bottomBox.getChildren().addAll(harvest, watering, save, error);
    }
  }

  private void treeViewUpdate() {
    bottomBox.getChildren().clear();
    bottomBox.getChildren().addAll(harvest, watering, save);
    for (int i = 0; i < treeTableView.getSelectionModel().getSelectedCells().size(); i++) {
      if (treeTableView.getSelectionModel().getSelectedCells().get(i) != null) {
        selectedItem = treeTableView.getSelectionModel().getSelectedCells().get(i).getTreeItem();
        if (selectedItem != null
            && selectedItem.getValue().getClass().getSimpleName().equals("Plot")) {
          pane.setVisible(true);
          plot = (Plot) selectedItem.getValue();
          field = (Field) selectedItem.getParent().getValue();

          if (plot.getLocation() == null) {
            DecimalFormat df = new DecimalFormat("#.######");
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
            GeoPoint location = new GeoPoint(gsehenInstance.parseDouble(df.format(lat)),
                gsehenInstance.parseDouble(df.format(lng)));
            plot.setLocation(location);
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

          if (date != null) {
            LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            soilStart.setValue(localDate);
            startType.getSelectionModel().select(mainBundle.getString("plotview.delayedstart"));
          } else {
            soilStart.setValue(null);
          }

          if (cropdate != null) {
            LocalDate cropDate = cropdate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            cropStart.setValue(cropDate);
            startType.getSelectionModel().select(mainBundle.getString("plotview.directstart"));
          } else {
            cropStart.setValue(null);
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

          if (plot.getCropDevelopmentStatus() != null) {
            devPhase = plot.getCropDevelopmentStatus();
          }

          if (plot.getCropRootingZone() != null) {
            devRoot = plot.getCropRootingZone();
          }

          setTableData();

          // setChartData();
          if (plot.getRecommendedAction() != null
              && plot.getRecommendedAction().getAvailableWater() != null) {
            setChartData();
          }

        } else {
          pane.setVisible(false);
        }
      }
    }
  }

}
