package de.hgu.gsehen.gui.view;

import be.quodlibet.boxable.BaseTable;
import be.quodlibet.boxable.Cell;
import be.quodlibet.boxable.HorizontalAlignment;
import be.quodlibet.boxable.Row;
import com.jfoenix.controls.JFXCheckBox;
import de.hgu.gsehen.Gsehen;
import de.hgu.gsehen.gui.GsehenGuiElements;
import de.hgu.gsehen.model.Drawable;
import de.hgu.gsehen.model.Farm;
import de.hgu.gsehen.model.Field;
import de.hgu.gsehen.model.ManualWaterSupply;
import de.hgu.gsehen.model.Plot;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import javafx.stage.FileChooser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

@SuppressWarnings("unchecked")
public class DataExport {
  /*
   * PDFBox: http://www.apache.org/licenses/LICENSE-2.0.txt & https://pdfbox.apache.org/download.cgi
   */
  private static final String FARM_TREE_VIEW_ID = "#farmTreeView";
  private Gsehen gsehenInstance = Gsehen.getInstance();
  protected final ResourceBundle mainBundle = ResourceBundle.getBundle("i18n.main",
      gsehenInstance.getSelectedLocale());
  private List<Plot> plotList;
  private BorderPane pane;
  private GridPane centerGrid;
  private TreeTableView<Drawable> treeTableView = (TreeTableView<Drawable>)gsehenInstance.getScene()
        .lookup(FARM_TREE_VIEW_ID);
  private Farm farm;
  private Text headline;
  private int fieldCounter;
  private int plotCounter;
  private Boolean mwsCheck;
  private Text mwsWarning;
  private DecimalFormat df2;

  private PDDocument exportDocument;
  private PDPage page;
  private PDRectangle rect;
  private int line;
  private PDPageContentStream contentStream;
  private Cell<PDPage> cell;
  private Row<PDPage> headerRow;
  private Row<PDPage> row;

  /**
   * Constructs a new data export associated with the given BorderPane.
   *
   * @param application the Gsehen application singleton reference
   * @param pane
   *          - the associated BorderPane.
   */
  public DataExport(Gsehen application, BorderPane pane) {
    this.gsehenInstance = application;
    this.pane = pane;
  }

