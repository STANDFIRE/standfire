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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class ColumnSizeList {

  /****************************************************************************/
  /****************************************************************************/
  /*Definition de classes utilisee uniquement a l'interieur de cette classe   */
  /****************************************************************************/
  /****************************************************************************/

  /* CLASSE IMBRIQUEE*/
  /****************************************************************************/
  /* Classe contenant le nom de la colonne (base de donnees) et la taille de  */
  /* cette derniere                                                           */
  /****************************************************************************/
  public class ColumnSize {

    private String columnName;
    private long columnSize;
    private long decimalColumnSize;
    private long columnScreenSize;

  /****************************************************************************/
  /* Constructeur:                                                            */
  /*  - name: nom de la colonne (base de donnees)                             */
  /*  - size: taille de la colonne en ASCII                                   */
  /****************************************************************************/
    public ColumnSize(String name, long size, long decimalSize, long screenSize)
    {
      columnName = name;
      columnSize = size;
      decimalColumnSize = decimalSize;
      columnScreenSize = screenSize;
    }
  /****************************************************************************/
  /* Renvoie le com de la colonne                                             */
  /****************************************************************************/
    String getColumnName()
    {
      return columnName;
    }
  /****************************************************************************/
  /* Renvoie la taille de la colonne                                          */
  /****************************************************************************/
    long getColumnSize()
    {
      return columnSize;
    }
  /****************************************************************************/
  /* Renvoie la taille de la partie decimale de la colonne                    */
  /****************************************************************************/
    long getDecimalColumnSize()
    {
      return decimalColumnSize;
    }
  /****************************************************************************/
  /* Renvoie la taille de la colonne pour affichage                           */
  /****************************************************************************/
    long getColumnScreenSize()
    {
      return columnScreenSize;
    }
  }

  private ArrayList ColumnSizeArray;
  private boolean exist = false;
 /* CLASSE PRINCIPALE */
  /****************************************************************************/
  /* Constructeur de la classe principale                                     */
  /* Cette classe contient un tableau d'instance de ColumnSize                */
  /* Cette classe contient donc un tableau de couple (nom de colonne, taille) */
  /****************************************************************************/
  public ColumnSizeList()
  {
    ColumnSizeArray = new ArrayList(100);
  }
  /****************************************************************************/
  /* Remplissage du tableau en fonction du resultat de la requete passe en    */
  /* parametre                                                                */
  /****************************************************************************/
  public void FillArraySize(ResultSet result)
  {
    try
    {
      /*Récupération des données lues*/
      while (result.next())
      {
          ColumnSizeArray.add(new ColumnSize(result.getString(1),result.getLong(2),
          result.getLong(3), result.getLong(4)));
      }
      result.close();
      Integer in = new Integer(ColumnSizeArray.size());
      exist = true;
      System.out.println("ColumnSize Taille :" + in.toString());
    }
    /*traitement des exceptions*/
    catch(SQLException erreur1)
    {
      System.out.println("FillTranslator :Erreur SQL" + erreur1);
    }
  }

  /****************************************************************************/
  /* Renvoie la taille de la colonne passe en parametre                       */
  /* (nom de colonne de la base de donnees                                    */
  /* parametre                                                                */
  /****************************************************************************/
  public long getColumnSize(String columnName)
  {
    long valToReturn=0;
    ColumnSize acolumn;
    for (int i=0;i<ColumnSizeArray.size();i++)
    {
      acolumn = (ColumnSize)ColumnSizeArray.get(i);
      if (acolumn !=null)
      {
        if (acolumn.getColumnName().equals(columnName))
          valToReturn = acolumn.getColumnSize();
      }
    }
    return valToReturn;
  }
 /****************************************************************************/
  /* Renvoie la taille de la colonne passe en parametre                       */
  /* (nom de colonne de la base de donnees                                    */
  /* parametre                                                                */
  /****************************************************************************/
  public long getDecimalColumnSize(String columnName)
  {
    long valToReturn=0;
    ColumnSize acolumn;
    for (int i=0;i<ColumnSizeArray.size();i++)
    {
      acolumn = (ColumnSize)ColumnSizeArray.get(i);
      if (acolumn !=null)
      {
        if (acolumn.getColumnName().equals(columnName))
          valToReturn = acolumn.getDecimalColumnSize();
      }
    }
    return valToReturn;
  }
  /****************************************************************************/
  /* Renvoie la taille d'affichage de la colonne passe en parametre           */
  /* (nom de colonne de la base de donnees                                    */
  /* parametre                                                                */
  /****************************************************************************/
  public long getColumnScreenSize(String columnName)
  {
    long valToReturn=0;
    ColumnSize acolumn;
    for (int i=0;i<ColumnSizeArray.size();i++)
    {
      acolumn = (ColumnSize)ColumnSizeArray.get(i);
      if (acolumn !=null)
      {
        if (acolumn.getColumnName().equals(columnName))
          valToReturn = acolumn.getColumnScreenSize();
      }
    }
    return valToReturn;
  }
  /****************************************************************************/
  /*    Verifie le chargement de la taille des colonnes                       */
  /****************************************************************************/
  public boolean exist()
  {
    return exist;
  }

}