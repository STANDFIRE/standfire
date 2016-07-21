package fireparadox.gui.plantpattern;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import jeeb.lib.util.AmapDialog;
import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.JWidthLabel;
import jeeb.lib.util.LinePanel;
import jeeb.lib.util.Translator;
import capsis.commongui.util.Helper;
import fireparadox.model.plant.fmgeom.FmGeom;
import fireparadox.model.plant.fmgeom.FmGeomDiameter;

/**
 *
 * @author S. Griffon - May 2007
 */
public class FmPatternEditor extends AmapDialog implements ActionListener {

	//private FiModel model;
	private JButton ok;
	private JButton cancel;
	private JButton help;
	private JButton add;
	private JButton remove;
	private JButton reset;
	private JTable listSupDiameter;
	private JTable listInfDiameter;
	private JScrollPane scroll1;
	private JScrollPane scroll2;
	private JTextField infoField;
	private JTextField aliasField;
	private JTextField diamMaxField;
	private JTextField heightDiamSupField;
	private JTextField heightDiamInfField;
	private JTextField valueDiamSupField;
	private JTextField valueDiamInfField;
	private JButton addDiamSupButton;
	private JButton addDiamInfButton;
	private JButton delDiamSupButton;
	private JButton delDiamInfButton;
	private String criteria;
	private FmGeom currentPattern;
	private FmGeom currentPatternClone;
	private NumberFormat numberFormat;
	private FmPattern2DPanel pattern2DPanel;


	/**	Constructor.
	 */
	public FmPatternEditor (String criteria, FmGeom f) {
		super ();
		this.criteria = criteria;
		currentPattern = f;
		currentPatternClone = f.clone ();
		currentPatternClone.setId (currentPattern.getId ());
		numberFormat = NumberFormat.getInstance ();
		numberFormat.setMinimumFractionDigits (0);
		numberFormat.setMaximumFractionDigits (2);
		createUI ();

		updateDiameterList ();

		// location is set by AmapDialog
		pack ();

		show ();


		//initFromPattern ();
	}

	public FmGeom getCurrentPattern () {
		return currentPattern;
	}

	//	Ok was hit
	//
	private void okAction () {
		if(isAllRight ())
			setValidDialog (true);
	}

	private boolean isAllRight () {
		if(listSupDiameter.isEditing () || listInfDiameter.isEditing ()) {
			JOptionPane.showMessageDialog (this, Translator.swap ("FiPatternEditor.tableDiameterIsEditing"));
			return false;
		}
		modifyAlias();
		return modifyMaximumDiameterValue();
	}

	private void reset () {
		currentPattern = currentPatternClone.clone ();
		currentPattern.setId(currentPatternClone.getId ());
		diamMaxField.setText (numberFormat.format (currentPattern.getHDMax ()));
		updateDiameterList ();
		pattern2DPanel.setPattern (currentPattern);
	}

	/**	Actions on the buttons
	 */
	public void actionPerformed (ActionEvent evt) {

		if (evt.getSource ().equals (ok)) {
			okAction ();
		} else if (evt.getSource ().equals (cancel)) {
			setValidDialog (false);
		} else if (evt.getSource ().equals (reset)) {
			reset ();
		} else if (evt.getSource ().equals (help)) {
			Helper.helpFor (this);
		} else if (evt.getSource ().equals (addDiamSupButton) || evt.getSource ().equals (heightDiamSupField) || evt.getSource ().equals (valueDiamSupField)) {
			addSuperiorDiameterEntry ();
		} else if (evt.getSource ().equals (addDiamInfButton) || evt.getSource ().equals (heightDiamInfField) || evt.getSource ().equals (valueDiamInfField)) {
			addInferiorDiameterEntry ();
		} else if (evt.getSource ().equals (delDiamSupButton)) {
			removeSuperiorDiameterEntry ();
		} else if (evt.getSource ().equals (delDiamInfButton)) {
			removeInferiorDiameterEntry ();
		} else if (evt.getSource ().equals (diamMaxField)) {
			modifyMaximumDiameterValue ();
		} else if (evt.getSource ().equals (aliasField)) {
			modifyAlias();
		}
	}

