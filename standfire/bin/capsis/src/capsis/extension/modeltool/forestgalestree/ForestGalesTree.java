/*
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 *
 * Copyright (C) 2000-2001  Francois de Coligny
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

package capsis.extension.modeltool.forestgalestree;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import jeeb.lib.util.Check;
import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.Command;
import jeeb.lib.util.JWidthLabel;
import jeeb.lib.util.Launcher;
import jeeb.lib.util.LinePanel;
import jeeb.lib.util.Log;
import jeeb.lib.util.MessageDialog;
import jeeb.lib.util.Question;
import jeeb.lib.util.Settings;
import jeeb.lib.util.StatusDispatcher;
import jeeb.lib.util.Translator;
import capsis.commongui.util.Helper;
import capsis.defaulttype.Tree;
import capsis.defaulttype.TreeCollection;
import capsis.extension.DialogModelTool;
import capsis.gui.MainFrame;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.PathManager;
import capsis.kernel.Step;
import capsis.util.methodprovider.ForestGalesFormat;
import capsis.util.methodprovider.ForestGalesTreeFormat;

/**
 * A connection tool for pp3 model (maritime pine) and ForestGales.
 * For individual tree calculation
 * @author C. Meredieu - august 2003
 * 
 */
public class ForestGalesTree extends DialogModelTool implements ItemListener, ActionListener, Command {

	static public final String AUTHOR="C. Meredieu";
	static public final String VERSION="1.02";

	// Both fgi and fgo file names are conventionnal
	// They are writen / read in capsis.tmp directory
	//
	public static final String fgiTreeFileName = PathManager.getDir("tmp")+File.separator+"FG_Tree_Capsis.fgi";
	public static final String fgoTreeFileName = PathManager.getDir("tmp")+File.separator+"FG_Tree_Capsis.fgo";

	
	private Step step;
	private GModel model;

//	private String defaultInventoryPath;
	private NumberFormat nf;
	static public final String columnSeparator = " ";	// choose here space or tab
	private BufferedWriter fgi;
	private BufferedReader fgo;

	private String header1;
	private String header2;
	

	// Possible values
	private Map speciesCodes;
	private Map soilCodes;
	private Map cultivationCodes;
	private Map drainageCodes;
	private Collection aCodes;
	private Collection kCodes;
//	private Collection damsCodes;

	// Chosen values
	private int speciesCode;
	private int soilCode;
	private int cultivationCode;
	private int drainageCode;
//	private int damsCode;
	private double aCode;
	private double kCode;

	private double upwindGapValue;	// fc - 24.9.2002

	
	// User controls
	private JTextField standId;	// project name (plus step name : a.1, a.2, ... a.55)
	private JComboBox species;		// from etc/forestgales.settings
	private JComboBox soil;			// from etc/forestgales.settings
	private JComboBox cultivation;	// from etc/forestgales.settings
	private JComboBox drainage;		// from etc/forestgales.settings
//	private JComboBox dams;			// from etc/forestgales.settings
	private JComboBox aCoefficient;
	private JComboBox kCoefficient;

	private JTextField upwindGap;

	private JButton trigger;

//	private JTextField fgiFileName;
//	private JButton fgiBrowse;
	private JButton launchFG;
	private JButton fgiTreeEdit;

//	private JTextField fgoFileName;
//	private JButton fgoBrowse;
	private JButton fgoTreeEdit;
//	private JButton fgoTartinate;

	private JButton close;	// after confirmation
	private JButton help;

	static {
		Translator.addBundle("capsis.extension.modeltool.forestgalestree.ForestGalesTree");
	}

	/**
	 * Phantom constructor.
	 * Only to ask for extension properties (authorName, version...).
	 */
	public ForestGalesTree () {		
		super();
	}
	
