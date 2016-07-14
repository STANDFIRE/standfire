package fireparadox.gui.database;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import jeeb.lib.util.AmapDialog;
import jeeb.lib.util.Translator;
import capsis.commongui.util.Helper;
import fireparadox.model.FmModel;
import fireparadox.model.FmInitialParameters;
import fireparadox.model.database.FmDBTeam;

/**	FiTeamListDialog = List of FiDBTeam objects stored in DB4O database
*
*	@author I. Lecomte - february 2008
*/
public class FmTeamListDialog extends AmapDialog implements ActionListener {

	private FmModel model;
	private FmInitialParameters settings;

	private LinkedHashMap  listTeam;				//team list extract from database
	private FmTeamResearchForm teamResultPanel;		//searching criterias
	private int rightLevel;							//to manage user rights

	private JButton close;
	private JButton help;
	private JButton add;
	private JButton modify;
	private JButton supress;

	/**	Constructor.
	*/
	public FmTeamListDialog (FmModel _model) {
		super ();
		model = _model;
		settings = model.getSettings ();
		rightLevel = _model.getRightLevel();

		createUI ();
		pack ();
		show ();
	}

	/**	Actions on the buttons
	*/
	public void actionPerformed (ActionEvent evt) {
		if (evt.getSource ().equals (close)) {
			closeAction ();
		} else if (evt.getSource ().equals (add)) {
			addTeamEntry ();
		} else if (evt.getSource ().equals (modify)) {
			modifyTeamEntry ();
		} else if (evt.getSource ().equals (supress)) {
			supressTeamEntry ();
		} else if (evt.getSource ().equals (help)) {
			Helper.helpFor (this);
		}
	}

	/**	Close was hit
	*/
	private void closeAction () {
		setVisible (false);
	}

	/**	Add a new team (for super administrator only)
	*/
	private void addTeamEntry () {
		FmTeamEditor addEntry = new FmTeamEditor (model, null, false);
		teamResultPanel.loadData();
		repaint();
	}

	/**	Modify a team
	*/
	private void modifyTeamEntry () {
		int [] selRow  = teamResultPanel.getSelectedRows ();


		if (selRow.length > 0) {
			int selectedRow = selRow[0];
			boolean deleted = teamResultPanel.getDeleted (selectedRow);
			FmDBTeam team = teamResultPanel.getTeam (selectedRow);
			if (!deleted) {
				FmTeamEditor addEntry = new FmTeamEditor (model, team, false);
				teamResultPanel.loadData();
				repaint();
			}
			else {
				JOptionPane.showMessageDialog (this, Translator.swap ("FiTeamListDialog.dataDeleted"),
				Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );

			}
		}
	}
	/**	DELETE/UNDELETE a  team (for super administrator only)
	*/
	private void supressTeamEntry () {
		int [] selRow  = teamResultPanel.getSelectedRows ();
		if (selRow.length > 0) {
			FmDBTeam team = teamResultPanel.getTeam (selRow[0]);
			FmTeamEditor addEntry = new FmTeamEditor (model, team, true);
			teamResultPanel.loadData();
			repaint();
		}
	}
	/**	Initialize the GUI.
	*/
	private void createUI () {

		JPanel teamPanel = new JPanel (new BorderLayout ());

		//Research Form
		teamResultPanel = new FmTeamResearchForm (model, rightLevel);
		teamResultPanel.setPreferredSize (new Dimension (200,350));
		if (rightLevel >=9) teamResultPanel.setPreferredSize (new Dimension (300,350));
		teamPanel.add (teamResultPanel);

		// Control panel
		JPanel controlPanel = new JPanel (new FlowLayout (FlowLayout.CENTER));
		close = new JButton (Translator.swap ("Shared.close"));
		help = new JButton (Translator.swap ("Shared.help"));

		modify = new JButton (Translator.swap ("FiTeamListDialog.modify"));
		controlPanel.add (modify);
		modify.addActionListener (this);

		//super admin
		if (rightLevel >= 9) {
			add = new JButton (Translator.swap ("FiTeamListDialog.add"));
			controlPanel.add (add);
			add.addActionListener (this);

			supress = new JButton (Translator.swap ("FiTeamListDialog.supress"));
			controlPanel.add (supress);
			supress.addActionListener (this);

		}

		controlPanel.add (close);
		controlPanel.add (help);

		close.addActionListener (this);
		help.addActionListener (this);

		getContentPane ().setLayout (new BorderLayout ());
		getContentPane ().add (teamPanel, BorderLayout.CENTER);
		getContentPane ().add (controlPanel, BorderLayout.SOUTH);

		setTitle (Translator.swap ("FiTeamListDialog.title"));

		setModal (true);
	}

}

