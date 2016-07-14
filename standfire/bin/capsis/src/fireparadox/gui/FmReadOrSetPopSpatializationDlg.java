package fireparadox.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import jeeb.lib.util.AmapDialog;
import jeeb.lib.util.Check;
import jeeb.lib.util.JWidthLabel;
import jeeb.lib.util.Translator;

/**
 * FiReadOrSetPopSpatializationDlg - Dialog box to input spatial constraints for each population.
 * Species, size (height, crown base height, crown diameter) and cover (in %) is read from a .popcov file
 * Values for spatial constraints are set in this dialog
 * they can also be read in the same file .popcov file, and can be change in this dialog
 * Called by FireDVOLoader2.
 *
 * @author Ph. Dreyfus - January 2009
 */
public class FmReadOrSetPopSpatializationDlg extends AmapDialog implements ActionListener {
//upgraded for c4.0 - fc - 12.1.2001

	private int nbPop;
	private int numpop;

	private JButton ok;
	private JButton cancel;
	//private JButton help;

	boolean fullDlg;

	private ButtonGroup rd2Group;
	private JRadioButton rd2Indep1;
	private JRadioButton rd2Dmin1;
	private JRadioButton rd2Dmax1;

	private ButtonGroup rd3Group;
	private JRadioButton rd3Indep1;
	private JRadioButton rd3Dmin1;
	private JRadioButton rd3Dmax1;

	private ButtonGroup rd4Group;
	private JRadioButton rd4Indep1;
	private JRadioButton rd4Dmin1;
	private JRadioButton rd4Dmax1;

	private JWidthLabel linePop1Name;
	private JWidthLabel linePop2Name;
	private JWidthLabel linePop3Name;
	private JWidthLabel linePop4Name;

	private double Gibbs1;
	private double Gibbs2;
	private double Gibbs3;
	private double Gibbs4;
	private JTextField fldGibbs1;
	private JTextField fldGibbs2;
	private JTextField fldGibbs3;
	private JTextField fldGibbs4;

	private double Radius1;
	private double Radius2;
	private double Radius3;
	private double Radius4;
	//private JTextField fldRadius1;
	//private JTextField fldRadius2;
	//private JTextField fldRadius3;
	//private JTextField fldRadius4;

	//private double distPopu_1;
	private double distPopi_1;
	private int distWeight_1;
	//private JTextField fldDistPopu_1;
	private JTextField fldDistPopi_1;
	private JTextField fldDistWeight_1;

	private double distPopi_2A;
	private int distWeight_2A;
	private double distPopu_2B;
	private int distWeight_2B;
	private double distPopi_2B;
	private double distPopu_2C;
	private JTextField fldDistPopi_2A;
	private JTextField fldDistWeight_2A;
	private JTextField fldDistPopu_2B;
	private JTextField fldDistWeight_2B;
	private JTextField fldDistPopi_2B;
	private JTextField fldDistPopu_2C;

	private double distPopi_3A;
	private int distWeight_3A;
	private double distPopu_3B;
	private int distWeight_3B;
	private double distPopi_3B;
	private double distPopu_3C;
	private JTextField fldDistPopi_3A;
	private JTextField fldDistWeight_3A;
	private JTextField fldDistPopu_3B;
	private JTextField fldDistWeight_3B;
	private JTextField fldDistPopi_3B;
	private JTextField fldDistPopu_3C;

	private double distPopi_4A;
	private int distWeight_4A;
	private double distPopu_4B;
	private int distWeight_4B;
	private double distPopi_4B;
	private double distPopu_4C;
	private JTextField fldDistPopi_4A;
	private JTextField fldDistWeight_4A;
	private JTextField fldDistPopu_4B;
	private JTextField fldDistWeight_4B;
	private JTextField fldDistPopi_4B;
	private JTextField fldDistPopu_4C;

	private JTextField fldEsp_1;
	private JTextField fldHeight_1;
	private JTextField fldCrownBaseHeight_1;
	private JTextField fldCrownDiameter_1;
	private JTextField fldCoverPct_1;
	private String Esp_1;
	private double Height_1;
	private double CrownBaseHeight_1;
	private double CrownDiameter_1;
	private double CoverPct_1;

	private JTextField fldEsp_2;
	private JTextField fldHeight_2;
	private JTextField fldCrownBaseHeight_2;
	private JTextField fldCrownDiameter_2;
	private JTextField fldCoverPct_2;
	private String Esp_2;
	private double Height_2;
	private double CrownBaseHeight_2;
	private double CrownDiameter_2;
	private double CoverPct_2;

	private JTextField fldEsp_3;
	private JTextField fldHeight_3;
	private JTextField fldCrownBaseHeight_3;
	private JTextField fldCrownDiameter_3;
	private JTextField fldCoverPct_3;
	private String Esp_3;
	private double Height_3;
	private double CrownBaseHeight_3;
	private double CrownDiameter_3;
	private double CoverPct_3;

	private JTextField fldEsp_4;
	private JTextField fldHeight_4;
	private JTextField fldCrownBaseHeight_4;
	private JTextField fldCrownDiameter_4;
	private JTextField fldCoverPct_4;
	private String Esp_4;
	private double Height_4;
	private double CrownBaseHeight_4;
	private double CrownDiameter_4;
	private double CoverPct_4;