	@Override
	public void init(GModel m, Step s){

		try {

			step = s;
			model = m;

			setTitle (Translator.swap ("ForestGalesTree")+" - "+step.getCaption ());

			/*defaultInventoryPath = Settings.getProperty ("capsis.inventory.path",  PathManager.getDir ("data"));
			if ((defaultInventoryPath == null) || defaultInventoryPath.equals ("")) {
				defaultInventoryPath = PathManager.getInstallDir();
			}*/
			nf = NumberFormat.getInstance(Locale.ENGLISH);
			nf.setMinimumFractionDigits(2);
			nf.setMaximumFractionDigits(2);
			nf.setGroupingUsed (false);

			prepareCodesMaps ();

			createUI ();

			pack ();	// sets the size
			setVisible (true);
			setModal (false);
			
		} catch (Exception exc) {
			Log.println (Log.ERROR, "ForestGalesTree.c ()", exc.toString (), exc);
		}

	}

	/**
	 * Extension dynamic compatibility mechanism.
	 * This matchwith method checks if the extension can deal (i.e. is compatible) with the referent.
	 */
	static public boolean matchWith (Object referent) {
		try {
			if (!(referent instanceof GModel)) {return false;}
			GModel m = (GModel) referent;

			GScene s = ((Step) m.getProject ().getRoot ()).getScene ();
			if (!(s instanceof ForestGalesFormat)) {return false;}
			
			//need trees with id, dbh and height
			TreeCollection tc = (TreeCollection) s;
			if (!tc.getTrees ().isEmpty () && !(tc.getTrees ().iterator ().next () instanceof ForestGalesTreeFormat)) {return false;}


		} catch (Exception e) {
			Log.println (Log.ERROR, "ForestGalesTree.matchWith ()", "Error in matchWith () (returned false)", e);
			return false;
		}

		return true;
	}



	/**
	 * Get data to feed combo boxes.
	 */
	private void prepareCodesMaps () {

		// 1. get codifications in etc/forestGalesCodes.properties
		ForestGalesTreeCodes codes = null;
		try {
			try {	// try to get specific typologies for the module
				String modelPackageName = model.getIdCard ().getModelPackageName ();	// ex: "pp3"
				String fileName = PathManager.getDir("etc")
						+File.separator
						+modelPackageName
						+"_"
						+"forestGalesCodes.properties";
			System.out.println ("properties: "+fileName);
				codes = new ForestGalesTreeCodes (fileName);
			}
			catch (Exception e) {	// default forest gales typologies
			System.out.println ("error, default path used for propertiers");
				codes = new ForestGalesTreeCodes (PathManager.getDir("etc")
						+File.separator
						+"forestGalesCodes.properties");
			}

			speciesCodes = codes.getSpeciesMap ();
			soilCodes = codes.getSoilMap ();
			cultivationCodes = codes.getCultivationMap ();
			drainageCodes = codes.getDrainageMap ();
			aCodes = codes.getACollection ();
			kCodes = codes.getKCollection ();

			// Note : damsCodes is a Collection
		//	int damsMin = codes.getDamsMin ();
		//	int damsMax = codes.getDamsMax ();

		//	damsCodes = new TreeSet ();
		/*	for (int i = damsMin; i <= damsMax; i++) {
				damsCodes.add (new Integer (i));
			}*/

		} catch (Exception e) {
			MessageDialog.print (this, Translator.swap ("ForestGalesTree.errorWhileReadingCodes"));
			Log.println (Log.ERROR, "ForestGalesTree.prepareCodesMaps ()", "Error: "+e);
			return;
		}

		// 2. create file header2 (s)

		StringBuffer b = new StringBuffer ();
		b.append ("#FG1.3 input tree file");
		header1 = b.toString ();

		StringBuffer h = new StringBuffer ();
		h.append ("StandID");
		h.append (columnSeparator);
		h.append ("TreeID");
		h.append (columnSeparator);
		h.append ("Species");
		h.append (columnSeparator);
		h.append ("Soil");
		h.append (columnSeparator);
		h.append ("Cultivation");
		h.append (columnSeparator);
		h.append ("Drainage");
		h.append (columnSeparator);
		h.append ("Hg(m)");
		h.append (columnSeparator);
		h.append ("Dg(cm)");
		h.append (columnSeparator);
		h.append ("Spacing(m)");
		h.append (columnSeparator);
		h.append ("Upwindgap(m)");
		h.append (columnSeparator);
		h.append ("A");
		h.append (columnSeparator);
		h.append ("k");
		h.append (columnSeparator);
		h.append ("H(m)");
		h.append (columnSeparator);
		h.append ("D(cm)");

		header2 = h.toString ();

	}

