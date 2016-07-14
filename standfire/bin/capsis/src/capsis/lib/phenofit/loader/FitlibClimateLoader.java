package capsis.lib.phenofit.loader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import jeeb.lib.util.AmapTools;
import jeeb.lib.util.Check;
import jeeb.lib.util.Log;
import jeeb.lib.util.StatusDispatcher;
import jeeb.lib.util.Translator;
import capsis.lib.phenofit.FitlibClimate;
import capsis.lib.phenofit.FitlibClimateDay;
import capsis.lib.phenofit.FitlibLocationClimate;

/**
 * FitlibClimateLoader loads files in a climate folder for a given climate
 * scenario to create a climate object.
 * 
 * @author Isabelle Chuine, Yassine Motie - January 2015
 */
public class FitlibClimateLoader {

	static {
		Translator.addBundle("capsis.lib.phenofit.FitlibLabels");
	}

	public String climateFolderName;
	public String climateScenario;
	public int locationNumber;
	// public short endingYear; // fc+ym-5.5.2015
	public int year1; // fc+ym-1.6.2015
	public int year2; // fc+ym-1.6.2015

	private FitlibClimate climate;

	/**
	 * Constructor.
	 */
	public FitlibClimateLoader(FitlibClimate climate, String climateFolderName, String climateScenario,
			int locationNumber) {
		this.climate = climate;
		this.climateFolderName = climateFolderName;
		this.climateScenario = climateScenario;
		this.locationNumber = locationNumber;
		// this.endingYear = endingYear; // fc+ym-5.5.2015
	}

	/**
	 * Loads the climate information for all locations between the two given
	 * dates included.
	 */
	public FitlibClimate loadClimate(int year1, int year2) throws Exception {

		System.out.println("Loading climate, folder name " + climateFolderName + "...");
		System.out.println("Loading climate, scenario " + climateScenario + "...");

		this.year1 = year1;
		this.year2 = year2;

		// climate = new FitlibClimate(locationNumber, endingYear);

		loadFiles("glo");
		loadFiles("pre");
		loadFiles("RH");
		loadFiles("tmn");
		loadFiles("tmp");
		loadFiles("tmx");
		loadFiles("wnd");
		System.out.println("loaded files for 7 climaticVariables for year " + year1);

		// System.out.println(Translator.swap("FitlibClimateLoader.climateFilesLoadingOver"));
		StatusDispatcher.print(Translator.swap("FitlibClimateLoader.climateFilesLoadingOver"));

		return climate;
	}

	private void loadFiles(String climaticVariable) throws Exception {

		Map<Integer, String> fileNames = getFileNames(climaticVariable);

		int n = fileNames.size();

		// This level is a bit written too often
		// StatusDispatcher.print(Translator.swap("FitlibClimateLoader.loadingFilesForClimaticVariable")
		// + " : "
		// + climaticVariable + " (" + n + " " +
		// Translator.swap("FitlibClimateLoader.files") + ")");

		// System.out.println(Translator.swap("FitlibClimateLoader.loading") +
		// " " + n + " "
		// + Translator.swap("FitlibClimateLoader.climateFilesForVariable") +
		// " " + climaticVariable+":"+year1+" -> "+year2);

		// StatusDispatcher.print(Translator.swap("FitlibClimateLoader.loading")
		// +
		// " " + n
		// + Translator.swap("FitlibClimateLoader.climateFilesForVariable") +
		// " " +
		// climaticVariable);

		List<Integer> copy = new ArrayList<>(fileNames.keySet());
		Collections.sort(copy);

		for (int year : copy) {
			String fileName = fileNames.get(year);
			loadFile(climaticVariable, year, fileName);
		}

	}

