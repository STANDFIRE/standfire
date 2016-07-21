package fireparadox.gui.database;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import jeeb.lib.util.AmapDialog;
import jeeb.lib.util.Translator;
import capsis.commongui.util.Helper;
import fireparadox.model.FmModel;
import fireparadox.model.database.FmDBTeam;


/**	FiChoiceDialog : Fuel database management, main choice of action
*
*	@author I. Lecomte - march 2008
*/
public class FmChoiceDialog extends AmapDialog implements ActionListener {

	private FmModel model;
	private int rightLevel;			//to manage user rights
	private FmDBTeam teamLogged;

	private JButton close;
	private JButton help;
	private JButton team;
	private JButton site;
	private JButton plant;
	private JButton layer;
	private JButton sample;


	/**	Constructor.
	*/
	public FmChoiceDialog (FmModel _model) throws Exception {

		super ();
		model = _model;
		rightLevel = _model.getRightLevel();
		teamLogged = _model.getTeamLogged();

		createUI ();
		pack ();
		show ();
	}

	/**	Actions on the buttons
	*/
	public void actionPerformed (ActionEvent evt) {
		if (evt.getSource ().equals (close)) {
			closeAction ();
		} else if (evt.getSource ().equals (team)) {
			teamDialog ();
		} else if (evt.getSource ().equals (site)) {
			siteDialog ();
		} else if (evt.getSource ().equals (plant)) {
			plantDialog ();
		} else if (evt.getSource ().equals (layer)) {
			layerDialog ();

		} else if (evt.getSource ().equals (sample)) {
			sampleDialog ();

		} else if (evt.getSource ().equals (help)) {
			Helper.helpFor (this);
		}
	}

	/**	Close was hit
	*/
	private void closeAction () {
		setVisible (false);
	}
	/**	Team editor
	*/
	private void teamDialog () {
		if (rightLevel >= 9) {
			FmTeamListDialog dial = new FmTeamListDialog (model);
		}
		if (rightLevel == 2) {
			FmTeamEditor dial = new FmTeamEditor (model, teamLogged, false);
		}
	}
	/**	Site
	*/
	private void siteDialog () {
		FmSiteListDialog site = new FmSiteListDialog (model);
	}
	/**	Fuel unique editor
	*/
	private void plantDialog () {

		FmPlantListDialog plant = new FmPlantListDialog (model,  1);
	}

	/**	Fuel layer editor
	*/
	private void layerDialog () {
		FmPlantListDialog layer = new FmPlantListDialog (model,  2);
	}

	/**	Fuel sample editor
	*/
	private void sampleDialog () {
		FmSampleListDialog sample = new FmSampleListDialog (model);
	}

	/**	Initialize the GUI.
	*/
	private void createUI () {

		// Choice panel
		JPanel choicePanel = new JPanel (new GridLayout(5,1) );

		//Except visitors
		if (rightLevel > 1) {
			team = new JButton (Translator.swap ("FiChoiceDialog.team"));
			choicePanel.add (team);
			team.addActionListener (this);
		}

		//for all users
		site = new JButton (Translator.swap ("FiChoiceDialog.site"));
		plant = new JButton (Translator.swap ("FiChoiceDialog.plant"));
		layer = new JButton (Translator.swap ("FiChoiceDialog.layer"));
		sample = new JButton (Translator.swap ("FiChoiceDialog.sample"));


		choicePanel.add (site);
		choicePanel.add (plant);
		choicePanel.add (layer);
		choicePanel.add (sample);

		site.addActionListener (this);
		plant.addActionListener (this);
		layer.addActionListener (this);
		sample.addActionListener (this);



		// Control panel
		JPanel controlPanel = new JPanel (new FlowLayout (FlowLayout.CENTER));
		close = new JButton (Translator.swap ("Shared.close"));
		help = new JButton (Translator.swap ("Shared.help"));
		controlPanel.add (close);
		controlPanel.add (help);



		close.addActionListener (this);
		help.addActionListener (this);

		getContentPane ().setLayout (new BorderLayout ());
		getContentPane ().add (choicePanel, BorderLayout.CENTER);
		getContentPane ().add (controlPanel, BorderLayout.SOUTH);

		setTitle (Translator.swap ("FiChoiceDialog.title"));
		setPreferredSize (new Dimension(450,300));

		setModal (true);
	}
}

