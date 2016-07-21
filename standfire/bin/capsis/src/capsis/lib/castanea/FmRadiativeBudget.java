package capsis.lib.castanea;

import java.io.Serializable;
import jeeb.lib.util.Log;
import java.util.Collection;
import java.util.Arrays;
import java.util.Iterator;



  /**	FmCanopy : Leaf of a FLCell of Dynaclim model.
  *
  *	@author Hendrik Davi - July 2010
  */
public class FmRadiativeBudget implements Serializable, Cloneable {

  	// WARNING: if references to objects (not primitive types) are added here,
  	// implement a "public Object clone ()" method (see RectangularPlot.clone () for template)



	protected double sghsol;
	protected double sghsolPAR;
	protected double sghsolPIR;
	protected double [] ais;
	protected double [] aid;
	protected double [] aisPIR;
	protected double [] aidPIR;
	protected double [] TMTss;
	protected double [] sghveg;
	protected double [] sghvegPAR;
	protected double [] sghvegPIR;
	protected double [] propsun;
	protected double [] propshad;
	protected double [][] pardiraLAI;
	protected double [][] pardifaLAI;
	protected double [][] pirdiraLAI;
	protected double [][] pirdifaLAI;
	protected double [][] pirsun;
	protected double [][] parsun;
	protected double [][] raufPARvar;
	protected double [][] taufPARvar;
	protected double [][] raufPIRvar;
	protected double [][] taufPIRvar;
	protected double [][] agregvar;
	protected double [][][] freqvar;
	protected double [] rnh;
	protected double [] rnhveg;
	protected double rnhsol;

	protected FmCell cell;
	protected FmSettings fm;
  	protected FmCanopy canopy;



  	/**	Constructor for new logical FmCanopy.
  	*/

  	public FmRadiativeBudget (
  		FmCell cell,
  		FmSettings fm,
  		FmCanopy canopy){

			this.cell= cell;
			this.canopy= canopy;
			this.fm= fm;

	}


	public double getSghsol() {return sghsol;}
	public double getSghsolPAR() {return sghsolPAR;}
	public double getSghsolPIR() {return sghsolPIR;}
	public double[] getSghveg() {return sghveg;}
	public double[] getSghvegPAR() {return sghvegPAR;}
	public double[] getSghvegPIR() {return sghvegPIR;}


	public double[][] getRaufPARvar() {return raufPARvar;}
	public double[][] getTaufPARvar() {return taufPARvar;}
	public double[][] getRaufPIRvar() {return raufPIRvar;}
	public double[][] getTaufPIRvar() {return taufPIRvar;}
	public double[][] getAgregvar() {return agregvar;}
	public double[] getRnh() {return rnh;}
	public double[] getRnhveg() {return rnhveg;}
	public double getRnhsol() {return rnhsol;}

	public double getParsun(int i,int sp) {return parsun[i][sp];}
	public double getPropsun(int i) {return propsun[i];}
	public double getPropshad(int i) {return propshad[i];}
	public double getPardiraLAI(int i,int sp) {return pardiraLAI[i][sp];}
	public double getPardifaLAI(int i,int sp) {return pardifaLAI[i][sp];}

	public double[] getAid() {return aid;}
	public double[] getAis() {return ais;}
	public double[] getAidPIR() {return aidPIR;}
	public double[] getAisPIR() {return aisPIR;}
	public double[] getTMTss() {return TMTss;}


	public void setRaufPARvar (double [][] v) {raufPARvar=v;}
	public void setTaufPARvar (double [][] v) {taufPARvar=v;}
	public void setRaufPIRvar (double [][] v) {raufPIRvar=v;}
	public void setTaufPIRvar (double [][] v) {taufPIRvar=v;}
	public void setAgregvar (double [][] v) {agregvar=v;}
	public void setFreqvar (double [][][] v) {freqvar=v;}

	public void setais (double [] v) {ais=v;}
	public void setaid (double [] v) {aid=v;}
	public void setTMTss(double [] v) {TMTss=v;}

	public void setaisPIR (double [] v) {aisPIR=v;}
	public void setaidPIR (double [] v) {aidPIR=v;}

	public void setPropsun (double [] v) {propsun=v;}
	public void setPropshad (double [] v) {propshad=v;}


	public void setSghveg (double [] v) {sghveg=v;}
	public void setSghvegPAR (double [] v) {sghvegPAR=v;}
	public void setSghvegPIR (double [] v) {sghvegPIR=v;}
	public void setSghsol (double v) {sghsol=v;}
	public void setSghsolPAR (double v) {sghsolPAR=v;}
	public void setSghsolPIR (double v) {sghsolPIR=v;}

	public void setParsun (double [][] v) {parsun=v;}
	public void setPardifaLAI (double [][] v) {pardifaLAI=v;}
	public void setPardiraLAI (double [][] v) {pardiraLAI=v;}


	public void setPirsun (double [][] v) {pirsun=v;}
	public void setPirdifaLAI (double [][] v) {pirdifaLAI=v;}
	public void setPirdiraLAI (double [][] v) {pirdiraLAI=v;}

	public void setRnh (double [] v) {rnh=v;}
	public void setRnhveg(double [] v) {rnhveg=v;}
	public void setRnhsol (double v) {rnhsol=v;}


