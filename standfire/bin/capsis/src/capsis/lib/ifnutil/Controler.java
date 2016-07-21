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

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.table.AbstractTableModel;


public class Controler extends AbstractTableModel implements Serializable
{
    private ArrayList data = new ArrayList(512);
    final static int CV_COLUMN_NOT_FOUND  = 9999;
    // En-tête table
    private int columnNumber;
    private String[] columnNames;
    private String[] translatedColumnNames;
    private int[] columnSize;
    private int[] columnScreenSize;
    private int[] columnAlignement;


  /****************************************************************************/
  /*Constructeur:                                                             */
  /* - colNum:  nombre maximum de colonne dans la requete                     */
  /* - result : resultat de la requete à la base de données                   */
  /* - key: langue                                                            */
  /* - trans: Traducteur de nom de colonne                                    */
  /****************************************************************************/
    public Controler (int colNum, ResultSet result, String key,ColumnSizeList size)
    {
       columnNumber = colNum;
       translatedColumnNames = new String[columnNumber];
       columnNames = new String[columnNumber];
       columnSize = new int[columnNumber];
       columnScreenSize = new int[columnNumber];
       columnAlignement = new int[columnNumber];


      try
      {
        fireTableStructureChanged();
        /*récupération des information de la couche metadata*/
        
        for (int i=1;i<=columnNumber;i++)
        {
          //Nom des colonnes
        	columnNames[i-1] = result.getMetaData().getColumnName(i).trim();
        	translatedColumnNames[i-1] = result.getMetaData().getColumnName(i).trim();
          
          //Taille des colonnes
          if (size!=null)
          {
            columnSize[i-1] = (int)size.getColumnSize(result.getMetaData().getColumnName(i).trim());
            columnScreenSize[i-1] = (int)size.getColumnScreenSize(result.getMetaData().getColumnName(i).trim());
          }
          else
          {
            if (result.getMetaData().getColumnType(i) == Types.TIMESTAMP)
            {
              columnSize[i-1] = 20;
              columnScreenSize[i-1] = 20;
            }
            else
            {
              columnSize[i-1] = result.getMetaData().getColumnDisplaySize(i);
              columnScreenSize[i-1] = result.getMetaData().getColumnDisplaySize(i);
            }
          }

          /*si la taille du champ à l'affichage est < au nom de la colonne on impose la */
          /* la taille de la colonne*/

          if (columnScreenSize[i-1] < translatedColumnNames[i-1].length())
            columnScreenSize[i-1] = translatedColumnNames[i-1].length();


          //Calcul de l'alignement
          if (result.getMetaData().getColumnType(i) == Types.NUMERIC)
            columnAlignement[i-1] = JLabel.RIGHT;
          else
            columnAlignement[i-1] = JLabel.LEFT;
        }

        /*Récupération des données lues*/
        while (result.next())
        {
          String []  Ligne = new String[columnNumber];
          for (int i=0;i<columnNumber;i++)
            Ligne[i] = result.getString(i+1);
          /*remplissage de la liste*/
          data.add(Ligne);
        }
        result.close();
      }
      /*traitement des exceptions*/
      catch(SQLException erreur1)
      {
        System.out.println("Erreur SQL" + erreur1);
      }
    }
  /****************************************************************************/
  /*Mise a jour de la Jtable                                                  */
  /****************************************************************************/
  public void updateTable()
  {
	  fireTableDataChanged();
  }

  /****************************************************************************/
  /*Renvoie le nombre de colonnes de la structure. Ce nombre correspond au    */
  /* nombre de colonne de la requete executee en base de donnee               */
  /****************************************************************************/
  public int getColumnCount()
  {
	  return columnNumber;
  }

  /**
   * Renvoie le nombre de lignes de la structure. Ce nombre correspond au
   * nombre de lignes de la requete executee en base de donnee i
   */
  public int getRowCount()
  {
	  if(data.isEmpty()) return 0;
	  return data.size();
  }

  /****************************************************************************/
  /*Recherche du numéro de colonne en fonction du nom de colonne passee en    */
  /* paramètre                                                                */
  /****************************************************************************/
    private int searchColumn(String columnName)
    {
      int valtoReturn =CV_COLUMN_NOT_FOUND;
      int index;
      for (index=0; (index<columnNumber) && (valtoReturn == CV_COLUMN_NOT_FOUND) ;index++)
      {
        if (getDBColumnName(index).equalsIgnoreCase(columnName))
        {
          valtoReturn = index;
        }
      }
      return valtoReturn;
    }

