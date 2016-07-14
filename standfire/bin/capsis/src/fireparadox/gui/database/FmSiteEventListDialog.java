package fireparadox.gui.database;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.LinkedHashMap;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import jeeb.lib.util.AmapDialog;
import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import capsis.commongui.util.Helper;
import fireparadox.model.FmModel;
import fireparadox.model.database.FmDBCommunicator;
import fireparadox.model.database.FmDBEvent;
import fireparadox.model.database.FmDBSite;

/**	FiSiteEventListDialog : Event list for a site
*
*	@author I. Lecomte - january 2010
*/
public class FmSiteEventListDialog extends AmapDialog implements ActionListener {

	private FmModel model;
	private FmDBCommunicator bdCommunicator;		//to read database

	private FmDBSite site;
	private	LinkedHashMap<Long, FmDBEvent> eventMap;

    private JTable resultTable;
    private FmSiteEventTableModel tableModel;
	private long lastId = 0;
	private boolean isValidated = false;
	private boolean isUpdated = false;


	private JButton close;
	private JButton help;
	private JButton add;
	private JButton modify;
	private JButton supress;
	private JButton valid;


	/**	Constructor.
	*/
	public FmSiteEventListDialog (FmModel _model, FmDBSite _site, LinkedHashMap<Long, FmDBEvent> _events) {

		super ();
		model = _model;
		site = _site;


		tableModel = new  FmSiteEventTableModel ();

		//load event from previous screen
		if (_events != null) {
			eventMap = _events;
		}
		//load event from database
		else {
			eventMap = new LinkedHashMap<Long, FmDBEvent> ();
			if (site != null) 	loadEvents();
		}

		loadTable();
		createUI ();
		pack ();
		show ();
	}

	/**	Actions on the buttons
	*/
	public void actionPerformed (ActionEvent evt) {
		if (evt.getSource ().equals (close)) {
			closeAction ();
		} else if (evt.getSource ().equals (add)) {
			addEvent ();
		} else if (evt.getSource ().equals (modify)) {
			modifyEvent ();
		} else if (evt.getSource ().equals (supress)) {
			deleteEvent ();
		} else if (evt.getSource ().equals (valid)) {
			valideEvent ();
		} else if (evt.getSource ().equals (help)) {
			Helper.helpFor (this);
		}
	}

	/**	Close was hit
	*/
	private void valideEvent () {
		isValidated = true;
		setVisible (false);
	}
	/**	Close was hit
	*/
	private void closeAction () {
		if (isUpdated) {

			if(exitQuestion()) {
				isValidated = false;
				eventMap.clear();
				setVisible (false);
			}

		}
		else setVisible (false);


	}


	private boolean exitQuestion () {

		boolean answer = false;
		Object[] options = {Translator.swap ("Shared.yes"), Translator.swap ("Shared.no")};
		int n = JOptionPane.showOptionDialog(
				this,
				Translator.swap ("FiSiteEditor.eventHasBeenValidated"),
				"",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				null,			//don't use a custom Icon
				options,		//the titles of buttons
				null /*options[1]*/);	//default button title
		if (n == JOptionPane.YES_OPTION) {
			answer = true;
		} else if (n == JOptionPane.NO_OPTION) {
			answer = false;
		}
		return answer;

	}
	/**	 adding
	*/
	private void addEvent () {

		lastId = lastId  - 1;

		FmSiteEventEditor dial  = new FmSiteEventEditor (model, null, lastId, false);

		FmDBEvent newEvent = dial.getNewEvent();
		if (newEvent != null) {
			isUpdated = true;
			eventMap.put (lastId, newEvent);
			loadTable();
			repaint ();
		}

	}

	/**	 modification
	*/
	private void modifyEvent() {
		int [] selRow  = this.getSelectedRows ();

		if (selRow.length > 0) {
			int selectedRow = selRow[0];
			FmDBEvent eSelected = this.getEvent (selectedRow);
			long id = eSelected.getEventId();

			FmSiteEventEditor dial  = new FmSiteEventEditor (model, eSelected, id, false);

			FmDBEvent newEvent = dial.getNewEvent();
			if (newEvent != null) {
				isUpdated = true;
				eventMap.remove (eSelected);
				eSelected = null;

				eventMap.put (id, newEvent);

				loadTable();
				repaint ();
			}

		}
	}



