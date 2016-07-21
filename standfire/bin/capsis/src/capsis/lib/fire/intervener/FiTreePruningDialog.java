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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JTextField;

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
 * @author F. Pimont fec 2010
 */
public class FiTreePruningDialog extends FiIntervenerWithRetentionDialog implements ActionListener {

	private JTextField pruningHeight;
	
	protected JButton ok;
	protected JButton cancel;
	protected JButton help;

	/**
	 * Constructor.
	 */
	public FiTreePruningDialog() {
		super();

		createUI();
		setTitle(Translator.swap("FiTreePruningDialog"));

		setModal(true);

		// location is set by AmapDialog
		//setSize (new Dimension (400, 150)); // fc-2.2.2015
		setSize (new Dimension (420, 300)); // fc-2.2.2015
//		pack(); // uses component's preferredSize
		show();

	}

	public double getPruningHeight() {
		return Check.doubleValue(pruningHeight.getText().trim());
	}
	/**
	 * Action on ok button.
	 */
	private void okAction() {

		// Checks...
		if (!Check.isDouble(pruningHeight.getText().trim())) {
			MessageDialog.print(this,
					Translator.swap("FiPruningHeightDialog.pruningHeightMustBeANumberGreaterOrEqualToZero"));
			return;
		}

		double _pruningHeight = Check.doubleValue(pruningHeight.getText().trim());
		if (_pruningHeight < 0.0) {
			MessageDialog.print(this,
					Translator.swap("FiPruningHeightDialog.pruningHeightMustBeANumberGreaterOrEqualToZero"));
			return;
		}
		Settings.setProperty("treepruning.dialog.last.pruningheight", "" + _pruningHeight);
		if (!okActionFuelRetention()) return;
		setValidDialog(true);
	}

	/**
	 * Someone hit a button.
	 */
	public void actionPerformed(ActionEvent evt) {
		actionPerformedFuelRetention(evt);
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
		l1.add(new JWidthLabel(Translator.swap("FiTreePruningDialog.pruningHeight") + " :", 160));
		pruningHeight = new JTextField(5);
		try {
			String v = System.getProperty("treepruning.dialog.last.pruningheight");
			if (v != null) {
				pruningHeight.setText(v);
			}

		} catch (Exception e) {
		}
		l1.add(pruningHeight);
		l1.addStrut0();
		main.add(l1);
		// retention fuel panel
		main.add(this.getFuelRetentionPanel());
				
		
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

}
