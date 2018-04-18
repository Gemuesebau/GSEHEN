package de.hgu.gsehen.gui.view;

import de.hgu.gsehen.gui.GeoPolygon;
import de.hgu.gsehen.gui.controller.MainController;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Logger;

import javafx.concurrent.Worker.State;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

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
        JSObject win = (JSObject)engine.executeScript("window");
        win.setMember("gsehenGuiViewMap", this);
        engine.executeScript(loadWorkerSucceededScript);
      }
    });
  }

  private void alert(String data) {
    LOGGER.info(data);
  }

  public GeoPolygon getEmptyPolygon() {
    return new GeoPolygon();
  }

  /**
   * Für den Aufruf seitens JavaScript (map.html) gedacht.
   *
   * @param polygon ein GeoPolygon, vorzugsweise mittels
   *     de.hgu.gsehen.gui.view.Map.getEmptyPolygon() erbaut und dann befüllt
   * @see de.hgu.gsehen.gui.view.Map.getEmptyPolygon
   */
  public void polygonDrawn(GeoPolygon polygon) {
    Alert alert = new Alert(AlertType.CONFIRMATION, "Bitte Objekttyp angeben",
        new ButtonType("Farm"), new ButtonType("Feld"), new ButtonType("Plot"),
        ButtonType.CANCEL);
    alert.showAndWait();
    if (alert.getResult() != ButtonType.CANCEL) {
      MainController.objectAdded(alert.getResult(), polygon);
    }
    LOGGER.info(String.valueOf(polygon.getGeoPoints()));
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
        + " zoom: 16, fullscreenControl: false" + " }); draw(); captureDrawing()";
    engine.loadContent(getMapHtml());
  }
}
