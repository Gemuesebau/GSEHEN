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
}
