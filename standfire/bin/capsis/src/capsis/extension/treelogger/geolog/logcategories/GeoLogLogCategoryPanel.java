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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import jeeb.lib.util.Translator;
import repicea.gui.UIControlManager;
import repicea.gui.components.NumberFormatFieldFactory;
import repicea.gui.components.NumberFormatFieldFactory.JFormattedNumericField;
import repicea.simulation.treelogger.TreeLogCategoryPanel;
import capsis.extension.treelogger.geolog.GeoLogTreeData;
import capsis.extension.treelogger.geolog.util.DiaUtil;

/**	GeoLogProductPanel : panel for editing the basic logging rules
*	of a GeoLogProduct
*
*	@author F. Mothe - january 2006
*/
public class GeoLogLogCategoryPanel extends TreeLogCategoryPanel<GeoLogLogCategory> implements DocumentListener, ItemListener {

	static {
		Translator.addBundle("capsis.extension.treelogger.geolog.GeoLog");
	}
	
	protected static final int NB_COL = 10;
	protected static final int WIDTH = 200;

	private JFormattedNumericField maxCount;
	private JCheckBox acceptCrown;
	private JFormattedNumericField minLength_m;
	private JFormattedNumericField maxLength_m;
	private JFormattedNumericField minDiam_cm;
	private JFormattedNumericField diamRelPos;
	private JCheckBox diamOverBark;
	private JFormattedNumericField priceModelSlope;
	private JFormattedNumericField priceModelIntercept;
	private JFormattedNumericField[] minRandAttributes;

	/**	
	 * Default constructor.
	 */
	protected GeoLogLogCategoryPanel(GeoLogLogCategory logCategory) {
		super(logCategory);
		instantiateVariables();
		createUI();
		refreshInterface();
	}

	/**
	 * This method is called just before creating the GUI.
	 */
	protected void instantiateVariables() {
		minRandAttributes = new JFormattedNumericField[GeoLogTreeData.NB_RANDOM_ATTRIBUTES];
		for (int i = 0; i < GeoLogTreeData.NB_RANDOM_ATTRIBUTES; i++) {
			minRandAttributes[i] = NumberFormatFieldFactory.createNumberFormatField(10,
					NumberFormatFieldFactory.Type.Double,
					NumberFormatFieldFactory.Range.All,
					false);
		}
		maxCount = NumberFormatFieldFactory.createNumberFormatField(10,
					NumberFormatFieldFactory.Type.Integer,
					NumberFormatFieldFactory.Range.All,
					false);
		acceptCrown = new JCheckBox();
		minLength_m = NumberFormatFieldFactory.createNumberFormatField(10,
				NumberFormatFieldFactory.Type.Double,
				NumberFormatFieldFactory.Range.Positive,
				false);
		maxLength_m = NumberFormatFieldFactory.createNumberFormatField(10,
				NumberFormatFieldFactory.Type.Double,
				NumberFormatFieldFactory.Range.All,
				false);
		minDiam_cm = NumberFormatFieldFactory.createNumberFormatField(10,
				NumberFormatFieldFactory.Type.Double,
				NumberFormatFieldFactory.Range.All,
				false);
		diamRelPos = NumberFormatFieldFactory.createNumberFormatField(10,
				NumberFormatFieldFactory.Type.Double,
				NumberFormatFieldFactory.Range.StrictlyPositive,
				false);
		diamOverBark = new JCheckBox();
		priceModelSlope = NumberFormatFieldFactory.createNumberFormatField(10,
				NumberFormatFieldFactory.Type.Double,
				NumberFormatFieldFactory.Range.All,
				false);
		priceModelIntercept = NumberFormatFieldFactory.createNumberFormatField(10,
				NumberFormatFieldFactory.Type.Double,
				NumberFormatFieldFactory.Range.All,
				false);
	}

	public boolean isValidData() {return true;}


