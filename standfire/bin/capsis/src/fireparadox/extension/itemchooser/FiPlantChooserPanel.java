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
import java.util.Collection;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import jeeb.lib.sketch.kernel.SketchModel;
import jeeb.lib.util.MessageDialog;
import jeeb.lib.util.InstantPanel;
import jeeb.lib.util.Translator;
import fireparadox.gui.database.FmPlantForm;
import fireparadox.gui.database.FmPlantFormLine;
import fireparadox.model.FmModel;
import fireparadox.sketch.FmSketchLinker;


/**	FiPlantChooserPanel.
 *	@author O. Vigy, F. de Coligny -  july 2007
 */
public class FiPlantChooserPanel extends InstantPanel implements ActionListener, 
		ListSelectionListener {
	// fc sept 2009 review

	private FiPlantChooser itemChooser;
	private FmPlantForm form;


	/**	Constructor.
	*/
	public FiPlantChooserPanel (FiPlantChooser itemChooser) {
		super (itemChooser);
		this.itemChooser = itemChooser;

		createUI ();
	}

	/**	Tests correctness of all the options, called by superclass InstantPanel.actionPerformed ()
	*	before sending an event "option changed" to the caller if isCorrect () is true.
	*/
	@Override
	public boolean isCorrect () {
		// Check all user entries, in case of trouble, tell him and return false (caller won't be notified)

		// If user did no selection in the table - fc + ov - 25.9.2007
		Collection selection = form.getSelection ();
		if (selection == null || selection.isEmpty ()) {
			MessageDialog.print (this, Translator.swap ("FiPlantChooser.pleaseSelectAFuelObjectInTheTable"));
			return false;
		}

		return true;
	}

	/**	Manage buttons
	*/
	@Override
	public void actionPerformed (ActionEvent evt) {
		//~NO -  super.actionPerformed (evt);
	}

	/**	ListSelectionListener interface, we are listeneing to the form.
	*/
	public void valueChanged (ListSelectionEvent evt) {
		
		// Get selection in form table
		Collection<FmPlantFormLine> selectedPlants = form.getSelection ();
		
		// If no selection, abort, will cause an error - fc - 28.9.2007
		if (selectedPlants == null || selectedPlants.size () == 0) {return;}
		
		// Consider the first plant in selection
		FmPlantFormLine firstPlant = selectedPlants.iterator ().next ();
		
		// Set the chooser's externalRef
		itemChooser.externalRef = firstPlant;
		
	}

	private void createUI () {

		SketchModel sketchModel = itemChooser.sketchModel;

		FmSketchLinker linker = (FmSketchLinker) sketchModel.getSketchLinker ();
		FmModel model = linker.getUserModel ();

		form = new FmPlantForm (model);
		form.addSelectionListener (this);

		setLayout (new BorderLayout ());
		this.add (form, BorderLayout.CENTER);
	}


}