	private boolean PopiDEPdePrevPops_2;
	private boolean PopiDEPdePrevPops_3;
	private boolean PopiDEPdePrevPops_4;


// ------------------------------------------------------------------------------------------------------------------------------------->
//               Fonction de lancement du dialogue																		>
// ------------------------------------------------------------------------------------------------------------------------------------->
	public FmReadOrSetPopSpatializationDlg (boolean _fullDlg, int _nbPop,
			String _Esp_1, double _Height_1, double _CrownBaseHeight_1, double _CrownDiameter_1, double _CoverPct_1, double _Gibbs1, double _Radius1, double _distPopi_1, double _distWeight_1,
			String _Esp_2, double _Height_2, double _CrownBaseHeight_2, double _CrownDiameter_2, double _CoverPct_2, double _Gibbs2, double _Radius2, double _distPopi_2A, double _distWeight_2A, double _distPopu_2B, double _distWeight_2B, double _distPopi_2B, double _distPopu_2C,
			String _Esp_3, double _Height_3, double _CrownBaseHeight_3, double _CrownDiameter_3, double _CoverPct_3, double _Gibbs3, double _Radius3, double _distPopi_3A, double _distWeight_3A, double _distPopu_3B, double _distWeight_3B, double _distPopi_3B, double _distPopu_3C,
			String _Esp_4, double _Height_4, double _CrownBaseHeight_4, double _CrownDiameter_4, double _CoverPct_4, double _Gibbs4, double _Radius4, double _distPopi_4A, double _distWeight_4A, double _distPopu_4B, double _distWeight_4B, double _distPopi_4B, double _distPopu_4C
			)
	{

		super ();

		fullDlg = _fullDlg;

		nbPop = _nbPop;

		Esp_1 = _Esp_1;
		Height_1 = _Height_1;
		CrownBaseHeight_1 = _CrownBaseHeight_1;
		CrownDiameter_1 = _CrownDiameter_1;
		CoverPct_1 = _CoverPct_1;
		Gibbs1 = _Gibbs1;
		Radius1 = _Radius1;
		distPopi_1 = _distPopi_1;
		distWeight_1 = (int) _distWeight_1;

		Esp_2 = _Esp_2;
		Height_2 = _Height_2;
		CrownBaseHeight_2 = _CrownBaseHeight_2;
		CrownDiameter_2 = _CrownDiameter_2;
		CoverPct_2 = _CoverPct_2;
		Gibbs2 = _Gibbs2;
		Radius2 = _Radius2;
		distPopi_2A = _distPopi_2A;
		distWeight_2A = (int) _distWeight_2A;
		distPopu_2B = _distPopu_2B;
		distWeight_2B = (int) _distWeight_2B;
		distPopi_2B = _distPopi_2B;
		distPopu_2C = _distPopu_2C;

		Esp_3 = _Esp_3;
		Height_3 = _Height_3;
		CrownBaseHeight_3 = _CrownBaseHeight_3;
		CrownDiameter_3 = _CrownDiameter_3;
		CoverPct_3 = _CoverPct_3;
		Gibbs3 = _Gibbs3;
		Radius3 = _Radius3;
		distPopi_3A = _distPopi_3A;
		distWeight_3A = (int) _distWeight_3A;
		distPopu_3B = _distPopu_3B;
		distWeight_3B = (int) _distWeight_3B;
		distPopi_3B = _distPopi_3B;
		distPopu_3C = _distPopu_3C;

		Esp_4 = _Esp_4;
		Height_4 = _Height_4;
		CrownBaseHeight_4 = _CrownBaseHeight_4;
		CrownDiameter_4 = _CrownDiameter_4;
		CoverPct_4 = _CoverPct_4;
		Gibbs4 = _Gibbs4;
		Radius4 = _Radius4;
		distPopi_4A = _distPopi_4A;
		distWeight_4A = (int) _distWeight_4A;
		distPopu_4B = _distPopu_4B;
		distWeight_4B = (int) _distWeight_4B;
		distPopi_4B = _distPopi_4B;
		distPopu_4C = _distPopu_4C;

		 /*if (distWeight_1 == -5 && fullDlg) distWeight_1 = 5;
		 if (distWeight_1 == -5 && !fullDlg) distWeight_1 = 5;

		 if (distWeight_2A == -5 && fullDlg) distWeight_2A = 5;
		 if (distWeight_2B == -5 && fullDlg) distWeight_2B = 5;
		 if (distWeight_2A == -5 && !fullDlg) distWeight_2A = 5;
		 if (distWeight_2B == -5 && !fullDlg) distWeight_2B = 5;

		 if (distWeight_3A == -5 && fullDlg) distWeight_3A = 5;
		 if (distWeight_3B == -5 && fullDlg) distWeight_3B = 5;
		 if (distWeight_3A == -5 && !fullDlg) distWeight_3A = 10;
		 if (distWeight_3B == -5 && !fullDlg) distWeight_3B = 10;

		 if (distWeight_4A == -5 && fullDlg) distWeight_4A = 5;
		 if (distWeight_4B == -5 && fullDlg) distWeight_4B = 5;
		 if (distWeight_4A == -5 && !fullDlg) distWeight_4A = 15;
		 if (distWeight_4B == -5 && !fullDlg) distWeight_4B = 15;*/

		 distWeight_1 = 5;
		 distWeight_2A = 5;
		 distWeight_2B = 5;
		 distWeight_3A = 5;
		 distWeight_3B = 5;
		 distWeight_4A = 5;
		 distWeight_4B = 5;

		createUI ();
		// location is set by AmapDialog
		pack ();
		show ();
	}

