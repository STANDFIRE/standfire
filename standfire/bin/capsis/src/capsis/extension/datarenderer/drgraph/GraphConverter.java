package capsis.extension.datarenderer.drgraph;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import jeeb.lib.util.AmapTools;
import jeeb.lib.util.Log;
import jeeb.lib.util.Vertex2d;

import org.jfree.data.xy.XYSeriesCollection;

import capsis.extension.dataextractor.XYSeries;
import capsis.extension.dataextractor.format.DFColoredCurves;
import capsis.extension.dataextractor.format.DFCurves;
import capsis.extension.dataextractor.format.DFListOfXYSeries;

/**
 * A data converter for the Graph renderer.
 * 
 * @author F. de Coligny - October 2015
 */
public class GraphConverter {

	private String title;
	private String xAxisName;
	private String yAxisName;
	private XYSeriesCollection xySeriesCollection;
	private List<Color> seriesColors;
	// Index of the first series of each source
	private List<Integer> sourceHeads;

	/**
	 * Constructor
	 */
	public GraphConverter() {
	}

	/**
	 * Converts a list of DFCurves into a single graph
	 */
	public static GraphConverter convertDFCurves(List<DFCurves> dataList) throws Exception {
		GraphConverter c = new GraphConverter();

		c.xySeriesCollection = new XYSeriesCollection();
		c.seriesColors = new ArrayList<>();
		c.sourceHeads = new ArrayList<>();

		// Extractors have all of the same class: consider the first
		DFCurves representative = (DFCurves) dataList.iterator().next();

		c.title = representative.getName();
		c.xAxisName = representative.getAxesNames().get(0);
		c.yAxisName = representative.getAxesNames().get(1);

		// For each DFCurves
		int sourceIndex = 0;
		for (DFCurves dfc : dataList) {

			c.sourceHeads.add(sourceIndex);

			List<List<? extends Number>> curves = dfc.getCurves();
			int nbCurves = curves.size() - 1;

			List<String> names = searchDFCurvesNames(dfc, nbCurves);

			Color color = dfc.getColor();
			Vector<Color> colors = null;
			if (dfc instanceof DFColoredCurves) {
				colors = new Vector<Color>(((DFColoredCurves) dfc).getColors());
			}

			// For each curves
			for (int j = 0; j < nbCurves; j++) {
				int k = j + 1;

				String kLabel = "";
				if (names.size() > 1)
					kLabel = "" + k; // only if several
				String name = j < names.size() ? names.get(j) : kLabel;
				String seriesName = dfc.getCaption() + " " + name; // e.g.
																	// pla.45a
																	// Ddom

				// Warning: autoSort must be false to prevent changing points
				// order (!)
				boolean autoSort = false;
				boolean allowDuplicateXValues = true;
				org.jfree.data.xy.XYSeries s = new org.jfree.data.xy.XYSeries(seriesName, autoSort,
						allowDuplicateXValues);
				s.setDescription(name); // short name, e.g. Dg

				// For each value
				int n = curves.get(k).size();
				for (int i = 0; i < n; i++) {

					Number y = curves.get(k).get(i);
					Number x = curves.get(0).get(i);
					
					// In DFCurves, some y can be NaN -> ignore the point
					if (Double.isNaN(x.doubleValue()) || Double.isNaN(y.doubleValue()))
						continue;
					
					s.add(x, y);

				}
				c.xySeriesCollection.addSeries(s);

				if (colors == null)
					c.seriesColors.add(color); // single color for all curves
				else
					c.seriesColors.add(colors.get(j)); // DFColoredCurves

			}
			sourceIndex += nbCurves;
		}

		return c;
	}

