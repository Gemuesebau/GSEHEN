package de.hgu.gsehen.gui.view;

import de.hgu.gsehen.Gsehen;
import de.hgu.gsehen.event.FarmDataChanged;
import de.hgu.gsehen.event.GsehenEventListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;




public class LogDataController implements GsehenEventListener<FarmDataChanged> {
  private Gsehen gsehenInstance;
  private BorderPane pane;

  // Path is hard coded
  static String Path = "C:\\Users\\jganin\\GsehenIrrigationManager.log"; 
  static FileReader fileReader; 
  static BufferedReader fileStream; 
  static String Zeile; 
 
  /**
   * Read logfile
   * @return 
   * 
   */
  public String LogReader() {

    File file = new File(Path); {
      if (file.exists()) {
        try {
          fileReader = new FileReader(file);
        } catch (FileNotFoundException e) {
          System.out.println("Datei könnte nicht geöffnet werden.");
          e.printStackTrace(); 
          System.exit(0);
        }
        fileStream = new BufferedReader(fileReader);
        try {
          Zeile = fileStream.readLine();
          while (Zeile != null) {
            System.out.println(Zeile);
            Zeile = fileStream.readLine();
            return Zeile;
          }
        } catch (IOException e) {
          System.out.println("Datei kann nicht gelesen werden.");
          e.printStackTrace();
        }
      } else {
        System.out.println("Datei wurde nicht gefunden.");
      }
    }
    return null;
  }

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
    TableView table = new TableView();
    pane.setTop(table);
    table.setMinHeight(pane.getHeight());
    addColumn(table);
    
  }


  private void addColumn(TableView table) {

    TableColumn date = new TableColumn<>("Datum");
    TableColumn time = new TableColumn<>("Zeit");
    TableColumn level = new TableColumn<>("Level");
    TableColumn massage = new TableColumn<>("Nachricht");

    table.getColumns().addAll(date, time, level, massage);
    date.setMinWidth(130);
    time.setMinWidth(130);
    level.setMinWidth(75);
    massage.setMinWidth(500);

  }


}