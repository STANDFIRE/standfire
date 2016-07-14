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
 * Bye request for AMAPsim client/server connection.
 * 
 * @author F. de Coligny - november 2002
 */
public class Bye extends Request {
	
	// Data to be sent to server
	public int dataLength;		// this field is not counted in dataLength	
	public String messageId;	// 5 chars
	public int requestType;		// 0 = Bye
	
	
	public Bye (Object params) {
		dataLength = 9;
		//~ messageId = ProtocolManager.nextMessageId ();
		messageId = "-bye-";		// fc - 28.1.2004 - request 7
		requestType = 9;
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
		return "Bye: dataLength="+dataLength+" messageId="+messageId
				+" requestType="+requestType;
	}
}

