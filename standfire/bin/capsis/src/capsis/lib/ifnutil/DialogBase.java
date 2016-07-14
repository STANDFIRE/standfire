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
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;

import jeeb.lib.util.AmapDialog;

public class DialogBase extends AmapDialog implements  ActionListener, KeyListener,IfnConstante,FocusListener {

	// Variable pour acces utilisateur
	public static String terminalName ;
	public static String userName ;
	protected int diaseq ;
	public static int nivTraceInfo = 2;


  static ResourceBundle StringTranslator;
  static private Locale currentLocale;
  static private String currentLanguageKey;
  static private String urlServer;
  static private String applicationName;
  static private String pathName;
  static private String dbConnectionName;
  static private String statusInfo;
  static private URL codeBase;
  static private ImageIcon logoIFN;
  static private ImageIcon logoERDF;
  static private String lookAndFeelType;
  static private boolean activationState;

  protected JButton quitButton;
  protected JTextField  statusBar;
  protected BorderLayout dialogBackGroundLayout;
  protected BorderLayout commandStatusLayout;
  protected GridLayout commandLayout;
  protected GridBagLayout commandLayoutConstant;
  protected GridBagLayout commandLayoutOther;
  protected GridBagLayout actionLayout;
  protected JPanel actionPanel;
  protected JPanel commandPanel;
  protected JPanel commandPanelConstant;
  public JPanel commandPanelOther;
  protected JPanel commandStatusPanel;
  protected PanelDB panelDB ;
  private DialogBase parent;
  private String title;



  //font de l'application
  Font titleSectionFont;
  Font standardFont;
  Font listFont;

  //transient private static DBconnection dbConnection;


  /****************************************************************************/
  /*Constructeur de la boite de dialogue:                                     */
  /* - title: titre de la boite de dialogue                                   */
  /* - aParent: pointe vers la boite de dilogue appelante                     */
  /****************************************************************************/
  public DialogBase(String in_title, DialogBase in_aParent)
  {
    try
    {
      //Declaration
      parent = in_aParent;
      title = in_title;

      //Traitement
      traceDialog();
      setTitle(title);
      initDialog();
    }
    catch(Exception ex)
    {
      showStatus(ex.toString());
    }
  }
  /****************************************************************************/
  /*Constructeur par defaut                                                   */
  /****************************************************************************/
  public DialogBase()
  {
    try
    {
      parent = null;
      initDialog();
    }
    catch(Exception ex)
    {
      showStatus(ex.toString());
    }
  }

  /****************************************************************************/
  /*Initialisation de la boite de dialogue                                    */
  /****************************************************************************/
  private void initDialog()
  {
    dialogBackGroundLayout  = new BorderLayout();
    this.getContentPane().setLayout(dialogBackGroundLayout);
    titleSectionFont    =   new java.awt.Font("Dialog", 1, 14);
    standardFont        =   new java.awt.Font("Dialog", 1, 12);
    listFont            =   new java.awt.Font("Courier", Font.PLAIN, 12);

    setLookAndFeel();

    initActionPanel();
    initCommandPanel();
    initStatusPanel();

    setModal(true);
   
    this.addKeyListener(this);
    /*gestion des évenements fenetre*/
    enableEvents(WindowEvent.WINDOW_CLOSING);

  }


  /****************************************************************************/
  /*Fixe l'apparence des fenetres                                             */
  /****************************************************************************/
  public void setLookAndFeel(String type)
  {
    lookAndFeelType = type;
  }
  /****************************************************************************/
  /*Fixe l'apparence des fenetres                                             */
  /****************************************************************************/
  private void setLookAndFeel()
  {
    try
    {
      if (lookAndFeelType!=null)
      {
        if (lookAndFeelType.equalsIgnoreCase("MOTIF"))
          UIManager.setLookAndFeel(new com.sun.java.swing.plaf.motif.MotifLookAndFeel());
        else if (lookAndFeelType.equalsIgnoreCase("WINDOWS"))
          UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        else
          UIManager.setLookAndFeel(new javax.swing.plaf.metal.MetalLookAndFeel());
      }
    }
    catch(Exception ex)
    {
      System.out.println(ex.toString());
    }

  }


  	/**
  	 * Création de la connexion
  	 */
  	/*public void createConnection() {
  		try {
  			// Initialisation de la connexion oracle
  			//dbConnection = new DBconnection(getURLServer(),getDBConnection(),
            //            getApplicationName(), getLanguage());
  			dbConnection = DBconnection.getInstance(getURLServer(),getDBConnection(),
  		                        getApplicationName(), getLanguage());
  		} catch(Exception e) {
  			//	showStatus(e.toString());
  			return;
  			//	System.out.println("Pb de connexion " + e.toString());
  		}
  	}*/

