package fireparadox.gui.database;

import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import jeeb.lib.util.Translator;
import fireparadox.model.database.FmDBPlant;

/**
 * FiPlantTableModel : Fuel database management (plant table result)
 *
 * @author Isabelle LECOMTE - March 2008
 */

public class FmPlantTableModel extends AbstractTableModel {

    private String[] superPlantColumnNames = {
					Translator.swap ("FiPlantTableModel.team"),		//for super administrators or owners
					Translator.swap ("FiPlantTableModel.site"),
    				Translator.swap ("FiPlantTableModel.species"),
				    Translator.swap ("FiPlantTableModel.height"),
				    Translator.swap ("FiPlantTableModel.CBH"),
				    Translator.swap ("FiPlantTableModel.diameter"),
				    Translator.swap ("FiPlantTableModel.reference"),
				    Translator.swap ("FiPlantTableModel.origin"),
				    Translator.swap ("FiPlantTableModel.validated"),
				    Translator.swap ("FiPlantTableModel.deleted")};

    private String[] plantColumnNames = {
					Translator.swap ("FiPlantTableModel.team"),		//for others
					Translator.swap ("FiPlantTableModel.site"),
					Translator.swap ("FiPlantTableModel.species"),
				    Translator.swap ("FiPlantTableModel.height"),
				    Translator.swap ("FiPlantTableModel.CBH"),
				    Translator.swap ("FiPlantTableModel.diameter"),
				    Translator.swap ("FiPlantTableModel.reference"),
				    Translator.swap ("FiPlantTableModel.origin")};

	private String[] superLayerColumnNames = {						//for super administrators or owners
					Translator.swap ("FiPlantTableModel.team"),
					Translator.swap ("FiPlantTableModel.site"),
					Translator.swap ("FiPlantTableModel.species"),
					Translator.swap ("FiPlantTableModel.height"),
					Translator.swap ("FiPlantTableModel.CBH"),
					Translator.swap ("FiPlantTableModel.reference"),
					Translator.swap ("FiPlantTableModel.origin"),
					Translator.swap ("FiPlantTableModel.validated"),
					Translator.swap ("FiPlantTableModel.deleted")};

    private String[] layerColumnNames = {							//for others
					Translator.swap ("FiPlantTableModel.team"),
					Translator.swap ("FiPlantTableModel.site"),
					Translator.swap ("FiPlantTableModel.species"),
				    Translator.swap ("FiPlantTableModel.height"),
				    Translator.swap ("FiPlantTableModel.CBH"),
				    Translator.swap ("FiPlantTableModel.reference"),
				    Translator.swap ("FiPlantTableModel.origin")};

	private String[] superSampleColumnNames = {						//for super administrators or owners
					Translator.swap ("FiPlantTableModel.team"),
					Translator.swap ("FiPlantTableModel.site"),
					Translator.swap ("FiPlantTableModel.species"),
					Translator.swap ("FiPlantTableModel.height"),
					Translator.swap ("FiPlantTableModel.reference"),
					Translator.swap ("FiPlantTableModel.origin"),
					Translator.swap ("FiPlantTableModel.validated"),
					Translator.swap ("FiPlantTableModel.deleted")};

    private String[] sampleColumnNames = {							//for others
					Translator.swap ("FiPlantTableModel.team"),
					Translator.swap ("FiPlantTableModel.site"),
					Translator.swap ("FiPlantTableModel.species"),
				    Translator.swap ("FiPlantTableModel.height"),
				    Translator.swap ("FiPlantTableModel.reference"),
				    Translator.swap ("FiPlantTableModel.origin")};


    private Vector plantList;
    private int rightLevel;
    private int fuelType;

    /** Creates a new instance of researchResultTableMode */
    public FmPlantTableModel(int _rightLevel, int _fuelType) {
		rightLevel = _rightLevel;
		fuelType = _fuelType;
		plantList = new Vector();
    }
	public int getColumnCount() {
		if (fuelType == 1 ) {
			if (rightLevel > 1) return superPlantColumnNames.length;
			return plantColumnNames.length;
		}
		else if (fuelType == 2) {
			if (rightLevel > 1) return superLayerColumnNames.length;
			return layerColumnNames.length;
		}
		else if (fuelType == 3) {
			if (rightLevel > 1) return superSampleColumnNames.length;
			return sampleColumnNames.length;
		}
		else return 0;
	}
	@Override
	public String getColumnName(int column) {
		if (fuelType == 1 ) {
			if (rightLevel > 1) return superPlantColumnNames[column];
			return plantColumnNames[column];
		}
		else if (fuelType == 2 ) {
			if (rightLevel > 1) return superLayerColumnNames[column];
			return layerColumnNames[column];
		}
		else if (fuelType == 3 ) {
			if (rightLevel > 1) return superSampleColumnNames[column];
			return sampleColumnNames[column];
		}
		else return null;
	}
    public int getRowCount() {
        return plantList.size();
    }
    public Object getValueAt(int row, int col) {
      FmDBPlant plant = (FmDBPlant)(plantList.elementAt (row));
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
		if(col == 3)
            return plant.getHeight();


        if (fuelType == 1) {
			if(col == 4)
           		return plant.getCrownBaseHeight();
			if(col == 5)
				return plant.getCrownDiameter();
			if(col == 6)
				return plant.getReference();
			if(col == 7)
				return plant.getOrigin();
			if ((rightLevel > 1) && (col == 8)) return plant.isValidated();
			if ((rightLevel > 1) && (col == 9)) return plant.isDeleted();

		}
		else if (fuelType == 2) {
			if(col == 4)
           		return plant.getCrownBaseHeight();
  			if(col == 5)
				return plant.getReference();
			if(col == 6)
				return plant.getOrigin();
			if ((rightLevel > 1) && (col == 7)) return plant.isValidated();
			if ((rightLevel > 1) && (col == 8)) return plant.isDeleted();

		}
		else if (fuelType == 3) {
			if(col == 4) return plant.getReference();
			if(col == 5) return plant.getOrigin();
			if ((rightLevel > 1) && (col == 6)) return plant.isValidated();
			if ((rightLevel > 1) && (col == 7)) return plant.isDeleted();
		}


	  	return "--";
    }

    @Override
	public Class getColumnClass (int c) {
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

    public void setListPlant (Vector list) {
    plantList = list;
    fireTableRowsInserted(0,0);
    }
    public void addPlant (FmDBPlant plant) {
		plantList.add (plant);
      	fireTableRowsInserted (plantList.size(),plantList.size());
    }
    public void removePlant (FmDBPlant plant) {
		 plantList.remove (plant);
		this.fireTableDataChanged ();
	}
    public void clear () {
		plantList.removeAllElements ();
		this.fireTableDataChanged ();
	}
	public FmDBPlant getPlant (int index) {
		return (FmDBPlant)(plantList.elementAt (index));
	}

}
