package de.hgu.gsehen.gui;

import static de.hgu.gsehen.util.MessageUtil.logMessage;

import com.jfoenix.controls.JFXTabPane;
import com.jfoenix.controls.JFXToggleButton;
import de.hgu.gsehen.Gsehen;
import de.hgu.gsehen.evapotranspiration.DayData;
import de.hgu.gsehen.event.DrawableSelected;
import de.hgu.gsehen.event.FarmDataChanged;
import de.hgu.gsehen.event.GsehenEvent;
import de.hgu.gsehen.event.GsehenEventListener;
import de.hgu.gsehen.event.GsehenViewEvent;
import de.hgu.gsehen.event.RecommendedActionChanged;
import de.hgu.gsehen.gui.view.PlotDataController;
import de.hgu.gsehen.model.Drawable;
import de.hgu.gsehen.model.Farm;
import de.hgu.gsehen.model.Field;
import de.hgu.gsehen.model.Plot;
import de.hgu.gsehen.model.SoilProfile;
import de.hgu.gsehen.util.LoggingList;
import de.hgu.gsehen.util.MessageUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableColumn.CellEditEvent;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

public abstract class GsehenTreeTable implements GsehenEventListener<GsehenViewEvent> {
  private Gsehen gsehenInstance;
  private PlotDataController plotInstance;
  protected final ResourceBundle mainBundle;

  private Map<Class<? extends GsehenEvent>, Class<? extends GsehenEventListener
      <? extends GsehenEvent>>> eventListeners = new HashMap<>();

  private <T extends GsehenEvent> void setEventListenerClass(Class<T> eventClass,
      Class<? extends GsehenEventListener<T>> eventListenerClass) {
    eventListeners.put(eventClass, eventListenerClass);
  }

  {
    gsehenInstance = Gsehen.getInstance();
    plotInstance = PlotDataController.getInstance();

    mainBundle = ResourceBundle.getBundle("i18n.main", gsehenInstance.getSelectedLocale());

    gsehenInstance.registerForEvent(FarmDataChanged.class,
        new GsehenEventListener<FarmDataChanged>() {
          {
            setEventListenerClass(FarmDataChanged.class, getClass());
          }

          @Override
          public void handle(FarmDataChanged event) {
            fillTreeView();
          }
        });
    gsehenInstance.registerForEvent(DrawableSelected.class,
        new GsehenEventListener<DrawableSelected>() {
          {
            setEventListenerClass(DrawableSelected.class, getClass());
          }

          @Override
          public void handle(DrawableSelected event) {
            fillTreeView();
          }
        });

    gsehenInstance.registerForEvent(RecommendedActionChanged.class,
        event -> updatePlotInfo(event.getPlot()));
  }

  private static final DataFormat SERIALIZED_MIME_TYPE = new DataFormat(
      "application/x-java-serialized-object");
  private static final String FARM_TREE_VIEW_ID = "#farmTreeView";
  private static final String FILTER_HBOX_ID = "#filterHBox";
  private static final String ARCHIVE_BUTTON_ID = "#archiveButton";
  private static final String FILTER_LABEL_ID = "#filterLabel";
  private static final String FILTER_FIELD_ID = "#filterField";
  private static final String DETAIL_BORDER_PANE_ID = "#detailBorderPane";
  private static final Logger LOGGER = Logger.getLogger(GsehenTreeTable.class.getName());
  // private static final String PLOT_RECOMMENDED_ACTION_TEXT_ID = "#plotRecommendedActionText";

  private Timeline scrolltimeline = new Timeline();
  private double scrollDirection = 0;

  private TreeTableView<Drawable> farmTreeView;
  private HBox filterHBox;
  private JFXToggleButton archiveButton;
  private Label filterLabel;
  private TextField filter;
  private TreeTableColumn<Drawable, String> column;
  private TreeItem<Drawable> farmItem;
  private TreeItem<Drawable> fieldItem;
  @SuppressWarnings("unused")
  private TreeItem<Drawable> plotItem;
  private TreeItem<Drawable> trash;
  private TreeItem<Drawable> item;
  private TreeItem<Drawable> rootItem;
  private LoggingList<Farm> farmsList = new LoggingList<>();
  private TreeItem<Drawable> selectedItem;

  private ContextMenu menu = new ContextMenu();
  private MenuItem deleteItem;

  private static BorderPane detailPane;
  private Text nameLabel;
  private Text name;
  private Text typeLabel;
  private Text type;
  private Text attributeLabel1;
  private Text attribute1;
  private Text attributeLabel2;
  private Text attribute2;
  private Text attributeLabel3;
  private Text attribute3;
  private Text action;

  private JFXTabPane tabPane;

