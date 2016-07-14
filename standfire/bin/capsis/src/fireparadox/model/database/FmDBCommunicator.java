package fireparadox.model.database;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.StringTokenizer;
import java.util.Vector;

import capsis.lib.fire.fuelitem.FiSpecies;

import jeeb.lib.util.TicketDispenser;
import fireparadox.model.FmModel;

/**
 * FiDBCommunicator : communication with DB4O database
 * @author Oana Vigy & Eric Rigaud - August 2007
 * Updated by Isabelle LECOMTE - July 2009
 */
public class FmDBCommunicator {
	// Database communication protocol
	public static final String LINE_BREAK = "<br>";
	public static final String LINE_SEPARATOR = "&";
	public static final String FIELD_SEPARATOR = ";";
	public static final String CODE_SEPARATOR = ":";

	// URL of the FireParadox data base
	// fc - 3.4.2009 - new location given by Boris Pezzatti
private static String dataBaseURL = "http://www.wsl.ch/WebFuel/";
//private static String dataBaseURL = "http://fireintuition.efi.int:8080/WebFuel/";
//private static String dataBaseURL = "http://localhost:8080/WebFuel/";


	private static FmDBCommunicator instance;
    private String lineBreak;
    private String lineSeparator;
    private String fieldSeparator;
    private String codeSeparator;


	/** Singleton pattern.
	*	FiDBCommunicator c = FiDBCommunicator.getInstance ();
	*/
	public static FmDBCommunicator getInstance () {
		if (instance == null) {instance = new FmDBCommunicator ();}
		return instance;
	}

    /** Creates a new instance of BDCommunicator */
    private FmDBCommunicator () {
		lineBreak  = LINE_BREAK;
		lineSeparator  = LINE_SEPARATOR;
		fieldSeparator = FIELD_SEPARATOR;
		codeSeparator  = CODE_SEPARATOR;
    }

	/**	Return a String containing the URL of the FireParadox data base.
	*/
	public static String getDataBaseURL () {
		return dataBaseURL;
	}

	/********* ENUMS **********************************************/
	/**
	 * REQUEST N� 19
	 * Return openess descriptions list
	 */
	public Vector<String> get0peness () throws Exception {
		Vector<String> list = getSimple (dataBaseURL+"par?numRequest=19");
		return list;
    }
	/**
	 * REQUEST N� 20
	 * Return topographics positions list
	 */
    public Vector<String> getTopographies () throws Exception {
		Vector<String> list = getSimple (dataBaseURL+"par?numRequest=20");
		return list;
    }
	/**
	 * REQUEST N� 21
	 * Return aspects descriptions list
	 */
    public Vector<String> getAspects () throws Exception {
		Vector<String> list = getSimple (dataBaseURL+"par?numRequest=21");
		return list;
    }
	/**
	 * REQUEST N� 22
	 * Return slope descriptions list
	 */
    public Vector<String> getSlopes () throws Exception {
		Vector<String> list = getSimple (dataBaseURL+"par?numRequest=22");
		return list;
    }
	/**
	* REQUEST N� 27
	* Return events list
	*/
	public Vector<String> getEvents () throws Exception {
		Vector<String> list = getSimple (dataBaseURL+"par?numRequest=27");
		return list;
	}
	/**
	 * REQUEST N� 28
	 * Return particles classes
	 */
	public Vector<String> getParticles () throws Exception {
		Vector<String> list = getSimple (dataBaseURL+"par?numRequest=28");
		return list;
    }
	/**
	* REQUEST N� 29
	* Return layers list
	*/
	public Vector getLayers () throws Exception {
		Vector list = getSimple (dataBaseURL+"par?numRequest=29");
		return list;
    }

	/********* TAXONS **********************************************/
	/**
	 * REQUEST N� 9
	 * Return taxons list  : the returned vector contains only  Strings (taxons names)
	 */
    public Vector<String> getTaxon () throws Exception {
		Vector<String> taxon = getSimple (dataBaseURL+"par?numRequest=9");
		return taxon;
    }
	/**
	 * REQUEST N� 11
	 * Return species list from the database
	 */
	public ArrayList<FiSpecies> getSpecies (TicketDispenser speciesIdDispenser, FiSpecies specimen) throws Exception {

		ArrayList species = new ArrayList<FiSpecies>();
		Vector names = new Vector<String>();
		Vector list = getComplex (dataBaseURL+"par?numRequest=11");


		for (int cpt=0; cpt < list.size (); cpt++) {
			Vector speciesInfo = (Vector)(list.elementAt(cpt));

			if (speciesInfo.size () >= 1)  {

				String trait = (String) (speciesInfo.elementAt (0));
				String genus = (String) (speciesInfo.elementAt (1));
				String name = (String) (speciesInfo.elementAt (2));

				if (!names.contains(trait)) {
					FiSpecies specieTrait = new FiSpecies (speciesIdDispenser.next (), specimen,
							"", "" , trait, FiSpecies.TRAIT_TAXON_LEVEL);
					species.add (specieTrait);
					names.add (trait);
				}

				if (!names.contains(genus)) {
					FiSpecies specieGenus = new FiSpecies (speciesIdDispenser.next (), specimen,
							trait, "",  genus , FiSpecies.GENUS_TAXON_LEVEL);
					species.add (specieGenus);
					names.add (genus);
				}

				if (!names.contains(name)) {
					FiSpecies specie = new FiSpecies (speciesIdDispenser.next (), specimen,
							trait, genus, name, FiSpecies.SPECIES_TAXON_LEVEL);
					species.add (specie);
					names.add (name);

				}
			}

		}
		return species;
    }

	/********* CONTEXT : teams, countries, sites *******************************************/
  	/**
  	 * REQUEST N� 5
  	 * Return teams list from the database
  	 * the returned LinkedHashMap  contains the collection of FiDBTeam objects
 	 */
     public LinkedHashMap  getTeams () throws Exception {

 		Vector list = getComplex (dataBaseURL+"par?numRequest=5");
 		LinkedHashMap  teamList = new LinkedHashMap ();
 		LinkedHashMap  personList = new LinkedHashMap ();

		for (int cpt=0; cpt < list.size (); cpt++) {
			Vector teamInfo = (Vector)(list.elementAt(cpt));

			//check fields from database are complete
			if (teamInfo.size () >= 1) {
				//id has to be split
				String code = (String) (teamInfo.elementAt (0));
				int index = code.indexOf (codeSeparator);
				if (index > 0) {
					String sid = code.substring (index+1);					//ID
					long id = Long.parseLong (sid);

					String name = (String) (teamInfo.elementAt (1));		//NAME

					boolean deleted = false;
					String sdel = (String) (teamInfo.elementAt (2));		//IS DELETED ?
					if (sdel.compareTo("true") == 0)
						deleted = true;


					FmDBTeam team = new FmDBTeam (id, name,  deleted);

					//searching for persons of the team
					personList = getPersons (id, team);
					team.setPersons (personList);

					addMap (teamList, id, team);
				}
			}
		}

 		return teamList;
 	}
  	/**
  	 * REQUEST N� 6
  	 * Return persons list for a team from the database
  	 * the returned LinkedHashMap  contains the collection of FiDBPerson objects
 	 */
     public LinkedHashMap  getPersons (long teamId, FmDBTeam team) throws Exception {

 		Vector list = getComplex (dataBaseURL+"par?numRequest=6&ID="+teamId);
 		LinkedHashMap  personList = new LinkedHashMap ();

		for (int cpt=0; cpt < list.size (); cpt++) {
			Vector info = (Vector)(list.elementAt(cpt));

			//check fields from database are complete
			if (info.size () >= 1) {
				//id has to be split
				String code = (String) (info.elementAt (0));
				int index = code.indexOf (codeSeparator);
				if (index > 0) {
					String sid = code.substring (index+1);					//ID
					long id = Long.parseLong (sid);

					String name = (String) (info.elementAt (1));			//NAME

					boolean deleted = false;
					String sdel = (String) (info.elementAt (2));		//IS DELETED ?
					if (sdel.compareTo("true") == 0) deleted = true;

					addMap (personList, id, new FmDBPerson (id, name, team, deleted));
				}
			}
		}

 		return personList;
 	}
     /**
     * REQUEST N� 4
   	 * Check the team password in the database
  	 */
      public boolean  checkTeamPass (long teamId, String password) throws Exception {

    	Boolean checked = false;
    	Vector<String> result = getSimple (dataBaseURL+"par?numRequest=4&ID="+teamId+"&password="+password);
    	String code = (String) (result.elementAt (0));
 		if (code.equals("true")) checked = true;
  		return checked;
  	}

  	/**
  	 * REQUEST N� 7
  	 * Return countries list from the database
  	 * the returned LinkedHashMap  contains the collection of FiDBCountry objects
 	 */
     public LinkedHashMap  getCountries () throws Exception {

 		Vector list = getComplex (dataBaseURL+"par?numRequest=7&level=Country");
 		LinkedHashMap  countryList = new LinkedHashMap ();

		for (int cpt=0; cpt < list.size (); cpt++) {
			Vector countryInfo = (Vector)(list.elementAt(cpt));
			if (countryInfo.size () >= 1) {
				//id has to be split
				String code = (String) (countryInfo.elementAt (0));
				int index = code.indexOf (codeSeparator);
				if (index > 0) {
					String sid = code.substring (index+1);					//ID
					long id = Long.parseLong (sid);
					String name =(String) (countryInfo.elementAt (1));		//NAME
					boolean deleted = false;
					String sdel = (String) (countryInfo.elementAt (2));		//IS DELETED ?
					if (sdel.compareTo("true") == 0) deleted = true;

					addMap (countryList, id, new FmDBCountry (id, name, deleted));
				}
			}
		}
 		return countryList;
 	}
  	/**
  	 * REQUEST N� 7
  	 * Return municipalities list from the database
  	 * the returned LinkedHashMap  contains the collection of FiDBMunicipality objects
 	 */
     public LinkedHashMap  getMunicipalities (LinkedHashMap  countryList) throws Exception {

		//loading data from database
 		Vector list = getComplex (dataBaseURL+"par?numRequest=7&level=Municipality");
 		LinkedHashMap  municipalityList = new LinkedHashMap ();


		for (int cpt=0; cpt < list.size (); cpt++) {

			Vector municipalityInfo = (Vector)(list.elementAt(cpt));

			FmDBCountry country = null;

			//decoding municipality
			if (municipalityInfo.size () >= 1)  {

				//Country has to be split
				String code = (String) (municipalityInfo.elementAt (3));

				int index = code.indexOf (codeSeparator);
				if (index > 0) {
					String attribute = code.substring (0, index);
					if (attribute.compareTo("Country") == 0) {
						String sid = code.substring (index+1);						//COUNTRY ID
						long countryId = Long.parseLong (sid);
						 //searching parent (country)
						 if (countryList != null) {
							country = (FmDBCountry) (getMap (countryList, countryId));	//COUNTRY
 						 }
					}
				}

				//id has to be split
				code = (String) (municipalityInfo.elementAt (0));


				index = code.indexOf (codeSeparator);
				if (index > 0) {
					String sid = code.substring (index+1);						//ID
					long id = Long.parseLong (sid);
					String name =(String) (municipalityInfo.elementAt (1));		//NAME
					boolean deleted = false;
					String sdel = (String) (municipalityInfo.elementAt (2));		//IS DELETED ?
					if (sdel.compareTo("true") == 0) deleted = true;

					FmDBMunicipality municipality = new FmDBMunicipality (id, name, country, deleted);
					addMap (municipalityList, id, municipality);
				}
			}
		}
 		return municipalityList;
 	}

