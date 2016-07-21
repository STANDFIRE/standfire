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

import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JPanel;


public class PanelGridbag extends JPanel implements IfnConstante
{
  // Déclaration de variables
  /*ATTENTION le gridbagconstraint est remanent entre 2 appel de la methode*/
  /*addComponent*/
  GridBagConstraints gbc ;
  static final int CV_PANEL_GRIDBAG_SPACE = 5;
  int height;

  public DialogBase dialogBase;
  String title;

  Component objet ;
  // Déclaration du gridbag pour le panel
  public static GridBagLayout gridBagLayout1 = new GridBagLayout();

/*------------------------------------------------------------------*/
/*                         Constructeur                             */
/*------------------------------------------------------------------*/
  public PanelGridbag()
  {
    super();
    this.setFont(new Font("Courrier new",1,12));
    //this.setBorder(BorderFactory.createEtchedBorder());
    // Affectation du gridBagLayout au panel
    this.setLayout(gridBagLayout1);
  }

  public PanelGridbag(DialogBase in_dlg, String in_title)throws IfnException
  {
    super();
    dialogBase    = in_dlg;
    title         = in_title;
    try
    {
      height = CV_PANEL_GRIDBAG_SPACE;
      gbc= new GridBagConstraints();

      // Nombre de colonnes occupées par le composant
      gbc.gridwidth =1;
      // Nombre de lignes occupées par le composant
      gbc.weightx = 1;
      gbc.weighty = 1;
      // Ancrage du composant (alignement)
      gbc.anchor = gbc.WEST;
      // Remplissage
      gbc.fill = gbc.NONE;
      // Marge par rapport à la cellule
      gbc.insets = new Insets(CV_PANEL_GRIDBAG_SPACE,CV_PANEL_GRIDBAG_SPACE,
        CV_PANEL_GRIDBAG_SPACE,CV_PANEL_GRIDBAG_SPACE);

      this.setFont(new Font("Courrier new",1,12));
      //this.setBorder(BorderFactory.createEtchedBorder());
      // Affectation du gridBagLayout au panel
      this.setLayout(gridBagLayout1);

    }
    catch(Exception e)
    {
       dialogBase.traceError( "PanelGridBag", e.toString());
       throw new IfnException(dialogBase.getString(CV_MESSAGE,CV_CHARGEMENT_IMPOSSIBLE)
        + " " +  title);
    }
  }


/*------------------------------------------------------------------*/
/*             Ajout d'un composant au panel                        */
/*------------------------------------------------------------------*/
  public void addComponent (Component in_compo, GridBagConstraints in_gbc,
                                   int in_colonne, int in_ligne)
  {
    // Récupération des contraintes
    gbc = in_gbc ;
    // Récupération de l'objet à insérer
    objet = in_compo ;

    // Choix de la cellule
    gbc.gridx = in_colonne ;
    gbc.gridy = in_ligne ;

    // Définition des contraintes pour l'objet et ajout de l'objet
    gridBagLayout1.setConstraints(objet, gbc);
    this.add(objet);
  }
  public void addComponent (Component in_compo,
                                   int in_colonne, int in_ligne, int in_lencart,
                                   int in_rencart, double in_xpoids)
  {
        // Nombre de lignes occupées par le composant
        gbc.weightx = in_xpoids;
        // Marge par rapport à la cellule
        gbc.insets = new Insets(height,in_lencart,height,in_rencart);

        addComponent(in_compo, gbc,in_colonne, in_ligne);
  }

  public void addComponent (Component in_compo,
                                   int in_colonne, int in_ligne, int in_lencart,
                                   int in_rencart, double in_xpoids,int in_anchor)
  {
    setAnchor(in_anchor);
    addComponent (in_compo,in_colonne, in_ligne, in_lencart,in_rencart,
                  in_xpoids);
  }


/*------------------------------------------------------------------*/
/*             Fixe le type d'ancrage                               */
/*------------------------------------------------------------------*/
  public void setAnchor(int in_anchor)
  {
    gbc.anchor = in_anchor;
  }
/*------------------------------------------------------------------*/
/*             Fixe le type remplissage                             */
/*------------------------------------------------------------------*/
  public void setFill(int in_fill)
  {
    gbc.fill = in_fill;
  }

/*------------------------------------------------------------------*/
/*             Fixe le poids en Y                                   */
/*------------------------------------------------------------------*/
  public void setHeight(int in_height)
  {
    height = in_height;
  }

/*------------------------------------------------------------------*/
/*             Fixe le poids en X                                   */
/*------------------------------------------------------------------*/
  public void setWeightX(double in_weight)
  {
    gbc.weightx = in_weight;
  }
/*------------------------------------------------------------------*/
/*             Fixe le poids en Y                                   */
/*------------------------------------------------------------------*/
  public void setWeightY(double in_weight)
  {
    gbc.weighty = in_weight;
  }

/*------------------------------------------------------------------*/
/*             Fixe le nombre de cellule a utiliser                 */
/*------------------------------------------------------------------*/
  public void setGridWidth(int in_width)
  {
    gbc.gridwidth = in_width;
  }

}

