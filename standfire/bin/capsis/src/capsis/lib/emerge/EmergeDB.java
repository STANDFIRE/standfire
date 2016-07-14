/*
 * HSQLDB Copyright (c) 2001-2010, The HSQL Development Group All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the
 * distribution.
 *
 * Neither the name of the HSQL Development Group nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL HSQL DEVELOPMENT GROUP, HSQLDB.ORG, OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *
 * For work originally developed by the Hypersonic SQL Group:
 *
 * Copyright (c) 1995-2000 by the Hypersonic SQL Group. All rights reserved. Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following
 * conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the
 * distribution.
 *
 * Neither the name of the Hypersonic SQL Group nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE HYPERSONIC SQL GROUP, OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software consists of voluntary contributions made by many individuals on behalf of the Hypersonic SQL Group.
 */
package capsis.lib.emerge;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import jeeb.lib.util.ListenedTo;
import jeeb.lib.util.Listener;
import jeeb.lib.util.Log;
import jeeb.lib.util.StatusDispatcher;
import capsis.defaulttype.Tree;
import capsis.defaulttype.TreeList;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.PathManager;
import capsis.kernel.Step;
import capsis.lib.emerge.treecompartmentsdata.EmergeBranchData;
import capsis.lib.emerge.treecompartmentsdata.EmergeCompartmentData;
import capsis.lib.emerge.treecompartmentsdata.EmergeFruitData;
import capsis.lib.emerge.treecompartmentsdata.EmergeLeafData;
import capsis.lib.emerge.treecompartmentsdata.EmergeRootData;
import capsis.lib.emerge.treecompartmentsdata.EmergeStemData;
import capsis.lib.emerge.treecompartmentsdata.EmergeStumpData;
import capsis.lib.emerge.treecompartmentsdata.EmergeTreeData;

/**
 * *
 * This is the class that wraps HSQLDB (see license at end of file) embedded database for use in Capsis4 to store and retrieve biomass / mineral mass values computations
 *
 * @author T. Bronner
 */
public class EmergeDB implements Listener {

    static public final String AUTHOR = "T.Bronner";
    static public final String VERSION = "1.0";

    /*
     * Verbose output
     */
    private boolean verbose = false;
    private boolean verboseSQL = false;

    /*
     * Database connection parameters
     */
    private static final String DB_TEMP_DIR = "capsis_hsqldb_tmp";/*
     * name of db dir to create/delete
     */

    private static final String DB_NAME = "BMM";
    private static final String DB_USERNAME = "BMM";
    private static final String DB_PASSWORD = "BMM";
    private boolean dbInMemory;
    private boolean useIndex = true;
    private static int insertBatchSize = 100000;/*
     * size of batch for big insertions
     */

    private static double tableCacheFactor = 0.5d;/*
     * factor for table cache size : % of free memory.( <100%)
     */

    private int tableCacheMaxSize = 100 * 1024;/*
     * max size of table cache in Ko
     */

    private String dbTablePrefix = "BMM";
    private static int instanceCounter = 0; /*
     * used to name table for each instance in the same JVM, incremented at table creation
     */

    private int currentInstance;
    private Connection connection;
    //private static boolean firstInstanceOnFS = true;/* to check if file maintenance needed (db dir creation/deletion)*/
    private ArrayList<String> nutrients; /*
     * list of nutrients name
     */

    private UseEmergeDB model;/*
     * linked model
     */

    private boolean connected = false;
    private boolean ready = false;
    /*
     * used to interrupt long updatde
     */
    private boolean continueUpdate = true;
    /*
     * used to measure time for long processes
     */
    long time;
    /*
     * Collection used for steps sync between DB and UI
     */
    private ArrayList<Step> steps;
    private boolean updateNeeded = true;

    /*
     * Dialog box for time consuming update
     */
    private JDialog dialog;
    private final JPanel contentPanel = new JPanel();
    JProgressBar progressBar;
    JScrollPane jsp;
    JTextArea console;
    JScrollBar scrollBar;
    JButton stopButton;
    PreparedStatement nbTreesInStep, insertStatement;

    /**
     * *
     * Create a EmergeDB instance with storage of data in filesystem (TEMP directory)
     *
     * @param model linked mode
     * @param nutrientList list of nutrients name
     * @param tablePrefix prefix of instance table
     * @param remainingMemoryCachePct % of remaining allocatable JVM memory to use for table cache. Try 25-50%
     * @param tableCacheMaxSize Max size of table cache in Mb. Depending on the workstation RAM.
     */
    public EmergeDB(UseEmergeDB model, Collection<String> nutrientList, String tablePrefix, int tableCacheMaxSize) {
        if (tableCacheMaxSize > 0) {
            this.tableCacheMaxSize = tableCacheMaxSize * 1024;/*
             * converted in in ko
             */
        }
        this.model = model;
        this.useIndex = true;
        this.dbInMemory = false;
        init(true, tablePrefix, nutrientList);
    }

    /**
     * *
     * Create a EmergeDB instance with storage of data in memory
     *
     * @param model linked mode
     * @param nutrientList list of nutrients name
     * @param tablePrefix prefix of instance table
     */
    public EmergeDB(UseEmergeDB model, Collection<String> nutrientList, String tablePrefix) {
        this.model = model;
        this.dbInMemory = true;
        init(true, tablePrefix, nutrientList);
    }

    /**
     * *
     * Initialize HSQLDB JDBC driver
     *
     * @param deleteFilesOnExit delete files on exit for database storage on file system
     * @param tablePrefix prefix of instance table
     * @param nutrientList list of nutrients name
     */
    private void init(boolean deleteFilesOnExit, String tablePrefix, Collection<String> nutrientList) {
        /*
         * try to find the hsqldb driver in hsqldb.jar
         */
        try {
            Class.forName("org.hsqldb.jdbc.JDBCDriver");
            /*
             * initialize nutrient collection
             */
            nutrients = new ArrayList();
            if (tablePrefix != null) {
                dbTablePrefix = tablePrefix.toUpperCase();
            }
            else {
                dbTablePrefix = "MASSES";
            }
            if (nutrientList != null && !nutrientList.isEmpty()) {
                nutrients = new ArrayList(nutrientList);
            }
            else {
                nutrients = new ArrayList();
                nutrients.add("nutrients");
            }
            //TB 2014 02 prevent some weird behavior : Connecting to a db resets java.util.Logger
            System.setProperty("hsqldb.reconfig_logging", "false");
            /*
             * Connect the db server and/or create it if not exists
             */
            if (dbInMemory) {
                dbCreateConnectOnMemory();
            }
            else {
                dbCreateConnectOnFileSystem(deleteFilesOnExit);
            }
            createMainTable(useIndex);

            /*
             * prepared statements
             */
            nbTreesInStep = connection.prepareStatement(new StringBuffer("SELECT COUNT(DISTINCT TREE_ID) FROM ").append(dbTablePrefix).append(currentInstance).append(
                    " WHERE STEP_NAME=?").toString());
            prepareInsertStatement();
            initDialog();
            steps = new ArrayList<Step>();
            //			stepsToAdd = new ArrayList<Step>();
            //			stepsToDelete = new ArrayList<Step>();
            ready = true;
        }
        catch (ClassNotFoundException e) {
            Log.println(Log.ERROR, "BMMDB()", "Failed to load HSQLDB JDBC driver " + e.getMessage(), e);
        }
        catch (SQLException e) {
            Log.println(Log.ERROR, "BMMDB()", "Failed to create database " + e.getMessage(), e);
        }
        catch (IOException e) {
            Log.println(Log.ERROR, "BMMDB()", "File system error " + e.getMessage(), e);
        }

    }

    /**
     * *
     * Close connection, shutdown server
     */
    private void cleanUp() {
        final String methodName = "BMMDB.cleanUp()";
//		/*delete instance table*/
//		deleteTables();

        /*
         * shutdown db
         */
        System.out.println("Shuttind down emerge database");
        if (connection != null) {
            try {
                if (verboseSQL) {
                    System.out.println("SHUTDOWN IMMEDIATELY");
                }
                connection.createStatement().executeUpdate("SHUTDOWN IMMEDIATELY");
            }
            catch (SQLException e) {
                Log.println(Log.ERROR, methodName, " Cannot shutdown db", e);
            }
            /*
             * close connection
             */
            try {
                connection.close();
            }
            catch (SQLException e) {
                Log.println(Log.ERROR, methodName, " Cannot close connection do db", e);
            }
        }

    }

    /**
     * *
     * Get TEMP directory for file writing
     *
     * @param fromCapsis true to use capsis TEMP dir, false to use OS defined user dir
     * @return
     */
    private File getTempDir(boolean fromCapsis) {
        String tempDirPath;
        /*
         * get temp dir
         */
        if (fromCapsis) {
            tempDirPath = PathManager.getDir("tmp");
        }
        else {
            tempDirPath = System.getProperty("java.io.tmpdir");
        }

        /*
         * add potentially missing trailing file separator
         */
        if (!(tempDirPath.endsWith("/") || tempDirPath.endsWith("\\"))) {
            tempDirPath = tempDirPath + System.getProperty("file.separator");
        }

        File tempDir = new File(tempDirPath);
        /*
         * check permissions
         */
        if (tempDir.canWrite() && tempDir.canRead() && tempDir.getUsableSpace() >= 512 * 1024 * 1024/*
                 * 512Mb
                 */) {
            return tempDir;
        }
        return null;
    }

    /**
     * *
     * Create database and database server (if not exists) and connects it. Use storage of tables on memory
     *
     * @throws SQLException
     */
    private void dbCreateConnectOnMemory() throws SQLException {
        connected = false;
        connection = DriverManager.getConnection("jdbc:hsqldb:mem:" + DB_NAME + "_MEM", DB_USERNAME, DB_PASSWORD);
        connected = true;
    }