  	/**
  	 * REQUEST N� 7
  	 * Return sites list from the database
  	 * the returned LinkedHashMap  contains the collection of FiDBSite objects
 	 */
     public LinkedHashMap  getSites (LinkedHashMap  municipalityList) throws Exception {
 		Vector list = getComplex (dataBaseURL+"par?numRequest=7&level=Plot");
  		LinkedHashMap  siteList = new LinkedHashMap ();

		for (int cpt=0; cpt < list.size (); cpt++) {
			Vector siteInfo = (Vector)(list.elementAt(cpt));

			String code = (String) (siteInfo.elementAt (0));
			int index = code.indexOf (codeSeparator);
			if (index > 0) {
				if (siteInfo.size () >= 4) {
					String sid = code.substring (index+1);					//ID
					long id = -1;
					id = Long.parseLong (sid);
					String name =(String) (siteInfo.elementAt (1));			//NAME

					boolean deleted = false;
					String sdel = (String) (siteInfo.elementAt (2));		//IS DELETED ?
					if (sdel.compareTo("true") == 0) deleted = true;

					//searching municipality
					FmDBMunicipality municipality = null;
					if (municipalityList != null) {
						code = (String) (siteInfo.elementAt (3));
						index = code.indexOf (codeSeparator);
						if (index > 0) {
							String muni = code.substring (0,index);
							if (muni.compareTo("Municipality") == 0) {
								String smid = code.substring (index+1);
								long mid = Long.parseLong (smid);
								municipality = (FmDBMunicipality) getMap (municipalityList, mid);		//MUNICIPALITY
							}
						}
					}

					if (!name.equals("") && (id > 0) ) {
						addMap (siteList, id, new FmDBSite(id, name, municipality, deleted));
					}
				}
			}

		}

 		return siteList;
	}


	/**
	 * REQUEST N� 8
  	 * Return site information from the database for a given ID
  	 * the returned object contains the complete FiDBSite
	 */
    public FmDBSite getSite (LinkedHashMap  municipalityList, long siteId) throws Exception {

		FmDBSite site = null;
		FmDBMunicipality municipality = null;
		String siteCode = "";

		long descriptionId = -1;
		long topograghyId = -1;
		long slopeId = -1;
		long aspectId = -1;
		long slopeValueId = -1;
		long aspectValueId = -1;
		String description = "";
		String topograghy ="";
		String slope = "";
		String aspect = "";
		Double slopeValue = 0.0;
		Double aspectValue = 0.0;

		long coordinateId = -1;
		long altitudeId = -1;
		Double latitude = 0.0;
		Double longitude = 0.0;
		Double altitude = 0.0;


		long dominantTaxaId = -1;
		Collection dominantTaxa = null;
		boolean deleted = false;

 		Vector list =  getComplex (dataBaseURL+"par?numRequest=8&ID="+siteId);

		for (int cpt=0; cpt < list.size (); cpt++) {

			Vector info = (Vector)(list.elementAt(cpt));


			if (info.size () >= 1)  {	//Check returned string is complete

				//id has to be split
				String code = (String) (info.elementAt (0));
				int index = code.indexOf (codeSeparator);

				if (index > 0) {
					String attribute = code.substring (0,index);
					String sid = code.substring (index+1);

					if (attribute.compareTo("Plot") == 0) {
						siteCode  = (String) (info.elementAt (1));

						String sdel = (String) (info.elementAt (2));		//IS DELETED ?
						if (sdel.compareTo("true") == 0) deleted = true;

						//municipality
						if (municipalityList != null) {
							String muni = (String) (info.elementAt (3));
							int index2 = muni.indexOf (codeSeparator);
							if (index2 > 0) {
								String attribute2 = muni.substring (0,index2);
								if (attribute2.compareTo("Municipality") == 0) {
									String municipalitySid = muni.substring (index2+1);
									long municipalityId = Long.parseLong (municipalitySid);
									municipality = (FmDBMunicipality) getMap (municipalityList, municipalityId);		//MUNICIPALITY
								}
							}
						}
					}

					if (attribute.compareTo("VegetationDescription") == 0) {
						descriptionId = Long.parseLong (sid);
						description = (String) (info.elementAt (1));
						description = description.replace("\"","");
					}
					if (attribute.compareTo("Coordinates") == 0) {
						coordinateId = Long.parseLong (sid);
						String chaine = (String) (info.elementAt (1));
						if ((chaine.compareTo("null") != 0) && (chaine.compareTo("NaN") != 0)) {

							latitude =  Double.parseDouble (chaine);
							if (info.size () >= 2)  {
								chaine = (String) (info.elementAt (2));
								if ((chaine.compareTo("null") != 0) && (chaine.compareTo("NaN") != 0))
									longitude =  Double.parseDouble (chaine);
							}
						}
					}
					if (attribute.compareTo("Altitude") == 0) {
						String chaine = (String) (info.elementAt (1));
						altitudeId = Long.parseLong (sid);
						if ((chaine.compareTo("null") != 0) && (chaine.compareTo("NaN") != 0))
							altitude =  Double.parseDouble (chaine);
					}
					if (attribute.compareTo("TopographicPosition") == 0) {
						topograghyId = Long.parseLong (sid);
						topograghy = (String) (info.elementAt (1));
					}

					if (attribute.compareTo("Slope_Description") == 0) {
						slopeId = Long.parseLong (sid);
						slope = (String) (info.elementAt (1));

					}
					if (attribute.compareTo("Slope") == 0) {
						slopeValueId = Long.parseLong (sid);
						String chaine = (String) (info.elementAt (1));
						if ((chaine.compareTo("null") != 0) && (chaine.compareTo("NaN") != 0))
							slopeValue =  Double.parseDouble (chaine);
					}
					if (attribute.compareTo("Aspect_Description") == 0) {
						aspectId = Long.parseLong (sid);
						aspect = (String) (info.elementAt (1));
					}
					if (attribute.compareTo("Aspect_Azimuth") == 0) {
						aspectValueId = Long.parseLong (sid);
						String chaine = (String) (info.elementAt (1));
						if ((chaine.compareTo("null") != 0) && (chaine.compareTo("NaN") != 0))
							aspectValue =  Double.parseDouble (chaine);
					}
					if (attribute.compareTo("DominantTaxa") == 0) {
						dominantTaxa =  new ArrayList();
						dominantTaxaId = Long.parseLong (sid);
						if (info.size () >= 2) {
							dominantTaxa.add((String) (info.elementAt (2)));
						}
						if (info.size () >= 4) {
							dominantTaxa.add((String) (info.elementAt (4)));
						}
						if (info.size () >= 6) {
							dominantTaxa.add((String) (info.elementAt (6)));
						}
					}
				}
			}
		}


		//Return the site object
		site = new FmDBSite (siteId, siteCode, municipality,
							coordinateId, latitude, longitude,
							altitudeId, altitude,
							descriptionId, description,
							topograghyId, topograghy,
							slopeId, slope,
							slopeValueId, slopeValue,
							aspectId, aspect,
							aspectValueId, aspectValue,
							dominantTaxaId, dominantTaxa,
							deleted);
 		return site;
    }

