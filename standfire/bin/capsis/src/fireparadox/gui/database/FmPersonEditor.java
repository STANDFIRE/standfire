package fireparadox.gui.database;

import java.awt.BorderLayout;
import java.awt.Color;
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
import fireparadox.model.database.FmDBPerson;
import fireparadox.model.database.FmDBTeam;
import fireparadox.model.database.FmDBUpdator;


/** FiPersonEditor : Creation/Modification of a FiDBPerson in DB4O database
 *
 * @author I. Lecomte - December 2007
 * Last Update by Isabelle LECOMTE      - July 2009
 */
public class FmPersonEditor extends AmapDialog implements ActionListener {

	private FmModel model;
	private FmDBUpdator bdUpdator;				//to update database

	//person and object to update
	private FmDBTeam team;
	private FmDBPerson person;
	private long personId, teamId;
	private String teamCode;
	private boolean deletePossible;		//if true, delete/undelete action is required for this team

	//Fields for user interface
	private JTextField nameField;


	private JButton ok;
	private JButton delete;
	private JButton cancel;
	private JButton help;

	//local data for retrieving user entries
	private String name;


	/**
	 * Constructor : UPDATE if team is not null
	 */
	public FmPersonEditor (FmModel _model, FmDBTeam _team, FmDBPerson _person) {

		super ();
		model = _model;
		bdUpdator = model.getBDUpdator ();
		team = _team;
		person = _person;

		if (team != null) {
			teamId = team.getTeamId();
			teamCode = team.getTeamCode();
		}

		// person UPDATE : person data loading
		if (person != null) {
			personId = person.getPersonId();
			name = person.getPersonName();
			deletePossible = true;
		}
		// person CREATION
		else {
			personId = -1;
			name = new String("");
			deletePossible = false;
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
		} else if (evt.getSource ().equals (delete)) {
			deleteAction ();
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

			name = nameField.getText ();
			boolean deleted = false;

			//PERSON CREATION
			if (person == null) {
				personId = -1;

				FmDBPerson newPerson = new FmDBPerson (personId, name, team, deleted);		//new team creation

				try {
					personId = bdUpdator.createPerson (newPerson, teamId);	//save in database
					newPerson = null;								//to clear memory

				} catch (Exception e) {
					Log.println (Log.ERROR, "FiPersonEditor ()", "error while UPDATING PERSON data base", e);
				}

			}
			//PERSON UPDATE
			else  {

				//this new person is temporary created to help the database update
				FmDBPerson newPerson= new FmDBPerson (personId, name, team, deleted);

				try {
					bdUpdator.updatePerson (person, newPerson);			//save in database
					newPerson = null;									//to clear memory

				} catch (Exception e) {
					Log.println (Log.ERROR, "FiPersonEditor ()", "error while UPDATING PERSON data base", e);
				}
			}

			setValidDialog (true);
		}
	}
	/**	Validation = save in the database
	 */
	private void deleteAction () {

		//PERSON REACTIVATION/DESACTIVATION
		if  (deletePossible == true) {
			if (person != null) {

				//this new site is temporary created to help the database update
				boolean deleted = false;
				if (!person.isDeleted()) deleted = true;
				FmDBPerson newPerson = new FmDBPerson (personId, name, team, deleted);

				try {
					bdUpdator.updatePerson (person, newPerson);			//save in database
					newPerson = null;									//to clear memory

				} catch (Exception e) {
					Log.println (Log.ERROR, "FiPersonEditor.c ()", "error while UPDATING PERSON data base", e);
				}

				setValidDialog (true);
			}
		}

	}
	/**	Control the value in the ComboBoxes and the TextFields
	 */
	private boolean controlValues ()   {
		if (Check.isEmpty (nameField.getText ())) {
			JOptionPane.showMessageDialog (this, Translator.swap ("FiPersonEditor.nameIsEmpty"),
			Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
			return false;
		}

		return true;
	}


	/**	Initialize the GUI.
	 */
	private void createUI () {

		/*********** PERSON panel **************/
		JPanel personPanel = new JPanel (new FlowLayout (FlowLayout.LEFT));
		personPanel.setPreferredSize (new Dimension (400,350));
		Box box = Box.createVerticalBox ();

		JPanel f1 = new JPanel (new FlowLayout (FlowLayout.LEFT));
		JPanel f2 = new JPanel (new FlowLayout (FlowLayout.LEFT));
		JPanel f3 = new JPanel (new FlowLayout (FlowLayout.LEFT));

		JLabel teamName = new JLabel(teamCode);
		nameField = new JTextField (20);
		nameField.setText (""+name);


		f1.add (new JWidthLabel (Translator.swap ("FiPersonEditor.team")+" :",150));
		f1.add (teamName);
		f2.add (new JWidthLabel (Translator.swap ("FiPersonEditor.name")+" :",150));
		f2.add (nameField);

		box.add(f1);
		box.add(f2);

		JLabel teamDelete;
		if ((person != null) && (person.isDeleted())) {
			teamDelete = new JLabel(Translator.swap ("FiPersonEditor.desactivate"));
			teamDelete.setForeground(Color.RED);
			f3.add (teamDelete);
			box.add(f3);
		}

		personPanel.add (box);

		/*********** CONTROL panel **************/
		JPanel controlPanel = new JPanel ();
		controlPanel.setLayout (new FlowLayout (FlowLayout.RIGHT));


		if ((person != null) && (deletePossible == true)) {
			if (person.isDeleted())
				delete = new JButton (Translator.swap ("FiPersonEditor.unsupress"));
			else
				delete = new JButton (Translator.swap ("FiPersonEditor.supress"));

			delete.addActionListener (this);
			controlPanel.add (delete);
		}

		ok = new JButton (Translator.swap ("FiPersonEditor.validate"));
		ok.addActionListener (this);
		controlPanel.add (ok);
		cancel = new JButton (Translator.swap ("Shared.cancel"));
		cancel.addActionListener (this);
		controlPanel.add (cancel);
		help = new JButton (Translator.swap ("Shared.help"));
		help.addActionListener (this);
		controlPanel.add (help);

		getContentPane ().setLayout (new BorderLayout ());
		getContentPane ().add (personPanel, BorderLayout.CENTER);
		getContentPane ().add (controlPanel, BorderLayout.SOUTH);
		setTitle (Translator.swap ("FiPersonEditor.title"));

		setModal (true);
	}

}
