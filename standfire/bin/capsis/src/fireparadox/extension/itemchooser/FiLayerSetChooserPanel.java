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

import jeeb.lib.sketch.kernel.SketchModel;
import jeeb.lib.sketch.scene.item.Polygon;
import jeeb.lib.sketch.scene.kernel.SceneModel;
import jeeb.lib.util.InstantPanel;
import jeeb.lib.util.ListenedTo;
import jeeb.lib.util.Listener;
import jeeb.lib.util.MessageDialog;
import jeeb.lib.util.Translator;
import capsis.lib.fire.fuelitem.FiLayer;
import fireparadox.gui.FmLayerFormFromDB;
import fireparadox.model.FmModel;
import fireparadox.model.layerSet.FmLayer;
import fireparadox.model.layerSet.FmLayerSetExternalRef;
import fireparadox.sketch.FmSketchLinker;


/**	FiLayerSetChooserPanel.
 *	@author F. de Coligny - march 2009
 */
public class FiLayerSetChooserPanel extends InstantPanel implements ActionListener, 
		Listener { 
	// fc sept 2009 review
	
	private FiLayerSetChooser itemChooser;
    private FmLayerFormFromDB form;


	/**	Constructor.
	*/
	public FiLayerSetChooserPanel (FiLayerSetChooser itemChooser) {
		super (itemChooser);
		this.itemChooser = itemChooser;

		createUI ();
	}


	/**	Tests correctness of all the options, called by superclass InstantPanel.actionPerformed ()
	*	before sending an event "option changed" to the caller if isCorrect () is true.
	*/
	@Override
	public boolean isCorrect () {
		// check all user entries, in case of trouble, tell him and return false (caller won't be notified)

		// Check the external reference and its lines (their percentage must be set...)
		FmLayerSetExternalRef extRef = (FmLayerSetExternalRef) itemChooser.externalRef;
		// No external ref
		if (extRef == null) {
			MessageDialog.print (this, 
					Translator.swap ("FiLayerSetChooser.pleaseAddAtLeastOneLayerInTheResultTable"));
			return false;
		}
		
		// Check the form
		if (!form.isCorrect ()) {return false;}
		
		Collection<FiLayer> layers = extRef.getLayers ();
		// No lines added
		if (layers == null || layers.isEmpty ()) {
			MessageDialog.print (this, 
					Translator.swap ("FiLayerSetChooser.pleaseAddAtLeastOneLayerInTheResultTable"));
			return false;
		}
		
		// note: itemChooser.externalRef was already set
		
		// set itemChooser.polygon (if no polygon selected in the scene, tell user)
		Polygon polygon = (Polygon) ((SceneModel)itemChooser.sketchModel).getSelectedPolygon ();
		if (polygon == null) {
			MessageDialog.print (this, Translator.swap ("FiLayerSetChooser.pleaseSelectAPolygonInTheScene"));
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
	
	/**	Listener interface. Called by ListenedTo when something happens.
	*/
	public void somethingHappened (ListenedTo l, Object param) {
		updateChooserFromTheForm ();
	}
	
	/**	Triggered when something changed in the form: get its lines and 
	*	update the external reference to keep the itemChooser up to date.
	*/
	private void updateChooserFromTheForm () {
		Collection<FmLayer> layers = form.getLayers ();
		
		// if layerLines is null or empty, also add in the externalRef
		FmLayerSetExternalRef extRef = new FmLayerSetExternalRef ();
		extRef.addLayers (layers);
		
		// Set the chooser's externalRef
		itemChooser.externalRef = extRef;
	}
	
	private void createUI () {
		SketchModel sketchModel = itemChooser.sketchModel;

		FmSketchLinker linker = (FmSketchLinker) sketchModel.getSketchLinker ();
		FmModel model = linker.getUserModel ();
		
		form = new FmLayerFormFromDB (model);
		form.addListener (this);

		setLayout (new BorderLayout ());
		this.add (form, BorderLayout.CENTER);
	}
	
}




