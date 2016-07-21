package fireparadox.model.database;

import java.io.Serializable;
import java.util.Collection;

/**
 * FiDBSite : Description of a site
 *
 * @author Oana Vigy & Eric Rigaud - May 2007
 * updated by Isabelle Lecomte     - January 2010
 */
public class FmDBSite implements Serializable, Comparable {

    private long siteId;						// id in database
    private String siteCode;
    private FmDBMunicipality municipality;		// municipality reference - give also the country

    private long coordinatesId;					// id of property coordinate
    private long zId;							// id of property elevation
    private double x;							// latitude  in degrees
    private double y;							// longitude in degrees
    private double z;							// elevation in m

    private long descriptionId;					// id of property description in database
    private long topographyId;					// id of property topography in database
    private long slopeId;						// id of property slope in database
    private long slopeValueId;					// id of property slopeValue in database
    private long aspectId;						// id of property aspect in database
    private long aspectValueId;					// id of property aspectValue in database

    private String description;
    private String topography;
    private String slope;						// slope type
    private double slopeValue;					// slope value in degrees
    private String aspect;						// aspect type
    private double aspectValue;					// aspect value in degrees

    private long dominantTaxaId;					// id of property description in database
	private Collection dominantTaxa;

    private Collection <FmDBEvent> events;


    private boolean deleted;					// if true, the site has been deleted in the database

	/**
	 * Creates a new full instance of FiDBSite - used in SiteEditor
 	*/
    public FmDBSite (long _siteId, String _siteCode,
    		FmDBMunicipality _municipality,
            long _coordinatesId, double _x, double _y,
            long _zId, double _z,
            long _descriptionId, String _description,
            long _topographyId, String _topography,
            long _slopeId, String _slope,
            long _slopeValueId, double _slopeValue,
            long _aspectId, String _aspect,
            long _aspectValueId, double _aspectValue,
            long _dominantTaxaId, Collection _dominantTaxa,
            boolean _deleted) {

		siteId = _siteId;
		siteCode = _siteCode;
		municipality = _municipality;

		coordinatesId = _coordinatesId;
		zId = _zId;
		x = _x;
		y = _y;
		z = _z;

		descriptionId = _descriptionId;
		topographyId = _topographyId;
		slopeId = _slopeId;
		slopeValueId = _slopeValueId;
		aspectId = _aspectId;
		aspectValueId = _aspectValueId;

		description = _description;
		topography = _topography;
		slope = _slope;
		slopeValue = _slopeValue;
		aspect = _aspect;
		aspectValue = _aspectValue;

		dominantTaxaId = _dominantTaxaId;
		dominantTaxa   = _dominantTaxa;

		deleted = _deleted;
    }
	/**
	 * Creates a simple instance of FiDBSite - used for site selection in Fuel editor
 	*/
    public FmDBSite (long _id, String _code,  FmDBMunicipality _municipality, boolean _deleted) {

		siteId = _id;
		siteCode = _code;
		municipality = _municipality;
		deleted = _deleted;
    }

	public long getSiteId () {return siteId;}		//NO MODIFICATION !!!

    public String getSiteCode () {return siteCode;}
    public void setSiteCode (String _siteCode) {siteCode = _siteCode;}
    public FmDBMunicipality getMunicipality () {return municipality;}
    public void setMunicipality (FmDBMunicipality _municipality) {municipality = _municipality;}

    public long getCoordinatesId () {return coordinatesId;}
    public long getZId () {return zId;}
 	public double getX () {return x;}
    public double getY () {return y;}
    public double getZ () {return z;}

    public void setX (double _x) {x = _x;}
    public void setY (double _y) {y = _y;}
    public void setZ (double _z) {z = _z;}

    public long getDescriptionId () {return descriptionId;}
    public long getTopographyId () {return topographyId;}
    public long getSlopeId () {return slopeId;}
    public long getSlopeValueId () {return slopeValueId;}
    public long getAspectId () {return aspectId;}
    public long getAspectValueId () {return aspectValueId;}

	public String getDescription () {return description;}
	public String getTopography () {return topography;}
	public String getSlope () {return slope;}
	public double getSlopeValue() {return slopeValue;}
	public String getAspect () {return aspect;}
    public double getAspectValue () {return aspectValue;}
    public void setDescription (String _description) {description = _description;}
    public void setTopography (String _topography) {topography = _topography;}
    public void setSlope (String _slope) {slope = _slope;}
    public void setSlopeValue (double _slopeValue) {slopeValue = _slopeValue;}
    public void setAspect (String _aspect) {aspect = _aspect;}
    public void setAspectValue (double _aspectValue) {aspectValue = _aspectValue;}


    public long getDominantTaxaId () {return dominantTaxaId;}
    public Collection getDominantTaxa () {return dominantTaxa;}

	public boolean isDeleted () {return deleted;}
	public void setDeleted (boolean d) {deleted=d;}

	@Override
	public String toString (){
		return "Site id="+siteId+" code="+siteCode;
	}
	/*
	* sort the list by code
	*/
   	public int compareTo (Object other) {
      String code1 = ((FmDBSite) other).getSiteCode();
      String code2 = this.getSiteCode();


      if (code1.compareTo(code2) > 0)  return -1;
      else if (code1.equals(code2)) return 0;
      else return 1;
   }
}
