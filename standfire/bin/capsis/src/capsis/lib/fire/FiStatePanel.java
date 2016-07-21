package capsis.lib.fire;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.JTextField;

import jeeb.lib.defaulttype.Item;
import jeeb.lib.defaulttype.Type;
import jeeb.lib.sketch.kernel.SketchEvent;
import jeeb.lib.sketch.kernel.SketchLinker;
import jeeb.lib.sketch.kernel.SketchListener;
import jeeb.lib.sketch.kernel.SketchModel;
import jeeb.lib.sketch.scene.item.Polygon;
import jeeb.lib.util.Check;
import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.JWidthLabel;
import jeeb.lib.util.LinePanel;
import jeeb.lib.util.MemoPanel;
import jeeb.lib.util.MessageDialog;
import jeeb.lib.util.Settings;
import jeeb.lib.util.Translator;
import capsis.lib.fire.fuelitem.FiLayerSet;
import capsis.lib.fire.fuelitem.FiPlant;

/**
 * A panel to show the state of a fuel vegetal scene.
 * 
 * @author O. Vigy, E. Rigaud - November 2006, F. Pimont - May 2009, F. de
 *         Coligny - January 2015
 */
public class FiStatePanel extends JPanel implements SketchListener, ActionListener {

	static {
		Translator.addBundle("capsis.lib.fire.FiLabels");
	}

	private FiModel model;
	private FiStand fiStand;
	private SketchModel sketchModel;

	private FiState state;

	private Polygon selectedPolygon = null; // if one polygon is selected by the
											// user, the statepanel provide the
											// information inside this polygon

	private JTextField heightThreshold; // FP 4-05-2009 separation between tree

	private JTextField totalNumber;
	private JTextField totalCover;
	private JTextField totalLoad;
	private JTextField maxHeight;
	// and shrub strata
	private JTextField treeNumberAboveThreshold;
	private JTextField treeCover;
	private JTextField treeLoad;
	private JTextField treeLAI;

	// private JTextField domTreeSpecies;

	private JTextField shrubCover;
	private JTextField shrubLoad;
	private JTextField shrubPhytovolume;

	// private JTextField domShrubSpecies;
	// private JTextField herbsCover;

	private double heightThresholdValue;
	private NumberFormat nf;

	/**
	 * Constructor.
	 */
	public FiStatePanel(SketchModel sketchModel, FiModel model) {
		super();
		this.model = model; // FiModel
		state = new FiState((FiMethodProvider) model.getMethodProvider());

		nf = NumberFormat.getInstance(Locale.ENGLISH);
		nf.setMaximumFractionDigits(3);
		nf.setGroupingUsed(false);

		this.sketchModel = sketchModel;
		sketchModel.addSketchListener(this); // we want to know when sketchModel
												// changes
		heightThresholdValue = Settings.getProperty("fire.state.panel.threshold", FiConstants.HEIGHT_THRESHOLD);
		createUI();

	}

	/**
	 * A method to update the state panel on a given FiStand. fc-19.1.2015
	 */
	public void update(FiStand fiStand) {
		this.fiStand = fiStand;
		update();
	}

	public void update() {
		// double provThresholdValue = 2;
		if (fiStand == null)
			return;

		state.setHeightThreshold(new Double(heightThreshold.getText()));

		// FP, I commented the next line to load an SVS file from Kyle
		state.update(fiStand, model, selectedPolygon);

		totalNumber.setText("" + state.getTotalNumber());
		totalCover.setText(nf.format(state.getTotalCover()));
		totalLoad.setText(nf.format(state.getTotalLoad()));
		maxHeight.setText(nf.format(state.getMaxHeight()));

		treeNumberAboveThreshold.setText("" + state.getTreeNumberAboveThreshold(heightThresholdValue));
		treeCover.setText(nf.format(state.getTreeCover()));
		treeLoad.setText(nf.format(state.getTreeLoad()));
		treeLAI.setText(nf.format(state.getTreeLAI()));

		// domTreeSpecies.setText (""+state.getDomTreeSpecies ());

		shrubCover.setText(nf.format(state.getShrubCover()));
		shrubLoad.setText(nf.format(state.getShrubLoad()));
		shrubPhytovolume.setText(nf.format(state.getShrubPhytovolume()));

		// domShrubSpecies.setText (""+state.getDomShrubSpecies ());
		// herbsCover.setText(nf.format(state.getHerbsCover()));
	}

	// fc-13.3.2007
	private Collection<FiPlant> getFiPlants(Collection<Item> items) {
		Collection<FiPlant> trees = new ArrayList<FiPlant>();
		for (Item item : items) {
			if (item instanceof FiPlant) {
				trees.add((FiPlant) item);
			}
		}
		return trees;
	}

