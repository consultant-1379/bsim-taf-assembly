package com.ericsson.oss.bsim.robustness.precheck;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;
import com.ericsson.cifwk.taf.tools.cli.Shell;
import com.ericsson.cifwk.taf.tools.cli.TimeoutException;
import com.ericsson.oss.bsim.data.model.NodeType;
import com.ericsson.oss.bsim.getters.api.BsimApiGetter;

/**
 * @author ejomclo
 */
public class PreCheckNetconfJksFile implements IBsimPreChecker {

    private static Logger log = Logger.getLogger(PreCheckNetconfJksFile.class);

    private static CLICommandHelper ossMasterCLICommandHelper = BsimApiGetter.getCLICommandHelper(BsimApiGetter.getHostMaster());

    private static CLICommandHelper omsasCLICommandHelper = BsimApiGetter.getCLICommandHelper(BsimApiGetter.getHostOmsas());

    private static String GENERATE_NETCONF_JKS_FILE = "/opt/ericsson/secinst/bin/config.sh";

    private static final int EXPECT_TIMEOUT_PERIOD = 180;

    private final Shell omsasShell = omsasCLICommandHelper.openShell();

    @Override
    public boolean doPreCheck(final NodeType nodeType) {

        return createNetconfJksFileIfRequired();
    }

    public boolean createNetconfJksFileIfRequired() {

        log.info("Beginning check for netconf jks. If file does not exist, an attempt will be made to generate it.");
        if (checkNetconfJKSFileExists()) {
            return true;
        }
        generateNetconfJks();
        if (checkNetconfJKSFileExists()) {
            return true;
        }
        log.error("Netconf jks file does not exist. Generate netconf jks script failed, manually check server configuration. Test Failure");
        return false;
    }

    @Override
    @Test(groups = { "common.precheck" }, alwaysRun = true)
    public void doPreCheck() {
        Assert.assertTrue(createNetconfJksFileIfRequired());
    }

    private boolean checkNetconfJKSFileExists() {

        log.info("<font color=purple><B>9> Start to check if Netconf JKS File is generated ......</B></font>");

        final String checkForNetconfJksFile = "ls -l /opt/ericsson/nms_netconf/security/";
        final String jksFileName = "nm_netconf.jks";

        if (ossMasterCLICommandHelper.simpleExec(checkForNetconfJksFile).contains(jksFileName)) {
            log.info(jksFileName + " exists");
            return true;
        }
        return false;

    }

    private void generateNetconfJks() {
        log.info("Generating netconf jks, running script...");
        omsasShell.writeln(GENERATE_NETCONF_JKS_FILE);
        try {
            omsasShell.expect("Enter password for", 540);
            omsasShell.writeln("ldappass");
            omsasShell.expect("Select ldap domain", EXPECT_TIMEOUT_PERIOD);
            omsasShell.writeln("2");
            omsasShell.expect("Enter new password", EXPECT_TIMEOUT_PERIOD);
            omsasShell.writeln("ldappass");
            omsasShell.expect("Confirm password for", EXPECT_TIMEOUT_PERIOD);
            omsasShell.writeln("ldappass");
            omsasShell.expect("Do you wish to generate \"nm_netconf.jks\" for \"ossmaster\"", EXPECT_TIMEOUT_PERIOD);
            omsasShell.writeln("y");
            omsasShell.expect("root@ossmaster's password: ", EXPECT_TIMEOUT_PERIOD);
            omsasShell.writeln("shroot12");
            omsasShell.expect("JKS file is successfully created ", EXPECT_TIMEOUT_PERIOD);

        } catch (final TimeoutException e) {
            log.error("TimeOut exception occured executing script. Manually check server configuration");
        }

    }

    @Override
    public String getCheckDescription() {

        return "Checking jks files..";
    }

    @AfterClass
    public void tearDown() {
        omsasShell.disconnect();
    }

}
