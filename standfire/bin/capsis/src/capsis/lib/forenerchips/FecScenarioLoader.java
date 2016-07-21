package capsis.lib.forenerchips;

//import java.util.HashMap;
import java.util.Iterator;
//import java.util.Map;

import jeeb.lib.util.Import;
import jeeb.lib.util.Record;
import jeeb.lib.util.RecordSet;
import capsis.lib.forenerchips.workingprocess.WorkingProcessFactory;

/**
 * Loads a ForEnerChips scenario from a text file.
 * 
 * @author N. Bilot, October 2014
 */
public class FecScenarioLoader extends RecordSet {
	
  // A record for FecScenario Phase
	@Import
	static public class PhaseRecord extends Record {

		public PhaseRecord () {
			super ();
		}

		public PhaseRecord (String line) throws Exception {
			super (line);
		}

		public int phaseNumber;
		public double beginDg;
		public double finalDg;
		public int numberOfFirstMethodUses;
    public String firstMethodName;
    public String secondMethodName;
	
  }
  
  private String fileName;
	
	/**
	 * Constructor
	 */
	public FecScenarioLoader (String fileName) throws Exception {
		super ();
		this.fileName = fileName;
		createRecordSet (fileName);
	}

	/**
	 * File interpretation
	 */
	public void interpret (FecScenario s) throws Exception {

    for (Iterator i = this.iterator (); i.hasNext ();) {
			Object record = i.next ();

			if (record instanceof KeyRecord) {
				KeyRecord r = (KeyRecord) record;
        
        // General parameters
				if (r.hasKey ("plotArea_ha")) {
					s.plotArea_ha = getDouble ("plotArea_ha", r);
				} else if (r.hasKey ("meanCarConsumption_kWperkm")) {
					s.meanCarConsumption_kWperkm = getDouble ("meanCarConsumption_kWperkm", r);        
        } else if (r.hasKey ("meanCarrierConsumption_kWperkm")) {
					s.meanCarrierConsumption_kWperkm = getDouble ("meanCarrierConsumption_kWperkm", r);        
        } else if (r.hasKey ("humanWorkingDayDuration_h")) {
					s.humanWorkingDayDuration_h = getDouble ("humanWorkingDayDuration_h", r);        
        } else if (r.hasKey ("contractorDistance_km")) {
					s.contractorDistance_km = getDouble ("contractorDistance_km", r);        
        } else if (r.hasKey ("deliveryDistance_km")) {
					s.deliveryDistance_km = getDouble ("deliveryDistance_km", r);        
        }
        
			// When the record correspond to a phase description
			} else if (record instanceof PhaseRecord) {
				// it is read
				PhaseRecord r = (PhaseRecord) record;
				// the parameters are implemented in a Phase
				FecScenario.Phase phase = new FecScenario.Phase (r.phaseNumber, r.beginDg, r.finalDg, r.numberOfFirstMethodUses, r.firstMethodName, r.secondMethodName);
				// the new Phase is implemented to the list of phases of the scenario
				s.phases.add(phase);
				
			} else {
				throw new Exception ("wrong format in " + fileName + " near record " + record);
			}


		}
    
    s.scenarioParameters = (" " + s.meanCarConsumption_kWperkm + " " + s.meanCarrierConsumption_kWperkm + " " + s.humanWorkingDayDuration_h + " " + s.contractorDistance_km + " " + s.deliveryDistance_km);
    
	}
    
    
  private String getString (String expectedVariableName, KeyRecord r) throws Exception {
		return r.value; // will hardly fail
	}

	private double getDouble (String expectedVariableName, KeyRecord r) throws Exception {
		try {
			return r.getDoubleValue ();
		} catch (Exception e) {
			throw new Exception ("Error in file: " + fileName + ", trouble with variable " + expectedVariableName
					+ ", expected a double value: " + r.value);
		}
	}

	private int getInt (String expectedVariableName, KeyRecord r) throws Exception {
		try {
			return r.getIntValue ();
		} catch (Exception e) {
			throw new Exception ("Error in file: " + fileName + ", trouble with variable " + expectedVariableName
					+ ", expected an int value: " + r.value);
		}
	}

	private boolean getBoolean (String expectedVariableName, KeyRecord r) throws Exception {
		try {
			return r.getBooleanValue ();
		} catch (Exception e) {
			throw new Exception ("Error in file: " + fileName + ", trouble with variable " + expectedVariableName
					+ ", expected a boolean value: " + r.value);
		}
	}

}
