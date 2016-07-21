/**
 * Capsis - Computer-Aided Projections of Strategies in Silviculture
 * 
 * Copyright (C) 1999-2010 INRA
 * 
 * Authors: F. de Coligny, S. Dufour-Kowalski,
 * 
 * This file is part of Capsis Capsis is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 2.1 of the License, or (at your option) any later version.
 * 
 * Capsis is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU lesser General Public License along with Capsis. If
 * not, see <http://www.gnu.org/licenses/>.
 * 
 */

package capsis.extension;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import jeeb.lib.util.AmapTools;
import jeeb.lib.util.Check;
import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import jeeb.lib.util.annotation.Param;
import jeeb.lib.util.extensionmanager.ExtensionManager;
import capsis.app.CapsisExtensionManager;
import capsis.commongui.projectmanager.ColorManager;
import capsis.commongui.projectmanager.ProjectManager;
import capsis.commongui.projectmanager.StepButton;
import capsis.commongui.util.Tools;
import capsis.defaulttype.TreeList;
import capsis.extension.dataextractor.DEStandTable;
import capsis.extension.dataextractor.XYSeries;
import capsis.extensiontype.DataBlock;
import capsis.extensiontype.DataExtractor;
import capsis.gui.GrouperChooser;
import capsis.gui.GrouperChooserListener;
import capsis.gui.StatusChooser;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.Step;
import capsis.util.Configurable;
import capsis.util.ConfigurationPanel;
import capsis.util.Group;
import capsis.util.Grouper;
import capsis.util.GrouperManager;
import capsis.util.SharedConfigurable;
import capsis.util.Spiable;
import capsis.util.Spy;

/**
 * Superclass for all Capsis data extractors.
 * 
 * @author F. de Coligny - November 2000
 */
