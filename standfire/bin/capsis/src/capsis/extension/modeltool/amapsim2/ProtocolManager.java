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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import jeeb.lib.util.Log;
import jeeb.lib.util.TicketDispenser;
import capsis.kernel.PathManager;
import capsis.util.SwapDataInputStream;

/**
 *
 * @author F. de Coligny - november 2002
 */
public class ProtocolManager {
	public static final int TECHNICAL_HEADER_SIZE = 17;	// 4+5+4+4 bytes
	private static String requestsFileName = PathManager.getDir("etc")+File.separator+"amapsim.pendingRequests";
	private static String responsesFileName = PathManager.getDir("etc")+File.separator+"amapsim.receivedResponses";
	
	static private NumberFormat formater;
	static private TicketDispenser messageIdDispenser;
		
	private SwapDataInputStream in;
	private AMAPsimCaller caller;
	private AMAPsimClient client;

	private Map pendingRequests;	// request key-> request
	private Map receivedResponses;	// response key -> response

	private boolean verbose = true;	// talks a lot

	// This id is used as messageId for calculation requests (mode1, mode2)
	// fc - 30.1.2004
	private String req7Id;	// we get it by response from server to request 7
	synchronized public void setReq7Id (String id) {
		req7Id = id;
		if (req7Id == null) {
			caller.lockRequestsNeedingAnId (true);
			// Get nextMessageId from server by request type 7
			// fc - 30.1.2004
			try  {this.sendRequest ("id_request", null);} 
				catch (Exception e) {
				Log.println (Log.ERROR, "ProtocolManager.setReq7Id (null)",
					"Error while sending request 7 to get an id");}
		} else {
			caller.lockRequestsNeedingAnId (false);
		}
	}
	synchronized public String getReq7Id () {return req7Id;}
	
	
	static {
		formater = NumberFormat.getNumberInstance ();
		formater.setMinimumIntegerDigits (5);
		formater.setMaximumIntegerDigits (5);
		formater.setGroupingUsed (false);
		formater.setMinimumFractionDigits (0);
		formater.setMaximumFractionDigits (0);
	}

	/**
	 * ProtocolManager (PM) is created by an AMAPsimCaller.
	 * The latter then creates an AMAPsimClient, giving it the PM.
	 * The client uses setClient () to declare itself to the PM.
	 * Caller sends requests to the PM which creates them and store 
	 * them in the client requests stack.
	 * Client sends server responses to the PM for interpretation, 
	 * storage and Caller notification (Caller.update ()). Caller can then have access
	 * to the PM lists to update itself.
	 */
	public ProtocolManager (AMAPsimCaller caller) {
		this.caller = caller;
		
		restoreMaps ();
		
	}

