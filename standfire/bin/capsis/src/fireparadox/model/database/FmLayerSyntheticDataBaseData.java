package fireparadox.model.database;


/**	FiLayerSyntheticDataBaseData: a layer synthetic description of some properties of the layers.
 * These properties can be got from the DB40 database or not (in the case of localLayer)
*	@author Isabelle LECOMTE - August 2009
*/
public class FmLayerSyntheticDataBaseData implements Comparable {

	// Values for dominance
	static public final int ISOLATED = 1;
	static public final int EMERGENT = 2;
	static public final int DOMINANT = 3;
	static public final int CODOMINANT = 4;
	static public final int INTERMEDIATE = 5;
	static public final int OVERTOPPED = 6;

    private long shapeId;					//UUID of the shape in the database
	private String speciesName;				//species name
//	private double voxelDx;					//size of voxel in m (X axe)
//	private double voxelDy;					//size of voxel in m (X axe)
//	private double voxelDz;					//size of voxel in m (X axe)
    private double height;						// height of the fuel in m (trunk+crown)
	private double bottomHeight;				// bottom height in m
	private int dominance;					// ISOLATED, EMERGENT...
    private double meanBulkdensity0_2mm;		//mean bulk density for 0-2mm particules (core)
    private double meanBulkdensity0_2mmEdge;	//mean bulk density for 0-2mm particules (edge)
    private double lai;							//leaf area index (core)
    private double laiEdge;						//leaf area index (edge)
	private String teamName;					//team name (owner)
    private boolean checked;					//plant is validated true/false


	/**	Constructor
	*/
	public FmLayerSyntheticDataBaseData (long _shapeId,
			String _speciesName,
			double _height,
			double _bottomHeight,
			int _dominance,
			double _meanBulkdensity0_2mm,
			double _meanBulkdensity0_2mmEdge,
			double _lai,
			double _laiEdge,
			String _teamName,
			boolean _checked) {

		shapeId= _shapeId;
		speciesName = _speciesName;
		height = _height;
		bottomHeight = _bottomHeight;
		dominance = _dominance;
		meanBulkdensity0_2mm = _meanBulkdensity0_2mm;
		meanBulkdensity0_2mmEdge = _meanBulkdensity0_2mmEdge;
		lai = _lai;
		laiEdge = _laiEdge;
		teamName = _teamName;
		checked = _checked;

		//Il 23/09/09 will be removed soon by fc ;-)
//		voxelDx = 0.0;
//		voxelDy = 0.0;
//		voxelDz = 0.0;
	}

	public long getShapeId () {return shapeId;}
	public String getSpeciesName () {return speciesName;}

//	public double getVoxelDx () {return voxelDx;}
//	public double getVoxelDy () {return voxelDy;}
//	public double getVoxelDz () {return voxelDz;}

	public double getHeight () {return height;}
	public double getBottomHeight () {return bottomHeight;}

	public int getDominance () {return dominance;}
	public double getMeanThinBulkdensity () {return meanBulkdensity0_2mm;}
	public double getThinBulkdensityEdge () {return meanBulkdensity0_2mmEdge;}
	public double getLai () {return lai;}
	public double getLaiEdge () {return laiEdge;}
	public String getTeamName () {return teamName;}
	public boolean isChecked () {return checked;}

	/**	Define an order on these objects.
	*/
	public int compareTo (Object o) {
		try {
			return sortingKey ().compareTo (((FmLayerSyntheticDataBaseData) o).sortingKey ());
		} catch (Exception e) {
			System.out.println ("FiLayerSyntheticDataBaseData exception in compareTo: "+e);
			return -1;	// in case of class cast exception upper
		}
	}

	public String sortingKey () {
		return "speciesName="+getSpeciesName ()
				+" bottomHeight="+bottomHeight
				+" height="+getHeight ()
				+" dominance="+dominance
				+" meanBulkdensity0_2mm="+meanBulkdensity0_2mm
				+" meanBulkdensity0_2mmEdge="+meanBulkdensity0_2mmEdge
				+" lai="+lai;
	}

	@Override
	public String toString (){
		return "FiLayerSyntheticDataBaseData shapeId=" + shapeId + " speciesName=" + speciesName;
	}

	public String toString2 () {
		return "FiLayerSyntheticDataBaseData"
				+" shapeId="+shapeId
				+" speciesName="+speciesName
//				+" voxelDx="+voxelDx
//				+" voxelDy="+voxelDy
//				+" voxelDz="+voxelDz
				+" height="+height
				+" bottomHeight="+bottomHeight
				+" dominance="+dominance
				+" meanBulkdensity0_2mm="+meanBulkdensity0_2mm
				+" meanBulkdensity0_2mmEdge="+meanBulkdensity0_2mmEdge
				+" lai="+lai
				+" laiEdge="+laiEdge
				+" teamName="+teamName
				+" checked="+checked;
	}

}