abstract public class AbstractDataExtractor implements DataExtractor, DataFormat, SharedConfigurable, Configurable,
		GrouperChooserListener, Comparable {

	public static final char COMMON = 'c'; // fc - 13.9.2004
	public static final char INDIVIDUAL = 'i';

	public static final String HECTARE = "HECTARE";

	public static final String STATUS = "STATUS"; // fc - 22.3.2004

	public static final String TREE_GROUP = "TREE_GROUP"; // tree group for all
															// the extractors in
															// the data block
	public static final String CELL_GROUP = "CELL_GROUP";
	public static final String I_TREE_GROUP = "I_TREE_GROUP"; // tree group for
																// one extractor
																// only
	public static final String I_CELL_GROUP = "I_CELL_GROUP";

	public static final String CLASS_WIDTH = "CLASS_WIDTH";
	public static final String TREE_IDS = "TREE_IDS";
	public static final String PERCENTAGE = "PERCENTAGE";

	public static final String INTERVAL_NUMBER = "INTERVAL_NUMBER";
	public static final String INTERVAL_SIZE = "INTERVAL_SIZE";
	public static final String IC_NUMBER_OF_SIMULATIONS = "IC_NUMBER_OF_SIMULATIONS";
	public static final String IC_RISK = "IC_RISK";
	public static final String IC_PRECISION = "IC_PRECISION";

	// This concerns Configurable: not memorized in Settings.
	// Only SharedConfigurable is memorized.
	@Param
	protected List<String> treeIds;

	// This concerns Configurable: not memorized in Settings.
	// Individual groupers management
	@Param
	public boolean i_grouperMode;
	@Param
	public String i_grouperName;
	@Param
	public boolean i_grouperNot; // fc - 21.4.2004

	// This concerns Configurable: not memorized in Settings.
	protected String[] statusSelection; // fc - 23.4.2004

	// Same DataRenderer for each instance of one given subclass
	protected String defaultDataRendererClassName;
	// REMOVED: this 'static' was a mistake, detected while adding
	// ExtractorGroups, DETimeG add an
	// DRHistograms renderer instead of a DRCurves fc-23.9.2013
	// static protected String defaultDataRendererClassName;

	// ~ protected java.util.List configProperties; // moved to DESettings - for
	// memorization - fc - 9.10.2003
	@Param
	protected DESettings settings;

	protected Step step;

	protected Color forcedColor; // fc - 29.4.2003 - optionnal (for PhD &
									// SChalon)
	protected int rank; // fc - 5.5.2003 - if several DE of same type on same
						// step (ex: != groups)

	// When false, indicates that current config has changed and that
	// extraction will have to be redone before next data renderer
	// paintComponent ();
	// NOTE: To be checked at the beginning of doExtraction ()
	protected boolean upToDate;

	private Collection<String> disabledProperties;

	// ex: NProvider, VProvider... -> will search pp3.NProvider,
	// mountain.VProvider... and document them in DEPropertyPage
	protected Collection<String> documentationKeys; // fc - 29.3.2005

	private Map<String, Map<String, String>> comboPropertyTranslationToValue;

	private DataBlock dataBlock; // fc-4.9.2012 to manage correctly optional
									// calibration data

	// fc-12.10.2015 REMOVED, replaced by XYSeries and Categories + DRGraph
	// family
	// // fc-21.9.2015 a data series is a list of points with a name and color
	// protected List<XYSeries> listOfDataSeries;

	@Override
	public void init(GModel m, Step s) throws Exception {
		try {
			this.step = s;

			upToDate = false;
			rank = 0;

			disabledProperties = new HashSet<String>(); // fast on contains ()
														// - fc - 6.2.2004
			documentationKeys = new HashSet<String>(); // fc - 29.3.2005

			// fc - 5.5.2009
			comboPropertyTranslationToValue = new HashMap<String, Map<String, String>>();

			retrieveSettings(); // was below - fc - 14.2.2002

			defaultDataRendererClassName = null;
			// ~ rescueColor = null;

			// Init Configurable Variables (not in settings)
			treeIds = new ArrayList<String>();

			// Retrieve default data renderer class name
			defaultDataRendererClassName = ExtensionManager.getStaticField(getClass(), "RENDERER");
			if (defaultDataRendererClassName == null) {
				defaultDataRendererClassName = CapsisExtensionManager.getInstance().getProperty(
						this.getClass().getName(), "defaultDataRenderer");
			}

			if (defaultDataRendererClassName == null) {
				defaultDataRendererClassName = "capsis.extension.datarenderer.drcurves.DRCurves";
			}

		} catch (Exception e) {
			Log.println(Log.ERROR, "DataExtractor.c ()", "Exception caught: ", e);
			throw e; // added this clause (M Fortin, CancelException, init now
						// throws Exception)
						// fc-26.3.2012
		}

	};

	/**
	 * Defines an order on the extractors for later sorting
	 */
	public int compareTo(Object o) {

		try {
			Step step1 = step;
			DataExtractor e2 = (DataExtractor) o;
			Step step2 = e2.getStep();

			// 1. compare project names
			String projectName1 = step1.getProject().getName();
			String projectName2 = step2.getProject().getName();
			if (projectName1.compareTo(projectName2) < 0) {
				return -1;
			} else if (projectName1.compareTo(projectName2) > 0) {
				return 1;
			} else {

				// 2. compare step dates
				int date1 = step1.getScene().getDate();
				int date2 = step2.getScene().getDate();
				if (date1 < date2) {
					return -1;
				} else if (date1 > date2) {
					return 1;
				} else {

					// 3. compare the scenario letter ('a', 'b'...), depends on
					// step width
					int w1 = step1.getWidth();
					int w2 = step2.getWidth();
					if (w1 < w2) {
						return -1;
					} else if (w1 > w2) {
						return 1;
					} else {

						// 4. compare '*' (means 'is an intervention result')
						boolean i1 = step1.getScene().isInterventionResult();
						boolean i2 = step2.getScene().isInterventionResult();
						if (!i1 && i2) {
							return -1;
						} else if (i1 && !i2) {
							return 1;
						} else {

							// 5. compare step depth
							int d1 = step1.getDepth();
							int d2 = step2.getDepth();
							if (d1 < d2) {
								return -1;
							} else if (d1 > d2) {
								return 1;
							} else {

								// fc-16.11.2011, did not work (order not
								// consistant for 2
								// extractors on same step)
								// // 6. compare extractor rank -> unique, no
								// // possible equality
								// int r1 = getRank ();
								// int r2 = getRank ();
								// if (r1 < r2) {
								// return -1;
								// } else if (r1 > r2) { return 1; } // should
								// be
								// // enough in
								// // all cases

								// 6. compare captions
								int r = this.getCaption().compareTo(e2.getCaption());
								if (r < 0 || r > 0) {
									return r;
								} else {

									// 7. compare extractors hashcodes
									// (fc-16.11.2011)
									int c1 = this.hashCode();
									int c2 = e2.hashCode();
									return c1 - c2;

								}
							}
						}

					}

				}
			}

		} catch (Exception e) {
			return -1; // error
		}
		// return -1; // error
	}

	/**
	 * Return true if the extractor can deal with only one individual at a time
	 * (ex: one single tree). Can be detected by config tools to enable single
	 * selection only.
	 */
	public boolean isSingleIndividual() {
		return false;
	} // fc - 28.9.2006

	/**
	 * If not null, type of group requested for individual configuration
	 */
	public String getIGrouperType() {
		return settings.i_grouperType;
	} // fc - 13.9.2004

	/**
	 * If not null, type of group requested for common configuration
	 */
	public String getCGrouperType() {
		return settings.c_grouperType;
	} // fc - 13.9.2004

	/**
	 * Grouper mode is true if one grouper was activated in config panel. fc -
	 * 23.3.2004 - common (memorized) and individual (forgotten) groupers are
	 * now separated.
	 */
	public boolean isGrouperMode() {
		return settings.c_grouperMode || i_grouperMode;
	}

	/**
	 * Grouper name is the one selected in the GrouperChooser combo box. fc -
	 * 23.3.2004 - common (memorized) and individual (forgotten) groupers are
	 * now separated.
	 */
	public String getGrouperName() {
		return (settings.c_grouperMode) ? settings.c_grouperName : i_grouperName;
	}

	/**
	 * Chosen grouper was in the common config panel. fc - 23.3.2004 - common
	 * (memorized) and individual (forgotten) groupers are now separated.
	 */
	public boolean isCommonGrouper() {
		return settings.c_grouperMode;
	}

	/**
	 * True if common grouper is in NOT mode (complementary).
	 */
	public boolean isCommonGrouperNot() {
		return settings.c_grouperNot;
	} // fc - 21.4.2004

	/**
	 * True if individual grouper is in NOT mode (complementary).
	 */
	public boolean isGrouperNot() {
		return i_grouperNot;
	} // fc - 21.4.2004

	/**
	 * Rank management. If several extractors of same type (ex DETimeN) are
	 * opened on same step, they have a different rank. Rank can be used to
	 * change data extractor color (darker, brighter). Default case : rank is
	 * set to 0. fc - 5.5.2003
	 */
	public void setRank(int rank) {
		this.rank = rank;
	}

	public int getRank() {
		return rank;
	}

	/**
	 * fc - 14.4.2003 - Added this for Connection with ObjectViewers (not tested
	 * yet) Sandrine Chalon - PhD
	 */
	public void setGrouperName(String grouperName) {
		try {
			// What about settings.useCommonGroup ??? - fc - 6.5.2003
			settings.i_grouperType = GrouperManager.getInstance().getGrouper(grouperName).getType(); // fc
																										// -
																										// 13.9.2004

			i_grouperName = grouperName;
			i_grouperMode = !grouperName.equals("");
			grouperChanged(grouperName);

		} catch (Exception e) {
			Log.println(Log.ERROR, "DataExtractor.setGrouperName ()", "Exception in extractor "
					+ this.getClass().getName(), e);
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// ///////////////////////////////////////////////////////////////// Config
	// Properties
	// //////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Used by subclasses to choose config properties.
	 */
	abstract public void setConfigProperties();

	/**
	 * Used by subclasses to add config properties.
	 */
	public void addConfigProperty(String property) {
		if (property.equals(TREE_GROUP)) { // fc - 13.9.2004 - short circuit
											// (see addGroupProperty ())
			addGroupProperty(Group.TREE, COMMON);
			return;
		}
		if (property.equals(I_TREE_GROUP)) { // fc - 13.9.2004
			addGroupProperty(Group.TREE, INDIVIDUAL);
			return;
		}
		if (property.equals(CELL_GROUP)) { // fc - 13.9.2004
			addGroupProperty(Group.CELL, COMMON);
			return;
		}
		if (property.equals(I_CELL_GROUP)) { // fc - 13.9.2004
			addGroupProperty(Group.CELL, INDIVIDUAL);
			return;
		}

		settings.configProperties.add(property);
	}

	/**
	 * Used in configuration panels to add needed components.
	 */
	@Override
	public boolean hasConfigProperty(String property) {
		return settings.configProperties.contains(property) || settings.booleanProperties.containsKey(property)
				|| settings.radioProperties.containsKey(property) || settings.intProperties.containsKey(property)
				|| settings.doubleProperties.containsKey(property) || settings.setProperties.containsKey(property)
				|| settings.comboProperties.containsKey(property) // fc -
																	// 14.10.2004
		;
	}

	// //////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////// New Config
	// Properties Framework
	// //////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Property enabling/disabling
	 */
	// fc - 6.2.2004
	public void setPropertyEnabled(String property, boolean enabled) {
		if (enabled) {
			disabledProperties.remove(property); // Should not throw exception
													// if not found
		} else {
			disabledProperties.add(property);
		}
	}

	public boolean isPropertyEnabled(String property) {
		return !disabledProperties.contains(property);
	}

	/**
	 * Used in subclasses to check boolean / radio properties values.
	 */
	public boolean isSet(String property) {
		try {
			if (settings.booleanProperties.containsKey(property) && isPropertyEnabled(property) // fc
																								// -
																								// 6.2.2004
					&& ((Boolean) settings.booleanProperties.get(property)).booleanValue()) {
				return true;
			}
			if (settings.radioProperties.containsKey(property) && isPropertyEnabled(property) // fc
																								// -
																								// 6.2.2004
					&& ((Boolean) settings.radioProperties.get(property)).booleanValue()) {
				return true;
			}
		} catch (Exception e) {
			return false;
		}
		return false;
	}

	/**
	 * Used by subclasses to add group properties. Type is one declared in the
	 * Group class (ex: TREE, CELL, FISH...) Target is either INDIVIDUAL or
	 * COMMON. INDIVIDUAL means that the group is for one single extractor and
	 * is set in the individual tab of DEMulticonfPanel. In this case, different
	 * extractors may be set on different groups. COMMON means that the group is
	 * used for all the extractors in the data block. If COMMON and INDIVIDUAL
	 * are added together, only COMMON will be considered if checked (grouper
	 * chooser must be checked).
	 */
	// fc - 13.9.2004 - group generalization - FISH...
	public void addGroupProperty(String type, char target) {
		// fc - 3.6.2008 - type is possibly null -> all types compatible with
		// the stand are possible
		if (type == null) {
			Collection possibleTypes = Group.getPossibleTypes(getStep().getScene());
			if (possibleTypes != null && !possibleTypes.isEmpty()) {
				type = (String) possibleTypes.iterator().next();
			} else {
				type = Group.UNKNOWN;
			}
		}
		if (target == INDIVIDUAL) {
			settings.i_grouperType = type;
		} else if (target == COMMON) {
			settings.c_grouperType = type;
		} else {
			Log.println(Log.ERROR, "DataExtractor.addGroupProperty ()",
					"unknown target (ignored), should be COMMON or INDIVIDUAL: " + target);
		}
	}

	/**
	 * Used by subclasses to add boolean properties (JCheckBox). User will be
	 * asked for a choice in some config panel.
	 */
	public void addBooleanProperty(String property) {
		addBooleanProperty(property, false);
	}

	public void addBooleanProperty(String property, boolean defaultValue) { // fc
																			// -
																			// 9.7.2003
																			// (defaultValue)
		if (!settings.booleanProperties.containsKey(property)) {
			settings.booleanProperties.put(property, new Boolean(defaultValue));
		}
	}

	public void removeBooleanProperty(String property) { // fc - 12.10.2004
		if (!settings.booleanProperties.containsKey(property)) {
			return;
		}
		settings.booleanProperties.remove(property);
	}

	/**
	 * Memorize a set of exclusive boolean properties (JRadioButton group). User
	 * will be prompted for a choice in some config panel.
	 */
	public void addRadioProperty(String[] tab) {
		for (int i = 0; i < tab.length; i++) {
			String property = tab[i];

			// if common prop and already known for this extractor, ignore
			if (!isIndividualProperty(property) && hasConfigProperty(property)) {
				continue;
			}

			boolean yep = i == 0 ? true : false;
			settings.radioProperties.put(property, new Boolean(yep));
		}
	}

	/**
	 * Provides input for an int value in some config panel (JTextField).
	 */
	public void addIntProperty(String property, int defaultValue) {
		if (!settings.intProperties.containsKey(property)) {
			settings.intProperties.put(property, new Integer(defaultValue));
		}
	}

	/**
	 * Can be used to retrieve the value chosen by user in config for this
	 * property.
	 */
	public int getIntProperty(String property) {
		if (!isPropertyEnabled(property)) {
			return 0;
		} // fc - 6.2.2004
		try {
			if (settings.intProperties.containsKey(property)) {
				return ((Integer) settings.intProperties.get(property)).intValue();
			}
		} catch (Exception e) {
			return 0;
		}
		return 0;
	}

	/**
	 * Provides input for a double value in some config panel (JTextField).
	 */
	public void addDoubleProperty(String property, double defaultValue) {
		if (!settings.doubleProperties.containsKey(property)) {
			settings.doubleProperties.put(property, new Double(defaultValue));
		}
	}

	/**
	 * Can be used to retrieve the value chosen by user in config for this
	 * property.
	 */
	public double getDoubleProperty(String property) {
		if (!isPropertyEnabled(property)) {
			return 0d;
		} // fc - 6.2.2004
		try {
			if (settings.doubleProperties.containsKey(property)) {
				return ((Double) settings.doubleProperties.get(property)).doubleValue();
			}
		} catch (Exception e) {
			return 0d;
		}
		return 0d;
	}

	/**
	 * Selection of some items inside a list of possible items. Items are
	 * Strings (ex: "1", "2",... or "Blue", "Red"...). selectedItems can be null
	 * or empty.
	 */
	public void addSetProperty(String property, String[] possibleItems, String[] selectedItems) {
		if (!settings.setProperties.containsKey(property)) {
			ItemSelector is = new ItemSelector(possibleItems, selectedItems);
			settings.setProperties.put(property, is);
		}
	}

	/**
	 * This must be used to update the possible values of the property when they
	 * can possibly change. Ex: if the property concerns treeIds, update it when
	 * step changes or when a tree grouper is used.
	 */
	public void updateSetProperty(String property, String[] possibleValues) {
		if (settings.setProperties.containsKey(property)) {
			ItemSelector is = (ItemSelector) settings.setProperties.get(property);
			is.setPossibleValues(possibleValues);
			// ~ is.clearSelectedValues (); // fc - 21.12.2004
		}
	}

	public void updateSetProperty(String property, Set possibleValues) {
		if (settings.setProperties.containsKey(property)) {
			ItemSelector is = (ItemSelector) settings.setProperties.get(property);
			is.possibleValues = possibleValues;
			// ~ is.clearSelectedValues (); // fc - 21.12.2004
		}
	}

	/**
	 * Can be used to retrieve the value chosen by user in config for this
	 * property.
	 */
	public Set getSetProperty(String property) {
		if (!isPropertyEnabled(property)) {
			return new TreeSet();
		} // fc -
			// 6.2.2004
		try {
			if (settings.setProperties.containsKey(property)) {
				return ((ItemSelector) settings.setProperties.get(property)).selectedValues;
			}
		} catch (Exception e) {
			return new TreeSet();
		}
		return new TreeSet();
	}

	/**
	 * Return the list of status selected by user (ex: "dead", "cut"). may
	 * return null if this extractor does not use STATUS property.
	 */
	public String[] getStatusSelection() {
		return statusSelection;
	}

	/**
	 * Provides input for the choice of a String out of a list of possibilities
	 * in a combo box in some config panel (JTextField). Convention : selected
	 * item is placed in first position.
	 */
	public void addComboProperty(String property, LinkedList values) {
		if (!settings.comboProperties.containsKey(property)) {
			settings.comboProperties.put(property, values);

			// When a translated value is selected, we will return the original
			// untranslated value
			// So we need a map "translated value" to "value"
			Map<String, String> map = new HashMap<String, String>();
			comboPropertyTranslationToValue.put(property, map);

			for (Iterator i = values.iterator(); i.hasNext();) {
				String v = (String) i.next();
				map.put(Translator.swap(v), v);
			}
		}
	}

	/**
	 * Remove combo property
	 */
	public void removeComboProperty(String property) {
		if (!settings.comboProperties.containsKey(property)) {
			return;
		}
		settings.comboProperties.remove(property);
	}

	/**
	 * Can be used to retrieve the value chosen by user in the combo list for
	 * this property. Convention : selected item is placed in first position.
	 */
	@Override
	public String getComboProperty(String property) {
		if (!isPropertyEnabled(property)) {
			return null;
		} // fc - 10.6.2004
		try {
			if (settings.comboProperties.containsKey(property)) {
				LinkedList values = (LinkedList) settings.comboProperties.get(property);
				// values contains translated string -> we return the original
				// not translated value
				// convention : selected item is placed in first position

				if (values == null || values.isEmpty()) {
					Log.println(Log.ERROR, "DataExtractor.getComboProperty ()", "Exception, comboProperty:" + property
							+ " empty values list: " + values + " returned empty string");
					return "";
				}

				String value = (String) values.iterator().next();
				if (value == null || value.equals("null")) {
					return "";
				}
				return value;
			}
		} catch (Exception e) {
			Log.println(Log.ERROR, "DataExtractor.getComboProperty ()", "Exception", e);
			return "";
		}
		return "";
	}

	/**
	 * There can be several DataExtractors in the DataBlock. In this case, they
	 * will be drawn on the same graphics. An individual property concerns only
	 * one extractor (ex: tree ids for several height evolution curves). A
	 * common property modifies all the extractors (ex: per hectare
	 * calculation). Properties are recognized to be individual if their name
	 * begins with "i-". Common properties are managed in a single config panel
	 * for all the extractors (MultiConfiguration). Individual properties are
	 * managed in one panel per extractor (Configuration). Reminder: some
	 * properties have a "_" inside their name. Possible name formats: name,
	 * i-name, group_suffix, i-group_suffix. fc - 25.7.2002
	 */
	public boolean isIndividualProperty(String property) {
		if (property.startsWith("i-")) {
			return true;
		}
		return false;
	}

	// //////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////
	// Extension
	// //////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Asks the extension manager for last version of settings for this
	 * extension type. redefinable by subclasses to get settings subtypes.
	 */
	protected void retrieveSettings() {

		settings = new DESettings();
		setConfigProperties();
		if (step.getProject().getModel() instanceof OverridableDataExtractorParameter) {
			((OverridableDataExtractorParameter) step.getProject().getModel()).setDefaultProperties(settings);
		}
		ExtensionManager.applySettings(this);

	}

	/**
	 * This prefix is built depending on current settings. ex: "+ 25 years /ha"
	 */
	protected String getNamePrefix() {
		String prefix = "";
		try {
			GrouperManager gm = GrouperManager.getInstance();
			if (isCommonGrouper() && isGrouperMode() && gm.getGrouperNames().contains(gm.removeNot(getGrouperName()))) {
				prefix += getGrouperName() + " - ";
			}

			if (settings.perHa) {
				prefix += "/ha - ";
			}

		} catch (Exception e) {
		} // if trouble, prefix is empty
		return prefix;
	}

	/**
	 * From Extension interface. May be redefined by subclasses. Called after
	 * constructor at extension creation (ex : for view2D zoomAll ()).
	 */
	public void activate() {
	}

	/**
	 * Changes current step.
	 */
	public void setStep(Step stp) {
		step = stp;
		upToDate = false;
	}

	public Step getStep() {
		return step;
	}

	public DataBlock getDataBlock() { // fc-26.9.2012
		return dataBlock;
	}

	// Update on toStep if required, depending on fromStep
	//
	@Override
	public boolean update(Step fromStep, Step toStep) throws Exception {
		// added throws Exception, fc-20.1.2014 trying to prevent an extractor
		// from blocking Capsis

		if (fromStep != null && fromStep.equals(step)) {
			setStep(toStep);

			// TO BE REMOVED, TESTING DEStandTable.createStandTable (step)
			if (getClass().getName().endsWith("DEStandTable")) {
				String[][] res = DEStandTable.createStandTable(toStep);

				System.out.println("AbstractDataExtractor TESTING DEStandTable.createStandTable ("
						+ toStep.getScene().getDate() + ")...");
				for (int i = 0; i < res.length; i++) {
					String line = "";
					for (int j = 0; j < res[0].length; j++) {
						line += res[i][j];
						if (j < res[0].length - 1) {
							line += "\t";
						}

					}
					System.out.println(line);
				}
				System.out.println("AbstractDataExtractor done.");

			}
			// TO BE REMOVED, TESTING DEStandTable.createStandTable (step)

			doExtraction();
			return true;
		}

		return false;
	}

	/**
	 * Apply selected status and grouper on stand, return a sub collection of
	 * individuals. The Collection contains individuals which type is known by
	 * the Group class.. This method should be called from doExtraction if
	 * needed.
	 */
	public Collection doFilter(GScene stand) { // fc -17.9.2004
		if (settings.c_grouperType == null) {
			Log.println(Log.ERROR, "DataExtractor.doFilter (stand)",
					"settings.c_grouperType == null, used TREE instead");
			settings.c_grouperType = Group.TREE;
		}
		return doFilter(stand, settings.c_grouperType);
	}

	/**
	 * Applies the current grouper on the given list. This is a more flexible
	 * version of doFilter(), can be applied on various tree lists.
	 * 
	 * @param input: a list of trees to be restricted by the current grouper
	 * @return
	 */
	// fc+bc-3.5.2016
	public Collection doFilter(List input) {

		if (!isGrouperMode()) {
			return input;
		} // no grouper selected

		GrouperManager gm = GrouperManager.getInstance();
		Grouper g = gm.getGrouper(getGrouperName()); // if group not found,
														// return a DummyGrouper

		// fc-16.11.2011 - use a copy of the grouper (several data extractors
		// are updated in several threads, avoid concurrence problems)
		Grouper copy = g.getCopy();

		Collection output = copy.apply(input, getGrouperName().toLowerCase().startsWith("not ")); // the
																									// DummyGrouper
																									// returns
																									// the
																									// Collection
																									// unchanged
		return output;
		
	}

	// Use this method instead of the one just before - fc - 16.9.2004
	public Collection doFilter(GScene stand, String type) { // fc -17.9.2004

		Collection input = Group.whichCollection(stand, type); // trees or
																// cells (...)

		// Consider status selection
		//
		if (type.equals(Group.TREE) && statusSelection != null) {
			TreeList gtcstand = (TreeList) stand;
			input = gtcstand.getTrees(statusSelection);
		}

		if (!isGrouperMode()) {
			return input;
		} // no grouper selected

		GrouperManager gm = GrouperManager.getInstance();
		Grouper g = gm.getGrouper(getGrouperName()); // if group not found,
														// return a DummyGrouper

		// fc-16.11.2011 - use a copy of the grouper (several data extractors
		// are updated in several threads, avoid concurrence problems)
		Grouper copy = g.getCopy();

		Collection output = copy.apply(input, getGrouperName().toLowerCase().startsWith("not ")); // the
																									// DummyGrouper
																									// returns
																									// the
																									// Collection
																									// unchanged
		return output;

	}

	/**
	 * From DataFormat interface.
	 */
	@Override
	public String getDefaultDataRendererClassName() {

		// fc-15.10.2015 New graphs in Capsis4.2.4
		if (defaultDataRendererClassName.equals("capsis.extension.datarenderer.drcurves.DRCurves"))
			defaultDataRendererClassName = "capsis.extension.datarenderer.drgraph.DRGraph";
		if (defaultDataRendererClassName.equals("capsis.extension.datarenderer.drcurves.DRHistogram"))
			defaultDataRendererClassName = "capsis.extension.datarenderer.drgraph.DRBarGraph";
		if (defaultDataRendererClassName.equals("capsis.extension.datarenderer.drcurves.DRScatterPlot"))
			defaultDataRendererClassName = "capsis.extension.datarenderer.drgraph.DRScatterGraph";

		return defaultDataRendererClassName;
	}

	/**
	 * From DataFormat interface. fc - 6.5.2003 - modified this method from
	 * DETimeDbh
	 */
	public String getCaption() {
		String caption = getStep().getCaption();

		// treeIds
		//
		if (treeIds != null && !treeIds.isEmpty()) {
			caption += " - " + Translator.swap("Shared.id") + " " + Tools.toString(treeIds);
		}

		// individual group - fc - 13.9.2004
		//
		if (isGrouperMode() && !isCommonGrouper()) {
			caption += " - " + getGrouperName();
		}

		// If Status is used, not all types of trees are concerned
		// fc - 23.3.2004
		//
		if (statusSelection != null) {

			caption += StatusChooser.getName(statusSelection); // ex:
																// " (cut+dead)"
		}

		return caption;
	}

	/**
	 * From DataFormat interface. Returns the color of the step button of the
	 * step of the output. If step button has no color, returns another color.
	 */
	public Color getColor() {
		// Check if forced color
		if (forcedColor != null) {
			return forcedColor;
		} // fc - 29.4.2003

		Color col = null;
		StepButton b = ProjectManager.getInstance().getStepButton(step); // fc
																			// -
																			// 10.12.2004

		col = b.getColor();
		if (col == null) {
			col = StepButton.DEFAULT_COLOR;
		}

		// Several extractors on the button
		if (rank > 0) {

			float[] hsb = ColorManager.getHSB(col);

			// Color families for a single step : 6 different (1/6 = 0.17)
			col = ColorManager.getColor(hsb[0] + (rank + 1) * 0.17f, hsb[1], hsb[2]);
		}

		// // In some cases the step button has no color : return some color.
		// if (col == StepButton.DEFAULT_COLOR) {
		// col = DefaultColorProvider.getInstance ().nextColor ();
		// }
		return col;
	}

	/**
	 * Force extractor color. Can be used when several extractors are used on
	 * the same step with different groupers. In normal case, color is not
	 * forced and is equal to step button's color.
	 */
	public void setColor(Color forcedColor) {
		this.forcedColor = forcedColor;
	} // fc - 29.4.2003

	/**
	 * Return a String representation of this obbject.
	 */
	public String toString() {
		String step = "";
		try {
			step = getStep().toString();
		} catch (Exception a) {
		}
		return getName() + ", " + step;
	}

	/**
	 * Sub class can check some functional considerations about the settings in
	 * multi configuration dialog box. If trouble, open a dialog with a message,
	 * then return false :
	 * 
	 * <pre>
	 * if (classWidth > 100000) {	// funny example
	 *     JOptionPane.showMessageDialog (...);	// tell user why it goes wrong
	 *     return false;
	 * }
	 * </pre>
	 * 
	 * In case all is ok, return true.
	 */
	protected boolean functionalTestsAreOk(DEMultiConfPanel panel) {
		return true;
	}

	/**
	 * This method is called if user selects or changes groupers. It can be
	 * redefined in subclasses to do things in this case. Ex: updateSetProperty
	 * ("somePropertyKey", someSetOfPossibleValues);
	 */
	public void grouperChanged(String getGrouperName) {
	}

	// //////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////// MultiConfigurations
	// considerations
	// //////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Reminder : multiconfiguration considerations concern all the extractors
	// in
	// one single data block (ex : per hectare calculation)
	// //////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * SharedConfigurable interface.
	 */
	public String getSharedConfLabel() {
		return Translator.swap("Shared.common"); // let's twist again...
	}

	/**
	 * SharedConfigurable interface.
	 */
	public ConfigurationPanel getSharedConfPanel(Object param) {
		dataBlock = (DataBlock) param; // will be also needed in sharedConfigure
										// () below

		// Calibration data (optional)
		Collection<String> modelNames = dataBlock.detectCalibrationData(); // fc
																			// -
																			// 12.10.2004

		if (!modelNames.isEmpty()) {

			LinkedList<String> l = new LinkedList<String>();
			l.add(Translator.swap("Shared.noneFeminine")); // first item in the
															// list will be
															// default selected
			l.addAll(modelNames);

			// If the comboProperty already exists and contains exactly the same
			// names, do nothing
			// to keep the entries order (the first entry will be selected in
			// the combo box)
			boolean doNothing = false;
			if (hasConfigProperty("activateCalibration")) {
				LinkedList<String> existingvalues = (LinkedList<String>) settings.comboProperties
						.get("activateCalibration");
				if (l.containsAll(existingvalues) && existingvalues.containsAll(l))
					doNothing = true;
			}
			if (!doNothing) {
				removeComboProperty("activateCalibration");
				addComboProperty("activateCalibration", l);
			}
		}

		// TRIED to MOVE this code in sharedConfigure () (seems more logical)
		// if (hasConfigProperty ("activateCalibration")) {
		// String modelName = getComboProperty ("activateCalibration");
		// if (!modelName.equals (Translator.swap ("Shared.noneFeminine"))) {
		// dataBlock.addCalibrationExtractor (modelName);
		// } else {
		// dataBlock.removeCalibrationExtractor ();
		// }
		// }

		return new DEMultiConfPanel(this);
	}

	/**
	 * SharedConfigurable interface. In case of both shared-configuration and
	 * single-configuration, sharedConfigure () is called before configure ().
	 */
	public void sharedConfigure(ConfigurationPanel panel) {
		DEMultiConfPanel p = (DEMultiConfPanel) panel;
		dataBlock = p.getDataBlock(); // fc-26.9.2012

		// Note: checks have been done in checksAreOk () (empty, wrong type...)

		// Here : groupers
		//
		// ~ try {useCommonGroup = p.groupChooser.isGroupSelected ();} catch
		// (Exception e) {}
		try {
			StringBuffer b = new StringBuffer("DE multiconfigure(): p.grouperChooser.isGrouperSelected ()="
					+ p.grouperChooser.isGrouperAvailable() + " ");
			if (p.grouperChooser.isGrouperAvailable()) {
				try {
					settings.c_grouperMode = true;
					settings.c_grouperName = p.grouperChooser.getGrouperName();
					settings.c_grouperNot = p.grouperChooser.isGrouperNot(); // fc
																				// -
																				// 21.4.2004
					settings.c_grouperType = p.grouperChooser.getType(); // fc
																			// -
																			// 3.6.2008

					grouperChanged(settings.c_grouperName);
				} catch (Exception e) {
				}

			} else {
				settings.c_grouperMode = false; // fc - 23.3.2004 - added this
												// line
				settings.c_grouperName = ""; // fc - 23.3.2004 - added this line
				settings.c_grouperNot = false; // fc - 21.4.2004 - added this
												// line
			}
			b.append("settings.c_grouperMode=" + settings.c_grouperMode + " ");
			b.append("settings.c_grouperName=" + settings.c_grouperName + " ");
		} catch (Exception e) {
		}

		// Here : memo settings current state
		//
		try {
			settings.perHa = p.perHa.isSelected();
		} catch (Exception e) {
		}
		try {
			settings.percentage = p.percentage.isSelected();
		} catch (Exception e) {
		}

		try {
			settings.classWidth = Check.intValue(p.classWidth.getText());
		} catch (Exception e) {
		} // classWidth validity was checked in panel.cheksAreOk ()

		try {
			settings.intervalNumber = Check.intValue(p.intervalNumber.getText());
		} catch (Exception e) {
		}
		try {
			settings.intervalSize = Check.doubleValue(p.intervalSize.getText());
		} catch (Exception e) {
		}
		try {
			settings.icNumberOfSimulations = Check.intValue(p.icNumberOfSimulations.getText());
		} catch (Exception e) {
		}
		try {
			settings.icRisk = Check.doubleValue(p.icRisk.getText());
		} catch (Exception e) {
		}
		try {
			settings.icPrecision = Check.doubleValue(p.icPrecision.getText());
		} catch (Exception e) {
		}

		// Set booleanProperties
		//
		Iterator keys = p.booleanPropertiesCheckBoxes.keySet().iterator();
		Iterator values = p.booleanPropertiesCheckBoxes.values().iterator();
		while (keys.hasNext() && values.hasNext()) {
			String name = (String) keys.next();
			JCheckBox cb = (JCheckBox) values.next();
			if (isIndividualProperty(name)) {
				continue;
			}
			settings.booleanProperties.remove(name);
			settings.booleanProperties.put(name, new Boolean(cb.isSelected()));
		}

		// Set radioProperties
		//
		keys = p.radioPropertiesRadioButtons.keySet().iterator();
		values = p.radioPropertiesRadioButtons.values().iterator();
		while (keys.hasNext() && values.hasNext()) {
			String name = (String) keys.next();
			JRadioButton rb = (JRadioButton) values.next();
			if (isIndividualProperty(name)) {
				continue;
			}
			settings.radioProperties.remove(name);
			settings.radioProperties.put(name, new Boolean(rb.isSelected()));
		}

		// Set intProperties
		//
		keys = p.intPropertiesTextFields.keySet().iterator();
		values = p.intPropertiesTextFields.values().iterator();
		while (keys.hasNext() && values.hasNext()) {
			String name = (String) keys.next();
			JTextField f = (JTextField) values.next();
			if (isIndividualProperty(name)) {
				continue;
			}
			settings.intProperties.remove(name);
			settings.intProperties.put(name, new Integer(f.getText()));
		}

		// Set doubleProperties
		//
		keys = p.doublePropertiesTextFields.keySet().iterator();
		values = p.doublePropertiesTextFields.values().iterator();
		while (keys.hasNext() && values.hasNext()) {
			String name = (String) keys.next();
			JTextField f = (JTextField) values.next();
			if (isIndividualProperty(name)) {
				continue;
			}
			settings.doubleProperties.remove(name);
			settings.doubleProperties.put(name, new Double(f.getText()));
		}

		// Set comboProperties
		keys = p.comboPropertiesComboBoxes.keySet().iterator();
		values = p.comboPropertiesComboBoxes.values().iterator();
		while (keys.hasNext() && values.hasNext()) {
			String comboPropertyName = (String) keys.next();
			JComboBox f = (JComboBox) values.next();

			if (isIndividualProperty(comboPropertyName))
				continue;

			// Move selected item in first position of the linked list
			selectOptionInComboProperty(comboPropertyName, (String) f.getSelectedItem());

		}

		// MOVED this code here from getSharedConfiguration upper fc-4.9.2012
		if (hasConfigProperty("activateCalibration")) {
			String modelName = getComboProperty("activateCalibration");
			if (!modelName.equals(Translator.swap("Shared.noneFeminine"))) {
				dataBlock.addCalibrationExtractor(modelName);
			} else {
				dataBlock.removeCalibrationExtractor();
			}
		}

	}

	/**
	 * Selects the option matching the given translated name in the given
	 * comboProperty. I.e. moves the matching option in fist position in the
	 * comboProperty list. fc-26.9.2012
	 */
	public void selectOptionInComboProperty(String comboPropertyName, String translatedName) {
		try {
			LinkedList list = (LinkedList) settings.comboProperties.get(comboPropertyName);
			String option = translatedName;
			try {
				Map<String, String> map = comboPropertyTranslationToValue.get(comboPropertyName);
				option = map.get(translatedName); // get the original option
													// name (combo box
													// contains translations)

			} catch (Exception e) {
				// Maybe not an error if missing translation
				// Log.println (Log.ERROR, "DataExtractor.sharedConfigure ()",
				// "Exception, comboProperty=" + name
				// + ", trouble while trying to de-translate " +
				// f.getSelectedItem (), e);
			}

			boolean removed = list.remove(option);
			if (removed)
				list.addFirst(option);

		} catch (Exception e) {
			Log.println(Log.ERROR, "DataExtractor.selectOptionInComboProperty ()",
					"error in combo property selection, passed", e);
		}

	}

	/**
	 * SharedConfigurable interface.
	 */
	public void postConfiguration() {

		ExtensionManager.recordSettings(this);
		// here : replace following line by : see sharedConfigure ()
		upToDate = false;

	}

	// //////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////
	// Configuration considerations
	// //////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * From Configurable interface.
	 */
	public String getConfigurationLabel() {
		return AmapTools.cutIfTooLong(getCaption(), 50);
	}

	/**
	 * From Configurable interface. Configurable interface allows to pass a
	 * parameter. Here, it is a Scenario.
	 */
	public ConfigurationPanel getConfigurationPanel(Object parameter) {
		DEConfigurationPanel panel = new DEConfigurationPanel(this);
		return panel;
	}

	/**
	 * From Configurable interface. In case of both multi-configuration and
	 * single-configuration, sharedConfigure () is called before configure ().
	 */
	public void configure(ConfigurationPanel panel) {
		DEConfigurationPanel p = (DEConfigurationPanel) panel;

		// Individual config: tree ids
		//
		try {
			String ids = p.treeIdsTF.getText(); // JTextField
			treeIds.clear();

			StringTokenizer st = new StringTokenizer(ids, ", ");
			while (st.hasMoreTokens()) {
				String id = st.nextToken();
				treeIds.add(id);
			}
		} catch (Exception e) {
		}

		// Individual config: groupers
		// Only if not managed commonly for all the extractors of the data
		// block.
		//
		StringBuffer b = new StringBuffer("DE configure(): isCommonGrouper ()=" + isCommonGrouper() + " ");
		if (!isCommonGrouper()) {
			try {
				GrouperChooser grouperChooser = p.getGrouperChooser();
				i_grouperMode = grouperChooser.isGrouperAvailable();
				if (i_grouperMode) {
					i_grouperName = grouperChooser.getGrouperName();
					i_grouperNot = grouperChooser.isGrouperNot(); // fc -
																	// 21.4.2004
					grouperChanged(i_grouperName);
				} else {
					i_grouperName = "";
					i_grouperNot = false; // fc - 21.4.2004
				}
				b.append("settings.i_grouperMode=" + i_grouperMode + " ");
				b.append("settings.i_grouperName=" + i_grouperName + " ");
			} catch (Exception e) {
			}
		}

		// Individual config: status (alive, dead, cut...)
		//
		if (p.getStatusChooser() != null) {
			statusSelection = p.getStatusChooser().getSelection();
		}

	}

	public Collection<String> getDocumentationKeys() {
		return documentationKeys;
	}

	/**
	 * Added this accessor to be able to configure the extractor in script mode.
	 * E.g. ex.getSettings ().perHa = true;
	 */
	public DESettings getSettings() {
		return settings;
	}

	// //////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// ///////////////////////////////////////////////////////////////// Inner
	// classes
	// //////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////////////////////////////////////////

	// This class is related to setProperties (key, value).
	// A button that can be recognizable by its type
	static class SelectButton extends JButton {

		public String propertyName;
		public ItemSelector itemSelector;
		public JTextField textField;

		public SelectButton(String text, String propertyName, ItemSelector itemSelector, JTextField textField) {
			super(text);
			this.propertyName = propertyName;
			this.itemSelector = itemSelector;
			this.textField = textField;
		}
	}

	// This class is related to setProperties (key, value).
	// Ex: key=treeIds value=ItemSelector with possibleValues = 1,2,3 and
	// SelectedValues = 2.
	static class ItemSelector implements Spiable, Serializable {

		public Set possibleValues; // contains Strings
		public Set selectedValues; // contains Strings

		public ItemSelector(String[] possibleValues, String[] selectedValues) {
			this.possibleValues = new TreeSet();
			this.selectedValues = new TreeSet();
			setPossibleValues(possibleValues);
			setSelectedValues(selectedValues);
		}

		public void setPossibleValues(String[] possibleValues) {
			try {

				// fc - 14.12.2007
				this.possibleValues.clear();

				for (int i = 0; i < possibleValues.length; i++) {
					this.possibleValues.add(possibleValues[i]);
				}
				clearSelectedValues();
			} catch (Exception e) {
			}
		}

		public void setSelectedValues(String[] selectedValues) {
			try {
				for (int i = 0; i < selectedValues.length; i++) {
					this.selectedValues.add(selectedValues[i]);
				}
			} catch (Exception e) {
			}
		}

		public void clearSelectedValues() { // fc - 21.12.2004
			this.selectedValues.clear();
			action(null);
		}

		// Spiable equipment
		transient private Spy spy; // fc-9.2.2011 trying to fix a xml
									// serialization bug (saves too much)

		public void setSpy(Spy s) {
			spy = s;
		}

		public Spy getSpy() {
			return spy;
		}

		public void action(Object sth) {
			try {
				spy.action(this, sth);
			} catch (Exception e) {
			} // spy may be null
		}
		// end-of Spiable equipment
	}

	/**
	 * Returns true is the extractor can work on the current Step (e.g. false if
	 * works on cut trees and no cut trees on this step).
	 */
	public boolean isAvailable() {
		return true;
	}

	// fc-12.10.2015 REMOVED
	// public int getNumberOfDataSeries() {
	// // fc-21.9.2015
	// return listOfDataSeries == null ? 0 : listOfDataSeries.size ();
	// }
	//
	// public int getMaxSizeOfDataSeries () {
	// // fc-21.9.2015
	// if (getNumberOfDataSeries() == 0)
	// return 0;
	// int maxSize = 0;
	// for (XYSeries s : getListOfDataSeries()) {
	// maxSize = Math.max (maxSize, s.size());
	// }
	// return maxSize;
	// }
	//
	// public List<XYSeries> getListOfDataSeries() {
	// // fc-21.9.2015
	// if (listOfDataSeries == null)
	// return new ArrayList<XYSeries> (); // empty
	// return listOfDataSeries;
	// }
	//
	// public void clearDataSeries () {
	// listOfDataSeries = null;
	// }
	//
	// public void addDataSeries(XYSeries dataSeries) {
	// // fc-21.9.2015
	// if (listOfDataSeries == null)
	// listOfDataSeries = new ArrayList<XYSeries> ();
	// this.listOfDataSeries.add(dataSeries);
	// }
	// fc-12.10.2015 REMOVED

}
