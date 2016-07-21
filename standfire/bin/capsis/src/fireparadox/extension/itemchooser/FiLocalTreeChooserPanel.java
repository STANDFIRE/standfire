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

package fireparadox.extension.itemchooser;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import jeeb.lib.sketch.kernel.SketchLinker;
import jeeb.lib.sketch.kernel.SketchModel;
import jeeb.lib.util.InstantPanel;
import jeeb.lib.util.ListenedTo;
import jeeb.lib.util.Listener;
import jeeb.lib.util.Log;
import fireparadox.gui.FmLocalTreeForm;
import fireparadox.model.FmModel;
import fireparadox.model.FmStand;
import fireparadox.model.plant.FmLocalTreeExternalRef;

/**	FiLocalTreeChooserPanel.
 *	@author F. Pimont, F. de Coligny - june 2009
 */
public class FiLocalTreeChooserPanel extends InstantPanel implements ActionListener, 
		/*ListSelectionListener,*/ Listener { 
	private FmModel model;
			
	private FiLocalTreeChooser itemChooser;
    private FmLocalTreeForm form;


	/**	Constructor.
	*/
	public FiLocalTreeChooserPanel (FiLocalTreeChooser itemChooser) {
		super (itemChooser);
		try {
			this.itemChooser = itemChooser;
			SketchModel sm = itemChooser.sketchModel;
			SketchLinker linker = (SketchLinker) sm.getSketchLinker ();
			FmStand stand = (FmStand) linker.getUserScene ();
			model = stand.getModel ();

			createUI ();
		} catch (Exception e) {
			Log.println (Log.ERROR, "FiLocalTreeChooserPanel.c ()", 
					"Error in constructor", e);
		}
	}


	/**	Tests correctness of all the options, called by superclass InstantPanel.actionPerformed ()
	 *	before sending an event "option changed" to the caller if isCorrect () is true.
	 */
	@Override
	public boolean isCorrect () {
		// check all user entries, in case of trouble, tell him and return false (caller won't be notified)

			// Check the external reference and its lines (their percentage must be set...)
			//~ FiLocalTreeExternalRef extRef = (FiLocalTreeExternalRef) itemChooser.externalRef;
			// No external ref
			//~ if (extRef == null) {
				//~ MessageDialog.print (this, 
						//~ Translator.swap ("FiLocalTreeChooserPanel.pleaseAddAtLeastOneLayerInTheResultTable"));
				//~ return false;
			//~ }
		
		// Check the form
		if (!form.isCorrect ()) {return false;}
		
		// Update externalRef in the chooser
		String speciesName = form.getSpeciesName ();
		double dominantHeight = form.getDominantHeight();
		double meanAge = form.getMeanAge ();
		double ageStandardDeviation = form.getAgeStandardDeviation ();
		double stemDensity = form.getStemDensity();
		double liveMoisture = form.getLiveMoisture();
		double deadMoisture = form.getDeadMoisture();
		double liveTwigMoisture = form.getLiveMoisture();
		// Create the externalRef, set it in the chooser
		FmLocalTreeExternalRef extRef = new FmLocalTreeExternalRef (
				speciesName,
				dominantHeight, meanAge, ageStandardDeviation, stemDensity,
				liveMoisture, deadMoisture, liveTwigMoisture, model);
		itemChooser.externalRef = extRef;
		
		return true;
	}

	/**	Listens to actions in the form
	*/
	@Override
	public void actionPerformed (ActionEvent evt) {
		//~NO -  super.actionPerformed (evt);
	}

	/**	Triggered when something changed in the form: get its lines and 
	*	update the external reference to keep the itemChooser up to date.
	*/
	private void updateChooserFromTheForm () {
		String speciesName = form.getSpeciesName ();
		double dominantHeight = form.getDominantHeight();
		double meanAge = form.getMeanAge ();
		double ageStandardDeviation = form.getAgeStandardDeviation ();
		double stemDensity = form.getStemDensity();
		double liveMoisture = form.getLiveMoisture();
		double deadMoisture = form.getDeadMoisture();
		double liveTwigMoisture = form.getLiveMoisture();
		
		// Create the externalRef, set it in the chooser
		FmLocalTreeExternalRef extRef = new FmLocalTreeExternalRef (
				speciesName,
				dominantHeight, meanAge, ageStandardDeviation, stemDensity,
				liveMoisture, deadMoisture, liveTwigMoisture, model);
		itemChooser.externalRef = extRef;
	}
	
	/**	Listener interface. Called by ListenedTo when something happens.
	*/
	public void somethingHappened (ListenedTo l, Object param) {
		updateChooserFromTheForm ();
	}
	
	private void createUI () {
		
		form = new FmLocalTreeForm (model);
		form.addListener (this);
		//~ form.addSelectionListener (this);
		//~ form.addActionListener (this);

		setLayout (new BorderLayout ());
		this.add (form, BorderLayout.CENTER);
	}
	
}




