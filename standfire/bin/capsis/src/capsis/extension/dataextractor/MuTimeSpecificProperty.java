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

import jeeb.lib.util.MessageDialog;
import jeeb.lib.util.Translator;
import simmem.gui.SimSpecificPropertyDialog;
import capsis.defaulttype.MultipartScene;
import capsis.defaulttype.ScenePart;
import capsis.kernel.GModel;
import capsis.kernel.MethodProvider;
import capsis.kernel.extensiontype.GenericExtensionStarter;
import capsis.util.methodprovider.SpecificPropertyProvider;
import capsis.kernel.GScene;
import simmem.model.SimScene;


/**	Evolution of selected specific property over Time in a MultipartScene.
 *	@author C Pilaar April 2013, based on similar modules by F. de Coligny
 */
public class MuTimeSpecificProperty extends MuTimeGraph {

	static String currentProperty = "";
	static {
		Translator.addBundle("capsis.extension.dataextractor.MuTimeSpecificProperty");
	}
	//private GScene scene;
	
	
	/**	Default constructor.
	 */
	public MuTimeSpecificProperty () {}


	/**	Constructor.
	 * @throws Exception 
	 */
	public MuTimeSpecificProperty (GenericExtensionStarter s) throws Exception {
		super (s);
		//scene = s.getScene();
		currentProperty = getSpecificProperty(currentProperty);
		
		if (currentProperty == "") {
			MessageDialog.print (this, "No specific property selected");
		}
		
	}

	public String getSpecificProperty(String defaultProperty) throws Exception {
		// Interactive start
		String newProperty = defaultProperty;  // default the returned value to the initial value;  
		GScene scene = step.getScene();
		SimSpecificPropertyDialog dlg = new SimSpecificPropertyDialog (defaultProperty,  scene);
		dlg.setVisible(true);

		if (dlg.isValidDialog ()) {
			newProperty = dlg.getSpecificProperty ();  // only update the return value if the user completes the dialog correctly  
		}
		dlg.dispose ();
		return newProperty;
	}
	
	//-------------------
	
	/**	Returns true if the 'hectare' option should be available 
	 * 	for this tool.
	 */
	public boolean acceptsPerHectare () {return true;}
	public boolean acceptsShowTotal () {return true;}
	public boolean acceptsShowMean () {return true;}
	public boolean acceptsShowParts () {return true;}
	public boolean acceptsIncrements() {return true;}
	
		
	
	/**	Returns true if the given Scene part can be accepted by this tool  
	 * 	(e.g. if we draw basal area over time, the part should contain a method
	 * 	to get the basal area...).
	 */
	
	public boolean matchWithPart (GModel model, MultipartScene scene, ScenePart part) {
		MethodProvider mp = model.getMethodProvider (); 
		return mp != null  && mp instanceof SpecificPropertyProvider;
	}

	
	/**	Returns the value of the specific property that must appear in the graph for the given ScenePart 
	 * 	(e.g. basal area, dominant height...).
	 */
	public double getValue (GModel model, MultipartScene scene, ScenePart part) {
		MethodProvider mp = model.getMethodProvider (); 
		// mp can not be null, was tested in matchWithPart (), see upper
		
		SpecificPropertyProvider m = (SpecificPropertyProvider) mp;
		
		System.out.println ("MuTimeSpecificProperty getValue () for" + scene.toString()+"-"+ currentProperty + ": "+m.getSpecificPropertyValue (scene, part, currentProperty));
		
		return m.getSpecificPropertyValue (scene, part, currentProperty);
	}
	
	/**	Graph name.
	 */
	public String getGraphName () {
	/*
	 * Note that getGraphName is used both for the menu list of graph types and in the title of graph windows.
	 * In the first instance currentProperty is blank, so the menu item is called "Specific Property / Time (Simmem)" as coded below - or a translation,
	 * whereas once a specific property has been selected, its property name is used in the form captions for the graphs.
	 * 
	 * It would be better to handle these two cases with different methods, so that the menu item consistently displayed the same
	 * name, regardless of which specific property was being displayed ....   
	 */
		if (currentProperty.trim () == "") {
			return Translator.swap ("Specific Property / Time (Simmem)");
					}
		else {
			return currentProperty;
		}		  
	}
	
	public String getYAxisName () {
		/* 
		 * It would be preferable to return the name of the specific property (currentProperty) but if there are multiple
		 * graphs for different specific properties, they all get the latest property shown as the y-axis label, whereas the
		 * graph title retains the correct name.
		 */
		return Translator.swap ("MuTimeSpecificProperty.yLabel");
	}

	/**	Extension version.
	 */
	public String getVersion () {return "1.0";}

	/**	Extension author.
	 */
	public String getAuthor () {return "C Pilaar (F. de Coligny)";}

	/**	Extension description.
	 */
	public String getDescription () {return Translator.swap ("MuTimeSpecificProperty.description");}
	
	//-------------------
	
	
	


}


