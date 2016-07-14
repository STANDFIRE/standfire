package fireparadox.gui.database;

import java.util.Vector;

import javax.swing.table.DefaultTableModel;

import jeeb.lib.util.Translator;
import fireparadox.model.database.FmDBMunicipality;

/**
 * FiMunicipalityTableModel : to display municipalities in a table
 *
 * @author Isabelle LECOMTE - february 2008
 */
public class FmMunicipalityTableModel extends DefaultTableModel {

	public String superColumnNames [] = {											//for super administrators only
		new String(Translator.swap ("FiMunicipalityTableModel.country")),
		new String(Translator.swap ("FiMunicipalityTableModel.municipality")),
		new String(Translator.swap ("FiMunicipalityTableModel.deleted"))
	};

	public String columnNames [] = {												//for others
		new String(Translator.swap ("FiMunicipalityTableModel.country")),
		new String(Translator.swap ("FiMunicipalityTableModel.municipality"))
	};

	protected Vector municipalityList;
	protected int rightLevel;

	public FmMunicipalityTableModel (int _rightLevel) {
		municipalityList = new Vector();
		rightLevel = _rightLevel;
		setDefaultData();
	}
	public void setDefaultData() {municipalityList.removeAllElements();}
	@Override
	public int getRowCount() {return municipalityList == null ? 0 : municipalityList.size();}
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
		FmDBMunicipality municipality = (FmDBMunicipality) municipalityList.elementAt(row);
		if (col == 0) {
				if (municipality.getCountry() != null)
					return municipality.getCountry().getCountryCode();
				else
					return "";
		}
		if (col == 1) return municipality.getMunicipalityName();
		if ((rightLevel ==9) && (col == 2)) return municipality.isDeleted();		//for super administrators only

		return "";
	}

	@Override
	public void setValueAt (Object object, int row, int col) {
		if (row < 0 || row > getRowCount()) return;
		FmDBMunicipality data = (FmDBMunicipality) municipalityList.elementAt(row);
		return;
	}
    public void setListMunicipality (Vector listMunicipality) {
		municipalityList = listMunicipality;
    }
    public void addMunicipality (FmDBMunicipality municipality) {
		municipalityList.add (municipality);
    }
    public void clear() {
		municipalityList.removeAllElements ();
	}
	public FmDBMunicipality getMunicipality (int index) {
		return (FmDBMunicipality)(municipalityList.elementAt (index));
	}

}


