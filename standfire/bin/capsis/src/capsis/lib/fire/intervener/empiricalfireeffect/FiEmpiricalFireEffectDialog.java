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

package capsis.lib.fire.intervener.empiricalfireeffect;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import jeeb.lib.util.AmapDialog;
import jeeb.lib.util.Check;
import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.MessageDialog;
import jeeb.lib.util.JWidthLabel;
import jeeb.lib.util.LinePanel;
import jeeb.lib.util.Settings;
import jeeb.lib.util.Translator;
import capsis.commongui.util.Helper;
import capsis.lib.fire.intervener.fireeffect.cambiumdamagemodel.BovaAndDickinson;
import capsis.lib.fire.intervener.fireeffect.cambiumdamagemodel.CambiumDamageModel;
import capsis.lib.fire.intervener.fireeffect.cambiumdamagemodel.NoCambiumDamageComputation;
import capsis.lib.fire.intervener.fireeffect.cambiumdamagemodel.PetersonAndRyanCambium;
import capsis.lib.fire.intervener.fireeffect.crowndamagemodel.CrownDamageModel;
import capsis.lib.fire.intervener.fireeffect.crowndamagemodel.FinneyAndMartin;
import capsis.lib.fire.intervener.fireeffect.crowndamagemodel.MichaletzAndJohnsonCrown;
import capsis.lib.fire.intervener.fireeffect.crowndamagemodel.NoCrownDamageComputation;
import capsis.lib.fire.intervener.fireeffect.crowndamagemodel.SavelandAndNeuenschwander;
import capsis.lib.fire.intervener.fireeffect.crowndamagemodel.VanWagner;
import capsis.lib.fire.intervener.fireeffect.mortalitymodel.GenericCVSDBH;
import capsis.lib.fire.intervener.fireeffect.mortalitymodel.MichaletzAndJohnsonMortality;
import capsis.lib.fire.intervener.fireeffect.mortalitymodel.MortalityModel;
import capsis.lib.fire.intervener.fireeffect.mortalitymodel.NoMortalityComputation;
import capsis.lib.fire.intervener.fireeffect.mortalitymodel.PetersonAndRyanMortality;
import capsis.lib.fire.intervener.fireeffect.mortalitymodel.SpeciesDependentCVSBLCDBH;
import capsis.lib.fire.intervener.fireeffect.mortalitymodel.SpeciesDependentCVSDBH;

/**
 * A dialog box to configure the FiEmpiricalFireEffect intervener.
 * 
 * @author F. de Coligny, F. Pimont - september 2009
 */
public class FiEmpiricalFireEffectDialog extends AmapDialog implements ActionListener {

	private Vector crownDamageModels;
	private Vector cambiumDamageModels;
	private Vector mortalityModels;


	private JRadioButton intensityAssessment;
	private JRadioButton intensityFromRos;
	private ButtonGroup rdGroup;

	private JTextField rateOfSpread;
	private JTextField fireIntensity;
	private JTextField residenceTime;
	private JTextField ambiantTemperature;
	private JTextField windVelocity;



	private JComboBox crownDamageCombo;
	private JComboBox cambiumDamageCombo;
	private JComboBox mortalityCombo;

	protected JButton ok;
	protected JButton cancel;
	protected JButton help;

	/**
	 * Constructor.
	 */
	public FiEmpiricalFireEffectDialog () {
		super ();

		initMaps ();

		createUI ();
		setTitle (Translator.swap ("FiEmpiricalFireEffectDialog"));

		setModal (true);

		// location is set by AmapDialog
		pack ();	// uses component's preferredSize
		show ();

	}