	//********************************************************************************
	public double [] getRefMonosail (int nvgt,
								double [] roo,
								double [] tau,
								double [] freq,
								double [] lai,
								double thetav,
								double thetasbid,
								double psi){


		//********************************************************************************
		//                         METHOD  monoSAIL
		//    Computation of SUITS system coefficient as a function of canopy
		//    characteristics (LAI, average leaf angle, leaf optical properties,hot spot),
		//    solar and view angles for one layer
		//    See Verhoef 1984
		//******************************************************************************
		//   nvgt   : Number of vegetation elements in the layer
		//   roo    : Leaf reflectance for Nveg elements   [lambertian]
		//   tau    : Leaf transmittance for Nveg elements [lambertian]
		//   lai    : Leaf area index for Nveg elements
		//   hotsp  : Hot Spot parameter of the layer
		//   thetas : solar zenith angle       [degrees]
		//   thetav : view zenith angle        [degrees]
		//   psi    : relative azimut angle    [degrees]
		//   tss,too,rdd,tdd,rsd,tsd,rdo,tdo,rso,tsstoo : terms of the
		//            scattering matrix of the layer
		//*******************************************************************************


		double too;
		double tss;
		double rdd;
		double tdd;
		double rsd;
		double tsd;
		double [] RefMonosail = new double[5];
		double m= 0;


	   	// output tss,rdd,tdd,rsd,tsd


	   	// problem of sun

		if (thetasbid < 0) {
			tss= 0.001;
			rdd= 0.999;
			tdd= 0.001;
			rsd= 0.999;
			tsd= 0.001;
		} else {
			double thetas= 90-thetasbid;	// le soleil doit ?tre donn?e par rapport ? la verticale dans monosail

			//...............................................................................
			// If negative zenith angle, add Math.PI to the azimuth, and positive zenith angle
			// Azimuth angle between 0 and 2pi
			// Symetrisation with respect to the principal plane
			//...............................................................................
       		double phi= psi;
       		if (thetav<0) {
          		thetav= -thetav;
          		phi= phi +180;
   			}
		    if (phi>=360) {
 	 	        phi= phi-360;
       		}
       		if (phi>180) {
          		phi= 360-phi;
			}
            double raddeg= Math.PI/180;
       		double tants= Math.tan(raddeg*thetas);
       		double tantv= Math.tan(raddeg*thetav);
       		double cosphi= Math.cos(raddeg*phi);
       		double phir= raddeg*phi;

			//...............................................................................
			//     Initialisations
			//..............................................................................

       		double kks= 0;
       		double kkv= 0;
       		double ks=  0;
       		double kv=  0;
       		double att= 0;
       		double sig= 0;
       		double sb=  0;
       		double sf=  0;
       		double u=   0;
       		double v=   0;
       		double w=   0;
       		double [] thetal= {5 ,10 ,15 ,20, 25 ,30 ,35 ,40 ,45 ,50 ,55 ,60 ,65 ,70 ,75 ,80 ,85 ,90};


			//...............................................................................
			//    Vegetation element loop : iveg
			//..............................................................................:

     		for (int iveg=0; iveg<nvgt; iveg++) {
          		if (lai[iveg]==0) {
             		lai[iveg]= 0.000001;
          		}
          		double rtp= (roo[iveg]+tau[iveg])*0.5;
          		double rtm= (roo[iveg]-tau[iveg])*0.5;
				//...............................................................................
				//    Angles classes loop : angle
				//..............................................................................:
				for (int angle=0; angle<18; angle++) {

         			double cstl= Math.cos(raddeg*thetal[angle]);
             		double sntl= Math.sin(raddeg*thetal[angle]);
             		double tantl= Math.tan(raddeg*thetal[angle]);
             		double cstl2= Math.pow(cstl,2);
             		double sntl2= Math.pow(sntl,2);

					// ..............................................................................
					//     betas and betav computation
					//     Transition angles (beta) for solar (betas) and view (betav) directions
					//     if thetav+thetal>Math.PI/2, bottom side of the leaves is observed for leaf azimut
					//     interval betav+phi<leaf azimut<2pi-betav+phi.
					//     if thetav+thetal<Math.PI/2, top side of the leaves is always observed, betav=Math.PI
					//     same consideration for solar direction to compute betas
					// ..............................................................................

		            double betas= Math.PI;
	           		if (thetal[angle]+thetas>90) {
						betas= Math.acos(-1./(tants*tantl));
					}
	           		double betav= Math.PI;
             		if (thetal[angle]+thetav>90) {
						betav= Math.acos(-1./(tantv*tantl));
					}

					// ..............................................................................
					//   Computation of auxiliary azimut angles bt1, bt2, bt3 used
					//   for the computation of the bidirectional scattering coefficient w
					// .............................................................................

		            double btran1= Math.abs(betas-betav);
		            double btran2= 2*Math.PI-betas-betav;

		            double bt1=0;
		            double bt2=0;
          			double bt3=0;

					if (phir<=btran1) {
		                bt1= phir;
          			    bt2= btran1;
          			    bt3= btran2;
             		} else {
						if (phir>=btran2) {
                			bt1= btran1;
                			bt2= btran2;
                			bt3= phir;
                		} else {
							bt1= btran1;
                			bt2= phir;
                			bt3= btran2;
						}
					}
					//*******************************************************************************
					//                   SUITS SYSTEM COEFFICIENTS
					//
					//      ks  : Extinction coefficient for direct solar flux
					//      kv  : Extinction coefficient for direct observed flux
					//      att : Attenuation coefficient for diffuse flux
					//      sig : Backscattering coefficient of the diffuse downward flux
					//      sf  : Scattering coefficient of the direct solar flux for downward diffuse flux
					//      sb  : Scattering coefficient of the direct solar flux for upward diffuse flux
					//      u   : Scattering coefficient of upward diffuse flux in the observed direction
					//      v   : Scattering coefficient of downward diffuse flux in the observed direction
					//      w   : Bidirectional scattering coefficient
					//*******************************************************************************

             		double sks= ((betas-Math.PI*0.5)*cstl + Math.sin(betas)*tants*sntl)*2/Math.PI;
             		double skv= ((betav-Math.PI*0.5)*cstl + Math.sin(betav)*tantv*sntl)*2/Math.PI;
             		ks= ks + sks*freq[angle]*lai[iveg];

             		kv= kv + skv*freq[angle]*lai[iveg];
             		kkv= kkv + skv*freq[angle];
             		kks= kks + sks*freq[angle];

             		att= att + lai[iveg]*(1-rtp+rtm*cstl2)*freq[angle];
             		sig= sig + lai[iveg]*(rtp+rtm*cstl2)*freq[angle];

             		sb= sb + lai[iveg]*(rtp*sks+rtm*cstl2)*freq[angle];
             		sf= sf + lai[iveg]*(rtp*sks-rtm*cstl2)*freq[angle];

             		u= u + lai[iveg]*(rtp*skv-rtm*cstl2)*freq[angle];
             		v= v + lai[iveg]*(rtp*skv+rtm*cstl2)*freq[angle];

             		double tsin= 0.5*sntl2*tants*tantv;
             		double t1= cstl2+tsin*cosphi;
             		double t2= 0;
             		double t3= 0;

             		if (bt2<=0) {
             		  	t3= cstl2/(Math.cos(betas)*Math.cos(betav));
               			t2= t3 + Math.cos(bt1)*Math.cos(bt3)*tsin;
               			}

             		w= w + lai[iveg]*(roo[iveg]*t1+2*rtp/Math.PI*(-bt2*t1+Math.sin(bt2)*(t3+Math.cos(bt1)*Math.cos(bt3)*tsin)))*freq[angle];

				}// end of boucle on angle
			}// end of boucle on nveg

			//*******************************************************************************
			//   Differential equation system resolution to describe the radiative transfer
			//   in a vegetation layer
			//   VERHOEF 1985
			//*******************************************************************************
			// ........................................................................
			//   Computation of dummy variables
			// .......................................................................


			if ((Math.pow(att,2)-Math.pow(sig,2)<0)) {
				m= 0;
			} else {
				m= Math.sqrt(Math.pow(att,2)-Math.pow(sig,2));
			}

			double h1= (att+m)/sig;
			double h2= 1/h1;
			double cs= (sb*(ks-att)-sf*sig)/(Math.pow(ks,2)-Math.pow(m,2));
			double cv= (v*(kv-att) -u*sig)/(Math.pow(kv,2)-Math.pow(m,2));
			double ds= (-sf*(ks+att)-sb*sig)/(Math.pow(ks,2)-Math.pow(m,2));
			double dv= (-u*(kv+att)-v*sig)/(Math.pow(kv,2)-Math.pow(m,2));
			double hv= (sf*cv+sb*dv)/(ks+kv);

			// ........................................................................
			//   Layer Scattering Matrix
			//   Allow to compute output fluxes from input fluxes
			// .......................................................................

			tss= Math.exp(-ks);
			too= Math.exp(-kv);

			if (m!=0) {
				rdd= (Math.exp(m)-Math.exp(-m))/(h1*Math.exp(m)-h2*Math.exp(-m));
				tdd= (h1-h2)/(h1*Math.exp(m)-h2*Math.exp(-m));
			} else {
				rdd= sig/(sig+1);
				tdd= 1-rdd;
			}
			rsd= cs*(1-tss*tdd)-ds*rdd;
			tsd=  ds*(tss-tdd)-cs*tss*rdd;





		} //end loop if


		//Log.println(fm.logPrefix+"DynaclimTest", "tsd=" +tsd+" rsd "+rsd+ " m "+m);



		RefMonosail[0]= rdd;
		RefMonosail[1]= rsd;
		RefMonosail[2]= tdd;
		RefMonosail[3]= tsd;
		RefMonosail[4]= tss;
		return RefMonosail;




	} // end of monoSAIL

//******************************************************************************************************
	// coefSAIL: Calculation of SAIL coefficients (Verhoef et al., 1984; C. Fran?ois 2005)
	//** version from CASTANEA 4.2 (8/2009 N. Delpierre)

