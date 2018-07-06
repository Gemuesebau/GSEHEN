package de.hgu.gsehen.gui.view;

import de.hgu.gsehen.Gsehen;

import de.hgu.gsehen.event.FarmDataChanged;
import de.hgu.gsehen.event.GsehenEventListener;
import de.hgu.gsehen.logging.Configurator;
import de.hgu.gsehen.logging.LogDataHandler;
import de.hgu.gsehen.model.LogEntry;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;


public class LogDataController implements GsehenEventListener<FarmDataChanged> {
  private static final Logger LOGGER = Logger.getLogger(Gsehen.class.getName());
  private Gsehen gsehenInstance;
  private BorderPane pane;
  private ObservableList<LogEntry> data;
  private MenuBar datePickerMenuBar = null;

  {
    gsehenInstance = Gsehen.getInstance();
    gsehenInstance.registerForEvent(FarmDataChanged.class, this);
  }

  /**
   * Constructs a new plot data controller associated with the given BorderPane.
   *
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
   * @return
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
   * @return
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public Parent createContent() {
    TableColumn dateCol = new TableColumn("Datum");
    dateCol.setSortable(false);
    dateCol.setCellValueFactory(new PropertyValueFactory<LogEntry, String>("date"));

    TableColumn timeCol = new TableColumn("Zeit");
    timeCol.setSortable(false);
    timeCol.setCellValueFactory(new PropertyValueFactory<LogEntry, String>("time"));

    TableColumn levelCol = new TableColumn("Level");
    levelCol.setSortable(false);
    levelCol.setCellValueFactory(new PropertyValueFactory<LogEntry, String>("level"));

    TableColumn massageCol = new TableColumn("Nachricht");
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
            popupfilter();
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
  public void popupfilter() {

    Stage stage = new Stage();
    stage.setTitle("Filteroptionen");

    // DatePicker and Label for Filter Date
    DatePicker startdatepicker = null;
    Label startLabel = new Label("Von: ");
    startLabel.setFont(Font.font("Arial", 14));
    startdatepicker = createDatePicker(startdatepicker);

    HBox startBox = new HBox();
    startBox.getChildren().addAll(startLabel, startdatepicker);

    DatePicker enddatepicker = null;
    Label endLabel = new Label("Bis:  ");
    endLabel.setFont(Font.font("Arial", 14));
    enddatepicker = createDatePicker(enddatepicker);

    HBox endBox = new HBox();
    endBox.getChildren().addAll(endLabel, enddatepicker);

    VBox upBox = new VBox(20);
    upBox.setPadding(new Insets(10, 10, 10, 10));
    upBox.setSpacing(10);
    upBox.getChildren().addAll(startBox, endBox);

    Group rootGroup = new Group();
    Scene scene = new Scene(rootGroup, 400, 600);
    rootGroup.getChildren().addAll(upBox);
    stage.setScene(scene);
    stage.centerOnScreen();
    stage.show();
  }

  /**
   * Datepicker.
   * 
   * @param datePicker
   *          take Date
   * @return
   */
  public DatePicker createDatePicker(DatePicker datePicker) {

    LocalDate value = null;
    if (datePicker != null) {
      value = datePicker.getValue();
    }
    DatePicker picker = new DatePicker();
    // Listen for DatePicker actions
    picker.setOnAction((ActionEvent t) -> {
      LocalDate isoDate = picker.getValue();
      if ((isoDate != null) && (!isoDate.equals(LocalDate.now()))) {
        for (Menu menu : datePickerMenuBar.getMenus()) {
          if (menu.getText().equals("Options for Locale")) {
            for (MenuItem menuItem : menu.getItems()) {
              if (menuItem.getText().equals("Set date to today")) {
                if ((menuItem instanceof CheckMenuItem)
                    && ((CheckMenuItem) menuItem).isSelected()) {
                  ((CheckMenuItem) menuItem).setSelected(false);
                }
              }
            }
          }
        }
      }
    });
    // hbox.getChildren().add(picker);
    if (value != null) {
      picker.setValue(value);
    }
    return picker;
  }

  /**
   * Reload Log.
   */
  public void onLogRecordPublish(LogRecord logRecord) {
    // TODO: Anstatt hier immer die ganze Datei neu zu laden wäre es wünschenswert, den hier
    // ankommenden LogRecord zu verwenden
    // Idee wäre: logReord mit HTMLFormatter formaieren und dann in die observable list (this.data)
    // packen
    // Nachteil vom kompletten ersetzen ist auch, dass der Zustand der TableView (Scroll, Highlight
    // etc.) zurückgesetzt wird
    updateLogData();
  }
}