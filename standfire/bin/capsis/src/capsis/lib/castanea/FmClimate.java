package capsis.lib.castanea;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

/**
 * FmClimate - A set of days of climate, loaded from a climate file.
 * 
 * @author Hendrik Davi - april 2006
 */
public class FmClimate implements Serializable {

	private String littleFileName;
	private String completeFileName; // climate file name
	private FmClimateDay[] days;

	private int currentIndex;

	private int lastMonth;
	private int lastYear;
	private int dayCount;
	private int monthCount;
	private int yearCount;

	private boolean monthChanged;
	private boolean yearChanged;
	private double frach;

	private FmSettings settings; // fc-11.6.2014 for logPrefix 
	
	/**
	 * Default constructor
	 */
	public FmClimate () {

	}

	/**
	 * Constructor
	 */
	public FmClimate (String completeFileName, FmClimateDay[] days, double frach, FmSettings settings) {
		this.completeFileName = completeFileName;
		this.littleFileName = new File (completeFileName).getName ();
		this.days = days;
		this.frach = frach;
		
		this.settings = settings; // fc-11.6.2014 for logPrefix 
	}

	/**
	 * Sets the climateDays array to the given value
	 */
	public void setClimateDay (FmClimateDay[] v) {
		days = v;
	}

	/**
	 * Returns a perfect copy of this object
	 */
	public FmClimate getClimateCopy () {

		// Prepare the copy
		FmClimateDay[] daysCopy = null;
		if (days != null) {
			daysCopy = new FmClimateDay[days.length];
			for (int i = 0; i < days.length; i++) {
				daysCopy[i] = days[i].getCopy ();
			}
		}

		// Create the copy
		FmClimate copy = new FmClimate ();

		copy.littleFileName = this.littleFileName;
		copy.completeFileName = this.completeFileName;

		copy.days = daysCopy; // this a an array of objects, see upper

		copy.currentIndex = this.currentIndex;
		copy.lastMonth = this.lastMonth;
		copy.lastYear = this.lastYear;
		copy.dayCount = this.dayCount;
		copy.monthCount = this.monthCount;
		copy.yearCount = this.yearCount;
		copy.monthChanged = this.monthChanged;
		copy.yearChanged = this.yearChanged;

		return copy;
	}

	public boolean contains (int year, int day) {
		for (int i = 0; i < days.length; i++) {
			FmClimateDay d = days[i];
			if (d.getYear () == year && d.getDay () == day) { return true; }
		}
		return false;
	}

