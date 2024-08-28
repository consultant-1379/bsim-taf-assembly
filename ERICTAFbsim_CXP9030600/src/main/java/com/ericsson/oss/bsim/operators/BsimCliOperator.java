package com.ericsson.oss.bsim.operators;

import com.ericsson.oss.bsim.operators.api.BsimCliApiOperator;

public class BsimCliOperator {

    BsimCliApiOperator bsimCliApiOperator = new BsimCliApiOperator();

    public String runHelpCommand(final String command, final String args, final String output) {

        return bsimCliApiOperator.runHelpCommand(command, args, output);
    }

    public void copyFileToRemoteHost(final String localFilePath, final String remoteFilePath) {
        bsimCliApiOperator.copyFileToRemoteHost(localFilePath, remoteFilePath);
    }

    public void deleteRemoteFile(final String remoteFilePath) {
        bsimCliApiOperator.deleteRemoteFile(remoteFilePath);
    }

    public String executeCommand(final String command) {
        return bsimCliApiOperator.executeCommand(command);
    }

}
