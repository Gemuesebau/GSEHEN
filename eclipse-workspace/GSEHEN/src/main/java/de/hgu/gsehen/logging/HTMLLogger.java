package de.hgu.gsehen.logging;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

@SuppressWarnings({"checkstyle:abbreviationaswordinname"})
public class HTMLLogger {
  private Logger delegate;

  private Pattern removeNewlinesEtc = Pattern.compile("\\s+");
  private static final String WHITESPACE_REPLACE = " ";

  public HTMLLogger(String name) {
    delegate = Logger.getLogger(name);
  }

  public void log(Level level, String message, Throwable throwable) {
    delegate.log(level, removeNewlinesEtc.matcher(message).replaceAll(WHITESPACE_REPLACE), throwable);
  }

  public void info(String message) {
    log(Level.INFO, message, null);
  }
}
