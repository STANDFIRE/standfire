package capsis.extension.datarenderer.drgraph;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;

import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.LinePanel;
import jeeb.lib.util.Translator;
import capsis.extension.DRConfigurationPanel;
import capsis.util.Configurable;

/**
 * Configuration Panel for DRBarGraph.
 * 
 * @author F. de Coligny - October 2015
 */
public class DRBarGraphConfigurationPanel extends DRConfigurationPanel implements ActionListener {

	private DRBarGraph graph;
	
	private JCheckBox visibleValues;
	
	/**
	 * Constructor
	 */
	public DRBarGraphConfigurationPanel(Configurable graph) {
		super(graph);
		
		this.graph = (DRBarGraph) graph;
		
		// Enlarged mode
		LinePanel l1 = new LinePanel();
		visibleValues = new JCheckBox(Translator.swap("DRBarGraph.visibleValues"), this.graph.visibleValues);
		l1.add(visibleValues);
		l1.addGlue();
		
		
		// Main layout
		ColumnPanel main = new ColumnPanel();
		main.add(l1);

		mainContent.add(main);
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// Nothing at this time: a checkbox can not be in error
		
	}

	@Override
	public boolean checksAreOk() {
		// A checkbox can not be in error
		return true;
		
	}
	
	public boolean isVisibleValues() {
		return visibleValues.isSelected();
	}

	
	
}
