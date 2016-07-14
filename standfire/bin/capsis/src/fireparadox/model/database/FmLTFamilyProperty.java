package fireparadox.model.database;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/** USED FOR NON LOCAL PLANT ONLY	
 * FiLTFamilyProperty : a Map to store the properties of the different 
*	families of particles. 
*	LT means Lite description, compared to DB, used for objects related 
*	to database edition.
*	The key is a family name (Leaves, Twigs, etc.), the related value 
*	can be a dead or alive biomass for the family (in a FiLTVoxel) or 
*	a value for MVR (MassToVolumeRatio) or SVR (SurfaceToVolumeRatio) 
*	in a FiPlant or a FiLayer. Various such maps are used to store MVR, SVR, 
*	dead or alive biomasses (the name of the map indicates what is inside).
*	@see FiPlant, FiLayer, FiLTVoxel
*	@author F. Pimont - july 2009
*/
public class FmLTFamilyProperty extends HashMap<String,Double> 
		implements Serializable {
	
	/**	Constructor
	*/
	public FmLTFamilyProperty () {
		super ();	
	}

	/**	Return the value for the given name of particle family
	*/
	public Double get (String familyName) {
		Double d = super.get(familyName); 
		if (d == null) {
			return 0d;
		}
		return d;
	}

	/**	Returns the collection of family names in this familyProperty.
	*/
	public Set<String> getFamilyNames () {
		return keySet ();
	}
	
	/**	Returns the map with everything in it as a String for test.
	*/
	@Override
	public String toString () {
		StringBuffer b = new StringBuffer ("FamilyProperty");
		for (Map.Entry entry : entrySet ()) {
			b.append (' ');
			b.append (entry.getKey ());
			b.append (' ');
			b.append (entry.getValue ());
		}
		
		return b.toString ();
	}
	
	
	/**	Test method
	*	cd capsis4/bin
	*	java -cp .:../ext/* fireparadox.model.FiLTFamilyProperty
	*/
	public final static void main (String[] args) {
		FmLTFamilyProperty biomasses = new FmLTFamilyProperty ();
		biomasses.put ("Leaves", 0.45);
		biomasses.put ("Twigs", 0.65);
		
		System.out.println (biomasses.toString ());
		
	}
	
}
