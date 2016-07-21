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

package capsis.lib.fire.intervener.physicalfireeffect;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import jeeb.lib.util.StatusDispatcher;
import jeeb.lib.util.Translator;
import capsis.lib.fire.fuelitem.FiFireParameters;
import capsis.lib.fire.fuelitem.FiPlant;
import capsis.lib.fire.fuelitem.FiSeverity;
import capsis.lib.fire.intervener.fireeffect.FiFireEffect;
import capsis.lib.fire.intervener.fireeffect.crowndamagemodel.BioPhysicalCrown;
import capsis.util.GroupableIntervener;
import capsis.util.NativeBinaryInputStream;


/**
 * FiPhysicalFireEffect : an intervener to simulate physical fire effects from firetec simulations 
 * 
 * @author F. de Coligny, F. Pimont - september 2009
 */
public class FiPhysicalFireEffect extends FiFireEffect implements GroupableIntervener {

	public static final String NAME = "FiPhysicalFireEffect";
	public static final String VERSION = "1.0";
	public static final String AUTHOR =  "F. Pimont";
	public static final String DESCRIPTION = "FiPhysicalFireEffect.description";
	
	static public String SUBTYPE = "fireeffect";
	
	// firetec data
	private int nx;
	private int ny;
	private int nz;
	private double dx;
	private double dy;
	private double sceneOriginX;
	private double sceneOriginY;
	private double sceneSizeX;
	private double sceneSizeY;
	private double timeStep;
	private int framePeriod;
	private int frameNumber;
	private int firstFrameSuffix;
	private String filePrefix;
	private String fileFormat;
	public Map<Integer, Map> treeCrownMap;
	
	static {
		Translator
		.addBundle("capsis.lib.fire.intervener.physicalfireeffect.FiPhysicalFireEffect");
	}

	/**
	 * Phantom constructor. Only to ask for extension properties (authorName,
	 * version...).
	 */
	public FiPhysicalFireEffect() {
	}

	
	@Override
	public boolean initGUI() throws Exception {
		// Interactive dialog
		constructionCompleted = false;
		FiPhysicalFireEffectDialog dlg = new FiPhysicalFireEffectDialog(stand);
		if (dlg.isValidDialog ()) {
			// valid -> ok was hit and check were ok
			try {
				// Fire parameters
				// fireIntensity = dlg.getFireIntensity ();
				// residenceTime=dlg.getResidenceTime ();
				// ambiantTemperature=dlg.getAmbiantTemperature();
				// windVelocity=dlg.getWindVelocity();

				nx = dlg.nx;
				ny = dlg.ny;
				nz = dlg.nz;
				dx = dlg.dx;
				dy = dlg.dy;
				sceneOriginX = dlg.sceneOriginX;
				sceneOriginY = dlg.sceneOriginY;
				sceneSizeX = dlg.sceneSizeX;
				sceneSizeY = dlg.sceneSizeY;
				timeStep = dlg.getFiretecTimeStep();
				framePeriod = dlg.getFiretecFramePeriod();
				frameNumber = dlg.firetecFrameNumber;
				firstFrameSuffix = dlg.getFirstFrameSuffix();
				filePrefix = dlg.firetecFileDir+"/"+dlg.firetecFilePrefix;
				fileFormat = NativeBinaryInputStream.X86;
				treeCrownMap = dlg.treeCrownMap;

				// Models
				crownDamageModel = dlg.getCrownDamageModel ();
				cambiumDamageModel = dlg.getCambiumDamageModel();
				mortalityModel = dlg.getMortalityModel();

				constructionCompleted = true;
			} catch (Exception e) {
				constructionCompleted = false;
				throw new Exception(
						"FiPhysicalFireEffect (): Could not get parameters in FiPhysicalFireEffectDialog due to "
						+ e);
			}
		}
		dlg.dispose ();
		return constructionCompleted;
		
	}

