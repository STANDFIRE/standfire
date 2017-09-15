import java.text.SimpleDateFormat
import java.util.Locale

// Imports for writing entry for new module in etc/capsis.models file
import java.io.File
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.lang.Exception;

// Create module script
// Copy template module to a new directory

//author = "__AUTHOR__"
//date = "__DATE__"
//institute = "__INSTITUTE__"


/** Copy a directory 
 * Ignore .svn and .class files
 */
def copy_dir(directory, srcDir, srcPrefix, destDir, destPrefix) {

  dir = new File(directory + "/" + srcDir)

  dir.eachFileRecurse{ 

    if(it.path.contains(".svn")) { return }
    if(it.path =~ /\.class$/) { return }

    newfile = it.path
    newfile = newfile.replaceFirst(srcDir, destDir)
    newfile = newfile.replaceFirst(srcPrefix, destPrefix)
    
    newfile = new File(newfile)
    
    if (it.isDirectory()) { 
      newfile.mkdirs()
       
    } else {

      new AntBuilder().copy( file: it.canonicalPath, tofile: newfile.canonicalPath )

      updateContents(newfile, srcDir, destDir)
      updateContents(newfile, srcPrefix, destPrefix)
      updateContents(newfile, "__PACKAGENAME__", packagename)
      updateContents(newfile, "__MODULENAME__", modulename)
      updateContents(newfile, "__AUTHOR__", author)
      updateContents(newfile, "__INSTITUTE__", institute)
      updateContents(newfile, "__DATE__", date)
      updateContents(newfile, "__PREFIX__", destPrefix)
    }

  }

}

/** Replace the contents of a file */
def updateContents(file, oldString, newString){

  text = file.getText()
  text = text.replaceAll(oldString, newString)
  file.write(text)

}

/** Check the script parameters */
def checkParams (args) {
	
	rc = 0
	
	// We need 7 parameters
	if (args.length < 7) {rc = 1}
	
	// Get the main variables
	sourcedir = args[0]
	packagename = args[1]
	prefix = args[2]
	source = args[3]
	sourceprefix = args[4]
	author = args[5]
	institute = args[6]
	
	// Check the main variables
	if (sourcedir.startsWith ("\$")) {
		println "  Error: Missing sourcedir"
		rc = 1
	}
	if (packagename.startsWith ("\$")) {
		println "  Error: Missing packagename"
		rc = 1
	}
	if (prefix.startsWith ("\$")) {
		println "  Error: Missing prefix"
		rc = 1
	}
	if (source.startsWith ("\$")) {
		println "  Error: Missing source"
		rc = 1
	}
	if (sourceprefix.startsWith ("\$")) {
		println "  Error: Missing sourceprefix"
		rc = 1
	}
	if (author.startsWith ("\$")) {
		println "  Error: Missing author"
		rc = 1
	}
	if (institute.startsWith ("\$")) {
		println "  Error: Missing institute"
		rc = 1
	}
	
	if (packagename.toLowerCase() != packagename) {
		println "  Error: packagename must contain only lower case letters"
		rc = 1
	}
	if (prefix.substring(0,1).toUpperCase() != prefix.substring(0,1)) {
		println "  Error: prefix must start with an upper case letter"
		rc = 1
	}
	
	// If trouble, print usage and abort
	if (rc != 0) {
		println "usage: ant createmodule -Dname=packagename -Dprefix=Prefix -Dauthor=Author -Dinstitute=Institute"
		println "  note for author and institute: all '_' chars will be replaced by spaces"
		return rc
	}
	
	author = author.replace('_',' ')
	institute = institute.replace('_',' ')
	
	// packagename = laricio -> modulename = Laricio
	modulename = packagename.substring(0,1).toUpperCase() + packagename.substring (1)
	
	println "  packagename " + packagename
	println "  modulename  " + modulename
	println "  prefix      " + prefix
	println "  author      " + author
	println "  institute   " + institute
	
	return rc  // ok
}

/* Stores all the lines of the file with path filePath in a list */
ArrayList<String> storesLinesOfFileInList(String filePath) {

	ArrayList<String> linesOfFile = new ArrayList<String>()

	// Fill in the linesOfFile list with all the lines of the file
	try {
    	BufferedReader reader = new BufferedReader(new FileReader(filePath))
    	String str;
    	while ((str = reader.readLine ()) != null) {
    	    linesOfFile.add(str)
    	}
    	reader.close();
	} catch (Exception e) {
    	println "Could not read from file: " + filePath
		println e.getMessage()
		return
	}

	return linesOfFile
}

