package fireparadox.model.plant.fmgeom;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.Serializable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;

import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import capsis.kernel.PathManager;
import capsis.lib.fire.fuelitem.FiPlant;
import fireparadox.model.plant.FmPlant;

/**
 * A map Criteria String -> Pattern id.
 * Criteria String = SpeciesCode_HeightMin,HeightMax_Environment
 * To add a new entry : put(criteria, patternId)
 * To get the pattern from a species code : getPattern(criteria)
 * Be careful : This is an interface to access to the pattern map as a single identity
 * BUT the map is componed of 2 maps : the client and the admin map.
 * @author S. Griffon - May 2007
 */
public class FmGeomMap implements Cloneable, Serializable {

	public static final FmGeom DEFAULT_PATTERN= new FmGeom ();

	//A list of known patterns
	private FmGeomList patternList;

	// fc - 3.6.2008 - the Sketchers must not be Serialized while saving a project -> transient
	//A list of known listener : e.g. FireSketcher
	transient private Collection<ActionListener> listeners;

	//Admin mode boolean
	private boolean admin;

	private TreeMap<String,String> patternMap;
	private TreeMap<String,String> adminPatternMap;

	public static final String fileName = PathManager.getDir("bin")+File.separator+"fireparadox"+File.separator+"patterns.map";
	public static final String fileAdminName = PathManager.getDir("bin")+File.separator+"fireparadox"+File.separator+"patterns.admin.map";

	/** Creates a new instance of FiPatternMap */
	public FmGeomMap (FmGeomList patternList) {
		super ();
		this.patternList = patternList;
		patternMap = new TreeMap<String,String> ();
		adminPatternMap = new TreeMap<String,String> ();
	}

	@Override
	public Object clone () {
		FmGeomMap o;
		try {
			o = (FmGeomMap) super.clone ();
			o.patternMap = (TreeMap<String,String>)this.patternMap.clone ();
			o.adminPatternMap = (TreeMap<String,String>)this.adminPatternMap.clone ();
			//Tips to have all the rights in the cloned map
			o.admin = true;
			return o;
		} catch (CloneNotSupportedException ex) {
			ex.printStackTrace ();
		}
		return null;
	}

	public boolean isAdmin () {
		return admin;
	}

	public void setAdmin (boolean admin) {
		this.admin = admin;
	}

	public void setAdminPatternMap (TreeMap<String,String> adminPatternMap) {
		this.adminPatternMap = adminPatternMap;
	}

	public int size () {
		return patternMap.size () + adminPatternMap.size ();
	}

	public boolean containsValue (String value) {
		return patternMap.containsValue (value) || adminPatternMap.containsValue (value);
	}

	public Set<Map.Entry<String, String>> entrySet () {
		LinkedHashSet<Map.Entry<String, String>> totalEntrySet = new LinkedHashSet<Map.Entry<String, String>>  ();
		totalEntrySet.addAll (patternMap.entrySet ());
		if(!adminPatternMap.isEmpty ()) {
			totalEntrySet.addAll (adminPatternMap.entrySet ());
		}
		return totalEntrySet;
	}

	public Set<String> keySet () {
		LinkedHashSet<String> totalKeySet = new LinkedHashSet<String>  ();
		totalKeySet.addAll (patternMap.keySet ());
		if(!adminPatternMap.isEmpty ()) {
			totalKeySet.addAll (adminPatternMap.keySet ());
		}
		return totalKeySet;
	}

	public String put (String key, String value) {
		if(admin) {
			return adminPatternMap.put (key, value);
		} else {
			return patternMap.put (key, value);
		}
	}

	public String get (String key) {
		if(isAdminEntry (key)) {
			return adminPatternMap.get (key);
		} else {
			return patternMap.get (key);
		}
	}

	public boolean isModifiable (String key) {
		if(isAdminEntry (key) && !isAdmin ()) {
			return false;
		}else {
			return true;
		}
	}
	public boolean isAdminEntry (String key) {
		return adminPatternMap.keySet ().contains (key);
	}

	public String remove (String key) throws Exception {
		if (isAdminEntry (key)) {
			if(!admin ) {
				throw new Exception (Translator.swap ("FireParadox.removeAdminEntryForbidden"));
			} else {
				return adminPatternMap.remove (key);
			}
		} else {
			return patternMap.remove (key);
		}
	}

	public void reset () throws Exception{
		patternMap.clear ();
		save ();
	}


	public FmGeom getPattern (String criteria)  {
		String patternId = this.get (criteria);
		if(patternId == null) {
			return FmGeomMap.DEFAULT_PATTERN;
		} else {
			FmGeom fPattern=patternList.get (patternId);
			if(fPattern == null) {
				return FmGeomMap.DEFAULT_PATTERN;
			} else {
				return fPattern;
			}
		}

	}

