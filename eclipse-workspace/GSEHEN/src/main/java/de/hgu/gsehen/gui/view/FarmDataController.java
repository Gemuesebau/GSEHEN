package de.hgu.gsehen.gui.view;

import de.hgu.gsehen.Gsehen;
import de.hgu.gsehen.event.DrawableFilterChanged;
import de.hgu.gsehen.event.DrawableSelected;
import de.hgu.gsehen.event.FarmDataChanged;
import de.hgu.gsehen.event.GsehenEvent;
import de.hgu.gsehen.event.GsehenEventListener;
import de.hgu.gsehen.gui.GeoPoint;
import de.hgu.gsehen.model.Drawable;
import de.hgu.gsehen.model.DrawableParent;
import de.hgu.gsehen.model.Farm;
import de.hgu.gsehen.model.Plot;
import de.hgu.gsehen.util.Pair;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Predicate;
import java.util.logging.Level;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.SplitPane;
import javafx.scene.web.WebView;

public abstract class FarmDataController extends WebController {
  private static final String GOOGLE_MAPS_API_KEY_PROPKEY = "google.maps.api.key";
  private Gsehen gsehenInstance;
  private Map<Class<? extends GsehenEvent>, Class<? extends 
      GsehenEventListener<? extends GsehenEvent>>> eventListeners = new HashMap<>();
  private Predicate<Drawable> currentFilter = drawable -> true;

  private <T extends GsehenEvent> void setEventListenerClass(Class<T> eventClass,
      Class<? extends GsehenEventListener<T>> eventListenerClass) {
    eventListeners.put(eventClass, eventListenerClass);
  }

  @SuppressWarnings("unchecked")
  protected <T extends GsehenEvent> Class<? extends GsehenEventListener<T>> getEventListenerClass(
      Class<T> eventClass) {
    return (Class<? extends GsehenEventListener<T>>) eventListeners.get(eventClass);
  }

  {
    gsehenInstance = Gsehen.getInstance();
    gsehenInstance.registerForEvent(FarmDataChanged.class,
        new GsehenEventListener<FarmDataChanged>() {
          {
            setEventListenerClass(FarmDataChanged.class, getClass());
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
            redrawOrReload(event.getClass().getSimpleName());
          }
        });
    gsehenInstance.registerForEvent(DrawableSelected.class,
        new GsehenEventListener<DrawableSelected>() {
          {
            setEventListenerClass(DrawableSelected.class, getClass());
          }

          @Override
          public void handle(DrawableSelected event) {
            Drawable drawable = event.getSubject();
            Pair<GeoPoint> viewport = event.getViewport();
            lastViewport = viewport != null ? viewport : findBounds(new Drawable[] { drawable });
            refocusOrReload(event);
          }
        });
    gsehenInstance.registerForEvent(DrawableFilterChanged.class,
        new GsehenEventListener<DrawableFilterChanged>() {
          {
            setEventListenerClass(DrawableFilterChanged.class, getClass());
          }

          @Override
          public void handle(DrawableFilterChanged event) {
            currentFilter = event.getFilter();
            lastViewport = findBounds(drawables);
            redrawOrReload(event.getClass().getSimpleName());
          }
        });
  }

  public Pair<GeoPoint> getLastViewport() {
    return lastViewport;
  }

  public void reloadWithViewport(Pair<GeoPoint> lastViewport) {
    this.lastViewport = lastViewport;
    reload();
  }

  protected void setLastViewport(double north, double south, double east, double west) {
    lastViewport = new Pair<>(new GeoPoint(south, west), new GeoPoint(north, east));
  }

  private void redraw() {
    engine.executeScript("redraw();");
  }

  private void redrawOrReload(String reason) {
    if (!isLoaded()) {
      logAboutToReload(reason, "reload");
      reload();
    } else {
      logAboutToReload(reason, "redraw");
      redraw();
    }
  }