	/**	delete
	*/
	private void deleteEvent () {
		int [] selRow  = this.getSelectedRows ();
		if (selRow.length > 0) {
			int selectedRow = selRow[0];
			FmDBEvent eSelected = this.getEvent (selectedRow);
			long id = eSelected.getEventId();

			FmSiteEventEditor dial  = new FmSiteEventEditor (model, eSelected, id, true);

			if (dial.isDeleted()) {
				if (id < 0) {
					isUpdated = true;
					eventMap.remove (id);
				    eSelected = null;
				}
				else {
					isUpdated = true;
					eventMap.remove (id);
					eSelected.setDeleted(true);
					eventMap.put (id, eSelected);

				}

				loadTable();
				repaint ();
			}

		}
	}


	/**	Initialize the GUI.
	*/
	private void createUI () {


		//Result Table
		JPanel tablePanel = new JPanel (new BorderLayout ());
		ColumnPanel colTable = new ColumnPanel ();
		resultTable = new JTable(tableModel);
		JScrollPane listSP = new JScrollPane(resultTable);
		listSP.setPreferredSize (new Dimension (600,240));
		colTable.add (listSP);

		tablePanel.add (colTable);


		// Control panel
		JPanel controlPanel = new JPanel (new FlowLayout (FlowLayout.CENTER));


		add = new JButton (Translator.swap ("FiSiteEventListDialog.add"));
		modify = new JButton (Translator.swap ("FiSiteEventListDialog.modify"));
		supress = new JButton (Translator.swap ("FiSiteEventListDialog.supress"));
		valid = new JButton (Translator.swap ("FiSiteEventListDialog.valid"));
		controlPanel.add (add);
		controlPanel.add (modify);
		controlPanel.add (supress);
		controlPanel.add (valid);
		modify.addActionListener (this);
		add.addActionListener (this);
		supress.addActionListener (this);
		valid.addActionListener (this);


		close = new JButton (Translator.swap ("Shared.close"));
		help = new JButton (Translator.swap ("Shared.help"));
		controlPanel.add (close);
		controlPanel.add (help);
		close.addActionListener (this);
		help.addActionListener (this);

		getContentPane ().setLayout (new BorderLayout ());
		//getContentPane ().add (sitePanel, BorderLayout.NORTH);
		getContentPane ().add (tablePanel, BorderLayout.CENTER);
		getContentPane ().add (controlPanel, BorderLayout.SOUTH);

		setTitle (Translator.swap ("FiSiteEventListDialog.title"));

		setModal (true);
	}

	/**
	* Load EVENTS  from database
	*/
	public void loadEvents () {

		try {

			bdCommunicator = model.getBDCommunicator ();
			eventMap = bdCommunicator.getSiteEvents (site);


		} catch (Exception e) {
			Log.println (Log.ERROR, "FiSiteEventListDialog ()", "error while opening data base", e);
		}

	}

	public void loadTable() {
		tableModel.clear();
		if (eventMap != null) {
			for (Iterator i = eventMap.keySet().iterator(); i.hasNext ();) {
				Object cle = i.next();
				FmDBEvent t = (FmDBEvent) eventMap.get(cle);
				if (!t.isDeleted()) tableModel.addEvent (t);
				lastId = Math.min (lastId, t.getEventId());
			}
		}
	}


    /** return the selected rows in the displayed table
     */
	public int[] getSelectedRows () {
		return resultTable.getSelectedRows();
	}

    /** return the event map created
     */
	public LinkedHashMap<Long, FmDBEvent>  getEvents () {
		return eventMap;
	}

    /** return the event from the table
     */
	public FmDBEvent getEvent (int index) {
		return tableModel.getEvent(index);
	}

    /** return the shape id store at this index in the displayed table
     */
	public long getEventId (int index) {
		FmDBEvent event =  tableModel.getEvent(index);
		return event.getEventId ();
	}
    /** return if the map have been validated
     */
	public boolean getIsValidated () {
		return isValidated;
	}

}

