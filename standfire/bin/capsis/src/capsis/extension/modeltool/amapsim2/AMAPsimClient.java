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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.LinkedList;

import jeeb.lib.util.Log;
import capsis.util.SwapDataInputStream;
import capsis.util.SwapDataOutputStream;

/**
 * A connection tool for pp3 model (maritime pine) and AMAPsim.
 * This tool is for crown shape computation using AMAPsim for some trees 
 * computed in pp3 module.
 *
 * @author F. de Coligny - november 2002
 */
public class AMAPsimClient extends Thread {

	private LinkedList requests;	// for FIFO stack use
	
	private Socket connection = null;
	private SwapDataOutputStream out;
	private SwapDataInputStream in;

	private AMAPsimCaller caller;

	private ProtocolManager protocolManager;

	private boolean endOfConnection;

	/**
	 * Constructor.
	 */
	public AMAPsimClient (InetAddress address, int port, 
			AMAPsimCaller caller, ProtocolManager protocolManager) throws Exception {
		this.caller = caller;
		this.protocolManager = protocolManager;
		protocolManager.setClient (this);
		
		connect (address, port);
		// if connection failed, an exception is thrown by connect : exit here
		
		endOfConnection = false;
		this.start ();
		
	}

	// Connection management
	//
	public void run () {
											//~ System.out.println ("AMAPsimClient: connection ok, entering run ()");
		try {
			
			while (! endOfConnection) {
				if (in.available () != 0) {	// listen to server
											//~ System.out.println ("AMAPsimClient: received response ("+in.available ()+" bytes), passing it to ProtocolManager...");
					protocolManager.interpret (in);
				}
				while (isRequestPending ()) {	// send requests if available
					Request r = nextRequest ();
											//~ System.out.println ("AMAPsimClient: sending request "+r);
					r.write (out);
				}
				// wait some seconds
				sleep (100);	// 100 milliseconds = 0.1s.
			}
											//~ System.out.println ("AMAPsimClient: endOfConnection, exiting run() normally");
			//~ connection.close ();
			//~ connection = null;
			//~ caller.update ();
			//~ caller.print ("Connection closed");
			
		} catch (Throwable t) {
											//~ System.out.println ("AMAPsimClient: Exception in run(): end of loop ("+t+")");
		} finally {
			try {
				connection.close ();
				connection = null;
				caller.update ();
				caller.print ("Connection closed");
			} catch (Exception e) {}
		}
		
	}

	synchronized private Request nextRequest () {	// internal use only, get next request to be sent to the server
		if (requests == null || requests.size () == 0) {return null;}
		Request r = (Request) requests.getFirst ();
		requests.removeFirst ();
		return r;
	}
	
	synchronized private boolean isRequestPending () {	// internal use only
		if (requests == null || requests.size () == 0) {return false;}
		return true;
	}

	synchronized public void storeRequest (Request r) {	// used by ProtocolManager to add user requests
		if (r == null) {return;}
		if (requests == null) {
			requests = new LinkedList ();
		}
		requests.addLast (r);
	}

	// Connection
	//
	private void connect (InetAddress address, int port) throws Exception {
		try {
			caller.print ("Openning connection on "+address+":"+port+"...");
			
			connection = new Socket (address, port);
			connection.setSoTimeout (2000);		// a read can not block more than 2s. (java.io.InterruptedIOException)
			caller.print ("Connected");
			
			out = new SwapDataOutputStream (new BufferedOutputStream (connection.getOutputStream ()));
			in = new SwapDataInputStream (new BufferedInputStream (connection.getInputStream ()));
			
			String serverWelcome = Response.readString (in);
			caller.print (serverWelcome);
			
		} catch (Throwable t) {
			caller.print ("Connection failed ("+t+")");
			Log.println (Log.ERROR, "AMAPsimClient.connect ()", "Connection failed due to: ", t);
			if (in != null) {in.close ();}
			if (out != null) {out.close ();}
			if (connection != null) {
				connection.close ();
				connection = null;
			}
			throw new Exception (""+t);
		}	
	}
	
	// Disconnection
	//
	synchronized public void disconnect () {
		if (!endOfConnection) {	// normal way
			endOfConnection = true;
			
		} else {	// second time (abnormal) : force
			try {
				connection.close ();
				connection = null;
				caller.update ();
				caller.print ("Connection closed");
			} catch (Exception e) {}
		}
	}
	
	public boolean isConnected () {return connection != null;}
	public Socket getConnection () {return connection;}		// fc - 29.1.2004
	
}