	public void addActionListeners (ActionListener l) {
		if(listeners == null) {
			listeners = new ArrayList <ActionListener> ();
		}
		listeners.add (l);

	}

	public void firePatternChanged () {
		if(listeners != null) {
			for (ActionListener a : listeners) {
				int id = 0;	//unused
				String command = "PatternChanged";
				ActionEvent e1 = new ActionEvent (this, id, command);
				a.actionPerformed (e1);
			}
		}
	}


	//return the position of the criteria in the keySet
	public int getIndexOf (String criteria) throws Exception {
		int pos = 0;

		for (String key : this.keySet ()) {
			if (key.equals (criteria)) {
				return pos;
			}
			pos++;
		}
		throw new Exception ("Error getIndexOf ");

	}

	public void checkCoherence (String selectSpecie, double heightMin, double heightMax, String selectEnvi) throws Exception {


		Object [] entry;
		if(isAdmin ()) {
			entry = adminPatternMap.entrySet ().toArray ();
		} else {
			entry = patternMap.entrySet ().toArray ();
		}

		String criteria = "";
		boolean conflict = false;
		double hMinCurrent = -Double.MAX_VALUE;
		double hMaxCurrent = Double.MAX_VALUE;

		for (Object o : entry) {
			criteria = (String)((Map.Entry)o).getKey ();
			String [] key = FmGeomMap.parseCriteria (criteria);

			if(!key[1].equals ("NODEF"))
				hMinCurrent = Double.parseDouble (key[1]);

			if(!key[2].equals ("NODEF"))
				hMaxCurrent = Double.parseDouble (key[2]);

			if (key[0].equals (selectSpecie)) {
				if (!selectEnvi.equals ("") && key[3].equals (selectEnvi)) {
					//If we are on the same specie and the same environment check if height's intervals intersect
					//First check if HeightMin < HeightMax
					if(hMinCurrent != -Double.MAX_VALUE && heightMin != -Double.MAX_VALUE) //We are on non-defined intervals
					{
						if(heightMax <= hMinCurrent || heightMin >= hMaxCurrent)	{
							conflict = false;
						} else {
							conflict = true;
							break;
						}
					}

				}
			}
		}

		if (conflict == true) {
			throw new Exception (criteria);
		}

	}


	public void load () throws Exception{
		try {
			//First if the files exist, otherwise create them
			File filePattern = new File (fileName);
			File fileAdminPattern = new File (fileAdminName);
			if(!filePattern.exists ()) filePattern.createNewFile ();
			if(!fileAdminPattern.exists ()) fileAdminPattern.createNewFile ();

			FmGeomMapLoader loaderMap;
			loaderMap = new FmGeomMapLoader (fileName, false);

			patternMap.putAll (loaderMap.getPatternMap ());


			loaderMap = new FmGeomMapLoader (fileAdminName, true);
			this.adminPatternMap.putAll (loaderMap.getPatternMap ());
		} catch (Exception e) {
			Log.println (Log.ERROR, "FiModel.c()", "Error during loading of the pattern list: ", e);
			throw new Exception (e.getMessage ());
		}
	}

	public void save () throws Exception{


		try {


			FmGeomMapLoader loaderMap;
			if(admin) {
				loaderMap = new FmGeomMapLoader (this.adminPatternMap);
				loaderMap.save (fileAdminName);
			}
			loaderMap = new FmGeomMapLoader (this.patternMap);
			loaderMap.save (fileName);

		} catch (Exception e) {
			throw e;
		}
	}

//int precisionLevel = the level of precision to find a pattern :
//			= 3 : check on the specie, the interval and the environment // the height must be in the interval and same environment
//			= 2 : check on the specie and the interval // the height must be in the interval
//			= 1 : check on the specie and the nearest interval
	// public FiPattern findPattern (String speciesSubject, double height,
	// boolean env, int precisionLevel) {
	public FmGeom findPattern(String speciesSubject, double height,
			int precisionLevel) {
		Object [] entry = this.entrySet ().toArray ();
		String criteria;
		double hMinCurrent=-Double.MAX_VALUE;
		double hMaxCurrent=Double.MAX_VALUE;
		boolean envCurrent;

		double minDifInterval = Double.MAX_VALUE; //save the minimal difference between the height given and the nearest limit interval of a criteria
		FmGeom fpTemp = null; //save the pattern of the nearest interval found


		for (Object o : entry) {
			criteria = (String)((Map.Entry)o).getKey ();
			String [] key = FmGeomMap.parseCriteria (criteria);
			if(speciesSubject.equals (key[0])) {

				if(!key[1].equals ("NODEF"))
					hMinCurrent = Double.parseDouble (key[1]);

				if(!key[2].equals ("NODEF"))
					hMaxCurrent = Double.parseDouble (key[2]);


				if (precisionLevel >= 2) {


					if (height >= hMinCurrent  && height < hMaxCurrent) {

						if(key[3].equals ("1")) {
							envCurrent=true;
						} else {
							envCurrent=false;
						}

						if (precisionLevel != 3) {
							// if(precisionLevel != 3 || env == envCurrent) {
							if(hMinCurrent != -Double.MAX_VALUE && hMaxCurrent != Double.MAX_VALUE ) {
								return getPattern (criteria);
							} else {
								fpTemp = getPattern (criteria);
							}
						}
					}
				} else {
					double difInterval=0;
					if(height > hMaxCurrent) {
						difInterval = height-hMaxCurrent ;

					} else if (height < hMinCurrent) {
						difInterval = hMinCurrent-height ;
					}
					if(minDifInterval>difInterval){
						minDifInterval=difInterval;
						fpTemp = getPattern (criteria);
					}
				}
			}
		}
		return fpTemp;
	}


