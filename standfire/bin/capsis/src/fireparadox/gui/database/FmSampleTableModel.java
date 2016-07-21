package fireparadox.gui.database;

import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import jeeb.lib.util.Translator;
import fireparadox.model.database.FmDBPlant;
import fireparadox.model.database.FmDBShape;

/**
 * FiSampleTableModel : sample table result
 *
 * @author Isabelle LECOMTE - March 2008
 */

public class FmSampleTableModel extends AbstractTableModel {
    private String[] superColumnNames = {
					Translator.swap ("FiSampleTableModel.team"),		//for super administrators or owners
					Translator.swap ("FiSampleTableModel.site"),
    				Translator.swap ("FiSampleTableModel.species"),
					Translator.swap ("FiSampleTableModel.type"),
				    Translator.swap ("FiSampleTableModel.method"),
				    Translator.swap ("FiSampleTableModel.height"),
				    Translator.swap ("FiSampleTableModel.diameter"),
				    Translator.swap ("FiSampleTableModel.reference"),
				     Translator.swap ("FiSampleTableModel.origin"),
 					Translator.swap ("FiSampleTableModel.validated"),
				    Translator.swap ("FiSampleTableModel.deleted")};

    private String[] columnNames = {
					Translator.swap ("FiSampleTableModel.team"),		//for others
					Translator.swap ("FiSampleTableModel.site"),
    				Translator.swap ("FiSampleTableModel.species"),
					Translator.swap ("FiSampleTableModel.type"),
				    Translator.swap ("FiSampleTableModel.method"),
				    Translator.swap ("FiSampleTableModel.height"),
				    Translator.swap ("FiSampleTableModel.diameter"),
				    Translator.swap ("FiSampleTableModel.reference"),
				    Translator.swap ("FiSampleTableModel.origin")};

    private Vector shapeList;
    private int rightLevel;


    public FmSampleTableModel (int _rightLevel) {
		rightLevel = _rightLevel;
		shapeList = new Vector();
    }
	public int getColumnCount() {
		if (rightLevel > 1 ) return superColumnNames.length;
		return columnNames.length;
	}
	@Override
	public String getColumnName(int column) {
		if (rightLevel > 1) return superColumnNames[column];
		return columnNames[column];
	}
    public int getRowCount() {
        return shapeList.size();
    }
    public Object getValueAt(int row, int col) {
      FmDBShape shape = (FmDBShape)(shapeList.elementAt (row));
 		FmDBPlant plant = shape.getPlant();

 		if (col == 0) {
			if (plant.getTeam() != null) {
		    	return plant.getTeam().getTeamCode();
			}
		    else
		    	return "null";
		}
		if (col == 1) {
			if (plant.getSite() != null) {
				return plant.getSite().getSiteCode();
			}
		    else
		    	return "null";
		}
		if(col == 2)
		    return plant.getSpecie();


        if (col == 3) {

			if (shape.getFuelType() == 3)
				return "Unique";
			if (shape.getFuelType() == 4)
				return "Core";
			if (shape.getFuelType() == 5)
				return "Edge";
			else return "null";
		}



		if (col == 4) {
			if (shape.isCubeMethod()) return "Cube";
			else return "Cage";
		}

		if (col == 5) return shape.getZMax();
		if (col == 6) return shape.getXMax();
		if (col == 7) return shape.getPlant().getReference();
		if (col == 8) return shape.getOrigin();

		if ((rightLevel > 1) && (col == 9)) return shape.getPlant().isValidated();
		if ((rightLevel > 1) && (col == 10)) return shape.isDeleted();

	  	return "--";
    }

    @Override
	public Class getColumnClass(int c) {
        return getValueAt(0,c).getClass();
    }
    @Override
	public boolean isCellEditable(int row, int col) {
         return false;
    }
    @Override
	public void setValueAt(Object value, int row, int col){
        fireTableCellUpdated(row, col);
    }

    public void setList (Vector listFuel) {
   	 	shapeList = listFuel;
    	fireTableRowsInserted(0,0);
    }
    public void addShape (FmDBShape shape) {
		shapeList.add(shape);
      	fireTableRowsInserted(shapeList.size(),shapeList.size());
    }
    public void removeShape (FmDBShape shape) {
		shapeList.remove (shape);
		this.fireTableDataChanged ();
	}
    public void clear() {
		shapeList.removeAllElements ();
		this.fireTableDataChanged ();
	}
	public FmDBShape getShape (int index) {
		return (FmDBShape)(shapeList.elementAt (index));
	}

}