	@Override
	public void activate() {
		// TODO Auto-generated method stub
		
	}
	
	
	static public boolean matchWith (Object referent) {
		return FiFireEffect.matchWith(referent);
	}


/**
 * This method initialize the different parameters required for computation of damage
 * @param treeBiomass : contain for each trees the time series of biomass for intensity computation (first time step is read here)
 * @param voxelConcernedForTreeBiomass :map of voxel concerned for tree biomass computation (voxels of the tree+immediate neiberhood)
 * @param r: firetecFireData reader: the firetec file; here the first file is read
 * @throws Exception
 */
	private void initialize(Map <Integer, double[]> treeBiomass, Map <Integer, ArrayList<Point>> voxelConcernedForTreeBiomass,FiretecFireDataReader r ) throws Exception {
		// remove uneeded trees and create FiFireParameters
		StatusDispatcher.print (Translator.swap ("FiPhysicalFireEffect.Initialization..."));
		for (Iterator i = concernedTrees.iterator (); i.hasNext ();) {
			FiPlant plant = (FiPlant) i.next ();
			int id = plant.getId();
			if (!treeCrownMap.containsKey(id)) {
				i.remove();
			} else {
				FiFireParameters fire = new FiFireParameters();
				plant.setFire(fire);
			}
		}
		// first frame to initialize parameter: ambiant temperature, temperature
		r.read(filePrefix+firstFrameSuffix);
		// ambiantTemperature = r.tGas[0][0][0]; // �K
		
		//StatusDispatcher.print (Translator.swap ("FiPhysicalFireEffect.frame 1/"+frameNumber)+"...");
		for (Iterator i = concernedTrees.iterator (); i.hasNext ();) {
			FiPlant plant = (FiPlant) i.next ();
			String speciesName = plant.getSpeciesName ();
			//System.out.println(speciesName + " ID " + plant.getId()
			//		+ " height " + " CBH " + plant.getCrownBaseHeight()
			//		+ plant.getHeight() + " dbh :" + plant.getDbh());
			treeBiomass.put(plant.getId(), new double[frameNumber]);
			ArrayList<Point> ar = new ArrayList<Point>();
			plant.getFire().setAmbiantTemperature(r.tGas[0][0][0]-273.15);
			Map<Integer, FiretecVoxel> voxelMap = treeCrownMap.get(plant.getId());
			
			for (int voxelNumber:voxelMap.keySet()) {
				FiretecVoxel v = voxelMap.get(voxelNumber);
				v.setInitialPhysicalProperties(r.tGas[v.i][v.j][v.k]);
				addPointNoDoublon(ar, new Point(Math.max(v.i - 1, 0), Math.max(
						v.j - 1, 0)));
				addPointNoDoublon(ar, new Point(Math.max(v.i - 1, 0), v.j));
				addPointNoDoublon(ar, new Point(Math.max(v.i - 1, 0), Math.min(
						v.j + 1, ny-1)));
				addPointNoDoublon(ar, new Point(v.i, Math.max(v.j - 1, 0)));
				addPointNoDoublon(ar, new Point(v.i, v.j));
				addPointNoDoublon(ar, new Point(v.i, Math.min(v.j + 1, ny-1)));
				addPointNoDoublon(ar, new Point(Math.min(v.i + 1, nx-1), Math
						.max(v.j - 1, 0)));
				addPointNoDoublon(ar, new Point(Math.min(v.i + 1, nx-1), v.j));
				addPointNoDoublon(ar, new Point(Math.min(v.i + 1, nx-1), Math
						.min(v.j + 1, ny-1)));
			
			}
			voxelConcernedForTreeBiomass.put(plant.getId(), ar);
			double biomassSum = 0d;
			for (Point p:ar) {
				for (int k = 0; k < nz; k++) {
					if (r.fuelMass[p.x][p.y][k] > 1e-5) biomassSum += r.fuelMass[p.x][p.y][k];
				}
			}
			treeBiomass.get(plant.getId())[0] = biomassSum;
		}


	}
	/**
	 * This method take eahc frame and update the FiretecCellElement (liveCellPop, temperature, etc.) for BioPhysicalCrown 
	 * It also computes the treeBiomass series
	 * @param it
	 * @param treeBiomass
	 * @param voxelConcernedForTreeBiomass
	 * @param r
	 * @throws Exception
	 */
	private void processFrame(int it, Map <Integer, double[]> treeBiomass, Map <Integer, ArrayList<Point>> voxelConcernedForTreeBiomass,FiretecFireDataReader r ) throws Exception {
		r.read(filePrefix+""+(framePeriod*it+firstFrameSuffix));
		for (Iterator i = concernedTrees.iterator (); i.hasNext ();) {
			FiPlant plant = (FiPlant) i.next ();
			Map<Integer, FiretecVoxel> voxelMap = treeCrownMap.get(plant.getId());
			for (int voxelNumber:voxelMap.keySet()) {
				FiretecVoxel v = voxelMap.get(voxelNumber);
				if (crownDamageModel instanceof BioPhysicalCrown) {
					v.update(timeStep * framePeriod, r.tGas[v.i][v.j][v.k],
							plant.getFire().getAmbiantTemperature()+273.15, r.velocity[v.i][v.j][v.k],
						r.radFlux[v.i][v.j][v.k]);
				}
			}
			double biomassSum = 0d;
			for (Point p:voxelConcernedForTreeBiomass.get(plant.getId())) {
				for (int k = 0; k < nz; k++) {
					if (r.fuelMass[p.x][p.y][k] > 1e-5) biomassSum += r.fuelMass[p.x][p.y][k];
				}
			}
			treeBiomass.get(plant.getId())[it] = biomassSum;
		}

	}
	
	
	/**
	 * This method computes the residence time of the fire near "plant", based on mean residence time in the cell at ground level near the tree
	 * This method works, because ground cells are in the treeCrownMap even if they are empty!
	 * @param plant
	 */
	
