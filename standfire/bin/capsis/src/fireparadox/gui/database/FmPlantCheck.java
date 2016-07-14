package fireparadox.gui.database;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import jeeb.lib.util.AmapDialog;
import jeeb.lib.util.LinePanel;
import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import capsis.commongui.util.Helper;
import fireparadox.model.FmModel;
import fireparadox.model.database.FmDBCommunicator;
import fireparadox.model.database.FmDBPlant;
import fireparadox.model.database.FmDBShape;
import fireparadox.model.database.FmDBUpdator;


/**	FiPlantCheck : Plant check in database
*
*	@author I. Lecomte - Janauary 2010
*/
public class FmPlantCheck extends AmapDialog implements ActionListener {

	private FmDBCommunicator bdCommunicator;			//to read database
	private FmDBUpdator bdUpdator;						//to update database

	private FmModel model;
	private FmDBPlant plant;
	private FmDBShape shape;
	private int levelError;
	private LinkedHashMap  shapeMap;

	private TextArea textRapport;
	private JButton close;
	private JButton help;
	private JButton valid;


	/**	Constructor.
	*/
	public FmPlantCheck (FmModel _model, FmDBPlant _plant) {

		super ();
		model = _model;
		plant = _plant;
		textRapport = new TextArea("");

		loadData ();

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

		if (plant.isValidated()) {
			bdUpdator = model.getBDUpdator ();

			//unvalidated plant and all shapes
			try {
				bdUpdator.unValidatePlant (plant);
				plant.setValidated(false);
				for (Iterator i = shapeMap.keySet().iterator(); i.hasNext ();) {
					Long cle = (Long) i.next();
					FmDBShape shape = (FmDBShape) shapeMap.get(cle);
					bdUpdator.unValidateShape (shape);
				}

			} catch (Exception e) {
				Log.println (Log.ERROR, "FiPlantCheck", "error while VALIDATE SHAPE data base", e);
			}

			setValidDialog (true);
			setVisible (false);

		}
		//validate plant and mean shape
		else if  (shape != null) {
			bdUpdator = model.getBDUpdator ();
			try {
				bdUpdator.validateShape (shape);
				bdUpdator.validatePlant (plant);
				plant.setValidated (true);

			} catch (Exception e) {
				Log.println (Log.ERROR, "FiPlantCheck", "error while VALIDATE SHAPE data base", e);
			}

			setValidDialog (true);
			setVisible (false);
		}
	}


	/**	Initialize the GUI.
	*/
	private void createUI () {

		/*********** Fuel info  **************/
		JPanel infoPanel = new JPanel (new FlowLayout (FlowLayout.LEFT));

		LinePanel info = new LinePanel ();
		FmPlantInfoPanel plantInfoPanel = new FmPlantInfoPanel (plant);
		info.add (plantInfoPanel);

		if ((!plant.isValidated()) && (shape != null)) {
			FmShapeInfoPanel shapeInfoPanel = new FmShapeInfoPanel (shape);
			info.add (shapeInfoPanel);
		}

		infoPanel.add (info);

		/*********** check results **************/
		JPanel messagePanel = new JPanel (new FlowLayout (FlowLayout.LEFT));
		Box box = Box.createVerticalBox ();

		if (!plant.isValidated())
			messagePanel.add (textRapport);
		else
			messagePanel.add (new JLabel(Translator.swap ("FiPlantCheck.unValidationMessage")));


		// Control panel
		JPanel controlPanel = new JPanel (new FlowLayout (FlowLayout.CENTER));
		close = new JButton (Translator.swap ("Shared.close"));
		help = new JButton (Translator.swap ("Shared.help"));
		valid = new JButton (Translator.swap ("FiPlantCheck.validation"));

		controlPanel.add (valid);
		controlPanel.add (close);
		controlPanel.add (help);
		close.addActionListener (this);
		help.addActionListener (this);
		valid.addActionListener (this);

		if (!plant.isValidated()) {
			if ((levelError < 0) || (levelError == 2)) valid.setEnabled(false);
		}



		getContentPane ().setLayout (new BorderLayout ());
		getContentPane ().add (infoPanel, BorderLayout.NORTH);
		getContentPane ().add (messagePanel, BorderLayout.CENTER);
		getContentPane ().add (controlPanel, BorderLayout.SOUTH);

		if (plant.isValidated())
			setTitle (Translator.swap ("FiPlantCheck.unvalidationTitle"));
		else
			setTitle (Translator.swap ("FiPlantCheck.validationTitle"));

		setModal (true);
	}

	private void loadData () {

		levelError = 0;

		try {
			bdCommunicator = model.getBDCommunicator ();

			//get the shapes
			shapeMap = new LinkedHashMap<Long, FmDBShape> ();
			shapeMap = bdCommunicator.getPlantShapes (plant, 1);
			for (Iterator i = shapeMap.keySet().iterator(); i.hasNext ();) {
				Long cle = (Long) i.next();
				FmDBShape f = (FmDBShape) shapeMap.get(cle);

				//plant or layer not DELETED
				if ((f.getFuelType() == 1) && (!f.isDeleted())) {
					if (f.getShapeKind().equals("XYZ")) {
						shape = f;
					}
				}

				//plant or layer not DELETED
				if ((f.getFuelType() == 2) && (!f.isDeleted())) {
					if (f.getShapeKind().equals("XZ")) {
						shape = f;
					}
				}
			}

			if ((!plant.isValidated()) && (shape != null)) {

				levelError = -1;

				//rapport for the main shape
				Vector<String> rapport = bdCommunicator.rapportShape (shape);

				for(String message : rapport) {
					textRapport.append(message);
					textRapport.append("\n");

					int index = message.indexOf (":");
					if (index > 0) {
						String attribute = message.substring (0, index);


						if (attribute.compareTo("RESULT") == 0)  {
							String error = message.substring (index+1);

							if (error.compareTo("error") == 0)  levelError=2;
							if (error.compareTo("warning") == 0)  levelError=1;
							if (error.compareTo("ok") == 0)  levelError=0;
						}
					}
				}
			}
			else {
				levelError = -1;
				textRapport.append(Translator.swap ("FiPlantCheck.message"));
				textRapport.append("\n");
			}




		} catch (Exception e) {
			Log.println (Log.ERROR, "FiPlantCheck.checkPlant() ", "error while opening FUEL data base", e);
		}

	}


	public FmDBPlant getNewPlant () {
		return plant;
	}

}