	private void okAction () {

		//if (!Check.isEmpty (fldEsp_1.getText ().trim ())) { Esp_1 = fldEsp_1.getText ().trim (); } else { Esp_1 = "Esp1"; }
		if (!Check.isEmpty (fldHeight_1.getText ().trim ())) { Height_1 = Check.doubleValue (fldHeight_1.getText ().trim ()); } else { Height_1 = 12.00; }
		if (!Check.isEmpty (fldCrownBaseHeight_1.getText ().trim ())) { CrownBaseHeight_1 = Check.doubleValue (fldCrownBaseHeight_1.getText ().trim ()); } else { CrownBaseHeight_1 = 8.00; }
		if (!Check.isEmpty (fldCrownDiameter_1.getText ().trim ())) { CrownDiameter_1 = Check.doubleValue (fldCrownDiameter_1.getText ().trim ()); } else { CrownDiameter_1 = 6.00; }
		if (!Check.isEmpty (fldCoverPct_1.getText ().trim ())) { CoverPct_1 = Check.doubleValue (fldCoverPct_1.getText ().trim ()); } else { CoverPct_1 = 15.00; }
		if (!Check.isEmpty (fldGibbs1.getText ().trim ())) { Gibbs1 = Check.doubleValue (fldGibbs1.getText ().trim ()); } else { Gibbs1 = 0; }
		if (!Check.isEmpty (fldDistPopi_1.getText ().trim ())) { distPopi_1 = Check.doubleValue (fldDistPopi_1.getText ().trim ()); } else { distPopi_1 = -9; }
		if (!Check.isEmpty (fldDistWeight_1.getText ().trim ())) { distWeight_1 = (int) Check.doubleValue (fldDistWeight_1.getText ().trim ()); } else { distWeight_1 = 5; }

		if (nbPop >= 2) {
			//if (!Check.isEmpty (fldEsp_2.getText ().trim ())) { Esp_2 = fldEsp_2.getText ().trim (); } else { Esp_2 = "Esp2"; }
			if (!Check.isEmpty (fldHeight_2.getText ().trim ())) { Height_2 = Check.doubleValue (fldHeight_2.getText ().trim ()); } else { Height_2 = 12.00; }
			if (!Check.isEmpty (fldCrownBaseHeight_2.getText ().trim ())) { CrownBaseHeight_2 = Check.doubleValue (fldCrownBaseHeight_2.getText ().trim ()); } else { CrownBaseHeight_2 = 8.00; }
			if (!Check.isEmpty (fldCrownDiameter_2.getText ().trim ())) { CrownDiameter_2 = Check.doubleValue (fldCrownDiameter_2.getText ().trim ()); } else { CrownDiameter_2 = 6.00; }
			if (!Check.isEmpty (fldCoverPct_2.getText ().trim ())) { CoverPct_2 = Check.doubleValue (fldCoverPct_2.getText ().trim ()); } else { CoverPct_2 = 15.00; }
			if (!Check.isEmpty (fldGibbs2.getText ().trim ())) { Gibbs2 = Check.doubleValue (fldGibbs2.getText ().trim ()); } else { Gibbs2 = 0; }

			if (!Check.isEmpty (fldDistPopi_2A.getText ().trim ())) { distPopi_2A = Check.doubleValue (fldDistPopi_2A.getText ().trim ()); } else { distPopi_2A = -5; }
			if (!Check.isEmpty (fldDistWeight_2A.getText ().trim ())) { distWeight_2A = (int) Check.doubleValue (fldDistWeight_2A.getText ().trim ()); } else { distWeight_2A = 5; }

			if (!Check.isEmpty (fldDistPopu_2B.getText ().trim ())) { distPopu_2B = Check.doubleValue (fldDistPopu_2B.getText ().trim ()); } else { distPopu_2B = -5; }
			if (!Check.isEmpty (fldDistWeight_2B.getText ().trim ())) { distWeight_2B = (int) Check.doubleValue (fldDistWeight_2B.getText ().trim ()); } else { distWeight_2B = 5; }
			if (!Check.isEmpty (fldDistPopi_2B.getText ().trim ())) { distPopi_2B = Check.doubleValue (fldDistPopi_2B.getText ().trim ()); } else { distPopi_2B = -5; }

			if (!Check.isEmpty (fldDistPopu_2C.getText ().trim ())) { distPopu_2C = Check.doubleValue (fldDistPopu_2C.getText ().trim ()); } else { distPopu_2C = -5; }

			if (rd2Indep1.isSelected()) {
				PopiDEPdePrevPops_2 = false;
			} else {
				PopiDEPdePrevPops_2 = true;
			}
		}

		if (nbPop >= 3) {
			//if (!Check.isEmpty (fldEsp_3.getText ().trim ())) { Esp_3 = fldEsp_3.getText ().trim (); } else { Esp_3 = "Esp3"; }
			if (!Check.isEmpty (fldHeight_3.getText ().trim ())) { Height_3 = Check.doubleValue (fldHeight_3.getText ().trim ()); } else { Height_3 = 13.55; }
			if (!Check.isEmpty (fldCrownBaseHeight_3.getText ().trim ())) { CrownBaseHeight_3 = Check.doubleValue (fldCrownBaseHeight_3.getText ().trim ()); } else { CrownBaseHeight_3 = 8.00; }
			if (!Check.isEmpty (fldCrownDiameter_3.getText ().trim ())) { CrownDiameter_3 = Check.doubleValue (fldCrownDiameter_3.getText ().trim ()); } else { CrownDiameter_3 = 6.00; }
			if (!Check.isEmpty (fldCoverPct_3.getText ().trim ())) { CoverPct_3 = Check.doubleValue (fldCoverPct_3.getText ().trim ()); } else { CoverPct_3 = 15.00; }
			if (!Check.isEmpty (fldGibbs3.getText ().trim ())) { Gibbs3 = Check.doubleValue (fldGibbs3.getText ().trim ()); } else { Gibbs3 = 0; }

			if (!Check.isEmpty (fldDistPopi_3A.getText ().trim ())) { distPopi_3A = Check.doubleValue (fldDistPopi_3A.getText ().trim ()); } else { distPopi_3A = -5; }
			if (!Check.isEmpty (fldDistWeight_3A.getText ().trim ())) { distWeight_3A = (int) Check.doubleValue (fldDistWeight_3A.getText ().trim ()); } else { distWeight_3A = 5; }

			if (!Check.isEmpty (fldDistPopu_3B.getText ().trim ())) { distPopu_3B = Check.doubleValue (fldDistPopu_3B.getText ().trim ()); } else { distPopu_3B = -5; }
			if (!Check.isEmpty (fldDistWeight_3B.getText ().trim ())) { distWeight_3B = (int) Check.doubleValue (fldDistWeight_3B.getText ().trim ()); } else { distWeight_3B = 5; }
			if (!Check.isEmpty (fldDistPopi_3B.getText ().trim ())) { distPopi_3B = Check.doubleValue (fldDistPopi_3B.getText ().trim ()); } else { distPopi_3B = -5; }

			if (!Check.isEmpty (fldDistPopu_3C.getText ().trim ())) { distPopu_3C = Check.doubleValue (fldDistPopu_3C.getText ().trim ()); } else { distPopu_3C = -5; }

			if (rd3Indep1.isSelected()) {
				PopiDEPdePrevPops_3 = false;
			} else {
				PopiDEPdePrevPops_3 = true;
			}
		}

		if (nbPop >= 4) {
			//if (!Check.isEmpty (fldEsp_4.getText ().trim ())) { Esp_4 = fldEsp_4.getText ().trim (); } else { Esp_4 = "Esp4"; }
			if (!Check.isEmpty (fldHeight_4.getText ().trim ())) { Height_4 = Check.doubleValue (fldHeight_4.getText ().trim ()); } else { Height_4 = 14.55; }
			if (!Check.isEmpty (fldCrownBaseHeight_4.getText ().trim ())) { CrownBaseHeight_4 = Check.doubleValue (fldCrownBaseHeight_4.getText ().trim ()); } else { CrownBaseHeight_4 = 8.00; }
			if (!Check.isEmpty (fldCrownDiameter_4.getText ().trim ())) { CrownDiameter_4 = Check.doubleValue (fldCrownDiameter_4.getText ().trim ()); } else { CrownDiameter_4 = 6.00; }
			if (!Check.isEmpty (fldCoverPct_4.getText ().trim ())) { CoverPct_4 = Check.doubleValue (fldCoverPct_4.getText ().trim ()); } else { CoverPct_4 = 15.00; }
			if (!Check.isEmpty (fldGibbs4.getText ().trim ())) { Gibbs4 = Check.doubleValue (fldGibbs4.getText ().trim ()); } else { Gibbs4 = 0; }

			if (!Check.isEmpty (fldDistPopi_4A.getText ().trim ())) { distPopi_4A = Check.doubleValue (fldDistPopi_4A.getText ().trim ()); } else { distPopi_4A = -5; }
			if (!Check.isEmpty (fldDistWeight_4A.getText ().trim ())) { distWeight_4A = (int) Check.doubleValue (fldDistWeight_4A.getText ().trim ()); } else { distWeight_4A = 5; }

			if (!Check.isEmpty (fldDistPopu_4B.getText ().trim ())) { distPopu_4B = Check.doubleValue (fldDistPopu_4B.getText ().trim ()); } else { distPopu_4B = -5; }
			if (!Check.isEmpty (fldDistWeight_4B.getText ().trim ())) { distWeight_4B = (int) Check.doubleValue (fldDistWeight_4B.getText ().trim ()); } else { distWeight_4B = 5; }
			if (!Check.isEmpty (fldDistPopi_4B.getText ().trim ())) { distPopi_4B = Check.doubleValue (fldDistPopi_4B.getText ().trim ()); } else { distPopi_4B = -5; }

			if (!Check.isEmpty (fldDistPopu_4C.getText ().trim ())) { distPopu_4C = Check.doubleValue (fldDistPopu_4C.getText ().trim ()); } else { distPopu_4C = -5; }

			if (rd4Indep1.isSelected()) {
				PopiDEPdePrevPops_4 = false;
			} else {
				PopiDEPdePrevPops_4 = true;
			}
		}

		setValidDialog (true);
	}

