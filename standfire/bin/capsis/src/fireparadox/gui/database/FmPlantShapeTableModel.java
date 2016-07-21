package fireparadox.gui.database;

import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import jeeb.lib.util.Translator;
import fireparadox.model.database.FmDBShape;

/**
 * FiPlantShapeTableModel : Plant shape table result
 *
 * @author Isabelle LECOMTE - March 2008
 */

public class FmPlantShapeTableModel extends AbstractTableModel {
    private String[] superColumnNames = {								//for super administrators
					Translator.swap ("FiPlantShapeTableModel.type"),
				    Translator.swap ("FiPlantShapeTableModel.kind"),
				    Translator.swap ("FiPlantShapeTableModel.method"),
				    Translator.swap ("FiPlantShapeTableModel.height"),
				    Translator.swap ("FiPlantShapeTableModel.diameter"),
				    Translator.swap ("FiPlantShapeTableModel.deleted")};

    private String[] columnNames = {									//for others
					Translator.swap ("FiPlantShapeTableModel.type"),
					Translator.swap ("FiPlantShapeTableModel.kind"),
					Translator.swap ("FiPlantShapeTableModel.method"),
				    Translator.swap ("FiPlantShapeTableModel.height"),
				    Translator.swap ("FiPlantShapeTableModel.diameter")};

    private Vector shapeList;
    private int rightLevel;


    /** Creates a new instance of researchResultTableMode */
    public FmPlantShapeTableModel (int _rightLevel) {
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

        if (col == 0) {
			if (shape.getFuelType() == 1)
				return "Plant";
			if (shape.getFuelType() == 2)
				return "Layer";
			if (shape.getFuelType() == 3)
				return "Sample Unique";
			if (shape.getFuelType() == 4)
				return "Sample Core";
			if (shape.getFuelType() == 5)
				return "Sample Edge";
			else return "null";
		}

		if (col == 1) {
			if (shape.getFuelType() < 3) return shape.getShapeKind();
			else return "";
		}

		if (col == 2) {
			if (shape.isCubeMethod()) return "Cube";
			else return "Cage";
		}

		if (col == 3) return shape.getZMax();
		if (col == 4) return shape.getXMax();


		if ((rightLevel > 1) && (col == 5)) return shape.isDeleted();

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
