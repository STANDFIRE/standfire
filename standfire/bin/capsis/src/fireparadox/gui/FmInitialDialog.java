package fireparadox.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import jeeb.lib.sketch.gui.EditorTab;
import jeeb.lib.sketch.gui.Panel3D;
import jeeb.lib.sketch.gui.SelectionManager;
import jeeb.lib.sketch.gui.SketcherManager;
import jeeb.lib.sketch.item.Grid;
import jeeb.lib.sketch.kernel.AddInfo;
import jeeb.lib.sketch.kernel.SimpleAddInfo;
import jeeb.lib.sketch.kernel.SketchController;
import jeeb.lib.sketch.kernel.SketchEvent;
import jeeb.lib.sketch.kernel.SketchFacade;
import jeeb.lib.sketch.kernel.SketchLinkable;
import jeeb.lib.sketch.kernel.SketchListener;
import jeeb.lib.sketch.scene.gui.TreeView;
import jeeb.lib.sketch.scene.kernel.SceneModel;
import jeeb.lib.sketch.util.SketchTools;
import jeeb.lib.util.Check;
import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.IconLoader;
import jeeb.lib.util.JWidthLabel;
import jeeb.lib.util.LinePanel;
import jeeb.lib.util.ListenedTo;
import jeeb.lib.util.Listener;
import jeeb.lib.util.Log;
import jeeb.lib.util.MessageDialog;
import jeeb.lib.util.Question;
import jeeb.lib.util.Settings;
import jeeb.lib.util.StatusDispatcher;
import jeeb.lib.util.Translator;
import jeeb.lib.util.UserDialog;
import jeeb.lib.util.WxHString;
import jeeb.lib.util.serial.Reader;
import jeeb.lib.util.serial.SerializerFactory;
import jeeb.lib.util.task.TaskManager;
import jeeb.lib.util.task.TaskManagerView;
import capsis.commongui.InitialDialog;
import capsis.commongui.ProjectFileAccessory;
import capsis.commongui.util.Helper;
import capsis.kernel.GModel;
import capsis.kernel.PathManager;
import capsis.lib.fire.FiStatePanel;
import capsis.util.AxBString;
import capsis.util.JSmartFileChooser;
import fireparadox.gui.database.FmChoiceDialog;
import fireparadox.gui.plantpattern.FmPatternMapDialog;
import fireparadox.model.FmInitialParameters;
import fireparadox.model.FmModel;
import fireparadox.model.FmStand;
import fireparadox.model.database.FmDBCommunicator;
import fireparadox.model.database.FmDBTeam;
import fireparadox.sketch.FmSketchLinker;


