package capsis.lib.castanea;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import jeeb.lib.util.Import;
import jeeb.lib.util.Log;
import jeeb.lib.util.Record;
import jeeb.lib.util.RecordSet;

/**
 * A format description to read a climate file at daily time step
 * 
 * @author H. Davi - 19.4.2006
 */
public class FmClimateReader extends RecordSet {

	private String completeFileName;


	// Every RecordSet may contain DailyClimateRecord
	@Import
	static public class DailyClimateRecord extends Record {

		public DailyClimateRecord () {
			super ();
		}

		public DailyClimateRecord (String line) throws Exception {
			super (line);
		}

		// public String getSeparator () {return ";";} // to change default "\t" separator
		public int year;
		public int month;
		public int day; // julian day
		public double globalRadiation;
		public double relativeHumidity;
		public double windSpeed;
		public double precipitation;
		public double maxTemperature;
		public double minTemperature;
		public double averageTemperature;
	}

	private FmClimate climate;

	/**
	 * Constructor
	 */
	public FmClimateReader (String completeFileName) throws Exception {
		super ();
		this.completeFileName = completeFileName;

		addAdditionalClass (FmHourlyClimateRecord.class);
		addAdditionalClass (FmHalfHourlyClimateRecord.class);
		
		createRecordSet (completeFileName);
	}

	// Interpret
	// Returns the little file name
	//
	public String interpret (FmSettings s, double latitude) throws Exception {

		FmClimateDay[] tab = null;
		int k = 0;
		double T_old = s.T_oldinit;

		int mode = 0; // 1: daily, 2: hourly, 3: halfHourly
		Map<String,FmClimateDay> dayMap = new LinkedHashMap<String,FmClimateDay> ();

		for (Iterator i = this.iterator (); i.hasNext ();) {
			Object record = i.next ();
			
			if (record instanceof DailyClimateRecord) {
				mode = 1;
				s.frach = 1;
				if (tab == null) tab = new FmClimateDay[this.size ()];

				DailyClimateRecord r = (DailyClimateRecord) record;

				FmClimateDay d = new FmClimateDay (r.year, r.month, r.day, r.globalRadiation, r.relativeHumidity,
						r.windSpeed, r.precipitation, r.maxTemperature, r.minTemperature, r.averageTemperature,
						s.latitude, s.longitude, T_old, s.frach, s);
				T_old = d.getTold ();
				
				tab[k++] = d;

			} else if (record instanceof FmHourlyClimateRecord) {
				FmHourlyClimateRecord r = (FmHourlyClimateRecord) record;
				mode = 2;
				s.frach = 1;
				if (tab == null) tab = new FmClimateDay[this.size () / 24];

				String dailyKey = ""+r.year+" "+r.month+" "+r.day;
				
				FmClimateDay day = dayMap.get (dailyKey);
				if (day == null) {
					day = new FmClimateDay (r.year, r.month, r.day, s);
					dayMap.put (dailyKey, day);
					
					tab[k++] = day;
				}
				day.addHourlyClimateRecord (r);

			} else if (record instanceof FmHalfHourlyClimateRecord) {
				FmHalfHourlyClimateRecord r = (FmHalfHourlyClimateRecord) record;
				mode = 3;
				s.frach = 0.5;
				if (tab == null) tab = new FmClimateDay[this.size () / 48];

				String dailyKey = ""+r.year+" "+r.month+" "+r.day;
				
				FmClimateDay day = dayMap.get (dailyKey);
				if (day == null) {
					day = new FmClimateDay (r.year, r.month, r.day, s);
					dayMap.put (dailyKey, day);
					
					tab[k++] = day;
				}
				day.addHalfHourlyClimateRecord (r);
				
			} else {
				Log.println (Log.ERROR, "FmClimateReader.interpret ()", "wrong format in " + completeFileName
						+ " near record " + record);
				throw new Exception ("wrong format in " + completeFileName + " near record " + record);
			}
		}

		
		if (mode == 2 && dayMap != null) {
			for (FmClimateDay d : dayMap.values ()) {
				d.initHourly (s.frach); // 1: hourly. daily means, creation of all hourly variables
				
//				System.out.println ("" + d);
			}
		} else if (mode == 3 && dayMap != null) {
			for (FmClimateDay d : dayMap.values ()) {
				d.initHalfHourly (s.frach); // 0.5: halfHourly. daily means, creation of all half hourly variables
				
//				System.out.println ("" + d);
			}
		}

		climate = new FmClimate (completeFileName, tab, s.frach, s);
		String littleFileName = climate.getLittleFileName ();

		if (s != null) {
			s.addClimate (littleFileName, climate);
		}

		return littleFileName;
	}

	public FmClimate getClimate () {
		return climate;
	}

}
