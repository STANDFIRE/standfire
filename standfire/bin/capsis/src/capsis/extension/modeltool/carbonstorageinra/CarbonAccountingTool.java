/*
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 *
 * Copyright (C) 2013 Mathieu Fortin (AgroParisTech/INRA - UMR LERFoB)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package capsis.extension.modeltool.carbonstorageinra;

import java.awt.Dialog.ModalExclusionType;
import java.awt.Window;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.JOptionPane;

import jeeb.lib.util.ListenedTo;
import jeeb.lib.util.Listener;
import jeeb.lib.util.Log;
import lerfob.carbonbalancetool.CATCompatibleStand;
import lerfob.carbonbalancetool.CATCompatibleTree;
import repicea.gui.CommonGuiUtility;
import repicea.gui.UIControlManager;
import repicea.gui.dnd.DragGestureImpl;
import repicea.simulation.treelogger.TreeLoggerDescription;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;
import capsis.commongui.projectmanager.Current;
import capsis.commongui.projectmanager.StepButton;
import capsis.defaulttype.Tree;
import capsis.defaulttype.TreeList;
import capsis.extension.modeltool.woodqualityworkshop.CapsisTreeLoggerDescription;
import capsis.extensiontype.ModelTool;
import capsis.gui.MainFrame;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.Step;

/**
 * CarbonAccountingTool is the class that implements a tool for the calculation of the carbon storage (LERFoB-CAT). 
 * @author Mathieu Fortin (INRA) - January 2010
 */
public final class CarbonAccountingTool extends lerfob.carbonbalancetool.CarbonAccountingTool implements ModelTool, Listener {
	
	protected static class ExtendedDragGestureImpl extends DragGestureImpl<ArrayList<CATCompatibleStand>> {
				
		@Override
		public void dragGestureRecognized(DragGestureEvent event) {
			if (event.getComponent() instanceof StepButton) {
				Step step = ((StepButton) event.getComponent()).getStep();
				if (step.getScene () instanceof CATCompatibleStand) {
					super.dragGestureRecognized(event);
				}
			}
		}

		
		@Override
		protected ArrayList<CATCompatibleStand> adaptSourceToTransferable(DragGestureEvent event) {
			Step step = ((StepButton) event.getComponent()).getStep();
			return CarbonAccountingTool.getStandList(step);
		}

	}
	
	protected static final String englishTitle = "LERFoB Carbon Accounting Tool (LERFoB-CAT)";
	protected static final String frenchTitle = "Outil d'\u00E9valuation du carbone LERFoB (LERFoB-CAT)";
	
	private static enum MessageID implements TextableEnum {

		Name(englishTitle, frenchTitle),
		Description("Assessment Tool for standing carbon, wood product carbon, substituted carbon, and wood-processing carbon emissions",
				"Outil d'\u00E9valuation du carbone sur pied, du carbone contenu dans les produits du bois, du carbone substitu\u00E9 et du carbone \u00E9mis par la production de produits du bois"),
		CATAlreadyRunning("The LERFoB-CAT tool is already running! Please refer to the appropriate window.", "L'outil LERFoB-CAT est d\u00E9j\u00E0 ouvert! Veuillez utiliser la fen\u00EAtre appropri\u00E9e.");
		
		
		MessageID(String englishText, String frenchText) {
			setText(englishText, frenchText);
		}

		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}
		
