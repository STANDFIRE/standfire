package fireparadox.model.database;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.StringTokenizer;
import java.util.Vector;


/**
 * FiDBUpdatorScene : communication with DB4O database for storing scenes
 *
 * 
 * 
 */
public class FmDBUpdatorScene {

    private String lineBreak;
    private String lineSeparator;
    private String fieldSeparator;
    private String codeSeparator;

	private static FmDBUpdatorScene instance;


	/** Singleton pattern.
	*	FiDBUpdator u = FiDBUpdator.getInstance ();
	*/
	public static FmDBUpdatorScene getInstance () {
		if (instance == null) {instance = new FmDBUpdatorScene ();}
		return instance;
	}

   /** Creates a new instance of FiDBUpdator */
    private FmDBUpdatorScene () {
		lineBreak  = FmDBCommunicator.LINE_BREAK;
		lineSeparator  = FmDBCommunicator.LINE_SEPARATOR;
		fieldSeparator = FmDBCommunicator.FIELD_SEPARATOR;
		codeSeparator  = FmDBCommunicator.CODE_SEPARATOR;
    }

	
		


	/**
	 * doPost : send a HTTP REQUEST to the server in POST mode
	 */
	public Vector doPost (String str) throws Exception {

  		Vector resultList = new Vector();	//Vector returned by with all items
  		String inputLine; 					//String returned by the database server
  		StringTokenizer token;


		URL url = new URL (FmDBCommunicator.getDataBaseURL ()+"scene");
		URLConnection connection = url.openConnection();
		connection.setDoOutput(true);

		OutputStreamWriter out = new OutputStreamWriter(
									 connection.getOutputStream());
		out.write(str);
		out.close();

		BufferedReader in = new BufferedReader(
					new InputStreamReader(
					connection.getInputStream()));

		String decodedString;

		//Remove first separator for each line
		while ((inputLine = in.readLine()) != null) {

			String res = inputLine.replaceAll(lineBreak, lineSeparator);

			token = new StringTokenizer(res, lineSeparator);
			int cpt = token.countTokens();
			//store each item in the returned list
			if (cpt > 0) {
				while (token.hasMoreTokens())
					resultList.add (token.nextToken(lineSeparator));
			}
		}
		in.close();
		return resultList;
    }



}
