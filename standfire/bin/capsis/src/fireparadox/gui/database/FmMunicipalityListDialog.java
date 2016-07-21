package fireparadox.gui.database;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.swing.JButton;
import javax.swing.JPanel;

import jeeb.lib.util.AmapDialog;
import jeeb.lib.util.Translator;
import capsis.commongui.util.Helper;
import fireparadox.model.FmModel;
import fireparadox.model.FmInitialParameters;
import fireparadox.model.database.FmDBMunicipality;

/**	FiMunicipalityListDialog = List of FiDBMunicipality objects stored in DB4O database
*
*	@author I. Lecomte - february 2008
*/
public class FmMunicipalityListDialog extends AmapDialog implements ActionListener {

	private FmModel model;
	private FmInitialParameters settings;
	private FmMunicipalityResearchForm municipalityResultPanel;	//searching criterias
	private int rightLevel;											//to manage user rights
	private HashMap listCountry;									//country list

	private JButton close;
	private JButton help;
	private JButton add;
	private JButton modify;
	private JButton supress;

	/**	Constructor.
	*/
	public FmMunicipalityListDialog (FmModel _model) {
		super ();
		model = _model;
		rightLevel = _model.getRightLevel();
		this.settings = model.getSettings ();

		createUI ();

		// location is set by AmapDialog
		pack ();
		show ();
	}

	/**	Actions on the buttons
	*/
	public void actionPerformed (ActionEvent evt) {
		if (evt.getSource ().equals (close)) {
			closeAction ();
		} else if (evt.getSource ().equals (add)) {
			addMunicipalityEntry ();
		} else if (evt.getSource ().equals (supress)) {
			supressMunicipalityEntry ();
		} else if (evt.getSource ().equals (modify)) {
			modifyMunicipalityEntry ();
		} else if (evt.getSource ().equals (help)) {
			Helper.helpFor (this);
		}
	}

	/**	Close was hit
	*/
	private void closeAction () {
		setVisible (false);
	}

	/**	Add a new Municipality with Municipality editor
	*/
	private void addMunicipalityEntry () {
		LinkedHashMap  listCountry = municipalityResultPanel.getCountryList ();
		FmMunicipalityEditor addEntry = new FmMunicipalityEditor (model, listCountry,  null, false);
		municipalityResultPanel.loadData();
		repaint();
	}

	/**	Modify a Municipality with Municipality editor
	*/
	private void modifyMunicipalityEntry () {
		int [] selRow  = municipalityResultPanel.getSelectedRows ();
		if (selRow.length > 0) {
			FmDBMunicipality municipalitySelected = municipalityResultPanel.getMunicipality (selRow[0]);
			LinkedHashMap  listCountry = municipalityResultPanel.getCountryList ();
			FmMunicipalityEditor addEntry = new FmMunicipalityEditor (model, listCountry,  municipalitySelected, false);
			municipalityResultPanel.loadData();
			repaint();
		}
	}
	/**	DELETE/UNDELETE a  team (for super administrator only)
	*/
	private void supressMunicipalityEntry () {
		int [] selRow  = municipalityResultPanel.getSelectedRows ();
		if (selRow.length > 0) {
			FmDBMunicipality municipalitySelected = municipalityResultPanel.getMunicipality (selRow[0]);
			LinkedHashMap  listCountry = municipalityResultPanel.getCountryList ();
			FmMunicipalityEditor addEntry = new FmMunicipalityEditor (model, listCountry,  municipalitySelected, true);
			municipalityResultPanel.loadData();
			repaint();
		}
	}
	/**	Initialize the GUI.
	*/
	private void createUI () {

		JPanel municipalityPanel = new JPanel (new BorderLayout ());

		//Research Form
		municipalityResultPanel = new FmMunicipalityResearchForm (model, rightLevel);
		municipalityResultPanel.setPreferredSize (new Dimension (600,350));
		municipalityPanel.add (municipalityResultPanel);

		// Control panel
		JPanel controlPanel = new JPanel (new FlowLayout (FlowLayout.CENTER));


		if (rightLevel >= 2) {
			add = new JButton (Translator.swap ("FiMunicipalityListDialog.add"));
			controlPanel.add (add);
			add.addActionListener (this);

			modify = new JButton (Translator.swap ("FiMunicipalityListDialog.modify"));
			controlPanel.add (modify);
			modify.addActionListener (this);
		}


		if (rightLevel >= 9) {

			supress = new JButton (Translator.swap ("FiMunicipalityListDialog.supress"));
			controlPanel.add (supress);
			supress.addActionListener (this);
		}

		close = new JButton (Translator.swap ("Shared.close"));
		help = new JButton (Translator.swap ("Shared.help"));
		controlPanel.add (close);
		controlPanel.add (help);
		close.addActionListener (this);
		help.addActionListener (this);

		getContentPane ().setLayout (new BorderLayout ());
		getContentPane ().add (municipalityPanel, BorderLayout.CENTER);
		getContentPane ().add (controlPanel, BorderLayout.SOUTH);

		setTitle (Translator.swap ("FiMunicipalityListDialog.title"));

		setModal (true);
	}

}

