package capsis.extension.treelogger.geolog;


/**
 * This class contains some internal GeoLogTreeData derived classes.
 * @author Fred Mothe - 2006
 * @author Mathieu Fortin - November 2011 (refactoring)
 */
@Deprecated
public class GeoLogTreeDataLibrary {
	
	
//	//	Returns the tree description required by GeoLog
//	protected static GeoLogTreeData makeTreeData(Tree tree, Collection <Step> stepsFromRoot, TreeRadius_cmProvider mp, SpecialCase special) {
//		GeoLogTreeData td;
//		switch (special) {
//		case FG_OAK :
//			td = new FgTreeData (tree, stepsFromRoot, mp, true);
//			break;
//		case FG_BEECH :
//			td = new FgTreeData (tree, stepsFromRoot, mp, false);
//			break;
//		case PP3 :
//			td = new Pp3TreeData (tree, stepsFromRoot, mp);
//			break;
//		case GENERIC :
//			td = new GenericTreeData (tree, stepsFromRoot, mp);
//			break;
//		default :
//			System.out.println ("GeoLog.makeTreeData : invalid special case");
//			td = null;
//		}
//		td.initialiseRandomAttributes(GeoLog.random);
//		return td;
//	}
	
	
//	/**
//	 * Internal class adapted to Fagacees
//	 * @author Fred Mothe - 2006
//	 * @author Mathieu Fortin - November 2011 (refactoring)
//	 */
//	static class FgTreeData extends GeoLogTreeData {
//
//		private String shortName;
//		private double crownExpansionFactor;
//
//		// TODO : should be modifiable
//		static final boolean USE_CROWN_EXPANSION_FACTOR = false;
//
//		/**
//		 * Hidden constructor.
//		 * @param tree a Tree instance
//		 * @param stepsFromRoot a Collection of Step from the root to the current step
//		 * @param mp a TreeRadius_cmProvider instance
//		 * @param isOak true if the species is sessile oak or false if the species is beech
//		 */
//		FgTreeData (Tree tree, Collection <Step> stepsFromRoot, TreeRadius_cmProvider mp, boolean isOak) {
//			super (tree, null, mp, isOak ? "sessileOak" : "beech");
//			this.shortName = isOak ? "FgOak" : "FgBeech";
//			this.crownExpansionFactor = 1.0;
//
////			boolean fullHistory = false;
////			boolean startFirstYear = false;
////			setTreeHistory(new FgTreeHistory(tree, stepsFromRoot, fullHistory, startFirstYear).getHistory());
//			setTreeHistory(new TreeHistory(tree, stepsFromRoot).getHistory());
//			initKnotProfile (mp);
//			if (isOak) {
//				//System.out.println ("Using FgOak profiles");
//				setHeartProfile(new FgOakHeartWoodProfile(getTreeHistory()));
//			} 
//			setLowestDeadBranchProfile(new FgFirstDeadBranchProfile(getTreeHistory(), mp));
//			if (USE_CROWN_EXPANSION_FACTOR) {
//				crownExpansionFactor = FgCrownExpansionFactor.getCrownExpansionFactor(this, 0., 0.);
//			}
//		}
//
//		@Override
//		protected String getShortName() {return shortName;}
//
//		@Override
//		protected double getCrownExpansionFactor(double botHeight_m, double topHeight_m) {
//			double number;
//			if (topHeight_m - botHeight_m < VERY_SMALL) {					// Very short log...
//				number = (botHeight_m <= getCrownBaseHeight()) ? 1.	: crownExpansionFactor;
//			} else {
//				double crownRatio = (topHeight_m - getCrownBaseHeight()) / (topHeight_m - botHeight_m);
//				crownRatio = Math.max (0., Math.min (1., crownRatio));
//				number = crownRatio * (crownExpansionFactor - 1.) + 1.;
//			}
//			return number;
//		}
//		
//	}
//
	
//	/**
//	 * Inner class for the implementation of Pp3Tree within GeoLog.
//	 * @author Fred Mothe
//	 * @author Mathieu Fortin - November 2011 (refactoring)
//	 */
//	static class Pp3TreeData extends GeoLogTreeData {
//
//		Pp3TreeData (Tree tree, Collection <Step> stepsFromRoot, TreeRadius_cmProvider mp) {
//			super (tree, stepsFromRoot, mp, "maritimePine");
//			setJuvenileWoodProfile(new Pp3JuveWoodProfile());
//		}
//
//		@Override
//		protected String getShortName() {return "Pp3";}
//	}
//
//
//	
//	static class GenericTreeData extends GeoLogTreeData {
//
//		GenericTreeData (Tree tree, Collection <Step> stepsFromRoot, TreeRadius_cmProvider mp) {
//			// TODO : find a way to get species in the general case
//			// "unknown" does not belong to GPieceTreeInfo.speciesName !!
//			super (tree, stepsFromRoot, mp, "unknown");
//			// heartProfile = new LongitProfile_FixRings (10, false);
//			// heartProfile = new LongitProfile_FixWidth (50.0, false);
//		}
//
//		@Override
//		protected String getShortName () {return "Unknown";}
//	}


}
