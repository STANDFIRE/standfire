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

package capsis.extension.treelogger.geolog;

import java.util.Collection;
import java.util.Random;

import jeeb.lib.util.Vertex3d;
import repicea.simulation.treelogger.WoodPiece.Property;
import capsis.defaulttype.Tree;
import capsis.extension.treelogger.GContour;
import capsis.extension.treelogger.GPiece;
import capsis.extension.treelogger.GPieceDisc;
import capsis.extension.treelogger.GPieceRing;
import capsis.extension.treelogger.geolog.logcategories.Tester;
import capsis.extension.treelogger.geolog.logcategories.TesterLibrary.DiameterTester;
import capsis.extension.treelogger.geolog.util.KnottyCoreProfile;
import capsis.extension.treelogger.geolog.util.LongitProfile;
import capsis.extension.treelogger.geolog.util.LongitProfile_Null;
import capsis.extension.treelogger.geolog.util.TreeHistory;
import capsis.kernel.MethodProvider;
import capsis.kernel.Step;
import capsis.util.methodprovider.TreeCrownBaseHeightProvider;
import capsis.util.methodprovider.TreeRadius_cmProvider;

/**	
 * The GeoLogTreeData class handles all the profile required to process the Tree instance in GeoLog. It has a knot profile,
 * a heartwood profile, a profile for the lowest dead branch and a juvenile wood profile.
 * @author F. Mothe - august 2006
 * @author M. Fortin - November 2011 (refactoring)
 */
public class GeoLogTreeData {
	
	// Random tree  :
	public static final int NB_RANDOM_ATTRIBUTES = 3;
	
	private static final double MAX_ITERATION_LENGTH = 50;
	protected final static double DEFAULT_PRECISION_LENGTH_m = 1e-3;
	protected final static double DEFAULT_PRECISION_THICKNESS_m = .10;

	private Tree tree;
	private Tree[] history;
	private TreeRadius_cmProvider radMp;
	private LongitProfile knotProfile;
	private LongitProfile heartProfile;
	private LongitProfile firstDeadBranchProfile;
	private LongitProfile juveProfile;
	private double crownBaseHeight_m;
	private String species;
	
	private double randomAttributes[];	// NB_RANDOM_ATTRIBUTES from 0 to 1

	private LoggingContext loggingContext;
	
	
	/**
	 * Official constructor
	 * @param tree a Tree instance that serves as reference for this GeoLogTreeData instance
	 * @param stepsFromRoot a Collection of Steps from the root to the current step
	 * @param mp a TreeRadius_cmProvider instance
	 * @param species a String that defines the species
	 */
	protected GeoLogTreeData(Tree tree, Collection <Step> stepsFromRoot, TreeRadius_cmProvider mp, String species) {
		this.tree = tree;
		this.radMp = mp;
		this.species = species;
		
		setHeartProfile(new LongitProfile_Null());
		setLowestDeadBranchProfile(new LongitProfile_Null());
		setJuvenileWoodProfile(new LongitProfile_Null());

		randomAttributes = new double [NB_RANDOM_ATTRIBUTES];

		if (radMp instanceof TreeCrownBaseHeightProvider) {
			crownBaseHeight_m = ((TreeCrownBaseHeightProvider) radMp).getTreeCrownBaseHeight(tree);
		} else {
			crownBaseHeight_m = tree.getHeight();
		}
		
		loggingContext = new LoggingContext(0d, tree.getHeight(), crownBaseHeight_m);
		
		if (stepsFromRoot != null && !stepsFromRoot.isEmpty()) {
			setTreeHistory(new TreeHistory(tree, stepsFromRoot).getHistory());
			initKnotProfile(radMp);
		} 
	}


