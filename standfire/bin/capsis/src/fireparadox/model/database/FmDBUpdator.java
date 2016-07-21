package fireparadox.model.database;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;


/**
 * FiDBUpdator : communication with DB4O database for updating data
 *
 * @author Isabelle LECOMTE - March 2008
 * Last update July 2009
 */
public class FmDBUpdator {

    private String lineBreak;
    private String lineSeparator;
    private String fieldSeparator;
    private String codeSeparator;

	private static FmDBUpdator instance;


	/** Singleton pattern.
	*	FiDBUpdator u = FiDBUpdator.getInstance ();
	*/
	public static FmDBUpdator getInstance () {
		if (instance == null) {instance = new FmDBUpdator ();}
		return instance;
	}

   /** Creates a new instance of FiDBUpdator */
    private FmDBUpdator () {
		lineBreak  = FmDBCommunicator.LINE_BREAK;
		lineSeparator  = FmDBCommunicator.LINE_SEPARATOR;
		fieldSeparator = FmDBCommunicator.FIELD_SEPARATOR;
		codeSeparator  = FmDBCommunicator.CODE_SEPARATOR;
    }
	/**
	 * createTeam : to add a new team in the database
	 */
	public long createTeam (FmDBTeam _team)  throws Exception  {

		long teamId = -1;
		String name = _team.getTeamCode ();

		teamId = create ("fireparadox.dbfuel.structure.context.Team");

		//If creation is done, set team name in the new object
		//by default password is the team name
		if (teamId > 0) {
			update (teamId, "setName", name, "java.lang.String");
			update (teamId, "setPW", name, "java.lang.String");
		}

		return teamId;		//id of the new team is returned
	}

	/**
	 * updateTeam : to update a team in the database
	 */
	public void updateTeam (FmDBTeam _teamBefore, FmDBTeam _teamAfter)  throws Exception  {

	   FmDBTeam teamBefore;				//the team before update
	   FmDBTeam teamAfter;				//the team after  update
	   teamBefore = _teamBefore;
	   teamAfter = _teamAfter;
	   long teamId = teamBefore.getTeamId();

		if (teamId > 0) {
			//Delete/undelete
			String delBefore = "false";
			String delAfter  = "false";
			if (teamBefore.isDeleted ()) delBefore="true";
			if (teamAfter.isDeleted ())  delAfter ="true";
			if (delBefore.compareTo(delAfter) != 0) {
				  update (teamId, "setDeleted", delAfter, "java.lang.Boolean");
			}

		   //Name update
			String nameBefore = teamBefore.getTeamCode ();
			String nameAfter = teamAfter.getTeamCode ();
			if (nameBefore.compareTo(nameAfter) != 0) {
				 update (teamId, "setName", nameAfter, "java.lang.String");
			}
		}
	}

	/**
	 * updateTeamPW : to update team password in the database
	 */
	public boolean updateTeamPW (Long teamId,  String oldPass, String newPass)  throws Exception  {

		Vector resultList;
		String result;
		if (teamId > 0) {

			resultList = doPost ("action=updatePassword&ID="+teamId+"&oldPass="+oldPass+"&newPass="+newPass);
			result = (String)(resultList.elementAt(0));

			if (result.equals("MAJ PASS OK")) return true;
			else return false;
		}
		else return false;

	}
	/**
	 * updateAdminPW : to force team password in the database by ADMIN
	 */
	public boolean updateAdminPW (Long teamId,  String adminPass, String newPass)  throws Exception  {

		Vector resultList;
		String result;
		if (teamId > 0) {

			resultList = doPost ("action=adminPassword&ID="+teamId+"&adminPass="+adminPass+"&newPass="+newPass);
			result = (String)(resultList.elementAt(0));

			if (result.equals("MAJ PASS OK")) return true;
			else return false;
		}
		else return false;

	}
	/**
	 * createPerson : to add a new person in the database
	 */
	public long createPerson (FmDBPerson _person, Long teamId)  throws Exception  {

		long personId = -1;
		String name = _person.getPersonName ();
		personId = create ("fireparadox.dbfuel.structure.context.Person");

		//If creation is done, set person name and team in the new object
		if (personId > 0) {
			update (personId, "setName", name, "java.lang.String");
			update (personId, "setTeam", teamId.toString(), "fireparadox.dbfuel.structure.context.Actor");
		}

		return personId;		//id of the new team is returned
	}

	/**
	 * updatePerson : to update a person in the database
	 */
   	public void updatePerson (FmDBPerson _personBefore, FmDBPerson _personAfter)  throws Exception  {
	   FmDBPerson personBefore;
	   FmDBPerson personAfter;
	   personBefore = _personBefore;
	   personAfter = _personAfter;
	   long personId = personBefore.getPersonId();

		//Delete/undelete
		String delBefore = "false";
		String delAfter  = "false";
		if (personBefore.isDeleted ()) delBefore="true";
		if (personAfter.isDeleted ())  delAfter ="true";
		if (delBefore.compareTo(delAfter) != 0) {
			  update (personId, "setDeleted", delAfter, "java.lang.Boolean");
		}

	   //Name update
		String nameBefore = personBefore.getPersonName ();
		String nameAfter = personAfter.getPersonName ();
		if (nameBefore.compareTo(nameAfter) != 0) {
			 if (personId > 0) update (personId, "setName", nameAfter, "java.lang.String");
		}
   }

	/**
	 * createMunicipality: to add a new Municipality in the database
	 */
	public long createMunicipality (FmDBMunicipality _mun)  throws Exception  {

	   //Name update
		long munId = -1;
		String name = _mun.getMunicipalityName ();
		munId = create ("fireparadox.dbfuel.structure.context.Municipality");

		//If creation is done, set Municipality name and country in the new object
		if (munId > 0) {
			update (munId, "setName", name, "java.lang.String");

			//municipality
			FmDBCountry country = _mun.getCountry();
			Long countryId = country.getCountryId ();
			update (munId, "setParent", countryId.toString(), "fireparadox.dbfuel.structure.context.Site");	//municipality is a kind of site
		}

		return munId;
	}
	/**
	 * updateMunicipality : to update a municipality in the database
	 */
	public void updateMunicipality (FmDBMunicipality _munBefore, FmDBMunicipality _munAfter)  throws Exception  {

	   FmDBMunicipality munBefore;				//the municipality before update
	   FmDBMunicipality munAfter;				//the municipality after  update
	   munBefore = _munBefore;
	   munAfter = _munAfter;
	   long munId = _munBefore.getMunicipalityId();

		if (munId > 0) {

			//Delete/undelete
			String delBefore = "false";
			String delAfter  = "false";
			if (_munBefore.isDeleted ()) delBefore="true";
			if (_munAfter.isDeleted ())  delAfter ="true";
			if (delBefore.compareTo(delAfter) != 0) {
				 update (munId, "setDeleted", delAfter, "java.lang.Boolean");
			}

		   //Name update
			String nameBefore = _munBefore.getMunicipalityName ();
			String nameAfter = _munAfter.getMunicipalityName ();
			if (nameBefore.compareTo(nameAfter) != 0) {
				  update (munId, "setName", nameAfter, "java.lang.String");
			}

			//country update
			FmDBCountry countryBefore = _munBefore.getCountry();
			FmDBCountry countryAfter = _munAfter.getCountry();
			if (countryBefore != countryAfter) {
				Long countryId = countryAfter.getCountryId ();
				update (munId, "setParent", countryId.toString(), "fireparadox.dbfuel.structure.context.Site");
			}
		}
	}

