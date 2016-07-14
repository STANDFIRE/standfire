package fireparadox.gui;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.swing.table.AbstractTableModel;

import jeeb.lib.util.Translator;
import capsis.lib.fire.fuelitem.FiParticle;
import fireparadox.model.layerSet.FmLayer;


/**	A table model for the FiLocalLayerForm. 
*	a filocallayer is a filayer with additional properties.
*	@author F. de Coligny - october 2009
*/
public class FmLayerFormTableModel extends AbstractTableModel {
	public final String[] COLUMN_NAMES = {
			Translator.swap ("FiLocalLayerForm.layerName"),
			Translator.swap ("FiLocalLayerForm.spatialGroup"),
			Translator.swap ("FiLocalLayerForm.minHeight"),
			Translator.swap ("FiLocalLayerForm.maxHeight"),
			Translator.swap ("FiLocalLayerForm.patchSize"),
			Translator.swap ("FiLocalLayerForm.patchPercentage"),
			Translator.swap("FiLocalLayerForm.liveMoisturePercentage"),
			Translator.swap("FiLocalLayerForm.deadMoisturePercentage"), 
			Translator.swap ("FiLocalLayerForm.aliveBulkDensity"), 
			Translator.swap ("FiLocalLayerForm.deadBulkDensity"), 
			Translator.swap ("FiLocalLayerForm.svr"), 
			Translator.swap ("FiLocalLayerForm.mvr")};
	private List<FmLayer> layers;
	private NumberFormat formater;

	public FmLayerFormTableModel () {
		formater = NumberFormat.getInstance (Locale.ENGLISH);
		formater.setGroupingUsed (false);
		formater.setMaximumFractionDigits (3);
		layers = new ArrayList<FmLayer>();
	}

	public void addLayer(FmLayer l) {
		layers.add (l);
		//~ fireTableRowsInserted (layers.size (), layers.size ());
		fireTableDataChanged ();
	}

	public void setLayers (Collection layers) {
		this.layers.clear ();
		this.layers.addAll (layers);
		//~ fireTableRowsInserted (0, layers.size ());
		fireTableDataChanged ();
	}

	public void removeLayer(FmLayer l) {
		layers.remove (l);
		fireTableDataChanged ();
		//~ fireTableRowsDeleted (0, layers.size ());	// not precise enough
	}

	public List<FmLayer> getLayers() {
		return layers;
	}

	public FmLayer getLayer(int index) {
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
		FmLayer layer = layers.get(row);
		if (col == 0) {
			return layer.getLayerType ();
		}  else if (col == 1) {
			return layer.getSpatialGroup ();
		} else if (col == 2) {
			return layer.getBaseHeight ();
		} else if (col == 3) {
			return layer.getHeight ();
		} else if (col == 4) {
			return layer.getCharacteristicSize ();
		} else if (col == 5) {
			return layer.getCoverFraction ();
		} else if (col == 6) {
			try {
				return layer.getMoisture (0, FiParticle.LIVE);
			} catch (Exception e) {
				// TODO FP Auto-generated catch block
				e.printStackTrace ();
			}
		} else if (col == 7) {
			try {
				return layer.getMoisture (0, FiParticle.DEAD);
			} catch (Exception e) {
				// TODO FP Auto-generated catch block
				e.printStackTrace ();
			}
		} else if (col == 8) {
			return layer.getBulkDensity (0, FiParticle.LIVE);
		} else if (col == 9) {
			return layer.getBulkDensity (0, FiParticle.DEAD);
		} else if (col == 10) {
			try {
				return layer.getSVR (0, FiParticle.LIVE);
			} catch (Exception e) {
				// TODO FP Auto-generated catch block
				e.printStackTrace ();
			}
		} else if (col == 11) {
			try {
				return layer.getMVR (0, FiParticle.LIVE);
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
		//return (col >= 3);
		return true;
	}
	
	/**	Don't need to implement this method unless your table's
	*	data can change.
	*/
	@Override
	public void setValueAt (Object value, int row, int col) {
		FmLayer layer = layers.get(row);

		if (col == 0) {
			layer.setLayerType((String) value);
		} else if (col == 1) {
			layer.setSpatialGroup ((Integer) value);
		} else if (col == 2) {
			layer.setBaseHeight ((Double) value);
		} else if (col == 3) {
			layer.setHeight ((Double) value);
		} else if (col == 4) {
			layer.setCharacteristicSize ((Double) value);
		} else if (col == 5) {
			layer.setCoverFraction ((Double) value);
		} else if (col == 6) {
			try {
				layer.setMoisture ((Double) value, 0, FiParticle.LIVE);
			} catch (Exception e) {
				// TODO FP Auto-generated catch block
				e.printStackTrace ();
			}
		} else if (col == 7) {
			try {
				layer.setMoisture ((Double) value, 0, FiParticle.DEAD);
			} catch (Exception e) {
				// TODO FP Auto-generated catch block
				e.printStackTrace ();
			}
		} else if (col == 8) {
			try {
				layer.setBulkDensity ((Double) value, 0, FiParticle.LIVE);
			} catch (Exception e) {
				// TODO FP Auto-generated catch block
				e.printStackTrace ();
			}
		} else if (col == 9) {
			try {
				layer.setBulkDensity ((Double) value, 0, FiParticle.DEAD);
			} catch (Exception e) {
				// TODO FP Auto-generated catch block
				e.printStackTrace ();
			}
		} else if (col == 10) {
			try {
				layer.setSVR ((Double) value, 0, FiParticle.LIVE);
			} catch (Exception e) {
				// TODO FP Auto-generated catch block
				e.printStackTrace ();
			}
		} else if (col == 11) {
			try {
				layer.setMVR ((Double) value, 0, FiParticle.LIVE);
			} catch (Exception e) {
				// TODO FP Auto-generated catch block
				e.printStackTrace ();
			}
		}

		fireTableCellUpdated (row, col);
	}

	@Override
	public String toString () {
		StringBuffer b = new StringBuffer ("FiLocalLayerFormTableModel...\n");
		for (int i = 0; i < getRowCount(); i++) {
			b.append (layers.get (i));
			b.append ('\n');
		}
		return b.toString ();
	}

}
