/*
* The ifn library for Capsis4
*
* Copyright (C) 2006 J-L Cousin, M-D Van Damme
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied
* warranty of MERCHANTABILITY or FITNESS FOR A
* PARTICULAR PURPOSE. See the GNU Lesser General Public
* License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/
package capsis.lib.ifnutil;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EventObject;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;

public class RadioCellEditor implements TableCellEditor {

	private JRadioButton cellRadio = new JRadioButton();
	
	public RadioCellEditor(ButtonGroup bg){
		cellRadio.addActionListener(new ActionListener() {
	         public void actionPerformed(ActionEvent e) {
	           b_actionPerformed(e);
	         }
	     });
		bg.add(cellRadio);
	}
	
	public Component getTableCellEditorComponent(JTable arg0, Object value, boolean isSelected,
            int row, int col) {
		//cellRadio.setName(value.toString());
		cellRadio.setForeground(Color.blue);
		if (value == "1"){
			cellRadio.setSelected(true);
        } else {
        	cellRadio.setSelected(isSelected);
        }
		return cellRadio;
	}

	public Object getCellEditorValue() {
		// TODO Auto-generated method stub
		if(cellRadio.isSelected()) return "1";
		else return "0";
		//return null;
	}
	
	void b_actionPerformed(ActionEvent e) {
		/*
		 * Color c =  jcc.showDialog(b, "choix de couleur",
	                                b.getBackground() );
	      laTable.getModel().setValueAt(c, i, j);
	      b.setBackground(c);
	     */
	}

	public boolean isCellEditable(EventObject anEvent) {
		return true;
	}

	public boolean shouldSelectCell(EventObject anEvent) {
		return true;
	}

	public boolean stopCellEditing() {
		return true ; // pour ne pas pouvoir recommencer une édition
	    //return false ; // pour pouvoir recommencer une édition
	}

	public void cancelCellEditing() {   
	}

	public void addCellEditorListener(CellEditorListener l) {   
	}

	public void removeCellEditorListener(CellEditorListener l) { 
	}
	
}