	/**
	 * createSite : to add a new site in the database
	 */
	public long createSite (FmDBSite _site)  throws Exception  {

		long siteId = -1;
		String name = _site.getSiteCode ();
		siteId = create ("fireparadox.dbfuel.structure.context.Site");

		//If creation is done, set site name and properties in the new object
		if (siteId > 0) {
			update (siteId, "setName", name, "java.lang.String");

			//municipality
			FmDBMunicipality municipality = _site.getMunicipality();
			Long municipalityId = municipality.getMunicipalityId ();
			update (siteId, "setParent", municipalityId.toString(), "fireparadox.dbfuel.structure.context.Site");

			//Coordinates
			double latitudeAfter = _site.getX();
			double longitudeAfter = _site.getY();
			if ((latitudeAfter > 0) || (longitudeAfter > 0))
			 	addProperty (siteId, "Coordinates", latitudeAfter+";"+longitudeAfter, "fireparadox.dbfuel.structure.context.Coordinates");

			//Altitude
			Double altitudeAfter = (Double) _site.getZ();
			if (altitudeAfter > 0) addProperty (siteId,  "Altitude", altitudeAfter.toString(), "java.lang.Double");

			//Description
			String descriptionAfter = _site.getDescription();
			if (descriptionAfter.compareTo("") != 0) addProperty (siteId,  "VegetationDescription", descriptionAfter, "java.lang.String");

			//topography
			String topographyAfter = _site.getTopography();
			if (topographyAfter.compareTo("") != 0) addProperty (siteId,  "TopographicPosition", topographyAfter, "fireparadox.dbfuel.structure.context.TopographicPosition");

			//slope description
			String slopeAfter = _site.getSlope();
			if (slopeAfter.compareTo("") != 0) addProperty (siteId,  "Slope_Description", slopeAfter, "fireparadox.dbfuel.structure.context.Slope_Description");

			//aspect description
			String aspectAfter = _site.getAspect();
			if (aspectAfter.compareTo("") != 0) addProperty (siteId,  "Aspect_Description", aspectAfter, "fireparadox.dbfuel.structure.context.Aspect_Description");

			//slope value
			Double slopeValueAfter =  (Double) _site.getSlopeValue();
			if (slopeValueAfter > 0) addProperty (siteId,  "Slope", slopeValueAfter.toString(), "java.lang.Double");

			//aspect value
			Double aspectValueAfter = (Double) _site.getAspectValue();
			if (aspectValueAfter > 0) addProperty (siteId,  "Aspect_Azimuth", aspectValueAfter.toString(), "java.lang.Double");

			//dominant taxa
			Collection dominantTaxaAfter = _site.getDominantTaxa();
			if (dominantTaxaAfter != null) {
				String taxa = "";
				for (Iterator i = dominantTaxaAfter.iterator(); i.hasNext ();) {
					String specieName = (String) i.next();
					if (specieName != null)  {
						taxa = taxa + specieName +";";
					}
				}
 				addProperty (siteId,  "DominantTaxa", taxa, "java.util.Collection");
			}
		}

		return siteId;
	}

