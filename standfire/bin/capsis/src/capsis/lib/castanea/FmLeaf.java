package capsis.lib.castanea;

import java.io.Serializable;
import jeeb.lib.util.Log;

/**	FmLeaf : leaves of Dynaclim model.
*
*	@author Hendrik Davi - october 2009
*/
public class FmLeaf implements Serializable, Cloneable {

	// WARNING: if references to objects (not primitive types) are added here,
	// implement a "public Object clone ()" method (see RectangularPlot.clone () for template)

    private FmCanopyLayer layer;
	private double netPhotosynthesis;
	private double grossPhotosynthesis;
	private double respiration;
	private double waterConductance;
	private double co2Conductance;
	private double delta13C;
	private double vcmax0;
	private double vjmax0;
	private double HaVcmax;
	private double HdVcmax;
	private double deltaVcmax;
	private double HaVjmax;
	private double HdVjmax;
	private double deltaVjmax;

	private FmSettings settings; // fc-11.6.2014 for logPrefix

	/**	Constructor for new logical FmCanopyLayer.
	*/
	public FmLeaf (
			FmCanopyLayer layer,
			double netPhotosynthesis,
			double grossPhotosynthesis,
			double respiration,
			double waterConductance,
			double co2Conductance,
			double delta13C,
			double vcmax0,
			double vjmax0,
			double HaVcmax,
			double HdVcmax,
			double deltaVcmax,
			double HaVjmax,
			double HdVjmax,
			double deltaVjmax,
			FmSettings settings) {

			this.layer = layer;
			this.netPhotosynthesis = netPhotosynthesis;
			this.delta13C = delta13C;
			this.grossPhotosynthesis = grossPhotosynthesis;
			this.respiration = respiration;
			this.waterConductance = waterConductance;
			this.co2Conductance = co2Conductance;
			this.delta13C= delta13C;
			this.vcmax0= vcmax0;
			this.vjmax0= vjmax0;
			this.HaVcmax= HaVcmax;
			this.HdVcmax=HdVcmax ;
			this.deltaVcmax=deltaVcmax;
			this.HaVjmax=deltaVcmax;
			this.HdVjmax=HdVjmax;
			this.deltaVjmax=deltaVjmax;

			this.settings = settings; // fc-11.6.2014 for logPrefix
	}

	//*******************************************************************************************************
			/**	processPhotosynthesis: Calculate half hourly leaf gross photosynthesis from Baldocchi's analytic version// Dufr?ne et al., 2005
				using LMALeaves, nitrogen, half hourly absorbed light, temperature and relative humidity.
			Returns netPhotosynthesis, grossPhotosynthesis, respiration, waterConductance, co2Conductance */

