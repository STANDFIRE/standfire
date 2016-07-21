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

package capsis.lib.fire.intervener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JTextField;

import jeeb.lib.util.AmapDialog;
import jeeb.lib.util.Check;
import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.JWidthLabel;
import jeeb.lib.util.LinePanel;
import jeeb.lib.util.MemoPanel;
import jeeb.lib.util.MessageDialog;
import jeeb.lib.util.Settings;
import jeeb.lib.util.Translator;

/**
 * A dialog box to help configure intervener with activityFuelretention option.
 * 
 * @author F. Pimont 19/11/2015
 */
public class FiIntervenerWithRetentionDialog extends AmapDialog implements ActionListener {
	protected JCheckBox activityFuelRetention; // fuel transferred to a layerSet
												// or removed
	private JTextField residualFuelHeight; // height of the slash fuel, when not
											// removed
	private JTextField residualFuelCoverFraction; // coverFraction of the slash
													// fuel, when not removed
	private JTextField residualFuelCharacteristicSize; // clump of the slash
														// fuel, when not
														// removed
	private JTextField residualFuelMoisture; // moisture content for this litter

	/**
	 * Constructor.
	 */
	public FiIntervenerWithRetentionDialog() {
		super();
	}

	public double getResidualFuelHeight() {
		return Check.doubleValue(residualFuelHeight.getText().trim());
	}

	public double getResidualFuelCoverFraction() {
		return Check.doubleValue(residualFuelCoverFraction.getText().trim());
	}

	public boolean isActivityFuelRetented() {
		return activityFuelRetention.isSelected();
	}

	public double getResidualFuelCharacteristicSize() {
		return Check.doubleValue(residualFuelCharacteristicSize.getText().trim());
	}

	public double getResidualFuelMoisture() {
		return Check.doubleValue(residualFuelMoisture.getText().trim());
	}

	/**
	 * Action on ok button.
	 */
	protected boolean okActionFuelRetention() {

		if (activityFuelRetention.isSelected()) {
			if (!Check.isDouble(residualFuelHeight.getText().trim())) {
				MessageDialog.print(this,
						Translator.swap("FiIntervenerWithRetentionDialog.residualFuelHeightMustBeANumberGreaterOrEqualToZero"));
				return false;
			}
			double _residualFuelHeight = Check.doubleValue(residualFuelHeight.getText().trim());
			if (_residualFuelHeight < 0.0) {
				MessageDialog.print(this,
						Translator.swap("FiIntervenerWithRetentionDialog.residualFuelHeightMustBeANumberGreaterOrEqualToZero"));
				return false;
			}
			Settings.setProperty("intervenerwithretention.dialog.last.residualfuelheight", "" + _residualFuelHeight);

			if (!Check.isDouble(residualFuelCoverFraction.getText().trim())) {
				MessageDialog.print(this, Translator
						.swap("FiIntervenerWithRetentionDialog.residualFuelCoverFractionMustBeANumberGreaterOrEqualToZero"));
				return false;
			}
			double _residualFuelCoverFraction = Check.doubleValue(residualFuelCoverFraction.getText().trim());
			if ((_residualFuelCoverFraction < 0.0) || _residualFuelCoverFraction > 1.0) {
				MessageDialog.print(this,
						Translator.swap("FiIntervenerWithRetentionDialog.residualFuelCoverFractionMustBeBetweenZeroAndOne"));
				return false;
			}
			Settings.setProperty("intervenerwithretention.dialog.last.residualfuelcoverfraction", "" + _residualFuelCoverFraction);

			if (!Check.isDouble(residualFuelCharacteristicSize.getText().trim())) {
				MessageDialog.print(this, Translator
						.swap("FiIntervenerWithRetentionDialog.residualFuelCharacteristicSizeMustBeANumberGreaterOrEqualToZero"));
				return false;
			}
			double _residualFuelCharacteristicSize = Check.doubleValue(residualFuelCharacteristicSize.getText().trim());
			if (_residualFuelCharacteristicSize < 0.0) {
				MessageDialog.print(this, Translator
						.swap("FiIntervenerWithRetentionDialog.residualFuelCharacteristicSizeMustBeANumberGreaterOrEqualToZero"));
				return false;
			}
			Settings.setProperty("intervenerwithretention.dialog.last.residualfuelcharacteristicsize", ""
					+ _residualFuelCharacteristicSize);
			double _residualFuelMoisture = Check.doubleValue(residualFuelMoisture.getText().trim());
			if (_residualFuelMoisture < 0.0) {
				MessageDialog.print(this,
						Translator.swap("FiIntervenerWithRetentionDialog.residualFuelMoistureMustBeANumberGreaterOrEqualToZero"));
				return false;
			}
			Settings.setProperty("intervenerwithretention.dialog.last.residualfuelmoisture", "" + _residualFuelMoisture);

		}
		return true;
	}