	protected void createUI () {
		addProductNamePanel() ;
		add(createMainPanel());
	}


	
	// Initialize geoPanel
	//
	protected JPanel createMainPanel() {
		JPanel col = new JPanel();
		col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
		
		col.add (DiaUtil.newTextComponent ("GeoLogProductPanel.maxCount", WIDTH,
				maxCount, NB_COL, getTreeLogCategory().maxCount));
		col.add (DiaUtil.newTextComponent ("GeoLogProductPanel.acceptCrown", WIDTH,
				acceptCrown, NB_COL, getTreeLogCategory().acceptCrown));
		col.add (DiaUtil.newTextComponent ("GeoLogProductPanel.minLength_m", WIDTH,
				minLength_m, NB_COL, getTreeLogCategory().minLength_m));
		col.add (DiaUtil.newTextComponent ("GeoLogProductPanel.maxLength_m", WIDTH,
				maxLength_m, NB_COL, getTreeLogCategory().maxLength_m));
		col.add (DiaUtil.newTextComponent ("GeoLogProductPanel.minDiam_cm", WIDTH,
				minDiam_cm, NB_COL, getTreeLogCategory().minDiam_cm));
		col.add (DiaUtil.newTextComponent ("GeoLogProductPanel.diamRelPos", WIDTH,
				diamRelPos, NB_COL, getTreeLogCategory().diamRelPos));
		col.add (DiaUtil.newTextAlone ("GeoLogProductPanel.diamRelPosComment",
				true, WIDTH));
		col.add (DiaUtil.newTextComponent ("GeoLogProductPanel.diamOverBark", WIDTH,
				diamOverBark = new JCheckBox(), NB_COL,
				getTreeLogCategory().diamOverBark));

		{
			Box l1 = Box.createHorizontalBox ();
			l1.add (new JLabel (Translator.swap ("GeoLogProductPanel.priceModel") + " :"));
			l1.add (Box.createHorizontalGlue ());

			l1.add (new JLabel (Translator.swap ("GeoLogProductPanel.priceModelSlope")));
			l1.add (Box.createHorizontalStrut (5));
			l1.add (DiaUtil.newTextComponent (null, 0,
				priceModelSlope, 8, getTreeLogCategory().priceModel.slope));

			l1.add (new JLabel (Translator.swap ("GeoLogProductPanel.priceModelIntercept")));
			l1.add (Box.createHorizontalStrut (5));
			l1.add (DiaUtil.newTextComponent (null, 0,
				priceModelIntercept, 8, getTreeLogCategory().priceModel.intercept));

			col.add (l1);
		}

		col.add (DiaUtil.newTextAlone ("GeoLogProductPanel.minRandAttributes",
				true, WIDTH));
		{
			Box l1 = Box.createHorizontalBox ();
			for (int n=0; n<GeoLogTreeData.NB_RANDOM_ATTRIBUTES; ++n) {
				l1.add (new JLabel (Translator.swap (
						"GeoLogProductPanel.attributeNumber") + " " + (n+1) + " :"));
				l1.add (Box.createHorizontalStrut (5));
				l1.add (DiaUtil.newTextComponent (null, 0,
					minRandAttributes[n], 5, getTreeLogCategory().minRandAttributes [n]));
			}
			col.add (l1);
		}


		JPanel lig = new JPanel(new FlowLayout(FlowLayout.CENTER));
		lig.add(col);
		
		return lig;
	}

