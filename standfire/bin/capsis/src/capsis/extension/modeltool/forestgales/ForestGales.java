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

package capsis.extension.modeltool.forestgales;

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
import java.util.HashMap;
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
import jeeb.lib.util.Visitable;
import capsis.commongui.util.Helper;
import capsis.extension.DialogModelTool;
import capsis.gui.MainFrame;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.PathManager;
import capsis.kernel.Project;
import capsis.kernel.Step;
import jeeb.lib.util.Visiter;
import capsis.util.methodprovider.ForestGalesFormat;

/**
 * A connection tool for pp3 model (maritime pine) and ForestGales.
 *
 * @author F. de Coligny - september 2002
 * Update C. Meredieu - december 2002
 */
public class ForestGales extends DialogModelTool implements ItemListener, ActionListener, Visiter, Command {

	static public final String AUTHOR="F. de Coligny";
	static public final String VERSION="1.0";
	
	public static final int FGI_CREATION_MODE = 0;
	public static final int FGO_PROCESSING_MODE = 1;

	// Both fgi and fgo file names are conventionnal
	// They are writen / read in capsis.tmp directory
	//
	public static final String fgiFileName = PathManager.getDir("tmp")+File.separator+"FG_Capsis.fgi";
	public static final String fgoFileName = PathManager.getDir("tmp")+File.separator+"FG_Capsis.fgo";

	private int visitMode;

	private Step step;
	private GModel model;

//	private String defaultInventoryPath;
	private NumberFormat nf;
	private NumberFormat nfdg;
	static public final String columnSeparator = " ";	// choose here space or tab
	private BufferedWriter fgi;
	private BufferedReader fgo;

	private String header1;
	private String header2;
	private Map fgoRecords;


	// Possible values
	private Map speciesCodes;
	private Map soilCodes;
	private Map cultivationCodes;
	private Map drainageCodes;
	private Collection aCodes;
	private Collection kCodes;
	private Map aMethodCodes;	//add cm 25/07/2006
//	private Collection damsCodes;

	// Chosen values
	private int speciesCode;
	private int soilCode;
	private int cultivationCode;
	private int drainageCode;
//	private int damsCode;
	private double aCode;
	private double kCode;
	private int aMethodCode;

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
	private JComboBox aMethod;		// from etc/forestgales.settings cm 26/07/06

	private JTextField upwindGap;

	private JButton trigger;

//	private JTextField fgiFileName;
//	private JButton fgiBrowse;
	private JButton launchFG;
	private JButton fgiEdit;

//	private JTextField fgoFileName;
//	private JButton fgoBrowse;
	private JButton fgoEdit;
//	private JButton fgoTartinate;

	private JButton close;	// after confirmation
	private JButton help;

	static {
		Translator.addBundle("capsis.extension.modeltool.forestgales.ForestGales");
	}

	/**
	 * Phantom constructor.
	 * Only to ask for extension properties (authorName, version...).
	 */
	public ForestGales () {		
		super();
	}
	
	@Override
	public void init(GModel m, Step s){


		try {

			step = s;
			model = m;

			setTitle (Translator.swap ("ForestGales")+" - "+step.getCaption ());

			/*defaultInventoryPath = Settings.getProperty ("capsis.inventory.path",  PathManager.getDir ("data"));
			if ((defaultInventoryPath == null) || defaultInventoryPath.equals ("")) {
				defaultInventoryPath = PathManager.getInstallDir();
			}*/
			nf = NumberFormat.getInstance(Locale.ENGLISH);
			nf.setMinimumFractionDigits(2);
			nf.setMaximumFractionDigits(2);
			nf.setGroupingUsed (false);

			nfdg = NumberFormat.getInstance(Locale.ENGLISH);
			nfdg.setMinimumFractionDigits(4);
			nfdg.setMaximumFractionDigits(4);
			nfdg.setGroupingUsed (false);

			prepareCodesMaps ();

			createUI ();

			pack ();	// sets the size
			setVisible (true);
			setModal (false);
			
		} catch (Exception exc) {
			Log.println (Log.ERROR, "ForestGales.c ()", exc.toString (), exc);
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

		} catch (Exception e) {
			Log.println (Log.ERROR, "ForestGales.matchWith ()", "Error in matchWith () (returned false)", e);
			return false;
		}

		return true;
	}



