package com.ericsson.oss.bsim.robustness.precheck;

import org.apache.log4j.Logger;

import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;
import com.ericsson.cifwk.taf.tools.cli.Shell;
import com.ericsson.cifwk.taf.tools.cli.TimeoutException;
import com.ericsson.oss.bsim.data.model.NodeType;
import com.ericsson.oss.bsim.getters.api.BsimApiGetter;

/**
 * @author xriskas
 */
public class PreCheckNewBsimJksFile implements IBsimPreChecker {

    private static Logger log = Logger.getLogger(PreCheckBsimJksFile.class);

    private static CLICommandHelper ossMasterCLICommandHelper = BsimApiGetter.getCLICommandHelper(BsimApiGetter.getHostMaster());

    private static CLICommandHelper omsasCLICommandHelper = BsimApiGetter.getCLICommandHelper(BsimApiGetter.getHostOmsas());

    private static String GENERATE_BSIM_JKS_FILE = "/opt/ericsson/cadm/bin/pkiAdmin cred bsim aiws generate";

    private static final String CHECK_BSIM_JKS_CMD = "ls /opt/ericsson/scs/aif_creds/";

    private static final String bsimJksFileName = "AIWS_BSIM.jks";

    @Override
    public boolean doPreCheck(final NodeType nodeType) {
        return createNewBsimJksFile();

    }

    public boolean createNewBsimJksFile() {

        log.info("Beginning check for BSIM jks file, if file does not exist. An attempt will be made to generate it");

        if (checkBsimJksFile(0)) {
            return true;
        }
        generateNewBsimJksFile();
        if (checkBsimJksFile(0)) {
            return true;
        }

        log.error(bsimJksFileName + " is not present on server, Pre Check FAILED");

        return false;
    }

    private void generateNewBsimJksFile() {
        final Shell omsasShell = omsasCLICommandHelper.openShell();

        omsasShell.writeln("/opt/ericsson/secinst/bin/config.sh");
        try {
            log.info("password prompt");
            omsasShell.expect("Enter password for", 540);
            omsasShell.writeln("ldappass");
            log.info("domain");
            omsasShell.expect("Select ldap domain", 180);
            omsasShell.writeln("1");
            log.info("2nd domain");
            omsasShell.expect("Select ldap domain", 250);
            omsasShell.writeln("1");
            log.info("3rd domain");
            omsasShell.expect("domain", 250);
            omsasShell.writeln("1");
            log.info("new password");
            omsasShell.expect("Enter new password", 180);
            omsasShell.writeln("sec94ft9");
            omsasShell.expect("Confirm password for", 180);
            omsasShell.writeln("sec94ft9");
            log.info("last domain");
            omsasShell.expect("Select ldap domain", 180);
            omsasShell.writeln("1");
            log.info("finish");
            final String output = omsasShell.expect("enrollmentdata", 180);
            if (output.contains("enrollmentdata")) {
                omsasCLICommandHelper.simpleExec(GENERATE_BSIM_JKS_FILE);

            }
            omsasShell.disconnect();
        } catch (final TimeoutException e) {
            log.error("TimeOut exception occured executing script. Manually check server configuration");
        }
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

    @Override
    public String getCheckDescription() {

        return "Checking jks files..";
    }

    @Override
    public void doPreCheck() {

    }

}
