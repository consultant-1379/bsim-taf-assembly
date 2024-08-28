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
 *         Class checks number of PICO nodes configured in OSSRC
 *         </p>
 */
public class PreCheckConfiguredPicoNodesInOSSRC implements IBsimPreChecker {

    private static Logger log = Logger.getLogger(PreCheckConfiguredPicoNodesInOSSRC.class);

    private static CLICommandHelper ossMasterCLICommandHelper = BsimApiGetter.getCLICommandHelper(BsimApiGetter.getHostMaster());

    private static final String CHECK_NUMBER_OF_CONFIGURED_PICO_NODES = "grep -i \"pico\" /ericsson/config/system.ini";

    @Override
    public boolean doPreCheck(final NodeType nodeType) {

        return checkNumberOfPicoNodes();
    }

    @Override
    @Test(groups = { "common.precheck" }, alwaysRun = true)
    public void doPreCheck() {
        assertTrue(checkNumberOfPicoNodes());
    }

    /**
     * <p>
     * Method executes the command on server and firstly checks if returned string is of an expected pattern and then determines value for
     * number of wcdma pico and lte pico nodes. Number of matches should be two and number of nodes should be at least one or greater
     * </p>
     * 
     * @return boolean
     */
    public boolean checkNumberOfPicoNodes() {

        log.info("<font color=purple><B>Start to check Number Of Pico Nodes on OSSRC......</B></font>");

        String returnedString = ossMasterCLICommandHelper.simpleExec(CHECK_NUMBER_OF_CONFIGURED_PICO_NODES);
        Pattern pattern = Pattern.compile("number_pico_\\w+_cells=\\d+");
        Matcher matcher = pattern.matcher(returnedString);
        int countMatches = 0;
        while (matcher.find()) {
            ++countMatches;
            String check = matcher.group();
            char numberOfNodes = check.charAt(check.length() - 1);
            if (numberOfNodes < 1) {
                return false;
            }

        }

        log.info("Finished checking Number Of Pico Nodes on OSSRC......");
        return countMatches == 2;
    }

    @Override
    public String getCheckDescription() {

        return "checking Number Of Pico Nodes on OSSRC...";
    }

}
