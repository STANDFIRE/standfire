package fireparadox.model.database;

import java.io.Serializable;
import java.util.LinkedHashMap;

/**
 * FiDBTeam : Description of a team
 *
 * @author Oana Vigy & Eric Rigaud - May 2007
 * Last Update by Isabelle LECOMTE - July 2009
 */
public class FmDBTeam implements Serializable, Comparable {

    private long teamId;						// id in database
    private String teamCode;
    private LinkedHashMap <Long, FmDBPerson>  persons; 	// used only for selection in the fuel editor
    private boolean deleted;					// if true, the team has been deleted in the database

	/**
	 * Creates a new instance of FiDBTeam
 	*/
    public FmDBTeam (long _idTeam, String _teamCode, boolean _deleted) {
        teamId = _idTeam;
        teamCode = _teamCode;
        persons = null;
        deleted = _deleted;
    }

    public long getTeamId () {return teamId;}				// NO MODIFICATION

    public String getTeamCode () { return teamCode;}
    public void setTeamCode (String _teamCode) {teamCode = _teamCode;}

	public LinkedHashMap<Long, FmDBPerson>  getPersons () { return persons;}
	public void setPersons (LinkedHashMap<Long, FmDBPerson>  _persons) {persons = _persons;}

	public boolean isDeleted () {return deleted;}
	public void setDeleted (boolean d) {deleted=d;}

	@Override
	public String toString (){
		return "Team id="+teamId+" code="+teamCode;
	}

	/*
	* sort the list by code
	*/
   	public int compareTo (Object other) {
      String code1 = ((FmDBTeam) other).getTeamCode();
      String code2 = this.getTeamCode();


      if (code1.compareTo(code2) > 0)  return -1;
      else if (code1.equals(code2)) return 0;
      else return 1;
   }
}
