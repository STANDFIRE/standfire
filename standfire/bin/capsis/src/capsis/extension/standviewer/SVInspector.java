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

package capsis.extension.standviewer;

import java.awt.GridLayout;

import javax.swing.JComponent;

import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import jeeb.lib.util.extensionmanager.ExtensionManager;
import capsis.commongui.projectmanager.StepButton;
import capsis.commongui.util.Tools;
import capsis.extension.AbstractStandViewer;
import capsis.kernel.GModel;
import capsis.kernel.Step;

/**
 * SVInspector is a viewer for stand dynamic introspection.
 * 
 * @author F. de Coligny -june 2001
 */
public class SVInspector extends AbstractStandViewer {
	
	static public String AUTHOR = "F. de Coligny";
	static public String VERSION = "1.0";
	

	static {
		Translator.addBundle("capsis.extension.standviewer.SVInspector");
	} 
	
	@Override
	public void init(GModel model, Step s, StepButton but) throws Exception {
		super.init (model, s, but);
		setLayout (new GridLayout (1, 1));		// important for viewer appearance
	
		update (stepButton);
	}

	/**
	 * Extension dynamic compatibility mechanism.
	 * This matchwith method checks if the extension can deal (i.e. is compatible) with the referent.
	 */
	static public boolean matchWith (Object referent) {
		try {
			if (!(referent instanceof GModel)) {return false;}
			
		} catch (Exception e) {
			Log.println (Log.ERROR, "SVInspector.matchWith ()", "Error in matchWith () (returned false)", e);
			return false;
		}
		
		return true;
	}


	
	// Needed
	public void update (StepButton sb) {
		super.update (sb);
			
		JComponent comp = Tools.getIntrospectionPanel (stepButton.getStep ().getScene ());
		
		removeAll ();
		add (comp);
		
		revalidate ();
		repaint ();
		
	}


}