	/**
	 * updateSite : to update a site in the database
	 */
	public void updateSite (FmDBSite _siteBefore, FmDBSite _siteAfter)  throws Exception  {

	   FmDBSite siteBefore;				//the site before update
	   FmDBSite siteAfter;				//the site after  update
	   siteBefore = _siteBefore;
	   siteAfter = _siteAfter;
	   long siteId = siteBefore.getSiteId();

		if (siteId > 0) {

			//DELETE/UNDELETE
			String delBefore = "false";
			String delAfter  = "false";
			if (siteBefore.isDeleted ()) delBefore="true";
			if (siteAfter.isDeleted ())  delAfter ="true";
			if (delBefore.compareTo(delAfter) != 0) {
				 update (siteId, "setDeleted", delAfter, "java.lang.Boolean");
			}



			//Name update
			String nameBefore = siteBefore.getSiteCode ();
			String nameAfter = siteAfter.getSiteCode ();
			if (nameBefore.compareTo(nameAfter) != 0) {
				  update (siteId, "setName", nameAfter, "java.lang.String");
			}

			//municipality update
			FmDBMunicipality municipalityBefore = siteBefore.getMunicipality ();
			FmDBMunicipality municipalityAfter = siteAfter.getMunicipality ();
			if (municipalityBefore != municipalityAfter) {
				 Long municipalityAfterId = municipalityAfter.getMunicipalityId ();
				 update (siteId, "setParent", municipalityAfterId.toString(), "fireparadox.dbfuel.structure.context.Site");
			}

			//Coordinates update
			long coordinateId = siteBefore.getCoordinatesId();
			double latitudeBefore = siteBefore.getX();
			double latitudeAfter = siteAfter.getX();
			double longitudeBefore = siteBefore.getY();
			double longitudeAfter = siteAfter.getY();
			if ((latitudeBefore != latitudeAfter) || (longitudeBefore != longitudeAfter)) {
				 if (coordinateId > 0)  {
					update (coordinateId, "set",latitudeAfter+";"+longitudeAfter, "fireparadox.dbfuel.structure.context.Coordinates");
				}
				 else if ((latitudeAfter > 0) || (longitudeAfter > 0))
					addProperty (siteId, "Coordinates", latitudeAfter+";"+longitudeAfter, "fireparadox.dbfuel.structure.context.Coordinates");
			}

			//Altitude update
			long altitudeId = siteBefore.getZId();
			double altitudeBefore = siteBefore.getZ();
			double altitudeAfter = siteAfter.getZ();
			if (altitudeBefore != altitudeAfter) {
				 if (altitudeId > 0)  {
					if (altitudeAfter > 0) update (altitudeId, "set", String.valueOf(altitudeAfter), "java.lang.Double");
					if (altitudeAfter == 0) removeProperty (siteId, altitudeId);
				}
				 else if (altitudeAfter > 0) addProperty (siteId, "Altitude", String.valueOf(altitudeAfter), "java.lang.Double");
			}

			//Description update
			long descriptionId = siteBefore.getDescriptionId();
			String descriptionBefore = siteBefore.getDescription();
			String descriptionAfter = siteAfter.getDescription();
			if (descriptionBefore.compareTo (descriptionAfter) != 0) {
				 if (descriptionId > 0) {
					 if (descriptionAfter.compareTo("") != 0) update (descriptionId, "set", descriptionAfter, "java.lang.String");
					 else removeProperty (siteId, descriptionId);
				 }
				 else if (descriptionAfter.compareTo("") != 0) addProperty (siteId,  "VegetationDescription", descriptionAfter, "java.lang.String");
			}

			//topography update
			long topographyId = siteBefore.getTopographyId();
			String topographyBefore = siteBefore.getTopography();
			String topographyAfter = siteAfter.getTopography();
			if (topographyBefore.compareTo (topographyAfter) != 0) {
				 if (topographyId > 0) {
					if (topographyAfter.compareTo("") != 0) update (topographyId, "set", topographyAfter, "fireparadox.dbfuel.structure.context.TopographicPosition");
					else removeProperty (siteId, topographyId);
				}
				else if (topographyAfter.compareTo("") != 0) addProperty (siteId,  "TopographicPosition", topographyAfter, "fireparadox.dbfuel.structure.context.TopographicPosition");
			}

			//slope description update
			long slopeId = siteBefore.getSlopeId();
			String slopeBefore = siteBefore.getSlope();
			String slopeAfter = siteAfter.getSlope();
			if (slopeBefore.compareTo (slopeAfter) != 0) {
				 if (slopeId > 0) {
					 if (slopeAfter.compareTo("") != 0)  update (slopeId, "set", slopeAfter, "fireparadox.dbfuel.structure.context.Slope_Description");
					 else removeProperty (siteId, slopeId);
				 }
				 else if (slopeAfter.compareTo("") != 0) addProperty (siteId,  "Slope_Description", slopeAfter, "fireparadox.dbfuel.structure.context.Slope_Description");
			}

			//aspect description update
			long aspectId = siteBefore.getAspectId();
			String aspectBefore = siteBefore.getAspect();
			String aspectAfter = siteAfter.getAspect();
			if (aspectBefore.compareTo (aspectAfter) != 0) {
				 if (aspectId > 0) {
					 if (aspectAfter.compareTo("") != 0) update (aspectId, "set", aspectAfter, "fireparadox.dbfuel.structure.context.Aspect_Description");
					else removeProperty (siteId, aspectId);
				 }
				 else if (aspectAfter.compareTo("") != 0) addProperty (siteId, "Aspect_Description", aspectAfter, "fireparadox.dbfuel.structure.context.Aspect_Description");
			}

			//slope value update
			long slopeValueId = siteBefore.getSlopeValueId();
			double slopeValueBefore = siteBefore.getSlopeValue();
			double slopeValueAfter  = siteAfter.getSlopeValue();
			if (slopeValueBefore != slopeValueAfter) {
				if (slopeValueId > 0)  {
					if (slopeValueAfter > 0)   update (slopeValueId, "set", String.valueOf(slopeValueAfter), "java.lang.Double");
					else removeProperty (siteId, slopeValueId);
				}
				else if (slopeValueAfter > 0) addProperty (siteId,  "Slope", String.valueOf(slopeValueAfter), "java.lang.Double");
			}

			//aspect value update
			long aspectValueId = siteBefore.getAspectValueId();
			double aspectValueBefore = siteBefore.getAspectValue();
			double aspectValueAfter = siteAfter.getAspectValue();
			if (aspectValueBefore != aspectValueAfter) {
				 if (aspectValueId > 0) {
					 if (aspectValueAfter > 0)  update (aspectValueId, "set", String.valueOf(aspectValueAfter), "java.lang.Double");
					 else removeProperty (siteId, aspectValueId);
				 }
				 else if (aspectValueAfter > 0) addProperty (siteId,  "Aspect_Azimuth", String.valueOf(aspectValueAfter), "java.lang.Double");
			}


			//dominant taxa
			long dominantTaxaId = siteBefore.getDominantTaxaId();
			Collection dominantTaxaBefore = siteBefore.getDominantTaxa();
			Collection dominantTaxaAfter = siteAfter.getDominantTaxa();
			String [] taxaBefore = new String [3];
			String [] taxaAfter  = new String [3];
			for (int j=0; j<3; j++) {
				taxaBefore [j] = "null";
				taxaAfter [j] = "null";
			}
			int j = 0;
			if (dominantTaxaBefore != null) {
				for (Iterator i = dominantTaxaBefore.iterator(); i.hasNext ();) {
					String specieName = (String) i.next();
					taxaBefore[j] = specieName;
					j++;
				}
			}
			j = 0;
			if (dominantTaxaAfter != null) {
				for (Iterator i = dominantTaxaAfter.iterator(); i.hasNext ();) {
					String specieName = (String) i.next();
					taxaAfter[j] = specieName;
					j++;
				}
			}

			//taxa have been modified
			if ((taxaBefore[0].compareTo(taxaAfter[0]) != 0) || (taxaBefore[1].compareTo(taxaAfter[1]) != 0) || (taxaBefore[2].compareTo(taxaAfter[2]) != 0)) {

				//property already exists
				if (siteBefore.getDominantTaxaId() > 0)  {

					String taxa = "";
					for (Iterator i = dominantTaxaAfter.iterator(); i.hasNext ();) {
						String specieName = (String) i.next();
						if (specieName != null)  {
							taxa = taxa + specieName +";";
						}
					}
					update (dominantTaxaId, "set", taxa, "DominantTaxa");
				}
				else {

					String taxa = "";
					for (Iterator i = dominantTaxaAfter.iterator(); i.hasNext ();) {
						String specieName = (String) i.next();
						if (specieName != null)  {
							taxa = taxa + specieName +";";
						}
					}
					addProperty (siteId,  "DominantTaxa", taxa, "java.util.Collection");
				}
			}

		}
	}

