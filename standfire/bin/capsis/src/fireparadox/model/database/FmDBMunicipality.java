package fireparadox.model.database;

import java.io.Serializable;

/**
 * FiDBMunicipality : Description of a municipality
 *
 * @author Isabelle Lecomte  - February 2008
 */
public class FmDBMunicipality implements Serializable {

    private long municipalityId;					// id in database
    private String municipalityName;				// name
    private FmDBCountry country;					// country object reference
    private boolean deleted;						// if true, the object has been deleted in the database

    /** Creates a new instance of FiDBMunicipality */
    public FmDBMunicipality (long _id, String _name, FmDBCountry _country, boolean _deleted) {
        municipalityId = _id;
        municipalityName = _name;
        country = _country;
        deleted = _deleted;
    }

    public long getMunicipalityId () {
        return  municipalityId;
    }

    public String getMunicipalityName () {
        return  municipalityName;
    }

    public void setMunicipalityName (String name) {
        municipalityName = name;
    }

    public FmDBCountry getCountry () {
        return  country;
    }

    public  void setCountry (FmDBCountry c) {
        country = c;
    }

	public boolean isDeleted () {return deleted;}
	public void setDeleted (boolean d) {deleted=d;}

	@Override
	public String toString (){
		return "Municipality id="+municipalityId+" name="+municipalityName;
	}

}
