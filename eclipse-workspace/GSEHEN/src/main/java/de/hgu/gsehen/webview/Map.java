package de.hgu.gsehen.webview;

import java.nio.file.Files;
import java.nio.file.Paths;
import javafx.concurrent.Worker.State;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

/**
 * Companion class to map view (HTML).
 *
 * @author AT
 */
public class Map {

  private static final String MAP_HTML = "map.html";
  private static final String WEB_VIEW_ID = "#webView";
  private Scene scene;

  public Map(Scene scene) {
    this.scene = scene;
  }

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

  /**
   * Reload map view HTML, and re-initialize JavaScript.
   */
  public void reload() {
    WebEngine engine = ((WebView) scene.lookup(WEB_VIEW_ID)).getEngine();
    engine.getLoadWorker().stateProperty().addListener((ov, oldState, newState) -> {
      if (newState == State.SUCCEEDED) {
        engine
            .executeScript("initialize({" + " center: new google.maps.LatLng(52.266344, 10.519835),"
                + " zoom: 16, fullscreenControl: false" + " }); draw()");
      }
    });
    engine.loadContent(getMapHtml());
  }
}
