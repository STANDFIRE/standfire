package fireparadox.model.plant.fmgeom;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import jeeb.lib.util.Import;
import jeeb.lib.util.Log;
import jeeb.lib.util.Record;
import jeeb.lib.util.RecordSet;

/**	A format description to read a pattern map : criteria -> pattern name
 *
 *	@author S. Griffon - may 2007
 */
public class FmGeomListLoader extends RecordSet {
	private String fileName;	
	
	private TreeMap<String,FmGeom> patternList;
	
	// A description for a line in the file for a PatterbDiameterRecord
	@Import
	static public class PatternDiameterRecord extends Record {
		public PatternDiameterRecord () {super ();}
		public PatternDiameterRecord (String line) throws Exception {super (line);}
		//~ public String getSeparator () {return ";";}	// to change default "\t" separator
		public char type; // i (inferior) or s (superior)
		public double height;
		public double width;
		
	}
	
	
	/**	Constructor 1: reads the given file
	 */
	public FmGeomListLoader (String fileName) throws Exception {
		super ();
		this.fileName = fileName;
		createRecordSet (fileName);
		interpret ();
	}
	
	/**	Constructor 2: saves the given parameters
	 */
	public FmGeomListLoader (TreeMap<String,FmGeom> patternList) throws Exception {
		super ();
		this.patternList = patternList;
		setHeaderEnabled (false);
		createRecordSet (patternList);
	}
	
	//	Export mode: Before saving, create the record set
	//
	private void createRecordSet (TreeMap<String,FmGeom> patternList) throws Exception {
		
		Set<Map.Entry<String, FmGeom>> setEntry = patternList.entrySet ();
		
		add (new CommentRecord ("FiPattern List"));
		
		add (new EmptyRecord ());
		
		
		for (Map.Entry e: setEntry) {
			
			FmGeom pattern = ((FmGeom)e.getValue ());
			KeyRecord id = new KeyRecord ();
			id.key = "patternid";
			id.value = (String)e.getKey ();
			add (id);
			KeyRecord alias = new KeyRecord ();
			alias.key = "alias";
			alias.value = pattern.getAlias ();
			add (alias);
			KeyRecord hDMax = new KeyRecord ();
			hDMax.key = "hDMax";
			hDMax.value = String.valueOf (pattern.getHDMax ());
			add (hDMax);
			
			
			for (FmGeomDiameter fpd : pattern.getDiametersSuperior ()) {
				PatternDiameterRecord diameters = new PatternDiameterRecord ();
				diameters.type = 's';
				diameters.height = fpd.getHeight ();
				diameters.width = fpd.getWidth ();
				add (diameters);
			}
			
			
			for (FmGeomDiameter fpd : pattern.getDiametersInferior ()) {
				PatternDiameterRecord diameters = new PatternDiameterRecord ();
				diameters.type = 'i';
				diameters.height = fpd.getHeight ();
				diameters.width = fpd.getWidth ();
				add (diameters);
			}
			
			add (new EmptyRecord ());
			
		}
		
	}
	
	//	Import mode: Interpret
	//
	private void interpret () throws Exception {
		patternList = new TreeMap<String,FmGeom> ();
		FmGeom current = null;
		
		for (Iterator i = this.iterator (); i.hasNext ();) {
			Object record = i.next ();
			
			if (record instanceof KeyRecord) {
				KeyRecord r = (KeyRecord)record;
				if (r.hasKey ("patternid")) {
					if(current!=null) {patternList.put(String.valueOf (current.getId ()),current);}
					
					try {
						int id = r.getIntValue ();
						current = new FmGeom (id);
					} catch (Exception e) {
						Log.println (Log.ERROR, "FiPatternListLoader.interpret ()",
							"Trouble with pattern name", e);
						throw new Exception ("Trouble with pattern id : "+r.key+" in "+fileName+" near record "+record);
					}
				} else if (r.hasKey ("hDMax")) {
					try {
						double hdmax = r.getDoubleValue ();
						current.setHDMax (hdmax);
					} catch (Exception e) {
						Log.println (Log.ERROR, "FiPatternListLoader.interpret ()",
							"Trouble with height of crown diameter", e);
						throw new Exception ("Trouble with heigth of crown diameter : "+r.key+" in "+fileName+" near record "+record);
					}
				} else if (r.hasKey ("alias")){
					try {						
						current.setAlias (r.value);
					} catch (Exception e) {
						Log.println (Log.ERROR, "FiPatternListLoader.interpret ()",
							"Trouble with alias", e);
						throw new Exception ("Trouble with alias : "+r.key+" in "+fileName+" near record "+record);
					}
				} else {
					Log.println (Log.ERROR, "FiPatternListLoader.interpret ()",
						"Unknown key : "+r.key);
					throw new Exception ("wrong key in "+fileName+" near record "+record);
				}
				
			} else if (record instanceof PatternDiameterRecord) {
				PatternDiameterRecord r = (PatternDiameterRecord) record;
				if(r.type == 'i') {
					current.addDiametersInferior (new FmGeomDiameter (r.height,r.width));
				} else if (r.type == 's') {
					current.addDiametersSuperior (new FmGeomDiameter (r.height,r.width));
				} else {
					throw new Exception ("wrong format in "+fileName+" near record "+record);
				}
				
			} else {
				Log.println (Log.ERROR, "FiPatternListLoader.interpret ()",
					"wrong format in "+fileName+" near record "+record);
				throw new Exception ("wrong format in "+fileName+" near record "+record);
			}
			
		}
		if(current!=null) {patternList.put(String.valueOf (current.getId ()),current);}		
		
		
	}
	
	public TreeMap<String,FmGeom> getPatternList () {return patternList;}
	
}


