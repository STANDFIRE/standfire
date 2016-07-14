package fireparadox.gui.database;

import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import jeeb.lib.util.Translator;
import fireparadox.model.database.FmDBEvent;

/**
 * FiSiteEventTableModel : Site events table result
 *
 * @author Isabelle LECOMTE - March 2008
 */

public class FmSiteEventTableModel extends AbstractTableModel {
    private String[] columnNames = {
					Translator.swap ("FiSiteEventTableModel.typeEvent"),
				    Translator.swap ("FiSiteEventTableModel.dateStart"),
				    Translator.swap ("FiSiteEventTableModel.dateEnd")};



    private Vector eventList;



    /** Creates a new instance of researchResultTableMode */
    public FmSiteEventTableModel () {
		eventList = new Vector();
    }
	public int getColumnCount() {
		return columnNames.length;
	}
	@Override
	public String getColumnName(int column) {
		return columnNames[column];
	}
    public int getRowCount() {
        return eventList.size();
    }
    public Object getValueAt(int row, int col) {
       FmDBEvent event = (FmDBEvent)(eventList.elementAt (row));

        if (col == 0) return event.getName();
		if (col == 1) return event.getDateStart();
		if (col == 2) return event.getDateEnd();


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

    public void setList (Vector list) {
   	 	eventList = list;
    	fireTableRowsInserted(0,0);
    }
    public void addEvent (FmDBEvent event) {
		eventList.add(event);
      	fireTableRowsInserted(eventList.size(),eventList.size());
    }
    public void removeEvent (FmDBEvent event) {
		eventList.remove (event);
		this.fireTableDataChanged ();
	}
    public void clear() {
		eventList.removeAllElements ();
		this.fireTableDataChanged ();
	}
	public FmDBEvent getEvent (int index) {
		return (FmDBEvent)(eventList.elementAt (index));
	}

}
