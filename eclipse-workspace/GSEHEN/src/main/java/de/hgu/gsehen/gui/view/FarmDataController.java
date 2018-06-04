package de.hgu.gsehen.gui.view;

import de.hgu.gsehen.Gsehen;
import de.hgu.gsehen.event.FarmDataChanged;
import de.hgu.gsehen.event.GsehenEventListener;
import de.hgu.gsehen.gui.GeoPoint;
import de.hgu.gsehen.model.Drawable;
import de.hgu.gsehen.model.DrawableParent;
import de.hgu.gsehen.model.Farm;
import de.hgu.gsehen.util.Pair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import javafx.scene.web.WebView;

public abstract class FarmDataController extends WebController
    implements GsehenEventListener<FarmDataChanged> {
  private Gsehen gsehenInstance;

  {
    gsehenInstance = Gsehen.getInstance();
    gsehenInstance.registerForEvent(FarmDataChanged.class, this);
  }

  private Drawable[] drawables;
  private Pair<GeoPoint> lastViewport = new Pair<>(new GeoPoint(-90, -180), new GeoPoint(90, 180));

  /**
   * Constructs a new farm data controller associated with the given WebView.
   *
   * @param webView the associated WebView
   */
  public FarmDataController(Gsehen application, WebView webView) {
    super(application, webView);
  }

  @Override
  public void handle(FarmDataChanged event) {
    List<Farm> farms = event.getFarms();
    Drawable[] farmsArray = new Drawable[farms.size()];
    int i = 0;
    for (Farm farm : farms) {
      farmsArray[i++] = farm;
    }
    drawables = flattenDrawables(farmsArray);
    Pair<GeoPoint> viewport = event.getViewport();
    lastViewport = viewport != null ? viewport : findBounds(drawables);
    getLogger().log(Level.INFO, "About to reload " + this.getClass().getSimpleName() + " web view,"
        + " with drawables=" + Arrays.asList(drawables)
        + " and lastViewport=" + lastViewport);
    reload();
  }

  private Drawable[] flattenDrawables(Drawable... drawables) {
    List<Drawable> result = new ArrayList<>();
    flattenDrawablesImpl(result, drawables);
    return result.toArray(new Drawable[0]);
  }

  private void flattenDrawablesImpl(List<Drawable> result, Drawable... drawables) {
    for (Drawable drawable : drawables) {
      result.add(drawable);
      if (drawable instanceof DrawableParent) {
        ((DrawableParent) drawable)
            .forAllChildDrawables(drawableChild -> flattenDrawablesImpl(result, drawableChild));
      }
    }
  }

  private Pair<GeoPoint> findBounds(Drawable[] drawables) {
    double minX =  180;
    double minY =  90;
    double maxX = -180;
    double maxY = -90;
    for (Drawable drawable : drawables) {
      try {
        double drawableMinX = drawable.getPolygon().getMinX();
        if (drawableMinX < minX) {
          minX = drawableMinX;
        }
      } catch (IllegalArgumentException e) {
        // just skip it
      }
      try {
        double drawableMinY = drawable.getPolygon().getMinY();
        if (drawableMinY < minY) {
          minY = drawableMinY;
        }
      } catch (IllegalArgumentException e) {
        // just skip it
      }
      try {
        double drawableMaxX = drawable.getPolygon().getMaxX();
        if (drawableMaxX > maxX) {
          maxX = drawableMaxX;
        }
      } catch (IllegalArgumentException e) {
        // just skip it
      }
      try {
        double drawableMaxY = drawable.getPolygon().getMaxY();
        if (drawableMaxY > maxY) {
          maxY = drawableMaxY;
        }
      } catch (IllegalArgumentException e) {
        // just skip it
      }
    }
    return new Pair<>(new GeoPoint(minY, minX), new GeoPoint(maxY, maxX));
  }

  public Drawable[] getDrawables() {
    //getLogger().log(Level.INFO, "getDrawables=" + Arrays.asList(drawables));
    return drawables;
  }

  public Pair<GeoPoint> getLastViewport() {
    return lastViewport;
  }

  public void setLastViewport(double north, double south, double east, double west) {
    lastViewport = new Pair<>(new GeoPoint(south, west), new GeoPoint(north, east));
  }

  public void setViewport(Pair<GeoPoint> lastViewport) {
    this.lastViewport = lastViewport;
  }

  /**
   * Determines the map or farm view polygon color for the given type (object).
   *
   * @param typeObject a "Drawable", or a String
   * @return the appropriate stroke and fill color for the given type
   */
  public String getFillStyle(Object typeObject) {
    String type = (typeObject instanceof String)
        ? ((String) typeObject)
        : typeObject.getClass().getSimpleName();
    switch (type) {
      case "Farm":
        return "black";
      case "Field":
        return "blue";
      case "Plot":
        // FIXME: depending on water balance! (according to most recent calculation)
        return "orange";
      default:
        return "white";
    }
  }
}
