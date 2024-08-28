package com.ericsson.oss.bsim.robustness.precheck;

import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Scanner;

import org.apache.log4j.Logger;
import org.testng.annotations.Test;

import com.ericsson.oss.bsim.data.model.NodeType;
import com.ericsson.oss.bsim.getters.api.BsimCsHandler;

public class PreCheckSyncedRNC implements IBsimPreChecker {

    private static Logger log;

    private static BsimCsHandler csHandler;

    @Override
    public boolean doPreCheck(final NodeType nodeType) {

        return true; // checkSyncedRNC(BsimPreCheckConstantData.RNC_NAME);

    }

    @Override
    @Test(groups = {}, alwaysRun = false)
    public void doPreCheck() {
        // Assert.assertEquals(checkSyncedRNC(BsimPreCheckConstantData.RNC_NAME), true);
    }

    /**
     * Check RNC exists
     * 
     * @param rncName
     * @return OK if RNC exists
     */
    public boolean checkSyncedRNC(final String rncName) {

        log = Logger.getLogger(PreCheckSyncedRNC.class);

        log.info("<font color=purple><B>Start to check the RNC created and synced...</B></font>");
        final LinkedHashMap<String, String> outputMap = new LinkedHashMap<String, String>();
        final LinkedHashMap<String, String> unexpectedResultMap = new LinkedHashMap<String, String>();
        final String successMessage = "The pre-check on RNC present & Synced is SUCCESSFUL.";
        final String failMessage = "Pre-check details on RNC Failed, details below:";
        final String consoleMessage = "Pre-check details on RNC are as below:";

        try {
            final String ONRM_CS = "onrm";
            checkRncExists(rncName, ONRM_CS, outputMap, unexpectedResultMap);
            final String Seg_masterservice_CS = "segment";
            checkRncExists(rncName, Seg_masterservice_CS, outputMap, unexpectedResultMap);
        } catch (final Exception ex) {
            log.error(ex.getMessage());
        }

        return analyseResultsAndReturnToTestCase(outputMap, unexpectedResultMap, successMessage, failMessage, consoleMessage);

    }

    private String checkRncExists(
            final String rncName,
            final String database,
            final LinkedHashMap<String, String> outputMap,
            final LinkedHashMap<String, String> unexpectedResultMap) {

        String myCmdString;
        String line;

        csHandler = new BsimCsHandler(database);

        myCmdString = "MeContext | grep -w MeContext=" + rncName;
        final String checkRncExists = csHandler.getListByType(myCmdString);
        log.info(checkRncExists);

        final Scanner scanner = new Scanner(checkRncExists);

        try {

            if (scanner.hasNextLine() && (line = scanner.nextLine()) != null) {
                outputMap.put("Check RNC in " + database, line);
            } else {
                unexpectedResultMap.put(database, String.format("RNC [%1$s] Not Found in " + database, rncName));
            }
        } finally {
            scanner.close();
        }

        return checkRncExists.toString();
    }

    private boolean analyseResultsAndReturnToTestCase(
            final LinkedHashMap<String, String> outputMap,
            final LinkedHashMap<String, String> unexpectedResultMap,
            final String successMessage,
            final String failMessage,
            final String consoleMessage) {

        boolean testResult;
        if (unexpectedResultMap.size() == 0) {
            log.info(successMessage);
            testResult = true;
        } else {
            testResult = failTestCase(unexpectedResultMap, failMessage);
        }
        printOutDetails(outputMap, consoleMessage);
        return testResult;
    }

    private boolean failTestCase(final LinkedHashMap<String, String> unexpectedResultMap, final String consoleMessage) {

        boolean testResult;
        final StringBuilder sb = new StringBuilder(consoleMessage + "\r\n");
        for (final Entry<String, String> pair : unexpectedResultMap.entrySet()) {
            sb.append(String.format("%1$s: %2$s\r\n", pair.getKey(), pair.getValue()));
        }
        log.error(sb.toString().replaceAll("\r\n$", ""));

        testResult = false;
        return testResult;
    }

    private void printOutDetails(final LinkedHashMap<String, String> outputMap, final String consoleMessage) {

        final StringBuilder sb = new StringBuilder(consoleMessage + "\r\n");
        for (final Entry<String, String> entry : outputMap.entrySet()) {
            sb.append(String.format("%1$s: %2$s\r\n", entry.getKey(), entry.getValue()));
        }

        log.info(sb.toString());

    }

    @Override
    public String getCheckDescription() {

        return "Check the synced RNC...";
    }
}
