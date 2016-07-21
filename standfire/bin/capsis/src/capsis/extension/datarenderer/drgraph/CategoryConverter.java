package capsis.extension.datarenderer.drgraph;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import capsis.extension.dataextractor.Categories;
import capsis.extension.dataextractor.format.DFColoredCurves;
import capsis.extension.dataextractor.format.DFCurves;
import capsis.extension.dataextractor.format.DFListOfCategories;

/**
 * A data converter for the Bar Graph renderer.
 * 
 * @author F. de Coligny - October 2015
 */
public class CategoryConverter {

	private String title;
	private String xAxisName;
	private String yAxisName;
	private DefaultCategoryDataset categoryDataset;
	private List<Color> seriesColors;

	/**
	 * Constructor
	 */
	public CategoryConverter() {
	}

	/**
	 * Converts a list of DFCurves into a single category graph
	 */
	public static CategoryConverter convertDFCurves(List<DFCurves> dataList) throws Exception {
		CategoryConverter c = new CategoryConverter();

		c.categoryDataset = new DefaultCategoryDataset();
		c.seriesColors = new ArrayList<Color>();

		// Extractors have all of the same class: consider the first
		DFCurves representative = (DFCurves) dataList.iterator().next();

		c.title = representative.getName();
		c.xAxisName = representative.getAxesNames().get(0);
		c.yAxisName = representative.getAxesNames().get(1);

		// fc-24-11.2015 adding twice a dataExtractor on same steps results
		// in twice the same name, e.g. mod.65a -> change to mod.65a(2)...
		Map<String, Integer> uniqueNameMap = new HashMap<>();

		// For each DFCurves
		for (DFCurves dfc : dataList) {

			List<List<? extends Number>> curves = dfc.getCurves();
			int nbCurves = curves.size() - 1;

			List<String> names = GraphConverter.searchDFCurvesNames(dfc, nbCurves);

			// labels
			List<List<String>> labels = dfc.getLabels();
			List<String> xLabels = null;
			if (labels != null)
				xLabels = labels.get(0);

			Color color = dfc.getColor();
			Vector<Color> colors = null;
			if (dfc instanceof DFColoredCurves) {
				colors = new Vector<Color>(((DFColoredCurves) dfc).getColors());
			}

			// fc-24.11.2015 ensuring dfcCaption are unique
			String dfcCaption = dfc.getCaption();

			Integer number = uniqueNameMap.get(dfcCaption);
			if (number == null) {
				uniqueNameMap.put(dfcCaption, 1); // first time
			} else {
				int newNumber = number + 1;
				uniqueNameMap.put(dfcCaption, newNumber);
				dfcCaption += "(" + newNumber + ")";
			}

			// For each curves
			for (int j = 0; j < nbCurves; j++) {
				int k = j + 1;

				List<? extends Number> curve = curves.get(k);
				int n = curve.size();

				// For each value
				for (int i = 0; i < n; i++) {

					Number y = curve.get(i);

					String kLabel = "";
					if (names.size() > 1)
						kLabel = "" + k; // only if several
					String name = j < names.size() ? names.get(j) : kLabel;
					String seriesName = dfcCaption + " " + name; // e.g.
																	// pla.45a
																	// Ddom

					// fc-7.1.2016 fixed a bug in pp3's DEBiomassDistrib (C. Meredieu, T. Labbé)
					String label = (labels != null && i < xLabels.size()) ? xLabels.get(i) : "" + curves.get(0).get(i);
//					String label = (labels != null && j < xLabels.size()) ? xLabels.get(i) : "" + curves.get(0).get(i);

					c.categoryDataset.addValue(y, seriesName, label);

				}

				if (colors == null)
					c.seriesColors.add(color); // single color for all curves
				else
					c.seriesColors.add(colors.get(j)); // DFColoredCurves
			}

		}

		return c;
	}

