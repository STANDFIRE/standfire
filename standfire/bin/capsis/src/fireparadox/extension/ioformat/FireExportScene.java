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

package fireparadox.extension.ioformat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

import jeeb.lib.util.Alert;
import jeeb.lib.util.Log;
import jeeb.lib.util.Question;
import jeeb.lib.util.RecordSet;
import jeeb.lib.util.Translator;
import capsis.defaulttype.Tree;
import capsis.defaulttype.TreeList;
import capsis.gui.MainFrame;
import capsis.kernel.Engine;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.PathManager;
import capsis.kernel.Step;
import capsis.lib.fire.fuelitem.FiPlant;
import capsis.util.GTreeIdComparator;
import capsis.util.StandRecordSet;
import fireparadox.model.FmPlot;
import fireparadox.model.FmStand;

/**
 * FireExportScene is used to export a scene to an import format file for FireParadox
 *
 * @author Ph. Dreyfus - september 2008
 */
public class FireExportScene extends StandRecordSet {

	static {
		Translator.addBundle("capsis.extension.ioformat.FireExportScene");
	}

	private File tempExportFile;

	// Generic keyword record is described in superclass: key = value


	

	/**
	 * Extension dynamic compatibility mechanism.
	 * This matchwith method checks if the extension can deal (i.e. is compatible) with the referent.
	 */
	static public boolean matchWith (Object referent) {
		try {
			if (!(referent instanceof GModel)) {return false;}
			GModel m = (GModel) referent;
			GScene s = ((Step) m.getProject ().getRoot ()).getScene ();
			if (!(s instanceof TreeList)) {return false;}
			TreeList tcs = (TreeList) s;
			Tree t = tcs.getTrees ().iterator ().next ();
			if (!(t instanceof FiPlant)) {return false;}

		} catch (Exception e) {
			Log.println (Log.ERROR, "FireExportScene.matchWith ()", "Error in matchWith () (returned false)", e);
			return false;
		}
		return true;
	}

	//
	// RecordSet -> File
	// is described in superclass (save (fileName)).
	//

	/**
	 * Phantom constructor.
	 * Only to ask for extension properties (authorName, version...).
	 */
	public FireExportScene () {}

	/**
	 * File -> RecordSet
	 * is delegated to superclass.
	 */
	//public FireExportScene (String fileName) throws Exception {super (fileName);}
	public FireExportScene (String fileName) throws Exception {createRecordSet (fileName);}	// for direct use for Import
	@Override
	public void createRecordSet (String fileName) throws Exception {super.createRecordSet (fileName);}

