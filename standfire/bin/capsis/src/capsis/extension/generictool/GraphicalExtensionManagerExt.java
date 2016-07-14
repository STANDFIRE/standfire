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
package capsis.extension.generictool;

import java.awt.Window;

import javax.swing.JFrame;

import jeeb.lib.util.LogBrowser;
import jeeb.lib.util.Translator;
import capsis.extensiontype.GenericTool;

/**
 * GraphicalExtensionManagerExt opens the GraphicalExtensionManager.
 * 
 * @author F. de Coligny - october 2011
 */
public class GraphicalExtensionManagerExt implements GenericTool {

	static {
		Translator.addBundle ("capsis.extension.generictool.GraphicalExtensionManager");
	}
	public static final String NAME = Translator.swap ("GraphicalExtensionManager");
	public static final String DESCRIPTION = Translator.swap ("GraphicalExtensionManager.description");
	public static final String AUTHOR = "F. de Coligny";
	public static final String VERSION = "1.4";

	/**
	 * Default constructor.
	 */
	public GraphicalExtensionManagerExt () {
	}

	@Override
	public void init (Window window) throws Exception {

		new GraphicalExtensionManager (window);

	}

	/**
	 * Extension compatibility system. Returns true if the extension can deal
	 * with the given object.
	 */
	static public boolean matchWith (Object referent) {
		return true;
	}

	@Override
	public void activate () {
	}

}