  /****************************************************************************/
  /*Renvoie la valeur lue en base de données                                  */
  /* row: Numero de l'enregistrement en en base de données                    */
  /*     (correspond au rownum)                                               */
  /* col: Numero de la colonne en base de données                             */
  /****************************************************************************/
  public Object getValueAt(int row, int col)
  {
    Object rt = null;
    String []  ligne = new String[columnNumber];
    ligne = (String[])data.get(row);
    if (col < columnNumber) rt = ligne[col];
    return rt;
  }

  /****************************************************************************/
  /*Renvoie la ligne des valeurs lues en base de données                                  */
  /* row: Numero de l'enregistrement en en base de données                    */
  /*     (correspond au rownum)                                               */
  /****************************************************************************/
  public Object getLineAt(int row)
  {
    String []  ligne = new String[columnNumber];
    ligne = (String[])data.get(row);
    return ligne;
  }

  /****************************************************************************/
  /*Renvoie le code                                                           */
  /* row: Numero de l'enregistrement en en base de données                    */
  /*     (correspond au rownum)                                               */
  /* col: Numero de la colonne en base de données                             */
  /****************************************************************************/
  public Object getCodeAt(int row, int col)
  {
    Object rt = null;
    String []  ligne = new String[columnNumber];
    ligne = (String[])data.get(row);
    if (col < columnNumber)
    {
     rt = ligne[col];
    }
    return rt;
  }

  /****************************************************************************/
  /*Renvoie la valeur lue en base de données                                  */
  /* row: Numero de l'enregistrement en en base de données                    */
  /*     (correspond au rownum)                                               */
  /* columnName: Nom de la colonne en base de données                         */
  /****************************************************************************/
  public Object getValueAt(int row, String columnName)
  {
    Object rt = null;
    int index = searchColumn(columnName);
    if (index !=CV_COLUMN_NOT_FOUND)
    {
        rt = getValueAt(row,index);
    }
    return rt;
  }
  /****************************************************************************/
  /*Renvoie le code                                                           */
  /* row: Numero de l'enregistrement en en base de données                    */
  /*     (correspond au rownum)                                               */
  /* columnName: Nom de la colonne en base de données                         */
  /****************************************************************************/
  public Object getCodeAt(int row, String columnName)
  {
    Object rt = null;
    int index = searchColumn(columnName);
    if (index !=CV_COLUMN_NOT_FOUND)
    {
        rt = getCodeAt(row,index);
    }
    return rt;
  }

  /****************************************************************************/
  /*Renvoie la valeur lue en base de données                                  */
  /* columnName: Nom de la colonne en base de données                         */
  /* l'élément lu correspond au premier enregistrement dans la base de donnés */
  /****************************************************************************/
  public Object getValueAt(String columnName)
  {
    return getValueAt(0,columnName);
  }
  /****************************************************************************/
  /*Renvoie le code                                                           */
  /* columnName: Nom de la colonne en base de données                         */
  /* l'élément lu correspond au premier enregistrement dans la base de donnés */
  /****************************************************************************/
  public Object getCodeAt(String columnName)
  {
    return getCodeAt(0,columnName);
  }

  /****************************************************************************/
  /*Fixe la valeur passee en parametre dans le buffer                         */
  /* row: Numero de l'enregistrement en en base de données                    */
  /*     (correspond au rownum)                                               */
  /* col: Numero de la colonne en base de données                             */
  /* value: nouvelle valeur de la cellule reperee par row,col                 */
  /****************************************************************************/
  public void setValueAt(Object value,int row, int col)
  {
    Object rt = null;
    String []  ligne = new String[columnNumber];
    ligne = (String[])data.get(row);
    ligne[col] = (String)value;
    data.set(row,ligne);
    fireTableDataChanged();
  }


  /****************************************************************************/
  /*Fixe la valeur passee en parametre dans le buffer                         */
  /* row: Numero de l'enregistrement en en base de données                    */
  /*     (correspond au rownum)                                               */
  /* columnName: Nom de la colonne en base de données                         */
  /* value: nouvelle valeur de la cellule                                     */
  /****************************************************************************/
  public void setValueAt(Object value, int row, String columnName)
  {
    Object rt = null;
    int index = searchColumn(columnName);
    if (index !=CV_COLUMN_NOT_FOUND)
    {
        setValueAt(value, row,index);
    }
  }

