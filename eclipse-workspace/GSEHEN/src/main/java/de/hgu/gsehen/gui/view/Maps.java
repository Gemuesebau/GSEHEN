package de.hgu.gsehen.gui.view;

import static de.hgu.gsehen.util.CollectionUtil.simpleClassMap;

import de.hgu.gsehen.Gsehen;
import de.hgu.gsehen.event.FarmDataChanged;
import de.hgu.gsehen.event.GsehenEventListener;
import de.hgu.gsehen.gui.GeoPolygon;
import de.hgu.gsehen.gui.GsehenTreeTable;
import de.hgu.gsehen.model.Drawable;
import de.hgu.gsehen.model.Farm;
import de.hgu.gsehen.model.Field;
import de.hgu.gsehen.model.Plot;
import de.hgu.gsehen.util.Pair;
import java.util.logging.Logger;
import javafx.scene.web.WebView;

/**
 * Companion class to map view (HTML).
 *
 * @author AT
 */
@SuppressWarnings({"checkstyle:commentsindentation"})
public class Maps extends FarmDataController implements GsehenEventListener<FarmDataChanged> {
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

  private java.util.Map<String, Class<?>> typesMap =
      simpleClassMap(new Class[] {Farm.class, Field.class, Plot.class});

  /**
   * Creates a new GeoPolygon; intended to be called from web JavaScript.
   *
   * @return a new, empty GeoPolygon
   */
  public GeoPolygon getEmptyPolygon() {
    return new GeoPolygon();
  }

  // ------------------------------- these should use getLastViewPort (???)
  // -------------------------------
  public double getCenterLat() {
    return 52.266344; // TODO get from current viewport, or, at least initially, from (user's)
                      // settings
  }

  public double getCenterLng() {
    return 10.519835; // TODO get from current viewport, or, at least initially, from (user's)
                      // settings
  }

  public double getZoom() {
    return 16; // TODO derive from current viewport, or, at least initially, from (user's) settings
  }
  // ----------------------------------------------------------------------------------------------

  /**
   * Returns the object types with their localized names; intended to be called from web JavaScript.
   *
   * @return an array of pairs, each of which contains the type key and its localized name
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public Pair<String>[] getLocalizedTypes() {
    Pair<String>[] result = new Pair[3];
    int[] i = new int[] {0};
    typesMap.keySet().forEach(type -> {
      result[i[0]++] =
          new Pair(type, application.getBundle().getString("gui.view.Map.drawableType." + type));
    });
    return result;
  }

  /**
   * Handles the event of a newly drawn polygon; intended to be called from web JavaScript.
   *
   * @param polygon a GeoPolygon, usually first created via
   *        de.hgu.gsehen.gui.view.Map.getEmptyPolygon() and with points then added as needed
   * @see de.hgu.gsehen.gui.view.Maps.getEmptyPolygon
   */
  @SuppressWarnings("checkstyle:rightcurly")
  public void polygonDrawn(GeoPolygon polygon, String typeKey) {
    String name = "Unbenannt";
    try {
      if (name != null) {
        Drawable object = (Drawable) typesMap.get(typeKey).newInstance();
        object.setNameAndPolygon(name, polygon);
        application.objectAdded(object, getClass());
        GsehenTreeTable.getInstance().fillTreeView(null);
      }
    } catch (Exception exception) {
      // Java reflection stuff - exception should not happen, since all input comes from code
      LOGGER.info(exception.getMessage());
    }
  }
}
