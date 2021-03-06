package de.hgu.gsehen.gui.view;

import de.hgu.gsehen.Gsehen;
import de.hgu.gsehen.event.FarmDataChanged;
import de.hgu.gsehen.model.Drawable;
import de.hgu.gsehen.util.MessageUtil;
import de.hgu.gsehen.util.Pair;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.web.WebView;

/**
 * Companion class to map view (HTML).
 *
 * @author AT
 */
@SuppressWarnings({"checkstyle:commentsindentation"})
public class Maps extends FarmDataController {
  private static final Logger LOGGER = Logger.getLogger(Maps.class.getName());
  private static final String MAPS_HTML_URL = "https://gemuesebau.github.io/GSEHEN/"
      + "eclipse-workspace/GSEHEN/src/main/resources/de/hgu/gsehen/gui/view/maps.html";

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

  /**
   * Constructs a new map in the given WebView.
   *
   * @param application the Gsehen application singleton reference
   * @param webView the WebView where to load the map
   */
  public Maps(Gsehen application, WebView webView) {
    super(application, webView);
  }

  @Override
  public void reload() {
    loadWorkerSucceededScript = getCompanionFileContents(".js");
    engine.load(MAPS_HTML_URL);
  }

  /**
   * Creates a new Drawable; intended to be called from web JavaScript.
   *
   * @param typeKey the key of the actual type of drawable to be created
   * @return a new drawable of the given type, with an empty GeoPolygon 
   * @throws IllegalAccessException via Class.newInstance
   * @throws InstantiationException via Class.newInstance
   */
  public Drawable getDrawableWithEmptyPolygon(String typeKey)
      throws InstantiationException, IllegalAccessException {
    return application.getDrawableWithEmptyPolygon(typeKey);
  }

  /**
   * Returns the object types with their localized names; intended to be called from web JavaScript.
   *
   * @return an array of pairs, each of which contains the type key and its localized name
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public Pair<String>[] getLocalizedTypes() {
    Pair<String>[] result = new Pair[3];
    int[] i = new int[] { 0 };
    application.getTypesMap().keySet().forEach(type -> {
      result[i[0]++] =
          new Pair(type, application.getBundle().getString("gui.view.Map.drawableType." + type));
    });
    return result;
  }

  /**
   * Handles the event of a newly drawn polygon; intended to be called from web JavaScript.
   *
   * @param drawable a Drawable (containing a GeoPolygon), usually first created via
   *        de.hgu.gsehen.gui.view.Map.getDrawableWithEmptyPolygon() and with points added as needed
   * @see de.hgu.gsehen.gui.view.Maps#getDrawableWithEmptyPolygon(String)
   */
  public void drawableDone(Drawable drawable) {
    application.drawableAdded(drawable, getEventListenerClass(FarmDataChanged.class));
  }

  /**
   * Handles the event of a change in the map's bounds (viewport).
   *
   * @param north northern latitude
   * @param south southern latitude
   * @param east eastern longitude
   * @param west western longitude
   */
  public void mapBoundsChanged(double north, double south, double east, double west) {
    MessageUtil.logMessage(getLogger(), Level.INFO, "map.bounds.changed", north, south, east, west);
    setLastViewport(north, south, east, west);
  }
}
