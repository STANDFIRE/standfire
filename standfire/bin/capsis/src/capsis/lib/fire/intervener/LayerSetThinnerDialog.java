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

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import jeeb.lib.util.Check;
import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.JWidthLabel;
import jeeb.lib.util.LinePanel;
import jeeb.lib.util.MessageDialog;
import jeeb.lib.util.Settings;
import jeeb.lib.util.Translator;
import capsis.commongui.util.Helper;
import capsis.lib.fire.fuelitem.FiLayerSet;

/**
 * This dialog box is used to set LayerSetThinner parameters in interactive
 * context.
 * 
 * 
 * 
 * @author F. Pimont - jan 2010
 */
public class LayerSetThinnerDialog extends FiIntervenerWithRetentionDialog implements ActionListener {

	private Collection<String> treatmentTypes;

	JRadioButton fire;
	JRadioButton prescribedBurning;
	JRadioButton mechanicalClearing;
	JTextField remainingFractionAfterBurning;
	
	protected JButton ok;
	protected JButton cancel;
	protected JButton help;

	private ButtonGroup rdGroup;

	private Collection<FiLayerSet> layerSets;
	private Collection<String> layerSetNames;
	private Map<String, FiLayerSet> layerSetMap = new HashMap<String, FiLayerSet>();

	private JComboBox layerSetCombo;

	public LayerSetThinnerDialog(Collection<FiLayerSet> layerSets) {
		super();
		this.layerSets = layerSets;
		layerSetNames = new Vector<String>();
		for (FiLayerSet ls : layerSets) {
			String name = ls.getName();

			layerSetNames.add(name);
			layerSetMap.put(name, ls);
		}
		// System.out.println("layersetthinnerdiaglog after :" +
		// layerSets.size());

		createUI();
		setTitle(Translator.swap("LayerSetThinnerDialog"));
		setModal(true);
		// location is set by AmapDialog
		pack(); // uses component's preferredSize
		show();

	}

	/**
	 * Accessor for context.
	 */
	public int getClearingType() {
		if (fire.isSelected()) {
			return LayerSetThinner.FIRE;
		} else if (prescribedBurning.isSelected()) {
			return LayerSetThinner.PRESCRIBED_BURNING;
		} else {
			return LayerSetThinner.MECHANICAL_CLEARING;
		}
	}

	public FiLayerSet getLayerSetToBeThinned() {
		String name = (String) layerSetCombo.getSelectedItem();
		return layerSetMap.get(name);
	}
	public double getRemainingFractionAfterBurning() {
		return Check.doubleValue(remainingFractionAfterBurning.getText().trim());
	}
	//
	// Action on ok button.
	//
	private void okAction() {
		if (!Check.isDouble(remainingFractionAfterBurning.getText().trim())) {
			MessageDialog.print(this,
					Translator.swap("LayerSetThinnerDialog.remainingFractionAfterBurningMustBeANumberBetweenZeroAndOne"));
			return;
		}

		double val = Check.doubleValue(remainingFractionAfterBurning.getText().trim());
		if (val < 0.0 || val > 1.0) {
			MessageDialog.print(this,
					Translator.swap("LayerSetThinnerDialog.remainingFractionAfterBurningMustBeANumberBetweenZeroAndOne"));
			return;
		}
		Settings.setProperty("layersetthinner.dialog.last.remainingfractionafterburning", "" + val);
		if (!okActionFuelRetention()) return;
		setValidDialog(true);
	}

	//
	// Action on cancel button.
	//
	private void cancelAction() {
		setValidDialog(false);
	}

