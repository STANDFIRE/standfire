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

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Toolkit;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class PanelDBChoiceList extends PanelDBList {
	
	/**
	 * Constructor
	 * @param dlg : DialogBox which contains panel
	 * @param titre : panel title
	 */
	public PanelDBChoiceList(DialogBase dlg, String Titre) throws IfnException {
	    super(dlg,Titre);
	}

	/**
	 * Default constructor
	 * @throws IfnException
	 */
	public PanelDBChoiceList() throws IfnException {
		super();
	}
	
	public void RefreshPanelWithQuery(String in_queryToProcess) throws IfnException {

	    queryToProcess = in_queryToProcess;
	    // Lors du premier passage
	    if (firstPass) {
	    	String name;
	    	int columnSize;
	    	table.setFont(dialogBase.listFont);

	    	control = dialogBase.getControler(queryToProcess.toString());
	    	if (control.getColumnCount() >0) {
	    		table.removeAll();

	    		TableColumnModel colModel = table.getColumnModel();
	    		if (colModel !=null) {
	    			int nbcol = colModel.getColumnCount();
	    			for (int i=0;i< nbcol;i++) {
	    				table.removeColumn(colModel.getColumn(0));
	    			}
	    		}
	    	}
	    	table.setAutoCreateColumnsFromModel(false);
	    	table.setModel(control);

	    	// Affectation du nombre de record
	    	Integer numberRecordInteger = new Integer(control.getRowCount());
	    	// recordNumberValueLabel.setText("Nb. =" + numberRecordInteger.toString()+" ");
	    	// System.out.println("Nb. =" + numberRecordInteger.toString()+" ");
	    	// gestion des alignements et tailles des colonnes
	    	
	    	DefaultTableCellRenderer render = new DefaultTableCellRenderer();
	        render.setHorizontalAlignment(JLabel.CENTER);
	    	TableColumn firstColumn = new TableColumn(0,20, render, null);
	    	firstColumn.setPreferredWidth(20);
	        table.addColumn(firstColumn);
	    	
	    	for (int i=0;i<control.getColumnCount();i++) {
	    		// determination de la taille de la colone en pixels
	    		name = control.getColumnName(i).trim();
	    		//	System.out.println("Name = " + name);
	    		columnSize = control.getColumnScreenSize(i);
	    		// Permet d'agrandir les colonnes pour qu'à l'impression
	    		// le resultat soit correct
	    		columnSize++;
	    		FontMetrics metrics = getFontMetrics(table.getFont());
	    		char[] wstring = new char [columnSize];
	    		// création d'un mot de largeur maximum (cas des polices proportionnelles
	    		for (int j=0;j<columnSize;j++) {
	    			wstring[j] ='W';
	    		}
	    		String wString = new String(wstring);
	    		columnSize = metrics.stringWidth( wString );
	    		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
	    		renderer.setHorizontalAlignment(control.getColumnAlignement(i));
	    		TableColumn column = new TableColumn(i+1,columnSize, renderer, null);
	    		column.setPreferredWidth(columnSize);
	    		table.addColumn(column);
	    	}

	    	TableColumnModel newColModel = table.getColumnModel();

	    	Dimension screenSize = this.getSize();
	    	int panelSize = screenSize.width;
	    	// cas de liste pour lequel setvisible non fait
	    	if (panelSize == 0) {
	    		//approximation: recuperation de la taille ecran au lieu de la taille liste
	    		screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	    		panelSize = screenSize.width;
	    	}
	    	int columnsSize = newColModel.getTotalColumnWidth();
	    	if (columnsSize < panelSize) {
	    		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
	    	} else {
	    		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	    	}
	    	control.updateTable();
	    	table.sizeColumnsToFit(1);

	    	firstPass = false;
	    } //FIN if (firstPass)
	    // Lors des passages suivants
	    else {
	    	//System.out.println("Second passage ...");
	    	// Execution de la requete
	    	control = dialogBase.getControler(queryToProcess.toString());
	    	// Affectation du nombre de record
	    	Integer numberRecordInteger = new Integer(control.getRowCount());
	        // recordNumberValueLabel.setText("Nb. =" + numberRecordInteger.toString()+" ");
	    	table.setAutoCreateColumnsFromModel(false);
	    	table.setModel(control);
	    	control.updateTable();
	    }
	    //liste vide appelee rafraichie par F5 (non appelee dans le constructeur)
	    if (control.getRowCount()==0 && getSize().width>0)
	    {
	      //si nous ne sommes pas en phase de d'InitAfterActivation
	      if (!dialogBase.activationState())
	          JOptionPane.showMessageDialog(this,getString(CV_MESSAGE,CV_LIST_EMPTY),
	                                     "INFORMATION",JOptionPane.INFORMATION_MESSAGE);
	    }
	  }

}
