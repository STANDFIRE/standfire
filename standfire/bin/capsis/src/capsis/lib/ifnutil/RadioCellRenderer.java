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

import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class RadioCellRenderer implements TableCellRenderer {
	
	private JRadioButton rb = new JRadioButton();
	
	public RadioCellRenderer(ButtonGroup bg) {
		//super();
		bg.add(rb);
	}
	
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
    		boolean hasFocus, int row, int col){
    	if (value == "1"){
    		rb.setSelected(true);
        } else {
        	rb.setSelected(false);
        }
    	rb.setBackground(Color.WHITE);
    	//System.out.println("VALUE = " + value);
    	rb.setName(value.toString());
    	//rb.setAlignmentX(rb.CENTER_ALIGNMENT);
    	
    	//rb.setForeground(Color.blue);
    	return rb;
    }

}


