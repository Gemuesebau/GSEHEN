package de.hgu.gsehen.gui.view;

import de.hgu.gsehen.Gsehen;
import javafx.scene.layout.BorderPane;

public class Plots extends PlotDataController {

  /**
   * Constructs the PlotView.
   *
   * @param application the Gsehen application singleton reference
   * @param pane the associated border pane
   */
  public Plots(Gsehen application, BorderPane pane) {
    super(application, pane);
  }

}
