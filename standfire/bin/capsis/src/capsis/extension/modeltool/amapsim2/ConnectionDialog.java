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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.InetAddress;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import jeeb.lib.util.AmapDialog;
import jeeb.lib.util.Check;
import jeeb.lib.util.MessageDialog;
import jeeb.lib.util.JWidthLabel;
import jeeb.lib.util.Log;
import jeeb.lib.util.Settings;
import jeeb.lib.util.Translator;
import capsis.commongui.util.Helper;

/**
 * A connection tool for pp3 model (maritime pine) and ConnectionDialog.
 * This tool is for crown shape computation using ConnectionDialog for some trees 
 * computed in pp3 module.
 *
 * @author F. de Coligny - july 2001
 */
public class ConnectionDialog extends AmapDialog implements ActionListener {

	private JButton ok;
	private JButton cancel;
	private JButton help;
	
	private JTextField localIpAddress;
	private JTextField ipAddress;
	private JTextField port;
	
	private InetAddress hostAddress;
	private int hostPort;


	/**
	 * Constructor.
	 */
	public ConnectionDialog () {
		super ();
		
		setTitle (Translator.swap ("ConnectionDialog"));
		
		createUI ();
		
		setDefaultCloseOperation (DO_NOTHING_ON_CLOSE);
		addWindowListener (new WindowAdapter () {
			public void windowClosing(WindowEvent e) {
				escapePressed ();
			}
		});
		
		pack ();
		//~ setVisible (true);
		setModal (true);
		
		show ();	
	}


	/** 
	 * Called on ok.
	 */
	public void okAction () {
		// check ip address
		if (ipAddress.getText ().trim ().length () == 0) {
			MessageDialog.print (this, Translator.swap ("ConnectionDialog.ipAddressNeeded"));
			return;
		}
		
		Settings.setProperty ("amapsimHostName", ipAddress.getText ().trim ());
		
		InetAddress address = null;
		try {
			address = InetAddress.getByName (ipAddress.getText ().trim ()); // <<<<<<<<<<<<<<<<<
		} catch (Throwable t) {
			Log.println (Log.ERROR, "ConnectionDialog.okAction ()", "Wrong Format for InetAddress", t);
			MessageDialog.print (this, Translator.swap ("ConnectionDialog.ipAddressWrongFormat"));
			return;
		}
		
		// check port number
		if (port.getText ().trim ().length () == 0) {
			MessageDialog.print (this, Translator.swap ("ConnectionDialog.portNeeded"));
			return;
		}
		
		if (!Check.isInt (port.getText ().trim ())) {
			MessageDialog.print (this, Translator.swap ("ConnectionDialog.portMustBeAnInteger"));
			return;
		}
		
		Settings.setProperty ("amapsimPort", port.getText ().trim ());
		
		int p = Check.intValue (port.getText ().trim ());
		if ((p < 0) || (p > 65535)) {
			MessageDialog.print (this, Translator.swap ("ConnectionDialog.portMustBeAnInteger"));
			return;
		}
		
		hostAddress = address;
		hostPort = p;
		
		setValidDialog (true);
	}


	public InetAddress getAddress () {return hostAddress;}
	public int getPort () {return hostPort;}


	/** 
	 * From ActionListener interface.
	 */
	public void actionPerformed (ActionEvent evt) {
		if (evt.getSource ().equals (ok)) {
			okAction ();
			
		} else if (evt.getSource ().equals (cancel)) {
			escapePressed ();
			
		} if (evt.getSource ().equals (help)) {
			Helper.helpFor (this);
			
		} 
	}

	/**
	 * Called on Escape. Redefinition of method in AmapDialog : ask for user confirmation.
	 */
	protected void escapePressed () {
		//~ if (Question.isTrue (Translator.swap ("ConnectionDialog.confirm"), 
				//~ Translator.swap ("ConnectionDialog.confirmClose"))) {
			setValidDialog (false);
		//~ }
	}

	/** 
	 * User interface defintion
	 */
	private void createUI () {
		
		JPanel mainPanel = new JPanel ();
		mainPanel.setLayout (new BorderLayout ());
		
		// 1. First tab : connection
		JPanel part1 = new JPanel ();
		part1.setLayout (new BoxLayout (part1, BoxLayout.Y_AXIS));
		
		JPanel l1 = new JPanel (new FlowLayout (FlowLayout.LEFT));
		l1.add (new JWidthLabel (Translator.swap ("ConnectionDialog.IPAddress")+" :", 120));
		ipAddress = new JTextField (10);
		l1.add (ipAddress);
		
		JPanel l2 = new JPanel (new FlowLayout (FlowLayout.LEFT));
		l2.add (new JWidthLabel (Translator.swap ("ConnectionDialog.port")+" :", 120));
		port = new JTextField (5);
		l2.add (port);
		
		// default : jeff's host
		//ipAddress.setText ("indy9.cirad.fr");
		ipAddress.setText (Settings.getProperty ("amapsimHostName", ""));	// ex : "indy9.cirad.fr", "pcps-178.cirad.fr"
		//port.setText ("1025");
		port.setText (Settings.getProperty ("amapsimPort", ""));	// ex : "1025", "32781"
		
		part1.add (l1);
		part1.add (l2);
		
		mainPanel.add (part1);
		
		
		
		// 4. Control panel
		JPanel pControl = new JPanel (new FlowLayout (FlowLayout.RIGHT));
		ok = new JButton (Translator.swap ("Shared.ok"));
		ok.addActionListener (this);
		cancel = new JButton (Translator.swap ("Shared.cancel"));
		cancel.addActionListener (this);
		help = new JButton (Translator.swap ("Shared.help"));
		help.addActionListener (this);
		pControl.add (ok);
		pControl.add (cancel);
		pControl.add (help);
		
		// set ok as default (see AmapDialog)
		ok.setDefaultCapable (true);
		getRootPane ().setDefaultButton (ok);
		
		// layout parts
		getContentPane ().add (mainPanel, BorderLayout.CENTER);
		getContentPane ().add (pControl, BorderLayout.SOUTH);
		
	}
	
}


