package fireparadox.model.database;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;

import jeeb.lib.util.Check;
import jeeb.lib.util.Log;

/**
 * FiDBShape : A shape description from the DB40 database.
 *
 * @author Isabelle LECOMTE     - September 2009
 */
public class FmDBShape implements Serializable {

    private long shapeId;						// id in the database
    private FmDBPlant plant;						// reference plant id
	private int fuelType;						// 1= Isolated 2= Layer 3=sample unique 4=Sample core 5=Sample edge
	private String shapeKind;					// XZ, XZ_YZ, XYZ ...
	private String origin;						// virtual, calculated, measured ...

	private boolean cubeMethod;					// if true, the shape is designed with cube method
	private boolean deleted;					// if true, the shape has been desactivated
	private boolean validated;					// if true, the shape is validated


	//shape cubes size
	private double voxelXSize;					//size of voxel in m (X axe)
	private double voxelYSize;					//size of voxel in m (Y axe)
	private double voxelZSize;					//size of voxel in m (Z axe)

	//shape cubes dimension
	private double xMax;
	private double yMax;
	private double zMax;
	private HashMap voxels;						// crown description for sample unique or core (FiVoxels)

	//shape layer edge dimension
	private double xEdgeMax;
	private double yEdgeMax;
	private double zEdgeMax;
	private HashMap edgeVoxels;					// crown description for edge (FiVoxels)

	//min and max size for a layer (known from the database)
	private double layerWidthMin;				//layer width min in m
	private double layerWidthMax;				//layer width max in m


	/**
	 * Creates a complete instance of FiDBShape -
	 * data from database request N� 38
	 */
	public FmDBShape (long _shapeId, FmDBPlant _plant,
					int _type,
					String _shapeKind,
					double _xSize, double _ySize, double _zSize,
					double _xMax, double _yMax, double _zMax,
					double _xEdgeMax, double _yEdgeMax, double _zEdgeMax,
					double _widthMin, double _widthMax,
					String _origin,
					boolean _cubeMethod,
					boolean _deleted,
					boolean _validated) {

		try {

			shapeId = _shapeId;
			plant = _plant;
			fuelType = _type;
			shapeKind= _shapeKind;

			voxelXSize = _xSize;
			voxelYSize = _ySize;
			voxelZSize = _zSize;

			xMax = _xMax;
			yMax = _yMax;
			zMax = _zMax;

			xEdgeMax = _xEdgeMax;
			yEdgeMax = _yEdgeMax;
			zEdgeMax = _zEdgeMax;

			origin = _origin;

			layerWidthMin = _widthMin;
			layerWidthMax = _widthMax;

			cubeMethod = _cubeMethod;
			deleted = _deleted;
			validated = _validated;

		} catch (Exception e) {
			Log.println (Log.ERROR, "FiDBShape()", "Error during SHAPE  constructor: ", e);
		}

	}

	/**
	 * CLONING a complete instance of FiDBShape -
	 */
	public FmDBShape (FmDBShape shape) {

		try {

			shapeId = shape.getShapeId ();
			plant = shape.getPlant ();
			fuelType = shape.getFuelType ();
			shapeKind= shape.getShapeKind ();

			voxelXSize = shape.getVoxelXSize ();
			voxelYSize = shape.getVoxelYSize ();
			voxelZSize = shape.getVoxelZSize ();

			xMax = shape.getXMax ();
			yMax = shape.getYMax ();
			zMax = shape.getZMax ();

			xEdgeMax = shape.getXEdgeMax ();
			yEdgeMax = shape.getYEdgeMax ();
			zEdgeMax = shape.getZEdgeMax ();

			origin = shape.getOrigin () ;

			layerWidthMin = shape.getLayerWidthMin ();
			layerWidthMax = shape.getLayerWidthMax ();

			cubeMethod = shape.isCubeMethod();
			deleted = shape.isDeleted ();
			validated = false;

		} catch (Exception e) {
			Log.println (Log.ERROR, "FiDBShape()", "Error during SHAPE  constructor: ", e);
		}

	}

	public long getShapeId() { return shapeId;}
	public void setShapeId (long id) {shapeId = id;}

	public FmDBPlant getPlant () { return plant;}
	public int getFuelType () {return fuelType;}

	public String getShapeKind () { return shapeKind;}
	public void setShapeKind (String kind) {shapeKind = kind;}
	public String getOrigin () { return origin;}
	public void setOrigin (String o) {origin = o;}
	public boolean isCubeMethod () {return cubeMethod;}
	public void setCubeMethod (boolean method) {cubeMethod = method;}

	public boolean isDeleted () {return deleted;}
	public void setDeleted (boolean d) {deleted=d;}
	public boolean isValidated () {return validated;}
	public void setValidated (boolean d) {validated=d;}

	//for layers
	public double getLayerWidthMin () {return layerWidthMin;}
	public double getLayerWidthMax () {return layerWidthMax;}
	public void setLayerWidthMin (double v) {layerWidthMin = v;}
	public void setLayerWidthMax (double v) {layerWidthMax = v;}

