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
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.util.Date;

import jeeb.lib.util.Check;
import jeeb.lib.util.MessageDialog;
import jeeb.lib.util.Translator;
import capsis.util.SwapDataOutputStream;

/**
 * A request superclass for AMAPsim requests.
 * 
 * @author F. de Coligny - november 2002
 */
abstract public class Request extends Message {
	public static final String[] REQUEST_NAMES = 	// fc - 13.10.2003
			{"Hello", "Mode 1", "Mode 2", "Ask again", "Force answer", "Cancel request", 
			"Ask status", "IdRequest", "Unused", "Bye"};
	
	private boolean canceled;
	private String date;		// fc - 29.1.2004 - dates in requests and responses
	
	static public String getRequestName (int code) {return REQUEST_NAMES [code];}	// fc - 13.10.2003
	static public int getRequestCode (String name) {	// fc - 13.10.2003
		for (int i = 0; i < REQUEST_NAMES.length; i++) {
			if (REQUEST_NAMES[i].equals (name)) {return i;}
		}
		return -1;	// not found
	}
	
	abstract public void write (SwapDataOutputStream out) throws IOException; 

	/**
	 * Writes a String to SwapDataOutputStream, encoded in a special manner : 
	 * one byte per char and a NULL (00) at the end.
	 */
	static public void writeString (SwapDataOutputStream out, String str) throws IOException {
		out.writeBytes (str);
		out.writeByte (0);
	}
	/**
	 * one byte per char and a NULL (00) at the end.
	 */
	static public int stringLength (String str) {return str.length () + 1;}

	abstract public int getDataLength ();
	abstract public String getMessageId ();
	abstract public int getRequestType ();
	
	public void setCanceled (boolean c) {canceled = c;}
	public boolean isCanceled () {return canceled;}
	
	public void setDate (Date d) {		// formatting request date+time : short aspect
		StringBuffer b = new StringBuffer ();
		date = Message.dateFormater.format (d, b, new FieldPosition (0)).toString ();
	}
	
	public String getDate () {return date;}
	
	/**	This key is unique for the couple (request, client) : notrx + request date.
	*/
	public String getKey () {
		StringBuffer key = new StringBuffer (getMessageId ());
		key.append (" ");
		key.append (date);
		return key.toString ();
	}
	
	/**	Formats an id into a message Id (ex: 12 -> 00012)
	*/
	static protected String formatId (String id) throws Exception {
		id = id.trim ();
		
		if (Check.isEmpty (id)) {
			MessageDialog.print (null, Translator.swap ("CancelRequestDialog.messageIdMustBeSet"));
			throw new Exception ("messageId must be set");
		}
		if (!Check.isInt (id)) {
			MessageDialog.print (null, Translator.swap ("CancelRequestDialog.messageIdMustBeAnInteger"));
			throw new Exception ("massageId must be an integer");
		}
		if (id.length () > 5) {	// fc - 2.2.2004 - changed != to >
			MessageDialog.print (null, Translator.swap ("CancelRequestDialog.messageIdMustBe5DigitsLong"));
			throw new Exception ("messageId must not exceed 5 digits");
		}
		
		int i = 0;
		try {
			i = new Integer (id).intValue ();
		} catch (Exception e) {
			MessageDialog.print (null, Translator.swap ("CancelRequestDialog.messageIdMustBeAnInteger"));
			throw new Exception ("messageId must be an integer");
		}
		
		NumberFormat messageIdFormater = NumberFormat.getNumberInstance ();
		messageIdFormater.setMinimumIntegerDigits (5);
		messageIdFormater.setMaximumIntegerDigits (5);
		messageIdFormater.setMaximumFractionDigits (0);
		messageIdFormater.setGroupingUsed (false);
		
		// return correct messageId (ex: 00012)
		return messageIdFormater.format (i);
	}

}

