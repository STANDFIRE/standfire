package capsis.lib.castanea;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * FmClimateDay - A day of climate, loaded from a climate file.
 * 
 * @author Hendrik Davi - april 2006, reviewed april 2013
 */
public class FmClimateDay implements Serializable {

	static public final Collection<String> dailyVariableNames;
	static public final Collection<String> hourlyVariableNames;
	static {
		dailyVariableNames = new ArrayList<String> ();
		dailyVariableNames.add ("GlobalRadiation");
		dailyVariableNames.add ("RelativeHumidity");
		dailyVariableNames.add ("WindSpeed");
		dailyVariableNames.add ("Precipitation");
		dailyVariableNames.add ("MaxTemperature");
		dailyVariableNames.add ("MinTemperature");
		dailyVariableNames.add ("AverageTemperature");

		hourlyVariableNames = new ArrayList<String> ();
		hourlyVariableNames.add ("GlobalRadiation");
		hourlyVariableNames.add ("RelativeHumidity");
		hourlyVariableNames.add ("WindSpeed");
		hourlyVariableNames.add ("Precipitation");
		hourlyVariableNames.add ("Temperature");
	}

	private int year;
	private int month;
	private int day; // julian day
	private double dailyGlobalRadiation;
	private double dailyRelativeHumidity;
	private double dailyWindSpeed;
	private double dailyPrecipitation;
	private double dailyMaxTemperature;
	private double dailyMinTemperature;
	private double dailyAverageTemperature;

	private double[] hourlyGlobalRadiation;
	private double[] hourlyRelativeHumidity;
	private double[] hourlyWindSpeed;
	private double[] hourlyPrecipitation;
	private double T_old; // temperature of the end of diurnal period
	private double[] hourlyTemperature;
	private double[] hourlyEa;

	private Map<Integer,FmHourlyClimateRecord> hourlyClimateRecords; // optional, key: hour
	private Map<Integer,FmHalfHourlyClimateRecord> halfHourlyClimateRecords; // optional, key: hour + mn / 100d

	private FmSettings settings; // fc-11.6.2014 for logPrefix
	
	/**
	 * Default constructor.
	 */
	public FmClimateDay () {}

	/**
	 * Constructor 1: for hourly and halfHourly records.
	 */
	public FmClimateDay (int year, int month, int day, FmSettings settings) {
		this.year = year;
		this.month = month;
		this.day = day;
		
		this.settings = settings;
	}

	/**
	 * Constructor 2: for daily records.
	 */
	public FmClimateDay (int year, int month, int day, double globalRadiation, double relativeHumidity,
			double windSpeed, double precipitation, double maxTemperature, double minTemperature,
			double averageTemperature,
			// double T_old
			double latitude, double longitude, double prevTmem, double frach, FmSettings settings) {
		
		this.settings = settings;
		
		this.year = year;
		this.month = month;
		this.day = day;
		this.dailyGlobalRadiation = globalRadiation;
		this.dailyRelativeHumidity = relativeHumidity;
		this.dailyWindSpeed = windSpeed;
		this.dailyPrecipitation = precipitation;
		this.dailyMaxTemperature = maxTemperature;
		this.dailyMinTemperature = minTemperature;
		this.dailyAverageTemperature = averageTemperature;

		this.calculateHourlyWindSpeed (frach); // fc+hd-12.4.2013
		this.calculateHourlyTemperature (latitude, prevTmem, frach);
		this.calculateHourlyGlobalRadiation (latitude, longitude, frach);
		this.calculateHourlyRelativeHumidity (latitude, frach);
	}
	
	/**
	 * For hourly records
	 */
	public void addHourlyClimateRecord (FmHourlyClimateRecord r) {
		if (hourlyClimateRecords == null) hourlyClimateRecords = new HashMap<Integer,FmHourlyClimateRecord> ();
		hourlyClimateRecords.put (r.hour, r); // r.hour: 1-24
	}

