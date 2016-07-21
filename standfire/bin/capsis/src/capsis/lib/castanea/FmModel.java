package capsis.lib.castanea;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import jeeb.lib.util.Log;

/**
 * FmModel -
 *
 * @author Hendrik Davi - september 2006
 */
public class FmModel implements Serializable {

	// public int numberOfDays = 365;

	protected int nbstrat = FmSettings.NB_CANOPY_LAYERS;

	/**
	 * Constructor for new logical FmCell. initialize must be called after
	 */
	public FmModel() {

	}

	// outputs of yearly computation of one castanea cell

	public void yearlyFmSimulation(FmSettings settings, FmClimate climateBasis, FmClimate climate, FmCell cell,
			FmYearlyResults yearlyResults, double altitude, int year) throws Exception {

		int nbstrat = settings.NB_CANOPY_LAYERS;
		double latitude= settings.latitude;
		double longitude= settings.longitude;

		FmSpecies[] fmSpeciesList = cell.usedFmSpecies;
		FmSpecies species=fmSpeciesList[0];
		FmCanopy canopy = cell.getCanopy();
		int nSpecies = fmSpeciesList.length;
		int sp=0;

		double LAItot = 0;
		double AsolPAR = 0;
		double AsolPIR = 0;
		double[] LAI = canopy.getLAI();
		double[] WAI = canopy.getWAI();
		double[] frec;
		double[][] speciesProportion = cell.getSpeciesProportion(); // species
																	// and
		int numberOfDays = cell.getNumberOfDays(year);
		cell.currentYear = year;
		double hourlyPhotosynthesis = 0;
		double hourlyRespiration = 0;
		double[][] dailyCanopyRespiration = new double[nSpecies][numberOfDays];
		double[][] dailyPhotosynthesis = new double[nSpecies][numberOfDays];
		double[][] dailyCanopyTranspiration = new double[nSpecies][numberOfDays];
		double[] dailyCanopyTranspiration_int = new double[nSpecies];
		double[][] dailyCanopyEvapoTranspiration = new double[nSpecies][numberOfDays];

		double[] dailySoilEvaporation = new double[numberOfDays];

		double[] agreg = canopy.getClumping();

		FmCanopyWaterReserves canopyWaterReserves = canopy.getCanopyWaterReserves();

		FmLeafDynamics fmLeafDynamics = new FmLeafDynamics(cell, settings);

		double[] L = new double[nSpecies];

		double WAItot = 0;

		initializeYear(settings, cell, yearlyResults);

		// HD KC Som 8/10/2013; to be changed so that Ca can be charged from the
		// climate file (3 options : 0/no Ca variation 2/Ca variation as
		// described below and 3/detailed Ca variation
		if (settings.variationOfCa) {
			if (cell.currentYear < 2000) {
				settings.Ca = Math.max(0.104 * cell.currentYear + 95,
						Math.max(0.303 * cell.currentYear - 279, 1.48 * cell.currentYear - 2591.8));
			} else {
				settings.Ca = 369 * Math.pow(1.00522, (cell.currentYear - 2000));
			}
		}

		for (int j = 0; j < numberOfDays; j++) {
			// Log.println("year", numberOfDays+";"+j+";"+year);

			int day = j + 1; // all the JAVA tables begin at zero, the day 1 is in the table at zero
			cell.currentDay = day;

			climate.init(year, day);

			FmClimateDay climateDay = climate.nextWithoutLooping();

			// added for simulation on altitude transect
			if (altitude > 0) {
				climateBasis.init(year, day);
				FmClimateDay climateBasisDay = climateBasis.nextWithoutLooping();

				this.altitudeEffect(altitude, climateBasisDay, climateDay, settings.basicMeteoFile, settings);
			}

			FmDailyResults dailyResults = new FmDailyResults(cell, settings); // open


			// double Tmoyp= mobileMeans[j][6];


			double LHMIN = 8.05; // to be improved
			double currentLAIsum = 0;

			double rlit = cell.getSoil().getRlit();

			if (rlit > 0) {
				AsolPIR = settings.Alit;
				AsolPAR = settings.Alit / 3; // oak litter
			} else {
				AsolPIR = settings.Asoil;
				AsolPAR = settings.Asoil / 1.25; // Nagler et al
			}


			yearlyResults.setPotleafmin(sp, j, 0);

			if (species.decidu == 1) { // all decidous species without
										// dormancy
				fmLeafDynamics.currentLAIdeciduous(cell, yearlyResults, climateDay, LHMIN, j, settings,
						species, sp, nSpecies);
			} else {
				fmLeafDynamics.currentLAIevergreen(cell, yearlyResults, climateDay, LHMIN, j, settings,
						species, sp, nSpecies);
			}

			LAI = canopy.getLAI(); // per species
			currentLAIsum = cell.getTotalLAI(LAI, settings);

			canopyWaterReserves.waterInterception(cell, climateDay, settings);
			WAItot = WAI[sp];
			double strat = currentLAIsum / nbstrat;
			canopy.setStrat(sp, strat);

			hourlyLoop(year, cell, climateDay, j, WAItot, nbstrat, strat, AsolPAR, AsolPIR, latitude, longitude,
					dailyResults, yearlyResults, settings);

			yearlyResults.fill(cell, dailyResults, j, settings, climateDay);
			FmWoodGrowth woodGrowth = new FmWoodGrowth(cell, settings);
			woodGrowth.carbonAllocation(cell, yearlyResults, dailyResults, j, settings);

			double RU = cell.getSoil().getUsefulReserve();
			cell.getSoil().waterDynamics(cell, settings, yearlyResults, j, canopyWaterReserves);
			cell.getSoil().soilHeterotrophicRespiration(cell,settings,yearlyResults,climateDay,j); 

			yearlyResults.sumVariables(cell, climateDay, numberOfDays, settings,j);

		}// day loop


	} // end of yearlyOutputs

