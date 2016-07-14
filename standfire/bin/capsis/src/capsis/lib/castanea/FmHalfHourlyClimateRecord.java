package capsis.lib.castanea;

import jeeb.lib.util.Record;

/**
 * A format description for a line in an HALF hourly climate file.
 * 
 * @author H. Davi - April 2013
 */
public class FmHalfHourlyClimateRecord extends Record {

	public FmHalfHourlyClimateRecord () {
		super ();
	}

	public FmHalfHourlyClimateRecord (String line) throws Exception {
		super (line);
	}

	public int year;
	public int month;
	public int day; // julian day
	public int hour; // 0-23
	public int mn; // 0 OR 30
	public double globalRadiation;
	public double par; // unused yet, to have a format different from daily and hourly files
	public double temperature;
	public double relativeHumidity;
	public double windSpeed;
	public double precipitation;

}
