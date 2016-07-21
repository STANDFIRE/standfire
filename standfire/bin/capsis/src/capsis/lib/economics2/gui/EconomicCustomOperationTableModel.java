package capsis.lib.economics2.gui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import jeeb.lib.util.Translator;
import capsis.lib.economics2.EconomicCustomOperation;
import capsis.lib.economics2.EconomicOperation;
import capsis.lib.economics2.EconomicScene;

public class EconomicCustomOperationTableModel extends AbstractTableModel{

	static {
		Translator.addBundle ("capsis.lib.economics2.gui.EconomicTranslator");
	}

	private String[] columnHeader = {Translator.swap ("opDate") ,
			Translator.swap ("label"),
			Translator.swap ("type"),
			Translator.swap ("trigger"),
			Translator.swap ("expanse"),
			Translator.swap ("income")
	};

	private List<EconomicOperation> ops = new ArrayList<EconomicOperation>();

	public Object getValueAt(int rowIndex, int columnIndex) {
		
		EconomicCustomOperation co = (EconomicCustomOperation) ops.get(rowIndex);
		
		switch(columnIndex){
		case 0:
			return co.getScene().getDate();
		case 1:
			return ops.get(rowIndex).getLabel();
		case 2:
			return ops.get(rowIndex).getType();
		case 3:
			return ops.get(rowIndex).getTrigger();
		case 4:
			//expanse
			if(! ops.get(rowIndex).isIncome())
				return ops.get(rowIndex).getPrice();
			else
				return 0d;
		case 5:
			//income
			if(ops.get(rowIndex).isIncome())
				return ops.get(rowIndex).getPrice();
			else
				return 0d;
		default :
			return null;
		}
	}

	/**
	 * @TODO : some additional checks would be required
	 */
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if(aValue != null){
			EconomicOperation op = ops.get(rowIndex);
			
			switch(columnIndex){
			case 1:
				op.setLabel((String) aValue);
				break;
			case 2:
				op.setType((EconomicOperation.Type) aValue);
				break;
			case 4:
				//expanse
				if(aValue != null & (double) aValue > 0){
					op.setPrice((double) aValue);
					op.setIncome(false);
				}
				break;
			case 5:
				//income
				if(aValue != null & (double) aValue > 0){
					op.setPrice((double) aValue);
					op.setIncome(true);
				}
				break;
			default :
			}
		}
	}
	
	@Override
	public Class getColumnClass(int columnIndex){
	    switch(columnIndex){
	        case 0:
	            return Integer.class;
	        case 1:
	            return String.class;
	        case 2 :
	        	return EconomicOperation.Type.class;
	        case 3 :
	        	return EconomicOperation.Trigger.class;
	        case 4 :
	        	return Double.class;
	        case 5 :
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
		return this.ops.size();
	}

	public boolean isCellEditable(int row, int column) {
		if(column == 0) return false; //the date cannot be modified - the custom operation is attached to a scene!
		if(column == 3) return false; //the trigger cannot be modified

		return true;// all cells can be edited 
	}


	public void addOperation(EconomicOperation op){
		ops.add(op);
		fireTableRowsInserted(ops.size() -1, ops.size() -1);
	}

	public void removeOperation(int rowIndex){
		ops.remove(rowIndex);
		fireTableRowsDeleted(rowIndex, rowIndex);
	}

	/**
	 * add a list of economic operation
	 */
	public void setData(List<EconomicOperation> ops){
		this.ops = ops;
		super.fireTableDataChanged();
	}

	public List<EconomicOperation> getOperations(){
		return ops;
	}
	

	


}
