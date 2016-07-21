package capsis.lib.economics2.gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.TreeSet;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.JViewport;

import jeeb.lib.util.IconLoader;
import jeeb.lib.util.Log;
import jeeb.lib.util.Spatialized;
import jeeb.lib.util.Translator;
import capsis.commongui.projectmanager.StepButton;
import capsis.commongui.util.Helper;
import capsis.commongui.util.Tools;
import capsis.defaulttype.Numberable;
import capsis.defaulttype.Speciable;
import capsis.defaulttype.Tree;
import capsis.defaulttype.TreeList;
import capsis.extension.AbstractStandViewer;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.Step;
import capsis.lib.economics2.EconomicModel;
import capsis.lib.economics2.EconomicScenario;
import capsis.lib.economics2.EconomicStandDescription;
import capsis.util.GTreeIdComparator;
import capsis.util.Pilotable;

public class EconomicTextViewer extends AbstractStandViewer implements ActionListener, Pilotable {

	static public String AUTHOR = "Gauthier Ligot";
	static public String VERSION = "1.0";

	private JButton helpButton;
	private JScrollPane scrollpane;
	private JViewport viewport;

	static {
		Translator.addBundle("capsis.lib.economics2.gui.EconomicTranslator");
	} 

	@Override
	public void init(GModel model, Step s, StepButton but) throws Exception {
	
		super.init (model, s, but);
		setLayout (new GridLayout (1, 1));		// important for viewer appearance
		
		try {		
			
			if(((EconomicModel) model).getEconomicScenario()==null){
				throw new Exception("EconomicTextViewer.init() - the economic scenario has not been defined yet!");
				
				// if so, open the model Tool?
			}
			
			createUI ();
			update ();
			
		} catch (Exception e) {
			Log.println (Log.ERROR, "EconomicTextViewer", "Error in constructor", e);
			throw e;	// propagate
		}
	}
	
	/**	Extension dynamic compatibility mechanism.
	*	This matchwith method checks if the extension can deal (i.e. is compatible) with the referent.
	*/
	static public boolean matchWith (Object referent) {
		try {
			if (!(referent instanceof EconomicModel)) {return false;}
			//~ GModel m = (GModel) referent;
			
		} catch (Exception e) {
			Log.println (Log.ERROR, "EconomicTextViewer.matchWith ()", "Error in matchWith () (returned false)", e);
			return false;
		}
		return true;
	}
	
	
	/**	Create the user interface
	*/
	public void createUI () {
		getContentPane ().setLayout (new BorderLayout ());
		getContentPane ().add (getPilot (), BorderLayout.NORTH);
		
		scrollpane = new JScrollPane (new JTextArea ());
		viewport = scrollpane.getViewport ();
		
		getContentPane ().add (scrollpane, BorderLayout.CENTER);
	}

