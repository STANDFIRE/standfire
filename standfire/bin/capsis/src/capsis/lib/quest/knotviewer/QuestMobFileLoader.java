package capsis.lib.quest.knotviewer;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import jeeb.lib.util.Record;
import jeeb.lib.util.Translator;
import jeeb.lib.util.fileloader.FileLoader;

/**
 * A file loader for a .mob input files.
 * 
 * @author F. de Coligny - March 2015
 */
public class QuestMobFileLoader extends FileLoader {

	// This cass heavily relies on its superclass to interpret a file

	// Will match all the lines in the file
	public List<Line> lines; // lines must all be inspected

	// private: not used for file interpretation
	private List<QuestGU> gus; // output of the loader
	private boolean mobFileRecognized;

	/**
	 * A line in the input file
	 */
	static public class Line extends Record {

		public String text;

		/**
		 * Constructor (super constructor is automatic, based on introspection).
		 */
		public Line(String line) throws Exception {
			super(line);
		}

	}

	public String load(String fileName) {
		// 1. super.load ()
		String loaderReport = super.load(fileName);

		// 2. file interpretation
		int guId = 1;
		double z0_m = 0;
		double length_m = 0;
		int n = 0;
		double treeDbh_cm = 0;
		double treeHeight_m = 0;

		// We add all the knots in a single fake growth unit
		QuestGU fakeGu = new QuestGU(guId, z0_m, length_m, n, treeDbh_cm, treeHeight_m);

		gus = new ArrayList<QuestGU>();
		gus.add(fakeGu);

		// Create the first knot
		int knotId = 1;
		double knotZr = 0;
		double knotZ0_mm = 0;
		double knotAzimut = 0;
		QuestKnot knot = null; // wait for first diameter to get z0_mm

		Color color = null;

		int ringNumber = 1;

		mobFileRecognized = false;

		for (Line l : lines) {

			String text = l.text;

			if (text.toLowerCase().startsWith("mob"))
				mobFileRecognized = true;

			if (ignoreLine(text))
				continue;

			if (text.toLowerCase().startsWith("fin contour")) {

				// Create next knot
				knot = null; // will be created when first diameter is found
				color = null; // can change for each knot
				ringNumber = 1;

			} else if (text.toLowerCase().startsWith("cou")) {
				color = extractColor(text); // null if trouble

			} else {
				try {
					double[] tab = get4Doubles(text);

					double x = tab[0];
					double y = tab[1];
					double z = tab[2];
					double diameter = tab[3] * 2d;
					boolean alive = true; // no information on dead

					if (knot == null) {
						knotZ0_mm = z;
						knot = new QuestKnot("" + guId + "_" + (knotId++), knotZr, knotZ0_mm, knotAzimut);
						// When reading a .mob file, the first technical
						// diameter in the knot must be removed
						knot.getDiameters().clear();
						// Add the new knot in the gu
						fakeGu.addKnot(knot);
					}

					QuestKnotDiameter d = new QuestKnotDiameter(ringNumber, x, y, z, diameter, alive);
					if (color != null)
						d.setColor(color);
					knot.addDiameter(d);

					ringNumber++;

				} catch (Exception e) {
					System.out.println("QuestMobFileLoader unexpected line (ignored): " + text);
				}
			}

		}

		if (!mobFileRecognized) {
			// Overwrite the success tag and the report
			success = false;
			loaderReport = Translator.swap("QuestMobFileLoader.notRecognizedAsMobFormat") + " : \n" + fileName;
		}

		return loaderReport;
	}

	/**
	 * Extract a color from a line: "cou = rvb256 255,  0,  0" In case of
	 * trouble, return null;
	 */
	private Color extractColor(String text) {
		try {

			int i = text.indexOf("rvb256") + 6;
			text = text.substring(i);
			text = text.replace(" ", "");
			StringTokenizer st = new StringTokenizer(text, ",");

			int[] tab = new int[3];
			int k = 0;

			while (st.hasMoreTokens()) {
				tab[k++] = Integer.parseInt(st.nextToken());
			}

			return new Color(tab[0], tab[1], tab[2]);

		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Extracts 4 double values from the given String. If trouble, sends an
	 * exception.
	 */
	private double[] get4Doubles(String text) throws Exception {

		text = text.replace(" ", "");
		StringTokenizer st = new StringTokenizer(text, ",");

		double[] tab = new double[4];
		int i = 0;

		while (st.hasMoreTokens()) {
			tab[i++] = Double.parseDouble(st.nextToken());
		}
		return tab;

	}

	private boolean ignoreLine(String line) {
		if (line.toLowerCase().startsWith("mob"))
			return true;
		if (line.toLowerCase().startsWith("#"))
			return true;
		if (line.toLowerCase().startsWith("tronc"))
			return true;
		if (line.toLowerCase().startsWith("format"))
			return true;
		if (line.toLowerCase().startsWith("orientation"))
			return true;
		if (line.toLowerCase().startsWith("nbry"))
			return true;
		if (line.toLowerCase().startsWith("contours"))
			return true;
		if (line.toLowerCase().startsWith("fin tronc"))
			return true;
		if (line.toLowerCase().startsWith("fin mob"))
			return true;
		return false;
	}

	@Override
	protected void checks() throws Exception {

	}

	public List<QuestGU> getGus() {
		return gus;
	}

}
