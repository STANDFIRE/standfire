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

package capsis.extension.treelogger.geolog.logcategories;

import java.awt.FlowLayout;

import javax.swing.JPanel;

import jeeb.lib.util.Translator;
import repicea.gui.UIControlManager;

/**	TopLogPanel : panel for editing the logging rules
*	of a TopLogProduct
*
*	@author F. Mothe - january 2006
*/
public class TopLogCategoryPanel extends GeoLogLogCategoryPanel {

	private static final long serialVersionUID = 20060318L;	// avoid java warning

	/**	Default constructor.
	*/
	protected TopLogCategoryPanel(GeoLogLogCategory logCategory) {
		super(logCategory);
		nameTextField.setEditable(false);
	}

	

	// Initialize the panel.
	//
	@Override
	protected void createUI () {
		addProductNamePanel ();
		
		JPanel lig = new JPanel (new FlowLayout (FlowLayout.CENTER));
		lig.add (UIControlManager.getLabel(Translator.swap("TopLogPanel.textInfo")));
		add(lig);
	}

	@Override
	public TopLogCategory getTreeLogCategory() {
		return (TopLogCategory) logCategory;
	}

}



