package com.ericsson.oss.bsim.robustness.precheck;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;
import com.ericsson.oss.bsim.data.model.NodeType;
import com.ericsson.oss.bsim.getters.api.BsimApiGetter;

/**
 * @author ejomclo
 */
public class PreCheckBsimJksFile implements IBsimPreChecker {

    private static Logger log = Logger.getLogger(PreCheckBsimJksFile.class);

    private static CLICommandHelper ossMasterCLICommandHelper = BsimApiGetter.getCLICommandHelper(BsimApiGetter.getHostMaster());

    private static CLICommandHelper omsasCLICommandHelper = BsimApiGetter.getCLICommandHelper(BsimApiGetter.getHostOmsas());

    private static String REVOKE_CREDENTIALS = "/opt/ericsson/cadm/bin/pkiAdmin cred bsim aiws revoke -reason 1";

    private static String GENERATE_BSIM_JKS_FILE = "/opt/ericsson/cadm/bin/pkiAdmin cred bsim aiws generate";

    private static final String REVOKE_CREDENTIALS_BSIM_MAIL = "/opt/ericsson/cadm/bin/pkiAdmin cred bsim mail_bsim renew -email bsim@athtem.eei.ericsson.se";

    private static final String REVOKE_CREDENTIALS_ANDROID_MAIL = "/opt/ericsson/cadm/bin/pkiAdmin cred bsim mail_mobileapp renew -email android@athtem.eei.ericsson.se";

    private static final String GENERATE_MAIL_BSIM_FILE = "/opt/ericsson/cadm/bin/pkiAdmin cred bsim mail_bsim generate -email bsim@athtem.eei.ericsson.se";

    private static final String GENERATE_MAIL_MOBILEAPP_FILE = "/opt/ericsson/cadm/bin/pkiAdmin cred bsim mail_mobileapp generate -email android@athtem.eei.ericsson.se";

    private static final String CHECK_BSIM_JKS_CMD = "ls /opt/ericsson/scs/aif_creds/";

    private static final String bsimJksFileName = "AIWS_BSIM.jks";

    private static final String ANDROID_MAIL_FILE = "android@athtem.eei.ericsson.se.p12";

    private static final String BSIM_MAIL_FILE = "bsim@athtem.eei.ericsson.se.p12";

    private static final String CHECK_CSA_DEPLOY_STATUS = "/opt/ericsson/cadm/bin/pkiAdmin ca list -cacerts";

    @Override
    public boolean doPreCheck(final NodeType nodeType) {

        return createBsimMailFilesIfRequired() && createBsimJksFileIfRequired();
    }

    public boolean createBsimJksFileIfRequired() {

        log.info("Beginning check for BSIM jks file, if file does not exist. An attempt will be made to generate it");

        if (checkBsimJksFile(0)) {
            return true;
        }
        executeSecurityFixCmdToGenerateBsimJksFile();
        if (checkBsimJksFile(0)) {
            return true;
        }
        generateBsimJksFile();
        if (checkBsimJksFile(0)) {
            return true;
        }

        revokeCredentialsandRetryToGenereateJksFile();
        if (checkBsimJksFile(15)) {
            return true;
        }
        log.error(bsimJksFileName + " is not present on server, Pre Check FAILED");
        log.error("Server may ot be configured correctly. As a manual workaround, run /opt/ericsson/secinst/bin/config.sh on OMSAS Server, Infra Master server, Infra Slave server");
        return false;

    }

    /**
     * <p>
     * Executes command on omsas server to generate BSIM jks file. Command supplied as a fix from security. Usually takes one minute to
     * return
     * </p>
     */
    private void executeSecurityFixCmdToGenerateBsimJksFile() {
        log.info("Security fix command executing on omsas server...");
        final String response = omsasCLICommandHelper.simpleExec("/opt/ericsson/secinst/bin/config.sh -p ERICcsa:deploy -f");
        final String returned = omsasCLICommandHelper.simpleExec(CHECK_CSA_DEPLOY_STATUS);
        if (returned.contains("Internal Error")) {
            omsasCLICommandHelper.simpleExec("svcadm disable csa");
            omsasCLICommandHelper.simpleExec("rm -rf /opt/ericsson/csa/domain/csa/");
            omsasCLICommandHelper.simpleExec("/opt/ericsson/secinst/bin/config.sh -p ERICcsa");
        }
        log.info("Response from server after running security fix\n: " + response);

    }

