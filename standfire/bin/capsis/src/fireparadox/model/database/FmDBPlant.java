package fireparadox.model.database;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;

import jeeb.lib.util.Check;
import jeeb.lib.util.Log;

/**
 * FiDBPlant : A plant description from the DB40 database.
 *
 * @author Isabelle LECOMTE     - October 2009
 *
 */
public class FmDBPlant implements Serializable {

	public static final String OPENESS_OPEN = "Open";
	public static final String OPENESS_CLOSED = "Closed";

    private long plantId;						// id in the database

    //team info
    private long teamId;						// id of the property team in the database
	private FmDBTeam team;
	private long samplingDateId;				// id of the property site in the database
	private String samplingDate;
	private long[] personIdList;

	//site info
	private long siteId;						// id of the property site in the database
    private FmDBSite site;

	//fuel info
	private String specie;
	private String origin;						// virtual, calculated, measured ...

	private long referenceId;					// id of reference defines by the user
    private String reference;					// reference defines by the user
    private long heightId;						// id of the property height in the database
    private double height;						// height of the fuel in m (trunk+crown)
    private long crownBaseHeightId;				// id of the property crown base height in the database
	private double crownBaseHeight;				// crown base height in m
	private long crownDiameterId;				// id of the property crown diameter in the database
	private double crownDiameter;				// crown diameter in m
	private long crownPerpendicularDiameterId;	// id of the property crown perpendicular diameter in the database
	private double crownPerpendicularDiameter;	// crown perpendicular diameter in m
	private long maxDiameterHeightId;			// id of the property max diameter height in the database
	private double maxDiameterHeight;			// max diameter height in m

    private long coordinatesId;					// id of property coordinate
    private long zId;							// id of property elevation
    private double x;							// latitude  in degrees
    private double y;							// longitude in degrees
    private double z;							// elevation in m


	private long plantStatusId;
    private String plantStatus;					// dominant, dominated ...
    private long dominantTaxaId;				// id of property description in database
	private Collection dominantTaxa;
   	private long coverPcId;						// id of the property site in the database
    private double coverPc;						// in %
   	private long openessId;						// id of the property site in the database
    private String openess;						// OPENESS_OPEN or OPENESS_CLOSED

	private long biomassId;
	private double totalMeasuredBiomass;

	private long commentId;						// id of the property site in the database
    private String comment;

	private boolean deleted;					// if true, the plant is desactivated
	private boolean validated;					// if true, the plant is validated


	private HashMap<Long, FmDBParticle> particleMap;		//to store plant particles parameters values from DB

														// meanBulkDensit and

	private HashMap<Long, FmDBShape> shapeMap;		//to store all shape for this plant 												// LAI



	/**
	 * Creates a simple instance of FiDBPlant -
	 * data from database request N� 39
	 */
	public FmDBPlant (long _plantId, String _specie,
					String _reference,
					double _height,
					double _baseHeight,
					double _diameter,
					double _perDiameter,
					FmDBTeam _team,
					FmDBSite _site,
					String _origin,
					boolean _deleted,
					boolean _validated) {
		try {

			plantId= _plantId;
			specie = _specie;
			reference = _reference;
			height = _height;
			crownBaseHeight = _baseHeight;
			crownDiameter = _diameter;
			crownPerpendicularDiameter = _perDiameter;
			origin = _origin;
			team = _team;
			personIdList = new long[3];
			site = _site;
			plantStatus = new String("");
			deleted = _deleted;
			validated = _validated;


		} catch (Exception e) {
			Log.println (Log.ERROR, "FiDBPlant()", "Error during PLANT  constructor: ", e);
		}

	}

