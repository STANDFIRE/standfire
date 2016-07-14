/* 
 * The Standfire model.
 *
 * Copyright (C) September 2013: F. Pimont (INRA URFM).
 * 
 * This file is part of the Standfire model and is NOT free software.
 * It is the property of its authors and must not be copied without their 
 * permission. 
 * It can be shared by the modellers of the Capsis co-development community 
 * in agreement with the Capsis charter (http://capsis.cirad.fr/capsis/charter).
 * See the license.txt file in the Capsis installation directory 
 * for further information about licenses in Capsis.
 */

package standfire.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import jeeb.lib.util.Check;
import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.IconLoader;
import jeeb.lib.util.JWidthLabel;
import jeeb.lib.util.LinePanel;
import jeeb.lib.util.MessageDialog;
import jeeb.lib.util.Translator;
import standfire.model.SFEvolutionParameters;
import capsis.commongui.EvolutionDialog;
import capsis.commongui.util.Helper;
import capsis.kernel.Step;

/**
 * SFEvolutionDialog is a dialog to set up evolution parameters in Standfire.
 * 
 * @author F. Pimont - September 2013
 */
public class SFEvolutionDialog extends EvolutionDialog implements ActionListener {

	private JTextField numberOfYears;

	private JButton ok;
	private JButton cancel;
	private JButton help;

	/**
	 * Constructor.
	 */
	public SFEvolutionDialog(Step fromStep) {
		super();

		createUI();

		pack();
		setVisible(true);
	}

	/**
	 * Action on Ok.
	 */
	private void okAction() {
		// Classic checks...
		if (!Check.isInt(numberOfYears.getText().trim())) {
			MessageDialog.print(this, Translator.swap("SFEvolutionDialog.numberOfYearsMustBeAPositiveInteger"));
			return;
		}

		int age = Check.intValue(numberOfYears.getText().trim());
		if (age <= 0) {
			MessageDialog.print(this, Translator.swap("SFEvolutionDialog.numberOfYearsMustBeAPositiveInteger"));
			return;
		}

		setEvolutionParameters(new SFEvolutionParameters(age));
		setValidDialog(true);
	}

	/**
	 * Actions on buttons
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
	 * Creates the GUI.
	 */
	private void createUI() {

		ColumnPanel c1 = new ColumnPanel();

		LinePanel l1 = new LinePanel();
		l1.add(new JWidthLabel(Translator.swap("SFEvolutionDialog.numberOfYears") + " :", 120));
		numberOfYears = new JTextField(5);
		l1.add(numberOfYears);
		l1.addStrut0();

		c1.add(l1);
		c1.addStrut0();

		// Control panel
		JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		ok = new JButton(Translator.swap("Shared.ok"));
		ok.addActionListener(this);
		ImageIcon icon = IconLoader.getIcon("ok_16.png");
		ok.setIcon(icon);

		cancel = new JButton(Translator.swap("Shared.cancel"));
		cancel.addActionListener(this);
		icon = IconLoader.getIcon("cancel_16.png");
		cancel.setIcon(icon);

		help = new JButton(Translator.swap("Shared.help"));
		help.addActionListener(this);
		icon = IconLoader.getIcon("help_16.png");
		help.setIcon(icon);

		controlPanel.add(ok);
		controlPanel.add(cancel);
		controlPanel.add(help);

		// Sets ok as default
		setDefaultButton(ok);

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(c1, BorderLayout.NORTH);
		getContentPane().add(controlPanel, BorderLayout.SOUTH);

		setTitle(Translator.swap("SFEvolutionDialog.evolutionParameters"));
		setModal(true);
	}

}
