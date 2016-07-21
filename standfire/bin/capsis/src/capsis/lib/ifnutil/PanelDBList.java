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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

//import test.EditeurRadio;
//import test.MonAfficheurCelluleRadio;


public class PanelDBList extends PanelDB implements KeyListener
{

	protected JTable table = null;
	private JScrollPane scrollpanelCenter = null;
	private int numberOfColumnToPrint = 0;
	private int[] ColumnToPrint = null;
	protected String queryToProcess = " ";
	private JLabel recordNumberTitleLabel;
	//private JLabel recordNumberValueLabel;
	protected boolean firstPass = true;
	private ButtonGroup buttonGroup;


	/**
	 * Constructeur :
	 * dlg: Boite de dialog qui contient le panel
	 * aTitle: titre du panel
	 */
	public PanelDBList(DialogBase dlg, String Titre) throws IfnException {
		super(dlg,Titre);
		scrollpanelCenter = new JScrollPane();
		scrollpanelCenter.setBorder(BorderFactory.createLineBorder(Color.black.darker(),1));
		scrollpanelCenter.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollpanelCenter.setFont(dialogBase.listFont);
		//recordNumberValueLabel = new JLabel();
		Inittable();
		this.add(scrollpanelCenter,BorderLayout.CENTER);
		//recordNumberValueLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		//this.add(recordNumberValueLabel,BorderLayout.SOUTH);
		//this.setBorder(BorderFactory.createEtchedBorder());
		//table.setDefaultRenderer(Object.class, new RadioCellRenderer());
	}

	/**
	 * Constructeur par defaut:
	 * 
	 */
	public PanelDBList()throws IfnException {
		super();
		scrollpanelCenter = new JScrollPane();
		scrollpanelCenter.setBorder(BorderFactory.createLineBorder(Color.black.darker(),1));
		scrollpanelCenter.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollpanelCenter.setFont(dialogBase.listFont);
		Inittable();
		this.add(scrollpanelCenter,BorderLayout.CENTER);
	}

	/**
	 * Initialisation de la JTable
	 */
	private void Inittable() {
		table = new JTable();
		//table.setBackground(Color.orange);
		table.setForeground(Color.black);
		table.setBorder(BorderFactory.createLineBorder(Color.black));
		scrollpanelCenter.getViewport().add(table, null);
		//Permet de ne pas déplacer les colonnes
		table.getTableHeader().setReorderingAllowed(false);
		//Permet de selectionner une cellule
		table.setCellSelectionEnabled(false);
		table.setRowSelectionAllowed(true);
		table.setColumnSelectionAllowed(false);
		//Ajoute les keys
		table.addKeyListener(this);
	}
  
	/**
	 * 
	 */
	public void setTableSize(int width, int height){
		table.setPreferredScrollableViewportSize(new Dimension(width, height));
	}