	public String toString() {
		String newLine = "\n";
		String tab = "\t";
		StringBuffer b = new StringBuffer("CategoryConverter");

		b.append (newLine);
		
		b.append(newLine + "   DefaultCategoryDataset:t");

		int nRow = categoryDataset.getRowCount();
		int nCol = categoryDataset.getColumnCount();

		for (int i = 0; i < nRow; i++) {
			b.append(newLine + "      ");
			for (int j = 0; j < nCol; j++) {
				b.append(categoryDataset.getValue(i, j) + tab);
			}
		}

		b.append (newLine);

		b.append(newLine + "   RowKeys: ");
		for (int i = 0; i < nRow; i++) {
			b.append(categoryDataset.getRowKey(i) + tab);
		}

		b.append(newLine + "   RowIndexs: ");
		for (int i = 0; i < nRow; i++) {
			b.append(categoryDataset.getRowIndex(categoryDataset.getRowKey(i)) + tab);
		}

		b.append (newLine);
		
		b.append(newLine + "   ColumnKeys: ");
		for (int j = 0; j < nCol; j++) {
			b.append(categoryDataset.getColumnKey(j) + tab);
		}

		b.append(newLine + "   ColumnIndexs: ");
		for (int j = 0; j < nCol; j++) {
			b.append(categoryDataset.getColumnIndex(categoryDataset.getColumnKey(j)) + tab);
		}

		return b.toString();
	}

	/**
	 * Converts a list of DFListOCategories into a single category graph
	 */
	public static CategoryConverter convertDFListOfCategories(List<DFListOfCategories> dataList) throws Exception {
		CategoryConverter c = new CategoryConverter();

		c.categoryDataset = new DefaultCategoryDataset();
		c.seriesColors = new ArrayList<Color>();

		// Extractors have all of the same class: consider the first
		DFListOfCategories representative = (DFListOfCategories) dataList.iterator().next();

		c.title = representative.getName();
		c.xAxisName = representative.getAxesNames().get(0);
		c.yAxisName = representative.getAxesNames().get(1);

		// fc-3.5.2016 adding twice a dataExtractor on same steps results
		// in twice the same name, e.g. mod.65a -> change to mod.65a(2)...
		Map<String, Integer> uniqueNameMap = new HashMap<>();

		// For each DFListOfXYSeries
		for (DFListOfCategories catList : dataList) {

			int nbHisto = catList.getListOfCategories().size();

			// // labels
			// List<List<String>> labels = xys.getLabels();
			// List<String> xLabels = null;
			// if (labels != null)
			// xLabels = labels.get(0);
			
			// fc-3.5.2016 ensuring dfcCaption are unique
			String catCaption = catList.getCaption();

			Integer number = uniqueNameMap.get(catCaption);
			if (number == null) {
				uniqueNameMap.put(catCaption, 1); // first time
			} else {
				int newNumber = number + 1;
				uniqueNameMap.put(catCaption, newNumber);
				catCaption += "(" + newNumber + ")";
			}

			// For each histo
			int j = 0;
			for (Categories cat : catList.getListOfCategories()) {
				int k = j + 1;

				for (Categories.Entry e : cat.getEntries()) {

					String label = e.label;
					double value = e.value;

					// String kLabel = "";
					// if (xy.getName () != null && xy.getName ().length () > 0)
					// kLabel = "" + k; // only if several
					// String name = j < names.size() ? names.get(j) : kLabel;
					// String seriesName = xys.getCaption() + " " + name; //
					// e.g.
					// // pla.45a
					// // Ddom

					String seriesName = catCaption + " " + cat.getName();

					c.categoryDataset.addValue(value, seriesName, label);

				}
				c.seriesColors.add(cat.getColor());

				j++;

			}

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

	public CategoryDataset getCategoryDataset() {
		return categoryDataset;
	}

	public List<Color> getSeriesColors() {
		return seriesColors;
	}

}
