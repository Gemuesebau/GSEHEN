package de.hgu.gsehen.gui.view;

import de.hgu.gsehen.Gsehen;
import javafx.scene.layout.BorderPane;

public class Logs extends LogDataController {

  /**
  * Constructs the LogView.
  *
  * @param application the Gsehen application singleton reference
  * @param pane the associated border pane
  */
  public Logs(Gsehen application, BorderPane pane) {
    super(application, pane); 
  }
}