	private void initMaps () {
		// Crown damage models
		crownDamageModels = new Vector ();
		// crownDamageModels.add(new PetersonAndRyanCrown());
		crownDamageModels.add (new VanWagner ());
		crownDamageModels.add (new SavelandAndNeuenschwander ());
		crownDamageModels.add (new FinneyAndMartin ());
		crownDamageModels.add(new MichaletzAndJohnsonCrown());
		crownDamageModels.add(new NoCrownDamageComputation());

		// Cambium damage models
		cambiumDamageModels = new Vector();
		cambiumDamageModels.add(new BovaAndDickinson());
		cambiumDamageModels.add(new PetersonAndRyanCambium());
		cambiumDamageModels.add(new NoCambiumDamageComputation());
		// Mortality models
		mortalityModels = new Vector();
		mortalityModels.add(new SpeciesDependentCVSBLCDBH());
		mortalityModels.add(new SpeciesDependentCVSDBH());
		mortalityModels.add(new GenericCVSDBH());
		mortalityModels.add(new PetersonAndRyanMortality());
		mortalityModels.add(new MichaletzAndJohnsonMortality());
		mortalityModels.add(new NoMortalityComputation());
	}

	public double getFireIntensity () {
		if (intensityAssessment.isSelected()) {
			return Check.doubleValue(fireIntensity.getText().trim());
		} else {
			return -1d;
		}

	}

	public double getRateOfSpread() {
		if (intensityFromRos.isSelected()) {
			return Check.doubleValue(rateOfSpread.getText().trim());
		} else {
			return -1d;
		}

	}
	
	public boolean fromRosIsSelected() {
		return intensityFromRos.isSelected();
	}
	
	
	public double getResidenceTime () {
		return Check.doubleValue (residenceTime.getText ().trim ());
	}
	public double getAmbiantTemperature () {
		return Check.doubleValue (ambiantTemperature.getText ().trim ());
	}
	public double getWindVelocity () {
		return Check.doubleValue (windVelocity.getText ().trim ());
	}

	public CrownDamageModel getCrownDamageModel () {
		return (CrownDamageModel) crownDamageCombo.getSelectedItem ();
	}

	public CambiumDamageModel getCambiumDamageModel() {
		return (CambiumDamageModel) cambiumDamageCombo.getSelectedItem();
	}

	public MortalityModel getMortalityModel() {
		return (MortalityModel) mortalityCombo.getSelectedItem();
	}

	/**
	 * Action on ok button.
	 */ 
	private void okAction () {

		// Checks...

		if (intensityAssessment.isSelected()) {
			if (!Check.isDouble(fireIntensity.getText().trim())) {
				MessageDialog
						.print(
								this,
								Translator
										.swap("FiEmpiricalFireEffectDialog.intensityMustBeANumberGreaterOrEqualToZero"));
				return;
			}

			double _fireIntensity = Check.doubleValue(fireIntensity.getText()
					.trim());
			if (_fireIntensity < 0.0) {
				MessageDialog
						.print(this, Translator
								.swap("FiEmpiricalFireEffectDialog.intensityMustBeANumberGreaterOrEqualToZero"));
				return;
			}
			Settings.setProperty(
					"firetec.empirical.fireeffect.dialog.last.fireintensity",
					"" + _fireIntensity);

		}

		if (intensityFromRos.isSelected()) {
			if (!Check.isDouble(rateOfSpread.getText().trim())) {
				MessageDialog
						.print(
								this,
								Translator
										.swap("FiEmpiricalFireEffectDialog.intensityMustBeANumberGreaterOrEqualToZero"));
				return;
			}
			double _ros = Check.doubleValue(rateOfSpread.getText().trim());
			if (_ros < 0.0) {
				MessageDialog
						.print(this, Translator
								.swap("FiEmpiricalFireEffectDialog.rosMustBeANumberGreaterOrEqualToZero"));
				return;
			}
			Settings.setProperty(
					"firetec.empirical.fireeffect.dialog.last.rateofspread", ""
							+ _ros);
		}
		
		if (!Check.isDouble(residenceTime.getText().trim())) {
			MessageDialog
			.print(
							this,
							Translator
									.swap("FiEmpiricalFireEffectDialog.residenceTimeMustBeANumberGreaterOrEqualToZero"));
			return;
		}
		if (!Check.isDouble(ambiantTemperature.getText().trim())) {
			MessageDialog
			.print(
							this,
							Translator
									.swap("FiEmpiricalFireEffectDialog.ambiantTemperatureMustBeANumberGreaterOrEqualToZero"));
			return;
		}
		if (!Check.isDouble(windVelocity.getText().trim())) {
			MessageDialog
			.print(
							this,
							Translator
									.swap("FiEmpiricalFireEffectDialog.windVelocityMustBeANumberGreaterOrEqualToZero"));
			return;
		}
		
		double _residenceTime = Check.doubleValue(residenceTime.getText()
				.trim());
		double _ambiantTemperature = Check.doubleValue(ambiantTemperature
				.getText().trim());
		double _windVelocity = Check.doubleValue(windVelocity.getText().trim());
		
		if (_residenceTime < 0.0) {
			MessageDialog
			.print(this, Translator
							.swap("FiEmpiricalFireEffectDialog.residenceTimeMustBeANumberGreaterOrEqualToZero"));
			return;
		}
		Settings.setProperty(
				"firetec.empirical.fireeffect.dialog.last.residencetime", ""
				+ _residenceTime);
		if (_ambiantTemperature < 0.0) {
			MessageDialog
			.print(this, Translator
							.swap("FiEmpiricalFireEffectDialog.ambiantTemperatureMustBeANumberGreaterOrEqualToZero"));
			return;
		}
		Settings.setProperty(
				"firetec.empirical.fireeffect.dialog.last.ambianttemperature",
				"" + _ambiantTemperature);
		if (_windVelocity < 0.0) {
			MessageDialog
			.print(this, Translator
							.swap("FiEmpiricalFireEffectDialog.windVelocityMustBeANumberGreaterOrEqualToZero"));
			return;
		}
		Settings.setProperty(
				"firetec.empirical.fireeffect.dialog.last.windvelocity", ""
				+ _windVelocity);
		setValidDialog (true);
	}

