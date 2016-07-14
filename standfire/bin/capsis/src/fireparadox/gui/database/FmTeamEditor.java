package fireparadox.gui.database;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.LinkedHashMap;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
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
import fireparadox.model.database.FmDBPerson;
import fireparadox.model.database.FmDBTeam;
import fireparadox.model.database.FmDBUpdator;


/** FiTeamEditor : Creation/Modification of a FiDBTeam in DB4O database
 *
 * @author I. Lecomte - December 2007
 * Last Update by Isabelle LECOMTE      - July 2009
 */
public class FmTeamEditor extends AmapDialog implements ActionListener {

	private FmModel model;
	private FmDBCommunicator bdCommunicator;		//to read database
	private FmDBUpdator bdUpdator;				//to update database

	//team object to update
	private FmDBTeam team;
	private long teamId;
	private int rightLevel;
	private boolean deleteAction;					//if true, delete/undelete action is required for this team

	//Fields for user interface
	private JTextField codeField;
	private JTextField newPassField;
	private JComboBox personComboBox;

	private JButton ok;
	private JButton personManaging;
	private JButton password;
	private JButton cancel;
	private JButton help;

	//local data for retrieving user entries
	private String code;
	private LinkedHashMap <String, Long> personMap;
	private LinkedHashMap <Long, FmDBPerson> personList;
	private String newPass, verifPass;

	/**
	 * Constructor : UPDATE if team is not null
	 */
	public FmTeamEditor (FmModel _model, FmDBTeam _team, boolean _deleteAction) {

		super ();
		model = _model;
		rightLevel = _model.getRightLevel();
		team = _team;
		deleteAction = _deleteAction;

		bdCommunicator = model.getBDCommunicator ();
		bdUpdator = model.getBDUpdator ();



		// team UPDATE : team data loading
		if (team != null) {
			teamId = team.getTeamId();
			code = team.getTeamCode();
			personComboBox = new JComboBox();
			loadPersons();
		}
		// team CREATION
		else {
			teamId = -1;
			code = new String("");
		}

		createUI ();
		pack ();
		show ();
	}

