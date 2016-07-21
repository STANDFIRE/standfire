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

/*
 * DBIfnList.java
 *
 * Created on 1 juin 2006, 16:14
 *
 */

import java.awt.Color;
import java.awt.event.FocusListener;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

/**
 *
 * @author mdvandamme
 */

public class DBIfnList extends DialogBase {

    private JList list;
    private JScrollPane listScrollPane;
    private DefaultListModel listModel;

    private Integer selected;
    private String name;
    private String query;
    private Controler control;
    private String dbColumnName;
    private String dbColumnId;

    /**
     * Creates a new instance of DBIfnList
     */
    public DBIfnList() {
    }

    public DBIfnList(String sql, String name, String wordingColumn, String idColumn) {
	query = sql;
	dbColumnName = wordingColumn;
	dbColumnId = idColumn;
        listScrollPane = new JScrollPane();
        listModel = new DefaultListModel();
    }

    public void init() {
        try {
            control = this.getControler(query);
            for(int i=0;i<control.getRowCount();i++){
                listModel.addElement(control.getValueAt(i,dbColumnName));
		if(i == 0) selected = (Integer)control.getValueAt(i,dbColumnId);
	    }
            list = new JList(listModel);
            list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            list.setSelectedIndex(0);
            list.setVisibleRowCount(3);
            listScrollPane = new JScrollPane(list);

            listScrollPane.setName("db_etude");
            listScrollPane.setSize(300,50);
            list.setBackground(Color.GREEN);
            list.addFocusListener((FocusListener)this);

	} catch(Exception e) {
            //traceError( "ExemplDialog", e.toString());
	    System.out.println(e.toString());
	}
    }

    public JScrollPane getJScrollPane() {
	return listScrollPane;
    }

}
