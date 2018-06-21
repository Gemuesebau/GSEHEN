package de.hgu.gsehen.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import de.hgu.gsehen.Gsehen;
import de.hgu.gsehen.event.DrawableSelected;
import de.hgu.gsehen.event.FarmDataChanged;
import de.hgu.gsehen.event.GsehenEventListener;
import de.hgu.gsehen.event.GsehenViewEvent;
import de.hgu.gsehen.model.Drawable;
import de.hgu.gsehen.model.Farm;
import de.hgu.gsehen.model.Field;
import de.hgu.gsehen.model.Plot;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.SelectionMode;
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
import javafx.util.Duration;

//TODO: TreeView tut nix. Mach mal heile.
public class GsehenTreeTable implements GsehenEventListener<GsehenViewEvent> {

	private Gsehen gsehenInstance;
	private static GsehenTreeTable instance;

	{
		instance = this;
		gsehenInstance = Gsehen.getInstance();
		gsehenInstance.registerForEvent(FarmDataChanged.class, instance);
		gsehenInstance.registerForEvent(DrawableSelected.class, instance);
	}

	protected static final ResourceBundle mainBundle = ResourceBundle.getBundle("i18n.main", Locale.GERMAN);
	private static final DataFormat SERIALIZED_MIME_TYPE = new DataFormat("application/x-java-serialized-object");
	private static final String FARM_TREE_VIEW_ID = "#farmTreeView";
	private static final Logger LOGGER = Logger.getLogger(Gsehen.class.getName());

	private List<Farm> newFarmsList;
	private Farm farm;
	private Timeline scrolltimeline = new Timeline();
	private double scrollDirection = 0;

	private TreeTableView<Drawable> farmTreeView;
	private TreeTableColumn<Drawable, String> column;
	private TreeItem<Drawable> farmItem;
	private TreeItem<Drawable> fieldItem;
	@SuppressWarnings("unused")
	private TreeItem<Drawable> plotItem;
	private TreeItem<Drawable> trash;
	private TreeItem<Drawable> item;
	private TreeItem<Drawable> rootItem;
	private List<Farm> farmsList = new ArrayList<>();
	private TreeItem<Drawable> selectedItem;

	private ContextMenu menu = new ContextMenu();
	private MenuItem deleteItem;