	public void calculateFluxes (
			FmSettings fm,
			FmSpecies species,
			FmCell cell,
			double NLAI,
			double T,
			double RH,
			double PARsun,
			double PARdif,
			double Propsun,
			double Propshad,
			double rb,
			double g1,
			int sp) {


		    double [] fluxes = new double [5];
//		    double netPhotosynthesis=0;
//		    double grossPhotosynthesis=0;
		    double RDhLAI=0;
//		    double waterConductance=0;
//		    double co2Conductance=0;


			double Ca= fm.Ca;
			double x1= 0;
			double x2= 0;
			double x3= 0;
			double TT= 0;

			double gb= (1/rb)/(22.4* (T+273)/273/1000);

			double PBhLAIdr= 0;
			double PBhLAIdf= 0;
			double gsLAIdr= 1000*species.g0;
			double gsLAIdf= 1000*species.g0;
			double PNhLAIdr=-RDhLAI;
			double PNhLAIdf=-RDhLAI;
			double Csdir= Ca-PNhLAIdr/gb;
			double Csdif= Ca-PNhLAIdf/gb;
			double gscLAIdr= gsLAIdr/1.6;
			double gscLAIdf= gsLAIdf/1.6;
//			double VCmax0=0;
			//double sumP= 0;
			//double sumN= 0;
			double Lnew=0;
			double photoEvergreenCorrection=0;
			double VCmax=0;
			double VJmax=0;
			double V0max=0;
			FmSoil soil= cell.getSoil();

			if (species.decidu==1) {

				if (settings.vcmaxStress) {
						this.vcmax0= cell.getNccell()[sp]*NLAI*soil.getStomatalControl();
					}else{
						this.vcmax0= cell.getNccell()[sp]*NLAI;
					}

			} else {


				int cohortesOfLeaves= (int)species.cohortesOfLeaves;

				FmCanopyEvergreen canopyEvergreen = cell.getCanopy ().getCanopyEvergreen ();
				double [] photoEvergreen= canopyEvergreen.getPhotoEvergreen(cell, fm, species,cohortesOfLeaves);

				for (int k = 0; k < cohortesOfLeaves; k++) {
					Lnew= Lnew+ canopyEvergreen.getLy(k);
					//sumP=sumP+canopyEvergreen.getLy(k)*canopyEvergreen.getCoefOfLeafMass(k)*canopyEvergreen.getCoefOfLeafNitrogen(k)*canopyEvergreen.getCoefOfPhotosynthesis(k);
					//sumN=sumN+canopyEvergreen.getLy(k)*canopyEvergreen.getCoefOfLeafMass(k)*canopyEvergreen.getCoefOfLeafNitrogen(k);
					// mass already taken into account in NLAI
					photoEvergreenCorrection= photoEvergreenCorrection+photoEvergreen[k]*canopyEvergreen.getLy(k);

					//sumP=sumP+canopyEvergreen.getLy(k)*canopyEvergreen.getCoefOfLeafNitrogen(k)*canopyEvergreen.getCoefOfPhotosynthesis(k);
					//sumN=sumN+canopyEvergreen.getLy(k)*canopyEvergreen.getCoefOfLeafNitrogen(k);
				}

				photoEvergreenCorrection= photoEvergreenCorrection/Lnew;

//				VCmax0= species.NC*NLAI*sumP/Lnew;
				if (settings.vcmaxStress) {
						vcmax0= cell.getNccell()[sp]*NLAI*photoEvergreenCorrection*soil.getStomatalControl();
					}else{
						vcmax0= cell.getNccell()[sp]*NLAI*photoEvergreenCorrection;
					}
			}
			this.vjmax0= vcmax0*fm.coefbeta;

			// Log.println(settings.logPrefix+"understanding2", vcmax0+";"+ vjmax0+";"+NLAI);


			RDhLAI = this.getLeafRespiration(fm, species,cell, NLAI, T);


			  // temperature effect on photosynthetic variables following Bernacchi 2001 or dreyer with arrhenius equation
			double Kc= Math.exp(fm.cKc-fm.EaKc/((T+273)*8.314));
			double Ko= Math.exp(fm.cKo-fm.EaKo/((T+273)*8.314));
			double gama= Math.exp(fm.cgama-fm.Eagama/((T+273)*8.314))/42.75*fm.Kc0*0.21*fm.Oi0/2/fm.Ko0;

				 // following Long (1991)
			double Oi= (fm.Oi0/ 0.026934)*(0.047- 0.0013087*T+ 2.5603*Math.pow(0.1,5)*T*T- 2.1441*Math.pow(0.1,7)*Math.pow(T,3));
			double fci= (1/ 0.73547)*(1.674- 0.061294*T+0.0011688*(T*T)- 8.8741*Math.pow(0.1,6)*Math.pow(T,3));


			if (fm.temperatureEffectOnPhotosynthesis=="Bernacchi") {

				double kt1= Math.exp(fm.cVc-fm.EaVc/((T+273)*8.314));
				double kt2= Math.exp(fm.cVo-fm.EaVo/((T+273)*8.314));
				double kt3= Math.exp(species.EaVJ*(T-25)/(298* 8.314* (T+273)));
				double kt4= Math.exp((species.ETT*(T+273)-species.JMT)/(8.314*(T+273)));


				VCmax= vcmax0*kt1;
				V0max= 0.21*vcmax0*kt2;
				VJmax= vjmax0*kt3/(1+kt4);
			}

			if (fm.temperatureEffectOnPhotosynthesis=="Arrhenius") {

				this.setSpeciesArrheniusParemeters(species);
				VCmax= vcmax0*Math.exp((HaVcmax/(8.314*(273+25)))*(1-((25+273)/(T +273))))*(1+Math.exp((deltaVcmax*(273+25)-HdVcmax)/(8.314*(273+25))))/(1+Math.exp((deltaVcmax*(273+ T)-HdVcmax)/(8.314*(273+T))));
				VJmax= vjmax0*Math.exp((HaVjmax/(8.314*(273+25)))*(1-((25+273)/(T +273))))*(1+Math.exp((deltaVjmax*(273+25)-HdVjmax)/(8.314*(273+25))))/(1+Math.exp((deltaVjmax*(273+ T)-HdVjmax)/(8.314*(273+ T))));
			}

			 // test on light
			if (PARsun + PARdif <= 0) {
			 	PBhLAIdr= 0;
				PBhLAIdf= 0;
				gsLAIdr= 1000*species.g0;
				gsLAIdf= 1000*species.g0;
				PNhLAIdr=-RDhLAI;
				PNhLAIdf=-RDhLAI;
				Csdir= Ca-PNhLAIdr/gb;
				Csdif= Ca-PNhLAIdf/gb;
				gscLAIdr= gsLAIdr/1.6;
		  	} else {
				double alpha2= 1+species.g0/1.6/gb-g1/1.6*RH/100;
				double beta2= Ca*(gb*g1/1.6*RH/100-2*species.g0/1.6 - gb);
				double gama2= species.g0/1.6*gb*Math.pow(Ca,2);
				double teta2= gb*g1/1.6*RH/100-species.g0/1.6;


				  // when there is strong water stress, g1 and RH small, avo?d the algorithm to unconverged
				if (alpha2>-0.2) {
					  alpha2= -0.2;
				}

				 // calculation for sunlit leaves
				 // PN1 when electron transport (light) is limitant
				double VJdir= 1/(2*species.teta)*(fm.rdtq*(PARsun+PARdif) + VJmax-Math.sqrt((Math.pow(fm.rdtq*(PARsun+PARdif)+VJmax,2))-4*species.teta*fm.rdtq*(PARsun+ PARdif)*VJmax));

				double aa= VJdir*fci;
				double bb= 8*gama;
				double ee= 4*fci;
				double dd= gama/fci;

				double p=(ee*beta2 + bb*teta2 - aa*alpha2 + ee*alpha2*RDhLAI)/(ee*alpha2);
				double q=(ee*gama2 + bb*gama2/Ca -aa*beta2 +aa*dd*teta2 + ee*RDhLAI*beta2 + RDhLAI*bb*teta2)/(ee*alpha2);
				double r=(- aa*gama2 +aa*dd*gama2/Ca + ee*RDhLAI* gama2 + RDhLAI*bb*gama2/Ca)/(ee*alpha2);

				double QQ= (Math.pow(p,2)-3*q)/9;
				double RR= (2*Math.pow(p,3)-9*p*q+27*r)/54;

				if (QQ<0 || Math.abs(RR/Math.pow(QQ,1.5))>1) {
					x1=  999;
					x2=  999;
					x3=  999;
				} else {

					TT= Math.acos(RR/Math.pow(QQ,1.5));
					x1=  -2*Math.sqrt(QQ)*Math.cos(TT/3)-p/3;
					x2=  -2*Math.sqrt(QQ)*Math.cos((TT+2*Math.PI)/3)-p/3;
					x3=  -2*Math.sqrt(QQ)*Math.cos((TT+4*Math.PI)/3)-p/3;
				}

				double PN1= x3;


				// PN2 when carboxylation is limitant

				aa= VCmax*fci;
				bb= Kc*(1+Oi/Ko);
				ee= fci;
				dd= gama/fci;

//			Log.println(settings.logPrefix+"DynaclimTest", "VJdir="+ VJdir+" aa= "+aa+" bb= "+bb+ " ee="+ee+" dd="+dd );


				p= (ee*beta2 + bb*teta2 - aa*alpha2 + ee*alpha2*RDhLAI)/(ee*alpha2);
				q= (ee*gama2 + bb*gama2/Ca -aa*beta2 +aa*dd*teta2 + ee*RDhLAI*beta2 + RDhLAI*bb*teta2)/(ee*alpha2);
				r= (- aa*gama2 +aa*dd*gama2/Ca + ee*RDhLAI* gama2 + RDhLAI*bb*gama2/Ca)/(ee*alpha2);

				QQ= (Math.pow(p,2)-3*q)/9;
				RR= (2*Math.pow(p,3)-9*p*q+27*r)/54;

				if (QQ<0 || Math.abs(RR/Math.pow(QQ,1.5))>1) {
				 	x1=  999;
					x2=  999;
					x3=  999;

				} else {

					TT= Math.acos(RR/Math.pow(QQ,1.5));
					x1=  -2*Math.sqrt(QQ)*Math.cos(TT/3)-p/3;
					x2=  -2*Math.sqrt(QQ)*Math.cos((TT+2*Math.PI)/3)-p/3;
					x3=  -2*Math.sqrt(QQ)*Math.cos((TT+4*Math.PI)/3)-p/3;
				}

				double PN2=x3;

				PNhLAIdr= Math.min(PN1,PN2);
				if (PNhLAIdr==999 || PNhLAIdr<-RDhLAI) {
					PNhLAIdr=-RDhLAI;
				}
				Csdir= Ca-PNhLAIdr/gb;
				gsLAIdr= 1000*species.g0+ 1000*g1*PNhLAIdr*RH/(100*Csdir);
				if (PNhLAIdr==-RDhLAI) {
					gsLAIdr= 1000*species.g0;
				}
				gscLAIdr= gsLAIdr/1.6;
				PBhLAIdr=PNhLAIdr+RDhLAI;

				// calculation for shade leaves
				// PN1 when electron transport (light) is limitant
				double VJdif=(1/(2*species.teta))*(fm.rdtq*PARdif + VJmax- Math.sqrt((Math.pow(fm.rdtq*PARdif+VJmax,2))- 4*species.teta*fm.rdtq*PARdif*VJmax));

	//			Log.println(settings.logPrefix+"DynaclimTest", "VJdir="+ VJdir+" gama= "+gama+"VJdif= "+VJdif+ " PARsun="+PARsun+" PARdif="+PARdif);


				aa= VJdif*fci;
				bb= 8*gama;
				ee= 4*fci;
				dd= gama/fci;

				p= (ee*beta2 + bb*teta2 - aa*alpha2 + ee*alpha2*RDhLAI)/(ee*alpha2);
				q= (ee*gama2 + bb*gama2/Ca -aa*beta2 +aa*dd*teta2 + ee*RDhLAI*beta2 + RDhLAI*bb*teta2)/(ee*alpha2);
				r= (-aa*gama2 +aa*dd*gama2/Ca + ee*RDhLAI* gama2 + RDhLAI*bb*gama2/Ca)/(ee*alpha2);

				QQ= (Math.pow(p,2)-3*q)/9;
				RR= (2*Math.pow(p,3)-9*p*q+27*r)/54;

			if (QQ<0 || Math.abs(RR/Math.pow(QQ,1.5))>1) {
					x1=  999;
					x2=  999;
					x3=  999;

				} else {

					TT= Math.acos(RR/Math.pow(QQ,1.5));

					x1=  -2*Math.sqrt(QQ)*Math.cos(TT/3)-p/3;
					x2=  -2*Math.sqrt(QQ)*Math.cos((TT+2*Math.PI)/3)-p/3;
					x3=  -2*Math.sqrt(QQ)*Math.cos((TT+4*Math.PI)/3)-p/3;
				}

				PN1=x3;

				// PN2 when carboxylation is limitant

				aa= VCmax*fci;
				bb= Kc*(1+Oi/Ko);
				ee= fci;
				dd= gama/fci;

				p= (ee*beta2 + bb*teta2 - aa*alpha2 + ee*alpha2*RDhLAI)/(ee*alpha2);
				q= (ee*gama2 + bb*gama2/Ca -aa*beta2 +aa*dd*teta2 + ee*RDhLAI*beta2 + RDhLAI*bb*teta2)/(ee*alpha2);
				r= (-aa*gama2 +aa*dd*gama2/Ca + ee*RDhLAI* gama2 + RDhLAI*bb*gama2/Ca)/(ee*alpha2);

				QQ= (Math.pow(p,2)-3*q)/9;
				RR= (2*Math.pow(p,3)-9*p*q+27*r)/54;

				if (QQ<0 || Math.abs(RR/Math.pow(QQ,1.5))>1) {
					x1=  999;
					x2=  999;
					x3=  999;
				} else {

					TT= Math.acos(RR/Math.pow(QQ,1.5));

					x1=  -2*Math.sqrt(QQ)*Math.cos(TT/3)-p/3;
					x2=  -2*Math.sqrt(QQ)*Math.cos((TT+2*Math.PI)/3)-p/3;
					x3=  -2*Math.sqrt(QQ)*Math.cos((TT+4*Math.PI)/3)-p/3;
			 	}

				PN2=x3;

				PNhLAIdf= Math.min(PN1,PN2);
				if (PNhLAIdf<-RDhLAI || PNhLAIdf==999) {
					PNhLAIdf=-RDhLAI;
				}
				Csdif= Ca-PNhLAIdf/gb;
				gsLAIdf= 1000*species.g0+1000*g1*PNhLAIdf*RH/(100*Csdif);
				if (PNhLAIdf==-RDhLAI) {
					gsLAIdf= 1000*species.g0;
				}
				gscLAIdf= gsLAIdf/1.6;
				PBhLAIdf=PNhLAIdf+RDhLAI;


		  }

			// Photosynthesis of sun and shade leaves

			grossPhotosynthesis = Math.max((PBhLAIdr*Propsun + PBhLAIdf* Propshad),0);
			netPhotosynthesis = grossPhotosynthesis - RDhLAI;
			co2Conductance = Math.max((gscLAIdr*Propsun + gscLAIdf*Propshad), 1000*species.g0/1.6);
			waterConductance= Math.max((gsLAIdr*Propsun + gsLAIdf*Propshad), 1000*species.g0);

			double Cidif= Csdif- PNhLAIdf* (1000/ gscLAIdf);
			double Cidir= Csdir- PNhLAIdr* (1000/ gscLAIdr);
			double Cistrat=(Cidir*Propsun + Cidif*Propshad);
			double Qd= Cistrat/Ca*(27-4.4)+4.4;

			delta13C= Qd;


	//		Log.println(settings.logPrefix+"DynaclimTest", "grossPhotosynthesis="+ grossPhotosynthesis+" waterConductance= "+waterConductance);

			respiration = RDhLAI;


			if (settings.output > 3) {
			        Log.println(settings.logPrefix+"photosynthesis", grossPhotosynthesis+";"+netPhotosynthesis+";"+waterConductance+";"+RDhLAI+";"+NLAI+";"+photoEvergreenCorrection);
			}

//			fluxes[0]= netPhotosynthesis;
//			fluxes[1]=	grossPhotosynthesis;
//			fluxes[2]= RDhLAI;
//			fluxes[3]= waterConductance;
//			fluxes[4]= co2Conductance;
//
//			return fluxes;

}// end of processLeaf



/**	Clone method.

public Object clone () {
	try {
		FmCanopyLayer l = (FmCanopyLayer) super.clone ();	// calls protected Object Object.clone () {}

		l.LMA = null;

		l.photosynthesis = null;

		l.respiration = null;

		return l;
	} catch (CloneNotSupportedException e) {
		Log.println (Log.ERROR, "Fmleaf.clone ()",
				"Error while cloning "
				+" "+e.toString (), e);
		return null;
	}
}
*/