	private void hourlyLoop(int year, FmCell cell, FmClimateDay climateDay, int j, double WAItot, int nbstrat, double strat,
			double AsolPAR, double AsolPIR, double latitude, double longitude, FmDailyResults dailyResults,
			FmYearlyResults yearlyResults, FmSettings settings) {

		// hourly OR half hourly loop return an Array with species lines and two
		// columns tyhe first one concerns leaf respiration, the second one
		// photsynthesis

		int day=j+1;
		FmWood wood = cell.getWood();
		FmSoil soil = cell.getSoil();
		FmCanopy canopy =  cell.getCanopy();

		FmSpecies[] fmSpeciesList = cell.usedFmSpecies;
		int nSpecies = fmSpeciesList.length;
		FmSpecies species=fmSpeciesList[0];

		Collection<FmCanopyLayer> layers = canopy.getLayers();

		int sp = 0; // to improve when changing species managing

		double LAItot = strat * nbstrat;

		int age = (int) cell.getMeanAge()[sp];
		double dbh= cell.getMeanDbh ()[sp];
		double height = cell.getTreesHeight(settings);

		double ProjectedAreaOfCanopy= cell.getProjectedAreaOfCanopy(dbh,species.CrownArea1,species.CrownArea2);
		double CrownRatioMean = 0.7; // to be improved


		double[] aerodynamicResistances = new double[2];// ras, rac
		double[][] speciesProportion = cell.getSpeciesProportion();
		double[][] leafRespiration = new double[nSpecies][nbstrat];
		double[][] leafPhotosynthesis = new double[nSpecies][nbstrat];
		double[][] leafDelta13C = new double[nSpecies][nbstrat];
		double[][] leafConductance = new double[nSpecies][nbstrat];

		double[][] agregvar = new double[nSpecies][nbstrat];

		int n = (int) Math.round(24 / settings.frach);

		double skylPAR = 0; // proportion of diffuse light in PAR
		double Sghdif = 0; // global radiation in diffuse light
		double Sghdir = 0; // global radiation in beam light
		double PARdifo = 0; // PAR in diffuse light w/m2
		double PARdiro = 0; // PAR in beam light
		double PIRhdir = 0; // PIR in beam light
		double PIRhdif = 0; // PIR in diffuse light

		double Tsolh = 0;
		double Tveg = 0;
		double Tav = 0;

		aerodynamicResistances[0] = 1000;
		aerodynamicResistances[1] = 1000;

		double[][] strat_sp = new double[nSpecies][nbstrat]; // Quantity of
																// developped
																// leaf area for
																// one species
																// in one layer
		double[][] WAI_sp = new double[nSpecies][nbstrat]; // Wood area for one
															// species in one
															// layer
		double Th;
		double RHh;
		double Sgh;
		double PARh;
		double skyl;
		double[][] hourlyWoodRespiration = new double[2][nSpecies];

		// FmRadiativeBudget radiativeBudget;

		// to move in dailyloop?

		FmRadiativeBudget radiativeBudget = new FmRadiativeBudget(cell, settings, canopy);
		double[] agreg = canopy.getClumping();

		radiativeBudget.init_parametersReflectances(canopy, nbstrat, strat, WAItot, speciesProportion,
				fmSpeciesList, 1);

		for (int h = 0; h < n; h++) {
			FmHourlyResults hourlyResults = new FmHourlyResults(cell, settings);

			int heure = h + 1;
			Th = climateDay.getHourlyTemperature(h);
			RHh = climateDay.getHourlyRelativeHumidity(h);
			Sgh = climateDay.getHourlyGlobalRadiation(h);
			double V = climateDay.getHourlyWindSpeed(h);

			PARh = Sgh * (4.54 * settings.ratio);

			if (Sgh > 0) {

				skyl = climateDay.getSkyl(latitude, longitude, h, day);
				skylPAR = (1 + 0.3 * (1 - skyl * skyl)) * skyl;
				Sghdif = skyl * Sgh;
				Sghdir = (1 - skyl) * Sgh;
				PARdifo = skylPAR * PARh;
				PARdiro = (1 - skylPAR) * PARh;
				PIRhdir = PARdiro * (1 / 0.47 - 1);
				PIRhdif = PARdifo * (1 / 0.47 - 1);

			} else {
				skyl = 1;
				skylPAR = 1;
				Sghdif = 0;
				Sghdir = 0;
				PARdifo = 0;
				PARdiro = 0;
				PIRhdir = 0;
				PIRhdif = 0;

			}

			double beta = climateDay.getBeta(latitude, longitude, h, day);
			double Ta = Tveg;
			double ea = climateDay.getEah(h);
			double es = climateDay.getHourlyEs(Ta);
			double deltae = es - ea;

			// double kbdir= climateDay.getKbdir (latitude, longitude, h, day,
			// alphalSum); for information not used

			// calculation of radiation from SAIL computation for k layers and n
			// vegetations types by layer

			int nveg = fmSpeciesList.length;
			Tveg = Th;
			Tav = Th;
			Tsolh = cell.getSoil().getTsol(Tav);

			int l = 0;

			strat_sp = canopy.getStrat_sp();
			WAI_sp = canopy.getWAI_sp();

			radiativeBudget.updateRadiation(cell, strat_sp, WAI_sp, LAItot, beta, AsolPAR, AsolPIR, nbstrat, PARdiro,
					PARdifo, PIRhdir, PIRhdif, Ta, Tsolh, ea, skyl, j, heure, settings);

			Collection<FmCanopyLayer> canopylayers = canopy.getLayers();

			double[] canopyRespirationSum = dailyResults.getCanopyRespiration();
			double[] canopyConductanceSum = dailyResults.getCanopyConductance();
			double[] canopyPhotosynthesisSum = dailyResults.getCanopyPhotosynthesis();
			double[] canopyDeltasum13C = dailyResults.getDeltasum13C();

			double[] canopyConductance = new double[nSpecies];
			double[] canopyPhotosynthesis = new double[nSpecies];
			double[] canopyRespiration = new double[nSpecies];

			double[] hourlyCanopyRespiration = hourlyResults.getCanopyRespiration();


			if (strat > 0) {

				for (Iterator i = canopylayers.iterator(); i.hasNext();) {
					FmCanopyLayer layer = (FmCanopyLayer) i.next();

					double aboveLAI = l * strat;
					double[][] layerExchange = new double[4][nSpecies]; // respiration
																		// and
																		// grossphotosynthesis


					layerExchange = getGazExchange(layer, species, cell, sp, l, strat_sp[sp][l], aboveLAI, beta, Tveg,
							RHh, radiativeBudget, settings, j);
					leafRespiration[sp][l] = layerExchange[0][sp];
					leafPhotosynthesis[sp][l] = layerExchange[1][sp];
					leafConductance[sp][l] = layerExchange[2][sp];
					double PNhLAI = leafPhotosynthesis[sp][l] - leafRespiration[sp][l];
					double conversion = 3.6 * 12 / 1000.; // conversion to
															// gc
					leafDelta13C[sp][l] = (-8 - layerExchange[3][sp]) * strat * PNhLAI;

					canopyDeltasum13C[sp] = canopyDeltasum13C[sp] + leafDelta13C[sp][l] * conversion
							* settings.frach;

					hourlyCanopyRespiration[sp] = hourlyCanopyRespiration[sp] + leafRespiration[sp][l]
							* strat_sp[sp][l] * settings.frach;
					canopyRespiration[sp] = leafRespiration[sp][l]; // respiration

					canopyRespirationSum[sp] = canopyRespirationSum[sp] + canopyRespiration[sp] * strat_sp[sp][l]
							* settings.frach;// sum
					// for
					canopyPhotosynthesis[sp] = leafPhotosynthesis[sp][l]; // respiration
					canopyPhotosynthesisSum[sp] = canopyPhotosynthesisSum[sp] + canopyPhotosynthesis[sp]
							* strat_sp[sp][l] * settings.frach; // sum for
																// photsynthesis

					// canopyConductance[sp] = leafConductance[sp][l];
					// canopyConductanceSum[sp] = canopyConductanceSum[sp] +
					// (canopyConductance[sp] * (22.4 * (Tveg + 273) / 273))
					// * strat_sp[sp][l] / 1000000;

					// m/s
					canopyConductance[sp] = canopyConductance[sp]
							+ (leafConductance[sp][l] * (22.4 * (Tveg + 273) / 273)) * strat_sp[sp][l] / 1000000;

					canopyConductanceSum[sp] = canopyConductanceSum[sp]
							+ (leafConductance[sp][l] * (22.4 * (Tveg + 273) / 273)) * strat_sp[sp][l] / 1000000;
					l = l + 1;
				}

				// Log.println(settings.logPrefix+"photosynthesis",
				// "canopyDeltasum13C[sp]" + canopyDeltasum13C[0]);

			}

			dailyResults.setCanopyRespiration(canopyRespirationSum);
			dailyResults.setCanopyPhotosynthesis(canopyPhotosynthesisSum);
			dailyResults.setDeltasum13C(canopyDeltasum13C);
			dailyResults.setCanopyConductance(canopyConductanceSum);

			hourlyResults.setCanopyRespiration(hourlyCanopyRespiration);
			hourlyResults.setCanopyPhotosynthesis(canopyPhotosynthesis);
			hourlyResults.setCanopyConductance(canopyConductance);

			cell.getWood().hourlyWoodRespiration(cell, j, Tveg, Tsolh, climateDay, yearlyResults, dailyResults,
					hourlyResults, settings, age);


			// double hauteurMax = this.max (hauteur); // / we take the maximuma
			// height to be improved



			if (height > 0) {

				aerodynamicResistances = canopy.getAerodynamicResistances(V, Ta, height, LAItot, WAItot,
						CrownRatioMean);
			}
			double[] RnhvegTab = radiativeBudget.getRnhveg();
			double[] LAI = canopy.getLAI();

			double[] canopyTranspiration = dailyResults.getCanopyTranspiration();
			double[] canopyEvapoTranspiration = dailyResults.getCanopyEvapoTranspiration();
			double[] canopyPotentialEvaporation = dailyResults.getCanopyPotentialEvaporation();
			double[] maxHourlyTranspiration = dailyResults.getMaxHourlyTranspiration();

			int sp2 = 0;
			double ETRhcan = 0;
			double[] transpirationResults = new double[3];

			if (strat > 0) {

				double Rnhveg = 0;
				double rac = 0;

				double alphalRadian = species.alphal / 180 * Math.PI;
				double gc = canopyConductance[sp2];
				double L = LAI[sp2];

				rac = aerodynamicResistances[0]; // aerodynamic resistance
				Rnhveg = RnhvegTab[sp2];
				transpirationResults = canopy.getTranspiration(settings, species, sp2, Ta, Tveg, Rnhveg,
						ea, es, deltae, rac, gc, L);

				canopyTranspiration[sp2] = canopyTranspiration[sp2] + transpirationResults[0] * settings.frach; // TR
				canopyEvapoTranspiration[sp2] = canopyEvapoTranspiration[sp2] + transpirationResults[1]
						* settings.frach; // ETR
				
				// ETRhcan=
				double potleafmin = yearlyResults.getPotleafmin(sp2, j);
				double ksmax = yearlyResults.getKsmax(sp2);
				ksmax = Math.max(ksmax, transpirationResults[0]);

				yearlyResults.setKsmax(sp2, ksmax);
				double newPotleafmin = canopy.getPotleafmin(h, settings, cell, species, transpirationResults[0],
						potleafmin, sp2, ksmax);
				yearlyResults.setPotleafmin(sp2, j, newPotleafmin);
				// ETRhcan+transpirationResults[1];
				// Log.println
				// (settings.logPrefix+"explain1",h+";"+newPotleafmin+";"+yearlyResults.getPotleafmin(sp2));
				if (transpirationResults[0]>maxHourlyTranspiration[sp2]) {
					maxHourlyTranspiration[sp2]=transpirationResults[0];

				}
			}
			canopyPotentialEvaporation[sp2] = canopyPotentialEvaporation[sp2] + transpirationResults[2]
						* settings.frach; // ETP

			dailyResults.setCanopyTranspiration(canopyTranspiration);
			dailyResults.setCanopyTranspiration(canopyTranspiration);

			dailyResults.setCanopyEvapoTranspiration(canopyEvapoTranspiration);
			dailyResults.setMaxHourlyTranspiration(maxHourlyTranspiration);

			double soilEvaporation = dailyResults.getSoilEvaporation();
			double ras = aerodynamicResistances[1];
			double rnhsol = radiativeBudget.getRnhsol();

			double hourlySoilEvaporation = cell.getSoil().getSoilEvaporation(settings, Tsolh, rnhsol, ras, deltae,
					ETRhcan);

			soilEvaporation = soilEvaporation + hourlySoilEvaporation; // sum
																		// for
																		// annual

			dailyResults.setSoilEvaporation(soilEvaporation);
			if (settings.output > 2) {
				Log.println(settings.logPrefix + "hourlyResults", h + ";" + day + ";"+ year+ ";" + rnhsol + ";" + ras + ";"
						+ hourlySoilEvaporation + ";" + canopyPhotosynthesis[0] + ";" + canopyRespiration[0] + ";"
						+ transpirationResults[0] + ";" + leafPhotosynthesis[0][0] + ";" + leafRespiration[0][0] + ";"
						+ leafConductance[0][0] + ";" + canopyConductance[0]+";"+canopyDeltasum13C[0]);
			}
		} // Hour loop

		// yearlyResults.fill (cell, dailyResults, day, settings, climateDay);

	} // end of HourlyLoop method

