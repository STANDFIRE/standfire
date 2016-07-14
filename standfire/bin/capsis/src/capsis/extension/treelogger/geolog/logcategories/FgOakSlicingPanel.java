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


/**	FgOakSlicingPanel : panel for editing the logging rules
*	of a FgOakSlicingProduct
*
*	@author F. Mothe - january 2006
*/
public class FgOakSlicingPanel extends GeoLogLogCategoryPanel {

	private static final long serialVersionUID = 20060318L;	// avoid java warning

	private JFormattedNumericField maxDiam_cm;
	private JFormattedNumericField maxKnotDiam_cm;
	private JFormattedNumericField knotHeartRatio;

	/**	Default constructor.
	*/
	protected FgOakSlicingPanel (GeoLogLogCategory product) {
		super (product);
	}

	@Override
	protected void instantiateVariables() {
		super.instantiateVariables();
		maxDiam_cm = NumberFormatFieldFactory.createNumberFormatField(10,
				NumberFormatFieldFactory.Type.Double,
				NumberFormatFieldFactory.Range.All,
				false);
		maxKnotDiam_cm = NumberFormatFieldFactory.createNumberFormatField(10,
				NumberFormatFieldFactory.Type.Double,
				NumberFormatFieldFactory.Range.All,
				false);
		knotHeartRatio = NumberFormatFieldFactory.createNumberFormatField(10,
				NumberFormatFieldFactory.Type.Double,
				NumberFormatFieldFactory.Range.All,
				false);
	}
	

	@Override
	public void refreshInterface() {
		super.refreshInterface();
		maxDiam_cm.setText ("" + getTreeLogCategory().maxDiam_cm);
		maxKnotDiam_cm.setText ("" + getTreeLogCategory().maxKnotDiam_cm);
		knotHeartRatio.setText ("" + getTreeLogCategory().knotHeartRatio);
	}

	// Initialize the panel.
	//
	protected void createUI () {
		super.createUI ();

		ColumnPanel col = new ColumnPanel ();
		col.add (DiaUtil.newTextComponent ("FgOakSlicingPanel.maxDiam_cm", WIDTH,
				maxDiam_cm, NB_COL, getTreeLogCategory().maxDiam_cm));
		col.add (DiaUtil.newTextComponent ("FgOakSlicingPanel.maxKnotDiam_cm", WIDTH,
				maxKnotDiam_cm, NB_COL, getTreeLogCategory().maxKnotDiam_cm));
		col.add (DiaUtil.newTextComponent ("FgOakSlicingPanel.knotHeartRatio", WIDTH,
				knotHeartRatio, NB_COL, getTreeLogCategory().knotHeartRatio));

		JPanel lig = new JPanel (new FlowLayout (FlowLayout.CENTER));
		lig.add (col);
		add (lig);

	}

	@Override
	public FgOakSlicingLogCategory getTreeLogCategory() {
		return (FgOakSlicingLogCategory) this.logCategory;
	}
	
	
	@Override
	public void listenTo() {
		super.listenTo();
		maxDiam_cm.getDocument().addDocumentListener(this);
		maxKnotDiam_cm.getDocument().addDocumentListener(this);
		knotHeartRatio.getDocument().addDocumentListener(this);
	}
	
	
	@Override
	public void doNotListenToAnymore() {
		super.doNotListenToAnymore();
		maxDiam_cm.getDocument().removeDocumentListener(this);
		maxKnotDiam_cm.getDocument().removeDocumentListener(this);
		knotHeartRatio.getDocument().removeDocumentListener(this);
	}

	
	
	@Override
	public void insertUpdate (DocumentEvent e) {updateValue(e);}
	
	@Override
	public void removeUpdate (DocumentEvent e) {updateValue(e);}

	@Override
	public void changedUpdate (DocumentEvent e) {updateValue(e);}

	@Override
	protected void updateValue(DocumentEvent e) {
		if (e.getDocument().equals(maxDiam_cm.getDocument())) {
			getTreeLogCategory().maxDiam_cm = maxDiam_cm.getValue().doubleValue();		
		} else if (e.getDocument().equals(maxKnotDiam_cm.getDocument())) {
			getTreeLogCategory().maxKnotDiam_cm = maxKnotDiam_cm.getValue().doubleValue();		
		} else if (e.getDocument().equals(knotHeartRatio.getDocument())) {
			getTreeLogCategory().knotHeartRatio = knotHeartRatio.getValue().doubleValue();		
		} else {
			super.updateValue(e);
		}
	}

	
}

