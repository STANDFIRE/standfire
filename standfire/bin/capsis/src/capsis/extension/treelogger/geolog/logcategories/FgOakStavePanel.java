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


/**	FgOakStavePanel : panel for editing the logging rules
*	of a FgOakStaveProduct
*
*	@author F. Mothe - february 2006
*/
public class FgOakStavePanel extends GeoLogLogCategoryPanel {

	private static final long serialVersionUID = 20060318L;	// avoid java warning

	private JFormattedNumericField staveWidth_cm;

	/**	Default constructor.
	*/
	public FgOakStavePanel (GeoLogLogCategory product) {
		super (product);
	}

	@Override
	protected void instantiateVariables() {
		super.instantiateVariables();
		staveWidth_cm = NumberFormatFieldFactory.createNumberFormatField(10,
				NumberFormatFieldFactory.Type.Double,
				NumberFormatFieldFactory.Range.All,
				false); 
	}

	@Override
	public void refreshInterface() {
		super.refreshInterface();
		staveWidth_cm.setText ("" + getTreeLogCategory().staveWidth_cm);
	}

	// Initialize the panel.
	//
	@Override
	protected void createUI () {
		super.createUI ();

		ColumnPanel col = new ColumnPanel ();
		col.add (DiaUtil.newTextComponent ("FgOakStavePanel.staveWidth_cm", WIDTH,
				staveWidth_cm, NB_COL, getTreeLogCategory().staveWidth_cm));

		JPanel lig = new JPanel (new FlowLayout (FlowLayout.CENTER));
		lig.add (col);
		add (lig);

	}


	@Override
	public FgOakStaveLogCategory getTreeLogCategory() {
		return (FgOakStaveLogCategory) this.logCategory;
	}
	
	
	@Override
	public void insertUpdate (DocumentEvent e) {updateValue(e);}
	
	@Override
	public void removeUpdate (DocumentEvent e) {updateValue(e);}

	@Override
	public void changedUpdate (DocumentEvent e) {updateValue(e);}

	protected void updateValue(DocumentEvent e) {
		if (e.getDocument().equals(staveWidth_cm.getDocument())) {
			getTreeLogCategory().staveWidth_cm = staveWidth_cm.getValue().doubleValue();		
		} else {
			super.updateValue(e);
		}
	}

	
	@Override
	public void listenTo() {
		super.listenTo();
		staveWidth_cm.getDocument().addDocumentListener(this);
	}
	
	
	@Override
	public void doNotListenToAnymore() {
		super.doNotListenToAnymore();
		staveWidth_cm.getDocument().removeDocumentListener(this);
	}

	
}