	double [][] getCoefSAIL(   double [][] rleafvar,
							double [][] tleafvar,
							double [][][] freqvar,
							double [][] lai,
							double [][] agregvar,
							double angsol,
							double rsoil,
							int nlayer,
							int jour,
							int heure,
							int nveg){

	// 		 this method gives an array 2D : rdd, rst, tdd, tsd, tss * nlayer



	  double [][] coefSail= new double [5][nlayer+2];


	//


		double [] rleaf = new double[nveg];
		double [] tleaf = new double[nveg];
//		double [] rleaf = new double[nlayer];
//		double [] tleaf = new double[nlayer];

		double [] freq = new double[18];
		double agreg=0;


	//	Transmittance and reflectance terms

		double thetas=angsol/Math.PI*180;

	// Initialisations for SAIL constants

		double thetav=0;			// aiming angle
		double phi=90;				// azimuth relatif soleil/vis?e
		int kdeb=2;				// ctype='multi'

		double [] rdd = new double[nlayer];
		double [] rsd = new double[nlayer];
		double [] tdd = new double[nlayer];
		double [] tsd= new double[nlayer];
		double [] tss= new double[nlayer];

		double [] newLAI= new double[nveg];
//		double [] newLAI= new double[nlayer];

	// Condition of lower limits (soil) :
		coefSail[4][0]=0;
		coefSail[0][0]= rsoil;
		coefSail[3][0]= rsoil;

	// ........................................................................
	//   Computation of upper limit conditions (incident beam)
	// .......................................................................
		coefSail[1][nlayer+1]=0;    // rsd
		coefSail[0][nlayer+1]=0;   //rdd
		coefSail[2][nlayer+1]=1;   //tdd
		coefSail[3][nlayer+1]=1;	//tsd
		coefSail[4][nlayer+1]=0;	//tss

	// ........................................................................
	//   Computation of SAIL coefficients for the layer k
	// .......................................................................
	// Initialisations

//	Log.println(fm.logPrefix+"DynaclimTest", "lai="+ lai[0][0]);
//	Log.println(fm.logPrefix+"DynaclimTest", "agregvar="+ lai[0][0]);

	for (int k= 0; k < nlayer; k++){

		for (int j= 0; j < nveg; j++){

			// fc+hd-28.2.2013 found an error ? k / j...
			newLAI[j]=lai[j][k]*agregvar[j][k];	 // effective LAI intercepting light in one layer
			rleaf[j]=rleafvar[j][k];
			tleaf[j]=tleafvar[j][k];

//			newLAI[k]=lai[j][k]*agregvar[j][k];	 // effective LAI intercepting light in one layer
//			rleaf[k]=rleafvar[j][k];
//			tleaf[k]=tleafvar[j][k];

			for (int jj= 0; jj < 18; jj++){
				freq[jj]=freqvar[j][k][jj];  // freqvar nstrat, 18

			}
		}
		double [] RefMonosail= getRefMonosail(nveg, rleaf,tleaf,freq,newLAI,thetav,thetas,phi);

//Log.println(fm.logPrefix+"DynaclimTest", "rdd="+ RefMonosail[0]+" Rsd= "+RefMonosail[1]+ "tdd"+RefMonosail[2]+" tsd= "+RefMonosail[3]);

		rdd[k]=RefMonosail[0];
		rsd[k]=RefMonosail[1];
		tdd[k]=RefMonosail[2];
		tsd[k]=RefMonosail[3];
		tss[k]=Math.max(RefMonosail[4],1e-6);

		coefSail[0][k+1]=rdd[k];			//+1 because coefSail begin with soil when l=0
		coefSail[1][k+1]=rsd[k];
		coefSail[2][k+1]=tdd[k];
		coefSail[3][k+1]=tsd[k];
		coefSail[4][k+1]=Math.max(tss[k],1e-6);






	} //end of loop

	return coefSail;
}	// end of method





///******************************************************************************************************
// calculation of radiation coefficient for PAR and Global version CASTANEA 4.2 august 2009
// need input PARdiro= direct PAR incident; PARdifo diffuse, PAR incident
// raufPARvar: array of reflectances in PAR bands for k layers
// taufPARvar: array of transmittance in PAR bands for k layers
// freqvar: distribution of leaves angles; agregvar: distribution of clumpinf coef across canopy

//******************************************************************************************************

