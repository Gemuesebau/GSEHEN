package de.hgu.gsehen.gui.view;

import de.hgu.gsehen.Gsehen;
import de.hgu.gsehen.gui.GsehenGuiElements;
import de.hgu.gsehen.model.Drawable;
import de.hgu.gsehen.model.Farm;
import de.hgu.gsehen.model.Field;
import de.hgu.gsehen.model.Plot;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import com.jfoenix.controls.JFXCheckBox;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

@SuppressWarnings("unchecked")
public class DataExport {

  private static final String FARM_TREE_VIEW_ID = "#farmTreeView";
  protected final ResourceBundle mainBundle;
  private GsehenGuiElements gsehenGuiElements;
  private Gsehen gsehenInstance;
  private String pattern;
  private BorderPane pane;
  private GridPane centerGrid;
  private TreeTableView<Drawable> treeTableView;
  private Farm farm;
  private Text headline;

  {
    gsehenInstance = Gsehen.getInstance();
    gsehenGuiElements = new GsehenGuiElements();

    DateFormat dateFormat =
        DateFormat.getDateInstance(DateFormat.SHORT, gsehenInstance.getSelectedLocale());
    pattern = ((SimpleDateFormat) dateFormat).toPattern();

    mainBundle = ResourceBundle.getBundle("i18n.main", gsehenInstance.getSelectedLocale());

    treeTableView =
        (TreeTableView<Drawable>) Gsehen.getInstance().getScene().lookup(FARM_TREE_VIEW_ID);
  }

  /**
   * Constructs a new data export associated with the given BorderPane.
   *
   * @param pane - the associated BorderPane.
   */
  public DataExport(Gsehen application, BorderPane pane) {
    this.gsehenInstance = application;
    this.pane = pane;
  }

  /**
   * Creates the view.
   */
  public void createExport() {
    for (int i = 0; i < treeTableView.getSelectionModel().getSelectedCells().size(); i++) {
      if (treeTableView.getSelectionModel().getSelectedCells().get(i) != null) {
        TreeItem<Drawable> selectedItem =
            treeTableView.getSelectionModel().getSelectedCells().get(i).getTreeItem();
        if (selectedItem != null
            && selectedItem.getValue().getClass().getSimpleName().equals("Farm")) {
          pane.setVisible(true);
          farm = (Farm) selectedItem.getValue();

          // GridPane - Center Section
          centerGrid = gsehenGuiElements.gridPane(pane);

          headline = gsehenGuiElements.text("Datenexport des Betriebs \"" + farm.getName() + "\"",
              FontWeight.BOLD);

          int fieldCounter = 3;
          int plotCounter = 0;

          for (Field field : farm.getFields()) {
            JFXCheckBox fieldCheckBox = new JFXCheckBox(field.getName());

            GridPane.setConstraints(fieldCheckBox, 0, fieldCounter);
            centerGrid.getChildren().add(fieldCheckBox);
            for (Plot plot : field.getPlots()) {
              plotCounter = fieldCounter + 1;
              JFXCheckBox plotCheckBox = new JFXCheckBox(plot.getName());

              GridPane.setConstraints(plotCheckBox, 1, plotCounter);
              centerGrid.getChildren().add(plotCheckBox);
              fieldCounter += 1;
            }
            Separator separator = new Separator();
            GridPane.setConstraints(separator, 0, plotCounter + 1, 2, 1);
            centerGrid.getChildren().add(separator);

            fieldCounter = plotCounter + 2;
          }

          Button exportButton = gsehenGuiElements.button(150);
          exportButton.setText("Export");
          exportButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
              // TODO
            }
          });

          // Set Row & Column Index for Nodes
          GridPane.setConstraints(headline, 0, 0);
          GridPane.setConstraints(exportButton, 0, plotCounter + 3);

          centerGrid.getChildren().addAll(headline, exportButton);

          ScrollPane scrollPane = new ScrollPane();
          scrollPane.setContent(centerGrid);
          scrollPane.setPannable(true);

          pane.setCenter(scrollPane);
        }
      }
    }
  }

}