  /****************************************************************************/
  /*recuperation du nom de l'application                                      */
  /****************************************************************************/
  public  String  getApplicationName()
  {
    return applicationName ;
  }

  /****************************************************************************/
  /*recuperation de la connexion vers la servlet                              */
  /****************************************************************************/
  public  ConnectionInt getConnection ()
  {
    /*if (dbConnection != null)
      return dbConnection.getConnection();
    else
      return null;*/
	  try {
		  return DBconnection.getInstance("", "", "erdf", "").getConnection();
	  } catch(Exception e) {
		  System.out.println(e.toString());
		  e.printStackTrace(System.out);
		  return null;
	}
	  
  }
/****************************************************************************/
  /*recuperation de la connexion vers la servlet                              */
  /****************************************************************************/
  /*public  DBconnection getDataBaseConnection()
  {
      return dbConnection;
  }*/

  /****************************************************************************/
  /*recuperation de la connexion vers la servlet                              */
  /****************************************************************************/
  /*public  int getCertificat()
  {
    if (dbConnection !=null)
      return dbConnection.getCertificat();
    else
      return 0;
  }*/


  /****************************************************************************/
  /*Initialisation du sud de la boite de dialogue                             */
  /* gestion des panels de commandes et status                                */
  /****************************************************************************/
  protected void initStatusPanel()
  {
    statusBar = new JTextField();
    statusBar.setBackground(Color.orange);
    statusBar.setHorizontalAlignment(JTextField.CENTER);
    statusBar.setFont(titleSectionFont);
    statusBar.setEditable(false);
    if (commandStatusPanel !=null)
      commandStatusPanel.add(statusBar, BorderLayout.SOUTH);
  }

  /****************************************************************************/
  /*Initialisation de l'est de la boite de dialogue                           */
  /* gestion des panels de commandes type liste par exmple                    */
  /****************************************************************************/
  protected void initActionPanel()
  {
    actionLayout =  new GridBagLayout();
    commandLayoutConstant =  new GridBagLayout();
    commandLayoutOther =  new GridBagLayout();
    actionPanel = new JPanel(actionLayout);
    this.getContentPane().add(actionPanel,BorderLayout.EAST);
  }


  /****************************************************************************/
  /*Initialisation du sud de la boite de dialogue                             */
  /* gestion des panels de commandes et status                                */
  /****************************************************************************/
  protected void initCommandPanel()
  {
    quitButton = new JButton(CV_QUIT);

    commandStatusLayout = new BorderLayout();
    commandLayout =  new GridLayout(2,1);
    commandLayoutConstant =  new GridBagLayout();
    commandLayoutOther =  new GridBagLayout();
    commandPanel = new JPanel(commandLayout);
    commandPanelConstant = new JPanel(commandLayoutConstant);
    commandPanelOther = new JPanel(commandLayoutOther);
    commandStatusPanel = new JPanel(commandStatusLayout);

    /*configuration des composants*/
    quitButton.setForeground(Color.red);

    /*ajout des composant dans leur panels*/
    this.getContentPane().add(commandStatusPanel,BorderLayout.SOUTH);
    commandPanel.add(commandPanelOther);
    commandPanel.add(commandPanelConstant);
    commandStatusPanel.add(commandPanel,BorderLayout.NORTH);

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridwidth =1;
    gbc.weightx = 1;
    gbc.anchor = gbc.SOUTHEAST;
    gbc.fill = gbc.BOTH;

    gbc.gridx = 3;
    commandLayoutConstant.setConstraints(quitButton, gbc);
    commandPanelConstant.add(quitButton);
    quitButton.addActionListener(this);

  }


  /****************************************************************************/
  /*   Sortie de la boitre de dialogue courante avec activation du timer de   */
  /*   de la fenetre precedente                                               */
  /****************************************************************************/
  public void closeDialog()
  {
      this.dispose();
  }

  /****************************************************************************/
  /*Gestion des evenements windows                                            */
  /****************************************************************************/
  protected void processWindowEvent(WindowEvent evt)
  {
    //Ouverture de la fenetre
    if (evt.getID() == WindowEvent.WINDOW_OPENED)
    {
      //Appel la méthode qui va permettre de pointer sur le premier champ du dialog
      firstFocus();
    }
    if (evt.getID() == WindowEvent.WINDOW_ACTIVATED)
    {
      setActivationState();
      initAfterActivation();
      resetActivationState();
    }
    //Permet de fermer le dialog
    if (evt.getID() == WindowEvent.WINDOW_CLOSING)
    {
      closeDialog();
    }
  }

