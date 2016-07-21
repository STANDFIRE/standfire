package fireparadox.gui.plantpattern;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import jeeb.lib.util.AmapDialog;
import jeeb.lib.util.LinePanel;
import jeeb.lib.util.Translator;
import capsis.commongui.util.Helper;
import fireparadox.model.FmModel;
import fireparadox.model.plant.fmgeom.FmGeom;

class PatternListCellRenderer extends JLabel implements ListCellRenderer {

	private FmModel model;

	public PatternListCellRenderer (FmModel fm) {
		model=fm;
		setOpaque (true);
	}
	public Component getListCellRendererComponent (
		JList list,
		Object value,
		int index,
		boolean isSelected,
		boolean cellHasFocus) {
		setText (value.toString ());
		Object [] entry = model.getPatternList ().entrySet ().toArray ();
		if(index < entry.length) {
			if(model.getPatternList ().isAdminEntry ((String)((Map.Entry)entry[index]).getKey ())) {
				setBackground (isSelected ? (Color)UIManager.get ("List.selectionBackground") : Color.LIGHT_GRAY);
			} else {
				setBackground (isSelected ? (Color)UIManager.get ("List.selectionBackground") : Color.white);
			}
		}  else setBackground (isSelected ? (Color)UIManager.get ("List.selectionBackground") : Color.white);

		setForeground (isSelected ? Color.white : Color.black);

		return this;
	}
}
/**
 *
 * @author S. Griffon - May 2007
 */
public class FmPatternListDialog extends AmapDialog implements ActionListener, ListSelectionListener {

	private FmModel model;

	private JButton ok;
	private JButton cancel;
	private JButton help;
	private JButton remove;
	private JButton reset;
	private JButton editPattern;

	private JScrollPane scroll1;

	private JList patternJList;

	private FmPattern2DPanel pattern2DPanel;

	private int selectedRow; //the current selected row

	/**	Constructor.
	 */
	public FmPatternListDialog (FmModel fm, int selectedRow) {

		super ();
		model = fm;
		this.selectedRow=selectedRow;
		createUI ();
		updateListPattern ();
		refreshPreviewer ();
		statusRemove();

		// location is set by AmapDialog
		pack ();
		show ();
	}

