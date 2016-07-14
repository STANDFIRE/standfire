package fireparadox.model.database;

import java.io.Serializable;


/**
 * FiDBEvent : Description of a site event
 *
 * @author Isabelle Lecomte     - January 2010
 */
public class FmDBEvent implements Serializable, Comparable {

    private long eventId;						// id in database
    private String name;
    private String dateStart;
    private String dateEnd;
    private boolean yearlyEvent;
    private boolean deleted;

	/**
	 * Creates a new full instance of fiDBEvent
 	*/
    public FmDBEvent (long _eventId, String _name, String _dateStart, String _dateEnd, boolean _yearlyEvent) {

		eventId = _eventId;
		name = _name;

		dateStart = _dateStart;
		dateEnd = _dateEnd;
		yearlyEvent = _yearlyEvent;

    }


	public long getEventId () {return eventId;}

    public String getName() {return name;}

    public String getDateStart() {return dateStart;}

    public String getDateEnd() {return dateEnd;}

    public boolean isYearlyEvent() {return yearlyEvent;}

    public boolean isDeleted() {return deleted;}
 	public void setDeleted (boolean d) {deleted = d;}


	@Override
	public String toString (){
		return "Event id="+eventId+" name="+name;
	}
	/*
	* sort the list by date
	*/
   	public int compareTo (Object other) {
      String date1 = ((FmDBEvent) other).getDateStart();
      String date2 = this.getDateStart();


      if (date1.compareTo(date2) > 0)  return -1;
      else if (date1.equals(date2)) return 0;
      else return 1;
   }
}
