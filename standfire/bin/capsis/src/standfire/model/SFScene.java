/* 
 * The Standfire model.
 *
 * Copyright (C) September 2013: F. Pimont (INRA URFM).
 * 
 * This file is part of the Standfire model and is NOT free software.
 * It is the property of its authors and must not be copied without their 
 * permission. 
 * It can be shared by the modellers of the Capsis co-development community 
 * in agreement with the Capsis charter (http://capsis.cirad.fr/capsis/charter).
 * See the license.txt file in the Capsis installation directory 
 * for further information about licenses in Capsis.
 */
package standfire.model;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import jeeb.lib.sketch.scene.item.Polygon;
import jeeb.lib.util.Vertex3d;
import capsis.defaulttype.DefaultPlot;
import capsis.defaulttype.Tree;
import capsis.lib.fire.FiStand;
import capsis.lib.fire.fuelitem.FiPlant;
import capsis.lib.fire.fuelitem.FiPlantPopulationGenerator;
import capsis.lib.fire.fuelitem.FiSpecies;

/**	SFScene is the description of the Standfire scene.
 * 	It is a list of SFTree instances.
 * 
 *	@author F. Pimont - September 2013
 */
public class SFScene extends FiStand {

	static double ONE_ACRE = 4046.85642; //m2

	/**	Constructor
	 */
	public SFScene () {
		super ();

		// Create a default plot
		setPlot (new DefaultPlot ());
		getPlot ().setScene (this);

	}

	@Override
	public String toString () {
		return "SFScene_" + getCaption ();
	}


	public List<SFTree> getTrees(FiSpecies sp) {
		List<SFTree> res = new ArrayList<SFTree>();
		for (Tree tree: this.getTrees ()) {
			if (((SFTree) tree).getSpeciesName ().equals(sp.getName ())) {
				res.add ((SFTree) tree);
			}
		}
		return res;
	}