	/**
	 * Server response interpretation.
	 */
	public void interpret (SwapDataInputStream in) {
		this.in = in;
		Response response = null;
		String trace = "";
		try {
			
			if (in.available () < TECHNICAL_HEADER_SIZE) {return;}	// not enough data to interpret, still wait a little
			
			in.mark (Integer.MAX_VALUE);
			
			// Read technical header to understand what this response is about
			int dataLength = in.readInt ();
			StringBuffer sb = new StringBuffer ();
			for (int i = 0; i < 5; i++) {
				sb.append ((char) in.readByte ());
			}
			String messageId = sb.toString ();
			int requestType = in.readInt ();
			int returnCode = in.readInt ();
			
			trace = "dataLength "+dataLength+" messageId "+messageId+" requestType "+requestType+" returnCode "+returnCode;
//~ System.out.println ("response header:["+dataLength+"] ["+messageId+"] ["+requestType+"] ["+returnCode+"]");
			
			in.reset ();
			
			switch (requestType) {
				case 0 :	// response to hello
					response = new HelloResponse (in);
					if (verbose) caller.print ("("+response.getMessageId ()+") Received hello response ("+dataLength+" bytes) <-");
					break;
					
				case 1 :	// response to mode_1
					response = new Mode1Response (in);
					if (verbose) caller.print ("("+response.getMessageId ()+") Received mode_1 response ("+dataLength+" bytes) <-");
					break;
					
				case 2 :	// response to mode_2
					response = new Mode2Response (in);
					if (verbose) caller.print ("("+response.getMessageId ()+") Received mode_2 response ("+dataLength+" bytes) <-");
					break;
					
				//~ case 3 :	// response to ask_again should be a mode_1 or mode_2
					//~ response = new AskAgainResponse (in);
					//~ caller.print ("("+response.getMessageId ()+") Received ask_again response ("+dataLength+" bytes) <-");
					//~ break;
					
				//~ case 4 :	// response to force_answer should be a mode_1 or mode_2
					//~ caller.print ("("+response.getMessageId ()+") Received force_answer response ("+dataLength+" bytes) <-");
					//~ break;
					
				case 5 :	// response to cancel_request
					response = new CancelRequestResponse (in);
					if (verbose) caller.print ("("+response.getMessageId ()+") Received cancel_request response ("+dataLength+" bytes) <-");
					break;
					
				case 6 :	// response to ask_status
					response = new AskStatusResponse (in);
					String status = ((AskStatusResponse) response).getStatus ();
					if (verbose) caller.print ("("+response.getMessageId ()+") Received ask_status response ("+dataLength+" bytes) <-");
					caller.print (""+status);
					break;
					
				case 7 :	// response to id_request
					response = new IdRequestResponse (in);
					
					String id = ((IdRequestResponse) response).getReq7Id ();	// new - fc - 3.2.2004
					
					setReq7Id (id);		// id != null => unlock mode1 and mode2
					//~ if (verbose) caller.print ("("+response.getMessageId ()+") Received id_request response ("+dataLength+" bytes) <-");
					break;
					
				case 9 :	// response to bye request
					response = new ByeResponse (in);
					if (verbose) caller.print ("("+response.getMessageId ()+") Received bye response ("+dataLength+" bytes) <-");
					
					client.disconnect ();	// disconnect
					break;
					
				default :
					caller.print ("Unknown response (requestType="+requestType+"): disconnect");
					client.disconnect ();	// unknown response : disconnect
					break;
					
			}
			
			Date now = new Date ();		// fc - 29.1.2004
			response.setDate (now);		// fc - 29.1.2004
			response.setServerId (client.getConnection ());	// Socket : IPaddr + port number = server id
			
			// IdRequestResponse are not stored (technical request)
			if (!(response instanceof IdRequestResponse)) {
				memoResponse (response);
			}
			caller.update ();	// hey guy, you have mail !
			
		} catch (java.io.InterruptedIOException e) {	// read timeout brings here
			caller.print ("Can not interpret response ["+trace+"], "+e);
		} catch (Throwable t) {
			caller.print ("Can not interpret response ["+trace+"], "+t);
			Log.println (Log.ERROR, "ProtocolManager.interpret ()", 
					"Can not intrepret response due to: ", t);
			client.disconnect ();	// unknown response : disconnect
			return;
		}
		
	}

	public boolean isConnected () {
		return (caller != null && caller.isConnected ());
	}

