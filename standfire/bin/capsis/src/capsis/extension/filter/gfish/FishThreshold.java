/*
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 *
 * Copyright (C) 2000-2003  Francois de Coligny
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

package capsis.extension.filter.gfish;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import capsis.commongui.util.Tools;
import capsis.extensiontype.Filter;
import capsis.util.Configurable;
import capsis.util.ConfigurationPanel;
import capsis.util.GFish;

/**
 * Filter for a GFish.
 * Applies a low/high threshold on forkLength (...).
 *
 * @author F. de Coligny - september 2004
 */
public class FishThreshold implements Filter, Configurable, Serializable {

	public static final String NAME = "FishThreshold";
	public static final String VERSION = "1.1";
	public static final String AUTHOR =  "F. de Coligny";
	public static final String DESCRIPTION = "FishThreshold.description";
	
	public static final int FORK_LENGTH = 0;
	//public static final int DISPERSAL_PROBA = 1;
	//public static final int MOVEMENT_PROBA = 2;
	public static final int AGE = 3;
	//public static final int SPAWNING_AGE = 4;
	public static final int SURVIVAL_PROBA = 5;
	public static final int SPAWN_COUNT = 6;
	public static final String UNUSED = "";

	// fc - 21.5.2003 - added transient : referent must not be serialized (huge)
	//
    transient protected Object referent;		// complete original object (a complete GStand or GPlot...)
    transient protected Collection candidates;	// individuals to filter

	// Filter can be configured by a config panel using its protected variables wo accessors
	protected int mode;			// FORK_LENGTH (...)
	protected double lowValue;		// possibly Double.MIN_VALUE
	protected double highValue;		// possibly Double.MAX_VALUE

	private boolean readyToUse;

	static {
		Translator.addBundle("capsis.extension.filter.gfish.FishThreshold");
	}

	
	public FishThreshold () {}


	/**	Constructor for non GUI mode.
	*	No more configuration required begore preset () and retain ().
	*/
	public FishThreshold (int m, double lv, double hv) {
		mode = m;
		lowValue = lv;
		highValue = hv;

		// fc - 20.4.2004 - if (highValue >= lowValue) {
		if (highValue < lowValue) {
			Log.println (Log.INFO, "FishThreshold.c ()", "highValue ("+highValue
					+") < lowValue ("+lowValue+")");
			return;
		}	// won't work

		// Check input
		if (mode != FishThreshold.FORK_LENGTH
				//&& mode != FishThreshold.DISPERSAL_PROBA
				//&& mode != FishThreshold.MOVEMENT_PROBA
				&& mode != FishThreshold.AGE
				//&& mode != FishThreshold.SPAWNING_AGE
				&& mode != FishThreshold.SURVIVAL_PROBA
				&& mode != FishThreshold.SPAWN_COUNT) {
			Log.println (Log.INFO, "FishThreshold.c ()", "mode ("+mode
					+") is unknown (should be"
					+" FORK_LENGTH ("+FORK_LENGTH+")"
				//	+" DISPERSAL_PROBA ("+DISPERSAL_PROBA+")"
				//	+" MOVEMENT_PROBA ("+MOVEMENT_PROBA+")"
					+" AGE ("+AGE+")"
				//	+" SPAWNING_AGE ("+SPAWNING_AGE+")"
					+" SURVIVAL_PROBA ("+SURVIVAL_PROBA+")"
					+" SPAWN_COUNT ("+SPAWN_COUNT+")"
					+")");
			return;
		}	// won't work

		readyToUse = true;		// will work
	}
	
	public Object clone () {
		try {
			// We just need another instance to prevent preset () ant retain () from
			// being called by several threads at the same time. 
			return super.clone ();
		} catch (Exception e) {
			Log.println (Log.ERROR, "FishThreshold.clone ()", "Could not clone", e);
			return null;
		}
	}

	/**	Used by extensionManager to look for compatibilities.
	*/
	static public boolean matchWith (Object o) {
		if (o instanceof Collection) {
			Collection c = (Collection) o;
			if (c.isEmpty ()) {return false;}
			Collection reps = Tools.getRepresentatives (c);	// one instance of each class
			//
			// Possibly several subclasses of GFish
			for (Iterator i = reps.iterator (); i.hasNext ();) {
				if (!(i.next () instanceof GFish)) {return false;}
			}
			return true;
		}
		return false;
	}

	/**	Return true if the filter keeps the given individual.
	*	This means that the object corresponds to the rules of the filter.
	*/
	@Override
	public boolean retain (Object individual) throws Exception {
		// Check if can be applied
		if (!readyToUse) {throw new Exception (
				"FishThreshold.retain () - bad configuration for "+toString ());}
		GFish f = (GFish) individual;
		if (mode == FishThreshold.FORK_LENGTH) {
			if (f.getForkLength () >= lowValue && f.getForkLength () < highValue) {return true;}

		//} else if (mode == FishThreshold.DISPERSAL_PROBA) {
		//	if (f.getDispersalProba () >= lowValue && f.getDispersalProba () < highValue) {return true;}
		//
		//} else if (mode == FishThreshold.MOVEMENT_PROBA) {
		//	if (f.getMovementProba () >= lowValue && f.getMovementProba () < highValue) {return true;}
		//
		} else if (mode == FishThreshold.AGE) {
			if (f.getAge () >= lowValue && f.getAge () < highValue) {return true;}

		//} else if (mode == FishThreshold.SPAWNING_AGE) {
		//	if (f.getSpawningAge () >= lowValue && f.getSpawningAge () < highValue) {return true;}
		//
		} else if (mode == FishThreshold.SURVIVAL_PROBA) {
			if (f.getSurvivalProba () >= lowValue && f.getSurvivalProba () < highValue) {return true;}

		} else if (mode == FishThreshold.SPAWN_COUNT) {
			if (f.getSpawnCount () >= lowValue && f.getSpawnCount () < highValue) {return true;}

		}
		return false;
	}

	/**	GUI use : configuration panel configures the filter before preset () and retain () are called.
	*/
	public ConfigurationPanel getConfigurationPanel (Object param) {
		// fc - 20.4.2004
		Object[] refAndCandidates = (Object[]) param;
		referent = refAndCandidates[0];
		candidates = (Collection) refAndCandidates[1];

		return new FishThresholdDialog (this);
	}

	/**	GUI use : configuration panel configures the filter before preset () and retain () are called.
	*/
	public void configure (ConfigurationPanel panel) {
		FishThresholdDialog p = (FishThresholdDialog) panel;
		this.mode = p.getMode ();
		this.lowValue = p.getLowValue ();
		this.highValue = p.getHighValue ();

		readyToUse = true;		// it will work
	}


	/**	From Configurable interface.
	*/
	public String getConfigurationLabel () {return Translator.swap(NAME);}

	// Needed because of Configurable and Extension interfaces, but unused
	public void postConfiguration () {}
	public void activate () {}

	@Override
	public void preset(Collection individuals) throws Exception {
		// TODO Auto-generated method stub
		
	}


}