	/**
	 * Listens to the extension type combo box.
	 * Memorize user choice to propose it directly next time.
	 */
	public void itemStateChanged (ItemEvent evt) {
		Object o = evt.getItem ();

		if (evt.getSource().equals (species)) {
			String key = (String) o;
			Settings.setProperty ("forest.gales.selected.species", key);

		} else if (evt.getSource().equals (soil)) {
			String key = (String) o;
			Settings.setProperty ("forest.gales.selected.soil", key);

		} else if (evt.getSource().equals (cultivation)) {
			String key = (String) o;
			Settings.setProperty ("forest.gales.selected.cultivation", key);

		} else if (evt.getSource().equals (drainage)) {
			String key = (String) o;
			Settings.setProperty ("forest.gales.selected.drainage", key);

/*		} else if (evt.getSource().equals (dams)) {
			String key = ""+ (Integer) o;
			Settings.setProperty ("forest.gales.selected.dams", key);*/

		} else if (evt.getSource().equals (aCoefficient)) {
			String key = ""+ (Double) o;
			Settings.setProperty ("forest.gales.selected.a", key);

		} else if (evt.getSource().equals (kCoefficient)) {
			String key = ""+ (Double) o;
			Settings.setProperty ("forest.gales.selected.k", key);

		}
	}

	/**
	 * Action on fgiBrowse button.
	 */
/*	private void fgiBrowseAction () {
		JFileChooser chooser = null;
		try {
			chooser = new JFileChooser (defaultInventoryPath);
			ProjectFileAccessory acc = new ProjectFileAccessory ();
			chooser.setAccessory (acc);
			chooser.addPropertyChangeListener (acc);
		} catch (Exception exc) {
			Log.println (Log.ERROR, "ForestGales.fgiBrowseAction ()",
					"Error while opening JFileChooser."
					+" "+exc.toString (), exc);
			return;
		}
		//chooser.setFileSelectionMode ();
		int returnVal = chooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			Settings.setProperty ("capsis.inventory.path", chooser.getSelectedFile ().toString ());
			fgiFileName.setText (chooser.getSelectedFile ().toString ());
		}
	}*/

	/**
	 * Check for input validity
	 */
	private boolean correctInput () {
		if (!Check.isDouble (upwindGap.getText ().trim ())) {
			MessageDialog.print (this, Translator.swap ("ForestGalesTree.upwindGapShouldBeDouble"));
			Log.println (Log.ERROR, "ForestGalesTree.correctInput ()", "upwind gap should be a double");
			return false;
		}
		double d = Check.doubleValue (upwindGap.getText ().trim ());
		if (d < 0) {
			MessageDialog.print (this, Translator.swap ("ForestGalesTree.upwindGapShouldBeGreaterThanZero"));
			Log.println (Log.ERROR, "ForestGalesTree.correctInput ()", "upwind gap should be greater than zero");
			return false;
		}

		return true;
	}

