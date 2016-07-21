/**
 * Capsis - Computer-Aided Projections of Strategies in Silviculture
 * 
 * Copyright (C) 1999-2010 INRA
 * 
 * Authors: F. de Coligny, S. Dufour-Kowalski,
 * 
 * This file is part of Capsis Capsis is free software: you can redistribute it
 * and/or modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the License,
 * or (at your option) any later version.
 * 
 * Capsis is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU lesser General Public License
 * along with Capsis. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package capsis.commongui.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import jeeb.lib.util.Check;
import jeeb.lib.util.Log;
import jeeb.lib.util.Settings;
import jeeb.lib.util.StatusDispatcher;
import jeeb.lib.util.Translator;
import jeeb.lib.util.UnZip;
import capsis.kernel.PathManager;
import capsis.util.HelpPageImmutable;

/**
 * This class deals with help.
 * 
 * @author F. de Coligny - S. Dufour
 */
public class Helper {

	static {
		// define default value if not defined
		Settings.setProperty("capsis.download.offline.help.freq",
				Settings.getProperty("capsis.download.offline.help.freq", "2"));
	}

	/** Capsis URL */
	static protected String getCapsisURL() {
		return Settings.getProperty("capsis.url", "http://capsis.cirad.fr");
	}

	// static boolean networkOk = true;

	/** Show tutorial */
	public static void showTutorial() throws Exception {
		URL url = new URL(getCapsisURL() + "/documentation/tutorial_" + Locale.getDefault().getLanguage());
		Helper.showPage(url);
	}

	/** Show tutorial */
	public static void showFAQ() throws Exception {
		URL url = new URL(getCapsisURL() + "/documentation/FAQ");
		Helper.showPage(url);
	}

	/**
	 * Shows the licence for the given module. If modulePackageName is null,
	 * shows the Capsis licence.
	 */
	public static void licenseFor(String modulePackageName) {
		if (modulePackageName == null) {
			return;
		}
		String name = null;

		try {
			// Look for a license in .html
			String path = modulePackageName.replaceAll("\\.", "/");
			name = path + "/license" + "_" + Locale.getDefault().getLanguage() + ".html";

			URL pageName = Helper.class.getClassLoader().getResource(name);

			// Try to find a license in .txt
			if (pageName == null) {
				name = path + "/license" + "_" + Locale.getDefault().getLanguage() + ".txt";
				pageName = Helper.class.getClassLoader().getResource(name);
			}

			Helper.showPage(pageName);

		} catch (Exception e) {
			if (modulePackageName.toLowerCase().equals("capsis")) {
				Log.println(Log.ERROR, "Helper.licenseFor (\"capsis\")", "Could not find capsis license: " + name);
				return;
			}

			// Shows special text : "module without license"
			name = "moduleWithoutLicense" + "_" + Locale.getDefault().getLanguage() + ".html";

			URL pageName = ClassLoader.getSystemClassLoader().getResource(name);
			try {
				Helper.showPage(pageName);

			} catch (Exception e2) {
				Log.println(Log.ERROR, "Helper.licenseFor (" + modulePackageName + ")",
						"Can not get license for module due to ", e2);
			}
		}
	}

	static boolean isWebSiteAccessible() {

		// REMOVED this to be able to detect if the network comes back (was not
		// checking any more)
		// if (!networkOk) return false;

		try {
			// URL web = new URL(System.getProperty("capsis.url"));
			URL web = new URL("http://www.google.com");
			URLConnection webconnect = web.openConnection();
			webconnect.setConnectTimeout(1000);
			webconnect.connect();

		} catch (MalformedURLException e) {
			// fc-23.3.2015 ONF trouble: cd can not see the Deesses help pages
			Log.println(Log.WARNING, "Helper.isWebSiteAccessible ()",
					"Could not open a web connection, returned false", e);
			return false;

		} catch (IOException e) {
			// fc-23.3.2015 ONF trouble: cd can not see the Deesses help pages
			Log.println(Log.WARNING, "Helper.isWebSiteAccessible ()",
					"Could not open a web connection, returned false", e);

			// networkOk = false;
			return false;
		}

		return true;
	}

	/** Return the help Resource URL given a classname */
	public static URL getHelpResourceURL(String className) {

		URL pageName;
		if (className == null) {
			return null;
		}
		String name;

		// fc-21.12.2011 REMOVED local pages access -> all should be copied to
		// the web site wiki
		// // Try local version
		//
		// // Extension help file are formed like this : classname_Help_en.html
		// className = className.replace ('.', '/');
		// name = className + "_Help_" + Locale.getDefault ().getLanguage ()
		// + ".html";
		//
		// pageName = Helper.class.getClassLoader ().getResource (name);
		// if (pageName != null) { return pageName; }
		//
		// // Other help file are formed like this : classname_index_en.html
		// name = className + File.separator + "index_"
		// + Locale.getDefault ().getLanguage () + ".html";
		// pageName = ClassLoader.getSystemClassLoader ().getResource (name);
		// if (pageName != null) { return pageName; }

		// Try External URL : capsis.url/help_en/directories

		name = getCapsisURL();

		className = className.replace('.', '/');
		name += "/help_" + Locale.getDefault().getLanguage() + "/" + className;
		try {
			pageName = new URL(name);
		} catch (MalformedURLException e) {
			pageName = null;
		}

		return pageName;

	}

	/** Does helper have a helper file for the given className ? */
	public static boolean hasHelpFor(String className) {
		return getHelpResourceURL(className) != null;
	}

	/**
	 * Show an information page about the given className. ClassName can be
	 * "capsis" -> capsis help, an extension className -> extension help, a
	 * modulePackageName -> module help.
	 */
	public static void helpFor(String className) {
		try {
			Helper.showPage(getHelpResourceURL(className));
		} catch (Exception e) {
			Log.println(Log.WARNING, "Helper.helpFor (" + className + ")", "Error due to ", e);
		}
	}