    public boolean createBsimMailFilesIfRequired() {

        log.info("Beginning check for BSIM Mail files, if files do not exist. An attempt will be made to generate them");

        if (checkBsimMailFiles(0)) {
            return true;
        }

        generateBsimMailFiles();
        if (checkBsimMailFiles(0)) {
            return true;
        }

        revokeBsimMailFiles();
        if (checkBsimMailFiles(7)) {
            return true;
        }
        log.error("BsimMailFiles are not present on server, Pre Check FAILED");
        return false;

    }

    private void generateBsimJksFile() {
        log.info("Attempting to generate " + bsimJksFileName);
        executeSecurityFixCmdToGenerateBsimJksFile();
        omsasCLICommandHelper.simpleExec(GENERATE_BSIM_JKS_FILE);
    }

    private void generateBsimMailFiles() {
        log.info("Attempting to generate BSIM Mail Files");
        executeSecurityFixCmdToGenerateBsimJksFile();
        omsasCLICommandHelper.simpleExec(GENERATE_MAIL_BSIM_FILE);
        omsasCLICommandHelper.simpleExec(GENERATE_MAIL_MOBILEAPP_FILE);
    }

    private void revokeBsimMailFiles() {
        log.info("Revoking BSIM Mail Files credentials");
        omsasCLICommandHelper.simpleExec(REVOKE_CREDENTIALS_BSIM_MAIL);
        omsasCLICommandHelper.simpleExec(REVOKE_CREDENTIALS_ANDROID_MAIL);
        generateBsimMailFiles();
    }

    @Override
    @Test(groups = { "common.precheck" }, alwaysRun = true)
    public void doPreCheck() {
        Assert.assertTrue(createBsimMailFilesIfRequired() && createBsimJksFileIfRequired());

    }

    /**
     * Checks if the BSIM Mail files are generated
     * 
     * @param numberOfMinutesToWaitForFileToBeGenerated
     * @return true if the BSIM Mail files are generated
     */
    private boolean checkBsimMailFiles(final int numberOfMinutesToWaitForFileToBeGenerated) {

        int count = 0;
        final int maximumCount = numberOfMinutesToWaitForFileToBeGenerated;

        do {
            log.info("Checking Bsim mail files under /opt/ericsson/scs/aif_creds/ on Master Server. Number of minutes waiting is: " + count + " minutes");
            final String returned = ossMasterCLICommandHelper.simpleExec(CHECK_BSIM_JKS_CMD);
            if (returned.contains(BSIM_MAIL_FILE) && returned.contains(ANDROID_MAIL_FILE)) {
                log.info("SUCCESS: Bsim mail files found under /opt/ericsson/scs/aif_creds/ on Master Server");
                return true;
            }
            if (count == maximumCount) {
                return false;
            }
            try {
                Thread.sleep(60000);
                log.info("Sleeping for one minute");
            } catch (final InterruptedException e) {
            }
            count++;
        } while (count < maximumCount);
        log.info("BSIM Mail files have not been created");
        return false;
    }

    /**
     * Checks if the BSIM Jks file has been generated
     * 
     * @param numberOfMinutesToWaitForFileToBeGenerated
     * @return true if the BSIM Jks file has been generated
     */
    private boolean checkBsimJksFile(final int numberOfMinutesToWaitForFileToBeGenerated) {

        int count = 0;
        final int maximumCount = numberOfMinutesToWaitForFileToBeGenerated;
        do {
            log.info("Checking Bsim jks file under /opt/ericsson/scs/aif_creds/ on Master Server. Number of minutes waiting is: " + count + " minutes");
            final String returned = ossMasterCLICommandHelper.simpleExec(CHECK_BSIM_JKS_CMD);
            if (returned.contains(bsimJksFileName)) {
                log.info("SUCCESS: " + bsimJksFileName + " file found under /opt/ericsson/scs/aif_creds/ on Master Server");
                return true;
            }
            if (count == maximumCount) {
                return false;
            }
            try {
                Thread.sleep(60000);
                log.info("Sleeping for one minute");
            } catch (final InterruptedException e) {
            }
            count++;
            log.info("JKS File has not been created");
        } while (count < maximumCount);
        return false;
    }

    private void revokeCredentialsandRetryToGenereateJksFile() {

        log.info("Revoking NetConf credentials");
        omsasCLICommandHelper.simpleExec(REVOKE_CREDENTIALS);
        generateBsimJksFile();
    }

    @Override
    public String getCheckDescription() {

        return "Checking jks files..";
    }

}