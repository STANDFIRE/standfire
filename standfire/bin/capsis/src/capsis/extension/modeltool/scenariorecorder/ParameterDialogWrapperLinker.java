package capsis.extension.modeltool.scenariorecorder;

import java.awt.Component;
import java.security.InvalidParameterException;

import javax.swing.text.JTextComponent;

import capsis.extension.modeltool.scenariorecorder.ParameterDialogWrapper.ComponentSummary;
import capsis.extension.modeltool.scenariorecorder.ParameterDialogWrapper.ControlType;


public class ParameterDialogWrapperLinker {

	protected final ParameterDialogWrapper wrapper;
	protected final int indexOfComponent;
	
	private ParameterDialogWrapperLinker(ParameterDialogWrapper wrapper, JTextComponent textComponent) {
		this.wrapper = wrapper;
		int index = -1;
		if (wrapper.controlMap.get(ControlType.TextField) != null) {
			index = wrapper.controlMap.get(ControlType.TextField).indexOf(textComponent);
		}
		if (index == -1) {
			throw new InvalidParameterException("The wrapper does not recognize the component or it has not been initialized yet!");
		} else {
			this.indexOfComponent = index;
		}
	}
	
	public static ParameterDialogWrapperLinker makeParameterDialogWrapperLinker(ParameterDialogWrapper wrapper, JTextComponent textComponent) {
		return new ParameterDialogWrapperLinker(wrapper, textComponent);
	}
	
	
	private ComponentSummary getComponentSummary() {
		return wrapper.controlSummary.get(ControlType.TextField).get(indexOfComponent);
	}
	
	public String getText() {return getComponentSummary().getValue();}
	
	public void setText(String str) {getComponentSummary().setValue(str);}
	
	public Component getComponent() {
		if (wrapper.controlMap != null) {
			return wrapper.controlMap.get(ControlType.TextField).get(indexOfComponent);
		} else {
			return null;
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ParameterDialogWrapperLinker) {
			ParameterDialogWrapperLinker thatLinker = (ParameterDialogWrapperLinker) obj;
			if (thatLinker.wrapper.equals(wrapper)) {
				if (thatLinker.indexOfComponent == this.indexOfComponent) {
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public String toString() {return wrapper.toString();}
	
}
