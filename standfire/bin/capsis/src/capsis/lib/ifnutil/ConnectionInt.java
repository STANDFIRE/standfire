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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;
import java.util.Vector;

import jeeb.lib.util.MessageDialog;
import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;

public class ConnectionInt
{
  private Connection connection ;
  private String machineName;
  private String tracePath;
  private String connectionName;
  private String languageKey;
  //private ColumnTranslatorList columnTranslator;
  //private ValueTranslatorList valueTranslator;
  private ColumnSizeList columnSize;
  //constante pour trace
  static final String CV_SVR_FILE_BAK_EXT       = ".bak";
  static final String CV_SVR_FILE_ACT_EXT       = ".tra";
  static final String CV_SVR_FILE_TRACE_ERROR   = "error";
  static final String CV_SVR_FILE_TRACE_INFO    = "info";
  static final int    CV_SVR_FILE_LENGTH_MAX    = 5000000;
  private FileWriter fwInfo;
  private FileWriter fwError;
  private int certificat;
  Random generator;


  /**
   * Constructeur :
   * - machine_name: nom de la machine
   */
  	public ConnectionInt(String machine_name, String trace_path) {
  		machineName = machine_name;
  		tracePath = trace_path;
  		/*    
  		if (columnTranslator == null)
      		columnTranslator = new ColumnTranslatorList();
    	if (valueTranslator == null)
      		valueTranslator = new ValueTranslatorList();
      	*/
  		if (columnSize == null)
  			columnSize = new ColumnSizeList();
  		generator = new Random(10);
  	}

  	public ConnectionInt() {
  		//System.out.println("trace");
  		tracePath = new String(".");
  		/*    
  		 * if (columnTranslator == null)
      			columnTranslator = new ColumnTranslatorList();
    	if (valueTranslator == null)
      		valueTranslator = new ValueTranslatorList();*/
  		if (columnSize == null)
  			columnSize = new ColumnSizeList();
  		generator = new Random(10);
  	}

  /****************************************************************************/
  /*Fixe le nom de la connexion a la base de donnees:                         */
  /* - connection_name : chaine de connexion                                  */
  /* - language: langue utilisee                                              */
  /* -> casse la connexion existante                                          */
  /****************************************************************************/
  public int setConnectionName (String connection_name, String language)
    {
    connectionName = new String (connection_name);
    languageKey = new String(language);

    //gestion des fichiers de traces lors de l'appel de la premiere connexion
    filesControl();
    //creation de la connexion
    getConnection ();
    try
    {
      //System.out.println("Hote client = " + getClientHost());
      //System.out.println("Machine distante = " + this.getRef().remoteToString());
      if (connection != null)
      {
        //System.out.println("Demande de connexion sur " + machineName);
        connection.rollback();
        connection.close();
        connection = null;
        //System.out.println("Rupture de l'ancienne connexion");
      }
      else
      {
        //System.out.println("Premiere demande de connexion sur " + machineName);
      }
    }
    catch(SQLException ex)
    {
      System.err.println(ex.toString());
    }
    catch(Exception ex)
    {
      System.err.println(ex.toString());
    }
    certificat = generator.nextInt();
    return certificat;
  }

  /****************************************************************************/
  /*Renvoie la connexion mise en place                                        */
  /****************************************************************************/
   public String getDescription()
   {
      String serverName = "";
      try
      {
        serverName = java.net.InetAddress.getLocalHost().getHostName();
        serverName.toUpperCase();
      }
      catch(Exception e)
      {
        System.err.println(e.toString());
      }
      return "Connexion Oracle de " + machineName + " vers " + serverName;
   }

   /**
    * Etablit et renvoie la connexion a la base de données
    */
   private Connection getConnection () {
	   if (connection == null) {
		   try {
			   //Class.forName("com.microsoft.jdbc.sqlserver.SQLServerDriver");
			   //connection =DriverManager.getConnection ("jdbc:microsoft:sqlserver://192.9.200.69:1433;"
	           //	+ "user=VENTE;password=dOph1n;DatabaseName=Cesame");
			   Class.forName("org.postgresql.Driver");

			   // connection = DriverManager.getConnection("jdbc:postgresql:Fangorn//localhost/erdf",
			   //                                  "Bouclefeuille", "m3rl1n");
			    connection = DriverManager.getConnection("jdbc:postgresql://192.9.200.80:5432/website",
			    								"Bouclefeuille", "m3rl1n");
			  // connection = DriverManager.getConnection("jdbc:postgresql://webserv:5433/ifndb",
			  // 			   "Bouclefeuille", "m3rl1n");
               connection.setAutoCommit(false);
          
               setLanguage(languageKey);
               //FillColumnSize();
		   } catch(Exception e) {
			   //System.err.println(e.toString());
			   //System.out.println("Err connexion : " + e.toString());
			   Log.println (Log.ERROR, "IfnCaDInitStand.IfnCaDInitStand (IfnCaRelay)",
						"Exception caught (IfnCaDInitStand.getConnection ()) : ",e);
			   MessageDialog.print (this, Translator.swap (
									"IfnCaDInitStand.getConnection"), e);
			   //return;
		   }
	   }
	   return connection;
   }
   
