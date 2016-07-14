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
import java.awt.Dimension;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ListSelectionModel;

/**
 *  
 * @author mdvandamme - october 2006
 *
 */

public class IfnJList extends JList {
	
	Controler control;
    private DialogBase dialogBase;
    private int listLenght;

    // Tableau contenant les codes et libelles de la combo
    private String listComboBox[][];

    private String columnName;
    private int JComboBoxLenght;
    private String query;
    private Dimension dimension = new Dimension(180,20);
    protected Color BackgroundColor;
    
    private DefaultListModel dlm;
    
    /**
     * Default Constructor
     */
     public IfnJList () {
         super();
     }

     /**
     * Constructor
     */
     public IfnJList(DialogBase in_dialogBase, String sql) {
         super();
         dialogBase = in_dialogBase;
         query = sql;
         dlm = new DefaultListModel();
         this.setModel(dlm);
         setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
         
         //columnName = in_columnName;
         initJList();
     }
     
     /**
      * Initialisation de la JList
      *
      */
     private void initJList() {

     	try {
     		control = dialogBase.getControler(query);
     		// System.out.println("Execution de la requete : " + query);
     	} catch(Exception exp){
     		System.out.println("ERR SQL : " + query);
     		System.out.println("ERR : " + exp.toString());
     		dialogBase.traceError("StkJComboBox.initJComboBox",exp.toString()+columnName);
     		System.out.println("ERROR getControler : " + exp.toString());
     	}

     	// Declaration tableau
     	listLenght = control.getRowCount();
     	listComboBox = new String[2][listLenght];
     	//String[] data = new String[control.getRowCount()];

     	if (listLenght>0){
     		for (int i=0;i<listLenght;i++){
     			//Alimenter tableau
     			listComboBox[0][i]=control.getValueAt(i,0).toString().trim();
     			listComboBox[1][i]=control.getValueAt(i,1).toString().trim();
     			//System.out.print(i);
     			//System.out.print("Champ 0 = " + listComboBox[0][i] + " Champ 1 = " + listComboBox[1][i]);
     			try {
     				if(control.getValueAt(i,1) != null){
     					//addItem(control.getValueAt(i,1).toString());
     					//data[i] = control.getValueAt(i,"LIBELLE").toString();
     					dlm.addElement(control.getValueAt(i,"LIBELLE").toString());
     				}
     					
     			} catch(Exception e){
     				//System.out.println("!!!"+e.toString());
     			}
     			//System.out.println("*");
     			//this.add
     		}
     		//this = new JList(data);
     		
     		//positionne le focus sur le premier element
     		//setSelectedIndex(0);
     	} else {

     		dialogBase.traceError("StkJComboBox.initJComboBox",
                          "La comboBox du champ " + columnName + " est vide.");
     	}

     	try {
     		JComboBoxLenght = (new Long (dialogBase.getConnection().getColumnSize
     			(columnName))).intValue();
     	} catch(Exception exp) {
     		dialogBase.traceError("StkJComboBox.initJComboBox",exp.toString());
     	}
     }
     
     /**
      * Retourne le code du libelle selectionne
      * @return
      */
     /*public String getCode()
     {
     	String item = (String)getSelectedItem();
     	if (item!=null)
     		return getElementTableau((String)getSelectedItem());
     	else
     		return null;
     }*/

     /**
      * Retourne le libelle selectionne
      * @return
      */
     public String getLibelle()
     {
     	//return (String)getSelectedItem();
    	 return (String)getSelectedValue();
     }
     
     public String[] getLibelles()
     {
     	//return (String)getSelectedItem();
    	 return (String[])getSelectedValues();
     }

   	/**
   	 * 
   	 * @param in_valeur
   	 * @return
   	 * Retourne le code ou le libelle correspondant à la valeur passée
   	 * en parametre
   	 */
     /*private String getElementTableau(String in_valeur)
     {
     	// Recherche la valeur in_valeur
     	for (int i=0; i<listLenght;i++){
     		if (listComboBox[0][i]==null){
     			return null;
     		}
     		if (listComboBox[0][i].equals(in_valeur.trim())){
     			return listComboBox[1][i];
     		}
     		if (listComboBox[1][i].equals(in_valeur.trim())){
     			return listComboBox[0][i];
     		}
     	}
     	return null;
     }*/

     /**
      * Affecte la valeur au comboBox
      * @param in_control
      */
     /*public void refresh(Controler in_control)
     {
     	if (in_control.getColumnName(columnName)!=null)
     		setLibelle((String)in_control.getValueAt(columnName));
     }*/

     /**
      * Retourne la longueur du code de la combo
      * @return
      */
     /*public int getLenght()
     {
     	return JComboBoxLenght;
     }*/

     /**
      * Definition pour un combo alimenté dynamiquement
      * @param in_dialogBase
      * @param in_query
      * @param in_columnName
      */
     public void setQuery(DialogBase in_dialogBase, String in_query, String in_columnName) {
    	 dialogBase = in_dialogBase;
    	 columnName = in_columnName;
    	 
    	 dlm.removeAllElements();

    	 try {
     		control = dialogBase.getControler(in_query);
     	 } catch(Exception exp){
     		System.out.println(exp.toString());
     		dialogBase.traceError("StkJComboBox.initJComboBox",exp.toString());
     	 }

     	// Declaration tableau
     	listLenght = control.getRowCount();
     	// System.out.println("Taille de la liste = " + Integer.toString(listLenght));
     	listComboBox = new String[2][listLenght];

     	if (listLenght>0){
     		for (int i=0;i<listLenght;i++){
     			//Alimenter tableau
     			listComboBox[0][i]=((String)control.getValueAt(i,0)).trim();
     			listComboBox[1][i]=((String)control.getValueAt(i,1)).trim();
     			//System.out.println("Champ 0 = " + listComboBox[0][i] + " Champ 1 = " + listComboBox[1][i]);
         
     			dlm.addElement((String)control.getValueAt(i,1));
     			//this.addItem((String)control.getValueAt(i,1));
     		}
     		//positionne le focus sur le premier element
     		setSelectedIndex(0);
     	} else {
     		dialogBase.traceError("StkJComboBox.initJComboBox",
     				"La comboBox du champ " + columnName + " est vide.");
     	}
     	
     	try {
     		JComboBoxLenght = (new Long (dialogBase.getConnection().getColumnSize
     				(columnName))).intValue();
     	} catch(Exception exp) {
     		dialogBase.traceError("StkJComboBox.initJComboBox",exp.toString());
     	}
     }

}
