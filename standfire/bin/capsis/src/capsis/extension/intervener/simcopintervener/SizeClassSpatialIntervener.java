package capsis.extension.intervener.simcopintervener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import jeeb.lib.util.Log;
import jeeb.lib.util.MessageDialog;
import jeeb.lib.util.Translator;
import capsis.extension.intervener.simcopintervener.SizeClassGenericEnums.AlgorithmType;
import capsis.extension.intervener.simcopintervener.SizeClassGenericEnums.AutoThinTargetValueType;
import capsis.extension.intervener.simcopintervener.SizeClassGenericEnums.AverageDistanceComputationMethod;
import capsis.extension.intervener.simcopintervener.SizeClassGenericEnums.SizeClassType;
import capsis.extension.intervener.simcopintervener.gui.SizeClassSpatialIntervenerDialog;
import capsis.extension.intervener.simcopintervener.gui.SizeClassSpatialPickerConfigPanel;
import capsis.extension.intervener.simcopintervener.picker.PickerStand;
import capsis.extension.intervener.simcopintervener.picker.PickerTree;
import capsis.extension.intervener.simcopintervener.picker.SimcopTreePicker;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.Step;
import capsis.kernel.automation.Automatable;
import capsis.kernel.extensiontype.Intervener;
import capsis.util.Group;
import capsis.util.GroupableIntervener;

/**
 * SimcopSizeClassSpatialIntervener is a thinner based on JM Ottorini design of a volume class and
 * spatial constraint tree picker
 * 
 * @author T. Bronner March 2013, we made it reusable for other models F. de Coligny, M. Pulkkinen - June 2014
 */
public class SizeClassSpatialIntervener implements Intervener, GroupableIntervener, Automatable {

	static {
		Translator.addBundle ("simcop.SimcopLabels");
	}
	public static final String NAME = "SimcopSizeClassSpatialIntervener";
	public static final String VERSION = "1.0";
	public static final String AUTHOR = "T. Bronner";
	public static final String DESCRIPTION = "SimcopSizeClassSpatialIntervener.description";
	static public final String SUBTYPE = "SelectiveThinner";

	private boolean constructionCompleted = false; // if cancel in interactive mode, false
	private PickerStand scene; // reference scene: will be altered by apply ()
	private GModel model;
	protected Collection<PickerTree> concernedTrees;
	private AlgorithmType algoType;
	private AutoThinTargetValueType targetValueType = AutoThinTargetValueType.DensityAfterThinning;
	private double targetDensity;
	private int targetNumberOfTree;
	private boolean wheightByFrequencies;
	private boolean doSecondPass;
	private SizeClassType sizeClassType = SizeClassType.DiameterClass;
	private AverageDistanceComputationMethod avgDistanceMethod;
	private List<Double> distanceFactors;
	private List<Double> sizeClassesWheights;
	private SizeClassSpatialIntervenerDialog dialog;
	private boolean interactiveMode = false;
	private SimcopTreePicker picker;
	private boolean integerBounds;

	public SizeClassSpatialIntervener (double targetDensity, AlgorithmType algoType, SizeClassType sizeClassType,
			List<Double> sizeClassesWheights, List<Double> distanceFactors, boolean wheightByFrequencies,
			boolean doSecondPass, AverageDistanceComputationMethod method, boolean integerBounds) {
		targetValueType = AutoThinTargetValueType.DensityAfterThinning;
		this.constructionCompleted = true;
		this.targetDensity = targetDensity;
		this.sizeClassesWheights = sizeClassesWheights;
		this.distanceFactors = distanceFactors;
		this.avgDistanceMethod = method;
		this.wheightByFrequencies = wheightByFrequencies;
		this.doSecondPass = doSecondPass;
		this.sizeClassType = sizeClassType;
		this.algoType = algoType;
		this.integerBounds = integerBounds;
	}

	public SizeClassSpatialIntervener (int targetNumberOfTrees, AlgorithmType algoType,
			SizeClassType sizeClassType, List<Double> sizeClassesWheights, List<Double> distanceFactors,
			boolean wheightByFrequencies, boolean doSecondPass, AverageDistanceComputationMethod method,
			boolean integerBounds) {
		targetValueType = AutoThinTargetValueType.NbTreesToPickPerHa;
		this.constructionCompleted = true;
		this.targetNumberOfTree = targetNumberOfTrees;
		this.sizeClassesWheights = sizeClassesWheights;
		this.distanceFactors = distanceFactors;
		this.avgDistanceMethod = method;
		this.wheightByFrequencies = wheightByFrequencies;
		this.doSecondPass = doSecondPass;
		this.sizeClassType = sizeClassType;
		this.algoType = algoType;
		this.integerBounds = integerBounds;
	}

