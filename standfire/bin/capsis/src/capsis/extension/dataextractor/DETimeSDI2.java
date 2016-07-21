package capsis.extension.dataextractor;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import jeeb.lib.util.Vertex2d;
import capsis.extension.AbstractDataExtractor;
import capsis.extension.dataextractor.format.DFListOfXYSeries;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.MethodProvider;
import capsis.kernel.Step;
import capsis.kernel.extensiontype.GenericExtensionStarter;
import capsis.util.methodprovider.SDIInterface;

/**
 * Evolution of Stand density index over time.
 * 
 * @author F. de Coligny, T. Fonseca - may 2011
 */
public class DETimeSDI2 extends AbstractDataExtractor implements DFListOfXYSeries {

	static {
		Translator.addBundle("capsis.extension.dataextractor.DETimeSDI");
	}

	private List<XYSeries> listOfXYSeries;

	/**
	 * Constructor.
	 */
	public DETimeSDI2() {
	}

	/**
	 * Init method, receives the Step to be synchronized on.
	 */
	@Override
	public void init(GModel model, Step step) throws Exception {

		super.init(model, step);
		listOfXYSeries = new ArrayList<>();

	}

	/**
	 * Extension dynamic compatibility mechanism. This matchwith method checks
	 * if the extension can deal (i.e. is compatible) with the referent.
	 */
	static public boolean matchWith(Object referent) {
		try {
			if (!(referent instanceof GModel))
				return false;

			MethodProvider mp = ((GModel) referent).getMethodProvider();
			return mp instanceof SDIInterface;

		} catch (Exception e) {
			Log.println(Log.ERROR, "DETimeSDI2.matchWith ()", "Error in matchWith () (returned false)", e);
			return false;
		}
	}

	/**
	 * This method is called by superclass DataExtractor.
	 */
	public void setConfigProperties() {
	}

	/**
	 * From DataExtractor SuperClass.
	 * 
	 * Computes the data series. This is the real output building. It needs a
	 * particular Step. This output computes the basal area of the stand versus
	 * date from the root Step to this one.
	 * 
	 * Return false if trouble while extracting.
	 */
	public boolean doExtraction() {
		if (upToDate) {
			return true;
		}
		if (step == null) {
			return false;
		}

		try {

			listOfXYSeries.clear();

			GModel model = (GModel) step.getProject().getModel();
			SDIInterface sdiInterface = (SDIInterface) model.getMethodProvider();

			// Retrieve Steps from root to this step
			Vector steps = step.getProject().getStepsFromRoot(step);

			// --- fc+tf-17.9.2015
			// Draw the SDI band in special case of optimisation (optional)
			Vector<List<Vertex2d>> band = sdiInterface.getSDIBand(step.getScene());

			// fc-21.9.2015
			if (band != null) {

				int k = 1; // fc-24.11.2015 added k
				for (List<Vertex2d> line : band) {
					XYSeries series = new XYSeries("SDI bound " + k, Color.BLACK);

					for (Vertex2d v : line)
						series.addPoint(v.x, v.y);

					listOfXYSeries.add(series);
					k++;
				}

			}
			// fc-21.9.2015

			// Data extraction : points with (Double, Double) coordinates
			XYSeries series = new XYSeries("SDI", getColor());
			listOfXYSeries.add(series);

			for (Iterator i = steps.iterator(); i.hasNext();) {
				Step s = (Step) i.next();

				GScene stand = s.getScene();

				double age = stand.getDate();
				double sdi = sdiInterface.getSDI(stand);

				series.addPoint(age, sdi);

			}

		} catch (Exception exc) {
			Log.println(Log.ERROR, "DETimeSDI2.doExtraction ()", "Exception: ", exc);
			return false;
		}

		upToDate = true;
		return true;
	}

	/**
	 * From DFListOfXYSeries.
	 */
	@Override
	public List<XYSeries> getListOfXYSeries() {
		return listOfXYSeries;
	}

	/**
	 * From DFListOfXYSeries.
	 */
	public List<String> getAxesNames() {
		Vector v = new Vector();
		v.add(Translator.swap("DETimeSDI.xLabel"));
		v.add(Translator.swap("DETimeSDI.yLabel"));
		return v;
	}

	/**
	 * From DFListOfXYSeries.
	 */
	public String getName() {
		return getNamePrefix() + Translator.swap("DETimeSDI");
	}

	/**
	 * From Extension interface.
	 */
	public String getVersion() {
		return VERSION;
	}

	public static final String VERSION = "1.2";

	/**
	 * From Extension interface.
	 */
	public String getAuthor() {
		return "F. de Coligny, T. Fonseca";
	}

	/**
	 * From Extension interface.
	 */
	public String getDescription() {
		return Translator.swap("DETimeSDI.description");
	}

}
