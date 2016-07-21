/*
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 *
 * Copyright (C) 2000-2003  Francois de Coligny
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package capsis.extension.modeltool.amapsim2;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.MessageDialog;
import jeeb.lib.util.Log;
import jeeb.lib.util.NonEditableTableModel;
import jeeb.lib.util.Question;
import jeeb.lib.util.StatusDispatcher;
import jeeb.lib.util.Translator;
import capsis.commongui.util.Helper;
import capsis.commongui.util.Tools;
import capsis.defaulttype.Tree;
import capsis.defaulttype.TreeCollection;
import capsis.defaulttype.TreeList;
import capsis.extension.DialogModelTool;
import capsis.gui.DialogWithClose;
import capsis.gui.MainFrame;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.Step;
import capsis.lib.amapsim.AMAPsimRequestableTree;
import capsis.lib.amapsim.AMAPsimTree;
import capsis.lib.amapsim.AMAPsimTreeData;
import capsis.lib.amapsim.AMAPsimTreeStep;
import capsis.util.Runner;

/**
 * A connection tool for pp3 model (maritime pine) and AMAPsim.
 * This tool is for crown shape computation using AMAPsim for some trees
 * computed in pp3 module.
 *
 * @author F. de Coligny - july 2001
 */
public class AMAPsim extends DialogModelTool implements ActionListener, 
		AMAPsimCaller, MouseListener, PropertyChangeListener {
	
	public static final String AUTHOR = "F. de Coligny";
	public static final String VERSION = "1.3";
	
	static {
		Translator.addBundle("capsis.extension.modeltool.amapsim2.AMAPsim");
	}
	private static final Object[] columnNames1 = {
			Translator.swap ("AMAPsim.date"),
			Translator.swap ("AMAPsim.id"),
			Translator.swap ("AMAPsim.type"),
			Translator.swap ("AMAPsim.length")};
	private static final Object[] columnNames2 = {
			Translator.swap ("AMAPsim.serverId"),
			Translator.swap ("AMAPsim.date"),
			Translator.swap ("AMAPsim.id"),
			Translator.swap ("AMAPsim.type"),
			Translator.swap ("AMAPsim.length"),
			Translator.swap ("AMAPsim.returnCode")};
	private static final Object[] codeLabelsArray = {
			new Integer (0), "AMAPsim.normal", 
			new Integer (-1), "AMAPsim.adjustmentFailed", 
			new Integer (-2), "AMAPsim.systemErrorCheckTraces", 
			new Integer (-3), "AMAPsim.wrongRequestFormat", 
			};
	private Map codeLabels;		// code -> label

	
	// Restore column sizes
	private Collection table2Columns;
	private int[] table2ColumnSizes = null;
	private Collection table1Columns;
	private int[] table1ColumnSizes = null;
	
	private ProtocolManager protocolManager;
	private AMAPsimClient client;

	private Step step;
	
	
	private JTextArea displayArea;

	private JTable table1;
	private JTable table2;
	
	private JScrollPane scroll1;
	private JScrollPane scroll2;

	
	private JButton connect;
	private JButton hello;
	private JButton mode1;
	private JButton mode2;
	private JButton askAgain;
	private JButton forceAnswer;
	private JButton cancelRequest;
	private JButton askStatus;
	private JButton bye;

	private JButton close;
	private JButton help;


	/**	Code labels table creation.
	*/
	public String getCodeLabel (int code) {
		if (codeLabels == null) {
			codeLabels = new HashMap ();
			int i = 0;
			try {
				while (i < codeLabelsArray.length) {
					Integer c = (Integer) codeLabelsArray[i++];
					String label = Translator.swap ((String) codeLabelsArray[i++]);
					codeLabels.put (c, label);
				}
			} catch (Exception e) {
				Log.println (Log.ERROR, "AMAPsim.getCodeLabels ()", "Exception during table creation, check code", e);
			}
		}
		String label = (String) codeLabels.get (new Integer (code));
		if (label == null) {label = Translator.swap ("AMAPsim.unknown");}
		return label;
	}
	
	
	/**
	 * Phantom constructor.
	 * Only to ask for extension properties (authorName, version...).
	 */
	public AMAPsim (){		
		super();
	}
	
	@Override
	public void init(GModel m, Step s){


		try {
						
			step = s;
			setTitle (Translator.swap ("AMAPsim")+" - "+step.getCaption ());

			protocolManager = new ProtocolManager (this);

			createUI ();

			setDefaultCloseOperation (DO_NOTHING_ON_CLOSE);
			addWindowListener (new WindowAdapter () {
				public void windowClosing(WindowEvent e) {
					escapePressed ();
				}
			});

			pack ();
			setModal (false);
			
			setVisible (true);

		} catch (Exception exc) {
			Log.println (Log.ERROR, "AMAPsim.c ()", exc.toString (), exc);
		}
	}

	/**
	 * Extension dynamic compatibility mechanism.
	 * This matchwith method checks if the extension can deal (i.e. is compatible) with the referent.
	 */
	static public boolean matchWith (Object referent) {
		try {
			if (!(referent instanceof GModel)) {return false;}
			GModel m = (GModel) referent;

			GScene s = ((Step) m.getProject ().getRoot ()).getScene ();
			if (!(s instanceof TreeCollection)) {return false;}

			TreeCollection tc = (TreeCollection) s;
			if (!(tc.getTrees ().iterator ().next () instanceof AMAPsimRequestableTree)) {return false;}

			// This must be considered optional
			// fc - 22.10.2003
			//
			//~ MethodProvider mp = m.getMethodProvider ();
			//~ if (mp == null) {return false;}
			//~ if (!(mp instanceof GProvider)) {return false;}
			//~ if (!(mp instanceof HgProvider)) {return false;}
			//~ if (!(mp instanceof HdomProvider)) {return false;}
			//~ if (!(mp instanceof DgProvider)) {return false;}
			//~ if (!(mp instanceof DdomProvider)) {return false;}

		} catch (Exception e) {
			Log.println (Log.ERROR, "AMAPsim.matchWith ()", "Error in matchWith () (returned false)", e);
			return false;
		}

		return true;
	}

	
	


	/**
	* From AMAPsimCaller interface.
	*/
	synchronized public void update () {
		try {
			Map map1 = protocolManager.getPendingRequests ();
			int nRows = 0;
			DefaultTableModel m1 = new NonEditableTableModel (columnNames1, nRows);
			Object[] row = new Object[4];
			for (Iterator i = map1.values ().iterator (); i.hasNext ();) {
				Request r = (Request) i.next ();
				row[0] = r.getDate ();		// fc - 29.1.2004
				row[1] = r.getMessageId ();
				row[2] = Request.getRequestName (r.getRequestType ());
				row[3] = ""+r.getDataLength ();
				m1.addRow (row);
			}
			
			table1.setModel (m1);
			table1.setAutoCreateRowSorter(true);			
			table1.revalidate ();
		} catch (Exception e) {
			Log.println (Log.ERROR, "AMAPsim.update ()", "Exception (1) occured", e);
		}

		try {
			Map map2 = protocolManager.getReceivedResponses ();
			int nRows = 0;
			DefaultTableModel m2 = new NonEditableTableModel (columnNames2, nRows);
			Object[] row2 = new Object[6];
			for (Iterator i = map2.values ().iterator (); i.hasNext ();) {
				Response r = (Response) i.next ();
				row2[0] = r.getServerId ();			// fc - 29.1.2004
				row2[1] = r.getDate ();				// fc - 29.1.2004
				row2[2] = r.getMessageId ();		// fc - 29.1.2004
				row2[3] = Request.getRequestName (r.getRequestType ());
				row2[4] = ""+r.getDataLength ();
				row2[5] = ""+r.getReturnCode ()+" ("+getCodeLabel (r.getReturnCode ())+")";
				m2.addRow (row2);
			}
			
			
			table2.setModel (m2);
			table2.setAutoCreateRowSorter(true);
			
			if (table2ColumnSizes != null) {
				for (int i = 0; i < table2ColumnSizes.length; i++) {
					table2.getColumnModel ().getColumn (i).setPreferredWidth (table2ColumnSizes[i]);
				}
			}
			if (table1ColumnSizes != null) {
				for (int i = 0; i < table1ColumnSizes.length; i++) {
					table1.getColumnModel ().getColumn (i).setPreferredWidth (table1ColumnSizes[i]);
				}
			}
			
			// New model => new columns => add listeners for size changes
			memoColumns ();
			
			table2.revalidate ();
		} catch (Exception e) {
			Log.println (Log.ERROR, "AMAPsim.update ()", "Exception (2) occured", e);
		}

		updateConnectButton ();
	}

	
	private void memoColumns () {
		// To manage column widths (memo when changed and restore)
		//
		TableColumnModel cm2 = table2.getColumnModel ();
		table2ColumnSizes = new int[cm2.getColumnCount ()];
		table2Columns = new HashSet ();
		for (int i = 0; i < cm2.getColumnCount (); i++) {
			TableColumn col = cm2.getColumn (i);
			table2Columns.add (col);
			table2ColumnSizes[i] = col.getWidth ();
			col.addPropertyChangeListener (this);
		}
		table2.setAutoResizeMode (JTable.AUTO_RESIZE_OFF);
		
		TableColumnModel cm1 = table1.getColumnModel ();
		table1ColumnSizes = new int[cm1.getColumnCount ()];
		table1Columns = new HashSet ();
		for (int i = 0; i < cm1.getColumnCount (); i++) {
			TableColumn col = cm1.getColumn (i);
			table1Columns.add (col);
			table1ColumnSizes[i] = col.getWidth ();
			col.addPropertyChangeListener (this);
		}
		table1.setAutoResizeMode (JTable.AUTO_RESIZE_OFF);
	}
	
	/**	Store column width when changed by user
	*/
	public void propertyChange (PropertyChangeEvent evt) {
		if (evt.getSource () instanceof TableColumn) {
			TableColumn col = (TableColumn) evt.getSource ();
			
			if (evt.getPropertyName ().equals ("width")) {
				int index = col.getModelIndex ();
				
				if (table2Columns.contains (col)) {	// a column of table2 ?
					table2ColumnSizes[index] = col.getPreferredWidth ();
				} else if (table1Columns.contains (col)) {	// a column of table1
					table1ColumnSizes[index] = col.getPreferredWidth ();
				}
			}
		}
	}

	
	/**
	* AMAPsim isConnected property.
	*/
	public boolean isConnected () {return client != null && client.isConnected ();}

	/**
	* From AMAPsimCaller interface.
	*/
	public void print (String msg) {
		display (msg);
	}

	/**
	 * From ActionListener interface.
	 */
	public void actionPerformed (ActionEvent evt) {
		if (evt.getSource ().equals (connect)) {
			connectAction ();

		} else if (evt.getSource ().equals (hello)) {
			helloAction ();

		} else if (evt.getSource ().equals (mode1)) {
			mode1Action ();

		} else if (evt.getSource ().equals (mode2)) {
			mode2Action ();

		} else if (evt.getSource ().equals (askAgain)) {
			askAgainAction (null);

		} else if (evt.getSource ().equals (forceAnswer)) {
			forceAnswerAction (null);

		} else if (evt.getSource ().equals (cancelRequest)) {
			cancelRequestAction (null);

		} else if (evt.getSource ().equals (askStatus)) {
			askStatusAction ();

		} else if (evt.getSource ().equals (bye)) {
			byeAction ();

		} else if (evt.getSource ().equals (close)) {
			escapePressed ();

		} else if (evt.getSource () instanceof JMenuItem) {
			JMenuItem menuItem = (JMenuItem) evt.getSource ();
			// INSPECT + REQUEST_TABLE
			if (menuItem.getMnemonic () == TablePopup.INSPECT + TablePopup.REQUEST_TABLE) {

				int numRow = table1.getSelectedRow ();
				String date = (String) table1.getModel ().getValueAt (numRow, 0);
				String messageId = (String) table1.getModel ().getValueAt (numRow, 1);
				String key = messageId+" "+date;
				Object target = protocolManager.getPendingRequests ().get (key);
				JComponent inspector = Tools.getIntrospectionPanel (target);
				JScrollPane sp = new JScrollPane (inspector);
				DialogWithClose dlg = new DialogWithClose (this, sp, Translator.swap ("AMAPsim.request")+" "+key, false);

			// INSPECT + RESPONSE_TABLE
			} else if (menuItem.getMnemonic () == TablePopup.INSPECT + TablePopup.RESPONSE_TABLE) {

				int numRow = table2.getSelectedRow ();
				String date = (String) table2.getModel ().getValueAt (numRow, 1);
				String messageId = (String) table2.getModel ().getValueAt (numRow, 2);
				String key = messageId+" "+date;
				Object target = protocolManager.getReceivedResponses ().get (key);
				JComponent inspector = Tools.getIntrospectionPanel (target);
				JScrollPane sp = new JScrollPane (inspector);
				DialogWithClose dlg = new DialogWithClose (this, sp, Translator.swap ("AMAPsim.response")+" "+key, false);

			// DELETE_ROW + REQUEST_TABLE
			} else if (menuItem.getMnemonic () == TablePopup.DELETE_ROW  + TablePopup.REQUEST_TABLE) {

				int[] indices = table1.getSelectedRows ();
				if (!Question.ask (MainFrame.getInstance (), 
						Translator.swap ("Shared.confirm"), Translator.swap ("AMAPsim.deleteTheseRows")
						+" ("
						+indices.length
						+" "
						+Translator.swap ("AMAPsim.rows")
						+") "
						)) {return;}
				
				Collection keys = new ArrayList ();
				for (int i = 0; i < indices.length; i++) {
					int numRow = indices[i];
					String date = (String) table1.getModel ().getValueAt (numRow, 0);
					String messageId = (String) table1.getModel ().getValueAt (numRow, 1);
					String key = messageId+" "+date;
					keys.add (key);
				}
				protocolManager.removeRequests (keys);
				update ();

			// DELETE_ROW + RESPONSE_TABLE
			} else if (menuItem.getMnemonic () == TablePopup.DELETE_ROW  + TablePopup.RESPONSE_TABLE) {

				int[] indices = table2.getSelectedRows ();
				if (!Question.ask (MainFrame.getInstance (), 
						Translator.swap ("Shared.confirm"), Translator.swap ("AMAPsim.deleteTheseRows")
						+" ("
						+indices.length
						+" "
						+Translator.swap ("AMAPsim.rows")
						+") "
						)) {return;}
				
				Collection keys = new ArrayList ();
				for (int i = 0; i < indices.length; i++) {
					int numRow = indices[i];
					
					String date = (String) table2.getModel ().getValueAt (numRow, 1);
					String messageId = (String) table2.getModel ().getValueAt (numRow, 2);
					String key = messageId+" "+date;
					keys.add (key);
				}
				protocolManager.removeResponses (keys);
				update ();




			// TAKE_FOR_MODEL	// fc - 13.10.2003
			// TAKE_FOR_MODEL	// fc - 13.10.2003
			// TAKE_FOR_MODEL	// fc - 13.10.2003
			// TAKE_FOR_MODEL	// fc - 13.10.2003
			} else if (menuItem.getMnemonic () == TablePopup.TAKE_FOR_MODEL) {

				int tableType = new Integer (menuItem.getActionCommand ()).intValue ();
				Request req = null;
				if (tableType == TablePopup.RESPONSE_TABLE) {
					int numRow = table2.getSelectedRow ();
					String date = (String) table2.getModel ().getValueAt (numRow, 1);
					String messageId = (String) table2.getModel ().getValueAt (numRow, 2);
					String key = messageId+" "+date;
					Object target = protocolManager.getReceivedResponses ().get (key);
					Response resp = (Response) target;
					req = resp.getRelatedRequest ();
				} else {
					int numRow = table1.getSelectedRow ();
					String date = (String) table1.getModel ().getValueAt (numRow, 0);
					String messageId = (String) table1.getModel ().getValueAt (numRow, 1);
					String key = messageId+" "+date;
					Object target = protocolManager.getPendingRequests ().get (key);
					req = (Request) target;
				}

				int messageType = req.getRequestType ();
				if (req != null && req instanceof AskAgain) {
					MessageDialog.print (this, Translator.swap ("AMAPsim.askAgainCanNotBeTakenForModel"));
					return;
				}
				
				
System.out.println ("AMAPsim.takeForModel: table="+tableType+" messageType="+messageType+" request="+req);
				if (messageType == 1) {
					// req will be opened in a Mode 1 dialog box
					try  {
						protocolManager.sendRequest ("mode_1", req);
					} catch (Exception e) {
						Log.println (Log.ERROR, "AMAPsim.actionPerformed ()", "error during sendRequest for mode_1", e);
						MessageDialog.print (this, Translator.swap ("AMAPsim.requestError"), e);
					}
				} else {
					// req will be opened in a Mode 2 dialog box
					try  {
						protocolManager.sendRequest ("mode_2", req);
					} catch (Exception e) {
						Log.println (Log.ERROR, "AMAPsim.actionPerformed ()", "error during sendRequest for mode_2", e);
						MessageDialog.print (this, Translator.swap ("AMAPsim.requestError"), e);
					}
				}

				//~ JPanel panel = new JPanel ();
				//~ panel.add (new JLabel (Translator.swap ("AMAPsim.takeForModel")), BorderLayout.NORTH);
				//~ DUser dlg = new DUser (panel);





			// TARTINATE
			} else if (menuItem.getMnemonic () == TablePopup.TARTINATE) {

				//~ JPanel panel = new JPanel ();
				//~ panel.add (new JLabel (Translator.swap ("AMAPsim.tartinate")), BorderLayout.NORTH);
				//~ DUser dlg = new DUser (panel);

				int numRow = table2.getSelectedRow ();
				String date = (String) table2.getModel ().getValueAt (numRow, 1);
				String messageId = (String) table2.getModel ().getValueAt (numRow, 2);
				String key = messageId+" "+date;
				Mode2Response r = (Mode2Response) protocolManager.getReceivedResponses ().get (key);

				tartinateAction (r);

			// CREATE_PROJECT
			} else if (menuItem.getMnemonic () == TablePopup.CREATE_PROJECT) {

				int numRow = table2.getSelectedRow ();
				String date = (String) table2.getModel ().getValueAt (numRow, 1);
				String messageId = (String) table2.getModel ().getValueAt (numRow, 2);
				String key = messageId+" "+date;
				Mode1Response r = (Mode1Response) protocolManager.getReceivedResponses ().get (key);

				try {
					new ProjectBuilder (r).execute ();
				} catch (Exception e) {
					Log.println (Log.WARNING, "AMAPsim.actionPerformed ()", "Project builder error", e);
					MessageDialog.print (this, Translator.swap ("AMAPsim.projectBuilderError"), e);
					return;
				}

				//~ JPanel panel = new JPanel ();
				//~ panel.add (new JLabel (Translator.swap ("AMAPsim.createProject")), BorderLayout.NORTH);
				//~ DUser dlg = new DUser (panel);

			// ASK_AGAIN
			} else if (menuItem.getMnemonic () == TablePopup.ASK_AGAIN) {

				int numRow = table1.getSelectedRow ();
				String date = (String) table1.getModel ().getValueAt (numRow, 0);
				String messageId = (String) table1.getModel ().getValueAt (numRow, 1);
				String key = messageId+" "+date;
				askAgainAction (key);

			// FORCE_ANSWER
			} else if (menuItem.getMnemonic () == TablePopup.FORCE_ANSWER) {

				int numRow = table1.getSelectedRow ();
				String date = (String) table1.getModel ().getValueAt (numRow, 0);
				String messageId = (String) table1.getModel ().getValueAt (numRow, 1);
				String key = messageId+" "+date;
				forceAnswerAction (key);

			// CANCEL_REQUEST
			} else if (menuItem.getMnemonic () == TablePopup.CANCEL_REQUEST) {

				int numRow = table1.getSelectedRow ();
				String date = (String) table1.getModel ().getValueAt (numRow, 0);
				String messageId = (String) table1.getModel ().getValueAt (numRow, 1);
				String key = messageId+" "+date;
				cancelRequestAction (key);

			}



		} if (evt.getSource ().equals (help)) {
			Helper.helpFor (this);
		}
	}

	// Tartinate a mode2Response in a Scenario (backwards from reference step to root).
	//
	private void tartinateAction (Mode2Response r) {
		try {
			Step refStep = step;	// clearer name
	System.out.println ("AMAPsim.tartinateAction: # AMAPsim trees "+r.trees.size ());

			// Securities
			if (r == null) {
				Log.println (Log.ERROR, "AMAPsim.tartinateAction ()", "Mode2Response is null, did nothing.");
				return;
			}
			if (r.trees == null) {
				Log.println (Log.ERROR, "AMAPsim.tartinateAction ()", "Mode2Response.trees is null, did nothing.");
				return;
			}

			// Iterate on trees in response (they contain an histo of their values)
			for (Iterator i = r.trees.iterator (); i.hasNext ();) {
				AMAPsimTree tree = (AMAPsimTree) i.next ();

				int id = tree.treeId;
	System.out.println ("tree "+id+" # steps "+tree.treeSteps.size ());

				Step stp = refStep;
				TreeList std = (TreeList) refStep.getScene ();

				Tree t = std.getTree (id);

				// Iterate on the response tree histo. Response is in the same order than request : NO SORT - 8.7.2003
				//
				for (Iterator j = tree.treeSteps.iterator (); j.hasNext ();) {
					AMAPsimTreeStep treeStep = (AMAPsimTreeStep) j.next ();
	System.out.println ("  capsis age="+t.getAge ()+" treeStep.age="+treeStep.age);

					// Set amapsim data in the found capsis tree
					//
					AMAPsimRequestableTree at = (AMAPsimRequestableTree) t;

					if (at.getAMAPsimTreeData () == null) {at.setAMAPsimTreeData (new AMAPsimTreeData ());}
					AMAPsimTreeData data = at.getAMAPsimTreeData ();

					data.fileName = tree.fileName;
					data.treeStep = treeStep;

					// Move one step before (several steps can have same date : interventions) - fc - 3.7.2003
					stp = (Step) stp.getFather ();
					if (stp == null) {break;}	// before root step : nothing
					std = (TreeList) stp.getScene ();
					t = std.getTree (id);

				}
			}
			
			String projectName = step.getProject ().getName ();
			MessageDialog.print (this, Translator.swap ("AMAPsim.mode2DataWereTartinatedCorrectlyInProject")+" "+projectName);
			
		} catch (Exception e) {
			Log.println (Log.ERROR, "AMAPsim.tartinateAction ()", "Error during tartination", e);
			MessageDialog.print (this, Translator.swap ("AMAPsim.errorDuringTartinationSeeLog"));
		}
	}

	/**
	 * Called on Escape. Redefinition of method in AmapDialog : ask for user confirmation.
	 */
	protected void escapePressed () {
		if (Question.ask (MainFrame.getInstance (),
				Translator.swap ("AMAPsim.confirm"), Translator.swap ("AMAPsim.confirmClose"))) {
			try {
				client.disconnect ();
			} catch (Exception e) {}
			dispose ();
		}
	}

	// Hello request
	//
	private void helloAction () {
		try  {protocolManager.sendRequest ("hello", null);} catch (Exception e) {
				MessageDialog.print (this, Translator.swap ("AMAPsim.requestError"), e);}
	}

	// Mode1 request
	//
	private void mode1Action () {
		try  {protocolManager.sendRequest ("mode_1", null);} catch (Exception e) {
				MessageDialog.print (this, Translator.swap ("AMAPsim.requestError"), e);}
	}

	// Mode2 request
	//
	private void mode2Action () {
		try  {protocolManager.sendRequest ("mode_2", null);} catch (Exception e) {
				MessageDialog.print (this, Translator.swap ("AMAPsim.requestError"), e);}
	}

	// AskAgain request
	//
	private void askAgainAction (Object params) {
		try  {protocolManager.sendRequest ("ask_again", params);} catch (Exception e) {
				MessageDialog.print (this, Translator.swap ("AMAPsim.requestError"), e);}
	}

	// ForceAnswer request
	//
	private void forceAnswerAction (Object params) {
		try  {protocolManager.sendRequest ("force_answer", params);} catch (Exception e) {
				MessageDialog.print (this, Translator.swap ("AMAPsim.requestError"), e);}
	}

	// CancelRequest request
	//
	private void cancelRequestAction (Object params) {
		try  {protocolManager.sendRequest ("cancel_request", params);} catch (Exception e) {
				MessageDialog.print (this, Translator.swap ("AMAPsim.requestError"), e);}
	}

	// AskStatus request
	//
	private void askStatusAction () {
		try  {protocolManager.sendRequest ("ask_status", null);} catch (Exception e) {
				MessageDialog.print (this, Translator.swap ("AMAPsim.requestError"), e);}
	}

	// Bye request
	//
	private void byeAction () {
		try  {protocolManager.sendRequest ("bye", null);} catch (Exception e) {
				MessageDialog.print (this, Translator.swap ("AMAPsim.requestError"), e);}
	}

	// Prepare connection
	//
	private void connectAction () {

		if (!isConnected ()) {

			// Try to connect
			ConnectionDialog dlg = new ConnectionDialog ();
			if (dlg.isValidDialog ()) {
				InetAddress address = dlg.getAddress ();
				int port = dlg.getPort ();
				
				try {
					client = new AMAPsimClient (address, port, this, protocolManager);	// deals with connection
					
					// Get nextMessageId from server by request type 7
					// settings req7Id to null triggers a request type 7 to get a new req7Id from the server
					// needs connection to be established : here
					// fc - 30.1.2004
					protocolManager.setReq7Id (null);
					
				} catch (Exception e) {
					client = null;
				}	// client printed in our display
			}
			dlg.dispose ();

		} else {
			// Try to disconnect
			//~ protocolManager.sendRequest ("bye", null);
			client.disconnect ();
		}
		updateConnectButton ();
	}

	public Step getStep () {return step;}

	synchronized public void display (final String msg) {
		SwingUtilities.invokeLater (new Runner () {
			public void run () {
				displayArea.setEditable (false);

				displayArea.getCaret ().setVisible (false);
				displayArea.append (msg+"\n");

				int h = displayArea.getPreferredSize ().height;
				displayArea.scrollRectToVisible (new Rectangle (0, h, 10, 10));  // scrolls to see last line if needed

				StatusDispatcher.print (msg);
			}
		});
	}

	// Change connect button text according to connection current status
	//
	private void updateConnectButton () {
		if (!isConnected ()) {
			connect.setText (Translator.swap ("AMAPsim.connect"));
			connect.validate ();
		} else {
			connect.setText (Translator.swap ("AMAPsim.disconnect"));
			connect.validate ();
		}
	}

	/**	Lock requests which need an id during request 7.
	*	When response to request 7 arrives, unlock (see ProtocolManager).
	*	fc - 30.1.2004
	*/
	synchronized public void lockRequestsNeedingAnId (boolean locked) {
		final boolean l = locked;
		SwingUtilities.invokeLater (new Runnable () {
			public void run () {	// must be executed by swing thread
				mode1.setEnabled (!l);
				mode2.setEnabled (!l);
			}
		});
	}
	
	/**	User interface defintion
	*/
	private void createUI () {
		JPanel mainPanel = new JPanel ();
		mainPanel.setLayout (new BorderLayout ());

		// 1. First tab : connection
		JPanel part1 = new JPanel (new GridLayout (9, 1));

		connect = new JButton (Translator.swap ("AMAPsim.connect"));
		connect.addActionListener (this);
		part1.add (connect);

		hello = new JButton (Translator.swap ("AMAPsim.hello"));
		hello.addActionListener (this);
		part1.add (hello);

		mode1 = new JButton (Translator.swap ("AMAPsim.mode1"));
		mode1.addActionListener (this);
		part1.add (mode1);

		mode2 = new JButton (Translator.swap ("AMAPsim.mode2"));
		mode2.addActionListener (this);
		part1.add (mode2);

		askAgain = new JButton (Translator.swap ("AMAPsim.askAgain"));
		askAgain.addActionListener (this);
		part1.add (askAgain);

		forceAnswer = new JButton (Translator.swap ("AMAPsim.forceAnswer"));
		forceAnswer.addActionListener (this);
		part1.add (forceAnswer);

		cancelRequest = new JButton (Translator.swap ("AMAPsim.cancelRequest"));
		cancelRequest.addActionListener (this);
		part1.add (cancelRequest);

		askStatus = new JButton (Translator.swap ("AMAPsim.askStatus"));
		askStatus.addActionListener (this);
		part1.add (askStatus);

		bye = new JButton (Translator.swap ("AMAPsim.bye"));
		bye.addActionListener (this);
		part1.add (bye);


		ColumnPanel filler = new ColumnPanel (0, 0);
		filler.add (part1);
		filler.addGlue ();
		mainPanel.add (filler, BorderLayout.EAST);

		// 2. Tables
		table1 = new JTable ();
		table2 = new JTable ();
		
		table1.setSelectionMode (ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);	// = not SINGLE_SELECTION
		table2.setSelectionMode (ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		table1.addMouseListener (this);
		table2.addMouseListener (this);

		scroll1 = new JScrollPane (table1);
		scroll2 = new JScrollPane (table2);
		update ();		// inits tables with ProtocolManager

		JTabbedPane tabSet = new JTabbedPane ();
		ColumnPanel tab1 = new ColumnPanel ();
		ColumnPanel tab2 = new ColumnPanel ();
		tab1.add (scroll1);
		tab2.add (scroll2);
		//~ tab1.addStrut0 ();
		//~ tab2.addStrut0 ();
		tab1.addGlue ();
		tab2.addGlue ();
		tabSet.addTab (Translator.swap ("AMAPsim.pendingRequests"), tab1);
		tabSet.addTab (Translator.swap ("AMAPsim.receivedResponses"), tab2);
		tabSet.setPreferredSize (new Dimension (500, 200));
		//~ mainPanel.add (tabSet, BorderLayout.CENTER);

		// 3. Display
		displayArea = new JTextArea ();
		displayArea.setEditable (false);		// for the moment
		//~ displayArea.setPreferredSize (new Dimension (500, 200));

		JScrollPane displayScrollPane = new JScrollPane (displayArea);
		displayScrollPane.setPreferredSize (new Dimension (500, 150));
		//~ mainPanel.add (displayScrollPane, BorderLayout.SOUTH);


		// A JSplitPane
		JSplitPane splitPane = new JSplitPane (JSplitPane.VERTICAL_SPLIT, true, tabSet, displayScrollPane);
		splitPane.setDividerLocation (0.75);
		mainPanel.add (splitPane, BorderLayout.CENTER);


		// 4. Control panel
		JPanel pControl = new JPanel (new FlowLayout (FlowLayout.RIGHT));
		close = new JButton (Translator.swap ("Shared.close"));
		close.addActionListener (this);
		help = new JButton (Translator.swap ("Shared.help"));
		help.addActionListener (this);
		pControl.add (close);
		pControl.add (help);

		// set close as default (see AmapDialog) - bad idea - fc - 3.2.2004
		//~ close.setDefaultCapable (true);
		//~ getRootPane ().setDefaultButton (close);

		// layout parts
		getContentPane ().add (mainPanel, BorderLayout.CENTER);
		getContentPane ().add (pControl, BorderLayout.SOUTH);

	}


	/**
	 * From MouseListener interface.
	 */
	public void mouseClicked (MouseEvent mouseEvent) {
//~ System.out.println ("  mouseClicked");
	}

	/**
	 * From MouseListener interface.
	 */
	public void mousePressed (MouseEvent m) {
		Object obj = m.getSource ();
		if (obj instanceof JTable) {
			JTable table = (JTable) obj;

			//~ if (m.isPopupTrigger ()) {	// fc - 4.2.2003 (failed under windows)
			if ((m.getModifiers () & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {

				int x = m.getX ();
				int y = m.getY ();
				int row = table.rowAtPoint (new Point (x, y));

				// Ensure some selection was made
				if (!table.isRowSelected (row)) {
					table.getSelectionModel ().setSelectionInterval (row, row);
				}

				if (table.equals (table1)) {
					int numRow = table1.getSelectedRow ();
					String date = (String) table1.getModel ().getValueAt (numRow, 0);
					String messageId = (String) table1.getModel ().getValueAt (numRow, 1);
					String key = messageId+" "+date;
					String messageName = (String) table1.getModel ().getValueAt (numRow, 2);	// fc - 13.10.2003
					int messageType = Request.getRequestCode (messageName);
					JPopupMenu popup = new TablePopup (table, this, TablePopup.REQUEST_TABLE,
							key, messageType);
					popup.show (m.getComponent (), m.getX (), m.getY ());

				} else {
					int numRow = table2.getSelectedRow ();
					String date = (String) table2.getModel ().getValueAt (numRow, 1);
					String messageId = (String) table2.getModel ().getValueAt (numRow, 2);
					String key = messageId+" "+date;
					String messageName = (String) table2.getModel ().getValueAt (numRow, 3);	// was 1 - fc - 3.2.2004
					int messageType = Request.getRequestCode (messageName);
					JPopupMenu popup = new TablePopup (table, this, TablePopup.RESPONSE_TABLE,
							key, messageType);
					popup.show (m.getComponent (), m.getX (), m.getY ());

				}
			}
		}
		//~ if (m.isControlDown ()) {
		//~ }
	}

	/**
	 * From MouseListener interface.
	 */
	public void mouseReleased (MouseEvent mouseEvent) {
		//~ Object obj = mouseEvent.getSource ();
		//~ if (obj instanceof JTable) {
			//~ if (mouseEvent.isPopupTrigger ()) {
//~ String name = (obj.equals (table1)) ? "table1" : "table2";
//~ System.out.println ("  left clic (mouseReleased) on "+name);
			//~ }
		//~ }
		//~ if (mouseEvent.isControlDown ()) {
		//~ }
	}

	/**
	 * From MouseListener interface.
	 */
	public void mouseEntered (MouseEvent mouseEvent) {
		Object obj = mouseEvent.getSource ();
		if (obj instanceof JTable) {
		}

	}

	/**
	 * From MouseListener interface.
	 */
	public void mouseExited (MouseEvent mouseEvent) {
		Object obj = mouseEvent.getSource ();
		if (obj instanceof JTable) {
		}
	}



}


