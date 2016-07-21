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

import javax.swing.JComboBox;

/**	IfnCaModel is the main class for module IfnCa
*
*	@author JL Cousin - june 2006
*/

public class IfnJComboBox extends JComboBox
{

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

    /**
    * Default Constructor
    */
    public IfnJComboBox () {
        super();
    }

    /**
    * Constructor
    */
    /*public IfnJComboBox(DialogBase in_dialogBase, String in_columnName) {
        super();
        dialogBase = in_dialogBase;
        columnName = in_columnName;
        initJComboBox();
    }*/

    public IfnJComboBox(DialogBase in_dialogBase, String sql) {
        super();
        dialogBase = in_dialogBase;
        query = sql;
        //columnName = in_columnName;
        initJComboBox();
    }

/* --------------------------------------------------------------------- */
/*                Initialisation de la combo box                         */
/* --------------------------------------------------------------------- */
    private void initJComboBox()
    {
    	//Ajoute au listener d'action
    	this.addActionListener(dialogBase);
    	//Determine la commande pour action
    	this.setActionCommand(columnName);
    	//Affecte le preferredSize
    	this.setPreferredSize(dimension);
    	//ajout de la gestion du focus
    	this.addFocusListener(dialogBase);

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

    	if (listLenght>0){
    		for (int i=0;i<listLenght;i++){
    			//Alimenter tableau
    			listComboBox[0][i]=control.getValueAt(i,0).toString().trim();
    			listComboBox[1][i]=control.getValueAt(i,1).toString().trim();
    			//System.out.print(i);
    			//System.out.print("Champ 0 = " + listComboBox[0][i] + " Champ 1 = " + listComboBox[1][i]);
    			
    			try {
    				if(control.getValueAt(i,1) != null)
    					addItem(control.getValueAt(i,1).toString());
    			} catch(Exception e){
    				//System.out.println("!!!"+e.toString());
    			}
    			//System.out.println("*");
    			//this.add
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

    /**
     * 
     * @param in_libelle
     * Positionnement sur le libelle
     */
    public void setLibelle(String in_libelle)
    {
    	this.setSelectedItem(in_libelle);
    }
    
    /**
     * 
     * @param in_code
     * Positionnement sur le libelle à partir du code
     */
    public void defautLibelle(String in_code)
    {
    	String valeur = getElementTableau(in_code);
    	if (valeur == null){
    		dialogBase.traceError("StkJComboBox.defautLibelle",
                         "Le code " + in_code + " ne correspond à aucune " +
                         "valeur connue de " + columnName + ".");
    	} else {
    		this.setSelectedItem(valeur);
    	}
    }

    /**
     * 
     * @param in_enabled
     * Gestion de la couleur lors du passage du Focus
     */
    void setFocusColor(boolean in_enabled)
    {
    	if (in_enabled)
    		setBackground(Color.orange);
    	else
    		setBackground(Color.lightGray);
    }
    
    /**
     *
     */
    void restoreColor()
    {
    	setBackground(Color.lightGray);
    }
    
    /**
     * 
     */
    public void setEnabled(boolean enabled)
    {
    	if (!enabled){
    		super.setEditable(true);
    		super.setEnabled(false);
    		setBackground(Color.lightGray);
    	}
    	if (enabled){
    		super.setEnabled(true);
    	}
    }


    /**
     * Retourne le code du libelle selectionne
     * @return
     */
    public String getCode()
    {
    	String item = (String)getSelectedItem();
    	if (item!=null)
    		return getElementTableau((String)getSelectedItem());
    	else
    		return null;
    }

    /**
     * Retourne le libelle selectionne
     * @return
     */
    public String getLibelle()
    {
    	return (String)getSelectedItem();
    }

  	/**
  	 * 
  	 * @param in_valeur
  	 * @return
  	 * Retourne le code ou le libelle correspondant à la valeur passée
  	 * en parametre
  	 */
    private String getElementTableau(String in_valeur)
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
    }

    /**
     * Affecte la valeur au comboBox
     * @param in_control
     */
    public void refresh(Controler in_control)
    {
    	if (in_control.getColumnName(columnName)!=null)
    		setLibelle((String)in_control.getValueAt(columnName));
    }

    /**
     * Retourne la longueur du code de la combo
     * @return
     */
    public int getLenght()
    {
    	return JComboBoxLenght;
    }

    /**
     * Definition pour un combo alimenté dynamiquement
     * @param in_dialogBase
     * @param in_query
     * @param in_columnName
     */
    public void setQuery(DialogBase in_dialogBase, String in_query,
    		String in_columnName)
    {
    	dialogBase = in_dialogBase;
    	columnName = in_columnName;
    	//Ajoute au listener d'action
    	this.addActionListener(dialogBase);
    	//Determine la commande pour action
    	this.setActionCommand(columnName);
    	//Affecte le preferredSize
    	this.setPreferredSize(dimension);
    	//Gestion du focus
    	this.addFocusListener(dialogBase);

    	try {
    		control = dialogBase.getControler(in_query);
    	}catch(Exception exp){
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
    			this.addItem(control.getValueAt(i,1).toString().trim());
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

