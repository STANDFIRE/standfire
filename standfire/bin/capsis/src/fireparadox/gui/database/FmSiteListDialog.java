package fireparadox.gui.database;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import jeeb.lib.util.AmapDialog;
import jeeb.lib.util.Translator;
import capsis.commongui.util.Helper;
import fireparadox.model.FmModel;
import fireparadox.model.FmInitialParameters;
import fireparadox.model.database.FmDBCommunicator;

/**	FiSiteListDialog = List of FiDBSite objects stored in DB4O database
*
*	@author I. Lecomte - december 2007
*/
public class FmSiteListDialog extends AmapDialog implements ActionListener {

	private FmModel model;
	private FmInitialParameters settings;
	private FmDBCommunicator bdCommunicator;

	private FmSiteResearchForm siteResultPanel;	//searching criterias
	private int rightLevel;							//to manage user rights

	private JButton close;
	private JButton help;
	private JButton add;
	private JButton modify;
	private JButton supress;

	/**	Constructor.
	*/
	public FmSiteListDialog (FmModel _model) {
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
			addSiteEntry ();
		} else if (evt.getSource ().equals (supress)) {
			supressSiteEntry ();
		} else if (evt.getSource ().equals (modify)) {
			modifySiteEntry ();
		} else if (evt.getSource ().equals (help)) {
			Helper.helpFor (this);
		}
	}

	/**	Close was hit
	*/
	private void closeAction () {
		setVisible (false);
	}

	/**	CREATE a new site  (for super administrator only)
	*/
	private void addSiteEntry () {
		FmSiteEditor addEntry = new FmSiteEditor (model,  -1,  false);
		siteResultPanel.loadAllData();
		repaint();
	}

	/**	UPDATE a site
	*/
	private void modifySiteEntry () {
		int [] selRow  = siteResultPanel.getSelectedRows ();
		if (selRow.length > 0) {
			int selectedRow = selRow[0];
			boolean deleted = siteResultPanel.getDeleted (selectedRow);
			if (!deleted) {
				long siteId = siteResultPanel.getSiteId (selectedRow);
				FmSiteEditor addEntry = new FmSiteEditor (model, siteId,  false);
				siteResultPanel.loadAllData();
				repaint();
			}
			else {
				JOptionPane.showMessageDialog (this, Translator.swap ("FiTeamListDialog.dataDeleted"),
			Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );

			}
		}
	}

	/**	DELETE/UNDELETE a site (for super administrator only)
	*/
	private void supressSiteEntry () {
		int [] selRow  = siteResultPanel.getSelectedRows ();
		if (selRow.length > 0) {
			long siteId = siteResultPanel.getSiteId (selRow[0]);
			FmSiteEditor addEntry = new FmSiteEditor (model,  siteId,  true);
			siteResultPanel.loadAllData();
			repaint();
		}
	}
	/**	Initialize the GUI.
	*/
	private void createUI () {

		JPanel sitePanel = new JPanel (new BorderLayout ());

		//Research Form
		siteResultPanel = new FmSiteResearchForm (model, rightLevel);
		siteResultPanel.setPreferredSize (new Dimension (600,350));
		sitePanel.add (siteResultPanel);

		// Control panel
		JPanel controlPanel = new JPanel (new FlowLayout (FlowLayout.CENTER));

		//ADMIN
		if (rightLevel >= 2) {
			modify = new JButton (Translator.swap ("FiSiteListDialog.modify"));
			controlPanel.add (modify);
			modify.addActionListener (this);

			add = new JButton (Translator.swap ("FiSiteListDialog.add"));
			controlPanel.add (add);
			add.addActionListener (this);
		}


		if (rightLevel >= 9) {
			supress = new JButton (Translator.swap ("FiSiteListDialog.supress"));
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
		getContentPane ().add (sitePanel, BorderLayout.CENTER);
		getContentPane ().add (controlPanel, BorderLayout.SOUTH);

		setTitle (Translator.swap ("FiSiteListDialog.title"));

		setModal (true);
	}


}