	/**
	 * Get data to feed combo boxes.
	 */
	private void prepareCodesMaps () {

		// 1. get codifications in etc/forestGalesCodes.properties
		ForestGalesCodes codes = null;
		try {
			try {	// try to get specific typologies for the module
				String modelPackageName = model.getIdCard ().getModelPackageName ();	// ex: "pp3"
				String fileName = PathManager.getDir("etc")
						+File.separator
						+modelPackageName
						+"_"
						+"forestGalesCodes.properties";
			System.out.println ("properties: "+fileName);
				codes = new ForestGalesCodes (fileName);
			}
			catch (Exception e) {	// default forest gales typologies
			System.out.println ("error, default path used for propertiers");
				codes = new ForestGalesCodes (PathManager.getDir("etc")
						+File.separator
						+"forestGalesCodes.properties");
			}

			speciesCodes = codes.getSpeciesMap ();
			soilCodes = codes.getSoilMap ();
			cultivationCodes = codes.getCultivationMap ();
			drainageCodes = codes.getDrainageMap ();
			aCodes = codes.getACollection ();
			kCodes = codes.getKCollection ();
			aMethodCodes = codes.getAMethodMap ();

			// Note : damsCodes is a Collection
		//	int damsMin = codes.getDamsMin ();
		//	int damsMax = codes.getDamsMax ();

		//	damsCodes = new TreeSet ();
		/*	for (int i = damsMin; i <= damsMax; i++) {
				damsCodes.add (new Integer (i));
			}*/

		} catch (Exception e) {
			MessageDialog.print (this, Translator.swap ("ForestGales.errorWhileReadingCodes"));
			Log.println (Log.ERROR, "ForestGales.prepareCodesMaps ()", "Error: "+e);
			return;
		}

		// 2. create file header2 (s)

		StringBuffer b = new StringBuffer ();
		b.append ("#FG1.3 input file");
		header1 = b.toString ();

		StringBuffer h = new StringBuffer ();
		h.append ("StandID");
		h.append (columnSeparator);
		h.append ("Age");					/// AGE AGE AGE AGE
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
		h.append ("Dg(m)");
		h.append (columnSeparator);
		h.append ("Spacing(m)");
		h.append (columnSeparator);
		h.append ("Upwindgap(m)");
		h.append (columnSeparator);
		h.append ("A");
		h.append (columnSeparator);
		h.append ("k");
		h.append (columnSeparator);			////METHOD METHOD
		h.append ("Method");

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

		} else if (evt.getSource().equals (aMethod)) {
			String key = ""+ (String) o;
			Settings.setProperty ("forest.gales.selected.method", key);


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
			MessageDialog.print (this, Translator.swap ("ForestGales.upwindGapShouldBeDouble"));
			Log.println (Log.ERROR, "ForestGales.correctInput ()", "upwind gap should be a double");
			return false;
		}
		double d = Check.doubleValue (upwindGap.getText ().trim ());
		if (d < 0) {
			MessageDialog.print (this, Translator.swap ("ForestGales.upwindGapShouldBeGreaterThanZero"));
			Log.println (Log.ERROR, "ForestGales.correctInput ()", "upwind gap should be greater than zero");
			return false;
		}