	/**
	 * Creates a complete instance of FiDBPlant -
	 * data from database request N�31
	 */
	public FmDBPlant (long _plantId, String _specie,
					long _referenceId, String _reference,
					String _plantOrigin,
					long _teamId, FmDBTeam _team, long [] _persons,
					long _siteId, FmDBSite _site,
					long _samplingDateId, String _samplingDate,
					long _coordinatesId, double _x, double _y,
            		long _zId, double _z,
					long _commentId, String _comment,
					long _heightId, double _height,
					long _crownBaseHeightId, double _crownBaseHeight,
					long _crownDiameterId, double _crownDiameter,
					long _crownPerpendicularDiameterId, double _crownPerpendicularDiameter,
					long _maxDiameterHeightId, double _maxDiameterHeight,
					long _biomassId, double _biomass,
					long _plantStatusId, String _plantStatus,
					long _openessId, String _openess,
					long _coverPcId, double _coverPc,
					long _dominantTaxaId, Collection _dominantTaxa,
					boolean _deleted, boolean _validated) {

		//en attendant mieux ....
		try {

			plantId = _plantId;
			specie = _specie;

			referenceId = _referenceId;
			reference = _reference;
			origin = _plantOrigin;
			teamId = _teamId;
			team = _team;

			personIdList = _persons;
			samplingDateId = _samplingDateId;
			samplingDate = _samplingDate;

			coordinatesId = _coordinatesId;
			zId = _zId;
			x = _x;
			y = _y;
			z = _z;

			siteId = _siteId;
			site = _site;

			heightId = _heightId;
			crownBaseHeightId = _crownBaseHeightId;
			crownDiameterId = _crownDiameterId;
			crownPerpendicularDiameterId = _crownPerpendicularDiameterId;
			maxDiameterHeightId = _maxDiameterHeightId;
			biomassId = _biomassId;

			height = _height;
			crownBaseHeight = _crownBaseHeight;
			crownDiameter = _crownDiameter;
			crownPerpendicularDiameter = _crownPerpendicularDiameter;
			maxDiameterHeight = _maxDiameterHeight;
			totalMeasuredBiomass = _biomass;

			plantStatus = _plantStatus;
			plantStatusId = _plantStatusId;
			openess = _openess;
			openessId = _openessId;
			coverPc = _coverPc;
			coverPcId = _coverPcId;
			dominantTaxaId = _dominantTaxaId;
			dominantTaxa   = _dominantTaxa;

			comment = _comment;
			commentId = _commentId;

			deleted = _deleted;
			validated = _validated;


		} catch (Exception e) {
			Log.println (Log.ERROR, "FiDBPlant()", "Error during PLANT  constructor: ", e);
		}

	}

	/**
	 * Creates a complete instance of FiDBPlant - for localtrees
	 *
	 */

	public FmDBPlant (String _speciesName, int _age, double _height, double _crownBaseHeight,
			double _crownDiameter, double dbh) {

		try {

			plantId = -1; // localTrees
			specie = _speciesName;
			this.height = _height;
			this.crownBaseHeight = _crownBaseHeight;
			this.crownDiameter = _crownDiameter;
			this.crownPerpendicularDiameter = _crownDiameter;
			maxDiameterHeight = 0.3 * _height; // guess from FP
			this.origin = "localTree";

		} catch (Exception e) {
			Log.println(Log.ERROR, "FiDBFuel()",
					"Error during FUEL constructor of localTree ", e);
		}

	}


	public long getPlantId () { return plantId;}
	public void setPlantId (long id) { plantId = id;}

	public long getReferenceId () { return referenceId;}
	public String getReference () { return reference;}

	public String getOrigin () { return origin;}

	public String getSpeciesName () { return specie;}	// better name for this method
	public String getSpecie() { return specie;}
	public long getTeamId () { return teamId;}
	public FmDBTeam getTeam () { return team;}
	public long getSiteId () { return siteId;}
	public FmDBSite getSite () { return site;}

	public long getSamplingDateId () { return samplingDateId;}
	public String getSamplingDate () { return samplingDate;}

	public long[] getPersonIdList () {return personIdList;}

    public long getCoordinatesId () {return coordinatesId;}
    public long getZId () {return zId;}
	public double getX () { return x;}
	public double getY () { return y;}
	public double getZ () { return z;}

	public long getCommentId () { return commentId;}
	public String getComment () { return comment;}
	public void setComment (String s) {comment = s;}

	public boolean isDeleted () {return deleted;}
	public void setDeleted (boolean d) {deleted=d;}
	public boolean isValidated () {return validated;}
	public void setValidated (boolean d) {validated=d;}

	public long getHeightId () {return heightId;}
	public long getCrownBaseHeightId () {return crownBaseHeightId;}
	public long getCrownDiameterId () {return crownDiameterId;}
	public long getCrownPerpendicularDiameterId () {return crownPerpendicularDiameterId;}
	public long getMaxDiameterHeightId () {return maxDiameterHeightId;}

	public double getHeight () {return height;}
	public double getCrownBaseHeight () {return crownBaseHeight;}
	public double getCrownDiameter () {return crownDiameter;}
	public double getCrownPerpendicularDiameter () {return crownPerpendicularDiameter;}
	public double getMaxDiameterHeight () {return maxDiameterHeight;}
	public double getReferenceVolume() { // FP 26/06/2009
		double surface=getReferenceSurface();
		/*if (this.getCrownDiameter() == 0) {//layer
			surface = FiInitialParameters.X_CUBE_SIZE * FiInitialParameters.Y_CUBE_SIZE;
		} else {
			surface = 3.14 * this.getCrownDiameter() * this.getCrownPerpendicularDiameter();
		}*/
		double crownheight=this.height-this.crownBaseHeight;
		return surface * crownheight;
	}
	public double getReferenceSurface() { // FP 26/06/2009
		//if (this.getCrownDiameter() == 0) {//layer
		//	return  this.voxelXSize * this.voxelYSize;
		//} else {
			return 3.14 * this.getCrownDiameter()
					* this.getCrownPerpendicularDiameter() / 4;
		//}
	}
	public double getCylindricVolume() { //FP 15/05/2009
		double surface= 3.14*this.getCrownDiameter()*this.getCrownPerpendicularDiameter();
		double crownheight=this.height-this.crownBaseHeight;
		return surface*crownheight;
	}