	/**
	 * Adds the FarmTreeView.
	 */
	@SuppressWarnings("unchecked")
	public void addFarmTreeView() {
		farmTreeView = (TreeTableView<Drawable>) Gsehen.getInstance().getScene().lookup(FARM_TREE_VIEW_ID);
		rootItem = new TreeItem<Drawable>();
		farmTreeView.setRoot(rootItem);
		farmTreeView.setShowRoot(false);
		farmTreeView.setEditable(true);

		farmTreeView.setRowFactory(this::rowFactory);
		addColumn(mainBundle.getString("treetableview.name"), "name");
		addColumn(mainBundle.getString("treetableview.type"), "type");
		addColumn(mainBundle.getString("treetableview.soilCrop"), "soilCrop");

		deleteItem = new MenuItem(mainBundle.getString("treeview.remove"));
		menu.getItems().add(deleteItem);
		deleteItem.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				for (int i = 0; i < farmTreeView.getSelectionModel().getSelectedItems().size(); i++) {
					farmTreeView.getSelectionModel().getSelectedItems().get(i).getValue().setName("del");
				}
				trash = farmTreeView.getSelectionModel().getSelectedItem();
				if (trash != null) {
					removeItem();
				}
			}
		});

		farmTreeView.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(final KeyEvent keyEvent) {
				if (keyEvent.getCode().equals(KeyCode.DELETE)) {
					for (int i = 0; i < farmTreeView.getSelectionModel().getSelectedItems().size(); i++) {
						farmTreeView.getSelectionModel().getSelectedItems().get(i).getValue().setName("del");
					}
					trash = farmTreeView.getSelectionModel().getSelectedItem();
					if (trash != null) {
						System.out.println(trash.getValue().getName());
						removeItem();
					}
				}
			}
		});

		farmTreeView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Object>() {
			@Override
			public void changed(ObservableValue<?> observable, Object oldVal, Object newVal) {
				if (newVal != null) {
					selectedItem = (TreeItem<Drawable>) newVal;
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							gsehenInstance.sendDrawableSelected(selectedItem.getValue(), null);
						}
					});
				}
			}
		});

		fillTreeView();
		setupScrolling();

		farmTreeView.setContextMenu(menu);
		farmTreeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		farmTreeView.getSelectionModel().setCellSelectionEnabled(true);
	}

	@SuppressWarnings("unchecked")
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
				int index = (Integer) db.getContent(SERIALIZED_MIME_TYPE);
				TreeItem<Drawable> item = farmTreeView.getTreeItem(index);

				Drawable drawableItem = item.getValue();
				String itemType = drawableItem.getClass().getSimpleName();

				Drawable map = row.getTreeItem().getValue();
				String destinationType = map.getClass().getSimpleName();

				Drawable object = null;

				if (itemType.equals("Plot") && destinationType.equals("Field")
						|| itemType.equals("Field") && destinationType.equals("Farm")) {
					item.getParent().getChildren().remove(item);
					getTarget(row).getChildren().add(item);
					event.setDropCompleted(true);
					farmTreeView.getSelectionModel().select(item);
					event.consume();

					LOGGER.info(item + " stacked on " + getTarget(row));

					newFarmsList = new ArrayList<>();

					String farmName;
					GeoPolygon farmGeo;

					String fieldName;
					GeoPolygon fieldGeo;
					Field field;

					String plotName;
					GeoPolygon plotGeo;
					Plot plot;

					// Updates the farmList.
					for (int i = 0; i < farmTreeView.getRoot().getChildren().size(); i++) {
						farmName = (String) farmTreeView.getRoot().getChildren().get(i).getValue().getName();
						farmGeo = farmTreeView.getRoot().getChildren().get(i).getValue().getPolygon();
						farm = new Farm(farmName, farmGeo);
						object = farm;
						for (int j = 0; j < farmTreeView.getRoot().getChildren().get(i).getChildren().size(); j++) {
							fieldName = (String) farmTreeView.getRoot().getChildren().get(i).getChildren().get(j)
									.getValue().getName();
							fieldGeo = farmTreeView.getRoot().getChildren().get(i).getChildren().get(j).getValue()
									.getPolygon();
							field = new Field(fieldName, fieldGeo);
							object = field;
							if (j == 0) {
								farm.setFields(field);
							} else {
								List<Field> fields = farm.getFields();
								fields.add(field);
							}
							for (int k = 0; k < farmTreeView.getRoot().getChildren().get(i).getChildren().get(j)
									.getChildren().size(); k++) {
								plotName = (String) farmTreeView.getRoot().getChildren().get(i).getChildren().get(j)
										.getChildren().get(k).getValue().getName();
								plotGeo = farmTreeView.getRoot().getChildren().get(i).getChildren().get(j).getChildren()
										.get(k).getValue().getPolygon();
								plot = new Plot(plotName, plotGeo);
								object = plot;
								if (k == 0) {
									field.setPlots(plot);
								} else {
									List<Plot> plots = farm.getFields().get(j).getPlots();
									plots.add(plot);
								}
							}
						}
						newFarmsList.add(farm);
					}
					farmsList.clear();
					farmsList.addAll(newFarmsList);
					gsehenInstance.sendFarmDataChanged(object, null);
				} else {
					LOGGER.info(itemType + " can't be stack on " + destinationType);
				}
			}
		});
		return row;
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

	@SuppressWarnings("rawtypes")
	private TreeItem getTarget(TreeTableRow<Drawable> row) {
		TreeItem target = farmTreeView.getRoot();
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
	 *            - Name of the column.
	 * @param dataIndex
	 *            - Content of the column.
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
					if (field.getSoilProfile() != null) {
						result = new ReadOnlyStringWrapper(field.getSoilProfile().getSoilType().toString());
						// TODO: Passt das so?
					} else {
						result = new ReadOnlyStringWrapper("/");
					}
				} else if (param.getValue().getValue().getClass().getSimpleName().equals("Plot")) {
					Plot plot = (Plot) param.getValue().getValue();
					if (plot.getCrop() != null) {
						result = new ReadOnlyStringWrapper(plot.getCrop().getName());
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
							LOGGER.info("\"Farm\": " + event.getOldValue() + " renamed in " + event.getNewValue());
							farm.setName(event.getNewValue());
							object = farm;
						}
						for (Field field : farm.getFields()) {
							if (field.getName().equals(event.getOldValue()) && farmTreeView.getSelectionModel()
									.getSelectedItem().getValue().getClass().getSimpleName().equals("Field")) {
								LOGGER.info("\"Field\": " + event.getOldValue() + " renamed in " + event.getNewValue());
								field.setName(event.getNewValue());
								object = field;
							}
							for (Plot plot : field.getPlots()) {
								if (plot.getName().equals(event.getOldValue()) && farmTreeView.getSelectionModel()
										.getSelectedItem().getValue().getClass().getSimpleName().equals("Plot")) {
									LOGGER.info(
											"\"Plot\": " + event.getOldValue() + " renamed in " + event.getNewValue());
									plot.setName(event.getNewValue());
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
		scrolltimeline.getKeyFrames().add(new KeyFrame(Duration.millis(20), "Scroll", (ActionEvent e) -> {
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
	 * Fills the TreeView with Farms, Fields and Plots.
	 */
	public void fillTreeView() {
		farmsList = Gsehen.getInstance().getFarmsList();
		farmTreeView.getRoot().getChildren().clear();
		for (Farm farm : farmsList) {
			farmItem = createItem(rootItem, farm);
			if (farm.getFields() != null) {
				for (Field field : farm.getFields()) {
					fieldItem = createItem(farmItem, field);
					if (field.getPlots() != null) {
						for (Plot plot : field.getPlots()) {
							plotItem = createItem(fieldItem, plot);
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

	/**
	 * Removes an item (and his childs) from the TreeTableView.
	 */
	public void removeItem() {
		List<Farm> delFarm = new ArrayList<Farm>();
		List<Field> delField = new ArrayList<Field>();
		List<Plot> delPlot = new ArrayList<Plot>();

		Drawable object = null;

		for (Farm farm : farmsList) {
			if (trash.getValue().getName().equals(farm.getName())) {
				delFarm.add(farm);
				object = farm;
			} else {
				for (Field field : farm.getFields()) {
					if (trash.getValue().getName().equals(field.getName())) {
						delField.add(field);
						object = field;
					} else {
						for (Plot plot : field.getPlots()) {
							if (trash.getValue().getName().equals(plot.getName())) {
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
		LOGGER.info(object + " deleted.");
		farmsList.removeAll(delFarm);
		gsehenInstance.sendFarmDataChanged(object, null);
	}

	public void handle(GsehenViewEvent event) {
		fillTreeView();
	}

}
