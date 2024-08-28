package com.ericsson.oss.bsim.data;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.utils.csv.CsvReader;
import com.ericsson.oss.bsim.batch.data.model.MockLRANPicoBatch;

public class BsimLRANPicoBatchDataProvider {

    private static Logger log = Logger.getLogger(BsimLRANPicoBatchDataProvider.class);

    protected static final String EMPTY_STRING_PLACEHOLDER = "";

    public static final String DATA_FILE = "BSIM_LRANPicoBatchTestData.csv";

    List<String> nodeFdns;

    CsvReader csvReader;

    public List<Object[]> getTestDataList() {

        initialization(DATA_FILE);
        return generateTestDataForLRANPicoBatch();
    }

    public void initialization(final String testDataFileName) {
        try {
            csvReader = DataHandler.readCsv(testDataFileName, ",");
        } catch (final Exception ex) {
            ex.printStackTrace();

        }
    }

    public List<Object[]> generateTestDataForLRANPicoBatch() {

        final List<Object[]> testDataList = new ArrayList<Object[]>();

        // prepare test data list
        final int rowCount = csvReader.getRowCount();

        // process csv file and generate test data
        // row index starts from 1 as row "0" stores column titles
        for (int rowNo = 1; rowNo < rowCount; rowNo++) {
            final Object[] testData = new Object[11];
            MockLRANPicoBatch mockLRANPicoBatch = new MockLRANPicoBatch();

            // all the data will store in the BsimLRANPicoBatch object
            final MockLRANPicoBatchBuilder mockLRANPicoBatchBuilder = new MockLRANPicoBatchBuilder(mockLRANPicoBatch, csvReader);
            mockLRANPicoBatch = mockLRANPicoBatchBuilder.prepareLRANPicoBatchTestData(rowNo, "");

            testData[0] = retrieveCellValueFromCSV(BatchCSVColumns.TC_ID, rowNo);
            testData[1] = retrieveCellValueFromCSV(BatchCSVColumns.TC_TITLE, rowNo);
            testData[2] = retrieveCellValueFromCSV(BatchCSVColumns.TC_DESC, rowNo);
            testData[3] = mockLRANPicoBatch;
            testData[4] = retrieveCellValueFromCSV(BatchCSVColumns.BIND, rowNo);
            testData[5] = retrieveCellValueFromCSV(BatchCSVColumns.NODES_TO_BIND, rowNo);
            testData[6] = retrieveCellValueFromCSV(BatchCSVColumns.DELETE_BOUND_NODES, rowNo);
            testData[7] = retrieveCellValueFromCSV(BatchCSVColumns.BATCH_RESULT, rowNo);
            testData[8] = retrieveCellValueFromCSV(BatchCSVColumns.BIND_RESULT, rowNo);
            testData[9] = retrieveCellValueFromCSV(BatchCSVColumns.DELETE_RESULT, rowNo);
            testData[10] = retrieveCellValueFromCSV(BatchCSVColumns.ADD_Cluster, rowNo);
            testDataList.add(testData);
        }

        return testDataList;

    }

    private String retrieveCellValueFromCSV(final String columnName, final int rowNo) {

        final int columnNo = csvReader.getColumnNoByHeader(columnName);
        if (columnNo != -1) {
            final String value = csvReader.getCell(columnNo, rowNo);

            // for test purpose
            log.debug(value);
            return value;
        } else {
            log.warn("********************Cannot retrieve value from column: " + columnName + "********************");
            return EMPTY_STRING_PLACEHOLDER;
        }
    }

    protected static class BatchCSVColumns {

        // test case info
        static final String TC_ID = "TC ID";

        static final String TC_TITLE = "TC Title";

        static final String TC_DESC = "TC Desc";

        static final String BIND = "Bind";

        static final String NODES_TO_BIND = "Nodes to Bind";

        static final String DELETE_BOUND_NODES = "Delete Bound Nodes";

        static final String BATCH_RESULT = "Batch Result";

        static final String BIND_RESULT = "Bind Result";

        static final String DELETE_RESULT = "Delete Result";

        // newly added here
        static final String ADD_Cluster = "Add to Cluster";

    }

}
