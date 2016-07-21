package capsis.extension.modeltool.scenariorecorder;

import repicea.util.REpiceaTranslator;
import capsis.extension.modeltool.scenariorecorder.ScenarioRecorder.MessageID;
import capsis.extensiontype.ModelTool;
import capsis.kernel.GModel;
import capsis.kernel.Step;


public class ScenarioRecorderTool implements ModelTool {

	static public final String AUTHOR="M. Fortin";
	static public final String VERSION="1.0";
	public static final String NAME = REpiceaTranslator.getString(MessageID.Name);
	public static final String DESCRIPTION = REpiceaTranslator.getString(MessageID.Description);

	public ScenarioRecorderTool() {}	// phantom constructor for extension manager
	
	
	/*	
	 * Extension dynamic compatibility mechanism.
	 * This matchwith method checks if the extension can deal (i.e. is compatible) with the referent.
	 */
	static public boolean matchWith(Object referent) {
		return true;
	}

	@Override
	public void init(GModel m, Step s) {
		if (!ScenarioRecorder.getInstance().getUI().isVisible()) {
			ScenarioRecorder.getInstance().initialize(m, s);
			ScenarioRecorder.getInstance().showUI();
		}
	}

	
	
	
	
	@Override
	public void activate () {}

}