	private void modifyAlias() {
		currentPattern.setAlias (aliasField.getText ());
		infoField.setText (currentPattern.getName ());
	}

	private boolean modifyMaximumDiameterValue ()	{
		if(diamMaxField.getText ().equals ("") ) {
			JOptionPane.showMessageDialog (this, Translator.swap ("FiPatternEditor.diamMaxFieldMustBeAPercentage"));
			return false;
		}

		double w = Double.valueOf (diamMaxField.getText ());

		if(w>=0 && w<=100) {
			currentPattern.setHDMax (w);
		} else {
			JOptionPane.showMessageDialog (this, Translator.swap ("FiPatternEditor.diamMaxFieldMustBeAPercentage"));
			return false;
		}

		infoField.setText (currentPattern.getName ());
		//update the previewer
		pattern2DPanel.reset ();
		return true;

	}

	//add a superior diameter in the current FiPattern
	private void addSuperiorDiameterEntry () {

		if(heightDiamSupField.getText ().equals ("") || valueDiamSupField.getText ().equals ("")) {
			JOptionPane.showMessageDialog (this, Translator.swap ("FiPatternEditor.heightDiamSupFieldMustBeAPercentage"));
			return;
		}

		double h = Double.valueOf (heightDiamSupField.getText ());
		double w = Double.valueOf (valueDiamSupField.getText ());

		if(h>=0 && h<=100&& w>=0 && w<=100) {
			currentPattern.addDiametersSuperior (new FmGeomDiameter (h,w));
		} else {
			JOptionPane.showMessageDialog (this, Translator.swap ("FiPatternEditor.heightDiamSupFieldMustBeAPercentage"));
			return;
		}
		heightDiamSupField.setText ("");
		valueDiamSupField.setText ("");

		updateDiameterList ();
	}

	private void addInferiorDiameterEntry () {

		if(heightDiamInfField.getText ().equals ("") || valueDiamInfField.getText ().equals ("")) {
			JOptionPane.showMessageDialog (this, Translator.swap ("FiPatternEditor.heightDiamInfFieldMustBeAPercentage"));
			return;
		}

		double h = Double.valueOf (heightDiamInfField.getText ());
		double w = Double.valueOf (valueDiamInfField.getText ());

		if(h>=0 && h<=100&& w>=0 && w<=100) {
			currentPattern.addDiametersInferior (new FmGeomDiameter (h,w));
		} else {
			JOptionPane.showMessageDialog (this, Translator.swap ("FiPatternEditor.heightDiamInfFieldMustBeAPercentage"));
			return;
		}
		heightDiamInfField.setText ("");
		valueDiamInfField.setText ("");

		updateDiameterList ();
	}

	private void removeInferiorDiameterEntry () {
		int [] selRow  = listInfDiameter.getSelectedRows ();
		Object [] arrayObj = currentPattern.getDiametersInferior ().toArray ();

		for(int row : selRow) {
			currentPattern.getDiametersInferior ().remove (arrayObj[row]);
		}

		updateDiameterList ();
	}

	private void removeSuperiorDiameterEntry () {
		int [] selRow  = listSupDiameter.getSelectedRows ();
		Object [] arrayObj = currentPattern.getDiametersSuperior ().toArray ();

		for(int row : selRow) {
			currentPattern.getDiametersSuperior ().remove (arrayObj[row]);
		}

		updateDiameterList ();
	}

	private void modifyInferiorDiameterEntry (int row, String height , String width) {
		Object [] arrayObj = currentPattern.getDiametersInferior ().toArray ();
		FmGeomDiameter fdiam = (FmGeomDiameter)arrayObj[row];

		if(!height.equals ("")) {
			double h = Double.valueOf (height);
			if(h>=0 && h<=100) {
				fdiam.setHeight (h);
			} else {
				JOptionPane.showMessageDialog (this, Translator.swap ("FiPatternEditor.heightDiamInfFieldMustBeAPercentage"));
				return;
			}
		}
		if(!width.equals ("")) {
			double w = Double.valueOf (width);
			if(w>=0 && w<=100) {
				fdiam.setWidth (w);
			} else {
				JOptionPane.showMessageDialog (this, Translator.swap ("FiPatternEditor.heightDiamInfFieldMustBeAPercentage"));
				return;
			}
		}
		updateDiameterList ();
	}

