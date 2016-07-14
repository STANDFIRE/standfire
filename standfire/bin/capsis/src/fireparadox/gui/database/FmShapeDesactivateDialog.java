package fireparadox.gui.database;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
import fireparadox.model.database.FmDBPlant;
import fireparadox.model.database.FmDBShape;
import fireparadox.model.database.FmDBUpdator;


/**	FiShapeDesactivateDialog : shape d�sactivation / reactivation
*
*	@author I. Lecomte - march 2008
*/
public class FmShapeDesactivateDialog extends AmapDialog implements ActionListener {

	private FmModel model;
	private FmDBUpdator bdUpdator;				//to update database

	private FmDBShape shape;
	private FmDBShape newShape;
	private FmDBPlant plant;

	private JButton close;
	private JButton help;
	private JButton valid;



	/**	Constructor.
	*/
	public FmShapeDesactivateDialog (FmModel _model, FmDBShape _shape)  {

		super ();
		model = _model;
		shape = _shape;
		plant = shape.getPlant();

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

		newShape = new FmDBShape (shape);
		newShape.setDeleted (!shape.isDeleted());
		newShape.setShapeId (shape.getShapeId());


		try {
			bdUpdator.deleteShape (shape, newShape);		// update in the database

		} catch (Exception e) {
			Log.println (Log.ERROR, "FiPlantEditor.validateAction ()", "error while UPDATING SHAPE data base", e);
		}
		setValidDialog (true);

	}


	/**	Initialize the GUI.
	*/
	private void createUI () {

		// Choice panel
		JPanel choicePanel = new JPanel (new FlowLayout (FlowLayout.CENTER));
		Box box = Box.createVerticalBox ();


		/*********** Fuel info **************/

		LinePanel info = new LinePanel ();

		FmPlantInfoPanel plantInfoPanel = new FmPlantInfoPanel (plant);
		info.add (plantInfoPanel);

		FmShapeInfoPanel shapeInfoPanel = new FmShapeInfoPanel (shape);
		info.add (shapeInfoPanel);

		choicePanel.add (info);




		JLabel texte;
		if (shape.isDeleted()) {
			texte = new JLabel(Translator.swap ("FiShapeDesactivateDialog.activate"));
		}
		else {
			texte = new JLabel(Translator.swap ("FiShapeDesactivateDialog.desactivate"));
		}
		JLabel texte2 = new JLabel(Translator.swap ("FiShapeDesactivateDialog.label"));

		box.add (texte);
		box.add (texte2);
		choicePanel.add (box);

		// Control panel
		JPanel controlPanel = new JPanel (new FlowLayout (FlowLayout.CENTER));


		if (shape != null){
			if (shape.isDeleted())
				valid = new JButton (Translator.swap ("FiPlantEditor.unsupress"));
			else
				valid = new JButton (Translator.swap ("FiPlantEditor.supress"));
		}


		close = new JButton (Translator.swap ("Shared.close"));
		help = new JButton (Translator.swap ("Shared.help"));
		controlPanel.add (valid);
		controlPanel.add (close);
		controlPanel.add (help);


		valid.addActionListener (this);
		close.addActionListener (this);
		help.addActionListener (this);

		getContentPane ().setLayout (new BorderLayout ());
		getContentPane ().add (choicePanel, BorderLayout.CENTER);
		getContentPane ().add (controlPanel, BorderLayout.SOUTH);

		setTitle (Translator.swap ("FiShapeDesactivateDialog.title"));
		setPreferredSize (new Dimension(450,200));

		setModal (true);
	}

	/**	To get update from outside
	 */
	public FmDBShape getNewShape() {
			return newShape;
	}

}