	public void actionPerformed (ActionEvent evt) {

		if (evt.getSource ().equals (ok)) {
			okAction ();
		} else if (evt.getSource ().equals (cancel)) {
			setValidDialog (false);
		} /*else if (evt.getSource ().equals (help)) {
			System.err.println ("help");
		}*/
	}

	private void setDisp (JTextField value, Number n) {
		if (n instanceof Double && (Double) n == -5.0) {
			value.setText ("");
		} else if (n instanceof Integer && (Integer) n == -5) {
			value.setText ("");
		} else {
			value.setText (""+n);
		}
	}

	/**
	 * Initializes the GUI.
	 */
	private void createUI () {

		Box vertibox = Box.createVerticalBox ();

		int width = 30;

		//  ***************************************************************************:
		JPanel lineCrit = new JPanel (new FlowLayout (FlowLayout.LEFT));
		lineCrit.add (new JWidthLabel ("", width));
		lineCrit.add (new JWidthLabel ("Esp", 165));
		lineCrit.add (new JWidthLabel ("Height", 60));
		lineCrit.add (new JWidthLabel ("CrownBH", 60));
		lineCrit.add (new JWidthLabel ("CrownD", 60));
		lineCrit.add (new JWidthLabel ("Cover (%)", 60));
		lineCrit.add (new JWidthLabel ("Gibbs", 60));
		vertibox.add (lineCrit);

		// Pop 1 ***************************************************************************:
		numpop = 1;
		JPanel linePop1 = new JPanel (new FlowLayout (FlowLayout.LEFT));
		linePop1Name = new JWidthLabel ("Pop1 :", width);
		fldEsp_1 = new JTextField (15);
			fldEsp_1.setText (Esp_1);
		fldHeight_1 = new JTextField (5);
		fldCrownBaseHeight_1 = new JTextField (5);
		fldCrownDiameter_1 = new JTextField (5);
		fldCoverPct_1 = new JTextField (5);
			setDisp(fldHeight_1, Height_1);
			setDisp(fldCrownBaseHeight_1, CrownBaseHeight_1);
			setDisp(fldCrownDiameter_1, CrownDiameter_1);
			setDisp(fldCoverPct_1, CoverPct_1);
		fldGibbs1 = new JTextField (5);
			setDisp(fldGibbs1, Gibbs1);
		fldEsp_1.setEnabled (false);
		linePop1.add (linePop1Name);
		linePop1.add (fldEsp_1);
		linePop1.add (fldHeight_1);
		linePop1.add (fldCrownBaseHeight_1);
		linePop1.add (fldCrownDiameter_1);
		linePop1.add (fldCoverPct_1);
		linePop1.add (fldGibbs1);
		vertibox.add (linePop1);

		JPanel linePop1b = new JPanel (new FlowLayout (FlowLayout.LEFT));
		fldDistPopi_1 = new JTextField (5);
			setDisp(fldDistPopi_1, distPopi_1);
		fldDistWeight_1 = new JTextField (5);
			setDisp(fldDistWeight_1, distWeight_1);
		linePop1b.add (new JWidthLabel ("", width));
		linePop1b.add (new JLabel ("  Min Dist btw Crowns of Pop"+numpop+" (optional) ="));
		linePop1b.add (fldDistPopi_1);
		if (fullDlg) linePop1b.add (new JLabel ("  Increased weight for this rule (1 -> 15) = "));
		if (fullDlg) linePop1b.add (fldDistWeight_1);
		vertibox.add (linePop1b);


		// Pop 2 ***************************************************************************:
		numpop = 2;
		JPanel linePop2 = new JPanel (new FlowLayout (FlowLayout.LEFT));
			linePop2Name = new JWidthLabel ("Pop2 :", width);
			fldEsp_2 = new JTextField (15);
				fldEsp_2.setText (Esp_2);
				fldEsp_2.setEnabled (false);
			fldHeight_2 = new JTextField (5);
			fldCrownBaseHeight_2 = new JTextField (5);
			fldCrownDiameter_2 = new JTextField (5);
			fldCoverPct_2 = new JTextField (5);
				setDisp(fldHeight_2, Height_2);
				setDisp(fldCrownBaseHeight_2, CrownBaseHeight_2);
				setDisp(fldCrownDiameter_2, CrownDiameter_2);
				setDisp(fldCoverPct_2, CoverPct_2);
			if (nbPop < 2) {
				fldHeight_2.setEnabled (false);
				fldCrownBaseHeight_2.setEnabled (false);
				fldCrownDiameter_2.setEnabled (false);
				fldCoverPct_2.setEnabled (false);
			}
			linePop2.add (linePop2Name);
			linePop2.add (fldEsp_2);
			linePop2.add (fldHeight_2);
			linePop2.add (fldCrownBaseHeight_2);
			linePop2.add (fldCrownDiameter_2);
			linePop2.add (fldCoverPct_2);

			fldGibbs2 = new JTextField (5);
				setDisp(fldGibbs2, Gibbs2);
			linePop2.add (fldGibbs2);
		vertibox.add (linePop2);

		JPanel linePop2a = new JPanel (new FlowLayout (FlowLayout.LEFT));
			if (!fullDlg) {
				rd2Indep1 = new JRadioButton ("NO Distance constraint");
			} else {
				rd2Indep1 = new JRadioButton ("NO Distance constraint to other Pops");
			}
			fldDistPopi_2A = new JTextField (5);
			fldDistWeight_2A = new JTextField (5);
				setDisp(fldDistPopi_2A, distPopi_2A);
				setDisp(fldDistWeight_2A, distWeight_2A);
			linePop2a.add (new JWidthLabel ("", width));
			linePop2a.add (rd2Indep1);
			if (fullDlg) linePop2a.add (new JLabel ("  Min Dist btw Crowns of Pop"+numpop+" (optional) ="));
			if (fullDlg) linePop2a.add (fldDistPopi_2A);
			if (fullDlg) linePop2a.add (new JLabel ("  Increased weight for this rule (1 ->15) = "));
			if (fullDlg) linePop2a.add (fldDistWeight_2A);
		vertibox.add (linePop2a);

		JPanel linePop2b = new JPanel (new FlowLayout (FlowLayout.LEFT));
			if (!fullDlg) {
				rd2Dmin1 = new JRadioButton ("MIN DIST btw CROWNS of this Pop and previous Pops (same min. dist. than for Pop1)");
			} else {
				rd2Dmin1 = new JRadioButton ("MIN DIST to CROWNS of previous Pops");
			}
			fldDistPopu_2B = new JTextField (5);
			fldDistWeight_2B = new JTextField (5);
			fldDistPopi_2B = new JTextField (5);
				setDisp(fldDistPopu_2B, distPopu_2B);
				setDisp(fldDistWeight_2B, distWeight_2B);
				setDisp(fldDistPopi_2B, distPopi_2B);
			linePop2b.add (new JWidthLabel ("", width));
			linePop2b.add (rd2Dmin1);
			if (fullDlg) linePop2b.add (new JLabel ("  Min Dist btw Crowns of previous Pops ="));
			if (fullDlg) linePop2b.add (fldDistPopu_2B);
			if (fullDlg) linePop2b.add (new JLabel ("  Increased weight for this rule (1 ->15) = "));
			if (fullDlg) linePop2b.add (fldDistWeight_2B);
			if (fullDlg) linePop2b.add (new JLabel ("  Min Dist btw Crowns of Pop"+numpop+" (optional) ="));
			if (fullDlg) linePop2b.add (fldDistPopi_2B);
		vertibox.add (linePop2b);

		JPanel linePop2c = new JPanel (new FlowLayout (FlowLayout.LEFT));
			rd2Dmax1 = new JRadioButton ("MAX DIST btw Pop"+numpop+" crowns and Pop1 STEMS");
			fldDistPopu_2C = new JTextField (5);
				setDisp(fldDistPopu_2C, distPopu_2C);
			linePop2c.add (new JWidthLabel ("", width));
			linePop2c.add (rd2Dmax1);
			linePop2c.add (new JLabel ("   Max Dist to Stems of Pop1 ="));
			linePop2c.add (fldDistPopu_2C);
		if (fullDlg) vertibox.add (linePop2c);

		rd2Group = new ButtonGroup ();
			if (distPopu_2B == -5 && distPopu_2C == -5) {
				rd2Indep1.setSelected(true);
			} else if (distPopu_2B != -5) {
				rd2Dmin1.setSelected(true);
			} else {
				rd2Dmax1.setSelected(true);
			}
		rd2Group.add (rd2Indep1);
		rd2Group.add (rd2Dmin1);
		rd2Group.add (rd2Dmax1);

		if (nbPop < 2) {
			rd2Indep1.setEnabled (false);
			rd2Dmin1.setEnabled (false);
			rd2Dmax1.setEnabled (false);
			fldGibbs2.setEnabled (false);
			fldDistPopi_2A.setEnabled (false);
			fldDistWeight_2A.setEnabled (false);
			fldDistPopu_2B.setEnabled (false);
			fldDistWeight_2B.setEnabled (false);
			fldDistPopi_2B.setEnabled (false);
			fldDistPopu_2C.setEnabled (false);
		}


		// Pop 3 ***************************************************************************:
		numpop = 3;
		JPanel linePop3 = new JPanel (new FlowLayout (FlowLayout.LEFT));
			linePop3Name = new JWidthLabel ("Pop3 :", width);
			fldEsp_3 = new JTextField (15);
				fldEsp_3.setText (Esp_3);
				fldEsp_3.setEnabled (false);
			fldHeight_3 = new JTextField (5);
			fldCrownBaseHeight_3 = new JTextField (5);
			fldCrownDiameter_3 = new JTextField (5);
			fldCoverPct_3 = new JTextField (5);
				setDisp(fldHeight_3, Height_3);
				setDisp(fldCrownBaseHeight_3, CrownBaseHeight_3);
				setDisp(fldCrownDiameter_3, CrownDiameter_3);
				setDisp(fldCoverPct_3, CoverPct_3);
			if (nbPop < 3) {
				fldHeight_3.setEnabled (false);
				fldCrownBaseHeight_3.setEnabled (false);
				fldCrownDiameter_3.setEnabled (false);
				fldCoverPct_3.setEnabled (false);
			}
			linePop3.add (linePop3Name);
			linePop3.add (fldEsp_3);
			linePop3.add (fldHeight_3);
			linePop3.add (fldCrownBaseHeight_3);
			linePop3.add (fldCrownDiameter_3);
			linePop3.add (fldCoverPct_3);

			fldGibbs3 = new JTextField (5);
				setDisp(fldGibbs3, Gibbs3);
			linePop3.add (fldGibbs3);
		vertibox.add (linePop3);

		JPanel linePop3a = new JPanel (new FlowLayout (FlowLayout.LEFT));
			if (!fullDlg) {
				rd3Indep1 = new JRadioButton ("NO Distance constraint");
			} else {
				rd3Indep1 = new JRadioButton ("NO Distance constraint to other Pops");
			}
			fldDistPopi_3A = new JTextField (5);
			fldDistWeight_3A = new JTextField (5);
				setDisp(fldDistPopi_3A, distPopi_3A);
				setDisp(fldDistWeight_3A, distWeight_3A);
			linePop3a.add (new JWidthLabel ("", width));
			linePop3a.add (rd3Indep1);
			if (fullDlg) linePop3a.add (new JLabel ("  Min Dist btw Crowns of Pop"+numpop+" (optional) ="));
			if (fullDlg) linePop3a.add (fldDistPopi_3A);
			if (fullDlg) linePop3a.add (new JLabel ("  Increased weight for this rule (1 ->15) = "));
			if (fullDlg) linePop3a.add (fldDistWeight_3A);
		vertibox.add (linePop3a);

		JPanel linePop3b = new JPanel (new FlowLayout (FlowLayout.LEFT));
			if (!fullDlg) {
				rd3Dmin1 = new JRadioButton ("MIN DIST btw CROWNS of this Pop and previous Pops (same min. dist. than for Pop1)");
			} else {
				rd3Dmin1 = new JRadioButton ("MIN DIST to CROWNS of previous Pops");
			}
			fldDistPopu_3B = new JTextField (5);
			fldDistWeight_3B = new JTextField (5);
			fldDistPopi_3B = new JTextField (5);
				setDisp(fldDistPopu_3B, distPopu_3B);
				setDisp(fldDistWeight_3B, distWeight_3B);
				setDisp(fldDistPopi_3B, distPopi_3B);
			linePop3b.add (new JWidthLabel ("", width));
			linePop3b.add (rd3Dmin1);
			if (fullDlg) linePop3b.add (new JLabel ("  Min Dist btw Crowns of previous Pops ="));
			if (fullDlg) linePop3b.add (fldDistPopu_3B);
			if (fullDlg) linePop3b.add (new JLabel ("  Increased weight for this rule (1 ->15) = "));
			if (fullDlg) linePop3b.add (fldDistWeight_3B);
			if (fullDlg) linePop3b.add (new JLabel ("  Min Dist btw Crowns of Pop"+numpop+" (optional) ="));
			if (fullDlg) linePop3b.add (fldDistPopi_3B);
		vertibox.add (linePop3b);

		JPanel linePop3c = new JPanel (new FlowLayout (FlowLayout.LEFT));
			rd3Dmax1 = new JRadioButton ("MAX DIST btw Pop"+numpop+" crowns and Pop1 STEMS");
			fldDistPopu_3C = new JTextField (5);
				setDisp(fldDistPopu_3C, distPopu_3C);
			linePop3c.add (new JWidthLabel ("", width));
			linePop3c.add (rd3Dmax1);
			linePop3c.add (new JLabel ("   Max Dist to Stems of Pop1 ="));
			linePop3c.add (fldDistPopu_3C);
		if (fullDlg) vertibox.add (linePop3c);

		rd3Group = new ButtonGroup ();
			if (distPopu_3B == -5 && distPopu_3C == -5) {
				rd3Indep1.setSelected(true);
			} else if (distPopu_3B != -5) {
				rd3Dmin1.setSelected(true);
			} else {
				rd3Dmax1.setSelected(true);
			}
		rd3Group.add (rd3Indep1);
		rd3Group.add (rd3Dmin1);
		rd3Group.add (rd3Dmax1);

		if (nbPop < 3) {
			rd3Indep1.setEnabled (false);
			rd3Dmin1.setEnabled (false);
			rd3Dmax1.setEnabled (false);
			fldGibbs3.setEnabled (false);
			fldDistPopi_3A.setEnabled (false);
			fldDistWeight_3A.setEnabled (false);
			fldDistPopu_3B.setEnabled (false);
			fldDistWeight_3B.setEnabled (false);
			fldDistPopi_3B.setEnabled (false);
			fldDistPopu_3C.setEnabled (false);
		}


		// Pop 4 ***************************************************************************:
		numpop = 4;
		JPanel linePop4 = new JPanel (new FlowLayout (FlowLayout.LEFT));
			linePop4Name = new JWidthLabel ("Pop4 :", width);
			fldEsp_4 = new JTextField (15);
				fldEsp_4.setText (Esp_4);
				fldEsp_4.setEnabled (false);
			fldHeight_4 = new JTextField (5);
			fldCrownBaseHeight_4 = new JTextField (5);
			fldCrownDiameter_4 = new JTextField (5);
			fldCoverPct_4 = new JTextField (5);
				setDisp(fldHeight_4, Height_4);
				setDisp(fldCrownBaseHeight_4, CrownBaseHeight_4);
				setDisp(fldCrownDiameter_4, CrownDiameter_4);
				setDisp(fldCoverPct_4, CoverPct_4);
			if (nbPop < 4) {
				fldHeight_4.setEnabled (false);
				fldCrownBaseHeight_4.setEnabled (false);
				fldCrownDiameter_4.setEnabled (false);
				fldCoverPct_4.setEnabled (false);
			}
			linePop4.add (linePop4Name);
			linePop4.add (fldEsp_4);
			linePop4.add (fldHeight_4);
			linePop4.add (fldCrownBaseHeight_4);
			linePop4.add (fldCrownDiameter_4);
			linePop4.add (fldCoverPct_4);

			fldGibbs4 = new JTextField (5);
				setDisp(fldGibbs4, Gibbs4);
			linePop4.add (fldGibbs4);
		vertibox.add (linePop4);

		JPanel linePop4a = new JPanel (new FlowLayout (FlowLayout.LEFT));
			if (!fullDlg) {
				rd4Indep1 = new JRadioButton ("NO Distance constraint");
			} else {
				rd4Indep1 = new JRadioButton ("NO Distance constraint to other Pops");
			}
			fldDistPopi_4A = new JTextField (5);
			fldDistWeight_4A = new JTextField (5);
				setDisp(fldDistPopi_4A, distPopi_4A);
				setDisp(fldDistWeight_4A, distWeight_4A);
			linePop4a.add (new JWidthLabel ("", width));
			linePop4a.add (rd4Indep1);
			if (fullDlg) linePop4a.add (new JLabel ("  Min Dist btw Crowns of Pop"+numpop+" (optional) ="));
			if (fullDlg) linePop4a.add (fldDistPopi_4A);
			if (fullDlg) linePop4a.add (new JLabel ("  Increased weight for this rule (1 ->15) = "));
			if (fullDlg) linePop4a.add (fldDistWeight_4A);
		vertibox.add (linePop4a);

		JPanel linePop4b = new JPanel (new FlowLayout (FlowLayout.LEFT));
			if (!fullDlg) {
				rd4Dmin1 = new JRadioButton ("MIN DIST btw CROWNS of this Pop and previous Pops (same min. dist. than for Pop1)");
			} else {
				rd4Dmin1 = new JRadioButton ("MIN DIST to CROWNS of previous Pops");
			}
			fldDistPopu_4B = new JTextField (5);
			fldDistWeight_4B = new JTextField (5);
			fldDistPopi_4B = new JTextField (5);
				setDisp(fldDistPopu_4B, distPopu_4B);
				setDisp(fldDistWeight_4B, distWeight_4B);
				setDisp(fldDistPopi_4B, distPopi_4B);
			linePop4b.add (new JWidthLabel ("", width));
			linePop4b.add (rd4Dmin1);
			if (fullDlg) linePop4b.add (new JLabel ("  Min Dist btw Crowns of previous Pops ="));
			if (fullDlg) linePop4b.add (fldDistPopu_4B);
			if (fullDlg) linePop4b.add (new JLabel ("  Increased weight for this rule (1 ->15) = "));
			if (fullDlg) linePop4b.add (fldDistWeight_4B);
			if (fullDlg) linePop4b.add (new JLabel ("  Min Dist btw Crowns of Pop"+numpop+" (optional) ="));
			if (fullDlg) linePop4b.add (fldDistPopi_4B);
		vertibox.add (linePop4b);

		JPanel linePop4c = new JPanel (new FlowLayout (FlowLayout.LEFT));
			rd4Dmax1 = new JRadioButton ("MAX DIST btw Pop"+numpop+" crowns and Pop1 STEMS");
			fldDistPopu_4C = new JTextField (5);
				setDisp(fldDistPopu_4C, distPopu_4C);
			linePop4c.add (new JWidthLabel ("", width));
			linePop4c.add (rd4Dmax1);
			linePop4c.add (new JLabel ("   Max Dist to Stems of Pop1 ="));
			linePop4c.add (fldDistPopu_4C);
		if (fullDlg) vertibox.add (linePop4c);

		rd4Group = new ButtonGroup ();
			if (distPopu_4B == -5 && distPopu_4C == -5) {
				rd4Indep1.setSelected(true);
			} else if (distPopu_4B != -5) {
				rd4Dmin1.setSelected(true);
			} else {
				rd4Dmax1.setSelected(true);
			}
		rd4Group.add (rd4Indep1);
		rd4Group.add (rd4Dmin1);
		rd4Group.add (rd4Dmax1);

		if (nbPop < 4) {
			rd4Indep1.setEnabled (false);
			rd4Dmin1.setEnabled (false);
			rd4Dmax1.setEnabled (false);
			fldGibbs4.setEnabled (false);
			fldDistPopi_4A.setEnabled (false);
			fldDistWeight_4A.setEnabled (false);
			fldDistPopu_4B.setEnabled (false);
			fldDistWeight_4B.setEnabled (false);
			fldDistPopi_4B.setEnabled (false);
			fldDistPopu_4C.setEnabled (false);
		}


		JPanel pControl = new JPanel (new FlowLayout (FlowLayout.RIGHT));
		ok = new JButton (Translator.swap ("Shared.ok"));
		cancel = new JButton (Translator.swap ("Shared.cancel"));
		//help = new JButton (Translator.swap ("Shared.help"));
		pControl.add (ok);
		pControl.add (cancel);
		//pControl.add (help);
		ok.addActionListener (this);
		cancel.addActionListener (this);
		//help.addActionListener (this);

		setDefaultButton (ok);	// from AmapDialog

		getContentPane ().setLayout (new BorderLayout ());
		getContentPane ().add (vertibox, "North");
		getContentPane ().add (pControl, "South");

		//setTitle (Translator.swap ("FiReadOrSetPopSpatializationDlg.spatialConstraints"));
		setTitle ("Spatial Rules & Constraints");
		
		setModal (true);
	}

