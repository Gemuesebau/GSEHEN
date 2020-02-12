package de.hgu.gsehen.model;

import java.util.Arrays;

public class LogEntry {
  public String date;
  public String time;
  public String level;
  public String message;

  @Override
  public String toString() {
    return Arrays.asList(date, time, level, message).toString();
  }
  
  /**
   * getter and setters for LogEntries.
   * @param date for date entry
   * @param time for time entry
   * @param level for level entry
   * @param message for message entry
   */
  public LogEntry(String date, String time, String level, String message) {
    this.date = date;
    this.time = time;
    this.level = level;
    this.message = message;
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

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}