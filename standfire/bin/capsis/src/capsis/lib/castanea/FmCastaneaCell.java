package capsis.lib.castanea;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import jeeb.lib.util.AmapTools;
import jeeb.lib.util.Log;
import jeeb.lib.util.Vertex3d;
import capsis.defaulttype.SquareCellHolder;
import dynaclim.model.DcFmCell;
import dynaclim.model.DcSpecies;
import dynaclim.model.DcTLCell;
import dynaclim.model.DcTree;

/**
 * This is a castanea only flux level
 *
 * @author Hendrik Davi - august 2011
 */
public class FmCastaneaCell extends FmCell {

	private double[] meanHeight;
	private double[] meanHcb;
	private double[] dominantHeight; // dominant height
	private double[] seedProduction; // seed production
	private double[] ringWidth; // ring width of average tree

	private double flCellWidth;




	/**
	 * Constructor
	 */
	public FmCastaneaCell (SquareCellHolder mother, int id, int motherId, Vertex3d origin, int iGrid, int jGrid,

	double nestedCellWidth, double fmCellWidth, // if > 0, this cell width
												// replaces mother.getCellWidth
												// ()
			FmSettings settings, int nbSpecies) {

		super (mother, id, motherId, origin, iGrid, jGrid, nestedCellWidth, fmCellWidth, settings, nbSpecies);

		meanHeight = new double[nbSpecies];
		meanHcb = new double[nbSpecies];
		dominantHeight = new double[nbSpecies];
		seedProduction = new double[nbSpecies];
		ringWidth=  new double[nbSpecies];




	}


public double[] getMeanHeight () {
		return meanHeight;
	}

	public double[] getTreeN () {
		return treeN;
	}

	public void setTreeN (double[] treeN) {
		this.treeN = treeN;
	}

	public void setMeanHeight (double[] meanHeight) {
		this.meanHeight = meanHeight;
	}

	public double[] getMeanHcb () {
		return meanHcb;
	}

	public void setMeanHcb (double[] meanHcb) {
		this.meanHcb = meanHcb;
	}


	public double[] getDominantHeight () {
		return dominantHeight;
	}

	public void setDominantHeight (double[] dominantHeight) {
		this.dominantHeight = dominantHeight;
	}

	public double[] getSeedProduction () {
		return seedProduction;
	}

	public void setSeedProduction (double[] seedProduction) {
		this.seedProduction = seedProduction;
	}

	public double[] getRingWidth () {
		return ringWidth;
	}

	public void setRingWidth (double[] v) {
		this.ringWidth = v;
	}


	public double getFlCellWidth () {
				return flCellWidth;
	}


	public void setFlCellWidth (double  flCellWidth) {
			this.flCellWidth = flCellWidth;
	}


	// copy from PDG


