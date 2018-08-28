package de.hgu.gsehen.gui.view;

import de.hgu.gsehen.Gsehen;
import de.hgu.gsehen.event.FarmDataChanged;
import de.hgu.gsehen.event.GsehenEventListener;
import de.hgu.gsehen.model.Crop;
import de.hgu.gsehen.model.Drawable;
import de.hgu.gsehen.model.Plot;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
// import java.util.logging.Logger;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
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
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.StringConverter;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.hibernate.Session;
import org.hibernate.query.Query;

public class PlotDataController implements GsehenEventListener<FarmDataChanged> {
  private final Timeline timeline = new Timeline();
  private static final String FARM_TREE_VIEW_ID = "#farmTreeView";
  protected static final ResourceBundle mainBundle =
      ResourceBundle.getBundle("i18n.main", Locale.GERMAN);
  // private static final Logger LOGGER = Logger.getLogger(Gsehen.class.getName());

  private List<Crop> cropList = new ArrayList<>();

  private TreeItem<Drawable> selectedItem;
  private Plot plot;

  private Gsehen gsehenInstance;
  private BorderPane pane;
  private TreeTableView<Drawable> treeTableView;

  private Text nameLabel;
  private Text areaLabel;
  private Text rootingZoneLabel;
  private Text soilStartLabel;
  private Text soilStartValueLabel;
  private Text cropStartLabel;

  private TextField name;
  private TextField rootingZone;
  private Text area;
  private DatePicker soilStart;
  private DatePicker cropStart;
  private TextField soilStartValue;

  private Text waterBalanceLabel;
  private boolean isActive = true;

  {
    gsehenInstance = Gsehen.getInstance();
    gsehenInstance.registerForEvent(FarmDataChanged.class, this);
  }