	public double getGibbs1 () { return Gibbs1; }
	public double getGibbs2 () { return Gibbs2; }
	public double getGibbs3 () { return Gibbs3; }
	public double getGibbs4 () { return Gibbs4; }

	public boolean isPopiDEPdePrevPops_2 () { return PopiDEPdePrevPops_2; }
	public boolean isPopiDEPdePrevPops_3 () { return PopiDEPdePrevPops_3; }
	public boolean isPopiDEPdePrevPops_4 () { return PopiDEPdePrevPops_4; }

	public double getDistPopi_1 () { return distPopi_1; }
	public double getDistWeight_1 () { return distWeight_1; }

	public double getDistPopi_2A () { return distPopi_2A; }
	public double getDistWeight_2A () { return distWeight_2A; }
	public double getDistPopu_2B () { return distPopu_2B; }
	public double getDistWeight_2B () { return distWeight_2B; }
	public double getDistPopi_2B () { return distPopi_2B; }
	public double getDistPopu_2C () { return distPopu_2C; }

	public double getDistPopi_3A () { return distPopi_3A; }
	public double getDistWeight_3A () { return distWeight_3A; }
	public double getDistPopu_3B () { return distPopu_3B; }
	public double getDistWeight_3B () { return distWeight_3B; }
	public double getDistPopi_3B () { return distPopi_3B; }
	public double getDistPopu_3C () { return distPopu_3C; }