	/**
	 * Someone hit a button.
	 */
	public void actionPerformed(ActionEvent evt) {
		if(evt.getSource().equals(prescribedBurning)||evt.getSource().equals(this.fire)||evt.getSource().equals(this.mechanicalClearing)) {
			this.remainingFractionAfterBurning.setEnabled(this.prescribedBurning.isSelected());
			this.activityFuelRetention.setEnabled(this.mechanicalClearing.isSelected());
			if (!this.mechanicalClearing.isSelected()) {
				this.activityFuelRetention.setSelected(false);
			}
		}
		actionPerformedFuelRetention(evt);
		if (evt.getSource().equals(ok)) {
			okAction();
		} else if (evt.getSource().equals(cancel)) {
			cancelAction();
		} else if (evt.getSource().equals(help)) {
			Helper.helpFor(this);
		}
	}

	//
	// Create the dialog box user interface.
	//
	private void createUI() {

		// 1. Util panel
		ColumnPanel panel = new ColumnPanel();

		LinePanel l1 = new LinePanel();
		l1.add(new JLabel(Translator.swap("LayerSetThinnerDialog.clearingLayerSetWith") + " :"));
		l1.addGlue();
		panel.add(l1);

		LinePanel l2 = new LinePanel();
		l2.add(new JWidthLabel(Translator.swap("LayerSetThinnerDialog.thinnedLayerSet") + " : ", 100));
		layerSetCombo = new JComboBox(new Vector(layerSetNames));
		layerSetCombo.addActionListener(this);
		l2.add(layerSetCombo);
		l2.addStrut0();
		panel.add(l2);

		// Radio buttons
		LinePanel l10 = new LinePanel();
		LinePanel l11 = new LinePanel();
		LinePanel l12 = new LinePanel();

		fire = new JRadioButton(Translator.swap("LayerSetThinnerDialog.fire"));
		prescribedBurning = new JRadioButton(Translator.swap("LayerSetThinnerDialog.prescribedBurning"));
		mechanicalClearing = new JRadioButton(Translator.swap("LayerSetThinnerDialog.mechanicalClearing"));
		fire.addActionListener(this);
		prescribedBurning.addActionListener(this);
		mechanicalClearing.addActionListener(this);
		rdGroup = new ButtonGroup();
		rdGroup.add(fire);
		rdGroup.add(prescribedBurning);
		rdGroup.add(mechanicalClearing);
		

		rdGroup.setSelected(mechanicalClearing.getModel(), true);
		l10.add(fire);
		l10.addGlue();
		l11.add(prescribedBurning);
		l11.addGlue();
		l12.add(mechanicalClearing);
		l12.addGlue();
		LinePanel l13 = new LinePanel();
		l13.add(new JWidthLabel(Translator.swap("LayerSetThinnerDialog.remainingFractionAfterBurning") + " : ", 100));
		remainingFractionAfterBurning = new JTextField(5);
		remainingFractionAfterBurning.setEnabled(false);
		try {
			String v = System.getProperty("layersetthinner.dialog.last.remainingfractionafterburning");
			if (v == null) {
				v=0d+"";}
			remainingFractionAfterBurning.setText(v);
			} catch (Exception e) {
		}
		l13.add(remainingFractionAfterBurning);
		l13.addGlue();
		panel.add(l10);
		panel.add(l11);
		panel.add(l13);
		panel.add(l12);
		
		panel.addGlue();
		
		// retention fuel panel
				panel.add(this.getFuelRetentionPanel());

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(panel, BorderLayout.NORTH);

		// 2. control panel (ok cancel help);
		JPanel pControl = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		ok = new JButton(Translator.swap("Shared.ok"));
		cancel = new JButton(Translator.swap("Shared.cancel"));
		help = new JButton(Translator.swap("Shared.help"));
		pControl.add(ok);
		pControl.add(cancel);
		pControl.add(help);
		ok.addActionListener(this);
		cancel.addActionListener(this);
		help.addActionListener(this);
		getContentPane().add(pControl, BorderLayout.SOUTH);

		// sets ok as default (see AmapDialog)
		ok.setDefaultCapable(true);
		getRootPane().setDefaultButton(ok);

	}

}
