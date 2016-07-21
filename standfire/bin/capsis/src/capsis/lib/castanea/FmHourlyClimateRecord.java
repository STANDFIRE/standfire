package capsis.lib.castanea;

import jeeb.lib.util.Record;

/**
 * A format description for a line in an hourly climate file.
 * 
 * @author H. Davi - April 2013
 */
public class FmHourlyClimateRecord extends Record {

	public FmHourlyClimateRecord () {
		super ();
	}

	public FmHourlyClimateRecord (String line) throws Exception {
		super (line);
	}

	public int year;
	public int month;
	public int day; // julian day
	public int hour; // 0-23
	public double globalRadiation;
	public double temperature;
	public double relativeHumidity;
	public double windSpeed;
	public double precipitation;

}