 public void updateRadiationCoef(FmCell cell,
	 							double [][] strat,
 								double [][] WAI,
 								double [][] agregvar,
 								double beta,
 								double AsolPAR,
 								double AsolPIR,
 								int nbstrat,
 								double PARdiro,
								double PARdifo,
								double PIRhdir,
								double PIRhdif,
								int jour,
								int heure,
								int nveg){




	double[][] raufPARvar= this.getRaufPARvar();
	double[][] taufPARvar=this.getTaufPARvar();
	double[][] raufPIRvar= this.getRaufPIRvar();
	double[][] TaufPIRvar= this.getTaufPIRvar();


	double [] ais= new double[nbstrat+1];
	double [] aid= new double[nbstrat+1];
	double [] aisPIR= new double[nbstrat+1];
	double [] aidPIR= new double[nbstrat+1];
	double [] TMTss= new double[nbstrat+2];

	double s;
	double tr=0;
	double [] isPAR= new double[nbstrat+1];



 	double epsrad=0.0001;	// residual radiation to stop iteration
 	double tauw=0;
 	int nbw=1;

 	double TaucPIR;
	double RaucPIR;
 	double Tauc;
	double Rauc;
	double sumStrat=0;
	double [] Is = new double[nbstrat+1];
	double [] Id = new double[nbstrat+1];
	double [] Idup = new double[nbstrat+1];

	double [] IsPIR = new double[nbstrat+1];
	double [] IdPIR = new double[nbstrat+1];

	double Tis;
	double Tid;
	double Rid;
	double Tidup;
	double Ridup;
	double Refcan=0;
	double Sgh;
	double Sghveg;
	double Sghsol;
	double SghvegPAR;
	double SghvegPIR;
	double SghsolPAR;
	double SghsolPIR;


	double [][] coefSail = new double [5][nbstrat+2];




	//Log.println(fm.logPrefix+"DynaclimTest", "nbstrat="+ nveg);

	for (int k=0; k <nbstrat; k++){
		for (int j= 0; j < nveg; j++){
			sumStrat=sumStrat+strat[j][k]; // effective LAI intercepting light in one layer // to be improved when more than one sepcies
			//Log.println(fm.logPrefix+"DynaclimTest", "nbstrat="+ nveg+"strat[j][k] "+strat[j][k]);
		}
   	}

   		//Log.println(fm.logPrefix+"coefSail", PARdiro+";"+PARdifo+";"+nveg+";"+nbstrat+";"+jour+";"+heure+";"+sumStrat+";"+beta);

//Log.println(fm.logPrefix+"DynaclimTest", "sumStrat= " +sumStrat);
 	if (sumStrat > 0) {		  // if there is canopy

 		//******************
 		// PAR domain *
 		//******************



			coefSail= this.getCoefSAIL(raufPARvar,taufPARvar,freqvar,strat,agregvar,beta,AsolPAR,nbstrat,
									jour,heure,nveg);


			//rdd coefSail[k][0]=rdd[k];
			//rsd coefSail[k][1]=rsd[k];
			//tdd coefSail[k][2]=tdd[k];
			//tsd coefSail[k][3]=tsd[k];
			//tss coefSail[k][4]=Math.max(tss[k],1e-6);


			Is[nbstrat]= PARdiro;

			isPAR[nbstrat]=PARdiro;
			Id[nbstrat]= PARdifo;



			double Irest= PARdiro+PARdifo;
			int iter=0;

			// Calculation of canopy transmittance for direct radiation
			TMTss[nbstrat+1]= 1;
			for (int k= nbstrat; k > 0; k--){
				TMTss[k]= coefSail[4][k]*TMTss[k+1];

			//	Log.println(fm.logPrefix+"DynaclimTest", "PARdiro=" +PARdiro);
			//	Log.println(fm.logPrefix+"DynaclimTest", "coefSail[4]= " +coefSail[4][k]);
			}

			// first direct radiation : IS

			// layer nbstrat+1
			// transmitted and reflected radiations
			Tis= coefSail[4][nbstrat]*Is[nbstrat];
			Tid= coefSail[3][nbstrat]*Is[nbstrat];
			Rid= coefSail[1][nbstrat]*Is[nbstrat];



			// absorbed radiation
			ais[nbstrat]= (1-coefSail[3][nbstrat]-coefSail[4][nbstrat]-coefSail[1][nbstrat])*Is[nbstrat];

//			Log.println(fm.logPrefix+"DynaclimTest", "ais= " +ais[nbstrat] +" is= "+Is[nbstrat] +" tmsd= "+ coefSail[3][nbstrat]+" tmss= "+ coefSail[4][nbstrat]+ " rsd "+ coefSail[1][nbstrat] );
			// new computed radiation up and down
			Is[nbstrat]= 0;
			Is[nbstrat-1]= Tis;
			Id[nbstrat-1]= Tid;


			Refcan= Refcan+ Rid;

			// for the other layers
			for (int k= nbstrat-1; k > 0; k--){
				isPAR[k]=Is[k];					// for shadow

				// Rayonnement transmis et r?fl?chi

				Tis= coefSail[4][k]*Is[k];
				Tid= coefSail[3][k]*Is[k];
				Rid= coefSail[1][k]*Is[k];

				// absorbed radiation

				ais[k]= (1-coefSail[3][k]-coefSail[4][k]-coefSail[1][k])*Is[k];


				// new computed radiation up and down
				Is[k]= 0;
				Idup[k]= Rid;
				Is[k-1]= Tis;
				Id[k-1]= Tid;
			} // end of loop

			 //  soil

			 tr= tr+Is[1];
			 Rid= AsolPAR*Is[1];
			 ais[1]= (1-AsolPAR)*Is[1];
			 Is[1]= 0;
			 Idup[1]= Rid;

			// 			Log.println(fm.logPrefix+"sailPAR", PARdiro+";"+PARdifo);


			 // we make go up the direct reflected radiation to end with Is
			 for (int k= 0; k<nbstrat; k++){
				// transmitted and reflected radiations
				Tidup= coefSail[2][k+1]*Idup[k];
				Ridup= coefSail[0][k+1]*Idup[k];

				// absorbed radiation
				aid[k+1]= aid[k+1]+ (Idup[k]-Tidup-Ridup);

				// new computed radiation up and down
				Id[k]= Id[k]+ Ridup;
				Idup[k]= 0;
				Idup[k+1]= Idup[k+1]+ Tidup;


			//Log.println(fm.logPrefix+"sailPAR", k+";"+PARdiro+";"+PARdifo+";"+coefSail[k][0]+";"+coefSail[k][1]+";"+coefSail[k][2]+";"+coefSail[k][3]+";"+coefSail[k][4]);

			 } // end of loop

			 // heaven case

			Refcan= Refcan + Idup[nbstrat];
			Idup[nbstrat]= 0;

			Irest=cell.sumArray(Id)+cell.sumArray(Idup);

			// diffuse radiation need iteration
			 while (Irest>epsrad && iter < 20){

				//layer nbstrat+1
				// transmitted and reflected raidations
				Tid= coefSail[2][nbstrat]*Id[nbstrat];
				Rid= coefSail[0][nbstrat]*Id[nbstrat];

				//absorbed radiation
				aid[nbstrat]= aid[nbstrat]+ (1-coefSail[2][nbstrat]-coefSail[0][nbstrat])*Id[nbstrat];

				// new computed radiation up and down
				Id[nbstrat]= 0;
				Id[nbstrat-1]= Id[nbstrat-1]+ Tid;
				Refcan= Refcan+ Rid;

				// for the other layers
				for (int k= nbstrat-1; k > 0; k--){
					// transmitted and reflected raidations
					Tid= coefSail[3][k]*Is[k] + coefSail[2][k]*Id[k];
					Tidup= coefSail[2][k+1]*Idup[k];
					Ridup= coefSail[0][k+1]*Idup[k];
					Rid= coefSail[0][k]*Id[k];

					//absorbed radiation
					aid[k]= aid[k]+ (1-coefSail[2][k]-coefSail[0][k])*Id[k];
					aid[k+1]= aid[k+1]+ (Idup[k]-Tidup-Ridup);
				//Log.println(fm.logPrefix+"DynaclimTest", "aid[k]="+ aid[k]+" ais[k]= "+ais[k]);


					// new computed radiation up and down
					Id[k]= Ridup;
					Idup[k]= Rid;
					Id[k-1]= Id[k-1]+ Tid;
					Idup[k+1]= Idup[k+1]+ Tidup;
				}

				// Soil
				tr= tr+Id[0];
				Tidup= coefSail[2][1]*Idup[0];
				Ridup= coefSail[0][1]*Idup[0];
				Rid= Id[0]*AsolPAR;
				aid[0]= aid[0]+(1-AsolPAR)*Id[0];
				aid[1]= aid[1]+ (Idup[0]-Tidup-Ridup);
				Id[0]= Ridup;
				Idup[0]= Rid;
				Idup[1]= Idup[1]+ Tidup;

				 // we make go up the direct reflected radiation to end with Is
				 for (int k= 0; k<nbstrat; k++){

					// transmitted and reflected raidations
					Tidup= coefSail[2][k+1]*Idup[k];
					Ridup= coefSail[0][k+1]*Idup[k];

					//absorbed radiation
					aid[k+1]= aid[k+1]+ (Idup[k]-Tidup-Ridup);

					// new computed radiation up and down
					Id[k]= Id[k]+ Ridup;
					Idup[k]= 0;
					Idup[k+1]= Idup[k+1]+ Tidup;
				}

				// heaven case
				Refcan= Refcan + Idup[nbstrat];
				Idup[nbstrat]= 0;

				iter=iter+1;
				Irest=cell.sumArray(Id)+cell.sumArray(Idup);

			}

			 // canopy transmittance

			 Tauc = ((ais[0]+aid[0])/(1-AsolPAR))/(PARdiro+PARdifo);
			 Rauc= Refcan/(PARdiro+PARdifo);

			 // other transmittance calculation

			 s=0;
			 for (int k= nbstrat; k > 0; k--){
				s=s+ais[k]+aid[k];
			 }

			Tauc= (PARdiro+PARdifo - Refcan -s)/(PARdiro+PARdifo);
			Tauc=tr/(PARdiro+PARdifo);

			 // Calculation light under canopy
			 Arrays.fill(Is,0);
			 Arrays.fill(Id,0);

			for (int k= nbstrat; k >= 0; k--){
				Is[k]= ais[k]/(1-coefSail[1][k]);
				Id[k]= aid[k]/(1-coefSail[0][k]);

				//Log.println(fm.logPrefix+"DynaclimTest", "Is= " +Is[k]+ "Id= "+Id[k]);

			}

			//******************
			// PIR domain	 *
			 //******************

			coefSail=getCoefSAIL(raufPIRvar,taufPIRvar,freqvar,strat,agregvar,beta,AsolPIR,nbstrat,jour,heure, nveg);

			Refcan=0;

			IsPIR[nbstrat]= PIRhdir;
			IdPIR[nbstrat]= PIRhdif;




			Irest= PIRhdir+PIRhdif;
			iter=0;

			 // direct radiation IS

			 // layer nbstrat+1
			 // transmitted and reflected raidations
			 Tis= coefSail[4][nbstrat]*IsPIR[nbstrat];
			 Tid= coefSail[3][nbstrat]*IsPIR[nbstrat];
			 Rid= coefSail[1][nbstrat]*IsPIR[nbstrat];

			 //absorbed radiation
			 aisPIR[nbstrat]= (1-coefSail[3][nbstrat]-coefSail[4][nbstrat]-coefSail[1][nbstrat])*IsPIR[nbstrat];

			 // new computed radiation up and down
			 IsPIR[nbstrat]= 0;
			 IsPIR[nbstrat-1]= Tis;
			 IdPIR[nbstrat-1]= Tid;
			 Refcan= Refcan+ Rid;

			 // for the other layers
			for (int k= nbstrat-1; k > 0; k--){
				// transmitted and reflected raidations
				Tis= coefSail[4][k]*IsPIR[k];
				Tid= coefSail[3][k]*IsPIR[k];
				Rid= coefSail[1][k]*IsPIR[k];

				// transmitted and reflected raidations
				aisPIR[k]= (1-coefSail[3][k]-coefSail[4][k]-coefSail[1][k])*IsPIR[k];

				// new computed radiation up and down
				IsPIR[k]= 0;
				Idup[k]= Rid;
				IsPIR[k-1]= Tis;
				IdPIR[k-1]= Tid;
			}

			 //  soil

			 Rid= AsolPIR*IsPIR[0];
			 aisPIR[0]= (1-AsolPIR)*IsPIR[0];
			 IsPIR[0]= 0;
			 Idup[0]= Rid;

			// we make go up the direct reflected radiation to end with Is
			for (int k= 0; k<nbstrat; k++){
				// transmitted and reflected raidations
				Tidup= coefSail[2][k+1]*Idup[k];
				Ridup= coefSail[0][k+1]*Idup[k];

				//absorbed radiation
				aidPIR[k+1]= aidPIR[k+1]+ (Idup[k]-Tidup-Ridup);

				// new computed radiation up and down

				IdPIR[k]= IdPIR[k]+ Ridup;
				Idup[k]= 0;
				Idup[k+1]= Idup[k+1]+ Tidup;
			}


			 // heaven case
			 Refcan= Refcan + Idup[nbstrat];
			 Idup[nbstrat]= 0;

			 Irest=cell.sumArray(IdPIR)+cell.sumArray(Idup);


			 // diffuse radiation needs iterations
			 while (Irest>epsrad && iter < 20) {

				// layer nbstrat+1
				// transmitted and reflected raidations
				Tid= coefSail[2][nbstrat]*IdPIR[nbstrat];
				Rid= coefSail[0][nbstrat]*IdPIR[nbstrat];

				//absorbed radiation
				aidPIR[nbstrat]= aidPIR[nbstrat]+ (1-coefSail[2][nbstrat]-coefSail[0][nbstrat])*IdPIR[nbstrat];

				// new computed radiation up and down
				IdPIR[nbstrat]= 0;
				IdPIR[nbstrat-1]= IdPIR[nbstrat-1]+ Tid;
				Refcan= Refcan+ Rid;

				// for the other layers

				for (int k= nbstrat-1; k > 0; k--){
					// transmitted and reflected raidations
					Tid= coefSail[3][k]*IsPIR[k] + coefSail[2][k]*IdPIR[k];
					Tidup= coefSail[2][k+1]*Idup[k];
					Ridup= coefSail[0][k+1]*Idup[k];
					Rid= coefSail[0][k]*IdPIR[k];

					//absorbed radiation
					aidPIR[k]= aidPIR[k]+ (1-coefSail[2][k]-coefSail[0][k])*IdPIR[k];
					aidPIR[k+1]= aidPIR[k+1]+ (Idup[k]-Tidup-Ridup);

					// new computed radiation up and down
					IdPIR[k]= Ridup;
					Idup[k]= Rid;
					IdPIR[k-1]= IdPIR[k-1]+ Tid;
					Idup[k+1]= Idup[k+1]+ Tidup;
				}

				// soil case
				Tidup= coefSail[2][1]*Idup[0];
				Ridup= coefSail[0][1]*Idup[0];
				Rid= IdPIR[0]*AsolPIR;
				aidPIR[0]= aidPIR[0]+(1-AsolPIR)*IdPIR[0];
				aidPIR[1]= aidPIR[1]+ (Idup[0]-Tidup-Ridup);
				IdPIR[0]= Ridup;
				Idup[0]= Rid;
				Idup[1]= Idup[1]+ Tidup;

				// we make go up the direct reflected radiation to end with Is
				for (int k= 0; k<nbstrat; k++){
					// transmitted and reflected raidations
					Tidup= coefSail[2][k+1]*Idup[k];
					Ridup= coefSail[0][k+1]*Idup[k];

					//absorbed radiation
					aidPIR[k+1]= aidPIR[k+1]+ (Idup[k]-Tidup-Ridup);

					// new computed radiation up and down
					IdPIR[k]= IdPIR[k]+ Ridup;
					Idup[k]= 0;
					Idup[k+1]= Idup[k+1]+ Tidup;
				}

				// heaven case
				Refcan= Refcan + Idup[nbstrat];
				Idup[nbstrat]= 0;

				iter=iter+1;
				Irest=cell.sumArray(IdPIR)+ cell.sumArray(Idup);
			}

			 // canopy PIR transmittance

			TaucPIR= ((aisPIR[1]+aidPIR[1])/(1-AsolPIR))/(PIRhdir+PIRhdif);
			RaucPIR= Refcan/(PIRhdir+PIRhdif);

			// PIR under canopy
			 //IsPIR[]=0;
			 //IdPIR[]=0;

			 for (int k= nbstrat; k >= 0; k--){
				IsPIR[k]= aisPIR[k]/(1-coefSail[1][k]);
				IdPIR[k]= aidPIR[k]/(1-coefSail[0][k]);
			 }



	}else {// case of wood without leaves in Winter: strat ==0

		//*****************************
		//* trunc case
		//* (with one iteration only)
		//*****************************

		//********************************
		//* domaine du PAR pour le tronc *
		//********************************



		// Log.println(fm.logPrefix+"DynaclimTest", "agregvar="+ agregvar[0][0]);
		 coefSail=getCoefSAIL(raufPARvar,taufPARvar,freqvar,WAI,agregvar,beta,AsolPAR,nbw,jour,heure, nveg);

		 // Initialisations
		 Is[1]=PARdiro;
		 Id[1]=PARdifo;

		 // direct radiation : IS
		 // first layer 1

		 // transmitted and reflected raidations
		 Is[0]= coefSail[4][1]*Is[1];
		 Id[0]= coefSail[3][1]*Is[1];
		 Refcan= coefSail[1][1]*Is[1];
		 ais[1]= (1-coefSail[3][1]-coefSail[4][1]-coefSail[1][1])*Is[1];

		 //soil case
		 tr= Is[0];
		 Idup[0]= AsolPAR*Is[0];
		 ais[0]= (1-AsolPAR)*Is[0];
		  // we make go up the direct reflected radiation to end with Is
		 Tidup= coefSail[2][1]*Idup[0];
		 Ridup= coefSail[0][1]*Idup[0];
		 aid[1]= aid[1]+ (Idup[0]-Tidup-Ridup);
		 Id[0]= Id[0]+ Ridup;
		 Refcan= Refcan + Tidup;

		 // diffuse radiation only for layer 1 just belaow the heaven

		 // transmitted and reflected raidations
		 Tid= coefSail[2][1]*Id[1];
		 Rid= coefSail[0][1]*Id[1];
		 aid[1]= aid[1]+ (1-coefSail[2][1]-coefSail[0][1])*Id[1];
		 Id[0]= Id[0]+ Tid;
		 Refcan= Refcan+ Rid;

		  //soil case
		 tr= tr+Id[0];
		 Rid= Id[0]*AsolPAR;
		 aid[0]= aid[0]+(1-AsolPAR)*Id[0];
		 Idup[0]= Rid;

		 // we make go up the direct reflected radiation to end with Is
		 Tidup= coefSail[2][1]*Idup[0];
		 Ridup= coefSail[0][1]*Idup[0];
		 aid[1]= aid[1]+ (Idup[0]-Tidup-Ridup);
		 Refcan= Refcan + Tidup;

		 // trunk PAR transmittance
		 Tauc = ((ais[0]+aid[0])/(1-AsolPAR))/(PARdiro+PARdifo);
		 Rauc= Refcan/(PARdiro+PARdifo);


		 //********************************
		 //* PIR domain for trunk *
		 //********************************


			coefSail=getCoefSAIL(raufPIRvar,taufPIRvar,freqvar,WAI,agregvar,beta,AsolPIR,nbw,jour,heure, nveg);

		 // Initialisations
		  IsPIR[1]=PIRhdir;IdPIR[1]=PIRhdif;

		 // direct radiation : IS
		 // layer 1)
		 // transmitted and reflected raidations
		 IsPIR[0]= coefSail[4][1]*IsPIR[1];
		 IdPIR[0]= coefSail[3][1]*IsPIR[1];
		 Refcan= coefSail[1][1]*IsPIR[1];
		 aisPIR[1]= (1-coefSail[3][1]-coefSail[4][1]-coefSail[1][1])*IsPIR[1];

		  //soil case
		 tr= IsPIR[0];
		 Idup[0]= AsolPIR*IsPIR[0];
		 aisPIR[0]= (1-AsolPIR)*IsPIR[0];
		  // we make go up the direct reflected radiation to end with Is
		 Tidup= coefSail[2][1]*Idup[0];
		 Ridup= coefSail[0][1]*Idup[0];
		 aidPIR[1]= aidPIR[1]+ (Idup[0]-Tidup-Ridup);
		 IdPIR[0]= IdPIR[0]+ Ridup;
		 Refcan= Refcan + Tidup;

		 //diffuse radiation one iteration
		 // layer 1
		 // transmitted and reflected raidations

		 Tid= coefSail[2][1]*IdPIR[1];
		 Rid= coefSail[0][2]*IdPIR[2];
		 aidPIR[1]= aidPIR[1]+ (1-coefSail[2][1]-coefSail[0][1])*IdPIR[1];
		 IdPIR[0]= IdPIR[0]+ Tid;
		 Refcan= Refcan+ Rid;

		//soil case
		 tr= tr+IdPIR[0];
		 Rid= IdPIR[0]*AsolPIR;
		 aidPIR[0]= aidPIR[0]+(1-AsolPIR)*IdPIR[0];
		 Idup[0]= Rid;
		 // we make go up the direct reflected radiation to end with Is
		 Tidup= coefSail[2][1]*Idup[0];
		 Ridup= coefSail[0][1]*Idup[0];
		 aidPIR[1]= aidPIR[1]+ (Idup[0]-Tidup-Ridup);
		 Refcan= Refcan + Tidup;

		 // calculation of PIR transmittance
		 TaucPIR = ((aisPIR[0]+aidPIR[0])/(1-AsolPIR))/(PIRhdir+PIRhdif);
		 RaucPIR= Refcan/(PIRhdir+PIRhdif);

		 }			// end of test if strat > 0 or not



	 //***************************************************************
	 //* Soil budget *
	 //***************************************************************
	SghsolPAR= ais[0]+aid[0];
	SghsolPIR= aisPIR[0]+aidPIR[0];
	Sghsol= SghsolPAR + SghsolPIR;


	setSghsol(Sghsol);
	setSghsolPAR(SghsolPAR);
	setSghsolPIR(SghsolPIR);

	setais(ais);
	setaid(aid);
	setaisPIR(aisPIR);
	setaidPIR(aidPIR);
	setTMTss(TMTss);

} //end of void


//***************************************************************************************************
// calculation of kbdif for IRT transmittance
//****************************************************************************************************

public double getKbdif(double alpha) {

	double SHADdif;

	// coefficient d'extinction du couvert pour le rayonnement diffus (kbdif)

	double kbdif=0;

	for (int i=0; i<8; i++){		//dom is the angle between horizontal and the considered heaven sector

		double dom= 15+i*10;

		double domrad= dom* Math.PI/180;

		if (domrad >= alpha) {
		   SHADdif= Math.cos(alpha) * Math.sin(domrad);
		} else{
		   double Q5= -(Math.sin(domrad) *Math.cos(alpha)) / (Math.sin(alpha) * Math.cos(domrad));
		   double Zdif= Math.PI/ 2 - Math.acos(Q5);
		   SHADdif= (2 / Math.PI) * (Math.cos(domrad) * Math.sin(alpha) * Math.cos(Zdif) - Zdif * Math.sin(domrad) * Math.cos(alpha));
		}

		kbdif= kbdif + SHADdif / (8 * Math.sin(domrad));
	}

	return kbdif;

} // end of kbdif calculation

//************************************************************
// initialization of parameters required for radiations transmission
//***********************************************************

public void init_parametersReflectances(FmCanopy canopy,
										int nbstrat,
										double strat,
										double WAI,
										double [][] speciesProportion,
										FmSpecies[] fmSpeciesList,
										int varChoice){

		double [] Freqint;
		double [] agreg= canopy.getClumping();
		int nbspecies=fmSpeciesList.length;
		FmSpecies species=fmSpeciesList[0];


		double [][] strat_sp = new double[nbspecies][nbstrat];
		double [][] WAI_sp= new double[nbspecies][nbstrat];


		double [][] raufPARvar = new double[nbspecies][nbstrat] ;
		double [][] taufPARvar = new double[nbspecies][nbstrat];
		double [][] raufPIRvar = new double[nbspecies][nbstrat];
		double [][] taufPIRvar = new double[nbspecies][nbstrat];
		double [][] agregvar = new double[nbspecies][nbstrat];
		double [][][] freqvar = new double[nbspecies][nbstrat][18];


		for (int k= 0; k<nbstrat; k++){
			int sp=0;

			double alphalRadian=species.alphal/180*Math.PI;
			Freqint= canopy.getFreq(alphalRadian);
			if (varChoice==1) { // when no variation is taken into account inside the canopy

				raufPARvar[sp][k]= species.RaufPAR;
				taufPARvar[sp][k]= species.TaufPAR;
				raufPIRvar[sp][k]= species.RaufPIR;
				taufPIRvar[sp][k]= species.TaufPIR;
				agregvar[sp][k]= agreg[sp];
				strat_sp[sp][k] = strat*speciesProportion[sp][k];

				WAI_sp[sp][k]=WAI/nbstrat*speciesProportion[sp][k];
				//Log.println(fm.logPrefix+"DynaclimTest", "WAI="+ WAI);
				//Log.println(fm.logPrefix+"DynaclimTest", "WAI_sp="+ WAI_sp[sp][k]+speciesProportion[sp][k]);

				for (int j= 0; j < 18; j++){
					freqvar[sp][k][j]= Freqint[j];
				}

			}else{ // when no variation is teken into account inside the canopy

			}
		}

		this.setRaufPARvar(raufPARvar);
		this.setRaufPIRvar(raufPIRvar);
		this.setTaufPARvar(taufPARvar);
		this.setTaufPIRvar(taufPIRvar);
		this.setAgregvar(agregvar);
		this.setFreqvar(freqvar);

		canopy.setStrat_sp(strat_sp);
		canopy.setWAI_sp(WAI_sp);




}// end of init method


////////////////////////////////////////////////////////

