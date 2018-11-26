package de.hgu.gsehen.gui.view;

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
  protected final ResourceBundle mainBundle;
  private GsehenGuiElements gsehenGuiElements;
  private Gsehen gsehenInstance;
  private List<Plot> plotList;
  private BorderPane pane;
  private GridPane centerGrid;
  private TreeTableView<Drawable> treeTableView;
  private Farm farm;
  private Plot plot;
  private Text headline;
  private int fieldCounter;
  private int plotCounter;
  private Boolean mwsCheck;
  private Text mwsWarning;

  private PDDocument exportDocument;
  private PDPage page;
  private PDRectangle rect;
  private int line;
  private PDPageContentStream contentStream;

  {
    gsehenInstance = Gsehen.getInstance();
    gsehenGuiElements = new GsehenGuiElements();

    mainBundle = ResourceBundle.getBundle("i18n.main", gsehenInstance.getSelectedLocale());

    treeTableView = (TreeTableView<Drawable>) Gsehen.getInstance().getScene()
        .lookup(FARM_TREE_VIEW_ID);
  }

  /**
   * Constructs a new data export associated with the given BorderPane.
   *
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
        centerGrid = gsehenGuiElements.gridPane(pane);

        headline = gsehenGuiElements.text(
            mainBundle.getString("dataexport.head") + " \"" + farm.getName() + "\"",
            FontWeight.BOLD);

        fieldCounter = 3;
        plotCounter = 0;
        plotList = new ArrayList<Plot>();
        mwsCheck = false;

        for (Field field : farm.getFields()) {
          JFXCheckBox allCheckBox = new JFXCheckBox(mainBundle.getString("dataexport.all"));
          allCheckBox.setStyle("-fx-font-weight: bold");
          Text fieldText = gsehenGuiElements.text(field.getName());

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

        Button exportButton = gsehenGuiElements.button(150);
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
                // Auto-generated catch block
                e1.printStackTrace();
              }
            } else {
              mwsWarning = gsehenGuiElements.text(mainBundle.getString("dataexport.mwswarning"),
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
    docInfo.setAuthor("GSEHEN");
    docInfo.setTitle(mainBundle.getString("dataexport.head") + " \"" + farm.getName() + "\"");

    Calendar today = Calendar.getInstance();
    today.set(Calendar.HOUR_OF_DAY, 0);
    docInfo.setCreationDate(today);

    line = 1;
  }

  private void checkForNewPage() {
    if (rect.getHeight() - 50 * (line) < 50.0) {
      try {
        contentStream.close();
        page = new PDPage(PDRectangle.A4);
        rect = page.getMediaBox();
        exportDocument.addPage(page);
        contentStream = new PDPageContentStream(exportDocument, page);
        contentStream.setLeading(14.5f);
        line = 1;
      } catch (IOException e1) {
        // Auto-generated catch block
        e1.printStackTrace();
      }
    }
  }

  private void writeText() throws IOException {
    // Adding the blank page to the document
    exportDocument.addPage(page);
    contentStream = new PDPageContentStream(exportDocument, page);

    // Begin the Content stream
    contentStream.beginText();
    // Setting the font to the Content stream
    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
    // Setting the position for the line
    contentStream.newLineAtOffset(50, rect.getHeight() - 50 * (line));
    String head = mainBundle.getString("dataexport.head") + " \"" + farm.getName() + "\"";
    // Adding text in the form of string
    contentStream.showText(head);
    // Ending the content stream
    contentStream.endText();
    line += 1;

    contentStream.beginText();
    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
    contentStream.newLineAtOffset(50, rect.getHeight() - 50 * (line));
    String farmString = farm.getName();
    contentStream.showText(farmString);
    contentStream.endText();

    // Setting the leading
    contentStream.setLeading(14.5f);

    for (Field field : farm.getFields()) {
      line += 1;
      checkForNewPage();

      contentStream.beginText();
      contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
      contentStream.newLineAtOffset(100, rect.getHeight() - 50 * (line));
      String fieldString = field.getName();
      contentStream.showText(fieldString);
      contentStream.newLine();
      contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, 9);

      DecimalFormat df2 = new DecimalFormat("#.##");

      String fieldAreaString = mainBundle.getString("fieldview.area") + " "
          + df2.format(field.getArea());
      contentStream.showText(fieldAreaString);
      contentStream.newLine();
      String fieldLocationString = mainBundle.getString("dataexport.latlng") + ": "
          + field.getLocation().getLat() + " / " + field.getLocation().getLng();
      contentStream.showText(fieldLocationString);
      contentStream.endText();
      for (Plot plot : plotList) {
        for (Plot fieldPlot : field.getPlots()) {
          if (plot == fieldPlot) {
            this.plot = plot;
            line += 1;
            checkForNewPage();

            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.newLineAtOffset(150, rect.getHeight() - 50 * (line));
            String plotString = plot.getName();
            contentStream.showText(plotString);
            contentStream.newLine();
            contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, 9);
            String plotAreaString = mainBundle.getString("fieldview.area") + " "
                + df2.format(plot.getArea());
            contentStream.showText(plotAreaString);
            contentStream.newLine();
            String plotLocationString = mainBundle.getString("dataexport.latlng") + ": "
                + plot.getLocation().getLat() + " / " + plot.getLocation().getLng();
            contentStream.showText(plotLocationString);

            if (plot.getRecommendedAction() != null) {
              contentStream.newLine();
              setActionText();
              contentStream.newLine();
            }

            contentStream.endText();

            int count = 1;
            if (plot.getManualData() != null) {
              for (ManualWaterSupply mws : plot.getManualData().getManualWaterSupply()) {
                line += 1;
                checkForNewPage();

                contentStream.beginText();
                contentStream.newLine();
                contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, 12);
                contentStream.newLineAtOffset(200, rect.getHeight() - 50 * (line));

                String mwsHead = mainBundle.getString("dataexport.mwshead") + count;
                contentStream.showText(mwsHead);
                contentStream.newLine();

                String mwsDate = mainBundle.getString("plotview.date") + " " + mws.getDate();
                contentStream.showText(mwsDate);
                contentStream.newLine();

                String mwsIrrigation1 = mainBundle.getString("plotview.irrigation") + " ";
                contentStream.showText(mwsIrrigation1);
                if (mws.getIrrigation() > 0.0) {
                  contentStream.setNonStrokingColor(Color.BLUE);
                } else {
                  contentStream.setNonStrokingColor(Color.RED);
                }

                String mwsMm = "mm";

                String mwsIrrigation2 = df2.format(mws.getIrrigation());
                contentStream.showText(mwsIrrigation2);
                contentStream.setNonStrokingColor(Color.BLACK);
                contentStream.showText(mwsMm);
                contentStream.newLine();

                String mwsPrecipitation1 = mainBundle.getString("plotview.precipitation") + " ";
                contentStream.showText(mwsPrecipitation1);
                if (mws.getPrecipitation() > 0.0) {
                  contentStream.setNonStrokingColor(Color.BLUE);
                } else {
                  contentStream.setNonStrokingColor(Color.RED);
                }
                String mwsPrecipitation2 = df2.format(mws.getPrecipitation());
                contentStream.showText(mwsPrecipitation2);
                contentStream.setNonStrokingColor(Color.BLACK);
                contentStream.showText(mwsMm);
                contentStream.newLine();

                contentStream.endText();
                line += 1;
                count++;
              }
            }
          }
        }
      }
    }

    // Closing the content stream
    contentStream.close();
  }

  private void setActionText() {
    try {
      if (plot.getRecommendedAction().getRecommendation().toString().equals("EXCESS")) {
        String recommendedAction1 = mainBundle.getString("treetableview.watering") + " "
            + mainBundle.getString("dataexport.excess1");
        contentStream.showText(recommendedAction1);
        contentStream.newLine();
        String recommendedAction2 = mainBundle.getString("dataexport.excess2");
        contentStream.showText(recommendedAction2);
        line += 1;
      } else if (plot.getRecommendedAction().getRecommendation().toString().equals("PAUSE")
          || plot.getRecommendedAction().getRecommendation().toString().equals("IRRIGATION")
          || plot.getRecommendedAction().getRecommendation().toString().equals("NO_DATA")
          || plot.getRecommendedAction().getRecommendation().toString().equals("TOMORROW")) {
        String recommendedAction = mainBundle.getString("treetableview.watering") + " " + mainBundle
            .getString(plot.getRecommendedAction().getRecommendation().getMessagePropertyKey());
        contentStream.showText(recommendedAction);
      } else if (plot.getRecommendedAction().getRecommendation().toString().equals("SOON")) {
        String recommendedAction1 = mainBundle.getString("treetableview.watering") + " "
            + mainBundle.getString("dataexport.soon1");
        contentStream.showText(recommendedAction1);
        contentStream.newLine();
        String recommendedAction2 = mainBundle.getString("dataexport.soon2");
        contentStream.showText(recommendedAction2);
        line += 1;
      } else if (plot.getRecommendedAction().getRecommendation().toString().equals("NOW")) {
        String recommendedAction1 = mainBundle.getString("treetableview.watering") + " "
            + mainBundle.getString("dataexport.now1");
        contentStream.showText(recommendedAction1);
        contentStream.newLine();
        String recommendedAction2 = mainBundle.getString("dataexport.now2");
        contentStream.showText(recommendedAction2);
        line += 1;
      }
    } catch (IOException e) {
      // Auto-generated catch block
      e.printStackTrace();
    }
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
