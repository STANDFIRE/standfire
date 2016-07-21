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
import fireparadox.model.database.FmDBPlant;
import fireparadox.model.database.FmDBTeam;


/**	FiPlantListDialog : Plant list from database
*
*	@author I. Lecomte - march 2008
*/
public class FmPlantListDialog extends AmapDialog implements ActionListener {

	private FmModel model;
	private FmPlantResearchForm plantList;			//research form
	private int rightLevel;							//user right management
	private FmDBTeam teamLogged;					//logged team
	private int fuelType; 							//1=plant 2=layer 3=sample

	private JButton close;
	private JButton help;
	private JButton add;
	private JButton supress;
	private JButton copy;
	private JButton modify;
	private JButton particles;
	private JButton shapes;
	private JButton validate;


	/**	Constructor.
	*/
	public FmPlantListDialog (FmModel _model, int _fuelType) {

		super ();
		model = _model;
		rightLevel = _model.getRightLevel();
		teamLogged = _model.getTeamLogged();
		fuelType = _fuelType;		//1=plant 2=layer

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
			addPlantEntry ();
		} else if (evt.getSource ().equals (modify)) {
			modifyPlantEntry ();
		} else if (evt.getSource ().equals (copy)) {
			copyPlantEntry ();
		} else if (evt.getSource ().equals (particles)) {
			modifyParticles();
		} else if (evt.getSource ().equals (validate)) {
			validatePlant();
		} else if (evt.getSource ().equals (shapes)) {
			shapesList();
		} else if (evt.getSource ().equals (supress)) {
			supressPlantEntry ();
		} else if (evt.getSource ().equals (help)) {
			Helper.helpFor (this);
		}
	}

	/**	Close was hit
	*/
	private void closeAction () {
		setVisible (false);
	}

	/**	Plant adding
	*/
	private void addPlantEntry () {

		FmPlantEditor addEntry = new FmPlantEditor (model,  -1, -1, fuelType);

		//refreshing plant list
		FmDBPlant newPlant = addEntry.getNewPlant ();
		if ((newPlant != null) && (newPlant.getPlantId() > 0)) {
			plantList.addPlantEntry (newPlant);
			repaint();
		}
	}

	/** Plant copy
	*/
	private void copyPlantEntry () {
		int [] selRow  = plantList.getSelectedRows ();
		if (selRow.length > 0) {
			int selectedRow = selRow[0];
			long plantSelectedId = plantList.getPlantId (selectedRow);
			FmDBTeam teamSelected = plantList.getTeam (selectedRow);
			//team restinction
			if ( (rightLevel < 9) && (teamSelected != null) && (!teamSelected.getTeamCode().equals(teamLogged.getTeamCode()))) {

				JOptionPane.showMessageDialog (this, Translator.swap ("FiPlantListDialog.noRightForThisAction"),
				Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
			}
			//if super adm, no restriction
			else  {
				FmPlantEditor addEntry = new FmPlantEditor (model,   -1,  plantSelectedId, fuelType);
				FmDBPlant newPlant = addEntry.getNewPlant();
				if ((newPlant != null) && (newPlant.getPlantId() > 0)) {
					plantList.addPlantEntry (newPlant);
					repaint();
				}
			}
		}
	}

	/**	Plant delete
	*/
	private void supressPlantEntry () {
		int [] selRow  = plantList.getSelectedRows ();
		if (selRow.length > 0) {

			int selectedRow = selRow[0];
			FmDBPlant plantSelected = plantList.getPlant(selectedRow);
			FmDBTeam teamSelected = plantList.getTeam (selectedRow);

			//team restinction
			if ((rightLevel < 9) && (teamSelected != null) && (!teamSelected.getTeamCode().equals(teamLogged.getTeamCode()))) {
				JOptionPane.showMessageDialog (this, Translator.swap ("FiPlantListDialog.noRightForThisAction"),
				Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
			}
			else {
				FmPlantDesactivate dialog = new FmPlantDesactivate (model,   plantSelected);

				//refreshing fuel list
				FmDBPlant newPlant = dialog.getNewPlant ();
				if (newPlant != null) {
					plantList.removePlantEntry (plantSelected);
					plantList.addPlantEntry (newPlant);
					repaint();
				}

			}
		}
	}

	/**	Plant update
	*/
	private void modifyPlantEntry () {
		int [] selRow  = plantList.getSelectedRows ();
		if (selRow.length > 0) {

			int selectedRow = selRow[0];
			FmDBPlant plantSelected = plantList.getPlant(selectedRow);
			long plantSelectedId = plantSelected.getPlantId ();
			boolean deleted = plantList.getDeleted (selectedRow);
			boolean validated = plantList.getValidated (selectedRow);
			FmDBTeam teamSelected = plantList.getTeam (selectedRow);


			//team restinction
			//if ( (rightLevel < 9) && (teamSelected != null) && (!teamSelected.getTeamCode().equals(teamLogged.getTeamCode()))) {
			//	JOptionPane.showMessageDialog (this, Translator.swap ("FiPlantListDialog.noRightForThisAction"),
			//	Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
			//}
			//else {
				FmPlantEditor addEntry = new FmPlantEditor (model, plantSelectedId,  -1, fuelType);

				//refreshing fuel list
				FmDBPlant newPlant = addEntry.getNewPlant ();
				if (newPlant != null) {
					plantList.removePlantEntry (plantSelected);
					plantList.addPlantEntry (newPlant);
					repaint();
				}
			//}



		}
	}

	/** PLANT validation
	*/
	private void validatePlant() {
		int [] selRow  = plantList.getSelectedRows ();
		if (selRow.length > 0) {

			int selectedRow = selRow[0];
			FmDBPlant plantSelected = plantList.getPlant(selectedRow);
			boolean deleted = plantList.getDeleted (selectedRow);
			boolean validated = plantList.getValidated (selectedRow);
			FmDBTeam teamSelected = plantList.getTeam (selectedRow);

			if (!deleted) {
				//team restinction
				if ( (rightLevel < 9) && (teamSelected != null) && (!teamSelected.getTeamCode().equals(teamLogged.getTeamCode()))) {
					JOptionPane.showMessageDialog (this, Translator.swap ("FiPlantListDialog.noRightForThisAction"),
					Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
				}
				else {
					FmPlantCheck dialog = new FmPlantCheck (model, plantSelected);
					//refreshing fuel list
					FmDBPlant newPlant = dialog.getNewPlant ();
					if (newPlant != null) {
						plantList.removePlantEntry (plantSelected);
						plantList.addPlantEntry (newPlant);
						repaint();
					}

				}
			}
			//deleted fuel
			else {
				JOptionPane.showMessageDialog (this, Translator.swap ("FiPlantListDialog.dataDeleted"),
				Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
			}

		}
	}

	/**	Plant particles edition
	*/
	private void modifyParticles () {

		//this option is only for plants or layers
		if (fuelType < 3) {
			int [] selRow  = plantList.getSelectedRows ();
			if (selRow.length > 0) {
				int selectedRow = selRow[0];
				FmDBPlant plantSelected = plantList.getPlant(selectedRow);
				FmDBTeam teamSelected = plantList.getTeam (selectedRow);
				boolean deleted = plantList.getDeleted (selectedRow);
				boolean validated = plantList.getValidated (selectedRow);

				if (!deleted) {
					if (!validated) {

						//team restinction
						if ( (rightLevel < 9) && (teamSelected != null) && (!teamSelected.getTeamCode().equals(teamLogged.getTeamCode()))) {
							JOptionPane.showMessageDialog (this, Translator.swap ("FiPlantListDialog.noRightForThisAction"),
							Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
						}
						else  {
							LinkedHashMap<Long, FmDBPlant> plantMap = plantList.getPlantMap();


							FmPlantParticleEditor particleEntry = new FmPlantParticleEditor (model, plantSelected, plantMap);
						}
					}
					else {
						JOptionPane.showMessageDialog (this, Translator.swap ("FiPlantListDialog.dataValidated"),
						Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );

					}
				}
				else {
					JOptionPane.showMessageDialog (this, Translator.swap ("FiPlantListDialog.dataDeleted"),
					Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );

				}
			}
		}
	}


	/**	Shapes list
	*/
	private void shapesList () {
		int [] selRow  = plantList.getSelectedRows ();
		if (selRow.length > 0) {
			int selectedRow = selRow[0];
			FmDBPlant plantSelected = plantList.getPlant(selectedRow);
			FmDBTeam teamSelected = plantList.getTeam (selectedRow);
			FmPlantShapeListDialog dialog = new FmPlantShapeListDialog (model, plantSelected, fuelType);
		}
	}



	/**	Initialize the GUI.
	*/
	private void createUI () {

		JPanel fuelPanel = new JPanel (new BorderLayout ());

		//Research Form
		plantList = new FmPlantResearchForm (model, teamLogged, rightLevel, fuelType);
		plantList.setPreferredSize (new Dimension (600,350));
		fuelPanel.add (plantList);

		// Control panel
		JPanel controlPanel = new JPanel (new FlowLayout (FlowLayout.CENTER));

		//EXCEPT VISITORS
		if (rightLevel > 1) {
			add = new JButton (Translator.swap ("FiPlantListDialog.add"));

			shapes = new JButton (Translator.swap ("FiPlantListDialog.shapes"));
			particles = new JButton (Translator.swap ("FiPlantListDialog.particles"));
			modify = new JButton (Translator.swap ("FiPlantListDialog.modify"));
			copy = new JButton (Translator.swap ("FiPlantListDialog.copy"));
			validate = new JButton (Translator.swap ("FiPlantListDialog.validate"));
			supress = new JButton (Translator.swap ("FiPlantListDialog.supress"));

			controlPanel.add (add);
			controlPanel.add (shapes);
			controlPanel.add (particles);
			controlPanel.add (validate);
			controlPanel.add (modify);
			controlPanel.add (copy);
			controlPanel.add (supress);

			add.addActionListener (this);
			copy.addActionListener (this);
			modify.addActionListener (this);
			supress.addActionListener (this);
			validate.addActionListener (this);
			particles.addActionListener (this);
			shapes.addActionListener (this);

		}

		close = new JButton (Translator.swap ("Shared.close"));
		help = new JButton (Translator.swap ("Shared.help"));
		controlPanel.add (close);
		controlPanel.add (help);
		close.addActionListener (this);
		help.addActionListener (this);

		getContentPane ().setLayout (new BorderLayout ());
		getContentPane ().add (fuelPanel, BorderLayout.CENTER);
		getContentPane ().add (controlPanel, BorderLayout.SOUTH);

		if (fuelType == 1) {
			setTitle (Translator.swap ("FiPlantListDialog.plant"));
		}
		else if (fuelType == 2) {
			setTitle (Translator.swap ("FiPlantListDialog.layer"));
		}
		else {
			setTitle (Translator.swap ("FiPlantListDialog.sample"));
		}

		setModal (true);
	}
}

