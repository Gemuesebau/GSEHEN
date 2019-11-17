package de.hgu.gsehen.gui.view;

import com.jfoenix.controls.JFXButton;
import de.hgu.gsehen.gui.GsehenGuiElements;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;

public class ConfigDialogActionButton extends ConfigDialogElement<JFXButton, String> {

  @SuppressWarnings("checkstyle:javadocmethod")
  public ConfigDialogActionButton(String caption, List<ConfigDialogElement<Node, Object>> add,
      EventHandler<ActionEvent> handler) {
    super(
        GsehenGuiElements.text(""), GsehenGuiElements.jfxButton(caption, handler), null, add,
        (node, value) -> { },
        node -> null);
  }
}