	// ***************************************************************************************************************

	public double[][] getGazExchange(FmCanopyLayer layer, FmSpecies species, FmCell cell, int sp, int istrat,
			double abovelai, double lai, double beta, double Tl, double RH, FmRadiativeBudget radiativeBudget,
			FmSettings settings, int j) {

		int day=j+1;
		FmSpecies[] fmSpeciesList = cell.usedFmSpecies;
		int nSpecies = fmSpeciesList.length;

		// FmSoil soil = cell.getSoil ();
		FmCanopy canopy = cell.getCanopy();

		double strat = canopy.getStrat(sp);

		double[][] gazExchange = new double[4][nSpecies];
		double nitrogenLayer;
		double LMAlayer;
		int k = nbstrat - istrat; // from nbstrat to 0)

		double[] leafNitrogen = canopy.getNitrogen();

		double netPhotosynthesis = 0;
		double grossPhotosynthesis = 0;
		double respiration = 0;
		double waterConductance = 0;
		double co2Conductance = 0;
		double delta13C = 0;
		double[] leafExchange = new double[5]; // netPhotosynthesis,
												// grossPhotosynthesis,
												// respiration,
												// waterConductance,
												// co2Conductance
		double g1 = layer.getG1(species, lai, cell, sp);
		double rb = layer.getRb(species, lai);
		int cohortesOfLeaves = (int) species.cohortesOfLeaves;

		if (species.decidu == 1) { // || species.castaneaCode==1
			LMAlayer = layer.getLMAdecidous(settings, cell, species, lai, sp);
			nitrogenLayer = LMAlayer * leafNitrogen[sp] / 100;
		} else {
			LMAlayer = layer.getLMAevergreen(cell, settings, species, lai, cohortesOfLeaves, strat, sp);
			nitrogenLayer = layer.getNitrogenAvEvergreen(cell, settings, species, lai, cohortesOfLeaves, strat)
					* LMAlayer / 100;
		}

		 //Log.println(settings.logPrefix+"NitrogenLayer", "LMAlayer="+ LMAlayer+" leafNitrogen[sp]= "+leafNitrogen[sp]);

		FmLeaf leaf = new FmLeaf(layer, netPhotosynthesis, grossPhotosynthesis, respiration, waterConductance,
				co2Conductance, delta13C, 0, 0, 0, 0, 0, 0, 0, 0, settings);
		double parsun = radiativeBudget.getParsun(istrat, sp);
		double PARdifaLAI = radiativeBudget.getPardifaLAI(istrat, sp);
		double propsun = radiativeBudget.getPropsun(istrat);
		double propshad = radiativeBudget.getPropshad(istrat);

		leaf.calculateFluxes(settings, species, cell, nitrogenLayer, Tl, RH, parsun, PARdifaLAI, propsun, propshad, rb,
				g1, sp);

		gazExchange[0][sp] = leaf.getRespiration(); // respiration;
		gazExchange[1][sp] = leaf.getGrossPhotosynthesis(); // grossPhotosynthesis;
		gazExchange[2][sp] = leaf.getWaterConductance();
		gazExchange[3][sp] = leaf.getDelta13C();

		// sp = sp + 1;

		return gazExchange;

	} // end of CanopyLoop