	private void computeResidenceTime(FiPlant plant) {
		//mean residence time computation
		Map<Integer, FiretecVoxel> voxelMap = treeCrownMap.get(plant.getId());
		int nbVoxelKEqual1 = 0;
		double residenceTime = 0d;
		for (int voxelNumber:voxelMap.keySet()) {
			if (voxelMap.get(voxelNumber).k == 0) {
				residenceTime += voxelMap.get(voxelNumber).residenceTimeInCell;
				nbVoxelKEqual1++;
			}
		}
		residenceTime *= 1d/nbVoxelKEqual1;
		plant.getFire().setResidenceTime(residenceTime);
		
	}
	/**
	 * Compute max intensity on 1 min period for each trees based on the voxels in the neighboorhood of the tree
	 * @param plant
	 * @param treeBiomass
	 * @param voxelConcernedForTreeBiomass
	 */
	
	private void computeIntensity(FiPlant plant, Map <Integer, double[]> treeBiomass,Map <Integer, ArrayList<Point>> voxelConcernedForTreeBiomass) {
		// max intensity computation (on 1min period)
		int frameDelta = Math.max(1, (int) Math.floor(60d/(timeStep*framePeriod)));
		double fuelMass1 = treeBiomass.get(plant.getId())[0];
		double fuelMass2 = treeBiomass.get(plant.getId())[Math.min(frameNumber - 1, frameDelta-1)];
		for (int it=1;it<frameNumber-frameDelta;it++) {
			double tempMass1 = treeBiomass.get(plant.getId())[it];
			double tempMass2 = treeBiomass.get(plant.getId())[it+frameDelta-1];
			if (tempMass1-tempMass2 > fuelMass1 - fuelMass2) {
				// higher intensity here
				fuelMass1 = tempMass1;
				fuelMass2 = tempMass2;
			}
		}
		double surfaceConcerned = dx * dy *voxelConcernedForTreeBiomass.get(plant.getId()).size();
		
		// Here we assumed that the surfaceConcerned is a circle
		// we extrapolate it to a square (*4/Pi) and divide it by the supposed fireLenght (Diam)
		double diameter = 2d * Math.sqrt(surfaceConcerned / Math.PI);
		double distance =  surfaceConcerned * 4d / (Math.PI * diameter);	
		double fireIntensity = 18000d * (fuelMass1 - fuelMass2)
				/ (distance * timeStep * framePeriod * frameDelta);
		plant.getFire().setFireIntensity(fireIntensity);
	}
	
