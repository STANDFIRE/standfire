package capsis.lib.economics2.gui;

import javax.swing.table.AbstractTableModel;

public class EconomicOperationTable extends AbstractTableModel{
	
	private static final long serialVersionUID = 8083956857937064934L;
	 
	private String[] columnHeader = null;
	private Object[][] rows = new Object[0][0];
	 
	public Object getValueAt(int arg0, int arg1) {
	 
	return rows[arg0][arg1];
	}
	 
	public void setHeader(String[] newHeaders){
		columnHeader = newHeaders;
		super.fireTableStructureChanged();
	}
	 
	public int getColumnCount() {
		return this.columnHeader.length;
	}
	 
	public String getColumnName(int column) {
		return this.columnHeader[column];
	}
	 
	public int getRowCount() {
		return this.rows.length;
	}
	 
	public boolean isCellEditable(int row, int column) {
		// Aucune cellule éditable
		return false;
	}
	 
	//Fonction qui va s'occupper de remettre à jour tout mon tableau et qui va mettre
	//à jour aussi l'affichage
	public void setData(Object[][] newData){
		rows = newData;
		super.fireTableDataChanged();
	}

}
