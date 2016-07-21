package fireparadox.model.database;

import java.io.Serializable;

import capsis.lib.fire.fuelitem.FiParticle;
import fireparadox.model.layerSet.FmLayer;



/**	A layer is an element in a layer set (for example a kermes oak strata in a garrigue layerSet)
 * A layer contains all the property of a fuel family
 * Notice that the class FiLocalLayer extend the present class to add other
 * properties for local layers (not database) :
 * aliveBulkDensity,deadBulkDensity, svr, mvr;

*	@author F. de Coligny, F Pimont - march 2009
*/
public class FmLayerFromDB extends FmLayer implements Serializable {

	// FiLayerSyntheticDataBaseData
	private long shapeId; // UUID of the shape in the database
	private int dominance; // ISOLATED, EMERGENT...
	private double meanBulkDensityEdge; // mean bulk density for 0-2mm
												// particules (edge)
	private double laiEdge; // leaf area index (edge)
	private String teamName; // team name (owner)
	private boolean checked; // plant is validated true/false


	
	/**	Constructor
	*/
	public FmLayerFromDB (FmLayerSyntheticDataBaseData data, double coverFraction, double characteristicSize, int spatialGroup, double liveMoisture, double deadMoisture) {

		this.shapeId = data.getShapeId ();
		this.layerType = data.getSpeciesName ();
		this.height = data.getHeight ();
		this.baseHeight = data.getBottomHeight ();
		this.dominance = data.getDominance ();
		// this.modelMeanBulkDensity = data.getMeanThinBulkdensity();
		this.meanBulkDensityEdge = data.getThinBulkdensityEdge ();
		//this.lai = data.getLai ();
		this.laiEdge = data.getLaiEdge ();
		this.teamName = data.getTeamName ();
		this.checked = data.isChecked ();
		this.setSpatialGroup(spatialGroup);
		this.setCharacteristicSize(characteristicSize);
		if (characteristicSize <= 0d) {
			this.setCoverFraction(1d);
		} else {
			this.setCoverFraction(coverFraction);
		}
		// TODO FP moisture for bd. Be careful with "copy"
		// this.setLiveMoisture(liveMoisture);
		// this.setDeadMoisture(deadMoisture);
	}	
	
	
	/**	Returns a copy of the FiLayer.
	 * @throws Exception 
	*/
	public FmLayerFromDB copy () throws Exception {
		FmLayerSyntheticDataBaseData data = new FmLayerSyntheticDataBaseData (
				shapeId,
				layerType, 
				height,
				baseHeight,
				dominance,
				getSumBulkDensity (FiParticle.ALL),
				meanBulkDensityEdge,
				getLai(),
				laiEdge,
				teamName,
				checked
				);
		//return  new FiLayer (data);
		FmLayerFromDB l = new FmLayerFromDB (data, coverFraction, characteristicSize, spatialGroup, 0d, 0d);// liveMoisture,
		// deadMoisture);
		return l;
	}

	
	public long getShapeId () {return shapeId;}
	public int getDominance () {return dominance;}
	public double getMeanBulkDensityEdge () {return meanBulkDensityEdge;}
	public double getLaiEdge () {return laiEdge;}
	public String getTeamName () {return teamName;}
	public boolean isChecked () {return checked;}
	public void setMeanBulkDensityEdge (double v) {meanBulkDensityEdge = v;}
	public void setLAIEdge (double v) {laiEdge = v;}
		
	
	@Override
	public String toString () {
		return "FiLayer shapeId=" + getShapeId ();
	}
	
	

}
