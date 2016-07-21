/*
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 *
 * Copyright (C) 2001-2003  Francois de Coligny
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
package capsis.extension.standviewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JToolBar;

import jeeb.lib.util.AmapTools;
import jeeb.lib.util.IconLoader;
import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import jeeb.lib.util.Vertex2d;
import jeeb.lib.util.annotation.Param;
import jeeb.lib.util.extensionmanager.ExtensionManager;
import capsis.commongui.projectmanager.StepButton;
import capsis.commongui.util.Helper;
import capsis.commongui.util.Tools;
import capsis.defaulttype.Tree;
import capsis.defaulttype.TreeCollection;
import capsis.extension.AbstractStandViewer;
import capsis.gui.DialogWithOkCancel;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.MethodProvider;
import capsis.kernel.Step;
import capsis.util.Drawer;
import capsis.util.Grouper;
import capsis.util.GrouperManager;
import capsis.util.Pilotable;
import capsis.util.diagram2d.Diagram2D;
import capsis.util.diagram2d.Grad;
import capsis.util.diagram2d.GraduationContext;
import capsis.util.diagram2d.MaidHisto;
import capsis.util.methodprovider.DdomProvider;
import capsis.util.methodprovider.DgProvider;
import capsis.util.methodprovider.GProvider;
import capsis.util.methodprovider.HdomProvider;
import capsis.util.methodprovider.HgProvider;
import capsis.util.methodprovider.NProvider;
import capsis.util.methodprovider.SHBProvider;
import capsis.util.methodprovider.VProvider;

/**	SVMaid draws histograms to represent two steps of a project associated to a MAID model.
*	MAID stands are Distance Independant Tree Models stands. Each tree is
*	a representative for several. It holds a number of represented individuals.
*	@author F. de Coligny - jan 2002 - jan 2005 - nov 2005
*/
public class SVMaid extends AbstractStandViewer implements ActionListener, Pilotable {
	
	static public String AUTHOR = "F. de Coligny";
	static public String VERSION = "1.0";
	

	public static final int MAX_FRACTION_DIGITS = 2;
	protected Diagram2D dbhDiagram;	// we draw histograms in this component
	protected Diagram2D heightDiagram;
	private Drawer dbhHistoDrawer;
	private Drawer heightHistoDrawer;
	protected GraduationContext xGradContext;	// step1, X axis (Dbh classes (cm) or Girth (cm))
	protected GraduationContext yGradContext;	// step1, Y axis (Numbers)
	protected GraduationContext zGradContext;	// step1, Z axis (Height (m))

	protected Step step1;		// main step
	protected MaidHisto histo1;

	protected Step step2;		// previous step
	protected MaidHisto histo2;

	@Param
	protected SVMaidSettings settings;	// memorized settings for the viewer

	protected JButton settingsButton;
	private JButton helpButton;

	protected SVMaidPanel svmaidDialog;	// svmaid option dialog
	protected JPanel optionPanel;

	protected JScrollPane dataScrollPane;
	protected JComponent dataPanel;
	protected NumberFormat formater;
	//~ protected int memoCurveHeight;	// to avoid unneeded repainting
	protected double memoX;
	protected double memoY;

	protected double hectareCoefficient;
		
	protected Color stepColor;	// fc - 18.11.2004

	private JSplitPane split;	// fc - 5.1.2005
	private JSplitPane split2;	// fc - 8.11.2005 - for variables
	
	static {
		Translator.addBundle("capsis.extension.standviewer.SVMaid");
	}


	@Override
	public void init(GModel model, Step s, StepButton but) throws Exception {
		super.init (model, s, but);
	
		stepColor = stepButton.getColor ();	// fc - 18.11.2004
		
		retrieveSettings ();

		resetSegment ();

		step1 = stepButton.getStep ();
		if (settings.showPreviousStep) {
			step2 = (Step) step1.getVisibleFather ();	// may be null (root step)
		} else {
			step2 = null;
		}

		// Convention : hectare coefficient is calculated with the area of the stand under step1
		// Note : area does not change with time
		hectareCoefficient = 10000 / step1.getScene ().getArea ();

		// Build drawers for every Diagram2D
		dbhHistoDrawer = new DbhHistoDrawer ();
		heightHistoDrawer = new HeightHistoDrawer ();

		formater = NumberFormat.getInstance (Locale.ENGLISH);		// fc - 18.11.2004
		formater.setGroupingUsed (false);							// fc - 18.11.2004
		formater.setMaximumFractionDigits (MAX_FRACTION_DIGITS);	// fc - 18.11.2004

		histo1 = new MaidHisto (getImpliedTrees (step1));
		applyOptions (histo1);
		histo1.update ();
		Rectangle2D.Double nBounds = histo1.getNBounds ();
		Rectangle2D.Double hBounds = histo1.getHBounds ();

		if (step2 != null) {
			histo2 = new MaidHisto (getImpliedTrees (step2));
			applyOptions (histo2);

			// Ensure the two histo have the same classWidth
			if (settings.aggregateMode == SVMaidSettings.AGGREGATE_CLASS_NUMBER) {
				histo2.setClassWidth (histo1.getClassWidth ());
			}

			histo2.update ();

			// Security
			if (!histo1.isFusionableWith (histo2)) {
				step2 = null;
				histo2 = null;
			} else {
				nBounds = computeUnion (nBounds, histo2.getNBounds ());
				hBounds = computeUnion (hBounds, histo2.getHBounds ());
			}
		}
		
		String ha = settings.perHectare ? " (ha)" : "";
		String xName = (histo1.isGirthMode ()) ? Translator.swap ("SVMaid.girth") : Translator.swap ("SVMaid.dbh");
		prepareGraduationContexts (histo1, histo2, xName, "N"+ha, Translator.swap ("SVMaid.height"));

		dbhDiagram = new Diagram2D (dbhHistoDrawer, nBounds, xGradContext, yGradContext);
		heightDiagram = new Diagram2D (heightHistoDrawer, hBounds, xGradContext, zGradContext);
		heightDiagram.setDrawXAxis (false);

		optionPanel = new JPanel ();
		createUI ();
		
		// Resizing management : check divider location - fc - 5.1.2005
		//~ addComponentListener (new ComponentAdapter () {
			//~ public void componentResized (ComponentEvent e) {
				//~ split.setDividerLocation (0.33d);
			//~ }
		//~ });

	}

