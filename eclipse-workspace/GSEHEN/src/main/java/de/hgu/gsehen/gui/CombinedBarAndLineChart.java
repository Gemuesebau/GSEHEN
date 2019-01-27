package de.hgu.gsehen.gui;

import de.hgu.gsehen.Gsehen;

import java.awt.Color;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.ResourceBundle;

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
 * Graphic for irrigation, precipitation, current available soil water & current total water
 * balance.
 * 
 * @author CW
 */
public class CombinedBarAndLineChart {

  protected final ResourceBundle mainBundle;
  private Gsehen gsehenInstance;
  private SortedList<Data<Date, Number>> caswDataList;
  private SortedList<Data<Date, Number>> twbDataList;
  private SortedList<Data<Date, Number>> precDataList;
  private SortedList<Data<Date, Number>> irriDataList;
  private XYDataset dataSet;

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
   */
  public JScrollPane scrollPane(SortedList<Data<Date, Number>> precBarList,
      SortedList<Data<Date, Number>> irriBarList, SortedList<Data<Date, Number>> caswList,
      SortedList<Data<Date, Number>> twbList) {

    caswDataList = caswList;
    twbDataList = twbList;
    precDataList = precBarList;
    irriDataList = irriBarList;

    // Create chart
    JFreeChart chart = ChartFactory.createTimeSeriesChart(mainBundle.getString("chart.name"),
        mainBundle.getString("chart.date"), mainBundle.getString("chart.water"), dataSet);

    // Create the plot and set tick-unit to 1 day
    XYPlot plot = (XYPlot) chart.getPlot();
    plot.setBackgroundPaint(Color.white);
    plot.setDomainGridlinePaint(Color.gray);
    plot.setRangeGridlinePaint(Color.gray);
    plot.setDomainPannable(true);
    DateTickUnit tickUnit = new DateTickUnit(DateTickUnitType.DAY, 1);
    DateAxis axis = (DateAxis) plot.getDomainAxis();
    axis.setTickUnit(tickUnit);
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

    StackedXYBarRenderer.setDefaultShadowsVisible(false);

    // Create and set the renderer
    XYItemRenderer caswRenderer = new XYLineAndShapeRenderer();
    XYItemRenderer twbRenderer = new XYLineAndShapeRenderer();
    StackedXYBarRenderer precRenderer = new StackedXYBarRenderer();
    StackedXYBarRenderer irriRenderer = new StackedXYBarRenderer();
    plot.setRenderer(0, caswRenderer);
    plot.setRenderer(1, twbRenderer);
    plot.setRenderer(2, precRenderer);
    plot.setRenderer(3, irriRenderer);
    plot.getRendererForDataset(plot.getDataset(0)).setSeriesPaint(0, Color.green);
    plot.getRendererForDataset(plot.getDataset(1)).setSeriesPaint(0, Color.red);
    plot.getRendererForDataset(plot.getDataset(2)).setSeriesPaint(0, Color.blue);
    plot.getRendererForDataset(plot.getDataset(3)).setSeriesPaint(0, Color.yellow);

    // define 'dataSet'
    dataSet = plot.getDataset();

    ChartPanel panel = new ChartPanel(chart);
    JScrollPane scrollPane = new JScrollPane(panel);

    return scrollPane;
  }

  private XYDataset createCaswDataset() {
    TimeSeriesCollection dataset = new TimeSeriesCollection();

    TimeSeries caswSeries = new TimeSeries(mainBundle.getString("dataexport.soilwater"));

    for (Data<Date, Number> data : caswDataList) {
      Date date = data.getXValue();
      LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
      int year = localDate.getYear();
      int month = localDate.getMonthValue();
      int day = localDate.getDayOfMonth();

      caswSeries.add(new Day(day, month, year), data.getYValue());
    }

    dataset.addSeries(caswSeries);

    return dataset;
  }

  private XYDataset createTwbDataset() {
    TimeSeriesCollection dataset = new TimeSeriesCollection();

    TimeSeries twbSeries = new TimeSeries(mainBundle.getString("dataexport.totalwater"));

    for (Data<Date, Number> data : twbDataList) {
      Date date = data.getXValue();
      LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
      int year = localDate.getYear();
      int month = localDate.getMonthValue();
      int day = localDate.getDayOfMonth();

      twbSeries.add(new Day(day, month, year), data.getYValue());
    }

    dataset.addSeries(twbSeries);

    return dataset;
  }

  private TimeTableXYDataset createPrecDataset() {
    TimeTableXYDataset dataset = new TimeTableXYDataset();

    // TimeSeries precSeries = new TimeSeries("Prec. Series");

    for (Data<Date, Number> data : precDataList) {
      Date date = data.getXValue();
      LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
      int year = localDate.getYear();
      int month = localDate.getMonthValue();
      int day = localDate.getDayOfMonth();

      dataset.add(new Day(day, month, year), (double) data.getYValue(),
          mainBundle.getString("dataexport.precipitation"));
    }

    // dataset.addSeries(precSeries);

    return dataset;
  }

  private TimeTableXYDataset createIrriDataset() {
    TimeTableXYDataset dataset = new TimeTableXYDataset();

    // TimeSeries irriSeries = new TimeSeries("Irri. Series");

    for (Data<Date, Number> data : irriDataList) {
      Date date = data.getXValue();
      LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
      int year = localDate.getYear();
      int month = localDate.getMonthValue();
      int day = localDate.getDayOfMonth();

      dataset.add(new Day(day, month, year), (double) data.getYValue(),
          mainBundle.getString("dataexport.irrigation"));
    }

    // dataset.addSeries(irriSeries);

    return dataset;
  }

}
