package de.hgu.gsehen.evapotranspiration;

import java.util.HashMap;
import java.util.Map;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
   
@SuppressWarnings("all")
public class TreeTableViewDragAndDropDemo extends Application {

    private static final DataFormat SERIALIZED_MIME_TYPE = new DataFormat("application/x-java-serialized-object");
   
    private TreeItem<Map<String, Object>> root;
    private TreeTableView<Map<String, Object>> tree;
    private Timeline scrolltimeline = new Timeline();
    private double scrollDirection = 0;

    @Override
    public void start(Stage primaryStage) throws Exception {
        VBox outer = new VBox();

        root = new TreeItem<>();
        tree = new TreeTableView<>(root);
        tree.setShowRoot(false);
        tree.setRowFactory(this::rowFactory);
        addColumn("Region", "region");
        addColumn("Type", "type");
        addColumn("Pop.", "population");
        setupData();
        setupScrolling();

        outer.getChildren().addAll(tree);
        Scene scene = new Scene(outer, 640, 480);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void setupScrolling() {
        scrolltimeline.setCycleCount(Timeline.INDEFINITE);
        scrolltimeline.getKeyFrames().add(new KeyFrame(Duration.millis(20), "Scoll", (ActionEvent) -> { dragScroll();}));
        tree.setOnDragExited(event -> {
            if (event.getY() > 0) {
                scrollDirection = 1.0 / tree.getExpandedItemCount();
            }
            else {
                scrollDirection = -1.0 / tree.getExpandedItemCount();
            }
            scrolltimeline.play();
        });
        tree.setOnDragEntered(event -> {
            scrolltimeline.stop();
        });
        tree.setOnDragDone(event -> {
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
        for (Node n : tree.lookupAll(".scroll-bar")) {
            if (n instanceof ScrollBar) {
                ScrollBar bar = (ScrollBar) n;
                if (bar.getOrientation().equals(Orientation.VERTICAL)) {
                    result = bar;
                }
            }
        }       
        return result;
    }
   

    private TreeTableRow<Map<String, Object>> rowFactory(TreeTableView<Map<String, Object>> view) {
        TreeTableRow<Map<String, Object>> row = new TreeTableRow<>();
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
            }
        });

        row.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            if (acceptable(db, row)) {
                int index = (Integer) db.getContent(SERIALIZED_MIME_TYPE);
                TreeItem item = tree.getTreeItem(index);
                item.getParent().getChildren().remove(item);
                getTarget(row).getChildren().add(item);
                event.setDropCompleted(true);
                tree.getSelectionModel().select(item);
                event.consume();
            }           
        });
       
        return row;
    }
   
    private boolean acceptable(Dragboard db, TreeTableRow<Map<String, Object>> row) {
        boolean result = false;
        if (db.hasContent(SERIALIZED_MIME_TYPE)) {
            int index = (Integer) db.getContent(SERIALIZED_MIME_TYPE);
            if (row.getIndex() != index) {
                TreeItem target = getTarget(row);
                TreeItem item = tree.getTreeItem(index);
                result = !isParent(item, target);
            }
        }
        return result;
    }

    private TreeItem getTarget(TreeTableRow<Map<String, Object>> row) {
        TreeItem target = tree.getRoot();
        if (!row.isEmpty()) {
            target = row.getTreeItem();
        }
        return target;
    }
   
    // prevent loops in the tree
    private boolean isParent(TreeItem parent, TreeItem child) {
        boolean result = false;
        while (!result && child != null) {
            result = child.getParent() == parent;
            child = child.getParent();
        }
        return result;
    }

    private void setupData() {
        TreeItem<Map<String, Object>> europe = createItem(root, "Europe", "continent", 742500000L);
        TreeItem<Map<String, Object>> austria = createItem(europe, "Austria", "country", 847400L);
        createItem(austria, "Tyrol", "state", 728537L);       
        createItem(europe, "Russia ", "country", 144031000);
        createItem(europe, "Germany ", "country", 81276000);
        createItem(europe, "Turkey ", "country", 78214000);
        createItem(europe, "France ", "country", 67063000);
        createItem(europe, "Italy ", "country", 60963000);
        createItem(europe, "Spain ", "country", 46335000);
        createItem(europe, "Ukraine ", "country", 42850000);
        createItem(europe, "Poland ", "country", 38494000);
        createItem(europe, "Romania ", "country", 19822000);
        createItem(europe, "Kazakhstan ", "country", 17543000);
        createItem(europe, "Netherlands ", "country", 16933000);
        createItem(europe, "Belgium ", "country", 11259000);
        createItem(europe, "Greece ", "country", 10769000);
        createItem(europe, "Portugal ", "country", 10311000);
        createItem(europe, "Hungary ", "country", 9835000);
        createItem(europe, "Sweden ", "country", 9794000);
        createItem(europe, "Azerbaijan ", "country", 9651000);
        createItem(europe, "Belarus ", "country", 9481000);
        createItem(europe, "Switzerland ", "country", 8265000);
        createItem(europe, "Bulgaria ", "country", 7185000);
        createItem(europe, "Serbia ", "country", 7103000);
        createItem(europe, "Denmark ", "country", 5673000);
        createItem(europe, "Finland ", "country", 5475000);
        createItem(europe, "Slovakia ", "country", 5426000);
        createItem(europe, "Norway ", "country", 5194000);
        createItem(europe, "Ireland ", "country", 4630000);
        createItem(europe, "Croatia ", "country", 4230000);
        createItem(europe, "Bosnia and Georgia ", "country", 3707000);
        createItem(europe, "Moldova ", "country", 3564000);
        createItem(europe, "Armenia ", "country", 3010000);
        createItem(europe, "Lithuania ", "country", 2906000);
        createItem(europe, "Albania ", "country", 2887000);
        createItem(europe, "Macedonia ", "country", 2071000);
        createItem(europe, "Slovenia ", "country", 2065000);
        createItem(europe, "Latvia ", "country", 1979000);
        createItem(europe, "Kosovo ", "country", 1867000);
        createItem(europe, "Estonia ", "country", 1315000);
        createItem(europe, "Cyprus ", "country", 876000);
        createItem(europe, "Montenegro ", "country", 620000);
        createItem(europe, "Luxembourg ", "country", 570000);
        createItem(europe, "Transnistria ", "country", 505153);
        createItem(europe, "Malta ", "country", 425000);
        createItem(europe, "Iceland ", "country", 331000);
        createItem(europe, "Jersey (UK) ", "country", 103000);
        createItem(europe, "Andorra ", "country", 78000);
        createItem(europe, "Guernsey (UK) ", "country", 66000);
        createItem(europe, "Liechtenstein ", "country", 37000);
        createItem(europe, "Monaco ", "country", 37000);
        TreeItem<Map<String, Object>> america = createItem(root, "America", "continent", 953700000L);
        createItem(america, "USA", "country", 318900000L);
        createItem(america, "Mexico", "country", 122300000L);       
    }
   
    private TreeItem<Map<String, Object>> createItem(TreeItem<Map<String, Object>> parent, String region, String type, long population) {
        TreeItem<Map<String, Object>> item = new TreeItem<>();
        Map<String, Object> value = new HashMap<>();
        value.put("region",  region);
        value.put("type", type);
        value.put("population", population);
        item.setValue(value);
        parent.getChildren().add(item);
        item.setExpanded(true);
        return item;
    }
   
    protected void addColumn(String label, String dataIndex) {
        TreeTableColumn<Map<String, Object>, String> column = new TreeTableColumn<>(label);
        column.setPrefWidth(150);
        column.setCellValueFactory(
            (TreeTableColumn.CellDataFeatures<Map<String, Object>, String> param) -> {
                ObservableValue<String> result = new ReadOnlyStringWrapper("");
                if (param.getValue().getValue() != null) {
                    result = new ReadOnlyStringWrapper("" + param.getValue().getValue().get(dataIndex));
                }
                return result;
            }
        );       
        tree.getColumns().add(column);
    }
   

    public static void main(String[] args) {
        launch(args);
    }

}
