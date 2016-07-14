/* 
 * The Standfire model.
 *
 * Copyright (C) 2013-2014: F. Pimont (INRA URFM).
 * 
 * This file is part of the Standfire model and is NOT free software.
 * It is the property of its authors and must not be copied without their 
 * permission. 
 * It can be shared by the modellers of the Capsis co-development community 
 * in agreement with the Capsis charter (http://capsis.cirad.fr/capsis/charter).
 * See the license.txt file in the Capsis installation directory 
 * for further information about licenses in Capsis.
 */
package standfire.myscripts;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;

import jeeb.lib.util.Alert;
import jeeb.lib.util.AmapDialog;
import jeeb.lib.util.IconLoader;
import jeeb.lib.util.Log;
import jeeb.lib.util.Settings;
import jeeb.lib.util.Translator;
import standfire.model.SFModel;
import standfire.model.SFScene;
import capsis.kernel.Project;
import capsis.kernel.Step;
import capsis.lib.fire.standviewer.FiStandViewer3D;

/**
 * An optional frame to be opened from a StandFireScript to show the scene in a
 * 3D viewer.
 * 
 * @author F. de Coligny - November 2014
 */
public class StandFireFrame extends AmapDialog implements ActionListener {

	static {
		Translator.addBundle("standfire.SFLabels");
		Translator.addBundle("capsis.Labels");
	}

	private Project project;
	private SFModel model;
	private Step step;
	private SFScene scene;

	private boolean wasValidated;
	private JButton close;

	/**
	 * Constructor.
	 */
	public StandFireFrame(Step step) {
		super(new JFrame());

		setTitle(Translator.swap("StandFireFrame"));

		activateLocationMemorization(this.getClass().getName());
		activateSizeMemorization(this.getClass().getName());

		this.project = step.getProject();
		this.model = (SFModel) project.getModel();
		this.step = step;
		this.scene = (SFScene) step.getScene();

		// Setup icon loader
		IconLoader.addPath("capsis/images");

		// AmapDialog.setParentFrame(this);

		createUI();

		// setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// setSize (new Dimension (800, 600));

		setMinimumSize(new Dimension(800, 600));

		pack();

		// setLocationRelativeTo(null);

		// // Center frame on screen
		// Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		// int w = 800;
		// int h = getHeight();
		// setLocation(screenSize.width / 2 - w / 2, screenSize.height / 2 - h /
		// 2);
		// setSize(w, h);

		setVisible(true);

	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		if (evt.getSource().equals(close)) {
			closeAction();
		}
	}

	@Override
	public void escapePressed() {
		closeAction();
	}

	/**
	 * Close button
	 */
	private void closeAction() {
		// Are you sure ?

		wasValidated = true;
		setVisible(false);
		AmapDialog.setParentFrame(null);
	}

	public boolean wasValidated() {
		return wasValidated;
	}

	private void createUI() {

		JPanel main = new JPanel(new BorderLayout());

		try {
			FiStandViewer3D viewer = new FiStandViewer3D();
			viewer.init(model, step, null); // StepButton is null here
			main.add(new JScrollPane(viewer), BorderLayout.CENTER);

			// SketchOV ov = new SketchOV();
			// ov.init(scene.getTrees());
			// main.add(new JScrollPane(ov), BorderLayout.CENTER);

		} catch (Exception e) {
			main.add(new JLabel(Translator.swap("Error while opening the viewer: " + e)), BorderLayout.NORTH);
			Writer result = new StringWriter();
			PrintWriter printWriter = new PrintWriter(result);
			e.printStackTrace(printWriter);

			main.add(new JScrollPane(new JTextArea(result.toString())), BorderLayout.CENTER);
		}

		// ControlPanel
		JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		close = new JButton(Translator.swap("Shared.close"));
		// help = new JButton (Translator.swap ("Shared.help"));
		controlPanel.add(close);
		// controlPanel.add (help);
		close.addActionListener(this);
		// help.addActionListener (this);

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(main, BorderLayout.CENTER);
		getContentPane().add(controlPanel, BorderLayout.SOUTH);

	}

	public static void showStandFireFrame(Step step) {

		// Alert messages will go to the terminal and will NOT open dialogs
		Alert.setInteractive(false);

		Log.println("StandFireFrame was launched...");

		// Install the system look and feel, always available
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		} catch (Exception e) { // ...should always be available
			System.out.println("Look and feel error: " + e);
		}

		// Initial user interaction
		StandFireFrame f = new StandFireFrame(step);

		// Wait until the frame is closed
		try {
			while (f.isVisible()) {
				Thread.currentThread().sleep(500);
			}
		} catch (Exception e) {
			System.out.println("StandFireFrame error: " + e);
			e.printStackTrace(System.out);
			System.exit(-3);
		}

		if (f.wasValidated()) {
			// do nothing, all the process is in SylvoDialog
		} else {
			Log.println("StandFireFrame was canceled by user");
		}
		f.dispose();

		// Save main properties file (see capsis.commongui.command.Quit)
		try {
			Settings.savePropertyFile();
		} catch (Exception e) {
			Log.println(Log.WARNING, "StandFireFrame.showStandFireFrame ()", "Unable to save capsis.options", e);
		}

	}

}