  /**
   * Adds the FarmTreeView.
   *
   * @param skipClass skip-class for the generated "DrawableSelected" event
   */
  @SuppressWarnings("unchecked")
  public void addFarmTreeView(Class<? extends GsehenEventListener<GsehenViewEvent>> skipClass) {
    farmTreeView = (TreeTableView<Drawable>) Gsehen.getInstance().getScene()
        .lookup(FARM_TREE_VIEW_ID);
    filterHBox = (HBox) Gsehen.getInstance().getScene().lookup(FILTER_HBOX_ID);
    filterHBox.setSpacing(10);
    filterHBox.setPadding(new Insets(0, 10, 0, 10));
    filterHBox.setAlignment(Pos.CENTER_LEFT);
    archiveButton = (JFXToggleButton) Gsehen.getInstance().getScene().lookup(ARCHIVE_BUTTON_ID);
    archiveButton.setText(mainBundle.getString("treetableview.archive"));

    archiveButton.selectedProperty().addListener(((observable, oldValue, newValue) -> {
      fillTreeView();
      updateDrawableFilter();
    }));

    filterLabel = (Label) Gsehen.getInstance().getScene().lookup(FILTER_LABEL_ID);
    filterLabel.setText(mainBundle.getString("treetableview.filterlabel"));
    filter = (TextField) Gsehen.getInstance().getScene().lookup(FILTER_FIELD_ID);
    filter.textProperty().addListener((observable, oldValue, newValue) -> filterChanged(newValue));
    rootItem = new TreeItem<Drawable>();
    farmTreeView.setRoot(rootItem);
    farmTreeView.setShowRoot(false);
    farmTreeView.setEditable(true);

    action = new Text();

    tabPane = gsehenInstance.getMainController().getJfxTabPane();

    farmTreeView.setRowFactory(this::rowFactory);
    addColumn(mainBundle.getString("treetableview.name"), "name");
    addColumn(mainBundle.getString("treetableview.type"), "type");
    addColumn(mainBundle.getString("treetableview.soilCrop"), "soilCrop");

    MenuItem export = new MenuItem("Export");
    menu.getItems().add(export);
    export.setOnAction(new EventHandler<ActionEvent>() {
      @SuppressWarnings("static-access")
      @Override
      public void handle(ActionEvent e) {
        tabPane.getSelectionModel().select(4);
        gsehenInstance.getExports().createExport();
      }
    });

    deleteItem = new MenuItem(mainBundle.getString("treeview.remove"));
    menu.getItems().add(deleteItem);
    deleteItem.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent e) {
        deleteSelectedItem();
      }
    });
    farmTreeView.setOnKeyPressed(new EventHandler<KeyEvent>() {
      @Override
      public void handle(final KeyEvent keyEvent) {
        if (keyEvent.getCode().equals(KeyCode.DELETE)) {
          deleteSelectedItem();
        }
      }
    });

    farmTreeView.getSelectionModel().selectedItemProperty()
        .addListener(new ChangeListener<Object>() {
          @Override
          public void changed(ObservableValue<?> observable, Object oldVal, Object newVal) {
            if (newVal != null) {
              selectedItem = (TreeItem<Drawable>) newVal;

              GridPane gridPane = new GridPane();

              // GridPane Configuration (Padding, Gaps, etc.)
              gridPane.setPadding(new Insets(20, 20, 20, 20));
              gridPane.setHgap(15);
              gridPane.setVgap(15);
              gridPane.setGridLinesVisible(false);

              // Set Column and Row Constraints
              ColumnConstraints column1 = new ColumnConstraints(200, 100, 300);
              ColumnConstraints column2 = new ColumnConstraints(200, 100, 100);
              column1.setHgrow(Priority.ALWAYS);
              column2.setHgrow(Priority.ALWAYS);
              RowConstraints rowEmpty = new RowConstraints();

              // Add Constraints to Columns & Rows
              gridPane.getColumnConstraints().addAll(column1, column2);
              gridPane.getRowConstraints().add(0, rowEmpty);
              gridPane.getRowConstraints().add(1, rowEmpty);

              nameLabel = new Text(mainBundle.getString("treetableview.name") + ": ");
              nameLabel.setFont(Font.font("Arial", 12));
              name = new Text(selectedItem.getValue().getName());
              name.setFont(Font.font("Arial", FontWeight.BOLD, 12));

              typeLabel = new Text(mainBundle.getString("treetableview.type") + ": ");
              typeLabel.setFont(Font.font("Arial", 12));
              type = new Text(selectedItem.getValue().getClass().getSimpleName());
              type.setFont(Font.font("Arial", FontWeight.BOLD, 12));

              // Set Row & Column Index for Nodes
              GridPane.setConstraints(nameLabel, 0, 0);
              GridPane.setConstraints(name, 1, 0);
              GridPane.setConstraints(typeLabel, 0, 1);
              GridPane.setConstraints(type, 1, 1);

              gridPane.getChildren().addAll(nameLabel, name, typeLabel, type);

              if (selectedItem.getValue().getClass().getSimpleName()
                  .equals(mainBundle.getString("gui.view.Map.drawableType.Farm"))) {
                if (!tabPane.getSelectionModel().isSelected(0)) {
                  tabPane.getSelectionModel().select(0);
                }
                checkCalculation();
                Farm farm = (Farm) selectedItem.getValue();

                attributeLabel1 = new Text(mainBundle.getString("treetableview.fieldnumber"));
                attributeLabel1.setFont(Font.font("Arial", 12));
                attribute1 = new Text(Integer.toString(farm.getFields().size()));
                attribute1.setFont(Font.font("Arial", FontWeight.BOLD, 12));

                // Set Row & Column Index for Nodes
                GridPane.setConstraints(attributeLabel1, 0, 2);
                GridPane.setConstraints(attribute1, 1, 2);

                gridPane.getChildren().addAll(attributeLabel1, attribute1);
              } else if (selectedItem.getValue().getClass().getSimpleName().equals("Field")) {
                if (!tabPane.getSelectionModel().isSelected(0)) {
                  tabPane.getSelectionModel().select(1);
                }
                Field field = (Field) selectedItem.getValue();

                attributeLabel1 = new Text(mainBundle.getString("treetableview.plotnumber"));
                attributeLabel1.setFont(Font.font("Arial", 12));
                attribute1 = new Text(Integer.toString(field.getPlots().size()));
                attribute1.setFont(Font.font("Arial", FontWeight.BOLD, 12));

                attributeLabel2 = new Text(mainBundle.getString("fieldview.area"));
                attributeLabel2.setFont(Font.font("Arial", 12));
                attribute2 = new Text(gsehenInstance.formatDoubleOneDecimal(
                    field.getPolygon().calculateArea(field.getPolygon().getGeoPoints())));
                attribute2.setFont(Font.font("Arial", FontWeight.BOLD, 12));

                attributeLabel3 = new Text(mainBundle.getString("fieldview.soilprofile"));
                attributeLabel3.setFont(Font.font("Arial", 12));
                SoilProfile fieldSoilProfile = gsehenInstance
                    .getSoilProfileForUuid(field.getSoilProfileUuid());
                if (fieldSoilProfile != null) {
                  attribute3 = new Text(fieldSoilProfile.getName());
                } else {
                  attribute3 = new Text("");
                }
                attribute3.setFont(Font.font("Arial", FontWeight.BOLD, 12));

                // Set Row & Column Index for Nodes
                GridPane.setConstraints(attributeLabel1, 0, 2);
                GridPane.setConstraints(attribute1, 1, 2);
                GridPane.setConstraints(attributeLabel2, 0, 3);
                GridPane.setConstraints(attribute2, 1, 3);
                GridPane.setConstraints(attributeLabel3, 0, 4);
                GridPane.setConstraints(attribute3, 1, 4);

                gridPane.getChildren().addAll(attributeLabel1, attribute1, attributeLabel2,
                    attribute2, attributeLabel3, attribute3);

                detailPane.setBottom(null);
              } else if (selectedItem.getValue().getClass().getSimpleName()
                  .equals(mainBundle.getString("gui.view.Map.drawableType.Plot"))) {
                if (!tabPane.getSelectionModel().isSelected(0)) {
                  tabPane.getSelectionModel().select(2);
                  plotInstance.getPane().setTop(null);
                }
                Plot plot = (Plot) selectedItem.getValue();

                attributeLabel1 = new Text(mainBundle.getString("fieldview.area"));
                attribute1 = new Text(gsehenInstance.formatDoubleOneDecimal(
                    plot.getPolygon().calculateArea(plot.getPolygon().getGeoPoints())));

                attributeLabel2 = new Text(mainBundle.getString("plotview.rootingzone"));

                if (plot.getRootingZone() != null) {
                  attribute2 = new Text(String.valueOf(plot.getRootingZone()));
                } else {
                  attribute2 = new Text("/");
                }

                attributeLabel3 = new Text(mainBundle.getString("plotview.crop"));
                if (plot.getCrop() != null) {
                  attribute3 = new Text(gsehenInstance.localizeCropText(plot.getCrop().getName()));
                } else {
                  attribute3 = new Text(mainBundle.getString("treetableview.nocrop"));
                }

                Text startDate;
                if (plot.getSoilStartDate() != null) {
                  startDate = new Text(gsehenInstance.formatDate(plot.getSoilStartDate()));
                } else if (plot.getCropStart() != null) {
                  startDate = new Text(gsehenInstance.formatDate(plot.getCropStart()));
                } else {
                  startDate = new Text("/");
                }

                Text soilValue;
                if (plot.getSoilStartValue() != null) {
                  soilValue = new Text(
                      gsehenInstance.formatDoubleOneDecimal(plot.getSoilStartValue()));
                } else {
                  soilValue = new Text("/");
                }

                Text locationLatLabel = new Text(mainBundle.getString("plotview.lat") + ":");
                Text locationLat = new Text(String.valueOf(plot.getLocation().getLat()));
                Text locationLngLabel = new Text(mainBundle.getString("plotview.lng") + ":");
                Text locationLng = new Text(String.valueOf(plot.getLocation().getLng()));
                Text startLabel = new Text(mainBundle.getString("plotview.cropstart"));
                Text soilValueLabel = new Text(mainBundle.getString("plotview.soilstartvalue"));

                // Set Row & Column Index for Nodes
                GridPane.setConstraints(attributeLabel1, 0, 2);
                GridPane.setConstraints(attribute1, 1, 2);
                GridPane.setConstraints(locationLatLabel, 0, 3);
                GridPane.setConstraints(locationLat, 1, 3);
                GridPane.setConstraints(locationLngLabel, 0, 4);
                GridPane.setConstraints(locationLng, 1, 4);
                GridPane.setConstraints(attributeLabel2, 0, 5);
                GridPane.setConstraints(attribute2, 1, 5);
                GridPane.setConstraints(attributeLabel3, 0, 6);
                GridPane.setConstraints(attribute3, 1, 6);
                GridPane.setConstraints(startLabel, 0, 7);
                GridPane.setConstraints(startDate, 1, 7);
                GridPane.setConstraints(soilValueLabel, 0, 8);
                GridPane.setConstraints(soilValue, 1, 8);

                gridPane.getChildren().addAll(attributeLabel1, attribute1, locationLatLabel,
                    locationLat, locationLngLabel, locationLng, attributeLabel2, attribute2,
                    attributeLabel3, attribute3, startLabel, startDate, soilValueLabel, soilValue);

                if (plot.getSoilStartValue() != null && plot.getRecommendedAction() != null) {
                  action.setText(getRecommendedActionText(
                      plot));

                } else {
                  action = new Text("/");
                }

                action.setId("plotRecommendedActionText");

                Text actionLabel = new Text(mainBundle.getString("treetableview.watering"));

                VBox bottomBox = new VBox(10);
                bottomBox.setPadding(new Insets(0, 0, 5, 20));
                bottomBox.getChildren().addAll(actionLabel, action);

                if (plot.getIsActive() != null && plot.getIsActive()) {
                  attributeLabel1.setFont(Font.font("Arial", 12));
                  attribute1.setFont(Font.font("Arial", FontWeight.BOLD, 12));
                  locationLatLabel.setFont(Font.font("Arial", 12));
                  locationLat.setFont(Font.font("Arial", FontWeight.BOLD, 12));
                  locationLngLabel.setFont(Font.font("Arial", 12));
                  locationLng.setFont(Font.font("Arial", FontWeight.BOLD, 12));
                  attributeLabel2.setFont(Font.font("Arial", 12));
                  attribute2.setFont(Font.font("Arial", FontWeight.BOLD, 12));
                  attributeLabel3.setFont(Font.font("Arial", 12));
                  attribute3.setFont(Font.font("Arial", FontWeight.BOLD, 12));
                  startLabel.setFont(Font.font("Arial", 12));
                  startDate.setFont(Font.font("Arial", FontWeight.BOLD, 12));
                  soilValueLabel.setFont(Font.font("Arial", 12));
                  soilValue.setFont(Font.font("Arial", FontWeight.BOLD, 12));
                  actionLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
                } else if (plot.getIsActive() != null && !plot.getIsActive()) {
                  nameLabel.setFont(Font.font("Arial", FontPosture.ITALIC, 12));
                  name.setFont(Font.font("Arial", FontWeight.BOLD, FontPosture.ITALIC, 12));
                  typeLabel.setFont(Font.font("Arial", FontPosture.ITALIC, 12));
                  type.setFont(Font.font("Arial", FontWeight.BOLD, FontPosture.ITALIC, 12));
                  attributeLabel1.setFont(Font.font("Arial", FontPosture.ITALIC, 12));
                  attribute1.setFont(Font.font("Arial", FontWeight.BOLD, FontPosture.ITALIC, 12));
                  locationLatLabel.setFont(Font.font("Arial", FontPosture.ITALIC, 12));
                  locationLat.setFont(Font.font("Arial", FontWeight.BOLD, FontPosture.ITALIC, 12));
                  locationLngLabel.setFont(Font.font("Arial", FontPosture.ITALIC, 12));
                  locationLng.setFont(Font.font("Arial", FontWeight.BOLD, FontPosture.ITALIC, 12));
                  attributeLabel2.setFont(Font.font("Arial", FontPosture.ITALIC, 12));
                  attribute2.setFont(Font.font("Arial", FontWeight.BOLD, FontPosture.ITALIC, 12));
                  attributeLabel3.setFont(Font.font("Arial", FontPosture.ITALIC, 12));
                  attribute3.setFont(Font.font("Arial", FontWeight.BOLD, FontPosture.ITALIC, 12));
                  startLabel.setFont(Font.font("Arial", FontPosture.ITALIC, 12));
                  startDate.setFont(Font.font("Arial", FontWeight.BOLD, FontPosture.ITALIC, 12));
                  soilValueLabel.setFont(Font.font("Arial", FontPosture.ITALIC, 12));
                  soilValue.setFont(Font.font("Arial", FontWeight.BOLD, FontPosture.ITALIC, 12));
                  actionLabel.setFont(Font.font("Arial", FontPosture.ITALIC, 12));
                  action.setFont(Font.font("Arial", FontWeight.BOLD, FontPosture.ITALIC, 12));
                  Text plotIsInactive = new Text(
                      mainBundle.getString("treetableview.plotinactive"));
                  plotIsInactive.setId("inactivePlot");
                  bottomBox.getChildren().add(plotIsInactive);
                }
                detailPane.setBottom(bottomBox);
              }

              detailPane.setCenter(gridPane);

              Platform.runLater(new Runnable() {
                @Override
                public void run() {
                  gsehenInstance.sendDrawableSelected(selectedItem.getValue(),
                      (Class<? extends GsehenEventListener<DrawableSelected>>) skipClass);
                }
              });
            }
          }
        });

    fillTreeView();
    updateDrawableFilter();
    setupScrolling();

    farmTreeView.setContextMenu(menu);
    farmTreeView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    farmTreeView.getSelectionModel().setCellSelectionEnabled(true);

    detailPane = (BorderPane) Gsehen.getInstance().getScene().lookup(DETAIL_BORDER_PANE_ID);
  }

  private void deleteSelectedItem() {
    trash = farmTreeView.getSelectionModel().getSelectedItem();
    if (trash != null) {
      Drawable trashDrawable = trash.getValue();
      if (trashDrawable != null) {
        List<Farm> delFarm = new ArrayList<Farm>();
        List<Field> delField = new ArrayList<Field>();
        List<Plot> delPlot = new ArrayList<Plot>();

        Drawable object = null;

        for (Farm farm : farmsList) {
          if (trashDrawable instanceof Farm
              && trashDrawable.getName().equals(farm.getName())) {
            delFarm.add(farm);
            object = farm;
          } else {
            for (Field field : farm.getFields()) {
              if (trashDrawable instanceof Field
                  && trashDrawable.getName().equals(field.getName())) {
                delField.add(field);
                object = field;
              } else {
                for (Plot plot : field.getPlots()) {
                  if (trashDrawable instanceof Plot
                      && trashDrawable.getName().equals(plot.getName())) {
                    delPlot.add(plot);
                    object = plot;
                  }
                }
              }
              field.getPlots().removeAll(delPlot);
            }
          }
          farm.getFields().removeAll(delField);
        }
        farmsList.removeAll(delFarm);
        gsehenInstance.getDeletedFarms().addAll(delFarm);

        logMessage(LOGGER, Level.INFO, "tree.table.item.deleted", object);
        gsehenInstance.sendFarmDataChanged(object, null);
      }
    }
  }

  @SuppressWarnings("checkstyle:indentation")
  private void updateDrawableFilter() {
    gsehenInstance.sendDrawableFilterChanged(
          archiveButton.isSelected()
            ? drawable -> true
            : drawable -> {
                  if (drawable instanceof Plot) {
                    return ((Plot) drawable).getIsActive();
                  } else {
                    return true;
                  }
            },
          null
    );
  }

  private void filterChanged(String filter) {
    if (filter.isEmpty()) {
      farmTreeView.setRoot(rootItem);
    } else {
      TreeItem<Drawable> filteredRoot = new TreeItem<>();
      filter(rootItem, filter, filteredRoot);
      farmTreeView.setRoot(filteredRoot);
    }
  }

  private void filter(TreeItem<Drawable> root, String filter, TreeItem<Drawable> filteredRoot) {
    for (TreeItem<Drawable> child : root.getChildren()) {
      TreeItem<Drawable> filteredChild = new TreeItem<>();
      filteredChild.setValue(child.getValue());
      filteredChild.setExpanded(true);
      filter(child, filter, filteredChild);
      if (!filteredChild.getChildren().isEmpty() || isMatch(filteredChild.getValue(), filter)) {
        filteredRoot.getChildren().add(filteredChild);
      }
    }
  }

  private boolean isMatch(Drawable value, String filter) {
    return value.getName().contains(filter);
  }

  /**
   * Checks, if a calculation can start.
   */
  public void checkCalculation() {

    Text general = new Text(mainBundle.getString("treetableview.general"));
    general.setFont(Font.font("Arial", FontWeight.BOLD, 14));
    Text noFarm = new Text(mainBundle.getString("treetableview.nofarm"));
    noFarm.setFont(Font.font("Arial", FontWeight.BOLD, 12));
    Text noField = new Text(mainBundle.getString("treetableview.nofield"));
    noField.setFont(Font.font("Arial", FontWeight.BOLD, 12));
    Text noPlot = new Text(mainBundle.getString("treetableview.noplot"));
    noPlot.setFont(Font.font("Arial", FontWeight.BOLD, 12));
    String fieldHasNoPlot = "";
    Text hasNoPlot = new Text(mainBundle.getString("treetableview.hasnoplot"));
    hasNoPlot.setFont(Font.font("Arial", FontWeight.BOLD, 12));
    Text noSoilProfile = new Text(mainBundle.getString("treetableview.nosoilprofile"));
    noSoilProfile.setFont(Font.font("Arial", FontWeight.BOLD, 12));
    Text noWeatherDataSource = new Text(mainBundle.getString("treetableview.noweatherdatasource"));
    noWeatherDataSource.setFont(Font.font("Arial", FontWeight.BOLD, 12));

    if (farmTreeView.getRoot().getChildren().isEmpty()) {
      detailPane = (BorderPane) Gsehen.getInstance().getScene().lookup(DETAIL_BORDER_PANE_ID);
      VBox center = new VBox(10);
      center.setPadding(new Insets(10, 10, 10, 10));
      center.getChildren().addAll(general, noFarm, noField, noPlot, noSoilProfile,
          noWeatherDataSource);
      detailPane.setCenter(center);
    } else if (selectedItem != null && selectedItem.getValue().getClass().getSimpleName()
        .equals(mainBundle.getString("gui.view.Map.drawableType.Farm"))) {
      VBox bottomBox = new VBox(10);
      bottomBox.setPadding(new Insets(10, 10, 10, 10));

      Farm farm = (Farm) selectedItem.getValue();

      if (!farm.getFields().isEmpty()) {
        for (Field f : farm.getFields()) {
          Field field = f;

          if (field.getSoilProfileUuid() == null) {
            Text needSoilProfile = new Text(
                mainBundle.getString("gui.view.Map.drawableType.Field") + " \"" + field.getName()
                    + "\" " + mainBundle.getString("treetableview.needsoilprofile"));
            needSoilProfile.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            if (!bottomBox.getChildren().contains(general)) {
              bottomBox.getChildren().add(general);
            }
            bottomBox.getChildren().add(needSoilProfile);
          }
          if (field.getWeatherDataSourceUuid() == null) {
            Text needWeatherDataSource = new Text(
                mainBundle.getString("gui.view.Map.drawableType.Field") + " \"" + field.getName()
                    + "\" " + mainBundle.getString("treetableview.needweatherdatasource"));
            needWeatherDataSource.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            if (!bottomBox.getChildren().contains(general)) {
              bottomBox.getChildren().add(general);
            }
            bottomBox.getChildren().add(needWeatherDataSource);
          }
          if (!field.getPlots().isEmpty()) {
            for (Plot p : field.getPlots()) {

              Plot plot = p;
              if (plot.getCrop() == null) {
                if (!bottomBox.getChildren().contains(general)) {
                  bottomBox.getChildren().add(general);
                }
                Text needCrop = new Text(
                    mainBundle.getString("gui.view.Map.drawableType.Plot") + " \"" + plot.getName()
                        + "\" " + mainBundle.getString("treetableview.needcrop"));
                needCrop.setFont(Font.font("Arial", FontWeight.BOLD, 12));
                bottomBox.getChildren().add(needCrop);
              }
              if (plot.getSoilStartDate() == null && plot.getCropStart() == null) {
                if (!bottomBox.getChildren().contains(general)) {
                  bottomBox.getChildren().add(general);
                }
                Text needDate = new Text(
                    mainBundle.getString("gui.view.Map.drawableType.Plot") + " \"" + plot.getName()
                        + "\" " + mainBundle.getString("treetableview.needdate"));
                needDate.setFont(Font.font("Arial", FontWeight.BOLD, 12));
                bottomBox.getChildren().add(needDate);
              }
            }
          } else {
            if (!bottomBox.getChildren().contains(general)) {
              bottomBox.getChildren().add(general);
            }
            fieldHasNoPlot += "\"" + field.getName() + "\" " + hasNoPlot.getText() + "\n \n";
          }
          if (farm.getFields().indexOf(field) == farm.getFields().size() - 1) {
            hasNoPlot.setText(fieldHasNoPlot);
            bottomBox.getChildren().add(hasNoPlot);
          }
        }
      } else {
        bottomBox.getChildren().addAll(general, noField, noPlot, noSoilProfile,
            noWeatherDataSource);
      }
      detailPane.setBottom(bottomBox);
    } else {
      detailPane.setBottom(null);
    }
  }

  private TreeTableRow<Drawable> rowFactory(TreeTableView<Drawable> view) {
    TreeTableRow<Drawable> row = new TreeTableRow<>();

    row.setOnDragDetected(event -> {
      if (!row.isEmpty()) {
        Dragboard db = row.startDragAndDrop(TransferMode.MOVE);
        db.setDragView(row.snapshot(null, null));
        ClipboardContent cc = new ClipboardContent();
        cc.put(SERIALIZED_MIME_TYPE, row.getIndex());
        db.setContent(cc);
        event.consume();
      }
    });

    row.setOnDragOver(event -> {
      Dragboard db = event.getDragboard();
      if (acceptable(db, row)) {
        event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
        event.consume();
        farmTreeView.getSelectionModel().clearSelection();
      }
    });

    row.setOnDragDropped(event -> {
      Dragboard db = event.getDragboard();
      if (acceptable(db, row)) {
        TreeItem<Drawable> movedItem =
            farmTreeView.getTreeItem((Integer)db.getContent(SERIALIZED_MIME_TYPE));
        String itemType = getItemType(movedItem);
        String destinationType = getItemType(row.getTreeItem());
        if (itemType.equals("Plot") && destinationType.equals("Field")
            || itemType.equals("Field") && destinationType.equals("Farm")) {
          moveInFarmDataObjects(movedItem, getTarget(row));
          purgeSpecialDataObjects();
          fillTreeView();
          event.setDropCompleted(true);
          farmTreeView.getSelectionModel().select(movedItem); // TODO this doesn't work - re-find!
          event.consume();
          logMessage(LOGGER, Level.INFO, "tree.table.item.dropped", item, getTarget(row));
          gsehenInstance.sendFarmDataChanged(movedItem.getValue(), null);
        } else {
          logMessage(LOGGER, Level.INFO, "tree.table.item.drop.fail", itemType, destinationType);
        }
      }
    });

    return row;
  }

  private void moveInFarmDataObjects(TreeItem<Drawable> moved, TreeItem<Drawable> target) {
    Drawable movedDrawable = moved.getValue();
    Drawable targetDrawable = target.getValue();
    for (Iterator<Farm> farmIterator = farmsList.iterator(); farmIterator.hasNext(); ) {
      Farm farm = farmIterator.next();
      for (Iterator<Field> fieldIterator = farm.getFields().iterator(); fieldIterator.hasNext(); ) {
        Field field = fieldIterator.next();
        if (field.equals(movedDrawable)) {
          fieldIterator.remove();
          continue;
        }
        for (Iterator<Plot> plotIterator = field.getPlots().iterator(); plotIterator.hasNext(); ) {
          Plot plot = plotIterator.next();
          if (plot.equals(movedDrawable)) {
            plotIterator.remove();
            continue;
          }
        }
      }
    }
    if (targetDrawable instanceof Farm && movedDrawable instanceof Field) {
      ((Farm)targetDrawable).getFields().add((Field)movedDrawable);
    } else {
      if (targetDrawable instanceof Field && movedDrawable instanceof Plot) {
        ((Field)targetDrawable).getPlots().add((Plot)movedDrawable);
      }
    }
  }

  private String getItemType(TreeItem<Drawable> treeItem) {
    return treeItem.getValue().getClass().getSimpleName();
  }

  private void purgeSpecialDataObjects() {
    String newPlotsFieldName = gsehenInstance.getNewPlotsFieldName();
    String newFieldsFarmName = gsehenInstance.getNewFieldsFarmName();
    for (Iterator<Farm> farmIterator = farmsList.iterator(); farmIterator.hasNext(); ) {
      Farm farm = farmIterator.next();
      for (Iterator<Field> fieldIterator = farm.getFields().iterator(); fieldIterator.hasNext(); ) {
        Field field = fieldIterator.next();
        if (field.getName().equals(newPlotsFieldName) && field.getPlots().isEmpty()) {
          fieldIterator.remove();
        }
      }
      if (farm.getName().equals(newFieldsFarmName) && farm.getFields().isEmpty()) {
        farmIterator.remove();
      }
    }
  }

  @SuppressWarnings("rawtypes")
  private boolean acceptable(Dragboard db, TreeTableRow<Drawable> row) {
    boolean result = false;
    if (db.hasContent(SERIALIZED_MIME_TYPE)) {
      int index = (Integer) db.getContent(SERIALIZED_MIME_TYPE);
      if (row.getIndex() != index) {
        TreeItem target = getTarget(row);
        TreeItem item = farmTreeView.getTreeItem(index);
        result = !isParent(item, target);
      }
    }
    return result;
  }

  private TreeItem<Drawable> getTarget(TreeTableRow<Drawable> row) {
    TreeItem<Drawable> target = farmTreeView.getRoot();
    if (!row.isEmpty()) {
      target = row.getTreeItem();
    }
    return target;
  }

  // prevent loops in the tree
  @SuppressWarnings("rawtypes")
  private boolean isParent(TreeItem parent, TreeItem child) {
    boolean result = false;
    while (!result && child != null) {
      result = child.getParent() == parent;
      child = child.getParent();
    }
    return result;
  }

  /**
   * Adds the columns to the TreeTableView.
   * 
   * @param label
   *          - Name of the column.
   * @param dataIndex
   *          - Content of the column.
   */
  public void addColumn(String label, String dataIndex) {

    column = new TreeTableColumn<>(label);
    column.setCellValueFactory((TreeTableColumn.CellDataFeatures<Drawable, String> param) -> {
      ObservableValue<String> result = new ReadOnlyStringWrapper("");
      if (param.getValue().getValue() != null && dataIndex.equals("name")) {
        result = new ReadOnlyStringWrapper(param.getValue().getValue().getName());
      } else if (param.getValue().getValue() != null && dataIndex.equals("type")) {
        result = new ReadOnlyStringWrapper(param.getValue().getValue().getClass().getSimpleName());
      } else {
        if (param.getValue().getValue().getClass().getSimpleName().equals("Farm")) {
          result = new ReadOnlyStringWrapper("/");
        } else if (param.getValue().getValue().getClass().getSimpleName().equals("Field")) {
          Field field = (Field) param.getValue().getValue();
          SoilProfile fieldSoilProfile = gsehenInstance
              .getSoilProfileForUuid(field.getSoilProfileUuid());
          if (fieldSoilProfile != null) {
            result = new ReadOnlyStringWrapper(fieldSoilProfile.getName());
          } else {
            result = new ReadOnlyStringWrapper("/");
          }
        } else if (param.getValue().getValue().getClass().getSimpleName().equals("Plot")) {
          Plot plot = (Plot) param.getValue().getValue();
          if (plot.getCrop() != null) {
            result = new ReadOnlyStringWrapper(
                gsehenInstance.localizeCropText(plot.getCrop().getName()));
          } else {
            result = new ReadOnlyStringWrapper("/");
          }
        }
      }
      return result;
    });

    column.setCellFactory(TextFieldTreeTableCell.forTreeTableColumn());

    if (column.getText().equals(mainBundle.getString("treetableview.name"))) {
      column.setPrefWidth(150);
      column.setOnEditCommit(new EventHandler<CellEditEvent<Drawable, String>>() {
        @Override
        public void handle(CellEditEvent<Drawable, String> event) {
          Drawable object = null;
          for (Farm farm : farmsList) {
            if (farm.getName().equals(event.getOldValue()) && farmTreeView.getSelectionModel()
                .getSelectedItem().getValue().getClass().getSimpleName().equals("Farm")) {
              String uniqueName = gsehenInstance.uniquify(Farm.class, event.getNewValue());
              logMessage(LOGGER, Level.INFO, "cell.edit.event.farm.renamed",
                  event.getOldValue(), uniqueName);
              farm.setName(uniqueName);
              object = farm;
            }
            for (Field field : farm.getFields()) {
              if (field.getName().equals(event.getOldValue()) && farmTreeView.getSelectionModel()
                  .getSelectedItem().getValue().getClass().getSimpleName().equals("Field")) {
                String uniqueName = gsehenInstance.uniquify(Field.class, event.getNewValue());
                logMessage(LOGGER, Level.INFO, "cell.edit.event.field.renamed",
                    event.getOldValue(), uniqueName);
                field.setName(uniqueName);
                object = field;
              }
              for (Plot plot : field.getPlots()) {
                if (plot.getName().equals(event.getOldValue()) && farmTreeView.getSelectionModel()
                    .getSelectedItem().getValue().getClass().getSimpleName().equals("Plot")) {
                  String uniqueName = gsehenInstance.uniquify(Plot.class, event.getNewValue());
                  logMessage(LOGGER, Level.INFO, "cell.edit.event.plot.renamed",
                      event.getOldValue(), uniqueName);
                  plot.setName(uniqueName);
                  object = plot;
                }
              }
            }
          }
          gsehenInstance.sendFarmDataChanged(object, null);
        }
      });
    } else {
      column.setPrefWidth(100);
      column.setEditable(false);
      column.setStyle("-fx-alignment: CENTER;");
      column.setSortType(TreeTableColumn.SortType.ASCENDING);
    }
    farmTreeView.getColumns().add(column);
  }

  /**
   * Scrollbar in the TreeTableView.
   */
  public void setupScrolling() {
    scrolltimeline.setCycleCount(Timeline.INDEFINITE);
    scrolltimeline.getKeyFrames()
        .add(new KeyFrame(Duration.millis(20), "Scroll", (ActionEvent e) -> {
          dragScroll();
        }));
    farmTreeView.setOnDragExited(event -> {
      if (event.getY() > 0) {
        scrollDirection = 1.0 / farmTreeView.getExpandedItemCount();
      } else {
        scrollDirection = -1.0 / farmTreeView.getExpandedItemCount();
      }
      scrolltimeline.play();
    });
    farmTreeView.setOnDragEntered(event -> {
      scrolltimeline.stop();
    });
    farmTreeView.setOnDragDone(event -> {
      scrolltimeline.stop();
    });
  }

  private void dragScroll() {
    ScrollBar sb = getVerticalScrollbar();
    if (sb != null) {
      double newValue = sb.getValue() + scrollDirection;
      newValue = Math.min(newValue, 1.0);
      newValue = Math.max(newValue, 0.0);
      sb.setValue(newValue);
    }
  }

  private ScrollBar getVerticalScrollbar() {
    ScrollBar result = null;
    for (Node n : farmTreeView.lookupAll(".scroll-bar")) {
      if (n instanceof ScrollBar) {
        ScrollBar bar = (ScrollBar) n;
        if (bar.getOrientation().equals(Orientation.VERTICAL)) {
          result = bar;
        }
      }
    }
    return result;
  }

  /**
   * Fills the TreeView with Farms, Fields and (active) Plots.
   */
  public void fillTreeView() {
    boolean alsoInActive = archiveButton.isSelected();
    farmsList = Gsehen.getInstance().getFarmsList();
    farmTreeView.getRoot().getChildren().clear();
    for (Farm farm : farmsList) {
      farmItem = createItem(rootItem, farm);
      if (farm.getFields() != null) {
        for (Field field : farm.getFields()) {
          field.setArea(field.getPolygon().calculateArea(field.getPolygon().getGeoPoints()));
          fieldItem = createItem(farmItem, field);
          if (field.getPlots() != null) {
            for (Plot plot : field.getPlots()) {
              if (alsoInActive || plot.getIsActive()) {
                plot.setArea(plot.getPolygon().calculateArea(plot.getPolygon().getGeoPoints()));
                plotItem = createItem(fieldItem, plot);
              }
            }
          }
        }
      }
    }
  }

  private TreeItem<Drawable> createItem(TreeItem<Drawable> parent, Drawable content) {
    item = new TreeItem<Drawable>();
    item.setValue(content);
    item.getValue().setName(content.getName());
    parent.getChildren().add(item);
    item.setExpanded(true);
    return item;
  }

  private String getRecommendedActionText(Plot plot) {
    List<DayData> dailyBalances = plot.getWaterBalance() != null
        ? plot.getWaterBalance().getDailyBalances() :
        null;
    String dateTimeInfo = (dailyBalances != null && dailyBalances.size() > 0)
        ? gsehenInstance.formatDateTime(dailyBalances.get(dailyBalances.size() - 1).getDate())
            + " — " : "";
    return dateTimeInfo + MessageUtil.renderMessage(gsehenInstance.getSelectedLocale(), mainBundle,
        plot.getRecommendedAction());
  }

  private void updatePlotInfo(Plot plot) {
    final String recommendedActionText = getRecommendedActionText(plot);
    logMessage(LOGGER, Level.CONFIG, "tree.table.update.localized.plot.info",
        plot, gsehenInstance.getSelectedLocale(), recommendedActionText);
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        action.setText(recommendedActionText);
      }
    });
  }
}