	/**
	 * REQUEST N� 12
  	 * Return EVENTS list for a given SITE
  	 * the returned object is a MAP of FiDBEvent
	 */
    public LinkedHashMap<Long, FmDBEvent>  getSiteEvents (FmDBSite _site) throws Exception {

		LinkedHashMap  eventMap = new LinkedHashMap<Long, FmDBEvent> ();

		FmDBSite site = _site;
		long siteId = site.getSiteId();

		Vector list =  getComplex (dataBaseURL+"par?numRequest=13&ID="+siteId);


		if (list != null) {
			for (int cpt=0; cpt < list.size (); cpt++) {

				Vector info = (Vector)(list.elementAt(cpt));

				long eventId = -1;

				if (info.size () >= 5)  {	//Check returned string is complete

					//id has to be split
					String code = (String) (info.elementAt (0));
					int index = code.indexOf (codeSeparator);

					if (index > 0) {
						String attribut = code.substring (0,index);
						if (attribut.compareTo("Event") == 0) {

							String sid = code.substring (index+1);
							eventId = Long.parseLong (sid);
						}

						String name = (String) (info.elementAt (1));
						String dateStart = (String) (info.elementAt (2));
						String dateEnd = (String) (info.elementAt (3));
						boolean isYearly = false;
						String isYear = (String) (info.elementAt (4));
						if (isYear.compareTo("true") == 0) isYearly = true;

						//Return the event object
						eventMap.put(eventId, new FmDBEvent (eventId, name, dateStart, dateEnd, isYearly) );

					}
				}
			}
		}
		return eventMap;
	}
	/**
	* REQUEST N� 14
	* Check list
	*/
	public ArrayList<FmDBCheck> getCheckList () throws Exception {

		ArrayList checks = new ArrayList<FmDBCheck>();

		Vector list =  getComplex (dataBaseURL+"par?numRequest=14&name=Pimont");

		for (int cpt=0; cpt < list.size (); cpt++) {
			Vector chaine = (Vector)(list.elementAt(cpt));

			Double errorMin = 0.0;
			Double warningMin = 0.0;
			Double errorMax = 0.0;
			Double warningMax = 0.0;
			String parameterName = "";
			String particleList = "";
			String typeName = "";
			String sErrorMin = "";
			String sErrorMax = "";
			String sWarningMin = "";
			String sWarningMax = "";



			if (chaine.size () == 7)  {

				parameterName = (String) (chaine.elementAt (0));
				typeName = (String) (chaine.elementAt (1));
				particleList = (String) (chaine.elementAt (2));
				sErrorMin = (String) (chaine.elementAt (3));
				sErrorMax = (String) (chaine.elementAt (4));
				sWarningMin = (String) (chaine.elementAt (5));
				sWarningMax = (String) (chaine.elementAt (6));
			}

			else if (chaine.size () == 6)  {
				parameterName = (String) (chaine.elementAt (0));
				particleList = (String) (chaine.elementAt (1));
				sErrorMin = (String) (chaine.elementAt (2));
				sErrorMax = (String) (chaine.elementAt (3));
				sWarningMin = (String) (chaine.elementAt (4));
				sWarningMax = (String) (chaine.elementAt (5));
			}




			if ((sErrorMin.compareTo("null") != 0) && (sErrorMin.compareTo("NaN") != 0))
				errorMin =  Double.parseDouble (sErrorMin);


			if ((sErrorMax.compareTo("null") != 0) && (sErrorMax.compareTo("NaN") != 0))
				errorMax =  Double.parseDouble (sErrorMax);


			if ((sWarningMin.compareTo("null") != 0) && (sWarningMin.compareTo("NaN") != 0))
				warningMin =  Double.parseDouble (sWarningMin);

			if ((sWarningMax.compareTo("null") != 0) && (sWarningMax.compareTo("NaN") != 0))
				warningMax =  Double.parseDouble (sWarningMax);

			particleList = particleList.replace("(","");
			particleList = particleList.replace(")","");

			StringTokenizer token = new StringTokenizer(particleList, ":");
			if (token.countTokens() > 0) {
				while (token.hasMoreTokens()) {

					String particleName = token.nextToken (":");

					FmDBCheck check = new FmDBCheck (particleName, parameterName,typeName,
													errorMin, errorMax, warningMin, warningMax);
					checks.add (check);

				}
			}

		}
		return checks;

	}
  	/********* PLANTS *******************************************/
	/**
	* REQUEST N� 39
   	* Return plant list from the database
   	* the returned object contains a map of FiDBPlant
	*/
	public LinkedHashMap<Long, FmDBPlant>  getPlants (int type, LinkedHashMap  teamList, LinkedHashMap  siteList,
													  int delete, int validate) throws Exception {

		LinkedHashMap  plantList = new LinkedHashMap<Long, FmDBPlant> ();

		String param = new String("");
		if (type == 2)
			param=param+"&shapeClass=ShapeLayer";

		if (type == 3)
			param=param+"&shapeClass=ShapeSample";


		if (validate > 0)
			param=param+"&validate="+validate;


		if (delete > 0)
			param=param+"&delete="+delete;

		int fuelType = type;

		Vector list =  getComplex (dataBaseURL+"par?numRequest=39"+param);

		if (list != null) {
			for (int cpt=0; cpt < list.size (); cpt++) {

				Vector info = (Vector)(list.elementAt(cpt));

				Double height = 0.0;
				Double baseHeight = 0.0;
				Double diameter = 0.0;
				Double perDiameter = 0.0;


				String teamName = null;
				String siteName = null;
				FmDBTeam team = null;
				FmDBSite site = null;

				boolean deleted = false;
				boolean validated = false;

				long plantId = -1;

				if (info.size () >= 6)  {	//Check returned string is complete

					//id has to be split
					String code = (String) (info.elementAt (0));
					int index = code.indexOf (codeSeparator);

					if (index > 0) {
						String sid = code.substring (index+1);
						plantId = Long.parseLong (sid);							//SHAPE ID


						String specieName = (String) (info.elementAt (1));			//SPECIES

						String origin =(String) (info.elementAt (2));				//ORIGIN

						String chaine =(String) (info.elementAt (3));
						if ((chaine.compareTo("null") != 0) && (chaine.compareTo("NaN") != 0))
							 height =  Double.parseDouble (chaine);					//HEIGHT
						chaine = (String) (info.elementAt (4));
						if ((chaine.compareTo("null") != 0) && (chaine.compareTo("NaN") != 0))
							 baseHeight =  Double.parseDouble (chaine);				//BASEHEIGHT
						chaine = (String) (info.elementAt (5));
						if ((chaine.compareTo("null") != 0) && (chaine.compareTo("NaN") != 0))
							 diameter =  Double.parseDouble (chaine);				//DIAMETER
						chaine = (String) (info.elementAt (6));
						if ((chaine.compareTo("null") != 0) && (chaine.compareTo("NaN") != 0))
							 perDiameter =  Double.parseDouble (chaine);				//DIAMETER 2

						String reference = (String) (info.elementAt (7));					//REF ID


						if (siteList != null) {
							chaine = (String) (info.elementAt (8));
							if (chaine.compareTo("null") != 0) {
								long siteId = Long.parseLong (chaine);
								site = (FmDBSite) getMap (siteList, siteId);			//SITE
							 }
						}
						if (teamList != null) {
							chaine = (String) (info.elementAt (9));
							if (chaine.compareTo("null") != 0) {
								long teamId = Long.parseLong (chaine);
								team = (FmDBTeam) getMap (teamList, teamId);			//TEAM
							}
						}


						chaine = (String) (info.elementAt (10));
						int index3 = chaine.indexOf (codeSeparator);
						if (index3 > 0) {
							String attrib = chaine.substring (0, index3);
							if (attrib.compareTo("isDeleted") == 0) {
								String sdel = chaine.substring (index3 + 1);
								if (sdel.compareTo("true") == 0) deleted = true;
								if (sdel.compareTo("false") == 0) deleted = false;
							}
						}


						if (info.size () >= 12)  {
							chaine = (String) (info.elementAt (11));
							index3 = chaine.indexOf (codeSeparator);
							if (index3 > 0) {
								String attrib = chaine.substring (0, index3);
								if (attrib.compareTo("isValidated") == 0) {
									String sdel = chaine.substring (index3 + 1);
									if (sdel.compareTo("true") == 0) validated = true;
									if (sdel.compareTo("false") == 0) validated = false;
								}
							}
						}



						//add fuel in the result table
						addMap (plantList, plantId, new FmDBPlant (plantId, specieName, reference,
															height, baseHeight,diameter, perDiameter,
															team, site, origin, deleted, validated));
					}
				}
			}
		}

		return plantList;
      }

