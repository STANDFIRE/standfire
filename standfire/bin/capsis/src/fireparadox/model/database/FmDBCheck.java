package fireparadox.model.database;

import java.io.Serializable;

/**
 * FiDBCheck : lits of chekings
 *
 * @author Isabelle LECOMTE     - February 2010
 */
public class FmDBCheck implements Serializable {

    private String particleName;					//particle name (Leaves - Twings ...)
    private String parameterName;					//parameter name (biomass - VF - MVS ..)
    private String typeName;						//type name (tree - shrubs)

    private double warningMin;
    private double warningMax;
    private double errorMin;
    private double errorMax;


    /**
    * Creates a new instance of FiDBCheck
    */
    public FmDBCheck (String _particleName, String _parameterName, String _typeName,
     					double _errorMin, double _errorMax,
    				  	double _warningMin, double _warningMax) {

		particleName = _particleName;
		parameterName = _parameterName;
		typeName = _typeName;
		warningMin = _warningMin;
		warningMax = _warningMax;
		errorMin = _errorMin;
		errorMax = _errorMax;
    }

	public String getParticleName() {return particleName;}
	public String getParameterName() {return parameterName;}
	public String getTypeName() {return typeName;}

	public double getWarningMin() {return warningMin;}
	public double getWarningMax() {return warningMax;}
	public double getErrorMin() {return errorMin;}
	public double getErrorMax() {return errorMax;}


}