  /**
   * Mise à jour de la liste en fonction de la requete passee
   */
	public void RefreshPanelWithQuery(String in_queryToProcess)throws IfnException {
		
		queryToProcess = in_queryToProcess;

		//Lors du premier passage
		if (firstPass) {
			String name;
			int columnSize;
			table.setFont(dialogBase.listFont);

			control = dialogBase.getControler(queryToProcess.toString());
			if (control.getColumnCount() > 0) {
				table.removeAll();

				TableColumnModel colModel = table.getColumnModel();
				if (colModel != null){
					int nbcol = colModel.getColumnCount();
					for (int i=0;i< nbcol;i++) {
						table.removeColumn(colModel.getColumn(0));
					}
				}
			}
			table.setAutoCreateColumnsFromModel(false);
			table.setModel(control);

			//Affectation du nombre de record
			Integer numberRecordInteger = new Integer(control.getRowCount());
			//recordNumberValueLabel.setText("Nb. =" + numberRecordInteger.toString()+" ");
			//System.out.println("Nb. =" + numberRecordInteger.toString()+" ");
			//gestion des alignements et tailles des colonnes
			for (int i=0;i<control.getColumnCount();i++) {
				//determination de la taille de la colone en pixels
				name = control.getColumnName(i).trim();
				//System.out.println("Name = " + name);
				columnSize = control.getColumnScreenSize(i);
				//Permet d'agrandir les colonnes pour qu'à l'impression
				//le resultat soit correct
				columnSize++;
				FontMetrics metrics = getFontMetrics(table.getFont());
				char[] wstring = new char [columnSize];
				//création d'un mot de largeur maximum (cas des polices proportionnelles
				for (int j=0;j<columnSize;j++) {
					wstring[j] ='W';
				}
				String wString = new String(wstring);
				columnSize = metrics.stringWidth( wString );
				DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
				renderer.setHorizontalAlignment(control.getColumnAlignement(i));

				TableColumn column = new TableColumn(i,columnSize, renderer, null);
				column.setPreferredWidth(columnSize);
				if(name.toUpperCase().equals("CHOIX")) {
					
					column.setCellEditor(new RadioCellEditor(buttonGroup));
					column.setCellRenderer(new RadioCellRenderer(buttonGroup));
					//table.setDefaultRenderer(Integer.class, new RadioCellRenderer(buttonGroup));
					
					//column.setCellRenderer(new RadioCellRenderer());
					//column.setCellEditor(new RadioCellEditor());
					column.setPreferredWidth(1);
				} else {
					column.setPreferredWidth(columnSize);
				}
				table.addColumn(column);
			}
			TableColumnModel newColModel = table.getColumnModel();

      Dimension screenSize = this.getSize();
      int panelSize = screenSize.width;
      if (panelSize == 0) /*cas de liste pour lequel setvisible non fait*/
      {
        //approximation: recuperation de la taille ecran au lieu de la taille liste
        screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        panelSize = screenSize.width;
      }
      int columnsSize = newColModel.getTotalColumnWidth();
      if (columnsSize < panelSize)
      {
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
      }
      else
      {
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
      }
      control.updateTable();
      table.sizeColumnsToFit(1);
      firstPass = false;
    }//FIN if (firstPass)
    //Lors des passages suivants
    else
    {
      //Execution de la requete
      control = dialogBase.getControler(queryToProcess.toString());
      //Affectation du nombre de record
      Integer numberRecordInteger = new Integer(control.getRowCount());
      //recordNumberValueLabel.setText("Nb. =" + numberRecordInteger.toString()+" ");
      table.setAutoCreateColumnsFromModel(false);
      table.setModel(control);
      control.updateTable();
    }
		
	/*
	boolean isRadio = false;
	String name = "";
	for (int i=0;i<control.getColumnCount();i++) {
		name = control.getColumnName(i).trim();
		if(name.toUpperCase().equals("CHOIX")) {
			isRadio = true;
			//System.out.println("Radio");
		}
	}
	if(isRadio){
		ButtonGroup buttonGroup = new ButtonGroup();
	    for (int i=0;i<table.getRowCount();i++) {
	    	//System.out.println("Groupe : " + i);
	    	buttonGroup.add((JRadioButton)table.getCellEditor(i,0));
	    }
	}
	*/
	
	//
	//System.out.println("Nb de colonne = " + table.getColumnCount());
		//System.out.println("Nb de ligne = " + table.getRowCount());
		/*for (int j=1;j<=control.getColumnCount();j++) {
			//determination de la taille de la colone en pixels
			//name = control.getColumnName(j).trim();
			//if(name.toUpperCase().equals("CHOIX")) {
				for(int i=1; i<=control.getRowCount(); i++){
					System.out.println("Ajout du bouton = " + i + "," + j);
					try {
						buttonGroup.add((JRadioButton)table.getCellEditor(i,j));
						//buttonGroup.add((JRadioButton)table.getC);
					} catch(Exception e){
						System.out.println("ERR = " + e.toString());
					}
				}
				
			//}
		}
		
		
		
		  //if(buttonGroup != null){
					//	buttonGroup.add((JRadioButton)column.);
					//buttonGroup.add((JRadioButton)table.getCellEditor());
					//}
		 */
		
		

    
    //liste vide appelee rafraichie par F5 (non appelee dans le constructeur)
    if (control.getRowCount()==0 && getSize().width>0)
    {
      //si nous ne sommes pas en phase de d'InitAfterActivation
      /*if (!dialogBase.activationState())
          JOptionPane.showMessageDialog(this,getString(CV_MESSAGE,CV_LIST_EMPTY),
                                     "INFORMATION",JOptionPane.INFORMATION_MESSAGE);*/
    }
  }
  /****************************************************************************/
  /* Mise à jour de la liste en fonction de la précédente requete passee       */
  /****************************************************************************/
  public void RefreshPanelWithQuery()throws IfnException
  {
    if (!queryToProcess.toString().equals(" "))
    {
      int row = table.getSelectedRow();
      RefreshPanelWithQuery(queryToProcess.toString());
      if(row>0 && table.getRowCount()>row )
        table.setRowSelectionInterval(row,row);
      table.requestFocus();
    }
    else table.requestFocus();
  }