	/**
	 * For halfHourly records
	 */
	public void addHalfHourlyClimateRecord (FmHalfHourlyClimateRecord r) {
		if (halfHourlyClimateRecords == null) halfHourlyClimateRecords = new HashMap<Integer,FmHalfHourlyClimateRecord> ();
		
		int key = r.hour * 2 - (r.mn == 0 ? 1 : 0); // 0 -> 47
		
		halfHourlyClimateRecords.put (key, r); // r.hour: 1-24
//		halfHourlyClimateRecords.put (r.hour + r.mn / 100d, r); // r.hour: 1-24
	}

	/**
	 * For hourly records.
	 * In Case addHourlyClimateRecord () was used, finish the
	 * initialization : calculate daily means and hourly variables.
	 */
	public void initHourly (double frach) { // frach = 1: hourly; frach = 0.5; halfHourly; 
		int size = (int) Math.round (24 / frach);
		
		dailyGlobalRadiation = 0;
		dailyRelativeHumidity = 0;
		dailyWindSpeed = 0;
		dailyPrecipitation = 0;
		dailyMaxTemperature = -Double.MAX_VALUE;
		dailyMinTemperature = Double.MAX_VALUE;
		dailyAverageTemperature = 0;

		hourlyGlobalRadiation = new double[size];
		hourlyRelativeHumidity = new double[size];
		hourlyWindSpeed = new double[size];
		hourlyPrecipitation = new double[size];
		hourlyTemperature = new double[size];
		hourlyEa = new double[size]; 

		for (int h = 0; h < size; h++) {
			FmHourlyClimateRecord rec = hourlyClimateRecords.get (h);

			dailyGlobalRadiation += rec.globalRadiation * frach * 3600d / 1000000d; // W/m2 ->
																					// MJ/m2/day
			dailyRelativeHumidity += rec.relativeHumidity;
			dailyWindSpeed += rec.windSpeed;
			dailyPrecipitation += rec.precipitation;
			dailyMaxTemperature = Math.max (dailyMaxTemperature, rec.temperature);
			dailyMinTemperature = Math.min (dailyMinTemperature, rec.temperature);
			dailyAverageTemperature += rec.temperature;

			hourlyGlobalRadiation[h] = rec.globalRadiation; // if not provided, to be calculated
															// from par WARNING
			hourlyRelativeHumidity[h] = rec.relativeHumidity;
			hourlyWindSpeed[h] = rec.windSpeed;
			hourlyPrecipitation[h] = rec.precipitation;
			hourlyTemperature[h] = rec.temperature;
			
			double es = this.getHourlyEs (hourlyTemperature[h]);
			hourlyEa[h] = hourlyRelativeHumidity[h] * es / 100;

		}

		dailyRelativeHumidity /= size;
		dailyWindSpeed /= size;
		dailyAverageTemperature /= size;

	}