	/**	Extension dynamic compatibility mechanism.
	*	This matchwith method checks if the extension can deal (i.e. is compatible) with the referent.
	*/
	static public boolean matchWith (Object referent) {

		try {
			if (!(referent instanceof GModel)) {return false;}
			GModel m = (GModel) referent;
			GScene s = ((Step) m.getProject ().getRoot ()).getScene ();
			if (!(s instanceof TreeCollection)) {return false;}
			TreeCollection tc = (TreeCollection) s;
			if (!tc.getTrees ().isEmpty () && !(tc.getTrees ().iterator ().next () instanceof Tree)) {return false;}

		} catch (Exception e) {
			Log.println (Log.ERROR, "SVMaid.matchWith ()", "Error in matchWith () (returned false)", e);
			return false;
		}
		return true;
	}

	/**	Method to draw a segment within this viewer.
	*	The segement is drawn from last memorized point to the given point.
	*	The given point is memorized to be the beginning of next segment to be drawn.
	*	Only rectangle r is visible (user coordinates) -> do not draw if outside.
	*	May be redefined in subclasses.
	*/
	protected void drawSegment (Graphics2D g2, Rectangle.Double r, double x, double y) {
		// a point
		if (y != 0) {
			Shape sh = new Line2D.Double (x, y, x, y);
			g2.draw (sh);
		}

		// a segment
		if (memoX != MaidHisto.INACTIVE && memoY != MaidHisto.INACTIVE) {
			if (y != 0) {
				Shape sh = new Line2D.Double (memoX, memoY, x, y);

				g2.draw (sh);
			}
		}

		if (y == 0) {	// do not draw segment next time
			resetSegment ();
		} else {	// memo x & y to draw segment next time
			memoX = x;
			memoY = y;
		}
	}

	protected void resetSegment () {
		memoX = MaidHisto.INACTIVE;
		memoY = MaidHisto.INACTIVE;
	}

	//	Apply current viewer options on the given MaidHisto.
	//
	private void applyOptions (MaidHisto histo) {
		// init
		histo.setForcedXMin (MaidHisto.INACTIVE);
		histo.setForcedXMax (MaidHisto.INACTIVE);
		histo.setClassWidth (0d);

		histo.setXBeginsAtZero (settings.zeroOnX);	// enlarge bounds
		histo.setGirthMode (settings.showGirth);	// girth instead of dbh on X axis
		if (settings.perHectare) {
			histo.setHectareCoefficient (hectareCoefficient);
		} else {
			histo.setHectareCoefficient (1);
		}

		if (settings.aggregate) {

			if (settings.isAggregateMinThreshold) {
				histo.setForcedXMin (settings.aggregateMinThreshold);
			}
			if (settings.isAggregateMaxThreshold) {
				histo.setForcedXMax (settings.aggregateMaxThreshold);
			}

			if (settings.aggregateMode == SVMaidSettings.AGGREGATE_CLASS_WIDTH) {
				double classWidth = settings.aggregateClassWidth;
				histo.setClassWidth (classWidth);
			} else {		// AGGREGATE_CLASS_NUMBER
				int classNumber = settings.aggregateClassNumber;
				// compute class width from classNumber
				double currentExtension = histo.getXMax () - histo.getXMin ();
				currentExtension *= 1.01d;	// to integrate upper limit
				double classWidth = currentExtension / classNumber;
				histo.setClassWidth (classWidth);
			}
		}

	}