	/**
	 * Create the fgi tree file.
	 * Iterate on all trees for the selected step of a project
	 * create a line per tree in .fgi tree file
	 */
	private void createFgiTreeFile () throws Exception {
		if (!correctInput ()) {
			Log.println (Log.ERROR, "ForestGalesTree.createFgiTreeFile ()", "incorrect data, can not create fgi tree file");
			throw new Exception ("incorrect values, could not create fgi tree file");
		}

		// 1. Open .fgi file for output
		try {
			fgi = new BufferedWriter (new FileWriter (fgiTreeFileName));
			fgi.write (header1);
			fgi.newLine ();
			fgi.write (header2);
			fgi.newLine ();
		} catch (Exception e) {
			MessageDialog.print (this, Translator.swap ("ForestGalesTree.errorWhileOpeningFgiTreeFile"));
			Log.println (Log.ERROR, "ForestGalesTree.createFgiTreeFile ()", "Error: "+e);
			throw new Exception ("error while opening fgi tree file, could not create fgi tree file: "+e.toString ());
		}


		// 2. Evaluate global values (same for all records)
		
		String selectedSpecies = (String) species.getSelectedItem ();
		speciesCode = ((Integer) speciesCodes.get (selectedSpecies)).intValue ();

		String selectedSoil = (String) soil.getSelectedItem ();
		soilCode = ((Integer) soilCodes.get (selectedSoil)).intValue ();

		String selectedCultivation = (String) cultivation.getSelectedItem ();
		cultivationCode = ((Integer) cultivationCodes.get (selectedCultivation)).intValue ();

		String selectedDrainage = (String) drainage.getSelectedItem ();
		drainageCode = ((Integer) drainageCodes.get (selectedDrainage)).intValue ();

//		damsCode = ((Integer) dams.getSelectedItem ()).intValue ();

		Double selectedA = (Double) aCoefficient.getSelectedItem ();
		aCode = selectedA.doubleValue ();

		Double selectedK = (Double) kCoefficient.getSelectedItem ();
		kCode = selectedK.doubleValue ();

		upwindGapValue = new Double (upwindGap.getText ().trim ()).doubleValue ();

		// 3. Iterate on trees and write lines in .fgi
		
		TreeCollection tc = (TreeCollection) step.getScene();
		
		Iterator i = tc.getTrees().iterator();
		while (i.hasNext()){
			Tree t= (Tree) i.next();
			
			writeFgiTreeCreationMode (t);
		}

		try {
			fgi.flush ();
			fgi.close ();
		} catch (Exception e) {
			MessageDialog.print (this, Translator.swap ("ForestGalesTree.errorWhileClosingFgiTreeFile"));
			Log.println (Log.ERROR, "ForestGalesTree.createFgiTreeFile ()", "Error: "+e);
			throw new Exception ("error while closing fgi tree file, could not create fgi tree file: "+e.toString ());
		}
		StatusDispatcher.print (Translator.swap ("Shared.done"));
	}

	
	/**
	 * Visiting a step to create a line in .fgi file.
	 */
	public void writeFgiTreeCreationMode ( Tree tree) {
		
		ForestGalesFormat stand = (ForestGalesFormat) step.getScene ();
		
		StringBuffer b = new StringBuffer ();

		b.append (step.getCaption ());	// i.e. Stand_ID ex: "a.1"
		b.append (columnSeparator);
		
		b.append (tree.getId());
		b.append (columnSeparator);

		b.append (speciesCode);
		b.append (columnSeparator);

		b.append (soilCode);
		b.append (columnSeparator);

		b.append (cultivationCode);
		b.append (columnSeparator);

		b.append (drainageCode);
		b.append (columnSeparator);

		b.append (nf.format (stand.getHg ()));
		b.append (columnSeparator);

		b.append (nf.format (stand.getDg ()));
		b.append (columnSeparator);

		b.append (nf.format (stand.getSpacing ()));
		b.append (columnSeparator);


/*		b.append (nf.format (stand.getUpwindGap ()));
		b.append (columnSeparator);*/

		b.append (nf.format (upwindGapValue));
		b.append (columnSeparator);


		b.append (nf.format (aCode));
		b.append (columnSeparator);

		b.append (nf.format (kCode));
		b.append (columnSeparator);

		b.append (nf.format (tree.getHeight ()));
		b.append (columnSeparator);

		b.append (nf.format (tree.getDbh ()));

		try {
			fgi.write (b.toString ());
			fgi.newLine ();
		} catch (Exception e) {
			MessageDialog.print (this, Translator.swap ("ForestGalesTree.errorWhileWritingFgiTreeFile"));
			Log.println (Log.ERROR, "ForestGales.visitFgiTreeCreationMode ()", "Error: "+e);
			return;
		}
	}

