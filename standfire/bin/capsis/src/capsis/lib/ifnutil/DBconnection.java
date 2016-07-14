/*
* The ifn library for Capsis4
*
* Copyright (C) 2006 J-L Cousin, M-D Van Damme
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

package capsis.lib.ifnutil;

import java.io.Serializable;



public class DBconnection implements Serializable {
	
	private int certificat;
	private ConnectionInt connection;
	private static DBconnection instance;

	/**
	 * Default constructor
	 *
	 */
	public DBconnection() {
	}
  
	/**
	 * Singleton pattern
	 * @return
	 */
	public static DBconnection getInstance(String urlserver,String dbconnection,
          String applicationName,String languageKey) throws IfnException {
		if(instance == null){
			instance = new DBconnection(urlserver,dbconnection,applicationName,languageKey);
		}
		return instance;
	}
	
	

	/**
	 * Constructor :
	 * - urlserver :serveur rmi
	 * - dbconnection : connection vers la base de données
	 * - languageKey : langue
	 */
	private DBconnection(String urlserver,String dbconnection,
            String applicationName,String languageKey) throws IfnException {
		if (connection == null)
			createConnection(urlserver,dbconnection,applicationName, languageKey);
	}

  	/**
  	 * Creation de la connexion a la servlet en utilisant le nom de la machine
  	 */
	private void createConnection(String urlserver,String dbconnection,
  						String applicationName,String languageKey) throws IfnException {
		try {
  			/*
      			String machineName;
      			machineName = java.net.InetAddress.getLocalHost().getHostName();
      			connection = (ConnectionInt)Naming.lookup(urlserver + applicationName +
        						'-'+machineName.toUpperCase());
      			certificat = connection.setConnectionName(dbconnection,languageKey);
      			System.out.println("Connexion ok avec le serveur");
      		*/
  			//System.out.println("Nouvelle interface de connexion");
  			connection = new ConnectionInt();
  			certificat = connection.setConnectionName(dbconnection,languageKey);
  		} catch(Exception e) {
  			//System.out.println(e.toString());
  			throw new IfnException("Connexion impossible avec le serveur");
  		}
	}
  	
	/**
	 * Return connection for servlet
	 */
	public ConnectionInt getConnection() {
		return connection;
	}
	
	/**
	 * Renvoie le certificat
	 */
	public int getCertificat() {
		return certificat;
	}
  
  
	public void close() throws IfnException {
		try {
			connection.closeConnection();
		} catch(Exception e) {
			//System.out.println(e.toString());
			throw new IfnException("Problème Fermeture Connexion");
		}
	}
  
}