	/**
	 * From Intervener. Makes the action : compute fire effects. Call private methods :
	 * - computeCrownDamage
	 * - computeCambiumDamage
	 * - computeMortality
	 */
	@Override
	public Object apply () throws Exception {
		// 0. Check if apply possible (should have been done before : security)
		if (!isReadyToApply ()) {
			throw new Exception(
			"FiPhysicalFireEffect.apply () - Wrong input parameters, see Log");
		}

		// There will be a "*" on the step carrying this stand
		// this stand is a copy of the initial stand
		stand.setInterventionResult (true);
		// Map of tree biomass array with time in the neighboorhood of a tree (for intensity computation)
		Map <Integer, double[]> treeBiomass = new HashMap <Integer, double[]> ();
		Map <Integer, ArrayList<Point>> voxelConcernedForTreeBiomass = new HashMap <Integer, ArrayList<Point>> ();
		// [�] : i;[1] : j
		
		FiretecFireDataReader r = new FiretecFireDataReader(nx, ny,
				nz, fileFormat);
		
		// initialization
		StatusDispatcher.print (Translator.swap ("FiPhysicalFireEffect.Initialization..."));
		initialize(treeBiomass, voxelConcernedForTreeBiomass, r);
		
		// process each frame
		for (int it = 0; it < frameNumber; it++) {
			StatusDispatcher.print (Translator.swap ("FiPhysicalFireEffect.frame "+(it+1)+"/"+frameNumber)+"...");
			processFrame(it,treeBiomass, voxelConcernedForTreeBiomass, r);
		}
		// severity and intensity computation process each plant
		for (Iterator i = concernedTrees.iterator(); i.hasNext();) {
			FiPlant plant = (FiPlant) i.next();
			String speciesName = plant.getSpeciesName();
		//	System.out.println(speciesName + " ID " + plant.getId()
		//			+ " height " + " CBH " + plant.getCrownBaseHeight()
		//			+ plant.getHeight() + " dbh :" + plant.getDbh());
			if (plant.getId()==287) {
				System.out.println();
				Map<Integer, FiretecVoxel> voxelMap = treeCrownMap.get(plant.getId());
				for (FiretecVoxel fc :voxelMap.values()) {
					//if (voxelMap.get(voxelNumber).k == 0) {
						//residenceTime += voxelMap.get(voxelNumber).residenceTimeInCell;
						System.out.println("maxgastemp (K) in cell k="+fc.k+" is "+fc.maxGasTemperatureInCell);
					//}
				}
				
			}
			computeResidenceTime(plant);
			computeIntensity(plant, treeBiomass, voxelConcernedForTreeBiomass);
			double scorchTemperature = computeScorchTemperature(speciesName);
			double killTemperature = computeKillTemperature(speciesName);
			FiSeverity severity = new FiSeverity ();
			double windVelocity = 0d;
			if (crownDamageModel instanceof BioPhysicalCrown) {
				((BioPhysicalCrown) crownDamageModel).set (treeCrownMap.get(plant.getId()), nx, ny, nz);
			}
			computeCrownDamage(severity, plant, scorchTemperature,
					killTemperature);
			
			computeCambiumDamage(severity, plant);
			computeMortality(severity, plant);
			// in case of multiple fire event
			FiSeverity previousSeverity = plant.getSeverity();
			if (!previousSeverity.isAlreadyBurn()) {
				plant.setSeverity(severity);
			} else { // multiple fire event
				computeWorseSeverity(severity, previousSeverity);
				plant.setSeverity(severity);
			}
			severity.alreadyBurn(true);

		}
		return stand;
	}


	
	
	private void addPointNoDoublon(ArrayList<Point> ar, Point np) {
		boolean shouldBeAdded = true;
		for (Point p : ar) {
			if (p.x == np.x && p.y == np.y)
				shouldBeAdded = false;
		}
		if (shouldBeAdded)
			ar.add(np);
	}
	
	/**
	 * Normalized toString () method : should allow to rebuild a filter with
	 * same parameters.
	 */
	@Override
	public String toString () {
		return "class="+getClass().getName ()
		+ " name=" + NAME
		+ " constructionCompleted=" + constructionCompleted;
	}

	

}