	/**
	 * For half hourly records.
	 * In Case addHalfHourlyClimateRecord () was used, finish the
	 * initialization : calculate daily means and halfHourly variables.
	 */
	public void initHalfHourly (double frach) { // frach = 1: hourly; frach = 0.5; halfHourly; 
		int size = (int) Math.round (24 / frach);
		
		dailyGlobalRadiation = 0;
		dailyRelativeHumidity = 0;
		dailyWindSpeed = 0;
		dailyPrecipitation = 0;
		dailyMaxTemperature = -Double.MAX_VALUE;
		dailyMinTemperature = Double.MAX_VALUE;
		dailyAverageTemperature = 0;

		hourlyGlobalRadiation = new double[size];
		hourlyRelativeHumidity = new double[size];
		hourlyWindSpeed = new double[size];
		hourlyPrecipitation = new double[size];
		hourlyTemperature = new double[size];
		hourlyEa = new double[size]; 

		for (int h = 0; h < size; h++) {
			FmHalfHourlyClimateRecord rec = halfHourlyClimateRecords.get (h);

			dailyGlobalRadiation += rec.globalRadiation * frach * 3600d / 1000000d; // W/m2 ->
																					// MJ/m2/day
			dailyRelativeHumidity += rec.relativeHumidity;
			dailyWindSpeed += rec.windSpeed;
			dailyPrecipitation += rec.precipitation;
			dailyMaxTemperature = Math.max (dailyMaxTemperature, rec.temperature);
			dailyMinTemperature = Math.min (dailyMinTemperature, rec.temperature);
			dailyAverageTemperature += rec.temperature;

			hourlyGlobalRadiation[h] = rec.globalRadiation; // if not provided, to be calculated
															// from par WARNING
			hourlyRelativeHumidity[h] = rec.relativeHumidity;
			hourlyWindSpeed[h] = rec.windSpeed;
			hourlyPrecipitation[h] = rec.precipitation;
			hourlyTemperature[h] = rec.temperature;
			
			double es = this.getHourlyEs (hourlyTemperature[h]);
			hourlyEa[h] = hourlyRelativeHumidity[h] * es / 100;

		}

		dailyRelativeHumidity /= size;
		dailyWindSpeed /= size;
		dailyAverageTemperature /= size;

	}

	/**
	 * Returns a perfect copy of this object
	 */
	public FmClimateDay getCopy () {
		FmClimateDay copy = new FmClimateDay ();

		// Copy the simple fields
		copy.year = this.year;
		copy.month = this.month;
		copy.day = this.day;
		copy.dailyGlobalRadiation = this.dailyGlobalRadiation;
		copy.dailyRelativeHumidity = this.dailyRelativeHumidity;
		copy.dailyWindSpeed = this.dailyWindSpeed;
		copy.dailyPrecipitation = this.dailyPrecipitation;
		copy.dailyMaxTemperature = this.dailyMaxTemperature;
		copy.dailyMinTemperature = this.dailyMinTemperature;
		copy.dailyAverageTemperature = this.dailyAverageTemperature;
		copy.T_old = this.T_old;

		// Copy the arrays
		if (this.hourlyTemperature != null) {
			copy.hourlyTemperature = new double[this.hourlyTemperature.length];
			System.arraycopy (this.hourlyTemperature, 0, copy.hourlyTemperature, 0, this.hourlyTemperature.length);
		}
		if (this.hourlyGlobalRadiation != null) {
			copy.hourlyGlobalRadiation = new double[this.hourlyGlobalRadiation.length];
			System.arraycopy (this.hourlyGlobalRadiation, 0, copy.hourlyGlobalRadiation, 0, this.hourlyGlobalRadiation.length);
		}
		if (this.hourlyRelativeHumidity != null) {
			copy.hourlyRelativeHumidity = new double[this.hourlyRelativeHumidity.length];
			System.arraycopy (this.hourlyRelativeHumidity, 0, copy.hourlyRelativeHumidity, 0, this.hourlyRelativeHumidity.length);
		}
		if (this.hourlyEa != null) {
			copy.hourlyEa = new double[this.hourlyEa.length];
			System.arraycopy (this.hourlyEa, 0, copy.hourlyEa, 0, this.hourlyEa.length);
		}

		if (this.hourlyPrecipitation != null) {
			copy.hourlyPrecipitation = new double[this.hourlyPrecipitation.length];
			System.arraycopy (this.hourlyPrecipitation, 0, copy.hourlyPrecipitation, 0, this.hourlyPrecipitation.length);
		}
		if (this.hourlyWindSpeed != null) {
			copy.hourlyWindSpeed = new double[this.hourlyWindSpeed.length];
			System.arraycopy (this.hourlyWindSpeed, 0, copy.hourlyWindSpeed, 0, this.hourlyWindSpeed.length);
		}

		return copy;
	}

	public int getYear () {
		return year;
	}

	public int getMonth () {
		return month;
	}

