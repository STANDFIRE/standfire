package fireparadox.gui.database;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import jeeb.lib.util.AmapDialog;
import jeeb.lib.util.Check;
import jeeb.lib.util.JWidthLabel;
import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import capsis.commongui.util.Helper;
import fireparadox.model.FmModel;
import fireparadox.model.database.FmDBCommunicator;
import fireparadox.model.database.FmDBEvent;


/** FiSiteEventEditor : Creation/Modification of a FiDBEvent
 *
 * @author I. Lecomte - January 2010
 */
public class FmSiteEventEditor extends AmapDialog implements ActionListener {


	//site object to update
	private FmModel model;
	private FmDBCommunicator bdCommunicator;
	private FmDBEvent event, newEvent;
	private long eventId;
	private boolean deleteAction, deleted;


	//Fields for user interface
	private JTextField dateStartField;
	private JTextField dateEndField;



	//country and municipality selection
	private JComboBox eventComboBox;
	private Vector<String> eventList = new Vector <String> ();


	//control buttons
	private JButton ok;
	private JButton cancel;
	private JButton help;

	//local data for retrieving user entries
	private String dateStart, dateEnd, eventType;



	/**
	 * Constructor : UPDATE if siteId is not null
	 */
	public FmSiteEventEditor (FmModel _model, FmDBEvent _event, long _lastId, boolean _deleteAction) {

		super ();
		model = _model;
		event = _event;
		deleteAction = _deleteAction;
		deleted = false;



		bdCommunicator =  model.getBDCommunicator ();

		//loading site fields
		if (event != null) {

			eventId = event.getEventId();

			dateStart = event.getDateStart();
			dateEnd = event.getDateEnd();


			eventType = event.getName();


		}
		//new site fields are empty
		else {
			eventId = _lastId;
			dateStart = "";
			dateEnd = "";

		}

		//loading enum and species list
		loadEnum ();


		createUI ();
		pack ();
		show ();
	}

	/**	Actions on the buttons
	 */
	public void actionPerformed (ActionEvent evt) {
		if (evt.getSource ().equals (ok)) {
			validateAction ();
		} else if (evt.getSource ().equals (cancel)) {
			setValidDialog (false);
		} else if (evt.getSource ().equals (help)) {
			Helper.helpFor (this);
		}
	}


	/**	Validation = save in the database
	 */
	private void validateAction () {

		if (deleteAction) {
			deleted = true;
			setValidDialog (true);

		}
		else {
			if (controlValues ()) {		//if control OK

				//retrieve dates
				dateStart = dateStartField.getText ();
				dateEnd = dateEndField.getText();

				eventType = "";
				int n = eventComboBox.getSelectedIndex();
				if (n >= 0) eventType = (String) eventComboBox.getItemAt (n);

				//this new event is temporary created to help the database update
				newEvent = new FmDBEvent (eventId, eventType,
									dateStart,
									dateEnd,
									false);

			}
			setValidDialog (true);

		}


	}

