package de.hgu.gsehen.model;

public class LogEntry {
  public String date;
  public String time;
  public String level;
  public String massage;

  
  /**
   * getter and setters for LogEntries.
   * @param date for date entry
   * @param time for time entry
   * @param level for level entry
   * @param massage for massage entry
   */
  public LogEntry(String date, String time, String level, String massage) {
    this.date = date;
    this.time = time;
    this.level = level;
    this.massage = massage;
  }

  public String getDatum() {
    return date;
  }

  public void setDatum(String date) {
    this.date = date;
  }

  public String getZeit() {
    return time;
  }

  public void setZeit(String time) {
    this.time = time;
  }

  public String getLevel() {
    return level;
  }

  public void setLevel(String level) {
    this.level = level;
  }

  public String getNachricht() {
    return massage;
  }

  public void setNachricht(String nachricht) {
    this.massage = nachricht;
  }
}