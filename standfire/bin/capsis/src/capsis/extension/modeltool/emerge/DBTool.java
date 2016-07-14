package capsis.extension.modeltool.emerge;

import capsis.extension.DialogModelTool;
import capsis.gui.MainFrame;
import capsis.kernel.GModel;
import capsis.kernel.Step;
import capsis.lib.emerge.EmergeDB;
import capsis.lib.emerge.UseEmergeDB;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import jeeb.lib.util.Log;
import jeeb.lib.util.Question;
import jeeb.lib.util.Translator;

public class DBTool extends DialogModelTool implements ActionListener {

    static public final String AUTHOR = "T.Bronner";
    static public final String VERSION = "1.0";
    private GModel model;
    private Step step;
    private EmergeDB db;
    private static int csvExportCounter = 0;

    static {
        Translator.addBundle("capsis.extension.modeltool.emerge.DBTool");
    }
    private final JPanel contentPanel = new JPanel();
    private JTable jtResult;
    private JTextArea jtaQuery;
    JButton btnOk;
    JScrollPane jspQuery;
    JScrollPane jspResults;
    private JButton btnMetadata;
    private JButton btnQuery;
    private JButton btnSelectAll;
    private JTextArea jtaConsole;
    private JScrollPane jscConsole;
    private JSplitPane jspltp1, jspltp2;
    private JTextField tfDisplayedLines, tfResultSetTotalLines;
    private JButton btnCsv;
    private PrintWriter out;
    private JProgressBar progressBar;
    Connection connection;
    private JPanel bottomPanel;
    private JButton btnUpdate;
    private JComboBox cbTables;
    private JPanel tablePanel;
    private int displayMaxLines = 100;

    class ExecuteQuerySWorker extends javax.swing.SwingWorker<Object, Integer> {

        Statement statement;
        ResultSet data;
        long time;
        boolean countLines;
        String sql = null;

        ExecuteQuerySWorker(boolean countLines) {
            super();
            this.countLines = countLines;
        }

        ExecuteQuerySWorker(String sql, boolean countLines) {
            super();
            this.sql = sql;
            this.countLines = countLines;
        }

        @Override
        protected Object doInBackground() throws Exception {
            progressBar.setIndeterminate(true);
            statement = connection.createStatement();
            try {
                if (!tfDisplayedLines.getText().isEmpty()) {
                    displayMaxLines = Integer.parseInt(tfDisplayedLines.getText());
                }
            }
            catch (NumberFormatException e) {
            }
            statement.setMaxRows(displayMaxLines);
            time = System.currentTimeMillis();

            /*use content of sql field if no query provided*/
            if (sql == null) {
                sql = jtaQuery.getText();
            }
            if (countLines) {
                statement.execute("SELECT COUNT(*) FROM (" + sql + ")");
            }
            else {
                statement.execute(sql);
            }
            time = System.currentTimeMillis() - time;
            ResultSet rs = statement.getResultSet();
            if (rs != null) {
                return rs;
            }
            else {
                return new Integer(statement.getUpdateCount());
            }

        }

        @Override
        protected void done() {
            try {
                final Object data = get();
                if (data != null) {
                    if (data instanceof ResultSet) {
                        if (countLines) {
                            try {
                                if (data != null && ((ResultSet) data).next()) {
                                    SwingUtilities.invokeLater(new Runnable() {

                                        @Override
                                        public void run() {
                                            try {
                                                tfResultSetTotalLines.setText(((ResultSet) data).getString(1));
                                            }
                                            catch (SQLException ex) {
                                                System.out.println(ex.toString());
                                            }
                                        }
                                    });

                                }
                            }
                            catch (SQLException ex) {
                                consoleAppend(ex.getMessage());
                            }
                        }
                        else {
                            displayResultSet((ResultSet) data);
                        }
                    }
                    else {
                        consoleAppend("result = " + data + " for " + jtaQuery.getText());
                    }
                    consoleAppend("query executed in " + time + " ms");
                }
            }
            catch (InterruptedException e) {
                consoleAppend(e.getMessage());
            }
            catch (ExecutionException e) {
                consoleAppend(e.getMessage());
            }
            finally {
                progressBar.setIndeterminate(false);
                try {
                    if (statement != null) {
                        statement.close();
                    }
                    if (data != null) {
                        data.close();
                    }
                }
                catch (SQLException e) {
                }
            }

        }
    }