  /****************************************************************************/
  /* Mise à jour de la liste en fonction de la précédente requete passee       */
  /****************************************************************************/
  public void emptyList()throws IfnException
  {
    control.empty();
    Integer numberRecordInteger = new Integer(control.getRowCount());
    //recordNumberValueLabel.setText("Nb. =" + numberRecordInteger.toString()+" ");
    table.setAutoCreateColumnsFromModel(false);
    table.setModel(control);
    control.updateTable();
  }
  /****************************************************************************/
  /* Mise à jour d'une colonne de la liste en fonction de la valeur passee en */
  /* parametre                                                                */
  /****************************************************************************/
  public void setValueAt(int column, String value)throws IfnException
  {
    table.setValueAt((Object) value,table.getSelectedRow(), column);
  }

  /****************************************************************************/
  /* Mise à jour d'une colonne de la liste en fonction de la valeur passee en */
  /* parametre                                                                */
  /****************************************************************************/
  public void setValueAt(int row, int column, String value)throws IfnException
  {
    table.setValueAt((Object) value,row, column);
  }

  /****************************************************************************/
  /* Recupere la valeur de la colonne                                         */
  /****************************************************************************/
  public String getValueAt(int row, int column)throws IfnException
  {
    return (String) table.getValueAt(row, column);
  }

  /****************************************************************************/
  /* Recupere le code de la colonne                                           */
  /****************************************************************************/
  public String getCodeAt(int row, int column)throws IfnException
  {
    return (String) control.getCodeAt(row, column);
  }


  /****************************************************************************/
  /* Lecture de la colonne de la ligne selectionnee                           */
  /****************************************************************************/
  public String getSelectedItem (String columnName)
  {
    int index;
    String value=null;
    if(control!=null)
    {
      for (index=0;index<control.getColumnCount()&&value==null;index++)
      {
        //determination de la taille de la colone en pixels
        if (control.getDBColumnName(index).equalsIgnoreCase(columnName))
          value = getSelectedItem(index);
      }
      if (value==null)
      {
        dialogBase.traceError( "getSelectedCode", "Nom de colonne invalide:" + columnName);
      }
    }
    return value;
  }

