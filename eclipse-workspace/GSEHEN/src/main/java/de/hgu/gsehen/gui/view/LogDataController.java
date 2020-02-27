package de.hgu.gsehen.gui.view;

import static de.hgu.gsehen.util.MessageUtil.logMessage;

import com.jfoenix.controls.JFXDatePicker;
import de.hgu.gsehen.Gsehen;
import de.hgu.gsehen.gui.GsehenGuiElements;
import de.hgu.gsehen.logging.Configurator;
import de.hgu.gsehen.logging.LogDataHandler;
import de.hgu.gsehen.model.LogEntry;
import de.hgu.gsehen.util.MessageUtil;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;

public class LogDataController {
  private static final int LOG_EVENT_HANDLE_THRESHOLD = Level.parse(
      System.getProperty("logViewLevel", "INFO")).intValue();
  private static final Logger LOGGER = Logger.getLogger(LogDataController.class.getName());

  private Gsehen instance;
  private ResourceBundle mainBundle;
  private BorderPane pane;

  private ObservableList<LogEntry> data;
  private ThreadLocal<ArrayList<LogEntry>> logEntryBuffer = new ThreadLocal<>();
  private List<ArrayList<LogEntry>> logEntryBuffers = new ArrayList<>();

  private ThreadLocal<SimpleDateFormat[]> dateFormats = new ThreadLocal<>();
  @SuppressWarnings("unchecked")
  private Supplier<SimpleDateFormat>[] dateFormatSuppliers = new Supplier[] {
      () -> Configurator.newSimpleDateFormatOnlyDate(),
      () -> Configurator.newSimpleDateFormatOnlyTime()
  };

  private boolean useFilter = false;

  private Pattern loggerFilter;

  private String startDateTimeStr;
  private String endDateTimeStr;

  private JFXDatePicker startDatePicker = new JFXDatePicker();
  private JFXDatePicker endDatePicker = new JFXDatePicker();

  private Node[] startTimeSpinners;
  private Node[] endTimeSpinners;

  private int fromLevel;
  private int toLevel;

  private ChoiceBox<String> fromLevelChoiceBox;
  private ChoiceBox<String> toLevelChoiceBox;

  private TextField loggerRegex;

  private Stage filterOptionsDialog = null;

  private synchronized void fillDateFormats(SimpleDateFormat[] formats) {
    for (int i = 0; i < formats.length; i++) {
      formats[i] = dateFormatSuppliers[i].get();
    }
  }

  private SimpleDateFormat getFormat(int index) {
    SimpleDateFormat[] formats = dateFormats.get();
    if (formats == null) {
      formats = new SimpleDateFormat[2];
      fillDateFormats(formats);
      dateFormats.set(formats);
    }
    return formats[index];
  }

  private synchronized void addLogEntryBuffer(ArrayList<LogEntry> logEntryBuffer) {
    logEntryBuffers.add(logEntryBuffer);
  }

  private ArrayList<LogEntry> getLogEntryBuffer() {
    ArrayList<LogEntry> result = logEntryBuffer.get();
    if (result == null) {
      result = new ArrayList<>();
      addLogEntryBuffer(result);
      logEntryBuffer.set(result);
    }
    return result;
  }

  private void configurePicker(JFXDatePicker startDatePicker, StringConverter<LocalDate> converter,
      String promptText) {
    startDatePicker.setShowWeekNumbers(true);
    startDatePicker.setConverter(converter);
    startDatePicker.setPromptText(promptText);
  }

