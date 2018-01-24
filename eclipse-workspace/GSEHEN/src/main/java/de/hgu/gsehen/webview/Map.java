package de.hgu.gsehen.webview;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Companion class to map view (HTML).
 *
 * @author AT
 */
public class Map {

  private static final String MAP_HTML = "map.html";

  /**
   * Loads the HTML (CSS, JS) code for the map view.
   *
   * @return a String containing the map view HTML code
   */
  @SuppressWarnings("checkstyle:rightcurly")
  public static String getMapHtml() {
    try {
      return new String(Files.readAllBytes(Paths.get(
          Map.class.getResource(MAP_HTML).toURI()
      )), "utf-8");
    }
    catch (Exception e) {
      throw new RuntimeException(MAP_HTML + " couldn't be loaded", e);
    }
  }
}