	//	Compute graduation contexts after an histo.update and before
	//	a Diagram2D.<init> or set ().
	//
	private void prepareGraduationContexts (MaidHisto histo1, MaidHisto histo2,
			String xAxisName, String yAxisName, String zAxisName) {

		// Graduation context for X axis (Dbh classes (cm))
		xGradContext = new GraduationContext ();
		xGradContext.axisName = "";
		if (settings.showAxisNames) {
			xGradContext.axisName = xAxisName;
		}
		xGradContext.grads = new ArrayList ();
		double classWidth = histo1.getClassWidth ();
		Set memo = new HashSet ();

		int k = 0;
		String[] optLabels = histo1.getXLabels ();	// may be null (optional)
		if (optLabels != null && optLabels.length != histo1.getNBars ().size ()) {optLabels = null;}	// security
		for (Iterator i = histo1.getNBars ().iterator (); i.hasNext ();) {
			Vertex2d bar = (Vertex2d) i.next ();
			String label = "";
			try {
				label = optLabels[k];	// optional case if optLabels not null
			} catch (Exception e) {
				label = ""+formater.format (bar.x);	// default case
			}
			memo.add (label);
			Grad grad = new Grad (bar.x, label);
			xGradContext.grads.add (grad);
			k++;
		}
		xGradContext.begin = histo1.getNBounds ().x;
		xGradContext.end = xGradContext.begin + histo1.getNBounds ().width;

		// second histo x grads if two steps
		if (step2 != null) {
			k = 0;
			optLabels = histo2.getXLabels ();	// may be null (optional)
			if (optLabels != null && optLabels.length != histo2.getNBars ().size ()) {optLabels = null;}	// security
			
			for (Iterator i = histo2.getNBars ().iterator (); i.hasNext ();) {
				Vertex2d bar = (Vertex2d) i.next ();
				String label = "";
				try {
					label = optLabels[k];	// optional case if optLabels not null
				} catch (Exception e) {
					label = ""+formater.format (bar.x);	// default case
				}
				if (!memo.contains (label)) {
					Grad grad = new Grad (bar.x, label);
					xGradContext.grads.add (grad);
				}
				k++;
			}
			xGradContext.begin = Math.min (xGradContext.begin, histo2.getNBounds ().x);
			xGradContext.end = Math.max (xGradContext.end,
					xGradContext.begin + histo2.getNBounds ().width);
		}

		// Graduation context for Y axis (Numbers)
		yGradContext = new GraduationContext ();
		yGradContext.axisName = "";
		if (settings.showAxisNames) {
			yGradContext.axisName = yAxisName;
		}
		yGradContext.integersOnly = true;
		yGradContext.begin = histo1.getNBounds ().y;
		yGradContext.end = yGradContext.begin + histo1.getNBounds ().height;
		if (step2 != null) {
			yGradContext.begin = Math.min (yGradContext.begin, histo2.getNBounds ().y);
			yGradContext.end = Math.max (yGradContext.end,
					yGradContext.begin + histo2.getNBounds ().height);
		}

		// Graduation context for Z axis (Height (m))
		zGradContext = new GraduationContext ();
		zGradContext.axisName = "";
		if (settings.showAxisNames) {
			zGradContext.axisName = zAxisName;
		}
		zGradContext.integersOnly = false;
		zGradContext.begin = histo1.getHBounds ().y;
		zGradContext.end = zGradContext.begin + histo1.getHBounds ().height;
		if (step2 != null) {
			zGradContext.begin = Math.min (zGradContext.begin, histo2.getHBounds ().y);
			zGradContext.end = Math.max (zGradContext.end,
					zGradContext.begin + histo2.getHBounds ().height);
		}

		// This is to align the two vertical axes of the two Diagram2D (s)
		String yMin = ""+formater.format (yGradContext.begin);
		String yMax = ""+formater.format (yGradContext.end);
		String zMin = ""+formater.format (zGradContext.begin);
		String zMax = ""+formater.format (zGradContext.end);
		int maxChars = Math.max (yMin.length (), yMax.length ());
		maxChars = Math.max (maxChars, zMin.length ());
		maxChars = Math.max (maxChars, zMax.length ());
		String longestGrad = "";
		for (int i = 0; i < maxChars; i++) {longestGrad += "X";}

		yGradContext.longestGrad = longestGrad;

		zGradContext.longestGrad = longestGrad;
	}

	/**	Refreshes the GUI with another Step.
	*	or after settings modification.
	*/
	public void update (StepButton sb) {
		super.update (sb);
		((DbhHistoDrawer) dbhHistoDrawer).resetSelection ();	// Cancel current selection, we change steps

		step1 = sb.getStep ();
		if (settings.showPreviousStep) {
			step2 = (Step) step1.getVisibleFather ();	// may be null (root step)
		} else {
			step2 = null;
		}

		// 1. MaidHisto1
		histo1.setTrees (getImpliedTrees (step1));
		applyOptions (histo1);
		
		histo1.update ();
		Rectangle2D.Double nBounds = histo1.getNBounds ();
		Rectangle2D.Double hBounds = histo1.getHBounds ();

		if (step2 != null) {
			if (histo2 == null) {
				histo2 = new MaidHisto (getImpliedTrees (step2));
			} else {
				histo2.setTrees (getImpliedTrees (step2));
			}
			applyOptions (histo2);

			// Ensure the two histo have the same classWidth
			if (settings.aggregateMode == SVMaidSettings.AGGREGATE_CLASS_NUMBER) {
				histo2.setClassWidth (histo1.getClassWidth ());
			}

			histo2.update ();	// recalculates

			// Security
			if (!histo1.isFusionableWith (histo2)) {
				Log.println (Log.ERROR, "SVMaid.update ()",
						"Histos are not fusionable. Previous step feature disabled.");
				step2 = null;
				histo2 = null;
			} else {
				nBounds = computeUnion (nBounds, histo2.getNBounds ());
				hBounds = computeUnion (hBounds, histo2.getHBounds ());
			}
		}

		String ha = settings.perHectare ? " (ha)" : "";
		String xName = (histo1.isGirthMode ()) ? Translator.swap ("SVMaid.girth") : Translator.swap ("SVMaid.dbh");
		prepareGraduationContexts (histo1, histo2, xName, "N"+ha, Translator.swap ("SVMaid.height"));
		dbhDiagram.set (nBounds, xGradContext, yGradContext);
		heightDiagram.set (hBounds, xGradContext, zGradContext);

		// 3. Optional variables
		dataPanel = makeDataPanel ();
		dataScrollPane.getViewport ().setView (dataPanel);
		
		// 3. validate contentPane
		getContentPane ().validate ();
	}

