package fireparadox.model.database;

import java.io.Serializable;

/**
 * FiDBCountry : Description of a country
 *
 * @author Isabelle Lecomte  - February 2008
 */
public class FmDBCountry implements Serializable {

    private long countryId;						// id in database
    private String countryCode;					// code
    private boolean deleted;					// if true, the object has been deleted in the database

    /** Creates a new instance of FiDBCountry */
    public FmDBCountry (long _id, String _code, boolean _deleted) {
        countryId = _id;
        countryCode = _code;
        deleted = _deleted;
    }

    public long getCountryId () {
        return  countryId;
    }

    public String getCountryCode () {
        return  countryCode;
    }

	public boolean isDeleted () {return deleted;}
	public void setDeleted (boolean d) {deleted=d;}

	@Override
	public String toString (){
		return "country id="+countryId+" code="+countryCode;
	}


}