	/**
	 * manageSiteEvent : to add or update or delete a event for a site in the database
	 */
	public long manageSiteEvent (Long siteId, FmDBEvent event, boolean deleted)  throws Exception  {

		long eventId  = event.getEventId();
		String type = event.getName();
		String deb  = event.getDateStart();
		String fin  = event.getDateEnd();
		Boolean isYearly = event.isYearlyEvent();

		Vector resultList;
		String result;

		if (eventId < 0) {

			resultList = doPost ("action=createSiteEvent&ID="+siteId+"&eventType="+type+"&dateStart="+deb+"&dateEnd="+fin+"&isYearly="+isYearly);
			result = (String)(resultList.elementAt(0));

			int index = result.indexOf (codeSeparator);
			if (index > 0) {
				String ident = result.substring (0,index);
				String value = result.substring (index + 1);
				if (ident.compareTo("EVENTID") == 0)
					eventId =  Long.parseLong (value);

			}
		}
		else if (!deleted) {




			String isYearLyS = "false";
			if (isYearly)  isYearLyS ="true";

			update (eventId, "setStartDate", deb, "java.util.Date");
			update (eventId, "setEndDate", fin, "java.util.Date");
			update (eventId, "setYearlyEvent", isYearLyS, "java.lang.Boolean");


		}
		else {
			resultList = doPost ("action=deleteSiteEvent&ID="+siteId+"&eventID="+eventId);
		}
		return eventId;

	}
	/**************************PLANTS ********************************************/
	/**
	 * createPlant : to add a new plant in the database
	 */
	public long createPlant (FmDBPlant _plant, int _fuelType)  throws Exception  {


		long plantId  = -1;
		int fuelType  = _fuelType;
		String specie = _plant.getSpecie();
		long ownerId  = _plant.getTeam().getTeamId();

		Vector resultList;
		String result;


		resultList = doPost ("action=createPlant&specie="+specie+"&ownerID="+ownerId);
		result = (String)(resultList.elementAt(0));

		int index = result.indexOf (codeSeparator);
		if (index > 0) {
			String ident = result.substring (0,index);
			String value = result.substring (index + 1);
			if (ident.compareTo("PLANTID") == 0) {
				plantId =  Long.parseLong (value);


				//Origin
				String origin = _plant.getOrigin();
				if (origin.equals("Virtual"))
					update (plantId, "setVirtual", "true", "java.lang.Boolean");


				//Team and persons
				String teamString;
				FmDBTeam teamAfter = _plant.getTeam();
				long [] personAfter   = _plant.getPersonIdList();
				if (teamAfter != null) {
				 	teamString= String.valueOf(teamAfter.getTeamId());

					for (int i=0; i<3; i++) {
						if (personAfter[i] > 0) teamString = teamString + ";" + personAfter[i];
					}
					addProperty (plantId, "Actors", teamString, "fireparadox.dbfuel.structure.context.Actors");
				}

				//Site
				FmDBSite siteAfter = _plant.getSite();
				if (siteAfter != null)
					addProperty (plantId, "Site", String.valueOf(siteAfter.getSiteId()), "fireparadox.dbfuel.structure.context.Site");

				//sampling date
				String dateAfter = _plant.getSamplingDate();
				if ((dateAfter != null) && (dateAfter.compareTo("") != 0))
					addProperty (plantId, "SamplingDate", dateAfter, "java.util.Date");

				//Coordinates
				double latitudeAfter = _plant.getX();
				double longitudeAfter = _plant.getY();
				if ((latitudeAfter > 0) || (longitudeAfter > 0))
					 addProperty (plantId, "Coordinates", latitudeAfter+";"+longitudeAfter, "fireparadox.dbfuel.structure.context.Coordinates");

				//Altitude
				double altitudeAfter = _plant.getZ();
				if (altitudeAfter > 0) addProperty (plantId, "Altitude", String.valueOf(altitudeAfter), "java.lang.Double");

				//Height
				double heightAfter = _plant.getHeight();
				if (heightAfter > 0) addProperty (plantId, "Height", String.valueOf(heightAfter), "java.lang.Double");

				//Crown base Height
				double baseHeightAfter = _plant.getCrownBaseHeight();
				if (baseHeightAfter > 0) addProperty (plantId, "CBH", String.valueOf(baseHeightAfter), "java.lang.Double");

				//Crown Diameter
				double diameterAfter = _plant.getCrownDiameter();
				if (diameterAfter > 0) addProperty (plantId, "CrownDiameter", String.valueOf(diameterAfter), "java.lang.Double");

				//Crown Perpendicular Diameter
				double perDiameterAfter = _plant.getCrownPerpendicularDiameter();
				if (perDiameterAfter > 0) addProperty (plantId, "CrownDiameterPerpendicular", String.valueOf(perDiameterAfter), "java.lang.Double");

				//Crown Perpendicular Diameter
				double maxDiameterHeightAfter = _plant.getMaxDiameterHeight();
				if (maxDiameterHeightAfter > 0) addProperty (plantId, "Height_MaxCrownDiameter", String.valueOf(maxDiameterHeightAfter), "java.lang.Double");


				//total measured biomass
				double biomassAfter = _plant.getTotalMeasuredBiomass();
				if (biomassAfter > 0) addProperty (plantId, "Biomass", String.valueOf(biomassAfter), "java.lang.Double");


				//Plant status update
				String plantStatusAfter = _plant.getPlantStatus();
				if (plantStatusAfter.compareTo("") != 0) addProperty (plantId, "Dominance", plantStatusAfter, "fireparadox.dbfuel.structure._biological.PlantSocialPosition");

				//cover PC update
				double coverPcAfter = _plant.getCoverPc();
				if (coverPcAfter > 0) addProperty (plantId, "Cover_PC", String.valueOf(coverPcAfter), "java.lang.Double");

				//Openess
				String openessAfter = _plant.getOpeness();
				if (openessAfter.compareTo("") != 0) addProperty (plantId, "Openess", openessAfter, "fireparadox.dbfuel.structure.context.Openess");

				//dominant taxa
				Collection dominantTaxaAfter = _plant.getDominantTaxa();
				String taxa = "";
				if (dominantTaxaAfter != null) {
					for (Iterator i = dominantTaxaAfter.iterator(); i.hasNext ();) {
						String specieName = (String) i.next();
						if (specieName != null)  {
							taxa = taxa + specieName +";";
						}
					}
				}
				if (taxa.compareTo("") != 0) addProperty (plantId,  "OverShadowingSpecies", taxa, "java.util.Collection");

				//comment
				String commentAfter = _plant.getComment();
				if ((commentAfter != null) && (commentAfter.compareTo("") != 0))
						 addProperty (plantId, "Note", commentAfter, "java.lang.String");


				//reference
				String referenceAfter = _plant.getReference();
				//if ((referenceAfter != null) && (referenceAfter.compareTo("") != 0))
				String teamName = teamAfter.getTeamCode();
				referenceAfter = teamName+"-03_"+referenceAfter;
					 addProperty (plantId, "ID", referenceAfter, "java.lang.String");

			}

		}

		return plantId;
	}
	/**
	 * updatePlant : to update a  plant in the database
	 */
	public void updatePlant (FmDBPlant _plantBefore, FmDBPlant _plantAfter)  throws Exception  {

		FmDBPlant plantBefore;				//the fuel before update
		FmDBPlant plantAfter;					//the fuel after  update
		plantBefore = _plantBefore;
		plantAfter = _plantAfter;
		long plantId = plantBefore.getPlantId();

		if (plantId > 0) {

			//species
			String speciesBefore = plantBefore.getSpecie();
			String speciesAfter = plantAfter.getSpecie();
			if (speciesBefore.compareTo(speciesAfter) != 0)
				update (plantId, "setTaxon", speciesAfter, "fireparadox.dbfuel.structure._biological.taxon");

			//Team and persons update
			long teamBefore = -1;
			if (plantBefore.getTeam() != null)
				teamBefore = plantBefore.getTeam().getTeamId();


			long teamAfter = plantAfter.getTeam().getTeamId();
			long [] personBefore  = plantBefore.getPersonIdList();
			long [] personAfter   = plantAfter.getPersonIdList();

			//team have been modified
			if (teamBefore != teamAfter) {
				 String teamString = String.valueOf(teamAfter);
				 if (teamBefore > 0) {
					 for (int i=0; i<personAfter.length; i++)
						if (personAfter[i] > 0) teamString = teamString + ";" + personAfter[i];

					 update (plantBefore.getTeamId(), "set", teamString, "fireparadox.dbfuel.structure.context.Actors");
				 }
				 //team have been added
				 else if (teamAfter > 0) {
					 for (int i=0; i<personAfter.length; i++)
						if (personAfter[i] > 0) teamString = teamString + ";" + personAfter[i];
					 addProperty (plantId, "Actors", teamString, "fireparadox.dbfuel.structure.context.Actors");
				 }
			}
			//team are same but persons have changed
			else if ((plantBefore.getTeamId() > 0) && (teamAfter > 0)) {

				 String personString = "";
				 for (int i=0; i<personAfter.length; i++) {

					 if ((personAfter[i] != personBefore[i]) && (personAfter[i] > 0))
						personString = personString  + personAfter[i] + ";";
				 }
				 if (!personString.equals("")) {
					String teamString = String.valueOf(teamAfter);
					teamString = teamString + ";" + personString;
					update (plantBefore.getTeamId(), "set", teamString, "fireparadox.dbfuel.structure.context.Actors");
				 }

			}

			//Site update
			long siteBefore = -1;
			if (plantBefore.getSite() != null)
				siteBefore = plantBefore.getSite().getSiteId();

			long siteAfter = plantAfter.getSite().getSiteId();
			if (siteBefore != siteAfter) {
				 if (siteBefore > 0) update (plantBefore.getSiteId(), "set", String.valueOf(siteAfter), "fireparadox.dbfuel.structure.context.Site");
				 else if (siteAfter > 0)
					 addProperty (plantId, "Site", String.valueOf(siteAfter), "fireparadox.dbfuel.structure.context.Site");
			}

			//sampling date
			long dateBeforeId = plantBefore.getSamplingDateId();
			String dateBefore = plantBefore.getSamplingDate();
			String dateAfter = plantAfter.getSamplingDate();
			if (dateBefore.compareTo(dateAfter) != 0) {
				 if (dateBeforeId > 0) {
					 if (dateAfter.compareTo("") != 0) update (plantBefore.getSamplingDateId(), "set", dateAfter, "java.util.Date");
					 else removeProperty (plantId, dateBeforeId);
				}
				 else if ((dateAfter != null) && (dateAfter.compareTo("") != 0)) {
					 addProperty (plantId, "SamplingDate", dateAfter, "java.util.Date");
				 }
			}

			//Coordinates update
			long coordinateId = plantBefore.getCoordinatesId();
			double latitudeBefore = plantBefore.getX();
			double latitudeAfter = plantAfter.getX();
			double longitudeBefore = plantBefore.getY();
			double longitudeAfter = plantAfter.getY();
			if ((latitudeBefore != latitudeAfter) || (longitudeBefore != longitudeAfter)) {
				 if (coordinateId > 0)  {
					update (coordinateId, "set",latitudeAfter+";"+longitudeAfter, "fireparadox.dbfuel.structure.context.Coordinates");
				}
				 else if ((latitudeAfter > 0) || (longitudeAfter > 0))
					addProperty (plantId, "Coordinates", latitudeAfter+";"+longitudeAfter, "fireparadox.dbfuel.structure.context.Coordinates");
			}

			//Altitude update
			long altitudeId = plantBefore.getZId();
			double altitudeBefore = plantBefore.getZ();
			double altitudeAfter = plantAfter.getZ();
			if (altitudeBefore != altitudeAfter) {
				 if (altitudeId > 0)  {
					if (altitudeAfter > 0) update (altitudeId, "set", String.valueOf(altitudeAfter), "java.lang.Double");
					if (altitudeAfter == 0) removeProperty (plantId, altitudeId);
				}
				 else if (altitudeAfter > 0) addProperty (plantId, "Altitude", String.valueOf(altitudeAfter), "java.lang.Double");
			}

			//Height update
			long heightId = plantBefore.getHeightId();
			double heightBefore = plantBefore.getHeight();
			double heightAfter = plantAfter.getHeight();
			if (heightBefore != heightAfter) {
				 if (heightId > 0) {
					if (heightAfter > 0) update (heightId, "set", String.valueOf(heightAfter), "java.lang.Double");
					else removeProperty (plantId, heightId);
				}
				 else if (heightAfter > 0) addProperty (plantId, "Height", String.valueOf(heightAfter), "java.lang.Double");
			}

			//Crown base Height update
			long baseHeightId = plantBefore.getCrownBaseHeightId();
			double baseHeightBefore = plantBefore.getCrownBaseHeight();
			double baseHeightAfter = plantAfter.getCrownBaseHeight();
			if (baseHeightBefore != baseHeightAfter) {
				 if (baseHeightId > 0) {
					if (baseHeightAfter > 0) update (baseHeightId, "set", String.valueOf(baseHeightAfter), "java.lang.Double");
					else removeProperty (plantId, baseHeightId);
				}
				 else if (baseHeightAfter > 0) addProperty (plantId, "CBH", String.valueOf(baseHeightAfter), "java.lang.Double");
			}

			//Crown Diameter update
			long diameterId = plantBefore.getCrownDiameterId();
			double diameterBefore = plantBefore.getCrownDiameter();
			double diameterAfter = plantAfter.getCrownDiameter();
			if (diameterBefore != diameterAfter) {
				 if (diameterId > 0) {
					if (diameterAfter > 0) update (diameterId, "set", String.valueOf(diameterAfter), "java.lang.Double");
					else removeProperty (plantId, diameterId);
				}
				 else if (diameterAfter > 0) addProperty (plantId, "CrownDiameter", String.valueOf(diameterAfter), "java.lang.Double");
			}

			//Crown Perpendicular Diameter update
			long perDiameterId = plantBefore.getCrownPerpendicularDiameterId();
			double perDiameterBefore = plantBefore.getCrownPerpendicularDiameter();
			double perDiameterAfter = plantAfter.getCrownPerpendicularDiameter();
			if (perDiameterBefore != perDiameterAfter) {
				 if (perDiameterId > 0) {
					 if (perDiameterAfter > 0) update (perDiameterId, "set", String.valueOf(perDiameterAfter), "java.lang.Double");
					 else removeProperty (plantId, perDiameterId);
				}
				 else if (perDiameterAfter > 0) addProperty (plantId, "CrownDiameterPerpendicular", String.valueOf(perDiameterAfter), "java.lang.Double");
			}

			//Max height Diameter update
			long maxHeightId = plantBefore.getMaxDiameterHeightId();
			double maxHeightBefore = plantBefore.getMaxDiameterHeight();
			double maxHeightAfter = plantAfter.getMaxDiameterHeight();
			if (maxHeightBefore != maxHeightAfter) {
				 if (maxHeightId > 0) {
					 if (maxHeightAfter > 0) update (maxHeightId, "set", String.valueOf(maxHeightAfter), "java.lang.Double");
					 else removeProperty (plantId, maxHeightId);
				}
				 else if (maxHeightAfter > 0) addProperty (plantId, "Height_MaxCrownDiameter", String.valueOf(maxHeightAfter), "java.lang.Double");
			}

			//total measured biomass update
			long biomassId = plantBefore.getBiomassId();
			double biomassBefore = plantBefore.getTotalMeasuredBiomass();
			double biomassAfter = plantAfter.getTotalMeasuredBiomass();
			if (biomassBefore != biomassAfter) {
				 if (biomassId > 0) {
					 if (biomassAfter > 0) update (biomassId, "set", String.valueOf(biomassAfter), "java.lang.Double");
					 else removeProperty (plantId, biomassId);
				}
				 else if (biomassAfter > 0) addProperty (plantId, "Biomass", String.valueOf(biomassAfter), "java.lang.Double");
			}


			//Plant status update
			long plantStatusId = plantBefore.getPlantStatusId();
			String plantStatusBefore = plantBefore.getPlantStatus();
			String plantStatusAfter = plantAfter.getPlantStatus();
			if (plantStatusBefore.compareTo(plantStatusAfter) != 0) {
				 if (plantStatusId > 0)  {
					if (plantStatusAfter.compareTo("") != 0) update (plantStatusId, "set", plantStatusAfter, "fireparadox.dbfuel.structure._biological.PlantSocialPosition");
					else removeProperty (plantId, plantStatusId);
				}
				 else if (plantStatusAfter.compareTo("") != 0) addProperty (plantId, "Dominance", plantStatusAfter, "fireparadox.dbfuel.structure._biological.PlantSocialPosition");
			}
			//cover PC update
			long coverPcId = plantBefore.getCoverPcId();
			double coverPcBefore = plantBefore.getCoverPc();
			double coverPcAfter = plantAfter.getCoverPc();
			if (coverPcBefore != coverPcAfter) {
				 if (coverPcId > 0) {
					  if (coverPcAfter > 0)  update (coverPcId, "set", String.valueOf(coverPcAfter), "java.lang.Double");
					  else removeProperty (plantId, coverPcId);
				 }
				 else if (coverPcAfter > 0) addProperty (plantId, "Cover_PC", String.valueOf(coverPcAfter), "java.lang.Double");
			}

			//Openess
			long openessId = plantBefore.getOpenessId();
			String openessBefore = plantBefore.getOpeness();
			String openessAfter = plantAfter.getOpeness();
			if (openessBefore.compareTo(openessAfter) != 0) {
				 if (openessId > 0) {
					  if (openessAfter.compareTo("") != 0) update (openessId, "set", openessAfter, "fireparadox.dbfuel.structure.context.Openess");
					  else removeProperty (plantId, openessId);
				 }
				 else if (openessAfter.compareTo("") != 0) addProperty (plantId, "Openess", openessAfter, "fireparadox.dbfuel.structure.context.Openess");
			}

			//dominant taxa
			long dominantTaxaId = plantBefore.getDominantTaxaId();
			Collection dominantTaxaBefore = plantBefore.getDominantTaxa();
			Collection dominantTaxaAfter = plantAfter.getDominantTaxa();
			String [] taxaBefore = new String [2];
			String [] taxaAfter  = new String [2];
			for (int j=0; j<2; j++) {
				taxaBefore [j] = "null";
				taxaAfter [j] = "null";
			}
			int j = 0;
			if (dominantTaxaBefore != null) {
				for (Iterator i = dominantTaxaBefore.iterator(); i.hasNext ();) {
					String specieName = (String) i.next();
					taxaBefore[j] = specieName;
					j++;
				}
			}
			j = 0;
			if (dominantTaxaAfter != null) {
				for (Iterator i = dominantTaxaAfter.iterator(); i.hasNext ();) {
					String specieName = (String) i.next();
					taxaAfter[j] = specieName;
					j++;
				}
			}

			//taxa have been modified
			if ((taxaBefore[0].compareTo(taxaAfter[0]) != 0) || (taxaBefore[1].compareTo(taxaAfter[1]) != 0)) {
				//property already exists
				if (plantBefore.getDominantTaxaId() > 0)  {

					String taxa = "";
					for (Iterator i = dominantTaxaAfter.iterator(); i.hasNext ();) {
						String specieName = (String) i.next();
						if (specieName != null)  {
							taxa = taxa + specieName +";";
						}
					}
					update (dominantTaxaId, "set", taxa, "OverShadowingSpecies");
				}
				else {

					String taxa = "";
					for (Iterator i = dominantTaxaAfter.iterator(); i.hasNext ();) {
						String specieName = (String) i.next();
						if (specieName != null)  {
							taxa = taxa + specieName +";";
						}
					}
					addProperty (plantId,  "OverShadowingSpecies", taxa, "java.util.Collection");
				}
			}

			//comment
			Long commentId = plantBefore.getCommentId();
			String commentBefore = plantBefore.getComment();
			String commentAfter = plantAfter.getComment();

			if (commentBefore.compareTo(commentAfter) != 0) {
				 if (commentId > 0) {
					if ((commentAfter != null) && (commentAfter.compareTo("") != 0))
						update (commentId, "set", commentAfter, "java.lang.String");
					else removeProperty (plantId, commentId);
				 }
				 else if ((commentAfter != null) && (commentAfter.compareTo("") != 0))
					 addProperty (plantId, "Note", commentAfter, "java.lang.String");
			}

			//reference
			Long referenceId = plantBefore.getReferenceId();
			String referenceBefore = plantBefore.getReference();
			String referenceAfter = plantAfter.getReference();

			if (referenceBefore.compareTo(referenceAfter) != 0) {

				 if (referenceId > 0) {
					if ((referenceAfter != null) && (referenceAfter.compareTo("") != 0))
						update (referenceId, "set", referenceAfter, "java.lang.String");
					else removeProperty (plantId, referenceId);
				 }
				 else if ((referenceAfter != null) && (referenceAfter.compareTo("") != 0))
				 System.out.println("addProperty="+referenceId+" referenceAfter="+referenceAfter);
					 addProperty (plantId, "ID", referenceAfter, "java.lang.String");
			}


		}

	}
	/**
	 * deletePlant : to desactivate a  plant in the database
	 */
	public void desactivatePlant (FmDBPlant _plant)  throws Exception  {

		long plantId = _plant.getPlantId();
		Vector resultList;
		String result;

		if (plantId > 0) {

			resultList = doPost ("action=desactivatePlant&plantID="+plantId);

		}
	}
	/**
	 * reActivatePlant : to reactivate a  plant in the database
	 */
	public void reActivatePlant (FmDBPlant _plant)  throws Exception  {

		long plantId = _plant.getPlantId();
		Vector resultList;
		String result;

		if (plantId > 0) {

			 update (plantId, "setDeleted", "false", "java.lang.Boolean");

		}
	}
	/**
	 * validatePlant : to validate a plant in the database
	 */
	public void validatePlant (FmDBPlant _plant)  throws Exception  {

		long plantId = _plant.getPlantId();
		Vector resultList;

		if (plantId > 0) {
			update (plantId, "setValidated", "true", "java.lang.Boolean");
		}

	}
	/**
	 * unValidatePlant : to validate a plant in the database
	 */
	public void unValidatePlant (FmDBPlant _plant)  throws Exception  {

		long plantId = _plant.getPlantId();
		Vector resultList;

		if (plantId > 0) {
			update (plantId, "setValidated", "false", "java.lang.Boolean");
		}

	}
	/************************** SHAPES ********************************************/

