package capsis.lib.economics2.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import jeeb.lib.util.Translator;
import capsis.lib.economics2.EconomicOperation;
import capsis.lib.economics2.EconomicPriceRecord;
import capsis.lib.economics2.EconomicScenario;
import capsis.lib.economics2.EconomicSettings;

public class EconomicPriceTableModel extends AbstractTableModel{

	static {
		Translator.addBundle ("capsis.lib.economics2.gui.EconomicTranslator");
	}

	private String[] columnHeader = {Translator.swap ("species") ,
			Translator.swap ("diameterClassHigh") ,
			Translator.swap ("price") ,
	};

	private List<EconomicPriceRecord> records = new ArrayList<EconomicPriceRecord>();

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		switch(columnIndex){
		case 0:
			return EconomicSettings.getSpeciesName(records.get(rowIndex).species);
		case 1:
			return records.get(rowIndex).dbh;
		case 2:
			return records.get(rowIndex).price;
		default :
			return null;
		}
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if(aValue != null){
			EconomicPriceRecord r = records.get(rowIndex);
			switch(columnIndex){
			case 0:
				r.species = EconomicSettings.getSpeciesCode((String) aValue);
				break;
			case 1:
				r.dbh = (double) aValue;
				break;
			case 2:
				r.price = (double) aValue;
				break;
			default :
			}
		}
	}
	
	@Override
	public Class getColumnClass(int columnIndex){
	    switch(columnIndex){
	        case 0:
	            return String[].class;
	        case 1:
	            return Double.class;
	        case 2 :
	            return Double.class;
	        default:
	            return Object.class;
	    }
	}

	public int getColumnCount() {
		return this.columnHeader.length;
	}

	public String getColumnName(int column) {
		return this.columnHeader[column];
	}

	public int getRowCount() {
		return this.records.size();
	}

	public boolean isCellEditable(int row, int column) {
		return true;// all cells can be edited 
	}


	public void addRecords(EconomicPriceRecord r){
		records.add(r);
		fireTableRowsInserted(records.size() -1, records.size() -1);
	}

	public void removeRecord(int rowIndex){
		records.remove(rowIndex);
		fireTableRowsDeleted(rowIndex, rowIndex);
	}

	/**
	 * add a list of economic operation
	 */
	public void setData(List<EconomicPriceRecord> records){
		this.records = records;
		super.fireTableDataChanged();
	}

	public List<EconomicPriceRecord> getPriceList(){
		return records;
	}

}
