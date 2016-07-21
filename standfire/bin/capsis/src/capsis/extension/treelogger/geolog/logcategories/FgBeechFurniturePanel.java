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
import javax.swing.event.DocumentEvent;

import jeeb.lib.util.ColumnPanel;
import repicea.gui.components.NumberFormatFieldFactory;
import repicea.gui.components.NumberFormatFieldFactory.JFormattedNumericField;
import capsis.extension.treelogger.geolog.util.DiaUtil;


/**	FgBeechFurniturePanel : panel for editing the logging rules
*	of a FgBeechFurnitureProduct
*
*	@author F. Mothe - february 2006 / N. Robert - January 2009
*/
public class FgBeechFurniturePanel extends GeoLogLogCategoryPanel {

	private JFormattedNumericField knotUnderBarkDiameterRatio;

	/**	Default constructor.
	*/
	protected FgBeechFurniturePanel (GeoLogLogCategory product) {
		super (product);
	}

	
	protected void instantiateVariables() {
		super.instantiateVariables();
		knotUnderBarkDiameterRatio = NumberFormatFieldFactory.createNumberFormatField(10,
				NumberFormatFieldFactory.Type.Double,
				NumberFormatFieldFactory.Range.All,
				false);
	}
	

	@Override
	public void refreshInterface() {
		super.refreshInterface();
		knotUnderBarkDiameterRatio.setText ("" + getTreeLogCategory().knotUnderBarkDiameterRatio);
	}

	// Initialize the panel.
	//
	protected void createUI () {
		super.createUI();

		ColumnPanel col = new ColumnPanel ();
		col.add (DiaUtil.newTextComponent ("FgBeechFurniturePanel.knotUnderBarkDiameterRatio", WIDTH,
				knotUnderBarkDiameterRatio, NB_COL, getTreeLogCategory().knotUnderBarkDiameterRatio));

		JPanel lig = new JPanel (new FlowLayout (FlowLayout.CENTER));
		lig.add (col);
		add (lig);

	}


	@Override
	public FgBeechFurnitureLogCategory getTreeLogCategory() {
		return (FgBeechFurnitureLogCategory) this.logCategory;
	}
	
	@Override
	public void insertUpdate (DocumentEvent e) {updateValue(e);}
	
	@Override
	public void removeUpdate (DocumentEvent e) {updateValue(e);}

	@Override
	public void changedUpdate (DocumentEvent e) {updateValue(e);}

	protected void updateValue(DocumentEvent e) {
		if (e.getDocument().equals(knotUnderBarkDiameterRatio.getDocument())) {
			getTreeLogCategory().knotUnderBarkDiameterRatio = knotUnderBarkDiameterRatio.getValue().doubleValue();		
		} else {
			super.updateValue(e);
		}
	}

}