  /****************************************************************************/
  /* Lecture de la colonne de la ligne selectionnee                           */
  /****************************************************************************/
  public String getSelectedItem (int column)
  {
    try
    {
      String value = (String) control.getValueAt(table.getSelectedRow(), column);
      return value;
    }
    catch(ArrayIndexOutOfBoundsException e)
    {
      return null;
    }
    catch(IndexOutOfBoundsException ie)
    {
      return null;
    }
  }
  /****************************************************************************/
  /* Lecture du code correspondant à la colonne de la ligne selectionnee      */
  /****************************************************************************/
  public String getSelectedCode (int column)
  {
    try
    {
      String value = getCodeAt(table.getSelectedRow(), column);
      return value;
    }
    catch(ArrayIndexOutOfBoundsException e)
    {
      return null;
    }
    catch(IndexOutOfBoundsException ie)
    {
      return null;
    }
    catch(IfnException stk)
    {
      return null;
    }
  }
  /****************************************************************************/
  /* Lecture du code correspondant à la colonne de la ligne selectionnee      */
  /****************************************************************************/
  public String getSelectedCode (String columnName)
  {
    int index;
    String value=null;
    if(control!=null)
    {
      for (index=0;index<control.getColumnCount()&&value==null;index++)
      {
        //determination de la taille de la colone en pixels
        if (control.getDBColumnName(index).equalsIgnoreCase(columnName))
          value = getSelectedCode(index);
      }
      if (value==null)
      {
        dialogBase.traceError( "getSelectedCode", "Nom de colonne invalide:" + columnName);
      }
    }
    return value;
  }


/*----------------------------------------------------------------------------*/
/* Permet de gérer les touches de fonctions (même vide, elle est obligatoire) */
/*----------------------------------------------------------------------------*/
  public void keyPressed(KeyEvent e)
  {
    if (e.isControlDown() && e.getKeyCode()== KeyEvent.VK_C)
      {
        //Récupération de la cellule
        String cell = table.getValueAt(table.getSelectedRow(),
                      table.getSelectedColumn()).toString();
        //Instance du presse papier
        Clipboard clipboard = getToolkit().getSystemClipboard() ;
        //Instance pour transfert de données
        StringSelection stringSelection = new StringSelection(cell);
        //Copie dans le presse papier
        clipboard.setContents(stringSelection,null);
      }
      if (e.getKeyCode()== KeyEvent.VK_F2)
      {
        dialogBase.keyPressed(e);
      }
      if (e.getKeyCode()== KeyEvent.VK_ESCAPE)
      {
        dialogBase.keyPressed(e);
      }

  }

/*----------------------------------------------------------------------------*/
/* Permet de gérer les touches de fonctions (même vide, elle est obligatoire) */
/*----------------------------------------------------------------------------*/
  public void keyTyped(KeyEvent e)
  {

  }
/*----------------------------------------------------------------------------*/
/* Permet de gérer les touches de fonctions (même vide, elle est obligatoire) */
/*----------------------------------------------------------------------------*/
  public void keyReleased(KeyEvent e)
  {
  }

  /**
   * @param in_columnIndex
   * Permet de cacher une colonne sans perdre les donnees qu'elle contient
   */
  public void hideColumn(int in_columnIndex) {
	  TableColumnModel tcm = table.getColumnModel();
	  TableColumn tableColumn = tcm.getColumn(in_columnIndex);
	  tcm.removeColumn(tableColumn);

	  Dimension screenSize = this.getSize();
	  int panelSize = screenSize.width;
	  if (panelSize == 0) {
		  //cas de liste pour lequel setvisible non fait
		  //approximation: recuperation de la taille ecran au lieu de la taille liste
		  screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		  panelSize = screenSize.width;
	  }
	  int columnsSize = tcm.getTotalColumnWidth();
	  if (columnsSize < panelSize) {
		  table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
	  } else {
		  table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	  }
	  //repositionne le flag pour reconstruire l'ensemble de la table.
	  //ceci evite que l'appel successif de RefreshPanel et Hide provoque
	  //une exception.
	  firstPass = true;
  }


  /**
   * Permet de cacher une colonne sans perdre les donnees qu'elle contient
   */
  public void hideColumn (String columnName) {
	  int index;
	  boolean found=false;
	  if(control!=null) {
		  for (index=0;index<control.getColumnCount()&&!found;index++) {
			  //determination de la taille de la colone en pixels
			  if (control.getDBColumnName(index).equalsIgnoreCase(columnName)) {
				  hideColumn(index);
				  found = true;
			  }
		  }
		  if(found==false) {
			  dialogBase.traceError( "hideColumn", "Nom de colonne invalide:" + columnName);
		  }
	  }
  }

  /****************************************************************************/
  /* Renvoie la JTable                                                        */
  /****************************************************************************/
  public JTable getTable()
  {
    return table;
  }
  
  public void setGroup(ButtonGroup b){
	  buttonGroup = b;
  }

}