	private void modifySuperiorDiameterEntry (int row, String height , String width) {
		Object [] arrayObj = currentPattern.getDiametersSuperior ().toArray ();
		FmGeomDiameter fdiam = (FmGeomDiameter)arrayObj[row];

		if(!height.equals ("")) {
			double h = Double.valueOf (height);
			if(h>=0 && h<=100) {
				fdiam.setHeight (h);
			} else {
				JOptionPane.showMessageDialog (this, Translator.swap ("FiPatternEditor.heightDiamSupFieldMustBeAPercentage"));
				return;
			}
		}
		if(!width.equals ("")) {
			double w = Double.valueOf (width);
			if(w>=0 && w<=100) {
				fdiam.setWidth (w);
			} else {
				JOptionPane.showMessageDialog (this, Translator.swap ("FiPatternEditor.heightDiamSupFieldMustBeAPercentage"));
				return;
			}
		}
		updateDiameterList ();
	}


	//	Initialize the GUI.
	//
	private void createUI () {


		ColumnPanel leftPanel = new ColumnPanel ();
		//CRITERIA PANEL
		ColumnPanel criteriaPanel = new ColumnPanel ();

		LinePanel l1 = new LinePanel ();
		l1.add (new JWidthLabel (Translator.swap ("FiPatternEditor.key")+" :", 80));
		JTextField keyField = new JTextField (criteria);
		keyField.setEditable (false);
		l1.add (keyField);
		l1.addStrut0 ();

		LinePanel infoPanel = new LinePanel ();

		infoPanel.add (new JWidthLabel (Translator.swap ("FiPatternEditor.name")+" :", 80));
		infoField = new JTextField (criteria.length ());
		infoField.setEditable (false);
		infoPanel.add (infoField);
		infoPanel.addStrut0 ();

		LinePanel aliasPanel = new LinePanel ();
		aliasPanel.add (new JWidthLabel (Translator.swap ("FiPatternEditor.alias")+" :", 80));
		aliasField = new JTextField (criteria.length ());
		aliasField.setText (currentPattern.getAlias ());
		aliasField.addActionListener (this);
		aliasPanel.add (aliasField);
		aliasPanel.addStrut0 ();

		criteriaPanel.add (l1);
		criteriaPanel.add (infoPanel);
		criteriaPanel.add (aliasPanel);

		TitledBorder tborder1 = new TitledBorder (criteriaPanel.getBorder (), Translator.swap ("FiPatternEditor.info"));
		criteriaPanel.setBorder (tborder1);

		leftPanel.add (criteriaPanel);

		LinePanel ldmax = new LinePanel ();
		ldmax.add (new JWidthLabel (Translator.swap ("FiPatternEditor.diamMax"), 30));
		diamMaxField = new JTextField (numberFormat.format (currentPattern.getHDMax ()));
		diamMaxField.addActionListener (this);
		ldmax.add (diamMaxField);
		ldmax.add (new JWidthLabel (" % ", 10));
		leftPanel.add (ldmax);


		//Diameter sup PANEL
		ColumnPanel diamSupPanel = new ColumnPanel ();
		LinePanel l2 = new LinePanel ();
		l2.add (new JWidthLabel (Translator.swap ("FiPatternEditor.heightDiam")+" :", 80));
		heightDiamSupField = new JTextField (4);
		heightDiamSupField.addActionListener (this);
		l2.add (heightDiamSupField);
		l2.add (new JWidthLabel (" % ", 10));
		l2.addStrut0 ();
		l2.add (new JWidthLabel (Translator.swap ("FiPatternEditor.valueDiam")+" :", 80));
		valueDiamSupField = new JTextField (4);
		valueDiamSupField.addActionListener (this);
		l2.add (valueDiamSupField);
		l2.add (new JWidthLabel (" % ", 10));
		l2.addStrut0 ();
		addDiamSupButton = new JButton (Translator.swap ("FiPatternEditor.addDiamButton"));
		addDiamSupButton.addActionListener (this);
		l2.add (addDiamSupButton);

		diamSupPanel.add (l2);

		LinePanel lpscroll1 = new LinePanel ();
		scroll1 = new JScrollPane ();
		scroll1.setPreferredSize (new Dimension (350,150));
		lpscroll1.add (scroll1);
		delDiamSupButton = new JButton (Translator.swap ("FiPatternEditor.delDiamButton"));
		delDiamSupButton.addActionListener (this);
		lpscroll1.add (delDiamSupButton);

		diamSupPanel.add (lpscroll1);

		TitledBorder tborder2 = new TitledBorder (diamSupPanel.getBorder (), Translator.swap ("FiPatternEditor.superiorPart"));
		diamSupPanel.setBorder (tborder2);

		leftPanel.add (diamSupPanel);


		//Diameter info PANEL
		ColumnPanel diamInfPanel = new ColumnPanel ();
		LinePanel l3 = new LinePanel ();
		l3.add (new JWidthLabel (Translator.swap ("FiPatternEditor.heightDiam")+" :", 80));
		heightDiamInfField = new JTextField (4);
		heightDiamInfField.addActionListener (this);
		l3.add (heightDiamInfField);
		l3.add (new JWidthLabel (" % ", 10));
		l3.addStrut0 ();
		l3.add (new JWidthLabel (Translator.swap ("FiPatternEditor.valueDiam")+" :", 80));
		valueDiamInfField = new JTextField (4);
		valueDiamInfField.addActionListener (this);
		l3.add (valueDiamInfField);
		l3.add (new JWidthLabel (" % ", 10));
		l3.addStrut0 ();
		addDiamInfButton = new JButton (Translator.swap ("FiPatternEditor.addDiamButton"));
		addDiamInfButton.addActionListener (this);
		l3.add (addDiamInfButton);

		diamInfPanel.add (l3);

		LinePanel lpscroll2 = new LinePanel ();
		scroll2 = new JScrollPane ();
		scroll2.setPreferredSize (new Dimension (350,150));
		lpscroll2.add (scroll2);
		delDiamInfButton = new JButton (Translator.swap ("FiPatternEditor.delDiamButton"));
		delDiamInfButton.addActionListener (this);
		lpscroll2.add (delDiamInfButton);

		diamInfPanel.add (lpscroll2);

		TitledBorder tborder3 = new TitledBorder (diamInfPanel.getBorder (), Translator.swap ("FiPatternEditor.inferiorPart"));
		diamInfPanel.setBorder (tborder3);

		leftPanel.add (diamInfPanel);



		pattern2DPanel = new FmPattern2DPanel (currentPattern);
		pattern2DPanel.setPreferredSize (new Dimension (250,500));
		TitledBorder tborder4 = new TitledBorder (pattern2DPanel.getBorder (), Translator.swap ("FiPatternEditor.previewer"));
		pattern2DPanel.setBorder (tborder4);

		//Control Panel
		LinePanel bottomPanel = new LinePanel ();
		JPanel resetPanel = new JPanel ();
		resetPanel.setLayout (new FlowLayout (FlowLayout.LEFT));
		reset = new JButton (Translator.swap ("FiPatternEditor.reset"));
		reset.addActionListener (this);
		resetPanel.add (reset);
		JPanel controlPanel = new JPanel ();
		controlPanel.setLayout (new FlowLayout (FlowLayout.RIGHT));
		ok = new JButton (Translator.swap ("Shared.ok"));
		ok.addActionListener (this);
		controlPanel.add (ok);
		cancel = new JButton (Translator.swap ("Shared.cancel"));
		cancel.addActionListener (this);
		controlPanel.add (cancel);
		help = new JButton (Translator.swap ("Shared.help"));
		help.addActionListener (this);
		controlPanel.add (help);
		bottomPanel.add (resetPanel);
		bottomPanel.add (controlPanel);

		// ov - 26.9.2007
		JSplitPane split = new JSplitPane (JSplitPane.HORIZONTAL_SPLIT,
				leftPanel, pattern2DPanel);
		split.setResizeWeight (0.5);


		//add cancel...
		getContentPane ().setLayout (new BorderLayout ());
//		getContentPane ().add (leftPanel, BorderLayout.WEST);		// ov - 26.9.2007
//		getContentPane ().add (pattern2DPanel, BorderLayout.CENTER);// ov - 26.9.2007
		getContentPane ().add (split, BorderLayout.CENTER);			// ov - 26.9.2007
		getContentPane ().add (bottomPanel, BorderLayout.SOUTH);

		setTitle (Translator.swap ("FiPatternEditor.patternEditorDialogTitle"));
		
		setModal (true);
	}

