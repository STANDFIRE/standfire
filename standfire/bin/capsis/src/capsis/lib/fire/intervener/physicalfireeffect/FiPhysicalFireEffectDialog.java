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

package capsis.lib.fire.intervener.physicalfireeffect;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JTextField;

import jeeb.lib.util.AmapDialog;
import jeeb.lib.util.Check;
import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.JWidthLabel;
import jeeb.lib.util.LinePanel;
import jeeb.lib.util.Log;
import jeeb.lib.util.MessageDialog;
import jeeb.lib.util.Settings;
import jeeb.lib.util.Translator;
import capsis.commongui.ProjectFileAccessory;
import capsis.commongui.util.Helper;
import capsis.lib.fire.FiStand;
import capsis.lib.fire.exporter.firetec.FiretecTreeLoaderWriter;
import capsis.lib.fire.intervener.fireeffect.cambiumdamagemodel.BovaAndDickinson;
import capsis.lib.fire.intervener.fireeffect.cambiumdamagemodel.CambiumDamageModel;
import capsis.lib.fire.intervener.fireeffect.cambiumdamagemodel.NoCambiumDamageComputation;
import capsis.lib.fire.intervener.fireeffect.cambiumdamagemodel.PetersonAndRyanCambium;
import capsis.lib.fire.intervener.fireeffect.crowndamagemodel.BioPhysicalCrown;
import capsis.lib.fire.intervener.fireeffect.crowndamagemodel.CrownDamageModel;
import capsis.lib.fire.intervener.fireeffect.crowndamagemodel.FinneyAndMartin;
import capsis.lib.fire.intervener.fireeffect.crowndamagemodel.MichaletzAndJohnsonCrown;
import capsis.lib.fire.intervener.fireeffect.crowndamagemodel.NoCrownDamageComputation;
import capsis.lib.fire.intervener.fireeffect.crowndamagemodel.SavelandAndNeuenschwander;
import capsis.lib.fire.intervener.fireeffect.crowndamagemodel.VanWagner;
import capsis.lib.fire.intervener.fireeffect.mortalitymodel.GenericCVSDBH;
import capsis.lib.fire.intervener.fireeffect.mortalitymodel.MichaletzAndJohnsonMortality;
import capsis.lib.fire.intervener.fireeffect.mortalitymodel.MortalityModel;
import capsis.lib.fire.intervener.fireeffect.mortalitymodel.NoMortalityComputation;
import capsis.lib.fire.intervener.fireeffect.mortalitymodel.PetersonAndRyanMortality;
import capsis.lib.fire.intervener.fireeffect.mortalitymodel.SpeciesDependentCVSBLCDBH;
import capsis.lib.fire.intervener.fireeffect.mortalitymodel.SpeciesDependentCVSDBH;
import capsis.util.NativeBinaryInputStream;

/**
 * A dialog box to configure the FiPhysicalFireEffect intervener.
 * 
 * @author F. de Coligny, F. Pimont - september 2009
 */
public class FiPhysicalFireEffectDialog extends AmapDialog implements ActionListener {

	private FiStand stand;
	private JTextField treeFileName; // produced by fuel manager export for
										// firetec
	private JButton treeFileBrowse; // browse
	
	
	
	private JTextField firetecTimeStep; // s
	private JTextField fireDataName; // current fireDataName
	private JButton firstFireDataFileBrowse; // browse the first fd firetec file
												// in a directory

	public Map<Integer, Map> treeCrownMap;
	public int nx;
	public int ny;
	private JTextField nzInFireData;
	public int nz;
	
	public double dx;
	public double dy;
	public double sceneOriginX;
	public double sceneOriginY;
	public double sceneSizeX;
	public double sceneSizeY;

	public int firetecFrameNumber;
	public int firetecFramePeriod;
	public int firstFrameSuffix;
	public String firetecFileDir;
	public String firetecFilePrefix;
	
	
	
	private Vector crownDamageModels;
	private Vector cambiumDamageModels;
	private Vector mortalityModels;

	
	private JComboBox crownDamageCombo;
	private JComboBox cambiumDamageCombo;
	private JComboBox mortalityCombo;