/**	FiInitialDialog - Dialog box to create the initial FireParadox stand.
*	Contains a 3D scene editor.
*
*	@author O. Vigy, E. Rigaud, F. de Coligny - september 2006
*/
public class FmInitialDialog extends InitialDialog implements SketchListener, 
		ActionListener, ComponentListener, /*CallBack,*/ SketchController, Listener {

	//~ static final Dimension TABS_PREFERRED_DIMENSION = new Dimension (350, 400);	// fc - 2.10.2007
	static final Dimension MAIN_PREFERRED_DIMENSION = new Dimension (350, 400);

	static final int MIN_WIDTH = 800;
	static final int MIN_HEIGHT = 300;

	private JButton trace;

	private boolean networkAvailable;

	private FmInitialParameters settings;
	private FmStand initStand;		// The initial stand to be generated
	private FmModel model;
	private FmSketchLinker sketchLinker;

	private JPanel main;
	private CardLayout cardLayout;	// Two tabs in main panel: generate stand and 3D scene editor
	private JButton prevNext;		// previous / next button

	// 
	private JTextField seed;
	private int seedval; // integer to deal with stochasticity
	
	// tab 1. scene initialisation
	private ButtonGroup buttonGroup1;
	private ButtonGroup buttonGroup2;

	private JRadioButton fromDatabase;
	private JTextField databaseInventoryName;
	private JButton databaseBrowse;

	private JRadioButton fromDetailed;
	private JRadioButton detailedViewOnly;
	private JRadioButton fromFieldParameters; // previously called fromIcfmePlot
	private JTextField fieldParameters; // previously called icfmePlotName
	private JButton fieldParameterBrowse; // previously called icfmePlotBrowse

	private JRadioButton detailedViewOnlyCover; // PhD 2008-09-17
	private JRadioButton detailedViewOnlyCoverFull; // PhD 2009-02-03
	private JRadioButton detailedDatabaseMatching;
	private JTextField detailedInventoryName;
	private JButton detailedBrowse;

	// private JRadioButton fromConstraints;
	// private JButton fieldParameters;
	private JRadioButton fromSVS; 
	private JTextField SVSname; 
	private JButton SVSBrowse; 

	
	private JRadioButton fromScratch;
	private JTextField xDimension;
	private JTextField yDimension;

	private JRadioButton savedScene;
	private JTextField savedSceneFileName;
	private JButton savedSceneBrowse;
	
	
	private JButton generateScene;	// fc - 2.10.2007
	private JCheckBox nextAfterGenerate;

	// tab 2: scene manipulation
	private JTabbedPane tabs;	// 3D scene editor lateral tabbed pane


	// Main components of the 3D scene editor
	private SceneModel sceneModel;
	private Panel3D panel3D;
	private SketcherManager sketchMan;
	private TreeView treeView;
	private SelectionManager selMan;	// 10.5.2007
	private EditorTab editorTab;

	private FiStatePanel statePanel;	// Fireparadox state panel

	private JLabel statusBar;	// fc - 1.6.2007 - status bar under the 3D scene editor

	// Control panel at the bottom
	private JButton ok;
	private JButton cancel;
	private JButton help;

	private JButton fuelEditor;
	private JButton patternEditor;

	// For database connection
	//~ private FiDBCommunicator bdCommunicator;		//to read database
	private JButton connexion;
	private boolean isConnected;
	private JLabel loginLabel;
	private JLabel passwordLabel;
	private JTextField loginTextField;
	private JPasswordField passwordTextField;
	private int rightLevel = 0;
	private LinkedHashMap<Long,FmDBTeam> teamMap;
	private FmDBTeam team;






	/**	Constructor.
	*	Creates the user interface. First tab deals with inventory loading, 2nd tab
	*	is the 3D scene editor.
	*/
	public FmInitialDialog (GModel m) throws Exception {
		super ();

		try {
			this.model = (FmModel) m;

			// Launch the task connecting to the database to build the buffers
			model.launchDataBaseBuffering ();
			
			// We know if network is available or not
			networkAvailable = model.isNetworkAvailable ();
			
			// Ask for the sketchLinker to the relay
			SketchLinkable relay = (SketchLinkable) model.getRelay ();
			sketchLinker = (FmSketchLinker) relay.getSketchLinker ();
			
			settings = model.getSettings ();
			initStand = null;
			isConnected = false;

			// Some icons here for sketch
			IconLoader.addPath ("fireparadox/gui/images");	// jlfgr.jar is built-in in IconLoader, we just add icons here
//			IconLoader.setDefaultPath ("fireparadox", "fireparadox/gui/images");	// jlfgr.jar is built-in in IconLoader, we just add icons here

			model.addListener (this);	// we want to be told when buffers are loaded

			// Create Sketch material with SketchFacade
			SketchFacade facade = new SketchFacade (this, PathManager.getDir("etc"));	// fc - 2.12.2008

			// sketch.addons was moved from capsis4/etc/ to capsis/extension/sketchaddon/
			// (i.e. in the classpath, related to the use of sketch in a jar file)
			
			facade.addExtensionResource ("fireparadox/sketch/sketch.addons");

			sceneModel = facade.getSceneModel ();
			sceneModel.setEditable (this, true);			// posible to add items interactively in it
			sceneModel.addSketchListener (this);	// we want to know when sceneModel changes

			// fc - 7.9.2009 - new SketchLinker framework
			sceneModel.setSketchLinker (this, sketchLinker);

			panel3D = facade.getPanel3D ();
			sketchMan = facade.getSketcherManager ();
			facade.addStatusListener (this);
			treeView = facade.getTreeView ();
			// the selection Manager will respond on fire trees selection - fc - 14.5.2007
			Collection candidateObjects = new ArrayList ();
			//~ candidateObjects.add (new FiPlant ()); // fp - 19.05.09 change constructor 2
			candidateObjects.add (new String ());	// try to get a simple inspector in the selection manager - fc - 25.9.2007
			selMan = facade.createSelectionManager (candidateObjects, model);

			editorTab = facade.getEditorTab ();
			if (editorTab == null) {throw new Exception ("Exception in FiInitialDialog constructor, editorTab = null, check Log");}

				// Add a grid
				Grid grid = new Grid ();	// Grid item
				AddInfo addInfo = new SimpleAddInfo (grid.getType (), SketchTools.inSet (grid));
				sceneModel.getUndoManager ().undoableAddItems (this, addInfo);

			createUI ();

			// Tell the user if the network is not available
			if (!networkAvailable) {
				StatusDispatcher.print (Translator.swap ("FiModel.offlineMode"));
				// Tune UI to work offline
				offlineMode ();
			}

			// location is set by AmapDialog
			pack ();

			// Try to restore user size
			try {
				WxHString memory = new WxHString (
						Settings.getProperty ("fire.initial.dialog.size", (String)null));
				if (memory.getW () < 200 || memory.getH () < 200) {throw new Exception ();}
				setSize (memory.getW (), memory.getH ());
			} catch (Exception e) {
				setSize (new Dimension (700, getSize ().height));
			}
			addComponentListener (this);	// will know when resized

			show ();

		} catch (Exception exc) {
			Log.println (Log.ERROR, "FiInitialDialog.c ()", exc.toString (), exc);
			throw exc;	// Object viewers may throw exception
		}
	}

	/**	Called at construction time if the network is not available
	*/
	private void offlineMode () {

		// fromDatabase is not available
		fromDatabase.setEnabled (false);
		if (fromDatabase.isSelected ()) {
			fromDetailed.doClick ();
		}

		// Activate buttons for loading / generating the initial scene
		prevNext.setEnabled (true);
		generateScene.setEnabled (true);
	}

		/**	Called by ListenedTo when something happens.
		*/
		public void somethingHappened (ListenedTo l, Object param) {
			if (l.equals (model)) {
				// param is the return code (0 = ok)
				Integer rc = (Integer) param;
				if (rc == 0) {	// ok
					connexion.setEnabled (true);
					try {
						teamMap = model.getTeamMap ();	// now loaded
						statusBar.setText (Translator.swap ("FiInitialDialog.buffersWereLoadedCorrectlyFromTheDataBase"));

						// Activate buttons for loading / generating the initial scene
						prevNext.setEnabled (true);
						generateScene.setEnabled (true);

					} catch (Exception e) {
						Log.println (Log.ERROR, "FiInitialDialog.somethingHappened ()",
								"Buffers seem correctly loaded but could not get the teamMap", e);
						statusBar.setText (Translator.swap (
								"FiInitialDialog.buffersSeemCorrectlyLoadedButCouldNotGetTheTeamMapSeeLog"));
					}
				} else {
					statusBar.setText (Translator.swap (
							"FiInitialDialog.errorWhileLoadingBuffersFromTheDataBaseSeeLog"));

					// Activate buttons for loading / generating the initial scene (local mode)
					prevNext.setEnabled (true);
					generateScene.setEnabled (true);
					
				}
			}
		}

	/**	Called by model when species loading from the fuel database is over.
	*	param is a String: the status of the action (ok or not). Tell the user it's over
	*	and the final status (ok or not).
	*/
	//~ public void callBack (Object source, Object param) {
		//~ status.setText (""+param);
		//~ progressBar.setIndeterminate (false);
		//~ progressBar.setValue (100);
		//~ progressBar.setStringPainted (true);
	//~ }

	//	Waits until the species list is loaded. FiModel uses another thread to
	//	load the species list (may be long) by calling the loadSpecies () method.
	//	This method is synchronized : call here from the dispatch event thread
	//	will block until the initial thread has finished it own call thus ensuring that
	//	the list has been loaded.
	//
	//~ private void waitUntilSpeciesListIsLoaded () {

		//~ System.out.println ("waitUntilDataListIsLoaded...");
		//~ model.loadTeams ();
		//~ model.loadSpecies ();
		//~ System.out.println ("ok, dataListIsLoaded, continue");

	//~ }

	//	detailedBrowse button was hit : open a file chooser
	//
	private void detailedBrowseAction () {
		JFileChooser chooser = new JFileChooser (Settings.getProperty (
			"capsis.inventory.path", ""));
		ProjectFileAccessory acc = new ProjectFileAccessory ();
		chooser.setAccessory (acc);
		chooser.addPropertyChangeListener (acc);
		int returnVal = chooser.showOpenDialog (this);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			Settings.setProperty ("capsis.inventory.path", chooser.getSelectedFile ().toString ());
			String fileName = chooser.getSelectedFile ().toString ();	//ov 07.08.07
			detailedInventoryName.setText (fileName);
		}
	}

	private FmStand loadDetailedViewOnly () throws Exception {
		try {
			// Build initial scene
			settings.setInitMode (FmInitialParameters.InitMode.DETAILED_VIEW_ONLY, seedval);
			settings.detailedInventoryName = detailedInventoryName.getText ();
			
			settings.buildInitScene(model);
			initStand = (FmStand) settings.getInitScene ();
			
		} catch (Exception e) {
			Log.println (Log.ERROR, "FiInitialDialog.loadDetailedViewOnlyCoverFull ()",
				"Scene file error see Log", e);
			MessageDialog.print (this, Translator.swap ("FiInitialDialog.detailedInventoryFileErrorSeeLog"), e);
			throw e;
		}
		return initStand;

	}
	
	private FmStand loadFromFieldParameters() throws Exception {
		try {
			// Build initial scene
			settings
					.setInitMode(FmInitialParameters.InitMode.FROM_FIELD_PARAMETERS, seedval);
			settings.fieldParameters = fieldParameters.getText();
			
			settings.buildInitScene(model);
			initStand = (FmStand) settings.getInitScene ();

		} catch (Exception e) {
			Log.println (Log.ERROR, "FiInitialDialog.detailedBrowseAction ()",
				"Scene file error see Log", e);
			MessageDialog.print (this, Translator.swap ("FiInitialDialog.icfmePlotsErrorSeeLog"), e);
			throw e;
		}
		return initStand;

	}


	private FmStand loadDetailedViewOnlyCover () throws Exception {			// PhD 2008-09
		try {
			String fileName = detailedInventoryName.getText ();
			boolean databaseMatching = detailedDatabaseMatching.isSelected ();

			initStand = (FmStand) model.loadDetailedViewOnlyCover (fileName);

		} catch (Exception e) {
			Log.println (Log.ERROR, "FiInitialDialog.loadDetailedViewOnlyCover ()",
				"Scene file error see Log", e);
			MessageDialog.print (this, Translator.swap ("FiInitialDialog.detailedInventoryFileErrorSeeLog"), e);
			throw e;
		}
		return initStand;
	}

	private FmStand loadDetailedViewOnlyCoverFull () throws Exception {			// PhD 2009-02-03
		try {
			String fileName = detailedInventoryName.getText ();
			boolean databaseMatching = detailedDatabaseMatching.isSelected ();

			initStand = (FmStand) model.loadDetailedViewOnlyCoverFull (fileName);

		} catch (Exception e) {
			Log.println (Log.ERROR, "FiInitialDialog.loadDetailedViewOnlyCoverFull ()",
				"Scene file error see Log", e);
			MessageDialog.print (this, Translator.swap ("FiInitialDialog.detailedInventoryFileErrorSeeLog"), e);
			throw e;
		}
		return initStand;
	}

	private FmStand loadFromSVSParameters() throws Exception {
		try {
			// Build initial scene
			settings
					.setInitMode(FmInitialParameters.InitMode.FROM_SVS_PARAMETERS, seedval);
			settings.SVSParameters = SVSname.getText();
			
			settings.buildInitScene(model);
			initStand = (FmStand) settings.getInitScene ();

		} catch (Exception e) {
			Log.println (Log.ERROR, "FiInitialDialog.detailedBrowseAction ()",
				"Scene file error see Log", e);
			MessageDialog.print (this, Translator.swap ("FiInitialDialog.SVSErrorSeeLog"), e);
			throw e;
		}
		return initStand;

	}

	
	
	//	The databaseBrowse button was hit : open a file chooser
	//
	private void databaseBrowseAction () {
		JFileChooser chooser = new JFileChooser (Settings.getProperty (
			"capsis.inventory.path", ""));
		ProjectFileAccessory acc = new ProjectFileAccessory ();
		chooser.setAccessory (acc);
		chooser.addPropertyChangeListener (acc);
		int returnVal = chooser.showOpenDialog (this);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			Settings.setProperty ("capsis.inventory.path", chooser.getSelectedFile ().toString ());
			String fileName = chooser.getSelectedFile ().toString ();
			databaseInventoryName.setText (fileName);
		}
	}

	// The icfmeBrowse button was hit : open a file chooser, now renamed
	// fieldParameterBrowseAction
	//
	private void fieldParameterBrowseAction() {
		JFileChooser chooser = new JFileChooser (Settings.getProperty (
			"capsis.icfmePlot.path", ""));
		ProjectFileAccessory acc = new ProjectFileAccessory ();
		chooser.setAccessory (acc);
		chooser.addPropertyChangeListener (acc);
		int returnVal = chooser.showOpenDialog (this);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			Settings.setProperty ("capsis.icfmePlot.path", chooser.getSelectedFile ().toString ());
			String fileName = chooser.getSelectedFile ().toString ();
			fieldParameters.setText(fileName);
		}
	}

	// The svsParameterBrowse button was hit : open a file chooser
	//
	private void SVSParameterBrowseAction() {
		JFileChooser chooser = new JFileChooser (Settings.getProperty (
			"capsis.svs.path", ""));
		ProjectFileAccessory acc = new ProjectFileAccessory ();
		chooser.setAccessory (acc);
		chooser.addPropertyChangeListener (acc);
		int returnVal = chooser.showOpenDialog (this);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			Settings.setProperty ("capsis.svs.path", chooser.getSelectedFile ().toString ());
			String fileName = chooser.getSelectedFile ().toString ();
			SVSname.setText(fileName);
		}
	}	
	

	private FmStand loadDataBaseFile () throws Exception {
		try {
			// fc - 28.9.2007
			// we must wait till we have the species list from the data base (loaded in another thread)
			//~ waitUntilSpeciesListIsLoaded ();

			String fileName = databaseInventoryName.getText ();
			//~ boolean databaseMatching = detailedDatabaseMatching.isSelected ();

			initStand = (FmStand) model.loadDataBaseFile (fileName);

		} catch (Exception e) {
			Log.println (Log.ERROR, "FiInitialDialog.loadDataBaseFile ()",
				"Scene file error see Log", e);
			MessageDialog.print (this, Translator.swap ("FiInitialDialog.loadDataBaseFileErrorSeeLog"), e);
			throw e;
		}

		return initStand;
	}

	/**	Not implemented yet
	 */
	private FmStand loadDetailedDatabaseMatching () throws Exception {
		MessageDialog.print (this, "loadDetailedDatabaseMatching () was not implemented yet...");
		throw new Exception ("loadDetailedDatabaseMatching () was implemented yet...");

//		return initStand,
	}



	//
	private FmStand loadFromScratch () throws Exception {
		String sXdim = xDimension.getText ().trim ();
		if (!Check.isDouble (sXdim)) {
			MessageDialog.print (this, Translator.swap (
				"FiInitialDialog.plotXDimensionShouldBeAPositiveDouble"));
			throw new Exception ();
		}
		double xDim = Check.doubleValue (sXdim);

		String sYdim = yDimension.getText ().trim ();
		if (!Check.isDouble (sYdim)) {
			MessageDialog.print (this, Translator.swap (
				"FiInitialDialog.plotYDimensionShouldBeAPositiveDouble"));
			throw new Exception ();
		}
		double yDim = Check.doubleValue (sYdim);

		if (xDim<=0) {
			MessageDialog.print (this, Translator.swap (
				"FiInitialDialog.plotXDimensionShouldBeAPositiveDouble"));
			throw new Exception ();
		}
		if (yDim<=0) {
			MessageDialog.print (this, Translator.swap (
				"FiInitialDialog.plotYDimensionShouldBeAPositiveDouble"));
			throw new Exception ();
		}

		Settings.setProperty ("fire.from.scratch.plot.size", new AxBString (xDim, yDim).toString ());

		try {
			// Build initial scene
			settings.setInitMode (FmInitialParameters.InitMode.FROM_SCRATCH, seedval);
			settings.xDim = xDim;
			settings.yDim = yDim;
			
			settings.buildInitScene(model);
			initStand = (FmStand) settings.getInitScene ();

		} catch (Exception e){
			Log.println (Log.ERROR, "FiInitialDialog.nextFromScratchAction ()",
				"exception during plot construction", e);
			MessageDialog.print (this, Translator.swap (
				"FiInitialDialog.exceptionDuringPlotConstructionSeeLog")+"\n"+e.getMessage (), e);
			throw new Exception ();
		}
		return initStand;
	
	}

	// Get a fileName for the savedScene feature
	private void savedSceneBrowseAction () {
		// Open a file chooser to get the file name
		JFileChooser chooser = new JSmartFileChooser (
					Translator.swap ("FiInitialDialog.savedScene"),
					Translator.swap ("Shared.open"),
					Translator.swap ("Shared.open"),
					Settings.getProperty ("fireparadox.file.path", (String)null),
					false);	// DIRECTORIES_ONLY=false

		//~ chooser.addChoosableFileFilter (new ScenarioFileFilter ());
		chooser.setAcceptAllFileFilterUsed (true);

//		int returnVal = chooser.showDialog (MainFrame.getInstance (), null);	// null : approveButton text was already set
		int returnVal = chooser.showDialog (this, null);	// null : approveButton text was already set

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			Settings.setProperty ("fireparadox.file.path", chooser.getSelectedFile ().getParent ());
			savedSceneFileName.setText (chooser.getSelectedFile ().getPath ());
		}
	}

	// Reload a scene that was previously saved for reedition
	private FmStand loadSavedScene () {

		String fileName = savedSceneFileName.getText ().trim ();

		try {
			Reader read = SerializerFactory.getReader (fileName);
			FmStand savedStand = (FmStand) read.readObject ();

			try {
				read.testCompatibility ();
			} catch (Exception e) {
				Log.println (Log.WARNING, "FiInitialDialog.loadSavedScene ()",
						"Warning, there was a change in the scene format since the file was saved, passed");
				MessageDialog.print (this, Translator.swap (
						"FiInitialDialog.warningThereWasAChangeInSceneFormatSinceTheFileWasSavedPassed"), e);
			}

			initStand = savedStand;

		} catch (Exception e) {
			Log.println (Log.ERROR, "FiInitialDialog.loadSavedScene ()",
					"Could not load savedScene: "+fileName, e);
			MessageDialog.print (this, Translator.swap (
					"FiInitialDialog.couldNotLoadSavedFileSeeLog")+": "+fileName);
		}
		return initStand;
	}


	
	
	
	/**	Create the initial stand according to the current options
	 */
	private FmStand createInitialStand () {

		try {
			
			String sseed = seed.getText ().trim ();
			if (!Check.isInt(sseed)) {
				MessageDialog.print (this, Translator.swap (
					"FiInitialDialog.seedShouldBeAnInteger"));
				throw new Exception ();
			}
			this.seedval = Check.intValue(sseed);
			if (fromDatabase.isSelected ()) {
				initStand = loadDataBaseFile ();

			} else if (fromDetailed.isSelected ()) {
				
				if (detailedViewOnly.isSelected ()) {
					initStand = loadDetailedViewOnly ();
					
				}  else if (detailedViewOnlyCover.isSelected ()) { // PhD 2008-09-17
					initStand = loadDetailedViewOnlyCover ();
					
				} else if (detailedViewOnlyCoverFull.isSelected ()) { // PhD 2009-02-03
					initStand = loadDetailedViewOnlyCoverFull ();
					
				} else {
					initStand = loadDetailedDatabaseMatching ();
					
				}
			} else if (fromFieldParameters.isSelected()) {
				initStand = loadFromFieldParameters();

			} else if (fromSVS.isSelected()) {
				initStand = loadFromSVSParameters();

				
			} else if (fromScratch.isSelected ()) {
				initStand = loadFromScratch ();

			} else if (savedScene.isSelected ()) {
				initStand = loadSavedScene ();
				
			}
		} catch (Exception e) {
			Log.println (Log.ERROR, "FiInitialDialog.createInitialStand ()", 
					"Exception, could not create initial stand", e);
			return null;
		}
		
		return initStand;
	}
		
	
	
	/**	Update the sceneModel to show this stand.
	*/
	private void updateScene (FmStand stand) {
		try {
			
//			boolean includingTechnicalItems = false;
//			sceneModel.getUndoManager ().undoableRemoveAllItems (this, includingTechnicalItems);
			sceneModel.clearModel(this);		
			
//			model.setInitStand (stand);

			sketchLinker.updateSketch (stand, sceneModel);

		} catch (Exception e) {
			Log.println (Log.ERROR, "FiInitialDialog.updateScene ()",
					"Exception while updating the SceneModel", e);
			MessageDialog.print (this, Translator.swap ("FiInitialDialog.couldNotUpdateSceneSeeLog"));
			
		}

	}

	
		
	/**	Action on 'Generate Scene' -> load a scene or create an empty scene
	 *	depending on the current options on the tab 1.
	 */
	private void generateSceneAction () {
		// If something to lose, ask for a confirmation
		if (initStand != null) {
			if (!Question.ask (this,
					Translator.swap ("FiInitialDialog.confirm"), 
					Translator.swap ("FiInitialDialog.thisActionWillEraseTheCurrentSceneDoYouWantToContinue"))) {
				return;
			}
		}
		
		// Create the initial stand
		initStand = createInitialStand ();  // tells the user if error during creation
		
		if (initStand == null) {return;}
		
		// Create the 3D scene in Sketch (deferred till here)
		updateScene (initStand);
		
		if (nextAfterGenerate.isSelected ()) {
			prevNextAction ();
		}

	}

	//	Action on next -> generally, go to scene editor
	//
	private void prevNextAction () {
		if (prevNext.getText ().equals (Translator.swap ("FiInitialDialog.next"))) {
			if (initStand == null) {

				// Create the initial stand
				initStand = createInitialStand ();  // tells the user if error during creation
				
				if (initStand == null) {return;}
				
				// Create the 3D scene in Sketch (deferred till here)
				updateScene (initStand);

				
//				MessageDialog.print (this, Translator.swap (
//					"FiInitialDialog.pleaseChooseASceneCreationMode"));
//				return;
			}

			cardLayout.next (main);
			prevNext.setText (Translator.swap ("FiInitialDialog.previous"));
		} else {
			cardLayout.previous (main);
			prevNext.setText (Translator.swap ("FiInitialDialog.next"));
		}
		statusBar.setText (" ");
	}


	// Ok was hit : controls and go out the dialog
	//
	private void okAction () {
		
		// Create the initial stand
		if (initStand == null) {
			initStand = createInitialStand ();
		}
		
		// Check that we have an initStand (needed on Ok)
		if (initStand == null) {
//			MessageDialog.print (this, 
//					Translator.swap ("FiInitialDialog.pleaseLoadAScene"));
			return;
		}

		// Note: settings.buildInitScene () was already called
		setInitialParameters (settings);
		
		// Propose to save the scene for later reedition
		new FmSaveForReeditionDialog (initStand);
		
		setValidDialog (true);
	}

	
	// To be called on action on the radio buttons
	//
	private void buttonGroupAction () {
		databaseInventoryName.setEnabled (fromDatabase.isSelected ());
		databaseBrowse.setEnabled (fromDatabase.isSelected ());

		detailedInventoryName.setEnabled (fromDetailed.isSelected ());
		detailedBrowse.setEnabled (fromDetailed.isSelected ());
		detailedViewOnly.setEnabled (fromDetailed.isSelected ());
		fieldParameters.setEnabled(fromFieldParameters.isSelected());
		fieldParameterBrowse.setEnabled(fromFieldParameters.isSelected());
		SVSname.setEnabled(fromSVS.isSelected());
		SVSBrowse.setEnabled(fromSVS.isSelected());
		detailedViewOnlyCover.setEnabled (fromDetailed.isSelected ()); // PhD 2008-09-17
		detailedViewOnlyCoverFull.setEnabled (fromDetailed.isSelected ()); // PhD 2009-02-03
		//~ detailedDatabaseMatching.setEnabled (fromDetailed.isSelected ());	// fc - 3.6.2008 - DISABLED - TEMPORARY



		// fieldParameters.setEnabled (fromConstraints.isSelected ());

		xDimension.setEnabled (fromScratch.isSelected ());
		yDimension.setEnabled (fromScratch.isSelected ());

		savedSceneFileName.setEnabled (savedScene.isSelected ());
		savedSceneBrowse.setEnabled (savedScene.isSelected ());
	
	}

	//~ private void setLollypop (String lollypopClassName) {

	//~ ExtensionStarter starter = new ExtensionStarter ();
	//~ try {
	//~ lollypop = (Lollypop) extMan.loadExtension (lollypopClassName, starter);
	//~ lollypop.addActionListener (this);
	//~ lollypopScrollPane.getViewport ().setView (lollypop.getInstantPanel ());
	//~ reset ();
	//~ } catch (Throwable e) {
	//~ Log.println (Log.WARNING, "FiInitialDialog.setLollypop ()",
	//~ "could not load Lollypop "+lollypopClassName+" due to exception ", e);
	//~ }

	//~ }

	//~ private void updateState () {
	//~ // calculate stateCover
	//~ statePanel.update (initStand.getTrees ());
	//~ }

	/**	SketchListener interface
	*/
	public void sketchHappening (SketchEvent evt) {

	}

	/**	Some button was hit...
	 */
	public void actionPerformed (ActionEvent evt) {
		if (evt.getSource ().equals (fromDatabase)
				|| evt.getSource ().equals (fromDetailed)
				|| evt.getSource().equals(fromFieldParameters)
				|| evt.getSource ().equals (fromSVS)
				|| evt.getSource ().equals (fromScratch)
				|| evt.getSource ().equals (savedScene))  {
			buttonGroupAction ();

		} else if (evt.getSource ().equals (databaseBrowse)) {
			databaseBrowseAction ();

		} else if (evt.getSource().equals(fieldParameterBrowse)) {
			fieldParameterBrowseAction();

		} else if (evt.getSource ().equals (detailedBrowse)) {
			detailedBrowseAction ();

		} else if (evt.getSource ().equals (savedSceneBrowse)) {
			savedSceneBrowseAction ();

			
		} else if (evt.getSource ().equals (SVSBrowse)) {
			SVSParameterBrowseAction ();

		} else if (evt.getSource ().equals (generateScene)) {
			generateSceneAction ();

		} else if (evt.getSource ().equals ("Status")) {	// fc - 1.6.2007
			//~ SketchEvent e = (SketchEvent) evt;
			String message = (String) evt.getActionCommand ();
			
// TODO: FP temporary commented this line
			if (!(message==null)) {
			 statusBar.setText (message);}

		} else if (evt.getSource ().equals (prevNext)) {
			prevNextAction ();

		} else if (evt.getSource ().equals (ok)) {
			okAction ();

		} else if (evt.getSource ().equals (cancel)) {
			escapePressed ();

		} else if (evt.getSource ().equals (help)) {
			Helper.helpFor (this);

		} else if (evt.getSource ().equals (trace)) {
			System.out.println (trace ());
			Log.println ("Trace mode switch...");
			((SceneModel) sceneModel).setTrace (!((SceneModel) sceneModel).isTrace ());

		} else if (evt.getSource ().equals (connexion)
				|| evt.getSource ().equals (loginTextField)
				|| evt.getSource ().equals (passwordTextField)) {
			checkRights ();

		} else if (evt.getSource ().equals (fuelEditor) ) {
			try {
				FmChoiceDialog dlg = new FmChoiceDialog (model);
				dlg.dispose ();
			} catch (Exception e) {
				Log.println (Log.ERROR, "FiInitialDialog.actionPerformed ()",
					"exception while opening fuel editor", e);
				MessageDialog.print (this, Translator.swap ("FiInitialDialog.exceptionWhileOpeningFuelEditorSeeLog"));
			}

		} else if (evt.getSource ().equals (patternEditor) ) {
			new FmPatternMapDialog (model);
		}
	}

	/**	To attribute user right with login and password - IL April 2008
	 */
	private void checkRights () {

		//~ teamMap = model.getTeamsList();
		rightLevel = 0;
		char[] input = passwordTextField.getPassword ();
		String password = new String (input);

		//visitor
		if (loginTextField.getText ().equals ("visitor") ) {
			team = null;
			rightLevel = 1;

		}
		//team
		else if (teamMap != null) {
			for (Iterator i = teamMap.keySet().iterator(); i.hasNext ();) {
				Object cle = i.next();
				FmDBTeam t = (FmDBTeam) teamMap.get(cle);
				if (loginTextField.getText ().equals (t.getTeamCode()))  {


					//check if password is valid
					Long teamId = t.getTeamId();
					boolean passworkOk = false;

					try {
						passworkOk = FmDBCommunicator.getInstance ().checkTeamPass (teamId, password);
						if (passworkOk) {
							team = t;

							if (team.getTeamCode ().equals ("Admin")) {
								rightLevel = 9;
							}
							else {
								rightLevel = 2;
							}
						}
					} catch (Exception e) {
						Log.println (Log.ERROR, "FiInitialDialog", "error while READING TEAM password", e);
					}

				}
			}
		}

		//set the model with the right values
		model.setRightLevel(rightLevel);
		model.setTeamLogged(team);
		model.setTeamPassword(password);

		if (rightLevel > 0) {
			isConnected = !isConnected;
			model.getPatternMap ().setAdmin (isConnected);
			model.getPatternList ().setAdmin (isConnected);
			if (isConnected) {
				fuelEditor.setVisible (true);
				passwordTextField.setVisible (false);
				loginTextField.setVisible (false);
				loginLabel.setVisible (false);
				passwordLabel.setVisible (false);
				connexion.setText (Translator.swap ("FiInitialDialog.connectOn"));
			} else {
				fuelEditor.setVisible (false);
				passwordTextField.setVisible (true);
				loginTextField.setVisible (true);
				loginLabel.setVisible (true);
				passwordLabel.setVisible (true);
				connexion.setText (Translator.swap ("FiInitialDialog.connectOff"));
			}
		} else {
			JOptionPane.showMessageDialog (this, Translator.swap ("FiInitialDialog.badLoginOrPassword"));
		}
		pack ();
	}

	public void componentHidden (ComponentEvent e) {}
	public void componentMoved (ComponentEvent e) {}
	public void componentResized (ComponentEvent e) {
		 int width = getWidth();
         int height = getHeight();
         //we check if either the width
         //or the height are below minimum
         boolean resize = false;
           if (width < MIN_WIDTH) {
                resize = true;
                width = MIN_WIDTH;
         }
           if (height < MIN_HEIGHT) {
                resize = true;
                height = MIN_HEIGHT;
           }
         if (resize) {
               setSize(width, height);
         }
		WxHString memory = new WxHString (getSize ().width, getSize ().height);
		Settings.setProperty ("fire.initial.dialog.size", memory.toString ());
	}
	public void componentShown (ComponentEvent e) {}


	/**	Called on ctrl-Z. Can trigger an undo () method.
	 */
	protected void ctrlZPressed () {	// fc - 18.6.2007
		System.out.println ("FiInitialDialog.ctrlZPressed ()");
		sceneModel.getUndoManager ().undo ();
	}

	/**	Called on ctrl-Y. Can trigger a redo () method.
	 */
	protected void ctrlYPressed () {	// fc - 18.6.2007
		System.out.println ("FiInitialDialog.ctrlYPressed ()");
		sceneModel.getUndoManager ().redo ();
	}

	/**	Called on Escape.
	*/
	protected void escapePressed () {
		JButton yes = new JButton (Translator.swap ("Shared.yes"));
		JButton no = new JButton (Translator.swap ("Shared.no"));
		Vector<JButton> buttons = new Vector<JButton> ();
		buttons.add (yes);
		buttons.add (no);

		JButton choice = UserDialog.promptUser (this, Translator.swap ("FiInitialDialog.confirmation"),
				Translator.swap ("FiInitialDialog.pleaseConfirmWindowClosing"), buttons, no);
		if (choice.equals (yes)) {
			initStand = null;
			setVisible(false);
		}
	}


	/**	Inits the GUI.
	 */
	private void createUI () {

		//~ main = new JTabbedPane ();
		cardLayout = new CardLayout ();
		main = new JPanel (cardLayout);

		//~ buttonGroup1 = new ButtonGroup ();

		// 1. Scene generation: initialisationTab
		JPanel initialisationTab = new JPanel (new BorderLayout ());

		ColumnPanel p1 = new ColumnPanel (Translator.swap (
				"FiInitialDialog.sceneGeneration"));
		//~ Border etched = BorderFactory.createEtchedBorder ();
		//~ Border b1 = BorderFactory.createTitledBorder (etched, Translator.swap (
			//~ "FiInitialDialog.sceneGeneration"));
		//~ p1.setBorder (b1);

		LinePanel l = new LinePanel ();
		l.add (new JWidthLabel ("",25));
		l.add (new JWidthLabel (Translator.swap ("FiInitialDialog.seed")+" :",75));
		seed = new JTextField ("0");
		l.add (seed);
		l.addGlue ();
		p1.add (l);

		
		buttonGroup1 = new ButtonGroup ();
		buttonGroup2 = new ButtonGroup ();
		//buttonGroup3 = new ButtonGroup ();

		// File with fuelIds and locations, needs a connection to the data base
		LinePanel l0 = new LinePanel ();
		fromDatabase = new JRadioButton (Translator.swap ("FiInitialDialog.fromDatabase")+" : ", false);
		fromDatabase.addActionListener (this);
		fromDatabase.setEnabled(false);
		buttonGroup1.add (fromDatabase);
		l0.add (fromDatabase);
		databaseInventoryName = new JTextField (15);
		l0.add (databaseInventoryName);
		databaseBrowse = new JButton (Translator.swap ("FiInitialDialog.databaseBrowse"));
		// initial version - ov - 07.08.07
					//~ fromDatabase.setEnabled(false);
		databaseInventoryName.setEnabled(false);
		databaseBrowse.setEnabled(false);
		// initial version - ov
		databaseBrowse.addActionListener (this);
		l0.add (databaseBrowse);
		l0.addStrut0 ();
		//p1.add (l0);

		// From inventory
		LinePanel l1 = new LinePanel ();
		fromDetailed = new JRadioButton (Translator.swap ("FiInitialDialog.fromDetailed")+" : ", false);
		fromDetailed.addActionListener (this);
		buttonGroup1.add (fromDetailed);
		l1.add (fromDetailed);
		detailedInventoryName = new JTextField (15);
		l1.add (detailedInventoryName);
		detailedBrowse = new JButton (Translator.swap ("FiInitialDialog.detailedBrowse"));
		detailedBrowse.addActionListener (this);
		l1.add (detailedBrowse);
		l1.addStrut0 ();
		

		LinePanel l20 = new LinePanel ();
		l20.add (new JWidthLabel ("", 25));
		detailedViewOnly = new JRadioButton (Translator.swap ("FiInitialDialog.detailedViewOnly"), false);
		detailedViewOnly.addActionListener (this);
		buttonGroup2.add (detailedViewOnly);
		l20.add (detailedViewOnly);

		// PhD 2008-09-17
		detailedViewOnlyCover = new JRadioButton(Translator
				.swap("FiInitialDialog.detailedViewOnlyCover"), false);
		detailedViewOnlyCover.addActionListener (this);
		buttonGroup2.add (detailedViewOnlyCover);
		l20.add (detailedViewOnlyCover);
		// PhD 2008-09-17

		// PhD 2009-02-03
		detailedViewOnlyCoverFull = new JRadioButton(Translator
				.swap("FiInitialDialog.detailedViewOnlyCoverFull"), false);
		detailedViewOnlyCoverFull.addActionListener (this);
		buttonGroup2.add (detailedViewOnlyCoverFull);
		l20.add (detailedViewOnlyCoverFull);
		// PhD 2009-02-03

		detailedDatabaseMatching = new JRadioButton (Translator.swap ("FiInitialDialog.detailedDatabaseMatching"), false);
		detailedDatabaseMatching.addActionListener (this);
			detailedDatabaseMatching.setEnabled (false);	// fc - 3.6.2008
		buttonGroup2.add (detailedDatabaseMatching);
		l20.add (detailedDatabaseMatching);
		l20.addGlue ();
		

		// ICFMEplots
		LinePanel l30 = new LinePanel ();
		fromFieldParameters = new JRadioButton(Translator
				.swap("FiInitialDialog.icfmePlots")
				+ " : ", true);
		fromFieldParameters.addActionListener(this);
		buttonGroup1.add(fromFieldParameters);
		l30.add(fromFieldParameters);
		fieldParameters = new JTextField(15);
		l30.add(fieldParameters);
		fieldParameterBrowse = new JButton(Translator
				.swap("FiInitialDialog.icfmePlotBrowse"));
		fieldParameters.setEnabled(false);
		fieldParameterBrowse.setEnabled(false);
		fieldParameterBrowse.addActionListener(this);
		l30.add(fieldParameterBrowse);
		l30.addStrut0 ();
		p1.add (l30);
		p1.add (l1);
		p1.add (l20);
		
		// from SVS
		LinePanel l30a = new LinePanel ();
		fromSVS = new JRadioButton(Translator
				.swap("FiInitialDialog.fromSVS")
				+ " : ", true);
		fromSVS.addActionListener(this);
		fromSVS.setEnabled(false);
		buttonGroup1.add(fromSVS);
		l30a.add(fromSVS);
		SVSname = new JTextField(15);
		l30a.add(SVSname);
		SVSBrowse = new JButton(Translator
				.swap("FiInitialDialog.SVSBrowse"));
		SVSname.setEnabled(false);
		SVSBrowse.setEnabled(false);
		SVSBrowse.addActionListener(this);
		l30a.add(SVSBrowse);
		l30a.addStrut0 ();
		
		

		// From scratch
		ColumnPanel l3 = new ColumnPanel (0,0);

		LinePanel l31 = new LinePanel ();
		fromScratch = new JRadioButton (Translator.swap ("FiInitialDialog.fromScratch"));
		fromScratch.addActionListener (this);
		buttonGroup1.add (fromScratch);
		l31.add (fromScratch);
		l31.addGlue ();
		l3.add (l31);

		LinePanel l32 = new LinePanel ();
		l32.add (new JWidthLabel ("",25));
		l32.add (new JWidthLabel (Translator.swap ("FiInitialDialog.plotLength")+" :",75));
		xDimension = new JTextField ();
		l32.add (xDimension);
		l32.add (new JWidthLabel ("",25));
		l32.add (new JWidthLabel (Translator.swap ("FiInitialDialog.plotWidth")+" :",75));
		yDimension = new JTextField ();
		l32.add (yDimension);
		l32.addGlue ();
		l3.add (l32);

		// try to remember the last entries for plot size - fc - 2.10.2007
			try {
				String s = Settings.getProperty ("fire.from.scratch.plot.size", (String)null);
				if (s != null) {
					AxBString memory = new AxBString (s);
					xDimension.setText (""+memory.getA ());
					yDimension.setText (""+memory.getB ());
				}
			} catch (Exception e) {}

		p1.add (l3);



		// Scene file saved a last session (at the end of the initial dialog) for scene reedition
		LinePanel l9 = new LinePanel ();

		savedScene = new JRadioButton (Translator.swap ("FiInitialDialog.savedScene")+" : ", false);
		savedScene.addActionListener (this);
		buttonGroup1.add (savedScene);
		l9.add (savedScene);

		savedSceneFileName = new JTextField (15);
		l9.add (savedSceneFileName);

		savedSceneBrowse = new JButton (Translator.swap ("Shared.browse"));
		savedSceneBrowse.addActionListener (this);
		l9.add (savedSceneBrowse);

		l9.addStrut0 ();
		p1.add (l9);


		p1.add (l30a);
        p1.add (l0);

		
		


		// generateScene button - fc - 2.10.2007
		LinePanel l33 = new LinePanel ();
		l33.addGlue ();
		l33.add (new JLabel (Translator.swap ("FiInitialDialog.onceTheMethodChosen")+" : "));
		generateScene = new JButton (Translator.swap ("FiInitialDialog.generateScene"));
		generateScene.addActionListener (this);
		// This button will be enabled after the buffers are correctly loaded
		generateScene.setEnabled (false);
		l33.add (generateScene);
		nextAfterGenerate = new JCheckBox (Translator.swap ("FiInitialDialog.nextAfterGenerate"), true);
		l33.add (nextAfterGenerate);
		l33.addStrut0 ();
		p1.add (l33);



		// fc - 28.9.2007 --------------------------------------------
		// a progress bar to wait until the data base species list is loaded
		LinePanel l50 = new LinePanel ();

			// A TaskManagerView showing the most recent task of the TaskManager
			TaskManagerView tmView = new TaskManagerView (TaskManager.getInstance (), true);	// onlyMostRecentTask = true
			l50.add (tmView);
			l50.addGlue ();

		//~ status = new JTextField (10);
		//~ status.setEditable (false);
		//~ status.setText (Translator.swap ("FiInitialDialog.loadingSpeciesListFromFuelDataBase")+"...");
		//~ l50.add (status);
		//~ progressBar = new JProgressBar (0, 100);
		//~ progressBar.setValue (0);
		//~ progressBar.setIndeterminate(true);
		//~ l50.add (progressBar);
		//~ l50.addGlue ();

		//fromDatabase.setSelected (true);

		initialisationTab.add (p1, BorderLayout.NORTH);
		initialisationTab.add (l50, BorderLayout.SOUTH);
		main.add (initialisationTab, Translator.swap ("FiInitialDialog.initialisation"));

		// 2. Panel3D: manipulationTab

		// 3. Tabbed pane
		tabs = new JTabbedPane ();

		// 3.1 State panel
		statePanel = new FiStatePanel (sceneModel, model);
		JPanel aux0 = new JPanel (new BorderLayout ());
		aux0.add (statePanel, BorderLayout.NORTH);
		//~ aux0.setPreferredSize (TABS_PREFERRED_DIMENSION);
		tabs.addTab (Translator.swap ("FiInitialDialog.state"), aux0);

		// fc - 13.3.2007
		//~ treeView.setPreferredSize (TABS_PREFERRED_DIMENSION);
		tabs.addTab (Translator.swap ("FiInitialDialog.treeView"), treeView);
		//~ sketchMan.setPreferredSize (TABS_PREFERRED_DIMENSION);
		tabs.addTab (Translator.swap ("FiInitialDialog.sketcherManager"), sketchMan);

		//~ selMan.setPreferredSize (TABS_PREFERRED_DIMENSION);
		tabs.addTab (Translator.swap ("FiInitialDialog.selectionManager"), selMan);
		// fc - 18.3.2009
		//~ editorTab.setPreferredSize (TABS_PREFERRED_DIMENSION);
		tabs.addTab (Translator.swap ("FiInitialDialog.editorTab"), editorTab);

		// fc - 9.5.2008 - place the Panel3D with its toolBar
		JPanel panel3DWithToolBar = panel3D.getPanel3DWithToolBar (BorderLayout.EAST, true, true, true, true, true);

		// Add a trace button in the Panel3D toolBar
		JToolBar toolBar = panel3D.getToolBar ();
			trace = new JButton ("Trc");
			trace.addActionListener (this);
			toolBar.add (trace);

		//~ JPanel manipulationTab = new JPanel (new BorderLayout ());
		//~ manipulationTab.add (panel3DWithToolBar, BorderLayout.CENTER);
		//~ manipulationTab.add (tabs, BorderLayout.EAST);
		//~ main.add (manipulationTab, Translator.swap ("FiInitialDialog.manipulation"));

		tabs.setPreferredSize (MAIN_PREFERRED_DIMENSION);

		// fc-10.8.2015 trying to get rid of the panel3D resizing bug under MacOSX, mimic of FiStandViewer3D
		JPanel left = new JPanel(new BorderLayout());
		left.add(panel3DWithToolBar, BorderLayout.CENTER);
		JSplitPane split = new JSplitPane (JSplitPane.HORIZONTAL_SPLIT, left, tabs);
//		panel3DWithToolBar.setPreferredSize (MAIN_PREFERRED_DIMENSION);
//		JSplitPane split = new JSplitPane (JSplitPane.HORIZONTAL_SPLIT, panel3DWithToolBar, tabs);
		
		split.setResizeWeight (0.5);
		split.setContinuousLayout (true);
		split.setDividerLocation (0.5d);
		split.setBorder (null);

		main.add (split, Translator.swap ("FiInitialDialog.manipulation"));

		// Bottom panel: line1: status bar, line 2: admin + control
		// Line 1
		// Status bar
		LinePanel bottomLine1 = new LinePanel ();
		statusBar = new JLabel ();
		bottomLine1.add (statusBar);
		bottomLine1.addGlue ();

		// Line 2
		LinePanel bottomLine2 = new LinePanel ();

		// Admin panel
		JPanel adminPanel = new JPanel (new FlowLayout (FlowLayout.LEFT));
		loginTextField = new JTextField (7);
		loginTextField.setEnabled(false);
		loginTextField.setEditable(false);
		loginTextField.addActionListener (this);
		loginLabel = new JLabel (Translator.swap ("FiInitialDialog.login"));
		loginLabel.setEnabled(false);
		adminPanel.add (loginLabel);
		adminPanel.add (loginTextField);
		passwordTextField = new JPasswordField (7);
		passwordTextField.addActionListener (this);
		passwordTextField.setEditable(false);
		passwordTextField.addActionListener (this);
		
		passwordLabel = new JLabel (Translator.swap ("FiInitialDialog.password"));
		passwordLabel.setEnabled(false);
		adminPanel.add (passwordLabel);
		adminPanel.add (passwordTextField);

		connexion = new JButton (Translator.swap ("FiInitialDialog.connectOff"));
			connexion.setEnabled (false);	// fc
		adminPanel.add (connexion);
		connexion.addActionListener(this);

		fuelEditor = new JButton (Translator.swap ("FiInitialDialog.fuelEditor"));
		fuelEditor.setVisible (false);
		fuelEditor.addActionListener (this);
		patternEditor = new JButton (Translator.swap ("FiInitialDialog.patternEditor"));
		patternEditor.addActionListener (this);
		patternEditor.setEnabled(false);
		adminPanel.add (fuelEditor);
		adminPanel.add (patternEditor);

		// Control panel
		JPanel controlPanel = new JPanel (new FlowLayout (FlowLayout.RIGHT));

		prevNext = new JButton (Translator.swap ("FiInitialDialog.next"));
		// This button will be enabled after the buffers are correctly loaded
		prevNext.setEnabled (false);

		ok = new JButton (Translator.swap ("FiInitialDialog.ok"));
		cancel = new JButton (Translator.swap ("Shared.cancel"));
		help = new JButton (Translator.swap ("Shared.help"));

		controlPanel.add (prevNext);
		controlPanel.add (ok);
		controlPanel.add (cancel);
		controlPanel.add (help);

		prevNext.addActionListener (this);
		ok.addActionListener (this);
		cancel.addActionListener (this);
		help.addActionListener (this);

		bottomLine2.add (adminPanel);
		bottomLine2.add (controlPanel);

		ColumnPanel bottom = new ColumnPanel ();
		bottom.add (bottomLine1);
		bottom.add (bottomLine2);

		JPanel aux = new JPanel (new BorderLayout ());
		aux.add (main, BorderLayout.CENTER);
		aux.add (bottom, BorderLayout.SOUTH);

		setContentPane (aux);

		// fromDetailed.setSelected(true); //ov - 07.08.07

		buttonGroupAction ();

		setTitle (Translator.swap ("FiInitialDialog.initializeScenario"));
		setResizable (true);
		setModal (true);
	}

	public String trace () {
		String t = "\ntrace.FireInitialDialog... ";
		t+="\n";
		t+="SceneModel="+((SceneModel) sceneModel).trace ();
		t+="\n";
		t+="SetMap="+sceneModel.toString2 ();
		t+="\n";
		t+=panel3D.trace ();
		t+="\nend-of-trace.FireInitialDialog";
		t+="\n";
		return t;
	}


}



