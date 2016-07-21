package fireparadox.model.database;

//~ import java.awt.*;
import java.io.Serializable;
import java.util.HashMap;


/**	FiDBVoxel : A voxel in the description of the fuel crown.
 * 	(In the database, FiDBVoxel is named Cell and	voxelType is named layerName.)
 *
 *	@author I.Lecomte - february 2008
 */
public class FmDBVoxel  implements Serializable {

	private long dbId;					//id of the voxel in the database
	private int i;						//coordinates
	private int j;
	private int k;
	private FmVoxelType voxelType;		//type of voxel (top-center-bottom)
	private boolean edge;				//true if edge

	private HashMap<Long, FmDBParticle> particleMap;		//to store particles parameters values from DB

    /**
    * Creates a new instance of FiDBVoxel
    */
	public FmDBVoxel (long _id, int _i, int _j, int _k, FmVoxelType _voxelType, boolean _edge) {
		dbId = _id;
		i = _i;
		j = _j;
		k = _k;
		voxelType = _voxelType;
		edge = _edge;
		particleMap = null;
	}

	public long getDBId () {return dbId;}
	public void setDBId (long id) {dbId = id;}

	public int getI () {return i;}
	public int getJ () {return j;}
	public int getK () {return k;}
	public boolean isEdge () {return edge;}

	public FmVoxelType getVoxelType () {return voxelType;}
	public void setVoxelType (FmVoxelType _voxelType) {voxelType = _voxelType;}

	//Get  particles values
	public HashMap  getParticleMap () {return particleMap;}
	public void setParticleMap (HashMap _particles) {particleMap = _particles;}

    /**
    * ADD a new particle in the particle map
    */
	public void addParticle (FmDBParticle particle) {
		if (particleMap==null) particleMap = new HashMap();
		Long particleId = particle.getId();
		particleMap.put (particleId, particle);
	}
    /**
    * REMOVE a particle in the particle map
    */
	public boolean removeParticle  (Long particleId) {
		if (particleMap==null) return false;
		if (!particleMap.containsKey(particleId)) return false;
		particleMap.remove (particleId);
		return true;
	}
    /**
    * CHECK if a particle is in the particle map
    */
	public boolean checkParticle (Long particleId) {
		if (particleMap==null) return false;
		if (particleMap.containsKey(particleId)) return true;
		return false;
	}
    /**
    * GET a particle from the particle map
    */
	public FmDBParticle getParticle (Long particleId) {
		if (particleMap==null) return null;
		if (!particleMap.containsKey(particleId)) return null;
		FmDBParticle particle = particleMap.get (particleId);
		return particle;
	}

	@Override
	public String toString (){
		return "Voxel dbId="+dbId+" i="+i+" j="+j+" k="+k;
	}

}
