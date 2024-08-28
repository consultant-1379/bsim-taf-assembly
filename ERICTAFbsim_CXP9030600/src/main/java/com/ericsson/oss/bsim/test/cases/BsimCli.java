/*------------------------------------------------------------------------
 *
 *
 *      COPYRIGHT (C)                   ERICSSON RADIO SYSTEMS AB, Sweden
 *
 *      The  copyright  to  the document(s) herein  is  the property of
 *      Ericsson Radio Systems AB, Sweden.
 *
 *      The document(s) may be used  and/or copied only with the written
 *      permission from Ericsson Radio Systems AB  or in accordance with
 *      the terms  and conditions  stipulated in the  agreement/contract
 *      under which the document(s) have been supplied.
 *
 *------------------------------------------------------------------------
 */
package com.ericsson.oss.bsim.test.cases;

import org.apache.log4j.Logger;
import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TestCase;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.DataDriven;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.Output;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;
import com.ericsson.oss.bsim.getters.api.BsimApiGetter;
import com.ericsson.oss.bsim.operators.BsimCliOperator;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;

public class BsimCli extends TorTestCaseHelper implements TestCase {
    private static Logger log = Logger.getLogger(BsimCli.class);

    @TestId(id = "OSS-77350", title = "Test Command")
    @Test(groups = { "KGB" })
    public void testCli() {
        final String command = "echo 'This is a hard-coded test case for command user story.'";

        final Host masterHost = HostGroup.getOssmaster(); // DataHandler.getHostByName("ossmaster");
        final CLICommandHelper executor = BsimApiGetter.getCLICommandHelper(masterHost);

        final String respVal = executor.simpleExec(command);

        log.info(respVal);
        // parse the respVal for check
    }

    @DataDriven(name = "Bsim_Cli_Help")
    @Context(context = { Context.API })
    @Test(groups = { "KGB" })
    public void verifyHelpCommandIsValid(
            @Input("command") final String command,
            @Input("args") final String args,
            @Output("output") final String output,
            @TestId @Input("tcID") final String tcID,
            @Input("tcTITLE") final String tcTITLE,
            final String tcDesc) throws Exception {

        setTestcase(tcID, tcTITLE);

        final BsimCliOperator bsimCliOperator = new BsimCliOperator();

        final String response = bsimCliOperator.runHelpCommand(command, args, output);
        // parse the response value for check

        assertTrue(response.contains(output));

    }

}