	/**
	 * Init the thinner on a given scene.
	 */
	@Override
	public void init (GModel model, Step s, GScene gscene, Collection c) {
		this.scene = (PickerStand) gscene; // this is referentScene.getInterventionBase ();
		this.model = model;
		if (c == null) {
			if (scene instanceof PickerStand) {
				concernedTrees = new ArrayList<PickerTree> (); // all trees
				for (PickerTree t : this.scene.getPickerTrees ()) {
					concernedTrees.add (t);
				}

			} else {
				constructionCompleted = false;
				return;
			}
		} else {
			// concernedTrees = c; // restrict to the given collection
			concernedTrees = new ArrayList<PickerTree> ();
			for (Object o : c) {
				concernedTrees.add ((PickerTree) o);
			}

		}
		constructionCompleted = true;
	}

	/**
	 * Open a dialog to tune the thinner.
	 */
	@Override
	public boolean initGUI () throws Exception {
		// Interactive start
		interactiveMode = true;
		dialog = new SizeClassSpatialIntervenerDialog (this);
		constructionCompleted = false;
		if (dialog.isValidDialog ()) {
			// Valid -> ok was hit and all checks were ok
			constructionCompleted = true;
		}
		return constructionCompleted;
	}

	/**
	 * Extension dynamic compatibility mechanism. This matchwith method checks if the extension can
	 * deal (i.e. is compatible) with the referent.
	 */
	static public boolean matchWith (Object referent) {
		try {

			GModel m = (GModel) referent;

			GScene scene = m.getProject ().getRoot ().getScene ();

			return scene instanceof PickerStand;

			// if (!(referent instanceof SimcopModel)) {
			// return false;
			// }

		} catch (Exception e) {
			Log.println (Log.ERROR, "SimcopSizeClassSpatialIntervener.matchWith ()", "Err&or in matchWith () (returned false)", e);
			return false;
		}

		// return true;
	}

	/**
	 * GroupableIntervener interface. This intervener acts on trees, tree groups can be processed.
	 */
	public String getGrouperType () {
		return Group.TREE;
	}

	/**
	 * These checks are done at the beginning of apply (). They are run in interactive AND script
	 * mode.
	 */
	@Override
	public boolean isReadyToApply () {
		if (!constructionCompleted) {
			// If cancel on dialog in interactive mode -> constructionCompleted = false
			Log.println (Log.ERROR, "SimcopSizeClassSpatialIntervener.isReadyToApply ()", "constructionCompleted is false. SimcopSizeClassSpatialIntervener is not appliable.");
			return false;
		}
		if (interactiveMode) {
			if (dialog == null) { return false; }
			SizeClassSpatialPickerConfigPanel configPanel = dialog.getSizeClassDistanceSelectorConfigPanel ();
			if (configPanel == null) { return false; }
			targetDensity = configPanel.getTargetDensity ();
			targetNumberOfTree = configPanel.getTargetNumberOfTrees ();
			avgDistanceMethod = configPanel.getAverageDistanceComputationMethod ();
			algoType = configPanel.getAlgoType ();
			sizeClassesWheights = configPanel.getSizeClassesFactors ();
			distanceFactors = configPanel.getDistanceFactors ();
			sizeClassType = configPanel.getSizeClassType ();
			wheightByFrequencies = configPanel.isWheighedtByFrequencies ();
			doSecondPass = configPanel.isDoSecondPass ();
			targetValueType = configPanel.getTargetValueType ();
			integerBounds = configPanel.isIntegerBounds ();
			return true;
		} else {
			return true;
		}
	}

	/**
	 * Makes the action: thinning.
	 */
	@Override
	public Object apply () throws Exception {
		// Check if apply is possible
		if (!isReadyToApply ()) { throw new Exception (
				"SimcopSizeClassSpatialIntervener.apply () - Wrong input parameters, see Log"); }
		scene.setInterventionResult (true);
		if (targetValueType == AutoThinTargetValueType.DensityAfterThinning) {
			picker = new SimcopTreePicker (concernedTrees, scene, targetDensity, avgDistanceMethod);
		} else {
			picker = new SimcopTreePicker (concernedTrees, scene, targetNumberOfTree, avgDistanceMethod);
		}
		picker.methodJMOMPMerge (algoType, sizeClassesWheights, distanceFactors, sizeClassType, integerBounds, wheightByFrequencies, doSecondPass);
		if (interactiveMode) {
			String logMessages = picker.getLogMessages ();
			if (logMessages != null) {
				MessageDialog.print (this, logMessages);
			}
		}
		Collection toCut = picker.getPickedTrees ();
		// iterate and cut
		for (PickerTree t : concernedTrees) {
			// do we cut this tree ?
			if (toCut.contains (t)) { // yes
				if (!model.isMarkModel ()) {
					scene.removeTree (t);
				} else {
					t.setMarked (true);
				}
			}
		}
		return scene;
	}

	public String getLogMessages () {
		if (picker != null) { return picker.getLogMessages (); }
		return null;
	}

	/**
	 * Intervener
	 */
	@Override
	public void activate () {
		// not used
	}

	
	public PickerStand getScene () {
		return scene;
	}

}