	public int getDay () {
		return day;
	}

	public double getDailyGlobalRadiation () {
		return dailyGlobalRadiation;
	}

	public double getDailyRelativeHumidity () {
		return dailyRelativeHumidity;
	}

	public double getDailyWindSpeed () {
		return dailyWindSpeed;
	}

	public double getDailyPrecipitation () {
		return dailyPrecipitation;
	}

	public double getDailyMaxTemperature () {
		return dailyMaxTemperature;
	}

	public double getDailyMinTemperature () {
		return dailyMinTemperature;
	}

	public double getDailyAverageTemperature () {
		return dailyAverageTemperature;
	}

	public double getTold () {
		return T_old;
	}

	public double getDaily (String variableName) throws Exception {
		if (variableName.equals ("GlobalRadiation")) {
			return getDailyGlobalRadiation ();
		} else if (variableName.equals ("RelativeHumidity")) {
			return getDailyRelativeHumidity ();
		} else if (variableName.equals ("WindSpeed")) {
			return getDailyWindSpeed ();
		} else if (variableName.equals ("Precipitation")) {
			return getDailyPrecipitation ();
		} else if (variableName.equals ("MaxTemperature")) {
			return getDailyMaxTemperature ();
		} else if (variableName.equals ("MinTemperature")) {
			return getDailyMinTemperature ();
		} else if (variableName.equals ("AverageTemperature")) {
			return getDailyAverageTemperature ();
		} else {
			throw new Exception ("Wrong variable Name: " + variableName + " in FmClimateDay.getDaily (variableName)");
		}
	}

	public void setDailyPrecipitation (double v) {
		dailyPrecipitation = v;
	}

	public void setDailyMaxTemperature (double v) {
		dailyMaxTemperature = v;
	}

	public void setDailyMinTemperature (double v) {
		dailyMinTemperature = v;
	}

	public void setDailyAverageTemperature (double v) {
		dailyAverageTemperature = v;
	}

	public void setDailyRelativeHumidity (double v) {
		dailyRelativeHumidity = v;
	}

	public double getHourly (String variableName, double latitude, double longitude, int h, int day) throws Exception {
		if (variableName.equals ("GlobalRadiation")) {
			return getHourlyGlobalRadiation (h);

		} else if (variableName.equals ("RelativeHumidity")) {
			return getHourlyRelativeHumidity (h);
		} else if (variableName.equals ("Temperature")) {
			return getHourlyTemperature (h);
		} else if (variableName.equals ("WindSpeed")) {
			return getHourlyWindSpeed (h);
		} else if (variableName.equals ("Precipitation")) {
			return getHourlyPrecipitation (h);

		} else {
			throw new Exception ("Wrong variable Name: " + variableName + " in FmClimateDay.getDaily (variableName)");
		}
	}

	public void setHourlyGlobalRadiation (int h, double v) {
		hourlyGlobalRadiation[h] = v;
	}

	public void setHourlyRelativeHumidity (int h, double v) {
		hourlyRelativeHumidity[h] = v;
	}

	public void setHourlyTemperature (int h, double v) {
		hourlyTemperature[h] = v;
	}

	public void setHourlyWindSpeed (int h, double v) {
		hourlyWindSpeed[h] = v;
	}

	public void setHourlyPrecipitation (int h, double v) {
		hourlyPrecipitation[h] = v;
	}

	private void calculateHourlyWindSpeed (double frach) { // fc+hd-12.4.2013
		int size = (int) Math.round (24 / frach);
		hourlyWindSpeed = new double[size];
		for (int h = 0; h < size; h++) {
			hourlyWindSpeed[h] = this.dailyWindSpeed; // no variation inside day to be improved
		}

	}

