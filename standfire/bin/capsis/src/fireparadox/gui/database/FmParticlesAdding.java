package fireparadox.gui.database;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import jeeb.lib.util.AmapDialog;
import jeeb.lib.util.JWidthLabel;
import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import capsis.commongui.util.Helper;
import fireparadox.model.FmModel;
import fireparadox.model.database.FmDBCommunicator;

/**
 * FiFuelParticlesAdding : Add a new particle for a FUEL attached to the plant or  voxel
 *
 * @author I. Lecomte - April 2008
 */
public class FmParticlesAdding extends AmapDialog implements ActionListener {

	private FmModel model;
	private FmDBCommunicator bdCommunicator;


	//JComboBox for choosing new particle
	public JComboBox particleComboBox;
	private Vector particleDBList;	//Available particle list in DB
	private Vector newParticleList;		//Particle list already defined for the fuel

	private JButton ok;
	private JButton cancel;
	private JButton help;

	/**	Constructor.
	 */
	public FmParticlesAdding (FmModel model, Vector existingParticleList) {
		super ();

		this.model = model;

		this.newParticleList = new Vector (existingParticleList);

		//Load particles parameters entire list from database
		try {
			bdCommunicator = model.getBDCommunicator ();
			particleDBList = bdCommunicator.getParticles();
			Collections.sort(particleDBList);

			if (newParticleList != null) {
				//remove already existing particles (to avoid double creation)
				for (Iterator i = newParticleList.iterator (); i.hasNext ();) {
					String particleName1 = (String) i.next ();
					for (Iterator j = particleDBList.iterator (); j.hasNext ();) {
						String particleName2 = (String) j.next ();
						if (particleName1.compareTo(particleName2)==0) {
							particleDBList.remove(particleName2);
							break;
						}
					}
				}
			}

		} catch (Exception e) {
			Log.println (Log.ERROR, "FiFuelParticlesAdding ()", "error while opening LIST PARTICLES data base", e);
		}

		createUI ();
		setSize (120, 50);
		pack ();
		show ();
	}

	/**	Actions on the buttons
	 */
	public void actionPerformed (ActionEvent evt) {
		if (evt.getSource ().equals (ok)) {
			validateAction ();
		} else if (evt.getSource ().equals (cancel)) {
			setValidDialog (false);
		} else if (evt.getSource ().equals (help)) {
			Helper.helpFor (this);
		}
	}

	/**	Validation = save in the database
	 */
	private void validateAction () {

		//retrieve particle name
		String newParticle = (String) particleComboBox.getSelectedItem();
		if (!newParticleList.contains(newParticle)) newParticleList.add (newParticle);

		setValidDialog (true);
	}

	/**	Initialize the GUI.
	 */
	private void createUI () {

		/*********** PARTICULE panel **************/
		JPanel particlePanel = new JPanel (new FlowLayout (FlowLayout.LEFT));

		//particle family choice
		JPanel p1 = new JPanel (new FlowLayout (FlowLayout.LEFT));
		particleComboBox = new JComboBox (particleDBList);
		p1.add (new JWidthLabel (Translator.swap ("FiParticlesAdding.particle")+" :",80));
		p1.add (particleComboBox);
		particlePanel.add (p1);

		/*********** CONTROL panel **************/
		JPanel controlPanel = new JPanel ();
		controlPanel.setLayout (new FlowLayout (FlowLayout.RIGHT));
		ok = new JButton (Translator.swap ("FiParticlesAdding.validate"));
		ok.addActionListener (this);
		controlPanel.add (ok);
		cancel = new JButton (Translator.swap ("Shared.cancel"));
		cancel.addActionListener (this);
		controlPanel.add (cancel);
		help = new JButton (Translator.swap ("Shared.help"));
		help.addActionListener (this);
		controlPanel.add (help);

		getContentPane ().setLayout (new BorderLayout ());
		getContentPane ().add (particlePanel, BorderLayout.CENTER);
		getContentPane ().add (controlPanel, BorderLayout.SOUTH);
		setTitle (Translator.swap ("FiParticlesAdding.title"));

		setModal (true);

	}

	//return new particle list
	public Vector getNewParticleList() {
		return newParticleList;
	}


}
