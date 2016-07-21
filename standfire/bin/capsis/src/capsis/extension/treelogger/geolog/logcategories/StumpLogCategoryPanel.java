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

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;

import jeeb.lib.util.Translator;
import repicea.gui.UIControlManager;
import repicea.gui.components.NumberFormatFieldFactory;
import repicea.gui.components.NumberFormatFieldFactory.JFormattedNumericField;

/**	
 * StumpPanel : panel for editing the logging rules of a StumpProduct
 * @author F. Mothe - january 2006
 */
public class StumpLogCategoryPanel extends GeoLogLogCategoryPanel {

	private static final long serialVersionUID = 20060318L;	// avoid java warning

	private JFormattedNumericField stumpHeight_m;

	/**	Default constructor.
	*/
	protected StumpLogCategoryPanel(StumpLogCategory logCategory) {
		super(logCategory);
	}

	@Override
	protected void instantiateVariables() {
		super.instantiateVariables();
		stumpHeight_m = NumberFormatFieldFactory.createNumberFormatField(10,
				NumberFormatFieldFactory.Type.Double,
				NumberFormatFieldFactory.Range.Positive,
				false);		// null are not allowed
	}
	
	@Override
	public void refreshInterface() {
		super.refreshInterface();
		stumpHeight_m.setText ("" + getTreeLogCategory().minLength_m);
	}
	

	// Initialize the panel.
	//
	@Override
	protected void createUI () {
		addProductNamePanel();

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		
		JPanel stumpHeightPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		stumpHeightPanel.add(Box.createHorizontalStrut(5));
		stumpHeightPanel.add(UIControlManager.getLabel(Translator.swap("StumpPanel.stumpHeigth_m")));
		stumpHeightPanel.add(Box.createHorizontalStrut(20));
		stumpHeightPanel.add(stumpHeight_m);
		stumpHeightPanel.add(Box.createHorizontalStrut(5));
		
		mainPanel.add(stumpHeightPanel);
		add(mainPanel);
		refreshInterface();
	}

	
	
	@Override
	public void listenTo() {
		super.listenTo();
		stumpHeight_m.getDocument().addDocumentListener(this);
	}
	
	@Override
	public void doNotListenToAnymore() {
		super.doNotListenToAnymore();
		stumpHeight_m.getDocument().removeDocumentListener(this);
	}
	
	
	@Override
	public StumpLogCategory getTreeLogCategory() {
		return (StumpLogCategory) logCategory;
	}

	
	@Override
	public void insertUpdate(DocumentEvent e) {updateValue(e);}

	@Override
	public void removeUpdate(DocumentEvent e) {updateValue(e);}

	@Override
	public void changedUpdate(DocumentEvent e) {updateValue(e);}

	@Override
	protected void updateValue(DocumentEvent e) {
		if (e.getDocument().equals(stumpHeight_m.getDocument())) {
			double value = stumpHeight_m.getValue().doubleValue();
			getTreeLogCategory().minLength_m = value;
			getTreeLogCategory().maxLength_m = value;
		} else {
			super.updateValue(e);
		}
	}

}