	public long getPlantStatusId () { return plantStatusId;}
	public String getPlantStatus () { return plantStatus;}
	public long getOpenessId () {return openessId;}
    public String getOpeness () {return openess;}
	public long getCoverPcId () { return coverPcId;}
	public Double getCoverPc () { return coverPc;}

	public long getDominantTaxaId () {return dominantTaxaId;}
    public Collection getDominantTaxa () {return dominantTaxa;}

	public long getBiomassId() { return biomassId;}
	public Double getTotalMeasuredBiomass() { return totalMeasuredBiomass;}





	//calculte the ellipsoide projection
	public double getSurface () {
		if (crownPerpendicularDiameter > 0) return (Math.PI * crownDiameter * crownPerpendicularDiameter)/4 ;
		else return (Math.PI * crownDiameter * crownDiameter)/4;
	}

    /**
    * Get  shapes map
    */
	public HashMap  getShapeMap () {return shapeMap;}

    /**
    * GET a shape from the shape map
    */
	public FmDBShape getShape (Long shapeId) {
		if (shapeMap==null) return null;
		if (!shapeMap.containsKey(shapeId)) return null;
		FmDBShape shape = shapeMap.get (shapeId);
		return shape;
	}
    /**
    * Set  shapes map
    */
	public void setShapeMap (HashMap _shapes) {shapeMap = _shapes;}
    /**
    * ADD a new shape in the shape map
    */
	public void addShape (FmDBShape shape) {
		if (shapeMap==null) shapeMap = new HashMap();
		Long shapeId = shape.getShapeId();
		shapeMap.put (shapeId, shape);
	}
    /**
    * Get  particles map
    */
	public HashMap  getParticleMap () {return particleMap;}
    /**
    * GET a particle from the particle map
    */
	public FmDBParticle getParticle (Long particleId) {
		if (particleMap==null) return null;
		if (!particleMap.containsKey(particleId)) return null;
		FmDBParticle particle = particleMap.get (particleId);
		return particle;
	}
    /**
    * Set  particle map
    */
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


	//check the site name
	public boolean checkSiteCode (String siteCode) {
		String fuelSiteCode = "";
		if (site != null) fuelSiteCode = site.getSiteCode();
		if (fuelSiteCode.compareTo(siteCode) == 0) return true;
		return false;
	}

	//check the team code
	public boolean checkTeamCode (String teamCode) {
		String fuelTeamCode = "";
		if (team != null) fuelTeamCode = team.getTeamCode();
		if (fuelTeamCode.compareTo(teamCode) == 0) return true;
		return false;
	}

	//check the species name
	public boolean checkSpeciesName (String speciesName) {
		if (speciesName.compareTo(specie) == 0) return true;
		return false;
	}

	//check the origin
	public boolean checkOrigin (String value) {
		if (origin.compareTo(value) == 0) return true;
		return false;
	}

	//check the validation
	public boolean checkValidated (boolean test) {
		if (validated == test) return true;
		return false;
	}

	//check the validation
	public boolean checkDeleted (boolean test) {
		if (deleted == test) return true;
		return false;
	}


	//check the height between min and max
	public boolean checkHeight (String heightParam) {
		double heightMin = 0.0;
		double heightMax = 999.0;
		int index = heightParam.indexOf (":");
		if (index >= 0) {
			heightMin = Check.doubleValue(heightParam.substring (0, index));
			heightMax = Check.doubleValue(heightParam.substring (index+1));
		}
		if ((height >= heightMin) && (height <= heightMax)) return true;
		return false;
	}

	//check the crown diameter between min and max
	public boolean checkDiameter (String diamParam) {
		double diamMin = 0.0;
		double diamMax = 999.0;
		int index = diamParam.indexOf (":");
		if (index >= 0) {
			diamMin = Check.doubleValue(diamParam.substring (0, index));
			diamMax = Check.doubleValue(diamParam.substring (index+1));
		}
		if ((crownDiameter >= diamMin) && (crownDiameter <= diamMax)) return true;
		return false;
	}


	@Override
	public String toString (){
		return "Plant id="+plantId+" specie="+specie;
	}





}