   /**
    * Fixe la langue et charge en memoire le nom des colonnes traduit dans la
    *  langue ainsi que les libelles des valeurs
    */
   public void setLanguage(String language)
   {
	   languageKey = new String(language);
   }

   /**
    * Remplissage du traducteur de taille de colonne
   	*/
   	private void FillColumnSize()
   	{
      try
      {
        if (connection != null)
        {
          if (!columnSize.exist())
          {
            ResultSet result = null;
            Statement statement = connection.createStatement();
            result = statement.executeQuery("SELECT OBJET, TAILLE, " +
                                            "DECIM, TAILLE_AFFICH FROM STKWSIZ");
            columnSize.FillArraySize(result);
            statement.close();
          }
        }
      }
      catch(SQLException sqlE)
      {
        System.err.println(sqlE.toString());
      }
   }

   	/**
   	 * Renvoie l'objet controleur correspondant a la requete passee en parametre
   	 * Cet objet contient les elements lus de la base de donnees
   	 */
   	public Controler getControler(String query) throws SQLException {
	   Controler control = null;
	   ResultSet result = null;
	   int colonNum;
      
	   try {
		   Statement statement = getConnection().createStatement();
		   result = statement.executeQuery(query);
		   colonNum = result.getMetaData().getColumnCount();
		   control = new Controler(colonNum, result, languageKey,columnSize);
		   result.close();
		   statement.close();
	   } catch(Exception e) {
    	  System.out.println("Exception = " + e.toString());
	   }
	   return control;
   }


  /****************************************************************************/
  /*Execution d'une requete en base de donnees                                */
  /* Cette requete doit etre une requete qui ne renvoie pas d'elements        */
  /* exmple: UPDATE,DROP, DELETE et non SELECT                                */
  /* commit: flag pour commiter la transaction a faire                        */
  /* cette methode renvoie le nombre de lignes traitées                       */
  /****************************************************************************/
   public int executeQuery(String query,int certif,boolean commit) throws SQLException,IfnException
   {
      int value_returned;
      //if (certif == certificat)
      //{
        Statement statement = getConnection().createStatement();
        value_returned = statement.executeUpdate(query);
        statement.close();
        if (commit == true)
          connection.commit();
      //}
      //else
      //  throw new IfnException("CERTIFICAT CORROMPU");
      return value_returned;
   }
   
   public int executeQuery(String query,boolean commit) throws SQLException,IfnException
   {
      int value_returned;

        Statement statement = getConnection().createStatement();
        value_returned = statement.executeUpdate(query);
        statement.close();
        if (commit == true)
          connection.commit();
      
      
      return value_returned;
   }
   
   /**
    * 
    * @param queries
    * @param commit
    * @return 
    * @throws SQLException
    * @throws IfnException
    */
   public int executeQueries(Vector queries,boolean commit) throws Exception
   {
      int value_returned = 0;
      String query = "";
      Statement statement;
      for(int nbsql = 0; nbsql < queries.size() ; nbsql++) {
    	  //System.out.println("SQL insert : " + queries.elementAt(nbsql).toString());
    	  query = queries.elementAt(nbsql).toString();
    	  statement = getConnection().createStatement();
    	  try {
    		  value_returned = statement.executeUpdate(query);
    		  //System.out.println(value_returned + " " + query);
    	  } catch(Exception e){
    		  System.out.println("ERR SQL " + query + " = " + e.toString());
    		  Log.println (Log.ERROR, "IfnCaDGrowthParameters.createUI ()",
						"Exception caught (IfnCaDGrowthParameters.getScenarioName ())",e);
			  MessageDialog.print (this, Translator.swap (
									"IfnCaDGrowthParameters.exceptionDuringGetScenarioName"), e);
			  throw e;
    	  }
          statement.close();
      }
      if (commit == true)
          connection.commit();
      
      /*if (certif == certificat)
      {
        Statement statement = getConnection().createStatement();
        value_returned = statement.executeUpdate(query);
        statement.close();
        if (commit == true)
          connection.commit();
      }
      else
        throw new IfnException("CERTIFICAT CORROMPU");
        */
      return value_returned;
   }

  /****************************************************************************/
  /*Renvoie le nombre d'element dans la table passe en parametre en fonction  */
  /* de la condition passee en parametre                                      */
  /****************************************************************************/
   public int getSelectCount(String table, String condition)throws SQLException
   {
      String result;
      Integer iresult;
      Controler control = this.getControler("select count(*) from " + table
         + " where " + condition);
      result = (String)control.getValueAt((int)0,(int)0);
      iresult = new Integer(result);
      return iresult.intValue();
    }

  /****************************************************************************/
  /*Commit de la ou des transactions précedemment effectuees                  */
  /****************************************************************************/
    public void commit()throws SQLException
    {
        getConnection().commit();
    }
  /****************************************************************************/
  /*Rollback de la ou des transactions précedemment effectuees                */
  /****************************************************************************/
    public void rollback()throws SQLException
    {
        getConnection().rollback();
    }
  /****************************************************************************/
  /*Fermeture de la connexion a la base de donnees                            */
  /****************************************************************************/
   public void closeConnection()throws SQLException
   {
      getConnection().close();
   }

