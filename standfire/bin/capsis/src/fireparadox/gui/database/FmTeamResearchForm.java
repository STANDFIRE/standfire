package fireparadox.gui.database;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.LinkedHashMap;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.LinePanel;
import jeeb.lib.util.Log;
import fireparadox.model.FmModel;
import fireparadox.model.FmInitialParameters;
import fireparadox.model.database.FmDBCommunicator;
import fireparadox.model.database.FmDBTeam;

/**	FiTeamResearchForm : research criterias for FiDBTeam objects
*
*	@author I. Lecomte - february 2008
*/
public class FmTeamResearchForm extends JPanel implements ActionListener {

	private FmModel model;
	private FmInitialParameters settings;

	private FmDBCommunicator bdCommunicator;		//to read database
    private LinkedHashMap  teamList;				//team list extract from database
    private int rightLevel;							//to manage user rights

	//to store and display results
    private JTable resultTable;
    private JScrollPane listSP;
    private FmTeamTableModel tableModel;


	/**	Constructor.
	*/
	public FmTeamResearchForm (FmModel _model,  int _rightLevel) {

		model = _model;
		settings = model.getSettings ();
		bdCommunicator = model.getBDCommunicator ();
		rightLevel = _rightLevel;

		//load entire team list
		tableModel = new  FmTeamTableModel (rightLevel);
		loadData();

		createUI();
    }

	/**	Load team list from database
	*/
	public void loadData () {

		try {
			tableModel. clear();
			teamList = new LinkedHashMap ();
			teamList = bdCommunicator.getTeams ();

			for (Iterator i = teamList.keySet().iterator(); i.hasNext ();) {
				Object cle = i.next();
				FmDBTeam t = (FmDBTeam) teamList.get(cle);


				//only super admin can see deleted objects
				if ((rightLevel < 9) && t.isDeleted()) {}
				else tableModel.addTeam  (t);
			}

		} catch (Exception e) {
			Log.println (Log.ERROR, "FiTeamResearchForm.loadData ()", "error while opening TEAM data base", e);
		}

	}

	/**	Changing criteria : : at the moment none !!
	 */
    public void actionPerformed(ActionEvent e) {
	}

	/**	Initialize the GUI.
	 */
    private void createUI() {

		ColumnPanel teamDBPanel = new ColumnPanel ();

		//1 Selection : at the moment none !!
		// if you need criterias, add them HERE

		// 2. Result Table
		LinePanel ligResult = new LinePanel ();
		ColumnPanel colTable = new ColumnPanel ();
		resultTable = new JTable(tableModel);
		listSP = new JScrollPane(resultTable);
		colTable.add (listSP);
		ligResult.add (colTable);

		teamDBPanel.add (ligResult);

		setLayout (new BorderLayout ());
		add (teamDBPanel, BorderLayout.NORTH);

    }

	//return the selected rows in the displayed table
	public int[] getSelectedRows () {
		return resultTable.getSelectedRows();
	}

	//return the team store at this index in the displayed table
	public FmDBTeam getTeam (int index) {
		return tableModel. getTeam (index);
	}

	//return the team id  store at this index in the displayed table
	public long getTeamId (int index) {
		return tableModel.getTeam (index).getTeamId();
	}

	 /** return if the team is deleted or not
	  */
	public boolean getDeleted (int index) {
		FmDBTeam team =  tableModel.getTeam(index);
		return team.isDeleted();

	}
}




