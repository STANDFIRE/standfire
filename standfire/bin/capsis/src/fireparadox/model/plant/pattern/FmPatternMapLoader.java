package fireparadox.model.plant.pattern;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import jeeb.lib.util.Log;
import jeeb.lib.util.RecordSet;

/**	A format description to read a list of pattern parameter files
 *
 *	@author F. de Coligny - june 2006
 */
public class FmPatternMapLoader extends RecordSet {
	
	private String fileName;
	
	
	private TreeMap<String,String> patternMap;
	
	//True : We read the admin file
	private boolean adminFile;
	
	
	
	/**	Constructor 1: reads the given file
	 */
	public FmPatternMapLoader (String fileName, boolean adminFile) throws Exception {
		super ();
		this.fileName = fileName;		
	
		this.adminFile = adminFile;
		patternMap = new TreeMap<String, String> ();
		createRecordSet (fileName);
		interpret (patternMap);		
	}
	
	/**	Constructor 2: saves the given parameters
	 */
	public FmPatternMapLoader (TreeMap<String,String> patternMap) throws Exception {
		super ();
		
		setHeaderEnabled (false);
		createRecordSet (patternMap);
	}
	
	//	Export mode: Before saving, create the record set
	//
	private void createRecordSet (TreeMap<String,String> patternMap) throws Exception {
		
		Set<Map.Entry<String, String>> setEntry = patternMap.entrySet ();
		
		add (new CommentRecord ("FiPattern Map"));
		
		add (new EmptyRecord ());
		
		
		for (Map.Entry e: setEntry) {
			
			String criteria = ((String)e.getKey ());
			String [] criteriaTokens = FmPatternMap.parseCriteria (criteria);
			String pattern = ((String)e.getValue ());
			
			KeyRecord species = new KeyRecord ();
			species.key = "species";
			species.value = criteriaTokens[0];
			add (species);
			
			KeyRecord hMin = new KeyRecord ();
			hMin.key = "hMin";
//			if(criteriaTokens[1].equals ("")) {
//					//hMin.value = String.valueOf (- Double.MAX_VALUE);
//					hMin.value = String.valueOf (-1);
//				} else {
				hMin.value = criteriaTokens[1];
//			}			 
			add (hMin);
			
			KeyRecord hMax = new KeyRecord ();
			hMax.key = "hMax";
//			if(criteriaTokens[2].equals ("")) {
//				//hMax.value = String.valueOf (Double.MAX_VALUE);
//				hMax.value = String.valueOf (-1);
//			} else {
				hMax.value = criteriaTokens[2];
//			}
			add (hMax);
			
			KeyRecord closed = new KeyRecord ();
			closed.key = "closed";
			closed.value = criteriaTokens[3];
			add (closed);
			
			KeyRecord patternid = new KeyRecord ();
			patternid.key = "patternid";
			patternid.value = pattern;
			add (patternid);
			
			
			add (new EmptyRecord ());
			
		}
		
	}
	
	//	Import mode: Interpret
	//
	private void interpret (TreeMap<String, String> treeMap) throws Exception {
		
		String currentPatternId,currentSpecie,currentEnvironment;
		double currentHMin, currentHMax;
		currentPatternId=currentSpecie=currentEnvironment= "";
		currentHMin=-Double.MAX_VALUE;
		currentHMax=Double.MAX_VALUE;
		
		
		
		for (Iterator i = this.iterator (); i.hasNext ();) {
			Object record = i.next ();
			
			if (record instanceof KeyRecord) {
				KeyRecord r = (KeyRecord)record;
				if (r.hasKey ("species")) {
					if(currentSpecie=="") {
						try {
							currentSpecie = r.value;
						} catch (Exception e) {
							Log.println (Log.ERROR, "FiPatternMapLoader.interpret ()",
								"Trouble with specie name", e);
							throw new Exception ("Trouble with specie name : "+r.key+" in "+fileName+" near record "+record);
						}
						
					} else throw new Exception ("Specie name redefinition : "+r.key+" in "+fileName+" near record "+record);
					
				} else if (r.hasKey ("hMin")) {
					if(currentHMin==-Double.MAX_VALUE) {
						try {
							if(!r.value.equals ("NODEF"))
								currentHMin = r.getDoubleValue ();
							//currentHMin=String.valueOf (hMin);
						} catch (Exception e) {
							Log.println (Log.ERROR, "FiPatternMapLoader.interpret ()",
								"Trouble with height min", e);
							throw new Exception ("Trouble with heigth min : "+r.key+" in "+fileName+" near record "+record);
						}
					} else throw new Exception ("height min redefinition : "+r.key+" in "+fileName+" near record "+record);
					
				} else if (r.hasKey ("hMax")) {
					if(currentHMax==Double.MAX_VALUE) {
						try {
							if(!r.value.equals ("NODEF"))
								currentHMax = r.getDoubleValue ();
							//currentHMax=String.valueOf (hMax);
						} catch (Exception e) {
							Log.println (Log.ERROR, "FiPatternMapLoader.interpret ()",
								"Trouble with height max", e);
							throw new Exception ("Trouble with heigth max : "+r.key+" in "+fileName+" near record "+record);
						}
					} else throw new Exception ("height max redefinition : "+r.key+" in "+fileName+" near record "+record);
					
				}else if (r.hasKey ("closed")) {
					if(currentEnvironment=="") {
						try {
							currentEnvironment = String.valueOf (r.getIntValue ());
						} catch (Exception e) {
							Log.println (Log.ERROR, "FiPatternMapLoader.interpret ()",
								"Trouble with environment type", e);
							throw new Exception ("Trouble with environment type : "+r.key+" in "+fileName+" near record "+record);
						}
					} else throw new Exception ("environment type redefinition : "+r.key+" in "+fileName+" near record "+record);
					
				} else if (r.hasKey ("patternid")) {
					try {
						currentPatternId = String.valueOf (r.getIntValue ()); //getIntValue throw exception if the value is not an integer
					} catch (Exception e) {
						Log.println (Log.ERROR, "FiPatternMapLoader.interpret ()",
							"Trouble with pattern id", e);
						throw new Exception ("Trouble with pattern id : "+r.key+" in "+fileName+" near record "+record);
					}
					if(currentSpecie!="") {
						String currentKey = FmPatternMap.createCriteria (currentSpecie,currentHMin,currentHMax,currentEnvironment,adminFile);
						treeMap.put (currentKey,currentPatternId);
					} else throw new Exception ("Map criteria is missing for the current pattern : "+r.key+" in "+fileName+" near record "+record);
					
					currentPatternId=currentSpecie=currentEnvironment = "";
					currentHMin=-Double.MAX_VALUE;
					currentHMax=Double.MAX_VALUE;
					
				} else {
					Log.println (Log.ERROR, "FiPatternMapLoader.interpret ()",
						"Unknown key : "+r.key);
					throw new Exception ("wrong key in "+fileName+" near record "+record);
				}
				
			} else {
				Log.println (Log.ERROR, "FiPatternMapLoader.interpret ()",
					"wrong format in "+fileName+" near record "+record);
				throw new Exception ("wrong format in "+fileName+" near record "+record);
			}
			
		}
		
	}
	
	public TreeMap<String,String> getPatternMap () {return patternMap;}
	
}