	// Calculation thermic transmittance and energetic budget
	public void updateNetRadiation(
			double Ta,
			double ea,
			double L,
			double WAI,
			double skyl,
			int h,
			double Tsoil,
			FmSpecies[] fmSpeciesList){

	int nSpecies = fmSpeciesList.length;
	FmSpecies species=fmSpeciesList[0];

	double [] sghveg = this.getSghveg();
	double [] sghvegPAR = this.getSghvegPAR();
	double [] sghvegPIR = this.getSghvegPIR();
	double  sghsol= this.getSghsol();

	double [] sghPAR= new double [nSpecies];
	double [] sghPIR= new double [nSpecies];
	double [] sgh= new double [nSpecies];

	double stefan= 5.6698e-8; //constant of Stefan-Boltzmann
	double [] tauv = new double [nSpecies];
	double [] rauv = new double [nSpecies];
	double [] emsf = new double [nSpecies];
	double [] emsv = new double [nSpecies];
	double [] rnhveg = new double [nSpecies];
	double [] rnh = new double [nSpecies];
	double [] agreg =canopy.getClumping();

	double emsvtot=0;
	double rauvtot=0;
	double tauvtot=0;
	double rnhsol;

	int sp=0;



	double alphalRadian=species.alphal/180*Math.PI;
	double kbdif= this.getKbdif(alphalRadian);
	double kirtdif= kbdif*agreg[sp];
	// d?termination of absorbed and trasnmitted in infra red thermic wavelenghts
	if (L < WAI) {
		tauv[sp]= Math.exp(-kirtdif* WAI);
	} else {
		tauv[sp]= Math.exp(-kirtdif* L);
	}

	// Transmittance, reflectance and emissivity of canopy (with multi reflexions soil/canopy)
	// adapation to multi species by simple averaging (Davi July 2010) to be improved

	rauv[sp]= (1-tauv[sp])*(1-species.emsf);
	emsv[sp]= (1-tauv[sp])* species.emsf;

	double emst= emsv[sp]/(1-(1-fm.emsg)*rauvtot);
	double raut= rauv[sp]+ (1-fm.emsg)*(Math.pow(tauvtot,2))/(1-(1-fm.emsg)*rauvtot);
	double taut= tauv[sp]/(1-(1-fm.emsg)*rauvtot);
	double wt= 1- raut- fm.emsg*taut;

	// Rah: Atmospheric radiation (W.m-2)

	// nouvelle formule Iziomon (2003)
	 double Xs=0.35;
	 double Ys=10.0;
	 double Zs=0.0035;
	 double oktas=skyl*8;
	 double Rah= (1+Zs*oktas*oktas)*(1-Xs*Math.exp(-Ys*ea/(Ta+273.15)))*stefan*Math.pow(Ta+273.15,4);


	// Rvsh: radiation from canopy (W.m-2) (and also soil)
	 double Rvsh= stefan*Math.pow(Ta+273.15,4);
	 double Rgsh= stefan*Math.pow(Tsoil+273.15,4);


	// atmospheric radiation absorbed by canopy and soil (W/m2)

	double Rahsol= tauvtot*fm.emsg*Rah;
	double Rahveg= emst*Rah;

	// Bilan des rayonnements ?mis pour la v?g?tation et pour le sol (W/m2)

 	double Rhsol= fm.emsg*(taut+emst)*Rgsh - fm.emsg*emst*Rvsh;	// emmited by soil- received by canopy
	double Rhveg= (wt+ fm.emsg*emst)*Rvsh - fm.emsg*emst*Rgsh;	// the opposite
	//// Diagnostic, IRT entrant et sortant (W/m2)
	//Rirtout= Rhveg+Rhsol
	double Rirtout= Rhveg+Rhsol+raut*Rah;
	double Rirtin= Rahveg+Rahsol;


	//// Conversion de Sghsol et Sghveg
	sghsol=sghsol/4.54;
	sghsolPAR=sghsolPAR/4.54;
	sghsolPIR=sghsolPIR/4.54;
	rnhsol= sghsol+ Rahsol- Rhsol;

    sp=0;


	sghveg[sp]=sghveg[sp]/4.54;
	sghvegPAR[sp]=sghvegPAR[sp]/4.54;
	sghvegPIR[sp]=sghvegPIR[sp]/4.54;

	sghPAR[sp]= sghsolPAR+sghvegPAR[sp];
	sghPIR[sp]= sghsolPIR+sghvegPIR[sp];

// Bilan Total (Rayonnement net)  (W/m2)
// Rnhveg = bilan pour le couvert, Rnhsol= bilan pour le sol, Rnh= bilan total
	rnhveg[sp]= sghveg[sp]+Rahveg- Rhveg;


//Rnhsol= 0.8*Rnhsol	//chaleur transmise dans le sol
	rnh[sp]= rnhveg[sp]+ rnhsol;


	// Somme et conversion
	//double RNj= Rnh* frach* 3600./ 1000000 + RNj;
	//double Rnmesj= Rnmesh* frach* 3600./ 1000000 + Rnmesj;

	this.setRnh(rnh);
	this.setRnhveg(rnhveg);
	this.setRnhsol(rnhsol);




	}// end of method


// main method
//*/********************************************************************************

