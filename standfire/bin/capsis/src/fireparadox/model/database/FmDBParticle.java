package fireparadox.model.database;

import java.io.Serializable;
import java.util.HashMap;

/**
 * FiDBParticle : Fuel particle
 *
 * @author Isabelle LECOMTE     - January 2008
 */
public class FmDBParticle implements Serializable {

	private long id;				//id of the particle in the database
    private String name;			//particle name (TWIGS - LEAVES ..)
    private boolean alive;			//alive or dead particle
    private HashMap <Long, FmDBParameter> parameterMap;	//map of parameters attached ti this particle

    /**
    * Creates a new instance of FiDBParticle
    */
    public FmDBParticle (long _particleId,  String _particleName, boolean _alive) {
		id = _particleId;
		name = _particleName;
		alive = _alive;
		parameterMap = null;
    }

	public long getId() {return id;}
	public String getName() {return name;}
	public boolean isAlive() {return alive;}
	public HashMap getParameterMap() {return parameterMap;}

    /**
    * ADD a new parameter in the parameter map
    */
	public void addParameter (FmDBParameter param) {
		if (parameterMap==null) parameterMap = new HashMap();
		Long paramId = param.getId();
		parameterMap.put (paramId, param);
	}
    /**
    * REMOVE a parameter in the parameter map
    */
	public boolean removeParameter (Long paramId) {
		if (parameterMap==null) return false;
		if (!parameterMap.containsKey(paramId)) return false;
		parameterMap.remove (paramId);
		return true;
	}
    /**
    * CHECK if a parameter is in the parameter map
    */
	public boolean checkParameter (Long paramId) {
		if (parameterMap==null) return false;
		if (parameterMap.containsKey(paramId)) return true;
		return false;
	}
    /**
    * GET a parameter from the parameter map
    */
	public FmDBParameter getParameter (Long paramId) {
		if (parameterMap==null) return null;
		if (!parameterMap.containsKey(paramId)) return null;
		FmDBParameter param = parameterMap.get (paramId);
		return param;
	}

}