	//	Creates a collection containing all trees in step in current grouper.
	//
	private Collection getImpliedTrees (Step step) {
		GScene stand = step.getScene ();

		// If group is set, restrict stand to given group
		Collection trees = ((TreeCollection) stand).getTrees ();
		if (settings.grouperMode) {
			String name = settings.grouperName;
			GrouperManager gm = GrouperManager.getInstance ();
			Grouper grouper = gm.getGrouper (name);
			try {
				trees = grouper.apply (trees, name.toLowerCase ().startsWith ("not "));	// fc - 21.4.2004
			} catch (Exception e) {
				Log.println (Log.ERROR, "SVSimple.draw ()",
						"Exception while applying grouper "+name+" on stand", e);
				settings.grouperMode = false;
				settings.grouperName = "";
			}
		}

		return Arrays.asList (trees.toArray ());
	}



	//	Calculate the union of the two rectangles (double precision).
	//
	private Rectangle.Double computeUnion (Rectangle.Double a, Rectangle.Double b) {
		Rectangle.Double union = new Rectangle.Double ();
		double x0 = Math.min (a.x, b.x);
		double y0 = Math.min (a.y, b.y);
		double x1 = Math.max (a.x+a.width, b.x+b.width);
		double y1 = Math.max (a.y+a.height, b.y+b.height);
		union.x = x0;
		union.y = y0;
		union.width = x1 - x0;
		union.height = y1 - y0;
		return union;
	}

	/**	From Pilotable interface.
	*/
	public JComponent getPilot () {

//		ImageIcon icon = IconLoader.getIcon ("properties_16.png");
		ImageIcon icon = IconLoader.getIcon ("option_16.png");
		settingsButton = new JButton (icon);
		Tools.setSizeExactly (settingsButton);
		settingsButton.setToolTipText (Translator.swap ("SVMaid.settings"));
		settingsButton.addActionListener (this);

		icon = IconLoader.getIcon ("help_16.png");
		helpButton = new JButton (icon);
		Tools.setSizeExactly (helpButton);
		helpButton.setToolTipText (Translator.swap ("Shared.help"));
		helpButton.addActionListener (this);

		JToolBar toolbar = new JToolBar ();
		toolbar.add (settingsButton);
		toolbar.addSeparator();
		toolbar.add (helpButton);
		toolbar.setVisible (true);

		return toolbar;
	}

	/**	Retrieve the settings for this viewer as saved the last time they were
	*	changed.
	*/
	protected void retrieveSettings () {
		settings = new SVMaidSettings ();
		
	}

	/**	Used for the settings and filtering buttons.
	*/
	public void actionPerformed (ActionEvent evt) {

		if (evt.getSource ().equals (settingsButton)) {
			svmaidDialog = new SVMaidPanel (this, settings, step1, optionPanel);
			DialogWithOkCancel dlg = new DialogWithOkCancel (svmaidDialog);
			if (dlg.isValidDialog ()) {

				settings.showAxisNames = svmaidDialog.getCkShowAxisNames ().isSelected ();
				settings.showPreviousStep = svmaidDialog.getCkShowPreviousStep ().isSelected ();
				settings.zeroOnX = svmaidDialog.getCkZeroOnX ().isSelected ();
				settings.showGirth = svmaidDialog.getCkShowGirth ().isSelected ();
				settings.selectUnderlyingTrees = svmaidDialog.getCkSelectUnderlyingTrees ().isSelected ();
				settings.perHectare = svmaidDialog.getCkPerHectare ().isSelected ();

				settings.grouperMode = svmaidDialog.getGrouperChooser ().isGrouperAvailable ();
				settings.grouperModeNot = svmaidDialog.getGrouperChooser ().isGrouperNot ();
				settings.grouperName = svmaidDialog.getGrouperChooser ().getGrouperName ();

				//~ settings.showVariables = svmaidDialog.getCkShowVariables ().isSelected ();

				//~ if (svmaidDialog.isVariablesBottom ()) {
					//~ settings.variablesPosition = SVMaidSettings.VARIABLES_BOTTOM;
				//~ } else {
					//~ settings.variablesPosition = SVMaidSettings.VARIABLES_RIGHT;
				//~ }

				settings.aggregate = svmaidDialog.isAggregate ();
				settings.aggregateClassWidth = svmaidDialog.getAggregateClassWidth ();
				settings.aggregateClassNumber = svmaidDialog.getAggregateClassNumber ();
				settings.isAggregateMinThreshold = svmaidDialog.isAggregateMinThreshold ();
				settings.isAggregateMaxThreshold = svmaidDialog.isAggregateMaxThreshold ();
				settings.aggregateMinThreshold = svmaidDialog.getAggregateMinThreshold ();
				settings.aggregateMaxThreshold = svmaidDialog.getAggregateMaxThreshold ();
				if (svmaidDialog.isAggregateClassWidth ()) {
					settings.aggregateMode = SVMaidSettings.AGGREGATE_CLASS_WIDTH;
				} else {
					settings.aggregateMode = SVMaidSettings.AGGREGATE_CLASS_NUMBER;
				}

				settings.enlargeBars = svmaidDialog.getCkEnlargeBars ().isSelected ();
				settings.selectionColor = svmaidDialog.getSelectionColor ();
				settings.color2 = svmaidDialog.getColor2 ();

				optionAction ();	// hook for subclasses
			}

			svmaidDialog.dispose ();
			dlg.dispose ();

			update (getStepButton ());
		} else if (evt.getSource ().equals (helpButton)) {
			Helper.helpFor (this);
		}
	}