	//	Ok was hit
	//
	private void okAction () {

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
		} else if (evt.getSource ().equals (remove)) {
			removePattern ();
		} else if (evt.getSource ().equals (editPattern)) {
			editPatterns ();
		} else if (evt.getSource ().equals (reset)) {
			reset ();
		}
	}

	private void reset () {

		int ret = JOptionPane.showConfirmDialog (this,Translator.swap ("FiPatternListDialog.resetYesNo"));
		if (ret==0) {
			try {
				model.getPatternList ().reset (model.getPatternMap ());
				updateListPattern ();
				refreshPreviewer ();
				model.getPatternMap ().firePatternChanged ();
			} catch (Exception e) {

			}
		}
	}

	private void editPatterns () {

		int [] selRow  = patternJList.getSelectedIndices ();
		selectedRow = selRow[0];
		Object [] entry = model.getPatternList ().entrySet ().toArray ();
		if(entry.length != 0) {
			if(selRow.length > 0) {
				if(model.getPatternList ().isModifiable ((String)((Map.Entry)entry[selRow[0]]).getKey ())) {
					FmGeom selPattern = ((FmGeom)((Map.Entry)entry[selRow[0]]).getValue ());
					FmPatternEditor fEditor = new FmPatternEditor ("",selPattern.clone ());
					if(fEditor.isValidDialog ()) {
						try {
							model.getPatternList ().remove (String.valueOf (selPattern.getId ()));
						} catch (Exception e) {
							JOptionPane.showMessageDialog (this,  e.getMessage ());
						}
						try {
							model.getPatternList ().addPattern (fEditor.getCurrentPattern ());
							model.getPatternList ().save ();
						} catch (Exception e) {
							JOptionPane.showMessageDialog (this,  Translator.swap ("FiPatternListDialog.errorOnWritingPatternList")+" : "+e.getMessage ());
						}
					}
				} else {
					JOptionPane.showMessageDialog (this,  Translator.swap ("FiPatternListDialog.removeAdminEntryForbidden"));
				}

			}

			updateListPattern ();
			refreshPreviewer ();
			model.getPatternMap ().firePatternChanged ();
		}
	}

	public void setSelectedRow (int selectedRow) {
		this.selectedRow = selectedRow;
	}


	//Remove an entry of the pattern list and redraw the list.
	private void removePattern () {

		int [] selRow  = patternJList.getSelectedIndices ();
		Object [] entry = model.getPatternList ().entrySet ().toArray ();
		if(entry.length != 0) {
			for (int row : selRow) {
				String patternid=String.valueOf (((FmGeom)((Map.Entry)entry[row]).getValue ()).getId ());
				int ret = JOptionPane.showConfirmDialog (this,Translator.swap ("FiPatternListDialog.removeYesNo") + " : " + ((FmGeom)((Map.Entry)entry[row]).getValue ()).getName ());
				if (ret==0) {
					if( ! model.getPatternMap ().containsValue (patternid) ) {
						try {
							model.getPatternList ().removePattern ((String)((Map.Entry)entry[row]).getKey ());
						} catch (Exception e) {
							JOptionPane.showMessageDialog (this,  e.getMessage ());
						}
						try{
							model.getPatternList ().save ();
						} catch (Exception e) {
							JOptionPane.showMessageDialog (this,  Translator.swap ("FiPatternListDialog.errorOnWritingPatternList"));
						}

					} else {
						JOptionPane.showMessageDialog (this, Translator.swap ("FiPatternListDialog.patternReferencedInPatternMap"));
					}
				}
			}
			selectedRow=0;
			//Redraw a new JList with the updated PatternMap
			updateListPattern ();
			refreshPreviewer ();
		}
	}

	//	Initialize the GUI.
	//
	private void createUI () {

		JPanel l1 = new JPanel (new BorderLayout ());
		scroll1 = new JScrollPane ();
		l1.add (scroll1, BorderLayout.CENTER);

		JPanel c2 = new JPanel (new GridLayout (3,1));
		remove = new JButton (Translator.swap ("FiPatternListDialog.remove"));
		remove.addActionListener (this);
		editPattern = new JButton (Translator.swap ("FiPatternListDialog.editPattern"));
		editPattern.addActionListener (this);
		c2.add (remove);
		c2.add (editPattern);
		JPanel aux = new JPanel (new BorderLayout ());
		aux.add (c2,BorderLayout.NORTH);
		l1.add (aux, BorderLayout.EAST);

		TitledBorder tborder2 = new TitledBorder (l1.getBorder (), Translator.swap ("FiPatternListDialog.patternsList"));
		l1.setBorder (tborder2);

		pattern2DPanel = new FmPattern2DPanel (null);
		TitledBorder tborder4 = new TitledBorder (pattern2DPanel.getBorder (), Translator.swap ("FiPatternListDialog.previewer"));
		pattern2DPanel.setBorder (tborder4);
		pattern2DPanel.setPreferredSize (new Dimension (200,400));

		//Control Panel
		LinePanel bottomPanel = new LinePanel ();
		JPanel resetPanel = new JPanel ();
		resetPanel.setLayout (new FlowLayout (FlowLayout.LEFT));
		reset = new JButton (Translator.swap ("FiPatternListDialog.reset"));
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
				l1, pattern2DPanel);
		split.setResizeWeight (0.5);

		//add cancel...
		getContentPane ().setLayout (new BorderLayout ());
		//getContentPane ().add (l1, BorderLayout.CENTER);			// ov - 26.9.2007
		//getContentPane ().add (pattern2DPanel, BorderLayout.EAST);// ov - 26.9.2007
		getContentPane ().add (split, BorderLayout.CENTER);			// ov - 26.9.2007
		getContentPane ().add (bottomPanel, BorderLayout.SOUTH);

		setTitle (Translator.swap ("FiPatternListDialog.patternListDialogTitle"));
		
		setModal (true);
	}




	private void updateListPattern () {

		//  To display a TreeMap into a JList
		//        - the row represent the pattern name of an entry of the map

		ListModel dataModel = new AbstractListModel () {

			public String getColumnName (int columnIndex) {
				return Translator.swap ("FiPatternListDialog.patternName");
			}

			public Object getElementAt (int index) {
				Object [] entry = model.getPatternList ().entrySet ().toArray ();
				FmGeom value = (FmGeom)((Map.Entry)entry[index]).getValue ();
				return value.getName ();
			}

			public int getSize () {
				return model.getPatternList ().size ();
			}
		};
		patternJList = new JList (dataModel);
		patternJList.setCellRenderer (new PatternListCellRenderer (model));
		patternJList.getSelectionModel ().addSelectionInterval (selectedRow,selectedRow);
		patternJList.getSelectionModel ().addListSelectionListener (this);
		scroll1.getViewport ().setView (patternJList);

	}

	private void refreshPreviewer () {
		int selectedRow = patternJList.getSelectedIndex ();
		Object [] entry = model.getPatternList ().entrySet ().toArray ();
		if(entry.length != 0) {
			FmGeom selPattern = ((FmGeom)((Map.Entry)entry[selectedRow]).getValue ());
			pattern2DPanel.setPattern (selPattern);
		} else {
			pattern2DPanel.setPattern (null);
		}
	}

	private void statusRemove () {
		int selectedRow = patternJList.getSelectedIndex ();
		Object [] entry = model.getPatternList ().entrySet ().toArray ();
		String patternid=String.valueOf (((FmGeom)((Map.Entry)entry[selectedRow]).getValue ()).getId ());
		if( ! model.getPatternMap ().containsValue (patternid) ) {
			remove.setEnabled (true);
		} else {
			remove.setEnabled (false);
		}
	}

	public void valueChanged (ListSelectionEvent e) {
		statusRemove ();
		refreshPreviewer ();
	}
}
