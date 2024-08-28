package com.ericsson.oss.bsim.robustness.precheck;

import static org.testng.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.HostType;
import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;
import com.ericsson.oss.bsim.data.model.NodeType;
import com.ericsson.oss.bsim.getters.api.BsimApiGetter;

/**
 * @author ejomclo
 *         <p>
 *         Class checks the official packages that the test cases are being ran against. Allows better tracking of packages used in KGB/CDB
 *         test runs./p>
 */
public class PreCheckOfficialPackages implements IBsimPreChecker {

    private static Logger log = Logger.getLogger(PreCheckOfficialPackages.class);

    private static CLICommandHelper ossMasterCLICommandHelper = BsimApiGetter.getCLICommandHelper(BsimApiGetter.getHostMaster());

    private final Host serverHost = DataHandler.getHostByType(HostType.RC);

    @Override
    public boolean doPreCheck(final NodeType nodeType) {

        return checkOfficialBsimPackage() && checkOfficialTengPackage();
    }

    /**
     * @return boolean
     */
    private boolean checkOfficialBsimPackage() {
        log.info("Checking packages on server " + serverHost.getIp());
        final String bsimPkg = ossMasterCLICommandHelper.simpleExec("pkginfo -l ERICbsim");
        if (bsimPkg.contains("VERSION") && bsimPkg.contains("STATUS:  completely installed")) {
            log.info("Bsim official package details:\n" + bsimPkg);
            return true;
        } else if (bsimPkg.contains("ERROR") || bsimPkg.contains("STATUS:  partially installed")) {
            log.error("Bsim package not found on server");
            return false;
        }
        return false;

    }

    /**
     * @return boolean
     */
    private boolean checkOfficialTengPackage() {
        final String tengPkg = ossMasterCLICommandHelper.simpleExec("pkginfo -l ERICteng");
        if (tengPkg.contains("VERSION") && tengPkg.contains("STATUS:  completely installed")) {
            log.info("TENG official package details:\n" + tengPkg);
            return true;
        } else if (tengPkg.contains("ERROR") || tengPkg.contains("STATUS:  partially installed")) {
            log.error("TENG package not found on server");
            return false;
        }
        return false;
    }

    @Override
    @Test(groups = { "common.precheck" }, alwaysRun = true)
    @TestId(id = "OSS-77349_Func_1", title = "PreCheck the official packages of BSIM and TENG before executing TAF Test Case for BSIM")
    @Context(context = { Context.API })
    public void doPreCheck() {

        assertTrue(checkOfficialBsimPackage() && checkOfficialTengPackage());
    }

    @Override
    public String getCheckDescription() {

        return "checking official packages on server...";
    }
}

