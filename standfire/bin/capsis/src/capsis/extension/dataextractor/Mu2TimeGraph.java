/*
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 *
 * Copyright (C) 2000-2011  Francois de Coligny
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package capsis.extension.dataextractor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import jeeb.lib.util.AmapTools;
import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import capsis.defaulttype.MultipartScene;
import capsis.defaulttype.ScenePart;
import capsis.defaulttype.ScenePartMap;
import capsis.extension.PaleoDataExtractor;
import capsis.extension.dataextractor.format.DFCurves;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.Step;
import capsis.kernel.extensiontype.GenericExtensionStarter;

/**	Evolution of some value-s- over Time in a MultipartScene.
 * 	
 * 	This graph draws -several values- for each ScenePart (e.g. ddom, dg), 
 * 	possibly per hectare.
 * 	
 * 	This class must be subclassed for each needed graph.
 * 	
 *	@author F. de Coligny - february 2011
 */
public abstract class Mu2TimeGraph extends PaleoDataExtractor implements DFCurves {

	static {
		Translator.addBundle("capsis.extension.dataextractor.Mu2TimeGraph");
	}
	
	private List<List<? extends Number>> curves;
	private List<List<String>> labels;

	
	
	/**	Default constructor.
	 */
	public Mu2TimeGraph () {}

	/**	Constructor.
	 */
	public Mu2TimeGraph (GenericExtensionStarter s) {
		super (s);
		try {
			curves = new ArrayList<List<? extends Number>> ();
			labels = new ArrayList<List<String>> ();

		} catch (Exception e) {
			Log.println (Log.ERROR, "Mu2TimeGraph.c ()", "Exception in constructor: ", e);
		}
	}

	/**	Extension dynamic compatibility mechanism.
	 *	This method checks if the extension can deal (i.e. is compatible) with the referent.
	 */
	public boolean matchWith (Object referent) {
		try {
			if (!(referent instanceof GModel)) {return false;}
			GModel model = (GModel) referent;
			
			Step root = model.getProject ().getRoot ();
			GScene scene = root.getScene ();
			
			// MultipartScenes only
			if (!(scene instanceof MultipartScene)) {return false;}
			
			// Check the parts compatibility
			MultipartScene ms = (MultipartScene) scene;
			for (ScenePart part : ms.getParts ()) {
				if (!matchWithPart (model, (MultipartScene) scene, part)) {return false;}
			}
			return true;
			
		} catch (Exception e) {
			Log.println (Log.ERROR, "Mu2TimeGraph.matchWith ()", "Error in matchWith () (returned false)", e);
			return false;
		}
	}

	
	/**	This method is called by superclass DataExtractor.
	 */
	public void setConfigProperties () {
		// Choose configuration properties
		
		if (acceptsPerHectare ()) addBooleanProperty ("Mu2TimeGraph.perHectare", true);
		if (acceptsIncrements ()) addBooleanProperty ("Mu2TimeGraph.showIncrements", false);
		
		String[] possibleItems = new String[0];
		String[] selectedItems = new String[0];
		addSetProperty ("Parts", possibleItems, selectedItems);
		
		for (String name : getNames ()) {
			addBooleanProperty (makePropName (name), true);  // e.g. Wb -> Mu2TimeCrobasBiomass.Wb
		}
		
	}
	
	/**	
	 */
	private String makePropName (String name) {  // e.g. Wb
		return getClass ().getSimpleName () + "." + name;  // e.g. Mu2TimeCrobasBiomass.Wb
	}
	
	/**	Search the name of the property with the given index in getNames ()
	 */
	private String searchPropName (int index) {  // e.g. 0
		int i = 0;
		for (String n : getNames ()) {
			if (i == index) return n;
			i++;
		}
		return "";  // not found
	}
	