	protected JButton ok;
	protected JButton cancel;
	protected JButton help;
	private JButton loadFiles;

	
	

	/**
	 * Constructor.
	 */
	public FiPhysicalFireEffectDialog(FiStand stand) {
		super ();
		this.stand = stand;
		initMaps ();

		createUI ();
		setTitle(Translator.swap("FiPhysicalFireEffectDialog"));

		setModal (true);

		// location is set by AmapDialog
		pack ();	// uses component's preferredSize
		show ();

	}

	private void initMaps () {
		// Crown damage models
		crownDamageModels = new Vector ();
		// crownDamageModels.add(new PetersonAndRyanCrown());
		crownDamageModels.add (new BioPhysicalCrown ());
		crownDamageModels.add (new VanWagner ());
		crownDamageModels.add (new SavelandAndNeuenschwander ());
		crownDamageModels.add (new FinneyAndMartin ());
		crownDamageModels.add(new MichaletzAndJohnsonCrown());
		crownDamageModels.add(new NoCrownDamageComputation());

		// Cambium damage models
		cambiumDamageModels = new Vector();
		cambiumDamageModels.add(new BovaAndDickinson());
		cambiumDamageModels.add(new PetersonAndRyanCambium());
		cambiumDamageModels.add(new NoCambiumDamageComputation());
		// Mortality models
		mortalityModels = new Vector();
		mortalityModels.add(new SpeciesDependentCVSBLCDBH());
		mortalityModels.add(new SpeciesDependentCVSDBH());
		mortalityModels.add(new GenericCVSDBH());
		mortalityModels.add(new PetersonAndRyanMortality());
		mortalityModels.add(new MichaletzAndJohnsonMortality());
		mortalityModels.add(new NoMortalityComputation());
	}

	public CrownDamageModel getCrownDamageModel () {
		return (CrownDamageModel) crownDamageCombo.getSelectedItem ();
	}

	public CambiumDamageModel getCambiumDamageModel() {
		return (CambiumDamageModel) cambiumDamageCombo.getSelectedItem();
	}