	/**	Control the value in the ComboBoxes and the TextFields
	 */
	 private boolean controlValues ()   {




		if (!Check.isEmpty (dateStartField.getText ())) {

			dateStart = dateStartField.getText ();

			int index = dateStart.indexOf ("/");
			if (index > 0) {
				String ljj = dateStart.substring (0,index);
				if (!Check.isInt (ljj)) {
					JOptionPane.showMessageDialog (this, Translator.swap ("FiSiteEventEditor.dateStartIsNotValid"),
					Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
					return false;
				}
				int jj = Integer.parseInt(ljj);
				if (jj<0 || jj>31) {
					JOptionPane.showMessageDialog (this, Translator.swap ("FiSiteEventEditor.dateStartIsNotValid"),
					Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
					return false;
				}
				String suite = dateStart.substring (index+1);
				index = suite.indexOf ("/");
				if (index > 0) {
					String lmm = suite.substring (0,index);
					if (!Check.isInt (lmm)) {
						JOptionPane.showMessageDialog (this, Translator.swap ("FiSiteEventEditor.dateStartIsNotValid"),
						Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
						return false;
					}
					int mm = Integer.parseInt(lmm);
					if (mm<0 || mm>12) {
						JOptionPane.showMessageDialog (this, Translator.swap ("FiSiteEventEditor.dateStartIsNotValid"),
						Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
						return false;
					}
					String laa = suite.substring (index+1);

					if (!Check.isInt (laa)) {
						JOptionPane.showMessageDialog (this, Translator.swap ("FiSiteEventEditor.dateStartIsNotValid"),
						Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
						return false;
					}
					int aa = Integer.parseInt(laa);
					if (aa<2000) {
						JOptionPane.showMessageDialog (this, Translator.swap ("FiSiteEventEditor.dateStartIsNotValid"),
						Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
						return false;
					}


				}
				else {
					JOptionPane.showMessageDialog (this, Translator.swap ("FiSiteEventEditor.dateStartIsNotValid"),
					Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
					return false;
				}

			}
			else {
				JOptionPane.showMessageDialog (this, Translator.swap ("FiSiteEventEditor.dateStartIsNotValid"),
				Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
				return false;
			}
		}
		else {
			JOptionPane.showMessageDialog (this, Translator.swap ("FiSiteEventEditor.dateStartIsEmpty"),
			Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
			return false;
		}

		//date end


		if (!Check.isEmpty (dateEndField.getText ())) {

dateEnd = dateEndField.getText ();

			int index = dateEnd.indexOf ("/");
			if (index > 0) {
				String ljj = dateEnd.substring (0,index);
				if (!Check.isInt (ljj)) {
					JOptionPane.showMessageDialog (this, Translator.swap ("FiSiteEventEditor.dateEndIsNotValid"),
					Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
					return false;
				}
				int jj = Integer.parseInt(ljj);
				if (jj<0 || jj>31) {
					JOptionPane.showMessageDialog (this, Translator.swap ("FiSiteEventEditor.dateEndIsNotValid"),
					Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
					return false;
				}
				String suite = dateEnd.substring (index+1);
				index = suite.indexOf ("/");
				if (index > 0) {
					String lmm = suite.substring (0,index);
					if (!Check.isInt (lmm)) {
						JOptionPane.showMessageDialog (this, Translator.swap ("FiSiteEventEditor.dateEndIsNotValid"),
						Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
						return false;
					}
					int mm = Integer.parseInt(lmm);
					if (mm<0 || mm>12) {
						JOptionPane.showMessageDialog (this, Translator.swap ("FiSiteEventEditor.dateEndIsNotValid"),
						Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
						return false;
					}
					String laa = suite.substring (index+1);

					if (!Check.isInt (laa)) {
						JOptionPane.showMessageDialog (this, Translator.swap ("FiSiteEventEditor.dateEndIsNotValid"),
						Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
						return false;
					}
					int aa = Integer.parseInt(laa);
					if (aa<2000) {
						JOptionPane.showMessageDialog (this, Translator.swap ("FiSiteEventEditor.dateEndIsNotValid"),
						Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
						return false;
					}


				}
				else {
					JOptionPane.showMessageDialog (this, Translator.swap ("FiSiteEventEditor.dateEndIsNotValid"),
					Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
					return false;
				}

			}
			else {
				JOptionPane.showMessageDialog (this, Translator.swap ("FiSiteEventEditor.dateEndIsNotValid"),
				Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
				return false;
			}
		}
		else {
			JOptionPane.showMessageDialog (this, Translator.swap ("FiSiteEventEditor.dateEndIsEmpty"),
			Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
			return false;
		}

		return true;
	}


	/**	Initialize the GUI.
	 */
	private void createUI () {





		JPanel eventPanel = new JPanel ();
		Box box = Box.createVerticalBox ();
		JPanel f1 = new JPanel (new FlowLayout (FlowLayout.LEFT));
		JPanel f2 = new JPanel (new FlowLayout (FlowLayout.LEFT));
		JPanel f3 = new JPanel (new FlowLayout (FlowLayout.LEFT));

		f1.add (new JWidthLabel (Translator.swap ("FiSiteEventEditor.eventType")+" :",150));
		f1.add (eventComboBox);


		dateStartField = new JTextField(10);
		dateStartField.setText (""+dateStart);
		dateEndField = new JTextField(10);
		dateEndField.setText (""+dateEnd);

		f2.add (new JWidthLabel (Translator.swap ("FiSiteEventEditor.dateStart")+" :",150));
		f2.add (dateStartField);
		f3.add (new JWidthLabel (Translator.swap ("FiSiteEventEditor.dateEnd")+" :", 150));
		f3.add (dateEndField);

		if (deleteAction) {
			eventComboBox.setEnabled (false);
			dateStartField.setEnabled (false);
			dateEndField.setEnabled (false);
		}


		box.add(f1);
		box.add(f2);
		box.add(f3);


		eventPanel.add (box);



		/*********** CONTROL panel **************/
		JPanel controlPanel = new JPanel ();
		controlPanel.setLayout (new FlowLayout (FlowLayout.CENTER));
		if (deleteAction)
			ok = new JButton (Translator.swap ("FiSiteEventEditor.delete"));
		else
			ok = new JButton (Translator.swap ("FiSiteEventEditor.validate"));
		ok.addActionListener (this);
		controlPanel.add (ok);
		cancel = new JButton (Translator.swap ("Shared.cancel"));
		cancel.addActionListener (this);
		controlPanel.add (cancel);
		help = new JButton (Translator.swap ("Shared.help"));
		help.addActionListener (this);
		controlPanel.add (help);

		getContentPane ().setLayout (new BorderLayout ());
		getContentPane ().add (eventPanel, BorderLayout.CENTER);
		getContentPane ().add (controlPanel, BorderLayout.SOUTH);
		setTitle (Translator.swap ("FiSiteEventEditor.title"));

		setModal (true);
	}

	//Load enum list from database
	private void  loadEnum () {



		try {
			//load enums
			eventList = bdCommunicator.getEvents();

			eventComboBox = new JComboBox (eventList);

			if ((event != null) && (eventType.compareTo("") != 0)) {
				eventComboBox.setSelectedItem (eventType);
			}
			else eventComboBox.setSelectedIndex(0);


		} catch (Exception e) {
			Log.println (Log.ERROR, "FiSiteEventEditor.c ()", "error while opening ENUM data base", e);
		}
	}

	public FmDBEvent getNewEvent() {return newEvent;}

	public boolean isDeleted() {return deleted;}


}
