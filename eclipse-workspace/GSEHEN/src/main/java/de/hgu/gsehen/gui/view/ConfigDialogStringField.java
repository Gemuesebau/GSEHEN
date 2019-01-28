package de.hgu.gsehen.gui.view;

import com.jfoenix.controls.JFXTextField;

import de.hgu.gsehen.Gsehen;

import java.util.List;
import javafx.scene.Node;
import javafx.scene.text.Text;

public class ConfigDialogStringField extends ConfigDialogElement<JFXTextField, String> {

  @SuppressWarnings("checkstyle:javadocmethod")
  public ConfigDialogStringField(Text text, Text label,
      List<ConfigDialogElement<Node, Object>> add, Gsehen gsehenInstance) {
    super(
        text, new JFXTextField(), label, add,
        (node, value) -> node.setText(value),
        node -> node.getText());
  }
}
