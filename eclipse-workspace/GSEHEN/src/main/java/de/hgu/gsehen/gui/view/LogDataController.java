package de.hgu.gsehen.gui.view;

import de.hgu.gsehen.Gsehen;
import de.hgu.gsehen.event.FarmDataChanged;
import de.hgu.gsehen.event.GsehenEventListener;
import de.hgu.gsehen.model.Log;
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
  private Gsehen gsehenInstance;
  private BorderPane pane;


  {
    gsehenInstance = Gsehen.getInstance();
    gsehenInstance.registerForEvent(FarmDataChanged.class, this);
  }

  /**
   * Constructs a new plot data controller associated with the given BorderPane.
   *
   * @param pane - the associated BorderPane.
   */
  public LogDataController(Gsehen application, BorderPane pane) {
    this.gsehenInstance = application;
    this.pane = pane; 
  }

  
  @Override
public void handle(FarmDataChanged event) {

    pane.setVisible(true);
    createContent();
  }
 
  /**
   * TODO.
   * @return
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
public Parent createContent() {

    ObservableList<Log> data = FXCollections.observableArrayList();

    Log log = new Log(Log.datumliste, Log.zeitliste, Log.levelliste, Log.nachrichtliste);
    data.add(log);
    
    TableColumn dateCol = new TableColumn("Datum");
    dateCol.setSortable(false);
    dateCol.setCellValueFactory(new PropertyValueFactory<Log, String>("datum"));
    TableColumn timeCol = new TableColumn("Zeit");
    timeCol.setSortable(false);
    timeCol.setCellValueFactory(new PropertyValueFactory<Log, String>("zeit"));
    TableColumn levelCol = new TableColumn("Level");
    levelCol.setSortable(false);
    levelCol.setCellValueFactory(new PropertyValueFactory<Log, String>("level"));
    TableColumn massageCol = new TableColumn("Nachricht");
    massageCol.setSortable(false);
    massageCol.setCellValueFactory(new PropertyValueFactory<Log, String>("nachricht"));
    TableView tableView = new TableView();
    tableView.getColumns().addAll(dateCol, timeCol, levelCol, massageCol);
    tableView.getItems().setAll(data);



    EventHandler<? super MouseEvent> handler = event -> {
      
    };

    tableView.addEventHandler(MouseEvent.MOUSE_CLICKED, handler);

    pane.setTop(tableView);
    tableView.setMinHeight(pane.getHeight());

    return tableView;
  
  }
}