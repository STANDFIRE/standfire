package fireparadox.gui;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.table.AbstractTableModel;

import jeeb.lib.util.Translator;
import capsis.lib.fire.fuelitem.FiLayer;
import capsis.lib.fire.fuelitem.FiParticle;
import fireparadox.model.database.FmLayerFromDB;


/**	A table model for candidate layers in FiLayerForm.
*	@author F. de Coligny - september 2009
*/
public class FmLayerFormTableModel1 extends AbstractTableModel {
	public final String[] COLUMN_NAMES = {
			Translator.swap ("FiLayerForm.layerName"),
			Translator.swap ("FiLayerForm.minHeight"),
			Translator.swap("FiLayerForm.maxHeight"),
			Translator.swap("FiLayerForm.layerDensity"),
			Translator.swap("FiLayerForm.leafAreaIndex") };

	private List<FmLayerFromDB> layers;
	private NumberFormat formater;

	public FmLayerFormTableModel1 () {
		formater = NumberFormat.getInstance (Locale.ENGLISH);
		formater.setGroupingUsed (false);
		formater.setMaximumFractionDigits (3);
		layers = new ArrayList<FmLayerFromDB>();
	}

	public void addLayer(FmLayerFromDB layer) {
		layers.add (layer);
		fireTableDataChanged ();
	}

	public void removeLayer(FmLayerFromDB layer) {
		layers.remove (layer);
		fireTableDataChanged ();
	}

	public List<FmLayerFromDB> getLayers() {
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
		FiLayer layer = layers.get (row);
		if (col == 0) {
			return layer.getLayerType ();
		} else if (col == 1) {
			return layer.getBaseHeight ();
		} else if (col == 2) {
			return layer.getHeight ();
		} else if (col == 3) {
			return layer.getSumBulkDensity (FiParticle.ALL);
		} else if (col == 4) {
			try {
				return layer.getLai ();
			} catch (Exception e) {
				// TODO FP Auto-generated catch block
				e.printStackTrace();
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
	public boolean isCellEditable(int row, int col) {
		return false;
	}



	@Override
	public String toString () {
		StringBuffer b = new StringBuffer ("FiLayerFormTableModel1...\n");
		for (int i = 0; i < getRowCount(); i++) {
			b.append (layers.get (i));
			b.append ('\n');
		}
		return b.toString ();
	}

}
