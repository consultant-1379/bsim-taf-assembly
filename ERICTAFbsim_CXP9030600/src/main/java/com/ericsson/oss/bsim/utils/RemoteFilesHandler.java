/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2014 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.bsim.utils;

import java.util.List;

import org.apache.log4j.Logger;

import com.ericsson.cifwk.taf.tools.cli.handlers.impl.RemoteObjectHandler;
//import com.ericsson.cifwk.taf.handlers.RemoteFileHandler;

/**
 * Utility Class for handling files
 * 
 * @author efitrob
 */
public class RemoteFilesHandler {

    private static Logger log = Logger.getLogger(RemoteFilesHandler.class);

    public RemoteFilesHandler() {

    }

    /**
     * Transfer local files to a remote folder
     * 
     * @param remoteFileHandler
     * @param remoteFolder
     * @param filesToTransfer
     * @return true if files transferred successfully, false if file transfer failed
     */
    public boolean transferLocalFilesToRemote(final RemoteObjectHandler remoteFileHandler, final String remoteFolder, final List<String> filesToTransfer) {

        boolean filesTransferred = false;

        for (final String localFile : filesToTransfer) {

            filesTransferred = false;
            log.info("Attempting to transfer file: " + localFile);

            remoteFileHandler.copyLocalFileToRemote(localFile, remoteFolder);

            String remoteFileName = "";

            if (localFile.contains("\\")) {

                remoteFileName = remoteFolder + localFile.substring(localFile.lastIndexOf("\\") + 1, localFile.length());
            } else if (localFile.contains("/")) {

                remoteFileName = remoteFolder + localFile.substring(localFile.lastIndexOf("/") + 1, localFile.length());
            } else {

                remoteFileName = remoteFolder + localFile;
            }

            final boolean isFileTransferred = remoteFileHandler.remoteFileExists(remoteFileName);

            if (isFileTransferred) {
                log.info("File " + localFile + " transfer to : " + remoteFolder + " is SUCCESSFUL");
                filesTransferred = true;
            } else {
                log.error("File " + localFile + " transfer to : " + remoteFolder + " has FAILED. No further files will be transferred");
                break;
            }
        }
        return filesTransferred;
    }

}