	/**
	 * createShape : to add a new shape for a plant in the database
	 */
	public long createShape (FmDBShape _shape)  throws Exception  {


		long shapeId = -1;
		long plantId = _shape.getPlant().getPlantId();
		String kind = _shape.getShapeKind();
		int type = _shape.getFuelType();
		double xSize = _shape.getVoxelXSize();
		double ySize = _shape.getVoxelYSize();
		double zSize = _shape.getVoxelZSize();
		String teamName = _shape.getPlant().getTeam().getTeamCode();


		Vector resultList;
		String result;

		resultList = doPost ("action=createShape&plantID="+plantId+"&shapeKind="+kind+"&shapeTyp="+type+"&xSize="+xSize+"&ySize="+ySize+"&zSize="+zSize);
		result = (String)(resultList.elementAt(0));

		int index = result.indexOf (codeSeparator);
		if (index > 0) {
			String ident = result.substring (0,index);
			String value = result.substring (index + 1);
			if (ident.compareTo("SHAPEID") == 0) {
				shapeId =  Long.parseLong (value);
				if (shapeId > 0) {

					//Origin
					String origin = _shape.getOrigin();
					if (origin.equals("Virtual"))
						update (shapeId, "setVirtual", "true", "java.lang.Boolean");

					if (type == 2) {
						Double widthMin = (Double) _shape.getLayerWidthMin();
						Double widthMax = (Double) _shape.getLayerWidthMax();
						update (shapeId, "setWidth_Min", widthMin.toString(), "java.lang.Double");
						update (shapeId, "setWidth_Max", widthMax.toString(), "java.lang.Double");

					}


					//temporary reference
					String reference = teamName+"-03_"+shapeId;
					addProperty (shapeId, "ID", reference, "java.lang.String");

				}

			}
		}

		return shapeId;
	}
	/**
	 * copyShape : to copy a shape for a plant in the database
	 */
	public long copyShape (long _plantId, FmDBShape _shape, String origin)  throws Exception  {


		long newShapeId = -1;
		long plantId = _plantId;
		long shapeId = _shape.getShapeId();


		Vector<String> resultList;

		resultList = doPost ("action=copyShape&plantID="+plantId+"&shapeID="+shapeId+"&origin="+origin);
		for (String result : resultList) {

			int index = result.indexOf (codeSeparator);
			if (index > 0) {
				String ident = result.substring (0,index);
				String value = result.substring (index + 1);
				if (ident.compareTo("SHAPEID") == 0) {
					newShapeId =  Long.parseLong (value);
				}
			}
		}

		return newShapeId;
	}
	/*
     * deleteShape : to delete a  shape in the database
	 */
	public void deleteShape (FmDBShape _shapeBefore, FmDBShape _shapeAfter)  throws Exception  {

		FmDBShape shapeBefore;				//the fuel before update
		FmDBShape shapeAfter;					//the fuel after  update
		shapeBefore = _shapeBefore;
		shapeAfter = _shapeAfter;
		long plantId = shapeBefore.getPlant().getPlantId();
		long shapeId = shapeBefore.getShapeId();

		Vector resultList;
		String result;

		if (shapeId > 0) {

			//DELETE/UNDELETE
			String delBefore = "false";
			String delAfter  = "false";
			if (shapeBefore.isDeleted ()) delBefore="true";
			if (shapeAfter.isDeleted ())  delAfter ="true";

			if (delBefore.compareTo(delAfter) != 0) {
				 update (shapeId, "setDeleted", delAfter, "java.lang.Boolean");
			}

		}

	}
	/**
	 * validateShape : to validate a shape in the database
	 */
	public void validateShape (FmDBShape _shape)  throws Exception  {

		long shapeId = _shape.getShapeId();
		Vector resultList;

		if (shapeId > 0) {
 			update (shapeId, "setValidated", "true", "java.lang.Boolean");
		}

	}
	/**
	 * unValidateShape : to unvalidate a shape in the database
	 */
	public void unValidateShape (FmDBShape _shape)  throws Exception  {

		long shapeId = _shape.getShapeId();
		Vector resultList;

		if (shapeId > 0) {
 			update (shapeId, "setValidated", "false", "java.lang.Boolean");
		}

	}
	/********************************* LAYERS ***************************************/
	/**
	 * createLayer : to create a new layer in the database
	 */
	public long createLayer (FmDBPlant _plant, long fuelId, String layerName)  throws Exception  {

		Vector resultList = null;
		long layerId = -1;
		long ownerId  = _plant.getTeam().getTeamId();


		resultList = doPost ("action=createLayer&value="+layerName+"&ownerID="+ownerId);
		String result = (String)(resultList.elementAt(0));

		int index = result.indexOf (codeSeparator);
		if (index > 0) {
			String ident = result.substring (0,index);
			String value = result.substring (index + 1);
			if (ident.compareTo("ID") == 0) {
				layerId =  Long.parseLong (value);

			}
		}

		return layerId;
	}
	/**
	 * updateLayer : update a cell layer in the database when this cell is the only one for this layer
	 */
	public void updateLayer (long layerId, String layer)  throws Exception  {

		if (layerId > 0) update (layerId, "setLayer", layer, "fireparadox.dbfuel.structure._functional.FLayer");

	}
	/********************************* CELLS ***************************************/
	/**
	 * createCell : to add a new cell for a new layer in the database
	 *              the first biomass and particle is filled
	 */
	public long createCell (long fuelId, FmDBVoxel newCell, String particleName, Double biomass, String state)  throws Exception  {

		Vector resultList = null;
		long cellId = -1;

	    //Coordinates
		int xi = newCell.getI();
		int xj = newCell.getJ();
		int xk = newCell.getK();


		//layer
		long layerId = -1;
		FmVoxelType voxelType = newCell.getVoxelType();
		if (voxelType != null) {
			 layerId = newCell.getVoxelType().getDBId();
		}


		//cell or edge ?
		String cellType = "cell";
		if (newCell.isEdge()) cellType = "edge";

		resultList = doPost ("action=createCell&shapeID="+fuelId+"&cellType="+cellType+
								"&cellI="+xi+"&cellJ="+xj+"&cellK="+xk+"&layerID="+layerId+
								"&particleName="+particleName+"&value="+biomass+"&particleState="+state);

		String result = (String)(resultList.elementAt(0));


		int index = result.indexOf (codeSeparator);
		if (index > 0) {
			String ident = result.substring (0,index);
			String value = result.substring (index+1);
			if (ident.compareTo("ID") == 0) {
				cellId =  Long.parseLong (value);
			}
		}

		return cellId;
	}
	/**
	 * updateCell : modify a cell in the database with same data of a another cell
	 */
	public void updateCell (FmDBVoxel newCell, FmDBVoxel refCell)  throws Exception  {

		Vector resultList = null;

		long cellId = newCell.getDBId();
		long refCellId = refCell.getDBId();

		resultList = doPost ("action=updateCell&ID="+cellId+"&cellID="+refCellId);

//		String result = (String)(resultList.elementAt(0));

	}