  /**
   * Creates the view.
   */
  @SuppressWarnings({ "checkstyle:all" })
  public void createExport() {
    for (int i = 0; i < treeTableView.getSelectionModel().getSelectedCells().size(); i++) {
      List<Farm> farmsList = gsehenInstance.getFarmsList();
      if (treeTableView.getSelectionModel().getSelectedCells().get(i) != null) {
        TreeItem<Drawable> selectedItem = treeTableView.getSelectionModel().getSelectedCells()
            .get(i).getTreeItem();
        if (selectedItem != null
            && selectedItem.getValue().getClass().getSimpleName().equals("Plot")) {
          Plot plot = (Plot) selectedItem.getValue();
          for (Farm farm : farmsList) {
            for (Field field : farm.getFields()) {
              if (field.getPlots().contains(plot)) {
                this.farm = farm;
              }
            }
          }
        }
        if (selectedItem != null
            && selectedItem.getValue().getClass().getSimpleName().equals("Field")) {
          Field field = (Field) selectedItem.getValue();
          for (Farm farm : farmsList) {
            if (farm.getFields().contains(field)) {
              this.farm = farm;
            }
          }
        }
        if (selectedItem != null
            && selectedItem.getValue().getClass().getSimpleName().equals("Farm")) {
          farm = (Farm) selectedItem.getValue();
        }

        pane.setVisible(true);

        // GridPane - Center Section
        centerGrid = GsehenGuiElements.gridPane(pane);

        headline = GsehenGuiElements.text(
            mainBundle.getString("dataexport.head") + " \"" + farm.getName() + "\"",
            FontWeight.BOLD);

        fieldCounter = 3;
        plotCounter = 0;
        plotList = new ArrayList<Plot>();
        mwsCheck = false;

        for (Field field : farm.getFields()) {
          JFXCheckBox allCheckBox = new JFXCheckBox(mainBundle.getString("dataexport.all"));
          allCheckBox.setStyle("-fx-font-weight: bold");
          Text fieldText = GsehenGuiElements.text(field.getName());

          GridPane.setConstraints(fieldText, 0, fieldCounter);
          GridPane.setConstraints(allCheckBox, 1, fieldCounter);
          centerGrid.getChildren().addAll(fieldText, allCheckBox);

          for (Plot plot : field.getPlots()) {
            plotCounter = fieldCounter + 1;
            JFXCheckBox plotCheckBox = new JFXCheckBox(plot.getName());
            if (plot.getManualData() != null) {
              mwsCheck = true;
            }

            allCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
              @Override
              public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue,
                  Boolean newValue) {
                plotCheckBox.setSelected(newValue);
              }
            });

            plotCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
              @Override
              public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue,
                  Boolean newValue) {
                if (newValue == true && !plotList.contains(plot)) {
                  plotList.add(plot);
                } else {
                  plotList.remove(plot);
                }
              }
            });

            GridPane.setConstraints(plotCheckBox, 1, plotCounter);
            centerGrid.getChildren().add(plotCheckBox);
            fieldCounter += 1;
          }
          Separator separator = new Separator();
          GridPane.setConstraints(separator, 0, plotCounter + 1, 2, 1);
          centerGrid.getChildren().add(separator);

          fieldCounter = plotCounter + 2;
        }

        Button exportButton = GsehenGuiElements.button(150);
        exportButton.setText(mainBundle.getString("dataexport.export"));
        exportButton.setOnAction(new EventHandler<ActionEvent>() {

          @Override
          public void handle(ActionEvent e) {
            if (mwsCheck == true) {
              if (centerGrid.getChildren().contains(mwsWarning)) {
                centerGrid.getChildren().remove(mwsWarning);
              }
              createDocument();
              try {
                writeText();
                save();
              } catch (IOException e1) {
                e1.printStackTrace();
              }
            } else {
              mwsWarning = GsehenGuiElements.text(mainBundle.getString("dataexport.mwswarning"),
                  FontWeight.BOLD);
              mwsWarning.setFill(javafx.scene.paint.Color.RED);
              GridPane.setConstraints(mwsWarning, 0, plotCounter + 4);
              centerGrid.getChildren().add(mwsWarning);
            }
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

  private void createDocument() {
    // Creating PDF document object
    exportDocument = new PDDocument();

    // Creating a blank page
    page = new PDPage(PDRectangle.A4);
    rect = page.getMediaBox();

    // Creating the PDDocumentInformation object
    PDDocumentInformation docInfo = exportDocument.getDocumentInformation();
    docInfo.setAuthor((mainBundle.getString("gsehen.name")));
    docInfo.setTitle(mainBundle.getString("dataexport.head") + " \"" + farm.getName() + "\"");

    Calendar today = Calendar.getInstance();
    today.set(Calendar.HOUR_OF_DAY, 0);
    docInfo.setCreationDate(today);

    line = 1;
  }

  private void writeText() throws IOException {
    // Adding the blank page to the document
    exportDocument.addPage(page);
    contentStream = new PDPageContentStream(exportDocument, page);

    // create the table
    float margin = 50;
    // starting y position is whole page height subtracted by top and bottom margin
    float startNewPageY = page.getMediaBox().getHeight() - (2 * margin);
    // we want table across whole page width (subtracted by left and right margin ofcourse)
    float tableWidth = page.getMediaBox().getWidth() - (2 * margin);

    boolean drawContent = true;
    float bottomMargin = 70;
    // y position is your coordinate of top left corner of the table
    float positionY = 750;

    BaseTable table = new BaseTable(positionY, startNewPageY, bottomMargin, tableWidth, margin,
        exportDocument, page, true, drawContent);

    // Begin the Content stream
    contentStream.beginText();
    // Setting the font to the Content stream
    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 20);
    // Setting the position for the line
    contentStream.newLineAtOffset(50, rect.getHeight() - 50 * (line));
    String head = mainBundle.getString("dataexport.head") + " \"" + farm.getName() + "\"";
    // Adding text in the form of string
    contentStream.showText(head);
    // Ending the content stream
    contentStream.endText();
    line += 1;

    // // Setting the leading
    contentStream.setLeading(14.5f);

    df2 = new DecimalFormat("#.##");

    for (Field field : farm.getFields()) {
      for (Plot plot : plotList) {
        for (Plot fieldPlot : field.getPlots()) {
          if (plot == fieldPlot) {
            headerRow = table.createRow(14.5f);
            cell = headerRow.createCell(100, field.getName() + " - " + plot.getName());
            cell.setFillColor(Color.LIGHT_GRAY);
            cell.setFont(PDType1Font.HELVETICA_BOLD);
            cell.setFontSize(12);
            cell.setAlign(HorizontalAlignment.CENTER);
            table.addHeaderRow(headerRow);

            headerRow = table.createRow(14.5f);
            cell = headerRow.createCell(25, "");
            cell.setFillColor(Color.BLACK);
            cell = headerRow.createCell(25, mainBundle.getString("plotview.date"));
            cell.setFont(PDType1Font.HELVETICA_BOLD);
            cell = headerRow.createCell(25, mainBundle.getString("plotview.irrigation"));
            cell.setFont(PDType1Font.HELVETICA_BOLD);
            cell = headerRow.createCell(25, mainBundle.getString("plotview.precipitation"));
            cell.setFont(PDType1Font.HELVETICA_BOLD);
            table.addHeaderRow(headerRow);

            int count = 1;
            if (plot.getManualData() != null) {
              for (ManualWaterSupply mws : plot.getManualData().getManualWaterSupply()) {
                String mwsHead = mainBundle.getString("dataexport.mwshead") + count;

                row = table.createRow(12);
                cell = row.createCell(25, mwsHead);
                cell = row.createCell(25, mws.getDate().toString());
                cell = row.createCell(25, df2.format(mws.getIrrigation()) + "mm");
                cell = row.createCell(25, df2.format(mws.getPrecipitation()) + "mm");

                count++;
              }
            }
          }
        }
      }
    }

    // Closing the content stream
    contentStream.close();
    table.draw();
  }

  private void save() throws IOException {
    FileChooser fileChooser = new FileChooser();
    LocalDate localDate = LocalDate.now();
    fileChooser.setInitialFileName(
        mainBundle.getString("dataexport.head") + " " + farm.getName() + "_(" + localDate + ")");
    FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PDF files (*.pdf)",
        "*.pdf");
    fileChooser.getExtensionFilters().add(extFilter);
    File file = fileChooser.showSaveDialog(gsehenInstance.getScene().getWindow());

    // Saving the document
    exportDocument.save(file);

    // Closing the document
    exportDocument.close();
  }

}