	/**	Actions on the buttons
	 */
	public void actionPerformed (ActionEvent evt) {
		if (evt.getSource ().equals (ok)) {
			validateAction ();
		} else if (evt.getSource ().equals (personManaging)) {
			personAdding ();
		} else if (evt.getSource ().equals (password)) {
			passChanging ();
		} else if (evt.getSource ().equals (cancel)) {
			setValidDialog (false);
		} else if (evt.getSource ().equals (help)) {
			Helper.helpFor (this);
		}
	}
	/**	Person list managing
	 */
	private void personAdding() {

		//Check the person selection
		if (team != null) {
			int n = personComboBox.getSelectedIndex();
			if (n > 0) {
				String code = (String) personComboBox.getSelectedItem ();
				Long id = personMap.get (code);
				FmDBPerson person = personList.get (id);
			   	FmPersonEditor dial  = new FmPersonEditor (model, team, person);
			   	loadPersons();
				repaint();
			}
			else {
			   	FmPersonEditor dial  = new FmPersonEditor (model, team, null);
			   	loadPersons();
				repaint();
			}
		}
	}
	/**	Password changing
	 */
	private void passChanging() {

		FmTeamPassEditor dial  = new FmTeamPassEditor (model, team, rightLevel);
		loadPersons();

	}
	/**	Validation = save in the database
	 */
	private void validateAction () {

		//TEAM REACTIVATION/DESACTIVATION
		if  (deleteAction == true) {
			if (team != null) {

				//this new site is temporary created to help the database update
				boolean deleted = false;
				if (!team.isDeleted()) deleted = true;
				FmDBTeam newTeam = new FmDBTeam (teamId, code,  deleted);

				try {
					bdUpdator.updateTeam (team, newTeam);			//save in database
					newTeam = null;									//to clear memory

				} catch (Exception e) {
					Log.println (Log.ERROR, "FiTeamEditor.c ()", "error while UPDATING TEAM data base", e);
				}

				setValidDialog (true);
			}
		}
		else {
			if (controlValues ()) {		//if control OK

				code = codeField.getText ();
				boolean deleted = false;

				//TEAM CREATION
				if (team == null) {
					teamId = -1;
					FmDBTeam newTeam = new FmDBTeam (teamId, code, deleted);		//new team creation

					try {
						teamId = bdUpdator.createTeam (newTeam);	//save in database


						//PASSWORD
						boolean ok = false;

						verifPass = model.getTeamPassword();
						ok = bdUpdator.updateAdminPW (teamId, verifPass, newPass);			//save in database

						if (!ok) {
							JOptionPane.showMessageDialog (this, Translator.swap ("FiTeamPassEditor.error"),
							Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
						}



					} catch (Exception e) {
						Log.println (Log.ERROR, "FiTeamEditor.c ()", "error while UPDATING TEAM data base", e);
					}

				}
				//TEAM UPDATE
				else  {

					//this new site is temporary created to help the database update
					FmDBTeam newTeam = new FmDBTeam (teamId, code, deleted);
					try {
						bdUpdator.updateTeam (team, newTeam);			//save in database


					} catch (Exception e) {
						Log.println (Log.ERROR, "FiTeamEditor.c ()", "error while UPDATING TEAM data base", e);
					}
				}



				setValidDialog (true);
			}
		}

	}
	/**	Control the value in the ComboBoxes and the TextFields
	 */
	private boolean controlValues ()   {

		if (deleteAction) return true;

		if (Check.isEmpty (codeField.getText ())) {
			JOptionPane.showMessageDialog (this, Translator.swap ("FiTeamEditor.teamCodeIsEmpty"),
			Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
			return false;
		}
		if (team == null) {
			if (Check.isEmpty (newPassField.getText ())) {
				JOptionPane.showMessageDialog (this, Translator.swap ("FiTeamEditor.passIsEmpty"),
				Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
				return false;
			}
		}
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


		codeField = new JTextField (10);
		codeField.setText (""+code);
		newPassField = new JTextField (10);
		newPassField.setText ("");



		f1.add (new JWidthLabel (Translator.swap ("FiTeamEditor.teamCode")+" :",150));
		f1.add (codeField);
		box.add(f1);

		if (deleteAction) {
			codeField.setEnabled(false);
		}
		else if (team == null) {
			f3.add (new JWidthLabel (Translator.swap ("FiTeamEditor.pass")+" :",150));
			f3.add (newPassField);
			box.add(f3);
		}

		//person combo box only if team UPDATE
		if ((team != null) && (deleteAction == false)) {
			f2.add (new JWidthLabel (Translator.swap ("FiTeamEditor.list")+" :",150));
			f2.add (personComboBox);
			box.add(f2);
		}


		teamPanel.add (box);




		/*********** CONTROL panel **************/
		JPanel controlPanel = new JPanel ();
		controlPanel.setLayout (new FlowLayout (FlowLayout.CENTER));

		//only if team exist if is possible to manage persons list
		if ((team != null) && (deleteAction == false)) {
			personManaging = new JButton (Translator.swap ("FiTeamEditor.person"));
			personManaging.addActionListener (this);
			controlPanel.add (personManaging);


			password = new JButton (Translator.swap ("FiTeamEditor.password"));
			password.addActionListener (this);
			controlPanel.add (password);

		}

		//if team exist and delte/undelete action
		if ((team != null) && (deleteAction == true)) {
			if (team.isDeleted())
				ok = new JButton (Translator.swap ("FiTeamEditor.unsupress"));
			else
				ok = new JButton (Translator.swap ("FiTeamEditor.supress"));
		}
		else
			ok = new JButton (Translator.swap ("FiTeamEditor.validate"));


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
		setTitle (Translator.swap ("FiTeamEditor.title"));

		setModal (true);
	}

	private void loadPersons () {

		personList = new LinkedHashMap <Long, FmDBPerson>  ();
		personMap = new LinkedHashMap <String, Long>  ();

		try {
			personList = bdCommunicator.getPersons (teamId, team);

			personComboBox.removeAllItems();
			personComboBox.addItem ("----- New person -----");
			for (Iterator i = personList.keySet().iterator(); i.hasNext ();) {
				Object cle = i.next();
				FmDBPerson p = personList.get(cle);
				personMap.put (p.getPersonName(),p.getPersonId());
				personComboBox.addItem (p.getPersonName());
			}
			personComboBox.setSelectedIndex(0);

		} catch (Exception e) {
					Log.println (Log.ERROR, "FiTeamEditor.reLoadPersons ()", "error while opening PERSON data base", e);
		}


	}

}
