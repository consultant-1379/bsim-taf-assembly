package com.ericsson.oss.bsim.robustness.precheck;

import static com.ericsson.cifwk.taf.assertions.TafAsserts.assertFalse;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.tools.cli.handlers.impl.RemoteObjectHandler;
import com.ericsson.oss.bsim.data.model.NodeType;
import com.ericsson.oss.bsim.getters.api.BsimApiGetter;
import com.ericsson.oss.bsim.getters.api.BsimRemoteCommandExecutor;
import com.ericsson.oss.bsim.utils.BsimTestCaseFileHelper;

public class PreCheckBSIMUserinDHCPServer implements IBsimPreChecker {

    private static Logger log = Logger.getLogger(PreCheckBSIMUserinDHCPServer.class);

    private static BsimRemoteCommandExecutor ossmasterRootSshRemoteCommandExecutor;

    private static BsimRemoteCommandExecutor infraServerSshRemoteCommandExecutor;

    private static RemoteObjectHandler ossmasterRootRemoteFileHandler;

    private static RemoteObjectHandler infraServerRemoteFileHandler;

    public static void main(final String[] args) {

        final PreCheckBSIMUserinDHCPServer check = new PreCheckBSIMUserinDHCPServer();

        check.doPreCheck(null);
    }

    @Override
    public boolean doPreCheck(final NodeType nodeType) {

        return checkBSIMUserinDHCPServer();
    }

    @Override
    @Test(groups = { "common.precheck" }, alwaysRun = true)
    public void doPreCheck() {
        Assert.assertEquals(checkBSIMUserinDHCPServer(), true);
    }

    /**
     * 1) Check to see if DHCP has been set up on the Infra Server 2) Check to
     * see the bsim user has been set up on the Infra Server and Master Server
     * 
     * @return OK if all checks have passed successfully
     */
    public boolean checkBSIMUserinDHCPServer() {

        infraServerSshRemoteCommandExecutor = BsimApiGetter.getRemoteCommandExecutor(BsimApiGetter.getHostInfraServer());
        log.info("<font color=purple><B>Start to check BSIM user for DHCP service...</B></font>");
        final LinkedHashMap<String, String> outputMap = new LinkedHashMap<String, String>();
        final LinkedHashMap<String, String> unexpectedResultMap = new LinkedHashMap<String, String>();
        final String successMessage = "Pre-check for Bsim user is SUCCESSFUL";
        final String failMessage = "Pre-check for Bsim user FAILED, details below:";
        final String consoleMessage = "Precheck details for DHCP Service and Bsim user:";
        try {
            checkDHCPServerIsConfigured(outputMap, unexpectedResultMap);
            createUserIfMissingOnInfraServer(outputMap, unexpectedResultMap);
            createUserIfMissingOnMasterServer(outputMap, unexpectedResultMap, "NORMAL");
            createUserIfMissingOnMasterServer(outputMap, unexpectedResultMap, "SECURE");
        } catch (final Exception e) {
            unexpectedResultMap.put("EXCEPTION-init", e.toString());
        }
        return analyseResultsAndReturnToTestCase(outputMap, unexpectedResultMap, successMessage, failMessage, consoleMessage);
    }

    private void checkDHCPServerIsConfigured(final LinkedHashMap<String, String> outputMap, final LinkedHashMap<String, String> unexpectedResultMap) {

        String myCommand;
        String outputResult;
        try {
            myCommand = "/ericsson/ocs/bin/ai_manager.sh -init";
            outputResult = infraServerSshRemoteCommandExecutor.simpleExec(myCommand);
            log.info("Going for  sleep for 5 seconds for getting the output of /ericsson/ocs/bin/ai_manager.sh -init ");
            Thread.sleep(5000);
            log.info("Executed /ericsson/ocs/bin/ai_manager.sh -init, output is ==>  = " + outputResult);
            if (outputResult.contains("DHCP service appears to be configured on this server")) {
                outputMap.put("DHCP Service", "The DHCP Server is configured on INFRA server.");
            } else {
                log.info("Re-attempting to execute /ericsson/ocs/bin/ai_manager.sh");
                outputResult = infraServerSshRemoteCommandExecutor.simpleExec(myCommand);
                log.info("Executed /ericsson/ocs/bin/ai_manager.sh -init, output is ==>  = " + outputResult);
                if (outputResult.contains("DHCP service appears to be configured on this server")) {
                    outputMap.put("DHCP Service", "The DHCP Server is configured on INFRA server.");
                } else {
                    unexpectedResultMap.put("DHCP Service", "The DHCP Server is NOT configured on INFRA server.");
                }
            }
        } catch (final Exception e) {
            unexpectedResultMap.put("EXCEPTION-init", e.toString());
        }
    }

