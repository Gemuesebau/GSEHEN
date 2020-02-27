package de.hgu.gsehen.model;

import java.util.Arrays;

public class LogEntry {
  public String date;
  public String time;
  public String level;
  public String logger;
  public String message;

  @Override
  public String toString() {
    return Arrays.asList(date, time, level, logger, message).toString();
  }

  public LogEntry(String date, String time, String level, String logger, String message) {
    this.date = date;
    this.time = time;
    this.level = level;
    this.logger = logger;
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

  public String getLogger() {
    return logger;
  }

  public void setLogger(String logger) {
    this.logger = logger;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}