/*
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 *
 * Copyright (C) 2001-2003  Francois de Coligny
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

package fireparadox.extension.objectviewer;

//import com.sun.j3d.utils.behaviors.mouse.*;
//import com.sun.j3d.utils.behaviors.vp.*;

import java.awt.BorderLayout;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.JTextField;
import javax.swing.border.Border;

import jeeb.lib.util.AmapTools;
import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.JWidthLabel;
import jeeb.lib.util.LinePanel;
import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import capsis.extension.AbstractObjectViewer;
import capsis.lib.fire.fuelitem.FiPlant;

/**
 * A selection state panel for FireParadox
 * 
 * @author O. Vigy - 6.6.2007
 */
public class FireSelectionState extends AbstractObjectViewer {

	static {
		Translator.addBundle ("fireparadox.extension.objectviewer.FireSelectionState");
	}
	static public final String NAME = Translator.swap ("FireSelectionState");
	static public final String DESCRIPTION = Translator.swap ("FireSelectionState.description");
	static public final String AUTHOR = "O. Vigy";
	static public final String VERSION = "1.0";

	private Collection trees; // the FireTrees in the selection

	private JTextField numberOfTrees;

	/**
	 * Default constructor.
	 */
	public FireSelectionState () {
	}

	@Override
	public void init (Collection s) throws Exception {

		try {
			trees = s;

			// fc - 22.9.2005 - keep only the trees, discard other objects
			// (cells...)
			for (Iterator i = trees.iterator (); i.hasNext ();) {
				if (!(i.next () instanceof FiPlant)) {
					i.remove ();
				}
			}

			createUI ();
		} catch (Exception exc) {
			Log.println (Log.ERROR, "FireSelectionState.c ()", exc.toString (), exc);
			throw exc; // fc - 4.11.2003 - object viewers may throw exception
		}

	}

	/**
	 * Extension dynamic compatibility mechanism. This matchwith method checks
	 * if the extension can deal (i.e. is compatible) with the referent.
	 */

	static public boolean matchWith (Object referent) {
		try {
			// A spatialized FiPlant or a collection of some : true
			if (referent instanceof FiPlant) { return true; }
			if (referent instanceof Collection) {
				Collection c = (Collection) referent;
				if (c.isEmpty ()) { return false; }

				// fc - 22.9.2005
				// Find representative objects (ie with different classes)
				// if there is at least one FiPlant in the collection, ok
				Collection reps = AmapTools.getRepresentatives (c);
				for (Iterator i = reps.iterator (); i.hasNext ();) {
					if (i.next () instanceof FiPlant) { return true; }
				}
				return false;
			}

		} catch (Exception e) {
			Log.println (Log.ERROR, "FireSelectionState.matchWith ()", "Error in matchWith () (returned false)", e);
			return false;
		}

		return false;
	}

	/**
	 * User interface definition.
	 */
	private void createUI () {

		// 3.1 State panel
		ColumnPanel statePanel = new ColumnPanel ();

		// Border generalState - ov - 15.01.2007
		ColumnPanel p0 = new ColumnPanel ();
		Border etched = BorderFactory.createEtchedBorder ();
		Border b = BorderFactory.createTitledBorder (etched, Translator.swap ("FireSelectionState.generalState"));
		p0.setBorder (b);

		LinePanel l10 = new LinePanel ();
		l10.add (new JWidthLabel (Translator.swap ("FireSelectionState.numberOfTrees") + " :", 150));
		numberOfTrees = new JTextField ();
		numberOfTrees.setText ("" + trees.size ());
		l10.add (numberOfTrees);
		p0.add (l10);

		// ~ LinePanel l11 = new LinePanel ();
		// ~ l11.add (new JWidthLabel (Translator.swap
		// ("FiStatePanel.heightMax")+" :", 150));
		// ~ heightMax = new JTextField ();
		// ~ l11.add (heightMax);
		// ~ p0.add (l11);

		statePanel.add (p0);

		/*
		 * //Border treeState - ov - 15.01.2007 ColumnPanel p1 = new ColumnPanel
		 * (); b = BorderFactory.createTitledBorder (etched, Translator.swap (
		 * "FiStatePanel.treeState")); p1.setBorder (b);
		 * 
		 * LinePanel l12 = new LinePanel (); l12.add (new JWidthLabel
		 * (Translator.swap ("FiStatePanel.treeCover")+" :", 150)); treeCover =
		 * new JTextField (); l12.add (treeCover); p1.add (l12);
		 * 
		 * LinePanel l13 = new LinePanel (); l13.add (new JWidthLabel
		 * (Translator.swap ("FiStatePanel.treeNumber")+" :", 150)); treeNumber
		 * = new JTextField (); l13.add (treeNumber); p1.add (l13);
		 * 
		 * LinePanel l14 = new LinePanel (); l14.add (new JWidthLabel
		 * (Translator.swap ("FiStatePanel.domTreeSpecies")+" :", 150));
		 * domTreeSpecies = new JTextField (); l14.add (domTreeSpecies); p1.add
		 * (l14);
		 * 
		 * statePanel.add (p1);
		 * 
		 * //Border shrubState - ov - 15.01.2007 ColumnPanel p2 = new
		 * ColumnPanel (); b = BorderFactory.createTitledBorder (etched,
		 * Translator.swap ( "FiStatePanel.shrubState")); p2.setBorder (b);
		 * 
		 * LinePanel l15 = new LinePanel (); l15.add (new JWidthLabel
		 * (Translator.swap ("FiStatePanel.shrubCover")+" :", 150)); shrubCover
		 * = new JTextField (); l15.add (shrubCover); p2.add (l15);
		 * 
		 * LinePanel l16 = new LinePanel (); l16.add (new JWidthLabel
		 * (Translator.swap ("FiStatePanel.shrubPhytovolume")+" :", 150));
		 * shrubPhytovolume = new JTextField (); l16.add (shrubPhytovolume);
		 * p2.add (l16);
		 * 
		 * LinePanel l17 = new LinePanel (); l17.add (new JWidthLabel
		 * (Translator.swap ("FiStatePanel.domShrubSpecies")+" :", 150));
		 * domShrubSpecies = new JTextField (); l17.add (domShrubSpecies);
		 * p2.add (l17);
		 * 
		 * statePanel.add (p2);
		 * 
		 * //Border herbsState - ov - 15.01.2007 ColumnPanel p3 = new
		 * ColumnPanel (); b = BorderFactory.createTitledBorder (etched,
		 * Translator.swap ( "FiStatePanel.herbsState")); p3.setBorder (b);
		 * 
		 * LinePanel l18 = new LinePanel (); l18.add (new JWidthLabel
		 * (Translator.swap ("FiStatePanel.herbsCover")+" :", 150)); herbsCover
		 * = new JTextField (); l18.add (herbsCover); p3.add (l18);
		 * 
		 * statePanel.add (p3);
		 */

		setLayout (new BorderLayout ());
		add (statePanel, BorderLayout.NORTH);

		// Do not set any size in ObjectViewers
		// ~ setPreferredSize (new Dimension (300, 400));

	}

}
