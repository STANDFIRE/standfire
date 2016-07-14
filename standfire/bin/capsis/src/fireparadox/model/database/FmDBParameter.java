package fireparadox.model.database;

import java.io.Serializable;

/**
 * FiDBParameter : Fuel particle parameter
 *
 * @author Isabelle LECOMTE     - January 2008
 */
public class FmDBParameter implements Serializable {

	private long id;						//id of the parameter in the database
    private String name;					//parameter name (biomass - VF - MVS ..)
    private double value;					//value of this parameter for this particle

    /**
    * Creates a new instance of FiDBParameter
    */
    public FmDBParameter (long _parameterId, String _parameterName,  double _value) {
		id = _parameterId;
		name = _parameterName;
		value = _value;
    }

	public long getId() {return id;}
	public String getName() {return name;}
	public double getValue() {return value;}

	@Override
	public String toString () {
		return name+";"+value;
	}


}
