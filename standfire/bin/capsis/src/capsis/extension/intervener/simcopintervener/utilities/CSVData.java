package capsis.extension.intervener.simcopintervener.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

/**
 *
 * @author thomas.bronner@gmail.com
 */
public class CSVData {

    public static final String CSV_FILE_DATA_SEPARATOR = ";\t";

    private ArrayList<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();
    ArrayList<String> columnsNames = new ArrayList<String>();

    public CSVData(File csvFile) throws FileNotFoundException, IOException {
        BufferedReader br = new BufferedReader(new FileReader(csvFile));
        String stringLine;
        HashMap<String, String> dataLine = null;
        boolean firsLine = true;
        while ((stringLine = br.readLine()) != null) {
            int tokenNumber = 0;
            if (!firsLine) {
                dataLine = new HashMap<String, String>();
            }
            StringTokenizer st = new StringTokenizer(stringLine, CSV_FILE_DATA_SEPARATOR);
            while (st.hasMoreTokens()) {
                String token = st.nextToken().replace("\"", "").trim();//remove double quotes, whitespaces and case errors
                if (firsLine) {
                    //make list of data names (meta data)
                    columnsNames.add(token);
                } else {
                    //fill map
                    dataLine.put(columnsNames.get(tokenNumber), token);
                }
                tokenNumber++;
            }
            if (!firsLine) {
                data.add(dataLine);
            } else {
                firsLine = false;
            }
        }
        if (br != null) {
            br.close();
        }
    }

    public ArrayList<HashMap<String, String>> getData() {
        return data;
    }

    public ArrayList<String> getColumnsNames() {
        return columnsNames;
    }

}