  	/**
  	 * REQUEST N� 32
	 * Return plant info for a given ID
	 * the returned object contains the complete FiDBPlant
  	 */
      public FmDBPlant getPlant (FmModel _model, long plantId) throws Exception {

  		String speciesName = "";
  		int fuelType = 0;
  		String origin = "";

  		long actorId = -1;
  		long teamId = -1;
  		FmDBTeam team = null;
  		long [] personTable = new long [3];
  		String actor = "";
  		long samplingDateId = -1;
  		String samplingDate = "";

  		long plotId = -1;
  		long siteId = -1;
  		FmDBSite site = null;


  		long referenceId = -1;
  		String reference = "";
  		long commentId = -1;
  		String comment = "";

  		double height = 0.0;
  		double crownBaseHeight = 0.0;
  		double crownDiameter = 0.0;
  		double crownPerpendicularDiameter = 0.0;
  		double maxDiameterHeight = 0.0;
  		double biomass = 0.0;
  		long heightId = -1;
  		long crownBaseHeightId = -1;
  		long crownDiameterId = -1;
  		long crownPerpendicularDiameterId = -1;
  		long maxDiameterHeightId = -1;
		long biomassId = -1;

  		long coordinateId = -1;
  		long altitudeId = -1;
  		Double latitude = 0.0;
  		Double longitude = 0.0;
  		Double altitude = 0.0;

  		long plantStatusId = -1;
  		String plantStatus = "";
  		long coverPcId = -1;
  		Double coverPc = 0.0;
  		long openessId = -1;
  		String openess = "";
  		long dominantTaxaId = -1;
  		Collection dominantTaxa = null;

  		boolean deleted = false;
  		boolean validated = false;


  		//init person table
  		personTable[0] = -1;
  		personTable[1] = -1;
  		personTable[2] = -1;

  		LinkedHashMap  teamMap = getTeams();
		LinkedHashMap countryMap = _model.getCountryMap ();
		LinkedHashMap municipalityMap = getMunicipalities (countryMap);
		LinkedHashMap  siteMap = getSites (municipalityMap) ;


   		Vector list =  getComplex (dataBaseURL+"par?numRequest=32&ID="+plantId);

  		for (int cpt=0; cpt < list.size (); cpt++) {

  			Vector info = (Vector)(list.elementAt(cpt));


  			if (info.size () >= 1)  {	//Check returned string is complete

  				//id has to be split
  				String code = (String) (info.elementAt (0));
  				int index = code.indexOf (codeSeparator);

  				if (index > 0) {
  					String attribute = code.substring (0,index);
  					String sid = code.substring (index+1);


  					if (attribute.compareTo("Plant") == 0) {
  						speciesName = (String) (info.elementAt (1));			//SPECIES
  					}


  					if (attribute.compareTo("isDeleted") == 0) {
  						if (sid.compareTo("true") == 0) deleted = true;
						if (sid.compareTo("false") == 0) deleted = false;
  					}

  					if (attribute.compareTo("isValidated") == 0) {
  						if (sid.compareTo("true") == 0) validated = true;
						if (sid.compareTo("false") == 0) validated = false;
  					}

  					if (attribute.compareTo("Origin") == 0) {
  						origin = sid;											//ORGIN
  					}


  					if (attribute.compareTo("ID") == 0) {
  					 	referenceId = Long.parseLong (sid);
  						reference = (String) (info.elementAt (1));			//REFERENCE
  						reference = reference.replace("\"","");
  					}


  					if (attribute.compareTo("Biomass") == 0) {
  					 	biomassId = Long.parseLong (sid);
  					 	String chaine = (String) (info.elementAt (1));
  					 	if ((chaine.compareTo("null") != 0) && (chaine.compareTo("NaN") != 0))
  							biomass =  Double.parseDouble (chaine); //BIOMASS TOTAL MEASURED
  					}

  					if (attribute.compareTo("Note") == 0) {
  					 	commentId = Long.parseLong (sid);
  						comment = (String) (info.elementAt (1));			//COMMENT
  						comment = comment.replace("\"","");
  					}

  					if (attribute.compareTo("SamplingDate") == 0) {
  						samplingDateId = Long.parseLong (sid);
  						samplingDate = (String) (info.elementAt (1));		//DATE
  					}

  					if (attribute.compareTo("Coordinates") == 0) {
  						coordinateId = Long.parseLong (sid);
    					 String chaine = (String) (info.elementAt (1));
  					 	if ((chaine.compareTo("null") != 0) && (chaine.compareTo("NaN") != 0))
  							latitude =  Double.parseDouble (chaine);
  						if (info.size () >= 2)  {
							chaine = (String) (info.elementAt (2));
  					 		if ((chaine.compareTo("null") != 0) && (chaine.compareTo("NaN") != 0))
  								longitude =  Double.parseDouble (chaine);
  						}
  					}
  					if (attribute.compareTo("Altitude") == 0) {
  						altitudeId = Long.parseLong (sid);
  						altitude =  Double.parseDouble ((String) (info.elementAt (1)));
  					}

  					if (attribute.compareTo("Height") == 0) {
  						heightId = Long.parseLong (sid);
  						height =  Double.parseDouble ((String) (info.elementAt (1)));
  					}

  					if (attribute.compareTo("CBH") == 0) {
  						crownBaseHeightId = Long.parseLong (sid);
  						crownBaseHeight =  Double.parseDouble ((String) (info.elementAt (1)));
  					}

  					if (attribute.compareTo("CrownDiameter") == 0) {
  						crownDiameterId = Long.parseLong (sid);
  						crownDiameter =  Double.parseDouble ((String) (info.elementAt (1)));
  					}

  					if (attribute.compareTo("CrownDiameterPerpendicular") == 0) {
  						crownPerpendicularDiameterId = Long.parseLong (sid);
  						crownPerpendicularDiameter =  Double.parseDouble ((String) (info.elementAt (1)));
  					}

  					if (attribute.compareTo("Height_MaxCrownDiameter") == 0) {
  						maxDiameterHeightId = Long.parseLong (sid);
  						maxDiameterHeight =  Double.parseDouble ((String) (info.elementAt (1)));
  					}

  					if (attribute.compareTo("Cover_PC") == 0) {
  						coverPcId = Long.parseLong (sid);
  						coverPc =  Double.parseDouble ((String) (info.elementAt (1)));
  					}
  					if (attribute.compareTo("Openess") == 0) {
  						openessId = Long.parseLong (sid);
  						openess =  (String) (info.elementAt (1));
  					}
  					if (attribute.compareTo("Dominance") == 0) {
  						plantStatusId = Long.parseLong (sid);
  						plantStatus =  (String) info.elementAt (1);
  					}

  					if (attribute.compareTo("OverShadowingSpecies") == 0) {
  						dominantTaxa =  new ArrayList();
  						dominantTaxaId = Long.parseLong (sid);
  						if (info.size () >= 2) {
  							dominantTaxa.add((String) (info.elementAt (2)));
  						}
  						if (info.size () >= 4) {
  							dominantTaxa.add((String) (info.elementAt (4)));
  						}
  						if (info.size () >= 6) {
  							dominantTaxa.add((String) (info.elementAt (6)));
  						}

  					}
  					if (attribute.compareTo("Actors") == 0) {

  						 //TEAM
  						actorId = Long.parseLong (sid);
  						String teamLine = (String) (info.elementAt (1));
  						int teamIndex = teamLine.indexOf (codeSeparator);
  						if ((teamIndex > 0) && (teamMap != null)) {
  							String teamAttribute = teamLine.substring (0,teamIndex);
  							String steam = teamLine.substring (teamIndex+1);
  							if (teamAttribute.compareTo("Team") == 0) {
  								teamId = Long.parseLong (steam);
  								team = (FmDBTeam) getMap (teamMap, teamId);		//TEAM
  							}
  						}
  						//PERSONS
  						int i = 0;
  						for (int nb=3; nb<info.size (); nb=nb+2) {
  							String personLine = (String) (info.elementAt (nb));
  							int personIndex = personLine.indexOf (codeSeparator);
  							if (personIndex > 0) {
  								String personAttribute = personLine.substring (0,personIndex);
  								String sperson = personLine.substring (personIndex+1);
  								if ((personAttribute.compareTo("Person") == 0) && (i < 10)) {
  									long personId = Long.parseLong (sperson);					//PERSONS ID
  									String personName = (String) (info.elementAt (nb+1));
  									personTable[i] = personId;
  									i++;
  								}
  							}
  						}

  					}
  					//SITE
  					if (attribute.compareTo("Site") == 0) {
  						siteId = Long.parseLong (sid);
  						String siteLine = (String) (info.elementAt (1));
  						int siteIndex = siteLine.indexOf (codeSeparator);
  						if ((siteIndex > 0) && (siteMap != null)) {
  							String siteAttribute = siteLine.substring (0,siteIndex);
  							String ssite = siteLine.substring (siteIndex+1);
  							if (siteAttribute.compareTo("Plot") == 0) {
  								plotId = Long.parseLong (ssite);
  								site = (FmDBSite) getMap (siteMap, plotId);		//SITE
  							}
  						}
  					}
  				}
  			}
  		}

  		//create the plant object
  		FmDBPlant plant = new FmDBPlant (plantId, speciesName,
  							referenceId, reference,
  							origin,
  							actorId, team, personTable,
  							siteId, site,
  							samplingDateId, samplingDate,
  							coordinateId, latitude, longitude,
  							altitudeId, altitude,
  							commentId, comment,
  							heightId, height,
  							crownBaseHeightId, crownBaseHeight,
  							crownDiameterId, crownDiameter,
  							crownPerpendicularDiameterId, crownPerpendicularDiameter,
  							maxDiameterHeightId, maxDiameterHeight,
  							biomassId, biomass,
  							plantStatusId, plantStatus,
  							openessId, openess,
  							coverPcId, coverPc,
  							dominantTaxaId, dominantTaxa,
  							deleted, validated);

   		return plant;
      }
   	/********* SHAPES *******************************************/
	/**
	 * REQUEST N� 38
  	 * Return SHAPE list for a given PLANT
  	 * the returned object is a MAP of FiDBShape
	 */
    public LinkedHashMap<Long, FmDBShape>  getPlantShapes (FmDBPlant _plant, int delete) throws Exception {

		LinkedHashMap  shapeMap = new LinkedHashMap<Long, FmDBShape> ();

		FmDBPlant plant = _plant;
		long plantId = plant.getPlantId();

		String param = new String("");


		if (delete > 0)
			param=param+"&delete="+delete;


		Vector list =  getComplex (dataBaseURL+"par?numRequest=38&ID="+plantId+param);


		if (list != null) {
			for (int cpt=0; cpt < list.size (); cpt++) {

				Vector info = (Vector)(list.elementAt(cpt));


				long shapeId = -1;
				int fuelType = 0;

				Double widthMin = 0.0;
				Double widthMax = 0.0;

				Double voxelXSize = 0.0;
				Double voxelYSize = 0.0;
				Double voxelZSize = 0.0;

				Double xMax = 0.0;
				Double yMax = 0.0;
				Double zMax = 0.0;
				Double xEdgeMax = 0.0;
				Double yEdgeMax = 0.0;
				Double zEdgeMax = 0.0;

				String shapeKind = "XZ";

				boolean cubeMethod = false;
				boolean deleted = false;
				boolean validated = false;


				if (info.size () >= 6)  {	//Check returned string is complete

					//id has to be split
					String code = (String) (info.elementAt (0));
					int index = code.indexOf (codeSeparator);

					if (index > 0) {
						String attribut = code.substring (0,index);
						if (attribut.compareTo("Shape") == 0) {
							fuelType = 1;
						}
						else if (attribut.compareTo("ShapeLayer") == 0) {
							fuelType = 2;
						}
						else if (attribut.compareTo("ShapeSample") == 0) {
							fuelType = 3;
						}

						String sid = code.substring (index+1);
						shapeId = Long.parseLong (sid);							//SHAPE ID

						String origin =(String) (info.elementAt (1));				//ORIGIN


						String chaine =(String) (info.elementAt (2));
						if ((chaine.compareTo("null") != 0) && (chaine.compareTo("NaN") != 0))
							 voxelXSize =  Double.parseDouble (chaine);				//VOXEL SIZE

						chaine = (String) (info.elementAt (3));
						if ((chaine.compareTo("null") != 0) && (chaine.compareTo("NaN") != 0))
							 voxelYSize =  Double.parseDouble (chaine);

						chaine = (String) (info.elementAt (4));
						if ((chaine.compareTo("null") != 0) && (chaine.compareTo("NaN") != 0))
							 voxelZSize =  Double.parseDouble (chaine);


						chaine =(String) (info.elementAt (5));
						if ((chaine.compareTo("null") != 0) && (chaine.compareTo("NaN") != 0))
							 xMax =  Double.parseDouble (chaine);				//GRID SIZE

						chaine = (String) (info.elementAt (6));
						if ((chaine.compareTo("null") != 0) && (chaine.compareTo("NaN") != 0))
							 yMax =  Double.parseDouble (chaine);

						chaine = (String) (info.elementAt (7));
						if ((chaine.compareTo("null") != 0) && (chaine.compareTo("NaN") != 0))
							 zMax =  Double.parseDouble (chaine);




						//SHAPE PLANT
						if (fuelType == 1) {

							//shape KIND (XZ, XZ_XZ, XYZ)
							chaine = (String) (info.elementAt (8));
							int index3 = chaine.indexOf (codeSeparator);
							if (index3 > 0) {
								String attrib = chaine.substring (0, index3);
								if (attrib.compareTo("isProfile") == 0) {
									shapeKind = chaine.substring (index3 + 1);

								}
							}

							chaine = (String) (info.elementAt (9));
							index3 = chaine.indexOf (codeSeparator);
							if (index3 > 0) {
								String attrib = chaine.substring (0, index3);
								if (attrib.compareTo("isCube") == 0) {
									String sdel = chaine.substring (index3 + 1);
									if (sdel.compareTo("true") == 0) cubeMethod = true;
									if (sdel.compareTo("false") == 0) cubeMethod = false;
								}
							}


							chaine = (String) (info.elementAt (10));
							index3 = chaine.indexOf (codeSeparator);
							if (index3 > 0) {
								String attrib = chaine.substring (0, index3);
								if (attrib.compareTo("isDeleted") == 0) {
									String sdel = chaine.substring (index3 + 1);
									if (sdel.compareTo("true") == 0) deleted = true;
									if (sdel.compareTo("false") == 0) deleted = false;
								}
							}


							chaine = (String) (info.elementAt (11));
							index3 = chaine.indexOf (codeSeparator);
							if (index3 > 0) {
								String attrib = chaine.substring (0, index3);
								if (attrib.compareTo("isValidated") == 0) {
									String sdel = chaine.substring (index3 + 1);
									if (sdel.compareTo("true") == 0) validated = true;
									if (sdel.compareTo("false") == 0) validated = false;
								}
							}
						}

						//SHAPE LAYER
						if (fuelType == 2) {


							chaine = (String) (info.elementAt (8));
							if ((chaine.compareTo("null") != 0) && (chaine.compareTo("NaN") != 0))
								xEdgeMax =  Double.parseDouble (chaine);


							chaine = (String) (info.elementAt (9));
							if ((chaine.compareTo("null") != 0) && (chaine.compareTo("NaN") != 0))
								yEdgeMax =  Double.parseDouble (chaine);

							chaine = (String) (info.elementAt (10));
							if ((chaine.compareTo("null") != 0) && (chaine.compareTo("NaN") != 0))
								zEdgeMax =  Double.parseDouble (chaine);


							chaine = (String) (info.elementAt (11));
							if ((chaine.compareTo("null") != 0) && (chaine.compareTo("NaN") != 0))
								widthMin =  Double.parseDouble (chaine);

							 chaine = (String) (info.elementAt (12));
							 if ((chaine.compareTo("null") != 0) && (chaine.compareTo("NaN") != 0))
								widthMax =  Double.parseDouble (chaine);


							//shape KIND (XZ, XZ_XZ, XYZ)
							chaine = (String) (info.elementAt (13));
							int index3 = chaine.indexOf (codeSeparator);
							if (index3 > 0) {
								String attrib = chaine.substring (0, index3);
								if (attrib.compareTo("isProfile") == 0) {
									shapeKind = chaine.substring (index3 + 1);

								}
							}

							chaine = (String) (info.elementAt (14));
							index3 = chaine.indexOf (codeSeparator);
							if (index3 > 0) {
								String attrib = chaine.substring (0, index3);
								if (attrib.compareTo("isCube") == 0) {
									String sdel = chaine.substring (index3 + 1);
									if (sdel.compareTo("true") == 0) cubeMethod = true;
									if (sdel.compareTo("false") == 0) cubeMethod = false;
								}
							}


							chaine = (String) (info.elementAt (15));
							index3 = chaine.indexOf (codeSeparator);
							if (index3 > 0) {
								String attrib = chaine.substring (0, index3);
								if (attrib.compareTo("isDeleted") == 0) {
									String sdel = chaine.substring (index3 + 1);
									if (sdel.compareTo("true") == 0) deleted = true;
									if (sdel.compareTo("false") == 0) deleted = false;
								}
							}


							chaine = (String) (info.elementAt (16));
							index3 = chaine.indexOf (codeSeparator);
							if (index3 > 0) {
								String attrib = chaine.substring (0, index3);
								if (attrib.compareTo("isValidated") == 0) {
									String sdel = chaine.substring (index3 + 1);
									if (sdel.compareTo("true") == 0) validated = true;
									if (sdel.compareTo("false") == 0) validated = false;
								}
							}
						}

						//SHAPE SAMPLE
						if (fuelType == 3) {

							//shape KIND (XZ, XZ_XZ, XYZ)
							chaine = (String) (info.elementAt (8));
							if (chaine.compareTo("Unique") == 0) fuelType = 3;
							if (chaine.compareTo("Core") == 0) fuelType = 4;
							if (chaine.compareTo("Edge") == 0) fuelType = 5;

							chaine = (String) (info.elementAt (9));
							int index3 = chaine.indexOf (codeSeparator);
							if (index3 > 0) {
								String attrib = chaine.substring (0, index3);
								if (attrib.compareTo("isCube") == 0) {
									String sdel = chaine.substring (index3 + 1);
									if (sdel.compareTo("true") == 0) cubeMethod = true;
									if (sdel.compareTo("false") == 0) cubeMethod = false;
								}
							}

							chaine = (String) (info.elementAt (10));
							index3 = chaine.indexOf (codeSeparator);
							if (index3 > 0) {
								String attrib = chaine.substring (0, index3);
								if (attrib.compareTo("isDeleted") == 0) {
									String sdel = chaine.substring (index3 + 1);
									if (sdel.compareTo("true") == 0) deleted = true;
									if (sdel.compareTo("false") == 0) deleted = false;
								}
							}


							chaine = (String) (info.elementAt (11));
							index3 = chaine.indexOf (codeSeparator);
							if (index3 > 0) {
								String attrib = chaine.substring (0, index3);
								if (attrib.compareTo("isValidated") == 0) {
									String sdel = chaine.substring (index3 + 1);
									if (sdel.compareTo("true") == 0) validated = true;
									if (sdel.compareTo("false") == 0) validated = false;
								}
							}
  						}



						//add fuel in the result table
						addMap (shapeMap, shapeId, new FmDBShape (shapeId, plant,
															fuelType, shapeKind,
															voxelXSize, voxelYSize, voxelZSize,
															xMax, yMax, zMax,
															xEdgeMax, yEdgeMax, zEdgeMax,
															widthMin, widthMax,
															origin,
															cubeMethod, deleted, validated));
					}
				}
			}
		}

 		return shapeMap;
    }

