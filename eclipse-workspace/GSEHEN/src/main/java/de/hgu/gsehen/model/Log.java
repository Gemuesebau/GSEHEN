package de.hgu.gsehen.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Log {

  public String datum;
  public String zeit;
  public String level;
  public String nachricht;
  static String Path = "C:\\Users\\jganin\\GsehenIrrigationManager.log";
  static FileReader fileReader;
  static BufferedReader fileStream;
  static String Zeile;

  /**
   * Read the Log.
   */
  public Log() { 
    String[] parts = null;
    File file = new File(Path);
    {
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
            for (int i = 0; i < Zeile.length();) {
              parts = Zeile.split(" ", 4);
              datum = parts[0];
              zeit = parts[1];
              level = parts[2];
              nachricht = parts[3];
              System.out.println(datum + " datum");
              System.out.println(zeit + " zeit");
              System.out.println(level + " levle");
              System.out.println(nachricht + " nachricht");
              Zeile = fileStream.readLine();
              return;
            }
            Zeile = fileStream.readLine();
            System.out.println(datum + "dasdadadadadadada");
          }
        } catch (IOException e) {
          System.out.println("Datei kann nicht gelesen werden.");
          e.printStackTrace();
        }
      } else {
        System.out.println("Datei wurde nicht gefunden.");
      }
    }
  }


  public String getDatum() {
    return datum;
  }



  public void setDatum(String datum) {
    this.datum = datum;
  }


  public String getZeit() {
    return zeit;
  }



  public void setZeit(String zeit) {
    this.zeit = zeit;
  }



  public String getLevel() {
    return level;
  }



  public void setLevel(String level) {
    this.level = level;
  }



  public String getNachricht() {
    return nachricht;
  }



  public void setNachricht(String nachricht) {
    this.nachricht = nachricht;
  }
}



