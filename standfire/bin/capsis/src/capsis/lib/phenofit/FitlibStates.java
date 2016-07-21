package capsis.lib.phenofit;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.Locale;

import jeeb.lib.util.Log;

/**
 * Memorizes a state value for a number of days. The period may start at year -1
 * (e.g. 1st July 1999) and generally stops at day 365 of year n (e.g. 31st
 * December 2000. The range of days can thus be greater than 365.
 * 
 * @author Isabelle Chuine, Yassine Motie - January 2015
 */
public class FitlibStates implements Serializable {
	static private NumberFormat nf;
	static {
		nf = NumberFormat.getInstance(Locale.ENGLISH);
		nf.setGroupingUsed(false);
		nf.setMaximumFractionDigits(5);
	}

	private int d0; // first date, e.g. -122
	private int d1; // last date, e.g. 365

	// fc-17.6.2015 this array must be replaced by a Set<value, upToThisDay>
	// XXXXXXXXXXXXXXXXXXXXXXXXXXXXXx

	private float[] values; // the value at each day in the range

	// // fc-22.6.2015 testing economic version
	// private double v1 = -1;
	// private double v2 = -1;
	// private int date1 = -1;
	// private int date2 = -1;
	// private boolean useV1 = true;
	// // fc-22.6.2015 testing economic version

	/**
	 * Constructor.
	 */
	public FitlibStates(int d0, int d1) throws Exception {
		// If trouble, throw an exception
		if (d0 >= d1)
			throw new Exception("Could not create a FitlibStates: d0 >= d1 (" + d0 + " > " + d1 + ")");
		if (d0 < -366)
			throw new Exception("Could not create a FitlibStates: d0 < -366 (d0 = " + d0 + ")");
		if (d1 > 366)
			throw new Exception("Could not create a FitlibStates: d1 > 366 (d1 = " + d1 + ")");

		this.d0 = d0;
		this.d1 = d1;

		int n = d1 - d0 + 1;
		// System.out.println("FitlibStates: allocating memory for "+n+" floats...");
		values = new float[n];
	}

	private void init(int d0, int d1) throws Exception {
		// If trouble, throw an exception
		if (d0 >= d1)
			throw new Exception("Could not create a FitlibStates: d0 >= d1 (" + d0 + " > " + d1 + ")");
		if (d0 < -366)
			throw new Exception("Could not create a FitlibStates: d0 < -366 (d0 = " + d0 + ")");
		if (d1 > 366)
			throw new Exception("Could not create a FitlibStates: d1 > 366 (d1 = " + d1 + ")");

		this.d0 = d0;
		this.d1 = d1;

		int n = d1 - d0 + 1;
		values = new float[n];
	}

	/**
	 * A method to reuse the object to save time at run time.
	 */
	public FitlibStates reset(int d0, int d1) throws Exception { // fc-22.6.2015
		if (d0 == this.d0 && d1 <= this.d1) {
			// System.out.println("FitlibStates: reseting...");
			this.d1 = d1; // 365 / 366...
			// Does not reallocate memory (cleans up)
			for (int i = 0; i < values.length; i++) {
				values[i] = 0;
			}

		} else {
			// Reallocates memory
			init(d0, d1);
		}
		return this; // convenient
	}

	public void setValue(int d, double value) throws Exception {
		if (d < d0 || d > d1)
			throw new Exception("Could not set FitlibStates value: wrong day: " + d + ", must be in [" + d0 + "," + d1
					+ "]");
		int i = d - d0;
		values[i] = (float) value;

		// // fc-22.6.2015 testing economic version
		// if (useV1) {
		// v1 = value;
		// date1 = d;
		// } else {
		// v2 = value;
		// date2 = d;
		// }
		// useV1 = !useV1;
		// // fc-22.6.2015 testing economic version
	}

	public double getValue(int d) throws Exception {
		if (d < d0 || d > d1)
			throw new Exception("Could not get FitlibStates value: wrong day: " + d + ", must be in [" + d0 + "," + d1
					+ "]");

		int i = d - d0;

		// // fc-22.6.2015 testing economic version
		// if (d != date1 && d != date2)
		// throw new
		// Exception("FitlibStates checking economic version **ERROR**: date1: "
		// + date1 + " date2: "
		// + date2 + " getValue () was called for index: " + i + " (i.e. date: "
		// + d + ")");
		// // fc-22.6.2015 testing economic version

		return values[i];
	}

	public double getPrevValue(int year, int day) throws Exception {
		double prevState = -1;
		if (FitlibPhenology.isPhenoStartDay(year, day)) {
			prevState = 0;
		} else {
			try {
				prevState = this.getValue(day - 1);
			} catch (Exception e) {
				if (day == 1) {
					prevState = this.getValue(1);
				} else {
					Log.println(Log.ERROR, "FitlibStates.getPrevValue ()", "Error for year: " + year + ", day: " + day
							+ ", could not find prevValue: " + e);
					throw e;
				}
			}
		}
		return prevState;
	}

	public int getD0() {
		return d0;
	}

	public int getD1() {
		return d1;
	}

	public String toString() {
		StringBuffer b = new StringBuffer("FitlibStates d0: " + d0 + " d1: " + d1 + "\n");
		b.append("  values length: " + values.length + " 0 -> " + (values.length - 1) + "\n");
		b.append("  day:value > ");
		for (int d = d0; d <= d1; d++) {

			// int i = d - d0;
			// b.append("i:"+i+",");

			try {
				double v = getValue(d);

				b.append("" + d + ":");
				b.append(nf.format(v));

			} catch (Exception e) {
				// fc-22.6.2015
				// b.append(e.toString());
				// break; // error
			}
			b.append(" ");

		}
		return b.toString();
	}

	// java -cp class:ext/* phenofit.model.FitlibStates
	public static void main(String[] args) throws Exception {
		FitlibStates state = null;
		try {
			int d0 = -3;
			int d1 = 365;

			state = new FitlibStates(d0, d1);

			for (int d = d0; d <= d1; d++) {
				state.setValue(d, d);
			}

			System.out.println(state.toString());
		} catch (Exception e) {
			System.out.println(state.toString());
		}
	}

}
