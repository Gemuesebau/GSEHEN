package de.hgu.gsehen.model;

import de.hgu.gsehen.Gsehen;
import java.util.Locale;
import java.util.TreeMap;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

public interface WeatherDataConfigurator {
  /**
   * Gets a GUI node list where new controls are to be added.
   */
  ObservableList<Node> getConfigNodes();

  /**
   * Gets the number of leading <i>GUI nodes</i> in the given list to be left untouched.
   *
   * <p>Some nodes in the list obtained by <code>getConfigNodes()</code> must not be touched.
   * This method returns the number of nodes, beginning from the list start, that are to be
   * regarded as "fixed", i.e., not to be touched.</p>
   */
  int getFixedNodesCount();

  /**
   * Gets the number of leading <i>configuration items</i> to be left untouched.
   *
   * <p>A configuration item may be displayed by one or more GUI nodes. This method returns the
   * number of such items, as opposed to the number of GUI nodes that is returned by
   * <code>getFixedNodesCount()</code>.</p>
   */
  int getFixedItemsCount();

  /**
   * Gets the GSEHEN application instance.
   */
  Gsehen getInstance();

  /**
   * Gets the currently selected UI locale.
   */
  Locale getLocale();

  /**
   * Gets a dictionary for available Java locale values.
   *
   * <p>The map values are the descriptions for the locales. These descriptions are themselves
   * localized, in the currently selected UI locale, see <code>getLocale()</code>.</p>
   */
  TreeMap<String, String> getJavaLocaleMap();

  /**
   * Gets the StackPane to be used for, e.g., (modal) dialogs.
   */
  StackPane getParentStackPane();

  /**
   * Shows the given error message.
   */
  void setError(String errorMessage);

  /**
   * Resets the (error-holding part of the) GUI.
   */
  void reset();
}
