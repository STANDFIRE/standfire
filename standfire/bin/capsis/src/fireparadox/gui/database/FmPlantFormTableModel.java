package fireparadox.gui.database;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.table.AbstractTableModel;

import capsis.lib.fire.FiConstants;
import jeeb.lib.util.AmapTools;
import jeeb.lib.util.Translator;


/**	FiPlantFormTableModel, for the table in FiPlantForm.
*	@author Oana Vigy & Eric Rigaud - March 2007
*/
public class FmPlantFormTableModel extends AbstractTableModel {
	// fc - sept 2009 review
	
    private String[] columnNames = {Translator.swap ("FiPlantFormTableModel.speciesName"),
			Translator.swap ("FiPlantFormTableModel.height"),
			Translator.swap ("FiPlantFormTableModel.crownBaseHeight"),
			Translator.swap ("FiPlantFormTableModel.crownDiameter"),
			Translator.swap ("FiPlantFormTableModel.meanBulkdensity0_2mm"),
			Translator.swap ("FiPlantFormTableModel.leafAreaIndex"),
			Translator.swap("FiPlantFormTableModel.liveMoisture"),
			Translator.swap("FiPlantFormTableModel.deadMoisture") };

	private List<FmPlantFormLine> plants = new ArrayList<FmPlantFormLine> ();

	
	/** Constructor
	*/
	public FmPlantFormTableModel () {super ();}
    
	@Override
	public String getColumnName (int col) {return columnNames[col];}
    
	public int getRowCount () {return plants.size ();}
	
	public int getColumnCount () {return columnNames.length;}
    
	public Object getValueAt (int row, int col) {
		try {
			FmPlantFormLine plant = plants.get (row);
			if (col == 0) {
				return plant.getSpeciesName ();
			} else if (col == 1) {
				return plant.getHeight ();
			} else if (col == 2) {
				return plant.getCrownBaseHeight ();
			} else if (col == 3) {
				return plant.getCrownDiameter ();
			} else if (col == 4) {
				return plant.getMeanBulkdensity0_2mm ();
			} else if (col == 5) {
				return plant.getLai ();
			} else if (col == 6) {
				return (plant.getLiveMoisture());
			} else if (col == 7) {
				return (plant.getDeadMoisture());
			}
		} catch (Exception e) {}	// when filtering, the tableModel may temporary be empty
		return null;
	}
	
	// Only column 6 (moisture) is editable
	@Override
	public boolean isCellEditable (int row, int col) {return col == 6;}
	
	@Override
	public void setValueAt (Object value, int row, int col){
		if (col == 6) {
			plants.get(row).setLiveMoisture((Double) value);
		}
		if (col == 7) {
			plants.get(row).setDeadMoisture((Double) value);
		}
		fireTableCellUpdated (row, col);
	}
	
	@Override
	public Class getColumnClass (int c) {
		try {
			return getValueAt (0, c).getClass ();
		} catch (Exception e) {
			return Object.class;
		}
	}
    
	private void initMoisture () {
    	for (int row = 0; row < getRowCount (); row++) {
        	FmPlantFormLine plant = plants.get (row);
			if (plant.getHeight () <= FiConstants.HEIGHT_THRESHOLD) {
				plant.setLiveMoisture(FiConstants.SHRUB_MOISTURE);
    		} else {
				plant.setLiveMoisture(FiConstants.TREE_MOISTURE);
			}
			plant.setDeadMoisture(FiConstants.DEAD_MOISTURE);
    	}
    }

    public void setPlants (List<FmPlantFormLine> list) {
		// list must not be null
		if (list == null) {return;}
    	
		//~ plants = list;
    	Set<FmPlantFormLine> tmp = new TreeSet<FmPlantFormLine> (list);
		System.out.println ("FiPlantFormTableModel, setPlants (), sorted: "+AmapTools.toString (tmp));
		
    	plants = new ArrayList<FmPlantFormLine> (tmp);
		
		initMoisture ();
		this.fireTableDataChanged ();
    }
    
	public void clear () {
		plants.clear ();
		this.fireTableDataChanged ();
	}
	
	public FmPlantFormLine getPlant (int index) {return plants.get (index);}
	

}