  private void refocusOrReload(Object event) {
    if (!isLoaded()) {
      logAboutToReload(event.getClass().getSimpleName(), "reload");
      reload();
    } else {
      logAboutToReload(event.getClass().getSimpleName(), "refocus");
      engine.executeScript("if ((typeof clearAndSetViewportByController)==\"function\") "
          + "clearAndSetViewportByController();");
    }
  }

  private void logAboutToReload(String reason, String verb) {
    getLogger().log(Level.INFO,
        "About to " + verb + " " + this.getClass().getSimpleName() + " web view due to " + reason
            + ", with drawables=" + Arrays.asList(drawables) + " and lastViewport=" + lastViewport);
  }

  private Drawable[] drawables;
  private Pair<GeoPoint> lastViewport = new Pair<>(new GeoPoint(-90, -180), new GeoPoint(90, 180));

  /**
   * Constructs a new farm data controller associated with the given WebView.
   *
   * @param webView
   *          the associated WebView
   */
  public FarmDataController(Gsehen application, WebView webView) {
    super(application, webView);

    ChangeListener<Number> splitPaneWidthHeightDividerPositionListener = (observable, oldValue,
        newValue) -> redrawOrReload(observable.getClass().getSimpleName());
    SplitPane mainSplitPane = getMainSplitPane();
    mainSplitPane.widthProperty().addListener(splitPaneWidthHeightDividerPositionListener);
    mainSplitPane.heightProperty().addListener(splitPaneWidthHeightDividerPositionListener);
    mainSplitPane.getDividers().get(0).positionProperty()
        .addListener(splitPaneWidthHeightDividerPositionListener);
  }

  public SplitPane getMainSplitPane() {
    return application.getMainSplitPane();
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
    double minX = 180;
    double minY = 90;
    double maxX = -180;
    double maxY = -90;
    for (Drawable drawable : drawables) {
      if (!passesFilter(drawable)) {
        continue;
      }
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
    return drawables;
  }

  public boolean passesFilter(Drawable drawable) {
    return currentFilter.test(drawable);
  }

  /**
   * Determines the Google Maps API Key.
   *
   * @return the API key
   */
  public String getGoogleMapsApiKey() {
    Reader reader = null;
    try {
      reader = new InputStreamReader(
          new FileInputStream(
              System.getProperty("user.home")
              + "/.gsehenIrrigationManager/properties/.GSEHEN.build.properties"), "ISO-8859-1");
    } catch (Exception e2) {
      throw new RuntimeException(
          "External properties not found", e2); // TODO show apikey.html (and import user's key)
    }
    Properties properties = new Properties();
    try {
      properties.load(reader);
    } catch (Exception e) {
      throw new RuntimeException("Properties not readable", e); // TODO really use properties?
    }
    return properties.getProperty(GOOGLE_MAPS_API_KEY_PROPKEY);
  }

  /**
   * Determines the map or farm view polygon color for the given type (object).
   *
   * @param typeObject
   *          a "Drawable", or a String
   * @return the appropriate stroke and fill color for the given type
   */
  public String getFillStyle(Object typeObject) {
    String type = (typeObject instanceof String) ? ((String) typeObject)
        : typeObject.getClass().getSimpleName();
    switch (type) {
      case "Farm":
        return "black";
      case "Field":
        return "green";
      case "Plot":
        Plot plot = (Plot) typeObject;
        int daysToIrrigation = 0;
        if (plot.getRecommendedAction() != null 
            && plot.getRecommendedAction().getProjectedDaysToIrrigation() != null) {
          daysToIrrigation = plot.getRecommendedAction().getProjectedDaysToIrrigation();
        }
        
        String color = "white";
        if (plot.getRecommendedAction() != null) {
          if (daysToIrrigation == 0) {
            color = "red";
          } else if (daysToIrrigation == 1) {
            color = "purple";
          } else {
            color = "blue";
          }
        }
        
        return color;

      default:
        return "white";
    }
  }

  public Object runJavaScript(String javaScript) {
    return engine.executeScript(javaScript);
  }
}