	/**	Called when ok on option panel, must be redefined by subclasses
	*	which use optionPanel
	*/
	protected void optionAction () {
		ExtensionManager.recordSettings (this);
	}

	//	Build and return an empty panel..
	//
	private JPanel getEmptyPanel () {
		JPanel emptyPanel = new JPanel ();
		emptyPanel.setSize (new Dimension (0, 0));
		emptyPanel.setMinimumSize (new Dimension (0, 0));
		emptyPanel.setPreferredSize (new Dimension (0, 0));
		emptyPanel.setMaximumSize (new Dimension (0, 0));
		return emptyPanel;
	}

	//	Viewer title definition : reference to current step and considered group.
	//
	protected void defineTitle () {

		try {
			// 1. Step reference
			String title = step.getProject ().getName ()+"."+step.getName ();

			// 2. Group name if exists
			if (settings.grouperMode
					&& settings.grouperName != null) {
				title += " / "+settings.grouperName;
			}

			// 3. Hectare ?
			if (settings.perHectare) {
				title += " /ha";
			}

			// 4. Viewer name
			title += " - " + ExtensionManager.getName (this);

			setTitle (title);
		} catch (Exception e) {
			setTitle (ExtensionManager.getName (this));
		}
	}

	/**	Make the dataPanel.
	*	This method can be redefined in subclass to show data differently
	*/
	protected JComponent makeDataPanel () {

		Vector rows = refreshData ();		// data under histogram (Co, Ho...)
		Vector cols = new Vector ();
		cols.add (Translator.swap ("SVMaid.variable"));
		if (step2 != null) {
			cols.add (step2.getProject ().getName ()+"."+step2.getScene ().getCaption ());	// previous
			cols.add (step1.getProject ().getName ()+"."+step1.getScene ().getCaption ());	// current
		} else {
			cols.add (Translator.swap ("SVMaid.value"));	// current step
		}

		JTable table = new JTable (rows, cols);
		return table;
	}

	//	Tool method for makeDataPanel ()
	//	Make a component key : value.
	//
	private Vector makeRow (String key, double value) {
		Vector row = new Vector ();
		row.add (Translator.swap (key));
		row.add (formater.format (value));
		return row;
	}

	//	Tool method for makeDataPanel ()
	//	This is used if previous step is shown : three cols in table.
	//
	private Vector makeRow (String key, double valueA, double valueB) {
		Vector row = new Vector ();

		row.add (Translator.swap (key));
		row.add (formater.format (valueA));
		row.add (formater.format (valueB));

		return row;
	}