	private void calculateHourlyGlobalRadiation (double latitude, double longitude, double frach) {
		int size = (int) Math.round (24 / frach);
		hourlyGlobalRadiation = new double[size];
		double Soj = getDailySolarRadiation (latitude, longitude, day, frach);

		for (int h = 0; h < size; h++) {

			double Soh = getHourlySolarRadiation (latitude, longitude, h, day);
			double Sgj = dailyGlobalRadiation;
			double Sghtemp = Sgj * Soh / Soj;

			// if (Sghtemp > Soh && Soh > 0) {
			// Sghtemp= Soh;
			// }

			hourlyGlobalRadiation[h] = Sghtemp;

		}

	}

	/**
	 * Hourly OR half hourly temperatures hd 11 06 2008
	 */
	private void calculateHourlyTemperature (double latitude, double prevTmem, double frach) {
		int size = (int) Math.round (24 / frach);
		hourlyTemperature = new double[size];
		double H = getDailyLength (latitude, day);
		double Tdeb = 12 - 0.5 * H; // Hour the beginning of the day
		double Tfin = 12 + 0.5 * H; // Hour of the end of the day, beginning of night
		double Ta;

		for (int h = 0; h < size; h++) {

			if (h < Tdeb) { // nocturnal period 0h-Morning
				Ta = prevTmem - (prevTmem - dailyMinTemperature) * (h + Tdeb) / (size - H);

			} else if (h > Tdeb && h < Tfin) { // diurnal period
				Ta = 0.5 * (dailyMinTemperature + dailyMaxTemperature - (dailyMaxTemperature - dailyMinTemperature)
						* Math.cos (1.5 * Math.PI * (h - Tdeb) / H));
				T_old = Ta;

			} else { // evenning-24h
				Ta = T_old - (T_old - dailyMinTemperature) * (h - Tfin) / (size - H);

			}

			hourlyTemperature[h] = Ta;

		}

	}// end of get getHourlyTemperature

	/**
	 * Hourly OR half hourly Relative humidity hd 11 06 2008
	 */
	private void calculateHourlyRelativeHumidity (double latitude, double frach) {
		int size = (int) Math.round (24 / frach);
		double Tj = dailyAverageTemperature;
		double RHj = dailyRelativeHumidity;
		double esmoy = this.getHourlyEs (Tj);

		hourlyRelativeHumidity = new double[size];
		hourlyEa = new double[size];
		// Log.println(settings.logPrefix+"DynaclimTest", "latitude=" +latitude );

		for (int h = 0; h < size; h++) {
			// int h = i+1;
			double Ta = getHourlyTemperature (h);
			double es = this.getHourlyEs (Ta);

			double eamoy = RHj * esmoy / 100;
			double RH = 100 * eamoy / es;
			RH = Math.min (RH, 100.);
			RH = Math.max (RH, 0.);
			hourlyRelativeHumidity[h] = RH;
			hourlyEa[h] = RH * es / 100;
		}

	}// end of RH

	public double getHourlyTemperature (int h) {
		return hourlyTemperature[h];
	}

	public double getHourlyGlobalRadiation (int h) {
		return hourlyGlobalRadiation[h];
	}

	public double getHourlyRelativeHumidity (int h) {
		return hourlyRelativeHumidity[h];
	}

	public double getEah (int h) {
		return hourlyEa[h];
	}

	public double getHourlyWindSpeed (int h) {
		return hourlyWindSpeed[h];
	}

	public double getHourlyPrecipitation (int h) {
		return hourlyPrecipitation[h];
	}

	// -----------------------------------------

	/**
	 * Calculation of declination
	 */
	public double getDeclination (int day) {

		double decli = (Math.PI * 23.45 / 180.) * Math.sin (2. * Math.PI * (284. + day) / 365.); // in
																									// radian
		return decli;

	}

	// calulation of daily length
	public double getDailyLength (double latitude, int day) {

		double lat_rad = latitude * Math.PI / 180;
		double decli = getDeclination (day);
		double H = (Math.acos (-Math.tan (lat_rad) * Math.tan (decli))) * 360. / (15. * Math.PI); // day
																									// length
																									// in
																									// hours
		return H;
	}

