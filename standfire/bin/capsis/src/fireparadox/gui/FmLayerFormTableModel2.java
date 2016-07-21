package fireparadox.gui;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.swing.table.AbstractTableModel;

import jeeb.lib.util.Translator;
import capsis.lib.fire.fuelitem.FiLayer;
import capsis.lib.fire.fuelitem.FiParticle;
import fireparadox.model.layerSet.FmLayer;


/**	A table model for selected layers part of the under construction
*	layerSet in FiLayerForm.
*	@author F. de Coligny - september 2009
*/
public class FmLayerFormTableModel2 extends AbstractTableModel {
	public final String[] COLUMN_NAMES = {
			Translator.swap ("FiLayerForm.layerName"),
			Translator.swap ("FiLayerForm.minHeight"),
			Translator.swap ("FiLayerForm.maxHeight"),
			Translator.swap ("FiLayerForm.layerSize"),
			Translator.swap ("FiLayerForm.layerPercentage"),
			Translator.swap("FiLayerForm.liveMoisturePercentage"),
			Translator.swap("FiLayerForm.deadMoisturePercentage") };
	private List<FmLayer> layers;
	private NumberFormat formater;

	public FmLayerFormTableModel2 () {
		formater = NumberFormat.getInstance (Locale.ENGLISH);
		formater.setGroupingUsed (false);
		formater.setMaximumFractionDigits (3);
		layers = new ArrayList<FmLayer> ();
	}

	public void addLayer (FmLayer fmLayer) {
		layers.add (fmLayer);
		//~ fireTableRowsInserted (layers.size (), layers.size ());
		fireTableDataChanged ();
	}

	public void setLayers (Collection<FmLayer> layers) {
		this.layers.clear ();
		this.layers.addAll (layers);
		//~ fireTableRowsInserted (0, layers.size ());
		fireTableDataChanged ();
	}

	public void removeLayer (FiLayer layer) {
		layers.remove (layer);
		fireTableDataChanged ();
		//~ fireTableRowsDeleted (0, layers.size ());	// not precise enough
	}

	public List<FmLayer> getLayers () {
		return layers;
	}

	public FiLayer getLayer (int index) {
		return layers.get (index);
	}

	public void clear () {
		layers.clear ();
		fireTableDataChanged ();
	}

	public int getColumnCount () {return COLUMN_NAMES.length;}

	public int getRowCount () {return layers.size ();}

	@Override
	public String getColumnName (int col) {return COLUMN_NAMES[col];}

	public Object getValueAt (int row, int col) {
		FmLayer layer = layers.get (row);
		if (col == 0) {
			return layer.getLayerType ();
		} else if (col == 1) {
			return layer.getBaseHeight ();
		} else if (col == 2) {
			return layer.getHeight ();
		} else if (col == 3) {
			return layer.getCharacteristicSize ();
		} else if (col == 4) {
			return layer.getCoverFraction ();
		} else if (col == 5) {
			try {
				return layer.getMoisture (0, FiParticle.LIVE);
			} catch (Exception e) {
				// TODO FP Auto-generated catch block
				e.printStackTrace ();
			}
		} else if (col == 6) {
			try {
				return layer.getMoisture (0, FiParticle.DEAD);
			} catch (Exception e) {
				// TODO FP Auto-generated catch block
				e.printStackTrace ();
			}
		}
		return "-";
	}

	@Override
	public Class getColumnClass (int c) {
		try {
			return getValueAt (0, c).getClass ();
		} catch (Exception e) {
			return Object.class;
		}
	}

	/**	Don't need to implement this method unless your table's
	*	editable.
	*/
	@Override
	public boolean isCellEditable (int row, int col) {
		//~ return isEditorShown () && (col == 3 || col == 4 || col == 5);
		return (col == 3 || col == 4 || col == 5 || col == 6);
	}
	
	/**	Don't need to implement this method unless your table's
	*	data can change.
	*/
	@Override
	public void setValueAt (Object value, int row, int col) {
		FmLayer layer = layers.get (row);

		if (col == 3) {
			layer.setCharacteristicSize ((Double) value);
		} else if (col == 4) {
			layer.setCoverFraction ((Double) value);
		} else if (col == 5) {
			try {
				layer.setMoisture ((Double) value, 0, FiParticle.LIVE);
			} catch (Exception e) {
				// TODO FP Auto-generated catch block
				e.printStackTrace ();
			}
		} else if (col == 6) {
			try {
				layer.setMoisture ((Double) value, 0, FiParticle.DEAD);
			} catch (Exception e) {
				// TODO FP Auto-generated catch block
				e.printStackTrace ();
			}
		}

		fireTableCellUpdated (row, col);
	}

	@Override
	public String toString () {
		StringBuffer b = new StringBuffer ("FiLayerFormTableModel2...\n");
		for (int i = 0; i < getRowCount(); i++) {
			b.append (layers.get (i));
			b.append ('\n');
		}
		return b.toString ();
	}

}
