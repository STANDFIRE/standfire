package fireparadox.model.database;

import java.io.Serializable;
import java.util.Collection;



/**
 * FiLayerVoxelData : A layer voxel and particle parameters from the DB40 database.
 *                    light description for FireExport
 *
 * @author Isabelle LECOMTE - September 2009
 */
public class FmLayerVoxelData implements Serializable {

    private long shapeId;						//UUID of the plant in the database
    private double voxelDx;						//size of voxel in m (X axe)
	private double voxelDy;						//size of voxel in m (Y axe)
	private double voxelDz;						//size of voxel in m (Z axe)
	private FmLTFamilyProperty MVR;				//to store plant particles values for MVR
	private FmLTFamilyProperty SVR;				//to store plant particles values for SVR
	private Collection<FmLTVoxel> centerVoxels;	//center voxel collection
	private Collection<FmLTVoxel> edgeVoxels;	//edge voxel collection

	/**
	 * Creates a simple instance of FiLayerVoxelData - data from database request
	 * request N� 45
	 */
	public FmLayerVoxelData (long _shapeId,
					double _dx,	double _dy,	double _dz) {

		shapeId= _shapeId;
		voxelDx = _dx;
		voxelDy = _dy;
		voxelDz = _dz;
	}

	public long getShapeId () { return shapeId;}

	/**	Return voxel sizes
	*/
	public double getVoxelDx () {return voxelDx;}
	public double getVoxelDy () {return voxelDy;}
	public double getVoxelDz () {return voxelDz;}

	/**	Return voxel collections
	*/
	public Collection<FmLTVoxel> getCenterVoxels () { return centerVoxels;}
	public Collection<FmLTVoxel> getEdgeVoxels () { return edgeVoxels;}

	/**	Return MVR
	*/
	public FmLTFamilyProperty getMVR () {
		return MVR;
	}
	/**	Return SVR
	*/
	public FmLTFamilyProperty getSVR () {
		return SVR;
	}
	/**	Return MVR value for the given family of particles.
	*/
	public double getMVR (String familyName) {
		if (MVR == null) {return 0d;}	// No MVR, return 0.
		return MVR.get (familyName);
	}

	/**	Return  SVR value  for the given family of particles.
	*/
	public double getSVR (String familyName) {
		if (SVR == null) {return 0d;}		// No SVR, return 0.
		return SVR.get (familyName);
	}

	public void setCenterVoxels (Collection<FmLTVoxel> _voxels) {centerVoxels = _voxels;}
	public void setEdgeVoxels (Collection<FmLTVoxel> _voxels) {edgeVoxels = _voxels;}
	public void setMVR (FmLTFamilyProperty _MVR) {MVR = _MVR;}
	public void setSVR (FmLTFamilyProperty _SVR) {SVR = _SVR;}

	@Override
	public String toString (){
		return "FiLayerVoxelData shapeId=" + shapeId
				+" voxelDx="+voxelDx
				+" voxelDy="+voxelDy
				+" voxelDz="+voxelDz;
	}

}
