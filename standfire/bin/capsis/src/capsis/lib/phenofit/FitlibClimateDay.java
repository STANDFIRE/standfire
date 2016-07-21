package capsis.lib.phenofit;

import java.io.Serializable;

/**
 * The climate for a given location at a given date (year_ddd).
 * 
 * @author Isabelle Chuine, Yassine Motie - January 2015
 */
public class FitlibClimateDay implements Serializable {

	public short year; // yyyy, e.g. 1951
	public short day; // ddd in year

	public float glo; // global radiation, MJ/m2
	public float pre; // precipitation, mm
	public float rh; // relative humidity, %
	public float tmn; // min temperature, celsius
	public float tmp; // average temperature, celsius
	public float tmx; // max temperature, celsius
	public float wnd; // wind speed, m/s

	/**
	 * Constructor
	 */
	public FitlibClimateDay(int year, int day) {
		super();
		this.year = (short) year;
		this.day = (short) day;
	}

	public void setValue(String climaticVariable, float value) throws Exception {
		if (climaticVariable.equals("glo")) {
			glo = value;
		} else if (climaticVariable.equals("pre")) {
			pre = value;
		} else if (climaticVariable.toLowerCase ().equals("rh")) {
			rh = value;
		} else if (climaticVariable.equals("tmn")) {
			tmn = value;
		} else if (climaticVariable.equals("tmp")) {
			tmp = value;
		} else if (climaticVariable.equals("tmx")) {
			tmx = value;
		} else if (climaticVariable.equals("wnd")) {
			wnd = value;
		} else {
			throw new Exception("Wrong variable name in FitlibClimateDay year: " + year + " day: " + day
					+ " unknown variable: " + climaticVariable);
		}

	}

	public String traceContent () {
		StringBuffer b = new StringBuffer ("FitlibClimateDay "+year+"_"+day);
		b.append(" glo: "+glo);
		b.append(" pre: "+pre);
		b.append(" rh: "+rh);
		// ... might be completed (big traces)
		
		return b.toString ();
	}
	
	public String toString () {
		StringBuffer b = new StringBuffer ("FitlibClimateDay "+year+"_"+day);
		b.append(" glo: "+glo);
		b.append(" pre: "+pre);
		b.append(" rh: "+rh);
		b.append(" tmn: "+tmn);
		b.append(" tmp: "+tmp);
		b.append(" tmx: "+tmx);
		b.append(" wnd: "+wnd);
		return b.toString ();
	}
	
}