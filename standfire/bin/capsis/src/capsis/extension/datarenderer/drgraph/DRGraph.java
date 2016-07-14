package capsis.extension.datarenderer.drgraph;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import jeeb.lib.util.Log;
import jeeb.lib.util.Settings;
import jeeb.lib.util.Translator;
import jeeb.lib.util.extensionmanager.ExtensionManager;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.TextAnchor;

import capsis.app.CapsisExtensionManager;
import capsis.extension.PanelDataRenderer;
import capsis.extension.dataextractor.format.DFCurves;
import capsis.extension.dataextractor.format.DFListOfXYSeries;
import capsis.extension.datarenderer.jfreechart.MessagePanel;
import capsis.extensiontype.DataBlock;
import capsis.extensiontype.DataExtractor;
import capsis.util.ColoredIcon;
import capsis.util.ConfigurationPanel;

/**
 * A data renderer for data extractors implementing DFListOfXYSeries and
 * DFCurves : draws curves. Version 3, replaces DRCurves (original 2003,
 * reviewed 2011).
 * 
 * @author F. de Coligny - October 2015
 */
public class DRGraph extends PanelDataRenderer {

	static {
		Translator.addBundle("capsis.extension.datarenderer.drgraph.Labels");
	}

	static final public String NAME = Translator.swap("DRGraph");
	static final public String VERSION = "3.0";
	static final public String AUTHOR = "F. de Coligny";
	static final public String DESCRIPTION = Translator.swap("DRGraph.description");

	protected boolean enlargedMode; // larger points on the graph
	
	protected GraphChartPanel chartPanel; // must be disposed
	
	/**
	 * Constructor
	 */
	public DRGraph() {
		super();
	}

	/**
	 * Inits the graph
	 */
	@Override
	public void init(DataBlock db) {
		try {
			super.init(db);

			enlargedMode = Settings.getProperty("drgraph.enlargedMode", false);

		} catch (Exception e) {
			Log.println(Log.ERROR, "DRGraph.init ()", "Error in init (), wrote in Log and passed", e);
		}

	}

	/**
	 * Tells if the renderer can show an extractor's production. True if the
	 * extractor is an instance of the renderer's compatible data formats Note:
	 * DataExtractor must implement a data format in order to be recognized by
	 * DataRenderers.
	 */
	static public boolean matchWith(Object target) {

		return target instanceof DataExtractor && (target instanceof DFListOfXYSeries || target instanceof DFCurves);

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
	 * One single chart with maybe several extractors inside with maybe several
	 * curves each.
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

			GraphConverter c = null;

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

				c = GraphConverter.convertDFCurves(dataList);

			} else if (representative instanceof DFListOfXYSeries) {
				// Case of DFListOfXYSeries instances
				List<DFListOfXYSeries> dataList = new ArrayList<DFListOfXYSeries>();
				for (Object e : allExtractors) {
					dataList.add((DFListOfXYSeries) e);
				}
				c = GraphConverter.convertDFListOfXYSeries(dataList);
			}

