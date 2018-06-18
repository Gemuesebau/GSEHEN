package de.hgu.gsehen.gui.view;

import de.hgu.gsehen.Gsehen;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import javafx.scene.layout.BorderPane;




public class LogDataController {
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


}