	/**
	 * Action on fgiEdit button
	 */
	public void fgiTreeEditAction () {
		try {
			createFgiTreeFile ();
		} catch (Exception e) {
			return;
		}
		new Launcher (Settings.getProperty ("capsis.default.editor", "")
				+" "+fgiTreeFileName).execute ();
	}

	/**
	 *
	 */
	public void launchForestGales () {
		try {
			createFgiTreeFile ();
		} catch (Exception e) {
			return;
		}
		String fgCommand = PathManager.getInstallDir()
				+File.separator
				+"fg"
				+File.separator
				+"fgtree.exe"		// Give this name to Juan SUAREZ
				+" "
				+fgiTreeFileName;
		new Launcher (fgCommand).execute ();	// at the end of the command, execute () will be called
	}

	/**
	 * Action on fgoEdit button
	 */
	public void fgoTreeEditAction () {
		new Launcher (Settings.getProperty ("capsis.default.editor", "")
				+" "+fgoTreeFileName).execute ();
	}

	/**
	 * Triggered when fg process is over : get the result : fgo file
	 */
	public int execute () {
		System.out.println ("ForestGalesTree.execute () -> process .fgo tree file");
		fgoTreeEdit.setEnabled (true);
		try {
			fgoTreeTartinateAction ();
		} catch (Exception e) {
			MessageDialog.print (this, Translator.swap ("ForestGalesTree.errorWhileProcessingfgoTreeFile"));
			Log.println (Log.ERROR, "ForestGalesTree.execute ()", "Error: ",e);
			return 1;
		}
		return 0;
	}

	/**
	 * Action on fgoTartinate button
	 */
	public void fgoTreeTartinateAction () throws Exception {
		StatusDispatcher.print (Translator.swap ("ForestGalesTree.readingFgoTreeFile"));

	
		String line = null;

		// 1. Open .fgo tree file for input
		fgo = new BufferedReader (new FileReader (fgoTreeFileName));
//		line = fgo.readLine ();	// jump header2

		TreeCollection tc = (TreeCollection) step.getScene();
		
		// 2. Create records from lines & memo then in map
		while ((line = fgo.readLine ()) != null) {
			System.out.println ("line="+line);
			FgoTreeRecord r = new FgoTreeRecord (line);
			int treeId = r.treeId;
			ForestGalesTreeFormat tree = (ForestGalesTreeFormat) tc.getTree(treeId);
			

		System.out.println ("fgoTreeRecord="+r);

		}

		// 3. Close file
		fgo.close ();

		StatusDispatcher.print (Translator.swap ("ForestGalesTree.tartinating"));

		StatusDispatcher.print (Translator.swap ("Shared.done"));
	}

	/**
	 * Visiting a step to complete it according to a .fgo file record.
	 */
	public void updateFgoTree (ForestGalesTreeFormat tree, FgoTreeRecord r) {
		
		
		double rp1,rp2;

		tree.setBreakageReturnPeriod (r.breakageProbability);			//to calculate
		tree.setBreakageProbability (r.breakageProbability);			//read
		tree.setOverturningProbability (r.overturningProbability);		//read
		tree.setOverturningReturnPeriod (r.overturningProbability);	//to calculate
		tree.setBreakageCWindSpeed (r.breakageCWindSpeed);				//read
		tree.setOverturningCWindSpeed (r.overturningCWindSpeed);		//read
		
		
		if (tree.getBreakageReturnPeriod () !=0 ) {
		rp1 = 1/ tree.getBreakageReturnPeriod ();
		}
		else  rp1 = 200d;
		tree.setBreakageReturnPeriod (rp1);
		
		if (tree.getOverturningReturnPeriod () !=0 ) {
		rp2 = 1/ tree.getOverturningReturnPeriod ();
		}
		else  rp2 = 200d;
		tree.setOverturningReturnPeriod (rp2);

		double bcws = tree.getBreakageCWindSpeed () / 1000 * 3600;			///  in km.h-1
		tree.setBreakageCWindSpeed (bcws);
		double ocws = tree.getOverturningCWindSpeed () / 1000 * 3600;
		tree.setOverturningCWindSpeed (ocws);		

	}

