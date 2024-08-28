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
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TestCase;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.DataDriven;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.Output;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.tools.cli.CLI;
import com.ericsson.cifwk.taf.tools.cli.Shell;
import com.ericsson.cifwk.taf.tools.cli.Terminal;
import com.ericsson.cifwk.taf.tools.cli.TimeoutException;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;

/**
 * @author epaudwa
 *         OSS-18004 - implement <bsim bind> run in any directory
 *         OSS-19661 - TAF specific task for CLItools
 */

public class BsimCliRunAnywhere extends TorTestCaseHelper implements TestCase {

    private static Logger log = Logger.getLogger(BsimCliRunAnywhere.class);

    private Host masterHost = null;

    private CLI cli = null;

    private final long timeout = 15;

    @BeforeSuite
    public void Setup() throws TimeoutException {

        masterHost = HostGroup.getOssmaster(); // DataHandler.getHostByName("ossmaster");
        cli = new CLI(masterHost, HostGroup.getOssmaster().getNmsadmUser());
    }

    @Test
    @DataDriven(name = "BsimCliRunAnywhere")
    @Context(context = { Context.API })
    public void verifyBsimCliRunsAnywhere(
            @Input("tcID") final String tcID,
            @Input("tcTitle") final String tcTitle,
            @Input("directory") final String directory,
            @Input("command") final String command,
            @Output("output") final String output) throws TimeoutException {

        log.info("------------------- verifyBsimCliRunAnywhere -------------------");

        final Shell shell = cli.openShell(Terminal.VT100);
        final String pwdDirectory = directory.substring(0, directory.length() - 1);

        setTestcase(tcID, tcTitle);

        try {
            shell.writeln("cd" + " " + directory);
            shell.read();
            shell.writeln("pwd");
            if (shell.expect(pwdDirectory, timeout).contains(pwdDirectory)) {
                shell.writeln(command);
                final String response = shell.expect(output, timeout);
                assertTrue(response.contains(output));
            }
            // outputValues(directory, pwdDirectory, command, output);
        } catch (final TimeoutException e) {
            log.info("verifyBsimCliRunsAnywhere >> OSS-18004 : TimeoutException : command did not execute properly --> cd " + directory + " : " + command);
            outputValues(directory, pwdDirectory, command, output);
            log.debug("TimeoutException : " + e);
        } catch (final Exception e) {
            log.info("verifyBsimCliRunsAnywhere >> OSS-18004 : Exception : command did not execute properly --> cd " + directory + " : " + command);
            outputValues(directory, pwdDirectory, command, output);
            log.debug("Exception " + e);
        }
        shell.writeln("exit");
        shell.expectClose();
        assertTrue(shell.isClosed());
        final int result = shell.getExitValue();
        shell.disconnect();
        if (result != 0) {
            log.info("verifyBsimCliRunsAnywhere >> OSS-18004 : error! - command did not execute properly --> cd " + directory + " : " + command);
            outputValues(directory, pwdDirectory, command, output);
        } else {
            log.info("verifyBsimCliRunsAnywhere >> OSS-18004 : done! - cd dir : command --> cd " + directory + " : " + command);
        }
    }

    @Test
    @DataDriven(name = "verifyBsimCliRunAnywhereInvalidCommand")
    @Context(context = { Context.API })
    public void verifyBsimCliRunAnywhereInvalidCommand(
            @Input("tcID") final String tcID,
            @Input("tcTitle") final String tcTitle,
            @Input("directory") final String directory,
            @Input("command") final String command,
            @Output("output") final String output) throws TimeoutException {

        log.info("------------------- verifyBsimCliRunAnywhereInvalidCommand -------------------");

        final Shell shell = cli.openShell(Terminal.VT100);
        final String pwdDirectory = directory.substring(0, directory.length() - 1);

        setTestcase(tcID, tcTitle);

        shell.writeln("cd" + " " + directory);
        shell.read();
        shell.writeln("pwd");
        if (shell.expect(pwdDirectory, timeout).contains(pwdDirectory)) {
            shell.writeln(command);
            assertFalse(shell.read().contains(output));
        }

        // outputValues(directory, pwdDirectory, command, output);

        shell.writeln("exit");
        shell.expectClose();
        assertTrue(shell.isClosed());
        final int result = shell.getExitValue();
        shell.disconnect();
        if (result != 0) {
            log.info("verifyBsimCliRunAnywhereInvalidCommand >> OSS-18004 : error! - command did not execute properly --> cd " + directory + " : " + command);
            outputValues(directory, pwdDirectory, command, output);
        } else {
            log.info("verifyBsimCliRunAnywhereInvalidCommand >> OSS-18004 : done! - cd dir : command --> cd " + directory + " : " + command);
        }
    }

    @Test
    @DataDriven(name = "verifyBsimCliRunAnywhereInvalidDirectory")
    @Context(context = { Context.API })
    public void verifyBsimCliRunAnywhereInvalidDirectory(
            @Input("tcID") final String tcID,
            @Input("tcTitle") final String tcTitle,
            @Input("directory") final String directory,
            @Input("command") final String command,
            @Output("output") final String output) throws TimeoutException {

        log.info("------------------- verifyBsimCliRunAnywhereInvalidCommand -------------------");

        final Shell shell = cli.openShell(Terminal.VT100);
        final String pwdDirectory = directory.substring(0, directory.length() - 1);

        setTestcase(tcID, tcTitle);

        shell.writeln("cd" + " " + directory);
        shell.read();
        shell.writeln("pwd");
        assertFalse(shell.read().contains(pwdDirectory));
        shell.writeln(command);
        assertTrue(shell.read().contains(output));

        // outputValues(directory, pwdDirectory, command, output);

        shell.writeln("exit");
        shell.expectClose();
        assertTrue(shell.isClosed());
        final int result = shell.getExitValue();
        shell.disconnect();
        if (result != 0) {
            log.info("verifyBsimCliRunAnywhereInvalidDirectory >> OSS-18004 : error! - command did not execute properly --> cd " + directory + " : " + command);
            outputValues(directory, pwdDirectory, command, output);
        } else {
            log.info("verifyBsimCliRunAnywhereInvalidDirectory >> OSS-18004 : done! - cd dir : command --> cd " + directory + " : " + command);
        }
    }

    private void outputValues(final String dir, final String pdir, final String cmd, final String op) {

        log.info("------------------- outputValues -------------------");
        log.info("     directory        : " + dir);
        log.info("     'pwd' directory  : " + pdir);
        log.info("     command          : " + cmd);
        log.info("     expected output  : " + op);
        log.info("------------------- outputValues -------------------");
    }
}
