package de.hgu.gsehen.gui.view;

import de.hgu.gsehen.Gsehen;
import de.hgu.gsehen.gui.GeoPolygon;
import de.hgu.gsehen.model.Drawable;
import de.hgu.gsehen.model.Farm;
import de.hgu.gsehen.model.Field;
import de.hgu.gsehen.model.Plot;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;

import javafx.concurrent.Worker.State;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

/**
 * Companion class to map view (HTML).
 *
 * @author AT
 */
@SuppressWarnings({"checkstyle:commentsindentation"})
public class Map {
  private static final ResourceBundle mainBundle = ResourceBundle.getBundle("i18n.main",
      Locale.GERMAN);
  private static final Logger LOGGER = Logger.getLogger(Map.class.getName());

  private static final String MAP_HTML = "map.html";

  private WebEngine engine;
  private String loadWorkerSucceededScript;
  //private MainController mainController;
  private Gsehen application;

  /**
   * Constructs a new map in the given WebView.
   *
   * @param webView the WebView where to load the map
   */
  public Map(Gsehen application, WebView webView) {
    this.application = application;
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
  @SuppressWarnings("checkstyle:rightcurly")
  public void polygonDrawn(GeoPolygon polygon) {
    java.util.Map<String, Class<?>> typesMap =
        createTypesMap(new Class[] { Farm.class, Field.class, Plot.class });
    Alert alert = new Alert(AlertType.NONE, null, createButtonTypes(typesMap, ButtonType.CANCEL));
    alert.setTitle("GSEHEN");
    alert.setContentText(mainBundle.getString("gui.view.Map.drawableTypeChoiceCaption"));
    alert.showAndWait();
    ButtonType dialogResult = alert.getResult();
    if (dialogResult != ButtonType.CANCEL) {
      TextInputDialog dialog = new TextInputDialog();
      dialog.setTitle("GSEHEN");
      dialog.setContentText(mainBundle.getString("gui.view.Map.drawableTypeNameCaption"));
      dialog.showAndWait();
      String name = dialog.getResult();
      try {
        if (name != null) {
          Drawable object =
              (Drawable)typesMap.get(dialogResult.getText()).newInstance();
          object.setNameAndPolygon(name, polygon);
          application.objectAdded(object);
        }
      }
      catch (Exception exception) {
        // should not happen, since all input comes from code
        LOGGER.info(exception.getMessage());
      }
    }
  }

  private ButtonType[] createButtonTypes(java.util.Map<String, Class<?>> typesMap,
      ButtonType additionalButton) {
    Set<String> keySet = typesMap.keySet();
    ButtonType[] result = new ButtonType[keySet.size() + 1];
    int i = 0;
    for (String key : keySet) {
      result[i++] = new ButtonType(key);
    }
    result[i] = additionalButton;
    return result;
  }

  private java.util.Map<String, Class<?>> createTypesMap(Class<?>[] clazzes) {
    java.util.Map<String, Class<?>> typesMap = new TreeMap<>();
    for (Class<?> clazz : clazzes) {
      typesMap.put(mainBundle.getString("gui.view.Map.drawableType." + clazz.getSimpleName()),
          clazz);
    }
    return typesMap;
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

//  public void setMainController(MainController mainController) {
//    this.mainController = mainController;
//  }
}
