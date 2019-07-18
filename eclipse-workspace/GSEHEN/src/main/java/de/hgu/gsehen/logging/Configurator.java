package de.hgu.gsehen.logging;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;

public class Configurator {

  // MUST be kept equivalent to the java.util.logging.FileHandler.pattern in logging.properties!
  public static final String LOG_FILE_NAME =
      System.getProperty("user.home") + "/.gsehenIrrigationManager/logs/"
          + "GsehenIrrigationManager.log";

  @SuppressWarnings({"checkstyle:javadocmethod", "checkstyle:rightcurly"})
  public Configurator() {
    new File(LOG_FILE_NAME).getParentFile().mkdirs();
    try (final InputStream inputstream = getClass().getResourceAsStream("logging.properties")) {
      LogManager.getLogManager().readConfiguration(inputstream);
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }
}