  /****************************************************************************/
  /*Renvoie la valeur lue en base de données                                  */
  /* columnName: Nom de la colonne en base de données                         */
  /* l'élément lu correspond au premier enregistrement dans la base de donnés */
  /****************************************************************************/
  public void setValueAt(Object value, String columnName)
  {
    setValueAt(value,0,columnName);
  }

  /****************************************************************************/
  /*Renvoie le nom de la colonne en fonction du numéro de cette derniere      */
  /* le nom renvoye est le nom base de donnee                                 */
  /****************************************************************************/
    public String getDBColumnName(int col)
    {
      if (col < columnNumber)
        return columnNames[col];
      else
        return null;
    }
  /****************************************************************************/
  /*Renvoie le nom de la colonne en fonction du numéro de cette derniere      */
  /* le nom renvoye est le nom interprete de la colonne dans la langue        */
  /* selectionnee au moment de la connexion                                   */
  /****************************************************************************/
    public String getColumnName(int col)
    {
      if (col < columnNumber)
        return translatedColumnNames[col];
      else
        return null;
    }

  /****************************************************************************/
  /*Renvoie le nom de la colonne en fonction du nom base de donnee de la      */
  /* colonne                                                                  */
  /* le nom renvoye est le nom interprete de la colonne dans la langue        */
  /* selectionnee au moment de la connexion                                   */
  /****************************************************************************/
    public String getColumnName(String columnName)
    {
      int index = searchColumn(columnName);
      if (index !=CV_COLUMN_NOT_FOUND)
      {
        return translatedColumnNames[index];
      }
      else
        return null;
    }

  /****************************************************************************/
  /*Renvoie la taille d'une colonne en fonction de son numero                 */
  /* Cette taille est recuperee d'une table STKWSIZE qui definit la taille    */
  /* d'une colonne en fonction de son nom                                     */
  /****************************************************************************/
    public int getColumnSize(int col)
    {
      if (col < columnNumber)
        return columnSize[col];
      else
        return 0;
    }

  /****************************************************************************/
  /*Renvoie la taille d'une colonne à l'affichage en fonction de son numero   */
  /* Cette taille est recuperee d'une table STKWSIZ qui definit la taille     */
  /* d'une colonne en fonction de son nom                                     */
  /****************************************************************************/
    public int getColumnScreenSize(int col)
    {
      if (col < columnNumber)
        return columnScreenSize[col];
      else
        return 0;
    }
  /****************************************************************************/
  /*Renvoie la taille d'une colonne en fonction de son nom                    */
  /* Cette taille est recuperee d'une table STKWSIZE qui definit la taille    */
  /* d'une colonne en fonction de son nom                                     */
  /****************************************************************************/
    public int getDBColumnSize(String columnName)
    {
      int index = searchColumn(columnName);
      if (index !=CV_COLUMN_NOT_FOUND)
      {
        return getColumnSize(index);
      }
      else return 0;
    }

  /****************************************************************************/
  /*Renvoie la taille d'une colonne à l'affichage en fonction de son nom      */
  /* Cette taille est recuperee d'une table STKWSIZ qui definit la taille     */
  /* d'une colonne en fonction de son nom                                     */
  /****************************************************************************/
    public int getDBColumnScreenSize(String columnName)
    {
      int index = searchColumn(columnName);
      if (index !=CV_COLUMN_NOT_FOUND)
      {
        return getColumnScreenSize(index);
      }
      else return 0;
    }
  /****************************************************************************/
  /*Renvoie le type d'alignement d'une colonne en fonction de son numero      */
  /****************************************************************************/
    public int getColumnAlignement(int col)
    {
      if (col < columnNumber)
        return columnAlignement[col];
      else
        return 0;
    }

  /****************************************************************************/
  /* Vidage des donnees                                                       */
  /****************************************************************************/
    public void empty()
    {
      data.clear();
    }
    
    /*
    public Class getColumnClass(int c) {
        // un exemple :
    	return getValueAt(0, c).getClass();
   }
    */
    
    public boolean isCellEditable(int row, int col) {
        //return true;
    	//return (col == 0 && columnNames[0].equals("choix"));
    	return (col == 0);
    }
    
}
