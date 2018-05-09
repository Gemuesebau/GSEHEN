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
import java.util.List;
import java.util.logging.Logger;
import javafx.scene.web.WebView;

public class Farms extends WebController implements GsehenEventListener<FarmDataChanged> {
  private Gsehen gsehenInstance;

  {
    gsehenInstance = Gsehen.getInstance();
    gsehenInstance.registerForEvent(FarmDataChanged.class, this);
  }

  private static final Logger LOGGER = Logger.getLogger(Farms.class.getName());
  private Drawable[] drawables;
  private Pair<GeoPoint> lastViewPort = new Pair<>(new GeoPoint(-90, -180), new GeoPoint(90, 180));

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

  /**
   * Constructs a new farm view in the given WebView.
   *
   * @param webView the WebView where to show the farms
   */
  public Farms(Gsehen application, WebView webView) {
    super(application, webView);
  }

  //canvasElement.width
  public double getCanvasWidth() {
    return (double) engine.executeScript("innerWidth * 0.96");
  }

  //canvasElement.height
  public double getCanvasHeight() {
    return (double) engine.executeScript("innerHeight * 0.96");
  }

  // TODO check if suited for usual farm sizes, or automate to initially show all farms
  //   (bounding box)
  public double getKfactor() {
    return 5000;
  }

  // BS, DE TODO generalize ....
  public double getTranslateOffsetKfactorX() {
    //return -0.007;
    return -0.7;
  }

  // BS, DE TODO generalize ....
  public double getTranslateOffsetKfactorY() {
    //return -0.427;
    return -42.7;
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
      double drawableMinX = drawable.getPolygon().getMinX();
      if (drawableMinX < minX) {
        minX = drawableMinX;
      }
      double drawableMinY = drawable.getPolygon().getMinY();
      if (drawableMinY < minY) {
        minY = drawableMinY;
      }
      double drawableMaxX = drawable.getPolygon().getMaxX();
      if (drawableMaxX > maxX) {
        maxX = drawableMaxX;
      }
      double drawableMaxY = drawable.getPolygon().getMaxY();
      if (drawableMaxY > maxY) {
        maxY = drawableMaxY;
      }
    }
    return new Pair<>(new GeoPoint(minY, minX), new GeoPoint(maxY, maxX));
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
    lastViewPort = findBounds(drawables);
    reload();
  }

  public Drawable[] getDrawables() {
    return drawables;
  }

  public Pair<GeoPoint> getLastViewPort() {
    return lastViewPort;
  }
}