	public Request sendRequest (String requestName, Object params) throws Exception {
		Request request = null;
		
		if (!isConnected ()) {
			caller.print ("Not connected");
			return null;
		}
		
		if (requestName == null) {
			caller.print ("Can not make request, requestName is null");
			
		} else if (requestName.equals ("hello")) {
			request = new Hello (params);	// if params == null, a dialog box may be used
			if (request.isCanceled ()) {return null;}
			if (verbose) caller.print ("("+request.getMessageId ()+") Sending hello request ("+request.getDataLength ()+" bytes)...");
			
		} else if (requestName.equals ("mode_1")) {
			request = new Mode1 (params, caller.getStep (), getReq7Id ());	// if params == null, a dialog box may be used
			if (request.isCanceled ()) {return null;}
			if (verbose) caller.print ("("+request.getMessageId ()+") Sending mode_1 request ("+request.getDataLength ()+" bytes)...");
		
		} else if (requestName.equals ("mode_2")) {
			request = new Mode2 (params, caller.getStep (), getReq7Id ());	// if params == null, a dialog box may be used
			if (request.isCanceled ()) {return null;}
			if (verbose) caller.print ("("+request.getMessageId ()+") Sending mode_2 request ("+request.getDataLength ()+" bytes)...");
		
		} else if (requestName.equals ("ask_again")) {
			request = new AskAgain (params);	// if params == null, a dialog box may be used
			if (request.isCanceled ()) {return null;}
					// fc - 4.2.2004 - several requests from one single dialog
					String[] otherMessageIds = ((AskAgain) request).otherMessageIds;
					if (otherMessageIds != null) {
						for (int i = 0; i < otherMessageIds.length; i++) {
							sendRequest ("ask_again", otherMessageIds[i]);
						}
					}
			if (verbose) caller.print ("("+request.getMessageId ()+") Sending ask_again request ("+request.getDataLength ()+" bytes)...");
		
		} else if (requestName.equals ("force_answer")) {
			request = new ForceAnswer (params);	// if params == null, a dialog box may be used
			if (request.isCanceled ()) {return null;}
					// fc - 4.2.2004 - several requests from one single dialog
					String[] otherMessageIds = ((ForceAnswer) request).otherMessageIds;
					if (otherMessageIds != null) {
						for (int i = 0; i < otherMessageIds.length; i++) {
							sendRequest ("force_answer", otherMessageIds[i]);
						}
					}
			if (verbose) caller.print ("("+request.getMessageId ()+") Sending force_answer request ("+request.getDataLength ()+" bytes)...");
		
		} else if (requestName.equals ("cancel_request")) {
			request = new CancelRequest (params);	// if params == null, a dialog box may be used
			if (request.isCanceled ()) {return null;}
					// fc - 4.2.2004 - several requests from one single dialog
					String[] otherMessageIds = ((CancelRequest) request).otherMessageIds;
					if (otherMessageIds != null) {
						for (int i = 0; i < otherMessageIds.length; i++) {
							sendRequest ("cancel_request", otherMessageIds[i]);
						}
					}
			if (verbose) caller.print ("("+request.getMessageId ()+") Sending cancel_request request ("+request.getDataLength ()+" bytes)...");
		
		} else if (requestName.equals ("ask_status")) {
			request = new AskStatus (params);	// if params == null, a dialog box may be used
			if (request.isCanceled ()) {return null;}
			if (verbose) caller.print ("("+request.getMessageId ()+") Sending ask_status request ("+request.getDataLength ()+" bytes)...");
		
		} else if (requestName.equals ("id_request")) {
			request = new IdRequest (params);	// if params == null, a dialog box may be used
			if (request.isCanceled ()) {return null;}
			//~ if (verbose) caller.print ("("+request.getMessageId ()+") Sending id_request request ("+request.getDataLength ()+" bytes)...");
		
		} else if (requestName.equals ("bye")) {
			request = new Bye (params);	// if params == null, a dialog box may be used
			if (request.isCanceled ()) {return null;}
			if (verbose) caller.print ("("+request.getMessageId ()+") Sending bye request ("+request.getDataLength ()+" bytes)...");
		
		} else {
			caller.print ("Unknown request: "+requestName);
			
		}
		
		if (!request.isCanceled ()) {
			
			Date now = new Date ();		// fc - 29.1.2004
			request.setDate (now);		// fc - 29.1.2004
			
			client.storeRequest (request);
			
			// fc - 30.1.2004
			if (request instanceof Mode1 || request instanceof Mode2) {
				setReq7Id (null);	// trigger new request 7 to get a new Id for next mode1/mode2
			}
			
			// IdRequests are not stored (technical request)
			if (!(request instanceof IdRequest)) {
				memoRequest (request);
			}
			
			caller.update ();
		}
		
		return request;
		
	}