	/**
	* REQUEST N� 46
   	* Return shape list from the database
   	* the returned object contains a map of FiDBShape
	*/
	public LinkedHashMap<Long, FmDBShape>  getShapes (LinkedHashMap  teamList, LinkedHashMap  siteList,
													  int delete, int validate) throws Exception {

		LinkedHashMap  allShapeList = new LinkedHashMap<Long, FmDBShape> ();


		Vector list =  getComplex (dataBaseURL+"par?numRequest=46");

		if (list != null) {
			for (int cpt=0; cpt < list.size (); cpt++) {

				Vector info = (Vector)(list.elementAt(cpt));

				Double height = 0.0;
				Double diameter = 0.0;


				String teamName = null;
				String siteName = null;
				FmDBTeam team = null;
				FmDBSite site = null;

				Double voxelXSize = 0.0;
				Double voxelYSize = 0.0;
				Double voxelZSize = 0.0;

				Double xMax = 0.0;
				Double yMax = 0.0;
				Double zMax = 0.0;


				boolean cubeMethod = false;
				boolean deleted = false;
				boolean validated = false;

				long plantId = -1;
				long shapeId = -1;
				int fuelType = 0;

				if (info.size () >= 6)  {	//Check returned string is complete

					//id has to be split
					String code = (String) (info.elementAt (0));
					int index = code.indexOf (codeSeparator);

					if (index > 0) {
						String sid = code.substring (index+1);
						shapeId = Long.parseLong (sid);							//SHAPE ID


						String specieName = (String) (info.elementAt (2));			//SPECIES

						String origin =(String) (info.elementAt (3));				//ORIGIN




						String chaine =(String) (info.elementAt (4));
						if ((chaine.compareTo("null") != 0) && (chaine.compareTo("NaN") != 0))
							 voxelXSize =  Double.parseDouble (chaine);				//VOXEL SIZE

						chaine = (String) (info.elementAt (5));
						if ((chaine.compareTo("null") != 0) && (chaine.compareTo("NaN") != 0))
							 voxelYSize =  Double.parseDouble (chaine);

						chaine = (String) (info.elementAt (6));
						if ((chaine.compareTo("null") != 0) && (chaine.compareTo("NaN") != 0))
							 voxelZSize =  Double.parseDouble (chaine);


						chaine =(String) (info.elementAt (7));
						if ((chaine.compareTo("null") != 0) && (chaine.compareTo("NaN") != 0))
							 xMax =  Double.parseDouble (chaine);				//GRID SIZE

						chaine = (String) (info.elementAt (8));
						if ((chaine.compareTo("null") != 0) && (chaine.compareTo("NaN") != 0))
							 yMax =  Double.parseDouble (chaine);

						chaine = (String) (info.elementAt (9));
						if ((chaine.compareTo("null") != 0) && (chaine.compareTo("NaN") != 0))
							 zMax =  Double.parseDouble (chaine);




						String reference = (String) (info.elementAt (10));

						if (siteList != null) {
							chaine = (String) (info.elementAt (11));
							if (chaine.compareTo("null") != 0) {
								long siteId = Long.parseLong (chaine);
								site = (FmDBSite) getMap (siteList, siteId);			//SITE
							 }
						}


						if (teamList != null) {
							chaine = (String) (info.elementAt (12));
							if (chaine.compareTo("null") != 0) {
								long teamId = Long.parseLong (chaine);
								team = (FmDBTeam) getMap (teamList, teamId);			//TEAM
							}
						}


						chaine = (String) (info.elementAt (13));
						if (chaine.compareTo("Unique") == 0) fuelType = 3;
						if (chaine.compareTo("Core") == 0) fuelType = 4;
						if (chaine.compareTo("Edge") == 0) fuelType = 5;


						chaine = (String) (info.elementAt (14));
						int index3 = chaine.indexOf (codeSeparator);
						if (index3 > 0) {
							String attrib = chaine.substring (0, index3);
							if (attrib.compareTo("isCube") == 0) {
								String sdel = chaine.substring (index3 + 1);
								if (sdel.compareTo("true") == 0) cubeMethod = true;
								if (sdel.compareTo("false") == 0) cubeMethod = false;
							}
						}


						chaine = (String) (info.elementAt (15));
						index3 = chaine.indexOf (codeSeparator);
						if (index3 > 0) {
							String attrib = chaine.substring (0, index3);
							if (attrib.compareTo("isDeleted") == 0) {
								String sdel = chaine.substring (index3 + 1);
								if (sdel.compareTo("true") == 0) deleted = true;
								if (sdel.compareTo("false") == 0) deleted = false;
							}
						}



						chaine = (String) (info.elementAt (16));
						index3 = chaine.indexOf (codeSeparator);
						if (index3 > 0) {
							String attrib = chaine.substring (0, index3);
							if (attrib.compareTo("isValidated") == 0) {
								String sdel = chaine.substring (index3 + 1);
								if (sdel.compareTo("true") == 0) validated = true;
								if (sdel.compareTo("false") == 0) validated = false;
							}
						}



						//add fuel in the result table
						FmDBPlant plant = new FmDBPlant (plantId, specieName,  reference,
														0.0, 0.0 ,0.0, 0.0,
														team, site, origin, deleted, validated);

						//add fuel in the result table
						addMap (allShapeList, shapeId, new FmDBShape (shapeId, plant,
															fuelType, "XZ",
															voxelXSize, voxelYSize, voxelZSize,
															xMax, yMax, zMax,
															xMax, yMax, zMax,
															0.0, 0.0,
															origin,
															cubeMethod, deleted, validated));


					}
				}
			}
		}

		//return all shape list
		return allShapeList;
      }