  /****************************************************************************/
  /*Gestion des evenements                                                    */
  /****************************************************************************/
  public void actionPerformed(ActionEvent evt)
  {
    traceAction(evt.getActionCommand().toString());

    /*pression sur le bouton Quitter*/
    if (evt.getActionCommand().equals(CV_QUIT))
    {
      closeDialog();
    }
    /*pression sur le bouton selectionner*/
    else if (evt.getActionCommand().equals(CV_AFFICHE))
    {
      selectionTraitement();
    }
    /*pression sur le bouton detail*/
    else if (evt.getActionCommand().equals(CV_DETAIL))
    {
      detailProcess();
    }
    /*pression sur le bouton creer*/
    else if (evt.getActionCommand().equals(CV_CREATE))
    {
      createProcess();
    }
    /*pression sur le bouton modifier*/
    else if (evt.getActionCommand().equals(CV_UPDATE))
    {
      updateProcess();
    }
    /*pression sur le bouton supprimer*/
    else if (evt.getActionCommand().equals(CV_DELETE))
    {
       deleteProcess();
    }
    /*pression sur le bouton confirmer*/
    else if (evt.getActionCommand().equals(CV_CONFIRM))
    {
      confirmProcess();
    }
    refreshStatus();
  }




  /****************************************************************************/
  /*Affichage du status                                                       */
  /****************************************************************************/
 public void showStatus(String status)
 {
    statusInfo = status;
    //System.out.println(statusInfo);
    Toolkit.getDefaultToolkit().beep();
    Toolkit.getDefaultToolkit().beep();
    Toolkit.getDefaultToolkit().beep();
    statusBar.setBackground(Color.red);
    statusBar.setForeground(Color.orange);
    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    statusBar.setText(statusInfo);
 }
  /****************************************************************************/
  /*RAZ du status                                                             */
  /****************************************************************************/
 private void refreshStatus()
 {
    if (statusInfo!=null)
    {
      statusInfo = null;
      statusBar.setBackground(Color.orange);
      setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      statusBar.setText("");
    }
 }

 /****************************************************************************/
 /*Fixe la langue par defaut                                                 */
 /****************************************************************************/
 public void setLanguage(String key)
 {
    try
    {
        currentLanguageKey = new String(key);
        currentLocale= new Locale(key.toLowerCase(),key.toUpperCase());
        if (getConnection()!=null)
        getConnection().setLanguage(key);
        if (StringTranslator !=null)
          StringTranslator = null;
    }
    catch(Exception e)
    {
      showStatus(e.toString());
    }
 }
 /****************************************************************************/
 /*Recupération de la langue par défaut                                      */
 /****************************************************************************/
 public String getLanguage()
 {
    if (currentLanguageKey == null)
      return CV_FRENCH_LOCAL;
    else
      return currentLanguageKey;
 }

 /****************************************************************************/
 /*Méthode permettant de récupérer depuis le fichier Resources la valeur de  */
 /*l'élément passé en paramètre :                                            */
 /*         - nature                                                         */
 /*         - objet                                                          */
 /*Cette methode permet de traduire un champ texte de type ressource (texte  */
 /* de bouton, message operateur, etc.)                                      */
 /****************************************************************************/
 public String getString(String in_nature, String in_objet)
 {
    String value;
    try
    {
      if (currentLocale==null)
      {
        /*on force la langue française*/
        setLanguage(CV_FRENCH_LOCAL);
      }
      if (StringTranslator == null)
        StringTranslator = ResourceBundle.getBundle(CV_TRANSLATED_FILE,
                                                    currentLocale);
      value  = StringTranslator.getString(in_nature + "_" + in_objet);
    }
    catch(Exception e)
    {
      traceError( "getString", "Ressource: " + in_nature + "_" + in_objet +
        " introuvable");
      value = in_nature + "_" + in_objet;
    }

    return value;

 }
 /****************************************************************************/
 /*Méthode permettant de récupérer depuis le fichier Resources la valeur de  */
 /*l'élément passé en paramètre :                                            */
 /*         - nature                                                         */
 /*         - objet                                                          */
 /*         - valeur                                                         */
 /*Cette methode permet de traduire un champ texte de type ressource (texte  */
 /* de bouton, message operateur, etc.)                                      */
 /****************************************************************************/
 public String getString(String in_nature, String in_objet, String in_valeur)
 {
  String value;
    try
    {
      if (currentLocale==null)
      {
        /*on force la langue française*/
        setLanguage(CV_FRENCH_LOCAL);
      }
      if (StringTranslator == null)
        StringTranslator = ResourceBundle.getBundle(CV_TRANSLATED_FILE,
                                                    currentLocale);
        value  = StringTranslator.getString(in_nature +
                                                 "_" + in_objet +
                                                 "_" + in_valeur);
    }
    catch(Exception e)
    {
      traceError( "getString", "Ressource: " + in_nature + "_" + in_objet +
        "_" + in_valeur + " introuvable");
      value = in_nature + "_" + in_objet + "_" + in_valeur;
    }
    return value;
 }