	// TEMPO : for building a fake td in ProductUtil.getVolume_m3 ()
	public static GeoLogTreeData makeFakeTreeData(Tree tree, TreeRadius_cmProvider radMp) {
		return new GeoLogTreeData(tree, null, radMp, "");
	}

	
	/**
	 * This method returns the Tree instance associated with this GeoLogTreeData object.
	 * @return a Tree instance
	 */
	public Tree getTree() {return tree;}
	
	
	/**
	 * This method returns the logging context associated with this GeoLogTreeData instance
	 * @return a LoggingContext instance
	 */
	public LoggingContext getLoggingContext() {return loggingContext;}
	
	
	/**
	 * This method returns the TreeRadius_cmProvider instance associated with this GeoLogTreeData object.
	 * @return a TreeRadius_cmProvider instance
	 */
	public TreeRadius_cmProvider getTreeRadiusMethodProvider() {return radMp;}
	
	
	/**
	 * This method returns all the Tree instances that represent the past state of this Tree object. 
	 * @return an array of Tree instances
	 */
	public Tree[] getTreeHistory() {return history;}
	
	
	/**
	 * This method sets the history of the tree member.
	 * @param history an array of Tree instances
	 */
	protected void setTreeHistory(Tree[] history) {this.history = history;}
	
	
	/**
	 * This method returns the knot profile of the tree member.
	 * @return a LongitProfile instance
	 */
	protected LongitProfile getKnotProfile() {return knotProfile;}
	
	
	/**
	 * This method returns the profile of the heartwood.
	 * @return a LongitProfile instance
	 */
	protected LongitProfile getHeartProfile() {return heartProfile;}
	

	/**
	 * This method sets the heart profile of this GeoLogTreeData instance.
	 * @param heartProfile a LongitProfile instance
	 */
	protected void setHeartProfile(LongitProfile heartProfile) {this.heartProfile = heartProfile;}
	
	
	/**
	 * This method returns the profile of the lowest dead branch.
	 * @return a LongitProfile
	 */
	protected LongitProfile getLowestDeadBranchProfile() {return firstDeadBranchProfile;}
	
	
	/**
	 * This method sets the profile of the lowest dead branch.
	 * @param lowestDeadBranchProfile a LongitProfile instance
	 */
	protected void setLowestDeadBranchProfile(LongitProfile lowestDeadBranchProfile) {this.firstDeadBranchProfile = lowestDeadBranchProfile;} 
	
	
	/**
	 * This method returns the profile of juvenile wood.
	 * @return a LongitProfile instance
	 */
	protected LongitProfile getJuvenileWoodProfile() {return juveProfile;}
	
	
	/**
	 * This method sets the profile of juvenile wood.
	 * @param juvenileWoodProfile a Longit instance
	 */
	protected void setJuvenileWoodProfile(LongitProfile juvenileWoodProfile) {this.juveProfile = juvenileWoodProfile;}
	
	
	/**
	 * This method returns the crown base height of this tree (m).
	 * @return a double
	 */
	public double getCrownBaseHeight() {return crownBaseHeight_m;}
	
	
	/**
	 * This method returns the species of the tree member.
	 * @return a String
	 */
	protected String getSpecies() {return species;}
	
	protected void initKnotProfile(MethodProvider mp) {
		if (mp instanceof TreeCrownBaseHeightProvider) {
			TreeCrownBaseHeightProvider cbhMp =	(TreeCrownBaseHeightProvider) mp;
			knotProfile = new KnottyCoreProfile (history, radMp, cbhMp);
			crownBaseHeight_m = ((KnottyCoreProfile) knotProfile).getCrownBaseHeight_m(tree.getAge());
		} else {
			knotProfile = new LongitProfile_Null ();
			crownBaseHeight_m = tree.getHeight();
		}
	}

	protected void initialiseRandomAttributes(Random random) {
		for (int n=0; n<NB_RANDOM_ATTRIBUTES; ++n) {
			randomAttributes [n] = random.nextDouble ();	// 0 to 1
			// randomAttributes [n] = random.nextGaussian ();	// mean 0, stdev = 1
		}
	}

	
	/**
	 * This method returns the name of this GeoLogTreeData instance. By default, it returns the species.
	 * @return a String
	 */
	protected String getShortName() {
		return getSpecies();
	}

	
	/**
	 * This method returns the crown expansion factor. The area expansion factor is now included in the WoodPiece class in the getAreaExpansionFactor() method. 
	 * If not overriden in derived class, this method returns 1 by default.
	 * @param botHeight_m the height of the bottom section (m)
	 * @param topHeight_m the height of the top section (m)
	 * @return the crown expansion factor (a double)
	 */
	protected double getCrownExpansionFactor(double botHeight_m, double topHeight_m) {
		return 1d;
	}