			return createXYChart(c);

		} catch (Exception e) {
			Log.println(Log.ERROR, "DRGraph.createView ()", "Exception", e);
			return new MessagePanel(CapsisExtensionManager.getInstance().getName(this) + " "
					+ Translator.swap("DRGraph.canNotShowTheseDataSeeLog"), (MouseListener) this);
		}

	}

	/**
	 * Returns a copy of the given shape, scaled with the given factor.
	 */
	protected Shape scaleShape(Shape shape) {

		// Enlarged shape is the normal JFreeChart shape (relatively big)
		if (enlargedMode)
			return shape;

		// Not enlarged: we scale the normal shape to make it smaller
		// warning: too small, we could not distinguish squares, circles,
		// triangles...
		double scale = 0.7;
		AffineTransform t = new AffineTransform();
		t.scale(scale, scale);

		Shape s = t.createTransformedShape(shape);

		return s;
	}

	public ImageIcon makeImageIcon(Shape shape, Color color) {

		// // move the shape in the region of the image
		// gr.translate(-r.x, -r.y);
		// gr.draw(s);
		// gr.dispose();
		// return image;
		//

		// Image image = new BufferedImage(width, height,
		// BufferedImage.TYPE_INT_RGB);

		Rectangle r = shape.getBounds();
		Image image = new BufferedImage(r.width, r.height, BufferedImage.TYPE_INT_RGB);
		// Image image = new BufferedImage(r.width, r.height,
		// BufferedImage.TYPE_BYTE_BINARY);

		Graphics2D g2 = (Graphics2D) image.getGraphics();

		g2.setColor(color);
		g2.draw(shape);

		ImageIcon icon = new ImageIcon();
		icon.setImage(image);

		return icon;
	}

	/**
	 * Creates a XYSeriesCollection chart: Xs are numbers.
	 */
	protected JComponent createXYChart(GraphConverter c) {

		// Use a simple theme
		StandardChartTheme theme = (StandardChartTheme) StandardChartTheme.createLegacyTheme();
		ChartFactory.setChartTheme(theme);

		// Chart title -> leave blank, already written in frame title bar
		String title = "";
		// String title = c.getTitle();

		JFreeChart chart = ChartFactory.createXYLineChart(title, c.getXAxisName(), // domain
																					// axis
																					// label
				c.getYAxisName(), // range axis label
				c.getXYSeriesCollection(), // data
				PlotOrientation.VERTICAL, // orientation
				true, // include legend
				true, // tooltips
				false // URLs?
				);

		XYPlot plot = (XYPlot) chart.getPlot();

		// fc-16.10.2015 Log axes should be checked with Robert Schneider: not
		// activated
		// // Log axis test
		// LogAxis xAxis = new LogAxis(c.getXAxisName());
		// plot.setDomainAxis(xAxis);
		// LogAxis yAxis = new LogAxis(c.getYAxisName());
		// plot.setRangeAxis(yAxis);
		// // make sure the current theme is applied to the axes just added
		// // ChartUtilities.applyCurrentTheme(chart);
		// // Log axis test

		// Set legend above the graph

		// Organise big legend
		LegendItemCollection lic = plot.getLegendItems();
		int ITEM_MAX = 10;

		// If legend size is small, complete legend
		if (lic.getItemCount() <= ITEM_MAX) {
			LegendTitle legend = chart.getLegend();
			legend.setPosition(RectangleEdge.TOP);
			legend.setFrame(BlockBorder.NONE);
			legend.setHorizontalAlignment(HorizontalAlignment.RIGHT);

		} else {
			// if legend is too big, extract one title per source
			LegendTitle legend = chart.getLegend();
			legend.setVisible(false);

			// Create a shorter legend
			JPanel newLegend = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			newLegend.setBackground(Color.WHITE);
			add(newLegend, BorderLayout.NORTH);

			int k = 1;

			for (int i = 0; i < c.getSourceNumber(); i++) {
				// Get the first legend of each source
				// Each should have a different source color
				int sourceHead = c.getSourceHeads().get(i);
				LegendItem li = lic.get(sourceHead);

				JLabel l = new JLabel("" + li.getSeriesKey());
				Color co = c.getSeriesColors().get(sourceHead);
				l.setIcon(new ColoredIcon(co, 8, 8));
				newLegend.add(l);

				if (k++ >= ITEM_MAX)
					break;
			}

		}

		// Add line names at the right side of the lines
		try {
			XYSeriesCollection sc = c.getXYSeriesCollection();

			for (Object o : sc.getSeries()) {
				org.jfree.data.xy.XYSeries xys = (org.jfree.data.xy.XYSeries) o;

				String text = xys.getDescription();
				if (text != null && text.length() > 0) {
					int n = xys.getItemCount();
					double xLast = xys.getX(n - 1).doubleValue();
					double yLast = xys.getY(n - 1).doubleValue();
					XYTextAnnotation ta = new XYTextAnnotation(text, xLast, yLast);
					ta.setTextAnchor(TextAnchor.BOTTOM_CENTER); // or
																// BASELINE_LEFT
					plot.addAnnotation(ta);
				}
			}
		} catch (Exception e) {
			// Annotations should not prevent displaying
			Log.println(Log.WARNING, "DRGraph.createXYChart ()",
					"Trouble while adding annotations in the graph, ignored", e);
		}

		// Tune the renderer
		boolean lines = true;
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

		// fc-9.3.2016
		if (chartPanel != null)
			chartPanel.dispose ();
		
		chartPanel = new GraphChartPanel(chart, dataBlock);

		return chartPanel;

	}

	/**
	 * In Configurable
	 */
	@Override
	public ConfigurationPanel getConfigurationPanel(Object parameter) {
		return new DRGraphConfigurationPanel(this);
	}

	/**
	 * In Configurable
	 */
	@Override
	public void configure(ConfigurationPanel panel) {
		super.configure(panel); // DataRenderer configuration

		DRGraphConfigurationPanel p = (DRGraphConfigurationPanel) panel;

		enlargedMode = p.isEnlargedMode();

		// Memo for next time
		Settings.setProperty("drgraph.enlargedMode", enlargedMode);

	}

	/**
	 * In Configurable
	 */
	@Override
	public void postConfiguration() {
		ExtensionManager.recordSettings(this);
	}

}