	/**
	* REQUEST N� 40
	* Return fuel crown description from the database for a given ID
	* the returned collection contains the cells describing the crown
	*/
	public FmDBShape getShapeVoxels (FmDBShape _shape, boolean isUUID) throws Exception {


		FmDBShape shape = _shape;
		long shapeID = shape.getShapeId();


		HashMap <Long, FmDBVoxel> voxels  = new HashMap <Long, FmDBVoxel> ();		//map of voxels
		HashMap <Long, FmDBVoxel> edges  = new HashMap <Long, FmDBVoxel> ();		//map of edges voxels
		HashMap <Long, FmVoxelType> typeMap = new HashMap <Long, FmVoxelType> ();	//map of voxel types
		HashMap <Long, FmDBParticle> particleMap = new HashMap <Long, FmDBParticle> ();	//map of particle for a voxel

		Collection plantParticles = new ArrayList ();

		Vector list =  getComplex (dataBaseURL+"par?numRequest=40&ID="+shapeID+"&UUID="+isUUID);


		for (int cpt=0; cpt < list.size (); cpt++) {

			Vector info = (Vector)(list.elementAt(cpt));

			//id has to be split
			String code = (String) (info.elementAt (0));
			int index = code.indexOf (codeSeparator);

			if (index > 0) {

				String attribute = code.substring (0, index);

				//PARTICLES ATTACHED TO THE CELLS
				if ((attribute.compareTo("Cell") == 0) || (attribute.compareTo("CellEdge") == 0)) {

					if (info.size () >= 9)  {	//Check returned string is complete

						String cid = code.substring (index+1);
						long cellId = Long.parseLong (cid);

						//get the cell in the collection if already listed
						Boolean edge = false;
						if (attribute.compareTo("CellEdge") == 0) edge = true;
						FmDBVoxel c  = null;
						if (edge) c = (FmDBVoxel) edges.get (cellId);
						else c = (FmDBVoxel) voxels.get (cellId);

						//Voxel type
						String typeInfo =(String) (info.elementAt (4));
						long typeId = -1;
						int index2 = typeInfo.indexOf (codeSeparator);
						if (index2 > 0) {
							String lid = typeInfo.substring (index2+1);
							typeId = Long.parseLong (lid);						//LAYER ID
						}
						String typeName =(String) (info.elementAt (5));

						//find the layer in the map
						FmVoxelType voxelType = null;
						Collection voxelParticle = null;
						if ((typeName.compareTo("Bottom_INRA")==0) || (typeName.compareTo("Center_INRA")==0) || (typeName.compareTo("Top_INRA")==0) ) {
							if (!typeMap.containsKey(typeId)) {
								voxelType = new FmVoxelType (typeId, typeName);
								typeMap.put (typeId, voxelType);
							}
							else {
								voxelType = (FmVoxelType) typeMap.get (typeId);
							}
						}

						String chaine = "";
						int i=0, j=0, k=0;

						if (c == null) {
							chaine =(String) (info.elementAt (1));
							if (chaine.compareTo("null") != 0)
								 i =  Integer.parseInt (chaine);					//CELL I
							chaine =(String) (info.elementAt (2));
							if (chaine.compareTo("null") != 0)
								 j =  Integer.parseInt (chaine);					//CELL J
							chaine =(String) (info.elementAt (3));
							if (chaine.compareTo("null") != 0)
								 k =  Integer.parseInt (chaine);					//CELL K

							c = new FmDBVoxel (cellId, i, j, k, voxelType, edge);

							//add cell in the right map
							if (edge)
								edges.put (cellId, c);
							else
								voxels.put (cellId, c);
						}


						//Particles
						FmDBParticle newParticle = null;
						String particle =(String) (info.elementAt (6));
						int index3 = particle.indexOf (codeSeparator);
						if (index3 > 0) {
							String pid = particle.substring (index3+1);
							long particleId = Long.parseLong (pid);						//PARTICULE ID

							String particleName =(String) (info.elementAt (7));			//PARTICLE NAME
							String alive =(String) (info.elementAt (8));				//ALIVE or DEAD
							boolean isAlive = true;
							if (alive.compareTo("Dead") == 0) isAlive = false;

							if (particleMap.containsKey(particleId)) {
								newParticle = (FmDBParticle) particleMap.get (particleId);
							}
							else {

								newParticle = new FmDBParticle (particleId,  particleName, isAlive);
								particleMap.put (particleId, newParticle);

								//loop for reading parameter list attached to this particle
								int lg = 9;
								while  ( lg < info.size ())  {

									String parameter = (String) (info.elementAt (lg));
									int index4 = parameter.indexOf (codeSeparator);
									if (index4 > 0) {
										String parid = parameter.substring (index4+1);
										long parameterId = Long.parseLong (parid);
										String parameterName = parameter.substring (0,index4);		//PARAMETER NAME

										Double value = new Double(Double.NaN);
										String svalue =(String) (info.elementAt (lg+1));

										if ((svalue.compareTo("null") != 0) && (svalue.compareTo("NaN") != 0))
											value =  Double.parseDouble (svalue);			//VALUE

										//add fuel particles in the returned collection
										newParticle.addParameter (new FmDBParameter (parameterId, parameterName, value));
									}

									lg = lg + 2;
								}
							}
							c.addParticle (newParticle);
						}

						//add cell in the right collection
						if (edge) {
							edges.remove (cellId);
							edges.put (cellId, c);
						}
						else {
							voxels.remove (cellId);
							voxels.put (cellId, c);
						}
					}
				}

				shape.setVoxels (voxels);
				shape.setEdgeVoxels (edges);

			}
		}

		return shape;
    }


	/**
	 * REQUEST N� 34
	 * Return particles collection attached to a PLANT
	 */
	public Collection getPlantParticles (long plantId) throws Exception {

		Collection particles = new ArrayList ();

		Vector list =  getComplex (dataBaseURL+"par?numRequest=34&ID="+plantId);

		for (int cpt=0; cpt < list.size (); cpt++) {

			Vector info = (Vector)(list.elementAt(cpt));

			if (info.size () > 4)  {	//Check returned string is complete

				String particle =(String) (info.elementAt (0));
				int index2 = particle.indexOf (codeSeparator);
				if (index2 > 0) {
					String pid = particle.substring (index2+1);
					long particleId = Long.parseLong (pid);			//PARTICLE ID

					String particleName =(String) (info.elementAt (1));	//PARTICLE NAME
					String alive =(String) (info.elementAt (2));		//ALIVE or DEAD
					boolean isAlive = true;
					if (alive.compareTo("Dead") == 0) isAlive = false;

					FmDBParticle newParticle = new FmDBParticle (particleId,  particleName, isAlive);

					//loop for reading parameter list attached to this particle
					//CAUTION for skipping BIOMASS !!!
					int lg = 3;
					boolean addParticle = false;
					while  ( lg < info.size ())  {

						String parameter = (String) (info.elementAt (lg));
						int index4 = parameter.indexOf (codeSeparator);
						if (index4 > 0) {
							String parid = parameter.substring (index4+1);
							long parameterId = Long.parseLong (parid);
							String parameterName = parameter.substring (0,index4);		//PARAMETER NAME

							if (!parameterName.equals("Biomass")) {
								Double value = new Double(Double.NaN);

								String svalue =(String) (info.elementAt (lg+1));

								if ((svalue.compareTo("null") != 0) && (svalue.compareTo("NaN") != 0))
									value =  Double.parseDouble (svalue);			//VALUE

								newParticle.addParameter (new FmDBParameter (parameterId, parameterName, value));
								addParticle = true;
							}
						}

						lg = lg + 2;
					}

					if (addParticle) particles.add (newParticle);

				}
			}
		}
		return particles;
    }

