package capsis.lib.phenofit;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import jeeb.lib.util.Log;

/**
 * The climate for a given location.
 * 
 * @author Isabelle Chuine, Yassine Motie - January 2015
 */
public class FitlibLocationClimate implements Serializable {

	private FitlibClimate climate; // fc+ym-8.6.2015

	private int year;
	private int locId;
	
	private Map<Integer, FitlibClimateDay> climateMap; // key: day
//	private int minYear = Integer.MAX_VALUE;
//	private int maxYear = -Integer.MAX_VALUE;

	/**
	 * Constructor
	 */
	public FitlibLocationClimate(FitlibClimate climate, int year, int locId) {
		this.climate = climate;
		this.year = year;
		this.locId = locId;
		climateMap = new HashMap<Integer, FitlibClimateDay>(); // key: day
	}

	// public void addClimateDay(FitlibClimateDay clim) {
	// climateMap.put(clim.year + "_" + clim.day, clim);
	//
	// minYear = Math.min(minYear, clim.year);
	// maxYear = Math.max(maxYear, clim.year);
	// }

	/**
	 * Option: changed the loaded tmp to be the mean of tmn and tmx.
	 * fc+ym-3.2.2015 CHECKED with I. Chuine
	 */
	public void computeTmpMeans() {
		for (FitlibClimateDay climDay : climateMap.values()) {
			climDay.tmp = (climDay.tmn + climDay.tmx) / 2f;
		}

	}

	public FitlibClimateDay loadClimateDay(int day) {
		FitlibClimateDay climDay = climateMap.get(day);
		// System.out.println("" + year + "_" + day);
		if (climDay == null) {
			climDay = new FitlibClimateDay(year, day);
			climateMap.put(day, climDay);

//			minYear = Math.min(minYear, year);
//			maxYear = Math.max(maxYear, year);
		}
		return climDay;
	}

	public int getId() {
		return locId;
	}

	public FitlibClimateDay getClimateDay(int y, int day) {
		
		// Day may be lower than 1, e.g. -122
		// -> 365 + (-122) of the previous year
		if (day <= 0) {
			y--;

			int nbDays = 365;
			if (FitlibClimate.isBissextile(y))
				nbDays++;

			// CHECKED correct with IC and YM
			day = nbDays + day; // e.g. 365 - 121 = 244
			
			// fc+ym-8.6.2015 climate was reorganized to save memory
			FitlibLocationClimate locClim = climate.getLocationClimate(y, locId);
			
			return locClim.getClimateDay(y, day);
			
		}
		
		FitlibClimateDay climDay = climateMap.get(day);
		
		// fc-7.10.2015 RESTORED this line in Log - Julie Gauzere has a bug
		if (climDay == null) {
			Log.println(Log.ERROR, "FitlibLocationClimate.getClimateDay ()", "could not find climDay for locId: "+locId+", y: "
					+ y + " and day: " + day+", returned null");
		}
		
		return climDay; // null if not found
	}

//	public int getMinYear() {
//		return minYear;
//	}

//	public int getMaxYear() {
//		return maxYear;
//	}

	// fc-22.6.2015 REPLACED by FitlibClimate.isBissextile(), getNbDays() and get1September ()
//	public boolean isBissextile(int y) {
//		
//		FitlibLocationClimate locClim = this;
//		if (y != year) {
//			locClim = climate.getLocationClimate(y, locId);
//		}
//		
//		FitlibClimateDay cd = locClim.getClimateDay(y, 366);
//		return cd != null;
//	}

	public int getYear() {
		return year;
	}

	public String traceContent() {
		StringBuffer b = new StringBuffer("FitlibLocationClimate locId: " + locId + " (extract)");

		b.append("\n  ");
		for (int d = 1; d <= 366; d++) {
			FitlibClimateDay climDay = getClimateDay(year, d);
			if (climDay != null) {
				b.append(climDay.traceContent());
				b.append(" | ");
			}
		}

		return b.toString();
	}

	public String toString() {
		return "FitlibLocationClimate: year: "+year+ " locId: " + locId + " #climateDays: " + climateMap.size();
	}

}