	public MortalityModel getMortalityModel() {
		return (MortalityModel) mortalityCombo.getSelectedItem();
	}
	
	
	/**
	 * read the treeCrownList from the firetecmatrix in export
	 * 
	 * @throws Exception
	 * 
	 */
	private void treeFileBrowseAction() {
		try {

			JFileChooser chooser = new JFileChooser(System
					.getProperty("treecrownlistfile.path"));
			ProjectFileAccessory acc = new ProjectFileAccessory();
			chooser.setAccessory(acc);
			chooser.addPropertyChangeListener(acc);
			int returnVal = chooser.showOpenDialog(this);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				Settings.setProperty("treecrownlistfile.path", chooser
						.getSelectedFile().toString());
				String fileName = chooser.getSelectedFile().toString();
				treeFileName.setText(fileName);
				// System.out.println("first fireDataFile :" + fileName);
				FiretecTreeLoaderWriter rd = new FiretecTreeLoaderWriter();
				treeCrownMap = rd.load(fileName, stand);
				nx = rd.nx;
				ny = rd.ny;
				// nzInFireData = rd.nz;
				dx = rd.dx;
				dy = rd.dy;
				
				sceneOriginX = rd.sceneOriginX;
				sceneOriginY = rd.sceneOriginY;
				sceneSizeX = rd.sceneSizeX;
				sceneSizeY = rd.sceneSizeY;
				firstFireDataFileBrowse.setEnabled(true);
			}

		} catch (Exception e) {
			Log.println(Log.ERROR,
					"FiPhysicalFireEffectDialog.treeFileBrowseBrowseAction ()",
					"Problem during file loading", e);
			MessageDialog
					.print(
							this,
							Translator
									.swap("FiPhysicalFireEffectDialog.treeCrownFileNotReadCorrectly"),
							e);
		}

	}
	

	private void checkFireDataFile(String fileName) throws Exception {

		// if (bigEndianTopo.isSelected()) {
		// format = NativeBinaryInputStream.SPARC;
		// }
		// if (littleEndianTopo.isSelected()) {
		String format = NativeBinaryInputStream.X86;
		// }
		FiretecFireDataReader reader = new FiretecFireDataReader(nx, ny, nz,
				format);
		reader.check(fileName);
	}

	/**
	 * browse the first fire damage file from firetec
	 * 
	 * @throws Exception
	 * 
	 */
	private void fireDataFileBrowseAction() {
		try {
			
			this.nz = Check.intValue(nzInFireData.getText().trim());
			System.out.println("nz=" + nz);
			if (nz <= 0) {
				MessageDialog
						.print(this, Translator
								.swap("FiPhysicalFireEffectDialog.numberOfCellConsideredShouldBePositive"));
				return;
			}
			Settings.setProperty("firetec.physical.fireeffect.dialog.last.nz",
					"" + nz);
			
			
			JFileChooser chooser = new JFileChooser(System
					.getProperty("firedatafile.path"));
			ProjectFileAccessory acc = new ProjectFileAccessory();
			chooser.setAccessory(acc);
			chooser.addPropertyChangeListener(acc);
			int returnVal = chooser.showOpenDialog(this);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				Settings.setProperty("firedatafile.path", chooser
						.getSelectedFile().toString());
				String fileName = chooser.getSelectedFile().toString();
				fireDataName.setText(fileName);
				System.out.println("first fireDataFile :" + fileName);
				checkFireDataFile(fileName);
				firetecFrameNumber = 1; // first frame read
				// TODO: this file name should be the prefix of filename
				// (before.)
				
				
				if (fileName.lastIndexOf(".") == -1) {
					throw new Exception(
							"FiPhysicalFireEffectDialog.fireDataFileBrowseAction ,  fileName does not contain any '.' "
									+ fileName);
				
				}
				
				firetecFileDir = new File (fileName).getParent();
				String fileNameTemp = new File (fileName).getName();
				firetecFilePrefix = fileNameTemp.substring(0, fileNameTemp.lastIndexOf(".")+1);
				//System.out.println("firetecFilePrefix "+firetecFileDir+"/"+firetecFilePrefix);
				firstFrameSuffix = getSuffix(new File (fireDataName.getText()));
				loadFiles.setEnabled(true);
				//loadFiles.setDefaultCapable(true);
				
			}

		} catch (Exception e) {
			Log.println(Log.ERROR,
					"FiPhysicalFireEffectDialog.fireDataFileBrowseAction ()",
					"Problem during file loading", e);
			MessageDialog
					.print(
							this,
							Translator
									.swap("FiPhysicalFireEffectDialog.firstFireDatafileNotReadCorrectly"),
							e);
		}

	}
	/**
	 * check entries and firetec
	 * 
	 * @throws Exception
	 * 
	 */
	private void loadFileAction() {

		if (!Check.isDouble(firetecTimeStep.getText().trim())) {
			MessageDialog
					.print(
							this,
							Translator
									.swap("FiPhysicalFireEffectDialog.timeStepMustBeANumberGreaterToZero"));
			return;
		}

		double _firetecTimeStep = Check.doubleValue(firetecTimeStep.getText()
				.trim());
		if (_firetecTimeStep < 0) {
			MessageDialog
					.print(this, Translator
							.swap("FiPhysicalFireEffectDialog.timeStepMustBeANumberGreaterToZero"));
			return;
		}
		Settings.setProperty(
				"firetec.physical.fireeffect.dialog.last.firetecTimeStep", ""
						+ _firetecTimeStep);

		try {
			// get the fileList with the appropriate prefix
			File dir = new File (fireDataName.getText()).getParentFile();
			File [] fileList = dir.listFiles(new FilenameFilter () {
				public boolean accept(File dir,String name) {
					//System.out.println("NAME="+name);
					return name.startsWith(firetecFilePrefix);
				}		
			} );
			
			if (fileList.length == 1) {
				MessageDialog
				.print(this, Translator
						.swap("FiPhysicalFireEffectDialog.onlyOneFileWithThisPrefixInDirectory"));
				return;
			}
			// sort the list based on index
			Arrays.sort(fileList, new Comparator<File>() {
				public int compare(File f1, File f2) {
					return (getSuffix(f1)-getSuffix(f2));
				}
			});
			firetecFrameNumber = 1;
			firetecFramePeriod = getSuffix(fileList[1])-getSuffix(fileList[0]);
			//firetecFramePeriod = getSuffix(fileList[0]);
			for (int i=1; i<fileList.length;i++) {
				//System.out.println("fireDataFile " + firetecFileDir+"/"+fileList[i].getName());
				
				checkFireDataFile(firetecFileDir+"/"+fileList[i].getName());
				if (!(firetecFramePeriod == getSuffix(fileList[i])-getSuffix(fileList[i-1]))) {
					MessageDialog
					.print(this, Translator
							.swap("FiPhysicalFireEffectDialog.firetecFramePeriodIsNotConstant"));
					return;
				}
				firetecFrameNumber++;
				System.out.println("fireDataFile " + firetecFileDir+"/"+fileList[i].getName()+" is OK");
				System.out.println("nz=" + nz);
			}
			MessageDialog.print(this, Translator
					.swap(firetecFrameNumber+" firetec files loaded"));
			ok.setEnabled(true);

		} catch (Exception e) {
			Log.println(Log.ERROR,
					"FiPhysicalFireEffectDialog.fireDataFileBrowseAction ()",
					"Problem during file loading", e);
			MessageDialog.print(this,Translator.swap("FiPhysicalFireEffectDialog.firstFireDatafileNotReadCorrectly"),e);
		}

	}
	
	/**
	 * return the suffix of a given firetecFile
	 * @param name
	 * @return
	 */
	private int getSuffix(File name) {
		String fn = name.getName();
		return new Integer(fn.substring(fn.lastIndexOf(".")+1,fn.length()));
	}
	/**
	 * Action on ok button.
	 */ 
	private void okAction () {

		// TODO :
		// faire des checks sur intensite et temps de residence au voisinage de
		// l'arbre
		
		
		// Checks...
		/*
		 * if (!Check.isDouble (fireIntensity.getText ().trim ())) {
		 * MessageDialog.print (this, Translator.swap
		 * ("FiPhysicalFireEffectDialog.intensityMustBeANumberGreaterOrEqualToZero"
		 * )); return; } if (!Check.isDouble(residenceTime.getText().trim())) {
		 * MessageDialog .print( this, Translator.swap(
		 * "FiPhysicalFireEffectDialog.residenceTimeMustBeANumberGreaterOrEqualToZero"
		 * )); return; } if
		 * (!Check.isDouble(ambiantTemperature.getText().trim())) { MessageDialog
		 * .print( this, Translator.swap(
		 * "FiPhysicalFireEffectDialog.ambiantTemperatureMustBeANumberGreaterOrEqualToZero"
		 * )); return; } if (!Check.isDouble(windVelocity.getText().trim())) {
		 * MessageDialog .print( this, Translator.swap(
		 * "FiPhysicalFireEffectDialog.windVelocityMustBeANumberGreaterOrEqualToZero"
		 * )); return; } double _fireIntensity =
		 * Check.doubleValue(fireIntensity.getText() .trim()); double
		 * _residenceTime = Check.doubleValue(residenceTime.getText() .trim());
		 * double _ambiantTemperature = Check.doubleValue(ambiantTemperature
		 * .getText().trim()); double _windVelocity =
		 * Check.doubleValue(windVelocity.getText().trim()); if (_fireIntensity
		 * < 0.0) { MessageDialog .promptError(Translator.swap(
		 * "FiPhysicalFireEffectDialog.intensityMustBeANumberGreaterOrEqualToZero"
		 * )); return; } Settings.setProperty(
		 * "firetec.empirical.fireeffect.dialog.last.fireintensity", "" +
		 * _fireIntensity); if (_residenceTime < 0.0) { MessageDialog
		 * .promptError(Translator.swap(
		 * "FiPhysicalFireEffectDialog.residenceTimeMustBeANumberGreaterOrEqualToZero"
		 * )); return; } Settings.setProperty(
		 * "firetec.empirical.fireeffect.dialog.last.residencetime", "" +
		 * _residenceTime); if (_ambiantTemperature < 0.0) { MessageDialog
		 * .promptError(Translator.swap(
		 * "FiPhysicalFireEffectDialog.ambiantTemperatureMustBeANumberGreaterOrEqualToZero"
		 * )); return; } Settings.setProperty(
		 * "firetec.empirical.fireeffect.dialog.last.ambianttemperature", "" +
		 * _ambiantTemperature); if (_windVelocity < 0.0) { MessageDialog
		 * .promptError(Translator.swap(
		 * "FiPhysicalFireEffectDialog.windVelocityMustBeANumberGreaterOrEqualToZero"
		 * )); return; } Settings.setProperty(
		 * "firetec.empirical.fireeffect.dialog.last.windvelocity", "" +
		 * _windVelocity);
		 */
		setValidDialog(true);
		 
	}

	/**
	 * Someone hit a button.
	 */
	public void actionPerformed (ActionEvent evt) {
		if (evt.getSource().equals(treeFileBrowse)) {
			treeFileBrowseAction();
		} else if (evt.getSource().equals(firstFireDataFileBrowse)) {
			fireDataFileBrowseAction();
		} else if (evt.getSource().equals(loadFiles)) {
			loadFileAction();
		} else if (evt.getSource().equals(ok)) {
			okAction ();
		} else if (evt.getSource ().equals (cancel)) {
			setValidDialog (false);
		} else if (evt.getSource ().equals (help)) {
			Helper.helpFor (this);
		}
	}

	public int getFiretecFramePeriod() {
		return firetecFramePeriod;
	}

	public double getFiretecTimeStep() {
		return Check.doubleValue(firetecTimeStep.getText().trim());
	}
	
	public int getFirstFrameSuffix() {
		return firstFrameSuffix;
	}
	
	/**
	 * Create the dialog box user interface.
	 */ 
	private void createUI () {

		ColumnPanel main = new ColumnPanel ();

		// 1. FireParamaters panel

		ColumnPanel p1 = new ColumnPanel(
				Translator.swap("FiPhysicalFireEffectDialog.fireParameters"));
		
		LinePanel l1 = new LinePanel();
		l1.add(new JWidthLabel(
				Translator.swap("FiPhysicalFireEffectDialog.treeFileName")
				+ " :", 160));
		treeFileName = new JTextField(15);
		l1.add(treeFileName);
		treeFileBrowse = new JButton(
				Translator.swap("FiPhysicalFireEffectDialog.treeFileBrowse"));
		l1.add(treeFileBrowse);
		treeFileName.setEnabled(false);
		treeFileBrowse.setEnabled(true);
		treeFileBrowse.addActionListener(this);
		l1.addStrut0();
		p1.add(l1);
		
		

		LinePanel l2a = new LinePanel();
		l2a
				.add(new JWidthLabel(
						Translator
								.swap("FiPhysicalFireEffectDialog.numberOfVerticalCellConsidered")
								+ " :", 160));
		nzInFireData = new JTextField(15);
		String v = Settings.getProperty(
				"firetec.physical.fireeffect.dialog.last.nz", "10");
		nzInFireData.setText(v);
		l2a.add(nzInFireData);
		l2a.addStrut0();
		p1.add(l2a);
		
		LinePanel l2 = new LinePanel();
		l2.add(new JWidthLabel(Translator
				.swap("FiPhysicalFireEffectDialog.fireDataName")
				+ " :", 160));
		fireDataName = new JTextField(15);
		l2.add(fireDataName);
		firstFireDataFileBrowse = new JButton(Translator
				.swap("FiPhysicalFireEffectDialog.fireDataFileBrowse"));
		l2.add(firstFireDataFileBrowse);
		fireDataName.setEnabled(false);
		firstFireDataFileBrowse.setEnabled(false);
		firstFireDataFileBrowse.addActionListener(this);
		l2.addStrut0();
		p1.add(l2);

		LinePanel l3 = new LinePanel();
		l3.add(new JWidthLabel(Translator
				.swap("FiPhysicalFireEffectDialog.firetecTimeStep")
				+ " :", 160));
		firetecTimeStep = new JTextField(15);
		v = Settings.getProperty(
				"firetec.physical.fireeffect.dialog.last.firetecTimeStep",
				"0.01");
		firetecTimeStep.setText (v);
		l3.add(firetecTimeStep);
		l3.addStrut0();
		p1.add(l3);
/*		LinePanel l4 = new LinePanel();
		l4.add(new JWidthLabel(Translator
				.swap("FiPhysicalFireEffectDialog.firetecFramePeriod")
				+ " :", 160));
		firetecFramePeriod = new JTextField(15);
		l4.add(firetecFramePeriod);
		l4.addStrut0();
		p1.add(l4);*/

		LinePanel controlPanel1 = new LinePanel();
		loadFiles = new JButton(Translator
				.swap("FiPhysicalFireEffectDialog.loadFiles"));
		controlPanel1.addGlue();
		controlPanel1.add(loadFiles);
		controlPanel1.addStrut0();
		loadFiles.addActionListener(this);
		p1.add(controlPanel1);
		
		loadFiles.setEnabled(false);
		p1.addStrut0();
		main.add(p1);
		getContentPane ().setLayout (new BorderLayout ());
		getContentPane ().add (main, BorderLayout.NORTH);

		
		
		
		
		// sets loadFile as default (see AmapDialog)
		loadFiles.setDefaultCapable(false);
		getRootPane().setDefaultButton(loadFiles);


		// 2. Models panel
		ColumnPanel p2 = new ColumnPanel(Translator
				.swap("FiPhysicalFireEffectDialog.models"));

		LinePanel l21 = new LinePanel();
		l21.add(new JWidthLabel(Translator
				.swap("FiPhysicalFireEffectDialog.crownDamageModel")
				+ " :", 120));
		crownDamageCombo = new JComboBox(crownDamageModels);
		l21.add(crownDamageCombo);
		l21.addStrut0();
		p2.add(l21);

		LinePanel l22 = new LinePanel();
		l22.add(new JWidthLabel(Translator
				.swap("FiPhysicalFireEffectDialog.cambiumDamageModel")
				+ " :", 120));
		cambiumDamageCombo = new JComboBox(cambiumDamageModels);
		l22.add(cambiumDamageCombo);
		l22.addStrut0();
		p2.add(l22);

		LinePanel l23 = new LinePanel();
		l23.add(new JWidthLabel(Translator
				.swap("FiPhysicalFireEffectDialog.mortalityModel")
				+ " :", 120));
		mortalityCombo = new JComboBox(mortalityModels);
		l23.add(mortalityCombo);
		l23.addStrut0();
		p2.add(l23);

		p2.addStrut0();
		main.add(p2);


		getContentPane ().setLayout (new BorderLayout ());
		getContentPane ().add (main, BorderLayout.NORTH);

		// 2. Control panel (ok cancel help);
		LinePanel controlPanel = new LinePanel();
		ok = new JButton (Translator.swap ("Shared.ok"));
		ok.setEnabled(false);
		cancel = new JButton (Translator.swap ("Shared.cancel"));
		help = new JButton (Translator.swap ("Shared.help"));
		controlPanel.addGlue ();
		controlPanel.add (ok);
		controlPanel.add (cancel);
		controlPanel.add (help);
		controlPanel.addStrut0 ();
		ok.addActionListener (this);
		
		cancel.addActionListener (this);
		help.addActionListener (this);
		getContentPane ().add (controlPanel, BorderLayout.SOUTH);

		// sets ok as default (see AmapDialog)
		//ok.setDefaultCapable(false);
		//getRootPane ().setDefaultButton (ok);

	}
	
	

}