	// fp - May 2009 add FiLayerSet
	private Collection<FiLayerSet> getFiLayerSets(Collection<Item> items) {
		Collection<FiLayerSet> layerSets = new ArrayList<FiLayerSet>();
		for (Item item : items) {
			if (item instanceof FiLayerSet) {
				layerSets.add((FiLayerSet) item);
			}
		}
		return layerSets;
	}

	/**
	 * SketchListener interface
	 */
	public void sketchHappening(SketchEvent evt) {

		if (evt.isAdjusting())
			return; // fc-5.9.2011 to accelerate on moves (adjusting is true
					// during the mouse drag, then false on mouse release)

		// System.out.println ("FiStatePanel: sketchHappening ()...");

		// At present time, we do not update on selection changes
		// if one and only one polygon is selected, selectedPolygon
		// else still null
		if (evt.getType().equals(SketchEvent.SELECTION_CHANGED)) {
			Collection<Item> selection = sketchModel.getSelection();
			int polySelectedNumber = 0;
			selectedPolygon = null;
			for (Item s : selection) {
				if (s instanceof Polygon) {
					selectedPolygon = (Polygon) s;
					polySelectedNumber++;
				}
			}

			if (polySelectedNumber > 1) {
				// more than one polygon selected
				selectedPolygon = null;
			}

			update();
			return;
		}

		// fc - 5.11.2008 - replaced all Set<Item> by Collection<AbstractItem>
		// extract the FireTrees from the whole sketchModel
		SketchLinker linker = (SketchLinker) sketchModel.getSketchLinker();
		if (linker != null) { // fc-2.2.2015
			fiStand = (FiStand) linker.getUserScene();
		}
		if (fiStand == null) {
			// previous method to get Trees (fc), initially removed by fp
			// but added again for the cases where something is loaded from a
			// file
			// because the update() need to know "firestand", which can not be
			// derived from
			// sketchModel.getSketchLinker() when nothing was added before
			Collection<Item> allItems = new HashSet<Item>();
			Set<Type> types = sketchModel.getTypes(); // fc - 5.11.2008
			for (Type type : types) { // fc - 5.11.2008
				Collection<Item> items = sketchModel.getItems(type); // fc -
																		// 5.11.2008
				allItems.addAll(items);
			}
			Collection<FiPlant> trees = getFiPlants(allItems);
			if (trees != null && !trees.isEmpty()) {
				Iterator i = trees.iterator();
				FiPlant tt = (FiPlant) i.next();
				fiStand = (FiStand) tt.getScene();
			}

		}
		// Collection trees=firestand.getTrees();
		// Collection layerSets = firestand.getLayerSets();
		update();
		return;
	}

	public void actionPerformed(ActionEvent evt) {

		if (evt.getSource().equals(heightThreshold)) {
			if (!Check.isDouble(heightThreshold.getText().trim())) {
				MessageDialog.print(this, Translator.swap("FiStatePanel.heightThresholMustBeAPositiveNumber"));
				return;
			}
			double v = Check.doubleValue(heightThreshold.getText().trim());
			if (v < 0) {
				MessageDialog.print(this, Translator.swap("FiStatePanel.heightThresholMustBeAPositiveNumber"));
				return;
			}
			Settings.setProperty("fire.state.panel.threshold", v);

			update();
		}
	}