	/**
	 * Provides help related to the subject in interactive mode. It can be
	 * triggered for example by a help button in a dialog box. Subject must be
	 * an instance of JDialog or JFrame.
	 */
	public static void helpFor(Object subject) {
		try {
			String className;
			if (subject instanceof HelpPageImmutable) {
				className = ((HelpPageImmutable) subject).getHelpPageAddress();
			} else {
				className = subject.getClass().getName();
			}
			URL resource = getHelpResourceURL(className);
			if (resource == null) {
				StatusDispatcher.print(Translator.swap("Helper.helpFileNotFound") + " : " + resource);
				return;
			}

			Helper.showPage(resource); // owner may change (modal box ?)

		} catch (Exception e) {
			Log.println(Log.WARNING, "Helper.helpFor (Object)", "Could not find help for " + subject + " due to ", e);
		}
	}

	/**
	 * Show an URL If the url begin by http://capsis.server Test first network
	 * connection If it fails, then try to open local version
	 * */
	public static void showPage(URL url) throws Exception {

		String urlstr = url.toString();

		if (!isWebSiteAccessible()) {
			urlstr = convertWebUrlToLocalUrl(urlstr);
		}

		// System.out.println("Helper showPage: "+urlstr);

		Browser.showPage(urlstr);

	}

	public static void showPage(String url) throws Exception {
		showPage(new URL(url));
	}

	/** Transform a web url to a local url for help file */
	public static String convertWebUrlToLocalUrl(String weburl) {

		// fc-23.3.2015 if weburl is getCapsisURL() (CapsisPreferences >
		// External tools > System browser > Test), it does not start with
		// getCapsisURL() + '/' -> changed
		// String baseurl = getCapsisURL() + '/';

		if (!weburl.startsWith(getCapsisURL())) {
			return weburl;
		}

		String baseurl = getCapsisURL() + '/';

		String localurl = "file://" + PathManager.getDir("doc") + "/capsis_help/";

		localurl = localurl.replace('\\', '/'); // fc-30.8.2012 - local url pb
												// under windows (CM & TL, pp3)

		String urlstr = weburl.replaceFirst(baseurl, "");
		urlstr = urlstr.replace('/', '-');
		urlstr = urlstr.concat(".html");

		urlstr = localurl + urlstr;
		urlstr = urlstr.toLowerCase();

		return urlstr;
	}

	/**
	 * Download offline version of the website
	 * 
	 * @return the temporary file
	 * */
	public static void downloadOfflineData() throws IOException {
		downloadOfflineData(false);
	}

	public static void downloadOfflineData(final boolean force) throws IOException {

		// get help from web in a thread to avoid blocking
		new Thread(new Runnable() {

			public void run() {
				try {

					if (!isWebSiteAccessible())
						return;

					int downloadFreq = Check.intValue(Settings.getProperty("capsis.download.offline.help.freq", "2"));
					if (!force && downloadFreq < 1) {
						Log.println("Help files were not downloaded: day frequency (" + downloadFreq + ") is < 1 ");
						return;
					}

					// Destination directory
					final String destdir = PathManager.getDir("doc");

					// Determine if the file must be download
					File dir = new File(destdir + "/capsis_help/");

					if (!force && dir.exists()) {

						// Compare day
						Date filedate = new Date(dir.lastModified());
						Calendar cal = new GregorianCalendar();
						cal.setTime(filedate);

						// if difference is lower than download frequency :
						// abort
						int fileday = cal.get(Calendar.DAY_OF_YEAR);
						int currentday = new GregorianCalendar().get(Calendar.DAY_OF_YEAR);
						if (Math.abs(currentday - fileday) < downloadFreq) {
							return;
						}
					}

					String zipUrl = getCapsisURL() + "/offline/capsis_help.zip";

					System.out.print("Downloading Help files from [" + zipUrl + "]... ");
					// Create an URL instance
					final URL url = new URL(zipUrl);
					getZip(url, destdir);

					// Touch directory
					dir.setLastModified(System.currentTimeMillis());
				} catch (Throwable e) {
					Log.println(Log.ERROR, "Helper.downloadOfflineData ()", "Could not download help", e);
					System.out.println("Could not download help (see Log file): " + e);
				}
			}
		}).start();
	}

	static void getZip(URL url, String destdir) throws IOException {
		// Get an input stream for reading
		URLConnection webconnect = url.openConnection();
		webconnect.setConnectTimeout(1000);
		webconnect.connect();
		InputStream in = webconnect.getInputStream();

		BufferedInputStream bufIn = new BufferedInputStream(in);

		// Create temp file.
		File temp = File.createTempFile("capsis_doc", ".zip");
		temp.deleteOnExit();

		// Write to temp file
		FileOutputStream fos = new FileOutputStream(temp);

		try {
			byte[] buf = new byte[1024];
			int i = 0;
			while ((i = bufIn.read(buf)) != -1) {
				fos.write(buf, 0, i);
			}
		} catch (Throwable e) {
			Log.println(Log.ERROR, "Helper.getZip ()", "Error while downloading zip", e);
			throw new IOException(e);
		} finally {
			if (in != null)
				in.close();
			if (fos != null)
				fos.close();
		}

		System.out.println("done");
		System.out.println("Unzipping archive...");

		// Unzip
		UnZip u = new UnZip(destdir);
		u.setMode(UnZip.EXTRACT);
		u.unZip(temp.toString());

	}

	public static void main(String[] args) throws Exception {
		// Test
		Helper.downloadOfflineData(true);
	}

}