	// Utility methods for symplifying the syntax :

	public double getTreeRadius_cm(double height_m, boolean overBark) {
		return getTreeRadiusMethodProvider().getTreeRadius_cm(tree, height_m, overBark);
	}

	public double getKnottyCoreRadius_mm (double height_m) {
//		return getKnotProfile().getRadius_mm (history, radMp, height_m);
		return getKnotProfile().getRadius_mm(this, height_m);
	}

	public double getHeartWoodRadius_mm(double height_m) {
//		return getHeartProfile().getRadius_mm (history, radMp, height_m);
		return getHeartProfile().getRadius_mm(this, height_m);
	}

	public double getJuvenileWoodRadius_mm(double height_m) {
//		return getJuvenileWoodProfile().getRadius_mm (history, radMp, height_m);
		return getJuvenileWoodProfile().getRadius_mm(this, height_m);
	}

	public double getRandomAttribute(int num) {
		return randomAttributes [num];
	}

//	public double getFirstDeadBranchRadius_mm(double height_m) {
//		return firstDeadBranchProfile.getRadius_mm (history, radMp, height_m);
//	}
	
//	public double getKnottyCoreRadius_mm (RadialProfile profile) {
//		return knotProfile.getRadius_mm (profile);
//	}

//	public double getFirstDeadBranchRadius_mm (RadialProfile profile) {
//		return firstDeadBranchProfile.getRadius_mm (profile);
//	}

//	public double getHeartWoodRadius_mm (RadialProfile profile) {
//		return heartProfile.getRadius_mm (profile);
//	}

//	public double getJuvenileWoodRadius_mm (RadialProfile profile) {
//		return juveProfile.getRadius_mm (profile);
//	}

	
	//	Add properties to piece
	//	(discs and rings should have been yet completed)
	/**
	 * This method sets the properties of a piece using this GeoLogTreeData instance.
	 * @param piece the piece to be set
	 */
	private void setPropertiesForThisPiece(GPiece piece) {
		// Median diameter :
		double medianHeight_m = piece.getHeightOfBottomSectionM() + .5 * piece.getLengthM();
		double medianDiamOverBark_cm = getTreeRadius_cm(medianHeight_m, true) * 2;
		piece.setProperty(Property.medianDiameter_cm, medianDiamOverBark_cm);
	}