	//voxels size
	public double getVoxelXSize () { return voxelXSize;}
	public double getVoxelYSize () { return voxelYSize;}
	public double getVoxelZSize () { return voxelZSize;}
	public void setVoxelXSize (double x) { voxelXSize = x;}
	public void setVoxelYSize (double y) { voxelYSize = y;}
	public void setVoxelZSize (double z) { voxelZSize = z;}

	//grid dimension
	public double getXMax () { return xMax;}
	public double getYMax () { return yMax;}
	public double getZMax () { return zMax;}
	public void setXMax (double x) { xMax = x;}
	public void setYMax (double y) { yMax = y;}
	public void setZMax (double z) { zMax = z;}

	public HashMap  getVoxels () {return voxels;}
	public void setVoxels (HashMap _voxels) {voxels = _voxels;}

	public FmDBVoxel getVoxel (long voxelId) {
		FmDBVoxel c = null;
		if (voxels.containsKey(voxelId)) c = (FmDBVoxel) voxels.get(voxelId);
		else if (edgeVoxels.containsKey(voxelId)) c = (FmDBVoxel) edgeVoxels.get(voxelId);
		return c;
	}


	//edge grid dimension
	public double getXEdgeMax () { return xEdgeMax;}
	public double getYEdgeMax () { return yEdgeMax;}
	public double getZEdgeMax () { return zEdgeMax;}
	public void setXEdgeMax (double x) { xEdgeMax = x;}
	public void setYEdgeMax (double x) { yEdgeMax = x;}
	public void setZEdgeMax (double x) { zEdgeMax = x;}

	public HashMap  getEdgeVoxels () {return edgeVoxels;}
	public void setEdgeVoxels (HashMap _voxels) {edgeVoxels = _voxels;}


	//Calculate crown dimension in number of cubes
	public int getNbVoxelI () {
		if (voxels==null) return 0;
		int max = 0;
		for (Iterator i = voxels.keySet().iterator(); i.hasNext ();) {
			Object cle = i.next();
			FmDBVoxel c = (FmDBVoxel) voxels.get(cle);
			max = Math.max (max,(c.getI()+1));
		}
		return max;
	}

	public int getNbVoxelJ () {
		if (voxels==null) return 0;
		int max = 0;
		for (Iterator i = voxels.keySet().iterator(); i.hasNext ();) {
			Object cle = i.next();
			FmDBVoxel c = (FmDBVoxel) voxels.get(cle);
			max = Math.max (max,(c.getJ()+1));
		}
		return max;
	}

	public int getNbVoxelK () {
		if (voxels==null) return 0;
		int max = 0;
		for (Iterator i = voxels.keySet().iterator(); i.hasNext ();) {
			Object cle = i.next();
			FmDBVoxel c = (FmDBVoxel) voxels.get(cle);
			max = Math.max (max,(c.getK()+1));
		}
		return max;
	}
	public int getNbEdgeI () {
		if (edgeVoxels==null) return 0;
		int max = 0;
		for (Iterator i = edgeVoxels.keySet().iterator(); i.hasNext ();) {
			Object cle = i.next();
			FmDBVoxel c = (FmDBVoxel) edgeVoxels.get(cle);
			max = Math.max (max,(c.getI()+1));
		}
		return max;
	}

	public int getNbEdgeJ () {
		if (edgeVoxels==null) return 0;
		int max = 0;
		for (Iterator i = edgeVoxels.keySet().iterator(); i.hasNext ();) {
			Object cle = i.next();
			FmDBVoxel c = (FmDBVoxel) edgeVoxels.get(cle);
			max = Math.max (max,(c.getJ()+1));
		}
		return max;
	}

	public int getNbEdgeK () {
		if (edgeVoxels==null) return 0;
		int max = 0;
		for (Iterator i = edgeVoxels.keySet().iterator(); i.hasNext ();) {
			Object cle = i.next();
			FmDBVoxel c = (FmDBVoxel) edgeVoxels.get(cle);
			max = Math.max (max,(c.getK()+1));
		}
		return max;
	}

	//check the origin
	public boolean checkOrigin (String value) {
		if (origin.compareTo(value) == 0) return true;
		return false;
	}

	//check the validation
	public boolean checkValidated (boolean test) {
		if (validated == test) return true;
		return false;
	}

	//check the validation
	public boolean checkDeleted (boolean test) {
		if (deleted == test) return true;
		return false;
	}


	//check the height between min and max
	public boolean checkHeight (String heightParam) {
		double heightMin = 0.0;
		double heightMax = 999.0;
		int index = heightParam.indexOf (":");
		if (index >= 0) {
			heightMin = Check.doubleValue(heightParam.substring (0, index));
			heightMax = Check.doubleValue(heightParam.substring (index+1));
		}
		if ((zMax >= heightMin) && (zMax <= heightMax)) return true;
		return false;
	}

	@Override
	public String toString (){
		return "Shape id="+shapeId+" origin="+origin;
	}



}