  /****************************************************************************/
  /* Renvoie la taille de la colonne: cette taille est issue de la table      */
  /* STKWSIZE                                                                 */
  /****************************************************************************/
   public long getColumnSize(String columnName)
   {
      if (columnSize != null)
        return columnSize.getColumnSize(columnName);
      else
        return 0;
   }
  /****************************************************************************/
  /* Renvoie la taille de la colonne: cette taille est issue de la table      */
  /* STKWSIZE                                                                 */
  /****************************************************************************/
   public long getDecimalColumnSize(String columnName)
   {
      if (columnSize != null)
        return columnSize.getDecimalColumnSize(columnName);
      else
        return 0;
   }
  /****************************************************************************/
  /* Renvoie la taille de la colonne pour affichage : cette taille est issue  */
  /*   de la table STKWSIZ                                                    */
  /****************************************************************************/
   public long getColumnScreenSize(String columnName)
   {
      if (columnSize != null)
        return columnSize.getColumnScreenSize(columnName);
      else
        return 0;
   }

  /****************************************************************************/
  /* Gestion des fichiers de traces                                           */
  /*  - renomage des fichiers avec destruction du fichier backup si la taille */
  /*   est superieur a la taille maximum                                      */
  /*  - ouverture des fichiers de trace                                       */
  /****************************************************************************/
  private void filesControl()
  {
    String pathName = tracePath +machineName;
    //gestion du fichier d'infos
    //**************************
    File fichier = new File(pathName + CV_SVR_FILE_TRACE_INFO + CV_SVR_FILE_ACT_EXT);
    if (fichier.length()> CV_SVR_FILE_LENGTH_MAX)
    {
      File fichierBak = new File(pathName + CV_SVR_FILE_TRACE_INFO + CV_SVR_FILE_BAK_EXT);
      fichierBak.delete();
      fichier.renameTo(fichierBak);
    }

    //gestion du fichier d'erreurs
    //****************************
    fichier = new File(pathName + CV_SVR_FILE_TRACE_ERROR + CV_SVR_FILE_ACT_EXT);
    if (fichier.length()> CV_SVR_FILE_LENGTH_MAX)
    {
      File fichierBak = new File(pathName + CV_SVR_FILE_TRACE_ERROR + CV_SVR_FILE_BAK_EXT);
      fichierBak.delete();
      fichier.renameTo(fichierBak);
    }
  }

  /****************************************************************************/
  /* Gestion des traces                                                       */
  /*  - affichage des informations avec renomage de fichier                   */
  /****************************************************************************/
  public void traceInfo( String line, int level)
  {
    try
    {
      String pathName = tracePath +machineName;
      //Ouvre le fichier ou créé le fichier si besoin (inexistant)
      fwInfo = new FileWriter(pathName + CV_SVR_FILE_TRACE_INFO + CV_SVR_FILE_ACT_EXT,
                                   true);
      BufferedWriter bw = new BufferedWriter(fwInfo);
      PrintWriter pw = new PrintWriter(bw);
      pw.println(line);
      pw.close();
      bw.close();
      fwInfo.close();
    }
    catch (IOException ioe)
    {
      System.out.println("Problème dans DialogBase.traceInfo()" + ioe);
    }
  }
  /****************************************************************************/
  /* Gestion des traces                                                       */
  /*  - affichage des erreurs avec renomage de fichier                        */
  /****************************************************************************/
  public void traceError( String line, int level)
  {
    try
    {
      String pathName = tracePath +machineName;
      //Ouvre le fichier ou créé le fichier si besoin (inexistant)
      FileWriter fwError = new FileWriter(pathName + CV_SVR_FILE_TRACE_ERROR + CV_SVR_FILE_ACT_EXT,
                                     true);
      BufferedWriter bw = new BufferedWriter(fwError);
      PrintWriter pw = new PrintWriter(bw);
      pw.println(line);
      pw.close();
      bw.close();
      fwError.close();
    }
    catch (IOException ioe)
    {
      System.out.println("Problème dans DialogBase.traceError()" + ioe);
    }
 }

 /****************************************************************************/
  /* Gestion des traces                                                       */
  /*  - affichage des informations avec renomage de fichier                   */
  /****************************************************************************/
   public StringBuffer getBufferFromFile(String fileName)
   {
      StringBuffer stringBuffer  = new StringBuffer();
      try
      {
        File f = new File(fileName);
        // Read in text file
        FileReader fin = new FileReader (f);
        BufferedReader br = new BufferedReader (fin);
        char buffer[] = new char[4096];
        int len;
        while ((len = br.read (buffer, 0, buffer.length)) != -1)
        {
          // Insert into pane
          stringBuffer.append(new String (buffer, 0, len));
        }
      }
      catch (Exception e)
      {
        System.out.println("Problème dans getFile()" + e.toString());
      }
      return stringBuffer;
  }
   
   
}