	//	Add rings to disc
	/**
	 * This method adds the rings to a particular disc using this GeoLogTreeData instance
	 * @param disc the disc to which the rings are to be added
	 */
	private void setRingsForThisDisc(GPieceDisc disc) {
		double centreX_mm = 0;		// Fagac�es simulates straight pith
		double centreZ_mm = 0;
		double discHeight_m = disc.getHeight_m(); //PieceUtil.getHeight_m (disc);	// disc.centreY_mm / 1000;

//		RadialProfile profile = new RadialProfile(getTreeHistory(), discHeight_m, getTreeRadiusMethodProvider());
		RadialProfile profile = getRadialProfileAtThisHeight(discHeight_m);

		int nbRings = profile.getNbRings ();
		if (nbRings >= 3) {
			// General case
			// Rings must be added from bark to pith
			for (int cambialAge = 0; cambialAge<nbRings; cambialAge++) {
				int ringId = cambialAge + 1;	// id 1 = bark (age = 0)
				double r_mm = profile.getRadius_mm (cambialAge);
				disc.addRing (new GPieceRing (
						ringId, disc.getId(), centreX_mm, centreZ_mm, r_mm) );
			}
		} else {
			// No rings (e.g. tree top) or 2 rings (pith and bark) without wood
			// rings but GPieceDisc needs at least 3 rings.
			// We add bark, a fake wood ring and pith :
			double rBark = 0.0;
			double rPith = 0.0;
			if (nbRings == 2) {
				// (normally, nbRings == 1 should not occur)
				rBark = profile.getRadius_mm (0);
				rPith = profile.getRadius_mm (1);
			}
			double rFake = rBark;	// ??
			int ringId = 1;
			disc.addRing (new GPieceRing (
					ringId++, disc.getId(), centreX_mm, centreZ_mm, rBark) );
			disc.addRing (new GPieceRing (
					ringId++, disc.getId(), centreX_mm, centreZ_mm, rFake) );
			disc.addRing (new GPieceRing (
					ringId++, disc.getId(), centreX_mm, centreZ_mm, rPith) );
		}
		
		if (!getHeartProfile().isNull()) {
			disc.getContours().put ("heartWood", new GContour(centreX_mm, centreZ_mm, getHeartProfile().getRadius_mm(profile)));
		}
		
		if (!getKnotProfile().isNull()) {
			disc.getContours().put ("crownBase", new GContour (centreX_mm, centreZ_mm, getKnotProfile().getRadius_mm (profile)));
		}
		
		if (!getLowestDeadBranchProfile().isNull()) {
			disc.getContours().put ("firstDeadBranch", new GContour (centreX_mm, centreZ_mm, getLowestDeadBranchProfile().getRadius_mm (profile)));
		}
		
		if (!getJuvenileWoodProfile().isNull()) {
			disc.getContours().put ("juvenileWood", new GContour (centreX_mm, centreZ_mm, getJuvenileWoodProfile().getRadius_mm (profile)));
		}
		
	}
	
	
	//	Add nbDiscs (>2) discs and pith points to piece
	//	with disc id=1 at bottom and disc id=nbDiscs at top
	/**
	 * This method sets the different discs for this particular piece according to a given interval. It also sets the properties for this piece.
	 * @param piece the piece for which the discs are going to be set
	 * @param discInterval_m the interval between the discs (m)
	 */
	protected void setDiscsForThisPiece(GPiece piece, double discInterval_m) {

		double centreX_mm = 0;		// Fagac�es simulates straight pith
		double centreZ_mm = 0;

		double h0_mm = piece.getHeightOfBottomSectionM() * 1000;
		double pieceLength_mm = piece.getLengthM() * 1000;

		int nbDiscs = (discInterval_m < .001) ? 2 : (int) Math.ceil(pieceLength_mm *.001 / discInterval_m) + 1;
		if (nbDiscs < 2) {
			nbDiscs = 2;
		}

		int discId = 1 ;
		for (int d = 0; d < nbDiscs; d++) {
			double discHeight_mm =  h0_mm + d * pieceLength_mm / (nbDiscs-1);

			// Pith:
			Vertex3d pith = new Vertex3d (
					centreX_mm, discHeight_mm, centreZ_mm);
			piece.addPithPoint(pith);

			// Disc:
			GPieceDisc disc = new GPieceDisc (discId++, discHeight_mm);
			piece.addDisc(disc);
			setRingsForThisDisc(disc);
		}
		
		setPropertiesForThisPiece(piece);
		
	}


	
	