	public void removeRequests (Collection keys) {
		for (Iterator i = keys.iterator (); i.hasNext ();) {
			String key = (String) i.next ();
			pendingRequests.remove (key);
		}
		saveMaps ();
	}

	public void removeResponses (Collection keys) {
		for (Iterator i = keys.iterator (); i.hasNext ();) {
			String key = (String) i.next ();
			receivedResponses.remove (key);
		}
		saveMaps ();
	}
	
	private void memoRequest (Request r) {
		pendingRequests.put (r.getKey (), r);
		saveMaps ();
	}
	
	private void memoResponse (Response r) {
		if (r == null) {return;}
		
		// Link related request (same messageId) into response
		//
		Request req = null;
		boolean found = false;
		Iterator i = pendingRequests.values ().iterator ();
		while (!found && i.hasNext ()) {
			req = (Request) i.next ();
			if (req.getMessageId ().equals (r.getMessageId ())) {found = true;}
		}
		if (found) {
			r.setRelatedRequest (req);
			pendingRequests.remove (req.getKey ());	// delete request from requests table
		}
		
		receivedResponses.put (r.getKey (), r);	// add response in responses table
		saveMaps ();
	}
	
	public Map getPendingRequests () {return pendingRequests;}
	public Map getReceivedResponses () {return receivedResponses;}
	

	public void setClient (AMAPsimClient client) {
		this.client = client;
	}

	private void saveMaps () {
		try {
			ObjectOutputStream out = new ObjectOutputStream (
					new BufferedOutputStream (
					new FileOutputStream (requestsFileName)));
			out.writeObject (pendingRequests);
			out.close ();	// also flushes output
		} catch (java.io.IOException exc) {
			Log.println (Log.ERROR, "ProtocolManager.saveMaps ()",
					"Unable to write pending requests to disk."
					+" Target file = "+requestsFileName, exc);
		}
		
		try {
			ObjectOutputStream out = new ObjectOutputStream (
					new BufferedOutputStream (
					new FileOutputStream (responsesFileName)));
			out.writeObject (receivedResponses);
			out.close ();	// also flushes output
		} catch (java.io.IOException exc) {
			Log.println (Log.ERROR, "ProtocolManager.saveMaps ()",
					"Unable to write received responses to disk."
					+" Target file = "+responsesFileName, exc);
		}
	}

	private void restoreMaps () {
		pendingRequests = new TreeMap ();
		receivedResponses = new TreeMap ();
		
		try {
			ObjectInputStream in = new ObjectInputStream (
					new BufferedInputStream (
					new FileInputStream (requestsFileName)));
			pendingRequests = (Map) in.readObject ();
			in.close ();
		} catch (java.io.FileNotFoundException exc) {
			Log.println (Log.WARNING, "ProtocolManager.restoreMaps ()", "File not found: "+requestsFileName);
		} catch (Exception exc) {
			Log.println (Log.ERROR, "ProtocolManager.restoreMaps ()",
					"Error while trying to read pending requests. Source file = "
					+requestsFileName, exc);
		} 
		
		try {
			ObjectInputStream in = new ObjectInputStream (
					new BufferedInputStream (
					new FileInputStream (responsesFileName)));
			receivedResponses = (Map) in.readObject ();
			in.close ();
		} catch (java.io.FileNotFoundException exc) {
			Log.println (Log.WARNING, "ProtocolManager.restoreMaps ()", "File not found: "+responsesFileName);
		} catch (Exception exc) {
			Log.println (Log.ERROR, "ProtocolManager.restoreMaps ()",
					"Error while trying to read received responses. Source file = "
					+responsesFileName, exc);
		} 
	}

}