	public void initDay(FmSpecies s, double fa0) {

		fa0 = s.fa0spg;

	} // end of init day

	public void altitudeEffect(double alt_tree, FmClimateDay climateBasisDay, FmClimateDay climateDay,
			String basicMeteoFile, FmSettings settings) {
		;

		// valid for Ventoux with tourniere meteo file in entry

		// initialization of new climate values
		double newTaverage = 0;
		double newTmin = 0;
		double newTmax = 0;
		double newPrecipitation = 0;
		double newRelativeHumidity = 0;

		// initialization of coeficients

		double aAltEffect_Taverage = 0;
		double aAltEffect_Tmin = 0;
		double aAltEffect_Tmax = 0;

		double bAltEffect_Taverage = 0;
		double bAltEffect_Tmin = 0;
		double bAltEffect_Tmax = 0;

		double aHumidityEffect = 0;
		double bHumidityEffect = 0;

		double ratioPrecipitation = 0;

		if (basicMeteoFile == "Tournieres") {

			// coefficient see Oddou & Davi 2014
			aAltEffect_Taverage = 1.02164;
			aAltEffect_Tmin = 1.07896;
			aAltEffect_Tmax = 0.9969;

			bAltEffect_Taverage = -0.00704 * alt_tree + 7.10665;
			bAltEffect_Tmin = -0.00649 * alt_tree + 7.15669;
			bAltEffect_Tmax = -0.00756 * alt_tree + 6.98725;

			ratioPrecipitation = 0.00096 * alt_tree - 0.07445;

			aHumidityEffect = 0.000123 * alt_tree + 0.728932;
			bHumidityEffect = 12; // fixed difference between north and south
									// aspect

			newTaverage = climateBasisDay.getDailyAverageTemperature() * aAltEffect_Taverage + bAltEffect_Taverage;
			newTmin = climateBasisDay.getDailyMinTemperature() * aAltEffect_Tmin + bAltEffect_Tmin;
			newTmax = climateBasisDay.getDailyMaxTemperature() * aAltEffect_Tmax + bAltEffect_Tmax;

			newPrecipitation = climateBasisDay.getDailyPrecipitation() * ratioPrecipitation;
			newRelativeHumidity = climateBasisDay.getDailyRelativeHumidity() * aHumidityEffect + bHumidityEffect;

		}

		if (basicMeteoFile == "Safran8147") {

			// coefficient see linearVentoux072014.xlsx

			aAltEffect_Tmin = 0.8971127;      //old value 0.91
			aAltEffect_Tmax = 0.9841416; // old value 0.978;

			if (alt_tree>=900) {
			        bAltEffect_Tmin =  -0.006214   * alt_tree + 9.009607; // old value -0.006449 and 8.522139
			        bAltEffect_Tmax = -0.008643  * alt_tree +  8.306412; // old value  -0.008730 and 8.182524
			        ratioPrecipitation = 0.0005797 * alt_tree + 0.3219238;// old value 0.000998 and - 0.329548
			}
			if (alt_tree<900) {
			        bAltEffect_Tmin =  -0.005   * alt_tree + 7.92; // old value -0.006449 and 8.522139
			        bAltEffect_Tmax = -0.006  * alt_tree +  5.93; // old value  -0.008730 and 8.182524
			        ratioPrecipitation = 0.0003 * alt_tree + 0.5736;// old value 0.000998 and - 0.329548
			}

			aHumidityEffect = 0.8125842; // old value 0.787
			bHumidityEffect = 0.006597 * alt_tree + 3.946111; // old value 0.007586 and 6.931239

			newTmin = climateBasisDay.getDailyMinTemperature() * aAltEffect_Tmin + bAltEffect_Tmin;
			newTmax = climateBasisDay.getDailyMaxTemperature() * aAltEffect_Tmax + bAltEffect_Tmax;
			newTaverage = (newTmin + newTmax) / 2;

			newPrecipitation = climateBasisDay.getDailyPrecipitation() * ratioPrecipitation;
			newRelativeHumidity = climateBasisDay.getDailyRelativeHumidity() * aHumidityEffect + bHumidityEffect;

		}

		double diffT = newTaverage - climateBasisDay.getDailyAverageTemperature();
		double diffTmin = newTmin - climateBasisDay.getDailyMinTemperature();

		// Log.println(settings.logPrefix+"ClimaticTest", alt_tree+ ";" +newTmin
		// +";" +newTmax+ ";"+newTaverage+";"+diffT+ ";"+newPrecipitation);

		climateDay.setDailyAverageTemperature(newTaverage);
		climateDay.setDailyMinTemperature(newTmin);
		climateDay.setDailyMaxTemperature(newTmax);
		climateDay.setDailyPrecipitation(newPrecipitation);
		climateDay.setDailyRelativeHumidity(newRelativeHumidity);

	}