	/**
	 * Loads a climate file for the given climatic variable and year. The
	 * fileName depends on conventions. fc-2.9.2015 Restored the original
	 * method, does not try to accept lines with errors inside (extra blanks...)
	 */
	private void loadFile(String climaticVariable, int year, String fileName) throws Exception {

		// System.out.println("FitlibClimateLoader climaticVariable: "+climaticVariable+" year: "+year+" fileName: "+fileName);

		String fullFileName = climateFolderName + "/" + fileName;

		try {

			// Map<Short, FitlibLocationClimate> locClimMap =
			// climate.getLocationClimates(year);
			// Iterator it = climate.getLocationClimates(year).iterator();

			int k = 0;
			String[] tokens;
			BufferedReader in = new BufferedReader(new FileReader(fullFileName));

			// System.out.println("FitlibClimateLoader loading file: "+fullFileName+"...");

			// fc-26.10.2015 Check there are no duplicates (Julie Gauzere repeated
			// problems with duplicates in input files)
			Set<String> loadedLocations = new HashSet<>();

			String line;
			while ((line = in.readLine()) != null) {
				k++; // line number
				if (k <= 4)
					continue; // jump 4 comment lines at the beginning

				// FitlibLocationClimate locClim = (FitlibLocationClimate)
				// it.next();

				StringTokenizer t = new StringTokenizer(line, "\t");

				// Columns 1 and 2: latitude and longitude
				double latitude = Double.parseDouble(t.nextToken());
				double longitude = Double.parseDouble(t.nextToken());

				String locKey = "" + latitude + "/" + longitude;
				if (loadedLocations.contains(locKey))
					throw new Exception("Duplicate error, found two lines for location: " + locKey
							+ " in climate file: " + fullFileName);

				// fc+ym-1.6.2015
				int locId = climate.getLocId(latitude, longitude);

				FitlibLocationClimate locClim = climate.getLocationClimate(year, locId);

				if (locClim == null) {
					throw new Exception("ClimateLoader could not load: " + fullFileName + "\n"
							+ "Could not find locClim for year: " + year + " locId: " + locId + " latitude: "
							+ latitude + " longitude: " + longitude + "\n" + "Please check the input file format");
				}

				int day = 1;
				while (t.hasMoreTokens()) {
					String value = t.nextToken();

					// fc-24.9.2015 BigDecimal + DECIMAL32 = better rounding
					float v = new BigDecimal(value, MathContext.DECIMAL32).floatValue();
					// float v = (float) Float.parseFloat(value);
					// float v = (float) Double.parseDouble(value);

//					System.out.println("FtClimateLoader day: " + day + " value: " + value + " v: " + v);

					FitlibClimateDay climDay = locClim.loadClimateDay(day);
					climDay.setValue(climaticVariable, v);

					day++;
				}

				loadedLocations.add(locKey);
			}
			in.close();
		} catch (Exception e) {
			String m = "Error while reading in file: " + fullFileName;
			Log.println(Log.ERROR, "FitlibClimateLoader.loadFile ()", m, e);
			throw new Exception(m, e);
		}

	}

