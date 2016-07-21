package fireparadox.gui.database;

import fireparadox.model.database.FmPlantSyntheticData;


/**	FiPlantFormLine contains a FiPlantSyntheticData with few additional
*	data. It is used to create trees from the FiPlantForm form.
*	@author F. de Coligny - september 2009
*/
public class FmPlantFormLine implements Comparable {

	private FmPlantSyntheticData plantSyntheticData;
    private double liveMoisture; // moisture entered by the user in the form
	private double deadMoisture; // moisture entered by the user in the form

	/**	Constructor
	*	Additional data (moisture is set to default value).
	*/
	public FmPlantFormLine (FmPlantSyntheticData plantSyntheticData) {
		this.plantSyntheticData = plantSyntheticData;
		this.liveMoisture = 0; // default value
		this.deadMoisture = 0; // default value
	}

	public FmPlantSyntheticData getPlantSyntheticData () {return plantSyntheticData;}
	
	// Redirections
		public long getShapeId () {return plantSyntheticData.getShapeId ();}
		public String getSpeciesName () {return plantSyntheticData.getSpeciesName ();}

		public double getVoxelDx () {return plantSyntheticData.getVoxelDx ();}
		public double getVoxelDy () {return plantSyntheticData.getVoxelDy ();}
		public double getVoxelDz () {return plantSyntheticData.getVoxelDz ();}

		public double getHeight () {return plantSyntheticData.getHeight ();}
		public double getCrownBaseHeight () {return plantSyntheticData.getCrownBaseHeight ();}
		public double getCrownDiameter () {return plantSyntheticData.getCrownDiameter ();}
		public double getCrownPerpendicularDiameter () {return plantSyntheticData.getCrownPerpendicularDiameter ();}
		public double getMaxDiameterHeight () {return plantSyntheticData.getMaxDiameterHeight ();}

		public int getDominance () {return plantSyntheticData.getDominance ();}
		public double getMeanBulkdensity0_2mm () {return plantSyntheticData.getMeanBulkdensity0_2mm ();}
		public double getLai () {return plantSyntheticData.getLai ();}

		public String getTeamName () {return plantSyntheticData.getTeamName ();}
		public boolean isChecked () {return plantSyntheticData.isChecked ();}
	// Redirections

	public double getLiveMoisture() {
		return liveMoisture;
	}

	public double getDeadMoisture() {
		return deadMoisture;
	}

	public void setLiveMoisture(double v) {
		liveMoisture = v;
	}

	public void setDeadMoisture(double v) {
		deadMoisture = v;
	}

	/**	Define an order on these objects.
	*/
	public int compareTo (Object o) {
		try {
			return sortingKey ().compareTo (((FmPlantFormLine) o).sortingKey ());
		} catch (Exception e) {
			System.out.println ("FiPlantFormLine exception in compareTo: "+e);
			return -1;	// in case of class cast exception upper
		}
	}

	@Override
	public String toString () {
		return "FiPlantFormLine"
				+" speciesName="+getSpeciesName ()
				+" height="+getHeight ();
	}

	public String sortingKey () {
		return "speciesName="+getSpeciesName ()
				+" height="+getHeight ()
				+" crownBaseHeight="+getCrownBaseHeight ()
				+" crownDiameter="+getCrownDiameter ()
				+" crownPerpendicularDiameter="+getCrownPerpendicularDiameter ()
				+" maxDiameterHeight="+getMaxDiameterHeight ();
	}

	public String toString2 () {
		return "FiPlantFormLine"
				+" shapeId="+getShapeId ()
				+" speciesName="+getSpeciesName ()
				+" voxelDx="+getVoxelDx ()
				+" voxelDy="+getVoxelDy ()
				+" voxelDz="+getVoxelDz ()
				+" height="+getHeight ()
				+" crownBaseHeight="+getCrownBaseHeight ()
				+" crownDiameter="+getCrownDiameter ()
				+" crownPerpendicularDiameter="+getCrownPerpendicularDiameter ()
				+" maxDiameterHeight="+getMaxDiameterHeight ()
				+" dominance="+getDominance ()
				+" meanBulkdensity0_2mm="+getMeanBulkdensity0_2mm ()
				+" lai="+getLai ()
				+" teamName="+getTeamName ()
				+" checked="+isChecked ()
				+ " liveMoisture=" + liveMoisture
				+ " deadMoisture=" + deadMoisture;
	}

}