	/**
	 * This method is specific to the FVS data that is define in a acre square :
	 * 4 046,85642 m2=(63.615m)^2
	 * - it moves the FVS tree sample to x>xFVSSampleBegin and center it over the y axis
	 * - build new trees base on the sample tree to put around, according to the ratio treefactor (ratio of (scenearea-samplearea)/samplearea)
	 * @throws Exception 
	 */
	public void extendInitialTreeSet (int extendFVSSampleSpatialOption,double xFVSSampleBegin) throws Exception {
		System.out.println("	Trying to extend initial tree set beyond "+xFVSSampleBegin+" m");
		//System.out.println("	sceneSize "+this.getArea()+" "+this.getXSize ()+"*"+this.getYSize ());
		//double treeFactor = (this.getArea () - ONE_ACRE)/ONE_ACRE;
		double fvsSceneSize = Math.sqrt (ONE_ACRE);
		if (xFVSSampleBegin + fvsSceneSize > this.getXSize () || fvsSceneSize > this.getYSize ()) {
			//System.out.println("WARNING: temorary scene extension ");
			//this.setXSize(Math.max(xFVSSampleBegin + fvsSceneSize+1,this.getXSize ()));
			//this.setYSize(Math.max(fvsSceneSize+1,this.getYSize ()));
		    throw new Exception ("SFScene.extendInitialTreeSet(): The extension of the initial tree set can not be done because fvs scene size is "+fvsSceneSize+", which is not wide enought for the scene of dimension "
				+this.getXSize ()+","+this.getYSize ()+", with a xFVSSampleBegin of "+xFVSSampleBegin+" m");
		}
		List<FiPlant> additionalTreeSet = new ArrayList<FiPlant>();
		Map<FiSpecies, Double> fvsSampleBasalAreaPerSpecies = new HashMap<FiSpecies, Double>();
		Map<FiSpecies, Double> sceneBasalAreaPerSpecies = new HashMap<FiSpecies, Double>();
		// move the SVS trees to their right position (x>=xFVSSampleBegin and centered)
		//System.out.println("	moving trees and computing a first guess of the additionaltreelist to put around FVS trees (respecting basalarea of each species"); 
		for (Tree tree: this.getTrees ()) {
			SFTree plant = (SFTree)tree;
			// move the tree from the FVS sample to the right position
			plant.setXYZ (plant.getX() + xFVSSampleBegin, plant.getY() + 0.5 * (this.getYSize () - fvsSceneSize), plant.getZ());

			// compute basalArea for fvsSample and whole scene
			double fvsSampleBasalArea =0d;
			double sceneBasalArea =0d;
			if (fvsSampleBasalAreaPerSpecies.containsKey (plant.getSpecies())) {
				fvsSampleBasalArea+=fvsSampleBasalAreaPerSpecies.get (plant.getSpecies());
				sceneBasalArea+=sceneBasalAreaPerSpecies.get (plant.getSpecies());
			}
			// update maps of basal area 
			fvsSampleBasalAreaPerSpecies.put(plant.getSpecies(), fvsSampleBasalArea + 0.25 * Math.pow(plant.getDbh (),2d) * Math.PI / ONE_ACRE);
			sceneBasalAreaPerSpecies.put(plant.getSpecies(), sceneBasalArea + 0.25 * Math.pow(plant.getDbh (),2d) * Math.PI /this.getArea());
			//			if (extendFVSSample) {
			//				for (int fact=0; fact<treeFactor;fact++) {
			//					SFTree newTree = (SFTree) plant.clone ();
			//					newTree.setXYZ (0d, 0d, 0d);
			//					additionalTreeSet.add (newTree);
			//				}
			//			}
		}
		System.out.println("	fvsSampleBasalAreaPerSpecies:"+fvsSampleBasalAreaPerSpecies);
		System.out.println("	sceneBasalAreaPerSpecies:"+sceneBasalAreaPerSpecies);
//		int ntree=0;
//		for (Tree tree: this.getTrees ()) {
//			ntree+=1;
//			SFTree plant = (SFTree)tree;
//			System.out.println("	tree "+ntree+":"+plant.getX()+","+plant.getY()+",rad="+plant.getCrownRadius ()+",height="+plant.getHeight ());
//		}
		//if (extendFVSSample) {
			//System.out.println("	completing the additionaltreelist to put around FVS trees (respecting basalarea of each species): initial tree number "+this.getTrees().size ());
			// complete the additionalTreeSet per species until the basal area per species is reached
			for (FiSpecies sp : sceneBasalAreaPerSpecies.keySet ()) {
				List<SFTree> treesOfSp = this.getTrees (sp);
				//System.out.println(sp.getName()+" has an initial basal area of "+initialBasalAreaPerSpecies.get (sp)+" and contains "+treesOfSp.size()+ " trees");
				int treeTested = 0;
				while (sceneBasalAreaPerSpecies.get(sp) < fvsSampleBasalAreaPerSpecies.get (sp) & treeTested<100) {
					//System.out.println("	species "+sp.getName ()+" initial basal area="+initialBasalAreaPerSpecies.get (sp)+" current basal area="+basalAreaPerSpecies.get (sp));
					Random rand = new Random();
					int itree = rand.nextInt(treesOfSp.size ());
					double treeBasalArea = 0.25 * Math.pow(treesOfSp.get (itree).getDbh (),2d) * Math.PI  / this.getArea();
					if (treeBasalArea + sceneBasalAreaPerSpecies.get(sp) < fvsSampleBasalAreaPerSpecies.get (sp) * 1.01) { // we authorize until 1% of extra basal area
						//System.out.println(" sourcetreeCBD="+treesOfSp.get (itree).getCrownBaseHeight()+","+treesOfSp.get (itree).getCrownBaseHeightBeforePruning());
						SFTree newTree = treesOfSp.get (itree).putCopyIn (this);
						additionalTreeSet.add (newTree);
						addTree(newTree);
						sceneBasalAreaPerSpecies.put(sp, sceneBasalAreaPerSpecies.get(sp) + treeBasalArea);
						treeTested = 0;
					} else {
						treeTested += 1; 
					}
					//System.out.println("itree="+itree+",treeTested="+treeTested+",basalarea="+basalAreaPerSpecies.get(sp)+",target="+initialBasalAreaPerSpecies.get(sp)+",treebasal="+treeBasalArea+",treefactor="+treeFactor);
				}
			}
			//System.out.println("	end completing the additionaltreelist to put around FVS trees (respecting basalarea of each species): final tree number "+(this.getTrees().size ()+additionalTreeSet.size ()));
			System.out.println("	final sceneBasalAreaPerSpecies:"+sceneBasalAreaPerSpecies);
			System.out.println("	final tree number: "+(this.getTrees().size ()+additionalTreeSet.size ())+" (fvs sample is "+this.getTrees().size ()+")");
			
//			for (Tree tree: this.getTrees ()) {
//				ntree+=1;
//				SFTree plant = (SFTree)tree;
//				System.out.println("	tree "+ntree+":"+plant.getX()+","+plant.getY()+",rad="+plant.getCrownRadius ()+",height="+plant.getHeight ());
//			}
			
			
			// generation of tree position
			// computation of the polygon around fvs sample
			List<Vertex3d> vertices = new ArrayList<Vertex3d>();
			Vertex3d origin = this.getOrigin ();
			vertices.add (origin);
			vertices.add (new Vertex3d(origin.x + this.getXSize (), origin.y, origin.z));
			vertices.add (new Vertex3d(origin.x + this.getXSize (), origin.y + 0.5 * (this.getYSize () + fvsSceneSize), origin.z));
			vertices.add (new Vertex3d(origin.x + xFVSSampleBegin + fvsSceneSize , origin.y + 0.5 * (this.getYSize () + fvsSceneSize), origin.z));
			vertices.add (new Vertex3d(origin.x + xFVSSampleBegin + fvsSceneSize , origin.y + 0.5 * (this.getYSize () - fvsSceneSize), origin.z));
			vertices.add (new Vertex3d(origin.x + xFVSSampleBegin , origin.y + 0.5 * (this.getYSize () - fvsSceneSize), origin.z));
			vertices.add (new Vertex3d(origin.x + xFVSSampleBegin , origin.y + 0.5 * (this.getYSize () + fvsSceneSize), origin.z));
			vertices.add (new Vertex3d(origin.x + this.getXSize () , origin.y + 0.5 * (this.getYSize () + fvsSceneSize), origin.z));
			vertices.add (new Vertex3d(origin.x + this.getXSize () , origin.y + this.getYSize (), origin.z));
			vertices.add (new Vertex3d(origin.x, origin.y + this.getYSize (), origin.z));
			Polygon polygon =new Polygon(vertices);
			System.out.println("	Trying populating additional trees in the polygon "+polygon.getMin ()+","+polygon.getMax ()+ " with pattern "+extendFVSSampleSpatialOption);
			FiPlantPopulationGenerator pg = new FiPlantPopulationGenerator (additionalTreeSet, polygon, rnd);
			if (extendFVSSampleSpatialOption==0) { 
				pg.setSpatialDistribution (pg.RANDOM, 0d);
			} else if (extendFVSSampleSpatialOption==1) {
				pg.setSpatialDistribution (pg.GIBBS, 1d);
			} else if (extendFVSSampleSpatialOption==2) {
				pg.setSpatialDistribution (pg.HARDCORE, 0d);
			}
		//}
//		ntree=0;
//		for (Tree tree: this.getTrees ()) {
//			ntree+=1;
//			SFTree plant = (SFTree)tree;
//			System.out.println("	tree "+ntree+":"+plant.getX()+","+plant.getY()+",rad="+plant.getCrownRadius ()+",height="+plant.getHeight ());
//		}

	}
	