	protected final void addProductNamePanel() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		JPanel lig = new JPanel (new FlowLayout(FlowLayout.CENTER));
//		lig.add(UIControlManager.getLabel("<html><b>" + getLogCategoryName() + "</b></html>"));
		lig.add(UIControlManager.getLabel(MessageID.LogGradeName));
		lig.add(Box.createHorizontalStrut(15));
		nameTextField.setColumns(10);
		nameTextField.setHorizontalAlignment(SwingConstants.RIGHT);
		lig.add(nameTextField);
		add(lig);
		add(Box.createVerticalStrut(2));
		Border etched = BorderFactory.createEtchedBorder ();
		setBorder(BorderFactory.createTitledBorder(etched, Translator.swap ("GeoLogProductPanel.textRules")));
	}


	@Override
	public void refreshInterface() {
		super.refreshInterface();
		maxCount.setText ("" + getTreeLogCategory().maxCount);
		acceptCrown.setSelected (getTreeLogCategory().acceptCrown);
		minLength_m.setText ("" + getTreeLogCategory().minLength_m);
		maxLength_m.setText ("" + getTreeLogCategory().maxLength_m);
		minDiam_cm.setText ("" + getTreeLogCategory().minDiam_cm);
		diamRelPos.setText ("" + getTreeLogCategory().diamRelPos);
		diamOverBark.setSelected (getTreeLogCategory().diamOverBark);
		priceModelSlope.setText ("" + getTreeLogCategory().priceModel.slope);
		priceModelIntercept.setText ("" + getTreeLogCategory().priceModel.intercept);
		for (int n=0; n<GeoLogTreeData.NB_RANDOM_ATTRIBUTES; ++n) {
			minRandAttributes [n].setText ("" + getTreeLogCategory().minRandAttributes [n]);
		}
	}
	
	@Override
	public void listenTo() {
		super.listenTo();
		maxCount.getDocument().addDocumentListener(this);
		acceptCrown.addItemListener(this);
		minLength_m.getDocument().addDocumentListener(this);
		maxLength_m.getDocument().addDocumentListener(this);
		minDiam_cm.getDocument().addDocumentListener(this);
		diamRelPos.getDocument().addDocumentListener(this);
		diamOverBark.addItemListener(this);
		priceModelSlope.getDocument().addDocumentListener(this);
		priceModelIntercept.getDocument().addDocumentListener(this);
		for (int i = 0; i < minRandAttributes.length; i++) {
			minRandAttributes[i].getDocument().addDocumentListener(this);
		}
	}
	
	
	@Override
	public void doNotListenToAnymore() {
		super.doNotListenToAnymore();
		maxCount.getDocument().removeDocumentListener(this);
		acceptCrown.removeItemListener(this);
		minLength_m.getDocument().removeDocumentListener(this);
		maxLength_m.getDocument().removeDocumentListener(this);
		minDiam_cm.getDocument().removeDocumentListener(this);
		diamRelPos.getDocument().removeDocumentListener(this);
		diamOverBark.removeItemListener(this);
		priceModelSlope.getDocument().removeDocumentListener(this);
		priceModelIntercept.getDocument().removeDocumentListener(this);
		for (int i = 0; i < minRandAttributes.length; i++) {
			minRandAttributes[i].getDocument().removeDocumentListener(this);
		}
	}

	
	@Override
	public void itemStateChanged (ItemEvent e) {
		if (e.getSource().equals(acceptCrown)) {
			getTreeLogCategory().acceptCrown = acceptCrown.isSelected();
		} else if (e.getSource().equals(diamOverBark)) {
			getTreeLogCategory().diamOverBark = diamOverBark.isSelected();
		}
	}

	@Override
	public void insertUpdate (DocumentEvent e) {updateValue(e);}
	
	@Override
	public void removeUpdate (DocumentEvent e) {updateValue(e);}

	@Override
	public void changedUpdate (DocumentEvent e) {updateValue(e);}

	protected void updateValue(DocumentEvent e) {
		if (e.getDocument().equals(maxCount.getDocument())) {
			getTreeLogCategory().maxCount = maxCount.getValue().intValue();		
		} else if (e.getDocument().equals(minLength_m.getDocument())) {
			getTreeLogCategory().minLength_m = minLength_m.getValue().doubleValue();
		} else if (e.getDocument().equals(maxLength_m.getDocument())) {
			getTreeLogCategory().maxLength_m = maxLength_m.getValue().doubleValue();
		} else if (e.getDocument().equals(minDiam_cm.getDocument())) {
			getTreeLogCategory().minDiam_cm = minDiam_cm.getValue().doubleValue();
		} else if (e.getDocument().equals(diamRelPos.getDocument())) {
			getTreeLogCategory().diamRelPos = diamRelPos.getValue().doubleValue();
		} else if (e.getDocument().equals(priceModelSlope.getDocument())) {
			getTreeLogCategory().priceModel.slope = priceModelSlope.getValue().doubleValue();
		} else if (e.getDocument().equals(priceModelIntercept.getDocument())) {
			getTreeLogCategory().priceModel.intercept = priceModelIntercept.getValue().doubleValue();
		} else {
			for (int i = 0; i < minRandAttributes.length; i++) {
				if (e.getDocument().equals(minRandAttributes[i].getDocument())) {
					getTreeLogCategory().minRandAttributes[i] = minRandAttributes[i].getValue().doubleValue();
					break;
				}
			}
		}
	}

	
	
}

