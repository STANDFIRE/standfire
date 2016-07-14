package capsis.lib.phenofit.function.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import jeeb.lib.util.Settings;

/**
 * Reads a senescence mean date file and returns a date for a given location.
 * 
 * @author Isabelle Chuine, Yassine Motie - February 2015
 */
public class FixedDateProcess {

	String fileName;
	List<String[]> cols;
//	String folderNalmeLoc = FitInitialParameters.climateFolderNameLoc;

	public FixedDateProcess(String fileName) throws IOException {

		this.fileName = fileName;
		String folderNameLoc = Settings.getProperty("locationFixedDateFolder", "");
		cols = readFile(folderNameLoc + "/" + fileName + ".txt");
		
	}

	public double methodToCompare(double lat, double lon) throws IOException {
		int i = 0;
		double result = 0;

		do {
			i++;
			if (Double.parseDouble(cols.get(i)[0]) == lat && Double.parseDouble(cols.get(i)[1]) == lon) {
				result = Double.parseDouble(cols.get(i)[2]);
				return result;
			}

		} while (i <= cols.size());

		return result;
	}

	private static List<String[]> readFile(String fileName) throws IOException {
		List<String[]> values = new ArrayList<String[]>();
		Scanner s = new Scanner(new File(fileName));
		while (s.hasNextLine()) {
			String line = s.nextLine();
			values.add(line.split("\\s+"));
		}
		return values;
	}

}
