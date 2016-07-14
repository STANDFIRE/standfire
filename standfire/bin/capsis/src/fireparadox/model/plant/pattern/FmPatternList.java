package fireparadox.model.plant.pattern;

import java.io.File;
import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import capsis.kernel.PathManager;

/**
 * A map Pattern Name -> FiPattern object
 * @author S. Griffon - May 2007
 */
public class FmPatternList implements Serializable {
	
	//Admin mode boolean
	private boolean admin;
	
	private TreeMap<String,FmPattern> patternList;
	private TreeMap<String,FmPattern> adminPatternList;
	
	public static final String fileName = PathManager.getDir("bin")+File.separator+"fireparadox"+File.separator+"patterns.list";
	public static final String fileAdminName = PathManager.getDir("bin")+File.separator+"fireparadox"+File.separator+"patterns.admin.list";
	
	/** Creates a new instance of FiPatternMap */
	public FmPatternList () {
		super ();
		patternList = new TreeMap<String,FmPattern> ();
		adminPatternList = new TreeMap<String,FmPattern> ();
	}
	
	public boolean isAdmin () {
		return admin;
	}
	
	public void setAdmin (boolean admin) {
		this.admin = admin;
	}
	
	public void setAdminPatternList (TreeMap<String,FmPattern> adminPatternList) {
		this.adminPatternList = adminPatternList;
	}
	
	public Set<Map.Entry<String, FmPattern>> entrySet () {
		LinkedHashSet<SortedMap.Entry<String, FmPattern>> totalEntrySet = new LinkedHashSet<SortedMap.Entry<String, FmPattern>>  ();
		if(!adminPatternList.isEmpty ()) {
			totalEntrySet.addAll (adminPatternList.entrySet ());
		}
		totalEntrySet.addAll (patternList.entrySet ());
		return totalEntrySet;
	}
	
	public Set<String> keySet () {
		LinkedHashSet<String> totalKeySet = new LinkedHashSet<String>  ();
		if(!adminPatternList.isEmpty ()) {
			totalKeySet.addAll (adminPatternList.keySet ());
		}
		totalKeySet.addAll (patternList.keySet ());
		return totalKeySet;
	}
	
	public int size () {
		return patternList.size () + adminPatternList.size ();
	}
	
	public boolean containsValue (FmPattern value) {
		return patternList.containsValue (value) || adminPatternList.containsValue (value);
	}
	
	public FmPattern put (String key, FmPattern value) {
		if(admin) {
			return adminPatternList.put (key, value);
		} else {
			return patternList.put (key, value);
		}
	}
	
	public FmPattern get (String key) {
		if(isAdminEntry (key)) {
			return adminPatternList.get (key);
		} else {
			return patternList.get (key);
		}
	}
	
	public boolean isAdminEntry (String key) {
		return adminPatternList.keySet ().contains (key);
	}
	
	public FmPattern getPattern (String patternName)  {
		return get (patternName);
	}
	
	public void reset (FmPatternMap patternMap) throws Exception{
		
		Object [] entry = patternList.entrySet ().toArray ();
		for (Object o : entry) {
			String patternid=String.valueOf (((FmPattern)((Map.Entry)o).getValue ()).getId ());
			if( ! patternMap.containsValue (patternid) ) {
				this.removePattern ((String)((Map.Entry)o).getKey ());
			}
		}		
		save ();
	}
	
	public void addPattern (FmPattern pattern) {
		this.put (String.valueOf (pattern.getId ()),pattern);
	}
	
	public FmPattern remove (String key) throws Exception {
		if (isAdminEntry (key)) {
			if(!admin ) {
				throw new Exception (Translator.swap ("FireParadox.removeAdminEntryForbidden"));
			} else {
				return adminPatternList.remove (key);
			}
		} else {
			return patternList.remove (key);
		}
	}
	
	public boolean isModifiable (String key) {
		if(isAdminEntry (key) && !isAdmin ()) {
			return false;
		}else {
			return true;
		}
	}
	
	//return the position of the id in the keySet
	public int getIndexOf (String id) throws Exception {
		int pos = 0;		
		for (String key : this.keySet ()) {
			if (key.equals (id)) {
				return pos;
			}
			pos++;
		}
		throw new Exception ("Error getIndexOf ");
		
	}
	
	public void removePattern (String key) throws Exception{
		remove (key);
		//We set the id Fatcory of the pattern at an increment after the last id in the list.
		setNextPatternId ();
		
	}
	
	public void setNextPatternId ()  {
		Object [] entrySetTab = this.entrySet ().toArray ();
		int max = 0;
		for (Object idS : entrySetTab) {
			FmPattern currentPattern = (FmPattern)((Map.Entry)idS).getValue ();
			int id = currentPattern.getId ();
			if (id>max) max = id;
		}
		
		FmPattern.idFactory.setCurrentValue (max);
	}
//    public FiPatternList load (String fileName) {
//        this.fileName = fileName;
//        return this;
//    }
//
	
	public void load () throws Exception{
		try {
			
			File filePattern = new File (fileName);
			File fileAdminPattern = new File (fileAdminName);
			if(!filePattern.exists ()) filePattern.createNewFile ();
			if(!fileAdminPattern.exists ()) fileAdminPattern.createNewFile ();
			
			FmPatternListLoader loaderList;
			loaderList = new FmPatternListLoader (fileName);
			
			patternList.putAll (loaderList.getPatternList ());
			
			
			loaderList = new FmPatternListLoader (fileAdminName);
			this.adminPatternList.putAll (loaderList.getPatternList ());
		} catch (Exception e) {
			Log.println (Log.ERROR, "FiModel.c()", "Error during loading of the pattern list: ", e);
			throw new Exception (e.getMessage ());
		}
	}
	
	public void save () throws Exception{
		
		try {
			
			FmPatternListLoader loaderList;
			
			if(admin) {
				loaderList = new FmPatternListLoader (this.adminPatternList);
				loaderList.save (fileAdminName);
			}
			
			loaderList = new FmPatternListLoader (this.patternList);
			loaderList.save (fileName);
			
		} catch (Exception e) {
			throw e;
		}
	}
	
	
}
