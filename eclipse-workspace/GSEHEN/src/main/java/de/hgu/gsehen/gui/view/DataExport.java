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
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.ResourceBundle;

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
  private BorderPane pane;
  private GridPane centerGrid;
  private TreeTableView<Drawable> treeTableView;
  private Farm farm;
  private Text headline;

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
      if (treeTableView.getSelectionModel().getSelectedCells().get(i) != null) {
        TreeItem<Drawable> selectedItem = treeTableView.getSelectionModel().getSelectedCells()
            .get(i).getTreeItem();
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
              // Creating PDF document object
              exportDocument = new PDDocument();

              // Creating a blank page
              page = new PDPage(PDRectangle.A4);
              rect = page.getMediaBox();

              // Creating the PDDocumentInformation object
              PDDocumentInformation docInfo = exportDocument.getDocumentInformation();
              docInfo.setAuthor("GSEHEN");
              docInfo.setTitle("Datenexport des Betriebs \"" + farm.getName() + "\"");

              Calendar today = Calendar.getInstance();
              today.set(Calendar.HOUR_OF_DAY, 0);
              docInfo.setCreationDate(today);

              line = 1;

              try {
                // Adding the blank page to the document
                exportDocument.addPage(page);
                contentStream = new PDPageContentStream(exportDocument, page);

                // Begin the Content stream
                contentStream.beginText();
                // Setting the font to the Content stream
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
                // Setting the position for the line
                contentStream.newLineAtOffset(50, rect.getHeight() - 50 * (line));
                String head = "Datenexport des Betriebs \"" + farm.getName() + "\"";
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
                  String fieldAreaString = "m²: " + field.getArea();
                  contentStream.showText(fieldAreaString);
                  contentStream.newLine();
                  String fieldLocationString = "Ort: " + field.getLocation();
                  contentStream.showText(fieldLocationString);
                  contentStream.endText();
                  for (Plot plot : field.getPlots()) {
                    line += 1;
                    checkForNewPage();

                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA, 12);
                    contentStream.newLineAtOffset(150, rect.getHeight() - 50 * (line));
                    String plotString = plot.getName();
                    contentStream.showText(plotString);
                    contentStream.newLine();
                    contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, 9);
                    String plotAreaString = "m²: " + plot.getArea();
                    contentStream.showText(plotAreaString);
                    contentStream.newLine();
                    String plotLocationString = "Ort: " + plot.getLocation();
                    contentStream.showText(plotLocationString);
                    contentStream.endText();

                    int count = 1;
                    DecimalFormat df2 = new DecimalFormat("#.##");
                    String mwsMM = "mm";

                    for (ManualWaterSupply mws : plot.getManualData().getManualWaterSupply()) {
                      line += 1;
                      checkForNewPage();

                      contentStream.beginText();
                      contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, 12);
                      contentStream.newLineAtOffset(200, rect.getHeight() - 50 * (line));

                      String mwsHead = "Bewässerung #" + count;
                      contentStream.showText(mwsHead);
                      contentStream.newLine();

                      String mwsDate = "Datum: " + mws.getDate();
                      contentStream.showText(mwsDate);
                      contentStream.newLine();

                      String mwsIrrigation1 = "Bewässerung: ";
                      contentStream.showText(mwsIrrigation1);
                      if (mws.getIrrigation() > 0.0) {
                        contentStream.setNonStrokingColor(Color.BLUE);
                      } else {
                        contentStream.setNonStrokingColor(Color.RED);
                      }
                      String mwsIrrigation2 = df2.format(mws.getIrrigation());
                      contentStream.showText(mwsIrrigation2);
                      contentStream.setNonStrokingColor(Color.BLACK);
                      contentStream.showText(mwsMM);
                      contentStream.newLine();

                      String mwsPrecipitation1 = "Niederschlag: ";
                      contentStream.showText(mwsPrecipitation1);
                      if (mws.getPrecipitation() > 0.0) {
                        contentStream.setNonStrokingColor(Color.BLUE);
                      } else {
                        contentStream.setNonStrokingColor(Color.RED);
                      }
                      String mwsPrecipitation2 = df2.format(mws.getPrecipitation());
                      contentStream.showText(mwsPrecipitation2);
                      contentStream.setNonStrokingColor(Color.BLACK);
                      contentStream.showText(mwsMM);
                      contentStream.newLine();

                      contentStream.endText();
                      line += 1;
                      count++;
                    }
                  }
                }

                // Closing the content stream
                contentStream.close();

                // Saving the document
                exportDocument.save("C:/Users/cwitzke/Desktop/Gsehen_Datenexport.pdf");

                // Closing the document
                exportDocument.close();
              } catch (IOException e1) {
                // Auto-generated catch block
                e1.printStackTrace();
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
  }

  private void checkForNewPage() {
    // System.out.println(rect.getHeight() - 50 * (++line));
    if (rect.getHeight() - 50 * (line) < 50.0) {
      try {
        contentStream.close();
        page = new PDPage(PDRectangle.A4);
        rect = page.getMediaBox();
        exportDocument.addPage(page);
        contentStream = new PDPageContentStream(exportDocument, page);
        line = 1;
      } catch (IOException e1) {
        // Auto-generated catch block
        e1.printStackTrace();
      }
    }
  }

}
