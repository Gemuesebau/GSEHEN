package de.hgu.gsehen.gui.view;

import de.hgu.gsehen.Gsehen;
import de.hgu.gsehen.event.FarmDataChanged;
import de.hgu.gsehen.event.GsehenEventListener;
import java.util.logging.Logger;
import javafx.scene.web.WebView;

public class Farms extends FarmDataController implements GsehenEventListener<FarmDataChanged> {
  private static final Logger LOGGER = Logger.getLogger(Farms.class.getName());

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

  /**
   * Handles the event of a change in the farm view's bounds (viewport).
   *
   * <p>All parameters must be given as numbers in the coordinate scale.</p>
   *
   * @param viewportWidth longitude difference between east and west bounds
   * @param viewportHeight latitude difference between north and south bounds
   * @param lngTranslation longitude of the viewport translation since last view initialization
   * @param latTranslation latitude of the viewport translation since last view initialization
   */
  public void farmBoundsChanged(double viewportWidth, double viewportHeight,
      double lngTranslation, double latTranslation) {
    //getLogger().info("*** "
    //    + "viewportWidth = " + viewportWidth
    //    + ", viewportHeight = " + viewportHeight
    //    + ", lngTranslation = " + lngTranslation
    //    + ", latTranslation = " + latTranslation
    //);
    setLastViewport(
        latTranslation + viewportHeight / 2,
        latTranslation - viewportHeight / 2,
        lngTranslation + viewportWidth / 2,
        lngTranslation - viewportWidth / 2
    );
  }
}
