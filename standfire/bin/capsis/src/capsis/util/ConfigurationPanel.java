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

import java.awt.Component;
import java.awt.Container;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JPanel;

import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.Log;

/**
 * ConfigurationPanel contains configuration information for one Configurable
 * object or several MultiConfigurable objects. The object must implement the
 * Configurable interface. If several, they must implement the MultiConfigurable
 * interface. The object (or one of them) gives a configuration panel (JPanel).
 * panel.execute () reconfigures the object(s) and all its (their) superclasses
 * (if needed).
 * 
 * @author F. de Coligny - september 2000
 */
abstract public class ConfigurationPanel extends JPanel {

	protected Set configurables;
	// protected ColumnPanel contentPane;
	private boolean isMultiConfPanel;

	/**
	 * Constructor
	 */
	public ConfigurationPanel() {
		super();
		configurables = new HashSet(); // no dupplicates
		isMultiConfPanel = false;

		// in subclasses : set layout and add configuration components
	}

	/**
	 * Constructor 2
	 */
	public ConfigurationPanel(Configurable obj) {
		this();
		configurables.add(obj);
	}

	/**
	 * Constructor 3
	 */
	public ConfigurationPanel(SharedConfigurable obj) {
		this();
		configurables.add(obj);
		isMultiConfPanel = true;
	}

	/**
	 * Return true if ConfigurationPanel is empty.
	 */
	public boolean isEmpty() {
		return getComponents().length == 0;
	}

	/**
	 * This method is called by containing dialog before calling execute (). If
	 * trouble, the method is responsible for user information
	 * ("Integer needed...").
	 */
	abstract public boolean checksAreOk();

	/**
	 * To add things in a ConfigurationPanel, use getContentPane ().add (...).
	 */
	// ~ public JPanel getContentPane () {return contentPane;}

	/**
	 * To add things in a ConfigurationPanel, use getContentPane ().add (...).
	 */
	public JPanel getContentPane() {
		return this;
	}

	/**
	 * Command to trigger the configuration, once panel modified. See
	 * checksAreOk ().
	 */
	public void execute() {
		if (configurables.isEmpty()) {
			Log.println(Log.ERROR, "ConfigurationPanel.execute ()", "No configurable found for panel: " + this);
		} else if (!isMultiConfPanel()) { // one single Configurable
			getConfigurable().configure(this);
			getConfigurable().postConfiguration();
		} else { // several MultiConfigurable with same panel
			for (Iterator i = configurables.iterator(); i.hasNext();) {
				SharedConfigurable c = (SharedConfigurable) i.next();
				c.sharedConfigure(this);
				c.postConfiguration();
			}
		}
	}

	/**
	 * Return true if the panel concerns several configurable
	 * (multi-configuration).
	 */
	public boolean isMultiConfPanel() {
		return isMultiConfPanel;
	}

	/**
	 * Return the configurable if single mode.
	 */
	public Configurable getConfigurable() {
		return (Configurable) configurables.iterator().next(); // first element
	}

	/**
	 * Return the multi configurable if multi mode.
	 */
	public SharedConfigurable getMultiConfigurable() {
		return (SharedConfigurable) configurables.iterator().next(); // first
																		// element
	}

	/**
	 * Add a MultiConfigurable to the list. They will all be reconfigured with
	 * the panel.
	 */
	public void addMultiConfigurable(SharedConfigurable c) {
		configurables.add(c);
	}

	/**
	 * Return the configurables Set. Contains one Configurable in single mode or
	 * several MultiConfigurable in multi mode.
	 */
	public Set getConfigurables() {
		return configurables;
	}

	/**
	 * Consider recursively all the panel's sub component and set them enabled
	 * (false).
	 */
	public void disablePanel() {
		disablePanel(this);
	}

	//
	// Recursive, triggered by disablePanel ().
	//
	private void disablePanel(Component c) {
		if (c instanceof Container) {
			Component[] components = ((Container) c).getComponents();
			for (int i = 0; i < components.length; i++) {
				disablePanel(components[i]);
			}
		}
		c.setEnabled(false);
	}

	public String toString() {
		StringBuffer b = new StringBuffer();
		b.append("ConfigurationPanel for : ");
		b.append(configurables.toString());
		return b.toString();
	}
}
