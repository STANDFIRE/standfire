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
import java.util.List;
import java.util.Map;
import java.util.Vector;

import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import capsis.defaulttype.MultipartScene;
import capsis.defaulttype.ScenePartMap;
import capsis.defaulttype.ScenePart;
import capsis.defaulttype.SpeciesPropsExplorer;
import capsis.defaulttype.SpeciesPropsOwner;
import capsis.defaulttype.SpeciesPropsOwnerList;
import capsis.extension.PaleoDataExtractor;
import capsis.extension.dataextractor.format.DFCurves;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.MethodProvider;
import capsis.kernel.Step;
import capsis.kernel.extensiontype.GenericExtensionStarter;
import capsis.util.methodprovider.MuDg;
import capsis.util.methodprovider.MuG;

/**	Evolution of BasalArea over Time in a MultipartScene.
 *	@author F. de Coligny - january 2011
 */
public class MuTimeG extends MuTimeGraph {

	static {
		Translator.addBundle("capsis.extension.dataextractor.MuTimeG");
	}
	
	
	
	/**	Default constructor.
	 */
	public MuTimeG () {}

	/**	Constructor.
	 */
	public MuTimeG (GenericExtensionStarter s) {super (s);}
	
	//-------------------
	
	/**	Returns true if the 'hectare' option should be available 
	 * 	for this tool.
	 */
	public boolean acceptsPerHectare () {return true;}
	public boolean acceptsShowTotal () {return true;}
	public boolean acceptsShowMean () {return true;}
	public boolean acceptsShowParts () {return true;}
	public boolean acceptsIncrements() {return true;}
	
	
	
	/**
	 * Returns the name of the species prop this extractor is for. E.g. "BasalArea".
	 */
	@Override
	protected String getShowSpeciesProp () {
		return "BasalArea";
	}

	
	
	/**	Returns true if the given Scene part can be accepted by this tool  
	 * 	(e.g. if we draw basal area over time, the part should contain a method
	 * 	to get the basal area...).
	 */
	public boolean matchWithPart (GModel model, MultipartScene scene, ScenePart part) {
		MethodProvider mp = model.getMethodProvider (); 
		return mp != null && mp instanceof MuG;
	}
	
	/**	Returns the value that must appear in the graph for the given ScenePart 
	 * 	(e.g. basal area, dominant height...).
	 */
	public double getValue (GModel model, MultipartScene scene, ScenePart part) {
		MethodProvider mp = model.getMethodProvider (); 
		// mp can not be null, was tested in matchWithPart (), see upper
		
		MuG m = (MuG) mp;
		return m.getBasalArea (scene, part);
	}
	
	/**	Graph name.
	 */
	public String getGraphName () {
		return Translator.swap ("MuTimeG");
	}
	
	public String getYAxisName () {return Translator.swap ("MuTimeG.yLabel");}

	/**	Extension version.
	 */
	public String getVersion () {return "1.0";}

	/**	Extension author.
	 */
	public String getAuthor () {return "F. de Coligny, V. Cucchi";}

	/**	Extension description.
	 */
	public String getDescription () {return Translator.swap ("MuTimeG.description");}
	
	//-------------------
	
	
	


}


