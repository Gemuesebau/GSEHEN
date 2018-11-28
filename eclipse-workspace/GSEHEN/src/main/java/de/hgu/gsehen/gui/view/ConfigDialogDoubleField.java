package de.hgu.gsehen.gui.view;

import com.jfoenix.controls.JFXTextField;

import de.hgu.gsehen.Gsehen;

import java.util.List;
import javafx.scene.Node;
import javafx.scene.text.Text;

public class ConfigDialogDoubleField extends ConfigDialogElement<JFXTextField, Double> {

  @SuppressWarnings("checkstyle:javadocmethod")
  public ConfigDialogDoubleField(Text text, Text label,
      List<ConfigDialogElement<Node, Object>> add, Gsehen gsehenInstance) {
    super(
        text, new JFXTextField(), label, add,
        (node, value) -> node.setText(gsehenInstance.formatDoubleTwoDecimal(value)),
        node -> gsehenInstance.parseDouble(node.getText()));
    node.textProperty().addListener((observable, oldValue, newValue) -> {
      if (!newValue.trim().isEmpty() && !gsehenInstance.isParseable(newValue)) {
        node.setText(oldValue);
      }
    });
  }
}
