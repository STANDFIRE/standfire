/*
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 *
 * Copyright (C) 2000-2001  Francois de Coligny
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package capsis.extension.modeltool.rockfornet;


import capsis.util.methodprovider.RockfornetStand;

/**
 * Rockfornet simulator
 *
 * @author Eric Mermin, Eric Maldonado - november 2006
 */
public class RockfornetSettings {

	// fc - 5.7.2007 - needed to avoid fr / en translations problems
	public static final int DISC = 1;
	public static final int RECTANGULAR = 2;
	public static final int ELLIPSOID = 3;
	public static final int SPHERE = 4;
	
	public RockfornetStand stand;
	//public SamsaStand stand;
	
	public int rockType;
	public int rockShape;	// fc - 5.7.2007 - was String, now DISC, RECTANGULAR, ELLIPSOID, SPHERE
	
	public double slope;
		
	public double lengthNonForestedSlope;
	public double lengthForestedSlope;
	public double heightCliff;
	public double rockDiameter1;
	public double rockDiameter2;
	public double rockDiameter3;
	public double meanStandDensity;
	public double standBasalArea_m2;
	public double meanStemDbh_cm;
	public double percentage[];
	
	public RockfornetSettings(RockfornetStand stand){
		this.stand=stand;
		double sum=0;
		System.out.println(stand.getStandBasalArea_m2());
		double a[]=stand.getSpeciesDistribution ();
		percentage=new double [10];
		int i;
		// calculate the number of trees
		for (i=0;i<10;i++){
			sum+=a[i];
			System.out.println("Species'" +  new Double(a[i]).toString());
		
			}
		
		// calculate the number of trees
		for (i=0;i<10;i++)
			percentage[i]=a[i]/sum*100;		
		
		//percentage [0]=50;
		//percentage [1]=50;
		standBasalArea_m2=0;
		meanStemDbh_cm=0;
		if (stand.getMode ()==stand.STAND_BASAL_AREA)
			standBasalArea_m2=stand.getStandBasalArea_m2 ();
		else
			meanStemDbh_cm=stand.getMeanStemDbh_cm ();
			
		System.out.println(standBasalArea_m2);
		meanStandDensity=stand.getMeanStandDensity ();
		
	}
	
	public String toString () {
		StringBuffer b = new StringBuffer ();
		b.append ("RockfornetSettings: ");
		b.append ("\n");
		b.append ("stand="+stand);
		b.append ("\n");
		b.append ("rockType="+rockType);
		b.append ("\n");
		b.append ("rockShape="+rockShape);
		b.append ("\n");
		b.append ("slope="+slope);
		b.append ("\n");
		b.append ("lengthNonForestedSlope="+lengthNonForestedSlope);
		b.append ("\n");
		b.append ("lengthForestedSlope="+lengthForestedSlope);
		b.append ("\n");
		b.append ("heightCliff="+heightCliff);
		b.append ("\n");
		b.append ("rockDiameter1="+rockDiameter1);
		b.append ("\n");
		b.append ("rockDiameter2="+rockDiameter2);
		b.append ("\n");
		b.append ("rockDiameter3="+rockDiameter3);
		b.append ("\n");
		b.append ("meanStandDensity="+meanStandDensity);
		b.append ("\n");
		b.append ("standBasalArea_m2="+standBasalArea_m2);
		b.append ("\n");
		b.append ("meanStemDbh_cm="+meanStemDbh_cm);
		b.append ("\n");
		b.append ("percentage=[");
		for (int i = 0; i < percentage.length; i++) {
			b.append (percentage[i]);
			b.append (" ");
		}
		b.append ("]");
		b.append ("\n");
		
		
		
		
		return b.toString ();
	}
}


