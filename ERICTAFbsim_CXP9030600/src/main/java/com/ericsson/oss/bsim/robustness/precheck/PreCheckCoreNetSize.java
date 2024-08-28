package com.ericsson.oss.bsim.robustness.precheck;

import static org.testng.Assert.assertTrue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;
import com.ericsson.cifwk.taf.tools.cli.Shell;
import com.ericsson.oss.bsim.data.model.NodeType;
import com.ericsson.oss.bsim.getters.api.BsimApiGetter;

/**
 * @author ejomclo
 *         <p>
 *         Class checks core net size.If core net size is zero, a script is run and another check is made to ensure script executed
 *         correctly/p>
 */
public class PreCheckCoreNetSize implements IBsimPreChecker {

    private static Logger log = Logger.getLogger(PreCheckCoreNetSize.class);

    private static CLICommandHelper ossMasterCLICommandHelper = BsimApiGetter.getCLICommandHelper(BsimApiGetter.getHostMaster());

    private static final String CHECK_CORE_NET_SIZE = "grep -i \"core_net\" /ericsson/config/system.ini";

    private static final String EXECUTE_SCRIPT_COMMAND = "/opt/ericsson/sck/bin/config_ossrc_server -a -core 2";

    private static final String RETURNED_FROM_SCRIPT = "Enter YES to update or NO to quit with no update ";

    @Override
    public boolean doPreCheck(final NodeType nodeType) {

        return setCoreNetSizeIfRequired();
    }

    private boolean setCoreNetSizeIfRequired() {
        boolean check = checkCoreNetSize();
        if (!check) {
            executeScript();
            check = checkCoreNetSize();
        }
        return check;
    }

    @Override
    @Test(groups = { "common.precheck" }, alwaysRun = true)
    public void doPreCheck() {

        assertTrue(setCoreNetSizeIfRequired());
    }

    /**
     * <p>
     * Method executes the command on server and firstly checks if returned string is of an expected pattern and then determines value for
     * core net site
     * </p>
     * 
     * @return boolean
     */
    public boolean checkCoreNetSize() {

        log.info("<font color=purple><B>Start to check Core Net Size......</B></font>");

        String returnedString = ossMasterCLICommandHelper.simpleExec(CHECK_CORE_NET_SIZE);
        Pattern pattern = Pattern.compile("core_net_size=\\d+");
        Matcher matcher = pattern.matcher(returnedString);

        while (matcher.find()) {

            String check = matcher.group();
            char netSize = check.charAt(check.length() - 1);
            if (netSize == 0) {
                return false;
            }

        }

        log.info("Finished checking core net size......");
        return true;
    }

    /**
     * <p>
     * Executes script on server to set core net size
     * </p>
     */
    private void executeScript() {
        log.warn("Core Net site is 0, running script to create...");
        Shell shell = ossMasterCLICommandHelper.openShell();
        shell.writeln(EXECUTE_SCRIPT_COMMAND);
        shell.expect(RETURNED_FROM_SCRIPT, 180);
        shell.writeln("YES");
        log.info(shell.expect("OSSRC Server Configuration - Complete - Stage 8", 180));
        log.info("Script finished executing");
    }

    @Override
    public String getCheckDescription() {

        return "checking Number Of Pico Nodes on OSSRC...";
    }
}