	public void updateRadiation (FmCell cell,
								double [][] strat,
								double [][] WAI,
								double Ltot,
								double beta,
								double AsolPAR,
								double AsolPIR,
								int nbstrat,
								double PARdiro,
								double PARdifo,
								double PIRhdir,
								double PIRhdif,
								double Ta,
								double Tsoil,
								double ea,
								double skyl,
								int jour,
								int heure,
								FmSettings settings) {

		FmSpecies[] fmSpeciesList = cell.usedFmSpecies;
		int nSpecies = fmSpeciesList.length;
		FmSpecies species=fmSpeciesList[0];
		int sp=0;


		Collection<FmCanopyLayer> canopylayers= canopy.getLayers ();
		double WAItot=0;
		double [][] parsun= new double[nbstrat][nSpecies];
		double [][] pardifaLAI= new double[nbstrat][nSpecies];
		double [][] pardiraLAI= new double[nbstrat][nSpecies];
		double [][] pirsun= new double[nbstrat][nSpecies];
		double [][] pirdifaLAI= new double[nbstrat][nSpecies];
		double [][] pirdiraLAI= new double[nbstrat][nSpecies];

		double [] sghveg= new double[nSpecies];
		double [] sghvegPAR= new double[nSpecies];
		double [] sghvegPIR= new double[nSpecies];

		double [] propsun= new double[nbstrat];
		double [] propshad= new double[nbstrat];

		double sghvegstratPAR= 0;
		double sghvegstratPIR=0;
		double sghvegstrat=0;



		this.updateRadiationCoef(cell, strat,WAI, agregvar,beta, AsolPAR, AsolPIR, nbstrat, PARdiro, PARdifo, PIRhdir, PIRhdif, jour, heure, nSpecies);
		double [] TMTss= this.getTMTss();
		double [] ais= this.getAis();
		double [] aid= this.getAid();
		double [] aisPIR= this.getAisPIR();
		double [] aidOIR= this.getAidPIR();

		for (int k = nbstrat-1; k>=0; k--) {

			propsun[k]= (TMTss[k+1]+TMTss[k])/2; // for the n species together
			propshad[k]=1-propsun[k];



			if (beta>0) {  // day

				pardiraLAI[k][sp]= ais[k]/strat[sp][k];  //ais  //pb array to be improved
				pardifaLAI[k][sp]= aid[k]/strat[sp][k];   //aid
				pirdiraLAI[k][sp]= aisPIR[k]/strat[sp][k];  //ais
				pirdifaLAI[k][sp]= aidPIR[k]/strat[sp][k];   //



				// ! On passe en m2 de feuilles
				if (propsun[k] > 0.0001) {
					parsun[k][sp]= pardiraLAI[k][sp]/propsun[k];
					pirsun[k][sp]= pirdiraLAI[k][sp]/propsun[k];
				}else{
					parsun[k][sp]= 0;
					pirsun[k][sp]= 0;
					propsun[k]= 0;
				}


			} else {

				parsun[k][sp]= 0;
				pardiraLAI[k][sp]=0;
				pardifaLAI[k][sp]=0;
				pirsun[k][sp]= 0;
				pirdiraLAI[k][sp]=0;
				pirdifaLAI[k][sp]=0;
			}

			// radiation absorbed by vegetation

			sghvegstratPAR= (pardifaLAI[k][sp]+pardiraLAI[k][sp])*strat[sp][k];
			sghvegstratPIR= (pirdifaLAI[k][sp]+pirdiraLAI[k][sp])*strat[sp][k];
			sghvegstrat= sghvegstratPAR + sghvegstratPIR;


			WAItot += WAI[sp][k];


			sghveg[sp]= sghveg[sp] + sghvegstrat;
			sghvegPAR[sp]= sghvegPAR[sp]+ sghvegstratPAR;
			sghvegPIR[sp]= sghvegPIR[sp]+ sghvegstratPIR;





		}




		setParsun(parsun);
		setPardifaLAI(pardifaLAI);
		setPardiraLAI(pardiraLAI);
		setParsun(pirsun);
		setPardifaLAI(pirdifaLAI);
		setPardiraLAI(pirdiraLAI);
		setSghveg(sghveg);
		setSghvegPAR(sghvegPAR);
		setSghvegPIR(sghvegPIR);
		setPropsun(propsun);
		setPropshad(propshad);


		this.updateNetRadiation(Ta, ea, Ltot, WAItot, skyl, heure, Tsoil,fmSpeciesList);
	} // end of Method


} //end of class

