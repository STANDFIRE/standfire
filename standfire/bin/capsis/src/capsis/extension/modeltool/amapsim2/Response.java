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
import java.net.InetAddress;
import java.net.Socket;
import java.text.FieldPosition;
import java.util.Date;

import capsis.util.SwapDataInputStream;

/**
 * A Response superclass for AMAPsim Responses. 
 * 
 * @author F. de Coligny - november 2002
 */
abstract public class Response extends Message {
	
	private Request relatedRequest;
	private String date;			// fc - 29.1.2004 - dates in requests and responses
	private String serverId;	// fc - 29.1.2004 - reference of the responding server
	
	
	/**
	 * Redefine constructor to get response data from in.
	 */
	public Response (SwapDataInputStream in) throws IOException {}

	abstract public int getDataLength ();
	abstract public String getMessageId ();
	abstract public int getRequestType ();
	abstract public int getReturnCode ();

	public Request getRelatedRequest () {return relatedRequest;}
	public void setRelatedRequest (Request request) {relatedRequest = request;}

	/**
	 * Reads a String from SwapDataInputStream, encoded in a special manner : 
	 * one byte per char and a NULL (00) at the end.
	 */
	static public String readString (SwapDataInputStream in) throws IOException {
		StringBuffer sb = new StringBuffer ();
		char c = (char) in.readByte ();
		while (c != 0) {
			sb.append (c);
			c = (char) in.readByte ();
		}
		return sb.toString ();
	}
	
	public void setDate (Date d) {		// formatting request date+time : short aspect
		StringBuffer b = new StringBuffer ();
		date = Message.dateFormater.format (d, b, new FieldPosition (0)).toString ();
	}
	
	public String getDate () {return date;}
	
	public void setServerId (Socket s) {
		InetAddress a = s.getInetAddress ();
		if (a.getHostName () != null && a.getHostName ().length () != 0) {
			serverId = a.getHostName ()+":"+s.getPort ();
		} else {
			serverId = a.getHostAddress ()+":"+s.getPort ();
		}
	}
	public String getServerId () {return serverId;}

	/**	This key is unique for the couple (response, client) : notrx + response date.
	*/
	public String getKey () {
		StringBuffer key = new StringBuffer (getMessageId ());
		key.append (" ");
		key.append (date);
		return key.toString ();
	}

}