	/**
	 * Search for a given DFCurves the curves names if any (e.g. Ddom, Dg...).
	 */
	static public List<String> searchDFCurvesNames(DFCurves dfc, int nbCurves) {

		List<String> names = new ArrayList<>();

		List<List<String>> labels = dfc.getLabels();
		if (labels != null && labels.size() != 0) {

			if (labels.size() > 1) {
				try {
					// There should be an yLabels list for each curve
					// With ONE label only inside (for curves annotation)
					for (int i = 1; i <= nbCurves; i++) {
						List<String> yLabels = labels.get(i);
						if (yLabels == null)
							throw new Exception(
									"Converter: DFCurves yLabels should not be null here (looking for curves names)");
						if (yLabels.size() == 0)
							throw new Exception(
									"Converter: DFCurves yLabels should not be empty here (looking for curves names)");
						if (yLabels.size() > 1)
							throw new Exception("Converter: DFCurves yLabels size should be 1 here (found "
									+ yLabels.size() + ", looking for curves names)");
						names.add(yLabels.get(0));
					}
				} catch (Exception e) {
					Log.println(Log.WARNING, "Converter.evaluateDFCurvesNames ()",
							"DFCurves inconsistency: incorrect curves names, ignored names: "
									+ dfc.getClass().getName(), e);
				}

			}

		}
		return names;
	}

	/**
	 * Converts a list of DFListOfXYSeries into a single graph
	 */
	public static GraphConverter convertDFListOfXYSeries(List<DFListOfXYSeries> dataList) throws Exception {
		GraphConverter c = new GraphConverter();

		c.xySeriesCollection = new XYSeriesCollection();
		c.seriesColors = new ArrayList<Color>();
		c.sourceHeads = new ArrayList<>();

		int sourceIndex = 0;
		for (DFListOfXYSeries xys : dataList) {

			c.sourceHeads.add(sourceIndex);

			if (c.title != null && !c.title.equals(xys.getName()))
				throw new Exception("Converter: can not merge several DFListOfXYSeries with different titles, found: "
						+ c.title + " and: " + xys.getName());
			c.title = xys.getName();

			List<String> l = xys.getAxesNames();
			if (l.size() < 2)
				throw new Exception("Converter: could not convert a DFListOfXYSeries without 2 axes Names: "
						+ AmapTools.toString(l));
			if (c.xAxisName != null && !c.xAxisName.equals(l.get(0)))
				throw new Exception(
						"Converter: can not merge several DFListOfXYSeries with different xAxisName, found: "
								+ c.xAxisName + " and: " + l.get(0));
			if (c.yAxisName != null && !c.yAxisName.equals(l.get(1)))
				throw new Exception(
						"Converter: can not merge several DFListOfXYSeries with different yAxisName, found: "
								+ c.yAxisName + " and: " + l.get(1));
			c.xAxisName = l.get(0);
			c.yAxisName = l.get(1);

			// For each XYSeries
			for (XYSeries xySeries : xys.getListOfXYSeries()) {

				// long name, e.g. mod.35a Dg
				String seriesName = xys.getCaption() + " " + xySeries.getName();

				// Warning: autoSort must be false to prevent changing points
				// order (!)
				boolean autoSort = false;
				boolean allowDuplicateXValues = true;
				org.jfree.data.xy.XYSeries s = new org.jfree.data.xy.XYSeries(seriesName, autoSort,
						allowDuplicateXValues);
				s.setDescription(xySeries.getName()); // short name, e.g. Dg

				for (Vertex2d v : xySeries.getPoints()) {

					Number x = v.x;
					Number y = v.y;
					s.add(x, y);

				}
				c.xySeriesCollection.addSeries(s);
				c.seriesColors.add(xySeries.getColor());
			}
			int n = xys.getListOfXYSeries().size();
			sourceIndex += n;

		}
		return c;
	}

	public String getTitle() {
		return title;
	}

	public String getXAxisName() {
		return xAxisName;
	}

	public String getYAxisName() {
		return yAxisName;
	}

	public XYSeriesCollection getXYSeriesCollection() {
		return xySeriesCollection;
	}

	public List<Color> getSeriesColors() {
		return seriesColors;
	}

	public int getSeriesNumber () {
		return getSourceHeads() == null ? 0 : getSourceHeads().size ();
	}

	public List<Integer> getSourceHeads() {
		return sourceHeads;
	}

	public int getSourceNumber () {
		return getSourceHeads() == null ? 0 : getSourceHeads().size ();
	}
	
}
