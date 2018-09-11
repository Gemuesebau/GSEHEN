package de.hgu.gsehen.gui.view;

import de.hgu.gsehen.Gsehen;
import de.hgu.gsehen.event.FarmDataChanged;
import de.hgu.gsehen.event.GsehenEventListener;
import de.hgu.gsehen.gui.CropPhase;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.StringConverter;

@SuppressWarnings("static-access")
public class PlotDataController implements GsehenEventListener<FarmDataChanged> {
  private final Timeline timeline = new Timeline();
  private static final String FARM_TREE_VIEW_ID = "#farmTreeView";
  protected final ResourceBundle mainBundle;

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
  private TabPane tabPane;
  private Tab mapViewTab;
  private Tab farmViewTab;
  private Tab fieldViewTab;
  private Tab plotViewTab;
  private Tab logViewTab;
  private TableView<CropPhase> cropTable;

  private Text nameLabel;
  private Text areaLabel;
  private Text rootingZoneLabel;
  private Text soilStartLabel;
  private Text soilStartValueLabel;
  private Text cropStartLabel;
  private StringConverter<LocalDate> convert;

  private TextField name;
  private TextField rootingZone;
  private Text area;
  private DatePicker soilStart;
  private DatePicker cropStart;
  private TextField soilStartValue;

  private Text waterBalanceLabel;
  private boolean isActive = true;
  private HBox bottomBox;
  private Text error;
  private Double waterLevel;

