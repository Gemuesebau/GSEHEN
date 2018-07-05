
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
import java.util.ArrayList;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;

public class LogDataController implements GsehenEventListener<FarmDataChanged> {
  private static final Logger LOGGER = Logger.getLogger(Gsehen.class.getName());
  private Gsehen gsehenInstance;
  private BorderPane pane;
  private ObservableList<LogEntry> data;

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
   * @param path from the log file.
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
   * generate TableView and TableColumns also fill TableCells.
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

    pane.setTop(tableView);
    tableView.setMinHeight(pane.getHeight());

    return tableView;
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
