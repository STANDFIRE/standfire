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

package capsis.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.Border;

import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.JWidthLabel;
import jeeb.lib.util.LinePanel;
import jeeb.lib.util.Translator;
import jeeb.lib.util.inspector.AmapInspectorPanel;
import capsis.commongui.command.MemorizerPanel;
import capsis.commongui.projectmanager.ProjectPanel;
import capsis.kernel.IdCard;
import capsis.util.Configurable;
import capsis.util.ConfigurationPanel;

/**
 * Configuration panel for a Project panel. Configuration of the 
 * underlying project.
 *
 * @author F. de Coligny - may 2002
 */
public class ProjectPanelConfigPanel extends ConfigurationPanel implements ActionListener {
	
	public JTextField scenarioName;
//	public DChangeVisibility visibilityDialog;
	private JButton visibility;

	private ProjectPanel mum;		// mummy is being configured
	
	
	/**
	 * Constructor.
	 */
	public ProjectPanelConfigPanel (Configurable sp, Object o) {
		super (sp);
		mum = (ProjectPanel) sp;
		createUI ();
	}

	public void actionPerformed (ActionEvent evt) {
//		if (evt.getSource ().equals (visibility)) {
//			visibilityDialog = new DChangeVisibility (mum.getProject());
//		}
	}

	public boolean checksAreOk () {
		// 1. scenario name
		String name = scenarioName.getText ().trim ();
		if (name.equals ("")) {
			JOptionPane.showMessageDialog (this, Translator.swap ("ScenarioPanel.scenarioNameNeeded"),
					Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
			return false;
		}
		
		return true;
	}

	protected void createUI () {
		Border etched = BorderFactory.createEtchedBorder ();
		
		// 1. General
		ColumnPanel part1 = new ColumnPanel ();
		Border b1 = BorderFactory.createTitledBorder (etched, Translator.swap ("ScenarioPanel.general"));
		part1.setBorder (b1);
		
		LinePanel l1 = new LinePanel ();
		l1.add (new JWidthLabel (Translator.swap ("ScenarioPanel.scenarioName")+" : ", 80));
		scenarioName = new JTextField (mum.getProject().getName (), 15);
		scenarioName.setEditable (false);
		l1.add (scenarioName);
		l1.addStrut0 ();
		part1.add (l1);
		
		// IDCard
		LinePanel l2 = new LinePanel ();
		JWidthLabel lab = new JWidthLabel (Translator.swap ("ScenarioPanel.model")+" : ", 80);
		ColumnPanel component1 = new ColumnPanel ();
		component1.add (lab);
		component1.addGlue ();
		
		AmapInspectorPanel pan = new AmapInspectorPanel (getCompactString (mum.getProject().getModel ().getIdCard ()), true);
		JScrollPane panModel = new JScrollPane (pan);
		
		JPanel aux1 = new JPanel (new BorderLayout ());
		aux1.add (panModel, BorderLayout.CENTER);
		
		l2.add (component1);
		l2.add (aux1);
		l2.addStrut0 ();
		part1.add (l2);
		part1.addStrut0 ();
		
		// 2. Steps visibility
		ColumnPanel part2 = new ColumnPanel ();
		Border b2 = BorderFactory.createTitledBorder (etched, Translator.swap ("ScenarioPanel.stepsVisibility"));
		part2.setBorder (b2);
		
		LinePanel l3 = new LinePanel ();
		l3.add (new JWidthLabel (Translator.swap ("ScenarioPanel.visibilitySettings")+" : ", 220));
		visibility = new JButton (Translator.swap ("ScenarioPanel.change"));
		visibility.addActionListener (this);
		l3.add (visibility);
		l3.addGlue ();
		
		part2.add (l3);
		part2.addStrut0 ();
		
		
		// 3. Memorizers
		ColumnPanel part3 = new ColumnPanel ();
		Border b3 = BorderFactory.createTitledBorder (etched, Translator.swap ("ScenarioPanel.memory"));
		part3.setBorder (b3);
		
		LinePanel l4 = new LinePanel ();
		l4.add (new JWidthLabel (Translator.swap ("ScenarioPanel.Memorizer")+" : ", 220));
		l4.add (new MemorizerPanel (mum.getProject()));
		l4.addGlue ();
		
		part3.add (l4);
		part3.addStrut0 ();
		
		
		ColumnPanel aux = new ColumnPanel ();
		aux.add (part1);
		aux.add (part2);
		aux.add (part3);
		aux.addGlue ();
		
		setLayout (new BorderLayout ());
		add (aux, BorderLayout.NORTH);
		
	}

	
	public String getCompactString (IdCard idCard) {
		String sep = "§";
		StringBuffer b = new StringBuffer ();
		b.append (sep);
		b.append (Translator.swap ("IdCard.model"));
		b.append ("=");
		b.append (idCard.getModelName ());
		b.append (sep);
		b.append (Translator.swap ("IdCard.version"));
		b.append ("=");
		b.append (idCard.getModelVersion ());
		b.append (sep);
		b.append (Translator.swap ("IdCard.type"));
		b.append ("=");
		b.append (idCard.getModelType ());
		b.append (sep);
		b.append (Translator.swap ("IdCard.author"));
		b.append ("=");
		b.append (idCard.getModelAuthor ());
		b.append (sep);
		b.append (Translator.swap ("IdCard.institute"));
		b.append ("=");
		b.append (idCard.getModelInstitute ());
		b.append (sep);
		return b.toString ();
	}


}

