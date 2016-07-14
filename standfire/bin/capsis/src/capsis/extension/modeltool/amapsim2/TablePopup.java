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

import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;

import jeeb.lib.util.Translator;

/**
 * TablePopup is a JPopupMenu.
 * 
 * @author F. de Coligny - december 2002
 */
public class TablePopup extends JPopupMenu {
	public static final int REQUEST_TABLE = 1;
	public static final int RESPONSE_TABLE = 2;
	
	public static final int MODE_1 = 1;
	public static final int MODE_2 = 2;
	
	public static final int DELETE_ROW = 200;
	public static final int TAKE_FOR_MODEL = 2;
	public static final int CREATE_PROJECT = 3;
	public static final int TARTINATE = 4;
	public static final int INSPECT = 100;
	
	public static final int ASK_AGAIN = 11;
	public static final int FORCE_ANSWER = 12;
	public static final int CANCEL_REQUEST = 13;
	
	private JTable table;
	private ActionListener listener;


	public TablePopup (JTable table, ActionListener listener, int tableType, String messageId, int messageType) {
		super ();
		this.table = table;
		this.listener = listener;
		
		boolean multipleSelection = false;
		if (table.getSelectedRows ().length > 1) {multipleSelection = true;}
		
		if (!multipleSelection) {
			// INSPECT + tableType = 101 : REQUEST_TABLE, 102 : RESPONSE_TABLE
			JMenuItem inspect = new JMenuItem (Translator.swap ("AMAPsim.inspect"), INSPECT + tableType);
			inspect.addActionListener (listener);
			add (inspect);
			
			if (tableType == RESPONSE_TABLE) {	// responses table
				// CREATE_PROJECT
				if (messageType == MODE_1) {
					JMenuItem createProject = new JMenuItem (Translator.swap ("AMAPsim.createProject"), CREATE_PROJECT);
					createProject.addActionListener (listener);
					add (createProject);
				}
				
				// TARTINATE
				if (messageType == MODE_2) {
					JMenuItem tartinate = new JMenuItem (Translator.swap ("AMAPsim.tartinate"), TARTINATE);
					tartinate.addActionListener (listener);
					add (tartinate);
				}
				
			} else {		// requests table
				// ASK_AGAIN
				JMenuItem askAgain = new JMenuItem (Translator.swap ("AMAPsim.askAgain"), ASK_AGAIN);
				askAgain.addActionListener (listener);
				add (askAgain);
				
				// FORCE_ANSWER
				JMenuItem forceAnswer = new JMenuItem (Translator.swap ("AMAPsim.forceAnswer"), FORCE_ANSWER);
				forceAnswer.addActionListener (listener);
				add (forceAnswer);
				
				// CANCEL_REQUEST
				JMenuItem cancelRequest = new JMenuItem (Translator.swap ("AMAPsim.cancelRequest"), CANCEL_REQUEST);
				cancelRequest.addActionListener (listener);
				add (cancelRequest);
				
			}
			
			if (getSubElements ().length != 0) {
				addSeparator ();
			}
			
			// TAKE FOR MODEL	// fc - 13.10.2003
			if (messageType == 1 || messageType == 2) {
				JMenuItem takeForModel = new JMenuItem (Translator.swap ("AMAPsim.takeForModel"), TAKE_FOR_MODEL);
				takeForModel.addActionListener (listener);
				takeForModel.setActionCommand (""+tableType);
				add (takeForModel);
			}
				
		}
			
		// DELETE_ROW + tableType = 201 : REQUEST_TABLE, 202 : RESPONSE_TABLE
		// multiple selection allowed for delete row(s)
		JMenuItem delete = new JMenuItem (Translator.swap ("AMAPsim.delete"), DELETE_ROW + tableType);
		delete.addActionListener (listener);
		add (delete);
		
		
	}

}
