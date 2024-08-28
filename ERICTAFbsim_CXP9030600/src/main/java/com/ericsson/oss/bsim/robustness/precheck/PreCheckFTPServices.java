package com.ericsson.oss.bsim.robustness.precheck;

import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.ericsson.oss.bsim.data.model.NodeType;
import com.ericsson.oss.bsim.getters.api.BsimCsHandler;

public class PreCheckFTPServices implements IBsimPreChecker {

    private static Logger log;

    private static BsimCsHandler csHandler;

    @Override
    public boolean doPreCheck(final NodeType nodeType) {

        // TODO Auto-generated method stub
        return checkFTPServices();
    }

    @Override
    @Test(groups = { "common.precheck" }, alwaysRun = true)
    public void doPreCheck() {
        Assert.assertEquals(checkFTPServices(), true);
    }

    /**
     * Check does the Ftp Server and Ftp Services exist
     * 
     * @return OK if Ftp Server and Ftp Services exist
     */
    public boolean checkFTPServices() {

        log = Logger.getLogger(PreCheckFTPServices.class);
        log.info("<font color=purple><B>5> Start to check FtpServer and FtpService...</B></font>");
        final LinkedHashMap<String, String> outputMap = new LinkedHashMap<String, String>();
        final LinkedHashMap<String, String> unexpectedResultMap = new LinkedHashMap<String, String>();
        // OSS-137737
        final String ftpServerName = "SMRSSLAVE-CORE-nedssv4";
        final String ftpServicePattern = "aif,back,config,key,sws";
        final String[] ftpServices = ftpServicePattern.split(",");
        final String successMessage = "Pre-check on FtpServer & FtpService is SUCCESSFUL.";
        final String failMessage = "Pre-check on FtpServer & FtpService FAILED, details below:";
        final String consoleMessage = "Available FtpServer & FtpServices:";

        csHandler = new BsimCsHandler("onrm");

        try {
            checkDoesFtpServerExist(outputMap, unexpectedResultMap, ftpServerName);

            checkDoesFtpServiceExist(outputMap, unexpectedResultMap, ftpServerName, ftpServices);
        } catch (final Exception ex) {
            log.equals("EXCEPTION " + ex.getMessage());
        }

        return analyseResultsAndReturnToTestCase(outputMap, unexpectedResultMap, successMessage, failMessage, consoleMessage);
    }

    private void checkDoesFtpServiceExist(
            final LinkedHashMap<String, String> outputMap,
            final LinkedHashMap<String, String> unexpectedResultMap,
            final String ftpServerName,
            final String[] ftpServices) {

        String line;

        // "/opt/ericsson/nms_cif_cs/etc/unsupported/bin/cstest -s ONRM_CS lt FtpService | grep " + ftpServerName;
        String myCmdString;
        myCmdString = "FtpService | grep " + ftpServerName;
        final String ftpService = csHandler.getListByType(myCmdString);
        final String checkFtpService = ftpService;

        final Pattern pattern = Pattern.compile("FtpService=(.*)");
        final StringBuilder sbServices = new StringBuilder();
        final Scanner scanner = new Scanner(checkFtpService);

        try {
            while ((line = scanner.next()) != null) {

                final String key = parseLine(line, 0, pattern);
                log.info("Key ==> " + key);
                log.info("Value ==> " + line);
                outputMap.put("FtpService-" + key, line);
                sbServices.append(key + ",");
            }
            final String servicesStr = sbServices.toString();
            for (final String serviceKey : ftpServices) {
                if (!servicesStr.contains(serviceKey)) {
                    log.info("<font color=red>FtpService Not Found ==> " + serviceKey + "</font");
                    unexpectedResultMap.put(serviceKey, "FtpService Not Found");
                }
            }
        } finally {
            scanner.close();
        }

    }

    private void checkDoesFtpServerExist(
            final LinkedHashMap<String, String> outputMap,
            final LinkedHashMap<String, String> unexpectedResultMap,
            final String ftpServerName) {

        String line;

        // "/opt/ericsson/nms_cif_cs/etc/unsupported/bin/cstest -s ONRM_CS lt FtpServer | grep " + ftpServerName;
        String myCmdString;
        myCmdString = "FtpServer | grep " + ftpServerName;

        final String ftpServer = csHandler.getListByType(myCmdString);
        final String checkFtpServer = ftpServer;
        final Scanner checkFtpScanner = new Scanner(checkFtpServer);

        try {
            while (checkFtpScanner.hasNextLine() && (line = checkFtpScanner.nextLine()) != null) {
                log.info("FtpServer ==>" + line);
                outputMap.put("FtpServer", line);
            }
            if (outputMap.size() == 0) {
                log.info("<font color=red>No FtpServer found! </font>");
                unexpectedResultMap.put(ftpServerName, "FtpServer Not Found");
            }
        } finally {
            checkFtpScanner.close();
        }

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

    static String parseLine(final String line, final int index, final Pattern pattern) {

        String key = null;

        final String[] arr = line.trim().split("\\s+");

        final String tmp = arr[index];
        if (pattern != null) {
            final Matcher m = pattern.matcher(tmp);
            if (m.find()) {
                key = m.group(1);
            } else {
                key = "[KeyNotFound: " + line + "]";
            }

        } else {
            key = tmp;
        }

        return key;
    }

    @Override
    public String getCheckDescription() {

        return "Check availability of FTP services...";
    }

}