	/**	Update the viewer with the current step button
	*/
	public void update () {
		super.update ();
		if (sameStep) {return;}
		
		final JScrollBar vBar = scrollpane.getVerticalScrollBar ();
		final JScrollBar hBar = scrollpane.getHorizontalScrollBar ();
		final int vBarPosition = vBar.getValue ();
		final int hBarPosition = hBar.getValue ();
		
		StringBuffer sb = new StringBuffer ();

		// GScene:
		String filler = "    ";
		
//		Collection data = new ArrayList ();	// Strings
		
		GScene scene = stepButton.getStep ().getScene ();
		EconomicScenario es = ((EconomicModel) model).getEconomicScenario();
		
				
		// Some general information
		append (sb, Translator.swap ("EconomicTextViewer") + " - " + VERSION + " - " + step.getCaption ());
		append (sb," ");
		append (sb," ");
		append (sb,"--------------------------------------");
		append (sb,Translator.swap ("EconomicTextViewer.titleParam"));
		append (sb,"--------------------------------------");
		
		EconomicScenario.EconomicCase economicCase = es.getEconomicCase();
		
		append (sb,Translator.swap ("economicCase") + " : " + economicCase);
		append (sb,Translator.swap ("EconomicTextViewer.nyears") + " : " + es.getnYears());
		
		if(economicCase.equals(EconomicScenario.EconomicCase.INFINITY_CYCLE_WITH_LAND_OBSERVATION_AT_FIRST_OR_LAST_DATE)){
			append (sb,Translator.swap ("firstDate") + " : " + es.getFirstDateInfiniteCycle());
			append (sb,Translator.swap ("lastDate") + " : " + es.getLastDate());
			append (sb,Translator.swap ("discountRate") + " : " + es.getDiscountRate());
			append (sb,Translator.swap ("land") + " : " + es.getLand());
		}else if(economicCase.equals(EconomicScenario.EconomicCase.INFINITY_CYCLE_WITHOUT_LAND_OBSERVATION)){
			append (sb,Translator.swap ("firstDate") + " : " + es.getFirstDateInfiniteCycle());
			append (sb,Translator.swap ("lastDate") + " : " + es.getLastDate());
			append (sb,Translator.swap ("discountRate") + " : " + es.getDiscountRate());
		}else if(economicCase.equals(EconomicScenario.EconomicCase.TRANSITORY_PERIOD_PLUS_INFINITY_CYCLE)){
			append (sb,Translator.swap ("firstDate") + " : " + es.getFirstDate());
			append (sb,Translator.swap ("intermediateDate") + " : " + es.getFirstDateInfiniteCycle());
			append (sb,Translator.swap ("lastDate") + " : " + es.getLastDate());
			append (sb,Translator.swap ("discountRate") + " : " + es.getDiscountRate());
		}else if(economicCase.equals(EconomicScenario.EconomicCase.TRANSITORY_PERIOD)){
			append (sb,Translator.swap ("firstDate") + " : " + es.getFirstDate());
			append (sb,Translator.swap ("lastDate") + " : " + es.getLastDate());
			append (sb,Translator.swap ("discountRate") + " : " + es.getDiscountRate());
		}
				
		if(economicCase.equals(EconomicScenario.EconomicCase.INFINITY_CYCLE_WITH_LAND_OBSERVATION_AT_FIRST_OR_LAST_DATE) & !es.isDiscountRateGiven()){
			append (sb,Translator.swap ("estimatedDiscountRate") + " : " + es.getEstimatedDiscountRate());

		}
		
		
		append (sb,"--------------------------------------");
		append (sb,Translator.swap ("EconomicTextViewer.titleStandDescription"));
		append (sb,"--------------------------------------");
		if(es.getStandDescriptionAtFirstDate() != null){
			append (sb,Translator.swap ("EconomicTextViewer.standDescriptionAtYear") + es.getFirstDate());
			append (sb,"---");
			sb.append(es.getStandDescriptionAtFirstDate().toStringBuffer());
		}else{
			append (sb,Translator.swap ("EconomicTextViewer.foundNoDescriptionAtYear") + es.getFirstDate());
			append (sb,"---");
		}
		
		if(es.getStandDescriptionAtFirstDateInfiniteCycle() != null){
			append (sb,Translator.swap ("EconomicTextViewer.standDescriptionAtYear") + es.getFirstDateInfiniteCycle());
			append (sb,"---");
			sb.append(es.getStandDescriptionAtFirstDateInfiniteCycle().toStringBuffer());
		}
//		}else{
//			append (sb,Translator.swap ("EconomicTextViewer.foundNoDescriptionAtYear") + es.getFirstDateInfiniteCycle());
//		}
		
		if(es.getStandDescriptionAtLastDate() != null){
			append (sb,Translator.swap ("EconomicTextViewer.standDescriptionAtYear") + es.getLastDate());
			append (sb,"---");
			sb.append(es.getStandDescriptionAtLastDate().toStringBuffer());
		}else{
			append (sb,Translator.swap ("EconomicTextViewer.foundNoDescriptionAtYear") + es.getLastDate());
			append (sb,"---");
		}
	
		
		append (sb,"--------------------------------------");
		append (sb,Translator.swap ("EconomicTextViewer.forestValue"));
		append (sb,"--------------------------------------");
		append (sb,Translator.swap ("BAS") + " = " + round(es.getBas()));
		if(!economicCase.equals(EconomicScenario.EconomicCase.TRANSITORY_PERIOD)){
//			append (sb,Translator.swap ("BASF") + " = " + round(es.getBasf()));
			
			if(economicCase.equals(EconomicScenario.EconomicCase.TRANSITORY_PERIOD_PLUS_INFINITY_CYCLE)){
				append (sb,Translator.swap ("FEV0") + " = " + round(es.getInitialFEV()));
				append (sb,Translator.swap ("FEVn") + " = " + round(es.getFinalFEV()));
			}
			append (sb,Translator.swap ("BASI") + " = " + round(es.getBasi()));
		}else{
			append (sb,Translator.swap ("EconomicTextViewer.BASFTransitoryPeriod"));
			append (sb,Translator.swap ("EconomicTextViewer.BASITransitoryPeriod"));
		}
		append (sb,"---");
		append (sb,Translator.swap ("BASExplanation"));
//		append (sb,Translator.swap ("BASFExplanation"));
		append (sb,Translator.swap ("BASIExplanation"));
		append (sb,Translator.swap ("EconomicTextViewer.BASIExplanation2"));


		append (sb,"--------------------------------------");
		append (sb,Translator.swap ("EconomicTextViewer.cashFlows"));
		append (sb,"--------------------------------------");
		append (sb,Translator.swap ("Bt") + " = " + round(es.getBt()));
		append (sb,Translator.swap ("Bm") + " = " + round(es.getBm()));
		append (sb,Translator.swap ("Rt") + " = " + round(es.getRt()));
		append (sb,Translator.swap ("Rm") + " = " + round(es.getRm()));
		append (sb,Translator.swap ("Vt") + " = " + round(es.getVt()));
		append (sb,Translator.swap ("Vm") + " = " + round(es.getVm()));
		
		append (sb,"--------------------------------------");
		append (sb,Translator.swap ("EconomicTextViewer.profitability"));
		append (sb,"--------------------------------------");
		append (sb,Translator.swap ("TIR") + " = " + round(es.getTir()*100));
		if(!economicCase.equals(EconomicScenario.EconomicCase.TRANSITORY_PERIOD)){
//			append (sb,Translator.swap ("TIRF") + " = " + round(es.getTirf()*100));
			append (sb,Translator.swap ("annuity") + " = " + round(es.getForwardAnnuity()));
		}
		append (sb,"---");
		append (sb,Translator.swap ("TIRExplanation"));
		append (sb,Translator.swap ("annuityExplanation"));
		
		append (sb,"--------------------------------------");
		append (sb,Translator.swap ("EconomicTextViewer.titleOperation"));
		append (sb,"--------------------------------------");
		sb.append(es.createBillBookBuffer("\t"));
			
		JTextArea view = new JTextArea (sb.toString ());
		viewport.setView (view);
		
		// restore scrollbar position
		EventQueue.invokeLater (new Runnable () {
			public void run () {
				vBar.setValue (vBarPosition);		
				hBar.setValue (hBarPosition);		
			}
		});
		
	}
	
	private double round (double v){
		int nbDecimal = 2;
		return round (v, nbDecimal);
	}
	private double round (double v, int nbDecimal){
		double coef = Math.pow(10, nbDecimal);
		return Math.round(v*coef)/coef;
	}
	
	private void append(StringBuffer sb, String line){
		sb.append (line);
		sb.append("\n");
	}
	
	/**	From Pilotable interface.
	*/
	@Override
	public JComponent getPilot() {
		ImageIcon icon = IconLoader.getIcon ("help_16.png");
		helpButton = new JButton (icon);
		Tools.setSizeExactly (helpButton, 23, 23);
		helpButton.setToolTipText (Translator.swap ("Shared.help"));
		helpButton.addActionListener (this);
		
		JToolBar toolbar = new JToolBar ();
		toolbar.add (helpButton);
		toolbar.setVisible (true);
		
		return toolbar;
	}

	/**	Used for the settings buttons.
	*/
	@Override
	public void actionPerformed(ActionEvent evt) {
		if (evt.getSource ().equals (helpButton)) {
			Helper.helpFor (this);
		}
	}
}