  /****************************************************************************/
 /*Ajout d'un composant dans le panel prevu a cet effet sur la partie EST     */
 /* les parametres y correspondent à des coordonnées type GridBagLayout       */
 /*****************************************************************************/
 public void addActionComponent(Component comp,  int y)
 {
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.gridx = 0;
      gbc.gridy = y;
      gbc.gridwidth =1;
      gbc.weightx = 1;
      gbc.anchor = gbc.SOUTHEAST;
      gbc.fill = gbc.BOTH;
      gbc.insets = new Insets(10, 10/*9*/, 10, 10);
      actionLayout.setConstraints(comp, gbc);
      actionPanel.add(comp);
 }

 /****************************************************************************/
 /*Ajout d'un composant dans le panel prevu a cet effet.                     */
 /* les parametres x,y correspondent à des coordonnées type GridBagLayout    */
 /****************************************************************************/
 public void addComponent(Component comp, int x, int y)
 {
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.gridx = x;
      gbc.gridy = y;
      gbc.gridwidth =1;
      gbc.weightx = 1;
      gbc.anchor = gbc.SOUTHEAST;
      gbc.fill = gbc.BOTH;
      commandLayoutOther.setConstraints(comp, gbc);
      commandPanelOther.add(comp);

 }
 /****************************************************************************/
 /*Fixe le nom de l'application issue de la page HTML                        */
 /****************************************************************************/
 public void setApplicationName(String name)
 {
    applicationName = name;
 }
 /****************************************************************************/
 /*Fixe le url de base de la servlet                                         */
 /****************************************************************************/
 public void setURLcodeBase (URL in_codeBase)
 {
   codeBase = in_codeBase;
 }
 /****************************************************************************/
 /*Fixe le chemin des fichiers 'trace'                                       */
 /****************************************************************************/
 public void setPathName(String name)
 {
    pathName = name;
 }

 /****************************************************************************/
 /*Fixe le parametre du serveur où se trouve la servlet                      */
 /****************************************************************************/
 public void setURLServer(String url)
 {
     urlServer = url;
 }
 /****************************************************************************/
 /*Fixe le logo IFN                                                       */
 /****************************************************************************/
 public void setlogoIFN(ImageIcon in_logo)
 {
     logoIFN = in_logo;
 }

 /****************************************************************************/
 /*Recupere Fixe le logo IFN                                                       */
 /****************************************************************************/
 public ImageIcon getlogoIFN()
 {
     return logoIFN ;
 }

 /****************************************************************************/
 /*Fixe le logo IFN                                                       */
 /****************************************************************************/
 public void setlogoERDF(ImageIcon in_logo)
 {
     logoERDF = in_logo;
 }

 /****************************************************************************/
 /*Recupere Fixe le logo IFN                                                       */
 /****************************************************************************/
 public ImageIcon getlogoERDF()
 {
     return logoERDF ;
 }


 /****************************************************************************/
 /*Restitue le parametre du serveur où se trouve la servlet                  */
 /****************************************************************************/
 public String getURLServer()
 {
     return urlServer;
 }

 /****************************************************************************/
 /*Fixe le parametre du serveur où se trouve la servlet                      */
 /****************************************************************************/
 public void setDBConnection(String dbconnection)
 {
	 dbConnectionName = dbconnection;
 }

 /****************************************************************************/
 /*Fixe la taille de la boite de dialogue pour remplir tout l'espace         */
 /****************************************************************************/
 public void setMaximumSize()
 {
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    setSize(screenSize.width , screenSize.height  - 28);
 }


 /****************************************************************************/
 /*Restitue le parametre du serveur où se trouve la servlet                  */
 /****************************************************************************/
 public String getDBConnection()
 {
     return dbConnectionName;
 }

