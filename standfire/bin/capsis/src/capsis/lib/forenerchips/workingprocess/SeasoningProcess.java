package capsis.lib.forenerchips.workingprocess;

import java.util.StringTokenizer;

import jeeb.lib.util.Check;
import capsis.lib.forenerchips.Resource;
import capsis.lib.forenerchips.ResourceSite;
import capsis.lib.forenerchips.ResourceStatus;
import capsis.lib.forenerchips.WorkingProcess;

/**
 * A seasoning working process (ressuyage)
 * 
 * @author N. Bilot - April 2013
 */
public class SeasoningProcess extends WorkingProcess {

  private String name;
	private double duration_month;
	private boolean cover;

	/**
	 * Constructor
	 */
	public SeasoningProcess (String name, double duration_month, boolean cover) throws Exception {
		super ("SeasoningProcess");

		// Check throws an exception if the condition is false
		check ("duration_month", duration_month >= 0);

		this.name = name;
		this.duration_month = duration_month;
		this.cover = cover;

		// What resource can be processed
		addCompatibleStatusOrSite (ResourceStatus.FALLEN_TREE);
		addCompatibleStatusOrSite (ResourceStatus.BRANCH);
		addCompatibleStatusOrSite (ResourceStatus.RESIDUAL);
		addCompatibleStatusOrSite (ResourceStatus.LOG);
		
		addCompatibleStatusOrSite (ResourceSite.PLOT);
		addCompatibleStatusOrSite (ResourceSite.ROADSIDE);

	}

	/**
	 * Creates an instance with all parameters in a single String, for scenarios in txt files.
	 */
	static public SeasoningProcess getInstance (String name, String parameters) throws Exception {
		StringTokenizer st = new StringTokenizer (parameters, " ");
		
    String wpName = name;
		double duration_month = doubleValue (st.nextToken ());
		boolean cover = booleanValue (st.nextToken ());
		
		return new SeasoningProcess (name, duration_month, cover);
	}

	/**
	 * Run the seasoning process
	 */
	@Override
	public void run () throws Exception {
		checkInputCompatibility ();

		// Outputs 1 resource: dryer with less dry matter
		Resource output = input.copy ();
    
    output.processName = name ;

		// Efficiency
		double efficiency = 1;

		// Consumptions
		output.machineWorkTime = 0d;
    output.humanWorkTime = 0d;
    output.fuelConsumption = 0d;
    output.oilConsumption = 0d;
    output.lcConsumption = 0d;
    output.logisticsConsumption = 0d;
    output.processConsumption = 0d;
    output.chainConsumption += 0d;

    
    // Moisture loss, by Nicolas Bilot, Dec 4, 2013
    double tmpMoisture = input.moistureContent * 100d ;
    if(cover){
      for (int i = 1; i < duration_month; i++) {
        tmpMoisture = -0.0091 * Math.pow(tmpMoisture, 2)  + 1.3782 * tmpMoisture - 4.3961 ;
      }
    } else {
      for (int i = 1; i < duration_month; i++) {
        tmpMoisture = -0.0121 * Math.pow(tmpMoisture, 2)  + 1.6804 * tmpMoisture -9.9131 ;
      }
    }
    output.moistureContent = tmpMoisture / 100d ;

    
		// Actualization of wet biomass considering moisture loss
		double dryBasisMC = (output.moistureContent) / (1d - output.moistureContent);
		
		output.wetBiomassBr0_4 = (input.wetBiomassBr0_4 * (1 - input.moistureContent)) * (dryBasisMC + 1);
		output.wetBiomassBr4_7 = (input.wetBiomassBr4_7 * (1 - input.moistureContent)) * (dryBasisMC + 1);
		output.wetBiomassBr7_more = (input.wetBiomassBr7_more * (1 - input.moistureContent)) * (dryBasisMC + 1);
		output.wetBiomassStem0_7 = (input.wetBiomassStem0_7 * (1 - input.moistureContent)) * (dryBasisMC + 1);
		output.wetBiomassStem7_more_top = (input.wetBiomassStem7_more_top * (1 - input.moistureContent)) * (dryBasisMC + 1);
    output.wetBiomassStem7_more_bole = (input.wetBiomassStem7_more_bole * (1 - input.moistureContent)) * (dryBasisMC + 1);
		output.wetBiomassLeaves = 0;
		
		// Dry biomass loss
		output.wetBiomassBr0_4 = output.wetBiomassBr0_4 - dryBiomassLoss (output.wetBiomassBr0_4, output.moistureContent, cover);
		output.wetBiomassBr4_7 = output.wetBiomassBr4_7 - dryBiomassLoss (output.wetBiomassBr4_7, output.moistureContent, cover);
		output.wetBiomassBr7_more = output.wetBiomassBr7_more - dryBiomassLoss (output.wetBiomassBr7_more, output.moistureContent, cover);
		output.wetBiomassStem0_7 = output.wetBiomassStem0_7 - dryBiomassLoss (output.wetBiomassStem0_7, output.moistureContent, cover);
		output.wetBiomassStem7_more_top = output.wetBiomassStem7_more_top - dryBiomassLoss (output.wetBiomassStem7_more_top, output.moistureContent, cover);
		output.wetBiomassStem7_more_bole = output.wetBiomassStem7_more_bole - dryBiomassLoss (output.wetBiomassStem7_more_bole, output.moistureContent, cover);
		
		output.updateBiomasses (efficiency);
		output.updateMineralMasses ();


    
		output.addProcessInHistory (this);

		outputs.add (output);

	}

	/**
	 * Calculates the loss in dry biomass (t) due to the seasoning process.
	 */
	private double dryBiomassLoss (double wetBiomass, double moistureContent, boolean cover) {
		double dryBiomassLoss_percent = 0;
		if (cover) {
			dryBiomassLoss_percent = 0.01d * duration_month;
		} else {
			dryBiomassLoss_percent = 0.02d * duration_month;
		}
		double dryBiomass = wetBiomass * (1 - moistureContent);
		double dryBiomassLoss_t = dryBiomass * (dryBiomassLoss_percent / 100d);
		return dryBiomassLoss_t;
		
	}

	public String toString () {
		return "SeasoningProcess" 
        + "name :" + name 
        + " duration_month:" + duration_month 
				+ " cover:" + cover;
	}

}