	/**
	 * Someone hit a button.
	 */
	public void actionPerformed (ActionEvent evt) {
		if (evt.getSource().equals(intensityAssessment)) {
			fireIntensity.setEnabled(intensityAssessment.isSelected());
		}
		if (evt.getSource().equals(intensityFromRos)) {
			rateOfSpread.setEnabled(intensityFromRos.isSelected());
		}
		if (evt.getSource ().equals (ok)) {
			okAction ();
		} else if (evt.getSource ().equals (cancel)) {
			setValidDialog (false);
		} else if (evt.getSource ().equals (help)) {
			Helper.helpFor (this);
		}
	}

	/**
	 * Create the dialog box user interface.
	 */ 
	private void createUI () {

		ColumnPanel main = new ColumnPanel ();

		// 1. FireParamaters panel
		ColumnPanel p1 = new ColumnPanel(Translator
				.swap("FiEmpiricalFireEffectDialog.fireParameters"));

		LinePanel l1 = new LinePanel();
		intensityAssessment = new JRadioButton(Translator
				.swap("FiEmpiricalFireEffectDialog.fireIntensity")
				+ " :");
		intensityAssessment.addActionListener(this);
		l1.add(this.intensityAssessment);

		fireIntensity = new JTextField(5);
		try {
			String v = System
					.getProperty("firetec.empirical.fireeffect.dialog.last.fireintensity");
			if (v != null) {
				fireIntensity.setText(v);
			}
		} catch (Exception e) {
		}
		l1.add(fireIntensity);
		l1.addStrut0();
		p1.add(l1);
		LinePanel l1b = new LinePanel();
		intensityFromRos = new JRadioButton(Translator
				.swap("FiEmpiricalFireEffectDialog.intensityFromRos")
				+ " :");
		intensityFromRos.addActionListener(this);
		l1b.add(this.intensityFromRos);

		rateOfSpread = new JTextField(5);
		try {
			String v = System
			.getProperty("firetec.empirical.fireeffect.dialog.last.rateofspread");
			if (v != null) {
				rateOfSpread.setText(v);
			}
		} catch (Exception e) {
		}
		
		rateOfSpread.setEnabled(false);
		l1b.add(rateOfSpread);
		l1b.addStrut0();
		p1.add(l1b);
		rdGroup = new ButtonGroup();
		
		rdGroup.add(intensityAssessment);
		rdGroup.add(intensityFromRos);
		rdGroup.setSelected(intensityAssessment.getModel(), true);




		l1.addStrut0();
		p1.add(l1);
		LinePanel l2 = new LinePanel();
		l2.add(new JWidthLabel(Translator
				.swap("FiEmpiricalFireEffectDialog.residenceTime")
				+ " :", 160));
		residenceTime = new JTextField(5);
		try {
			String v = System
			.getProperty("firetec.empirical.fireeffect.dialog.last.residencetime");
			if (v != null) {
				residenceTime.setText(v);
			}
		} catch (Exception e) {
		}
		l2.add(residenceTime);
		l2.addStrut0();
		p1.add(l2);
		LinePanel l3 = new LinePanel();
		l3.add(new JWidthLabel(Translator
				.swap("FiEmpiricalFireEffectDialog.ambiantTemperature")
				+ " :", 160));
		ambiantTemperature = new JTextField(5);
		try {
			String v = System
			.getProperty("firetec.empirical.fireeffect.dialog.last.ambianttemperature");
			if (v != null) {
				ambiantTemperature.setText(v);
			}
		} catch (Exception e) {
		}
		l3.add(ambiantTemperature);
		l3.addStrut0();
		p1.add(l3);
		LinePanel l4 = new LinePanel();
		l4.add(new JWidthLabel(Translator
				.swap("FiEmpiricalFireEffectDialog.windVelocity")
				+ " :", 160));
		windVelocity = new JTextField(5);
		try {
			String v = System
			.getProperty("firetec.empirical.fireeffect.dialog.last.windvelocity");
			if (v != null) {
				windVelocity.setText(v);
			}
		} catch (Exception e) {
		}
		l4.add(windVelocity);
		l4.addStrut0();
		p1.add(l4);

		p1.addStrut0();
		main.add(p1);

		// 2. Models panel
		ColumnPanel p2 = new ColumnPanel(Translator
				.swap("FiEmpiricalFireEffectDialog.models"));

		LinePanel l21 = new LinePanel();
		l21.add(new JWidthLabel(Translator
				.swap("FiEmpiricalFireEffectDialog.crownDamageModel")
				+ " :", 120));
		crownDamageCombo = new JComboBox(crownDamageModels);
		l21.add(crownDamageCombo);
		l21.addStrut0();
		p2.add(l21);

		LinePanel l22 = new LinePanel();
		l22.add(new JWidthLabel(Translator
				.swap("FiEmpiricalFireEffectDialog.cambiumDamageModel")
				+ " :", 120));
		cambiumDamageCombo = new JComboBox(cambiumDamageModels);
		l22.add(cambiumDamageCombo);
		l22.addStrut0();
		p2.add(l22);

		LinePanel l23 = new LinePanel();
		l23.add(new JWidthLabel(Translator
				.swap("FiEmpiricalFireEffectDialog.mortalityModel")
				+ " :", 120));
		mortalityCombo = new JComboBox(mortalityModels);
		l23.add(mortalityCombo);
		l23.addStrut0();
		p2.add(l23);

		p2.addStrut0();
		main.add(p2);


		getContentPane ().setLayout (new BorderLayout ());
		getContentPane ().add (main, BorderLayout.NORTH);

		// 2. Control panel (ok cancel help);
		LinePanel controlPanel = new LinePanel();
		ok = new JButton (Translator.swap ("Shared.ok"));
		cancel = new JButton (Translator.swap ("Shared.cancel"));
		help = new JButton (Translator.swap ("Shared.help"));
		controlPanel.addGlue ();
		controlPanel.add (ok);
		controlPanel.add (cancel);
		controlPanel.add (help);
		controlPanel.addStrut0 ();
		ok.addActionListener (this);
		cancel.addActionListener (this);
		help.addActionListener (this);
		getContentPane ().add (controlPanel, BorderLayout.SOUTH);

		// sets ok as default (see AmapDialog)
		ok.setDefaultCapable (true);
		getRootPane ().setDefaultButton (ok);

	}

}