	/**	From DataExtractor SuperClass.
	*
	*	Computes the data series. This is the real output building.
	*	It needs a particular Step.
	*
	*	Returns false if trouble while extracting.
	*/
	public boolean doExtraction () {

		if (upToDate) {return true;}
		if (step == null) {return false;}

		try {
			
			// Retrieve Steps from root to this step
			Vector<Step> steps = step.getProject ().getStepsFromRoot (step);
			
			GModel model = step.getProject ().getModel ();
			
			MultipartScene scene0 = (MultipartScene) step.getScene ();
	
			// To show only the parts selected by the user 
			Set set = new TreeSet (scene0.getPartNames ());
			updateSetProperty ("Parts", set);
			// Get the selected part names
			Set selectedPartNames = getSetProperty ("Parts");
			
			// Retain only the selected parts
			List<ScenePart> parts = scene0.getParts ();
			// If no selection, select the 'first one'
			if (selectedPartNames.isEmpty ()) {
				selectedPartNames.add (new TreeSet (scene0.getPartNames ()).iterator ().next ());
			}
			for (Iterator i = parts.iterator (); i.hasNext ();) {
				String name = ((ScenePart) i.next ()).getName ();
				if (!selectedPartNames.contains (name)) {i.remove ();}
			}
			
			int nParts = parts.size ();
			
			ScenePartMap partMap = new ScenePartMap (parts);  // name -> id
			
			int n = numberOfValues ();  // e.g. 2: ddom, dg
			String[] names = getNames ();  // e.g. "ddom", "dg"
			
			Vector xs = new Vector ();		// x coordinates
			Vector[] ys = new Vector[nParts * n];
			for (int i = 0; i < nParts * n; i++) {
				ys[i] = new Vector ();
			}
			String[] yLabels = new String[nParts * n];

			double[] prevValue = new double[nParts * n];  // for increments

			for (Step step : steps) {
			
				// MultipartScenes only
				MultipartScene scene = (MultipartScene) step.getScene ();
				double sceneArea = 0;
				
				xs.add (scene.getDate ());

				// Consider the selected parts under this step 
				List<ScenePart> stepParts = scene.getParts();
				for (Iterator i = stepParts.iterator (); i.hasNext ();) {
					String name = ((ScenePart) i.next ()).getName ();
					if (!selectedPartNames.contains (name)) {i.remove ();}
				}
				
				for (ScenePart part : stepParts) {
					
					sceneArea += part.getArea_m2();
					
					// per Ha computation
					double k0 = 1;  // 1: no effect
					if (isSet ("Mu2TimeGraph.perHectare")) {k0 = 10000d / part.getArea_m2 ();}
					
					double[] values = getValues (model, scene, part);
					
					int i = partMap.getId (part.getName ());
					
					for (int k = 0; k < n; k++) {
						int z = i*n + k;
						
						double v = isSet ("Mu2TimeGraph.showIncrements") 
								? values[k] - prevValue[z]
								: values[k];

						v *= k0;
						
						ys[z].add (v);
						yLabels[z] = names[k]+" "+part.getName ();
						
						prevValue[z] = values[k];
					}
					
				}
				
			}
			
			curves.clear ();
			labels.clear ();
			
			curves.add (xs);
			labels.add(Collections.EMPTY_LIST);  // no labels for xs
			
			
			for (int k = 0; k < nParts; k++) {
				for (int i = 0; i < n; i++) {
					
					// Search the index of the property in getNames (), check if it was requested
					String name = searchPropName (i);
					if (!isSet (makePropName(name))) continue;
					
					int z = k * n + i;
					curves.add (ys[z]);
					labels.add (Collections.singletonList(yLabels[z]));
					
				}
			}

			// If increments, remove the first step values (except if one single step)
			if (isSet ("Mu2TimeGraph.showIncrements") && steps.size () > 1) {
				for (List<? extends Number> l : curves) {
					l.remove (0);
				}
			}

			
			
		} catch (Exception e) {
			Log.println (Log.ERROR, "Mu2TimeGraph.doExtraction ()", "Exception : ", e);
			return false;
		}

		upToDate = true;
		return true;
	}

	
	/**	Extension name.
	 */
	public String getName () {
		return getNamePrefix () + getGraphName ();
	}
	
	/**	Graph X axis name (should be translated by Translator.swap ()).
	 */
	public String getXAxisName () {
		return Translator.swap ("Mu2TimeGraph.xLabel");
	}

	/**	DFCurves
	 */
	public List<List<? extends Number>> getCurves () {
		return curves;
	}

	/**	DFCurves
	 */
	public List<List<String>> getLabels () {
		return labels;
	}

	/**	DFCurves
	 */
	public List<String> getAxesNames () {
			Vector v = new Vector ();
			v.add (getXAxisName ());
			String yLab;
			yLab = getYAxisName ();
			if (isSet ("Mu2TimeGraph.perHectare")) {
				yLab = yLab +" (ha)";
			}
			if (isSet ("Mu2TimeGraph.showIncrements")) {
				yLab = yLab + "(" + Translator.swap ("Mu2TimeGraph.increments") + ")";
			}
			v.add (yLab);
			return v;
	}

	/**	DFCurves
	 */
	public int getNY () {
		return curves == null ? 0 : curves.size () - 1;
	}


	//-------------------
	
	/**	Returns true if the 'hectare' option should be made available for this tool.
	 */
	abstract public boolean acceptsPerHectare ();
	
	/**	Returns true if the 'show increments' option should be made available for this tool.
	 */
	abstract public boolean acceptsIncrements ();
	
	/**	Returns true if the given Scene part can be accepted by this tool  
	 * 	(e.g. if we draw basal area over time, the part should provide a way 
	 * 	to get the basal area...).
	 */
	abstract public boolean matchWithPart (GModel model, MultipartScene scene, ScenePart part);

	
	
	/**	Returns the number of values to be drawn for each part (over time).
	 */
	abstract public int numberOfValues ();
	
	/**	Returns the values that must appear in the graph for the given ScenePart 
	 * 	(e.g. ddom, dg).
	 */
	abstract public double[] getValues (GModel model, MultipartScene scene, ScenePart part);
	
	/**	Returns the names of the values returned by getValues () (e.g. "ddom", "dg").
	 */
	abstract public String[] getNames ();
	
	
	
	/**	Graph name (should be translated by Translator.swap ()).
	 */
	abstract public String getGraphName ();
	
	/**	Graph Y axis name (should be translated by Translator.swap ()).
	 */
	abstract public String getYAxisName ();

	/**	Extension version.
	 */
	abstract public String getVersion ();

	/**	Extension author.
	 */
	abstract public String getAuthor ();

	/**	Extension description (should be translated by Translator.swap ()).
	 */
	abstract public String getDescription ();
	
	//-------------------
	
	
	


}