	private void updateDiameterList () {

		TableModel diameterSupListModel = new AbstractTableModel () {
			// an entry map is always composed of 2 value : a key and value
			public int getColumnCount () {return 2;}

			public int getRowCount () {return currentPattern.getDiametersSuperior ().size ();}
			//Returns the name of the column at columnIndex.
			@Override
			public String getColumnName (int columnIndex) {
				switch (columnIndex) {
					case 0  : return Translator.swap ("FiPatternEditor.heightDiam");
					case 1  : return Translator.swap ("FiPatternEditor.valueDiam");
					default : break;
				}
				return Translator.swap ("FiPatternEditor.tableColumnNameError");
			}

			//Return the key (col=0) or value (col=1) of the entry map at the position 'row'
			public Object getValueAt (int row, int col) {
				Object [] entry = currentPattern.getDiametersSuperior ().toArray ();
				String height = numberFormat.format (((FmGeomDiameter)entry[row]).getHeight ());
				String width = numberFormat.format (((FmGeomDiameter)entry[row]).getWidth ());

				if(col == 0) { return height; } else { return width; }
			}

			@Override
			public boolean isCellEditable (int row, int col) {
				return true;
			}

			@Override
			public void setValueAt (Object value, int row, int col) {
				if(col==0)	{
					modifySuperiorDiameterEntry (row,(String)value, "");
				} else {
					modifySuperiorDiameterEntry (row,"", (String)value);
				}
			}
		};

		TableModel diameterInfListModel = new AbstractTableModel () {
			// an entry map is always composed of 2 value : a key and value
			public int getColumnCount () {return 2;}

			public int getRowCount () {return currentPattern.getDiametersInferior ().size ();}
			//Returns the name of the column at columnIndex.
			@Override
			public String getColumnName (int columnIndex) {
				switch (columnIndex) {
					case 0  : return Translator.swap ("FiPatternEditor.heightDiam");
					case 1  : return Translator.swap ("FiPatternEditor.valueDiam");
					default : break;
				}
				return Translator.swap ("FiPatternEditor.tableColumnNameError");
			}

			//Return the key (col=0) or value (col=1) of the entry map at the position 'row'
			public Object getValueAt (int row, int col) {
				Object [] entry = currentPattern.getDiametersInferior ().toArray ();
				String height = numberFormat.format (((FmGeomDiameter)entry[row]).getHeight ());
				String width = numberFormat.format (((FmGeomDiameter)entry[row]).getWidth ());

				if(col == 0) { return height; } else { return width; }
			}

			@Override
			public boolean isCellEditable (int row, int col) {
				return true;
			}

			@Override
			public void setValueAt (Object value, int row, int col) {
				if(col==0)	{
					modifyInferiorDiameterEntry (row,(String)value, "");
				} else {
					modifyInferiorDiameterEntry (row,"", (String)value);
				}
			}
		};

		listSupDiameter = new JTable (diameterSupListModel);

		listInfDiameter = new JTable (diameterInfListModel);

		scroll1.getViewport ().setView (listSupDiameter);

		scroll2.getViewport ().setView (listInfDiameter);

		infoField.setText (currentPattern.getName ());

		//update the previewer
		pattern2DPanel.reset ();

	}

}

