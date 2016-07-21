package capsis.extension.datarenderer.drgraph;

import java.awt.Color;
import java.awt.Shape;

import javax.swing.JComponent;

import jeeb.lib.util.Translator;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleEdge;

/**
 * A data renderer for data extractors implementing DFListOfXYSeries and
 * DFCurves : draws scatter graphs. Version 3, replaces DRScatterPlot (original
 * 2003).
 * 
 * @author F. de Coligny - October 2015
 */
public class DRScatterGraph extends DRGraph {

	static final public String NAME = Translator.swap("DRScatterGraph");
	static final public String VERSION = "3.0";
	static final public String AUTHOR = "F. de Coligny";
	static final public String DESCRIPTION = Translator.swap("DRScatterGraph.description");

	/**
	 * Creates a XYSeriesCollection chart.
	 */
	protected JComponent createXYChart(GraphConverter c) {

		// Use a simple theme
		StandardChartTheme theme = (StandardChartTheme) StandardChartTheme.createLegacyTheme();
		ChartFactory.setChartTheme(theme);

		// Chart title -> leave blank, already written in frame title bar
		String title = "";
		// String title = c.getTitle();

//		JFreeChart chart = ChartFactory.createScatterPlot(
		JFreeChart chart = ChartFactory.createXYLineChart( // fc-7.1.2016 missing series with ScatterPlot...
				title, 
				c.getXAxisName(),// domain axis label
				c.getYAxisName(), // range axis label
				c.getXYSeriesCollection(), // data
				PlotOrientation.VERTICAL, // orientation
				true, // include legend
				true, // tooltips
				false // URLs?
				);

		// Set legend above the graph
		LegendTitle legend = chart.getLegend();
		legend.setPosition(RectangleEdge.TOP);
		legend.setFrame(BlockBorder.NONE);
		legend.setHorizontalAlignment(HorizontalAlignment.RIGHT); // bugs when
																	// too
																	// long...

		XYPlot plot = (XYPlot) chart.getPlot();
		
		// Tune the renderer
		boolean lines = false;
		boolean shapes = true;
		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(lines, shapes) {

			/**
			 * Shape scaling
			 */
			public Shape getItemShape(int row, int column) {
				Shape sup = super.getItemShape(row, column);
				return scaleShape(sup);
			}

		};

		plot.setRenderer(renderer);

		// Set colors
		int i = 0;
		for (Color color : c.getSeriesColors()) {
			renderer.setSeriesPaint(i++, color);
		}

		// Theme customization
		chart.setBackgroundPaint(Color.WHITE);
		plot.setBackgroundPaint(Color.WHITE);

		ChartPanel chartPanel = new GraphChartPanel(chart, dataBlock);

		return chartPanel;

	}

}