	/********* PLANT SYNTHETIC DATA *******************************************/
	/**
	 * REQUEST N� 41
  	 * Return all plant list from the database
  	 * the returned LinkedHashMap  contains the collection of FiPlantSyntheticData objects
	 */
    public LinkedHashMap<Long, FmPlantSyntheticData>  getPlantSyntheticData (LinkedHashMap<Long, FmDBTeam>  teamList) throws Exception {

		LinkedHashMap<Long, FmPlantSyntheticData>  plantList = new LinkedHashMap ();

		Vector list =  getComplex (dataBaseURL+"par?numRequest=41");

		if (list != null) {
			for (int cpt=0; cpt < list.size (); cpt++) {

				Vector info = (Vector)(list.elementAt(cpt));

				Double height = 0.0;
				Double baseHeight = 0.0;
				Double diameter = 0.0;
				Double perpendicularDiameter = 0.0;
				double maxDiameterHeight = 0.0;
				Double meanBulkDensity = 0.0;
				Double lai = 0.0;
				String plantDominance = null;
				int dominance = 0;
				String teamName = null;
				FmDBTeam team = null;
				boolean checked = false;

				long shapeId = -1;
				long shapeUUId = -1;

				if (info.size () >= 10)  {	//Check returned string is complete


					//id has to be split
					String code = (String) (info.elementAt (0));
					int index = code.indexOf (codeSeparator);

					if (index > 0) {
						String sid = code.substring (index+1);
						shapeId = Long.parseLong (sid);							//SHAPE ID

						String code2 = (String) (info.elementAt (1));			//UUID
						int index2 = code2.indexOf (codeSeparator);
						if (index2 > 0) {
							String uuid = code2.substring (index2+1);
							shapeUUId = Long.parseLong (uuid);
						}

						String specieName = (String) (info.elementAt (2));			//SPECIES



						String chaine =(String) (info.elementAt (3));
						if ((chaine.compareTo("null") != 0) && (chaine.compareTo("NaN") != 0))
							 height =  Double.parseDouble (chaine);					//HEIGHT

						chaine = (String) (info.elementAt (4));
						if ((chaine.compareTo("null") != 0) && (chaine.compareTo("NaN") != 0))
							 baseHeight =  Double.parseDouble (chaine);				//BASEHEIGHT

						chaine = (String) (info.elementAt (5));
						if ((chaine.compareTo("null") != 0) && (chaine.compareTo("NaN") != 0))
							 diameter =  Double.parseDouble (chaine);				//DIAMETER

						chaine = (String) (info.elementAt (6));
						if ((chaine.compareTo("null") != 0) && (chaine.compareTo("NaN") != 0))
							 perpendicularDiameter =  Double.parseDouble (chaine);	//PERPENDICULAR DIAMETER

						chaine = (String) (info.elementAt (7));
						if ((chaine.compareTo("null") != 0) && (chaine.compareTo("NaN") != 0))
							 maxDiameterHeight =  Double.parseDouble (chaine);		//MAX DIAMETER HEIGHT

						plantDominance = (String) (info.elementAt (8));
						if (plantDominance.equals("Isolated")) dominance = FmPlantSyntheticData.ISOLATED;
						if (plantDominance.equals("Emergent")) dominance = FmPlantSyntheticData.EMERGENT;
						if (plantDominance.equals("Dominant")) dominance = FmPlantSyntheticData.DOMINANT;
						if (plantDominance.equals("Codominant")) dominance = FmPlantSyntheticData.CODOMINANT;
						if (plantDominance.equals("Intermediate")) dominance = FmPlantSyntheticData.INTERMEDIATE;
						if (plantDominance.equals("Overtopped")) dominance = FmPlantSyntheticData.OVERTOPPED;


						chaine = (String) (info.elementAt (9));
						if ((chaine.compareTo("null") != 0) && (chaine.compareTo("NaN") != 0))
							 meanBulkDensity =  Double.parseDouble (chaine);		//MEAN BULK DENSITY

						chaine = (String) (info.elementAt (10));
						if ((chaine.compareTo("null") != 0) && (chaine.compareTo("NaN") != 0))
							 lai =  Double.parseDouble (chaine);					//LAI

						if (teamList != null) {
							chaine = (String) (info.elementAt (11));
							if (chaine.compareTo("null") != 0) {
								long teamId = Long.parseLong (chaine);
								team = (FmDBTeam) getMap (teamList, teamId);			//TEAM NAME
								if (team != null) teamName = team.getTeamCode();
							}
						}

						chaine = (String) (info.elementAt (12));
						if (chaine.compareTo("true") == 0) checked = true;
						if (chaine.compareTo("false") == 0) checked = false;

						//add fuel in the result table

						// fc - 9.9.2009 - we need one single id for the shape: we consider only shapeUUId
						//~ addMap (plantList, shapeId, new FiPlantSyntheticData (shapeUUId, specieName,
						addMap (plantList, shapeUUId, new FmPlantSyntheticData (shapeUUId, specieName,
															height, baseHeight,
															diameter, perpendicularDiameter, maxDiameterHeight,
															dominance,
															meanBulkDensity, lai,
															teamName, checked));
					}

				}
			}
		}

 		return plantList;
    }
	/**
	 * REQUEST N� 42
  	 * Return all layers list from the database
  	 * the returned LinkedHashMap  contains the collection of FiLayerSyntheticDataBaseData objects
	 */
    public LinkedHashMap<Long,FmLayerSyntheticDataBaseData> getLayerSyntheticData (
			LinkedHashMap<Long,FmDBTeam>  teamList) throws Exception {

		LinkedHashMap<Long,FmLayerSyntheticDataBaseData> layerList = new LinkedHashMap<Long,FmLayerSyntheticDataBaseData> ();

		Vector list =  getComplex (dataBaseURL+"par?numRequest=42");

		if (list != null) {
			for (int cpt=0; cpt < list.size (); cpt++) {

				Vector info = (Vector)(list.elementAt(cpt));

				Double height = 0.0;
				Double baseHeight = 0.0;
				Double meanBulkDensity = 0.0;
				Double lai = 0.0;
				Double meanBulkDensityEdge = 0.0;
				Double laiEdge = 0.0;
				String teamName = null;
				FmDBTeam team = null;
				boolean checked = false;
				String plantDominance = null;
				int dominance = 0;
				long shapeId = -1;
				long shapeUUId = -1;

				if (info.size () >= 10)  {	//Check returned string is complete

					//id has to be split
					String code = (String) (info.elementAt (0));
					int index = code.indexOf (codeSeparator);

					if (index > 0) {
						String sid = code.substring (index+1);
						shapeId = Long.parseLong (sid);							//SHAPE ID

						String code2 = (String) (info.elementAt (1));			//UUID
						int index2 = code2.indexOf (codeSeparator);
						if (index2 > 0) {
							String uuid = code2.substring (index2+1);
						    shapeUUId = Long.parseLong (uuid);
						}

						String specieName = (String) (info.elementAt (2));			//SPECIES

						String chaine =(String) (info.elementAt (3));
						if ((chaine.compareTo("null") != 0) && (chaine.compareTo("NaN") != 0))
							 height =  Double.parseDouble (chaine);					//HEIGHT

						chaine = (String) (info.elementAt (4));
						if ((chaine.compareTo("null") != 0) && (chaine.compareTo("NaN") != 0))
							 baseHeight =  Double.parseDouble (chaine);				//BASEHEIGHT

						plantDominance = (String) (info.elementAt (5));
						if (plantDominance.equals("Isolated")) dominance = FmPlantSyntheticData.ISOLATED;
						if (plantDominance.equals("Emergent")) dominance = FmPlantSyntheticData.EMERGENT;
						if (plantDominance.equals("Dominant")) dominance = FmPlantSyntheticData.DOMINANT;
						if (plantDominance.equals("Codominant")) dominance = FmPlantSyntheticData.CODOMINANT;
						if (plantDominance.equals("Intermediate")) dominance = FmPlantSyntheticData.INTERMEDIATE;
						if (plantDominance.equals("Overtopped")) dominance = FmPlantSyntheticData.OVERTOPPED;						if (plantDominance.equals("Isolated")) dominance = FmLayerSyntheticDataBaseData.ISOLATED;

						chaine = (String) (info.elementAt (6));
						if ((chaine.compareTo("null") != 0) && (chaine.compareTo("NaN") != 0))
							 meanBulkDensity =  Double.parseDouble (chaine);		//MEAN BULK DENSITY

						chaine = (String) (info.elementAt (7));
						if ((chaine.compareTo("null") != 0) && (chaine.compareTo("NaN") != 0))
							 meanBulkDensityEdge =  Double.parseDouble (chaine);		//MEAN BULK DENSITY EDGE

						chaine = (String) (info.elementAt (8));
						if ((chaine.compareTo("null") != 0) && (chaine.compareTo("NaN") != 0))
							 lai =  Double.parseDouble (chaine);					//LAI

						chaine = (String) (info.elementAt (9));
						if ((chaine.compareTo("null") != 0) && (chaine.compareTo("NaN") != 0))
							 laiEdge =  Double.parseDouble (chaine);					//LAI EDGE

						if (teamList != null) {
							chaine = (String) (info.elementAt (10));
							if (chaine.compareTo("null") != 0) {
								long teamId = Long.parseLong (chaine);
								team = (FmDBTeam) getMap (teamList, teamId);			//TEAM NAME
								if (team != null) teamName = team.getTeamCode();
							}
						}

						chaine = (String) (info.elementAt (11));
						if (chaine.compareTo("true") == 0) checked = true;
						if (chaine.compareTo("false") == 0) checked = false;


						//add fuel in the result table

						// fc - 9.9.2009 - we need one single id for the shape: we consider only shapeUUId
						//~ addMap (layerList, shapeId, new FiLayerSyntheticDataBaseData (shapeUUId, specieName,
						addMap (layerList, shapeUUId, new FmLayerSyntheticDataBaseData (shapeUUId, specieName,
															height, baseHeight,
															dominance,
															meanBulkDensity, meanBulkDensityEdge,
															lai, laiEdge,
															teamName, checked));
					}

				}
			}
		}

 		return layerList;
    }