	private double getHourlySolarRadiation (double latitude, double longitude, int h, int day) {
		double IO = 1370;
		double R = 1 / (1. + (0.033 * Math.cos (2. * Math.PI * day / 365.)));

		double decli = getDeclination (day);
		double lat_rad = latitude * Math.PI / 180;
		double La = (279.48 + 0.9856 * day) * Math.PI / 180.;
		double Ma = (356.54 + 0.9856 * day) * Math.PI / 180.;
		double deltaeq = (-9.86 * Math.sin (2 * La) + 7.66 * Math.sin (Ma) - 0.66 * Math.sin (Ma) * Math.cos (2 * La)
				+ 0.21 * Math.sin (4 * La) + 0.08 * Math.sin (2 * Ma)) / 60.;
		double deltalong = (longitude * 4.) / 60.;
		double delta = deltaeq + deltalong;
		double heuresol = h - 0.25 - delta;
		double soh = (IO / Math.pow (R, 2))
				* (Math.sin (decli) * Math.sin (lat_rad) - 12. / Math.PI * Math.cos (decli) * Math.cos (lat_rad)
						* (Math.sin (Math.PI * (heuresol + 0.5) / 12.) - Math.sin (Math.PI * (heuresol - 0.5) / 12.)));
		soh = Math.max (0., soh);
		// Log.println(settings.logPrefix+"DynaclimTest", "Soh=" +soh+"  decli= " +
		// decli+" delta= "+delta+" lat_rad= "+lat_rad);

		return soh;
	}

	public double getDailySolarRadiation (double latitude, double longitude, int day, double frach) {
		int size = (int) Math.round (24 / frach);
		double Soj = 0;
		double soh = 0;
		for (int h = 0; h < size; h++) {
			soh = getHourlySolarRadiation (latitude, longitude, h, day);
			Soj += soh * 3600. / 1000000;
		}
		return Soj;
	}

	public double getHourlyEs (double Ta) {
		double es = 6.1078 * Math.exp (17.269 * Ta / (Ta + 237.3));
		return es;
	}

	/**
	 * Calculation of the proportion of diffuse light at the top of the canopy from Spitters & al.
	 * (1986)
	 */
	public double getSkyl (double latitude, double longitude, int h, int day) {

		double SghTemp = hourlyGlobalRadiation[h];
		double Soh = getHourlySolarRadiation (latitude, longitude, h, day);
		double beta = getBeta (latitude, longitude, h, day); // solar angle above horizon in radian
		double skyl = 0;

		double Q3 = Math.sin (beta); //
		double Q2 = 0.847 - 1.61 * Q3 + 1.04 * Q3 * Q3;
		double Q1 = (1.47 - Q2) / 1.66;

		if (SghTemp / Soh <= 0.22) {

			skyl = 1;

		} else if (SghTemp / Soh <= 0.35) {
			skyl = (1 - 6.4 * (SghTemp / Soh - 0.22) * (SghTemp / Soh - 0.22));
		} else if (SghTemp / Soh <= Q1) {
			skyl = 1.47 - 1.66 * (SghTemp / Soh);
		} else {
			skyl = Q2;
		}

		skyl = Math.min (1., Math.max (0., skyl)); // to avoid errors
		double cosBeta = Math.cos (beta);
		// take into account the diffuse part of circum solar
		double circum = Math.cos (Math.PI / 2 - beta) * Math.cos (Math.PI / 2 - beta) * Math.pow (cosBeta, 3);
		skyl = skyl / (1 + (1 - skyl * skyl) * circum);

		return skyl;
	}

