/* 
 * Capsis - Computer-Aided Projections of Strategies in Silviculture
 * 
 * Copyright (C) 2015 LERFoB AgroParisTech/INRA 
 * 
 * Authors: M. Fortin, 
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
package capsis.util.extendeddefaulttype;

import java.awt.Window;
import java.util.concurrent.CancellationException;

import repicea.gui.AutomatedHelper;
import repicea.gui.UIControlManager;
import repicea.io.tools.ImportFieldManagerDialog;
import repicea.io.tools.REpiceaRecordReader;
import capsis.commongui.util.Helper;


public abstract class ExtRecordReader<P extends ExtInitialParameters> extends REpiceaRecordReader {
	
	private boolean enableStrataSelection;
	private P initParms;

	protected ExtRecordReader(P parms) {
		super();
		setHelper();
		setPopUpWindowEnabled(true);
		enableStrataSelection = true;
		this.initParms = parms;
	}

	
	private void setHelper() {
		Object[] arguments = new Object[1];
		arguments[0] = "quebecmrnf.gui.ImportDialog";
		AutomatedHelper helper;
		try {
			helper = new AutomatedHelper(Helper.class.getMethod("helpFor", String.class), arguments);
			UIControlManager.setHelpMethod(ImportFieldManagerDialog.class, helper);
		} catch (Exception e) {
			System.out.println("Error while setting the helper for the import dialog!");
		}
	}

	protected P getSettings() {return initParms;}

	/**
	 * This method enables or disables the selection of a stratum in the input data set.
	 * @param bool a boolean
	 */
	public void setStrataSelectionEnabled(boolean bool) {
		enableStrataSelection =  bool;
	}
	
	@Override
	protected void makeASelection(Window owner) throws CancellationException {
		if (enableStrataSelection) {
			super.makeASelection(owner);
		}
	}

}
