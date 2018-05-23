package de.hgu.gsehen.gui.view;

import de.hgu.gsehen.Gsehen;
import de.hgu.gsehen.gui.GeoPolygon;
import de.hgu.gsehen.model.Drawable;
import de.hgu.gsehen.model.Farm;
import de.hgu.gsehen.model.Field;
import de.hgu.gsehen.model.Plot;
import java.util.TreeMap;
import java.util.logging.Logger;

import javafx.scene.web.WebView;

/**
 * Companion class to map view (HTML).
 *
 * @author AT
 */
@SuppressWarnings({"checkstyle:commentsindentation"})
public class Maps extends WebController {
  private static final Logger LOGGER = Logger.getLogger(Maps.class.getName());

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

  /**
   * Constructs a new map in the given WebView.
   *
   * @param webView the WebView where to load the map
   */
  public Maps(Gsehen application, WebView webView) {
    super(application, webView);
  }

  /**
   * Creates a new GeoPolygon; intended to be called from web JavaScript.
   *
   * @return a new, empty GeoPolygon
   */
  public GeoPolygon getEmptyPolygon() {
    return new GeoPolygon();
  }

  /**
   * Handles the event of a newly drawn polygon; intended to be called from web JavaScript.
   *
   * @param polygon a GeoPolygon, usually first created via
   *     de.hgu.gsehen.gui.view.Map.getEmptyPolygon() and with points then added as needed
   * @see de.hgu.gsehen.gui.view.Maps.getEmptyPolygon
   */
  @SuppressWarnings("checkstyle:rightcurly")
  public void polygonDrawn(GeoPolygon polygon, String typeKey) {
    // TODO move/"copy" types to maps.js ... use code like this old snippet from createTypesMap
//    typesMap.put(getBundleString("gui.view.Map.drawableType." + clazz.getSimpleName()),
//        clazz);
    java.util.Map<String, Class<?>> typesMap =
        createTypesMap(new Class[] { Farm.class, Field.class, Plot.class });
//    Alert alert = new Alert(AlertType.NONE, null, createButtonTypes(typesMap, ButtonType.CANCEL));
//    alert.setTitle("GSEHEN");
//    alert.setContentText(getBundleString("gui.view.Map.drawableTypeChoiceCaption"));
//    alert.showAndWait();
//    ButtonType dialogResult = alert.getResult();
//    if (dialogResult != ButtonType.CANCEL) {
//      TextInputDialog dialog = new TextInputDialog();
//      dialog.setTitle("GSEHEN");
//      dialog.setContentText(getBundleString("gui.view.Map.drawableTypeNameCaption"));
//      dialog.showAndWait();
//      String name = dialog.getResult();
    String name = "Unbenannt";
    try {
      if (name != null) {
//      Drawable object =
//          (Drawable)typesMap.get(dialogResult.getText()).newInstance();
        Drawable object =
            (Drawable)typesMap.get(typeKey).newInstance();
        object.setNameAndPolygon(name, polygon);
        application.objectAdded(object);
      }
    }
    catch (Exception exception) {
      // Java reflection stuff - exception should not happen, since all input comes from code
      LOGGER.info(exception.getMessage());
    }
//    }
  }

//  private ButtonType[] createButtonTypes(java.util.Map<String, Class<?>> typesMap,
//      ButtonType additionalButton) {
//    Set<String> keySet = typesMap.keySet(); // java.util.Set
//    ButtonType[] result = new ButtonType[keySet.size() + 1];
//    int i = 0;
//    for (String key : keySet) {
//      result[i++] = new ButtonType(key);
//    }
//    result[i] = additionalButton;
//    return result;
//  }

  private java.util.Map<String, Class<?>> createTypesMap(Class<?>[] clazzes) {
    java.util.Map<String, Class<?>> typesMap = new TreeMap<>();
    for (Class<?> clazz : clazzes) {
      typesMap.put(clazz.getSimpleName(), clazz);
    }
    return typesMap;
  }

//  private String getBundleString(String key) {
//    return application.getBundle().getString(key);
//  }
}
