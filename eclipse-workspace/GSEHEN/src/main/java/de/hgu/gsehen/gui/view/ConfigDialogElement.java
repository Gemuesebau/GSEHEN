package de.hgu.gsehen.gui.view;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import javafx.scene.Node;
import javafx.scene.text.Text;

public class ConfigDialogElement<T extends Node, V> {
  private Text label;
  protected T node;
  private Text example;
  private BiConsumer<T, V> nodeValueSetter;
  private Function<T, V> nodeValueGetter;

  @SuppressWarnings({"checkstyle:javadocmethod", "unchecked"})
  public ConfigDialogElement(Text label, T node, Text example,
      List<ConfigDialogElement<Node, Object>> add,
      BiConsumer<T, V> nodeValueSetter, Function<T, V> nodeValueGetter) {
    super();
    this.label = label;
    this.node = node;
    this.example = example;
    this.nodeValueSetter = nodeValueSetter;
    this.nodeValueGetter = nodeValueGetter;
    if (add != null) {
      add.add((ConfigDialogElement<Node, Object>) this);
    }
  }

  public Text getLabel() {
    return label;
  }

  public void setLabel(Text label) {
    this.label = label;
  }

  public T getNode() {
    return node;
  }

  public void setNode(T node) {
    this.node = node;
  }

  public Text getExample() {
    return example;
  }

  public void setExample(Text example) {
    this.example = example;
  }

  public void setNodeValue(V value) {
    nodeValueSetter.accept(node, value);
  }

  public V getNodeValue() {
    return nodeValueGetter.apply(node);
  }
}