	public void initialize (FmSettings settings) {

			int nbstrat = settings.NB_CANOPY_LAYERS;
			int nSpecies = usedFmSpecies.length;

//			Collection<FmSpecies> fmSpeciesList = settings.usedFmSpecies;

			double usefulReserve = this.getSoil().getUsefulReserve ();
			double soilHeight = this.getSoil().getHeight ();

			//Log.println(settings.logPrefix+"SoilTest3",soilHeight+";"+usefulReserve);


			canopy.init (this, settings);
					//	wood.init (this, settings);

			// to be improved to have one params_LAI by species
			//double [] params_LAI= settings.params_LAI;

			//params_LAI[0] = 0.5; // constante of interaction between species (to// determine) to be improved
			//params_LAI[1] = 1.8; //fmSpecies.CoefLAI1; // effect of G (Mulpilication)
			//params_LAI[2] = 0.35; //fmSpecies.CoefLAI2; // effect of G (power) 0.5 in PDg, to high for castanea only
			//params_LAI[3] = 0.3; //0.5 // effect of reserve (power)

			// we collect the species composition of FmCASTANEACell


			int sp = 0; //

			// lopp on species collection
			for (FmSpecies species : usedFmSpecies) {

				soil.setLitterHeight (species.litterHeight);
				soil.setTopHeight (species.topHeight);

				soil.init (this, settings, sp);

				double age=getMeanAge()[sp];
				int ageOfTrees= (int)(age);

				double Bfinal = 0;
				double D130 = getMeanDbh ()[sp];
				double C130 = D130 * Math.PI;
				double Ntree= getTreeN ()[sp];
				double G = Math.pow ((D130 / 2 / 100), 2) * Math.PI / getArea() * 10000*Ntree; // average
				double hauteur= getMeanHeight ()[sp];
				double volmenu = 0;


				double Vhetre = (3.99957 * 1e-5 * Math.pow (D130, 2) * hauteur + 1.09819 * 1e-7
						* hauteur * Math.pow (D130, 3) - 2.82354 * 1e-7 * Math.pow (D130, 2)
						* Math.pow (hauteur, 2))
						* (1 + (-2.41275 * 10 * 10. / Math.pow (D130, 3))
								+ (1.37238 * 1e-5 * Math.pow (D130, 2)) + (1.04979 /hauteur) + 2.85037 * 1e-3 * hauteur);

				if (C130 <= 31) {
					volmenu = 0.316 * Vhetre;
				}
				if (31 < C130 & C130 <= 47) {
					volmenu = 0.05 * Vhetre;
				}
				if (47 < C130 & C130 <= 63) {
					volmenu = 0.062 * Vhetre;
				}
				if (63 < C130 & C130 <= 79) {
					volmenu = 0.09 * Vhetre;
				}
				if (79 < C130 & C130 <= 94) {
					volmenu = 0.104 * Vhetre;
				}
				if (94 < C130 & C130 <= 110) {
					volmenu = 0.11 * Vhetre;
				}
				if (110 < C130 & C130 <= 126) {
					volmenu = 0.106 * Vhetre;
				}
				if (126 < C130 & C130 <= 141) {
					volmenu = 0.1 * Vhetre;
				}
				if (141 < C130 & C130 <= 157) {
					volmenu = 0.092 * Vhetre;
				}
				if (157 < C130 & C130 <= 173) {
					volmenu = 0.087 * Vhetre;
				}
				if (173 < C130 & C130 <= 188) {
					volmenu = 0.082 * Vhetre;
				}
				if (188 < C130 & C130 <= 204) {
					volmenu = 0.081 * Vhetre;
				}
				if (C130 > 204) {
					volmenu = 0.08 * Vhetre;
				}

				double Bhetre = (Vhetre + volmenu) * 550; // 550 = densite du bois
				double Bsetage = 0.0762 * Math.pow (D130, 2.523) + 0.002 * Math.pow (D130, 3.256);
				if (C130 < 60) {
					Bfinal = Bsetage;
				} else {
					Bfinal = Bhetre;
				}


				Bfinal = Bfinal * 1000 * 0.5 / getArea()*Ntree;


				// fc-31.8.2011 - added this (copied from below), needed in canopy init below
				for (int l = 0; l < nbstrat; l++) {
					speciesProportion[sp][l] = 1.;
				}

				wood.getBiomassOfTrunk ()[sp] = Bfinal * (1 - species.ratioBR);
				wood.getBiomassOfBranch ()[sp] = Bfinal * species.ratioBR;
				wood.getBiomassOfCoarseRoot ()[sp] = Bfinal * species.RS;

				if (settings.fixedTronviv) {
					wood.getBiomassOfAliveWood ()[sp] = species.tronviv * (wood.getBiomassOfTrunk ()[sp]) + species.branviv
											* wood.getBiomassOfBranch ()[sp];
				} else {

					double tronvivAge= wood.getTronvivAge(this,species,settings,ageOfTrees);
					wood.getBiomassOfAliveWood ()[sp] = tronvivAge * (wood.getBiomassOfTrunk ()[sp]) + species.branviv
											* wood.getBiomassOfBranch ()[sp];
				}

				wood.getBiomassOfReserves ()[sp] = species.TGSS * wood.getBiomassOfAliveWood ()[sp];
				wood.getBiomassOfReservesMinimal ()[sp] = 1000;

				double BSSth = species.TGSS * wood.getBiomassOfAliveWood ()[sp];
				if (settings.fixedLAI>0) {

					if (BSSth > 0 & wood.getBiomassOfReserves ()[sp]>0) {
						canopy.getLAImax ()[sp] = FmCanopy.getLaiCalc (this, species, settings,sp, false);
							} else {
						canopy.getLAImax ()[sp] = 0;
					}
				}


//				Log.println(settings.logPrefix+"initBiomasse",BSSth+";"+canopy.getLAImax ()[sp]+";"+Bfinal+";"+wood.getBiomassOfAliveWood ()[sp]+";"+G);


				double LMAmoy = species.LMA0 * (1 - Math.exp (-species.KLMA * canopy.getLAImax ()[sp]))
								/ (species.KLMA * canopy.getLAImax ()[sp]);
				double BF = LMAmoy * settings.tc * canopy.getLAImax ()[sp];
				wood.getBiomassOfFineRoot ()[sp] = BF * this.getCoefraccell()[sp];

				canopy.setWAI (getWAIfromLAI (canopy.getLAImax (), usedFmSpecies));
				sp=sp+1;


			}


			//end of loop on species

		}