	/**
	 * This method is used to work on scene smaller than 
	 */
	public void reduceSceneTo(double sceneOriginX, double sceneOriginY, double sceneSizeX, double sceneSizeY) {
		if (sceneOriginX!=this.getOrigin().x || sceneOriginY!=this.getOrigin().y) {
			System.out.println("WARNING: fvsScene scene origin  is currently "+this.getOrigin().x+","+this.getOrigin().y+" and will be truncated to "+sceneOriginX+","+sceneOriginY);
			this.setOrigin (new Vertex3d (sceneOriginX, sceneOriginY, 0));
		} 
		if (sceneSizeX!=this.getXSize() || sceneSizeY!=this.getYSize()) {
			System.out.println("WARNING: fvsScene dimension is currently "+this.getXSize()+","+this.getYSize()+" and will be truncated to "+sceneSizeX+","+sceneSizeY);
			this.setXSize (sceneSizeX);
			this.setYSize (sceneSizeY);
			this.setArea (this.getXSize () * this.getYSize ());			
		}
//		for (Iterator iter = this.getTrees().iterator(); iter.hasNext();) {
//			SFTree pt = (SFTree) iter.next();
//			if (!pt.isInRectangle(sceneOriginX, sceneOriginX + sceneSizeX, sceneOriginY, sceneOriginY + sceneSizeY)) {
//				this.removeTree(pt);
//			}
//		}
		
	}


}
