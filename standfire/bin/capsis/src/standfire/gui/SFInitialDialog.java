package standfire.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;

import jeeb.lib.util.Check;
import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.LinePanel;
import jeeb.lib.util.Log;
import jeeb.lib.util.MessageDialog;
import jeeb.lib.util.Settings;
import jeeb.lib.util.Translator;
import standfire.model.SFInitialParameters;
import standfire.model.SFModel;
import capsis.commongui.InitialDialog;
import capsis.commongui.util.Helper;
import capsis.kernel.GModel;
import capsis.kernel.PathManager;

/**
 * SFInitialDialog is the Standfire initial dialog.
 * 
 * @author F. de Coligny - January 2015
 */
public class SFInitialDialog extends InitialDialog implements ActionListener {

	private SFModel model;

	private JTextField scriptFileName;
	private JButton browse;
	
	private JButton ok;
	private JButton cancel;
	private JButton help;

	/**
	 * Constructor.
	 */
	public SFInitialDialog(GModel model) {
		super();

		this.model = (SFModel) model;

		createUI();
		
		pack ();
		setVisible(true);
	}

	/**
	 * Action on browse.
	 */
	private void browseAction() {
		String path = scriptFileName.getText();

		JFileChooser chooser = new JFileChooser(path);

		int returnVal = chooser.showOpenDialog(this);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			String f = chooser.getSelectedFile().toString();
			Settings.setProperty("SFInitialDialog.scriptFileName", f);
			scriptFileName.setText(f);
		}
	}

	/**
	 * Action on Ok.
	 */
	private void okAction() {

		if (!Check.isFile(scriptFileName.getText().trim())) {
			MessageDialog.print(this, Translator.swap("SFInitialDialog.wrongScriptFileName") + " : "
					+ scriptFileName.getText().trim());
			return;
		}
		String fileName = scriptFileName.getText().trim();		
		
		Settings.setProperty("SFInitialDialog.scriptFileName", fileName);

		try {
			SFInitialParameters i = model.getSettings();
			i.scriptFileName = fileName;
			
			i.buildInitScene(model);
			setInitialParameters(i);

		} catch (Exception e) {
			Log.println(Log.ERROR, "SFInitialDialog.okAction ()",
					"Error during standFire module initialisation (buildInitScene ())", e);
			MessageDialog.print(this, Translator.swap("SFInitialDialog.initialisationErrorSeeLog"), e);
			return;
		}

		setValidDialog(true);
	}

	/**
	 * Actions on buttons
	 */
	public void actionPerformed(ActionEvent evt) {
		if (evt.getSource().equals(browse)) {
			browseAction();
		} else if (evt.getSource().equals(ok)) {
			okAction();
		} else if (evt.getSource().equals(cancel)) {
			setValidDialog(false);
		} else if (evt.getSource().equals(help)) {
			Helper.helpFor(this);
		}
	}

	/**
	 * Creates the GUI.
	 */
	private void createUI() {

		ColumnPanel main = new ColumnPanel();

		LinePanel l1 = new LinePanel();
		l1.add(new JLabel(Translator.swap("SFInitialDialog.scriptFileName") + " : "));
		scriptFileName = new JTextField();
		scriptFileName.setText(Settings.getProperty("SFInitialDialog.scriptFileName", PathManager.getDir("data")));
		l1.add(scriptFileName);
		browse = new JButton(Translator.swap("Shared.browse"));
		browse.addActionListener(this);
		l1.add(browse);
		l1.addStrut0();
		main.add(l1);

		main.addGlue();

		// 2. Control panel (ok cancel help);
		LinePanel controlPanel = new LinePanel();
		ok = new JButton(Translator.swap("Shared.ok"));
		cancel = new JButton(Translator.swap("Shared.cancel"));
		help = new JButton(Translator.swap("Shared.help"));
		controlPanel.addGlue();
		controlPanel.add(ok);
		controlPanel.add(cancel);
		controlPanel.add(help);
		controlPanel.addStrut0();
		ok.addActionListener(this);
		cancel.addActionListener(this);
		help.addActionListener(this);

		// Sets ok as default
		setDefaultButton(ok);

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(main, BorderLayout.NORTH);
		getContentPane().add(controlPanel, BorderLayout.SOUTH);

		setTitle(Translator.swap("SFInitialDialog.title"));
		setModal(true);
	}

}