    /**
     * *
     * Create database and database server (if not exists) and connects it. Use storage of tables on filesystem
     *
     * @param deleteFilesOnExit Remove database files when JVM exits
     * @throws IOException
     * @throws SQLException
     */
    private void dbCreateConnectOnFileSystem(boolean deleteFilesOnExit) throws IOException, SQLException {
        StringBuffer connectionString;
        File tempDir = getTempDir(false);
        /*
         * check if there is somewhere we can write and enough free space
         */
        if (tempDir != null) {
            File dbDir = new File(tempDir, DB_TEMP_DIR);
            /*
             * If db dir exists,try to connect, il fail choose another
             */
            try {
                if (dbDir.exists()) {
                    connectionString = new StringBuffer("jdbc:hsqldb:file:" + dbDir.getCanonicalPath() + System.getProperty("file.separator") + DB_NAME + "_DISK");
                    connection = DriverManager.getConnection(connectionString.toString(), DB_USERNAME, DB_PASSWORD);
                    System.out.println("Connected to db stored in  " + dbDir.getCanonicalPath());
                    connected = true;
                    return;
                }
            }
            catch (SQLException se) {
                System.out.println("Could not connect db stored in " + dbDir.getCanonicalPath());
                System.out.println("Trying to create db in another dir");
            }
            int counter = 0;
            while (dbDir.exists()) {
                dbDir = new File(tempDir, new StringBuffer(DB_TEMP_DIR).append(counter++).toString());
//                if (removeDir(dbDir)) {
//                    System.out.println("Previous emerge db files found and deletez in " + dbDir.getAbsolutePath());
//                }
//                else {
//                    System.out.println("ERROR : Unable to delete Previous emerge db files found  in " + dbDir.getAbsolutePath());
//                }
            }
            if (dbDir.mkdir()) {
                System.out.println("Created dir : " + dbDir.getAbsolutePath());
            }

            connectionString = new StringBuffer("jdbc:hsqldb:file:" + dbDir.getCanonicalPath() + System.getProperty("file.separator") + DB_NAME);

            if (deleteFilesOnExit) {/*
                 * create a thread that will clean up the db dir on exit of the jvm
                 */
                final File dirToDelete = dbDir;
                Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

                    @Override
                    public void run() {
                        cleanUp();
                        removeDir(dirToDelete);
                    }
                }) {
                });
            }

            /*
             * Setup server parameter
             */

            //			System.out.println("Max mem = " + Runtime.getRuntime().maxMemory() / 1024 / 1024 + " Mb");
            //			System.out.println("Total mem = " + Runtime.getRuntime().totalMemory() / 1024 / 1024 + " Mb");
            //			System.out.println("Free mem = " + Runtime.getRuntime().freeMemory() / 1024 / 1024 + " Mb");
            /*
             * set the table cache size
             */
            long size = (Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory()) / 1024;/*
             * get free space in kb
             */
            System.out.println("Max free heap space = " + size + " Kb");
            size *= tableCacheFactor;
            if (size > tableCacheMaxSize) {
                size = tableCacheMaxSize;
            }
            System.out.println("HSQLDB table cache set to " + size + " Kb");
            connectionString.append(";hsqldb.cache_size=" + size);
            /*
             * Disable logging
             */
            connectionString.append(";hsqldb.log_data=false");

            connection = DriverManager.getConnection(connectionString.toString(), DB_USERNAME, DB_PASSWORD);
            System.out.println("Emerge database files created in " + dbDir.getCanonicalPath());
            connected = true;
        }
    }

    /**
     * *
     * remove dir in FS
     *
     * @param dir dit to delete
     * @return true if successful
     */
    private boolean removeDir(File dir) {
        final String methodName = "BMMDB.removeDir()";
        boolean result = false;
        try {
            File[] filesToDelete = dir.listFiles();
            for (int i = 0; i < filesToDelete.length; i++) {
                System.out.println("Attempting to remove " + (filesToDelete[i].isDirectory() ? " dir " : " fil ") + filesToDelete[i].getCanonicalPath());
                result = filesToDelete[i].delete();
                if (result) {
                    System.out.println(" OK");
                }
                else {
                    System.out.println(" Failure");
                }
            }
            System.out.println("Attempting to remove db dir : " + dir.getCanonicalPath());
            result = dir.delete();
            if (result) {
                System.out.println(" OK");
            }
            else {
                System.out.println(" Failure");
            }
        }
        catch (IOException e) {
            Log.println(Log.ERROR, methodName, " Cannot remove db dir " + e.getMessage(), e);
        }
        return result;
    }

    /**
     * *
     * Prepare insert statement as PreparedStatement for fast inserts
     */
    private void prepareInsertStatement() {
        final String methodName = "BMMDB.prepareInsertStatement()";
        try {
            if (ready) {
                StringBuffer sql = new StringBuffer("INSERT INTO ").append(dbTablePrefix).append(currentInstance).append(" VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?");
                for (Iterator it = nutrients.iterator(); it.hasNext();) {
                    sql.append(",?");
                    it.next();
                }
                sql.append(")");
                insertStatement = connection.prepareStatement(sql.toString());
            }
        }
        catch (Exception e) {
            Log.println(Log.ERROR, methodName, "Cannot create prepared statement", e);
        }
    }

    public boolean isConnected() {
        return connected;
    }

    public Connection getConnection() {
        if (ready) {
            return connection;
        }
        else {
            return null;
        }
    }

    /**
     * *
     * @return the name of this EmergeDB object instance table.
     */
    public String getTableName() {
        return dbTablePrefix + currentInstance;
    }

    public GModel getModel() {
        return (GModel) model;
    }

    /**
     * *
     * Report status of the DB server/ database / instance table status
     *
     * @return
     */
    public boolean isReady() {
        return ready;
    }

    /**
     * *
     * Delete instance table.
     *
     * @return
     */
    public  boolean deleteTables() {
        final String methodName = "BMMDB.deleteTables()";
        Statement statement = null;
        try {
            statement = connection.createStatement();

            if (verboseSQL) {
                System.out.println("DROP TABLE " + dbTablePrefix + currentInstance);
            }
            statement.executeUpdate("DROP TABLE " + dbTablePrefix + currentInstance);
            Log.println(Log.INFO, methodName, "Table " + dbTablePrefix + currentInstance + " dropped");
            return true;
        }
        catch (SQLException e) {
            Log.println(Log.ERROR, methodName, e.getMessage(), e);
        }
        finally {
            if (statement != null) {
                try {
                    statement.close();
                }
                catch (SQLException e) {
                    Log.println(Log.ERROR, methodName, " Cannot close statement", e);
                }
            }
        }
        return false;
    }

    /**
     * *
     * Create data table for this instance
     *
     * @param withIndexes use indexes. Table withoug index has faster insert / slower query (test purposes)
     * @return true if successful.
     */
    private boolean createMainTable(boolean withIndexes) {
        final String methodName = "BMMDB.createMainTable()";
        Statement statement = null;
        try {
            statement = connection.createStatement();
            /*
             * Manage instance counter for table suffix
             */
            currentInstance = instanceCounter++;
            /*
             * Drop table if exists
             */
            StringBuffer sql = new StringBuffer("DROP TABLE IF EXISTS ");
            sql.append(dbTablePrefix).append(currentInstance);
            statement.executeUpdate(sql.toString());
            /*
             * create table for our data
             */
            sql = new StringBuffer("CREATE ");
            if (!dbInMemory) /*
             * for data storage in file, used cached table type
             */ {
                sql.append(" CACHED ");
            }

            sql.append("TABLE ").append(dbTablePrefix).append(currentInstance).append(" (");/*
             * if multiple models/instance use emerge concurently on the same JVM, each one must have its own table
             */
            //"ROW_ID INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY," + /* in ordre to have a primary key, maybe not useful..*/
            sql.append("STEP_ID SMALLINT NOT NULL,");/*
             * required to manage step deletions and scenarios (interventions)
             */
            sql.append("STEP_NAME VARCHAR(5) NOT NULL,");/*
             * required to manage step deletions and scenarios (interventions)
             */
            sql.append("TREE_AGE SMALLINT NOT NULL,");
            sql.append("TREE_ID INTEGER NOT NULL,");/*
             * id of the tree. If one tree represents the whole pop, set to whatever user want
             */
            sql.append("COMPONENT_ID SMALLINT NOT NULL,");            /*
             * id of the component : integer if individual component (a root, a branch,..),
             */
            sql.append("COMPONENT VARCHAR(10) NOT NULL,");            /*
             * TREE, STEM, BRANCH, STUMP, LEAF, ROOT, FRUIT
             */
            sql.append("SUB_COMPONENT VARCHAR(10) NOT NULL,");            /*
             * WOOD, BARK
             */
            sql.append("ALIVE BOOLEAN NOT NULL,");            /*
             * is this component/subComponent alive
             */
            sql.append("THIN_AGE SMALLINT NOT NULL,");            /*
             * age at witch the tree was thinned
             */
            sql.append("ABOVE_GROUND BOOLEAN NOT NULL,");            /*
             * is this component above or below the ground
             */
            sql.append("CROWN BOOLEAN NOT NULL,");/*
             * is this component/subComponent part of the crown (branch, fruitor leaf)
             */
            /*
             * Dimension for components : not every dimension is applicable to every component/subComponent
             */
            sql.append("GROWTH_UNIT SMALLINT,");/*
             * Vertical growth unit, generally corresponding to one year of growth
             */
            /*
             * ogive shaped layer of growth for stems, under bark
             */
            sql.append("RING SMALLINT,");
            /*
             * stem section (wood and bark) for wich the diameter is less than 7cm
             */
            sql.append("COMMERCIAL_WOOD BOOLEAN,");/*
             * /*
             * Diameter class appliable only to branches and roots
             */
            sql.append("DIAMETER_CLASS_LOWER_BOUND SMALLINT,");
            sql.append("DIAMETER_CLASS_UPPER_BOUND SMALLINT,");
            /*
             * relative heigth position on the crown appliable to branches and leaves (possibly fruits), in % ; i.e. between 0 and 100%
             */
            sql.append("CROWN_HEIGHT_RANGE_LOWER_BOUND TINYINT,");/*
             * >=0
             */
            sql.append("CROWN_HEIGHT_RANGE_UPPER_BOUND TINYINT,");/*
             * <=100
             */
            sql.append("COHORT_AGE SMALLINT,");/*
             * age of a leaves cohort
             */
            /*
             * Depth of roots in soil
             */
            sql.append("SOIL_DEPTH_LOWER_BOUND SMALLINT,");
            sql.append("SOIL_DEPTH_UPPER_BOUND SMALLINT,");
            /*
             * Biomass
             */
            sql.append("BIOMASS DOUBLE NOT NULL");
            /*
             * iterate nutrients list.
             */
            if (nutrients != null && !nutrients.isEmpty()) {
                for (Iterator it = nutrients.iterator(); it.hasNext();) {
                    String nutrName = (String) it.next();
                    sql.append(",").append(nutrName.toUpperCase()).append(" DOUBLE NOT NULL");
                }
            }
            else {
                nutrients = new ArrayList<String>();
                nutrients.add("NUTRIENTS");
                sql.append(",NUTRIENTS DOUBLE NOT NULL");
                Log.println(Log.WARNING, methodName, " nutrient list provided is null. Using default general purpose nutrient container 'NUTRIENTS'");
            }
            sql.append(")");
            if (verboseSQL) {
                System.out.println(sql);
            }
            statement.executeUpdate(sql.toString());
            if (verbose) {
                Log.println(Log.INFO, methodName, " Table " + dbTablePrefix + currentInstance + " created by BMMDB instance");
            }
            if (withIndexes) { /*
                 * WARNING : too many index slows inserts, too few or poorly designed slow extractions
                 */
                int i = 0;
                statement.executeUpdate("CREATE INDEX " + dbTablePrefix + currentInstance + "_I" + i++ + " ON " + dbTablePrefix + currentInstance + " (STEP_ID)");
                //statement.executeUpdate("CREATE INDEX " + dbTablePrefix + currentInstance + "_I" + i++ + " ON " + dbTablePrefix + currentInstance + " (STEP_NAME,COMPONENT,TREE_ID)");
                //statement.executeUpdate("CREATE INDEX " + dbTablePrefix + currentInstance + "_I" + i++ + " ON " + dbTablePrefix + currentInstance + " (STEP_NAME,ABOVE_GROUND,COMPONENT,TREE_ID");
                statement.executeUpdate("CREATE INDEX " + dbTablePrefix + currentInstance + "_I" + i++ + " ON " + dbTablePrefix + currentInstance + " (ABOVE_GROUND)");
                statement.executeUpdate("CREATE INDEX " + dbTablePrefix + currentInstance + "_I" + i++ + " ON " + dbTablePrefix + currentInstance + " (COMPONENT)");
                statement.executeUpdate("CREATE INDEX " + dbTablePrefix + currentInstance + "_I" + i++ + " ON " + dbTablePrefix + currentInstance + " (STEP_NAME)");
                statement.executeUpdate("CREATE INDEX " + dbTablePrefix + currentInstance + "_I" + i++ + " ON " + dbTablePrefix + currentInstance + " (TREE_ID)");
                //statement.executeUpdate("CREATE INDEX " + dbTablePrefix + currentInstance + "_I" + i++ + " ON " + dbTablePrefix + currentInstance + " (TREE_AGE)");
                //statement.executeUpdate("CREATE INDEX " + dbTablePrefix + currentInstance + "_I" + i++ + " ON " + dbTablePrefix + currentInstance + " (COMPONENT_ID)");
                //statement.executeUpdate("CREATE INDEX " + dbTablePrefix + currentInstance + "_I" + i++ + " ON " + dbTablePrefix + currentInstance + " (SUB_COMPONENT)");
                //statement.executeUpdate("CREATE INDEX " + dbTablePrefix + currentInstance + "_I" + i++ + " ON " + dbTablePrefix + currentInstance + " (THIN_AGE)");
                //statement.executeUpdate("CREATE INDEX " + dbTablePrefix + currentInstance + "_I" + i++ + " ON " + dbTablePrefix + currentInstance + " (ALIVE)");
                //statement.executeUpdate("CREATE INDEX " + dbTablePrefix + currentInstance + "_I" + i++ + " ON " + dbTablePrefix + currentInstance + " (CROWN)");
                //statement.executeUpdate("CREATE INDEX " + dbTablePrefix + currentInstance + "_I" + i++ + " ON " + dbTablePrefix + currentInstance + " (GROWTH_UNIT)");
                //statement.executeUpdate("CREATE INDEX " + dbTablePrefix + currentInstance + "_I" + i++ + " ON " + dbTablePrefix + currentInstance + " (RING)");
                //statement.executeUpdate("CREATE INDEX " + dbTablePrefix + currentInstance + "_I" + i++ + " ON " + dbTablePrefix + currentInstance + " (COMMERCIAL_WOOD)");
                //statement.executeUpdate("CREATE INDEX " + dbTablePrefix + currentInstance + "_I" + i++ + " ON " + dbTablePrefix + currentInstance + " (DIAMETER_CLASS_LOWER_BOUND)");
//                statement.executeUpdate("CREATE INDEX " + dbTablePrefix + currentInstance + "_I" + i++ + " ON " + dbTablePrefix + currentInstance + " (DIAMETER_CLASS_UPPER_BOUND)");
//                statement.executeUpdate("CREATE INDEX " + dbTablePrefix + currentInstance + "_I" + i++ + " ON " + dbTablePrefix + currentInstance + " (CROWN_HEIGHT_RANGE_LOWER_BOUND)");
//                statement.executeUpdate("CREATE INDEX " + dbTablePrefix + currentInstance + "_I" + i++ + " ON " + dbTablePrefix + currentInstance + " (CROWN_HEIGHT_RANGE_UPPER_BOUND)");
//                statement.executeUpdate("CREATE INDEX " + dbTablePrefix + currentInstance + "_I" + i++ + " ON " + dbTablePrefix + currentInstance + " (COHORT_AGE)");
//                statement.executeUpdate("CREATE INDEX " + dbTablePrefix + currentInstance + "_I" + i++ + " ON " + dbTablePrefix + currentInstance + " (SOIL_DEPTH_LOWER_BOUND)");
//                statement.executeUpdate("CREATE INDEX " + dbTablePrefix + currentInstance + "_I" + i++ + " ON " + dbTablePrefix + currentInstance + " (SOIL_DEPTH_UPPER_BOUND)");
                Log.println(Log.INFO, methodName, " Indexes created for " + dbTablePrefix + currentInstance);
            }
            ready = true;
        }
        catch (Exception e) {
            Log.println(Log.ERROR, methodName, " Cannot create table " + dbTablePrefix + currentInstance + " " + e.getMessage(), e);
            ready = false;
            return false;
        }
        finally {
            if (statement != null) {
                try {
                    statement.close();
                }
                catch (SQLException e) {
                    Log.println(Log.ERROR, methodName, " Cannot close statement", e);
                }
            }
        }
        return ready;
    }

    /**
     * *
     * Create agregate table, not yet used
     *
     * @return
     */
    private boolean createMaxRingValuesTable() {
        final String methodName = "BMMDB.createMaxRingValuesTable()";
        try {
            Statement statement = connection.createStatement();
            String sql = "DROP TABLE " + dbTablePrefix + currentInstance + "_PREV_RINGS_MAX_VALUES IF EXISTS";
            if (verboseSQL) {
                System.out.println(sql);
            }
            statement.executeUpdate(sql);
            sql = "CREATE TABLE " + dbTablePrefix + currentInstance + "_PREV_RINGS_MAX_VALUES AS (" + " SELECT TREE_ID,RING,BIOMASS";
            /*
             * iterate nutrients list.
             */
            if (nutrients != null && !nutrients.isEmpty()) {
                for (Iterator it = nutrients.iterator(); it.hasNext();) {
                    String nutrName = (String) it.next();
                    sql += "," + nutrName.toUpperCase();
                }
            }
            sql +=
                    " FROM " + dbTablePrefix + currentInstance + " AS MT," + " (SELECT MAX(TREE_AGE)AS AGE FROM " + dbTablePrefix + currentInstance
                    + " WHERE RING IS NOT NULL GROUP BY RING) AS M"
                    + " WHERE MT.TREE_AGE=M.AGE" + " AND MT.COMPONENT='STEM' AND MT.SUB_COMPONENT='WOOD'" + " ORDER BY TREE_ID,RING) WITH DATA";
            if (verboseSQL) {
                System.out.println(sql);
            }
            statement.executeUpdate(sql);
            if (verbose) {
                Log.println(Log.INFO, methodName, " Table " + dbTablePrefix + currentInstance + "_PREV_RINGS_MAX_VALUES created by BMMDB instance");
            }
            ready = true;
            return true;
        }
        catch (SQLException e) {
            Log.println(Log.ERROR, methodName, " Cannot create table " + dbTablePrefix + currentInstance + "_PREV_RINGS_MAX_VALUES " + e.getMessage(), e);
            ready = false;
            return false;
        }
    }

    public boolean isVerbose() {
        return verbose;
    }

    /**
     * *
     * Set verbose mode
     *
     * @param verbose
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public boolean isVerboseSQL() {
        return verboseSQL;
    }

    /**
     * *
     * Set SQL verbose mode : ie display SQL queries used by the instance
     *
     * @param verboseSQL
     */
    public void setVerboseSQL(boolean verboseSQL) {
        this.verboseSQL = verboseSQL;
    }

    /**
     * *
     * Check input for methods witch add lines in db. Check if nutrient list is coherent with db Correct values below 0 by setting to 0.
     *
     * @param input Data to parse
     * @param sourceMethod Name of source method
     * @return true if everything ok
     */
    private boolean checkInput(Collection<? extends EmergeCompartmentData> input, String sourceMethod) {
        if (input == null) {
            Log.println(Log.ERROR, sourceMethod + "=>checkInput()", "Data provided is null");
            return false;
        }
        /*
         * Check consistency
         */
        for (Iterator it = input.iterator(); it.hasNext();) {
            Object element = it.next();
            if (element == null)/*
             * remove null element
             */ {
                it.remove();
            }
            else {
                EmergeCompartmentData line = (EmergeCompartmentData) element;
                if (line.nutrients == null) {
                    Log.println(Log.ERROR, sourceMethod, "nutrient list is null");
                    return false;
                }
                if (!(line.nutrients instanceof HashMap<?, ?>)) {
                    Log.println(Log.ERROR, sourceMethod, "nutrient lists must be HashMap<String,Double>");
                    return false;
                }
                /*
                 * check that the nutrient list in the input corresponds to the list in the db
                 */
                if (nutrients.size() != line.nutrients.size()) {
                    Log.println(Log.ERROR, sourceMethod, "nutrient lists mismatch");
                    return false;
                }
                for (Iterator iNut = nutrients.iterator(); iNut.hasNext();) {
                    String nutName = (String) iNut.next();
                    if (!line.nutrients.containsKey(nutName)) {
                        Log.println(Log.ERROR, sourceMethod, "nutrient not found in db");
                        return false;
                    }
                }
                /*
                 * correct values below 0
                 */
                line.correctMasses();
            }
        }
        if (input.isEmpty()) {
            Log.println(Log.WARNING, sourceMethod, "Nothing to add : Empty data provided");
            return false;
        }
        return true;
    }

    /**
     * *
     * add a data line to the db
     *
     * @param stepId Id of the step to add
     * @param stepName Name of the step to add
     * @param input List of object extending EmergeCompartmentData
     * @param allowZeroMassValues true => dont add line in db if all masses = 0.
     * @param removeDuplicate true => remove duplicates if any before inserting (slows insert process)
     * @return true if successful
     */
    public boolean add(short stepId, String stepName, List<? extends EmergeCompartmentData> input, boolean allowZeroMassValues, boolean removeDuplicate) {
        final String methodName = "BMMDB.add()";
        boolean result = false;
        Statement deleteStatement = null;
        try {
            if (!ready) {
                Log.println(Log.ERROR, methodName, "Cannot insert data in table " + dbTablePrefix + currentInstance + " because db is not ready");
                return result;
            }
            if (!checkInput(input, methodName)) {
                return result;
            }
            connection.setAutoCommit(false);
            StringBuffer sql;
            int parameterIndex;
            /*
             * batch insert lines
             */
            int totalToInsert = input.size();
            int progress = 0;
            int fromIndex = 0;
            int toIndex;
            if (totalToInsert < insertBatchSize) {
                toIndex = totalToInsert;
            }
            else {
                toIndex = insertBatchSize;
            }
            while (fromIndex < totalToInsert && continueUpdate) {
                if (removeDuplicate) {
                    deleteStatement = connection.createStatement();
                    /*
                     * remove duplicate
                     */
                    for (Iterator it = input.subList(fromIndex, toIndex).iterator(); it.hasNext();) {
                        EmergeCompartmentData data = (EmergeCompartmentData) it.next();
                        sql = new StringBuffer("DELETE FROM " + dbTablePrefix + currentInstance + " WHERE  (");
                        /*
                         * StepName
                         */ sql.append(" STEP_NAME='").append(stepName).append("'");
                        if (data instanceof EmergeTreeData) {/*
                             * Tree line
                             */
                            EmergeTreeData line = (EmergeTreeData) it.next();
                            /*
                             * component
                             */
                            sql.append(" AND COMPONENT='TREE'");
                            /*
                             * subComponent
                             */
                            sql.append(" AND SUB_COMPONENT").append((line.subComponent == null ? " IS NULL " : ("='" + line.subComponent + "'")));
                        }
                        else if (data instanceof EmergeStemData) {/*
                             * Stem line
                             */
                            EmergeStemData line = (EmergeStemData) data;
                            /*
                             * componentId
                             */
                            sql.append(" AND COMPONENT_ID").append((line.componentId == null ? " IS NULL " : ("=" + line.componentId)));
                            /*
                             * component
                             */
                            sql.append(" AND COMPONENT='STEM'");
                            /*
                             * subComponent
                             */
                            sql.append(" AND SUB_COMPONENT").append((line.subComponent == null ? " IS NULL " : ("='" + line.subComponent + "'")));
                            /*
                             * growthUnit
                             */
                            sql.append(" AND GROWTH_UNIT ").append((line.growthUnit == null ? " IS NULL " : ("=" + line.growthUnit)));
                            /*
                             * ring
                             */
                            sql.append(" AND RING ").append((line.ring == null ? " IS NULL " : ("=" + line.ring)));
                            /*
                             * commercial wood
                             */
                            sql.append(" AND COMMERCIAL_WOOD ").append((line.commercialWood == null ? " IS NULL " : ("=" + line.commercialWood)));
                        }
                        else if (data instanceof EmergeBranchData) {/*
                             * Branch line
                             */
                            EmergeBranchData line = (EmergeBranchData) data;
                            /*
                             * componentId
                             */
                            sql.append(" AND COMPONENT_ID").append((line.componentId == null ? " IS NULL " : ("=" + line.componentId)));
                            /*
                             * component
                             */
                            sql.append(" AND COMPONENT='BRANCH'");
                            /*
                             * subComponent
                             */
                            sql.append(" AND SUB_COMPONENT").append((line.subComponent == null ? " IS NULL " : ("='" + line.subComponent + "'")));
                            /*
                             * diameterClassLowerBound
                             */
                            sql.append(" AND DIAMETER_CLASS_LOWER_BOUND ").append((line.diameterClassLowerBound == null ? " IS NULL " : ("=" + line.diameterClassLowerBound)));
                            /*
                             * diameterClassUpperBound
                             */
                            sql.append(" AND DIAMETER_CLASS_UPPER_BOUND ").append((line.diameterClassUpperBound == null ? " IS NULL " : ("=" + line.diameterClassUpperBound)));
                            /*
                             * crownHeigthRangeLowerBound
                             */
                            sql.append(" AND CROWN_HEIGHT_RANGE_LOWER_BOUND ").append((line.crownHeigthRangeLowerBound == null ? " IS NULL " : ("=" + line.crownHeigthRangeLowerBound)));                            /*
                             * crownHeigthRangeUpperBound
                             */
                            sql.append(" AND CROWN_HEIGHT_RANGE_UPPER_BOUND ").append((line.crownHeigthRangeUpperBound == null ? " IS NULL " : ("=" + line.crownHeigthRangeUpperBound)));
                            /*
                             * commercial wood
                             */
                            sql.append(" AND COMMERCIAL_WOOD ").append((line.commercialWood == null ? " IS NULL " : ("=" + line.commercialWood)));
                        }
                        else if (data instanceof EmergeRootData) {/*
                             * Root line
                             */
                            EmergeRootData line = (EmergeRootData) data;
                            /*
                             * componentId
                             */
                            sql.append(" AND COMPONENT_ID").append((line.componentId == null ? " IS NULL " : ("=" + line.componentId)));
                            /*
                             * component
                             */
                            sql.append(" AND COMPONENT='ROOT' ");
                            /*
                             * subComponent
                             */
                            sql.append(" AND SUB_COMPONENT").append((line.subComponent == null ? " IS NULL " : ("='" + line.subComponent + "'")));
                            /*
                             * diameterClassLowerBound
                             */
                            sql.append(" AND DIAMETER_CLASS_LOWER_BOUND ").append((line.diameterClassLowerBound == null ? " IS NULL " : ("=" + line.diameterClassLowerBound)));
                            /*
                             * diameterClassUpperBound
                             */
                            sql.append(" AND DIAMETER_CLASS_UPPER_BOUND ").append((line.diameterClassUpperBound == null ? " IS NULL " : ("=" + line.diameterClassUpperBound)));
                            /*
                             * soilDepthRangeLowerBound
                             */
                            sql.append(" AND SOIL_DEPTH_LOWER_BOUND ").append((line.soilDepthRangeLowerBound == null ? " IS NULL " : ("=" + line.soilDepthRangeLowerBound)));
                            /*
                             * soilDepthRangeUpperBound
                             */
                            sql.append(" AND SOIL_DEPTH_UPPER_BOUND ").append((line.soilDepthRangeUpperBound == null ? " IS NULL " : ("=" + line.soilDepthRangeUpperBound)));
                        }
                        else if (data instanceof EmergeLeafData) {/*
                             * Leaf line
                             */
                            EmergeLeafData line = (EmergeLeafData) data;
                            /*
                             * component
                             */
                            sql.append(" AND COMPONENT='LEAF' ");
                            /*
                             * cohortAge
                             */
                            sql.append(" AND COHORT_AGE ").append((line.cohortAge == null ? " IS NULL " : ("=" + line.cohortAge)));
                            /*
                             * crownHeigthRangeLowerBound
                             */
                            sql.append(" AND CROWN_HEIGHT_RANGE_LOWER_BOUND ").append((line.crownHeigthRangeLowerBound == null ? " IS NULL " : ("=" + line.crownHeigthRangeLowerBound)));
                            /*
                             * crownHeigthRangeUpperBound
                             */
                            sql.append(" AND CROWN_HEIGHT_RANGE_UPPER_BOUND ").append((line.crownHeigthRangeUpperBound == null ? " IS NULL " : ("=" + line.crownHeigthRangeUpperBound)));
                        }
                        else if (data instanceof EmergeFruitData) {/*
                             * Fruit line
                             */
                            EmergeFruitData line = (EmergeFruitData) data;
                            /*
                             * component
                             */
                            sql.append(" AND COMPONENT='FRUIT' ");
                        }
                        else if (data instanceof EmergeStumpData) {/*
                             * Stump line
                             */
                            EmergeStumpData line = (EmergeStumpData) data;
                            /*
                             * component
                             */
                            sql.append(" AND COMPONENT='STUMP' ");
                            /*
                             * subComponent
                             */
                            sql.append(" AND SUB_COMPONENT" + (line.subComponent == null ? " IS NULL " : ("='" + line.subComponent + "'")));
                        }
                        /*
                         * treeId
                         */
                        sql.append(" AND TREE_ID=").append(data.treeId);
                        /*
                         * alive
                         */
                        sql.append(" AND ALIVE=").append(data.alive).append(")");
                        if (verboseSQL) {
                            System.out.println(sql.toString());
                        }
                        deleteStatement.addBatch(sql.toString());
                    }
                    time = System.currentTimeMillis();
                    int[] temp = deleteStatement.executeBatch();
                    time = System.currentTimeMillis() - time;
                    int duplicatesFound = 0;
                    for (int i = 0; i < temp.length; i++) {
                        duplicatesFound += temp[i];
                    }
                    if (duplicatesFound > 0) {
                        StatusDispatcher.print(duplicatesFound + " duplicate row(s) found and deleted");
                        consoleAppend(duplicatesFound + " duplicate row(s) found and deleted in " + time + " ms");
                        Log.println(Log.WARNING, methodName, duplicatesFound + " duplicate row(s) found and deleted");
                    }
                    deleteStatement.close();
                }
                /*
                 * add in table
                 */
                if (insertStatement == null) {
                    connection.rollback();
                    return false;
                }
                insertStatement.clearParameters();
                insertStatement.clearBatch();

                for (Iterator it = input.subList(fromIndex, toIndex).iterator(); it.hasNext();) {
                    EmergeCompartmentData data = (EmergeCompartmentData) it.next();
                    if (allowZeroMassValues || !data.isZeroMass()) {
                        parameterIndex = 1;
                        /*
                         * prevent insertion of 0 values, depending on corresponding boolean
                         */
                        /*
                         * StepId
                         */ insertStatement.setShort(parameterIndex++, stepId);
                        /*
                         * StepName
                         */ insertStatement.setString(parameterIndex++, stepName);
                        /*
                         * treeAge
                         */ insertStatement.setShort(parameterIndex++, data.treeAge);
                        /*
                         * treeId
                         */ insertStatement.setInt(parameterIndex++, data.treeId);
                        if (data instanceof EmergeTreeData) {/*
                             * Tree line
                             */
                            EmergeTreeData line = (EmergeTreeData) data;
                            /*
                             * componentId
                             */ insertStatement.setInt(parameterIndex++, -1);
                            /*
                             * component
                             */ insertStatement.setString(parameterIndex++, "TREE");
                            /*
                             * subComponent
                             */
                            if (line.subComponent != null) {
                                insertStatement.setString(parameterIndex++, data.subComponent.toString());
                            }
                            else {
                                insertStatement.setString(parameterIndex++, "NA");
                            }
                            /*
                             * alive
                             */ insertStatement.setBoolean(parameterIndex++, data.alive);
                            /*
                             * thinYear
                             */
                            if (line.thinAge != null) {
                                insertStatement.setShort(parameterIndex++, data.thinAge);
                            }
                            else {
                                insertStatement.setShort(parameterIndex++, (short) -1);
                            }
                            /*
                             * AboveGround
                             */ insertStatement.setBoolean(parameterIndex++, false);
                            /*
                             * crown
                             */ insertStatement.setBoolean(parameterIndex++, false);
                            /*
                             * growthUnit
                             */ insertStatement.setShort(parameterIndex++, (short) -1);
                            /*
                             * ring
                             */ insertStatement.setShort(parameterIndex++, (short) -1);
                            /*
                             * commecialWood
                             */
                            if (line.commercialWood != null) {
                                insertStatement.setBoolean(parameterIndex++, line.commercialWood);
                            }
                            else {
                                insertStatement.setNull(parameterIndex++, java.sql.Types.BOOLEAN);
                            }
                            /*
                             * diameterClassLowerBound
                             */ insertStatement.setShort(parameterIndex++, (short) -1);
                            /*
                             * diameterClassUpperBound
                             */ insertStatement.setShort(parameterIndex++, (short) -1);
                            /*
                             * crownHeigthRangeLowerBound
                             */ insertStatement.setByte(parameterIndex++, (byte) -1);
                            /*
                             * crownHeigthRangeUpperBound
                             */ insertStatement.setByte(parameterIndex++, (byte) -1);
                            /*
                             * cohortAge
                             */ insertStatement.setShort(parameterIndex++, (short) -1);
                            /*
                             * soilDepthRangeLowerBound
                             */ insertStatement.setShort(parameterIndex++, (short) -1);
                            /*
                             * soilDepthRangeUpperBound
                             */ insertStatement.setShort(parameterIndex++, (short) -1);
                        }
                        else if (data instanceof EmergeStemData) {/*
                             * Stem line
                             */
                            EmergeStemData line = (EmergeStemData) data;
                            /*
                             * componentId
                             */
                            if (line.componentId != null) {
                                insertStatement.setShort(parameterIndex++, data.componentId);
                            }
                            else {
                                insertStatement.setShort(parameterIndex++, (short) -1);
                            }
                            /*
                             * component
                             */ insertStatement.setString(parameterIndex++, "STEM");
                            /*
                             * subComponent
                             */
                            if (line.subComponent != null) {
                                insertStatement.setString(parameterIndex++, data.subComponent.toString());
                            }
                            else {
                                insertStatement.setString(parameterIndex++, "NA");
                            }
                            /*
                             * alive
                             */ insertStatement.setBoolean(parameterIndex++, data.alive);
                            /*
                             * thinYear
                             */
                            if (line.thinAge != null) {
                                insertStatement.setShort(parameterIndex++, data.thinAge);
                            }
                            else {
                                insertStatement.setShort(parameterIndex++, (short) -1);
                            }
                            /*
                             * AboveGround
                             */ insertStatement.setBoolean(parameterIndex++, true);
                            /*
                             * crown
                             */ insertStatement.setBoolean(parameterIndex++, false);
                            /*
                             * growthUnit
                             */
                            if (line.growthUnit != null) {
                                insertStatement.setShort(parameterIndex++, line.growthUnit);
                            }
                            else {
                                insertStatement.setShort(parameterIndex++, (short) -1);
                            }
                            /*
                             * ring
                             */
                            if (line.ring != null) {
                                insertStatement.setShort(parameterIndex++, line.ring);
                            }
                            else {
                                insertStatement.setShort(parameterIndex++, (short) -1);
                            }
                            /*
                             * commecialWood
                             */
                            if (line.commercialWood != null) {
                                insertStatement.setBoolean(parameterIndex++, line.commercialWood);
                            }
                            else {
                                insertStatement.setNull(parameterIndex++, java.sql.Types.BOOLEAN);
                            }
                            /*
                             * diameterClassLowerBound
                             */ insertStatement.setShort(parameterIndex++, (short) -1);
                            /*
                             * diameterClassUpperBound
                             */ insertStatement.setShort(parameterIndex++, (short) -1);
                            /*
                             * crownHeigthRangeLowerBound
                             */ insertStatement.setByte(parameterIndex++, (byte) -1);
                            /*
                             * crownHeigthRangeUpperBound
                             */ insertStatement.setByte(parameterIndex++, (byte) -1);
                            /*
                             * cohortAge
                             */ insertStatement.setShort(parameterIndex++, (short) -1);
                            /*
                             * soilDepthRangeLowerBound
                             */ insertStatement.setShort(parameterIndex++, (short) -1);
                            /*
                             * soilDepthRangeUpperBound
                             */ insertStatement.setShort(parameterIndex++, (short) -1);
                        }
                        else if (data instanceof EmergeBranchData) {/*
                             * Branch line
                             */
                            EmergeBranchData line = (EmergeBranchData) data;
                            /*
                             * componentId
                             */
                            if (line.componentId != null) {
                                insertStatement.setShort(parameterIndex++, data.componentId);
                            }
                            else {
                                insertStatement.setShort(parameterIndex++, (short) -1);
                            }
                            /*
                             * component
                             */ insertStatement.setString(parameterIndex++, "BRANCH");
                            /*
                             * subComponent
                             */
                            if (line.subComponent != null) {
                                insertStatement.setString(parameterIndex++, data.subComponent.toString());
                            }
                            else {
                                insertStatement.setString(parameterIndex++, "NA");
                            }
                            /*
                             * alive
                             */ insertStatement.setBoolean(parameterIndex++, data.alive);
                            /*
                             * thinYear
                             */
                            if (line.thinAge != null) {
                                insertStatement.setShort(parameterIndex++, data.thinAge);
                            }
                            else {
                                insertStatement.setShort(parameterIndex++, (short) -1);
                            }
                            /*
                             * AboveGround
                             */ insertStatement.setBoolean(parameterIndex++, true);
                            /*
                             * crown
                             */ insertStatement.setBoolean(parameterIndex++, true);
                            /*
                             * growthUnit
                             */ insertStatement.setShort(parameterIndex++, (short) -1);
                            /*
                             * ring
                             */ insertStatement.setShort(parameterIndex++, (short) -1);
                            /*
                             * commecialWood
                             */
                            if (line.commercialWood != null) {
                                insertStatement.setBoolean(parameterIndex++, line.commercialWood);
                            }
                            else {
                                insertStatement.setNull(parameterIndex++, java.sql.Types.BOOLEAN);
                            }
                            /*
                             * diameterClassLowerBound
                             */
                            if (line.diameterClassLowerBound != null) {
                                insertStatement.setShort(parameterIndex++, line.diameterClassLowerBound);
                            }
                            else {
                                insertStatement.setShort(parameterIndex++, (short) -1);
                            }
                            /*
                             * diameterClassUpperBound
                             */
                            if (line.diameterClassUpperBound != null) {
                                insertStatement.setShort(parameterIndex++, line.diameterClassUpperBound);
                            }
                            else {
                                insertStatement.setShort(parameterIndex++, (short) -1);
                            }
                            /*
                             * crownHeigthRangeLowerBound
                             */
                            if (line.crownHeigthRangeLowerBound != null) {
                                insertStatement.setByte(parameterIndex++, line.crownHeigthRangeLowerBound);
                            }
                            else {
                                insertStatement.setByte(parameterIndex++, (byte) -1);
                            }
                            /*
                             * crownHeigthRangeUpperBound
                             */
                            if (line.crownHeigthRangeUpperBound != null) {
                                insertStatement.setByte(parameterIndex++, line.crownHeigthRangeUpperBound);
                            }
                            else {
                                insertStatement.setByte(parameterIndex++, (byte) -1);
                            }
                            /*
                             * cohortAge
                             */ insertStatement.setShort(parameterIndex++, (short) -1);
                            /*
                             * soilDepthRangeLowerBound
                             */ insertStatement.setShort(parameterIndex++, (short) -1);
                            /*
                             * soilDepthRangeUpperBound
                             */ insertStatement.setShort(parameterIndex++, (short) -1);
                        }
                        else if (data instanceof EmergeRootData) {/*
                             * Root line
                             */
                            EmergeRootData line = (EmergeRootData) data;
                            /*
                             * componentId
                             */
                            if (line.componentId != null) {
                                insertStatement.setShort(parameterIndex++, data.componentId);
                            }
                            else {
                                insertStatement.setShort(parameterIndex++, (short) -1);
                            }
                            /*
                             * component
                             */ insertStatement.setString(parameterIndex++, "ROOT");
                            /*
                             * subComponent
                             */
                            if (line.subComponent != null) {
                                insertStatement.setString(parameterIndex++, data.subComponent.toString());
                            }
                            else {
                                insertStatement.setString(parameterIndex++, "NA");
                            }
                            /*
                             * alive
                             */ insertStatement.setBoolean(parameterIndex++, data.alive);
                            /*
                             * thinYear
                             */
                            if (line.thinAge != null) {
                                insertStatement.setShort(parameterIndex++, data.thinAge);
                            }
                            else {
                                insertStatement.setShort(parameterIndex++, (short) -1);
                            }
                            /*
                             * AboveGround
                             */ insertStatement.setBoolean(parameterIndex++, false);
                            /*
                             * crown
                             */ insertStatement.setBoolean(parameterIndex++, true);
                            /*
                             * growthUnit
                             */ insertStatement.setShort(parameterIndex++, (short) -1);
                            /*
                             * ring
                             */ insertStatement.setShort(parameterIndex++, (short) -1);
                            /*
                             * Commercial wood
                             */
                            insertStatement.setNull(parameterIndex++, java.sql.Types.BOOLEAN);
                            /*
                             * diameterClassLowerBound
                             */
                            if (line.diameterClassLowerBound != null) {
                                insertStatement.setShort(parameterIndex++, line.diameterClassLowerBound);
                            }
                            else {
                                insertStatement.setShort(parameterIndex++, (short) -1);
                            }
                            /*
                             * diameterClassUpperBound
                             */
                            if (line.diameterClassUpperBound != null) {
                                insertStatement.setShort(parameterIndex++, line.diameterClassUpperBound);
                            }
                            else {
                                insertStatement.setShort(parameterIndex++, (short) -1);
                            }
                            /*
                             * crownHeigthRangeLowerBound
                             */ insertStatement.setByte(parameterIndex++, (byte) -1);
                            /*
                             * crownHeigthRangeUpperBound
                             */ insertStatement.setByte(parameterIndex++, (byte) -1);
                            /*
                             * cohortAge
                             */ insertStatement.setShort(parameterIndex++, (short) -1);
                            /*
                             * soilDepthRangeLowerBound
                             */
                            if (line.soilDepthRangeLowerBound != null) {
                                insertStatement.setShort(parameterIndex++, line.soilDepthRangeLowerBound);
                            }
                            else {
                                insertStatement.setShort(parameterIndex++, (short) -1);
                            }
                            /*
                             * soilDepthRangeUpperBound
                             */
                            if (line.soilDepthRangeUpperBound != null) {
                                insertStatement.setShort(parameterIndex++, line.soilDepthRangeUpperBound);
                            }
                            else {
                                insertStatement.setShort(parameterIndex++, (short) -1);
                            }
                        }
                        else if (data instanceof EmergeLeafData) {/*
                             * Leaf line
                             */
                            EmergeLeafData line = (EmergeLeafData) data;
                            /*
                             * componentId
                             */ insertStatement.setShort(parameterIndex++, (short) -1);
                            /*
                             * component
                             */ insertStatement.setString(parameterIndex++, "LEAF");
                            /*
                             * subComponent
                             */ insertStatement.setString(parameterIndex++, "NA");
                            /*
                             * alive
                             */ insertStatement.setBoolean(parameterIndex++, data.alive);
                            /*
                             * thinYear
                             */
                            if (line.thinAge != null) {
                                insertStatement.setShort(parameterIndex++, data.thinAge);
                            }
                            else {
                                insertStatement.setShort(parameterIndex++, (short) -1);
                            }
                            /*
                             * AboveGround
                             */ insertStatement.setBoolean(parameterIndex++, true);
                            /*
                             * crown
                             */ insertStatement.setBoolean(parameterIndex++, true);
                            /*
                             * growthUnit
                             */ insertStatement.setShort(parameterIndex++, (short) -1);
                            /*
                             * ring
                             */ insertStatement.setShort(parameterIndex++, (short) -1);
                            /*
                             * Commercial wood
                             */
                            insertStatement.setNull(parameterIndex++, java.sql.Types.BOOLEAN);
                            /*
                             * diameterClassLowerBound
                             */ insertStatement.setShort(parameterIndex++, (short) -1);
                            /*
                             * diameterClassUpperBound
                             */ insertStatement.setShort(parameterIndex++, (short) -1);
                            /*
                             * crownHeigthRangeLowerBound
                             */
                            if (line.crownHeigthRangeLowerBound != null) {
                                insertStatement.setByte(parameterIndex++, line.crownHeigthRangeLowerBound);
                            }
                            else {
                                insertStatement.setByte(parameterIndex++, (byte) -1);
                            }
                            /*
                             * crownHeigthRangeUpperBound
                             */
                            if (line.crownHeigthRangeUpperBound != null) {
                                insertStatement.setByte(parameterIndex++, line.crownHeigthRangeUpperBound);
                            }
                            else {
                                insertStatement.setByte(parameterIndex++, (byte) -1);
                            }
                            /*
                             * cohortAge
                             */ insertStatement.setShort(parameterIndex++, line.cohortAge);
                            /*
                             * soilDepthRangeLowerBound
                             */ insertStatement.setShort(parameterIndex++, (short) -1);
                            /*
                             * soilDepthRangeUpperBound
                             */ insertStatement.setShort(parameterIndex++, (short) -1);
                        }
                        else if (data instanceof EmergeFruitData) {/*
                             * Fruit line
                             */
                            EmergeFruitData line = (EmergeFruitData) data;
                            /*
                             * componentId
                             */ insertStatement.setShort(parameterIndex++, (short) -1);
                            /*
                             * component
                             */ insertStatement.setString(parameterIndex++, "FRUIT");
                            /*
                             * subComponent
                             */ insertStatement.setString(parameterIndex++, "NA");
                            /*
                             * alive
                             */ insertStatement.setBoolean(parameterIndex++, data.alive);
                            /*
                             * thinYear
                             */
                            if (line.thinAge != null) {
                                insertStatement.setShort(parameterIndex++, data.thinAge);
                            }
                            else {
                                insertStatement.setShort(parameterIndex++, (short) -1);
                            }
                            /*
                             * AboveGround
                             */ insertStatement.setBoolean(parameterIndex++, true);
                            /*
                             * crown
                             */ insertStatement.setBoolean(parameterIndex++, true);
                            /*
                             * growthUnit
                             */ insertStatement.setShort(parameterIndex++, (short) -1);
                            /*
                             * ring
                             */ insertStatement.setShort(parameterIndex++, (short) -1);
                            /*
                             * Commercial wood
                             */
                            insertStatement.setNull(parameterIndex++, java.sql.Types.BOOLEAN);
                            /*
                             * diameterClassLowerBound
                             */ insertStatement.setShort(parameterIndex++, (short) -1);
                            /*
                             * diameterClassUpperBound
                             */ insertStatement.setShort(parameterIndex++, (short) -1);
                            /*
                             * crownHeigthRangeLowerBound
                             */ insertStatement.setByte(parameterIndex++, (byte) -1);
                            /*
                             * crownHeigthRangeUpperBound
                             */ insertStatement.setByte(parameterIndex++, (byte) -1);
                            /*
                             * cohortAge
                             */ insertStatement.setShort(parameterIndex++, (short) -1);
                            /*
                             * soilDepthRangeLowerBound
                             */ insertStatement.setShort(parameterIndex++, (short) -1);
                            /*
                             * soilDepthRangeUpperBound
                             */ insertStatement.setShort(parameterIndex++, (short) -1);
                        }
                        else if (data instanceof EmergeStumpData) {/*
                             * Stump line
                             */
                            EmergeStumpData line = (EmergeStumpData) data;
                            /*
                             * componentId
                             */ insertStatement.setShort(parameterIndex++, (short) -1);
                            /*
                             * component
                             */ insertStatement.setString(parameterIndex++, "STUMP");
                            /*
                             * subComponent
                             */ insertStatement.setString(parameterIndex++, "NA");
                            /*
                             * alive
                             */ insertStatement.setBoolean(parameterIndex++, data.alive);
                            /*
                             * thinYear
                             */
                            if (line.thinAge != null) {
                                insertStatement.setShort(parameterIndex++, data.thinAge);
                            }
                            else {
                                insertStatement.setShort(parameterIndex++, (short) -1);
                            }
                            /*
                             * AboveGround
                             */ insertStatement.setBoolean(parameterIndex++, true);
                            /*
                             * crown
                             */ insertStatement.setBoolean(parameterIndex++, false);
                            /*
                             * growthUnit
                             */ insertStatement.setShort(parameterIndex++, (short) -1);
                            /*
                             * ring
                             */ insertStatement.setShort(parameterIndex++, (short) -1);
                            /*
                             * Commercial wood
                             */
                            insertStatement.setNull(parameterIndex++, java.sql.Types.BOOLEAN);
                            /*
                             * diameterClassLowerBound
                             */ insertStatement.setShort(parameterIndex++, (short) -1);
                            /*
                             * diameterClassUpperBound
                             */ insertStatement.setShort(parameterIndex++, (short) -1);
                            /*
                             * crownHeigthRangeLowerBound
                             */ insertStatement.setShort(parameterIndex++, (short) -1);
                            /*
                             * crownHeigthRangeUpperBound
                             */ insertStatement.setShort(parameterIndex++, (short) -1);
                            /*
                             * cohortAge
                             */ insertStatement.setShort(parameterIndex++, (short) -1);
                            /*
                             * soilDepthRangeLowerBound
                             */ insertStatement.setShort(parameterIndex++, (short) -1);
                            /*
                             * soilDepthRangeUpperBound
                             */ insertStatement.setShort(parameterIndex++, (short) -1);
                        }
                        insertStatement.setDouble(parameterIndex++, data.biomass);

                        for (Iterator iNut = nutrients.iterator(); iNut.hasNext();) {
                            String key = (String) iNut.next();
                            insertStatement.setDouble(parameterIndex++, data.nutrients.get(key));
                        }
                        insertStatement.addBatch();
                    }
                }
                /*
                 * execute batch
                 */
//                time = System.currentTimeMillis() - time;
//                consoleAppend("batch of  lines prepared in " + time + " ms");
                time = System.currentTimeMillis();
                int[] temp = insertStatement.executeBatch();
                connection.commit();
                time = System.currentTimeMillis() - time;
                /*
                 * sum up what was inserted
                 */
                for (int i = 0; i < temp.length; i++) {
                    progress += temp[i];
                }
                /*
                 * user feedback
                 */
                consoleAppend(progress + "/" + totalToInsert + " lines inserted in " + time + " ms");
                /*
                 * update sublist parameters
                 */
                if (totalToInsert - progress > 0) {
                    fromIndex += insertBatchSize;
                    toIndex += insertBatchSize;
                    if (toIndex > totalToInsert) {
                        toIndex = totalToInsert;
                    }
                }
                else {
                    if (totalToInsert != progress) {
                        Log.println(Log.WARNING, methodName, "Some data was not inserted into table " + dbTablePrefix + currentInstance + " !!");
                    }
                    break;
                }
            }
            connection.setAutoCommit(true);
            result = true;
        }
        catch (SQLException e) {
            Log.println(Log.ERROR, methodName, e.getMessage(), e);
        }
        finally {
            try {
                if (deleteStatement != null) {
                    deleteStatement.close();
                }
            }
            catch (Exception e) {
            }
        }
        return result;
    }

    /**
     * *
     * Mark/unmark a tree as dead in the DB by setting THIN_AGE to treeAge/null for the corresponding tree for years>=treeAge in the step scenario
     *
     * @param treeId id of the tree
     * @param treeAge age of the tree
     * @param thinStatus mark or unmark
     * @return true if successful
     */
    public boolean setThin(Tree tree, Step step, boolean thinStatus) {
        final String methodName = "BMMDB.setThin()";
        Statement statement = null;
        boolean result = false;
        if (ready) {
            String scen = getScenario(step);
            try {
                statement = connection.createStatement();
                String sql;
                if (thinStatus) {
                    sql =
                            "UPDATE  " + dbTablePrefix + currentInstance + " SET THIN_AGE=" + tree.getAge() + " WHERE TREE_ID=" + tree.getId() + " AND TREE_AGE>=" + tree.getAge()
                            + " AND UPPER(RIGHT(STEP_NAME,1))='" + scen + "' ";
                }
                else {
                    sql =
                            "UPDATE  " + dbTablePrefix + currentInstance + " SET THIN_AGE=NULL WHERE TREE_ID=" + tree.getId() + " AND TREE_AGE>=" + tree.getAge()
                            + " AND UPPER(RIGHT(STEP_NAME,1))='"
                            + scen + "' ";
                }
                if (verboseSQL) {
                    System.out.println(sql);
                }
                int updatedRows = statement.executeUpdate(sql);
                Log.println(Log.INFO, methodName, updatedRows + "row(s) updated");
                if (updatedRows > 0) {
                    result = true;
                }
            }
            catch (SQLException e) {
                Log.println(Log.ERROR, methodName, e.getMessage(), e);
            }
            finally {
                if (statement != null) {
                    try {
                        statement.close();
                    }
                    catch (SQLException e) {
                        Log.println(Log.ERROR, methodName, " Cannot close statement", e);
                    }
                }
            }
        }
        else {
            result = false;
            Log.println(Log.ERROR, methodName, "Cannot update " + dbTablePrefix + currentInstance + " because db is not ready");
        }
        return result;
    }

    /**
     * *
     * Remove a step from the db
     *
     * @param stepId Id of the step to remove (step name can change if not main scenario (a)).
     * @return true if successful
     */
    boolean removeStep(Integer stepId) {
        final String methodName = "BMMDB.removeStep()";
        Statement statement = null;
        boolean result = false;
        if (ready) {
            try {
                statement = connection.createStatement();
                String sql = "DELETE FROM " + dbTablePrefix + currentInstance + " WHERE STEP_ID=" + stepId;
                if (verboseSQL) {
                    System.out.println(sql);
                }
                int deletedRows = statement.executeUpdate(sql);
                Log.println(Log.INFO, methodName, deletedRows + "row(s) found and deleted");
                if (deletedRows > 0) {
                    result = true;
                }
            }
            catch (SQLException e) {
                Log.println(Log.ERROR, methodName, e.getMessage(), e);
            }
            finally {
                if (statement != null) {
                    try {
                        statement.close();
                    }
                    catch (SQLException e) {
                        Log.println(Log.ERROR, methodName, " Cannot close statement", e);
                    }
                }
            }
        }
        else {
            result = false;
            Log.println(Log.ERROR, methodName, "Cannot delete in table " + dbTablePrefix + currentInstance + " because db is not ready");
        }
        return result;
    }

    /**
     * *
     * Fetch metadata of instance table
     *
     * @return metadata as ResultSet
     */
    public ResultSet getTablesInfos() {
        final String methodName = "BMMDB.getTablesInfos()";
        Statement statement = null;
        ResultSet result = null;
        if (ready) {
            try {
                statement = connection.createStatement();
                result = statement.executeQuery("SELECT COLUMN_NAME, TYPE_NAME,  IS_NULLABLE FROM INFORMATION_SCHEMA.SYSTEM_COLUMNS WHERE TABLE_NAME = '" + dbTablePrefix
                        + currentInstance + "'");
            }
            catch (Exception e) {
                Log.println(Log.ERROR, methodName, e.getMessage(), e);
            }
            finally {
                if (statement != null) {
                    try {
                        statement.close();
                    }
                    catch (SQLException e) {
                        Log.println(Log.ERROR, methodName, " Cannot close statement", e);
                    }
                }
            }
        }
        else {
            Log.println(Log.ERROR, methodName, "Cannot get info for table " + dbTablePrefix + currentInstance + " because db is not ready");
        }
        return result;
    }

    /**
     * *
     * Initialize DB update progress window
     */
    private void initDialog() {
        dialog = new JDialog();
        dialog.setBounds(100, 100, 400, 600);
        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.setTitle("Biomass and mineral mass computation progress");
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        dialog.getContentPane().add(contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(new BorderLayout(0, 0));
        console = new JTextArea();
        console.setEditable(false);
        jsp = new JScrollPane(console);
        contentPanel.add(jsp, BorderLayout.CENTER);
        progressBar = new JProgressBar(0, 0);
        //progressBar.setIndeterminate(true);
        contentPanel.add(progressBar, BorderLayout.SOUTH);
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
        dialog.getContentPane().add(buttonPane, BorderLayout.SOUTH);
        stopButton = new JButton("Stop");
        stopButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                continueUpdate = false;
                consoleAppend("Stopping update process..");
            }
        });
        buttonPane.add(stopButton);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }

    /**
     * *
     * Append string in the DB update progress window
     *
     * @param string
     */
    public void consoleAppend(final String string) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                console.append(string + "\n");
                jsp.getVerticalScrollBar().setValue(jsp.getVerticalScrollBar().getMaximum());
            }
        });
    }

    /**
     * *
     * Check if db is sync with capsis UI. If not, update it by deleting steps if needed and filling new steps by calling the getBMMValues method in the model
     *
     * @return true if successful
     */
    public boolean update() {
        final String methodName = "BMMDB.update()";

        Statement statement = null;
        boolean result = false;
        if (ready) {
            try {/*
                 * if db listens to project, use add/delete lists, else compare db and ui
                 */
                if (updateNeeded) {
                    time = System.currentTimeMillis();
                    if (verbose) {
                        System.out.println("Updating datbase...");
                    }
                    progressBar.setMaximum(steps.size());
                    console.setText("");
                    dialog.setVisible(true);
                    int n = 0;
                    /*
                     * iterate steps collection
                     */
                    for (Iterator iSteps = steps.iterator(); iSteps.hasNext();) {
                        if (!continueUpdate)/*
                         * stop button set this to false
                         */ {
                            break;
                        }
                        Step step = (Step) iSteps.next();
                        /*
                         * if the step has no project, remove it
                         */
                        if (step.getProject() == null) {
                            consoleAppend("removing step #" + step.getId());
                            removeStep(step.getId());
                        }
                        else {
                            consoleAppend("Computing bio/mineral masses for step " + step.getName());
                            model.getBMMValues(step);
                        }
                        /*
                         * removed the processed step
                         */
                        iSteps.remove();
                        progressBar.setValue(++n);
                    }
                    updateThinnedTrees();
                    time = System.currentTimeMillis() - time;
                    System.out.println("Database update finished in " + time + " ms");
                    dialog.setVisible(false);
                    updateNeeded = false;
                }
//                else if (VERBOSE) {
//                    System.out.println("No database update needed");
//                }
                continueUpdate = true;
                result = true;
            }
            catch (Exception e) {
                Log.println(Log.ERROR, methodName, e.getMessage(), e);
            }
            finally {
                if (statement != null) {
                    try {
                        statement.close();
                    }
                    catch (SQLException e) {
                        Log.println(Log.ERROR, methodName, " Cannot close statement", e);
                    }
                }
            }
        }
        else {
            result = false;
            Log.println(Log.ERROR, methodName, "Cannot update " + dbTablePrefix + currentInstance + " because db is not ready");
        }
        return result;
    }

    /**
     * *
     * Interpolate missing values from age=0 for stem wood values
     *
     * @return true if successful
     */
    public boolean interpolateMissingStemWoodValues() {
        final String methodName = "BMMDB.interpolateMissingStemWoodValues()";
        Statement statement = null;
        int parameterIndex;
        boolean result = false;
        if (ready) {
            try {
                Integer treeId = null, stemId = null;
                statement = connection.createStatement();
                /*
                 * get list of stem/tree without data for age=0
                 */
                StringBuffer sql = new StringBuffer("SELECT DISTINCT TREE_ID,COMPONENT_ID");
                sql.append(" FROM(SELECT TREE_ID,COMPONENT_ID,MIN(TREE_AGE) AS MINAGE FROM ");
                sql.append(dbTablePrefix).append(currentInstance);
                sql.append(" WHERE COMPONENT='STEM' AND SUB_COMPONENT='WOOD'");
                sql.append(" GROUP BY TREE_ID,COMPONENT_ID ORDER BY TREE_ID) WHERE MINAGE>0");
                /*
                 * insert missing values for age 0
                 */
                boolean hasData = false;
                if (verboseSQL) {
                    System.out.println(sql);
                }
                ResultSet rs = statement.executeQuery(sql.toString());
                if (rs != null) {
                    insertStatement.clearParameters();
                    insertStatement.clearBatch();
                    while (rs.next()) {
                        parameterIndex = 1;
                        hasData = true;
                        treeId = rs.getInt(1);
                        stemId = rs.getInt(2);
                        insertStatement.setShort(parameterIndex++, (short) 0);/*
                         * stepId
                         */
                        insertStatement.setString(parameterIndex++, "NA");/*
                         * stepName
                         */
                        insertStatement.setShort(parameterIndex++, (short) 0);/*
                         * treeAge
                         */
                        insertStatement.setInt(parameterIndex++, treeId);/*
                         * treeId
                         */
                        insertStatement.setInt(parameterIndex++, stemId);/*
                         * componentId
                         */
                        insertStatement.setString(parameterIndex++, "STEM");/*
                         * component
                         */
                        insertStatement.setString(parameterIndex++, "WOOD");/*
                         * subComponent
                         */
                        insertStatement.setBoolean(parameterIndex++, true);/*
                         * alive
                         */
                        insertStatement.setShort(parameterIndex++, (short) -1);/*
                         * thinAge
                         */
                        insertStatement.setBoolean(parameterIndex++, true);/*
                         * aboveGround
                         */
                        insertStatement.setBoolean(parameterIndex++, false);/*
                         * crown
                         */
                        insertStatement.setShort(parameterIndex++, (short) -1);/*
                         * growthUnit
                         */
                        insertStatement.setShort(parameterIndex++, (short) -1);/*
                         * ring
                         */
                        /*
                         * Commercial wood
                         */
                        insertStatement.setNull(parameterIndex++, java.sql.Types.BOOLEAN);
                        insertStatement.setShort(parameterIndex++, (short) -1);/*
                         * diameterClassLowerBound
                         */
                        insertStatement.setShort(parameterIndex++, (short) -1);/*
                         * diameterClassUpperBound
                         */
                        insertStatement.setByte(parameterIndex++, (byte) -1);/*
                         * crownHeightRangeLowerBound
                         */
                        insertStatement.setByte(parameterIndex++, (byte) -1);/*
                         * crownHeightRangeUpperrBound
                         */
                        insertStatement.setShort(parameterIndex++, (short) -1);/*
                         * cohortAge
                         */
                        insertStatement.setShort(parameterIndex++, (short) -1);/*
                         * soilDepthRangeLowerBound
                         */
                        insertStatement.setShort(parameterIndex++, (short) -1);/*
                         * soilDepthRangeUpperBound
                         */
                        insertStatement.setDouble(parameterIndex++, 0d);         /*
                         * Biomass
                         */
                        for (Iterator it = nutrients.iterator(); it.hasNext();) {
                            insertStatement.setDouble(parameterIndex++, 0d);/*
                             * nutriment
                             */
                            it.next();
                        }
                        insertStatement.addBatch();
//                        if (VERBOSE_SQL) {
//                            System.out.println(insertStatement.toString());
//                        }
                    }
                    if (hasData) {
                        int[] insertedLines = insertStatement.executeBatch();
                        if (verbose) {
                            int totalInsertedLines = 0;
                            for (int i = 0; i < insertedLines.length; i++) {
                                totalInsertedLines += insertedLines[i];
                            }
                            System.out.println(totalInsertedLines + " zero lines inserted for age 0");
                        }
                    }
                }
                /*
                 * get max age
                 */
                int maxAge = 0;
                sql = new StringBuffer("SELECT MAX(TREE_AGE) FROM ").append(dbTablePrefix).append(currentInstance).append(" WHERE COMPONENT='STEM' AND SUB_COMPONENT='WOOD'");
                if (verboseSQL) {
                    System.out.println(sql);
                }
                rs = statement.executeQuery(sql.toString());
                if (rs != null) {
                    if (rs.next()) {
                        maxAge = rs.getInt(1);
                    }
                }
                if (rs.wasNull()) {
                    Log.println(Log.ERROR, methodName, "Interpolation failed : Could not retrieve maximum tree age");
                    return false;
                }
                if (maxAge == 0) {
                    Log.println(Log.ERROR, methodName, "Interpolation failed : Maximum tree age = 0");
                    return false;
                }
                /*
                 * Get list of tree/stem ids to initialize tree/stem/age list
                 */
                HashMap<Integer, HashMap<Integer, HashMap<Integer, Boolean>>> treeStemAgeList = new HashMap<Integer, HashMap<Integer, HashMap<Integer, Boolean>>>();
                sql = new StringBuffer("SELECT DISTINCT TREE_ID,COMPONENT_ID FROM ").append(dbTablePrefix).append(currentInstance).append(" WHERE COMPONENT='STEM' AND SUB_COMPONENT='WOOD'");
                if (verboseSQL) {
                    System.out.println(sql);
                }
                rs = statement.executeQuery(sql.toString());
                if (rs == null) {
                    Log.println(Log.ERROR, methodName, "Interpolation failed : Could not retrieve tree/stem ids");
                    return false;
                }
                while (rs.next()) {
                    treeId = rs.getInt(1);
                    stemId = rs.getInt(2);
                    if (!treeStemAgeList.containsKey(treeId))/*
                     * create sub collection for tree id if not exists
                     */ {
                        treeStemAgeList.put(treeId, new HashMap<Integer, HashMap<Integer, Boolean>>());
                    }
                    if (!treeStemAgeList.get(treeId).containsKey(stemId)) {/*
                         * create sub collection for stem id if not exists
                         */
                        treeStemAgeList.get(treeId).put(stemId, new HashMap<Integer, Boolean>());
                    }
                    /*
                     * set everything to false for all ages
                     */
                    for (int i = 0; i <= maxAge; i++) {
                        treeStemAgeList.get(treeId).get(stemId).put(i, false);
                    }
                }

                /*
                 * check what is missing by marking existing data
                 */
                sql = new StringBuffer("SELECT DISTINCT TREE_ID,COMPONENT_ID,TREE_AGE FROM ").append(dbTablePrefix).append(currentInstance);
                sql.append(" WHERE COMPONENT='STEM' AND SUB_COMPONENT='WOOD'");
                if (verboseSQL) {
                    System.out.println(sql);
                }
                rs = statement.executeQuery(sql.toString());
                if (rs == null) {
                    Log.println(Log.ERROR, methodName, "Interpolation failed : Could not retrieve tree ids / stem ids / tree age");
                    return false;
                }
                while (rs.next()) {
                    treeStemAgeList.get(rs.getInt(1)).get(rs.getInt(2)).put(rs.getInt(3), true);
                }
                /*
                 * interpolate missing values
                 */
                insertStatement.clearParameters();
                insertStatement.clearBatch();
                boolean ageIsMissing = false;
                for (Iterator iTree = treeStemAgeList.keySet().iterator(); iTree.hasNext();) {
                    Integer tId = (Integer) iTree.next();
                    for (Iterator iStem = treeStemAgeList.get(tId).keySet().iterator(); iStem.hasNext();) {
                        Integer sId = (Integer) iStem.next();
                        for (Iterator iAge = treeStemAgeList.get(tId).get(sId).keySet().iterator(); iAge.hasNext();) {
                            Integer tAge = (Integer) iAge.next();
                            /*
                             * if age is missing, interpolate
                             */
                            ageIsMissing = false;
                            if (treeStemAgeList.containsKey(tId)) {
                                if (treeStemAgeList.get(tId).containsKey(sId)) {
                                    if (treeStemAgeList.get(tId).get(sId).containsKey(tAge)) {
                                        ageIsMissing = !treeStemAgeList.get(tId).get(sId).get(tAge);
                                    }
                                }
                            }
                            if (ageIsMissing) {/*
                                 * interpolation query
                                 */
                                sql = new StringBuffer("SELECT (B.TREE_AGE-").append(tAge.doubleValue()).append(")/(B.TREE_AGE-A.TREE_AGE)*A.BIOMASS");
                                sql.append("+(").append(tAge.doubleValue()).append("-A.TREE_AGE)/(B.TREE_AGE-A.TREE_AGE)*B.BIOMASS  as INFERED_B");/*
                                 * biomass
                                 */
                                for (Iterator it = nutrients.iterator(); it.hasNext();) {/*
                                     * nutrients
                                     */
                                    String nutName = ((String) it.next()).toUpperCase();
                                    sql.append(",(B.TREE_AGE-").append(tAge.doubleValue()).append(")/(B.TREE_AGE-A.TREE_AGE)*A.").append(nutName);
                                    sql.append("+(").append(tAge.doubleValue()).append("-A.TREE_AGE)/(B.TREE_AGE-A.TREE_AGE)*B.").append(nutName);
                                    sql.append(" as INFERED_").append(nutName);
                                }
                                sql.append(" FROM ").append(dbTablePrefix).append(currentInstance).append(" AS A,");
                                sql.append(dbTablePrefix).append(currentInstance).append(" AS B");
                                sql.append(" WHERE ").append(" A.COMPONENT='STEM' AND A.SUB_COMPONENT='WOOD' AND A.COMPONENT_ID ").append("=").append(sId);
                                sql.append(" AND A.TREE_ID=").append(tId);
                                sql.append(" AND").append(" B.COMPONENT='STEM' AND B.SUB_COMPONENT='WOOD' AND B.COMPONENT_ID ").append("=").append(sId);
                                sql.append(" AND B.TREE_ID=").append(tId);
                                sql.append(" AND A.TREE_AGE = (SELECT MAX(TREE_AGE)");
                                sql.append(" FROM (SELECT TREE_AGE FROM EPTUS0 WHERE COMPONENT='STEM' AND SUB_COMPONENT='WOOD' AND TREE_ID=1 AND COMPONENT_ID =").append(sId);
                                sql.append(" AND TREE_AGE<").append(tAge.intValue()).append(" )) AND");
                                sql.append(" B.TREE_AGE = (SELECT MIN(TREE_AGE) FROM (SELECT TREE_AGE FROM EPTUS0 WHERE COMPONENT='STEM' AND SUB_COMPONENT='WOOD'");
                                sql.append(" AND TREE_ID=1 AND COMPONENT_ID ").append("=").append(sId).append(" AND TREE_AGE>").append(tAge.intValue()).append(" )) ");
                                /*
                                 * update collection ?
                                 */
                                if (verboseSQL) {
                                    System.out.println(sql);
                                }
                                rs = statement.executeQuery(sql.toString());
                                if (rs == null) {
                                    Log.println(Log.ERROR, methodName, "Interpolation failed : could not perform interpolation query");
                                    return false;
                                }
                                if (rs.next()) {
                                    parameterIndex = 1;
                                    insertStatement.setShort(parameterIndex++, (short) 0);/*
                                     * stepId
                                     */
                                    insertStatement.setString(parameterIndex++, "NA");/*
                                     * stepName
                                     */
                                    insertStatement.setShort(parameterIndex++, tAge.shortValue());/*
                                     * treeAge
                                     */
                                    insertStatement.setInt(parameterIndex++, tId);/*
                                     * treeId
                                     */
                                    insertStatement.setInt(parameterIndex++, sId);/*
                                     * componentId
                                     */
                                    insertStatement.setString(parameterIndex++, "STEM");/*
                                     * component
                                     */
                                    insertStatement.setString(parameterIndex++, "WOOD");/*
                                     * subComponent
                                     */
                                    insertStatement.setBoolean(parameterIndex++, true);/*
                                     * alive
                                     */
                                    insertStatement.setShort(parameterIndex++, (short) -1);/*
                                     * thinAge
                                     */
                                    insertStatement.setBoolean(parameterIndex++, true);/*
                                     * aboveGround
                                     */
                                    insertStatement.setBoolean(parameterIndex++, false);/*
                                     * crown
                                     */
                                    insertStatement.setShort(parameterIndex++, (short) -1);/*
                                     * growthUnit
                                     */
                                    insertStatement.setShort(parameterIndex++, (short) -1);/*
                                     * ring
                                     */
                                    insertStatement.setNull(parameterIndex++, java.sql.Types.BOOLEAN);/*
                                     * Commercial wood
                                     */
                                    insertStatement.setShort(parameterIndex++, (short) -1);/*
                                     * diameterClassLowerBound
                                     */
                                    insertStatement.setShort(parameterIndex++, (short) -1);/*
                                     * diameterClassUpperBound
                                     */
                                    insertStatement.setByte(parameterIndex++, (byte) -1);/*
                                     * crownHeightRangeLowerBound
                                     */
                                    insertStatement.setByte(parameterIndex++, (byte) -1);/*
                                     * crownHeightRangeUpperrBound
                                     */
                                    insertStatement.setShort(parameterIndex++, (short) -1);/*
                                     * cohortAge
                                     */
                                    insertStatement.setShort(parameterIndex++, (short) -1);/*
                                     * soilDepthRangeLowerBound
                                     */
                                    insertStatement.setShort(parameterIndex++, (short) -1);/*
                                     * soilDepthRangeUpperBound
                                     */
                                    insertStatement.setDouble(parameterIndex++, rs.getDouble("INFERED_B"));         /*
                                     * Biomass
                                     */
                                    for (Iterator it = nutrients.iterator(); it.hasNext();) {
                                        String fieldName = "INFERED_" + ((String) it.next()).toUpperCase();
                                        insertStatement.setDouble(parameterIndex++, rs.getDouble(fieldName));/*
                                         * nutriment
                                         */
                                    }
                                    insertStatement.addBatch();
//                                    if (VERBOSE_SQL) {
//                                        System.out.println(insertStatement.toString());
//                                    }
                                }
                            }
                        }
                    }
                }
                /*
                 * update db
                 */
                int[] insertedLines = insertStatement.executeBatch();
                if (verbose) {
                    int totalInsertedLines = 0;
                    for (int i = 0; i < insertedLines.length; i++) {
                        totalInsertedLines += insertedLines[i];
                    }
                    System.out.println(totalInsertedLines + " interpolated lines inserted");
                }
                result = true;
            }
            catch (Exception e) {
                Log.println(Log.ERROR, methodName, e.getMessage(), e);
            }
            finally {
                if (statement != null) {
                    try {
                        statement.close();
                    }
                    catch (SQLException e) {
                        Log.println(Log.ERROR, methodName, " Cannot close statement", e);
                    }
                }
            }
        }
        else {
            Log.println(Log.ERROR, methodName, "Cannot interpolate data for " + dbTablePrefix + currentInstance + " because db is not ready");
        }
        return result;

        /*
         * un truc a creuser pour trouver les ages manquants WITH AGES(A) AS (SELECT * FROM UNNEST(SEQUENCE_ARRAY(1, 105, 1))), T(T_ID,S_ID,T_A) AS (SELECT DISTINCT TREE_ID,COMPONENT_ID,TREE_AGE FROM
         * EPTUS0 WHERE COMPONENT='STEM' AND SUB_COMPONENT='WOOD') SELECT A,T_ID,S_ID,T_A FROM AGES LEFT JOIN T ON AGES.A=T.T_A
         *
         */
    }

    /**
     * *
     * compute ring values by
     *
     * @param ageFactor Indicate the scale of the tree age : 1 if age in year, 12 if age in months..
     * @return true if successful
     */
    private boolean computeRingValues(int ageFactor, Boolean alive, boolean replaceNonRingValue) {
        final String methodName = "BMMDB.computeRingValues()";
        Statement statement = null;
        boolean result = false;
        if (ready) {
            try {
                statement = connection.createStatement();
                /*
                 * delete previous ring values
                 */
                StringBuffer sql = new StringBuffer("DELETE FROM ").append(dbTablePrefix).append(currentInstance).append(" WHERE COMPONENT='STEM' AND SUB_COMPONENT='WOOD' AND NOT RING=-1");
                if (verboseSQL) {
                    System.out.println(sql.toString());
                }
                time = System.currentTimeMillis();
                statement.execute(sql.toString());
                if (verbose) {
                    time = System.currentTimeMillis() - time;
                    System.out.println("Query executed in " + time + " ms");
                }
                /*
                 * get list of last value for each tree/component/age
                 */
                sql = new StringBuffer("SELECT TREE_ID,COMPONENT_ID,TREE_AGE,STEP_NAME,TREE_AGE");
                if (ageFactor < 1) {
                    ageFactor = 1;
                }
                else if (ageFactor > 1) {
                    sql.append("/").append(ageFactor);
                }
                sql.append(" AS RING,ALIVE,GROWTH_UNIT,COMMERCIAL_WOOD,BIOMASS");
                for (Iterator<String> it = nutrients.iterator(); it.hasNext();) {
                    String nutName = it.next();
                    sql.append(",").append(nutName);
                }
                sql.append(" FROM ").append(dbTablePrefix).append(currentInstance).append(" WHERE COMPONENT='STEM' AND SUB_COMPONENT='WOOD'");
                if (alive != null) {
                    sql.append(" AND ALIVE=").append(alive);
                }
                sql.append(" ORDER BY TREE_ID,COMPONENT_ID,TREE_AGE,STEP_NAME");
                if (verboseSQL) {
                    System.out.println(sql.toString());
                }
                time = System.currentTimeMillis();
                ResultSet rs = statement.executeQuery(sql.toString());
                if (verbose) {
                    time = System.currentTimeMillis() - time;
                    System.out.println("Query executed in " + time + " ms");
                }
                /*
                 * iterate resultset and compute ring values
                 */
                if (rs != null) {
                    time = System.currentTimeMillis();
                    HashMap<String, ArrayList< EmergeStemData>> stemDataWRing = new HashMap<String, ArrayList<EmergeStemData>>();
                    HashMap<String, Double> providedNutrtimentsMasses, ringMassMaxValuesSum = null;
                    String stepName;
                    int treeId, currentTreeId = -1;
                    short stemId, treeAge, currentStemId = -2/*
                             * -1 is used when no component id
                             */, ring;
                    double biomass;
                    while (rs.next()) {
                        treeId = rs.getInt("TREE_ID");
                        stemId = rs.getShort("COMPONENT_ID");
                        ring = rs.getShort("RING");
                        treeAge = rs.getShort("TREE_AGE");
                        stepName = rs.getString("STEP_NAME");
                        biomass = rs.getDouble("BIOMASS");
                        providedNutrtimentsMasses = new HashMap<String, Double>();
                        for (Iterator<String> it = nutrients.iterator(); it.hasNext();) {
                            String nutName = it.next();
                            providedNutrtimentsMasses.put(nutName, rs.getDouble(nutName));
                        }
                        /*
                         * initialise max values collection at the beginning of the loop and for each tree/stem change
                         */
                        if ((currentTreeId == -1 || currentStemId == -2) || (currentTreeId != treeId || currentStemId != stemId)) {
                            ringMassMaxValuesSum = new HashMap<String, Double>();
                            currentTreeId = treeId;
                            currentStemId = stemId;
                            ringMassMaxValuesSum.put("BIOMASS", 0d);
                            for (Iterator<String> it = nutrients.iterator(); it.hasNext();) {
                                ringMassMaxValuesSum.put(((String) it.next()), 0d);
                            }
                        }
                        /*
                         * substract last prev values from the give results
                         */
                        biomass -= ringMassMaxValuesSum.get("BIOMASS");
                        for (Iterator<String> it = nutrients.iterator(); it.hasNext();) {
                            String nutName = it.next();
                            providedNutrtimentsMasses.put(nutName, providedNutrtimentsMasses.get(nutName) - ringMassMaxValuesSum.get(nutName));
                        }
                        /*
                         * create entry if not exists
                         */
                        if (!stemDataWRing.containsKey(stepName)) {
                            stemDataWRing.put(stepName, new ArrayList<EmergeStemData>());
                        }

                        stemDataWRing.get(stepName).add(new EmergeStemData(treeAge, treeId, stemId, EmergeTreeSubComponent.WOOD, rs.getBoolean("ALIVE"), null,
                                biomass, providedNutrtimentsMasses, rs.getShort("GROWTH_UNIT"), ring, rs.getBoolean("COMMERCIAL_WOOD")));

                        /*
                         * sum up lat value for each ring
                         */
                        if (treeAge == ((ring + 1) * ageFactor) - 1) {
                            ringMassMaxValuesSum.put("BIOMASS", ringMassMaxValuesSum.get("BIOMASS") + biomass);
                            for (Iterator<String> it = nutrients.iterator(); it.hasNext();) {
                                String nutName = it.next();
                                ringMassMaxValuesSum.put(nutName, ringMassMaxValuesSum.get(nutName) + providedNutrtimentsMasses.get(nutName));
                            }
                        }
                    }
                    for (Iterator<String> it = stemDataWRing.keySet().iterator(); it.hasNext();) {
                        String sName = it.next();
                        add((short) -1, sName, stemDataWRing.get(sName), true, false);
                    }

                }
                if (verbose) {
                    time = System.currentTimeMillis() - time;
                    System.out.println("Ring data computed in " + time + " ms");
                }

                if (replaceNonRingValue) {
                    sql = new StringBuffer("DELETE FROM ").append(dbTablePrefix).append(currentInstance).append(" WHERE COMPONENT='STEM' AND SUB_COMPONENT='WOOD' AND RING=-1");
                    if (verboseSQL) {
                        System.out.println(sql.toString());
                    }
                    time = System.currentTimeMillis();
                    statement.execute(sql.toString());
                    if (verbose) {
                        time = System.currentTimeMillis() - time;
                        System.out.println("Query executed in " + time + " ms");
                    }
                }
            }
            catch (Exception e) {
                Log.println(Log.ERROR, methodName, e.getMessage(), e);
            }
            finally {
                if (statement != null) {
                    try {
                        statement.close();
                    }
                    catch (SQLException e) {
                        Log.println(Log.ERROR, methodName, " Cannot close statement", e);
                    }
                }
            }
        }
        else {
            Log.println(Log.ERROR, methodName, "Cannot compute ring data for " + dbTablePrefix + currentInstance + " because db is not ready");
        }
        return result;
    }

    /**
     * *
     * Interpolate missing stem wood values from age=0 then compute ring data from raw stem data Warning : previous existing ring values will be deleted.
     *
     * @param ageFactor Indicate the scale of the tree age : 1 if age in year, 12 if age in months..
     * @return true if successful
     */
    public boolean computeRing(int ageFactor, Boolean alive, boolean removeNonRingLines) {
        update();
        interpolateMissingStemWoodValues();
        computeRingValues(ageFactor, alive, removeNonRingLines);
        return false;
    }

    /**
     * Iterate all steps to get trees to mark thinned in db. In the db, thinned status is associated with thin age. All occurence of this tree in the scenario in wich it was thinned will be set to
     * thin age.
     */
    public void updateThinnedTrees() {
        for (Iterator it = ((GModel) model).getProject().getNodes().iterator(); it.hasNext();) {/*
             *
             */
            Step step = (Step) it.next();
            GScene scene = step.getScene();
            if (scene instanceof TreeList) {
                if (((TreeList) scene).getTrees("cut").size() > 0) {/*
                     * check if trees were really cut
                     */
                    /*
                     * propagate tree mark in emerge db
                     */
                    for (Iterator iCutTrees = ((TreeList) scene).getTrees("cut").iterator(); iCutTrees.hasNext();) {
                        Tree cutted = (Tree) iCutTrees.next();
                        setThin(cutted, step, true);
                    }
                }
            }

        }
    }

    /**
     * *
     * compute scenario letter
     *
     * @param step Step
     * @return step scenarie. Ex : "12a" => 'a'
     */
    private String getScenario(Step step) {
        String scen = step.getName();
        scen = scen.substring(scen.length() - 1, scen.length()).toUpperCase();
        return scen;
    }

    /**
     * *
     * get user tables in the db. Ie all EmergeDB instances tables
     *
     * @return List of table names
     */
    public ArrayList<String> getUserTables() {
        final String methodName = "BMMDB.getUserTables()";
        ResultSet tables = null;
        ArrayList<String> result = null;
        try {
            if (ready) {
                tables = connection.getMetaData().getTables(connection.getCatalog(), null, "%", null);
                if (tables != null) {
                    result = new ArrayList<String>();
                    while (tables.next()) {
                        if (tables.getString("TABLE_TYPE") == "TABLE") {
                            result.add(tables.getString("TABLE_NAME"));
                        }
                    }
                }
            }
            else {
                Log.println(Log.ERROR, methodName, "Cannot run query because db is not ready");
            }
        }
        catch (SQLException e) {
            Log.println(Log.ERROR, methodName, e.getMessage(), e);
        }
        finally {
            if (tables != null) {
                try {
                    tables.close();
                }
                catch (SQLException e) {
                    Log.println(Log.ERROR, methodName, " Cannot close statement", e);
                }
            }
        }
        return result;
    }

    /**
     * Exectute a Data Query Langage SQL query on the instance table.
     *
     * @param sql The SQL query string to execute
     * @return ResultSet containing query result
     */
    public ResultSet executeDQLQuery(String sql) {
        final String methodName = "BMMDB.runDQLQuery()";
        Statement statement = null;
        ResultSet result = null;
        try {
            if (ready) {
                update();/*
                 * update db if necessary
                 */
                statement = connection.createStatement();
                result = statement.executeQuery(sql);
            }
            else {
                Log.println(Log.ERROR, methodName, "Cannot run query because db is not ready");
            }
        }
        catch (SQLException e) {
            Log.println(Log.ERROR, methodName, e.getMessage(), e);
        }
        finally {
            if (statement != null) {
                try {
                    statement.close();
                }
                catch (SQLException e) {
                    Log.println(Log.ERROR, methodName, " Cannot close statement", e);
                }
            }
        }
        return result;
    }

    /**
     * *
     * Get a collection of available component/sub component/dimensions in current table
     *
     * @return
     */
    public HashMap<Object, Object> getAvailableItems() {
        final String methodName = "BMMDB.getAvailableItems()";

        HashMap<Object, Object> result = null;
        Statement statement = null;
        ResultSet rs;
        StringBuilder sql;
        try {
            if (ready) {
                result = new HashMap<Object, Object>();
                /*
                 * update db if necessary
                 */
                update();
                statement = connection.createStatement();
                /*
                 * get masses set
                 */
                ArrayList<String> masses = (ArrayList<String>) nutrients.clone();
                masses.add(0, "BIOMASS");
                result.put("MASS", masses);
                /*
                 * get components set
                 */
                sql = new StringBuilder(" SELECT DISTINCT COMPONENT FROM ");
                sql.append(getTableName());
                sql.append(" ORDER BY COMPONENT ");
                if (verboseSQL) {
                    System.out.println(sql.toString());
                }
                rs = statement.executeQuery(sql.toString());
                if (rs != null) {
                    ArrayList<String> components = new ArrayList<String>();
                    while (rs.next()) {
                        components.add(rs.getString("COMPONENT"));
                    }
                    result.put("COMPONENT", components);
                }
                /*
                 * get sub components set for non root
                 */
                sql = new StringBuilder(" SELECT DISTINCT SUB_COMPONENT FROM ");
                sql.append(getTableName());
                sql.append(" WHERE COMPONENT IN ('TREE','STEM', 'BRANCH', 'STUMP' ) ORDER BY SUB_COMPONENT ");
                if (verboseSQL) {
                    System.out.println(sql.toString());
                }
                rs = statement.executeQuery(sql.toString());
                if (rs != null) {
                    ArrayList<String> subComponents = new ArrayList<String>();
                    while (rs.next()) {
                        subComponents.add(rs.getString("SUB_COMPONENT"));
                    }
                    result.put("SUB_COMPONENT", subComponents);
                }
                /*
                 * get sub components set for root
                 */
                sql = new StringBuilder(" SELECT DISTINCT SUB_COMPONENT FROM ");
                sql.append(getTableName());
                sql.append(" WHERE COMPONENT='ROOT' ORDER BY SUB_COMPONENT ");
                if (verboseSQL) {
                    System.out.println(sql.toString());
                }
                rs = statement.executeQuery(sql.toString());
                if (rs != null) {
                    ArrayList<String> subComponents = new ArrayList<String>();
                    while (rs.next()) {
                        subComponents.add(rs.getString("SUB_COMPONENT"));
                    }
                    result.put("ROOT_SUB_COMPONENT", subComponents);
                }
                /*
                 * get growth unit set
                 */
                sql = new StringBuilder(" SELECT DISTINCT GROWTH_UNIT FROM ");
                sql.append(getTableName());
                sql.append(" ORDER BY GROWTH_UNIT ");
                if (verboseSQL) {
                    System.out.println(sql.toString());
                }
                rs = statement.executeQuery(sql.toString());
                if (rs != null) {
                    ArrayList<String> growthUnit = new ArrayList<String>();
                    while (rs.next()) {
                        growthUnit.add(rs.getString("GROWTH_UNIT"));
                    }
                    result.put("GROWTH_UNIT", growthUnit);
                }
                /*
                 * get ring set
                 */
                sql = new StringBuilder(" SELECT DISTINCT RING FROM ");
                sql.append(getTableName());
                sql.append(" ORDER BY RING ");
                if (verboseSQL) {
                    System.out.println(sql.toString());
                }
                rs = statement.executeQuery(sql.toString());
                if (rs != null) {
                    ArrayList<String> ring = new ArrayList<String>();
                    while (rs.next()) {
                        ring.add(rs.getString("RING"));
                    }
                    result.put("RING", ring);
                }
                /*
                 * get commercialWood set
                 */
                sql = new StringBuilder(" SELECT DISTINCT COMMERCIAL_WOOD FROM ");
                sql.append(getTableName());
                sql.append(" ORDER BY COMMERCIAL_WOOD ");
                if (verboseSQL) {
                    System.out.println(sql.toString());
                }
                rs = statement.executeQuery(sql.toString());
                if (rs != null) {
                    String value;
                    ArrayList<String> commercialWood = new ArrayList<String>();
                    while (rs.next()) {
                        value = rs.getString("COMMERCIAL_WOOD");
                        if (!rs.wasNull()) {
                            commercialWood.add(value.toUpperCase());
                        }
                    }
                    result.put("COMMERCIAL_WOOD", commercialWood);
                }
//                /*
//                 * get aboveGround set
//                 */
//                sql = new StringBuilder(" SELECT DISTINCT ABOVE_GROUND FROM ");
//                sql.append(getTableName());
//                sql.append(" ORDER BY ABOVE_GROUND ");
//                if (verboseSQL) {
//                    System.out.println(sql.toString());
//                }
//                rs = statement.executeQuery(sql.toString());
//                if (rs != null) {
//                    ArrayList<String> aboveGround = new ArrayList<String>();
//                    while (rs.next()) {
//                        aboveGround.add(rs.getString("ABOVE_GROUND"));
//                    }
//                    result.put("ABOVE_GROUND", aboveGround);
//                }
//                /*
//                 * get crown set
//                 */
//                sql = new StringBuilder(" SELECT DISTINCT CROWN FROM ");
//                sql.append(getTableName());
//                sql.append(" ORDER BY CROWN ");
//                if (verboseSQL) {
//                    System.out.println(sql.toString());
//                }
//                rs = statement.executeQuery(sql.toString());
//                if (rs != null) {
//                    ArrayList<String> crown = new ArrayList<String>();
//                    while (rs.next()) {
//                        crown.add(rs.getString("CROWN"));
//                    }
//                    result.put("CROWN", crown);
//                }
                /*
                 * get branch diameter class set
                 */
                sql = new StringBuilder(" SELECT DISTINCT DIAMETER_CLASS_LOWER_BOUND,DIAMETER_CLASS_UPPER_BOUND FROM ");
                sql.append(getTableName());
                sql.append(" WHERE COMPONENT='BRANCH' ORDER BY DIAMETER_CLASS_LOWER_BOUND,DIAMETER_CLASS_UPPER_BOUND ");
                if (verboseSQL) {
                    System.out.println(sql.toString());
                }
                rs = statement.executeQuery(sql.toString());
                if (rs != null) {
                    ArrayList<String> diameter = new ArrayList<String>();
                    while (rs.next()) {
                        diameter.add(rs.getString("DIAMETER_CLASS_LOWER_BOUND"));
                        diameter.add(rs.getString("DIAMETER_CLASS_UPPER_BOUND"));
                    }
                    result.put("BRANCH_DIAMETER_CLASS", diameter);
                }
                /*
                 * get root diameter class set
                 */
                sql = new StringBuilder(" SELECT DISTINCT DIAMETER_CLASS_LOWER_BOUND,DIAMETER_CLASS_UPPER_BOUND FROM ");
                sql.append(getTableName());
                sql.append(" WHERE COMPONENT='ROOT' ORDER BY DIAMETER_CLASS_LOWER_BOUND,DIAMETER_CLASS_UPPER_BOUND ");
                if (verboseSQL) {
                    System.out.println(sql.toString());
                }
                rs = statement.executeQuery(sql.toString());
                if (rs != null) {
                    ArrayList<String> diameter = new ArrayList<String>();
                    while (rs.next()) {
                        diameter.add(rs.getString("DIAMETER_CLASS_LOWER_BOUND"));
                        diameter.add(rs.getString("DIAMETER_CLASS_UPPER_BOUND"));
                    }
                    result.put("ROOT_DIAMETER_CLASS", diameter);
                }
                /*
                 * get heigh range set
                 */
                sql = new StringBuilder(" SELECT DISTINCT CROWN_HEIGHT_RANGE_LOWER_BOUND,CROWN_HEIGHT_RANGE_UPPER_BOUND FROM ");
                sql.append(getTableName());
                sql.append(" ORDER BY CROWN_HEIGHT_RANGE_LOWER_BOUND,CROWN_HEIGHT_RANGE_UPPER_BOUND ");
                if (verboseSQL) {
                    System.out.println(sql.toString());
                }
                rs = statement.executeQuery(sql.toString());
                if (rs != null) {
                    ArrayList<String> heightRange = new ArrayList<String>();
                    while (rs.next()) {
                        heightRange.add(rs.getString("CROWN_HEIGHT_RANGE_LOWER_BOUND"));
                        heightRange.add(rs.getString("CROWN_HEIGHT_RANGE_UPPER_BOUND"));
                    }
                    result.put("CROWN_HEIGHT", heightRange);
                }
                /*
                 * get cochort set
                 */
                sql = new StringBuilder(" SELECT DISTINCT COHORT_AGE FROM ");
                sql.append(getTableName());
                sql.append(" ORDER BY COHORT_AGE ");
                if (verboseSQL) {
                    System.out.println(sql.toString());
                }
                rs = statement.executeQuery(sql.toString());
                if (rs != null) {
                    ArrayList<String> cohort = new ArrayList<String>();
                    while (rs.next()) {
                        cohort.add(rs.getString("COHORT_AGE"));
                    }
                    result.put("COHORT_AGE", cohort);
                }
                /*
                 * get depth range set
                 */
                sql = new StringBuilder(" SELECT DISTINCT SOIL_DEPTH_LOWER_BOUND,SOIL_DEPTH_UPPER_BOUND FROM ");
                sql.append(getTableName());
                sql.append(" ORDER BY SOIL_DEPTH_LOWER_BOUND,SOIL_DEPTH_UPPER_BOUND ");
                if (verboseSQL) {
                    System.out.println(sql.toString());
                }
                rs = statement.executeQuery(sql.toString());
                if (rs != null) {
                    ArrayList<String> soilDepth = new ArrayList<String>();
                    while (rs.next()) {
                        soilDepth.add(rs.getString("SOIL_DEPTH_LOWER_BOUND"));
                        soilDepth.add(rs.getString("SOIL_DEPTH_UPPER_BOUND"));
                    }
                    result.put("SOIL_DEPTH", soilDepth);
                }
            }
            else {
                Log.println(Log.ERROR, methodName, "Cannot run query because db is not ready");
            }
        }
        catch (SQLException e) {
            Log.println(Log.ERROR, methodName, e.getMessage(), e);
        }
        finally {
            if (statement != null) {
                try {
                    statement.close();
                }
                catch (SQLException e) {
                    Log.println(Log.ERROR, methodName, " Cannot close statement", e);
                }
            }
        }
        return result;
    }

    /**
     * *
     * Provide beginning of all SQL queries for aggregate methods
     *
     *
     * @param element "BIOMASS" to get biomass, "[nutrient name]" to get nutrient.
     * @param stepName Name of the step, ex "12a"
     * @param trees Collection of tree to take into account. Set to null or to empty collection to take every trees of the step/stand
     * @param alive Take into account only alive/dead material. Set to null to ignore.
     * @return Beginning of SQL statement
     */
    private StringBuffer getCommonSQL(String element, String stepName, Boolean alive, Collection<? extends Tree> trees) {
        StringBuffer sql = null;
        ResultSet rs;
        final String methodName = "BMMDB.getCommonSQL()";
        try {
            if (ready) {
                update();/*
                 * update db if necessary
                 */
                if ("NUTRIENTS".equals(element.toUpperCase())) {
                    sql = new StringBuffer("SELECT ");
                    for (Iterator<String> it = nutrients.iterator(); it.hasNext();) {
                        sql.append("SUM(").append(it.next()).append(")");
                        if (it.hasNext()) {
                            sql.append("+");
                        }
                    }
                    sql.append(" AS NUTRIENTS");
                }
                else {
                    sql = new StringBuffer("SELECT SUM(").append(element.toUpperCase()).append(")");
                }
                sql.append(" FROM ").append(dbTablePrefix).append(currentInstance);
                if (stepName != null) {
                    sql.append(" WHERE STEP_NAME='").append(stepName).append("'");
                }
                if (alive != null) {
                    sql.append(" AND ALIVE=").append(alive);
                }
                if (trees != null) {
                    if (!trees.isEmpty()) {/*
                         * check nb of trees against db
                         */
                        if (nbTreesInStep != null) {
                            nbTreesInStep.clearParameters();
                            nbTreesInStep.setString(1, stepName);
                            nbTreesInStep.execute();
                            rs = nbTreesInStep.getResultSet();
                            if (rs != null && rs.next()) {
                                if (rs.getInt(1) != trees.size()) {/*
                                     * trees provided collection is not every tree in the step => add IN clause
                                     */
                                    sql.append(" AND TREE_ID IN (");
                                    for (Iterator it = trees.iterator(); it.hasNext();) {
                                        Integer id = (Integer) ((Tree) it.next()).getId();
                                        sql.append(id);
                                        if (it.hasNext()) {
                                            sql.append(",");
                                        }
                                    }
                                    sql.append(")");
                                }
                            }
                        }
                    }
                }
            }
            else {
                Log.println(Log.ERROR, methodName, "Cannot get data for table " + dbTablePrefix + currentInstance + " because db is not ready");
            }
        }
        catch (SQLException e) {
            Log.println(Log.ERROR, methodName, e.getMessage(), e);
        }
        return sql;
    }

    /**
     * *
     * Get biomass or nutrient mass of whole trees on a given step.
     *
     * @param element "BIOMASS" to get biomass, "[nutrient name]" to get nutrient.
     * @param subComponent "BARK" for bark, "WOOD" for wood, null to ignore criteria
     * @param stepName Name of the step, ex "12a"
     * @param trees Collection of tree to take into account. Set to null or to empty collection to take every trees of the step/stand
     * @param alive Take into account only alive/dead material. Set to null to ignore.
     * @return Element mass for whole tree
     */
    public double getTreeElement(String element, EmergeTreeSubComponent subComponent, String stepName, Boolean commercialWood, Collection<? extends Tree> trees, Boolean alive) {
        final String methodName = "BMMDB.getTreeElement()";
        Statement statement = null;
        double result = 0d;
        try {
            if (ready) {
                update();/*
                 * update db if necessary
                 */
                statement = connection.createStatement();
                StringBuffer sql = getCommonSQL(element, stepName, alive, trees);
                sql.append(" AND COMPONENT='TREE' ");
                if (subComponent != null) {
                    sql.append(" AND SUB_COMPONENT='").append(subComponent).append("'");
                }
                if (commercialWood != null) {
                    if (commercialWood) {
                        sql.append(" AND COMMERCIAL_WOOD=TRUE)");
                    }
                    else {
                        sql.append(" AND COMMERCIAL_WOOD=FALSE)");
                    }
                }
                if (verboseSQL) {
                    System.out.println(sql.toString());
                }
                time = System.currentTimeMillis();
                ResultSet rs = statement.executeQuery(sql.toString());
                if (verbose) {
                    System.out.println("Query executed in " + ((System.currentTimeMillis() - time)) + " ms");
                }
                if (rs != null) {
                    if (rs.next()) {
                        result += rs.getDouble(1);
                    }
                }
            }
            else {
                Log.println(Log.ERROR, methodName, "Cannot get info for table " + dbTablePrefix + currentInstance + " because db is not ready");
            }
        }
        catch (SQLException e) {
            Log.println(Log.ERROR, methodName, e.getMessage(), e);
        }
        finally {
            if (statement != null) {
                try {
                    statement.close();
                }
                catch (SQLException e) {
                    Log.println(Log.ERROR, methodName, " Cannot close statement", e);
                }
            }
        }
        return result;
    }

    /**
     * *
     * Get biomass or nutrient mass of stem compartments on a given step.
     *
     * @param element "BIOMASS" to get biomass, "[nutrient name]" to get nutrient.
     * @param subComponent "BARK" for bark, "WOOD" for wood, null to ignore criteria
     * @param growthUnit Growth unit. Generally equal to tree age in year
     * @param ring Ring. Generally one ring per year
     * @param stepName Name of the step, ex "12a"
     * @param trees Collection of tree to take into account. Set to null or to empty collection to take every trees of the step/stand
     * @param alive Take into account only alive/dead material. Set to null to ignore.
     * @return Element mass for stem component
     */
    public double getStemElement(String element, EmergeTreeSubComponent subComponent, String stepName, Short growthUnit, Short ring, Boolean commercialWood, Collection<? extends Tree> trees, Boolean alive) {
        final String methodName = "BMMDB.getStemElement()";
        Statement statement = null;
        double result = 0d;
        try {
            if (ready) {
                update();/*
                 * update db if necessary
                 */
                statement = connection.createStatement();
                StringBuffer sql = getCommonSQL(element, stepName, alive, trees);
                sql.append(" AND COMPONENT='STEM' ");
                if (subComponent != null) {
                    sql.append(" AND SUB_COMPONENT='").append(subComponent).append("'");
                }
                if (growthUnit != null) {
                    sql.append(" AND GROWTH_UNIT=").append(growthUnit);
                }
                else {
                    sql.append(" AND GROWTH_UNIT=-1");
                }
                if (ring != null) {
                    sql.append(" AND RING=").append(ring);
                }
                else {
                    sql.append(" AND RING=-1");//AND RING IS NULL if nullable column
                }
                if (commercialWood != null) {
                    if (commercialWood) {
                        sql.append(" AND COMMERCIAL_WOOD=TRUE)");
                    }
                    else {
                        sql.append(" AND COMMERCIAL_WOOD=FALSE)");
                    }
                }

                if (verboseSQL) {
                    System.out.println(sql.toString());
                }
                time = System.currentTimeMillis();
                ResultSet rs = statement.executeQuery(sql.toString());
                if (verbose) {
                    System.out.println("Query executed in " + ((System.currentTimeMillis() - time)) + " ms");
                }
                if (rs != null) {
                    if (rs.next()) {
                        result += rs.getDouble(1);
                    }
                }
            }
            else {
                Log.println(Log.ERROR, methodName, "Cannot get info for table " + dbTablePrefix + currentInstance + " because db is not ready");
            }
        }
        catch (SQLException e) {
            Log.println(Log.ERROR, methodName, e.getMessage(), e);
        }
        finally {
            if (statement != null) {
                try {
                    statement.close();
                }
                catch (SQLException e) {
                    Log.println(Log.ERROR, methodName, " Cannot close statement", e);
                }
            }
        }
        return result;
    }

    /**
     * *
     * Get biomass or nutrient mass of branch compartments on a given step.
     *
     * @param element "BIOMASS" to get biomass, "[nutrient name]" to get nutrient.
     * @param subComponent "BARK" for bark, "WOOD" for wood, null to ignore criteria
     * @param diameterClassLowerBound Lower bound for root diameter class
     * @param diameterClassUpperBound Upper bound for root diameter class
     * @param crownHeigthRangeLowerBound Lower bound for crown height section (%)
     * @param crownHeigthRangeUpperBound Upper bound for crown height section (%)
     * @param stepName Name of the step, ex "12a"
     * @param trees Collection of tree to take into account. Set to null or to empty collection to take every trees of the step/stand
     * @param alive Take into account only alive/dead material. Set to null to ignore.
     * @return Element mass for branch component
     */
    public double getBranchElement(String element, EmergeTreeSubComponent subComponent, String stepName, Short diameterClassLowerBound, Short diameterClassUpperBound, Byte crownHeigthRangeLowerBound,
            Byte crownHeigthRangeUpperBound, Boolean commercialWood, Collection<? extends Tree> trees, Boolean alive) {
        final String methodName = "BMMDB.getBranchElement()";
        Statement statement = null;
        double result = 0d;
        try {
            if (ready) {
                statement = connection.createStatement();
                StringBuffer sql = getCommonSQL(element, stepName, alive, trees);
                sql.append(" AND COMPONENT='BRANCH' ");
                if (subComponent != null) {
                    sql.append(" AND SUB_COMPONENT='").append(subComponent).append("'");
                }
                if (diameterClassLowerBound != null) {
                    sql.append(" AND DIAMETER_CLASS_LOWER_BOUND=").append(diameterClassLowerBound);
                }
                else {
                    sql.append(" AND DIAMETER_CLASS_LOWER_BOUND=-1");
                }
                if (diameterClassUpperBound != null) {
                    sql.append(" AND DIAMETER_CLASS_UPPER_BOUND=").append(diameterClassUpperBound);
                }
                else {
                    sql.append(" AND DIAMETER_CLASS_UPPER_BOUND=-1");
                }
                if (crownHeigthRangeLowerBound != null) {
                    sql.append(" AND CROWN_HEIGHT_RANGE_LOWER_BOUND=").append(crownHeigthRangeLowerBound);
                }
                else {
                    sql.append(" AND CROWN_HEIGHT_RANGE_LOWER_BOUND=-1");
                }
                if (crownHeigthRangeUpperBound != null) {
                    sql.append(" AND CROWN_HEIGHT_RANGE_UPPER_BOUND=").append(crownHeigthRangeUpperBound);
                }
                else {
                    sql.append(" AND CROWN_HEIGHT_RANGE_UPPER_BOUND=-1");
                }
                if (commercialWood != null) {
                    if (commercialWood) {
                        sql.append(" AND COMMERCIAL_WOOD=TRUE)");
                    }
                    else {
                        sql.append(" AND COMMERCIAL_WOOD=FALSE)");
                    }
                }
                if (verboseSQL) {
                    System.out.println(sql.toString());
                }
                time = System.currentTimeMillis();
                ResultSet rs = statement.executeQuery(sql.toString());
                if (verbose) {
                    System.out.println("Query executed in " + ((System.currentTimeMillis() - time)) + " ms");
                }
                if (rs != null) {
                    if (rs.next()) {
                        result += rs.getDouble(1);
                    }
                }
            }
            else {
                Log.println(Log.ERROR, methodName, "Cannot get info for table " + dbTablePrefix + currentInstance + " because db is not ready");
            }
        }
        catch (SQLException e) {
            Log.println(Log.ERROR, methodName, e.getMessage(), e);
        }
        finally {
            if (statement != null) {
                try {
                    statement.close();
                }
                catch (SQLException e) {
                    Log.println(Log.ERROR, methodName, " Cannot close statement", e);
                }
            }
        }
        return result;
    }

    /**
     * *
     * Get biomass or nutrient mass of stump compartments on a given step.
     *
     * @param element "BIOMASS" to get biomass, "[nutrient name]" to get nutrient.
     * @param subComponent "BARK" for bark, "WOOD" for wood, null to ignore criteria
     * @param stepName Name of the step, ex "12a"
     * @param trees Collection of tree to take into account. Set to null or to empty collection to take every trees of the step/stand
     * @param alive Take into account only alive/dead material. Set to null to ignore.
     * @return Element mass for stump component
     */
    public double getStumpElement(String element, EmergeTreeSubComponent subComponent, String stepName, Collection<? extends Tree> trees, Boolean alive) {
        final String methodName = "BMMDB.getStumpElement()";
        Statement statement = null;
        double result = 0d;
        try {
            if (ready) {
                update();/*
                 * update db if necessary
                 */
                statement = connection.createStatement();
                StringBuffer sql = getCommonSQL(element, stepName, alive, trees);
                sql.append(" AND COMPONENT='STUMP' ");
                if (subComponent != null) {
                    sql.append(" AND SUB_COMPONENT='").append(subComponent).append("'");
                }
                if (verboseSQL) {
                    System.out.println(sql.toString());
                }
                time = System.currentTimeMillis();
                ResultSet rs = statement.executeQuery(sql.toString());
                if (verbose) {
                    System.out.println("Query executed in " + ((System.currentTimeMillis() - time)) + " ms");
                }
                if (rs != null) {
                    if (rs.next()) {
                        result += rs.getDouble(1);
                    }
                }
            }
            else {
                Log.println(Log.ERROR, methodName, "Cannot get info for table " + dbTablePrefix + currentInstance + " because db is not ready");
            }
        }
        catch (Exception e) {
            Log.println(Log.ERROR, methodName, e.getMessage(), e);
        }
        finally {
            if (statement != null) {
                try {
                    statement.close();
                }
                catch (SQLException e) {
                    Log.println(Log.ERROR, methodName, " Cannot close statement", e);
                }
            }
        }
        return result;
    }

    /**
     * *
     * Get biomass or nutrient mass of leaf compartments on a given step.
     *
     * @param element "BIOMASS" to get biomass, "[nutrient name]" to get nutrient.
     * @param cohortAge Age of leaf cohort
     * @param crownHeigthRangeLowerBound Lower bound for crown height section (%)
     * @param crownHeigthRangeUpperBound Upper bound for crown height section (%)
     * @param stepName Name of the step, ex "12a"
     * @param trees Collection of tree to take into account. Set to null or to empty collection to take every trees of the step/stand
     * @param alive Take into account only alive/dead material. Set to null to ignore.
     * @return Element mass for leaf component
     */
    public double getLeafElement(String element, Short cohortAge, Byte crownHeigthRangeLowerBound, Byte crownHeigthRangeUpperBound, String stepName, Collection<? extends Tree> trees, Boolean alive) {
        final String methodName = "BMMDB.getLeafElement()";
        Statement statement = null;
        double result = 0d;
        try {
            if (ready) {
                update();/*
                 * update db if necessary
                 */
                statement = connection.createStatement();
                StringBuffer sql = getCommonSQL(element, stepName, alive, trees);
                sql.append(" AND COMPONENT='LEAF' ");
                if (cohortAge != null) {
                    sql.append(" AND COHORT_AGE=").append(cohortAge);
                }
                if (crownHeigthRangeLowerBound != null) {
                    sql.append(" AND CROWN_HEIGHT_RANGE_LOWER_BOUND=").append(crownHeigthRangeLowerBound);
                }
                else {
                    sql.append(" AND CROWN_HEIGHT_RANGE_LOWER_BOUND=-1");
                }
                if (crownHeigthRangeUpperBound != null) {
                    sql.append(" AND CROWN_HEIGHT_RANGE_UPPER_BOUND=").append(crownHeigthRangeUpperBound);
                }
                else {
                    sql.append(" AND CROWN_HEIGHT_RANGE_UPPER_BOUND=-1");
                }
                if (verboseSQL) {
                    System.out.println(sql.toString());
                }
                time = System.currentTimeMillis();
                ResultSet rs = statement.executeQuery(sql.toString());
                if (verbose) {
                    System.out.println("Query executed in " + ((System.currentTimeMillis() - time)) + " ms");
                }
                if (rs != null) {
                    if (rs.next()) {
                        result += rs.getDouble(1);
                    }
                }
            }
            else {
                Log.println(Log.ERROR, methodName, "Cannot get info for table " + dbTablePrefix + currentInstance + " because db is not ready");
            }
        }
        catch (Exception e) {
            Log.println(Log.ERROR, methodName, e.getMessage(), e);
        }
        finally {
            if (statement != null) {
                try {
                    statement.close();
                }
                catch (SQLException e) {
                    Log.println(Log.ERROR, methodName, " Cannot close statement", e);
                }
            }
        }
        return result;
    }

    /**
     * *
     * Get biomass or nutrient mass of root compartments on a given step.
     *
     * @param element "BIOMASS" to get biomass, "[nutrient name]" to get nutrient.
     * @param subComponent "LITTER" for litter roots, "SOIL" for soil roots, null to ignore this criteria.
     * @param diameterClassLowerBound Lower bound for root diameter class
     * @param diameterClassUpperBound Upper bound for root diameter class
     * @param soilDepthRangeLowerBound Lower bound for depth range of root in the soil
     * @param soilDepthRangeUpperBound Upper bound for depth range of root in the soil
     * @param stepName Name of the step, ex "12a"
     * @param trees Collection of tree to take into account. Set to null or to empty collection to take every trees of the step/stand
     * @param alive Take into account only alive/dead material. Set to null to ignore.
     * @return Element mass for root component
     */
    public double getRootElement(String element, EmergeTreeSubComponent subComponent, String stepName, Short diameterClassLowerBound, Short diameterClassUpperBound, Short soilDepthRangeLowerBound,
            Short soilDepthRangeUpperBound, Collection<? extends Tree> trees, Boolean alive) {
        final String methodName = "BMMDB.getRootElement()";
        Statement statement = null;
        double result = 0d;
        try {
            if (ready) {
                update();/*
                 * update db if necessary
                 */
                statement = connection.createStatement();
                StringBuffer sql = getCommonSQL(element, stepName, alive, trees);
                sql.append(" AND COMPONENT='ROOT' ");
                if (subComponent != null) {
                    sql.append(" AND SUB_COMPONENT='").append(subComponent).append("'");
                }
                if (diameterClassLowerBound != null) {
                    sql.append(" AND DIAMETER_CLASS_LOWER_BOUND=").append(diameterClassLowerBound);
                }
                else {
                    sql.append(" AND DIAMETER_CLASS_LOWER_BOUND=-1");
                }
                if (diameterClassUpperBound != null) {
                    sql.append(" AND DIAMETER_CLASS_UPPER_BOUND=").append(diameterClassUpperBound);
                }
                else {
                    sql.append(" AND DIAMETER_CLASS_UPPER_BOUND=-1");
                }
                if (soilDepthRangeLowerBound != null) {
                    sql.append(" AND SOIL_DEPTH_LOWER_BOUND=").append(soilDepthRangeLowerBound);
                }
                else {
                    sql.append(" AND SOIL_DEPTH_LOWER_BOUND=-1");
                }
                if (soilDepthRangeUpperBound != null) {
                    sql.append(" AND SOIL_DEPTH_UPPER_BOUND=").append(soilDepthRangeUpperBound);
                }
                else {
                    sql.append(" AND SOIL_DEPTH_UPPER_BOUND=-1");
                }
                if (verboseSQL) {
                    System.out.println(sql.toString());
                }
                time = System.currentTimeMillis();
                ResultSet rs = statement.executeQuery(sql.toString());
                if (verbose) {
                    System.out.println("Query executed in " + ((System.currentTimeMillis() - time)) + " ms");
                }
                if (rs != null) {
                    if (rs.next()) {
                        result += rs.getDouble(1);
                    }
                }
            }
            else {
                Log.println(Log.ERROR, methodName, "Cannot get info for table " + dbTablePrefix + currentInstance + " because db is not ready");
            }
        }
        catch (Exception e) {
            Log.println(Log.ERROR, methodName, e.getMessage(), e);
        }
        finally {
            if (statement != null) {
                try {
                    statement.close();
                }
                catch (SQLException e) {
                    Log.println(Log.ERROR, methodName, " Cannot close statement", e);
                }
            }
        }
        return result;
    }

    /**
     * *
     * Get biomass or nutrient mass of fruit compartments on a given step.
     *
     * @param element "BIOMASS" to get biomass, "[nutrient name]" to get nutrient.
     * @param stepName Name of the step, ex "12a"
     * @param trees Collection of tree to take into account. Set to null or to empty collection to take every trees of the step/stand
     * @param alive Take into account only alive/dead material. Set to null to ignore.
     * @return Element mass for root component
     */
    public double getFruitElement(String element, String stepName, Collection<? extends Tree> trees, Boolean alive) {
        final String methodName = "BMMDB.getFruitElement()";
        Statement statement = null;
        double result = 0d;
        try {
            if (ready) {
                update();/*
                 * update db if necessary
                 */
                statement = connection.createStatement();
                StringBuffer sql = getCommonSQL(element, stepName, alive, trees);
                sql.append(" AND COMPONENT='FRUIT' ");
                if (verboseSQL) {
                    System.out.println(sql.toString());
                }
                time = System.currentTimeMillis();
                ResultSet rs = statement.executeQuery(sql.toString());
                if (verbose) {
                    System.out.println("Query executed in " + ((System.currentTimeMillis() - time)) + " ms");
                }
                if (rs != null) {
                    if (rs.next()) {
                        result += rs.getDouble(1);
                    }
                }
            }
            else {
                Log.println(Log.ERROR, methodName, "Cannot get info for table " + dbTablePrefix + currentInstance + " because db is not ready");
            }
        }
        catch (Exception e) {
            Log.println(Log.ERROR, methodName, e.getMessage(), e);
        }
        finally {
            if (statement != null) {
                try {
                    statement.close();
                }
                catch (SQLException e) {
                    Log.println(Log.ERROR, methodName, " Cannot close statement", e);
                }
            }
        }
        return result;
    }

    /**
     * *
     * Get biomass or nutrient mass of below ground compartment on a given step (roots+stump). Don not take whole tree into account (componnent=TREE <=> EmergeTreeData)
     *
     * @param element "BIOMASS" to get biomass, "[nutrient name]" to get nutrient.
     * @param subComponent restrict to a certain sub component type
     * @param stepName Name of the step, ex "12a"
     * @param trees Collection of tree to take into account. Set to null or to empty collection to take every trees of the step/stand
     * @param alive Take into account only alive/dead material. Set to null to ignore.
     * @return Total below ground mass for element
     */
    public double getBelowGroundElement(String element, EmergeTreeSubComponent subComponent, String stepName, Collection<? extends Tree> trees, Boolean alive) {
        final String methodName = "BMMDB.getBelowGroundElement()";
        Statement statement = null;
        double result = 0d;
        try {
            if (ready) {
                update();/*
                 * update db if necessary
                 */
                statement = connection.createStatement();
                StringBuffer sql = getCommonSQL(element, stepName, alive, trees);
                sql.append(" AND NOT COMPONENT='TREE' AND ABOVE_GROUND=FALSE ");
                if (subComponent != null) {
                    sql.append(" AND SUB_COMPONENT='").append(subComponent).append("'");
                }
                if (verboseSQL) {
                    System.out.println(sql.toString());
                }
                time = System.currentTimeMillis();
                ResultSet rs = statement.executeQuery(sql.toString());
                if (verbose) {
                    System.out.println("Query executed in " + ((System.currentTimeMillis() - time)) + " ms");
                }
                if (rs != null) {
                    if (rs.next()) {
                        result += rs.getDouble(1);
                    }
                }
            }
            else {
                Log.println(Log.ERROR, methodName, "Cannot get info for table " + dbTablePrefix + currentInstance + " because db is not ready");
            }
        }
        catch (Exception e) {
            Log.println(Log.ERROR, methodName, e.getMessage(), e);
        }
        finally {
            if (statement != null) {
                try {
                    statement.close();
                }
                catch (SQLException e) {
                    Log.println(Log.ERROR, methodName, " Cannot close statement", e);
                }
            }
        }
        return result;
    }

    /**
     * *
     * Get biomass or nutrient mass of above ground compartments on a given step (stems+branches+fruits+leaves). Don not take whole tree into account (componnent=TREE <=> EmergeTreeData)
     *
     * @param element "BIOMASS" to get biomass, "[nutrient name]" to get nutrient.
     * @param subComponent "LITTER" for litter roots, "SOIL" for soil roots, null to ignore this criteria.
     * @param stepName Name of the step, ex "12a"
     * @param trees Collection of tree to take into account. Set to null or to empty collection to take every trees of the step/stand
     * @param alive Take into account only alive/dead material. Set to null to ignore.
     * @return Total above ground mass for element
     */
    public double getAboveGroundElement(String element, EmergeTreeSubComponent subComponent, String stepName, Collection<? extends Tree> trees, Boolean alive) {
        final String methodName = "BMMDB.getAboveGroundElement()";
        Statement statement = null;
        double result = 0d;
        try {
            if (ready) {
                update();/*
                 * update db if necessary
                 */
                statement = connection.createStatement();
                StringBuffer sql = getCommonSQL(element, stepName, alive, trees);
                sql.append(" AND NOT COMPONENT='TREE' AND ABOVE_GROUND=TRUE ");
                if (subComponent != null) {
                    sql.append(" AND SUB_COMPONENT='").append(subComponent).append("'");
                }
                if (verboseSQL) {
                    System.out.println(sql.toString());
                }
                time = System.currentTimeMillis();
                ResultSet rs = statement.executeQuery(sql.toString());
                if (verbose) {
                    System.out.println("Query executed in " + ((System.currentTimeMillis() - time)) + " ms");
                }
                if (rs != null) {
                    if (rs.next()) {
                        result += rs.getDouble(1);
                    }
                }
            }
            else {
                Log.println(Log.ERROR, methodName, "Cannot get info for table " + dbTablePrefix + currentInstance + " because db is not ready");
            }
        }
        catch (Exception e) {
            Log.println(Log.ERROR, methodName, e.getMessage(), e);
        }
        finally {
            if (statement != null) {
                try {
                    statement.close();
                }
                catch (SQLException e) {
                    Log.println(Log.ERROR, methodName, " Cannot close statement", e);
                }
            }
        }
        return result;
    }

    /**
     * *
     * Get total biomass mass on a given step
     *
     * @param subComponent "BARK" for bark, "WOOD" for wood, null to ignore criteria
     * @param useWholeTree True to use component of type=TREE <=> EmergeTreeData to sum up values. False to use every other components
     * @param stepName Name of the step, ex "12a"
     * @param trees Collection of tree to take into account. Set to null or to empty collection to take every trees of the step/stand
     * @param alive Take into account only alive/dead material. Set to null to ignore.
     * @return Total above ground mass for element
     */
    public double getTotalBiomass(EmergeTreeSubComponent subComponent, boolean useWholeTree, String stepName, Collection<? extends Tree> trees, Boolean alive) {
        final String methodName = "BMMDB.getTotalBiomass()";
        Statement statement = null;
        double result = 0d;
        try {
            if (ready) {
                update();/*
                 * update db if necessary
                 */
                statement = connection.createStatement();
                StringBuffer sql = getCommonSQL("BIOMASS", stepName, alive, trees);
                if (useWholeTree) {
                    sql.append(" AND COMPONENT='TREE'");
                }
                else {
                    sql.append(" AND NOT COMPONENT='TREE'");
                }
                if (alive != null) {
                    sql.append(" AND ALIVE=").append(alive);
                }
                if (subComponent != null) {
                    sql.append(" AND SUB_COMPONENT='").append(subComponent).append("'");
                }
                if (verboseSQL) {
                    System.out.println(sql.toString());
                }
                time = System.currentTimeMillis();
                ResultSet rs = statement.executeQuery(sql.toString());
                if (verbose) {
                    System.out.println("Query executed in " + ((System.currentTimeMillis() - time)) + " ms");
                }
                if (rs != null) {
                    if (rs.next()) {
                        result += rs.getDouble(1);
                    }
                }
            }
            else {
                Log.println(Log.ERROR, methodName, "Cannot get info for table " + dbTablePrefix + currentInstance + " because db is not ready");
            }
        }
        catch (Exception e) {
            Log.println(Log.ERROR, methodName, e.getMessage(), e);
        }
        finally {
            if (statement != null) {
                try {
                    statement.close();
                }
                catch (SQLException e) {
                    Log.println(Log.ERROR, methodName, " Cannot close statement", e);
                }
            }
        }
        return result;
    }

    /**
     * *
     * Get total nutrient type mass on a given step
     *
     * @param element "BIOMASS" to get biomass, "[nutrient name]" to get nutrient.
     * @param subComponent "BARK" for bark, "WOOD" for wood, null to ignore criteria
     * @param useWholeTree True to use component of type=TREE <=> EmergeTreeData to sum up values. False to use every other components
     * @param stepName Name of the step, ex "12a"
     * @param trees Collection of tree to take into account. Set to null or to empty collection to take every trees of the step/stand
     * @param alive Take into account only alive/dead material. Set to null to ignore.
     * @return Total above ground mass for element
     */
    public double getTotalNutrient(String element, EmergeTreeSubComponent subComponent, boolean useWholeTree, String stepName, Collection<? extends Tree> trees, Boolean alive) {
        final String methodName = "BMMDB.getTotalNutrient()";
        Statement statement = null;
        double result = 0d;
        try {
            if (ready) {
                /*
                 * update db if necessary
                 */ update();
                statement = connection.createStatement();
                StringBuffer sql = getCommonSQL(element, stepName, alive, trees);
                if (useWholeTree) {
                    sql.append(" AND COMPONENT='TREE'");
                }
                else {
                    sql.append(" AND NOT COMPONENT='TREE'");
                }
                if (alive != null) {
                    sql.append(" AND ALIVE=").append(alive);
                }
                if (subComponent != null) {
                    sql.append(" AND SUB_COMPONENT='").append(subComponent).append("'");
                }
                if (verboseSQL) {
                    System.out.println(sql.toString());
                }
                time = System.currentTimeMillis();
                ResultSet rs = statement.executeQuery(sql.toString());
                if (verbose) {
                    System.out.println("Query executed in " + ((System.currentTimeMillis() - time)) + " ms");
                }
                if (rs != null) {
                    if (rs.next()) {
                        result += rs.getDouble(1);
                    }
                }
            }
            else {
                Log.println(Log.ERROR, methodName, "Cannot get info for table " + dbTablePrefix + currentInstance + " because db is not ready");
            }
        }
        catch (Exception e) {
            Log.println(Log.ERROR, methodName, e.getMessage(), e);
        }
        finally {
            if (statement != null) {
                try {
                    statement.close();
                }
                catch (SQLException e) {
                    Log.println(Log.ERROR, methodName, " Cannot close statement", e);
                }
            }
        }
        return result;
    }

    /**
     * *
     * Get total nutrients mass on a given step
     *
     * @param subComponent "BARK" for bark, "WOOD" for wood, "LITTER" for litter roots, "SOIL" for soil roots, null to ignore this criteria.
     * @param useWholeTree True to use component of type=TREE <=> EmergeTreeData to sum up values. False to use every other components
     * @param stepName Name of the step, ex "12a"
     * @param trees Collection of tree to take into account. Set to null or to empty collection to take every trees of the step/stand
     * @param alive Take into account only alive/dead material. Set to null to ignore.
     * @return Total nutrients mass
     */
    public double getTotalNutrients(EmergeTreeSubComponent subComponent, boolean useWholeTree, String stepName, Collection<? extends Tree> trees, Boolean alive) {
        final String methodName = "BMMDB.getTotalNutrients()";
        Statement statement = null;
        double result = 0d;
        try {
            if (ready) {
                update();/*
                 * update db if necessary
                 */
                statement = connection.createStatement();
                StringBuffer sql = getCommonSQL("nutrients", stepName, alive, trees);
                if (useWholeTree) {
                    sql.append(" AND COMPONENT='TREE'");
                }
                else {
                    sql.append(" AND NOT COMPONENT='TREE'");
                }
                if (alive != null) {
                    sql.append(" AND ALIVE=").append(alive);
                }
                if (subComponent != null) {
                    sql.append(" AND SUB_COMPONENT='").append(subComponent).append("'");
                }
                if (verboseSQL) {
                    System.out.println(sql.toString());
                }
                time = System.currentTimeMillis();
                ResultSet rs = statement.executeQuery(sql.toString());
                if (verbose) {
                    System.out.println("Query executed in " + ((System.currentTimeMillis() - time)) + " ms");
                }
                if (rs != null) {
                    if (rs.next()) {
                        result += rs.getDouble(1);
                    }
                }
            }
            else {
                Log.println(Log.ERROR, methodName, "Cannot get info for table " + dbTablePrefix + currentInstance + " because db is not ready");
            }
        }
        catch (Exception e) {
            Log.println(Log.ERROR, methodName, e.getMessage(), e);
        }
        finally {
            if (statement != null) {
                try {
                    statement.close();
                }
                catch (SQLException e) {
                    Log.println(Log.ERROR, methodName, " Cannot close statement", e);
                }
            }
        }
        return result;
    }

    /**
     * *
     * implements project listener to manage updates on the db
     *
     * @param l Project linked
     * @param param array containing type of event and reference to step added/deleted
     */
    @Override
    public void somethingHappened(ListenedTo l, Object param) {
        /*
         * report need of db update
         */
        updateNeeded = true;
        /*
         * ad step added/deleted to list
         */
        if (param instanceof Object[]) {
            steps.add(((Step) ((Object[]) param)[2]));
        }
    }
}

