/*
 * This file is part of the LERFoB modules for Capsis4.
 *
 * Copyright (C) 2009-2014 UMR 1092 (AgroParisTech/INRA) 
 * Contributors Jean-Francois Dhote, Patrick Vallet,
 * Jean-Daniel Bontemps, Fleur Longuetaud, Frederic Mothe,
 * Laurent Saint-Andre, Ingrid Seynave, Mathieu Fortin.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package capsis.extension.intervener;

import java.security.InvalidParameterException;

import repicea.simulation.covariateproviders.standlevel.AreaHaProvider;
import repicea.simulation.covariateproviders.standlevel.MeanQuadraticDiameterCmProvider;
import capsis.lib.lerfobutil.Dendro;

/**
 * The RdiAutoThinnerStandData contains a summary of the stand characteristics.
 * @author F. Mothe, G. LeMoguedec - May 2010
 * @author Mathieu Fortin (refactoring) - May 2014
 */
public class RdiAutoThinnerStandData implements AreaHaProvider, MeanQuadraticDiameterCmProvider {
	
    private double N;
    private double G_m2;
    private double Dg_cm;
    private double rdi;
    private final double areaHa;
    private double coppiceBasalAreaM2;
    private double thinningCoefficientKg = 0;

//    public RdiAutoThinnerStandData(double areaHa) {
//        this.areaHa = areaHa;
//    }

//    protected RdiAutoThinnerStandData(RdiAutoThinnerStandData data) {
//        this.N = data.N;
//        this.G_m2 = data.G_m2;
//        this.Dg_cm = data.Dg_cm;
//        this.rdi = data.rdi;
//        this.areaHa = data.areaHa;
//    }
    
    
    protected RdiAutoThinnerStandData(double N, double G_m2, double Dg_cm, double rdi, double areaHa) {
        this.N = N;
        this.G_m2 = G_m2;
        this.Dg_cm = Dg_cm;
        this.rdi = rdi;
        this.areaHa = areaHa;
    }

    
    
    public RdiAutoThinnerStandData getDifference(RdiAutoThinnerStandData initialStandData) {
    	if (initialStandData.areaHa != this.areaHa) {
    		throw new InvalidParameterException("The two instances of RdiAutoThinnerStandData do not share the same area per hectare!");
    	}
    	RdiAutoThinnerStandData differenceStand = new RdiAutoThinnerStandData(initialStandData.N - N,
    			initialStandData.G_m2 - G_m2,
    			Dendro.calcDg_cm(initialStandData.G_m2 - G_m2, initialStandData.N - N),
    			initialStandData.rdi - rdi, 	// TODO is this relevant ??
    			areaHa);
		differenceStand.thinningCoefficientKg = Dendro.calcKg(initialStandData.G_m2, initialStandData.N, G_m2, N);
		differenceStand.coppiceBasalAreaM2 = initialStandData.getCoppiceBasalAreaM2() - coppiceBasalAreaM2;
    	return differenceStand;
    }
    
    
    protected double getThinningCoefficientKg() {return thinningCoefficientKg;}
    
    public double getRDI() {return rdi;}
    protected void setRDI(double rdi) {this.rdi = rdi;}
    
    @Override
    public double getAreaHa() {return areaHa;}
    
    public double getN() {return N;}
    protected void setN(double N) {this.N = N;}
    
    public double getBasalAreaM2() {return G_m2;}
    protected void setBasalAreaM2(double basalAreaM2) {this.G_m2 = basalAreaM2;}

	@Override
	public double getMeanQuadraticDiameterCm() {return Dg_cm;}
    protected void setMeanQuadraticDiameterCm(double mdqCm) {this.Dg_cm = mdqCm;}


    protected void setCoppiceBasalAreaM2(double coppiceBasalAreaM2) {this.coppiceBasalAreaM2 = coppiceBasalAreaM2;}
    protected double getCoppiceBasalAreaM2() {return coppiceBasalAreaM2;}
//    /**
//     * For debug output
//     */
//    public String histoToString() {
//        String s = "";
//        for (int n = 0; n < histo.length; ++n) {
//            s += "\t" + histo[n];
//        }
//        return s;
//    }

}
