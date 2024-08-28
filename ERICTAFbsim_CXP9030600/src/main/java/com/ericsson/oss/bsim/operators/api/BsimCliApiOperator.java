package com.ericsson.oss.bsim.operators.api;

import org.apache.log4j.Logger;
import org.testng.Assert;

import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;
import com.ericsson.cifwk.taf.tools.cli.handlers.impl.RemoteObjectHandler;
import com.ericsson.oss.bsim.getters.api.BsimApiGetter;
import com.ericsson.oss.bsim.utils.file.LocalTempFileConstants;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;

public class BsimCliApiOperator {

    private final Logger log = Logger.getLogger(BsimCliApiOperator.class);

    private final RemoteObjectHandler fileHandler = BsimApiGetter.getMasterHostFileHandler();

    private final Host masterHost = HostGroup.getOssmaster();// DataHandler.getHostByName("ossmaster");

    private final CLICommandHelper executor = BsimApiGetter.getCLICommandHelper(masterHost);

    public String runHelpCommand(final String command, final String args, final String output) {

        final String response = executor.simpleExec(command, args);
        System.out.println("Command: " + command + " args: " + args + " output: " + output);
        System.out.println("Response: " + response);

        return response;
    }

    public String executeCommand(final String command) {

        final String response = executor.simpleExec(command);
        return response;
    }

    public void copyFileToRemoteHost(final String localFilePath, final String remoteFilePath) {
        boolean success = true;
        try {
            fileHandler.copyLocalFileToRemote(localFilePath, remoteFilePath, LocalTempFileConstants.getLocalTempDirName());
        } catch (final Exception e) {
            log.error("Exception " + e.getMessage() + " thrown");
            success = false;
        }
        Assert.assertEquals(success, true);
    }

    public void deleteRemoteFile(final String remoteFilePath) {
        boolean success = true;
        try {
            fileHandler.deleteRemoteFile(remoteFilePath);
        } catch (final Exception e) {
            log.error("Exception " + e.getMessage() + " thrown");
            success = false;
        }
        Assert.assertEquals(success, true);

    }

}