	/**
	 * Someone hit a button.
	 */
	public void actionPerformedFuelRetention(ActionEvent evt) {
		if (evt.getSource().equals(activityFuelRetention)) {
			residualFuelHeight.setEnabled(activityFuelRetention.isSelected());
			residualFuelCoverFraction.setEnabled(activityFuelRetention.isSelected());
			residualFuelCharacteristicSize.setEnabled(activityFuelRetention.isSelected());
			residualFuelMoisture.setEnabled(activityFuelRetention.isSelected());
		}
	}

	/**
	 * Create the end of dialog box user interface.
	 */
	protected ColumnPanel getFuelRetentionPanel() {

		ColumnPanel main = new ColumnPanel();

		LinePanel l2 = new LinePanel();
		activityFuelRetention = new JCheckBox(Translator.swap("FiIntervenerWithRetentionDialog.activityFuelRetention"), false);
		activityFuelRetention.addActionListener(this);
		l2.add(activityFuelRetention);
		l2.addStrut0();
		main.add(l2);
		MemoPanel memo = new MemoPanel(
				Translator.swap("FiIntervenerWithRetentionDialog.selectIfFuelCollectedDuringInterventionShouldBeAddedToTheLitter"));
		main.add(memo);

		LinePanel l3 = new LinePanel();
		l3.add(new JWidthLabel(Translator.swap("FiIntervenerWithRetentionDialog.residualFuelHeight") + " :", 160));
		residualFuelHeight = new JTextField(5);
		try {
			String v = System.getProperty("intervenerwithretention.dialog.last.residualfuelheight");
			if (v == null) {
				v = FiIntervenerWithRetention.HEIGHT+"";
			}
			residualFuelHeight.setText(v);

		} catch (Exception e) {
		}
		l3.add(residualFuelHeight);
		residualFuelHeight.setEnabled(false);
		l3.addStrut0();
		main.add(l3);

		LinePanel l4 = new LinePanel();
		l4.add(new JWidthLabel(Translator.swap("FiIntervenerWithRetentionDialog.residualFuelCoverFraction") + " :", 160));
		residualFuelCoverFraction = new JTextField(5);
		try {
			String v = System.getProperty("intervenerwithretention.dialog.last.residualfuelcoverfraction");
			if (v == null) {
				v = FiIntervenerWithRetention.COVERFRACTION+"";
			}
			residualFuelCoverFraction.setText(v);

		} catch (Exception e) {
		}
		l4.add(residualFuelCoverFraction);
		residualFuelCoverFraction.setEnabled(false);
		l4.addStrut0();
		main.add(l4);

		LinePanel l5 = new LinePanel();
		l5.add(new JWidthLabel(Translator.swap("FiIntervenerWithRetentionDialog.residualFuelCharacteristicSize") + " :", 160));
		residualFuelCharacteristicSize = new JTextField(5);
		try {
			String v = System.getProperty("intervenerwithretention.dialog.last.residualfuelcharacteristicsize");
			if (v == null) {
				v = FiIntervenerWithRetention.CHARACTERISTICSIZE+"";
			}
			residualFuelCharacteristicSize.setText(v);

		} catch (Exception e) {
		}
		l5.add(residualFuelCharacteristicSize);
		residualFuelCharacteristicSize.setEnabled(false);
		l5.addStrut0();
		main.add(l5);
		LinePanel l6 = new LinePanel();
		l6.add(new JWidthLabel(Translator.swap("FiIntervenerWithRetentionDialog.residualFuelMoisture") + " :", 160));
		residualFuelMoisture = new JTextField(5);
		try {
			String v = System.getProperty("intervenerwithretention.dialog.last.residualfuelmoisture");
			if (v == null) {
				v = FiIntervenerWithRetention.MOISTURE+"";
			}
			residualFuelMoisture.setText(v);
		} catch (Exception e) {
		}
		l6.add(residualFuelMoisture);
		residualFuelMoisture.setEnabled(false);
		l6.addStrut0();
		main.add(l6);
		return main;

	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO FP Auto-generated method stub
		
	}

}