	//*******************************************************************************************************
	// processLeavesRespiration: Calculate half hourly leaf from Dufr?ne et al., 2005 modified by Davi 2008 to account for multispecific species

	public double getLeafRespiration(FmSettings settings,
								FmSpecies species,
								FmCell cell,
								double NLAI,
								double T) {

		double RDhLAI=0;

		if (species.decidu==1 ) { //|| sp.castaneaCode==1
			 RDhLAI= settings.tc* NLAI* (1000000/ 3600)*settings.MRN*Math.pow(settings.QDIX,((T- settings.Tbase)/10));
	 	} else {

			//int cohortesOfLeaves= (int)sp.cohortesOfLeaves;
			//FmCanopyEvergreen canopyEvergreen = cell.getCanopy ().getCanopyEvergreen ();
			//for (int k = 0; k < cohortesOfLeaves; k++) {
			//	Lnew= Lnew+ canopyEvergreen.getLy(k);
		  	//}
			//NLAI nitrogen leaf/needle for current year
			RDhLAI= settings.tc* NLAI* (1000000/ 3600)*settings.MRN*Math.pow(settings.QDIX,((T- settings.Tbase)/10));

		}



		 return RDhLAI;
	}

	public void setSpeciesArrheniusParemeters(FmSpecies species) {
	// see also Temperature response of parameters of a biochemically based model of photosynthesis. II. A review of experimental data

		if (species.castaneaCode==1) {


		}
		if (species.castaneaCode==2) {


		}
		if (species.castaneaCode==3) {
			// data for Q Petraea seedlings...
			this.HaVcmax= 67.6*1000;
			this.HdVcmax=144;
			this.deltaVcmax=451;
			this.HaVjmax=46.1*1000;
			this.HdVjmax=280;
			this.deltaVjmax=888;

		}
		if (species.castaneaCode==4) {
			this.HaVcmax= 75.4*1000;
			this.HdVcmax=175;
			this.deltaVcmax=559;
			this.HaVjmax=65.3*1000;
			this.HdVjmax=129;
			this.deltaVjmax=420;

		}
		if (species.castaneaCode==5) {


		}
		if (species.castaneaCode==7) {
			// Piotr Robakowski, Pierre Montpied and Erwin Dreyer
			// Temperature response of photosynthesis of silver fir (Abies alba Mill.) seedlings
			// Ann. For. Sci. 59 (2002) 163?170
			this.HaVcmax= 56.2*1000;
			this.HdVcmax=272;
			this.deltaVcmax=867;
			this.HaVjmax=50.3*1000;
			this.HdVjmax=217;
			this.deltaVjmax=697;

		}
		if (species.castaneaCode==8) {


		}
		if (species.castaneaCode==9) {


		}
		if (species.castaneaCode==10) {


		}

	}

	// creation of accessors

	public double getNetPhotosynthesis() {return netPhotosynthesis;}
	public double getGrossPhotosynthesis() {return grossPhotosynthesis;}
	public double getDelta13C() {return delta13C;}
	public double getRespiration() {return respiration;}
	public double getWaterConductance() {return waterConductance;}
	public double getCo2Conductance() {return co2Conductance;}

	public void setNetPhotosynthesis (double  v) {netPhotosynthesis=v;}
	public void setGrossPhotosynthesis (double v) {grossPhotosynthesis=v;}
	public void setRespiration (double v) {respiration=v;}
	public void setWaterConductance (double v) {waterConductance=v;}
	public void setCo2Conductance (double v) {co2Conductance=v;}

}