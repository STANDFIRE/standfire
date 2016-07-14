package capsis.lib.phenofit;

import java.io.Serializable;

import jeeb.lib.util.Log;

/**
 * Memorizes a phase value (int) for a number of days. The period may start at
 * year -1 (e.g. 1st July 1999) and generally stops at day 365 of year n (e.g.
 * 31st December 2000. The range of days can thus be greater than 365.
 * 
 * @author Isabelle Chuine, Yassine Motie - January 2015
 */
public class FitlibPhases implements Serializable {

	private int d0; // first date, e.g. -122
	private int d1; // last date, e.g. 365
	private byte[] values; // the value at each day in the range

	/**
	 * Constructor.
	 */
	public FitlibPhases(int d0, int d1) throws Exception {
		init (d0, d1);
	}
	
	private void init (int d0, int d1) throws Exception {
		// If trouble, throw an exception
		if (d0 >= d1)
			throw new Exception("Could not create a FitlibPhases: d0 >= d1 (" + d0 + " > " + d1 + ")");
		if (d0 < -366)
			throw new Exception("Could not create a FitlibPhases: d0 < -366 (d0 = " + d0 + ")");
		if (d1 > 366)
			throw new Exception("Could not create a FitlibPhases: d1 > 366 (d1 = " + d1 + ")");

		this.d0 = d0;
		this.d1 = d1;

		
		int n = d1 - d0 + 1;
		//System.out.println("FitlibPhases: allocating memory for "+n+" bytes...");
		values = new byte[n];
		
	}

	/**
	 * A method to reuse the object to save time at run time.
	 */
	public FitlibPhases reset (int d0, int d1) throws Exception { // fc-22.6.2015
		if (d0 == this.d0 && d1 <= this.d1) {
//			System.out.println("FitlibPhases: reseting...");
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

	public void setValue(int d, int value) throws Exception {
		if (d < d0 || d > d1)
			throw new Exception("Could not set FitlibPhases value: wrong day: " + d + ", must be in [" + d0 + "," + d1
					+ "]");
		int i = d - d0;
		values[i] = (byte) value;
	}

	public int getValue(int d) throws Exception {
		if (d < d0 || d > d1)
			throw new Exception("Could not get FitlibPhases value: wrong day: " + d + ", must be in [" + d0 + "," + d1
					+ "]");
		int i = d - d0;
		return (int) values[i];
	}

	public int getPrevValue(int year, int day) throws Exception {
		int prevPhase = -1;
		if (FitlibPhenology.isPhenoStartDay(year, day)) {
			prevPhase = 0;
		} 
		else {
			try {
				prevPhase = this.getValue(day - 1);
			} catch (Exception e) {
				if (day == 1) {
					prevPhase = this.getValue(1);
				} else {
					Log.println(Log.ERROR, "FitlibPhases.getPrevValue ()", "Error for year: " + year + ", day: " + day
							+ ", could not find prevValue: " + e);
					throw e;
				}
			}
		}
		return prevPhase;
	}

	public int getD0() {
		return d0;
	}

	public int getD1() {
		return d1;
	}

	public String toString() {
		StringBuffer b = new StringBuffer("FitlibPhases d0: " + d0 + " d1: " + d1 + "\n");
		b.append("  values length: " + values.length + " 0 -> " + (values.length - 1) + "\n");
		b.append("  day:value > ");
		for (int d = d0; d <= d1; d++) {

			// int i = d - d0;
			// b.append("i:"+i+",");

			b.append("" + d + ":");

			try {
				int v = getValue(d);
				b.append(v);
			} catch (Exception e) {
				b.append(e.toString());
				break; // error
			}
			b.append(" ");

		}
		return b.toString();
	}

	// java -cp class:ext/* phenofit.model.FitlibPhases
	public static void main(String[] args) throws Exception {
		FitlibPhases phase = null;
		try {
			int d0 = -3;
			int d1 = 365;

			phase = new FitlibPhases(d0, d1);

			for (int d = d0; d <= d1; d++) {
				phase.setValue(d, d);
			}

			System.out.println(phase.toString());
		} catch (Exception e) {
			System.out.println(phase.toString());
		}
	}

}
