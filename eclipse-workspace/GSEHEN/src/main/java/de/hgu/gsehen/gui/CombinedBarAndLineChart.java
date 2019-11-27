package de.hgu.gsehen.gui;

import static de.hgu.gsehen.util.MessageUtil.logMessage;

import de.hgu.gsehen.Gsehen;
import java.awt.Color;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.transformation.SortedList;
import javafx.scene.chart.XYChart.Data;
import javax.swing.JScrollPane;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.DateTickUnitType;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StackedXYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeTableXYDataset;
import org.jfree.data.xy.XYDataset;

/**
 * Graphic for irrigation, precipitation, current available soil water &amp; current total water
 * balance.
 * 
 * @author CW
 */
public class CombinedBarAndLineChart {
  private static final Logger LOGGER = Logger.getLogger(CombinedBarAndLineChart.class.getName());

  protected final ResourceBundle mainBundle;
  private Gsehen gsehenInstance;
  private SortedList<Data<Date, Number>> caswDataList;
  private SortedList<Data<Date, Number>> twbDataList;
  private SortedList<Data<Date, Number>> precDataList;
  private SortedList<Data<Date, Number>> irriDataList;

  {
    gsehenInstance = Gsehen.getInstance();
    mainBundle = ResourceBundle.getBundle("i18n.main", gsehenInstance.getSelectedLocale());
  }

  /**
   * Creates the chart(Panel).
   * 
   * @param precBarList
   *          - list with precipitation data.
   * @param irriBarList
   *          - list with irrigation data.
   * @param caswList
   *          - list with current available soil water data.
   * @param twbList
   *          - list with total water balance.
   * @return a scroll pane with plots based on the given data
   */
  public JScrollPane scrollPane(SortedList<Data<Date, Number>> precBarList,
      SortedList<Data<Date, Number>> irriBarList, SortedList<Data<Date, Number>> caswList,
      SortedList<Data<Date, Number>> twbList) {

    caswDataList = caswList;
    twbDataList = twbList;
    precDataList = precBarList;
    irriDataList = irriBarList;

    JFreeChart chart = ChartFactory.createTimeSeriesChart(mainBundle.getString("chart.name"),
        mainBundle.getString("chart.date"), mainBundle.getString("chart.water"), null);
    XYPlot plot = (XYPlot)chart.getPlot();
    plot.setBackgroundPaint(Color.white);
    plot.setDomainGridlinePaint(Color.gray);
    plot.setRangeGridlinePaint(Color.gray);
    plot.setDomainPannable(true);
    DateAxis axis = (DateAxis)plot.getDomainAxis();
    axis.setTickUnit(new DateTickUnit(DateTickUnitType.DAY, 1));
    axis.setVerticalTickLabels(true);

    ValueMarker marker = new ValueMarker(0);
    marker.setPaint(Color.black);
    marker.setStroke(new java.awt.BasicStroke(2));
    plot.addRangeMarker(marker);

    // Create the datasets
    plot.setDataset(0, createCaswDataset());
    plot.setDataset(1, createTwbDataset());
    plot.setDataset(2, createPrecDataset());
    plot.setDataset(3, createIrriDataset());

    plot.setRenderer(0, createRenderer(Color.green));
    plot.setRenderer(1, createRenderer(Color.red));
    StackedXYBarRenderer.setDefaultShadowsVisible(false);
    plot.setRenderer(2, createEventRenderer(Color.blue));
    plot.setRenderer(3, createEventRenderer(Color.yellow));

    return new JScrollPane(new ChartPanel(chart));
  }

  private XYDataset createCaswDataset() {
    return createDataset("dataexport.soilwater", caswDataList);
  }

  private XYDataset createTwbDataset() {
    return createDataset("dataexport.totalwater", twbDataList);
  }

  private TimeTableXYDataset createPrecDataset() {
    return createEventDataSet("dataexport.precipitation", precDataList);
  }

  private TimeTableXYDataset createIrriDataset() {
    return createEventDataSet("dataexport.irrigation", irriDataList);
  }

  private TimeTableXYDataset createEventDataSet(final String eventNameKey,
      final SortedList<Data<Date, Number>> dataList) {
    TimeTableXYDataset dataset = new TimeTableXYDataset();
    for (Data<Date, Number> data : dataList) {
      dataset.add(toDay(data), (double)data.getYValue(),
          mainBundle.getString(eventNameKey));
    }
    return dataset;
  }

  private XYDataset createDataset(final String seriesNameKey,
      final SortedList<Data<Date, Number>> dataList) {
    /*                       FINE    */
    logMessage(LOGGER, Level.CONFIG, "creating.dataset.of.size", seriesNameKey, dataList.size());
    TimeSeriesCollection dataset = new TimeSeriesCollection();
    TimeSeries timeSeries = new TimeSeries(mainBundle.getString(seriesNameKey));
    for (Data<Date, Number> data : dataList) {
      timeSeries.add(toDay(data), data.getYValue());
    }
    dataset.addSeries(timeSeries);
    return dataset;
  }

  private Day toDay(Data<Date, Number> data) {
    LocalDate local = data.getXValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    return new Day(local.getDayOfMonth(), local.getMonthValue(), local.getYear());
  }

  private XYItemRenderer createRenderer(Color color) {
    final XYItemRenderer renderer = new XYLineAndShapeRenderer();
    renderer.setSeriesPaint(0, color);
    return renderer;
  }

  private XYItemRenderer createEventRenderer(Color color) {
    final XYItemRenderer renderer = new StackedXYBarRenderer();
    renderer.setSeriesPaint(0, color);
    return renderer;
  }
}