	//	Tool method for makeDataPanel ()
	//	Compute the dendrometric data for the underlying stand.
	//
	private Vector refreshData () {

		Vector rows = new Vector ();
		MethodProvider mp = null;
		try {
			mp = step1.getProject ().getModel ().getMethodProvider ();
		} catch (Exception e) {
			Log.println (Log.ERROR, "SVMaid.refreshData ()", "Can not find MethodProvider", e);
			return rows;
		}

		GScene stand1 = step1.getScene ();
		// If group is set, restrict stand to given group
		Collection trees1 = ((TreeCollection) stand1).getTrees ();;
		if (settings.grouperMode) {
			String name = settings.grouperName;
			GrouperManager gm = GrouperManager.getInstance ();
			Grouper grouper = gm.getGrouper (name);
			try {
				trees1 = grouper.apply (trees1, name.toLowerCase ().startsWith ("not "));
			} catch (Exception e) {
				Log.println (Log.ERROR, "SVMaid.refreshData ()",
						"Exception while applying grouper "+name+" on stand1", e);
				settings.grouperMode = false;
				settings.grouperName = "";
			}	// if trouble, subStand is unchanged (= stand)
		}

		GScene stand2 = null;
		Collection trees2 = null;	// fc - 24.3.2004

		if (step2 != null) {	// previous step
			stand2 = step2.getScene ();
			// If group is set, restrict stand to given group
			trees2 = ((TreeCollection) stand2).getTrees ();
			if (settings.grouperMode) {
				String name = settings.grouperName;
				GrouperManager gm = GrouperManager.getInstance ();
				Grouper grouper = gm.getGrouper (name);
				try {
					trees2 = grouper.apply (trees2, name.toLowerCase ().startsWith ("not "));
				} catch (Exception e) {
					Log.println (Log.ERROR, "SVMaid.refreshData ()",
							"Exception while applying grouper "+name+" on stand2", e);
					settings.grouperMode = false;
					settings.grouperName = "";
				}
			}
		}

		// Hectare processing
		double haCoef = 1d;
		if (settings.perHectare) {
			haCoef = hectareCoefficient;
		}

		try {
			//~ int N = (int) ( ((NProvider) mp).getN (stand1, trees1) * haCoef );
			double N = ((NProvider) mp).getN (stand1, trees1) * haCoef;	// fc - 22.8.2006 - Numberable is double
			if (step2 == null) {
				rows.add (makeRow ("N", N));
			} else {
				//~ int N2 = (int) ( ((NProvider) mp).getN (stand2, trees2) * haCoef );
				double N2 = ((NProvider) mp).getN (stand2, trees2) * haCoef;	// fc - 22.8.2006 - Numberable is double
				rows.add (makeRow ("N", N2, N));
			}
		} catch (Exception e) {}

		try {
			double G = ((GProvider) mp).getG (stand1, trees1) * haCoef;
			if (step2 == null) {
				rows.add (makeRow ("G (m2)", G));
			} else {
				double G2 = ((GProvider) mp).getG (stand2, trees2) * haCoef;
				rows.add (makeRow ("G (m2)", G2, G));
			}
		} catch (Exception e) {}

		try {
			double V = ((VProvider) mp).getV (stand1, trees1) * haCoef;
			if (step2 == null) {
				rows.add (makeRow ("V (m3)", V));
			} else {
				double V2 = ((VProvider) mp).getV (stand2, trees2) * haCoef;
				rows.add (makeRow ("V (m3)", V2, V));
			}
		} catch (Exception e) {}

		try {
			double Dg = ((DgProvider) mp).getDg (stand1, trees1);
			if (step2 == null) {
				rows.add (makeRow ("Dg (cm)", Dg));
			} else {
				double Dg2 = ((DgProvider) mp).getDg (stand2, trees2);
				rows.add (makeRow ("Dg (cm)", Dg2, Dg));
			}
		} catch (Exception e) {}

		//added by Celine Meredieu - december 2002
		try {
			double Ddom = ((DdomProvider) mp).getDdom (stand1, trees1);
			if (step2 == null) {
				rows.add (makeRow ("Ddom (cm)", Ddom));
			} else {
				double Ddom2 = ((DdomProvider) mp).getDdom (stand2, trees2);
				rows.add (makeRow ("Ddom (cm)", Ddom2, Ddom));
			}
		} catch (Exception e) {}

		try {
			double Hg = ((HgProvider) mp).getHg (stand1, trees1);
			if (step2 == null) {
				rows.add (makeRow ("Hg (m)", Hg));
			} else {
				double Hg2 = ((HgProvider) mp).getHg (stand2, trees2);
				rows.add (makeRow ("Hg (m)", Hg2, Hg));
			}
		} catch (Exception e) {}

		try {
			double Hdom = ((HdomProvider) mp).getHdom (stand1, trees1);
			if (step2 == null) {
				rows.add (makeRow ("Hdom (m)", Hdom));
			} else {
				double Hdom2 = ((HdomProvider) mp).getHdom (stand2, trees2);
				rows.add (makeRow ("Hdom (m)", Hdom2, Hdom));
			}
		} catch (Exception e) {}

		try {
			double SHB = ((SHBProvider) mp).getSHB (stand1, trees1);
			if (step2 == null) {
				rows.add (makeRow ("SHB (%)", SHB));
			} else {
				double SHB2 = ((SHBProvider) mp).getSHB (stand2, trees2);
				rows.add (makeRow ("SHB (%)", SHB2, SHB));
			}
		} catch (Exception e) {}

			// could be added (examples)
			double Cdom = 0d;
			double crownRatiodom = 0d;
			double ratioHdomDdom = 0d;
			double Vdom = 0d;
			double Cg = 0d;
			double crownRatiog = 0d;
			double ratioHgDg = 0d;
			double Vg = 0d;
			double V = 0d;
			double F = 0d;
			double CCF = 0d;
			double SPerCent = 0d;
			double CvrtPerCent = 0d;

		return rows;
	}

	//	Create the GUI.
	//
	private void createUI () {

		// 1. Viewer title
		defineTitle ();

		//~ Dimension minimumSize = new Dimension (200, 100);
		getContentPane ().setLayout (new BorderLayout ());	// mainBox in the internalFrame

		// 3. A panel for the stand variables
		dataScrollPane = new JScrollPane (getEmptyPanel ());	//empty
		dataScrollPane.getViewport().putClientProperty
			  ("EnableWindowBlit", Boolean.TRUE);	// faster
		
		// 4. Variables panel is optional
		dataPanel = makeDataPanel ();
		dataScrollPane.getViewport ().setView (dataPanel);
		
		// 5. Global disposition
		getContentPane ().add (getPilot (), BorderLayout.NORTH);
		
		heightDiagram.setMinimumSize (new Dimension (120, 60));
		dbhDiagram.setMinimumSize (new Dimension (120, 120));
		dataScrollPane.setMinimumSize (new Dimension (120, 60));
		
		heightDiagram.setPreferredSize (new Dimension (200, 200));
		dbhDiagram.setPreferredSize (new Dimension (200, 300));
		dataScrollPane.setPreferredSize (new Dimension (200, 500));
		
		split = new JSplitPane (JSplitPane.VERTICAL_SPLIT, 
				heightDiagram, dbhDiagram);
		split.setResizeWeight (0);
		split.setOneTouchExpandable (true);
		
		split2 = new JSplitPane (JSplitPane.HORIZONTAL_SPLIT, 
				split, dataScrollPane);
		split2.setResizeWeight (1);
		split2.setOneTouchExpandable (true);
		
		getContentPane ().add (split2, BorderLayout.CENTER);
	}

