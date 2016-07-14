package fireparadox.gui.database;

import java.util.Vector;

import javax.swing.table.DefaultTableModel;

import jeeb.lib.util.Translator;
import fireparadox.model.database.FmDBSite;

/**
 * FiSiteTableModel : to display sites in a table
 *
 * @author Isabelle LECOMTE - January 2008
 */

public class FmSiteTableModel extends DefaultTableModel
{
	public String superColumnNames [] = {
		new String(Translator.swap ("FiSiteTableModel.country")),				//for super administrators
		new String(Translator.swap ("FiSiteTableModel.municipality")),
		new String(Translator.swap ("FiSiteTableModel.siteCode")),
		new String(Translator.swap ("FiSiteTableModel.deleted"))
	};

	public String columnNames [] = {
		new String(Translator.swap ("FiSiteTableModel.country")),				//for others
		new String(Translator.swap ("FiSiteTableModel.municipality")),
		new String(Translator.swap ("FiSiteTableModel.siteCode"))
	};

	protected Vector siteList;
	protected int rightLevel;

	public FmSiteTableModel (int _rightLevel) {
		siteList = new Vector();
		rightLevel = _rightLevel;
		setDefaultData();
	}
	public void setDefaultData() {siteList.removeAllElements();}
	@Override
	public int getRowCount() {return siteList == null ? 0 : siteList.size();}
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
		FmDBSite site = (FmDBSite) siteList.elementAt(row);
		if (col == 0) {
			if (site.getMunicipality() != null)
				if (site.getMunicipality().getCountry() != null)
					return site.getMunicipality().getCountry().getCountryCode();
				else
					return "";
			else
				return "";
		}
		if (col == 1) {
			if (site.getMunicipality() != null)
				return site.getMunicipality().getMunicipalityName();
			else
				return "";
		}
		if (col == 2) return site.getSiteCode();
		if ((rightLevel ==9) && (col == 3)) return site.isDeleted();	//for super administrators only
		return "";
	}

	@Override
	public void setValueAt (Object object, int row, int col) {
		if (row < 0 || row > getRowCount()) return;
		FmDBSite data = (FmDBSite) siteList.elementAt(row);
		return;
	}
    public void setListSite (Vector listSite) {
		siteList = listSite;
    }
    public void addSite (FmDBSite site) {
		siteList.add (site);
    }
    public void clear() {
		siteList.removeAllElements ();
	}
	public FmDBSite getSite (int index) {
		return (FmDBSite)(siteList.elementAt (index));
	}
}


