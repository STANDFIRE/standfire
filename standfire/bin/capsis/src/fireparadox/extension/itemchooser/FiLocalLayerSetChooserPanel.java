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

import jeeb.lib.sketch.kernel.SketchLinker;
import jeeb.lib.sketch.kernel.SketchModel;
import jeeb.lib.sketch.scene.item.Polygon;
import jeeb.lib.sketch.scene.kernel.SceneModel;
import jeeb.lib.util.InstantPanel;
import jeeb.lib.util.ListenedTo;
import jeeb.lib.util.Listener;
import jeeb.lib.util.Log;
import jeeb.lib.util.MessageDialog;
import jeeb.lib.util.Translator;
import fireparadox.gui.FmLayerForm;
import fireparadox.model.FmModel;
import fireparadox.model.FmStand;
import fireparadox.model.layerSet.FmLayer;
import fireparadox.model.layerSet.FmLayerSetExternalRef;

/**	FiLocalLayerSetChooserPanel.
 *	@author F. Pimont, F. de Coligny - july 2009
 */
public class FiLocalLayerSetChooserPanel extends InstantPanel implements ActionListener, 
		/*ListSelectionListener,*/ Listener { 
	private FmModel model;
			
	private FiLocalLayerSetChooser itemChooser;
	// new form in Avignon - oct 2009
	private FmLayerForm form;


	/**	Constructor.
	*/
	public FiLocalLayerSetChooserPanel (FiLocalLayerSetChooser itemChooser) {
		super (itemChooser);
		try {
			this.itemChooser = itemChooser;
			
			SketchModel sm = itemChooser.sketchModel;
			SketchLinker linker = (SketchLinker) sm.getSketchLinker ();
			FmStand stand = (FmStand) linker.getUserScene ();
			model = stand.getModel ();

			createUI ();
		} catch (Exception e) {
			Log.println (Log.ERROR, "FiLocalLayerSetChooserPanel.c ()", 
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
			//~ FiLayerSetExternalRef extRef = (FiLayerSetExternalRef) itemChooser.externalRef;
			// No external ref
			//~ if (extRef == null) {
				//~ MessageDialog.print (this, 
						//~ Translator.swap ("FiLocalLayerSetChooserPanel.pleaseAddAtLeastOneLayerInTheResultTable"));
				//~ return false;
			//~ }
		
		// Check the form
		try {
			if (!form.isCorrect ()) { return false; }
		} catch (Exception e) {
			// TODO FP Auto-generated catch block
			e.printStackTrace ();
		}
		
		updateChooserFromTheForm ();
		
		// set itemChooser.polygon (if no polygon selected in the scene, tell user)
		//Polygon polygon = (Polygon) itemChooser.sketchModel.getSelectedPolygon ();
		Polygon polygon = (Polygon) ((SceneModel)itemChooser.sketchModel).getSelectedPolygon ();
		if (polygon == null) {
			MessageDialog.print (this, Translator.swap ("FiLocalLayerSetChooserPanel.pleaseSelectAPolygonInTheScene"));
			return false;
		}
		itemChooser.polygon = polygon;
		
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
		Collection<FmLayer> layers = form.getLayers();
		
		// if layers is null or empty, also add in the externalRef
		FmLayerSetExternalRef extRef = new FmLayerSetExternalRef ();
		extRef.addLayers (layers);
		
		// Set the chooser's externalRef
		itemChooser.externalRef = extRef;
	}
	
	/**	Listener interface. Called by ListenedTo when something happens.
	*/
	public void somethingHappened (ListenedTo l, Object param) {
		updateChooserFromTheForm ();
	}
	
	private void createUI () {
		
		// - New form in Avignon, oct 2009 -
		form = new FmLayerForm (model);
		
		//~ form.addListener (this);
		//~ form.addSelectionListener (this);
		//~ form.addActionListener (this);

		setLayout (new BorderLayout ());
		this.add (form, BorderLayout.CENTER);
	}
	
}