 /****************************************************************************/
 /*                       Trace dans un fichier                              */
 /****************************************************************************/
 public void traceError( String method, String message)
 {
    try
    {
      //formatage de la ligne a tracer
      //******************************
      //Défini le format de la date
      SimpleDateFormat formatter = new SimpleDateFormat
                                       ("dd/MM/yyyy HH:mm:ss");
      //Date actuelle format String
      String dateString = formatter.format(new Date
                                               (System.currentTimeMillis()));
      //Constitution de la ligne à générer
      String line = dateString + " - " + this.getClass().getName() +
                                         "." + method + " - " + message;
      //trace dans le fichier courant si le chemin existe: cf parametre applet
      if (pathName !=null)
      {
        //gestion de la taille du fichier
        File fichier = new File(pathName + CV_FILE_TRACE_ERROR + CV_FILE_ACT_EXT);
        if (fichier.length()> CV_FILE_LENGTH_MAX)
        {
          File fichierBak = new File(pathName + CV_FILE_TRACE_ERROR + CV_FILE_BAK_EXT);
          fichierBak.delete();
          fichier.renameTo(fichierBak);
        }
        //Ouvre le fichier ou créé le fichier si besoin (inexistant)
        FileWriter fw = new FileWriter(pathName + CV_FILE_TRACE_ERROR + CV_FILE_ACT_EXT,
                                       true);
        BufferedWriter bw = new BufferedWriter(fw);
        PrintWriter pw = new PrintWriter(bw);
        pw.println(line);
        pw.close();
        bw.close();
        fw.close();
      }
      //trace sur le serveur
      if (getConnection()!=null)
        getConnection().traceInfo(line,0);
    }
    catch (IOException ioe)
    {
      System.out.println("Problème dans DialogBase.traceError()" + ioe);
    }
 }
 /****************************************************************************/
 /*                       Trace dans un fichier                              */
 /****************************************************************************/
 public void traceInfo( String method, String message)
 {
     System.out.println(message);
    try
    {
      //formatage de la ligne a tracer
      //******************************
      //Défini le format de la date
      SimpleDateFormat formatter = new SimpleDateFormat
                                       ("dd/MM/yyyy HH:mm:ss");
      //Date actuelle format String
      String dateString = formatter.format(new Date
                                               (System.currentTimeMillis()));
      //Constitution de la ligne à générer
      String line = dateString + " - " + this.getClass().getName() +
                                         "." + method + " - " + message;
      //trace dans le fichier courant si le chemin existe: cf parametre applet
      if (pathName !=null)
      {
        //gestion de la taille du fichier
        File fichier = new File(pathName + CV_FILE_TRACE_INFO + CV_FILE_ACT_EXT);
        if (fichier.length()> CV_FILE_LENGTH_MAX)
        {
          File fichierBak = new File(pathName + CV_FILE_TRACE_INFO + CV_FILE_BAK_EXT);
          fichierBak.delete();
          fichier.renameTo(fichierBak);
        }
        //Ouvre le fichier ou créé le fichier si besoin (inexistant)
        FileWriter fw = new FileWriter(pathName + CV_FILE_TRACE_INFO + CV_FILE_ACT_EXT,
                                       true);
        BufferedWriter bw = new BufferedWriter(fw);
        PrintWriter pw = new PrintWriter(bw);
        pw.println(line);
        pw.close();
        bw.close();
        fw.close();
      }
      //trace sur le serveur
      if (getConnection()!=null)
        getConnection().traceInfo(line,0);
    }
    catch (IOException ioe)
    {
      System.out.println("Problème dans DialogBase.traceInfo()" + ioe);
    }
 }


/*=======================================================================*/
/*                                                                       */
/*         Gestion des requetes SQL                                      */
/*    redefinition des methodes issus de ConnectionInt afin :            */
/*     - d'intégrer les traces des requetes                              */
/*     - de gérer les exceptions systemes et les transformer en          */
/*       IfnException                                                   */
/*     - gérer la sécurité d'accès                                       */
/*                                                                       */
/*=======================================================================*/

/****************************************************************************/
/*Renvoie l'objet controleur correspondant a la requete passee en parametre */
/* Cet objet contient les elements lus de la base de donnees                */
/****************************************************************************/
public Controler getControler(String query, boolean traceRequete) throws IfnException
{
    Controler control=null;
    if (getConnection()!=null)
    {
      //trace de la requete
      //if (traceRequete)
        //traceInfo("getControler", "Requete = "+ query);
      try
      {
        //execution de la requete
        control = getConnection().getControler(query);
      }
      //traitement des execptions
      catch(SQLException exp)
      {
        traceError("getControler",exp.toString());
        System.out.println("ERR CONTROLER (" + query + ") : " + exp.toString());
        IfnException exception = new IfnException(getString(CV_MESSAGE,CV_PB_DATA_BASE)+" (" +
          exp.getMessage()+ ")");
        exception.setErrorCode(exp.getErrorCode());
        throw exception;
      }
    }
    else
    {
      System.out.println("connection non initialisée");
      traceError("executeQuery","connection non initialisée");
      throw new IfnException(getString(CV_MESSAGE,CV_CONNEXION_SERVER_IMPOSSIBLE));
    }
    return control;
}

/****************************************************************************/
/* id. getControler sans trace                                              */
/****************************************************************************/
public Controler getControler(String query) throws IfnException
{
  return getControler(query,true);
}


/****************************************************************************/
/*Execution d'une requete en base de donnees                                */
/* Cette requete doit etre une requete qui ne renvoie pas d'elements        */
/* exmple: UPDATE,DROP, DELETE et non SELECT                                */
/* commit: flag pour commiter la transaction a faire                        */
/* cette methode renvoie le nombre de lignes traitées                       */
/****************************************************************************/
 public int executeQuery(String query,boolean commit,boolean traceRequete) throws IfnException
 {
    int valreturn = 0;
    if (getConnection()!=null)
    {
      //trace de la requete
      //if (traceRequete)
        //traceInfo("executeQuery", "Requete = "+ query);
      try
      {
        //execution de la requete
        valreturn = getConnection().executeQuery(query,0,commit);
      }
      //traitement des execptions
      catch(SQLException exp)
      {
    	 System.out.println("Error = " + exp.getMessage());
        /*traceError("executeQuery",exp.toString());
        IfnException exception = new IfnException(getString(CV_MESSAGE,CV_PB_DATA_BASE)+" (" +
          Integer.toString(exp.getErrorCode())+ ")");
        exception.setErrorCode(exp.getErrorCode());
        throw exception;*/
      }
    }
    else
    {
      traceError("executeQuery","Connexion non initialisée");
      throw new IfnException(getString(CV_MESSAGE,CV_CONNEXION_SERVER_IMPOSSIBLE));
    }
    return valreturn;
 }
/****************************************************************************/
/* id. getControler sans trace                                              */
/****************************************************************************/
 public int executeQuery(String query,boolean commit) throws IfnException
 {
  return executeQuery(query,commit,true);
 }