  /**
   * Constructs a new log data controller associated with the given BorderPane.
   *
   * @param application the Gsehen application singleton reference
   * @param borderPane the associated BorderPane
   */
  public LogDataController(Gsehen application, BorderPane borderPane) {
    instance = application;
    String applicationDateFormat = instance.getFormat();
    StringConverter<LocalDate> converter = new StringConverter<LocalDate>() {
      DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(applicationDateFormat);
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
    configurePicker(startDatePicker, converter, applicationDateFormat);
    configurePicker(endDatePicker, converter, applicationDateFormat);
    mainBundle = ResourceBundle.getBundle("i18n.main", instance.getSelectedLocale());
    pane = borderPane;
    initLogData();
    initializeLogUpdater();
    registerLogHandler();
    createContent();
    pane.setVisible(true);
  }

  private void registerLogHandler() {
    Logger.getLogger("").addHandler(new LogDataHandler(this));
  }

  private void initLogData() {
    data = FXCollections.observableArrayList();
    final ArrayList<LogEntry> logEntries = readLog(Configurator.LOG_FILE_NAME);
    final int size = logEntries.size();
    int fromIndex = size
        - Integer.parseInt(instance.getPreferenceValue("logViewInitialLineCount", "20"));
    if (fromIndex < 0) {
      fromIndex = 0;
    }
    data.setAll(logEntries.subList(fromIndex, size)); 
  }

  private ArrayList<LogEntry> readLog(String path) {
    ArrayList<LogEntry> logEntries = new ArrayList<>();
    File file = new File(path);
    if (file.exists()) {
      flushLogFile(file);
      try {
        BufferedReader fileStream = new BufferedReader(new FileReader(file));
        String line;
        while ((line = fileStream.readLine()) != null) {
          String[] parts = line.split(" ", 5);
          LogEntry logEntry = newLogEntry(parts[0], parts[1], parts[2], parts[3], parts[4]);
          if (logEntry != null) {
            logEntries.add(logEntry);
          }
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

  private void flushLogFile(File file) {
    for (Handler handler : Logger.getLogger("").getHandlers()) {
      if (handler instanceof FileHandler) {
        handler.flush();
      }
    }
  }

  private void initializeLogUpdater() {
    Timeline logUpdater =
        new Timeline(new KeyFrame(Duration.seconds(1), event -> {
          moveLogEntries();
        }));
    logUpdater.setCycleCount(Timeline.INDEFINITE);
    logUpdater.play();
  }

  private synchronized void moveLogEntries() {
    for (ArrayList<LogEntry> logEntryBuffer : logEntryBuffers) {
      data.addAll(logEntryBuffer);
      logEntryBuffer.clear();
    }
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

    TableColumn loggerCol = new TableColumn(mainBundle.getString("logview.logger"));
    loggerCol.setSortable(false);
    loggerCol.setCellValueFactory(new PropertyValueFactory<LogEntry, String>("logger"));

    TableColumn messageCol = new TableColumn(mainBundle.getString("logview.text"));
    messageCol.setSortable(false);
    messageCol.setCellValueFactory(new PropertyValueFactory<LogEntry, String>("message"));

    TableView tableView = new TableView();
    tableView.getColumns().addAll(dateCol, timeCol, levelCol, loggerCol, messageCol);

    tableView.setItems(data);

    tableView.setTooltip(new Tooltip(mainBundle.getString("logview.tooltip")));
    tableView.setOnMouseClicked(new EventHandler<MouseEvent>() {
      @Override
      public void handle(MouseEvent mouseEvent) {
        if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
          if (mouseEvent.getClickCount() == 2) {
            popupFilterOptions();
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
  @SuppressWarnings("unchecked")
  public void popupFilterOptions() {
    if (filterOptionsDialog != null) {
      filterOptionsDialog.setAlwaysOnTop(true);
      filterOptionsDialog.setAlwaysOnTop(false);
      return;
    }
    Stage stage = new Stage();
    filterOptionsDialog = stage;
    stage.setTitle(mainBundle.getString("logview.filter"));
    GridPane gridPane = new GridPane();
    gridPane.setPadding(new Insets(15, 15, 15, 15));
    int rowIndex = 0;
    setRow(gridPane, rowIndex++, newHeading(mainBundle.getString("logview.from")));
    setRow(gridPane, rowIndex++, true, newLabel(mainBundle.getString("logview.date")),
        startDatePicker);
    setRow(gridPane, rowIndex++, newLabel(mainBundle.getString("logview.time"), 5.0));
    startTimeSpinners = setRow(gridPane, rowIndex++,
        newSpinner(23, false), newSpinner(59, false), newSpinner(59, false));

    setRow(gridPane, rowIndex++, true, new Separator(), newPaddedSeparator(10, 0, 10, 0));
    setRow(gridPane, rowIndex++, newHeading(mainBundle.getString("logview.to")));
    setRow(gridPane, rowIndex++, true, newLabel(mainBundle.getString("logview.date")),
        endDatePicker);
    setRow(gridPane, rowIndex++, newLabel(mainBundle.getString("logview.time"), 5.0));
    endTimeSpinners = setRow(gridPane, rowIndex++,
        newSpinner(23, true), newSpinner(59, true), newSpinner(59, true));

    setRow(gridPane, rowIndex++, true, new Separator(), newPaddedSeparator(10, 0, 10, 0));
    setRow(gridPane, rowIndex++, newHeading(mainBundle.getString("logview.level")));
    fromLevelChoiceBox = (ChoiceBox<String>)setRow(gridPane, rowIndex++, true,
        newLabel(mainBundle.getString("logview.from")), newLevelChoiceBox(s -> s.selectLast()))[1];
    toLevelChoiceBox = (ChoiceBox<String>)setRow(gridPane, rowIndex++, true,
        newLabel(mainBundle.getString("logview.to")), newLevelChoiceBox(s -> s.selectFirst()))[1];

    setRow(gridPane, rowIndex++, true, new Separator(), newPaddedSeparator(10, 0, 10, 0));
    setRow(gridPane, rowIndex++, newHeading(mainBundle.getString("logview.logger")));
    loggerRegex = (TextField)setRow(gridPane, rowIndex++, true,
        newLabel(mainBundle.getString("logview.regex")), new TextField())[1];
    loggerRegex.textProperty().addListener((o, oldValue, newValue) -> {
      if (!isCompileable(newValue)) {
        loggerRegex.setText(oldValue);
      }
    });

    setRow(gridPane, rowIndex++, true,
        newPaddedSeparator(10, 0, 20, 0), newPaddedSeparator(10, 0, 20, 0));
    Button save = GsehenGuiElements.button(100);
    save.setText(mainBundle.getString("menu.file.save"));
    save.setOnAction(e -> {
      LocalDate startDate = nvl(startDatePicker.getValue(), LocalDate.ofEpochDay(0));
      LocalDate endDate = nvl(endDatePicker.getValue(), LocalDate.now().plusDays(1));
      LocalTime startTime = LocalTime.of(getStartTimeSpinnerValue(0),
          getStartTimeSpinnerValue(1), getStartTimeSpinnerValue(2));
      LocalTime endTime = LocalTime.of(getEndTimeSpinnerValue(0),
          getEndTimeSpinnerValue(1), getEndTimeSpinnerValue(2));
      DateTimeFormatter formatter = Configurator.newDateTimeFormatter();
      startDateTimeStr = LocalDateTime.of(startDate, startTime).format(formatter);
      endDateTimeStr = LocalDateTime.of(endDate, endTime).format(formatter);
      fromLevel = Level.parse(fromLevelChoiceBox.valueProperty().get()).intValue();
      toLevel = Level.parse(toLevelChoiceBox.valueProperty().get()).intValue();
      loggerFilter = Pattern.compile(loggerRegex.getText());
      logMessage(LOGGER, Level.INFO, "logview.filter.save", startDateTimeStr, endDateTimeStr,
          fromLevel, toLevel);
      useFilter = true;
      for (Iterator<LogEntry> iterator = data.iterator(); iterator.hasNext(); ) {
        LogEntry logEntry = iterator.next();
        if (filterReject(logEntry.date, logEntry.time, logEntry.level, logEntry.logger)) {
          iterator.remove();
        }
      }
    });
    setRow(gridPane, rowIndex++, true, null, save);

    LocalDate now = LocalDate.now();
    startDatePicker.setValue(now);
    endDatePicker.setValue(now);

    Scene scene = new Scene(gridPane, 400, 600);
    stage.setScene(scene);
    stage.centerOnScreen();
    stage.setOnHidden(we -> filterOptionsDialog = null);
    stage.show();
  }

  private boolean isCompileable(String regex) {
    try {
      Pattern.compile(regex);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  private Node newPaddedSeparator(double i, double j, double k, double l) {
    Separator separator = new Separator();
    separator.setPadding(new Insets(i, j, k, l));
    return separator;
  }

  @SuppressWarnings("unchecked")
  private int getStartTimeSpinnerValue(int i) {
    return ((Spinner<Integer>)startTimeSpinners[i]).valueProperty().get();
  }

  @SuppressWarnings("unchecked")
  private int getEndTimeSpinnerValue(int i) {
    return ((Spinner<Integer>)endTimeSpinners[i]).valueProperty().get();
  }

  private ChoiceBox<String> newLevelChoiceBox(Consumer<SingleSelectionModel<String>> initAction) {
    ChoiceBox<String> choiceBox = new ChoiceBox<>();
    for (String levelName : new String[] {
        "SEVERE", "WARNING", "INFO", "CONFIG", "FINE", "FINER", "FINEST"
    }) {
      if (Level.parse(levelName).intValue() >= LOG_EVENT_HANDLE_THRESHOLD) {
        choiceBox.getItems().add(levelName);
      }
    }
    initAction.accept(choiceBox.getSelectionModel());
    return choiceBox;
  }

  private Node newHeading(String text) {
    Label label = new Label(text);
    label.setFont(Font.font("Arial", FontWeight.BOLD, 16));
    return label;
  }

  private Label newLabel(String text) {
    return newLabel(text, null);
  }

  private Label newLabel(String text, Double top) {
    Label label = new Label(text + ":");
    label.setFont(Font.font("Arial", 14));
    if (top != null) {
      label.setPadding(new Insets(top, 0, 0, 0));
    }
    return label;
  }

  private Spinner<Integer> newSpinner(int maxValue, boolean initWithMax) {
    Spinner<Integer> ssp = new Spinner<>();
    ssp.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, maxValue,
        initWithMax ? maxValue : 0));
    ssp.setPrefWidth(60);
    return ssp;
  }

  private Node[] setRow(GridPane gridPane, int rowIndex, Node... nodes) {
    return setRow(gridPane, rowIndex, false, nodes);
  }

  private Node[] setRow(GridPane gridPane, int rowIndex, boolean spanLastOverTwo, Node... nodes) {
    for (int columnIndex = 0; columnIndex < nodes.length; columnIndex++) {
      if (nodes[columnIndex] == null) {
        continue;
      }
      gridPane.add(nodes[columnIndex], columnIndex, rowIndex,
          spanLastOverTwo && columnIndex == nodes.length - 1 ? 2 : 1, 1);
    }
    return nodes;
  }

  private <T> T nvl(T a, T b) {
    return a != null ? a : b;
  }

  /**
   * Handle a log event.
   *
   * @param logRecord the newly published log record
   */
  public void onLogRecordPublish(LogRecord logRecord) {
    if (logRecord == null || logRecord.getLevel().intValue() < LOG_EVENT_HANDLE_THRESHOLD) {
      return;
    }
    Date dateTime = new Date(logRecord.getMillis());
    LogEntry logEntry = newLogEntry(
        getFormat(0).format(dateTime),
        getFormat(1).format(dateTime),
        logRecord.getLevel() == null ? "null" : logRecord.getLevel().toString(),
        logRecord.getLoggerName(), logRecord.getMessage());
    if (logEntry != null) {
      getLogEntryBuffer().add(logEntry);
    }
  }

  private LogEntry newLogEntry(String date, String time, String level, String logger, String msg) {
    if (useFilter && filterReject(date, time, level, logger)) {
      return null;
    }
    return new LogEntry(date, time, level, logger, MessageUtil.localizedLogMessage(msg)
        .replace(Configurator.NEWLINE_REPLACE, "\n"));
  }

  private synchronized boolean filterReject(String date, String time, String level, String logger) {
    int levelIntValue = Level.parse(level).intValue();
    String dateTimeStr = date + " " + time;
    return levelIntValue < fromLevel || levelIntValue > toLevel
        || dateTimeStr.compareTo(startDateTimeStr) < 0
        || dateTimeStr.compareTo(endDateTimeStr) > 0
        || !loggerFilter.matcher(logger).find();
  }
}