	/**
	 * copyCell : to create a new cell for a existing layer in the database
	 */
	public long copyCell (long fuelId, FmDBVoxel newCell, FmDBVoxel layerCell)  throws Exception  {

		Vector resultList = null;
		long cellId = -1;

	    //Coordinates
		int xi = newCell.getI();
		int xj = newCell.getJ();
		int xk = newCell.getK();

		long layerCellId = layerCell.getDBId();

		//cell or edge ?
		String cellType = "cell";
		if (newCell.isEdge()) cellType = "edge";


		resultList = doPost ("action=copyCell&shapeID="+fuelId+"&cellType="+cellType+"&cellI="+xi+"&cellJ="+xj+"&cellK="+xk+"&cellID="+layerCellId);

		String result = (String)(resultList.elementAt(0));

		int index = result.indexOf (codeSeparator);
		if (index > 0) {
			String ident = result.substring (0,index);
			String value = result.substring (index+1);
			if (ident.compareTo("ID") == 0) {
				cellId =  Long.parseLong (value);
			}
		}

		return cellId;
	}


	/**
	 * updateCell : update a cell layer in the database
	 */
	public void deleteCell (long fuelId, FmDBVoxel cell)  throws Exception  {

		Vector resultList = null;
		long cellId = cell.getDBId();

		//cell or edge ?
		String cellType = "cell";
		if (cell.isEdge()) cellType = "edge";

		if ((fuelId > 0) && (cellId > 0))
			resultList = doPost ("action=deleteCell&shapeID="+fuelId+"&cellID="+cellId+"&cellType="+cellType);

	}
	/********************************* CELLS ***************************************/
	/**
	 * createCellComplexe : to add a new cell WITHOUT LAYER in the database
	 *              the biomass and particle LIST is filled
	 */
	public long createCellComplexe (long fuelId, FmDBVoxel newCell, String[] particleName, double[] alive, double[] dead)  throws Exception  {

		Vector resultList = null;
		long cellId = -1;

	    //Coordinates
		int xi = newCell.getI();
		int xj = newCell.getJ();
		int xk = newCell.getK();

		String particleList = "";
		String aliveList = "";
		String deadList = "";

		for (int i=0; i<particleName.length; i++) {
			if ((particleName[i] != null)  && (particleName[i] != "")) {
				particleList = particleList + particleName[i]  + ";";
				aliveList = aliveList + alive[i] + ";";
				deadList = deadList + dead[i] + ";";
			}
		}



		//cell or edge ?
		String cellType = "cell";
		if (newCell.isEdge()) cellType = "edge";

		resultList = doPost ("action=createCellComplexe&shapeID="+fuelId+"&cellType="+cellType+
								"&cellI="+xi+"&cellJ="+xj+"&cellK="+xk+
								"&particleList="+particleList+"&aliveList="+aliveList+"&deadList="+deadList);

		String result = (String)(resultList.elementAt(0));


		int index = result.indexOf (codeSeparator);
		if (index > 0) {
			String ident = result.substring (0,index);
			String value = result.substring (index+1);
			if (ident.compareTo("ID") == 0) {
				cellId =  Long.parseLong (value);
			}
		}

		return cellId;
	}
	/**
	 * addCellParticle : to add a new particle to a cell  in the database
	 */
	public long addCellParticle (long fuelId, long cellId, String particleName, Double biomass, String state)  throws Exception  {

		Vector resultList = null;

		long particleId = -1;

		resultList = doPost ("action=addCellParticle&shapeID="+fuelId+"&cellID="+cellId+
								"&particleName="+particleName+"&value="+biomass+"&particleState="+state);

		String result = (String)(resultList.elementAt(0));

		int index = result.indexOf (codeSeparator);
		if (index > 0) {
			String ident = result.substring (0,index);
			String value = result.substring (index+1);
			if (ident.compareTo("ID") == 0) {
				particleId =  Long.parseLong (value);

			}
		}

		return particleId;
	}
	/**
	 * copyCellParticle : to add a new particle to a cell  in the database
	 */
	public void copyCellParticle (long fuelId, long cellId,  long particleId)  throws Exception  {

		Vector resultList = null;

		resultList = doPost ("action=copyCellParticle&shapeID="+fuelId+"&cellID="+cellId+
								"&particleID="+particleId);

	}