	/********* FIRETEC EXPORT ***************************************************************
	/**
	* REQUEST N� 45
	* Return fuel light crown description from the database for a given ID
	*/
	public Collection<Object> getVoxelData (Collection<Long> shapeIDList) throws Exception {


		HashMap <Long, FmLTVoxel> voxelMap  = new HashMap <Long, FmLTVoxel> ();		//map of voxels
		HashMap <Long, FmLTVoxel> edgeMap   = new HashMap <Long, FmLTVoxel> ();		//map of edges voxels
		HashMap <Long, FmLTFamilyProperty> layerAliveMap = new HashMap <Long, FmLTFamilyProperty> ();
		HashMap <Long, FmLTFamilyProperty> layerDeadMap = new HashMap <Long, FmLTFamilyProperty> ();
		FmLTFamilyProperty aliveBiomass = null;
		FmLTFamilyProperty deadBiomass = null;
		Long layerId = null;
		Boolean copy = false;


		//to store return object data
		Collection  <Object> shapeList = new ArrayList <Object> ();
		Collection  <FmLTVoxel> voxelList = new ArrayList <FmLTVoxel> ();
		Collection  <FmLTVoxel> edgeList = new ArrayList <FmLTVoxel> ();

		FmLTFamilyProperty MVR = new FmLTFamilyProperty ();
		FmLTFamilyProperty SVR = new FmLTFamilyProperty ();
		Double voxelDx = 0.0;
		Double voxelDy = 0.0;
		Double voxelDz = 0.0;

		String shapeIDS = "";
		for (Iterator i = shapeIDList.iterator(); i.hasNext ();) {
			Long id = (Long) i.next();
			shapeIDS = shapeIDS + id +";";
		}

		FmPlantVoxelData plant = null;
		FmLayerVoxelData layer = null;

		Vector list =  getComplex (dataBaseURL+"par?numRequest=45&IDS="+shapeIDS);

		for (int cpt=0; cpt < list.size (); cpt++) {

			Vector info = (Vector)(list.elementAt(cpt));

			String code = (String) (info.elementAt (0));
			int index = code.indexOf (codeSeparator);
			if (index > 0) {
				String attribute = code.substring (0, index);

				//VOXELS SIZE
				if ((attribute.compareTo("Shape") == 0) || (attribute.compareTo("ShapeLayer") == 0)) {

					//****** saving previous plant or layer*******
					// A REECRIRE, C'EST MOCHE .....
					/**********************************************/
					//convert voxel map into collection
					if (voxelMap != null) {
						for (Iterator i = voxelMap.keySet().iterator(); i.hasNext ();) {
							Object cle = i.next();
							FmLTVoxel v = (FmLTVoxel) voxelMap.get(cle);
							voxelList.add(v);
						}
					}

					//convert voxel map into collection
					if (edgeMap != null) {
						for (Iterator i = edgeMap.keySet().iterator(); i.hasNext ();) {
							Object cle = i.next();
							FmLTVoxel v = (FmLTVoxel) edgeMap.get(cle);
							edgeList.add(v);
						}
					}

					//Saving last plant or layer
					if (plant != null) {
						plant.setVoxels (voxelList);
						plant.setMVR (MVR);
						plant.setSVR (SVR);
						shapeList.add (plant);
					}
					if (layer != null) {
						layer.setCenterVoxels (voxelList);
						layer.setEdgeVoxels (edgeList);
						layer.setMVR (MVR);
						layer.setSVR (SVR);
						shapeList.add (layer);
					}
					plant = null;
					layer = null;
					voxelList.clear();
					edgeList.clear();
					voxelMap.clear();
					edgeMap.clear();

					String sid = code.substring (index+1);
					Long shapeID = Long.parseLong (sid);

					String chaine =(String) (info.elementAt (2));
					if ((chaine.compareTo("null") != 0) && (chaine.compareTo("NaN") != 0))
						 voxelDx =  Double.parseDouble (chaine);

					chaine =(String) (info.elementAt (3));
					if ((chaine.compareTo("null") != 0) && (chaine.compareTo("NaN") != 0))
						 voxelDy =  Double.parseDouble (chaine);

					chaine =(String) (info.elementAt (4));
					if ((chaine.compareTo("null") != 0) && (chaine.compareTo("NaN") != 0))
						 voxelDz =  Double.parseDouble (chaine);

					if (attribute.compareTo("Shape") == 0)
						plant = new FmPlantVoxelData (shapeID, voxelDx, voxelDy, voxelDz);

					if (attribute.compareTo("ShapeLayer") == 0)
						layer = new FmLayerVoxelData (shapeID, voxelDx, voxelDy, voxelDz);

				}


				//PARTICLES ATTACHED TO THE CELLS
				if ((attribute.compareTo("Cell") == 0) || (attribute.compareTo("CellEdge") == 0)) {

					if (info.size () >= 9)  {	//Check returned string is complete

						String cid = code.substring (index+1);
						Long cellId = Long.parseLong (cid);

						Boolean edge = false;
						if (attribute.compareTo("CellEdge") == 0) edge = true;



						//get the voxel in the collection if already listed
						FmLTVoxel voxel  = null;
						if ((edge) && (edgeMap!=null)) voxel = (FmLTVoxel) edgeMap.get (cellId);
						else if ((!edge) && (voxelMap!=null)) voxel = (FmLTVoxel) voxelMap.get (cellId);

						//else voxel creation
						if (voxel == null) {
							String chaine = "";
							int i=0, j=0, k=0;
							chaine =(String) (info.elementAt (1));
							if (chaine.compareTo("null") != 0)
								 i =  Integer.parseInt (chaine);					//CELL I
							chaine =(String) (info.elementAt (2));
							if (chaine.compareTo("null") != 0)
								 j =  Integer.parseInt (chaine);					//CELL J
							chaine =(String) (info.elementAt (3));
							if (chaine.compareTo("null") != 0)
								 k =  Integer.parseInt (chaine);					//CELL K

							voxel  = new FmLTVoxel (i, j, k);

							//add voxel in the right map
							if (edge)
								edgeMap.put (cellId, voxel);
							else {
								voxelMap.put (cellId, voxel);
							}

							//store for next cell with the same layer
							if ((layerId != null) && (layerId > 0)) {
								if (!layerAliveMap.containsKey (layerId)) {
									layerAliveMap.put (layerId, aliveBiomass);
								}
								if (!layerDeadMap.containsKey (layerId)) {
									layerDeadMap.put (layerId, deadBiomass);
								}
							}


							//layer
							copy = false;
							String layerDef =(String) (info.elementAt (4));
							int layerIndex = layerDef.indexOf (codeSeparator);
							if (layerIndex > 0) {
								String lid = layerDef.substring (layerIndex+1);
								layerId = Long.parseLong (lid);

								if (layerAliveMap.containsKey (layerId)) {
									aliveBiomass = (FmLTFamilyProperty) layerAliveMap.get (layerId);
									voxel.setAliveBiomasses (aliveBiomass);
									copy = true;

								}
								if (layerDeadMap.containsKey (layerId)) {
									deadBiomass = (FmLTFamilyProperty) layerDeadMap.get (layerId);
									voxel.setDeadBiomasses (deadBiomass);
								}
							}
							if (!copy) {
								aliveBiomass = null;
								deadBiomass = null;
							}
						}


						if (!copy)  {


							String particle =(String) (info.elementAt (6));
							int index3 = particle.indexOf (codeSeparator);
							if (index3 > 0) {
								String pid = particle.substring (index3+1);
								long particleId = Long.parseLong (pid);						//PARTICULE ID

								String particleName =(String) (info.elementAt (7));			//PARTICLE NAME
								String alive =(String) (info.elementAt (8));				//ALIVE or DEAD
								boolean isAlive = true;
								if (alive.compareTo("Dead") == 0) isAlive = false;


								//loop for reading parameter list attached to this particle
								int lg = 9;
								while  ( lg < info.size ())  {

									String parameter = (String) (info.elementAt (lg));
									int index4 = parameter.indexOf (codeSeparator);
									if (index4 > 0) {
										String parid = parameter.substring (index4+1);
										long parameterId = Long.parseLong (parid);
										String parameterName = parameter.substring (0,index4);		//PARAMETER NAME

										if (parameterName.compareTo("Biomass") == 0) {
											Double value = new Double(Double.NaN);
											String svalue =(String) (info.elementAt (lg+1));

											if ((svalue.compareTo("null") != 0) && (svalue.compareTo("NaN") != 0))
												value =  Double.parseDouble (svalue);			//BIOMASS VALUE

											//add the biomass value to the voxel particle map
											voxel.setBiomass (particleName, isAlive, value);
											if (isAlive) {
												if (aliveBiomass == null) aliveBiomass = new FmLTFamilyProperty ();
												aliveBiomass.put(particleName,  value);
											}
											else {
												if (deadBiomass == null) deadBiomass = new FmLTFamilyProperty ();
												deadBiomass.put(particleName,  value);
											}
										}
									}

									lg = lg + 2;
								}

							}
						}


						//add cell in the right collection
						if (edge) {
							edgeMap.remove (cellId);
							edgeMap.put (cellId, voxel);
						}
						else {
							voxelMap.remove (cellId);
							voxelMap.put (cellId, voxel);
						}
					}
				}


				//PARTICLES ATTACHED TO THE PLANT
				if (attribute.compareTo("Particle") == 0)  {

					if (info.size () >= 5)  {	//Check returned string is complete

						String pid = code.substring (index+1);
						long particleId = Long.parseLong (pid);							//PARTICULE ID

						String particleName =(String) (info.elementAt (1));				//PARTICLE NAME
						String alive =(String) (info.elementAt (2));					//ALIVE or DEAD
						boolean isAlive = true;
						if (alive.compareTo("Dead") == 0) isAlive = false;

						//loop for reading parameter list attached to this particle
						int lg = 3;
						while  ( lg < info.size ())  {

							String parameter = (String) (info.elementAt (lg));
							int index4 = parameter.indexOf (codeSeparator);
							if (index4 > 0) {
								String parid = parameter.substring (index4+1);
								long parameterId = Long.parseLong (parid);
								String parameterName = parameter.substring (0,index4);		//PARAMETER NAME
								if (parameterName.compareTo("MVR") == 0) {
									Double value = new Double(Double.NaN);
									String svalue =(String) (info.elementAt (lg+1));

									if ((svalue.compareTo("null") != 0) && (svalue.compareTo("NaN") != 0))
										value =  Double.parseDouble (svalue);				// MVR VALUE
										MVR.put (particleName, value);
								}

								if (parameterName.compareTo("SVR") == 0) {
									Double value = new Double(Double.NaN);
									String svalue =(String) (info.elementAt (lg+1));

									if ((svalue.compareTo("null") != 0) && (svalue.compareTo("NaN") != 0))
										value =  Double.parseDouble (svalue);				// SVR VALUE
										SVR.put (particleName, value);
								}
							}

							lg = lg + 2;
						}
					}
				}
			}
		}

		//convert voxel map into collection
		for (Iterator i = voxelMap.keySet().iterator(); i.hasNext ();) {
			Object cle = i.next();
			FmLTVoxel v = (FmLTVoxel) voxelMap.get(cle);
			voxelList.add(v);
		}

		//convert voxel map into collection
		for (Iterator i = edgeMap.keySet().iterator(); i.hasNext ();) {
			Object cle = i.next();
			FmLTVoxel v = (FmLTVoxel) edgeMap.get(cle);
			edgeList.add(v);
		}

		//Saving last plant or layer
		if (plant != null) {
			plant.setVoxels (voxelList);
			plant.setMVR (MVR);
			plant.setSVR (SVR);
			shapeList.add (plant);
		}
		if (layer != null) {
			layer.setCenterVoxels (voxelList);
			layer.setEdgeVoxels (edgeList);
			layer.setMVR (MVR);
			layer.setSVR (SVR);
			shapeList.add (layer);
		}

		return shapeList;
    }

    /********* CHECKING ***********************************************************
	/**
	* REQUEST N� 50
	* Check a shape and return rapport
	*/
	public Vector<String> rapportShape (FmDBShape _shape) throws Exception {

		long shapeID = _shape.getShapeId();
		Vector list =  getComplex (dataBaseURL+"par?numRequest=50&name=Pimont&ID="+shapeID);
		Vector<String> rapport = new Vector<String>();

		if (list != null) {
			for (int cpt=0; cpt < list.size (); cpt++) {
				Vector info = (Vector)(list.elementAt(cpt));

				String message = (String) (info.elementAt (0));
				rapport.add (message);

			}
		}
		return rapport;
	}

    /********* DECODING STRINGS ****************************************************************
	/**
	 * Execute a simle request on the database server
	 * The server return a String with each item separated by ;
	 * These items are separated and stored in a returned simple Vector
	 */
    public Vector<String> getSimple (String request) throws Exception {

		Vector<String> resultList = new Vector<String>();	//Vector returned by with all items
		Vector<String> tmpList = new Vector<String>();	//Vector returned by with all items
		String inputLine; 					//String returned by the database server
		StringTokenizer token, token2;

		URL serveur = new URL (request);
		URLConnection yc = serveur.openConnection();
		BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));


		//Remove first separator for each line
		while ((inputLine = in.readLine()) != null) {


			//Replace line break for each line
			String res = inputLine.replaceAll(lineBreak, lineSeparator);

			token = new StringTokenizer(res, lineSeparator);
			int cpt = token.countTokens();
			//store each item in the returned list
			if (cpt > 0) {
				while (token.hasMoreTokens())
					tmpList.add (token.nextToken(lineSeparator));
			}
		}

		in.close();


		//Remove second separator for each field
		for (int cpt=0; cpt < tmpList.size (); cpt++){
			token2 = new StringTokenizer((String)(tmpList.elementAt (cpt)), fieldSeparator);
			int cpt2 = token2.countTokens();
			if (cpt2 > 0) {
				while (token2.hasMoreTokens())
					resultList.add (token2.nextToken (fieldSeparator));
			}
		}

		return resultList;
    }

  	/**
  	 * Execute a request on the database server
  	 * The server return a String with each item separated by ;
  	 * These items are separated and stored in a returned list (Vector)
  	 */
      public Vector getComplex (String request) throws Exception {

  		Vector resultList = new Vector();	//Vector returned by with all items
  		Vector<String> tmpList = new Vector<String>();	//Vector returned by with all items
  		String inputLine; 					//String returned by the database server
  		StringTokenizer token, token2;

  		URL serveur = new URL (request);
  		URLConnection yc = serveur.openConnection();
  		BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));

		//Remove first separator for each line
		while ((inputLine = in.readLine()) != null) {

			String res = inputLine.replaceAll(lineBreak, lineSeparator);

			token = new StringTokenizer(res,lineSeparator);
			int cpt = token.countTokens();
			//store each item in the returned list
			if (cpt > 0) {
				while (token.hasMoreTokens())
					tmpList.add (token.nextToken(lineSeparator));
			}
		}

		in.close();

  		//Second separator for each fuel field = ;
  		for (int cpt=0; cpt < tmpList.size (); cpt++){
  			token2 = new StringTokenizer((String)(tmpList.elementAt (cpt)), fieldSeparator);
  			int cpt2 = token2.countTokens();
  			if (cpt2 > 0) {
  				Vector field = new Vector();
  				while (token2.hasMoreTokens()) {
  					field.add (token2.nextToken (fieldSeparator));
				}

  				resultList.add (field);
  			}
  		}

  		return resultList;
    }


  	/**
  	 * To add an entry key and an object in a LinkedHashMap
  	 */
	public void addMap (LinkedHashMap  map, Object key, Object object) {
		if (map == null) map = new LinkedHashMap ();
		map.put (key, object);
	}

	public Object getMap (LinkedHashMap  map, Object key) {
		if (map.containsKey(key)) return map.get (key);
		else return null;
	}




}
