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
package fireparadox.extension.editor;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.Collection;

import javax.swing.JPanel;

import jeeb.lib.sketch.extension.ItemEditor;
import jeeb.lib.sketch.extension.ItemEditorReferent;
import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.LinePanel;
import jeeb.lib.util.ListenedTo;
import jeeb.lib.util.Listener;
import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import capsis.lib.fire.fuelitem.FiLayerSet;
import fireparadox.gui.FmLayerForm;
import fireparadox.gui.FmLayerFormFromDB;
import fireparadox.model.FmModel;
import fireparadox.model.database.FmLayerSetFromDB;
import fireparadox.model.layerSet.FmLayer;
import fireparadox.model.layerSet.FmLayerSet;


/**	LayerSetEditor is an editor for a FiLayerSet (multi-layer polygon).
*	@author F. de Coligny - march 2009
*/
public class LayerSetEditor extends ItemEditor implements Listener {
	static {
		Translator.addBundle("fireparadox.extension.editor.LayerSetEditor");
	}
	
	// private SketchModel sketchModel;		// in superclass
	// private Item item;					// in superclass
	
	// IMPORTANT NOTE: this implementation is a workarround to edit
	// layerSets made of FiLayer with a FiLayerForm and a layerSet made
	// of FiLocalLayer with a FiLocalLayerForm
	// The editor framework in Sketch should be reviewed to do this better
	
	private FmLayerFormFromDB form;
	private FmLayerForm localForm;
	
	private JPanel scroll;		// to show the form inside
	
	
	/**	Phantom constructor.
	*/
	public LayerSetEditor () {super ();}
	
	/**	Standard constructor.
	*	Call the super constructor, then create the user interface, 
	*	then set the source, this will call refreshUI ().
	*	See super.setSource ().
	*/
	public LayerSetEditor (Object referent) throws Exception {	// referent is an ItemEditorReferent instance
		super ();
		try {
			ItemEditorReferent r = (ItemEditorReferent) referent;
			
			// May be needed for createUI
			this.sketchModel = r.sketchModel;
			this.item = r.item;
			createUI ();
			
			setSource (sketchModel, item);
			
		} catch (Exception e) {
			Log.println (Log.ERROR, "LayerSetEditor.c ()", "Error in constructor", e);
			throw e;
		}
	}
	
	
	/**	Extension dynamic compatibility mechanism.
	*	Compatibility checks are embedded in a matchwith method for each extension.
	*	The method makes checks on the referent and returns true if it can deal with it.
	*	The referent type depends on the extension type.
	*	Reminder : the matchwith method must be redefined for each extension.
	*/
	@Override
	public boolean matchWith (Object referent) {
		// This editor is for FiLayerSets with FiLayers inside
		if (!(referent instanceof FiLayerSet)) {return false;}
		return true;
	}
	
	/**	Return extension name.
	*/
	@Override
	public String getName () {return Translator.swap ("LayerSetEditor");}
	
	/**	Return version.
	*/
	@Override
	public String getVersion () {return "1.0";}

	/**	Return author name.
	*/
	@Override
	public String getAuthor () {return "F. de Coligny";}

	/**	Return short description.
	*/
	@Override
	public String getDescription () {return Translator.swap ("LayerSetEditor.description");}
	
	/**	Called by ListenedTo when something happens.
	*/
	public void somethingHappened (ListenedTo l, Object param) {
		configChanged (null);
	}

	/**	Refresh the editor's UI with the current values
	*	of the item's properties.
	*/
	@Override
	protected void refreshUI () {
		FiLayerSet layerSet = (FiLayerSet) item;
		
		if (newItem) {
			if (layerSet instanceof FmLayerSetFromDB) {

				// Create a FiLayerForm for edition, with showEditor button =
				// true
				form = new FmLayerFormFromDB ((FmModel) userModel, (FmLayerSetFromDB) layerSet, true);
				// we want to know when something happens in the form (user
				// edition)
				form.addListener(this);
				scroll.removeAll();
				scroll.add(form);

			} else { // FmLayerSet
				// Create a FiLocalLayerForm for edition, with showEditor button = true
				localForm = new FmLayerForm ((FmModel) userModel, (FmLayerSet) layerSet, true);
				// we want to know when something happens in the localForm (user edition)
				localForm.addListener (this);
				scroll.removeAll ();
				scroll.add (localForm);
				
			}	
		} else {
			//~ form.set (layerSet);
		}

	}
	
	/**	Check the user values in the JTextFields (e.g.: 'should be a 
	*	positive int'...) before updating the item.
	*	If trouble, send a message to the user and return false.
	*	If everything is correct, return true.
	*/
	@Override
	protected boolean isCorrect () {
		if (localForm != null) {
			try {
				if (!localForm.isCorrect ()) { return false; }
			} catch (Exception e) {
				// TODO FP Auto-generated catch block
				e.printStackTrace ();
			}
		} else {
			if (!form.isCorrect ()) {return false;}
		}
		
		return true;
	}

	/**	Apply the user changes (e.g. in the JTextFields) into the item.
	*	Changes the item properties.
	*/
	@Override
	protected void updateItem (Object source) {
		if (localForm != null) {
			Collection<FmLayer> layers = localForm.getLayers();
			FiLayerSet layerSet = (FiLayerSet) item;
			layerSet.setLayers (layers);
		} else {
			Collection<FmLayer> layers = form.getLayers ();
			FiLayerSet layerSet = (FiLayerSet) item;
			layerSet.setLayers (layers);
		}
		
	}
	
	/**	Create the user interface.
	*/
	@Override
	protected void createUI () {
		removeAll ();
		
		ColumnPanel c1 = new ColumnPanel ();
		c1.setMargin (5);	// fc - 8.9.2008 - lighter design, underlining
		
		c1.add (LinePanel.getTitle2 (Translator.swap ("LayerSetEditor.layerSet")));
		c1.newLine ();
		
		//~ scroll = new JScrollPane ();
		scroll = new JPanel (new GridLayout (1, 1));
		scroll.setBorder (null);
			//~ form = new FiLayerForm (layerSet, true);
			//~ // we want to know when something happens in the form (user edition)
			//~ form.addListener (this);
			//~ scroll.getViewport ().setView (form);
		c1.add (scroll);
		
		setLayout (new BorderLayout ());
		add (c1, BorderLayout.NORTH);
	}
	
}