	// Dichotomic search of maximal length between lengthMin and
	// lengthMax such as test.isValid (length) == true.
	// Preconditions :
	// - test.isValid (lengthMin) should be true
	// - test.isValid (lengthMax) should be false
	private double getMaxLength(Tester test, double lengthMin, double lengthMax, double precisionLength_m) {
		boolean convergence = false;
		for (int cpt = 0; cpt < MAX_ITERATION_LENGTH; cpt++) {
			double length = (lengthMin + lengthMax) / 2.;
			if (test.isValid (length)) {
				lengthMin = length;
			} else {
				lengthMax = length;
			}
			if (lengthMax - lengthMin < precisionLength_m) {
				convergence = true;
				break;
			}
		}
		if (!convergence) {
			System.out.println ("WARNING : divergence in getMaxLength for precision = "
					+ precisionLength_m);
		}
		// Returns lengthMin because test.isValid (length) may be false
		return lengthMin;

	}

	
	/**
	 * This method searches the maximal length between the parameters lengthMin and lengthMax such 
	 * that it satisfies the validity criterion. It first checks if test.isValid(lengthMin) returns true while  
	 * test.isValid(lengthMax) returns false before launching the dichotomic search. It calls the method setLength in
	 * the loggingContext member and returns true if a valid log can be cut.
	 * @param test a Tester instance
	 * @param lengthMin the minimum length (m)
	 * @param lengthMax the maximum length (m)
	 * @param precisionLength_m the precision (m), the default value is 1e-3
	 * @return true if a log can be cut or false otherwise
	 */
	public boolean testMaxLength(Tester test, double lengthMin, double lengthMax, double precisionLength_m) {
		LoggingContext lc = getLoggingContext();
		boolean valid = test.isValid(lengthMin);
		if (valid) {
			double length;
			if ((lengthMax <= lengthMin) || test.isValid (lengthMax)) {
				length = lengthMax;	// should be >= lengthMin
			} else {
				length = getMaxLength(test, lengthMin, lengthMax, precisionLength_m);
			}
			
			lc.setLength (length);
			// added 13/02/2009 to prevent potential infinite loop :
			valid = length > 0.;
		}
		// System.out.println ("testMaxLength :> valid=" + valid + " l=" + lc.getLength ());
		return valid;
	}


	// Returns the height where the tree stem reaches diameter_cm
	// (or 0.0 if it the tree is too small)
	public double getMaxHeight_m(double diameter_cm, boolean diamOverBark, double precisionLength_m) {
		if (precisionLength_m <= 0.) {
			precisionLength_m = DEFAULT_PRECISION_LENGTH_m;
		}

		double height = 0.0;

		LoggingContext lc = getLoggingContext();
		DiameterTester test = new DiameterTester(this, 0.0, diameter_cm, 1.0, diamOverBark);

		if (testMaxLength(test, 0.0, getTree().getHeight (), precisionLength_m)) {
			height = lc.getLength ();	// (+ lc.getHeight () ==0.0)
		}
		return height;
	}
	
	
	/**
	 * This method returns the volume between two cross sections by trapezoidal integration. 
	 * @param botHeight_m the height of the lower cross section (m)
	 * @param topHeight_m the height of the upper cross section (m)
	 * @param overBark true to calculate the volume over bark or false otherwise
	 * @param precisionThickness_m the precision of the integral (set to 0.1 m)
	 * @return the volume (m3)
	 */
	public double getVolume_m3 (double botHeight_m, 
			double topHeight_m, 
			boolean overBark, 
			double precisionThickness_m) {
		
		if (precisionThickness_m <= 0.) {
			precisionThickness_m = DEFAULT_PRECISION_THICKNESS_m;
		}
		
		// Trapezoid integration :
		// TODO : optimise, replace by a standard algo...
		double volume = 0.0;
		double length = topHeight_m - botHeight_m;
		if (length > 0.0) {
			int nbSlices = Math.max (2, 1 + (int) (length / precisionThickness_m));
			double thickness = length / (nbSlices - 1);
			double sum = 0.0;
			double measure1 = 0.0;
			for (int s=0; s<nbSlices; ++s) {
				double z = botHeight_m + s * thickness;
				double radius_m = getTreeRadius_cm(z, overBark) * .01;		// .01 to change from cm to m
				double measure2 = Math.PI * radius_m * radius_m;
//				double measure2 = surfMeas.getMeasure (z);
				if (s > 0) {
					// sum += (measure2 + measure1) / 2.0 * thickness;
					sum += (measure2 + measure1);
				}
				measure1 = measure2;
			}
			//volume = sum;
			volume = sum / 2.0 * thickness;
		}
		return volume;
	}

	/**
	 * This method returns a radial profile at a given height.
	 * @param height the height (m)
	 * @return a RadialProfile instance
	 */
	public RadialProfile getRadialProfileAtThisHeight(double height) {
		return new RadialProfile(this, height);
	}
	
	
}