	//Find always a pattern to return
	//Search a firepattern according the species, height and environment of the subject at different precision level
	//If not found for the species , search in the same "genre"
	//If not found for the "genre" , return the "resineous" or "broad leave" pattern
	public FmGeom findAlwaysPattern (FmPlant subject) {


		String speciesSubject = subject.getSpecies ().getName ();

		int level = 3;
		FmGeom foundPattern = null;

		while (level > 0 && foundPattern == null) {
			// foundPattern = findPattern (speciesSubject,subject.getHeight
			// (),subject.isClosedEnvironment (),level);
			foundPattern = findPattern(speciesSubject, subject.getHeight(),
					level);
			level--;
		}

		if(foundPattern == null) {//Search in the same genre
			String genre = subject.getSpecies ().getGenus ();
			if(genre.length () != 0)
				foundPattern = findPattern(genre, subject.getHeight(), 3);
		}

		if(foundPattern == null) {//if always null
			String trait = subject.getSpecies ().getTrait ();
			if(trait.length () != 0)
				foundPattern = findPattern(trait, subject.getHeight(), 3);
		}

		if(foundPattern == null) {//if always null
			// modified by FP to uniformize with crownProfile of plants
			foundPattern = new FmGeom (subject); // default pattern based on crownprofile of the subject
		}
		subject.setPatternName(foundPattern.getName ());
		return foundPattern;

	}

	static public String[] parseCriteria (String criteria) {
		// fc - 27.9.2007 - System.out.println ("Critere to parse : "+criteria);
		StringTokenizer st = new StringTokenizer (criteria,"_");
		String [] tabStr = new String [5];
		tabStr[0] = st.nextToken ();

		StringTokenizer stvir = new StringTokenizer (st.nextToken (),",");
		tabStr[1] = stvir.nextToken ();
		tabStr[2] = stvir.nextToken ();

		tabStr[3] = st.nextToken ();
		tabStr[4] = st.nextToken ();
		return tabStr;

	}

//	static public String[] formatCriteria (String criteria) {
//		String [] tabStr=parseCriteria(criteria);
//
//		//modif sg 27.07.07
//		if(Double.valueOf (tabStr[1]) == -Double.MAX_VALUE) {
//
//			tabStr[1] = "";
//		}
//
//		if(Double.valueOf (tabStr[2]) == Double.MAX_VALUE) {
//
//			tabStr[2] = "";
//		}
//
//		return tabStr;
//	}

	static public String[] formatCriteria (String criteria) {
		String [] tabStr=parseCriteria(criteria);

		//modif sg 27.07.07
		if(tabStr[1].equals ("NODEF")) {

			tabStr[1] = "";
		}

		if(tabStr[2].equals ("NODEF")) {

			tabStr[2] = "";
		}

		//return tabStr[0]+"_"+tabStr[1]+","+tabStr[2]+"_"+tabStr[3]+"_"+tabStr[4];
		return tabStr;
	}

	static public String createCriteria (String specie, double hMin, double hMax, String environment, boolean adminB) {
		String currentHMin = "NODEF";
		String currentHMax = "NODEF";
		if(specie.equals ("")) specie = " ";
		if(environment.equals ("")) environment = " ";
		//modif sg 27.07.07
		//if(hMin != -1)
		NumberFormat format = NumberFormat.getInstance (Locale.ENGLISH);
		format.setGroupingUsed (false);
		format.setMaximumFractionDigits (2);
		if(hMin !=  -Double.MAX_VALUE)
			currentHMin=format.format (hMin);
		if(hMax != Double.MAX_VALUE)
			currentHMax=format.format (hMax);

		return specie+"_"+currentHMin+","+currentHMax+"_"+environment+"_"+String.valueOf (adminB);
	}



}
