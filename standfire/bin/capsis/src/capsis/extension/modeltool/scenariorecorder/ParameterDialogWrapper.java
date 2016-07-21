/*
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 *
 * Copyright (C) 2014 Mathieu Fortin (AgroParisTech/INRA - UMR LERFoB)
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
package capsis.extension.modeltool.scenariorecorder;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.lang.reflect.Constructor;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.JDialog;
import javax.swing.text.JTextComponent;

import jeeb.lib.util.AmapDialog;
import repicea.gui.CommonGuiUtility;
import repicea.gui.REpiceaShowableUI;
import repicea.gui.SynchronizedListening;
import repicea.gui.UIToolKit;
import repicea.gui.UIToolKit.WindowTrackerListener;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;
import capsis.commongui.EvolutionDialog;
import capsis.extension.modeltool.optimizer.OverridableEvolutionDialog;
import capsis.kernel.Step;

public class ParameterDialogWrapper implements REpiceaShowableUI, SynchronizedListening, WindowListener,
		ActionListener, WindowTrackerListener {

	protected static enum ControlType {
		Button, TextField
	};

	protected static enum ParameterCategory {
		Evolution, Intervention
	}

	static class ComponentSummary {
		private String className;
		private String value;

		protected ComponentSummary(Component comp) {
			className = comp.getClass().getName();
			if (comp instanceof AbstractButton) {
				value = ((Boolean) ((AbstractButton) comp).isSelected()).toString();
			} else if (comp instanceof JTextComponent) {
				value = ((JTextComponent) comp).getText();
			}
		}

		protected void updateComponentValue(Component comp) {
			if (comp.getClass().getName().equals(className)) {
				if (comp instanceof AbstractButton) {
					((AbstractButton) comp).setSelected(Boolean.parseBoolean(value));
				} else if (comp instanceof JTextComponent) {
					((JTextComponent) comp).setText(value);
				}
			} else {
				throw new InvalidParameterException("The controls seem to have changed!");
			}
		}

		protected void setValue(String value) {
			this.value = value;
		}

		protected String getValue() {
			return value;
		}
	}

	protected static enum MessageID implements TextableEnum {
		Segment("Segment", "Segment");

		;
		MessageID(String englishText, String frenchText) {
			setText(englishText, frenchText);
		}

		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}

		@Override
		public String toString() {
			return REpiceaTranslator.getString(this);
		}
	}

	private String wrapperLabel;
	private final String className;
	private final ParameterCategory category;
	protected Map<ControlType, List<ComponentSummary>> controlSummary = new HashMap<ControlType, List<ComponentSummary>>();
	private String okButtonText;

	private transient AbstractButton lastButtonPressed;
	private transient AbstractButton okButton;
	private transient ScenarioRecorder recorder;
	protected transient AmapDialog guiInterface;
	protected transient Map<ControlType, List<Component>> controlMap = new HashMap<ControlType, List<Component>>();
	private transient Step instantiationStep;
	private transient boolean isOverridable;
	private transient boolean overridableChecked;

	protected ParameterDialogWrapper(ParameterCategory category, AmapDialog window, ScenarioRecorder recorder) {
		this.recorder = recorder;
		this.category = category;
		this.className = window.getClass().getName();
		this.guiInterface = window;
		controlMap = trackControlsOfThisWindow(window);
		listenTo();
	}

	protected boolean triggerOkButton() {
		if (isOverridable && guiInterface != null) {
			((OverridableEvolutionDialog) guiInterface).okAction();
			return true;
		} else if (okButton != null) {
			okButton.doClick();
			return true;
		} else {
			return false;
		}
	}

	protected String getStringFromComponentAtIndex(int i) {
		if (controlMap.get(ControlType.TextField) != null && i < controlMap.get(ControlType.TextField).size()) {
			return ((JTextComponent) controlMap.get(ControlType.TextField).get(i)).getText();
		}
		return null;
	}

	protected void setInstantiationStep(Step step) {
		instantiationStep = step;
		if (isOverridable()) {
			if (guiInterface != null) {
				((OverridableEvolutionDialog) guiInterface).reinit(instantiationStep);
				resetValues();
			}
		} else {
			guiInterface = null;
		}
	}

	private boolean isOverridable() {
		if (!overridableChecked) {
			try {
				isOverridable = OverridableEvolutionDialog.class.isAssignableFrom(Class.forName(className));
			} catch (ClassNotFoundException e) {
			}
			overridableChecked = true;
		}
		return isOverridable;
		// return false;
	}

	private Map<ControlType, List<Component>> trackControlsOfThisWindow(Window window) {
		Map<ControlType, List<Component>> controlMap = new HashMap<ControlType, List<Component>>();
		List<Component> buttons = CommonGuiUtility.mapComponents(window, AbstractButton.class);
		for (Component comp : buttons) {
			((AbstractButton) comp).addActionListener(this);
		}
		controlMap.put(ControlType.Button, buttons);
		controlMap.put(ControlType.TextField, CommonGuiUtility.mapComponents(window, JTextComponent.class));
		return controlMap;
	}

	@Override
	public JDialog getUI() {
		if (guiInterface == null) {
			UIToolKit.addWindowTrackerListener(this);
			try {
				Class evolParametersClass = Class.forName(className);
				Constructor ctr = null;
				if (category == ParameterCategory.Evolution) {
					ctr = evolParametersClass.getConstructor(new Class[] { Step.class });
				}
				guiInterface = (AmapDialog) ctr.newInstance(instantiationStep);
			} catch (Exception e) {
				throw new InvalidParameterException("The parameters to create the dialog are invalid!");
			} finally {
				UIToolKit.removeWindowTrackerListener(this);
			}
			guiInterface.setAlwaysOnTop(true);
		}
		return guiInterface;
	}
	
	@Override 
	public boolean isVisible() {
		return guiInterface != null && guiInterface.isVisible();
	}


	protected void resetValues() throws InvalidParameterException {
		for (ControlType controlType : controlMap.keySet()) {
			List<Component> controls = controlMap.get(controlType);
			List<ComponentSummary> controlSummary = this.controlSummary.get(controlType);
			if (controls.size() != controlSummary.size()) {
				throw new InvalidParameterException("The controls seem to have changed!");
			} else {
				for (int i = 0; i < controls.size(); i++) {
					Component comp = controls.get(i);
					ComponentSummary summary = controlSummary.get(i);
					summary.updateComponentValue(comp);
				}
			}
		}

	}

	@Override
	public void showUI() {
		if (guiInterface == null) {
			getUI();
		} else {
			getUI().setVisible(true);
		}
	}

	public void hideInterface() {
		if (guiInterface != null && guiInterface.isVisible()) {
			guiInterface.setVisible(false);
		}
	}

	@Override
	public String toString() {
		if (wrapperLabel != null) {
			return wrapperLabel;
		} else {
			return "";
		}
	}

	@Override
	public void listenTo() {
		if (guiInterface != null) {
			guiInterface.addWindowListener(this);
		}
	}

	@Override
	public void doNotListenToAnymore() {
		if (guiInterface != null) {
			guiInterface.removeWindowListener(this);
		}
		List<Component> list = controlMap.get(ControlType.Button);
		if (list != null) {
			for (Component comp : list) {
				((AbstractButton) comp).removeActionListener(this);
			}
		}
	}

	@Override
	public void windowOpened(WindowEvent e) {
	}

	@Override
	public void windowClosing(WindowEvent e) {
	}

	@Override
	public void windowClosed(WindowEvent e) {
		if (guiInterface instanceof AmapDialog) {
			if (((AmapDialog) guiInterface).isValidDialog()) {
				disableControls();
				okButton = lastButtonPressed;
				okButtonText = okButton.getText();
				saveControlSummary();
				recorder.registerWrapper(this);
			}
		}
		doNotListenToAnymore();
	}

	private void saveControlSummary() {
		controlSummary.clear();
		for (ControlType controlType : controlMap.keySet()) {
			if (!controlSummary.containsKey(controlType)) {
				controlSummary.put(controlType, new ArrayList<ComponentSummary>());
			}
			List<ComponentSummary> summaryList = controlSummary.get(controlType);
			for (Component comp : controlMap.get(controlType)) {
				summaryList.add(new ComponentSummary(comp));
			}
		}

	}

	private void disableControls() {
		for (List<Component> components : controlMap.values()) {
			for (Component comp : components) {
				comp.setEnabled(false);
			}
		}

	}

	@Override
	public void windowIconified(WindowEvent e) {
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
	}

	@Override
	public void windowActivated(WindowEvent e) {
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
	}

	protected Object getParameters() {
		if (this.category == ParameterCategory.Evolution) {
			EvolutionDialog dlg = ((EvolutionDialog) getUI());
			if (triggerOkButton() && dlg.isValidDialog()) {
				return ((EvolutionDialog) getUI()).getEvolutionParameters();
			} else {
				throw new InvalidParameterException();
			}
		} else {
			return null;
		}
	}

	protected ParameterCategory getCategory() {
		return category;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		lastButtonPressed = (AbstractButton) e.getSource();
	}

	@Override
	public void receiveThisWindow(Window retrievedWindow) {
		if (retrievedWindow instanceof AmapDialog) {
			if (category == ParameterCategory.Evolution && retrievedWindow instanceof EvolutionDialog) {
				if (guiInterface == null) {
					Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
					double x = screenSize.getWidth();
					double y = screenSize.getHeight();
					Point point = new Point((int) x, (int) y);
					retrievedWindow.setLocation(point);
					controlMap = trackControlsOfThisWindow(retrievedWindow);
					List<Component> buttons = controlMap.get(ControlType.Button);
					for (Component button : buttons) {
						if (((AbstractButton) button).getText().equals(okButtonText)) {
							okButton = (AbstractButton) button;
							break;
						}
					}
					resetValues();
					triggerOkButton();
					disableControls();
				}
			}
		}
	}

	protected void addToWrappersAndSetName() {
		recorder.parameterWrappers.add(this);
		Integer index = recorder.parameterWrappers.indexOf(this) + 1;
		wrapperLabel = MessageID.Segment.toString() + " " + index.toString() + ": " + category.name();
	}

}
