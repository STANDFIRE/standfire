package fireparadox.gui.database;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.LinkedHashMap;

import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import jeeb.lib.util.Check;
import jeeb.lib.util.JWidthLabel;
import jeeb.lib.util.Translator;
import fireparadox.model.FmModel;
import fireparadox.model.database.FmDBPerson;
import fireparadox.model.database.FmDBPlant;
import fireparadox.model.database.FmDBTeam;


/** FiPlantTeamPanel : panel for choosing a team for a plant or sample
 *
 * @author I. Lecomte - December 2007
 */
public class FmPlantTeamPanel extends JPanel implements ActionListener {


	//Entry parameters
	private FmModel model;
	private FmDBPlant plant;					//plant if update or delete
	private FmDBTeam teamLogged;				//team logged
	private int rightLevel;						//to manage user rights
	private LinkedHashMap<Long, FmDBTeam> teamList;		//map of id = FiDBTeam


	//Return information on VALID
	private FmDBTeam team;
	private String teamCode;
	private long samplingDateId;
	private String samplingDate;
	private long [] teamPersonsList;

	//entry field for UI
	private JComboBox teamComboBox;
	private LinkedHashMap<String, Long>   teamMap; 		//map of team code = id
	private LinkedHashMap<Long, FmDBPerson> personList;	//map of id = FiDBPerson
	private LinkedHashMap<String, Long>     personMap; 	//map of person name = id
	private JTextField dateField;
	private JComboBox person1, person2, person3;


	/**		Constructor
	 */
	 public FmPlantTeamPanel (FmModel _model, FmDBPlant _plant,
	 						LinkedHashMap<Long, FmDBTeam> _teamList) {

		super (new FlowLayout (FlowLayout.LEFT));

		model = _model;
		plant = _plant;
		teamList = _teamList;


		rightLevel = _model.getRightLevel();
		teamLogged = _model.getTeamLogged();


		//create empty combo boxes
		teamComboBox = new JComboBox ();
		person1 = new JComboBox ();
		person2 = new JComboBox ();
		person3 = new JComboBox ();

		teamPersonsList = new long[3];

		//LOAD PLANT information for UPDATE ou COPY
		if (plant != null) {

			//Load team info
			teamCode = new String("");
			team = plant.getTeam();
			System.arraycopy(plant.getPersonIdList(),0,teamPersonsList,0,plant.getPersonIdList().length);
			samplingDateId = plant.getSamplingDateId();
			samplingDate = plant.getSamplingDate ();

			if (team != null)
				initTeamInfo (team);					//init team data in UI entry fields
		}

		//FUEL CREATION
		else {

			team = null;
			teamCode = new String("");
			samplingDate = new String("");
			samplingDateId = -1;
		}

		//loading data from database
		loadTeams ();

		createUI ();

	}

	/**	Actions on the buttons
	 */
	public void actionPerformed (ActionEvent evt) {
		if (evt.getSource ().equals (teamComboBox))
			selectTeam ();

	}

	/**	team selection has changed
	 */
	private void selectTeam() {
		int n = teamComboBox.getSelectedIndex();
		if (n > 0) {
			String name = (String) teamComboBox.getSelectedItem ();
			Long id = teamMap.get (name);
			team = teamList.get (id);
			if (team != null) initTeamInfo (team);
			this.repaint();
		}
	}


