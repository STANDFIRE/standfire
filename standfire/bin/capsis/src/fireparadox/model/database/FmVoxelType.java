package fireparadox.model.database;

import java.io.Serializable;

/**
 * FiVoxelType : The type of a voxel in the DB40 database.
 * (In the database, FiVoxelType is named layerName).
 *
 * @author Isabelle LECOMTE - January 2008
 */
public class FmVoxelType implements Serializable {

    private long dbId;			//id in the database
    private String name;		//voxelType name (top center bottom free)

    /** Creates a new instance of FiVoxelType
    */
    public FmVoxelType (long _dbId,  String _name) {
		dbId = _dbId;
		name = _name;
    }

	public long getDBId() {return dbId;}
	public String getName() {return name;}
	public int getTypeIndex () {
		if (name.compareTo("Top_INRA") == 0) return 1;
		if (name.compareTo("Center_INRA") == 0) return 2;
		if (name.compareTo("Bottom_INRA") == 0) return 3;
		return -1;
	}

	@Override
	public String toString (){
		return "voxelType id="+dbId+" name="+name;
	}
}
