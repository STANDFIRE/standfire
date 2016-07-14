/*
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 *
 * Copyright (C) 2013-2014  Mathieu Fortin - UMR LERFoB (AgroParisTech/INRA)
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
package capsis.extension.modeltool.optimizer;

import java.awt.Component;
import java.awt.Container;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragSource;
import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JPanel;
import javax.swing.text.JTextComponent;

import repicea.gui.CommonGuiUtility;
import repicea.gui.ListManager;
import repicea.gui.REpiceaUIObject;
import repicea.gui.dnd.DnDPanel;
import repicea.gui.dnd.DragGestureImpl;
import capsis.extension.modeltool.scenariorecorder.ParameterDialogWrapper;
import capsis.extension.modeltool.scenariorecorder.ParameterDialogWrapperLinker;
import capsis.extension.modeltool.scenariorecorder.ScenarioRecorder;
import capsis.extension.modeltool.scenariorecorder.ScenarioRecorderEvent;
import capsis.extension.modeltool.scenariorecorder.ScenarioRecorderEvent.EventType;
import capsis.extension.modeltool.scenariorecorder.ScenarioRecorderListener;

/**
 * The ControlVariableManager class contains a list of ControlVariableHandler instances.
 * @author Mathieu Fortin - May 2014
 */
public class ControlVariableManager implements REpiceaUIObject, ListManager<ControlVariableHandler>, ScenarioRecorderListener, Serializable {

	
	class InternalDragGestureImpl extends DragGestureImpl<ControlVariableHandler> {

		final ParameterDialogWrapperLinker wrapperLinker; 

		InternalDragGestureImpl(ParameterDialogWrapper wrapper, JTextComponent textComponent) {
			super();
			wrapperLinker = ParameterDialogWrapperLinker.makeParameterDialogWrapperLinker(wrapper, textComponent);
		}
		
		@SuppressWarnings("unchecked")
		protected ControlVariableHandler adaptSourceToTransferable(DragGestureEvent event) {
			return new ControlVariableHandler(wrapperLinker, (JPanel) event.getComponent());
		}
		
		@Override
		public void dragGestureRecognized(DragGestureEvent event) {
			if (!ControlVariableManager.this.alreadyContainsThisHandler(wrapperLinker)) {
				super.dragGestureRecognized(event);
			}
		}
	}
	

	private final List<ControlVariableHandler> list;
	private transient DnDPanel guiInterface;
	private transient final Random random;
	private transient final OptimizerTool tool;

	/**
	 * Contructor.
	 */
	protected ControlVariableManager(OptimizerTool tool) {
		list = new ArrayList<ControlVariableHandler>();
		this.tool = tool;
		random = new Random();
		ScenarioRecorder.getInstance().addScenarioRecorderListener(this);
	}
	
	
	@Override
	public List<ControlVariableHandler> getList() {return list;}

	@Override
	public void registerObject(ControlVariableHandler obj) {
		list.add(obj);
		tool.fireOptimizerEvent(OptimizerEvent.EventType.PARAMETER_ADDED, null);
	}

	@Override
	public void removeObject(ControlVariableHandler obj) {
		list.remove(obj);
		tool.fireOptimizerEvent(OptimizerEvent.EventType.PARAMETER_REMOVED, null);
	}

	@Override
	public DnDPanel getUI() {
		if (guiInterface == null) {
			guiInterface = new DnDPanel<ControlVariableHandler>(this, ControlVariableHandler.class);
		}
		return guiInterface;
	}

	
	private boolean alreadyContainsThisHandler(ParameterDialogWrapperLinker linker) {
		for (ControlVariableHandler handler : list) {
			if (handler.wrapperLinker.equals(linker)) {
				return true;
			}
		}
		return false;
	}
	
	
	
	private void enableDnDOnWrapper(ParameterDialogWrapper wrapper) {
		List<Component> components = CommonGuiUtility.mapComponents(wrapper.getUI(), JTextComponent.class);
		for (Component comp : components) {
			
			Container parent = CommonGuiUtility.getParentComponent(comp, JPanel.class);
//			while (parent != null && !(parent instanceof JPanel)) {
//				parent = parent.getParent();
//			}
//			
//			
			if (parent != null) {
				DragSource ds = new DragSource();
				ds.createDefaultDragGestureRecognizer(parent, DnDConstants.ACTION_COPY, new InternalDragGestureImpl(wrapper, (JTextComponent) comp));
			}
		}
	}
	
	@Override
	public void scenarioRecorderJustDidThis(ScenarioRecorderEvent evt) {
		if (evt.getType () == EventType.PLAY_TERMINATED) {
			List<ParameterDialogWrapper> wrappers = evt.getWrappers();
			for (ParameterDialogWrapper wrapper : wrappers) {
				enableDnDOnWrapper(wrapper);
			}
		} else if (evt.getType() == EventType.WRAPPER_ADDED) {
			List<ParameterDialogWrapper> wrappers = evt.getWrappers();
			enableDnDOnWrapper(wrappers.get(wrappers.size() - 1));
		}
	}

	@Override
	protected void finalize() {
		ScenarioRecorder.getInstance().removeScenarioRecorderListener(this);
	}
	
	protected void setControlVariables(double[] realValues) {
		if (realValues.length != list.size()) {
			throw new InvalidParameterException("The number of values does not match the number of control variables");
		} else {
			for (int i = 0; i < realValues.length; i++) {
				list.get(i).setValue(realValues[i]);
			}
		}
	}

	protected double[] getControlVariablesValues() {
		double[] values = new double[list.size()];
		ControlVariableHandler cvh;
		for (int i = 0; i < list.size(); i++) {
			cvh = list.get(i);
			values[i] = cvh.getValue();
		}
		return values;
	}

	protected double[] getControlVariablesRealValues() {
		double[] realValues = new double[list.size()];
		ControlVariableHandler cvh;
		for (int i = 0; i < list.size(); i++) {
			cvh = list.get(i);
			realValues[i] = cvh.getRealValue();
		}
		return realValues;
	}
	
	protected double[] getRandomRealValues() {
		double[] realValues = new double[list.size()];
		for (int i = 0; i < list.size(); i++) {
			realValues[i] = ControlVariableHandler.getRandomReal(random);
		}
		return realValues;
	}
	
	protected double[][] getRandomSimplex() {
		double[][] simplex = new double[list.size() + 1][list.size()];
		for (int i = 0; i < simplex.length; i++) {
			simplex[i] = getRandomRealValues();
		}
		return simplex;
	}
	
	protected double[] convertQuantileToReal(double[] quantiles) {
		double[] realPoints = new double[quantiles.length] ;
		for (int i = 0; i < quantiles.length; i++){
			realPoints[i] = ControlVariableHandler.getRealValueFromCurrentValue(quantiles[i], 0d, 1d);
		}
		return realPoints;
	}


	protected void loadFrom(ControlVariableManager controlVariableManager) {
		list.clear();
		list.addAll(controlVariableManager.list);
		tool.fireOptimizerEvent(OptimizerEvent.EventType.PARAMETER_ADDED, null);
	}
	
}
