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
import java.util.HashSet;
import java.util.Iterator;

import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import capsis.commongui.util.Tools;
import capsis.extension.filter.general.FQualitativeProperty;
import capsis.extensiontype.Filter;
import capsis.util.Configurable;
import capsis.util.ConfigurationPanel;
import capsis.util.FishStand;
import capsis.util.GFish;
import capsis.util.GReach;
import capsis.util.Grouper;
import capsis.util.GrouperManager;

/**
 * Filter for a GFish.
 * Check if the fish is in the given reach group.
 *
 * @author F. de Coligny - may 2005
 */
public class FishInReachGroup implements Filter, Configurable, Serializable {

	public static final String NAME = "FishInReachGroup";
	public static final String VERSION = "1.0";
	public static final String AUTHOR =  "F. de Coligny";
	public static final String DESCRIPTION = "FishInReachGroup.description";
	
	//~ public static final int FORK_LENGTH = 0;
	//~ public static final int DISPERSAL_PROBA = 1;
	//~ public static final int MOVEMENT_PROBA = 2;
	//~ public static final int AGE = 3;
	//~ public static final int SPAWNING_AGE = 4;
	//~ public static final int SURVIVAL_PROBA = 5;
	//~ public static final int SPAWN_COUNT = 6;
	//~ public static final String UNUSED = "";

	// fc - 21.5.2003 - added transient : referent must not be serialized (huge)
	//
    transient protected Object referent;		// complete original object (a complete GStand or GPlot...)
    transient protected Collection candidates;	// individuals to filter

	// Filter can be configured by a config panel using its protected variables wo accessors
	//~ protected int mode;			// FORK_LENGTH (...)
	//~ protected double lowValue;		// possibly Double.MIN_VALUE
	//~ protected double highValue;		// possibly Double.MAX_VALUE

	private String grouperName;		// a reach grouper
	private Collection fishIds;		// ids of the fishes in the reach group

	private boolean readyToUse;

	static {
		Translator.addBundle("capsis.extension.filter.gfish.FishInReachGroup");
	}


	public FishInReachGroup () {}

	/**	Constructor for non GUI mode.
	*	No more configuration required begore preset () and retain ().
	*/
	public FishInReachGroup (String gName) {
		//~ mode = s.getMode ();
		//~ lowValue = s.getLowValue ();
		//~ highValue = s.getHighValue ();
		grouperName = gName;

		// fc - 20.4.2004 - if (highValue >= lowValue) {
		//~ if (highValue < lowValue) {
			//~ Log.println (Log.INFO, "FishInReachGroup.c ()", "highValue ("+highValue
					//~ +") < lowValue ("+lowValue+")");
			//~ return;
		//~ }	// won't work

		readyToUse = true;		// will work
	}
	
	public Object clone () {
		try {
			// We just need another instance to prevent preset () ant retain () from
			// being called by several threads at the same time. 
			return super.clone ();
		} catch (Exception e) {
			Log.println (Log.ERROR, "FishInReachGroup.clone ()", "Could not clone", e);
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

	/**	Some filters may need the complete collection to be set correctly
	*	before evaluating retain ().
	*	In such a case, they can redefine this method
	*	@see FQualitativeProperty
	*/
	@Override
	public void preset (Collection individuals) throws Exception {

		// Build the fishIds collection : ids of the fishes in the reach group (before retain ())
		GFish firstFish = (GFish) individuals.iterator ().next ();
		FishStand stand = firstFish.getFishStand ();
		Collection reaches = stand.getReachMap ().values ();

//~ System.out.println ("FishInReachGroup.preset(): group="+grouperName);
//~ System.out.println ("   before: "+individuals.size ());
		Grouper grouper = GrouperManager.getInstance ().getGrouper (grouperName);
		Collection reachGroup = grouper.apply (reaches);
//~ System.out.println ("   reachGroup: "+reachGroup.size ());
		fishIds = new HashSet ();
		for (Iterator i = reachGroup.iterator (); i.hasNext ();) {
			GReach r = (GReach) i.next ();
			Collection fishes = r.getFishes ();
			for (Iterator j = fishes.iterator (); j.hasNext ();) {
				GFish f = (GFish) j.next ();
				fishIds.add (f.getId ());
			}
		}
//~ System.out.println ("   after: "+fishIds.size ());
	}

	/**	Return true if the filter keeps the given individual.
	*	This means that the object corresponds to the rules of the filter.
	*/
	@Override
	public boolean retain (Object individual) throws Exception {
		// Check if can be applied
		if (!readyToUse) {throw new Exception (
				"FishInReachGroup.retain () - bad configuration for "+toString ());}
		GFish f = (GFish) individual;

		return fishIds.contains (f.getId ());	// return true if fish is contained in FishIds.
	}

	/**	GUI use : configuration panel configures the filter before preset () and retain () are called.
	*/
	public ConfigurationPanel getConfigurationPanel (Object param) {
		// fc - 20.4.2004
		Object[] refAndCandidates = (Object[]) param;
		referent = refAndCandidates[0];
		candidates = (Collection) refAndCandidates[1];

		return new FishInReachGroupDialog (this);
	}

	/**	GUI use : configuration panel configures the filter before preset () and retain () are called.
	*/
	public void configure (ConfigurationPanel panel) {
		FishInReachGroupDialog p = (FishInReachGroupDialog) panel;
		this.grouperName = p.getGrouperName ();

		readyToUse = true;		// it will work
	}

	
	/**	From Configurable interface.
	*/
	public String getConfigurationLabel () {return Translator.swap(NAME);}

	// Needed because of Configurable and Extension interfaces, but unused
	public void postConfiguration () {}
	public void activate () {}

	

}


