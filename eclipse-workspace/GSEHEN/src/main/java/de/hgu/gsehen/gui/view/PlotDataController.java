package de.hgu.gsehen.gui.view;

import de.hgu.gsehen.Gsehen;
import de.hgu.gsehen.event.FarmDataChanged;
import de.hgu.gsehen.event.GsehenEventListener;
import de.hgu.gsehen.model.Crop;
import de.hgu.gsehen.model.Drawable;
import de.hgu.gsehen.model.Plot;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.logging.Logger;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.util.StringConverter;

public class PlotDataController implements GsehenEventListener<FarmDataChanged> {
  private final Timeline timeline = new Timeline();
  private static final String FARM_TREE_VIEW_ID = "#farmTreeView";
  private static final Logger LOGGER = Logger.getLogger(Gsehen.class.getName());

  private TreeItem<Drawable> selectedItem;
  private Plot plot;

  private Gsehen gsehenInstance;
  private BorderPane pane;
  private TreeTableView<Drawable> treeTableView;

  private Text nameLabel;
  private Text areaLabel;
  private Text soilStartLabel;
  private Text soilStartValueLabel;

  private TextField name;
  private Text area;
  private DatePicker soilStart;
  private Text soilStartValue;

  private Text waterBalanceLabel;

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

  @SuppressWarnings("unchecked")
  @Override
  public void handle(FarmDataChanged event) {
    /*
     * TODO: Alles noch sehr wild auf einen Haufen geworfen. Hier wird also noch aufgeräumt! 1)
     * Echte Daten erstellen. 2) Schauen, was sinnvoll ist und was nicht.
     */

    pane.setVisible(false);

    // TOP ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    nameLabel = new Text("Name: ");
    nameLabel.setFont(Font.font("Arial", 14));
    name = new TextField("");

    HBox nameBox = new HBox();
    // nameBox.setStyle("-fx-background-color: #d39494;"); // Nur zur Übersicht!
    nameBox.getChildren().addAll(nameLabel, name);

    areaLabel = new Text("m²: ");
    areaLabel.setFont(Font.font("Arial", 14));
    area = new Text("");
    area.setFont(Font.font("Arial", FontWeight.BOLD, 14));

    HBox locationBox = new HBox();
    // locationBox.setStyle("-fx-background-color: #acd293;"); // Nur zur Übersicht!
    locationBox.getChildren().addAll(areaLabel, area);

    VBox topBox = new VBox(25);
    // topBox.setStyle("-fx-background-color: #f4ec46;"); // Nur zur Übersicht!
    topBox.setPadding(new Insets(20, 20, 20, 20));
    topBox.getChildren().addAll(nameBox, locationBox);
    pane.setTop(topBox);
    // TOP END ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    // LEFT ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    Text crop = new Text("Kultur: ");
    crop.setFont(Font.font("Arial", 14));
    // Dummy-Liste
    ChoiceBox<Crop> cropChoiceBox = new ChoiceBox<Crop>();
    cropChoiceBox.getItems().addAll(new Crop("Apfel"), new Crop("Birne"), new Crop("Löwenzahn"),
        new Crop("Tomate"), new Crop("Zwiebel"));
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

    // cropChoiceBox.valueProperty().addListener(new ChangeListener<String>() {
    // @Override
    // public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2) {
    // Drawable obj = null;
    // Crop crop = new Crop();
    // crop.setName(cropChoiceBox.getValue().toString());
    //
    // for (Farm farm : gsehenInstance.getFarmsList()) {
    // for (Field field : farm.getFields()) {
    // for (Plot plot : field.getPlots()) {
    // obj = plot;
    // Crop dummyCrop = new Crop();
    // dummyCrop.setName("");
    // plot.setCrop(dummyCrop);
    // for (int i = 0; i < treeTableView.getSelectionModel().getSelectedCells()
    // .size(); i++) {
    // if (treeTableView.getSelectionModel().getSelectedCells().get(i) != null
    // && plot.getName().equals(treeTableView.getSelectionModel().getSelectedCells()
    // .get(i).getTreeItem().getValue().getName())) {
    // plot.setCrop(crop);
    // LOGGER.info("'" + crop.getName() + "' was set as crop in" + obj);
    // }
    // }
    // }
    // }
    // }
    // gsehenInstance.sendFarmDataChanged(obj, null);
    // }
    // });

    HBox cropBox = new HBox();
    // cropBox.setStyle("-fx-background-color: #d39494;"); // Nur zur Übersicht!
    cropBox.getChildren().addAll(crop, cropChoiceBox);

    soilStartLabel = new Text("Start der Wasserbilanz: ");
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

    HBox soilStartBox = new HBox();
    // cropStartBox.setStyle("-fx-background-color: #acd293;"); // Nur zur
    // Übersicht!
    soilStartBox.getChildren().addAll(soilStartLabel, soilStart);

    soilStartValueLabel = new Text("Startwert der Wasserbilanz: ");
    soilStartValueLabel.setFont(Font.font("Arial", 14));
    soilStartValue = new Text("");
    soilStartValue.setFont(Font.font("Arial", FontWeight.BOLD, 14));

    HBox soilStartValueBox = new HBox();
    // cropEndBox.setStyle("-fx-background-color: #466bf4;"); // Nur zur Übersicht!
    soilStartValueBox.getChildren().addAll(soilStartValueLabel, soilStartValue);

    VBox leftBox = new VBox(50);
    // leftBox.setStyle("-fx-background-color: #f4ba46;"); // Nur zur Übersicht!
    leftBox.setPadding(new Insets(20, 20, 20, 20));
    leftBox.getChildren().addAll(cropBox, soilStartBox, soilStartValueBox);
    pane.setLeft(leftBox);
    // LEFT END ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    // CENTER ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    waterBalanceLabel = new Text("Wasserbilanz: ");
    waterBalanceLabel.setFont(Font.font("Arial", 14));

    // styled ProgressIndicator
    final ProgressIndicator waterBalance = new ProgressIndicator();
    waterBalance.setPrefSize(200, 200);
    waterBalance.styleProperty().bind(Bindings.createStringBinding(() -> {
      final double percent = waterBalance.getProgress();
      if (percent < 0) {
        // progress bar went indeterminate
        return null;
      }

      // poor man's gradient for stops: red, yellow 50%, green
      // Based on
      // http://en.wikibooks.org/wiki/Color_Theory/Color_gradient#Linear_RGB_gradient_with_6_segments
      //
      final double m = (2d * percent);
      final int n = (int) m;
      final double f = m - n;
      final int t = (int) (255 * f);
      int r = 0;
      int g = 0;
      int b = 0;
      switch (n) {
        case 0:
          r = 255;
          g = t;
          b = 0;
          break;
        case 1:
          r = 255 - t;
          g = 255;
          b = 0;
          break;
        case 2:
          r = 0;
          g = 255;
          b = 0;
          break;
        default:
          break;
      }
      final String style = String.format("-fx-progress-color: rgb(%d,%d,%d)", r, g, b);
      return style;
    }, waterBalance.progressProperty()));

    // animate the styled ProgressIndicator - NUR ZUR VERANSCHAULICHUNG!
    timeline.setCycleCount(Timeline.INDEFINITE);
    timeline.setAutoReverse(true);
    final KeyValue kv0 = new KeyValue(waterBalance.progressProperty(), 0);
    final KeyValue kv1 = new KeyValue(waterBalance.progressProperty(), 1);
    final KeyFrame kf0 = new KeyFrame(Duration.ZERO, kv0);
    final KeyFrame kf1 = new KeyFrame(Duration.millis(10000), kv1);
    timeline.getKeyFrames().addAll(kf0, kf1);

    HBox waterBalanceBox = new HBox();
    // waterBalanceBox.setStyle("-fx-background-color: #466bf4;"); // Nur zur
    // Übersicht!
    waterBalanceBox.getChildren().addAll(waterBalanceLabel, waterBalance);

    VBox centerBox = new VBox();
    // leftBox.setStyle("-fx-background-color: #917d57;"); // Nur zur Übersicht!
    centerBox.setPadding(new Insets(20, 20, 20, 20));
    centerBox.getChildren().addAll(waterBalanceBox);
    pane.setCenter(centerBox);
    play();
    // CENTER END ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    Button save = new Button("Speichern");
    save.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent e) {
        // TODO
        // Name
        plot.setName(name.getText());
        // Crop
        plot.setCrop(cropChoiceBox.getValue());
        // Date (SoilStart)
        LocalDate localDate = soilStart.getValue();
        Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        plot.setSoilStartDate(date);
        
        gsehenInstance.sendFarmDataChanged(plot, null);
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
                  // TODO

                  pane.setVisible(true);
                  plot = (Plot) selectedItem.getValue();

                  name.setText(plot.getName());

                  area.setText(String.valueOf(plot.getArea()));

                  cropChoiceBox.getSelectionModel().select(plot.getCrop());

                  Date date = plot.getSoilStartDate();
                  if (date != null) {
                    LocalDate localDate =
                        date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    soilStart.setValue(localDate);
                  } else {
                    soilStart.setValue(null);
                  }

                  soilStartValue.setText(String.valueOf(plot.getSoilStartValue()));

                  // String geoPolygon = "";
                  // DecimalFormat decimal = new DecimalFormat("#.#####");
                  // for (double dx : selectedItem.getValue().getPolygon().getPolygonData()
                  // .getPointsX()) {
                  // for (double dy : selectedItem.getValue().getPolygon().getPolygonData()
                  // .getPointsY()) {
                  // geoPolygon += "[X: " + decimal.format(dx) + " / Y: " + decimal.format(dy)
                  // + "] \n\t\t\t\t";
                  // }
                  // }
                  // geopolygon.setText("GeoPolygon:\t\t" + geoPolygon);
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
