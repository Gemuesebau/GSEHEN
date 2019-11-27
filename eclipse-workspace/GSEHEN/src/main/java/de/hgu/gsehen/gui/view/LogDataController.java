package de.hgu.gsehen.gui.view;

import static de.hgu.gsehen.util.MessageUtil.logException;

import com.jfoenix.controls.JFXDatePicker;

import de.hgu.gsehen.Gsehen;
import de.hgu.gsehen.event.FarmDataChanged;
import de.hgu.gsehen.event.GsehenEventListener;
import de.hgu.gsehen.gui.GsehenGuiElements;
import de.hgu.gsehen.logging.Configurator;
import de.hgu.gsehen.logging.LogDataHandler;
import de.hgu.gsehen.model.LogEntry;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class LogDataController implements GsehenEventListener<FarmDataChanged> {
  private static final Logger LOGGER = Logger.getLogger(Gsehen.class.getName());
  private Gsehen gsehenInstance;
  protected final ResourceBundle mainBundle;
  private BorderPane pane;
  private ObservableList<LogEntry> data;
  public static LocalDate arr;
  private static LocalDate startDate;
  private static LocalDate endDate;

  JFXDatePicker startpicker = new JFXDatePicker();
  JFXDatePicker endpicker = new JFXDatePicker();

  {
    gsehenInstance = Gsehen.getInstance();
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
  public LogDataController(Gsehen application, BorderPane pane) {
    this.gsehenInstance = application;
    this.pane = pane;

    initLogData();
    registerLogHandler();
  }

  protected void registerLogHandler() {
    // Optional<Handler> optionalFileHandler = Arrays.stream(LOGGER.getHandlers()).filter(e -> e
    // instanceof FileHandler).findAny();
    // if (optionalFileHandler.isPresent()) {
    // FileHandler fileHandler = (FileHandler) optionalFileHandler.get();
    // }

    LOGGER.addHandler(new LogDataHandler(this));
  }

  protected void initLogData() {
    data = FXCollections.observableArrayList();
    updateLogData();
  }

  protected void updateLogData() {
    data.setAll(readLog(Configurator.LOG_FILE_NAME));
  }

  @Override
  public void handle(FarmDataChanged event) {
    pane.setVisible(true);
    createContent();
  }

  /**
   * Read Log.
   * 
   * @param path
   *          from the log file.
   * @return a list of log entries
   */
  protected ArrayList<LogEntry> readLog(String path) {
    ArrayList<LogEntry> logEntries = new ArrayList<>();
    FileReader fileReader;

    File file = new File(path);

    if (file.exists()) {
      try {
        fileReader = new FileReader(file);

        BufferedReader fileStream = new BufferedReader(fileReader);

        String line = fileStream.readLine();

        while (line != null) {
          String[] parts = line.split(" ", 4);
          logEntries.add(new LogEntry(parts[0], parts[1], parts[2], parts[3]));
          line = fileStream.readLine();
        }

        fileStream.close();
      } catch (FileNotFoundException e) {
        e.printStackTrace();
        System.exit(0);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    return logEntries;
  }

  /**
   * Generate TableView and TableColumns also fill TableCells.
   * 
   * @return the generated table view
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public Parent createContent() {
    TableColumn dateCol = new TableColumn(mainBundle.getString("logview.date"));
    dateCol.setSortable(false);
    dateCol.setCellValueFactory(new PropertyValueFactory<LogEntry, String>("date"));

    TableColumn timeCol = new TableColumn(mainBundle.getString("logview.time"));
    timeCol.setSortable(false);
    timeCol.setCellValueFactory(new PropertyValueFactory<LogEntry, String>("time"));

    TableColumn levelCol = new TableColumn(mainBundle.getString("logview.level"));
    levelCol.setSortable(false);
    levelCol.setCellValueFactory(new PropertyValueFactory<LogEntry, String>("level"));

    TableColumn massageCol = new TableColumn(mainBundle.getString("logview.text"));
    massageCol.setSortable(false);
    massageCol.setCellValueFactory(new PropertyValueFactory<LogEntry, String>("massage"));

    TableView tableView = new TableView();
    tableView.getColumns().addAll(dateCol, timeCol, levelCol, massageCol);

    tableView.setItems(data);

    EventHandler<? super MouseEvent> handler = event -> {
    };

    tableView.addEventHandler(MouseEvent.MOUSE_CLICKED, handler);

    tableView.setOnMouseClicked(new EventHandler<MouseEvent>() {
      @Override
      public void handle(MouseEvent mouseEvent) {
        if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
          if (mouseEvent.getClickCount() == 2) {
            popupfilteroptions();
          }
        }
      }

    });
    pane.setTop(tableView);
    tableView.setMinHeight(pane.getHeight());
    return tableView;
  }

  /**
   * Popup-Window for Filtering the entries.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public void popupfilteroptions() {

    Stage stage = new Stage();
    stage.setTitle(mainBundle.getString("logview.filter"));
    Label titleTime = new Label(mainBundle.getString("logview.time") + ":");
    titleTime.setFont(Font.font("Arial", 14));

    // JFXDatePicker and Label for Filter Date
    Label startLabel = new Label(mainBundle.getString("logview.from") + ":");
    startLabel.setFont(Font.font("Arial", 14));

    Label endLabel = new Label(mainBundle.getString("logview.to") + ":");
    endLabel.setFont(Font.font("Arial", 14));

    Label titledate = new Label(mainBundle.getString("logview.date") + ":");
    titledate.setFont(Font.font("Arial", 14));

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
    startpicker.setShowWeekNumbers(true);
    startpicker.setConverter(converter);
    startpicker.setPromptText("dd-MM-yyyy");

    endpicker.setShowWeekNumbers(true);
    endpicker.setConverter(converter);
    endpicker.setPromptText("dd-MM-yyyy");

    HBox startBox = new HBox();
    startBox.getChildren().addAll(startLabel, startpicker);

    HBox endBox = new HBox();
    endBox.getChildren().addAll(endLabel, endpicker);

    HBox dateBox = new HBox();
    dateBox.getChildren().addAll(titledate);

    VBox upBox = new VBox(20);
    upBox.setPadding(new Insets(10, 10, 10, 10));
    upBox.setSpacing(10);
    upBox.getChildren().addAll(dateBox, startBox, endBox);

    HBox titlebox = new HBox();
    titlebox.setCenterShape(true);
    titlebox.getChildren().add(titleTime);
    upBox.getChildren().add(titlebox);

    Group rootGroup = new Group();
    Scene scene = new Scene(rootGroup, 400, 600);
    rootGroup.getChildren().addAll(upBox);
    stage.setScene(scene);
    stage.centerOnScreen();
    stage.show();

    String[] startstyles = { "spinner1", "spinner2", "spinner3" };

    // Spinner von
    SpinnerValueFactory hour = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23);
    Spinner ssp = new Spinner();
    ssp.setValueFactory(hour);
    ssp.getStyleClass().add(startstyles[0]);
    ssp.setPrefWidth(60);
    SpinnerValueFactory minute = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59);
    Spinner ssp1 = new Spinner();
    ssp1.setPrefWidth(60);
    ssp1.setValueFactory(minute);
    ssp1.getStyleClass().add(startstyles[1]);
    SpinnerValueFactory sec = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59);
    Spinner ssp2 = new Spinner();
    ssp2.setPrefWidth(60);
    ssp2.setValueFactory(sec);
    ssp2.getStyleClass().add(startstyles[2]);

    // Spinner bis
    SpinnerValueFactory hours = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23);
    Spinner esp = new Spinner();
    esp.setValueFactory(hours);
    esp.getStyleClass().add(startstyles[0]);
    esp.setPrefWidth(60);
    SpinnerValueFactory minutes = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59);
    Spinner esp1 = new Spinner();
    esp1.setPrefWidth(60);
    esp1.setValueFactory(minutes);
    esp1.getStyleClass().add(startstyles[1]);
    SpinnerValueFactory seconds = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59);
    Spinner esp2 = new Spinner();
    esp2.setPrefWidth(60);
    esp2.setValueFactory(seconds);
    esp2.getStyleClass().add(startstyles[2]);

    Label slabel = new Label(" : ");
    slabel.setFont(Font.font("Arial", 14));
    Label slLabel = new Label(" : ");
    slLabel.setFont(Font.font("Arial", 14));
    Label elabel = new Label(" : ");
    elabel.setFont(Font.font("Arial", 14));
    Label elLabel = new Label(" : ");
    elLabel.setFont(Font.font("Arial", 14));

    HBox startSpinnerBox = new HBox();
    startSpinnerBox.getChildren().addAll(ssp, slabel, ssp1, slLabel, ssp2);
    HBox endSpinnerBox = new HBox();
    endSpinnerBox.getChildren().addAll(esp, elabel, esp1, elLabel, esp2);

    upBox.getChildren().addAll(startSpinnerBox, endSpinnerBox);

    Text slevel = new Text(mainBundle.getString("logview.from") + ":");
    slevel.setFont(Font.font("Arial", 14));
    ChoiceBox stratcb = new ChoiceBox();
    stratcb.getItems().addAll("SEVERE", "WARNING", "INFO", "CONFIG", "FINE", "FINER", "FINEST");
    stratcb.getSelectionModel().selectFirst();

    HBox slev = new HBox();
    slev.getChildren().addAll(slevel, stratcb);

    Text elevel = new Text(mainBundle.getString("logview.to") + ":");
    elevel.setFont(Font.font("Arial", 14));
    ChoiceBox endcb = new ChoiceBox();
    endcb.getItems().addAll("SEVERE", "WARNING", "INFO", "CONFIG", "FINE", "FINER", "FINEST");
    endcb.getSelectionModel().selectFirst();

    Label levlabel = new Label(mainBundle.getString("logview.level") + ":");
    levlabel.setFont(Font.font("Arial", 14));

    HBox levelBox = new HBox();
    levelBox.getChildren().addAll(levlabel);

    HBox elev = new HBox();
    elev.getChildren().addAll(elevel, endcb);

    upBox.getChildren().addAll(levelBox, slev, elev);

    Button save = GsehenGuiElements.button(100);
    save.setText(mainBundle.getString("menu.file.save"));

    save.setOnAction(e -> {
      startDate = startpicker.getValue();
      endDate = endpicker.getValue();
      try {
        while (!startDate.isAfter(endDate)) {
          arr = startDate;
          startDate = startDate.plusDays(1);
        }
      } catch (Exception ex) {
        logException(LOGGER, Level.INFO, ex, "logview.filter.save.exception");
      }
    });

    HBox button = new HBox();
    button.getChildren().addAll(save);

    upBox.getChildren().add(button);

  }

  /**
   * Reload Log.
   *
   * @param logRecord the newly published log record
   */
  public void onLogRecordPublish(LogRecord logRecord) {
    // Anstatt hier immer die ganze Datei neu zu laden wäre es wünschenswert, den hier
    // ankommenden LogRecord zu verwenden
    // Idee wäre: logReord mit HTMLFormatter formaieren und dann in die observable list (this.data)
    // packen
    // Nachteil vom kompletten ersetzen ist auch, dass der Zustand der TableView (Scroll, Highlight
    // etc.) zurückgesetzt wird
    updateLogData();
  }
}