  /****************************************************************************/
  /*Renvoie le nombre d'element dans la table passe en parametre en fonction  */
  /* de la condition passee en parametre                                      */
  /****************************************************************************/
   public int getSelectCount(String table, String condition)throws
    IfnException
  {
    int valreturn = 0;
    if (getConnection()!=null)
    {
      //trace de la requete
      traceInfo("getSelectCount", "table = "+ table + ", condition = " + condition);
      try
      {
        //execution de la requete
        valreturn = getConnection().getSelectCount(table, condition);
      }
      //traitement des execptions
      catch(SQLException exp)
      {
        traceError("getSelectCount",exp.toString());
        IfnException exception = new IfnException(getString(CV_MESSAGE,CV_PB_DATA_BASE)+" (" +
          Integer.toString(exp.getErrorCode())+ ")");
        exception.setErrorCode(exp.getErrorCode());
        throw exception;
      }
    }
    else
    {
      traceError("getSelectCount","Connexion non initialisée");
      throw new IfnException(getString(CV_MESSAGE,CV_CONNEXION_SERVER_IMPOSSIBLE));
    }
    return valreturn;
  }

  /****************************************************************************/
  /* Renvoie la taille de la colonne: cette taille est issue de la table      */
  /* STKWSIZE                                                                 */
  /****************************************************************************/
   public long getColumnSize(String columnName)
   {
    long valreturn=0;
    if (getConnection()!=null)
    {
        //execution de la requete
        valreturn = getConnection().getColumnSize(columnName);
    }
    return valreturn;
   }

  /****************************************************************************/
  /* Renvoie la partie decimale de la taille de la colonne:                   */
  /* cette taille est issue de la table                                       */
  /* STKWSIZE                                                                 */
  /****************************************************************************/
   public long getDecimalColumnSize(String columnName)
   {
    long valreturn=0;
    if (getConnection()!=null)
    {
        //execution de la requete
        valreturn = getConnection().getDecimalColumnSize(columnName);
    }
    return valreturn;
   }

  /****************************************************************************/
  /* Renvoie la taille a utilisée dans l'ecran:                               */
  /* cette taille est issue de la table                                       */
  /* STKWSIZE                                                                 */
  /****************************************************************************/
   public long getColumnScreenSize(String columnName)
   {
    long valreturn=0;
    if (getConnection()!=null)
    {
        //execution de la requete
        valreturn = getConnection().getColumnScreenSize(columnName);
    }
    return valreturn;
   }




