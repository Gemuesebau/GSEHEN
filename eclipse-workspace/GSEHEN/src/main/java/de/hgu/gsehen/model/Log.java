package de.hgu.gsehen.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class Log {
  public String[] parts;
  public String datum;
  public String zeit;
  public String level;
  public String nachricht;

  static String Path = "C:\\Users\\jganin\\GsehenIrrigationManager.log";
  static FileReader fileReader;
  static BufferedReader fileStream;
  public static String Zeile;
  public static int counter;
  public static ArrayList<String> nachrichtliste = new ArrayList<>();
  public static ArrayList<String> datumliste = new ArrayList<>();
  public static ArrayList<String> levelliste = new ArrayList<>();
  public static ArrayList<String> zeitliste = new ArrayList<>();


  /**
   * Read the Log.
   * 
   */
  public Log(ArrayList<String> datumliste, ArrayList<String> zeitliste,
      ArrayList<String> levelliste, ArrayList<String> nachrichtliste) { 
    
    File file = new File(Path);
    
    {
      if (file.exists()) {
        try {
          fileReader = new FileReader(file);
        } catch (FileNotFoundException e) {
          e.printStackTrace();
          System.exit(0);
        }
        fileStream = new BufferedReader(fileReader);
        try {
          Zeile = fileStream.readLine();
          while (Zeile != null) {
            parts = Zeile.split(" ", 4);
            datumliste.add(parts[0]);
            zeitliste.add(parts[1]);
            levelliste.add(parts[2]);
            nachrichtliste.add(parts[3]);
            counter++;

            Zeile = fileStream.readLine();
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }
  


  public String getDatum() {
    return datumliste.stream().collect(Collectors.joining("\n"));
  }

  public void setDatum(String datum) {
    this.datum = datum;
  }


  public String getZeit() {
    return zeitliste.stream().collect(Collectors.joining("\n"));
  }

  public void setZeit(String zeit) {
    this.zeit = zeit;
  }
  
  public void setLevel(String level) {
    this.level = level;
  }

  public String getLevel() {
    return levelliste.stream().collect(Collectors.joining("\n"));
  }

  public void setNachricht(String nachricht) {
    this.nachricht = nachricht;
  }

  public String getNachricht() {
    return nachrichtliste.stream().collect(Collectors.joining("\n"));
  }

  public static int getCounter() {
    return counter;
  }

  @SuppressWarnings("static-access")
public void setCounter(int counter) {
    this.counter = counter;
  }
  
}