		@Override
		public String toString() {return REpiceaTranslator.getString(this);}
	}
	
	static public final String AUTHOR="M. Fortin";
	static public final String VERSION="2.0";
	public static final String NAME = REpiceaTranslator.getString(MessageID.Name);
	public static final String DESCRIPTION = REpiceaTranslator.getString(MessageID.Description);

	private static Map<StepButton, DragGestureRecognizer> stepButtons = new HashMap<StepButton, DragGestureRecognizer>();
	private static final ExtendedDragGestureImpl dgl = new ExtendedDragGestureImpl();
	private GModel model;
	
	/**
	 * Empty constructor for the extension manager.
	 */
	public CarbonAccountingTool() {}

	
	/**
	 * Constructor in script mode.
	 * @param mod a GModel instance
	 * @param step a Step instance
	 */
	public CarbonAccountingTool(GModel mod, Step step) {
		this();
		initInScriptMode(mod, step);
	}
	
	
	@Override
	public void init(GModel mod, Step step) {
		initializeTool(mod, step, true);
	}
	
	/**
	 * This method initializes the tool in script mode.
	 * @param mod a GModel instance 
	 * @param step a Step instance
	 */
	public void initInScriptMode(GModel mod, Step step) {
		initializeTool(mod, step, false);
	}
	
	/**
	 * This method initializes the carbon accounting tool either in script or in GUI mode.
	 * @param mod a GModel instance
	 * @param lastStep the last step
	 */
	protected void initializeTool(GModel model, Step lastStep, boolean guiMode) {
		if (!stepButtons.isEmpty()) {	// means the application is already running
			JOptionPane.showMessageDialog(MainFrame.getInstance(), 
					MessageID.CATAlreadyRunning.toString(), 
					UIControlManager.InformationMessageTitle.Information.toString(), 
					JOptionPane.INFORMATION_MESSAGE);
			return;
		} else {
			Window parentFrame = null;
			if (guiMode) {
				parentFrame = MainFrame.getInstance();
				Current.getInstance().addListener(this);
			}
			if (super.initializeTool(guiMode, parentFrame)) {
				List<CATCompatibleStand> stands = getStandList(lastStep);
				setStandList(stands);
				if (guiMode) {
					scanForStepButtons();
				}
			} else {
				stepButtons.clear();
				Current.getInstance().removeListener(this);
			}
		}
	}

	private void scanForStepButtons() {
		DragSource ds = new DragSource();
		List<StepButton> localStepButton = (List) CommonGuiUtility.mapComponents(parentFrame, StepButton.class);
		for (StepButton stepButton : localStepButton) {
			if (!stepButtons.containsKey(stepButton)) {
				DragGestureRecognizer dgr = ds.createDefaultDragGestureRecognizer(stepButton, DnDConstants.ACTION_REFERENCE, CarbonAccountingTool.dgl);
				stepButtons.put(stepButton, dgr);
			}
		}
	}
	
	

	@Override
	protected Vector<TreeLoggerDescription> findMatchingTreeLoggers(Object referent) {
		Vector<TreeLoggerDescription> treeLoggerDescriptions = super.findMatchingTreeLoggers(referent);
		if (model != null) {
			List<TreeLoggerDescription> additionalTreeLoggers = new ArrayList<TreeLoggerDescription>();
			additionalTreeLoggers.addAll(CapsisTreeLoggerDescription.getMatchingTreeLoggers(model));
			for (TreeLoggerDescription desc : additionalTreeLoggers) {
				if (!treeLoggerDescriptions.contains(desc)) {
					treeLoggerDescriptions.add(desc);
				}
			}
		}
		return treeLoggerDescriptions;
	}

	private void clearAdditionalStep() {
		if (finalCutHadToBeCarriedOut) {
			Step stepToBeDeleted = ((GScene) getCarbonCompartmentManager().getLastStand()).getStep();
			stepToBeDeleted.setVisible(true);
			stepToBeDeleted.getProject().processDeleteStep(stepToBeDeleted);
			getCarbonCompartmentManager().init(null);
			System.out.println("Additional step eliminated!");
		}
	}
	
	@Override
	protected void setStandList() {
		clearAdditionalStep();
		model = ((GScene) waitingStandList.get(0)).getStep().getProject().getModel();
		super.setStandList();
	}
	
	@Override
	protected void shutdown(int shutdownCode) {
		clearAdditionalStep();
		if (!stepButtons.isEmpty()) {
			for (StepButton stepButton : stepButtons.keySet()) {
				stepButtons.get(stepButton).removeDragGestureListener(dgl);
			}
			stepButtons.clear();
			if (parentFrame instanceof MainFrame) {
				Current.getInstance().removeListener(this);
			}
		}
		System.out.println("Shutting down the CarbonCalculator engine...");
	}



	/**	
	 * Extension dynamic compatibility mechanism.
	 * This matchwith method checks if the extension can deal (i.e. is compatible) with the referent.
	 */
	static public boolean matchWith(Object referent) {
		try {
			if (!(referent instanceof GModel)) {return false;}		// the referent must inherit from the GModel class
			GModel m = (GModel) referent;
			GScene s = ((Step) m.getProject().getRoot()).getScene();
			if (!(s instanceof CATCompatibleStand)) {return false;}
			Tree t = ((TreeList) s).getTrees ().iterator ().next();
			if (!(t instanceof CATCompatibleTree)) {return false;}		// provides the wood product
			
			return true;
		} catch (Exception e) {
			Log.println (Log.ERROR, "DETimeCarbonContentWithSubstitution.matchWith ()", "Error in matchWith () (returned false)", e);
			return false;
		}
	}


	/*
	 * Useless (non-Javadoc)
	 * @see jeeb.lib.defaulttype.Extension#activate()
	 */
	@Override
	public void activate() {}


	protected static ArrayList<CATCompatibleStand> getStandList(Step step) {
		ArrayList<CATCompatibleStand> stands = new ArrayList<CATCompatibleStand>();
		Vector<Step> steps = step.getProject().getStepsFromRoot(step);
		for (Step stp : steps) {
			if (stp.getScene () instanceof CATCompatibleStand) {
				stands.add((CATCompatibleStand) stp.getScene());
			}
		}
		return stands;
	}

	@Override
	public void showUI() {
		getUI(MainFrame.getInstance()).setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
		super.showUI();
	}


	@Override
	public void somethingHappened (ListenedTo l, Object param) {
		scanForStepButtons();
	}

}
