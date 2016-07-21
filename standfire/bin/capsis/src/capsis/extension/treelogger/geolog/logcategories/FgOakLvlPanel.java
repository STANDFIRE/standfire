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


/**	FgOakLvlPanel : panel for editing the logging rules
*	of a FgOakLvlProduct
*
*	@author F. Mothe - february 2006
*/
public class FgOakLvlPanel extends GeoLogLogCategoryPanel {

	private static final long serialVersionUID = 20060318L;	// avoid java warning

	private JFormattedNumericField maxKnotDiam_cm;
	private JFormattedNumericField minHeartDiam_cm;

	/**	Default constructor.
	*/
	protected FgOakLvlPanel (GeoLogLogCategory product) {
		super (product);
	}

	@Override
	protected void instantiateVariables() {
		super.instantiateVariables();
		maxKnotDiam_cm = NumberFormatFieldFactory.createNumberFormatField(10,
				NumberFormatFieldFactory.Type.Double,
				NumberFormatFieldFactory.Range.All,
				false);
		minHeartDiam_cm = NumberFormatFieldFactory.createNumberFormatField(10,
				NumberFormatFieldFactory.Type.Double,
				NumberFormatFieldFactory.Range.All,
				false);	
		
	}
	

	@Override
	public void refreshInterface() {
		super.refreshInterface();
		maxKnotDiam_cm.setText ("" + getTreeLogCategory().maxKnotDiam_cm);
		minHeartDiam_cm.setText ("" + getTreeLogCategory().minHeartDiam_cm);
	}

	// Initialize the panel.
	//
	@Override
	protected void createUI () {
		super.createUI ();

		ColumnPanel col = new ColumnPanel ();
		col.add (DiaUtil.newTextComponent ("FgOakLvlPanel.maxKnotDiam_cm", WIDTH,
				maxKnotDiam_cm, NB_COL, getTreeLogCategory().maxKnotDiam_cm));
		col.add (DiaUtil.newTextComponent ("FgOakLvlPanel.minHeartDiam_cm", WIDTH,
				minHeartDiam_cm, NB_COL, getTreeLogCategory().minHeartDiam_cm));

		JPanel lig = new JPanel (new FlowLayout (FlowLayout.CENTER));
		lig.add (col);
		add (lig);

	}


	@Override
	public FgOakLvlLogCategory getTreeLogCategory() {
		return (FgOakLvlLogCategory) this.logCategory;
	}
	
	@Override
	public void insertUpdate (DocumentEvent e) {updateValue(e);}
	
	@Override
	public void removeUpdate (DocumentEvent e) {updateValue(e);}

	@Override
	public void changedUpdate (DocumentEvent e) {updateValue(e);}

	@Override
	protected void updateValue(DocumentEvent e) {
		if (e.getDocument().equals(maxKnotDiam_cm.getDocument())) {
			getTreeLogCategory().maxKnotDiam_cm = maxKnotDiam_cm.getValue().doubleValue();		
		} else if (e.getDocument().equals(minHeartDiam_cm.getDocument())) {
			getTreeLogCategory().minHeartDiam_cm = minHeartDiam_cm.getValue().doubleValue();		
		} else {
			super.updateValue(e);
		}
	}
	
	@Override
	public void listenTo() {
		super.listenTo();
		maxKnotDiam_cm.getDocument().addDocumentListener(this);
		minHeartDiam_cm.getDocument().addDocumentListener(this);
	}

	@Override
	public void doNotListenToAnymore() {
		super.doNotListenToAnymore();
		maxKnotDiam_cm.getDocument().removeDocumentListener(this);
		minHeartDiam_cm.getDocument().removeDocumentListener(this);
	}

}

