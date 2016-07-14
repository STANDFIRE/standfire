package capsis.extension.datarenderer.drgraph;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseListener;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JComponent;

import jeeb.lib.util.DefaultNumberFormat;
import jeeb.lib.util.Log;
import jeeb.lib.util.Settings;
import jeeb.lib.util.Translator;
import jeeb.lib.util.extensionmanager.ExtensionManager;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleEdge;

import capsis.app.CapsisExtensionManager;
import capsis.extension.PanelDataRenderer;
import capsis.extension.dataextractor.format.DFCurves;
import capsis.extension.dataextractor.format.DFListOfCategories;
import capsis.extension.datarenderer.jfreechart.MessagePanel;
import capsis.extensiontype.DataBlock;
import capsis.extensiontype.DataExtractor;
import capsis.util.ConfigurationPanel;

/**
 * A data renderer for data extractors implementing DFListOfXYSeries and
 * DFCurves : draws histigrams. Version 3, replaces DRHistogram (original 2003).
 * 
 * @author F. de Coligny - October 2015
 */
public class DRBarGraph extends PanelDataRenderer {

	static {
		Translator.addBundle("capsis.extension.datarenderer.drgraph.Labels");
	}

	static final public String NAME = Translator.swap("DRBarGraph");
	static final public String VERSION = "3.0";
	static final public String AUTHOR = "F. de Coligny";
	static final public String DESCRIPTION = Translator.swap("DRBarGraph.description");

	protected boolean visibleValues;
	
	/**
	 * Constructor
	 */
	public DRBarGraph() {
		super();
	}

	/**
	 * Inits the graph
	 */
	@Override
	public void init(DataBlock db) {
		try {
			super.init(db);

			visibleValues = Settings.getProperty("drbargraph.visibleValues", false);

		} catch (Exception e) {
			Log.println(Log.ERROR, "DRBarGraph.init ()", "Error in init (), wrote in Log and passed", e);
		}

	}

	/**
	 * Tells if the renderer can show an extractor's production. True if the
	 * extractor is an instance of the renderer's compatible data formats Note:
	 * DataExtractor must implement a data format in order to be recognized by
	 * DataRenderers.
	 */
	static public boolean matchWith(Object target) {

		return target instanceof DataExtractor && (target instanceof DFListOfCategories || target instanceof DFCurves);

	}

	/**
	 * Update strategy for DRGraph and its subclasses. This method is used to
	 * refresh the renderer after configuration.
	 */
	public void update() {
		super.update();
		removeAll();
		add(createView(), BorderLayout.CENTER);
		revalidate();
	}

	/**
	 * One single chart with maybe several histograms inside.
	 */
	protected JComponent createView() {
		try {

			// Consider all extractors
			Collection extractors = dataBlock.getDataExtractors();
			Collection specialExtractors = dataBlock.getSpecialExtractors();

			List allExtractors = new ArrayList<>(extractors);
			if (specialExtractors != null) {
				allExtractors.addAll(specialExtractors);
			}

			// Extractors have all of the same class: consider the first
			Object representative = allExtractors.iterator().next();

			CategoryConverter c = null;

			if (representative instanceof DFCurves) {
				// Case of DFCurve instances
				List<DFCurves> dataList = new ArrayList<DFCurves>();
				for (Object e : allExtractors) {
					dataList.add((DFCurves) e);
				}

				// WARNING: DFCurves may contain extra XYSeries... (since Teresa
				// Fonseca SDI graph with two additional lines for min and max
				// sdi corridor)
				// -> Remove extra XYSeries, replace by a DFListOfXYSeries

				c = CategoryConverter.convertDFCurves(dataList);

				// TMP: fc-7.1.2016 pb on pp3 DEBiomassDistrib histogram (found it...)
//				System.out.println(c.toString ());
				
			} else if (representative instanceof DFListOfCategories) {
				// Case of DFListOfCategories instances
				List<DFListOfCategories> dataList = new ArrayList<DFListOfCategories>();
				for (Object e : allExtractors) {
					dataList.add((DFListOfCategories) e);
				}
				c = CategoryConverter.convertDFListOfCategories(dataList);
			}

			return createCategoryChart(c);

		} catch (Exception e) {
			Log.println(Log.ERROR, "DRBarGraph.createView ()", "Exception", e);
			return new MessagePanel(CapsisExtensionManager.getInstance().getName(this) + " "
					+ Translator.swap("DRBarGraph.canNotShowTheseDataSeeLog"), (MouseListener) this);
		}

	}

	/**
	 * Creates a category chart.
	 */
	protected JComponent createCategoryChart(CategoryConverter c) {

		// Use a simple theme
		StandardChartTheme theme = (StandardChartTheme) StandardChartTheme.createLegacyTheme();
		ChartFactory.setChartTheme(theme);

		// Chart title -> leave blank, already written in frame title bar
		String title = "";
		// String title = c.getTitle();

		JFreeChart chart = ChartFactory.createBarChart(title, // chart title ->
																// leave blank,
																// written in
																// frame title
																// bar
				c.getXAxisName(), // domain axis label
				c.getYAxisName(), // range axis label
				c.getCategoryDataset(), // data
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

		CategoryPlot plot = (CategoryPlot) chart.getPlot();

		BarRenderer renderer = (BarRenderer) plot.getRenderer();

		// Removes the bar shadows
		renderer.setShadowVisible(false);
		renderer.setMaximumBarWidth(.15); // set maximum width to 35% of chart

		// Rotate domain labels vertically
		CategoryAxis domainAxis = plot.getDomainAxis();
		domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
		// domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);

//		System.out.println("DRBarGraph #seriesColors: " + c.getSeriesColors().size());

		// Set colors / item labels
		NumberFormat nf = DefaultNumberFormat.getInstance();
		nf.setMaximumFractionDigits(1);
		nf.setGroupingUsed(false);
		StandardCategoryItemLabelGenerator cilg = new StandardCategoryItemLabelGenerator(
				StandardCategoryItemLabelGenerator.DEFAULT_LABEL_FORMAT_STRING, nf);
		
		int i = 0;
		for (Color color : c.getSeriesColors()) {
			renderer.setSeriesPaint(i, color);

			if (visibleValues) {
				renderer.setSeriesItemLabelGenerator(i, cilg);
				renderer.setSeriesItemLabelsVisible(i, true);
			}
			
			i++;
		}

		// Theme customization
		chart.setBackgroundPaint(Color.WHITE);
		plot.setBackgroundPaint(Color.WHITE);

		ChartPanel chartPanel = new GraphChartPanel(chart, dataBlock);

		return chartPanel;

	}

	/**
	 * In Configurable
	 */
	@Override
	public ConfigurationPanel getConfigurationPanel(Object parameter) {
		return new DRBarGraphConfigurationPanel(this);
	}

	/**
	 * In Configurable
	 */
	@Override
	public void configure(ConfigurationPanel panel) {
		super.configure(panel); // DataRenderer configuration

		DRBarGraphConfigurationPanel p = (DRBarGraphConfigurationPanel) panel;

		visibleValues = p.isVisibleValues();

		// Memo for next time
		Settings.setProperty("drbargraph.visibleValues", visibleValues);

	}

	/**
	 * In Configurable
	 */
	@Override
	public void postConfiguration() {
		ExtensionManager.recordSettings(this);
	}

}