	/**
	 * Use this method to set the reading index on a given year / day. A call to next () will return
	 * the FmClimateDay located at this current index.
	 */
	public boolean init (int year, int day) {
		monthChanged = false;
		yearChanged = false;
		lastMonth = 0;
		lastYear = 0;
		dayCount = 0;
		monthCount = 0;
		yearCount = 0;

		// Log.println(settings.logPrefix+"DynaclimTest", "day=" +day);

		for (int i = 0; i < days.length; i++) {
			FmClimateDay d = days[i];
			if (d.getYear () == year && d.getDay () == day) {
				currentIndex = i;
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the FmClimateDay at the current value of currentIndex. When the end is reached,
	 * returns null. See init ().
	 */
	public FmClimateDay nextWithoutLooping () {
		// when last element is reached, return null
		if (currentIndex > days.length - 1) { return null; }
		FmClimateDay d = days[currentIndex];
		return basicNext (d);
	}

	/**
	 * Returns the FmClimateDay at the current value of currentIndex. When the end is reached, loops
	 * (with a modulo) to return the first entry. Never returns null. See init ().
	 */
	public FmClimateDay next () {

		FmClimateDay d = days[currentIndex % days.length];
		return basicNext (d);
	}

	/**
	 * A private method to factorize some processes performed by next () and nextWithoutLooping ().
	 */
	private FmClimateDay basicNext (FmClimateDay d) {
		monthChanged = false;
		yearChanged = false;

		// ~ FmClimateDay d = days[currentIndex % days.length];
		dayCount++;

		// ~ System.out.println ("climate.next ()");
		// ~ System.out.println ("d="+d.getYear ()+" "+d.getMonth ()+" "+d.getDay ());

		if (lastMonth != 0 && d.getMonth () != lastMonth) {
			monthChanged = true;
			monthCount++;
		}
		if ((lastYear != 0 && d.getYear () != lastYear) || (currentIndex != 0 && currentIndex % days.length == 0)) {
			yearChanged = true;
			yearCount++;
		}

		lastMonth = d.getMonth ();
		lastYear = d.getYear ();

		currentIndex++;

		// ~ System.out.println ("  dayCount="+dayCount);
		// ~ System.out.println ("   monthChanged="+monthChanged);
		// ~ System.out.println ("   monthCount="+monthCount);
		// ~ System.out.println ("    yearChanged="+yearChanged);
		// ~ System.out.println ("    yearCount="+yearCount);

		return d;
	}

	public boolean monthChanged () {
		return monthChanged;
	}

	public boolean yearChanged () {
		return yearChanged;
	}

	public int getDayCount () {
		return dayCount;
	}

	public int getMonthCount () {
		return monthCount;
	}

	public int getYearCount () {
		return yearCount;
	}

	public FmClimateDay getDay (int i) {
		return days[i];
	}

	public String getLittleFileName () {
		return littleFileName;
	}

	public String getCompleteFileName () {
		return completeFileName;
	}

	public FmClimateDay[] getDays () {
		return days;
	}

	/**
	 * Return the yearly means of all variables from yyyyMin to yyyyMax included
	 */
	public List<FmClimateDay> getYearlyMeans (int yyyyMin, int yyyyMax) throws Exception {
		List<FmClimateDay> days = new ArrayList<FmClimateDay> ();

		this.init (yyyyMin, 1);

		int cptYear = 0;
		int nbDays = 0;
		double grSum = 0;
		double rhSum = 0;
		double wsSum = 0;
		double pSum = 0;
		double maxtSum = 0;
		double mintSum = 0;
		double tSum = 0;

		boolean finished = false;
		do {
			FmClimateDay day = this.nextWithoutLooping ();
			if (day != null && day.getYear () <= yyyyMax) {
				nbDays++;
				grSum += day.getDailyGlobalRadiation ();
				rhSum += day.getDailyRelativeHumidity ();
				wsSum += day.getDailyWindSpeed ();
				pSum += day.getDailyPrecipitation ();
				maxtSum += day.getDailyMaxTemperature ();
				mintSum += day.getDailyMinTemperature ();
				tSum += day.getDailyAverageTemperature ();

			} else {
				finished = true;
			}
			if (finished || this.yearChanged ()) {
				double grMean = grSum / nbDays;
				double rhMean = rhSum / nbDays;
				double wsMean = wsSum / nbDays;
				double pMean = pSum / nbDays;
				double maxtMean = maxtSum / nbDays;
				double mintMean = mintSum / nbDays;
				double tMean = tSum / nbDays;

				days.add (new FmClimateDay (cptYear, 0, // month
						0, // day

						grMean, rhMean, wsMean, pMean, maxtMean, mintMean, tMean, -1, -1, -1, frach, settings));

				// ~ relativeHumidity,
				// ~ windSpeed,
				// ~ precipitation,
				// ~ maxTemperature,
				// ~ minTemperature,
				// ~ averageTemperature,
				// ~ T_old);

				cptYear++;
				nbDays = 0;
				grSum = 0;
				rhSum = 0;
				wsSum = 0;
				pSum = 0;
				maxtSum = 0;
				mintSum = 0;
				tSum = 0;
			}

		} while (!finished);

		return days;
	}

	/**
	 * Return the monthly means of all variables from yyyyMin to yyyyMax included
	 */
	public List<FmClimateDay> getMonthlyMeans (int yyyyMin, int yyyyMax) throws Exception {
		List<FmClimateDay> days = new ArrayList<FmClimateDay> ();
		this.init (yyyyMin, 1);

		int cptYear = 0;
		int nbDays = 0;
		double grSum = 0;
		double rhSum = 0;
		double wsSum = 0;
		double pSum = 0;
		double maxtSum = 0;
		double mintSum = 0;
		double tSum = 0;

		boolean finished = false;
		do {
			FmClimateDay day = this.nextWithoutLooping ();
			if (day != null && day.getYear () <= yyyyMax) {
				nbDays++;
				grSum += day.getDailyGlobalRadiation ();
				rhSum += day.getDailyRelativeHumidity ();
				wsSum += day.getDailyWindSpeed ();
				pSum += day.getDailyPrecipitation ();
				maxtSum += day.getDailyMaxTemperature ();
				mintSum += day.getDailyMinTemperature ();
				tSum += day.getDailyAverageTemperature ();

			} else {
				finished = true;
			}
			if (finished || this.monthChanged ()) {
				double grMean = grSum / nbDays;
				double rhMean = rhSum / nbDays;
				double wsMean = wsSum / nbDays;
				double pMean = pSum / nbDays;
				double maxtMean = maxtSum / nbDays;
				double mintMean = mintSum / nbDays;
				double tMean = tSum / nbDays;

				days.add (new FmClimateDay (cptYear, 0, // month
						0, // day

						grMean, rhMean, wsMean, pMean, maxtMean, mintMean, tMean, -1, -1, -1, frach, settings));

				// ~ relativeHumidity,
				// ~ windSpeed,
				// ~ precipitation,
				// ~ maxTemperature,
				// ~ minTemperature,
				// ~ averageTemperature,
				// ~ T_old);

				cptYear++;
				nbDays = 0;
				grSum = 0;
				rhSum = 0;
				wsSum = 0;
				pSum = 0;
				maxtSum = 0;
				mintSum = 0;
				tSum = 0;
			}

		} while (!finished);

		return days;
	}

	public double[][] getMobileMeans (int mobileValue) {

		double[][] daysMobile = new double[days.length][7];
		int cptYear = 0;
		int nbDays = 0;
		int k = 0;
		double[] grDay = new double[days.length];
		double[] rhDay = new double[days.length];;
		double[] wsDay = new double[days.length];;
		double[] pDay = new double[days.length];;
		double[] maxtDay = new double[days.length];;
		double[] mintDay = new double[days.length];;
		double[] tDay = new double[days.length];;

		double grMean = 0;
		double grSum = 0;
		double rhMean = 0;
		double rhSum = 0;
		double wsMean = 0;
		double wsSum = 0;
		double pMean = 0;
		double pSum = 0;
		double maxtMean = 0;
		double maxtSum = 0;
		double mintMean = 0;
		double mintSum = 0;
		double tMean = 0;
		double tSum = 0;

		// davi 08/06/2010 need to creat each table before or averaging value
		for (int i = 0; i < days.length; i++) { // /pb dont know the length!
			FmClimateDay day = this.nextWithoutLooping ();
			grDay[i] = day.getDailyGlobalRadiation ();
			rhDay[i] = day.getDailyRelativeHumidity ();
			wsDay[i] = day.getDailyWindSpeed ();
			pDay[i] = day.getDailyPrecipitation ();
			maxtDay[i] = day.getDailyMaxTemperature ();
			mintDay[i] = day.getDailyMinTemperature ();
			tDay[i] = day.getDailyAverageTemperature ();

		}

		for (int i = 0; i < days.length; i++) {
			nbDays++;

			if (i < mobileValue - 1) {
				grSum += grDay[i];
				rhSum += rhDay[i];
				wsSum += wsDay[i];
				pSum += pDay[i];
				maxtSum += maxtDay[i];
				mintSum += mintDay[i];
				tSum += tDay[i];

				grMean = grSum / nbDays;
				rhMean = rhSum / nbDays;
				wsMean = wsSum / nbDays;
				pMean = pSum / nbDays;
				maxtMean = maxtSum / nbDays;
				mintMean = mintSum / nbDays;
				tMean = tSum / nbDays;

			} else {
				for (int kk = 0; kk < mobileValue; kk++) {
					grSum += grDay[i - kk];
					rhSum += rhDay[i - kk];
					wsSum += wsDay[i - kk];
					pSum += pDay[i - kk];
					maxtSum += maxtDay[i - kk];
					mintSum += mintDay[i - kk];
					tSum += tDay[i - kk];

				}

				grMean = grSum / mobileValue;
				rhMean = rhSum / mobileValue;
				wsMean = wsSum / mobileValue;
				pMean = pSum / mobileValue;
				maxtMean = maxtSum / mobileValue;
				mintMean = mintSum / mobileValue;
				tMean = tSum / mobileValue;

				daysMobile[i][0] = grMean;
				daysMobile[i][1] = rhMean;
				daysMobile[i][2] = wsMean;
				daysMobile[i][3] = pMean;
				daysMobile[i][4] = maxtMean;
				daysMobile[i][5] = mintMean;
				daysMobile[i][6] = tMean;

			}
		}

		return daysMobile;
	}

}
