/** 
 * Capsis - Computer-Aided Projections of Strategies in Silviculture
 * 
 * Copyright (C) 1999-2010 INRA 
 * 
 * Authors: F. de Coligny, S. Dufour-Kowalski, 
 * 
 * This file is part of Capsis
 * Capsis is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * Capsis is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU lesser General Public License
 * along with Capsis.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package capsis.util;

import jeeb.lib.util.RecordSet;
import jeeb.lib.util.extensionmanager.ExtensionManager;
import capsis.app.CapsisExtensionManager;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.Project;
import capsis.kernel.Step;
import capsis.kernel.extensiontype.IFormat;
import capsis.kernel.extensiontype.OFormat;

/**
 * StandRecordSet is a superclass for user input/output stand inventories. 
 *
 * @author F. de Coligny - april 2001
 */
abstract public class StandRecordSet extends RecordSet implements IFormat, OFormat {
		
	/**
	 * Phantom constructor. 
	 * Only to ask for extension properties (authorName, version...).
	 */
	public StandRecordSet () { super(); }

		
	/** Initialisation */
	@Override
	public void initExport(GModel m, Step s) throws Exception {
		clear();
		createRecordSet (s.getScene ());
	}


	// Loads RecordSet from user GStand
	// Sets source field
	public void createRecordSet (GScene stand) throws Exception {
		Project scenario = stand.getStep ().getProject ();
		GModel model = scenario.getModel ();
		
		StringBuffer b = new StringBuffer ();
		b.append (RecordSet.commentMark);
		b.append (" Model\t: ");
		b.append (model.getPackageName ());
		b.append ("\n");
		b.append (RecordSet.commentMark);
		b.append (" Author\t: ");
		b.append (model.getIdCard ().getModelAuthor ());
		b.append ("\n");
		b.append (RecordSet.commentMark);
		b.append (" Project\t: ");
		b.append (scenario.getName ());
		b.append ("\n");
		b.append (RecordSet.commentMark);
		b.append (" Stand date\t: ");
		b.append (stand.getDate ());
		b.append ("\n");
		b.append (RecordSet.commentMark);
		b.append (" Initial source\t: ");
		b.append (stand.getSourceName ());
		b.append ("\n");
		b.append (RecordSet.commentMark);
		b.append (" File format : \n");
		b.append (RecordSet.commentMark);
		b.append ("    Name\t: ");
		b.append (ExtensionManager.getName (this.getClass().getName()));
		b.append ("\n");
		b.append (RecordSet.commentMark);
		b.append ("    Class name\t: ");
		b.append (getClassName ());
		b.append ("\n");
		b.append (RecordSet.commentMark);
		b.append ("    Version\t: ");
		b.append (ExtensionManager.getVersion (this.getClass().getName()));
		b.append ("\n");
		b.append (RecordSet.commentMark);
		b.append ("    Author\t: ");
		b.append (ExtensionManager.getAuthor (this.getClass().getName()));
		b.append ("\n");
				
		source = b.toString ();
	}
	
	/**
	 * To load a GStand from the record set.
	 * Reference to GModel allows to use some settings or values known
	 * by the model during load.
	 */
	abstract public GScene load (GModel model) throws Exception;
		
	////////////////////////////////////////////////// Extension stuff
	/**
	 * From Extension interface.
	 */
	public String getType () {
		return CapsisExtensionManager.IO_FORMAT;
	}

	/**
	 * From Extension interface.
	 */
	public String getClassName () {
		return this.getClass ().getName ();
	}

	/**
	 * From Extension interface.
	 * May be redefined by subclasses. Called after constructor
	 * at extension creation (ex : for view2D zoomAll ()).
	 */
	public void activate () {}


}


	
		

		