  /**
   * Constructs a new plot data controller associated with the given BorderPane.
   *
   * @param pane - the associated BorderPane.
   */
  public PlotDataController(Gsehen application, BorderPane pane) {
    this.gsehenInstance = application;
    this.pane = pane;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public void handle(FarmDataChanged event) {
    pane.setVisible(false);

    // GridPane - Center Section
    GridPane top = new GridPane();

    // GridPane Configuration (Padding, Gaps, etc.)
    top.setPadding(new Insets(20, 20, 20, 20));
    top.setHgap(15);
    top.setVgap(15);
    top.setGridLinesVisible(false);

    // Set Column and Row Constraints
    ColumnConstraints column1 = new ColumnConstraints(200, 100, 300);
    ColumnConstraints column2 = new ColumnConstraints(200, 100, 100);
    column1.setHgrow(Priority.ALWAYS);
    column2.setHgrow(Priority.ALWAYS);
    RowConstraints rowEmpty = new RowConstraints();

    // Add Constraints to Columns & Rows
    top.getColumnConstraints().addAll(column1, column2);
    top.getRowConstraints().add(0, rowEmpty);
    top.getRowConstraints().add(1, rowEmpty);

    // TOP ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    nameLabel = new Text(mainBundle.getString("fieldview.name"));
    nameLabel.setFont(Font.font("Arial", 14));
    name = new TextField("");

    areaLabel = new Text(mainBundle.getString("fieldview.area"));
    areaLabel.setFont(Font.font("Arial", 14));
    area = new Text("");
    area.setFont(Font.font("Arial", FontWeight.BOLD, 14));

    rootingZoneLabel = new Text(mainBundle.getString("plotview.rootingzone"));
    rootingZoneLabel.setFont(Font.font("Arial", 14));
    rootingZone = new TextField("");

    cropStartLabel = new Text(mainBundle.getString("plotview.cropstart"));
    cropStartLabel.setFont(Font.font("Arial", 14));
    cropStart = new DatePicker();
    cropStart.setShowWeekNumbers(true);
    StringConverter<LocalDate> convert = new StringConverter<LocalDate>() {
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

    soilStartLabel = new Text(mainBundle.getString("plotview.soilstart"));
    soilStartLabel.setFont(Font.font("Arial", 14));
    soilStart = new DatePicker();
    soilStart.setShowWeekNumbers(true);

    // Converter
    StringConverter<LocalDate> converter = new StringConverter<LocalDate>() {
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

    soilStart.setConverter(converter);
    soilStart.setPromptText("dd-MM-yyyy");

    Button b1 = new Button(mainBundle.getString("plotview.pause"));
    b1.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent e) {
        // TODO: pausieren der berechnung
      }
    });

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

    Button b2 = new Button(mainBundle.getString("plotview.harvest"));
    b2.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent e) {
        isActive = false;
        Date date = Calendar.getInstance().getTime();
        DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        String enddate = formatter.format(date);
      }
    });

    // Gsehen.crop(); TODO: Schauen, ob's läuft, sobald die Methode funktioniert.

    Text crop = new Text(mainBundle.getString("plotview.crop"));
    crop.setFont(Font.font("Arial", 14));

    EntityManagerFactory emf = Persistence.createEntityManagerFactory("GSEHEN");
    EntityManager em = emf.createEntityManager();
    if (cropList.isEmpty()) {
      try {
        Session session = em.unwrap(Session.class);
        Query<Crop> query = session.createQuery("from Crop");
        cropList = query.list();
      } finally {
        em.close();
      }
    }

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
      public String toString(Crop object) {
        return object.getName();
      }

      @Override
      public Crop fromString(String string) {
        return cropChoiceBox.getItems().stream().filter(ap -> ap.getName().equals(string))
            .findFirst().orElse(null);
      }
    });

    // Set Nodes Vertical & Horizontal Alignment
    GridPane.setHalignment(nameLabel, HPos.LEFT);
    GridPane.setHalignment(name, HPos.LEFT);
    GridPane.setHalignment(areaLabel, HPos.LEFT);
    GridPane.setHalignment(area, HPos.LEFT);
    GridPane.setHalignment(rootingZoneLabel, HPos.LEFT);
    GridPane.setHalignment(rootingZone, HPos.LEFT);
    GridPane.setHalignment(cropStartLabel, HPos.LEFT);
    GridPane.setHalignment(cropStart, HPos.LEFT);
    GridPane.setHalignment(soilStartLabel, HPos.LEFT);
    GridPane.setHalignment(soilStart, HPos.LEFT);
    GridPane.setHalignment(b1, HPos.LEFT);
    GridPane.setHalignment(soilStartValueLabel, HPos.LEFT);
    GridPane.setHalignment(soilStartValue, HPos.LEFT);
    GridPane.setHalignment(b2, HPos.LEFT);
    GridPane.setHalignment(crop, HPos.LEFT);
    GridPane.setHalignment(cropChoiceBox, HPos.LEFT);

    // Set Row & Column Index for Nodes
    GridPane.setConstraints(nameLabel, 0, 0);
    GridPane.setConstraints(name, 1, 0);
    GridPane.setConstraints(areaLabel, 0, 1);
    GridPane.setConstraints(area, 1, 1);
    GridPane.setConstraints(rootingZoneLabel, 0, 2);
    GridPane.setConstraints(rootingZone, 1, 2);
    GridPane.setConstraints(cropStartLabel, 0, 3);
    GridPane.setConstraints(cropStart, 1, 3);
    GridPane.setConstraints(soilStartLabel, 0, 4);
    GridPane.setConstraints(soilStart, 1, 4);
    GridPane.setConstraints(b1, 2, 4);
    GridPane.setConstraints(soilStartValueLabel, 0, 5);
    GridPane.setConstraints(soilStartValue, 1, 5);
    GridPane.setConstraints(b2, 2, 5);
    GridPane.setConstraints(crop, 0, 6);
    GridPane.setConstraints(cropChoiceBox, 1, 6);

    top.getChildren().addAll(nameLabel, name, areaLabel, area, rootingZoneLabel, rootingZone,
        cropStartLabel, cropStart, soilStartLabel, soilStart, b1, soilStartValueLabel,
        soilStartValue, b2, crop, cropChoiceBox);

    pane.setTop(top);
    // TOP END ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    // CENTER ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    waterBalanceLabel = new Text(mainBundle.getString("plotview.waterbalance"));
    waterBalanceLabel.setFont(Font.font("Arial", 14));

    // Balkendiagramm Wasserbillanz TODO: Farben einbauen/Wassertiefe in scala
    CategoryAxis xaxis;
    NumberAxis yaxis;
    String[] plote = {"Plot"};
    xaxis = new CategoryAxis();
    xaxis.setCategories(FXCollections.<String>observableArrayList(plote));
    yaxis = new NumberAxis("cm", 0.0d, 25.0d, 1);
    ObservableList<BarChart.Series> barChartData = FXCollections
        .observableArrayList(new BarChart.Series(mainBundle.getString("plotview.waterinsoil"),
            FXCollections.observableArrayList(new BarChart.Data(plote[0], 7.0d))));
    BarChart chart;
    chart = new BarChart(xaxis, yaxis, barChartData, 25.0d);

    HBox waterBalanceBox = new HBox();
    waterBalanceBox.getChildren().addAll(waterBalanceLabel, chart);

    VBox centerBox = new VBox();
    centerBox.setPadding(new Insets(20, 20, 20, 20));
    centerBox.getChildren().addAll(waterBalanceBox);
    pane.setCenter(centerBox);

    play();
    // CENTER END ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    Button save = new Button(mainBundle.getString("menu.file.save"));
    save.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent e) {
        try {
          EntityManagerFactory emf = Persistence.createEntityManagerFactory("GSEHEN");
          EntityManager em = emf.createEntityManager();
          try {
            em.getTransaction().begin();
            plot.setName(name.getText());
            plot.setCrop(cropChoiceBox.getValue());

            LocalDate localDate = soilStart.getValue();
            LocalDate cropDate = cropStart.getValue();
            Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date cropdate = Date.from(cropDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

            plot.setSoilStartDate(date);
            plot.setCropStart(cropdate);
            plot.setRootingZone(Double.valueOf(rootingZone.getText()));
            plot.setSoilStartValue(Double.valueOf(soilStartValue.getText()));
            // plot.setCalculationPaused(calculationPaused); TODO
            plot.setIsActive(isActive);

            em.getTransaction().commit();
          } catch (Exception d) {
            em.getTransaction().rollback();
          } finally {
            em.close();
          }
        } finally {
          gsehenInstance.sendFarmDataChanged(plot, null);
        }
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
                    && selectedItem.getValue().getClass().getSimpleName().equals("Plot")) {
                  pane.setVisible(true);
                  plot = (Plot) selectedItem.getValue();

                  name.setText(plot.getName());

                  area.setText(String.valueOf(plot.getPolygon().calculateArea()));

                  if (plot.getCrop() != null && cropList.size() != 0) {
                    cropChoiceBox.getSelectionModel().select(plot.getCrop());
                  } else {
                    cropChoiceBox.getSelectionModel().selectFirst();
                  }

                  Date date = plot.getSoilStartDate();
                  Date cropdate = plot.getCropStart();
                  if (date != null) {
                    LocalDate localDate =
                        date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    LocalDate cropDate =
                        date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    soilStart.setValue(localDate);
                    cropStart.setValue(cropDate);
                  } else {
                    soilStart.setValue(null);
                    cropStart.setValue(null);
                  }
                  if (cropdate != null) {
                    LocalDate cropDate =
                        cropdate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
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
                } else {
                  pane.setVisible(false);
                }
              }
            }
          }
        });
  }

  public void play() {
    timeline.play();
  }

}
