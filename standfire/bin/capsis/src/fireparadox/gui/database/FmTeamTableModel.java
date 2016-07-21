package fireparadox.gui.database;

import java.util.Vector;

import javax.swing.table.DefaultTableModel;

import jeeb.lib.util.Translator;
import fireparadox.model.database.FmDBTeam;

/**	FiTeamTableModel : to display teams in a table
 *
 * @author Isabelle LECOMTE - february 2008
 */

public class FmTeamTableModel extends DefaultTableModel
{
	public String superColumnNames [] = {								//for super administrators
		new String(Translator.swap ("FiTeamTableModel.team")),
		new String(Translator.swap ("FiTeamTableModel.deleted"))
	};

	public String columnNames [] = {									//for others
		new String(Translator.swap ("FiTeamTableModel.team"))
	};

	protected Vector teamList;
	protected int rightLevel;

	public FmTeamTableModel (int _rightLevel) {
		teamList = new Vector();
		rightLevel = _rightLevel;
		setDefaultData();
	}
	public void setDefaultData() {teamList.removeAllElements();}
	@Override
	public int getRowCount() {return teamList == null ? 0 : teamList.size();}
	@Override
	public int getColumnCount() {
		if (rightLevel ==9) return superColumnNames.length;
		return columnNames.length;
	}
	@Override
	public String getColumnName(int column) {
		if (rightLevel ==9) return superColumnNames[column];
		return columnNames[column];
	}
    @Override
	public boolean isCellEditable(int row, int col) {return false;}

	@Override
	public Object getValueAt(int row, int col) {
		if (row < 0 || row > getRowCount()) return "";
		FmDBTeam team = (FmDBTeam) teamList.elementAt(row);
		if (col == 0) return team.getTeamCode();
		if ((rightLevel ==9) && (col == 1)) return team.isDeleted();	//for super administrators only
		return "";
	}

	@Override
	public void setValueAt (Object object, int row, int col) {
		if (row < 0 || row > getRowCount()) return;
		FmDBTeam data = (FmDBTeam) teamList.elementAt(row);
		return;
	}
    public void setListTeam (Vector listTeam) {
		teamList = listTeam;
    }
    public void addTeam (FmDBTeam team) {
		teamList.add (team);
    }
    public void clear() {
		teamList.removeAllElements ();
	}
	public FmDBTeam getTeam (int index) {
		return (FmDBTeam)(teamList.elementAt (index));
	}



}


