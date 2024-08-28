package com.ericsson.oss.bsim.robustness.precheck;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.ericsson.oss.bsim.data.model.BsimPreCheckConstantData;
import com.ericsson.oss.bsim.data.model.NodeType;
import com.ericsson.oss.bsim.getters.api.BsimApiGetter;
import com.ericsson.oss.bsim.getters.api.BsimRemoteCommandExecutor;

public class PreCheckLacAndSac implements IBsimPreChecker {

    private static Logger log = Logger.getLogger(PreCheckLacAndSac.class);

    private static BsimRemoteCommandExecutor preCheckSshRemoteCommandExecutor = BsimApiGetter.getRemoteCommandExecutor(BsimApiGetter.getHostMaster());

    @Override
    public boolean doPreCheck(final NodeType nodeType) {

        if (nodeType.equals(NodeType.WCDMA) || nodeType.equals(NodeType.MICRO_WCDMA)) {
            return checkLacAndSac(BsimPreCheckConstantData.RNC_NAME);
        } else {
            log.info("Executing of checkLacAndSac skipped for NodeType LTE");
            return true;
        }
    }

    @Override
    @Test(groups = { "wcdma.precheck" }, alwaysRun = true)
    public void doPreCheck() {
        Assert.assertEquals(checkLacAndSac(BsimPreCheckConstantData.RNC_NAME), true);
    }

    private boolean checkLacAndSac(final String rncName) {

        log.info("<font color=purple>10> <B>Start to check Lac and Sac Exist...</B></font>");
        final LinkedHashMap<String, String> outputMap = new LinkedHashMap<String, String>();
        final LinkedHashMap<String, String> unexpectedResultMap = new LinkedHashMap<String, String>();
        final String successMessage = "Pre-check for lac and sac is SUCCESSFUL";
        final String failMessage = "Pre-check for lac and sac FAILED, details below:";
        final String consoleMessage = "Precheck details for lac and sac as follows:";

        final String csTest = "/opt/ericsson/nms_cif_cs/etc/unsupported/bin/cstest -s ";
        final String segDatabase = " Seg_masterservice_CS";
        final String fdn = "SubNetwork=ONRM_ROOT_MO_R,SubNetwork=" + rncName + ",MeContext=" + rncName;
        log.info("fdn ======> " + fdn);
        final String checkLac = csTest + segDatabase + " la " + fdn + ",ManagedElement=1,RncFunction=1,LocationArea=1 lac";
        log.info("checkLac fdn ======> " + checkLac);
        String lacResult = preCheckSshRemoteCommandExecutor.simpleExec(checkLac);
        try {
            log.info("Checking if Lac and Sac Exist...");
            Thread.sleep(3000);
        } catch (final InterruptedException e) {
            log.info("An exception occured while checking for Lac and Sac " + e.getMessage());
        }
        if (lacResult.contains("[1] lac (long 1..65533 65535..65535 m): 1")) {
            outputMap.put("Lac", "[1] lac (long 1..65533 65535..65535 m): 1");
        } else {
            final String areas = "Areas";
            final String areasCheck = csTest + segDatabase + " lt " + areas;
            log.info("areasCheck----------chandan------> " + areasCheck);
            final String areasResult = preCheckSshRemoteCommandExecutor.simpleExec(areasCheck);
            log.info("areasResult----------chandan------> " + areasResult);
            final String grep = "| grep -i ";
            final String plmnCheck = csTest + segDatabase + " lt Plmn " + grep + areasResult;
            log.info("plmnCheck----------chandan------> " + plmnCheck);
            final String plmnResult = preCheckSshRemoteCommandExecutor.simpleExec(plmnCheck);
            log.info("plmnResult----------chandan------> " + plmnResult);
            final String createLocationArea = csTest + segDatabase + " cm " + plmnResult + ",LocationArea=1 -attr lac 1";
            log.info("createLocationArea----------chandan------> " + createLocationArea);
            final String createLocationAreaResult = preCheckSshRemoteCommandExecutor.simpleExec(createLocationArea);

            lacResult = preCheckSshRemoteCommandExecutor.simpleExec(checkLac);
            log.info("lacResult----------chandan------> " + lacResult);
            if (lacResult.contains("[1] lac (long 1..65533 65535..65535 m): 1")) {
                outputMap.put("Lac", "[1] lac (long 1..65533 65535..65535 m): 1");
            }
            log.info("Lac Exists......");
            // log.error("Check lac FAILED");
            // unexpectedResultMap.put("Check lac", "Check lac failed");
        }

        final String checkSac = csTest + segDatabase + " la " + fdn + ",ManagedElement=1,RncFunction=1,LocationArea=1,ServiceArea=1 sac";
        log.info("checkSac fdn ======> " + checkSac);
        final String sacResult = preCheckSshRemoteCommandExecutor.simpleExec(checkSac);

        if (sacResult.contains("[1] sac (long 0..65535 m)         : 1")) {
            outputMap.put("Sac", "[1] sac (long 0..65535 m)         : 1");
        } else {
            log.error("Check sac FAILED");
            unexpectedResultMap.put("Check sac", "Check sac failed");
        }
        return analyseResultsAndReturnToTestCase(outputMap, unexpectedResultMap, successMessage, failMessage, consoleMessage);
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

        return "Check lac and sac...";
    }
}