/* Checks if the module with name packagename already exists in a list containing all the lines 
   of a file (which is in practice the etc/capsis.models file)
*/
boolean isPackageFound(String packagename, ArrayList<String> linesOfFile) {

	boolean packageFound = false
	String packageNameInList

	for (int i=0 ; i<linesOfFile.size() ; ++i) {
		if ( linesOfFile[i].trim().length()>0 && linesOfFile[i].charAt(0) != "#" ) {						
			int equalCharacterIndex = linesOfFile[i].indexOf('=')
			if ( equalCharacterIndex != -1 ) {
				packageNameInList = linesOfFile[i].substring(0, equalCharacterIndex).trim()
			}
			if ( packagename.equals(packageNameInList) ) {
				packageFound = true
				break
			}
		}
	}

	return packageFound
}

/* Adds a new line above the first non-commented or empty line of an existing file with path filePath.
   Lines of file have already been stored in the linesOfFile list.
*/
void addLineAtTopFile(String newLine , ArrayList<String> linesOfFile, String filePath) {

	// Defines String lists
	ArrayList<String> finalLinesOfFile = new ArrayList<String>()

	// Fills in the finalLinesOfFile list. The new line is inserted in the list above the first item 
	// which is different from a commented line (beginning with #) or an empty line
	boolean isNewModuleLineAdded = false
	for (int i=0 ; i<linesOfFile.size() ; ++i) {
		if (!isNewModuleLineAdded) {
			if ( linesOfFile[i].trim().length()>0 && linesOfFile[i].charAt(0) != "#" ) {
				finalLinesOfFile.add(newLine)
				finalLinesOfFile.add(linesOfFile[i])
				isNewModuleLineAdded = true
			} else {
				finalLinesOfFile.add(linesOfFile[i])
			}
		} else {
			finalLinesOfFile.add(linesOfFile[i])
		}
	}

	// Write the content of the finalLinesOfFile list in file with path filePath
	try {
		BufferedWriter out = new BufferedWriter(new FileWriter(filePath))
		for (int i=0 ; i<finalLinesOfFile.size() ; ++i) {
			out.write(finalLinesOfFile[i])
			if (i != finalLinesOfFile.size()-1) out.newLine()
		}
		out.close()
	} catch (Exception e) {
		println "Could not write in file ${filePath}\n"
		println "Please edit the ${filePath} file to add an entry for the new module"
		println e.getMessage()
		return
	}

}



/* Main */

println "CreateModule..."

// Check the params, abort if trouble
r = checkParams (args)
if (r != 0) return r

// Set the date variable
//def df = DateFormat.getDateInstance(DateFormat.MEDIUM)
def df = new SimpleDateFormat ("MMMM yyyy", Locale.ENGLISH)
date = df.format(new Date()).toString()

// Check if the module with name packagename already exists in file fileName
String capsisModelsFilePath = "etc" + File.separator + "capsis.models"
ArrayList<String> linesOfCapsisModelsFile = storesLinesOfFileInList(capsisModelsFilePath)
boolean packageFound = isPackageFound(packagename, linesOfCapsisModelsFile)

if (!packageFound) {
	// Add the name of the new module (with name packagename) at the top of the list 
	// of existing modules in etc/capsis.models file
	println "  Writing entry for module ${packagename} in ${capsisModelsFilePath} file..."
	String newModuleLine = packagename + "=true"	
	addLineAtTopFile(newModuleLine, linesOfCapsisModelsFile , capsisModelsFilePath)	
}

// Delete src/packagename and data/packagename directories if they already exist
String srcPackagenameDirectoryPath = "src" + File.separator + packagename
String dataPackagenameDirectoryPath = "data" + File.separator + packagename
File srcPackagenameDirectory = new File(srcPackagenameDirectoryPath)
File dataPackagenameDirectory = new File(dataPackagenameDirectoryPath)

if ( srcPackagenameDirectory.exists() ) {
	println "  Deleting ${srcPackagenameDirectory} directory: it already exists"
	srcPackagenameDirectory.deleteDir()
}
if ( dataPackagenameDirectory.exists() ) {
	println "  Deleting ${dataPackagenameDirectory} directory: it already exists"
	dataPackagenameDirectory.deleteDir()
}

// Create the new module
println "  Creating src" + File.separator + packagename + " directory..."
copy_dir(sourcedir, source, sourceprefix, packagename, prefix)
	
// Create the data/packagename directory
println "  Creating data" + File.separator + packagename + " directory..."
new File(dataPackagenameDirectoryPath).mkdir()

// Copy files from data/template directory to data/packagename directory
String dataTemplateDirectoryPath = "data" + File.separator + "template" 
(new AntBuilder()).copy( todir: dataPackagenameDirectoryPath ) {
	fileset(dir: dataTemplateDirectoryPath)
}

// Rename files from data/packagename directory
(new File(dataPackagenameDirectoryPath)).eachFile() {
	String oldFileName = it.getName()
	String newFileName = oldFileName.replace("template", packagename)
	it.renameTo(new File(dataPackagenameDirectoryPath + File.separator + newFileName))
}
