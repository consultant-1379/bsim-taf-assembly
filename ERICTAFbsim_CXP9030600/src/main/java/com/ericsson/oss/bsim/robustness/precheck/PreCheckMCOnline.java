package com.ericsson.oss.bsim.robustness.precheck;

import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Scanner;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.ericsson.oss.bsim.data.model.NodeType;
import com.ericsson.oss.bsim.getters.api.BsimApiGetter;
import com.ericsson.oss.bsim.getters.api.BsimRemoteCommandExecutor;

public class PreCheckMCOnline implements IBsimPreChecker {

    private static Logger log;

    private static BsimRemoteCommandExecutor preCheckSshRemoteCommandExecutor = BsimApiGetter.getRemoteCommandExecutor(BsimApiGetter.getHostMaster());

    @Override
    public boolean doPreCheck(final NodeType nodeType) {

        return checkMCOnline();
    }

    @Override
    @Test(groups = { "common.precheck" }, alwaysRun = true)
    public void doPreCheck() {
        Assert.assertEquals(checkMCOnline(), true);
    }

    public boolean checkMCOnline() {

        log = Logger.getLogger(PreCheckMCOnline.class);

        log.info("<font color=purple><B>1> Start to check required online MCs...</B></font>");
        final LinkedHashMap<String, String> outputMap = new LinkedHashMap<String, String>();
        final LinkedHashMap<String, String> unexpectedResultMap = new LinkedHashMap<String, String>();
        final String mcs = "ONRM_CS,Seg_masterservice_CS,OsgiFwk,oss_cex,pci_service,ARNEServer,ActivityManager";
        final String successMessage = "The pre-check on RNC present & Synced is SUCCESSFUL.";
        final String failMessage = "Pre-check on required online MCs is failed. See problems as below:";
        final String consoleMessage = "Pre-check details on RNC are as below:";

        final String command = "/opt/ericsson/bin/smtool ";
        final String option = "list ";
        final String[] requiredMCs = mcs.trim().split(",");
        boolean testResult;

        try {

            for (final String mc : requiredMCs) {
                final String checkMcCommand = command + option + mc;
                final String checkMCs = preCheckSshRemoteCommandExecutor.simpleExec(checkMcCommand);

                final Scanner scanner = new Scanner(checkMCs);

                try {
                    String line = null;
                    while (scanner.hasNextLine() && (line = scanner.nextLine()) != null) {
                        boolean mcFound = false;
                        // for (final String mc : requiredMCs) {
                        if (line.toLowerCase().contains(mc.toLowerCase())) {
                            mcFound = true;
                            if (line.toLowerCase().contains("started".toLowerCase())) {
                                // the mc is online
                                outputMap.put(mc, "started");
                            } else {
                                // the mc is installed but not online
                                final int index = line.lastIndexOf(" ");
                                unexpectedResultMap.put(mc, line.substring(index));
                                mcFound = false;
                                break;
                            }
                            break;
                        }
                        // }

                        // unexpected exception happens
                        if (!mcFound) {
                            unexpectedResultMap.put("Unknown", line);
                        }

                    }
                } finally {
                    scanner.close();
                }

            }
            // log.info(checkMCs);

        } catch (final Exception e) {
            unexpectedResultMap.put("EXCEPTION ", e.toString());
        }

        if (outputMap.size() == requiredMCs.length && unexpectedResultMap.size() == 0) {

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

        return "Check the online MCs...";
    }
}