  /****************************************************************************/
  /*Commit de la ou des transactions précedemment effectuees                  */
  /****************************************************************************/
    public void commit()throws IfnException
    {
    if (getConnection()!=null)
    {
      //trace de la requete
      traceInfo("commit","");
      try
      {
        //execution de la requete
        getConnection().commit();
      }
      //traitement des execptions
      catch(SQLException exp)
      {
        traceError("commit",exp.toString());
        IfnException exception = new IfnException(getString(CV_MESSAGE,CV_PB_DATA_BASE)+" (" +
          Integer.toString(exp.getErrorCode())+ ")");
        exception.setErrorCode(exp.getErrorCode());
        throw exception;
      }
    }
    else
    {
      traceError("commit","Connexion non initialisée");
      throw new IfnException(getString(CV_MESSAGE,CV_CONNEXION_SERVER_IMPOSSIBLE));
    }
  }

  /****************************************************************************/
  /*Commit de la ou des transactions précedemment effectuees                  */
  /****************************************************************************/
  public void rollback()throws IfnException
  {
    if (getConnection()!=null)
    {
      //trace de la requete
      traceInfo("rollback","");
      try
      {
        //execution de la requete
        getConnection().rollback();
      }
      //traitement des execptions
      catch(SQLException exp)
      {
        traceError("rollback",exp.toString());
        IfnException exception = new IfnException(getString(CV_MESSAGE,CV_PB_DATA_BASE)+" (" +
          Integer.toString(exp.getErrorCode())+ ")");
        exception.setErrorCode(exp.getErrorCode());
        throw exception;
      }
    }
    else
    {
      traceError("rollback","Connexion non initialisée");
      throw new IfnException(getString(CV_MESSAGE,CV_CONNEXION_SERVER_IMPOSSIBLE));
    }
  }


/*----------------------------------------------------------------------------*/
/* Fixe les parametres de l'utilisateur                                       */
/* voir ecran de login                                                        */
/*----------------------------------------------------------------------------*/
  public void setUserParameters(String in_username)
  {
      userName  = in_username;
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
/*------------------------------------------------------------------------------*/
/* Permet de gérer les touches de fonctions (même vide, elle est obligatoire)   */
/*------------------------------------------------------------------------------*/
  public void keyPressed(KeyEvent e)
  {
    refreshStatus();
    //Gestion du bouton F1: Creer
    if (e.getKeyCode()== KeyEvent.VK_F1)
    {
      //Trace les actions
      traceAction(e.getKeyText(e.getKeyCode()).toString());
      createProcess();
    }
    //Gestion du bouton F2: Modifier
    else if (e.getKeyCode()== KeyEvent.VK_F2)
    {
      //Trace les actions
      traceAction(e.getKeyText(e.getKeyCode()).toString());
      updateProcess();
      e.consume();
    }
    //Gestion du bouton F3: supprimer
    else if (e.getKeyCode()== KeyEvent.VK_F3)
    {
      //Trace les actions
      traceAction(e.getKeyText(e.getKeyCode()).toString());
      deleteProcess();
    }
    //Gestion du bouton F4: Confirmer
    else if (e.getKeyCode()== KeyEvent.VK_F4)
    {
      //Trace les actions
      traceAction(e.getKeyText(e.getKeyCode()).toString());
      confirmProcess();
    }
    //Gestion du bouton F5: Lister
    else if (e.getKeyCode()== KeyEvent.VK_F5)
    {
      //Trace les actions
      traceAction(e.getKeyText(e.getKeyCode()).toString());
      selectionTraitement();
    }
    //Gestion du bouton F6: Detail
    else if (e.getKeyCode()== KeyEvent.VK_F6)
    {
      //Trace les actions
      traceAction(e.getKeyText(e.getKeyCode()).toString());
      detailProcess();
    }
    //Gestion du bouton ESCAPE: Quitter
    else if (e.getKeyCode()== KeyEvent.VK_ESCAPE)
    {
      //Trace les actions
      traceAction(e.getKeyText(e.getKeyCode()).toString());
      closeDialog();
    }
  }

/*=======================================================================*/
/*           Méthode qui est redéfinie dans les dialogs concernés        */
/*           Elle est déclenchée par le bouton Affichage                 */
/*=======================================================================*/
  protected void selectionTraitement()
  {
  }

/*=======================================================================*/
/*           Méthode qui est redéfinie dans les dialogs concernés        */
/*           Elle est déclenchée par le bouton Affichage                 */
/*=======================================================================*/
  protected void detailProcess()
  {
  }

/*=======================================================================*/
/*           Méthode qui est redéfinie dans les dialogs concernés        */
/*           Elle est déclenchée par le bouton Créer                     */
/*=======================================================================*/
  protected void createProcess()
  {
  }
/*=======================================================================*/
/*           Méthode qui est redéfinie dans les dialogs concernés        */
/*           Elle est déclenchée par le bouton Modifier                  */
/*=======================================================================*/
  protected void updateProcess()
  {
  }
/*=======================================================================*/
/*           Méthode qui est redéfinie dans les dialogs concernés        */
/*           Elle est déclenchée par le bouton supprimer                 */
/*=======================================================================*/
  protected void deleteProcess()
  {
  }
/*=======================================================================*/
/*           Méthode qui est redéfinie dans les dialogs concernés        */
/*           Elle est déclenchée par le bouton Confirmer                 */
/*=======================================================================*/
  protected void confirmProcess()
  {
  }
/*=======================================================================*/
/*           Méthode qui renvoie le parent                               */
/*=======================================================================*/
  protected DialogBase getDialogParent()
  {
    return parent;
  }
/****************************************************************************/
/*          Méthode redefinie dans les dialogs                              */
/*          Elle permet de définir le premier champ a avoir le focus        */
/****************************************************************************/
  protected void firstFocus()
  {
  }
/****************************************************************************/
/*          Méthode redefinie dans les dialogs                              */
/*          Elle permet d'initailiser des controles apres creation          */
/****************************************************************************/
  protected void initAfterActivation()
  {
  }
/****************************************************************************/
/*  Méthode permettant de tracer tous les passages dans le constructeur     */
/****************************************************************************/
  private void traceDialog()
  {
    if (nivTraceInfo == 1)
    {
      traceInfo(userName, " pour ouverture du dialogue");
    }
  }
/****************************************************************************/
/*          Méthode permettant de tracer toutes les actions                 */
/****************************************************************************/
  protected void traceAction(String in_action)
  {
    if (nivTraceInfo == 1)
    {
      traceInfo(userName + " pour action : ", in_action);
    }
  }


  /****************************************************************************/
  /*                    Méthode d'autorisation d'acces                        */
  /****************************************************************************/
  public void refresh(Container in_container, Controler in_control)
  {
    int indexComponent =0;
    while (indexComponent < in_container.getComponentCount())
    {
        Component component = in_container.getComponent(indexComponent);

        if (component instanceof PanelDB)
        {
            refresh(((Container)component), in_control);
        }
        if (component instanceof PanelGridbag)
        {
            refresh(((Container)component), in_control);
        }
        else if (component instanceof JPanel)
        {
            refresh(((Container)component), in_control);
        }
        indexComponent++;
      }

  }
  /****************************************************************************/
  /*                    Méthodes de gestion de l'initAfterActivation          */
  /* La methode activationState() permet de savoir si nous sommes dans une    */
  /* phase d'initAfterActivation. Auquel cas, il ne faut pas afficher de boite*/
  /* de dialogue (risque de reentrance CF liste vide)                         */
  /****************************************************************************/
 private void setActivationState()
  {
    activationState = true;
  }
  private void resetActivationState()
  {
    activationState = false;
  }
  public boolean activationState()
  {
    return activationState;
  }

  public void focusLost(FocusEvent event) {
	    //System.out.println("Focus Lost");
	}
	public void focusGained(FocusEvent event) {
	    //System.out.println("Focus Gained");

	}
	
	/**
	 * 
	 * @param query
	 * @param columnName
	 * @param traceRequete
	 * @return the unique value of the query
	 * @throws IfnException
	 */
	public String getUniqueValue(String query, String columnName, boolean traceRequete) throws IfnException
	{
		Controler control = null;
		String value = null;
	    if (getConnection()!=null) {
	    	try {
	    		//execution de la requete
	    		control = getConnection().getControler(query);
	    		if(control.getRowCount() > 0){
	    			if(control.getValueAt(0,columnName) != null)
	    				value = control.getValueAt(0,columnName).toString().trim(); 
	    		}
	    	} catch(SQLException exp) {
	    		traceError("getControler",exp.toString());
	    		System.out.println("ERR CONTROLER (" + query + ") : " + exp.toString());
	    		IfnException exception = new IfnException(getString(CV_MESSAGE,CV_PB_DATA_BASE)+" (" +
	    				exp.getMessage()+ ")");
	    		exception.setErrorCode(exp.getErrorCode());
	    		throw exception;
	    	}
	    } else {
	      System.out.println("Connection non initialisée");
	      traceError("executeQuery","connection non initialisée");
	      throw new IfnException(getString(CV_MESSAGE,CV_CONNEXION_SERVER_IMPOSSIBLE));
	    }
	    return value;
	}
}
