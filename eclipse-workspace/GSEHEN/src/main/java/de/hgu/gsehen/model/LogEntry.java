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

  public String getDate() {
    return date;
  }

  public void setDate(String date) {
    this.date = date;
  }

  public String getTime() {
    return time;
  }

  public void setTime(String time) {
    this.time = time;
  }

  public String getLevel() {
    return level;
  }

  public void setLevel(String level) {
    this.level = level;
  }

  public String getMassage() {
    return massage;
  }

  public void setMassage(String massage) {
    this.massage = massage;
  }
}