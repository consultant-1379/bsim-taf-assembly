package com.ericsson.oss.bsim.robustness.precheck;

import static org.testng.Assert.assertTrue;
import org.apache.log4j.Logger;
import org.testng.annotations.Test;
import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;
import com.ericsson.cifwk.taf.tools.cli.Shell;
import com.ericsson.oss.bsim.data.model.NodeType;
import com.ericsson.oss.bsim.getters.api.BsimApiGetter;

/**
 * @author ejomclo
 *         <p>
 *         Class creates AIF service/p>
 *         <p>
 *         Class not to be added to prechecks yet, waiting on clarification
 *         </p>
 */
public class PreCheckAifServiceCreation implements IBsimPreChecker {

    private static final String PASSWORD = "osstest123!";

    private static final String YES = "yes";

    private static Logger log = Logger.getLogger(PreCheckAifServiceCreation.class);

    private static CLICommandHelper ossMasterCLICommandHelper = BsimApiGetter.getCLICommandHelper(BsimApiGetter.getHostMaster());

    private static final String EXECUTE_SCRIPT_COMMAND = "/opt/ericsson/nms_bismrs_mc/bin/configure_smrs.sh add aif";

    private static final int EXPECT_TIMEOUT_PERIOD = 180;

    @Override
    public boolean doPreCheck(final NodeType nodeType) {

        return createAifService();
    }

    @Override
    @Test(groups = { "common.precheck" }, alwaysRun = true)
    public void doPreCheck() {

        assertTrue(createAifService());
    }

    /**
     * <p>
     * Method executes script on master server to create AIF service
     * </p>
     * 
     * @return boolean
     */
    public boolean createAifService() {

        log.info("<font color=purple><B>Creating AIF Service......</B></font>");
        Shell shell = ossMasterCLICommandHelper.openShell();
        shell.writeln(EXECUTE_SCRIPT_COMMAND);
        shell.expect("Enter Network Type", EXPECT_TIMEOUT_PERIOD);
        shell.writeln("CORE");
        shell.expect("What is the name for this user", EXPECT_TIMEOUT_PERIOD);
        shell.writeln("CAIFosstest");
        shell.expect("What is the password for this user", EXPECT_TIMEOUT_PERIOD);
        shell.writeln(PASSWORD);
        shell.expect("Please confirm the password for this user", EXPECT_TIMEOUT_PERIOD);
        shell.writeln(PASSWORD);
        shell.expect("Would you like to create autoIntegration FtpService for that user", EXPECT_TIMEOUT_PERIOD);
        shell.writeln(YES);
        shell.expect("Associate AIF user with", EXPECT_TIMEOUT_PERIOD);
        shell.writeln("1");
        shell.expect("Associate AIF user with", EXPECT_TIMEOUT_PERIOD);
        shell.writeln(YES);
        return shell.expect("INFO Starting ARNE import of XML file ", EXPECT_TIMEOUT_PERIOD).contains("CAIFosstest has been successfully added");

    }

    @Override
    public String getCheckDescription() {

        return "Creating AIF Service...";
    }
}