		return true;
	}

	/**
	 * Create the fgi file.
	 * Iterate on all steps in project (in preorder, why not)
	 * create a line per step in .fgi file
	 */
	private void createFgiFile () throws Exception {
		if (!correctInput ()) {
			Log.println (Log.ERROR, "ForestGales.createFgiFile ()", "incorrect data, can not create fgi file");
			throw new Exception ("incorrect values, could not create fgi file");
		}

		// 1. Open .fgi file for output
		try {
			fgi = new BufferedWriter (new FileWriter (fgiFileName));
			fgi.write (header1);
			fgi.newLine ();
			fgi.write (header2);
			fgi.newLine ();
		} catch (Exception e) {
			MessageDialog.print (this, Translator.swap ("ForestGales.errorWhileOpeningFgiFile"));
			Log.println (Log.ERROR, "ForestGales.createFgiFile ()", "Error: "+e);
			throw new Exception ("error while opening fgi file, could not create fgi file: "+e.toString ());
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

		String selectedAMethod = (String) aMethod.getSelectedItem ();
		aMethodCode = ((Integer) aMethodCodes.get (selectedAMethod)).intValue ();

		// 3. Iterate on steps and write lines in .fgi
		visitMode = FGI_CREATION_MODE;
		Project project = step.getProject ();
		Iterator i = project.preorderIterator ();
		while (i.hasNext ()) {
			Step s = (Step) i.next ();
			s.accept (this);		// Visitor pattern : s will call this.visit (s);
		}

		try {
			fgi.flush ();
			fgi.close ();
		} catch (Exception e) {
			MessageDialog.print (this, Translator.swap ("ForestGales.errorWhileClosingFgiFile"));
			Log.println (Log.ERROR, "ForestGales.createFgiFile ()", "Error: "+e);
			throw new Exception ("error while closing fgi file, could not create fgi file: "+e.toString ());
		}
		StatusDispatcher.print (Translator.swap ("Shared.done"));
	}

	/**
	 * Process a step : write a line in .fgi file
	 */
	public void visit (Visitable visitable) {
		if (visitMode == FGI_CREATION_MODE) {
			visitFgiCreationMode (visitable);
		} else {
			visitFgoProcessingMode (visitable);
		}
	}

	/**
	 * Visiting a step to create a line in .fgi file.
	 */
	public void visitFgiCreationMode (Visitable visitable) {
		Step step = (Step) visitable;
		ForestGalesFormat stand = (ForestGalesFormat) step.getScene ();

		StringBuffer b = new StringBuffer ();

		b.append (step.getCaption ());	// i.e. Stand_ID ex: "a.1"
		b.append (columnSeparator);

		b.append ((stand.getAge ()));		/// AGE AGE AGE AGE
		b.append (columnSeparator);

		b.append (speciesCode);
		b.append (columnSeparator);

		b.append (soilCode);
		b.append (columnSeparator);

		b.append (cultivationCode);
		b.append (columnSeparator);

		b.append (drainageCode);
		b.append (columnSeparator);

		//b.append (nf.format (stand.getHeight ()));
		//b.append (columnSeparator);

		//b.append (nf.format (stand.getDbh () / 100 ));
		//b.append (columnSeparator);

		b.append (nf.format (stand.getHg ()));
		b.append (columnSeparator);

		b.append (nfdg.format (stand.getDg () / 100 )); 	// value in meter
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

		b.append (aMethodCode);

		try {
			fgi.write (b.toString ());
			fgi.newLine ();
		} catch (Exception e) {
			MessageDialog.print (this, Translator.swap ("ForestGales.errorWhileWritingFgiFile"));
			Log.println (Log.ERROR, "ForestGales.visitFgiCreationMode ()", "Error: "+e);
			return;
		}
	}

	/**
	 * Action on fgiEdit button
	 */
	public void fgiEditAction () {
		try {
			createFgiFile ();
		} catch (Exception e) {
			return;
		}
		new Launcher (Settings.getProperty ("capsis.default.editor", "")
				+" "+fgiFileName).execute ();
	}

	/**
	 *
	 */
	public void launchForestGales () {
		try {
			createFgiFile ();
		} catch (Exception e) {
			return;
		}
		String fgCommand = PathManager.getInstallDir()
				+File.separator
				+"fg"
				+File.separator
				+"fg.exe"
				+" "
				+fgiFileName;
		new Launcher (fgCommand).execute ();	// at the end of the command, execute () will be called
	}

	/**
	 * Action on fgoEdit button
	 */
	public void fgoEditAction () {
		new Launcher (Settings.getProperty ("capsis.default.editor", "")
				+" "+fgoFileName).execute ();
	}

	/**
	 * Triggered when fg process is over : get the result : fgo file
	 */
	public int execute () {
		System.out.println ("ForestGales.execute () -> process .fgo file");
		fgoEdit.setEnabled (true);
		try {
			fgoTartinateAction ();
		} catch (Exception e) {
			MessageDialog.print (this, Translator.swap ("ForestGales.errorWhileProcessingfgoFile"));
			Log.println (Log.ERROR, "ForestGales.execute ()", "Error: ",e);
			return 1;
		}
		return 0;
	}

	/**
	 * Action on fgoTartinate button
	 */
	public void fgoTartinateAction () throws Exception {
		StatusDispatcher.print (Translator.swap ("ForestGales.readingFgoFile"));

		fgoRecords = new HashMap ();
		String line = null;

		// 1. Open .fgo file for input
		fgo = new BufferedReader (new FileReader (fgoFileName));
		line = fgo.readLine ();	// jump header2

		// 2. Create records from lines & memo then in map
		while ((line = fgo.readLine ()) != null) {
			System.out.println ("line="+line);
			FgoRecord r = new FgoRecord (line);
			fgoRecords.put (r.standId, r);

		System.out.println ("fgoRecord="+r);

		}

		// 3. Close file
		fgo.close ();

		StatusDispatcher.print (Translator.swap ("ForestGales.tartinating"));

		// 4. Iterate on steps and complete underlying stand with matching .fgo record info
		visitMode = FGO_PROCESSING_MODE;
		Project project = step.getProject ();
		Iterator i = project.preorderIterator ();
		while (i.hasNext ()) {
			Step s = (Step) i.next ();
			s.accept (this);		// Visitor pattern : s will call this.visit (s);
		}

		StatusDispatcher.print (Translator.swap ("Shared.done"));
	}

	/**
	 * Visiting a step to complete it according to a .fgo file record.
	 */
	public void visitFgoProcessingMode (Visitable visitable) {
		Step step = (Step) visitable;
		String key = step.getCaption ();	// i.e. Stand_ID ex: "a.1"
		FgoRecord r = (FgoRecord) fgoRecords.get (key);

		double rp1,rp2;

		ForestGalesFormat stand = (ForestGalesFormat) step.getScene ();

		stand.setBreakageReturnPeriod (r.breakageProbability);			//to calculate
		stand.setBreakageProbability (r.breakageProbability);			//read
		stand.setOverturningProbability (r.overturningProbability);		//read
		stand.setOverturningReturnPeriod (r.overturningProbability);	//to calculate
		stand.setBreakageCWindSpeed (r.breakageCWindSpeed);				//read
		stand.setOverturningCWindSpeed (r.overturningCWindSpeed);		//read


		if (stand.getBreakageReturnPeriod () !=0 ) {
		rp1 = 1/ stand.getBreakageReturnPeriod ();
		}
		else  rp1 = 200d;
		stand.setBreakageReturnPeriod (rp1);

		if (stand.getOverturningReturnPeriod () !=0 ) {
		rp2 = 1/ stand.getOverturningReturnPeriod ();
		}
		else  rp2 = 200d;
		stand.setOverturningReturnPeriod (rp2);

		double bcws = stand.getBreakageCWindSpeed () / 1000 * 3600;			///  in km.h-1
		stand.setBreakageCWindSpeed (bcws);
		double ocws = stand.getOverturningCWindSpeed () / 1000 * 3600;
		stand.setOverturningCWindSpeed (ocws);

	}

	/**
	 * From ActionListener interface.
	 */
	public void actionPerformed (ActionEvent evt) {
		if (evt.getSource ().equals (close)) {
			if (Question.ask (MainFrame.getInstance (),
					Translator.swap ("ForestGales.confirm"), Translator.swap ("ForestGales.confirmClose"))) {
				dispose ();
			}

		} else if (evt.getSource ().equals (launchFG)) {
			launchForestGales ();

		} else if (evt.getSource ().equals (fgiEdit)) {
			fgiEditAction ();

		} else if (evt.getSource ().equals (fgoEdit)) {
			fgoEditAction ();

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
				Translator.swap ("ForestGales.confirm"), Translator.swap ("ForestGales.confirmClose"))) {
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

		LinePanel l22 = new LinePanel ();
		aMethod = new JComboBox (new Vector (aMethodCodes.keySet ()));
		aMethod.addItemListener (this);
		l22.add (new JWidthLabel (Translator.swap ("ForestGales.aMethod")+" :", 120));
		l22.add (aMethod);
		l22.addStrut0 ();


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
		fgiEdit = new JButton (Translator.swap ("ForestGales.fgiEdit"));
		fgiEdit.addActionListener (this);
		fgoEdit = new JButton (Translator.swap ("ForestGales.fgoEdit"));
		fgoEdit.addActionListener (this);
		fgoEdit.setEnabled (false);
		l8.add (new JWidthLabel (120));
		l8.add (fgiEdit);
		l8.add (fgoEdit);
	//	l8.addStrut0 ();
		l8.addGlue ();

		LinePanel l9 = new LinePanel ();
		launchFG = new JButton (Translator.swap ("ForestGales.launchFG"));
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
//		dams.setSelectedItem (new Integer (Settings.getProperty ("forest.gales.selected.dams")));} catch (Exception e, null) {}
		aCoefficient.setSelectedItem (new Double (Settings.getProperty ("forest.gales.selected.a", -1)));
		kCoefficient.setSelectedItem (new Double (Settings.getProperty ("forest.gales.selected.k", -1)));
		aMethod.setSelectedItem (Settings.getProperty ("forest.gales.selected.method", ""));


		part1.add (l0);
		part1.add (l1);
		part1.add (l2);
		part1.add (l3);
		part1.add (l4);

		part1.add (l20);
		part1.add (l21);
		part1.add (l22);
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


