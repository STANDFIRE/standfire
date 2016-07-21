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
import fireparadox.model.database.FmDBShape;
import fireparadox.model.database.FmDBTeam;


/**	FiSampleListDialog : sample list from database
*
*	@author I. Lecomte - march 2008
*/
public class FmSampleListDialog extends AmapDialog implements ActionListener {

	private FmModel model;
	private FmSampleResearchForm shapeList;			//research form
	private int rightLevel;							//user right management
	private FmDBTeam teamLogged;					//logged team
	private int fuelType; 							//1=plant 2=layer 3=sample

	private JButton close;
	private JButton help;
	private JButton supress;
	private JButton modify;


	/**	Constructor.
	*/
	public FmSampleListDialog (FmModel _model) {

		super ();
		model = _model;
		rightLevel = _model.getRightLevel();
		teamLogged = _model.getTeamLogged();

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
		} else if (evt.getSource ().equals (modify)) {
			modifyShape ();
		} else if (evt.getSource ().equals (supress)) {
			desactivateShape ();
		} else if (evt.getSource ().equals (help)) {
			Helper.helpFor (this);
		}
	}

	/**	Close was hit
	*/
	private void closeAction () {
		setVisible (false);
	}




	/**	SHAPE desactivation
	*/
	private void desactivateShape() {
		int [] selRow  = shapeList.getSelectedRows ();
		if (selRow.length > 0) {
			int selectedRow = selRow[0];
			FmDBShape fuelSelected = shapeList.getShape (selectedRow);
			FmDBTeam teamSelected = fuelSelected.getPlant().getTeam ();

			//team restinction
			if ( (rightLevel < 9) && ( !teamSelected.equals(teamLogged.getTeamCode()))) {
				JOptionPane.showMessageDialog (this, Translator.swap ("FiSampleListDialog.noRightForThisAction"),
				Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
			}

			else  {

				FmShapeDesactivateDialog dialog = new FmShapeDesactivateDialog (model, fuelSelected);

			}
		}
	}

	/**	SHAPE modification
	*/
	private void modifyShape () {
		int [] selRow  = shapeList.getSelectedRows ();
		if (selRow.length > 0) {
			int selectedRow = selRow[0];
			FmDBShape fuelSelected = shapeList.getShape (selectedRow);
			FmDBTeam teamSelected = fuelSelected.getPlant().getTeam ();
			boolean isCubeMethod = fuelSelected.isCubeMethod();
			boolean deleted = shapeList.getDeleted (selectedRow);
			if (!deleted) {
				//team restinction
				if ( (rightLevel < 9) && ( !teamSelected.equals(teamLogged.getTeamCode()))) {
					JOptionPane.showMessageDialog (this, Translator.swap ("FiSampleListDialog.noRightForThisAction"),
					Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
				}
				else  {
					if (isCubeMethod) {
						FmSample2DEditor dialog = new FmSample2DEditor (model, fuelSelected, -1, -1, -1);
					}
					else {
						FmSample3DCageEditor dialog = new FmSample3DCageEditor (model,  fuelSelected);
					}
				}

			}
			//deleted fuel
			else {
				JOptionPane.showMessageDialog (this, Translator.swap ("FiSampleListDialog.dataDeleted"),
				Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
			}
		}
	}





	/**	Initialize the GUI.
	*/
	private void createUI () {

		JPanel fuelPanel = new JPanel (new BorderLayout ());

		//Research Form
		shapeList = new FmSampleResearchForm (model, teamLogged, rightLevel);
		shapeList.setPreferredSize (new Dimension (600,350));
		fuelPanel.add (shapeList);

		// Control panel
		JPanel controlPanel = new JPanel (new FlowLayout (FlowLayout.CENTER));

		//EXCEPT VISITORS
		if (rightLevel > 1) {

			modify = new JButton (Translator.swap ("FiSampleListDialog.modify"));
			supress = new JButton (Translator.swap ("FiSampleListDialog.supress"));
			controlPanel.add (modify);
			controlPanel.add (supress);
			modify.addActionListener (this);
			supress.addActionListener (this);
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

		setTitle (Translator.swap ("FiSampleListDialog.title"));


		setModal (true);
	}

}