	/**
	 * Solar angle above horizon in radian
	 */
	public double getBeta (double latitude, double longitude, int h, int day) {

		double decli = getDeclination (day);
		double lat_rad = latitude * Math.PI / 180;
		double La = (279.48 + 0.9856 * day) * Math.PI / 180.;
		double Ma = (356.54 + 0.9856 * day) * Math.PI / 180.;
		double deltaeq = (-9.86 * Math.sin (2 * La) + 7.66 * Math.sin (Ma) - 0.66 * Math.sin (Ma) * Math.cos (2 * La)
				+ 0.21 * Math.sin (4 * La) + 0.08 * Math.sin (2 * Ma)) / 60.;
		double deltalong = (longitude * 4.) / 60.;
		double delta = deltaeq + deltalong;
		double heuresol = h - 0.25 - delta;

		double Q3int = Math.sin (lat_rad) * Math.sin (decli) + Math.cos (lat_rad) * Math.cos (decli)
				* Math.cos (15. * Math.PI * (heuresol - 12.) / 180.);
		double beta = Math.asin (Q3int);

		return beta;
	}

	/**
	 * Calculation of Kbdir following CASTANEA
	 */
	public double getKbdir (double latitude, double longitude, int h, int day, double alpha) {

		double beta = getBeta (latitude, longitude, h, day);
		double shade = 0;
		double Q4 = 0;
		double Zdir = 0;
		double kbdir = 0;

		if (beta > 0) {
			if (beta >= alpha) {
				shade = Math.cos (alpha) * Math.sin (beta);
			} else {
				Q4 = -(Math.sin (beta) * Math.cos (alpha)) / (Math.sin (alpha) * Math.cos (beta));
				Zdir = Math.PI / 2 - Math.acos (Q4);
				shade = (2 / Math.PI)
						* (Math.cos (beta) * Math.sin (alpha) * Math.cos (Zdir) - Zdir * Math.sin (beta)
								* Math.cos (alpha));
			}
			kbdir = shade / Math.sin (beta);

		} else {
			kbdir = 999;
		}
		return kbdir;
	}

	public Map<Integer,FmHourlyClimateRecord> getHourlyClimateRecords () {
		return hourlyClimateRecords;
	}

	public String toString () {
		StringBuffer b = new StringBuffer ("FmClimateDay year: " + year + " month: " + month + " day: " + day);
		b.append ("\n  FmClimateDay hourlyClimateRecords: " + hourlyClimateRecords);
		b.append ("\n  dailyGlobalRadiation: " + dailyGlobalRadiation);
		b.append ("\n  dailyRelativeHumidity: " + dailyRelativeHumidity);
		b.append ("\n  dailyWindSpeed: " + dailyWindSpeed);
		b.append ("\n  dailyPrecipitation: " + dailyPrecipitation);
		b.append ("\n  dailyMaxTemperature: " + dailyMaxTemperature);
		b.append ("\n  dailyMinTemperature: " + dailyMinTemperature);
		b.append ("\n  dailyAverageTemperature: " + dailyAverageTemperature);
		b.append ("\n  hourlyGlobalRadiation: ");
//		for (int h = 0; h < 24; h++)
//			b.append (hourlyGlobalRadiation[h] + " ");
//		b.append ("\n  hourlyRelativeHumidity: ");
//		for (int h = 0; h < 24; h++)
//			b.append (hourlyRelativeHumidity[h] + " ");
//		b.append ("\n  hourlyWindSpeed: ");
//		for (int h = 0; h < 24; h++)
//			b.append (hourlyWindSpeed[h] + " ");
//		b.append ("\n  hourlyPrecipitation: ");
//		for (int h = 0; h < 24; h++)
//			b.append (hourlyPrecipitation[h] + " ");
//		b.append ("\n  hourlyTemperature: ");
//		for (int h = 0; h < 24; h++)
//			b.append (hourlyTemperature[h] + " ");
//		if (hourlyEa != null) {
//			b.append ("\n  hourlyEa: ");
//			for (int h = 0; h < 24; h++)
//				b.append (hourlyEa[h] + " ");
//		}
		return b.toString ();
	}

}