	/**	Forget resources.
	*/
	public void dispose () {
		super.dispose ();
		dbhDiagram.dispose ();
		settings = null;
		dbhDiagram = null;
		settingsButton = null;
		svmaidDialog = null;
		optionPanel = null;
	}

	//	Draw a little cross
	//
	private void littleCross (Graphics2D g2, double x, double y) {
		Color c = g2.getColor ();
		g2.setColor (Color.red);
		Line2D.Double l1 = new Line2D.Double (x-5, y, x, y);
		Line2D.Double l2 = new Line2D.Double (x, y, x, y+5);
		g2.draw (l1);
		g2.draw (l2);
		g2.setColor (c);
	}

	private String traceTransform (AffineTransform t) {
		return "scale=("+t.getScaleX ()+" "+t.getScaleY ()+") "
				+"translate=("+t.getTranslateX ()+" "+t.getTranslateY ()+")";
	}

	/**	Build the component answering to a selection. Can be redefined in
	*	some subclass to answer differently.
	*/
	protected JComponent getSelectionComponent (MaidHisto histo, int index) {
		if (settings.selectUnderlyingTrees) {
			return AmapTools.createInspectorPanel (histo.getUTrees (index));
			
		} else {
			Vertex2d nBar = (Vertex2d) histo.getNBars ().get (index);
			Vertex2d hBar = (Vertex2d) histo.getHBars ().get (index);
			double d = nBar.x;
			double h = hBar.y;
			//~ int n = (int) nBar.y;
			double n = nBar.y;	// fc - 22.8.2006 - Numberable is double

			String lab = (settings.showGirth)
					? Translator.swap ("SVMaid.girth")
					: Translator.swap ("SVMaid.dbh");

			String[][] rows = {	{lab, 									""+formater.format (d)},
								{Translator.swap ("SVMaid.height"),		""+formater.format (h)},
								{Translator.swap ("SVMaid.numbers"),	""+formater.format (n)}	};
			String[] columnNames = {Translator.swap ("SVMaid.variable"), 
					Translator.swap ("SVMaid.value")};

			JTable table = new JTable (rows, columnNames);
			JScrollPane s = new JScrollPane (table);
			s.setPreferredSize (new Dimension (250, 100));
			return s;
		}
	}

//----------------------------------------------------------------------- Drawers -------------
//----------------------------------------------------------------------- Drawers -------------
//----------------------------------------------------------------------- Drawers -------------
//----------------------------------------------------------------------- Drawers -------------
//----------------------------------------------------------------------- Drawers -------------
//----------------------------------------------------------------------- Drawers -------------
//----------------------------------------------------------------------- Drawers -------------
	private class DbhHistoDrawer implements Drawer {

		private Collection screenBars;	// bars drawn on screen by last draw () method call
		private Collection selectedBars1;	// Item collection matching the selected bars
		private Collection selectedBars2;	// Item collection matching the selected bars

		private class ScreenBar {
			public double anchor;
			public int stepNumber;
			public int histoBarIndex;
			public Rectangle2D.Double r2;
			public ScreenBar (double anchor, int stepNumber, int histoBarIndex, Rectangle2D.Double r2) {
				this.anchor = anchor;
				this.stepNumber = stepNumber;
				this.histoBarIndex = histoBarIndex;
				this.r2 = r2;
			}
		}

		/**	From Drawer interface.
		*	This method draws in the Diagram2D each time this one must be repainted.
		*	The given Rectangle is unused (no zoom in Diagram2D).
		*	The drawBar () methods may be redefined in subclasses
		*	to draw differently.
		*/
		public void draw (Graphics g, Rectangle.Double r) {

			Graphics2D g2 = (Graphics2D) g;

			// Memorize original color
			Color memoColor = g.getColor ();

			// 1. Ensure title is ok
			defineTitle ();

			//
			if (screenBars == null) {screenBars = new ArrayList ();} else {screenBars.clear ();}

			// 2. Draw the bars
			int n = 0;
			int totalPwidth = dbhDiagram.getPixelWidth (dbhDiagram.getUserBounds ().width);	// pixels
			if (!histo1.isEmpty ()) {	// empty means no trees
				n = histo1.getNBars ().size ();
				if (step2 != null && !histo2.isEmpty ()) {	// empty means no trees
					n *= 2;	// number of bars
				}
			}
			
			int w = 3;	// pixels
			//~ if (settings.enlargeBars) {w = 5;}
			if (settings.enlargeBars) {w = Math.min (Math.max ((totalPwidth / n) - 4, 3), 10);}	// fc - 6.1.2005
			double userTreeWidth = dbhDiagram.getUserWidth (w);	// bars width  = 3 or more pixels -> in user width
			double userSinglePixelWidth = dbhDiagram.getUserWidth (1);	// to separate bars

			double xShift = 0d;
			if (step2 != null) {xShift = userTreeWidth/2 + userSinglePixelWidth/2;}

			if (step2 != null && !histo2.isEmpty ()) {	// empty means no trees
				g2.setColor (settings.color2);
				java.util.List nBars = histo2.getNBars ();
				for (int i = 0; i < nBars.size (); i++) {
					Vertex2d bar = (Vertex2d) nBars.get (i);
					if (selectedBars2 != null && selectedBars2.contains (new Double (bar.x))) {g2.setColor (settings.selectionColor);}
					Rectangle2D.Double r2 = drawBar (g2, r, bar.x - xShift, bar.y, userTreeWidth);
					screenBars.add (new ScreenBar (bar.x, 2, i, r2));
					g2.setColor (settings.color2);
				}
			}

			if (!histo1.isEmpty ()) {	// empty means no trees
				g2.setColor (stepColor);	// was settings.color1
				java.util.List nBars = histo1.getNBars ();
				for (int i = 0; i < nBars.size (); i++) {
					Vertex2d bar = (Vertex2d) nBars.get (i);
					if (selectedBars1 != null && selectedBars1.contains (new Double (bar.x))) {g2.setColor (settings.selectionColor);}
					Rectangle2D.Double r2 = drawBar (g2, r, bar.x + xShift, bar.y, userTreeWidth);
					screenBars.add (new ScreenBar (bar.x, 1, i, r2));

					g2.setColor (stepColor);	// was settings.color1

				}
			}

			// Step1 bars must be before Step2 bars for selection preferences
			Collections.reverse ((java.util.List) screenBars);

			// Reset original color
			g.setColor (memoColor);
		}