	/**
	 * From ActionListener interface.
	 */
	public void actionPerformed (ActionEvent evt) {
		if (evt.getSource ().equals (close)) {
			if (Question.ask (MainFrame.getInstance (),
					Translator.swap ("ForestGalesTree.confirm"), Translator.swap ("ForestGalesTree.confirmClose"))) {
				dispose ();
			}

		} else if (evt.getSource ().equals (launchFG)) {
			launchForestGales ();

		} else if (evt.getSource ().equals (fgiTreeEdit)) {
			fgiTreeEditAction ();

		} else if (evt.getSource ().equals (fgoTreeEdit)) {
			fgoTreeEditAction ();

		} else if (evt.getSource ().equals (trigger)) {
			execute ();

		} else if (evt.getSource ().equals (help)) {
			Helper.helpFor (this);
		}

	}

	/**
	 * Called on Escape. Redefinition of method in AmapDialog : ask for user confirmation.
	 */
	protected void escapePressed () {
		if (Question.ask (MainFrame.getInstance (),
				Translator.swap ("ForestGalesTree.confirm"), Translator.swap ("ForestGalesTree.confirmClose"))) {
			dispose ();
		}
	}

	/**
	 * User interface defintion
	 */
	private void createUI () {
		JPanel mainPanel = new JPanel ();
		mainPanel.setLayout (new BorderLayout ());

//		JTabbedPane masterPane = new JTabbedPane ();

		// 1. First tab : prepareFgi
		ColumnPanel part1 = new ColumnPanel ();

		LinePanel l0 = new LinePanel ();
		standId = new JTextField (step.getProject ().getName ());
		standId.setEditable (false);
		l0.add (new JWidthLabel (Translator.swap ("ForestGales.standId")+" :", 120));
		l0.add (standId);
		l0.addStrut0 ();
		
		//LinePanel l00 = new LinePanel ();
		//treeNumber = new JTextField (step.getStand ().getNha ());		///TO CHANGE ??
		//treeNumber.setEditable (false);
		//l00.add (new JWidthLabel (Translator.swap ("ForestGalesTree.treeNumber")+" :", 120));
		//l00.add (treeNumber);
		//l00.addStrut0 ();

		LinePanel l1 = new LinePanel ();
		species = new JComboBox (new Vector (speciesCodes.keySet ()));
		species.addItemListener (this);
		l1.add (new JWidthLabel (Translator.swap ("ForestGales.species")+" :", 120));
		l1.add (species);
		l1.addStrut0 ();

		LinePanel l2 = new LinePanel ();
		soil = new JComboBox (new Vector (soilCodes.keySet ()));
		soil.addItemListener (this);
		l2.add (new JWidthLabel (Translator.swap ("ForestGales.soil")+" :", 120));
		l2.add (soil);
		l2.addStrut0 ();

		LinePanel l3 = new LinePanel ();
		cultivation = new JComboBox (new Vector (cultivationCodes.keySet ()));
		cultivation.addItemListener (this);
		l3.add (new JWidthLabel (Translator.swap ("ForestGales.cultivation")+" :", 120));
		l3.add (cultivation);
		l3.addStrut0 ();

		LinePanel l4 = new LinePanel ();
		drainage = new JComboBox (new Vector (drainageCodes.keySet ()));
		drainage.addItemListener (this);
		l4.add (new JWidthLabel (Translator.swap ("ForestGales.drainage")+" :", 120));
		l4.add (drainage);
		l4.addStrut0 ();

		LinePanel l20 = new LinePanel ();
		aCoefficient = new JComboBox (new Vector (aCodes));
		l20.add (new JWidthLabel (Translator.swap ("ForestGales.aCoefficient")+" :", 120));
		l20.add (aCoefficient);
		l20.addStrut0 ();

		LinePanel l21 = new LinePanel ();
		kCoefficient = new JComboBox (new Vector (kCodes));
		l21.add (new JWidthLabel (Translator.swap ("ForestGales.kCoefficient")+" :", 120));
		l21.add (kCoefficient);
		l21.addStrut0 ();

		LinePanel l30 = new LinePanel ();
		upwindGap = new JTextField ("0");	// default value = 0
		l30.add (new JWidthLabel (Translator.swap ("ForestGales.upwindGap")+" :", 120));
		l30.add (upwindGap);
		l30.addStrut0 ();


		// .fgi file name input : browse
/*		LinePanel l7 = new LinePanel ();
		l7.add (new JWidthLabel (Translator.swap ("ForestGales.fgiFileName")+" :", 120));
		fgiFileName = new JTextField (15);
		fgiBrowse = new JButton (Translator.swap ("ForestGales.browse"));
		fgiBrowse.addActionListener (this);
		l7.add (fgiFileName);
		l7.add (fgiBrowse);
		l7.addStrut0 ();*/

		LinePanel l8 = new LinePanel ();
		fgiTreeEdit = new JButton (Translator.swap ("ForestGalesTree.fgiTreeEdit"));
		fgiTreeEdit.addActionListener (this);
		fgoTreeEdit = new JButton (Translator.swap ("ForestGalesTree.fgoTreeEdit"));
		fgoTreeEdit.addActionListener (this);
		fgoTreeEdit.setEnabled (false);
		l8.add (new JWidthLabel (120));
		l8.add (fgiTreeEdit);
		l8.add (fgoTreeEdit);
	//	l8.addStrut0 ();
		l8.addGlue ();

		LinePanel l9 = new LinePanel ();
		launchFG = new JButton (Translator.swap ("ForestGalesTree.launchFG"));
		launchFG.addActionListener (this);
		l9.add (new JWidthLabel (120));
		l9.add (launchFG);
	//	l9.addStrut0 ();
		l9.addGlue ();

		LinePanel l31 = new LinePanel ();
		trigger = new JButton (Translator.swap ("Interpret"));
		trigger.addActionListener (this);
		l31.add (new JWidthLabel (120));
		l31.add (trigger);
	//	l31.addStrut0 ();
		l31.addGlue ();




		// propose last user choice directly
		species.setSelectedItem (Settings.getProperty ("forest.gales.selected.species", ""));
		soil.setSelectedItem (Settings.getProperty ("forest.gales.selected.soil", ""));
		cultivation.setSelectedItem (Settings.getProperty ("forest.gales.selected.cultivation", ""));
		drainage.setSelectedItem (Settings.getProperty ("forest.gales.selected.drainage", ""));
//		try {dams.setSelectedItem (new Integer (Settings.getProperty ("forest.gales.selected.dams")));} catch (Exception e, null) {}
		aCoefficient.setSelectedItem (new Double (Settings.getProperty ("forest.gales.selected.a", -1)));
		kCoefficient.setSelectedItem (new Double (Settings.getProperty ("forest.gales.selected.k", -1)));

		part1.add (l0);
		//part1.add (l00);
		part1.add (l1);
		part1.add (l2);
		part1.add (l3);
		part1.add (l4);

		part1.add (l20);
		part1.add (l21);
		part1.add (l30);

		part1.add (l8);
		part1.add (l9);
		part1.add (l31);

		part1.addGlue ();

		// 4. Control panel
		JPanel pControl = new JPanel (new FlowLayout (FlowLayout.RIGHT));
		close = new JButton (Translator.swap ("Shared.close"));
		close.addActionListener (this);
		help = new JButton (Translator.swap ("Shared.help"));
		help.addActionListener (this);
		pControl.add (close);
		pControl.add (help);
		// set close as default (see AmapDialog)
		close.setDefaultCapable (true);
		getRootPane ().setDefaultButton (close);

		// layout parts
		getContentPane ().add (part1, BorderLayout.CENTER);
		getContentPane ().add (pControl, BorderLayout.SOUTH);

	}


}


