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

package capsis.extension.treelogger.log2job;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import jeeb.lib.util.Check;
import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.JWidthLabel;
import jeeb.lib.util.LinePanel;
import jeeb.lib.util.MessageDialog;
import jeeb.lib.util.Translator;
import repicea.gui.AutomatedHelper;
import repicea.gui.UIControlManager;
import repicea.gui.UIControlManager.CommonControlID;
import repicea.simulation.treelogger.TreeLoggerParameters;
import repicea.simulation.treelogger.TreeLoggerParametersDialog;
import repicea.util.REpiceaTranslator;
import capsis.commongui.util.Helper;

/**	Log2JobDialog is a dialog box for Log2Job starter complement.
*
*	@author F. de Coligny - dec 2005
*/
//public class Log2JobDialog extends AmapDialog implements ActionListener {
public class Log2JobTreeLoggerParametersDialog extends TreeLoggerParametersDialog implements ActionListener {
	
	static {
		try {
			Method callHelp = Helper.class.getMethod("helpFor", String.class);
			String url = "http://www.inra.fr/capsis/help_"+ 
					REpiceaTranslator.getCurrentLanguage().getLocale().getLanguage() +
					"capsis/extension/treelogger/log2job/Log2JobTreeLoggerParametersDialog";
			AutomatedHelper helper = new AutomatedHelper(callHelp, new Object[]{url});
			UIControlManager.setHelpMethod(Log2JobTreeLoggerParametersDialog.class, helper);
		} catch (Exception e) {}
	}

	private JTextField numberOfLogsInTheTree;
	private JButton helpButton;

	/**	Default constructor.
	*/
	public Log2JobTreeLoggerParametersDialog(Window parent, Log2JobTreeLoggerParameters params) {
		super(parent, params);
	}

	
	@Override
	protected void instantiateVariables(TreeLoggerParameters params) {
		super.instantiateVariables(params);
		helpButton = UIControlManager.createCommonButton(CommonControlID.Help);
	}	
	
	
	@Override
	protected Log2JobTreeLoggerParameters getTreeLoggerParameters() {
		return (Log2JobTreeLoggerParameters) this.params;
	}

	
	@Override
	public void okAction() {
		if (checkConfig()) {
			setVisible(false);
			params.setParameterDialogCanceled(false);
			updateConfig();
		}
	}
	
	protected boolean checkConfig() {
		if (!Check.isInt(numberOfLogsInTheTree.getText ().trim ())) {
			MessageDialog.print (this, Translator.swap ("Log2JobDialog.numberOfLogsInTheTreeMustBeAnInteger"));
			return false;
		} else {
			return true;
		}
	}
	
	protected void updateConfig() {
		getTreeLoggerParameters().numberOfLogsInTheTree = Check.intValue(numberOfLogsInTheTree.getText().trim());
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		if (evt.getSource().equals(helpButton)) {
			helpAction ();
		} else {
			super.actionPerformed (evt);
		}
	}
	
	@Override
	protected void initUI() {

		LinePanel l1 = new LinePanel ();
		l1.add (new JWidthLabel (Translator.swap ("Log2JobDialog.numberOfLogsInTheTree")+" :", 160));
		numberOfLogsInTheTree = new JTextField (15);
		refreshInterface();
		l1.add (numberOfLogsInTheTree);
		l1.addStrut0 ();

		JPanel pControl = new JPanel (new FlowLayout (FlowLayout.RIGHT));
		pControl.add (ok);
		pControl.add (cancel);
		pControl.add (helpButton);
//		ok.addActionListener (this);
//		cancel.addActionListener (this);
//		help.addActionListener (this);

		ColumnPanel part1 = new ColumnPanel ();
		part1.add (l1);
		part1.addGlue ();

		getContentPane ().setLayout (new BorderLayout ());
		getContentPane ().add (part1, BorderLayout.NORTH);
		getContentPane ().add (pControl, BorderLayout.SOUTH);

		setTitle (Translator.swap ("Log2JobDialog"));
		pack();
		
		setMinimumSize(getSize());
		
		ok.setDefaultCapable(true);
		getRootPane().setDefaultButton(ok);
	}


	/*
	 * Useless for this class (non-Javadoc)
	 * @see repicea.simulation.treelogger.TreeLoggerParametersDialog#settingsAction()
	 */
	@Override
	protected void settingsAction () {}
	
	
	@Override
	public void listenTo() {
		cancel.addActionListener(this);
		ok.addActionListener(this);
		helpButton.addActionListener(this);
	}

	@Override
	public void doNotListenToAnymore() {
		cancel.removeActionListener(this);
		ok.removeActionListener(this);
		helpButton.removeActionListener(this);
	}

	@Override
	public void refreshInterface() {
		numberOfLogsInTheTree.setText("" + getTreeLoggerParameters().numberOfLogsInTheTree);
	}


}



