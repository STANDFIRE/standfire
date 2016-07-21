package capsis.extension.modeltool.forestgales2013;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import jeeb.lib.util.Translator;
import capsis.kernel.GScene;
import capsis.kernel.Project;
import capsis.kernel.Step;
import capsis.lib.forestgales.FGMethod;
import capsis.lib.forestgales.FGSLStandInterface;
import capsis.lib.forestgales.FGTLStandInterface;

/**
 * A panel to show the reports of ForestGales methods (2013 implementation).
 * 
 * @author F. de Coligny - August 2013
 */
public class FGDamageReportsPanel extends JPanel {

	static {
		Translator.addBundle ("capsis.extension.modeltool.forestgales2013.FGDamageCalculator");
	}

	private Step refStep;
	private Project project;

	private JTabbedPane tabs;

	/**
	 * Constructor.
	 */
	public FGDamageReportsPanel (Step refStep) {
		super (new BorderLayout ());

		this.refStep = refStep;
		this.project = refStep.getProject ();

		Vector<Step> steps = project.getStepsFromRoot (refStep); // from root step to refStep
		for (Step step : steps) {
			considerNewTab (step.getScene ());
		}

		createUI ();

	}

	/**
	 * Check if a tab should be added for the given scene (if ForestGales methods can be found).
	 */
	private void considerNewTab (GScene scene) {

		List<FGMethod> methods = null;
		if (scene instanceof FGSLStandInterface) methods = ((FGSLStandInterface) scene).getFGMethods ();
		if (scene instanceof FGTLStandInterface) methods = ((FGTLStandInterface) scene).getFGMethods ();
		if (methods == null) return; // No ForestGales methods were found on this scene

		createNewTab (scene.getStep ().getCaption (), methods);

	}

	/**
	 * Create a tab to show the reports of the given methods.
	 */
	private void createNewTab (String tabName, List<FGMethod> methods) {
		if (tabs == null) tabs = new JTabbedPane ();

		JTabbedPane tabs2 = new JTabbedPane ();
		tabs2.setTabLayoutPolicy (JTabbedPane.SCROLL_TAB_LAYOUT);

		for (FGMethod m : methods) {
			String report = m.getReport ();
			JTextArea area = new JTextArea (report);
			area.setEditable (false);
			JScrollPane s = new JScrollPane (area);
//			s.setPreferredSize (new Dimension (250, 500));
			s.setPreferredSize (new Dimension (s.getPreferredSize ().width, 500));
			tabs2.addTab (m.getName (), s);
		}

		tabs.addTab (tabName, tabs2);
	}

	private void createUI () {
		if (tabs == null) {
			add (new JLabel (Translator.swap ("FGDamageReportsPanel.noForestGalesReportsCouldBeFound")), BorderLayout.CENTER);
		} else {
//			tabs.setAutoscrolls (true);
			tabs.setTabLayoutPolicy (JTabbedPane.SCROLL_TAB_LAYOUT);
			add (tabs, BorderLayout.CENTER);
		}
	}

}