		/**	Cancel current selections.
		*/
		public void resetSelection () {
			selectedBars1 = null;
			selectedBars2 = null;
		}

		/**	From Drawer interface.
		*	We may receive (from Panel2D) a selection rectangle (in user space i.e. meters)
		*	and return a JPanel containing information about the objects (trees) inside
		*	the rectangle.
		*	If no objects are found in the rectangle, return null.
		*/
		public JPanel select (Rectangle.Double r, boolean more) {

			JPanel info = null;

			if (selectedBars1 == null) {selectedBars1 = new ArrayList ();} else {selectedBars1.clear ();}
			if (selectedBars2 == null) {selectedBars2 = new ArrayList ();} else {selectedBars2.clear ();}

			// Scan drawn bars and seek which ones intersect selection rectangle
			for (Iterator i = screenBars.iterator (); i.hasNext ();) {
				ScreenBar sb = (ScreenBar) i.next ();
				if (sb.r2.intersects (r)) {
					MaidHisto histo = null;
					if (sb.stepNumber == 1) {
						selectedBars1.add (new Double (sb.anchor));
						histo = histo1;
					} else {
						selectedBars2.add (new Double (sb.anchor));
						histo = histo2;
					}

					info = new JPanel (new BorderLayout ());
					info.setOpaque (true);
					info.setBackground (Color.white);

					JComponent answer = getSelectionComponent (histo, sb.histoBarIndex);
					info.add (answer, BorderLayout.CENTER);

					break;	// only one bar selection
				}
			}

			return info;
		}

		/**	Method to draw an histogram bar within this viewer.
		*	This bar may represent several MAID trees, each representing several (y)
		*	real trees.
		*	Only rectangle r is visible (user coordinates) -> do not draw if outside.
		*	May be redefined in subclasses.
		*/
		protected Rectangle2D.Double drawBar (Graphics2D g2, Rectangle.Double r, double x, double y, double width) {

			// An histogram vertical bar
			double halfWidth = width / 2;
			Rectangle2D.Double r2 = new Rectangle2D.Double (x - halfWidth, 0, width, y);
			Rectangle2D bBox = r2.getBounds2D ();
			if (r.intersects (bBox)) {
				g2.fill (r2);
			}
			return r2;
		}
	}

	
	private class HeightHistoDrawer implements Drawer {
		
		/**	From Drawer interface.
		*	This method draws in the Diagram2D each time this one must be repainted.
		*	The given Rectangle is unused (no zoom in Diagram2D).
		*/
		public void draw (Graphics g, Rectangle.Double r) {
			Graphics2D g2 = (Graphics2D) g;
			
			// Memorize original color
			Color memoColor = g.getColor ();

			// 1. Ensure title is ok
			defineTitle ();

			// 2. Draw the height curves
			if (step2 != null && !histo2.isEmpty ()) {	// empty means no trees
				g2.setColor (settings.color2);

				resetSegment ();	// needed
				for (Iterator i = histo2.getHBars ().iterator (); i.hasNext ();) {
					Vertex2d bar = (Vertex2d) i.next ();
					drawSegment (g2, r, bar.x, bar.y);	// Draw simply, without added nothing now
				}
			}

			if (!histo1.isEmpty ()) {	// empty means no trees
				g2.setColor (stepColor);	// was settings.color1

				resetSegment ();	// needed
				for (Iterator i = histo1.getHBars ().iterator (); i.hasNext ();) {
					Vertex2d bar = (Vertex2d) i.next ();
					drawSegment (g2, r, bar.x, bar.y);	// Draw simply, without added nothing now
				}
			}

			// Reset original color
			g.setColor (memoColor);
		}
		
		/**	From Drawer interface.
		*	We may receive (from Panel2D) a selection rectangle (in user space i.e. meters)
		*	and return a JPanel containing information about the objects (trees) inside
		*	the rectangle.
		*	If no objects are found in the rectangle, return null.
		*/
		public JPanel select (Rectangle.Double r, boolean more) {
			return null;	// XXX to be done
		}
	}


}




