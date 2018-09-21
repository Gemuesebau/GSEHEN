package de.hgu.gsehen.gui.view;

import de.hgu.gsehen.Gsehen;
import de.hgu.gsehen.evapotranspiration.DayData;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javafx.beans.value.ChangeListener;
import javafx.scene.control.SplitPane;
import javafx.scene.web.WebView;

public abstract class FarmDataController extends WebController {
  private Gsehen gsehenInstance;
  private Map<Class<? extends GsehenEvent>, Class<? extends 
      GsehenEventListener<? extends GsehenEvent>>> eventListeners = new HashMap<>();

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
            redrawOrReload(event);
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

  private void redrawOrReload(Object event) {
    if (!isLoaded()) {
      logAboutToReload(event.getClass().getSimpleName(), "reload");
      reload();
    } else {
      logAboutToReload(event.getClass().getSimpleName(), "redraw");
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
        newValue) -> redrawOrReload(observable);
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
        Double waterLevel = 0.0;
        if (plot.getRecommendedAction() != null) {
          waterLevel = plot.getRecommendedAction().getAvailableWater();
        }
        Double availableSoilWater = 0.0;

        if (plot.getWaterBalance() != null) {
          final List<DayData> dailyBalances = plot.getWaterBalance().getDailyBalances();
          if (!dailyBalances.isEmpty()) {
            int waterBalance = dailyBalances.size() - 1;
            availableSoilWater = dailyBalances.get(waterBalance).getCurrentAvailableSoilWater() 
                * 1.1;
          }
        }

        // TODO: Prozentwerte anpassen!
        String color = "white";
        if (waterLevel != null) {
          if (100 / (availableSoilWater / 1.1) * waterLevel < 25) {
            color = "red";
          } else if (100 / (availableSoilWater / 1.1) * waterLevel >= 25) {
            color = "purple";
          } else if (100 / (availableSoilWater / 1.1) * waterLevel >= 75) {
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
