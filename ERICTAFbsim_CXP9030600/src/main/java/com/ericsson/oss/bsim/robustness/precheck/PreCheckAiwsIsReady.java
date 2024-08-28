package com.ericsson.oss.bsim.robustness.precheck;

import static org.testng.Assert.assertTrue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;
import com.ericsson.oss.bsim.data.model.NodeType;
import com.ericsson.oss.bsim.getters.api.BsimApiGetter;

/**
 * @author ejomclo
 *         <p>
 *         Class checks if AIWS is ready. If not, it prepares AIWS./p>
 */
public class PreCheckAiwsIsReady implements IBsimPreChecker {

    private static final String INFRA_SERVER_SCRIPT_EXECUTE_COMMAND = "/opt/ericsson/secinst/bin/config.sh -p ERICaiws";

    private static final String CHECK_AIWS_CONFIGURATION = "/opt/ericsson/nms_cif_pas/bin/pastool -getsubtree com.ericsson.oss.aiws";

    private static final String ERIC_CAIWS_WAS_NOT_FOUND = "\"ERICaiws\" was not found";

    private static final String CHECK_IF_AIWS_PACKAGE_IS_INSTALLED = "pkginfo -l ERICaiws";

    private static Logger log = Logger.getLogger(PreCheckAiwsIsReady.class);

    private static CLICommandHelper ossMasterCLICommandHelper = BsimApiGetter.getCLICommandHelper(BsimApiGetter.getHostMaster());

    private static CLICommandHelper omsasCLICommandHelper = BsimApiGetter.getCLICommandHelper(BsimApiGetter.getHostOmsas());

    private static CLICommandHelper infraServerCLICommandHelper = BsimApiGetter.getCLICommandHelper(BsimApiGetter.getHostInfraServer());

    @Override
    public boolean doPreCheck(final NodeType nodeType) {
        return checkAiwsReadyness();
    }

    @Override
    @Test(groups = { "common.precheck" }, alwaysRun = true)
    public void doPreCheck() {

        assertTrue(checkAiwsReadyness());
    }

    /**
     * <p>
     * Method checks if AIWS packages are installed, then checks AIWS configuration
     * </p>
     *
     * @return boolean
     */
    public boolean checkAiwsReadyness() {

        log.info("<font color=purple><B>Start to check if AIWS is ready......</B></font>");

        if (!checkIfAiwsPackagesInstalled()) {
            log.error("One or both packages are not installed. Check if AIWS packages are installed on on OMSAS and Infra servers.");
            return false;
        }
        if (!checkAiwsConfiguration()) {
            log.warn("AIWS is not configured, running configuration scripts");
            configureInfra();

        }
        if (!checkAiwsConfiguration()) {
            log.error("Failed to configure AIWS, test failure");
            return false;
        }

        log.info("AIWS is ready. Precheck test passed");
        return true;
    }

    private boolean checkIfAiwsPackagesInstalled() {
        final String returnedOmsas = omsasCLICommandHelper.simpleExec(CHECK_IF_AIWS_PACKAGE_IS_INSTALLED);
        log.info(returnedOmsas.trim());
        final String returnedInfra = infraServerCLICommandHelper.simpleExec(CHECK_IF_AIWS_PACKAGE_IS_INSTALLED);
        log.info(returnedInfra.trim());
        if (returnedOmsas.contains(ERIC_CAIWS_WAS_NOT_FOUND) || returnedInfra.contains(ERIC_CAIWS_WAS_NOT_FOUND)) {
            return false;
        }
        return true;
    }

    private void configureInfra() {

        log.info("Running script on Infra server...");
        infraServerCLICommandHelper.simpleExec(INFRA_SERVER_SCRIPT_EXECUTE_COMMAND);

    }

    private boolean checkAiwsConfiguration() {

        log.info("Checking Aiws Configuration");
        final String returned = ossMasterCLICommandHelper.simpleExec(CHECK_AIWS_CONFIGURATION);
        log.info(returned.trim());
        final Pattern pattern = Pattern.compile("\\d{1,3}.\\d{1,3}.\\d{1,3}.\\d{1,3}:\\d{1,4}");
        final Matcher matcher = pattern.matcher(returned);
        int matches = 0;
        while (matcher.find()) {
            ++matches;

        }

        return matches == 4;
    }

    @Override
    public String getCheckDescription() {

        return "checking AIWS readyness...";
    }
}