	public void initializeYear(FmSettings settings, FmCell cell, FmYearlyResults yearlyResults) {
		int sp = 0;
		FmSpecies[] fmSpeciesList = cell.usedFmSpecies;
		FmSpecies species=fmSpeciesList[0];

		FmWood wood = cell.getWood();
		FmCanopy canopy = cell.getCanopy();
		
		if (settings.iFROST>=1) {
		        canopy.setLAImaxsp(0,canopy.getLAImaxBeforeFrost());
		}

		if (species.decidu == 2) {
			FmCanopyEvergreen canopyEvergreen = canopy.getCanopyEvergreen();
			canopyEvergreen.setLmaxYear(0);

			double age = cell.getMeanAge()[sp];
			int ageOfTrees = (int) age;
			wood.getRootShoot()[sp] = wood.getAgeEffectRS(settings, cell, species, sp, ageOfTrees);

		// ETRhcan+transpirationResults[1];

		}
		cell.getSoil().setStressCompteur(0);
		cell.getSoil().setStressLevel(0);

	}

	public static double max(double[] t) {
		double maximum = t[0]; // start with the first value
		for (int i = 1; i < t.length; i++) {
			if (t[i] > maximum) {
				maximum = t[i]; // new maximum
			}
		}
		return maximum;
	}// end method max

}
