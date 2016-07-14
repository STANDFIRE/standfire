package capsis.lib.forestgales;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import jeeb.lib.util.Import;
import jeeb.lib.util.Record;
import jeeb.lib.util.RecordSet;
import capsis.lib.forestgales.function.Function;

/**
 * A tool to load ForestGales' species file.
 * 
 * @author B. Gardiner, K. Kamimura - August 2013
 */
public class FGSpeciesLoader extends RecordSet {

	private String fileName;


	// Species line format
	@Import
	static public class SpeciesRecord extends Record {

		public SpeciesRecord () {
			super ();
		}

		public SpeciesRecord (String line) throws Exception {
			super (line);
		}

		public int id;
		public String name;
		public double topHeightMultiplier; // unitless
		public double topHeightIntercept; // m
		public String canopyWidthFunction; // f(dbh)
		public String canopyDepthFunction; // f(height)
		public double greenWoodDensity; // kg/m3
		public double canopyDensity; // kg/m3 branches + leaves / needles
		public double modulusOfRupture; // Pa
		public double knotFactor; // percentage, unitless, e.g. 0.8
		
		public double crownFactor; // unitless
		
		public double modulusOfElasticity; // Pa
		public double canopyStreamliningC; // unitless
		public double canopyStreamliningN; // unitless
		public double rootBendingK; // degrees, TO BE CHECKED
		public double MA1; // N.m/kg
		public double MA2; // N.m/kg
		public double MA3; // N.m/kg
		public double MB1; // N.m/kg
		public double MB2; // N.m/kg
		public double MB3; // N.m/kg
		public double MC1; // N.m/kg
		public double MC2; // N.m/kg
		public double MC3; // N.m/kg
		public double MD1; // N.m/kg
		public double MD2; // N.m/kg
		public double MD3; // N.m/kg

		public double WA1; // kg
		public double WA2; // kg
		public double WA3; // kg
		public double WB1; // kg
		public double WB2; // kg
		public double WB3; // kg
		public double WC1; // kg
		public double WC2; // kg
		public double WC3; // kg
		public double WD1; // kg
		public double WD2; // kg
		public double WD3; // kg

	}

	/**
	 * Constructor 1: reads the given file
	 */
	public FGSpeciesLoader (String fileName) throws Exception {
		super ();
		this.fileName = fileName;
		createRecordSet (fileName);

	}

	/**
	 * Interprets the species file, creates and returns a map.
	 */
	public Map<String,FGSpecies> interpret () throws Exception {
		Map<String,FGSpecies> speciesMap = new HashMap<String,FGSpecies> ();

		for (Iterator i = this.iterator (); i.hasNext ();) {
			Object record = i.next ();

			// Found a line, create a IsgmThinningStep object, add it to the collection
			if (record instanceof SpeciesRecord) {
				SpeciesRecord r = (SpeciesRecord) record;

				double[][] overturningMomentMultipliers = new double[4][3];
				overturningMomentMultipliers[0][0] = r.MA1;
				overturningMomentMultipliers[0][1] = r.MA2;
				overturningMomentMultipliers[0][2] = r.MA3;

				overturningMomentMultipliers[1][0] = r.MB1;
				overturningMomentMultipliers[1][1] = r.MB2;
				overturningMomentMultipliers[1][2] = r.MB3;

				overturningMomentMultipliers[2][0] = r.MC1;
				overturningMomentMultipliers[2][1] = r.MC2;
				overturningMomentMultipliers[2][2] = r.MC3;

				overturningMomentMultipliers[3][0] = r.MD1;
				overturningMomentMultipliers[3][1] = r.MD2;
				overturningMomentMultipliers[3][2] = r.MD3;

				double[][] overturningMomentMaximumStemWeights = new double[4][3];
				overturningMomentMaximumStemWeights[0][0] = r.WA1;
				overturningMomentMaximumStemWeights[0][1] = r.WA2;
				overturningMomentMaximumStemWeights[0][2] = r.WA3;

				overturningMomentMaximumStemWeights[1][0] = r.WB1;
				overturningMomentMaximumStemWeights[1][1] = r.WB2;
				overturningMomentMaximumStemWeights[1][2] = r.WB3;

				overturningMomentMaximumStemWeights[2][0] = r.WC1;
				overturningMomentMaximumStemWeights[2][1] = r.WC2;
				overturningMomentMaximumStemWeights[2][2] = r.WC3;

				overturningMomentMaximumStemWeights[3][0] = r.WD1;
				overturningMomentMaximumStemWeights[3][1] = r.WD2;
				overturningMomentMaximumStemWeights[3][2] = r.WD3;

				Function canopyWidthFunction = Function.getFunction (r.canopyWidthFunction);
				Function canopyDepthFunction = Function.getFunction (r.canopyDepthFunction);

				FGSpecies sp = new FGSpecies (r.id, r.name, r.topHeightMultiplier, r.topHeightIntercept,
						canopyWidthFunction, canopyDepthFunction, r.greenWoodDensity, r.canopyDensity,
						r.modulusOfRupture, r.knotFactor, r.crownFactor, r.modulusOfElasticity, r.canopyStreamliningC,
						r.canopyStreamliningN, r.rootBendingK, overturningMomentMultipliers,
						overturningMomentMaximumStemWeights);

				speciesMap.put (sp.getName (), sp);

			} else {
				throw new Exception ("wrong format in " + fileName + " near record " + record);
			}

		}
		return speciesMap;
	}

}
