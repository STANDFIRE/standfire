package fireparadox.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import jeeb.lib.util.AmapDialog;
import jeeb.lib.util.Check;
import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.MessageDialog;
import jeeb.lib.util.JWidthLabel;
import jeeb.lib.util.LinePanel;
import jeeb.lib.util.Log;
import jeeb.lib.util.Question;
import jeeb.lib.util.Settings;
import jeeb.lib.util.Translator;
import jeeb.lib.util.serial.SerializerFactory;
import jeeb.lib.util.serial.Writer;
import capsis.commongui.util.Helper;
import capsis.gui.MainFrame;
import capsis.util.JSmartFileChooser;
import fireparadox.model.FmSceneDatabaseSaver;
import fireparadox.model.FmStand;


/**	FiSaveForReeditionDialog: proposes to save the scene for later reedition.
*	Either in a file by serialization, or in the database with Boris's format.
*	Ok saves, cancel skips this feature.
*	@author F. de Coligny, B. Pezzatti - february 2010
*/
public class FmSaveForReeditionDialog extends AmapDialog implements ActionListener {

	private FmStand scene;

	private JRadioButton saveToFile;
	private JRadioButton saveToDatabase;
	private ButtonGroup group1;
	
	private JTextField databaseSceneName;
	private JTextField databaseTeamName;

	// Control panel at the bottom
	private JButton ok;
	private JButton cancel;
	private JButton help;


	/**	Constructor.
	*/
	public FmSaveForReeditionDialog (FmStand scene) {
		super ();
		this.scene = scene;
		
		createUI ();
		pack (); 
		setVisible (true);
	}