	/**
	 * deleteCellParticle : to remove a particle for a cell in the database
	 */
	public void deleteCellParticle (long cellId, long particleId)  throws Exception  {

		if (particleId > 0) {
			Vector resultList = doPost ("action=deleteCellParticle&cellID="+cellId+"&particleID="+particleId);
		}

	}
	/**
	 * createPlantParticle : to add a new particle for a plant in the database
	 */
	public long createPlantParticle (long plantId, String particleName, String state)  throws Exception  {

	   //Name update
		long particleId = -1;
		long returnId = 0;

		Vector resultList = doPost ("action=createPlantParticle&plantID="+plantId+"&particleName="+particleName+"&particleState="+state);

		String result = (String)(resultList.elementAt(0));

		int index = result.indexOf (codeSeparator);
		if (index > 0) {
			String ident = result.substring (0,index);
			String val = result.substring (index+1);
			if (ident.compareTo("ID") == 0) {
				particleId =  Long.parseLong (val);
			}
		}
		return particleId;
	}

	/**
	 * copyPlantParticle : to copy particle from a plant to another in the database
	 * NA MARCHE PAS !!!
	 */
	public void copyPlantParticle (long plantId, long copyId)  throws Exception  {

	   //Name update

		Vector resultList = doPost ("action=copyPlantParticle&plantID="+plantId+"&copyID="+copyId);


	}
	/**
	 * addPlantParticle : to add a new parameter to a particle in the database
	 */
	public long addParticleParameter (long particleId,  String parameterName, String value)  throws Exception  {

		long paramId = -1;
		Vector resultList = doPost ("action=addParticleParameter&particleID="+particleId+"&paramName="+parameterName+"&value="+value);

		String result = (String)(resultList.elementAt(0));

		int index = result.indexOf (codeSeparator);
		if (index > 0) {
			String ident = result.substring (0,index);
			String val = result.substring (index+1);
			if (ident.compareTo("ID") == 0) {
				paramId =  Long.parseLong (val);
			}
		}
		return paramId;
	}
	/**
	 * deletePlantParticle : to remove a particle for a cell in the database
	 */
	public void deletePlantParticle (long fuelId, long particleId)  throws Exception  {

		if (particleId > 0) {
			Vector resultList = doPost ("action=deletePlantParticle&shapeID="+fuelId+"&particleID="+particleId);
		}

	}

