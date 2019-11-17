package de.hgu.gsehen.gui;

import de.hgu.gsehen.Gsehen;
import java.util.Map;
import java.util.TreeMap;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

public final class GsehenSave {
  private final Gsehen gsehenInstance = Gsehen.getInstance();

  /**
   * Shows a dialog when the user wants to exit.
   */
  public void exitApplication() {
    Map<String, EventHandler<ActionEvent>> buttons = new TreeMap<>();
    buttons.put("save.0.saveandexit", e -> {
      gsehenInstance.saveUserData();
      Platform.exit();
      System.exit(0);
    });
    buttons.put("save.1.exitwithoutsave", e -> {
      Platform.exit();
      System.exit(0);
    });
    buttons.put("save.2.cancel", e -> {
    });
    gsehenInstance.showDialog("save.title", null, buttons);
  }
}