  {
    gsehenInstance = Gsehen.getInstance();
    cropList = gsehenInstance.getCrops();

    gsehenInstance.registerForEvent(FarmDataChanged.class, this);

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

    // GridPane - Center Section
    GridPane centerGrid = new GridPane();

    // GridPane Configuration (Padding, Gaps, etc.)
    centerGrid.setPadding(new Insets(20, 20, 20, 20));
    centerGrid.setHgap(15);
    centerGrid.setVgap(15);
    centerGrid.setGridLinesVisible(false);

    // Set Column and Row Constraints
    ColumnConstraints column1 = new ColumnConstraints(200, 100, 300);
    ColumnConstraints column2 = new ColumnConstraints(200, 100, 100);
    column1.setHgrow(Priority.ALWAYS);
    column2.setHgrow(Priority.ALWAYS);
    RowConstraints rowEmpty = new RowConstraints();

    // Add Constraints to Columns & Rows
    centerGrid.getColumnConstraints().addAll(column1, column2);
    centerGrid.getRowConstraints().add(0, rowEmpty);
    centerGrid.getRowConstraints().add(1, rowEmpty);

    // CENTER (From "Name" to "Bewässerungsfaktor")
    // Name
    nameLabel = new Text(mainBundle.getString("fieldview.name"));
    nameLabel.setFont(Font.font("Arial", 14));
    name = new TextField("");

    // m²
    areaLabel = new Text(mainBundle.getString("fieldview.area"));
    areaLabel.setFont(Font.font("Arial", 14));
    area = new Text("");
    area.setFont(Font.font("Arial", FontWeight.BOLD, 14));

    // Max. durchw. Zone
    rootingZoneLabel = new Text(mainBundle.getString("plotview.rootingzone"));
    rootingZoneLabel.setFont(Font.font("Arial", 14));
    rootingZone = new TextField("");
    rootingZone.textProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observable, String oldValue,
          String newValue) {
        if (newValue != null) {
          if (!newValue.matches("\\d{0,7}([\\.]\\d{0,4})?")) {
            soilStartValue.setText(oldValue);
          }
        }
      }
    });

    // Start der Inkulturnahme
    cropStartLabel = new Text(mainBundle.getString("plotview.cropstart"));
    cropStartLabel.setFont(Font.font("Arial", 14));
    cropStart = new DatePicker();
    cropStart.setShowWeekNumbers(true);
    convert = new StringConverter<LocalDate>() {
      DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

      @Override
      public String toString(LocalDate date) {
        if (date != null) {
          return dateFormatter.format(date);
        } else {
          return "";
        }
      }

      @Override
      public LocalDate fromString(String string) {
        if (string != null && !string.isEmpty()) {
          return LocalDate.parse(string, dateFormatter);
        } else {
          return null;
        }
      }
    };
    cropStart.setConverter(convert);
    cropStart.setPromptText("dd-MM-yyyy");

    // Start der Bodenwasserbilanz
    soilStartLabel = new Text(mainBundle.getString("plotview.soilstart"));
    soilStartLabel.setFont(Font.font("Arial", 14));
    soilStart = new DatePicker();
    soilStart.setShowWeekNumbers(true);
    soilStart.setConverter(convert);
    soilStart.setPromptText("dd-MM-yyyy");

    soilStartValueLabel = new Text(mainBundle.getString("plotview.soilstartvalue"));
    soilStartValueLabel.setFont(Font.font("Arial", 14));
    soilStartValue = new TextField("");
    soilStartValue.textProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observable, String oldValue,
          String newValue) {
        if (newValue != null) {
          if (!newValue.matches("\\d{0,7}([\\.]\\d{0,4})?")) {
            soilStartValue.setText(oldValue);
          }
        }
      }
    });

    // Kultur
    Text crop = new Text(mainBundle.getString("plotview.crop"));
    crop.setFont(Font.font("Arial", 14));

    ChoiceBox<Crop> cropChoiceBox = new ChoiceBox<Crop>();
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

    ChangeListener<Crop> changeListener = new ChangeListener<Crop>() {
      @Override
      public void changed(ObservableValue<? extends Crop> observable, //
          Crop oldValue, Crop newValue) {
        if (oldValue != newValue) {
          Tooltip tooltip = new Tooltip(
              gsehenInstance.localizeCropText(cropChoiceBox.getValue().getDescription()));
          tooltip.setStyle(
              "-fx-font-style: italic; -fx-background-color: #ffffff; "
              + "-fx-text-fill: #000000; -fx-font-size: 9pt;");
          cropChoiceBox.setTooltip(tooltip);

          plot.setCrop(newValue);
          // devPhase is a helper-object where you can set the duration of each crop phase
          devPhase = new CropDevelopmentStatus();
          devPhase.setPhase1(plot.getCrop().getPhase1());
          devPhase.setPhase2(plot.getCrop().getPhase2());
          devPhase.setPhase3(plot.getCrop().getPhase3());
          devPhase.setPhase4(plot.getCrop().getPhase4());
          plot.setCropDevelopmentStatus(devPhase);
          // devRoot is a helper-object where you can set the rooting-zone of each crop
          devRoot = new CropRootingZone();
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
    phase.setMinWidth(30);
    phase.setStyle("-fx-alignment:top-center; -fx-font-style: italic");
    phase.setCellValueFactory(new PropertyValueFactory<CropPhase, Integer>("phase"));
    description.setMinWidth(125);
    description.setStyle("-fx-alignment:top-center; -fx-font-style: italic");
    description.setCellValueFactory(new PropertyValueFactory<CropPhase, String>("description"));
    today.setMinWidth(100);
    today.setStyle("-fx-alignment:top-right");
    today.setCellValueFactory(new PropertyValueFactory<CropPhase, String>("todayMarker"));
    startDate.setMinWidth(100);
    startDate.setStyle("-fx-alignment:top-center; -fx-font-style: italic");
    startDate.setCellValueFactory(new PropertyValueFactory<CropPhase, String>("cropStart"));
    duration.setMinWidth(100);
    duration.setStyle("-fx-alignment:top-center; -fx-font: 14px; -fx-font-weight: bold");
    duration.setCellValueFactory(new PropertyValueFactory<CropPhase, Integer>("duration"));
    cropRootingZone.setMinWidth(200);
    cropRootingZone.setStyle("-fx-alignment:top-center; -fx-font: 14px; -fx-font-weight: bold");
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
        if (cropTable.getFocusModel().getFocusedIndex() == 0) {
          devPhase.setPhase1(Integer.valueOf(t.getNewValue()));
        } else if (cropTable.getFocusModel().getFocusedIndex() == 1) {
          devPhase.setPhase2(Integer.valueOf(t.getNewValue()));
        } else if (cropTable.getFocusModel().getFocusedIndex() == 2) {
          devPhase.setPhase3(Integer.valueOf(t.getNewValue()));
        } else if (cropTable.getFocusModel().getFocusedIndex() == 3) {
          devPhase.setPhase4(Integer.valueOf(t.getNewValue()));
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

    // Wasserbilanz
    waterBalanceLabel = new Text(mainBundle.getString("plotview.waterbalance"));
    waterBalanceLabel.setFont(Font.font("Arial", 14));

    // Balkendiagramm TODO: Farben einbauen
    CategoryAxis xaxis;
    NumberAxis yaxis;
    String[] plote = { mainBundle.getString("gui.view.Map.drawableType.Plot") };
    xaxis = new CategoryAxis();
    xaxis.setCategories(FXCollections.<String>observableArrayList(plote));
    yaxis = new NumberAxis("cm", 0.0d, 25.0d, 1);

    waterLevel = 0.0;

    ObservableList<BarChart.Series> barChartData = FXCollections
        .observableArrayList(new BarChart.Series(mainBundle.getString("plotview.waterinsoil"),
            FXCollections.observableArrayList(new BarChart.Data(plote[0], waterLevel))));
    BarChart chart;
    chart = new BarChart(xaxis, yaxis, barChartData, 25.0d);
    play();

    // Bewässerungsfaktor
    Text scalingFactorLabel = new Text(mainBundle.getString("plotview.scalingfactor"));
    scalingFactorLabel.setFont(Font.font("Arial", 14));
    Slider scalingFactor = new Slider();
    scalingFactor.setMin(0.0);
    scalingFactor.setMax(2.0);
    scalingFactor.setValue(1.0);
    scalingFactor.setShowTickLabels(true);
    scalingFactor.setShowTickMarks(true);
    scalingFactor.setMajorTickUnit(1.0);
    scalingFactor.setBlockIncrement(0.1);

    Text scalingValue = new Text();
    scalingValue.setFont(Font.font("Arial", 12));

    scalingFactor.valueProperty().addListener(new ChangeListener<Number>() {
      public void changed(ObservableValue<? extends Number> ov, Number oldVal, Number newVal) {
        scalingValue.setText(String.format("%.2f", newVal));
      }
    });

    // Set Row & Column Index for Nodes
    GridPane.setConstraints(nameLabel, 0, 0);
    GridPane.setConstraints(name, 1, 0, 2, 1);
    GridPane.setConstraints(areaLabel, 0, 1);
    GridPane.setConstraints(area, 1, 1, 2, 1);
    GridPane.setConstraints(rootingZoneLabel, 0, 2);
    GridPane.setConstraints(rootingZone, 1, 2, 2, 1);
    GridPane.setConstraints(cropStartLabel, 0, 3);
    GridPane.setConstraints(cropStart, 1, 3, 2, 1);
    GridPane.setConstraints(soilStartLabel, 0, 4);
    GridPane.setConstraints(soilStart, 1, 4, 2, 1);
    GridPane.setConstraints(soilStartValueLabel, 0, 5);
    GridPane.setConstraints(soilStartValue, 1, 5, 2, 1);
    GridPane.setConstraints(crop, 0, 6);
    GridPane.setConstraints(cropChoiceBox, 1, 6, 2, 1);
    GridPane.setConstraints(tablePane, 0, 7, 3, 1, HPos.CENTER, VPos.CENTER, Priority.ALWAYS,
        Priority.ALWAYS);
    GridPane.setConstraints(waterBalanceLabel, 0, 8);
    GridPane.setConstraints(chart, 1, 8, 2, 1);
    GridPane.setConstraints(scalingFactorLabel, 0, 9);
    GridPane.setConstraints(scalingFactor, 1, 9);
    GridPane.setConstraints(scalingValue, 2, 9);

    centerGrid.getChildren().addAll(nameLabel, name, areaLabel, area, rootingZoneLabel, rootingZone,
        cropStartLabel, cropStart, soilStartLabel, soilStart, soilStartValueLabel, soilStartValue,
        crop, cropChoiceBox, tablePane, waterBalanceLabel, chart, scalingFactorLabel, scalingFactor,
        scalingValue);

    ScrollPane scrollPane = new ScrollPane();
    scrollPane.setContent(centerGrid);
    scrollPane.setPannable(true);

    pane.setCenter(scrollPane);
    // CENTER END ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    // Ernte
    Button harvest = new Button(mainBundle.getString("plotview.harvest"));
    harvest.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent e) {
        isActive = false;
        Date date = Calendar.getInstance().getTime();
        DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        String enddate = formatter.format(date);
        Date cropEnd;
        try {
          cropEnd = formatter.parse(enddate);
          plot.setCropEnd(cropEnd);
          plot.setIsActive(isActive);
          gsehenInstance.saveUserData();
        } catch (ParseException e1) {
          e1.printStackTrace();
        }
      }
    });

    // Plot manuell bewässern (creates a new view)
    Button watering = new Button(mainBundle.getString("plotview.watering"));
    watering.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent e) {
        pane.getChildren().clear();
        treeTableView.setVisible(false);
        tabPane.getTabs().removeAll(mapViewTab, farmViewTab, fieldViewTab, logViewTab);

        // Name of the plot
        Text nameLabel = new Text(plot.getName());
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        pane.setTop(nameLabel);

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

        // Datum (when the irrigation/precipitation should be booked)
        Text dateLabel = new Text(mainBundle.getString("plotview.date"));
        dateLabel.setFont(Font.font("Arial", 14));
        DatePicker date = new DatePicker();
        date.setShowWeekNumbers(true);
        date.setConverter(convert);
        date.setPromptText("dd-MM-yyyy");
        date.setValue(LocalDate.now());

        // Bewässerung (in mm)
        Text irrigationLabel = new Text(mainBundle.getString("plotview.irrigation"));
        irrigationLabel.setFont(Font.font("Arial", 14));
        TextField irrigation = new TextField();
        irrigation.textProperty().addListener(new ChangeListener<String>() {
          @Override
          public void changed(ObservableValue<? extends String> observable, String oldValue,
              String newValue) {
            if (newValue != null) {
              if (!newValue.matches("\\d{0,7}([\\.]\\d{0,4})?")) {
                irrigation.setText(oldValue);
              }
            }
          }
        });
        irrigation.setText(String.valueOf(0.0));

        // Niederschlag (in mm)
        Text precipitationLabel = new Text(mainBundle.getString("plotview.precipitation"));
        precipitationLabel.setFont(Font.font("Arial", 14));
        TextField precipitation = new TextField();
        precipitation.textProperty().addListener(new ChangeListener<String>() {
          @Override
          public void changed(ObservableValue<? extends String> observable, String oldValue,
              String newValue) {
            if (newValue != null) {
              if (!newValue.matches("\\d{0,7}([\\.]\\d{0,4})?")) {
                precipitation.setText(oldValue);
              }
            }
          }
        });
        precipitation.setText(String.valueOf(0.0));

        date.valueProperty().addListener((ov, oldValue, newValue) -> {
          ManualData md = new ManualData();
          boolean newData = true;

          if (plot.getManualData() != null) {
            md = plot.getManualData();
          }

          // Books the irrigation/precipitation for the right day
          for (ManualWaterSupply mws : md.getManualWaterSupply()) {
            Date wateringDate = Date
                .from(newValue.atStartOfDay(ZoneId.systemDefault()).toInstant());
            if ((wateringDate.compareTo(mws.getDate()) == 0)) {
              irrigation.setText(String.valueOf(mws.getIrrigation()));
              precipitation.setText(String.valueOf(mws.getPrecipitation()));
              newData = false;
            } else if (newData) {
              irrigation.setText(String.valueOf(0.0));
              precipitation.setText(String.valueOf(0.0));
            }
          }
        });

        // Set Row & Column Index for Nodes
        GridPane.setConstraints(dateLabel, 0, 0);
        GridPane.setConstraints(date, 1, 0);
        GridPane.setConstraints(irrigationLabel, 0, 1);
        GridPane.setConstraints(irrigation, 1, 1);
        GridPane.setConstraints(precipitationLabel, 0, 2);
        GridPane.setConstraints(precipitation, 1, 2);

        center.getChildren().addAll(dateLabel, date, irrigationLabel, irrigation,
            precipitationLabel, precipitation);

        pane.setCenter(center);

        Button back = new Button(mainBundle.getString("fieldview.back"));
        back.setOnAction(new EventHandler<ActionEvent>() {
          @Override
          public void handle(ActionEvent arg0) {
            pane.getChildren().clear();
            treeTableView.setVisible(true);
            tabPane.getTabs().clear();
            tabPane.getTabs().addAll(mapViewTab, farmViewTab, fieldViewTab, plotViewTab,
                logViewTab);
            gsehenInstance.sendFarmDataChanged(plot, null);
            tabPane.getSelectionModel().select(3);
            treeTableView.getSelectionModel().clearSelection();
            treeTableView.getSelectionModel().select(currentItem);
          }
        });

        // Bewässerung buchen
        Button book = new Button(mainBundle.getString("plotview.book"));
        book.setOnAction(new EventHandler<ActionEvent>() {
          @Override
          public void handle(ActionEvent arg0) {
            LocalDate localDate = date.getValue();
            Date wateringDate = DateUtil
                .truncToDay(Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));

            if (plot.getManualData() == null) {
              ManualData manualData = new ManualData();
              List<ManualWaterSupply> mwsList = new ArrayList<ManualWaterSupply>();
              mwsList.add(parseSupply(wateringDate, irrigation, precipitation));
              manualData.setManualWaterSupply(mwsList);
              plot.setManualData(manualData);
            } else {
              ManualData manualData = plot.getManualData();
              boolean newDate = true;
              for (ManualWaterSupply mws : manualData.getManualWaterSupply()) {
                if (wateringDate.equals(mws.getDate())) {
                  newDate = false;
                  mws.setIrrigation(parseDouble(irrigation));
                  mws.setPrecipitation(parseDouble(precipitation));
                  break;
                }
              }
              if (newDate) {
                manualData.getManualWaterSupply()
                    .add(parseSupply(wateringDate, irrigation, precipitation));
              }
            }

            pane.getChildren().clear();
            treeTableView.setVisible(true);
            tabPane.getTabs().clear();
            tabPane.getTabs().addAll(mapViewTab, farmViewTab, fieldViewTab, plotViewTab,
                logViewTab);
            gsehenInstance.sendFarmDataChanged(plot, null);
            gsehenInstance.sendManualDataChanged(field, plot, wateringDate, null);
            tabPane.getSelectionModel().select(3);
            treeTableView.getSelectionModel().clearSelection();
            treeTableView.getSelectionModel().select(currentItem);
          }

          private ManualWaterSupply parseSupply(Date wateringDate, TextField irrigation,
              TextField precipitation) {
            return new ManualWaterSupply(wateringDate, parseDouble(irrigation),
                parseDouble(precipitation));
          }

          private Double parseDouble(TextField textField) {
            return gsehenInstance.parseDouble(textField.getText()); // FIXME localize! DateFormat!
          }
        });

        HBox buttonBox = new HBox();
        buttonBox.getChildren().addAll(back, book);
        pane.setBottom(buttonBox);
      }
    });

    // Speichern
    Button save = new Button(mainBundle.getString("menu.file.save"));
    save.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent e) {
        if (cropStart.getValue() != null || soilStart.getValue() != null) {
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
            plot.setRootingZone(gsehenInstance.parseDouble(rootingZone.getText()));
            plot.setSoilStartValue(gsehenInstance.parseDouble(soilStartValue.getText()));
            plot.setIsActive(isActive);
            plot.setScalingFactor(scalingFactor.getValue());
          } finally {
            if (bottomBox.getChildren().contains(error)) {
              bottomBox.getChildren().remove(error);
            }
            gsehenInstance.sendFarmDataChanged(plot, null);
            tabPane.getSelectionModel().select(3);
            treeTableView.getSelectionModel().clearSelection();
            treeTableView.getSelectionModel().select(currentItem);
          }
        } else {
          error = new Text(mainBundle.getString("plotview.error"));
          error.setFont(Font.font("Verdana", 14));
          error.setFill(Color.RED);
          bottomBox.getChildren().clear();
          bottomBox.getChildren().addAll(harvest, watering, save, error);
        }
      }
    });
    bottomBox = new HBox();
    bottomBox.setPadding(new Insets(20, 20, 20, 20));
    bottomBox.setSpacing(10);
    bottomBox.getChildren().addAll(harvest, watering, save);

    pane.setBottom(bottomBox);

    tabPane = gsehenInstance.getMainController().getTabPane();
    mapViewTab = gsehenInstance.getMainController().getMapViewTab();
    farmViewTab = gsehenInstance.getMainController().getFarmViewTab();
    fieldViewTab = gsehenInstance.getMainController().getFieldViewTab();
    plotViewTab = gsehenInstance.getMainController().getPlotViewTab();
    logViewTab = gsehenInstance.getMainController().getLogViewTab();

    // Actions that will happen, if you click a 'plot' in the TreeTableView
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
                    && selectedItem.getValue().getClass().getSimpleName().equals("Plot")) {
                  pane.setVisible(true);
                  plot = (Plot) selectedItem.getValue();
                  field = (Field) selectedItem.getParent().getValue();

                  name.setText(plot.getName());

                  area.setText(String.valueOf(plot.getPolygon().calculateArea()));

                  if (plot.getScalingFactor() != null) {
                    scalingFactor.setValue(plot.getScalingFactor());
                  }

                  if (plot.getCrop() != null && cropList.size() != 0) {
                    cropChoiceBox.getSelectionModel().select(plot.getCrop());
                  }

                  Date date = plot.getSoilStartDate();
                  Date cropdate = plot.getCropStart();

                  if (date != null) {
                    LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault())
                        .toLocalDate();
                    LocalDate cropDate = date.toInstant().atZone(ZoneId.systemDefault())
                        .toLocalDate();
                    soilStart.setValue(localDate);
                    cropStart.setValue(cropDate);
                  } else {
                    soilStart.setValue(null);
                    cropStart.setValue(null);
                  }

                  if (cropdate != null) {
                    LocalDate cropDate = cropdate.toInstant().atZone(ZoneId.systemDefault())
                        .toLocalDate();
                    cropStart.setValue(cropDate);
                  } else {
                    cropStart.setValue(null);
                  }

                  if (plot.getRootingZone() == null) {
                    rootingZone.setText(String.valueOf(0.0));
                  } else {
                    rootingZone.setText(String.valueOf(plot.getRootingZone()));
                  }

                  if (plot.getSoilStartValue() == null) {
                    soilStartValue.setText(String.valueOf(0.0));
                  } else {
                    soilStartValue.setText(String.valueOf(plot.getSoilStartValue()));
                  }

                  if (plot.getCropDevelopmentStatus() != null) {
                    devPhase = plot.getCropDevelopmentStatus();
                  }

                  if (plot.getCropRootingZone() != null) {
                    devRoot = plot.getCropRootingZone();
                  }

                  setTableData();

                  if (plot.getRecommendedAction() != null) {
                    waterLevel = plot.getRecommendedAction().getAvailableWater();
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

  @SuppressWarnings("checkstyle:all")
  /**
   * Sets the data in the TableView.
   */
  private void setTableData() {
    cropTable.getItems().clear();

    Date cropdate = plot.getCropStart();

    if (cropdate != null && plot.getCropDevelopmentStatus() != null) {
      Date today = Date
          .from(java.time.LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());

      Date cropStartDate = plot.getCropStart();
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(cropStartDate);

      List<Integer> durations = Arrays.asList(plot.getCrop().getPhase1(),
          plot.getCrop().getPhase2(), plot.getCrop().getPhase3(), plot.getCrop().getPhase4());
      List<String> bbchs = Arrays.asList(plot.getCrop().getBbch1(), plot.getCrop().getBbch2(),
          plot.getCrop().getBbch3(), plot.getCrop().getBbch4());
      List<Integer> rootingZones = Arrays.asList(devRoot.getRootingZone1(),
          devRoot.getRootingZone2(), devRoot.getRootingZone3(), devRoot.getRootingZone4());
      List<Integer> devCropDurations = Arrays.asList(devPhase.getPhase1(), devPhase.getPhase2(),
          devPhase.getPhase3(), devPhase.getPhase4());

      List<CropPhase> cropPhases = new ArrayList<>();
      int index = 0;
      for (Integer duration : durations) {
        if (duration == null || duration == 0) {
          break;
        }
        // TODO
        final Integer currentPhaseDuration = devCropDurations.get(index);
        final Date currentCalendarTime = calendar.getTime();
        calendar.add(Calendar.DAY_OF_YEAR, currentPhaseDuration);
        cropPhases.add(new CropPhase(index + 1, gsehenInstance.localizeCropText(bbchs.get(index)),
            DateUtil.between(today, currentCalendarTime, calendar.getTime()) ? "\u25B6" : "",
            gsehenInstance.formatDate(currentCalendarTime), String.valueOf(currentPhaseDuration),
            String.valueOf(rootingZones.get(index++))));
      }
      cropTable.getItems().addAll(FXCollections.observableArrayList(cropPhases));
    }
  }

  /**
   * Necessary for the bar graph.
   */
  private void play() {
    timeline.play();
  }
}
