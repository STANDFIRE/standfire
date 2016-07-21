package fireparadox.model.database;


/**	FiPlantSyntheticData: a plant synhetic description from the DB40 database.
*	@author Isabelle LECOMTE - August 2009
*/
public class FmPlantSyntheticData  {

	// Values for dominance
	static public final int ISOLATED = 1;
	static public final int EMERGENT = 2;
	static public final int DOMINANT = 3;
	static public final int CODOMINANT = 4;
	static public final int INTERMEDIATE = 5;
	static public final int OVERTOPPED = 6;

    private long shapeId;					//UUID of the plant in the database
	private String speciesName;				//species name
	private double voxelDx;					//size of voxel in m (X axe)
	private double voxelDy;					//size of voxel in m (X axe)
	private double voxelDz;					//size of voxel in m (X axe)
    private double height;						// height of the fuel in m (trunk+crown)
	private double crownBaseHeight;				// crown base height in m
	private double crownDiameter;				// crown diameter in m
	private double crownPerpendicularDiameter;	// crown perpendicular diameter in m
	private double maxDiameterHeight;			// max diameter height in m
	private int dominance;				// ISOLATED, EMERGENT...
	private double meanBulkdensity0_2mm;
    private double lai;
	private String teamName;					//team name (owner)
    private boolean checked;					//plant is validated true/false


	/**	Constructor.
	*/
	public FmPlantSyntheticData (long _shapeId,
			String _species,
			double _height,
			double _baseHeight,
			double _diameter,
			double _perpenDiameter,
			double _maxDiameterHeight,
			int _dominance,
			double _meanBulkdensity0_2mm,
			double _lai,
			String _team,
			boolean _checked) {

		shapeId = _shapeId;
		speciesName = _species;
		height = _height;
		crownBaseHeight = _baseHeight;
		crownDiameter = _diameter;
		crownPerpendicularDiameter = _perpenDiameter;
		maxDiameterHeight = _maxDiameterHeight;
		dominance = _dominance;
		meanBulkdensity0_2mm = _meanBulkdensity0_2mm;
		lai = _lai;
		teamName = _team;
		checked = _checked;

		//Il 23/09/09 will be removed soon by fc ;-)
		voxelDx = 0.0;
		voxelDy = 0.0;
		voxelDz = 0.0;
	}

	public long getShapeId () {return shapeId;}
	public String getSpeciesName () {return speciesName;}

	public double getVoxelDx () {return voxelDx;}
	public double getVoxelDy () {return voxelDy;}
	public double getVoxelDz () {return voxelDz;}

	public double getHeight () {return height;}
	public double getCrownBaseHeight () {return crownBaseHeight;}
	public double getCrownDiameter () {return crownDiameter;}
	public double getCrownPerpendicularDiameter () {return crownPerpendicularDiameter;}
	public double getMaxDiameterHeight () {return maxDiameterHeight;}

	public int getDominance () {return dominance;}
	public double getMeanBulkdensity0_2mm () {return meanBulkdensity0_2mm;}
	public double getLai () {return lai;}

	public String getTeamName () {return teamName;}
	public boolean isChecked () {return checked;}

	@Override
	public String toString (){
		return "PlantSyntheticData shapeId="+shapeId+" speciesName="+speciesName;
	}

	public String toString2 () {
		return "FiPlantSyntheticData"
				+" shapeId="+shapeId
				+" speciesName="+speciesName
				+" voxelDx="+voxelDx
				+" voxelDy="+voxelDy
				+" voxelDz="+voxelDz
				+" height="+height
				+" crownBaseHeight="+crownBaseHeight
				+" crownDiameter="+crownDiameter
				+" crownPerpendicularDiameter="+crownPerpendicularDiameter
				+" maxDiameterHeight="+maxDiameterHeight
				+" dominance="+dominance
				+" meanBulkdensity0_2mm="+meanBulkdensity0_2mm
				+" lai="+lai
				+" teamName="+teamName
				+" checked="+checked;
	}

}