    private void createUserIfMissingOnInfraServer(final LinkedHashMap<String, String> outputMap, final LinkedHashMap<String, String> unexpectedResultMap) {

        infraServerRemoteFileHandler = BsimApiGetter.getInfrServerFileHandler();

        String myCommand;
        String outputResult;
        try {
            // ** For testing only ==> To manually delete a "bsim" user on the
            // command line execute ==> userdel bsim
            myCommand = "id bsim";
            outputResult = infraServerSshRemoteCommandExecutor.simpleExec(myCommand);
            log.info("Executed id bsim " + outputResult);
            if (outputResult.matches("(?s)uid=.+ gid=.+") || outputResult.contains("(bsim)")) {
                log.info("After check ==> User already exists ==> OK");
                outputMap.put("BSIM user", "User bsim is created in INFRA server.");
            } else {
                transferCreateUserScripts(infraServerSshRemoteCommandExecutor, infraServerRemoteFileHandler);
                myCommand = ". utilityScript/create_bsim_user.sh";

                log.info("<font color=red>Bsim user does not exixt ==> attempting to create bsim user on INFRA Server... ==> executing : " + myCommand
                        + "</font>");
                outputResult = infraServerSshRemoteCommandExecutor.simpleExec(myCommand);

                myCommand = "id bsim";
                outputResult = infraServerSshRemoteCommandExecutor.simpleExec(myCommand);
                log.info("Executed id bsim " + outputResult);

                if (!outputResult.matches("(?s)uid=.+ gid=.+") && !outputResult.contains("bsim")) {
                    log.info("<font color=red>Bsim user creation FAILED</font>");
                    unexpectedResultMap.put("BSIM user", "Double check failed after trying to create user bsim in INFRA server.");
                } else {
                    log.info("<font color=green>Bsim user created SUCCESSFULLY</font>");
                }
            }
        } catch (final Exception e) {
            unexpectedResultMap.put("EXCEPTION-init", e.toString());
        }
    }

    private void createUserIfMissingOnMasterServer(
            final LinkedHashMap<String, String> outputMap,
            final LinkedHashMap<String, String> unexpectedResultMap,
            final String userType) {

        ossmasterRootSshRemoteCommandExecutor = BsimApiGetter.getRemoteCommandExecutor(BsimApiGetter.getHostMaster());
        ossmasterRootRemoteFileHandler = BsimApiGetter.getMasterRootFileHandler();
        String outputResult;
        final String password = "bsim01";
        try {
            final String scriptToExecute = "create_" + userType + "_user.sh";
            final String checkUserExists = "/opt/ericsson/bin/pwAdmin -g BSIM " + userType + " bsim";
            outputResult = ossmasterRootSshRemoteCommandExecutor.simpleExec(checkUserExists);

            log.info("outputResult:" + outputResult);

            if (outputResult.contains(password)) {
                log.info(userType + " bsim user is present");
                outputMap.put("bsim (" + userType + ")", "User bsim (" + userType + ") has been already created on master server.");
            } else {
                transferCreateUserScripts(ossmasterRootSshRemoteCommandExecutor, ossmasterRootRemoteFileHandler);
                final String createUserCommand = ". utilityScript/" + scriptToExecute;
                log.info(userType + " Bsim user does not exixt ==> attempting to create bsim user on Master Server... ==> executing : " + createUserCommand);
                log.info("Executing create (" + userType + ") user Script : \nOutput from console is :\n"
                        + ossmasterRootSshRemoteCommandExecutor.simpleExec(createUserCommand));

                outputResult = ossmasterRootSshRemoteCommandExecutor.simpleExec(checkUserExists);
                if (outputResult.contains(password)) {
                    log.info(userType + " bsim user created SUCCESSFULLY");
                    outputMap.put("bsim (" + userType + ")", "User bsim (" + userType + ") has been created on master server successfully.");
                } else {
                    log.info(userType + " bsim user creation FAILED");
                    unexpectedResultMap.put("bsim (" + userType + ")", "Double check existance of bsim (" + userType
                            + ") failed after trying to create user bsim (" + userType + ") automatically .");
                }
            }
        } catch (final Exception e) {
            unexpectedResultMap.put("EXCEPTION-normal", e.toString());
        }
    }

    private void transferCreateUserScripts(final BsimRemoteCommandExecutor executor, final RemoteObjectHandler fileHandler) {

        final String templatesFolder = "utilityScript/";

        log.info("Making directory " + executor.simpleExec("mkdir " + templatesFolder));
        final String homeDirectory = executor.simpleExec("pwd ").trim();
        String transferredTemplatesDirectory = homeDirectory + "/" + templatesFolder;
        transferredTemplatesDirectory = transferredTemplatesDirectory.replace("//", "/").trim();

        final LinkedHashMap<String, String> scriptFileMap = BsimTestCaseFileHelper.searchFilesInWorkspace("user.sh", "/utility_scripts/");// ".jar/utility_scripts/"
        assertFalse("Could not find utility scripts to transfer! Please ensure that there is a test jar file on the classpath", scriptFileMap.isEmpty());

        for (final String fullFilePath : scriptFileMap.values()) {
            log.info("Transferring " + fullFilePath + " to the " + templatesFolder + " folder on the omsrvm server");
            fileHandler.copyLocalFileToRemote(fullFilePath, transferredTemplatesDirectory);
            executor.simpleExec("chmod 755 " + fullFilePath);
        }

        final String filesTransferred = executor.simpleExec("ls " + transferredTemplatesDirectory);
        for (final String fileName : scriptFileMap.keySet()) {
            if (filesTransferred.contains(fileName)) {
                final String response = "File " + fileName + " transferred successfully";
                log.info(response);
            }
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

    private void printOutDetails(final LinkedHashMap<String, String> outputMap, final String consoleMessage) {

        final StringBuilder sb = new StringBuilder(consoleMessage + "\r\n");
        for (final Entry<String, String> entry : outputMap.entrySet()) {
            sb.append(String.format("%1$s: %2$s\r\n", entry.getKey(), entry.getValue()));
        }

        log.info(sb.toString());

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

    @Override
    public String getCheckDescription() {

        return "Check BSIM user for DHCP service...";
    }

}
