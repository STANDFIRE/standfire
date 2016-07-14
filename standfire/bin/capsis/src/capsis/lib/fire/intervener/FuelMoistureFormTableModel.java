package capsis.lib.fire.intervener;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.swing.table.AbstractTableModel;

import jeeb.lib.util.Translator;
import capsis.lib.fire.fuelitem.FiParticle;
import fireparadox.model.layerSet.FmLayer;
import fireparadox.model.layerSet.FmLayerSet;

/**
 * A table model for the FuelMoistureForm.
 * 
 * @author Pimont - spte 2011
 */
public class FuelMoistureFormTableModel extends AbstractTableModel {
	public final String[] COLUMN_NAMES = { Translator.swap("FuelMoistureForm.layerSetName"),
			Translator.swap("FuelMoistureForm.shrubLiveMoisture"),
			Translator.swap("FuelMoistureForm.shrubDeadMoisture"),
			Translator.swap("FuelMoistureForm.herbLiveMoisture"), Translator.swap("FuelMoistureForm.herbDeadMoisture") };

	private List<FmLayerSet> layerSets;
	private NumberFormat formater;

	public FuelMoistureFormTableModel() {
		formater = NumberFormat.getInstance(Locale.ENGLISH);
		formater.setGroupingUsed(false);
		formater.setMaximumFractionDigits(3);
		layerSets = new ArrayList<FmLayerSet>();
	}

	public void addLayerSet(FmLayerSet ls) {
		layerSets.add(ls);
		// ~ fireTableRowsInserted (layers.size (), layers.size ());
		fireTableDataChanged();
	}

	public void setLayerSets(Collection layerSets) {
		this.layerSets.clear();
		this.layerSets.addAll(layerSets);
		// ~ fireTableRowsInserted (0, layers.size ());
		fireTableDataChanged();
	}

	public void removeLayerSet(FmLayerSet ls) {
		layerSets.remove(ls);
		fireTableDataChanged();
		// ~ fireTableRowsDeleted (0, layers.size ()); // not precise enough
	}

	public List<FmLayerSet> getLayerSets() {
		return layerSets;
	}

	public FmLayerSet getLayerSet(int index) {
		return layerSets.get(index);
	}

	public void clear() {
		layerSets.clear();
		fireTableDataChanged();
	}

	public int getColumnCount() {
		return COLUMN_NAMES.length;
	}

	public int getRowCount() {
		return layerSets.size();
	}

	@Override
	public String getColumnName(int col) {
		return COLUMN_NAMES[col];
	}

	public Object getValueAt(int row, int col) {
		FmLayerSet ls = layerSets.get(row);
		if (col == 0) {
			return ls.getName();
		} else if (col == 1) {
			try {
				return ls.getLayer(FmLayer.SHRUB).getMoisture(0, FiParticle.LIVE);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (col == 2) {
			try {
				return ls.getLayer(FmLayer.SHRUB).getMoisture(0, FiParticle.DEAD);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (col == 3) {
			try {
				return ls.getLayer(FmLayer.HERB).getMoisture(0, FiParticle.LIVE);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (col == 4) {
			try {
				return ls.getLayer(FmLayer.HERB).getMoisture(0, FiParticle.DEAD);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return "-";
	}

	@Override
	public Class getColumnClass(int c) {
		try {
			return getValueAt(0, c).getClass();
		} catch (Exception e) {
			return Object.class;
		}
	}

	/**
	 * Don't need to implement this method unless your table's editable.
	 */
	@Override
	public boolean isCellEditable(int row, int col) {
		// ~ return isEditorShown () && (col == 3 || col == 4 || col == 5);
		// return (col >= 3);
		return (col >= 1);
	}

	/**
	 * Don't need to implement this method unless your table's data can change.
	 */
	@Override
	public void setValueAt(Object value, int row, int col) {
		FmLayerSet layerSet = layerSets.get(row);
		FmLayer shrub = null;
		FmLayer herb = null;
		try {
			shrub = layerSet.getLayer(FmLayer.SHRUB);
			// System.out.println("shrub is :" + shrub.getSpeciesName());
			herb = layerSet.getLayer(FmLayer.HERB);
			// System.out.println("herb is :" + herb.getSpeciesName());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (col == 0) {
			// layerSet.setName((String) value);
		} else if (col == 1) {
			try {
				shrub.setMoisture((Double) value, 0, FiParticle.LIVE);
			} catch (Exception e) {
				// TODO FP Auto-generated catch block
				e.printStackTrace();
			}
		} else if (col == 2) {
			try {
				shrub.setMoisture((Double) value, 0, FiParticle.DEAD);
			} catch (Exception e) {
				// TODO FP Auto-generated catch block
				e.printStackTrace();
			}
		} else if (col == 3) {
			try {
				herb.setMoisture((Double) value, 0, FiParticle.LIVE);
			} catch (Exception e) {
				// TODO FP Auto-generated catch block
				e.printStackTrace();
			}
		} else if (col == 4) {
			try {
				herb.setMoisture((Double) value, 0, FiParticle.DEAD);
			} catch (Exception e) {
				// TODO FP Auto-generated catch block
				e.printStackTrace();
			}
		}
		fireTableCellUpdated(row, col);
	}

	@Override
	public String toString() {
		StringBuffer b = new StringBuffer("FuelMoistureFormTableModel...\n");
		for (int i = 0; i < getRowCount(); i++) {
			b.append(layerSets.get(i));
			b.append('\n');
		}
		return b.toString();
	}

}
