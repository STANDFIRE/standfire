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

import java.io.IOException;

import capsis.util.SwapDataOutputStream;

/**
 * AskAgain request for AMAPsim client/server connection.
 * 
 * @author F. de Coligny - november 2002
 */
public class AskAgain extends Request {
	
	// Data to be sent to server
	public int dataLength;		// this field is not counted in dataLength	
	public String messageId;	// 5 chars
	public int requestType;		// 3 = AskAgain
	
	// Dialog may accept several messageIds. This request processes the first one.
	// if otherIds != null, ProtocolManager will send one more request for each other id, 
	// without dialog opening.
	//
	public String[] otherMessageIds;	// fc - 4.2.2004 - if not null, these ids will be sent in other requests (not in write ())

	
	public AskAgain (Object params) {
		
		// Hard coded data
		dataLength = 9;
		requestType = 3;
		
		// Parametrable data
		if (params != null) {
			messageId = (String) params;
		} else {
			AskAgainDialog dlg = new AskAgainDialog (this);
			dlg.dispose ();
			//~ System.out.println (toString ());	// what was selected in dialog
		}
		
	}
	
	public void write (SwapDataOutputStream out) throws IOException {
		out.writeInt (dataLength);
		out.writeBytes (messageId);		// 5 bytes for 5 chars, NO NULL after (known length)
		out.writeInt (requestType);
		out.flush ();
	}

	public int getDataLength () {return dataLength;}
	public String getMessageId () {return messageId;}
	public int getRequestType () {return requestType;}
	public String toString () {
		StringBuffer b = new StringBuffer ("AskAgain:");
		b.append (" dataLength=");
		b.append (dataLength);
		b.append (" messageId=");
		b.append (messageId);
		b.append (" requestType=");
		b.append (requestType);
		b.append (" otherMessageIds=[");
		if (otherMessageIds == null) {
			b.append ("null");
		} else {
			for (int i = 0; i < otherMessageIds.length; i++) {
				b.append (otherMessageIds[i]);
				if (i < otherMessageIds.length-1) {
					b.append (",");
				}
			}
		}
		b.append ("]");
		return b.toString ();

	}
}

