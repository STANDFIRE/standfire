package fireparadox.model.database;

import java.io.Serializable;

/**
 * FiDBPerson :  Description of a person
 *
 * @author Oana Vigy & Eric Rigaud - May 2007
 * Update by Isabelle LECOMTE      - January 2008
 */

public class FmDBPerson implements Serializable {

    private long personId;			// id in database
    private String personName;		// name
    private FmDBTeam team;			// team reference
    private boolean deleted;		// if true, the person has been deleted in the database

    /** Creates a new instance of FiDBPerson */
    public FmDBPerson (long _personId, String _personName, FmDBTeam _team, boolean _deleted) {
		personId = _personId;
		personName = _personName;
		team = _team;
		deleted = _deleted;
    }

    public long getPersonId () {return personId;}

    public String getPersonName () { return personName;}
    public void setPersonName (String _personName) {personName = _personName;}

    public FmDBTeam getTeam () { return team;}
    public void setTeam (FmDBTeam _team) {team = _team;}

	public boolean isDeleted () {return deleted;}
	public void setDeleted (boolean d) {deleted=d;}

	@Override
	public String toString (){
		return "Person id="+personId+" name="+personName;
	}

}