	/**	Control the value in the ComboBoxes and the TextFields
	 */
	public boolean controlValues ()   {


		//Check the team info
		int n = teamComboBox.getSelectedIndex();
		if (n <= 0) {
			JOptionPane.showMessageDialog (this, Translator.swap ("FiPlantEditor.teamIsEmpty"),
			Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
			return false;
		}

		//Check the person info
		n = person1.getSelectedIndex();
		if (n <= 0) {
			JOptionPane.showMessageDialog (this, Translator.swap ("FiPlantEditor.personIsEmpty"),
			Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
			return false;
		}


		//team
		n = teamComboBox.getSelectedIndex();
		if (n > 0) {
			String name = (String) teamComboBox.getSelectedItem ();
			long tid = teamMap.get (name);
			team = teamList.get (tid);
		}

		//Sampling date
		samplingDate = dateField.getText ();

		if (!Check.isEmpty (dateField.getText ())) {

			int index = samplingDate.indexOf ("/");
			if (index > 0) {
				String ljj = samplingDate.substring (0,index);
				if (!Check.isInt (ljj)) {
					JOptionPane.showMessageDialog (this, Translator.swap ("FiPlantEditor.dateIsNotValid"),
					Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
					return false;
				}
				int jj = Integer.parseInt(ljj);
				if (jj<0 || jj>31) {
					JOptionPane.showMessageDialog (this, Translator.swap ("FiPlantEditor.dateIsNotValid"),
					Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
					return false;
				}
				String suite = samplingDate.substring (index+1);
				index = suite.indexOf ("/");
				if (index > 0) {
					String lmm = suite.substring (0,index);
					if (!Check.isInt (lmm)) {
						JOptionPane.showMessageDialog (this, Translator.swap ("FiPlantEditor.dateIsNotValid"),
						Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
						return false;
					}
					int mm = Integer.parseInt(lmm);
					if (mm<0 || mm>12) {
						JOptionPane.showMessageDialog (this, Translator.swap ("FiPlantEditor.dateIsNotValid"),
						Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
						return false;
					}
					String laa = suite.substring (index+1);

					if (!Check.isInt (laa)) {
						JOptionPane.showMessageDialog (this, Translator.swap ("FiPlantEditor.dateIsNotValid"),
						Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
						return false;
					}
					int aa = Integer.parseInt(laa);
					if (aa<2000) {
						JOptionPane.showMessageDialog (this, Translator.swap ("FiPlantEditor.dateIsNotValid"),
						Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
						return false;
					}
				}
			}
			else {
				JOptionPane.showMessageDialog (this, Translator.swap ("FiPlantEditor.dateIsNotValid"),
				Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
				return false;
			}
		}


		//team person name
		n = person1.getSelectedIndex();
		if (n > 0) {
			String name = (String) person1.getItemAt(n);
			Long perId = (personMap.get (name));
			teamPersonsList [0]  = perId;
		 }
		else teamPersonsList [0] = -1;

		n = person2.getSelectedIndex();
		if (n > 0) {
			String name = (String) person2.getItemAt(n);
			Long perId = (personMap.get (name));
			teamPersonsList [1]  = perId;
		 }
		else teamPersonsList [1] = -1;

		n = person3.getSelectedIndex();
		if (n > 0) {
			String name = (String) person3.getItemAt(n);
			Long perId = (personMap.get (name));
			teamPersonsList [2]  = perId;
		 }
		else teamPersonsList [2] = -1;

		return true;
	}



	/**	Initialize the GUI.
	 */
	private void createUI () {


		/*********** TEAM panel **************/

		Box box1 = Box.createVerticalBox ();

		JPanel t1 = new JPanel (new FlowLayout (FlowLayout.LEFT));
		JPanel t3 = new JPanel (new FlowLayout (FlowLayout.LEFT));
		JPanel t4 = new JPanel (new FlowLayout (FlowLayout.LEFT));
		JPanel t5 = new JPanel (new FlowLayout (FlowLayout.LEFT));
		JPanel t6 = new JPanel (new FlowLayout (FlowLayout.LEFT));

		teamComboBox.addActionListener (this);
		person1.addActionListener (this);
		person2.addActionListener (this);
		person3.addActionListener (this);

		dateField = new JTextField (10);
		dateField.setText (""+samplingDate);

		t1.add (new JWidthLabel (Translator.swap ("FiPlantEditor.teamSelection")+" :",210));
		t1.add (teamComboBox);
		t3.add (new JWidthLabel (Translator.swap ("FiPlantEditor.entryDate")+" :",210));
		t3.add (dateField);
		t3.add (new JWidthLabel (Translator.swap ("FiPlantEditor.formatDate"),50));

		t4.add (new JWidthLabel (Translator.swap ("FiPlantEditor.name")+" :",210));
		t4.add (person1);
		t5.add (new JWidthLabel (Translator.swap ("FiPlantEditor.name2")+" :",210));
		t5.add (person2);
		t6.add (new JWidthLabel (Translator.swap ("FiPlantEditor.name3")+" :",210));
		t6.add (person3);

		// NO UPDATE if plant is deleted or validated or team logged have no right for this plant
		if (plant != null) {
			if ( (plant.isDeleted())|| (plant.isValidated())
				|| ((rightLevel < 9) && (!teamLogged.getTeamCode().equals (plant.getTeam().getTeamCode()))) ) {
					teamComboBox.setEnabled(false);
					person1.setEnabled(false);
					person2.setEnabled(false);
					person3.setEnabled(false);
					dateField.setEnabled(false);

			}
		}


		box1.add(t1);
		box1.add(t3);
		box1.add(t4);
		box1.add(t5);
		box1.add(t6);

		this.add (box1);

	}


	/**	Load team list from database (done once)
	 */
	private void loadTeams () {

		teamMap  = new LinkedHashMap<String, Long> ();

		//team combo box
		teamComboBox.removeAllItems();
		teamComboBox.addItem ("--------");

		for (Iterator i = teamList.keySet().iterator(); i.hasNext ();) {
			Object cle = i.next();
			FmDBTeam t = teamList.get(cle);

			//to discard deleted teams
			if (!t.isDeleted()) {
				//team restinction
				if ( (rightLevel < 9) && (t.getTeamCode().equals(teamLogged.getTeamCode()))) {
					teamMap.put (t.getTeamCode(),t.getTeamId());
					teamComboBox.addItem (t.getTeamCode());
					teamComboBox.setSelectedItem(t.getTeamCode());
					team = t;
					initTeamInfo (t);
				}
				//if super adm no restriction
				else if (rightLevel >= 9) {
					teamMap.put (t.getTeamCode(),t.getTeamId());
					teamComboBox.addItem (t.getTeamCode());
					if (teamCode.equals(t.getTeamCode())) teamComboBox.setSelectedItem(t.getTeamCode());
				}
			}
		}
	}

	/**	load person comboBox with FiDBPerson of the FiDBTeam selected
	 */
	private void initTeamInfo (FmDBTeam team) {

		personMap = new LinkedHashMap<String, Long> ();

		teamCode = team.getTeamCode ();


		personList = team.getPersons ();
		person1.removeAllItems();
		person2.removeAllItems();
		person3.removeAllItems();
		person1.addItem ("------------");
		person2.addItem ("------------");
		person3.addItem ("------------");


		if (personList != null) {
			for (Iterator i = personList.keySet().iterator(); i.hasNext ();) {
				Object cle = i.next();
				FmDBPerson p = personList.get(cle);
				String name = p.getPersonName();
				personMap.put (name, p.getPersonId());
				person1.addItem (name);
				person2.addItem (name);
				person3.addItem (name);
				if (teamPersonsList != null) {
					if ((teamPersonsList.length > 0) && (teamPersonsList[0] == p.getPersonId()))
						person1.setSelectedItem(name);
					if ((teamPersonsList.length > 1) && (teamPersonsList[1] == p.getPersonId()))
						person2.setSelectedItem(name);
					if ((teamPersonsList.length > 2) && (teamPersonsList[2] == p.getPersonId()))
						person3.setSelectedItem(name);
				}
			}
		}
	}

	public FmDBTeam getTeam () {
		return team;
	}
	public long[] getPersons () {
		return teamPersonsList;
	}
	public String getSamplingDate () {
		return samplingDate;
	}
	public long getSamplingDateId () {
		return samplingDateId;
	}
}