	/**
	 * FiStand -> RecordSet.
	 * Implementation here.
	 */
	@Override
	public void createRecordSet (GScene sc) throws Exception {
		FmStand stand = (FmStand) sc;
		super.createRecordSet (stand);		// deals with RecordSet's source

		TreeList std = stand;

		String tempExportFileName = PathManager.getDir("tmp");
		tempExportFileName += File.separator;
		tempExportFileName += stand.getStep().getProject().getName();
		tempExportFileName += ".FireExportScene";

		BufferedWriter out = null;
		try {
			out = new BufferedWriter (new FileWriter (tempExportFileName));
			tempExportFile = new File(tempExportFileName);
		} catch (Exception e) {
			throw new Exception ("File name "+tempExportFileName+" causes error : "+e.toString ());
		}

		out.write (RecordSet.commentMark+" Capsis "+Engine.getVersion ()+" geneRated file - "
				+new Date ().toString ());
		out.newLine ();
		out.write (source);
		out.newLine ();

		Step step = ((GScene) stand).getStep ();		// current step

		// 1. Entete
		// TO DO : nom du fichier d'origine
		out.write("#Scene file of a mixed plot ...");
		out.newLine ();
		out.newLine ();

		// 2. "Terrain"
		out.write("#Terrain");
		out.newLine ();
		out.write("#name	cellWidth(m)	altitude(m)	xMin	yMin	xMax	yMax");
		out.newLine ();
		double xmax = std.getOrigin ().x + std.getXSize ();
		double ymax = std.getOrigin ().y + std.getYSize ();
		FmPlot plot = (FmPlot) std.getPlot ();
		out.write(
		"Terrain0"
		+"\t"+plot.getCellWidth ()
		+"\t"+std.getOrigin ().z
		+"\t"+std.getOrigin ().x
		+"\t"+std.getOrigin ().y
		+"\t"+xmax
		+"\t"+ymax
		);
		out.newLine ();

		// 3. Polygons (optionnal)


		// 4. TreeRecords
		out.newLine ();
		out.write("#Trees");
		out.write("#fileId	speciesName	x	y	z	height	crownBaseHeight	crownDiameter crownDiameterHeight	openess");
			// ... faut des tabs
		out.newLine ();

		NumberFormat nf2 = NumberFormat.getInstance(Locale.ENGLISH);
		nf2.setMinimumFractionDigits(2);
		nf2.setMaximumFractionDigits(2);
		nf2.setGroupingUsed (false);

		Collection trees = std.getTrees ();
		Object[] vctI;
		vctI = trees.toArray ();
		Iterator ite1 = trees.iterator ();
		Arrays.sort (vctI, new GTreeIdComparator (true));  // sort in ascending order

		for (int i = 0; i < vctI.length; i++) {
			FiPlant t = (FiPlant) vctI [i];
			out.write(
			""+t.getId ()
			+"\t"+t.getSpecies ()
			+"\t"+nf2.format ( t.getX () )
			+"\t"+nf2.format ( t.getY () )
			+"\t"+nf2.format ( t.getZ () )
			+"\t"+nf2.format ( t.getHeight () )
			+"\t"+nf2.format ( t.getCrownBaseHeight () )
			+"\t"+nf2.format ( t.getCrownDiameter () )
			+"\t"+nf2.format ( t.getMaxDiameterHeight () )
		// +"\t"+t.isClosedEnvironment ()
			);
			out.newLine ();
		}

		/*for (Iterator i = std.getTrees ().iterator (); i.hasNext ();) {
			FiPlant t = (FiPlant) i.next ();
			out.write(
			""+t.getId ()
			+"\t"+t.getSpecies ()
			+"\t"+nf2.format ( t.getX () )
			+"\t"+nf2.format ( t.getY () )
			+"\t"+nf2.format ( t.getZ () )
			+"\t"+nf2.format ( t.getHeight () )
			+"\t"+nf2.format ( t.getCrownBaseHeight () )
			+"\t"+nf2.format ( t.getCrownDiameter () )
			+"\t"+nf2.format ( t.getCrownDiameterHeight () )
			+"\t"+t.getClosedEnvironment ()
			);
			out.newLine ();
		}*/

		try {out.close ();} catch (Exception e) {}	// no exception reported if trouble while closing
	}


	/**
	 * Overwrite save in RecordSet
	 * rename (move) temporary file to the file (including destination) chosen by user
	 */
	@Override
	public void save (String fileName) throws Exception {
		File f = new File (fileName); // une exception si probleme ?

		// fc - 8.10.2004 - in script mode, overwrite files without asking
		//
		if (!(Engine.getInstance ().getPilotName ().equals ("script"))
				&& f.exists ()) { // vrai si fichier existant
			if (Question.ask (MainFrame.getInstance (), Translator.swap ("FireExportScene.confirm"), fileName+" "+Translator.swap ("FireExportScene.fileExistsDoYouWantItToBeReplaced"))) { // ouvre un dialogue, pose la question et renvoit vrai ou faux
				f.delete(); // efface le fichier si n�cessaire
			}
		}

		try {
			//throw new Exception (); // to test Alert.print
			tempExportFile.renameTo(new File(fileName));
		} catch (Exception e) {
			Alert.print(Translator.swap ("FireExportScene.failedToRenameFile")+tempExportFile.toString()+Translator.swap ("FireExportScene.inToFile")+fileName);
		}
	}

	/**
	 * RecordSet -> FiStand
	 * Implementation here.
	 */
	@Override
	public GScene load (GModel model) throws Exception {
		return null;
	}



	////////////////////////////////////////////////// Extension stuff
	/**
	 * From Extension interface.
	 */
	public String getName () {return Translator.swap ("FireExportScene");}

	/**
	 * From Extension interface.
	 */
	public String getVersion () {return VERSION;}
	public static final String VERSION = "1.0";

	/**
	 * From Extension interface.
	 */
	public String getAuthor () {return "Ph. Dreyfus";}

	/**
	 * From Extension interface.
	 */
	public String getDescription () {return Translator.swap ("FireExportScene.description");}


	////////////////////////////////////////////////// IOFormat stuff
	public boolean isImport () {return false;}
	public boolean isExport () {return true;}




}