	// Ask a file name to the user
	// Reason is what we need a fileName for, e.g. "Save the scene for later reediton"
	// Returns a fileName or null if the user aborts
	private String askFileName (String reason) {
		String result = null;

		boolean trouble = false;
		JFileChooser chooser = null;
		int returnVal = 0;
		do {
			trouble = false;
			chooser = new JSmartFileChooser (
						reason,
						Translator.swap ("Shared.save"),
						Translator.swap ("Shared.save"),
						Settings.getProperty ("fireparadox.file.path", (String)null),
						false);	// DIRECTORIES_ONLY=false

			//~ chooser.addChoosableFileFilter (new ScenarioFileFilter());
			chooser.setAcceptAllFileFilterUsed (true);
			chooser.setDialogType (JFileChooser.SAVE_DIALOG);
			returnVal = chooser.showDialog (MainFrame.getInstance (), null);	// null : approveButton text was already set

			if (returnVal == JFileChooser.APPROVE_OPTION &&
					chooser.getSelectedFile ().exists ()) {
				if (!Question.ask (MainFrame.getInstance (),
						Translator.swap ("Shared.confirm"), ""
						+chooser.getSelectedFile ().getPath ()
						+"\n"
						+Translator.swap ("Shared.fileExistsPleaseConfirmOverwrite"))) {
					trouble = true;
				}
			}

		} while (trouble);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File fileName = chooser.getSelectedFile ();

			result =  fileName.getAbsolutePath ();
			Settings.setProperty ("fireparadox.file.path", fileName.getParent ());
		}
		return result;
	}

	
	/**	Ask a fileName then serializes the scene
	*	Returns true if success, false if user abort or error.
	*/
	private boolean saveToFile () {
		String sceneFileName = askFileName (Translator.swap ("FiSaveForReeditionDialog.sceneFileName"));
		if (sceneFileName == null) {
			// User aborted
			return false;
		} else {
			try {
				Writer writer = SerializerFactory.getWriter (sceneFileName);
				writer.write (sceneFileName, scene, "FireParadox Scene");
				return true;
				
			} catch (Exception e) {
				Log.println (Log.ERROR, "FiSaveForReeditionDialog.saveToFile ()",
						"Could not write scene into target file: "+sceneFileName, e);
				MessageDialog.print (this, Translator.swap ("FiSaveForReeditionDialog.couldNotSaveTheSceneSeeLog"));
				return false;
			}
		}
	}

	/**	Save scene to database
	*/
	private boolean saveToDatabase (String sceneName, String ownerTeam) {
		FmSceneDatabaseSaver saver = new FmSceneDatabaseSaver (scene, sceneName, ownerTeam);
		int result = saver.execute ();
		
		return result == 0;
	}


	/**	Ok was hit : controls and go out the dialog
	*/
	private void okAction () {

		if (saveToFile.isSelected ()) {
			boolean success = saveToFile ();
			if (!success) {return;}
		} else {
			
			// Check user entries
			if (Check.isEmpty (databaseSceneName.getText ().trim ())) {
				MessageDialog.print (this, Translator.swap ("FiSaveForReeditionDialog.wrongSceneName"));
				return;
			}
			if (Check.isEmpty (databaseTeamName.getText ().trim ())) {
				MessageDialog.print (this, Translator.swap ("FiSaveForReeditionDialog.wrongTeamName"));
				return;
			}
			
			boolean success = saveToDatabase (databaseSceneName.getText ().trim (), databaseTeamName.getText ().trim ());
			if (!success) {return;}
		}
		
		setValidDialog (true);
	}
	
	
	/**	To be called when ckicking on the radio buttons
	*/
	private void synchro () {
		databaseSceneName.setEnabled (saveToDatabase.isSelected ());
		databaseTeamName.setEnabled (saveToDatabase.isSelected ());
	}


	/**	Some button was hit...
	*/
	public void actionPerformed (ActionEvent evt) {
		if (evt.getSource () instanceof JRadioButton) {
			synchro ();
		} else if (evt.getSource ().equals (ok)) {
			okAction ();
		} else if (evt.getSource ().equals (cancel)) {
			setValidDialog (false);
		} else if (evt.getSource ().equals (help)) {
			Helper.helpFor (this);
		}
	}

	
	/**	Called on Escape.
	*/
	protected void escapePressed () {
		// User abort
		setValidDialog (false);
	}


	/**	Inits the GUI.
	*/
	private void createUI () {

		ColumnPanel main = new ColumnPanel ();

		LinePanel l0 = new LinePanel ();
		l0.add (new JLabel (Translator.swap ("FiSaveForReeditionDialog.saveTheSceneForLaterReedition")));
		l0.addGlue ();
		main.add (l0);

		LinePanel l1 = new LinePanel ();
		saveToFile = new JRadioButton (Translator.swap ("FiSaveForReeditionDialog.saveToFile"));
		saveToFile.addActionListener (this);
		l1.add (saveToFile);
		l1.addGlue ();
		main.add (l1);

		LinePanel l2 = new LinePanel ();
		saveToDatabase = new JRadioButton (Translator.swap ("FiSaveForReeditionDialog.saveToDatabase"));
		saveToDatabase.addActionListener (this);
		l2.add (saveToDatabase);
		l2.addGlue ();
		main.add (l2);

		group1 = new ButtonGroup ();
		group1.add (saveToFile);
		group1.add (saveToDatabase);
		saveToFile.setSelected (true);
		
		LinePanel l3 = new LinePanel ();
		l3.add (new JWidthLabel (Translator.swap ("FiSaveForReeditionDialog.databaseSceneName")+" : ", 150));
		databaseSceneName = new JTextField (5);
		l3.add (databaseSceneName);
		l3.addStrut0 ();
		main.add (l3);
		
		LinePanel l4 = new LinePanel ();
		l4.add (new JWidthLabel (Translator.swap ("FiSaveForReeditionDialog.databaseTeamName")+" : ", 150));
		databaseTeamName = new JTextField (5);
		l4.add (databaseTeamName);
		l4.addStrut0 ();
		main.add (l4);



		// Control panel
		LinePanel controlPanel = new LinePanel ();

		ok = new JButton (Translator.swap ("Shared.ok"));
		cancel = new JButton (Translator.swap ("FiSaveForReeditionDialog.ignore"));
		help = new JButton (Translator.swap ("Shared.help"));

		controlPanel.addGlue ();	// will justify the buttons to the right
		controlPanel.add (ok);
		controlPanel.add (cancel);
		controlPanel.add (help);
		controlPanel.addStrut0 ();

		ok.addActionListener (this);
		cancel.addActionListener (this);
		help.addActionListener (this);

		setLayout (new BorderLayout ());
		add (main, BorderLayout.NORTH);
		add (controlPanel, BorderLayout.SOUTH);

		synchro ();

		setTitle (Translator.swap ("FiSaveForReeditionDialog.savingForReedition"));
		setResizable (true);
		setModal (true);
	}


}



