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

package capsis.extension.memorizer;

import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import capsis.kernel.GModel;
import capsis.kernel.Project;
import capsis.kernel.Step;
import capsis.kernel.extensiontype.Memorizer;

/**
 * A Project Memorizer for complete memorization: all steps are memorized.
 * @author F. de Coligny - october 2002, october 2010
 */
public class DefaultMemorizer implements Memorizer {

	static {
		Translator.addBundle("capsis.extension.memorizer.DefaultMemorizer");
	}

	public static final String NAME = "DefaultMemorizer";
	public static final String VERSION = "1.1";
	public static final String AUTHOR = "F. de Coligny";
	public static final String DESCRIPTION = "DefaultMemorizer.description";

	/**
	 * Default constructor.
	 */
	public DefaultMemorizer() {
	}

	/**
	 * Extension dynamic compatibility mechanism. This method checks if the extension can deal (i.e.
	 * is compatible) with the referent.
	 */
	static public boolean matchWith(Object referent) {
		try {
			if (!(referent instanceof GModel)) { return false; }
		}
		catch (Exception e) {
			Log.println(Log.ERROR, "DefaultMemorizer.matchWith ()", "Exception in matchWith () method, returned false", e);
			return false;
		}
		return true;
	}

	/**
	 * Memorizer interface.
	 */
	@Override
	public void reinit() {
	};

	/**
	 * Memorizer interface. Default memorization strategy: memorize every step.
	 */
	@Override
	public void memorize(Project s, Step fatherStep, Step newStep) {
		s.tieStep(fatherStep, newStep);
	}

	/**
	 * Memorizer interface.
	 */
	@Override
	public String getCaption() {
		return Translator.swap("DefaultMemorizer");
	}

	/**
	 * Extension interface. Optional initialization processing. Called after constructor.
	 */
	@Override
	public void activate() {
	} // unused

	@Override
	public String toString() {
		return "DefaultMemorizer";
	}

	//------------------ PaleoExtension interface below

	/** Return extension type : STAND_VIEWER, DATA_EXTRACTOR... */
	//	@Override
	//	public String getType () {return CapsisExtensionManager.MEMORIZER;}
	/** Return getClass ().getName () : complete class name including package name. */
	//	@Override
	//	public String getClassName () {return getClass ().getName ();}
	/** Return a Translated name, usable in GUI components. */
	//	@Override
	//	public String getName () {return Translator.swap ("DefaultMemorizer");}
	/** Return version. */
	//	@Override
	//	public String getVersion () {return VERSION;}
	//	private static final String VERSION = "1.1";
	/** Return author name. */
	//	@Override
	//	public String getAuthor () {return "F. de Coligny";}
	/** Return short description. */
	//	@Override
	//	public String getDescription () {return Translator.swap ("DefaultMemorizer.description");}
}