	public double getDistPopi_4A () { return distPopi_4A; }
	public double getDistWeight_4A () { return distWeight_4A; }
	public double getDistPopu_4B () { return distPopu_4B; }
	public double getDistWeight_4B () { return distWeight_4B; }
	public double getDistPopi_4B () { return distPopi_4B; }
	public double getDistPopu_4C () { return distPopu_4C; }

	//public String getEsp_1 () { return Esp_1; }
	public double getHeight_1 () { return Height_1; }
	public double getCrownBaseHeight_1 () { return CrownBaseHeight_1; }
	public double getCrownDiameter_1 () { return CrownDiameter_1; }
	public double getCoverPct_1 () { return CoverPct_1; }

	//public String getEsp_2 () { return Esp_2; }
	public double getHeight_2 () { return Height_2; }
	public double getCrownBaseHeight_2 () { return CrownBaseHeight_2; }
	public double getCrownDiameter_2 () { return CrownDiameter_2; }
	public double getCoverPct_2 () { return CoverPct_2; }

	//public String getEsp_3 () { return Esp_3; }
	public double getHeight_3 () { return Height_3; }
	public double getCrownBaseHeight_3 () { return CrownBaseHeight_3; }
	public double getCrownDiameter_3 () { return CrownDiameter_3; }
	public double getCoverPct_3 () { return CoverPct_3; }

	//public String getEsp_4 () { return Esp_4; }
	public double getHeight_4 () { return Height_4; }
	public double getCrownBaseHeight_4 () { return CrownBaseHeight_4; }
	public double getCrownDiameter_4 () { return CrownDiameter_4; }
	public double getCoverPct_4 () { return CoverPct_4; }

}

