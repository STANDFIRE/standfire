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

package capsis.commongui;

import java.io.File;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JScrollPane;

import jeeb.lib.util.FileAccessory;
import jeeb.lib.util.inspector.AmapInspectorPanel;
import capsis.kernel.ProjectIdCard;
import capsis.kernel.SessionIdCard;

/**	Accessory panel for JFileChoosers.
 *	Can give information about capsis.kernel Session, Project, 
 *	but also text and images files.
 *
 *	@author F. de Coligny - march 2001
 */
public class ProjectFileAccessory extends FileAccessory {

	/**	Constructor. 
	 * 	The superclass constructor does the following: 
	 * 	<pre>
	 * 	chooser.setAccessory(this);
	 *	chooser.addPropertyChangeListener(this);
	 *	</pre>
	 */
	public ProjectFileAccessory (JFileChooser chooser) {
		super (chooser);
	}
	
	// This constructor will be removed when we have time.
	// Use the  other constructor.
	@Deprecated
	public ProjectFileAccessory () {super (null);}

	@Override
	protected void initFactory() {
		 factory = new ProjectFileAccessory.CapsisAccessoryPanelFactory ();
	}	

	/**	A factory to create an accessory panel for the given file.
	 * 	This factory deals with capsis.kernel.Project and Session objects.
	 */
	static private class CapsisAccessoryPanelFactory extends AccessoryPanelFactory {

		/**	Constructor
		 */
		public CapsisAccessoryPanelFactory () {super();}

		@Override
		public JComponent getAccessoryPanel (File f) {
			// Session file
			try {
				SessionIdCard idc = new SessionIdCard (f);
				return new JScrollPane (new AmapInspectorPanel (idc.getCompactString (), true));	// true: sorted mode
			} catch (Exception e) {}

			// Project file
			try {
				ProjectIdCard idc = new ProjectIdCard (f);
				return new JScrollPane (new AmapInspectorPanel (idc.getCompactString (), true));	// true: sorted mode
			} catch (Exception e) {}

			return super.getAccessoryPanel(f);
		}

	}

}