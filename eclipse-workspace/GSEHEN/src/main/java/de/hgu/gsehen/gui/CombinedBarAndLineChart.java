package de.hgu.gsehen.gui;

import de.hgu.gsehen.Gsehen;
import javafx.collections.transformation.SortedList;
import javafx.scene.chart.XYChart.Data;
import java.awt.Color;
import java.util.Date;
import java.util.ResourceBundle;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.GroupedStackedBarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 * @author CW Via: https://www.boraji.com/how-to-combine-line-chart-and-bar-chart-in-jfreechart
 */
public class CombinedBarAndLineChart {

  protected final ResourceBundle mainBundle;
  private Gsehen gsehenInstance;
  private DefaultCategoryDataset lineDataset;
  private DefaultCategoryDataset barDataset;

  {
    gsehenInstance = Gsehen.getInstance();
    mainBundle = ResourceBundle.getBundle("i18n.main", gsehenInstance.getSelectedLocale());
  }

  public ChartPanel chartPanel(SortedList<Data<Date, Number>> precBarList,
      SortedList<Data<Date, Number>> irriBarList, SortedList<Data<Date, Number>> lineList) {

    JFreeChart chart = new JFreeChart(new CategoryPlot());
    chart.removeLegend();

    CategoryPlot plot = chart.getCategoryPlot();
    lineDataset = new DefaultCategoryDataset();
    barDataset = new DefaultCategoryDataset();

    // Set Axis
    plot.setDomainAxis(new CategoryAxis(mainBundle.getString("chart.date")));
    plot.setRangeAxis(new NumberAxis(mainBundle.getString("chart.water")));

    // Add the first dataset and render as lines
    CategoryItemRenderer lineRenderer = new LineAndShapeRenderer();
    lineRenderer.setSeriesPaint(0, Color.green);
    plot.setDataset(0, createDataset(lineList, "lineData"));
    plot.setRenderer(0, lineRenderer);

    // Add the second dataset and render as bars
    GroupedStackedBarRenderer barRenderer = new GroupedStackedBarRenderer();
    barRenderer.setSeriesPaint(1, Color.blue);
    barRenderer.setSeriesPaint(2, Color.yellow);
    plot.setDataset(1, createDataset(precBarList, "precData"));
    plot.setRenderer(1, barRenderer);
    plot.setDataset(2, createDataset(irriBarList, "irriData"));
    plot.setRenderer(2, barRenderer);

    ChartPanel panel = new ChartPanel(chart);
    return panel;
  }

  private DefaultCategoryDataset createDataset(SortedList<Data<Date, Number>> list, String d) {
    DefaultCategoryDataset dataSet = new DefaultCategoryDataset();

    if (d.equals("lineData")) {
      for (Data<Date, Number> data : list) {
        lineDataset.addValue(data.getYValue(), d, data.getXValue());
      }
      dataSet = lineDataset;
    }

    if (d.equals("precData") || d.equals("irriData")) {
      for (Data<Date, Number> data : list) {
        barDataset.addValue(data.getYValue(), d, data.getXValue());
      }
      dataSet = barDataset;
    }

    return dataSet;
  }

}
