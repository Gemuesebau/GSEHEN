package de.hgu.gsehen.gui.view;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Logger;
import javafx.concurrent.Worker.State;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

/**
 * Companion class to map view (HTML).
 *
 * @author AT
 */
public class Map {

  private static final Logger LOGGER = Logger.getLogger(Map.class.getName());

  private static final String MAP_HTML = "map.html";

  private WebEngine engine;
  private String loadWorkerSucceededScript;

  /**
   * Constructs a new map in the given WebView.
   *
   * @param webView the WebView where to load the map
   */
  public Map(WebView webView) {
    engine = webView.getEngine();
    engine.setOnAlert(event -> alert(event.getData()));
    engine.getLoadWorker().stateProperty().addListener((ov, oldState, newState) -> {
      if (newState == State.SUCCEEDED) {
        engine.executeScript(loadWorkerSucceededScript);
      }
    });
  }

  private void alert(String data) {
    LOGGER.info(data);
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
    loadWorkerSucceededScript = "initialize({"
        + " center: new google.maps.LatLng(52.266344, 10.519835),"
        + " zoom: 16, fullscreenControl: false" + " }); draw()";
    engine.loadContent(getMapHtml());
  }
}
