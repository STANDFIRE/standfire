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
import java.awt.Graphics;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;


public class PanelDB extends JPanel implements IfnConstante
{
  /* composants graphiques */
  public JLabel title;
  protected int maxNumPage = 1;
  JPanel panelCommand ;
  BorderLayout LayoutPanel;

  /* composant liés à la connexion oracle */
  StringBuffer query = new StringBuffer (256);
  protected Controler control;
  protected String languageKey;
  protected DialogBase dialogBase;
  protected ConnectionInt connection;

  /****************************************************************************/
  /* Constructeur:                                                            */
  /* dlg: Boite de dialog qui contient le panel                               */
  /* aTitle: titre du panel                                                   */
  /****************************************************************************/
  public PanelDB(DialogBase dlg,String aTitle) throws IfnException
  {
    try
    {
      /*construction des composants*/
      dialogBase = dlg;
      languageKey = new String(dialogBase.getLanguage());
      title = new JLabel(aTitle);
      title.setFont(dialogBase.standardFont);
      //title.setForeground(Color.red);
      title.setHorizontalAlignment(SwingConstants.CENTER);
      LayoutPanel = new BorderLayout();

      this.setLayout(LayoutPanel);
      JPanel panelTitle = new JPanel(new BorderLayout());
      panelTitle.add(Box.createVerticalStrut(10),BorderLayout.NORTH);
      panelTitle.add(title,BorderLayout.SOUTH);
      this.add(panelTitle,BorderLayout.NORTH);

      /*ajout d'espace a gauche, a droite et en bas du panel*/
      this.add(Box.createVerticalStrut(20),BorderLayout.SOUTH);
      this.add(Box.createHorizontalStrut(20), BorderLayout.EAST);
      this.add(Box.createHorizontalStrut(20), BorderLayout.WEST);

      /*récupération de la connexion a la servlet*/
      connection = dialogBase.getConnection();
    }
    catch(Exception e)
    {
       dialogBase.traceError( "PanelDB", e.toString());
       throw new IfnException(getString(CV_MESSAGE,CV_CHARGEMENT_IMPOSSIBLE)
        + " " +  title);
    }

  }
  /****************************************************************************/
  /* Constructeur par defaut:                                                 */
  /* utilise pour JBuiler                                                     */
  /****************************************************************************/
  public PanelDB() throws IfnException
  {
    dialogBase = null;
    languageKey = "FR";
    try
    {
      title = new JLabel("no title");
      title.setForeground(Color.red);
      title.setHorizontalAlignment(SwingConstants.CENTER);
      LayoutPanel = new BorderLayout();

      this.setLayout(LayoutPanel);
      this.add(title,BorderLayout.NORTH);

      /*ajout d'espace a gauche, a droite et en bas du panel*/
      this.add(Box.createVerticalStrut(10),BorderLayout.SOUTH);
      this.add(Box.createHorizontalStrut(10), BorderLayout.EAST);
      this.add(Box.createHorizontalStrut(10), BorderLayout.WEST);
    }
    catch(Exception e)
    {
       throw new IfnException(getString(CV_MESSAGE,CV_CHARGEMENT_IMPOSSIBLE)
        + " " +  title);
    }

  }

  /****************************************************************************/
  /* Impression du contenu du DB panel (surchargee dans les classes qui en    */
  /* heritent                                                                 */
  /****************************************************************************/
  public int  printInformation(Graphics pg, PageFormat pf,int xPage,int yPage,
   int wPage,int hPage, int pageIndex ) throws PrinterException
  {
    /*A developper dans les classes qui herite de cette classe*/
    return Printable.NO_SUCH_PAGE;
  }

  /****************************************************************************/
  /* Mise a jour du panel en fonction de la requete passee en parametre       */
  /****************************************************************************/
  public void RefreshPanelWithQuery(String queryToProcess)
    throws IfnException
  {
    /*A developper dans les classes qui herite de cette classe*/
  }

  /****************************************************************************/
  /* Recuperation de la phrase traduite dans la langue courante de l'ecran    */
  /* en fonction de la clef passee en parametre (nature et objet)             */
  /****************************************************************************/
  public String getString(String in_nature, String in_objet)
  {
    return dialogBase.getString(in_nature, in_objet);
  }

  /****************************************************************************/
  /* Affichage du status dans la boite de dialogue d'appartenenace            */
  /****************************************************************************/
   public void showStatus(String status)
  {
    dialogBase.showStatus(status);
  }

  /****************************************************************************/
  /*Trace dans un fichier                                                    */
  /****************************************************************************/
  public void traceError(String method, String message)
  {
    dialogBase.traceError(method, message);
  }
  /****************************************************************************/
  /*Trace dans un fichier                                                    */
  /****************************************************************************/
  public void traceInfo(String method, String message)
  {
    dialogBase.traceInfo(method, message);
  }



}