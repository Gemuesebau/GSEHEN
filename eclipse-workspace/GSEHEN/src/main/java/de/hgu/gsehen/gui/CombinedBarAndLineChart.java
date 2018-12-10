package de.hgu.gsehen.gui;

import de.hgu.gsehen.Gsehen;
import javafx.collections.transformation.SortedList;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;

import java.util.Date;
import java.util.ResourceBundle;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 * @author CW
 *     Via: https://www.boraji.com/how-to-combine-line-chart-and-bar-chart-in-jfreechart
 */
public class CombinedBarAndLineChart {

  protected final ResourceBundle mainBundle;
  private Gsehen gsehenInstance;

  {
    gsehenInstance = Gsehen.getInstance();
    mainBundle = ResourceBundle.getBundle("i18n.main", gsehenInstance.getSelectedLocale());
  }

  public ChartPanel chartPanel(SortedList<Data<Date, Number>> barList,
      SortedList<Data<Date, Number>> lineList) {

    // Create Category plot
    CategoryPlot plot = new CategoryPlot();

    // Add the first dataset and render as lines
    CategoryItemRenderer lineRenderer = new LineAndShapeRenderer();
    plot.setDataset(0, createDataset(lineList));
    plot.setRenderer(0, lineRenderer);

    // Add the second dataset and render as bars
    CategoryItemRenderer baRenderer = new BarRenderer();
    plot.setDataset(1, createDataset(barList));
    plot.setRenderer(1, baRenderer);

    // Set Axis
    plot.setDomainAxis(new CategoryAxis(mainBundle.getString("chart.date")));
    plot.setRangeAxis(new NumberAxis(mainBundle.getString("chart.water")));

    JFreeChart chart = new JFreeChart(plot);
    chart.removeLegend();

    ChartPanel panel = new ChartPanel(chart);
    return panel;
  }

  private DefaultCategoryDataset createDataset(SortedList<Data<Date, Number>> list) {

    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
    String series1 = "test";

    for (Data<Date, Number> data : list) {
      dataset.addValue(data.getYValue(), series1, data.getXValue());
    }

    return dataset;
  }

}