    public DBTool() {
        super();
        setTitle(Translator.swap("DBTool"));
        setBounds(0, 0, 1024, 768);
        getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(new BorderLayout(0, 0));
        jtResult = new JTable();
        jtaQuery = new JTextArea();
        jtaQuery.setRows(4);
        jtaQuery.addKeyListener(new KeyAdapter() {
            /*execute query in text area if enter is typed*/

            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == '\n') {
                    executeQuery();
                }
            }
        });
        jspResults = new JScrollPane(jtResult);
        jspQuery = new JScrollPane(jtaQuery);
        jspltp2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, jspResults, jspQuery);
        jspltp2.setResizeWeight(0.9);
        jtaConsole = new JTextArea();
        jtaConsole.setRows(2);
        jtaConsole.setEditable(false);
        jscConsole = new JScrollPane(jtaConsole);
        jspltp1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, jspltp2, jscConsole);
        jspltp1.setResizeWeight(0.9);
        jspltp2.setDividerLocation(0.90);
        jspltp1.setDividerLocation(0.90);
        contentPanel.add(jspltp1, BorderLayout.CENTER);
        bottomPanel = new JPanel();
        getContentPane().add(bottomPanel, BorderLayout.SOUTH);
        bottomPanel.setLayout(new BorderLayout(0, 0));
        JPanel buttonPanel = new JPanel();
        bottomPanel.add(buttonPanel, BorderLayout.EAST);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        btnQuery = new JButton("Run");
        btnQuery.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                executeQuery();
            }
        });
        buttonPanel.add(btnQuery);
        btnOk = new JButton("Quit");
        btnOk.addActionListener(this);
        btnCsv = new JButton("CSV");
        btnCsv.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                saveAsCSV();
            }
        });
        tfDisplayedLines = new JTextField();
        tfDisplayedLines.setText(String.valueOf(displayMaxLines));
        tfDisplayedLines.setColumns(10);
        tfResultSetTotalLines = new JTextField();
        tfResultSetTotalLines.setEditable(false);
        tfResultSetTotalLines.setColumns(10);
        tfDisplayedLines.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
                tfDisplayedLines.setText(tfResultSetTotalLines.getText());
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });
        buttonPanel.add(new JLabel("Lines displayed : "));
        buttonPanel.add(tfDisplayedLines);
        buttonPanel.add(new JLabel("Total lines : "));
        buttonPanel.add(tfResultSetTotalLines);
        buttonPanel.add(btnCsv);
        btnUpdate = new JButton("Update");
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnOk);
        btnOk.setDefaultCapable(true);
        getRootPane().setDefaultButton(btnOk);
        progressBar = new JProgressBar();
        bottomPanel.add(progressBar, BorderLayout.CENTER);
        tablePanel = new JPanel();
        bottomPanel.add(tablePanel, BorderLayout.WEST);
        cbTables = new JComboBox();
        tablePanel.add(cbTables);
        btnSelectAll = new JButton("SELECT*");
        btnSelectAll.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                displayAll();
            }
        });
        tablePanel.add(btnSelectAll);
        btnMetadata = new JButton("Meta data");
        btnMetadata.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                displayMetaData();
            }
        });
        tablePanel.add(btnMetadata);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

    }

    @Override
    public void init(GModel m, Step s) {
        try {
            step = s;
            model = m;
            db = ((UseEmergeDB) model).getEmergeDB();
            connection = db.getConnection();
            /*UI settings wich needs db*/

            jtaQuery.setText("SELECT * FROM " + db.getTableName());
            displayAll();/* default : select * */
            jtaConsole.setText("Current table is " + db.getTableName() + "\n");/* display name of table in console */
            cbTables.addItem(db.getTableName());
            cbTables.addItemListener(new ItemListener() {

                public void itemStateChanged(ItemEvent arg0) {/*selction changed*/
                    jtaQuery.setText("SELECT * FROM " + cbTables.getSelectedItem());
                }
            });
            cbTables.addMouseListener(new MouseAdapter() {

                @Override
                public void mousePressed(MouseEvent arg0) {
                    cbTables.removeAllItems();
                    int i = 0;
                    for (Iterator it = db.getUserTables().iterator(); it.hasNext();) {
                        String table = (String) it.next();
                        cbTables.addItem(table);
                        i++;
                    }
                    cbTables.setMaximumRowCount(i);
                }
            });
            btnUpdate.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent arg0) {
                    new SwingWorker<Void, Void>() {

                        @Override
                        protected Void doInBackground() throws Exception {
                            db.update();
                            return null;
                        }
                    }.execute();
                }
            });
            setVisible(true);
            toFront();
            setModal(false);
        }
        catch (Exception exc) {
            Log.println(Log.ERROR, "DBTool.init()", exc.toString(), exc);
        }

    }

    /*append to console and scroll*/
    private void consoleAppend(final String message) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                jtaConsole.append(message + "\n");
                jscConsole.getVerticalScrollBar().setValue(jscConsole.getVerticalScrollBar().getMaximum());
            }
        });
    }

    /**
     * Extension dynamic compatibility mechanism. This matchwith method checks if the extension can deal (i.e. is
     * compatible) with the referent.
     */
    static public boolean matchWith(Object referent) {
        try {
            if (!(referent instanceof GModel)) {
                return false;
            }
            /*Check that the needed interface is implemented*/
            GModel m = (GModel) referent;
            if (m instanceof UseEmergeDB) {
                return true;
            }

        }
        catch (Exception e) {
            Log.println(Log.ERROR, "DBTool.matchWith ()", "Error in matchWith () (returned false)", e);
            return false;
        }
        return true;
    }

    private void saveAsCSV() {
        final String delimiter = ";";
        try {
            TableModel tableModel = jtResult.getModel();
            int nbCol = tableModel.getColumnCount();
            int nbRow = tableModel.getRowCount();
            if (nbRow == 0) {
                System.out.println("DBTool.saveAsCSV() : Output of empty table aborted.");
                return;
            }
            JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new File(fc.getCurrentDirectory().getAbsolutePath() + System.getProperty("file.separator") + db.getTableName() + (csvExportCounter++) + ".csv"));
            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int returnVal = fc.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                out = new PrintWriter(new BufferedWriter((new FileWriter(file, false))));

                if (tableModel != null) {

                    /*write columns name*/
                    for (int i = 0; i < nbCol; i++) {
                        out.write(tableModel.getColumnName(i) + delimiter);
                    }
                    out.write("\n");
                    for (int j = 0; j < nbRow; j++) {
                        for (int i = 0; i < nbCol; i++) {
                            out.write(tableModel.getValueAt(j, i) + delimiter);
                        }
                        out.write("\n");
                    }
                }
                out.close();
            }
        }
        catch (Exception e) {
            Log.println(Log.ERROR, "DBTool.saveAsCSV()", e.getMessage(), e);
        }

    }

    private void executeQuery() {
        /*run count lines on query*/
        new ExecuteQuerySWorker(true).execute();
        /*run query*/
        new ExecuteQuerySWorker(false).execute();
    }

    private void displayAll() {
        /*run count lines on query*/
        new ExecuteQuerySWorker("SELECT * FROM " + db.getTableName(), true).execute();
        /*run query*/
        new ExecuteQuerySWorker("SELECT * FROM " + db.getTableName(), false).execute();
    }

    private void displayMetaData() {
        ResultSet data = db.getTablesInfos();
        displayResultSet(data);
    }

    private void displayResultSet(ResultSet data) {
        if (db != null) {
            if (data != null) {
                try {
                    /*Get columns names*/
                    int columnsCount = data.getMetaData().getColumnCount();
                    String[] tableColumnsName = new String[columnsCount];
                    for (int i = 0; i < columnsCount; i++) {
                        tableColumnsName[i] = data.getMetaData().getColumnLabel(i + 1);
                    }
                    DefaultTableModel tableModel = (DefaultTableModel) jtResult.getModel();
                    tableModel.setColumnIdentifiers(tableColumnsName);
                    /*get rid of old results*/
                    for (int i = tableModel.getRowCount() - 1; i >= 0; i--) {
                        tableModel.removeRow(i);
                    }
                    /*Get data*/
                    while (data.next()) {
                        Object[] objects = new Object[columnsCount];
                        for (int i = 0; i < columnsCount; i++) {
                            objects[i] = data.getObject(i + 1);
                        }
                        tableModel.addRow(objects);
                    }
                    jtResult.setModel(tableModel);
                }
                catch (Exception exc) {
                    Log.println(Log.ERROR, "DBTool ", exc.toString(), exc);
                }
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource().equals(btnOk)) {
            if (Question.ask(MainFrame.getInstance(), Translator.swap("DBTool.confirm"), Translator.swap("DBTool.confirmClose"))) {
                dispose();
            }
        }
    }
}