	/**
	 * This version by Yassine Motié relies on a regular expression to help load
	 * files with errors in their lines (extra spaces...). Deprecated by
	 * fc-2.9.2015: the conventions should be respected and the regular
	 * expression is more complicated and less robust than a simple
	 * StringTokenizer.
	 */
	@Deprecated
	private void loadFileMotie(String climaticVariable, int year, String fileName) throws Exception {

		// System.out.println("FitlibClimateLoader climaticVariable: "+climaticVariable+" year: "+year+" fileName: "+fileName);

		String fullFileName = climateFolderName + "/" + fileName;

		try {

			// Map<Short, FitlibLocationClimate> locClimMap =
			// climate.getLocationClimates(year);
			// Iterator it = climate.getLocationClimates(year).iterator();

			int k = 0;
			String[] tokens;
			BufferedReader in = new BufferedReader(new FileReader(fullFileName));

			// System.out.println("FitlibClimateLoader loading file: "+fullFileName+"...");

			String line, firstspace;
			while ((line = in.readLine()) != null) {
				k++; // line number
				if (k <= 4)
					continue; // jump 4 comment lines at the beginning

				// FitlibLocationClimate locClim = (FitlibLocationClimate)
				// it.next();

				firstspace = line.trim(); // espace de debut
				// yassine/////////////////////////////////////////////////////////
				tokens = firstspace.split("\\s+");

				// StringTokenizer t = new StringTokenizer(line, "\t");

				// Columns 1 and 2: latitude and longitude
				// double latitude = Double.parseDouble(t.nextToken());
				// double longitude = Double.parseDouble(t.nextToken());
				double latitude = Double.parseDouble(tokens[0]);
				double longitude = Double.parseDouble(tokens[1]);

				// fc+ym-1.6.2015
				int locId = climate.getLocId(latitude, longitude);

				FitlibLocationClimate locClim = climate.getLocationClimate(year, locId);
				// FitlibLocationClimate locClim = locClimMap.get(locId);

				if (locClim == null) {
					throw new Exception("ClimateLoader could not load: " + fullFileName + "\n"
							+ "Could not find locClim for year: " + year + " locId: " + locId + " latitude: "
							+ latitude + " longitude: " + longitude + "\n" + "Please check the input file format");
				}

				int day = 1;
				for (int j = 2; j < tokens.length; j++) {
					// while (t.hasMoreTokens()) {
					String value = tokens[j];
					// String value = t.nextToken();

					float v = (float) Double.parseDouble(value);
					// System.out.println("FtClimateLoader day: "+day+" value: "+value+" v: "+v);

					FitlibClimateDay climDay = locClim.loadClimateDay(day);
					climDay.setValue(climaticVariable, v);

					// System.out.println("-> climDay: "+climDay.traceContent()
					// );

					day++;
				}
				tokens = null;
				// System.out.println("line : " + k);
			}
			in.close();
		} catch (Exception e) {
			String m = "Error while reading in file: " + fullFileName;
			Log.println(Log.ERROR, "FitlibClimateLoader.loadFileMotie ()", m, e);
			throw new Exception(m, e);
		}

	}

	private Map<Integer, String> getFileNames(String climaticVariable) throws Exception {

		// fc-2.9.2015 Check the 2 years of the bounds are available. Note:
		// year1 and year2 may be equal.
		Set<Integer> expectedYears = new HashSet<>();
		expectedYears.add(year1);
		expectedYears.add(year2);

		Map<Integer, String> fileNames = new LinkedHashMap<Integer, String>();

		File dir = new File(climateFolderName);

		final String filePrefix = climateScenario + "_" + climaticVariable; // CERFACS_SCION_glo

		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File directory, String fileName) {
				return fileName.startsWith(filePrefix);
			}
		};

		String[] files = dir.list(filter);

		if (files == null || files.length == 0) {
			throw new Exception("Could not find any climatic file for the variable " + climaticVariable
					+ " in climate folder: " + climateFolderName + ", should start with: " + filePrefix);
		}

		for (int i = 0; i < files.length; i++) {
			String f = files[i]; // CERFACS_SCION_glo_1951_dly.fit
			String a = f.substring(filePrefix.length() + 1); // 1951_dly.fit
			a = a.substring(0, a.indexOf("_")); // 1951
			int yyyy = Check.intValue(a); // 1951

			// fc+ym-5.5.2015 restriction to the given date range
			if (yyyy >= year1 && yyyy <= year2) {
				fileNames.put(yyyy, f); // year -> fileName
				expectedYears.remove(yyyy); // found
			}
		}

		// fc-2.9.2015
		if (!expectedYears.isEmpty()) {
			throw new Exception("Missing climate file for year: " + AmapTools.toString(expectedYears));
		}

		return fileNames;
	}

	// public static void main(String[] args) throws Exception {
	//
	// String climateFolderName = args[0];
	// String climateScenario = args[1];
	//
	// //FitlibClimate climate = new FitlibClimateLoader(climateFolderName,
	// climateScenario, 38, 1955).loadClimate();
	//
	// //System.out.println("Loaded climate: " + climate.traceContent());
	//
	// }

}