	/**
	 * updateParticle : update a  particle layer in the database
	 */
	public void updateParticle (long particleId, String layer)  throws Exception  {

		if (particleId > 0) update (particleId, "setLayer", layer, "fireparadox.dbfuel.structure._functional.FLayer");

	}

	/**
	 * updateParameter : update a  parameter value for a particle in the database
	 */
	public void updateParameter (long parameterId, double value)  throws Exception  {

		if (parameterId > 0) update (parameterId, "set", String.valueOf(value), "java.lang.Double");
	}


	/**
	 * create : invoke a request of creation in the database
	 */
	public long create (String createType)  throws Exception  {
		long returnId = 0;
		Vector resultList = doPost ("action=create&valueType="+createType);

		String result = (String)(resultList.elementAt(0));

		int index = result.indexOf (codeSeparator);
		if (index > 0) {
			String ident = result.substring (0,index);
			String value = result.substring (index+1);
			if (ident.compareTo("ID") == 0) returnId =  Long.parseLong (value);
		}
		return returnId;
    }


	/**
	 * create : invoke a request of updating in the database
	 */
	public void update (long id, String method, String value, String type)  throws Exception  {

		Vector resultList = doPost ("action=update&ID="+id+"&method="+method+"&value="+value+"&valueType="+type);

	}

	/**
	 * addProperty : invoke a request for adding a new property in the database
	 */
   	public void addProperty (long id, String propertyType, String value, String type)  throws Exception  {


        Vector resultList = doPost ("action=addProperty&ID="+id+"&propertyType="+propertyType+"&value="+value+"&valueType="+type);

    }

 	/**
 	 * removeProperty : invoke a request for removing a  property in the database
 	 */
    	public void removeProperty (long id, long propertyId)  throws Exception  {

        Vector result = doPost ("action=removeProperty&ID="+id+"&propertyID="+propertyId);
    }
	/**
	 * doPost : send a HTTP REQUEST to the server in POST mode
	 */
	public Vector doPost (String str) throws Exception {

  		Vector resultList = new Vector();	//Vector returned by with all items
  		String inputLine; 					//String returned by the database server
  		StringTokenizer token;


		URL url = new URL (FmDBCommunicator.getDataBaseURL ()+"update");
		URLConnection connection = url.openConnection();
		connection.setDoOutput(true);

		OutputStreamWriter out = new OutputStreamWriter(
									 connection.getOutputStream());
		out.write(str);
		out.close();

		BufferedReader in = new BufferedReader(
					new InputStreamReader(
					connection.getInputStream()));

		String decodedString;

		//Remove first separator for each line
		while ((inputLine = in.readLine()) != null) {

			String res = inputLine.replaceAll(lineBreak, lineSeparator);

			token = new StringTokenizer(res, lineSeparator);
			int cpt = token.countTokens();
			//store each item in the returned list
			if (cpt > 0) {
				while (token.hasMoreTokens())
					resultList.add (token.nextToken(lineSeparator));
			}
		}
		in.close();
		return resultList;
    }



}