		/**
		 * Update the flux level cell for one year wood, canopy and soil objects are
		 * updated by this method. YearlyResult must be set before calling this
		 * method.
		 */
		public void updateFluxModel (FmSettings settings) {

			FmSpecies[] fmSpeciesList = usedFmSpecies;
			FmSpecies species=fmSpeciesList[0];

			int nSpecies = fmSpeciesList.length;

			double[] BSSmin = wood.getBiomassOfReservesMinimal ();
			double[] biomassOfReserves = wood.getBiomassOfReserves ();
			double[] biomassOfBranch = wood.getBiomassOfBranch ();
			double[] biomassOfTrunk = wood.getBiomassOfTrunk ();
			double[] biomassOfAliveWood = new double[nSpecies];
			double[] newBiomassOfReservesMinimal = new double[nSpecies];
			double[] LAImax = new double[nSpecies];

			double[] oldLAImax = canopy.getLAImax ();

			int sp=0;

			double [] trueOldDbh= new double[nSpecies];;
			double [] trueOldHeight= new double[nSpecies];;
			double [] trueNewDbh= new double[nSpecies];;
			double [] trueNewHeight= new double[nSpecies];;
			double [] dbhIncrement= new double[nSpecies];;
			double []  HIncrement= new double[nSpecies];;


			double age=getMeanAge()[sp];
			int ageOfTrees= (int)(age);
			double Ntree= treeN[sp];

			trueOldDbh[sp] = getMeanDbh()[sp];
			trueOldHeight[sp] = getMeanHeight()[sp];

			// What are tronvivAge

			double yearlyWoodGrowth = Yr.getYearlyWoodGrowth (sp);
			double reservesToMortality= species.reservesToMortality;
			double BSSminCrit= species.BSSminCrit;

			//if (BSSmin[sp] <= 0) { // BSS min = the minimum level of reserve during year
			if (biomassOfReserves[sp] <= reservesToMortality) {  // BSS total
			//if ( (biomassOfReserves[sp] <= reservesToMortality) || (BSSmin[sp] <= 160) ) {  // BSS total + BSSMin
				treeN [sp]=0;  // to improved to be probabilist
				seedProduction[sp] = 0;

			} else if  (BSSmin[sp] <= BSSminCrit) {
				//Log.println(settings.logPrefix+"understanding-"+PDGModel.counter, "BSSmin = " +BSSmin[sp] );
				treeN [sp]=0; // to improved to be probabilist
				seedProduction[sp] = 0;
			} else {

				if (settings.fixedTronviv) {
					biomassOfAliveWood[sp] =  species.tronviv * (biomassOfTrunk[sp]) + species.branviv * biomassOfBranch[sp];

				} else {

					double tronvivAge= wood.getTronvivAge(this,species,settings,getMeanDbh()[sp]);
					wood.getBiomassOfAliveWood ()[sp] = tronvivAge * (wood.getBiomassOfTrunk ()[sp]) + species.branviv
									* wood.getBiomassOfBranch ()[sp];
					biomassOfAliveWood[sp] = tronvivAge * (biomassOfTrunk[sp]) + species.branviv * biomassOfBranch[sp];
				}

				double BSSth = species.TGSS * biomassOfAliveWood[sp];


				double oldBiomass = (biomassOfBranch[sp] + biomassOfTrunk[sp]) / 1000 / 0.5
						* getArea();

				double oldLogC130 = 0.3888 * Math.log (oldBiomass) + 2.142;
				double oldDbh = Math.exp (oldLogC130) / Math.PI;
				double oldHeight = 8.1622 * oldLogC130 - 14.565;



				double newBiomass = oldBiomass + yearlyWoodGrowth / 1000 / 0.5 * getArea ();
				double newLogC130 = 0.3888 * Math.log (newBiomass) + 2.142;
				double newDbh = Math.exp (newLogC130) / Math.PI;
				double newHeight = 8.1622 * newLogC130 - 14.565;

				dbhIncrement[sp] = newDbh - oldDbh;
				HIncrement[sp] = newHeight - oldHeight;

				ringWidth[sp] =dbhIncrement[sp] * 10d/2; // / in mm

				trueNewDbh[sp] = trueOldDbh[sp] + dbhIncrement[sp];
				trueNewHeight[sp] = trueOldHeight[sp] + HIncrement[sp];

				//Yr.setDbh(sp,trueNewDbh[sp]);
				Yr.setRingWidth(sp,ringWidth[sp]);
				//Yr.setHeight(sp, trueNewHeight[sp]);

				double reservesToReproduce= species.reservesToReproduce;
				double rateOfSeedProduction= species.rateOfSeedProduction;
				double costOfOneSeed= species.costOfOneSeed;
				//double levelOfSeedProduction= settings.levelOfSeedProduction;

				if (biomassOfReserves[sp] > reservesToReproduce & settings.simulationReproduction) {
					// seedProduction[sp]=
					// Math.pow(concBSS/(levelSeedProduction*species.TGSS),0.4)*oldLAImax[sp]*25;
					// // to be improve
					seedProduction[sp] = (biomassOfReserves[sp] - reservesToReproduce)/costOfOneSeed*getArea ()*rateOfSeedProduction;
					wood.getBiomassOfReserves ()[sp] = biomassOfReserves[sp]-seedProduction[sp]*costOfOneSeed/getArea ();

				} else {
					seedProduction[sp] = 0;
				}


				double G = Math.pow ((trueNewDbh[sp] / 2 / 100), 2) * Math.PI / getArea() * 10000* Ntree; // average

				if (settings.fixedLAI>0) {
					if (BSSth > 0 & wood.getBiomassOfReserves ()[sp]>0) {
						canopy.getLAImax ()[sp] = FmCanopy.getLaiCalc(this, species, settings,sp, false);
					} else {
						canopy.getLAImax ()[sp] = 0;
					}

				}
				setMeanDbh(trueNewDbh);
				setMeanHeight(trueNewHeight);

				Yr.setSeedProduction(sp,seedProduction[sp]);

//					Log.println(settings.logPrefix+"Update", seedProduction[sp]+";"+ canopy.getLAImax ()[sp]+";"+biomassOfReserves[sp]+";"+reservesToReproduce+";"+BSSth+";"+G+";"+ringWidth[sp]+";"+trueNewDbh[sp]+";"+trueOldDbh[sp]+";"+oldBiomass);



			}  // end of test on alive tree



				//wood.setBiomassOfReservesMinimal (BSSmin);

				newBiomassOfReservesMinimal[sp] = species.TGSS * biomassOfAliveWood[sp];
				wood.setBiomassOfReserves (biomassOfReserves); // we remove at the end of year the impact of reproduction







	}// end of method





	public double getTreesHeight (FmSettings settings) {
		double h = 0;
		int i = 0;
		for (FmSpecies species : usedFmSpecies) {

			h += meanHeight[i];
			i++;
		}
		h = h / usedFmSpecies.length;
		return h;
	}

	/**
	 * Clone method.
	 */
	public Object clone () {
		try {
			FmCastaneaCell c = (FmCastaneaCell) super.clone ();

			c.meanHeight = AmapTools.getCopy (meanHeight);
			c.meanHcb = AmapTools.getCopy (meanHcb);
			c.meanDbh = AmapTools.getCopy (meanDbh);
			c.treeN = AmapTools.getCopy (treeN);
			c.meanAge = AmapTools.getCopy (meanAge);
			c.G = AmapTools.getCopy (G);
			c.dominantHeight = AmapTools.getCopy (dominantHeight);
			c.seedProduction = AmapTools.getCopy (seedProduction);

			return c;

		} catch (Exception e) {
			Log.println (Log.ERROR, "FmCastaneaCell.clone ()", "Error while cloning", e);
			return null;
		}

	}


}
