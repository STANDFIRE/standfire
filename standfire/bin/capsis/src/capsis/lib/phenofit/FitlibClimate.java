package capsis.lib.phenofit;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;

import jeeb.lib.util.AmapTools;
import capsis.lib.phenofit.loader.FitlibClimateLoader;

/**
 * The climate object in the Phenofit model.
 * 
 * @author Isabelle Chuine, Yassine Motie - January 2015
 */
public class FitlibClimate implements Serializable {

	private int locationNumber;
	private FitlibClimateLoader loader;

	// year > locId > FitlibLocationClimate
	private Map<Integer, Map<Integer,FitlibLocationClimate>> climateMap; // fc+sg-6.7.2015
	
//	private Map<Integer, List<FitlibLocationClimate>> climateMap; // fc+ym-1.6.2015
																	// year ->
																	// locClimate
	private static GregorianCalendar calendar;

	// For each location, latitude_longitude -> locId (= locClimId)
	private NumberFormat nf;
	private Map<String, Integer> locIdMap;

	/**
	 * Constructor.
	 */
	public FitlibClimate(String climateFolderName, String climateScenario, int locationNumber, List<FitlibLocation> locs) {
		this.locationNumber = locationNumber;

		nf = NumberFormat.getInstance(Locale.ENGLISH);
		nf.setMaximumFractionDigits(7);
		nf.setGroupingUsed(false);

		climateMap = new HashMap<>();

		// Init locId map
		locIdMap = new HashMap<String, Integer>();
		for (FitlibLocation loc : locs) {
			String key = nf.format(loc.latitude) + "_" + nf.format(loc.longitude);
			locIdMap.put(key, loc.getId());
		}

		loader = new FitlibClimateLoader(this, climateFolderName, climateScenario, locationNumber);

	}
	
	public static int getYesterday (int year, int day) {
		int sept1 = FitlibClimate.get1September(year);
		int nbDays0 = FitlibClimate.getNbDays(year - 1);
		
		int yesterday = -1;
		if (FitlibPhenology.isPhenoStartDay(year, day)) {
			yesterday = sept1 - 1;
		} else if (day == 1) {
			yesterday = nbDays0;
		} else {
			yesterday = day - 1;
		}
		return yesterday;
	}
	
	/**
	 * Tells if the given year is bissextile (i.e. leap year).
	 */
	public static boolean isBissextile(int year) {
		if (calendar == null) {
			calendar = new GregorianCalendar();
		}
		return calendar.isLeapYear(year);
	}
	
	/**
	 * Returns the number of days in the given year.
	 */
	public static int getNbDays (int year) {
		int nbDays = 365;
		if (isBissextile(year)) {
			nbDays++;
		}
		return nbDays;
	}
	
	/**
	 * Returns the day number of 1st Sep of the given year.
	 */
	public static int get1September (int year) {
		int sept1 = 244;
		if (isBissextile(year)) {
			sept1++;
		}
		return sept1;
	}

	// For each location, latitude_longitude -> locId (= locClimId)
	public int getLocId(double latitude, double longitude) {
		String key = nf.format(latitude) + "_" + nf.format(longitude);
		Integer id = locIdMap.get(key);
		if (id == null)
			System.out.println("*** FitLibClimate: could not find locId for latitude: " + latitude + " and longitude: "
					+ longitude);
		return id;
	}

	public void load(int year1, int year2) throws Exception {

		// 1. release the uneeded years from memory
		List<Integer> yearsToBeRemoved = new ArrayList<Integer>();
		for (int y : climateMap.keySet()) {
			if (y < year1 || y > year2) {
				yearsToBeRemoved.add(y);
				System.out.println("FitlibClimate: released climate for year: " + y);
			}
		}

		for (int y : yearsToBeRemoved) {
			climateMap.remove(y);
		}

		// 2. Ensure the needed years are loaded
		for (int y = year1; y <= year2; y++) {

			if (isLoaded(y))
				continue;

			System.out.println("FitlibClimate: loading climate variables for year: " + y);

			Map<Integer,FitlibLocationClimate> locClims = new HashMap<Integer,FitlibLocationClimate>(); // locId -> locClim
			for (int locId = 1; locId <= locationNumber; locId++) {

				locClims.put(locId, new FitlibLocationClimate(this, y, locId));

			}

			climateMap.put(y, locClims);

			loader.loadClimate(y, y);

		}

	}

	public boolean isLoaded(int year) {
		return climateMap != null && climateMap.containsKey(year);
	}

	public void release(int year) {
		climateMap.remove(year);
	}

	public Map<Integer, FitlibLocationClimate> getLocationClimates(int y) {
		
		return climateMap.get(y);
		
//		Map<Integer, FitlibLocationClimate> locClimMap = new HashMap<>();
//
//		List<FitlibLocationClimate> locClims = climateMap.get(y);
//		for (FitlibLocationClimate locClim : locClims) {
//			locClimMap.put(locClim.getId(), locClim);
//		}
//
//		return locClimMap;
	}

	// fc+ym-8.6.2015
	public FitlibLocationClimate getLocationClimate(int year, int locId) {
		
		return climateMap.get(year).get (locId);
		
//		List<FitlibLocationClimate> locClims = climateMap.get(year);
//		for (FitlibLocationClimate locClim : locClims) {
//			if (locClim.getId() == locId) {
//				return locClim;
//			}
//		}
//		return null; // Error
		
		// fc+sg-6.7.2015 THIS WAS TOO LONG
//		Map<Integer, FitlibLocationClimate> locClimMap = this.getLocationClimates(year);
//		FitlibLocationClimate locClim = locClimMap.get(locId);
//		return locClim;
		
	}

	public int getLocationNumber() {
		return locationNumber;
	}

	// public int getEndingYear() {
	// return endingYear;
	// }

	public void computeTmpMeans() {
//		boolean done = false;
		
		for (Map<Integer,FitlibLocationClimate> locClimMap : climateMap.values()) {
			for (FitlibLocationClimate locClim : locClimMap.values ()) {
				locClim.computeTmpMeans();
//				if (!done) {
					// System.out.println(""+locClim.getClimateDay(1952, 77));
//					done = true;
//				}
			}
		}
		
		
//		for (List<FitlibLocationClimate> locClims : climateMap.values()) {
//			for (FitlibLocationClimate locClim : locClims) {
//				locClim.computeTmpMeans();
//				if (!done) {
//					// System.out.println(""+locClim.getClimateDay(1952, 77));
//					done = true;
//				}
//			}
//		}
	}

//	@Override
//	public String toString() {
//		StringBuffer b = new StringBuffer("FitlibClimate...");
//		for (int y : new TreeSet<Integer>(climateMap.keySet())) {
//			b.append('\n');
//			List<FitlibLocationClimate> locClims = climateMap.get(y);
//			b.append("year: " + y + ": " + AmapTools.toString(locClims));
//		}
//		return b.toString();
//	}

	// public String traceContent() {
	// int n = locationClimates == null ? 0 : locationClimates.size();
	//
	// StringBuffer b = new StringBuffer("FitlibClimate: #locationClimates: " +
	// n);
	// for (FitlibLocationClimate lc : locationClimates) {
	// b.append("\n  " + lc.traceContent());
	// }
	//
	// return b.toString();
	// }

}
