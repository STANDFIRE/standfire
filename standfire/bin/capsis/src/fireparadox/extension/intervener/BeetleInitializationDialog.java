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

package fireparadox.extension.intervener;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JTextField;

import jeeb.lib.util.AmapDialog;
import jeeb.lib.util.Check;
import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.JWidthLabel;
import jeeb.lib.util.LinePanel;
import jeeb.lib.util.MessageDialog;
import jeeb.lib.util.Settings;
import jeeb.lib.util.Translator;
import capsis.commongui.util.Helper;

/**
 * A dialog box to configure the FiTreePruning intervener.
 * 
 * @author R. Parsons nov 2010
 */
public class BeetleInitializationDialog extends AmapDialog implements
		ActionListener {

	private BeetleInitialization intervener;

	private JTextField initialProportionAttacked; // between 0 and 1
	private JTextField maxSpreadDistance; // m
	private JTextField param_a;
	private JTextField param_b;
	private JTextField param_c;

	protected JButton ok;
	protected JButton cancel;
	protected JButton help;

	/**
	 * Constructor.
	 */
	public BeetleInitializationDialog(BeetleInitialization intervener) {
		super();
		this.intervener = intervener;
		createUI();
		setTitle(Translator.swap("BeetleInitializationDialog"));

		setModal(true);

		// location is set by AmapDialog
		pack(); // uses component's preferredSize
		show();

	}

	/**
	 * Action on ok button.
	 */
	private void okAction() {

		// Checks...
		if (!Check.isDouble(initialProportionAttacked.getText().trim())) {
			MessageDialog
			.print(this,
					Translator
					.swap("BeetleInitializationDialog.initialProportionAttackedMustBeANumberBetweenZeroAndOne"));
			return;
		}

		double _initialProportionAttacked = Check
				.doubleValue(initialProportionAttacked.getText().trim());
		if (_initialProportionAttacked < 0.0
				|| _initialProportionAttacked > 1.0) {
			MessageDialog
					.print(this,
							Translator
									.swap("BeetleInitializationDialog.initialProportionAttackedMustBeANumberBetweenZeroAndOne"));
			return;
		}
		Settings.setProperty("beetle.dialog.last.initialProportionAttacked", ""
				+ _initialProportionAttacked);

		// Checks...
		if (!Check.isDouble(maxSpreadDistance.getText().trim())) {
			MessageDialog
					.print(this,
							Translator
									.swap("BeetleInitializationDialog.maxSpreadDistanceMustBeGreaterToZero"));
			return;
		}

		double _maxSpreadDistance = Check.doubleValue(maxSpreadDistance
				.getText().trim());
		if (_maxSpreadDistance < 0.0) {
			MessageDialog
					.print(this,
							Translator
									.swap("BeetleInitializationDialog.maxSpreadDistanceMustBeGreaterToZero"));
			return;
		}
		Settings.setProperty("beetle.dialog.last.maxSpreadDistance", ""
				+ _maxSpreadDistance);

		// Checks...
		if (!Check.isDouble(param_a.getText().trim())) {
			MessageDialog
					.print(this,
							Translator
									.swap("BeetleInitializationDialog.param_aMustBeGreaterToZero"));
			return;
		}

		double _param_a = Check.doubleValue(param_a.getText().trim());
		if (_param_a < 0.0) {
			MessageDialog
					.print(this,
							Translator
									.swap("BeetleInitializationDialog.param_aMustBeGreaterToZero"));
			return;
		}
		Settings.setProperty("beetle.dialog.last.param_a", "" + _param_a);
		if (!Check.isDouble(param_b.getText().trim())) {
			MessageDialog
					.print(this,
							Translator
									.swap("BeetleInitializationDialog.param_bMustBeGreaterToZero"));
			return;
		}

		double _param_b = Check.doubleValue(param_b.getText().trim());
		if (_param_b < 0.0) {
			MessageDialog
					.print(this,
							Translator
									.swap("BeetleInitializationDialog.param_bMustBeGreaterToZero"));
			return;
		}
		Settings.setProperty("beetle.dialog.last.param_b", "" + _param_b);

		if (!Check.isDouble(param_c.getText().trim())) {
			MessageDialog
					.print(this,
							Translator
									.swap("BeetleInitializationDialog.param_cMustBeGreaterToZero"));
			return;
		}

		double _param_c = Check.doubleValue(param_c.getText().trim());
		if (_param_c < 0.0) {
			MessageDialog
					.print(this,
							Translator
									.swap("BeetleInitializationDialog.param_cMustBeGreaterToZero"));
			return;
		}
		Settings.setProperty("beetle.dialog.last.param_c", "" + _param_c);

		setValidDialog(true);
	}

	/**
	 * Someone hit a button.
	 */
	public void actionPerformed(ActionEvent evt) {
		if (evt.getSource().equals(ok)) {
			okAction();
		} else if (evt.getSource().equals(cancel)) {
			setValidDialog(false);
		} else if (evt.getSource().equals(help)) {
			Helper.helpFor(this);
		}
	}

	/**
	 * Create the dialog box user interface.
	 */
	private void createUI() {

		ColumnPanel main = new ColumnPanel();

		LinePanel l1 = new LinePanel();
		l1.add(new JWidthLabel(Translator
				.swap("BeetleInitializationDialog.initialProportionAttacked")
				+ " :", 160));
		initialProportionAttacked = new JTextField(5);
		String v = Settings.getProperty(
				"beetle.dialog.last.initialProportionAttacked", ""
						+ intervener.initialProportionAttacked);
		initialProportionAttacked.setText(v);

		l1.add(initialProportionAttacked);
		l1.addStrut0();
		main.add(l1);

		LinePanel l2 = new LinePanel();
		l2.add(new JWidthLabel(Translator
				.swap("BeetleInitializationDialog.maxSpreadDistance") + " :",
				160));
		maxSpreadDistance = new JTextField(5);
		v = Settings.getProperty("beetle.dialog.last.maxSpreadDistance", ""
				+ intervener.maxSpreadDistance);
		maxSpreadDistance.setText(v);

		l2.add(maxSpreadDistance);
		l2.addStrut0();
		main.add(l2);

		LinePanel l3 = new LinePanel();
		l3.add(new JWidthLabel(Translator
				.swap("BeetleInitializationDialog.param_a") + " :", 160));
		param_a = new JTextField(5);
		v = Settings.getProperty("beetle.dialog.last.param_a", ""
				+ intervener.param_a);
		param_a.setText(v);

		l3.add(param_a);
		l3.addStrut0();
		main.add(l3);

		LinePanel l4 = new LinePanel();
		l4.add(new JWidthLabel(Translator
				.swap("BeetleInitializationDialog.param_b") + " :", 160));
		param_b = new JTextField(5);
		v = Settings.getProperty("beetle.dialog.last.param_b", ""
				+ intervener.param_b);
		param_b.setText(v);

		l4.add(param_b);
		l4.addStrut0();
		main.add(l4);

		LinePanel l5 = new LinePanel();
		l5.add(new JWidthLabel(Translator
				.swap("BeetleInitializationDialog.param_c") + " :", 160));
		param_c = new JTextField(5);
		v = Settings.getProperty("beetle.dialog.last.param_c", ""
				+ intervener.param_c);
		param_c.setText(v);

		l5.add(param_c);
		l5.addStrut0();
		main.add(l5);

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(main, BorderLayout.NORTH);

		// 2. Control panel (ok cancel help);
		LinePanel controlPanel = new LinePanel();
		ok = new JButton(Translator.swap("Shared.ok"));
		cancel = new JButton(Translator.swap("Shared.cancel"));
		help = new JButton(Translator.swap("Shared.help"));
		controlPanel.addGlue();
		controlPanel.add(ok);
		controlPanel.add(cancel);
		controlPanel.add(help);
		controlPanel.addStrut0();
		ok.addActionListener(this);
		cancel.addActionListener(this);
		help.addActionListener(this);
		getContentPane().add(controlPanel, BorderLayout.SOUTH);

		// sets ok as default (see AmapDialog)
		ok.setDefaultCapable(true);
		getRootPane().setDefaultButton(ok);

	}

	public double getInitialProportionAttacked() {
		return Check.doubleValue(initialProportionAttacked.getText().trim());
	}

	public double getMaxSpreadDistance() {
		return Check.doubleValue(maxSpreadDistance.getText().trim());
	}

	public double getParam_a() {
		return Check.doubleValue(param_a.getText().trim());
	}

	public double getParam_b() {
		return Check.doubleValue(param_b.getText().trim());
	}

	public double getParam_c() {
		return Check.doubleValue(param_c.getText().trim());
	}

}
