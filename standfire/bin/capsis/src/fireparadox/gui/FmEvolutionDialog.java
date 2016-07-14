package fireparadox.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import jeeb.lib.util.Check;
import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.JWidthLabel;
import jeeb.lib.util.MessageDialog;
import jeeb.lib.util.Translator;
import capsis.commongui.EvolutionDialog;
import capsis.commongui.util.Helper;
import capsis.kernel.Step;
import fireparadox.model.FmEvolutionParameters;
import fireparadox.model.FmStand;


/**	FiEvolutionDialog - Dialog box to input the limit parameters for growth.
 *
 *	@author O. Vigy, E. Rigaud - september 2006
 */
public class FmEvolutionDialog extends EvolutionDialog implements ActionListener {
	
	private FmStand stand;
	
	private JTextField numberOfSteps;
	
	private JButton ok;
	private JButton cancel;
	private JButton help;
	
	
	/**	Constructor.
	 */
	public FmEvolutionDialog (Step s) {
		super ();
		
		this.stand = (FmStand) s.getScene ();
		
		createUI ();
		// location is set by AmapDialog
		pack ();
		show ();
	}
	
	//	Ok was hit
	//
	private void okAction () {
		// Checks...
		String nos = numberOfSteps.getText ().trim ();
		if (Check.isEmpty (nos) || !Check.isInt (nos)) {
			MessageDialog.print (this, Translator.swap ("FiEvolutionDialog.numberOfStepsMustBeAnIntegerGreaterThanZero"));
			return;
		}
		
		int n = Check.intValue (numberOfSteps.getText ());
		if (n <= 0) {
			MessageDialog.print (this, Translator.swap ("FiEvolutionDialog.numberOfStepsMustBeAnIntegerGreaterThanZero"));
			return;
		}
		
		// Create the EvolutionParameters
		FmEvolutionParameters p = new FmEvolutionParameters ();
		p.numberOfSteps = n;
		
		setEvolutionParameters (p);
				
		// Close dialog: valid
		setValidDialog (true);
	}
	
	/**	Actions on the buttons
	 */
	public void actionPerformed (ActionEvent evt) {
		if (evt.getSource ().equals (ok)) {
			okAction ();
		} else if (evt.getSource ().equals (cancel)) {
			setValidDialog (false);
		} else if (evt.getSource ().equals (help)) {
			Helper.helpFor (this);
		}
	}
	
	//	Initialize the GUI.
	//
	private void createUI () {
		JPanel p0 = new JPanel (new FlowLayout (FlowLayout.LEFT));
		JPanel p1 = new JPanel (new FlowLayout (FlowLayout.LEFT));
		
		p0.add (new JWidthLabel (Translator.swap ("FiEvolutionDialog.fromDate")+" :", 120));
		p0.add (new JLabel (""+stand.getDate ()));
		
		p1.add (new JWidthLabel (Translator.swap ("FiEvolutionDialog.numberOfSteps")+" :", 120));
		numberOfSteps = new JTextField (5);
		p1.add (numberOfSteps);
		
		JPanel controlPanel = new JPanel (new FlowLayout (FlowLayout.RIGHT));
		ok = new JButton (Translator.swap ("Shared.ok"));
		cancel = new JButton (Translator.swap ("Shared.cancel"));
		help = new JButton (Translator.swap ("Shared.help"));
		controlPanel.add (ok);
		controlPanel.add (cancel);
		controlPanel.add (help);
		ok.addActionListener (this);
		cancel.addActionListener (this);
		help.addActionListener (this);
		
		setDefaultButton (ok);	// from AmapDialog
		
		ColumnPanel part1 = new ColumnPanel ();
		part1.add (p0);
		part1.add (p1);
		
		getContentPane ().setLayout (new BorderLayout ());
		getContentPane ().add (part1, BorderLayout.CENTER);
		getContentPane ().add (controlPanel, BorderLayout.SOUTH);
		
		setTitle (Translator.swap ("FiEvolutionDialog.growthParameters"));
		
		setModal (true);
	}
	
//	public int getStepNumber () {return Check.intValue (numberOfSteps.getText ().trim ());}
	
}

