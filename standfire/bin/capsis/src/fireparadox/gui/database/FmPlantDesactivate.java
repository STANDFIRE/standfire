package fireparadox.gui.database;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import jeeb.lib.util.AmapDialog;
import jeeb.lib.util.LinePanel;
import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import capsis.commongui.util.Helper;
import fireparadox.model.FmModel;
import fireparadox.model.database.FmDBPlant;
import fireparadox.model.database.FmDBUpdator;



/**	FiPlantDesactivate : Plant deasctivation/reactivation in database
*
*	@author I. Lecomte - January 2010
*/
public class FmPlantDesactivate extends AmapDialog implements ActionListener {

	private FmDBUpdator bdUpdator;						//to update database
	private FmModel model;
	private FmDBPlant plant;

	private JButton close;
	private JButton help;
	private JButton valid;


	/**	Constructor.
	*/
	public FmPlantDesactivate (FmModel _model, FmDBPlant _plant) {

		super ();
		model = _model;
		plant = _plant;

		createUI ();

		pack ();
		show ();
	}

	/**	Actions on the buttons
	*/
	public void actionPerformed (ActionEvent evt) {
		if (evt.getSource ().equals (close)) {
			closeAction ();
		} else if (evt.getSource ().equals (valid)) {
			validAction ();
		} else if (evt.getSource ().equals (help)) {
			Helper.helpFor (this);
		}
	}

	/**	Close was hit
	*/
	private void closeAction () {
		setVisible (false);
	}



	/**	Validation
	*/
	private void validAction () {

		bdUpdator = model.getBDUpdator ();

		if (!plant.isDeleted()) {	//DELETE
			try {

				if (plant.isValidated()) {
					bdUpdator.unValidatePlant (plant);
					plant.setValidated(false);
				}

				bdUpdator.desactivatePlant (plant);
				plant.setDeleted(true);

			} catch (Exception e) {
				Log.println (Log.ERROR, "FiPlantDesactivate.validateAction ()", "error while UPDATING PLANT data base", e);
			}
		}
		else {			//UNDELETE
			try {
				bdUpdator.reActivatePlant (plant);
				plant.setDeleted(false);

			} catch (Exception e) {
				Log.println (Log.ERROR, "FiPlantDesactivate.validateAction ()", "error while UPDATING PLANT data base", e);
			}
		}

		setValidDialog (true);
		setVisible (false);

	}


	/**	Initialize the GUI.
	*/
	private void createUI () {

		/*********** Fuel info  **************/
		JPanel infoPanel = new JPanel (new FlowLayout (FlowLayout.LEFT));

		LinePanel info = new LinePanel ();
		FmPlantInfoPanel plantInfoPanel = new FmPlantInfoPanel (plant);
		info.add (plantInfoPanel);

		infoPanel.add (info);

		JPanel messagePanel = new JPanel (new FlowLayout (FlowLayout.LEFT));
		if (!plant.isDeleted())
			messagePanel.add (new JLabel(Translator.swap ("FiPlantDesactivate.desactivationMessage")));
		else
			messagePanel.add (new JLabel(Translator.swap ("FiPlantDesactivate.reactivationMessage")));


		// Control panel
		JPanel controlPanel = new JPanel (new FlowLayout (FlowLayout.CENTER));
		close = new JButton (Translator.swap ("Shared.close"));
		help = new JButton (Translator.swap ("Shared.help"));
		valid = new JButton (Translator.swap ("FiPlantDesactivate.validation"));

		controlPanel.add (valid);
		controlPanel.add (close);
		controlPanel.add (help);
		close.addActionListener (this);
		help.addActionListener (this);
		valid.addActionListener (this);



		getContentPane ().setLayout (new BorderLayout ());
		getContentPane ().add (infoPanel, BorderLayout.NORTH);
		getContentPane ().add (messagePanel, BorderLayout.CENTER);
		getContentPane ().add (controlPanel, BorderLayout.SOUTH);

		if (!plant.isDeleted())
			setTitle (Translator.swap ("FiPlantDesactivate.desactivationTitle"));
		else
			setTitle (Translator.swap ("FiPlantDesactivate.reactivationTitle"));

		setModal (true);
	}


	public FmDBPlant getNewPlant () {
		return plant;
	}


}

