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

package capsis.extension.filter.gtree;

import java.awt.Color;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import capsis.commongui.util.Tools;
import capsis.defaulttype.Tree;
import capsis.extensiontype.Filter;
import capsis.kernel.GScene;
import capsis.kernel.Plot;
import capsis.util.Configurable;
import capsis.util.ConfigurationPanel;

/**
 * Selector for GTrees in GCells.
 * FTreeGridSelector allows to choose trees according to their position in a cell grid.
 * 
 * Selects trees. 
 * Config dialog shows cells and trees. 
 * Ctrl-selection : inspector on trees. 
 * 
 * @author F. de Coligny - november 2000
 */
public class FTreeGridSelector implements Filter, Configurable, Serializable {
	public static final String NAME = "FTreeGridSelector";
	public static final String VERSION = "1.2";
	public static final String AUTHOR =  "F. de Coligny";
	public static final String DESCRIPTION = "FTreeGridSelector.description";
	
    public final static Color NORMAL = Color.WHITE;
    public final static Color SELECTED = Color.RED;
	
    // Filter can be configured by a config panel using its protected variables wo accessors
    // fc - 21.5.2003 - added transient : referent must not be serialized (huge)
    //
	transient protected Object referent;		// reference stand
	transient protected Collection candidates;	// candidates may result of previous filters action
    
    protected Set<Integer> selectedCellIds;
    private boolean readyToUse;
	
    static {
        Translator.addBundle("capsis.extension.filter.gtree.FTreeGridSelector");
    } 
	
	
	
    public FTreeGridSelector () {
		selectedCellIds = new HashSet<Integer> ();
    } 
	
	/**	Constructor for non GUI mode. 
	*	No more configuration required begore preset () and retain ().
	*/
    public FTreeGridSelector (Set<Integer> s) {
        selectedCellIds = s;	// a Set of Integers : CELL ids
        readyToUse = true;		// it will work
    }
	
	public Object clone () {
		try {
			// We just need another instance to prevent preset () ant retain () from
			// being called by several threads at the same time. 
			return super.clone ();
		} catch (Exception e) {
			Log.println (Log.ERROR, "FTreeGridSelector.clone ()", "Could not clone", e);
			return null;
		}
	}
	
	/**	Used by extensionManager to look for compatibilities.
	*	Target must be a GStand with a non null GPlot containing GCells
	*	refering to GTrees.
	*/
	static public boolean matchWith (Object o) {
		if (o instanceof Collection) {
			Collection c = (Collection) o;
			if (c.isEmpty ()) {return false;}
			Collection reps = Tools.getRepresentatives (c);	// one instance of each class
			//
			// Possibly several subclasses of GTree
			for (Iterator i = reps.iterator (); i.hasNext ();) {
				Object o2 = i.next ();
				if (!(o2 instanceof Tree)) {
					return false;
				}
			}
			Iterator i = reps.iterator ();
			Tree t = (Tree) i.next ();
			GScene s = t.getScene ();
			if (s == null) {return false;}
			Plot p = s.getPlot ();
			if (p == null) {return false;}
			if (p.isEmpty ()) {return false;}
			
			return true;
		}
		return false;
	}

	/**	Return true if the filter keeps the given individual.
	*	This means that the object corresponds to the rules of the filter.
	*/
	// fc - 26.3.2004
	public boolean retain (Object individual) throws Exception {
		// Check if can be applied
		if (!readyToUse) {throw new Exception (
				"FTreeGridSelector.retain () - bad configuration for "+toString ());}
		Tree t = (Tree) individual;
		
		// Complexity: contains is O(1) on a HashSet -> quite good !
		if (selectedCellIds.contains (new Integer (t.getCell ().getId ()))) {return true;}
		
		return false;
	}
	
	/**	GUI use : configuration panel configures the filter before preset () and retain () are called.
	*/
    public ConfigurationPanel getConfigurationPanel (Object param) {
		// fc - 20.4.2004
		Object[] refAndCandidates = (Object[]) param;
		referent = refAndCandidates[0];
		candidates = (Collection) refAndCandidates[1];
		
        return new FTreeGridSelectorConfigPanel (this);
    }
	
	/**	GUI use : configuration panel configures the filter before preset () and retain () are called.
	*/
    public void configure (ConfigurationPanel panel) {
        FTreeGridSelectorConfigPanel p = (FTreeGridSelectorConfigPanel) panel;
        readyToUse = true;		// it will work
    }
	

	/**	From Configurable interface.
	*/
    public String getConfigurationLabel () {return NAME;}

    // Needed because of Configurable and Extension interfaces, but unused
    public void postConfiguration () {}
    public void activate () {}

	@Override
	public void preset(Collection individuals) throws Exception {
		// TODO Auto-generated method stub
		
	}
}
	



