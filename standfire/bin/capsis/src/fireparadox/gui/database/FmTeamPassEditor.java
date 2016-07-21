package fireparadox.gui.database;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import jeeb.lib.util.AmapDialog;
import jeeb.lib.util.Check;
import jeeb.lib.util.JWidthLabel;
import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import capsis.commongui.util.Helper;
import fireparadox.model.FmModel;
import fireparadox.model.database.FmDBCommunicator;
import fireparadox.model.database.FmDBTeam;
import fireparadox.model.database.FmDBUpdator;


/** FiTeamPassEditor : Creation/Modification of a password for FiDBTeam in DB4O database
 *
 * @author I. Lecomte - December 2007
 * Last Update by Isabelle LECOMTE      - July 2009
 */
public class FmTeamPassEditor extends AmapDialog implements ActionListener {

	private FmModel model;
	private FmDBCommunicator bdCommunicator;		//to read database
	private FmDBUpdator bdUpdator;				//to update database

	//team object to update
	private FmDBTeam team;
	private long teamId;
	private String teamCode;
	private int rightLevel;

	//Fields for user interface
	private JTextField verifPassField;
	private JTextField newPassField;


	private JButton ok;
	private JButton cancel;
	private JButton help;

	//local data for retrieving user entries
	private String newPass, verifPass;


	/**
	 * Constructor : UPDATE if team is not null
	 */
	public FmTeamPassEditor (FmModel _model, FmDBTeam _team, int _rightLevel) {

		super ();
		model = _model;
		bdCommunicator = model.getBDCommunicator ();
		bdUpdator = model.getBDUpdator ();
		team = _team;
		teamCode = team.getTeamCode();
		rightLevel = _rightLevel;
		teamId = team.getTeamId();


		createUI ();
		pack ();
		show ();
	}

	/**	Actions on the buttons
	 */
	public void actionPerformed (ActionEvent evt) {
		if (evt.getSource ().equals (ok)) {
			validateAction ();
		} else if (evt.getSource ().equals (cancel)) {
			setValidDialog (false);
		} else if (evt.getSource ().equals (help)) {
			Helper.helpFor (this);
		}
	}

	/**	Validation = save in the database
	 */
	private void validateAction () {
		if (controlValues ()) {		//if control OK

			boolean ok = false;

			try {
				if (rightLevel < 9) {
					ok = bdUpdator.updateTeamPW (teamId, verifPass, newPass);			//save in database
				}
				else {
					ok = bdUpdator.updateAdminPW (teamId, verifPass, newPass);			//save in database
				}

				if (!ok) {
					JOptionPane.showMessageDialog (this, Translator.swap ("FiTeamPassEditor.error"),
					Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
				}
				else {
					JOptionPane.showMessageDialog (this, Translator.swap ("FiTeamPassEditor.ok"),
					Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
					setValidDialog (true);
				}

			} catch (Exception e) {
				Log.println (Log.ERROR, "FiTeamEditor.c ()", "error while UPDATING TEAM data base", e);
			}

		}
	}
	/**	Control the value in the ComboBoxes and the TextFields
	 */
	private boolean controlValues ()   {

		if (rightLevel < 9) {
			//check is old password is filled
			if (Check.isEmpty (verifPassField.getText ())) {
				JOptionPane.showMessageDialog (this, Translator.swap ("FiTeamPassEditor.ancPassIsEmpty"),
				Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
				return false;
			}
			//check if old password is valid
			boolean passworkOk = false;
			verifPass = verifPassField.getText ();
			try {
				passworkOk = bdCommunicator.checkTeamPass (teamId, verifPass);
				if (!passworkOk) {
					JOptionPane.showMessageDialog (this, Translator.swap ("FiTeamPassEditor.ancPassIsFalse"),
					Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
					return false;
				}
			} catch (Exception e) {
				Log.println (Log.ERROR, "FiTeamEditor.c ()", "error while READING TEAM data base", e);
			}
		}

		else {

			verifPass = model.getTeamPassword();

		}

		//check is new password is filled
		if (Check.isEmpty (newPassField.getText ())) {
			JOptionPane.showMessageDialog (this, Translator.swap ("FiTeamPassEditor.newPassIsEmpty"),
			Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
			return false;
		}
		newPass = newPassField.getText ();
		return true;
	}


	/**	Initialize the GUI.
	 */
	private void createUI () {

		/*********** TEAM panel **************/
		JPanel teamPanel = new JPanel (new FlowLayout (FlowLayout.LEFT));
		teamPanel.setPreferredSize (new Dimension (400,350));
		Box box = Box.createVerticalBox ();

		JPanel f1 = new JPanel (new FlowLayout (FlowLayout.LEFT));
		JPanel f2 = new JPanel (new FlowLayout (FlowLayout.LEFT));
		JPanel f3 = new JPanel (new FlowLayout (FlowLayout.LEFT));
		JPanel f4 = new JPanel (new FlowLayout (FlowLayout.LEFT));


		JLabel codeField = new JLabel(teamCode);
		verifPassField = new JTextField (10);
		verifPassField.setText ("");
		newPassField = new JTextField (10);
		newPassField.setText ("");

		f1.add (new JWidthLabel (Translator.swap ("FiTeamPassEditor.teamCode")+" :",150));
		f1.add (codeField);
		box.add(f1);

		//super admin don't have to produice old password
		if (rightLevel < 9) {
			f2.add (new JWidthLabel (Translator.swap ("FiTeamPassEditor.ancPass")+" :",150));
			f2.add (verifPassField);
			box.add(f2);

		}


		f3.add (new JWidthLabel (Translator.swap ("FiTeamPassEditor.newPass")+" :",150));
		f3.add (newPassField);

		box.add(f3);


		teamPanel.add (box);

		/*********** CONTROL panel **************/
		JPanel controlPanel = new JPanel ();
		controlPanel.setLayout (new FlowLayout (FlowLayout.RIGHT));


		ok = new JButton (Translator.swap ("FiTeamPassEditor.validate"));
		ok.addActionListener (this);
		controlPanel.add (ok);
		cancel = new JButton (Translator.swap ("Shared.cancel"));
		cancel.addActionListener (this);
		controlPanel.add (cancel);
		help = new JButton (Translator.swap ("Shared.help"));
		help.addActionListener (this);
		controlPanel.add (help);

		getContentPane ().setLayout (new BorderLayout ());
		getContentPane ().add (teamPanel, BorderLayout.CENTER);
		getContentPane ().add (controlPanel, BorderLayout.SOUTH);
		setTitle (Translator.swap ("FiTeamPassEditor.title"));

		setModal (true);
	}



}
