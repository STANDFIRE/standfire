/*
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 *
 * Copyright (C) 2000-2003  Francois de Coligny
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


 package capsis.extension.dataextractor;

 import capsis.util.methodprovider.ShannonGProvider;
 import capsis.extension.dataextractor.DETimeY;
 import capsis.kernel.GModel;
 import capsis.kernel.GScene;
 import capsis.kernel.MethodProvider;
 import jeeb.lib.util.*;
 import capsis.kernel.*;
 import capsis.defaulttype.*;

 /**
  * Shannon Index on Basal Areas per DBH class, versus Date.
  *
  * @author T.Cordonnier, V. Lafond - June 2012
  */
 public class DETimeShannonG extends DETimeY {

     static {
         Translator.addBundle("capsis.extension.dataextractor.DETimeShannonG");
     }

     public static final String NAME = "DETimeShannonG";
     public static final String DESCRIPTION = "Calculate Shannon Index Over Time";
     public static final String AUTHOR = "T.Cordonnier, V. Lafond";
     public static final String VERSION = "1.1";


     static public boolean matchWith(Object referent) {
         if (!(referent instanceof GModel)) {return false;}
         GModel m = ((GModel)referent);
         MethodProvider p = m.getMethodProvider();
         if (!(p instanceof ShannonGProvider))return false;
         GScene s = ((Step) m.getProject().getRoot()).getScene();
         return s instanceof TreeList;
     }

     @Override
     protected Number getValue(GModel m, GScene stand, int date) {
         	ShannonGProvider p = (ShannonGProvider)m.getMethodProvider();
         	TreeList tl = (TreeList) stand;
         	double g = p.getShannonG(stand,tl.getTrees());
         	return g; // return ShannonG

     }

}