	/**
	 * Initialize the GUI.
	 */
	private void createUI() {

		ColumnPanel statePanel = new ColumnPanel();

		// Threshold for trees
		ColumnPanel p1 = new ColumnPanel(Translator.swap("FiStatePanel.thresholdBetwShrubAndTrees"));
		LinePanel l11 = new LinePanel();

		l11.add(new JWidthLabel(Translator.swap("FiStatePanel.heightThreshold") + " :", 170));
		heightThreshold = new JTextField();
		heightThreshold.setText("" + heightThresholdValue);
		heightThreshold.addActionListener(this);
		l11.add(heightThreshold);
		l11.addStrut0();
		p1.add(l11);
		statePanel.add(p1);

		// General
		ColumnPanel p0 = new ColumnPanel(Translator.swap("FiStatePanel.generalState"), 2, 1);
		LinePanel l10 = new LinePanel();

		LinePanel l10c = new LinePanel();
		l10c.add(new JWidthLabel(Translator.swap("FiStatePanel.totalNumber") + " :", 170));
		totalNumber = new JTextField();
		totalNumber.setEditable(false);
		l10c.add(totalNumber);
		l10c.addStrut0();
		p0.add(l10c);

		l10.add(new JWidthLabel(Translator.swap("FiStatePanel.totalCover") + " :", 170));
		totalCover = new JTextField();
		totalCover.setEditable(false);
		l10.add(totalCover);
		l10.addStrut0();
		p0.add(l10);

		LinePanel l10b = new LinePanel();
		l10b.add(new JWidthLabel(Translator.swap("FiStatePanel.totalLoad") + " :", 170));
		totalLoad = new JTextField();
		totalLoad.setEditable(false);
		l10b.add(totalLoad);
		l10b.addStrut0();
		p0.add(l10b);

		LinePanel l10a = new LinePanel();
		l10a.add(new JWidthLabel(Translator.swap("FiStatePanel.maxHeight") + " :", 170));
		maxHeight = new JTextField();
		maxHeight.setEditable(false);
		l10a.add(maxHeight);
		l10a.addStrut0();
		p0.add(l10a);

		p0.addStrut0();
		statePanel.add(p0);

		// Trees state
		ColumnPanel p2 = new ColumnPanel(Translator.swap("FiStatePanel.treeState"));
		LinePanel l12a = new LinePanel();

		LinePanel l13 = new LinePanel();
		l13.add(new JWidthLabel(Translator.swap("FiStatePanel.treeNumberAboveThreshold") + " :", 170));
		treeNumberAboveThreshold = new JTextField();
		treeNumberAboveThreshold.setEditable(false);
		l13.add(treeNumberAboveThreshold);
		l13.addStrut0();
		p2.add(l13);

		LinePanel l12 = new LinePanel();
		l12.add(new JWidthLabel(Translator.swap("FiStatePanel.treeCover") + " :", 170));
		treeCover = new JTextField();
		treeCover.setEditable(false);
		l12.add(treeCover);
		l12.addStrut0();
		p2.add(l12);

		LinePanel l14 = new LinePanel();
		l14.add(new JWidthLabel(Translator.swap("FiStatePanel.treeLoad") + " :", 170));
		treeLoad = new JTextField();
		treeLoad.setEditable(false); // ov - 03.08.07
		l14.add(treeLoad);
		l14.addStrut0();
		p2.add(l14);
		p2.addStrut0();
		statePanel.add(p2);

		l12a.add(new JWidthLabel(Translator.swap("FiStatePanel.treeLAI") + " :", 170));
		treeLAI = new JTextField();
		treeLAI.setEditable(false);
		l12a.add(treeLAI);
		l12a.addStrut0();
		p2.add(l12a);

		// Shrub state
		ColumnPanel p3 = new ColumnPanel(Translator.swap("FiStatePanel.shrubState"));

		LinePanel l15 = new LinePanel();
		l15.add(new JWidthLabel(Translator.swap("FiStatePanel.shrubCover") + " :", 170));
		shrubCover = new JTextField();
		shrubCover.setEditable(false); // ov - 03.08.07
		l15.add(shrubCover);
		l15.addStrut0();
		p3.add(l15);

		LinePanel l17 = new LinePanel();
		l17.add(new JWidthLabel(Translator.swap("FiStatePanel.shrubLoad") + " :", 170));
		shrubLoad = new JTextField();
		shrubLoad.setEditable(false); // ov - 03.08.07
		l17.add(shrubLoad);
		l17.addStrut0();
		p3.add(l17);

		LinePanel l16 = new LinePanel();
		l16.add(new JWidthLabel(Translator.swap("FiStatePanel.shrubPhytovolume") + " :", 170));
		shrubPhytovolume = new JTextField();
		shrubPhytovolume.setEditable(false); // ov - 03.08.07
		l16.add(shrubPhytovolume);
		l16.addStrut0();
		p3.add(l16);

		p3.addStrut0();
		statePanel.add(p3);

		MemoPanel memo = new MemoPanel(Translator.swap("FiStatePanel.setAHeightThresholdThenValidate"));
		statePanel.add(memo);

		// Herbs state
		// ColumnPanel p4 = new ColumnPanel (Translator.swap
		// ("FiStatePanel.herbsState"));

		// LinePanel l18 = new LinePanel ();
		// l18.add (new JWidthLabel (Translator.swap
		// ("FiStatePanel.herbsCover")+" :", 170));
		// herbsCover = new JTextField ();
		// herbsCover.setEnabled(false); //ov - 03.08.07
		// l18.add (herbsCover);
		// p4.add (l18);

		// p4.addStrut0 ();
		// statePanel.add (p4);

		statePanel.addStrut0();

		this.setLayout(new BorderLayout());
		add(statePanel, BorderLayout.NORTH);

	}

}
