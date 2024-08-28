/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2014 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.bsim.robustness.precheck;

import java.io.File;

import org.apache.log4j.Logger;

import com.ericsson.oss.bsim.data.model.NodeType;
import com.ericsson.oss.bsim.utils.file.LocalTempFileConstants;

/**
 * This Pre Check is required to create a temporary directory used by the TAF test cases.
 * Fo example the temporary directory is used in the creation of temporary kertayle scripts for execution during integration test cases.
 * Note:The directory is removed after test case execution.
 *
 * @author ebrimah
 */
public class PreCheckLocalTempDir implements IBsimPreChecker {

    private static Logger log = Logger.getLogger(PreCheckLocalTempDir.class);

    @Override
    public boolean doPreCheck(final NodeType nodeType) {
        return createTempDir();
    }

    @Override
    public String getCheckDescription() {
        return "create temp directory..." + LocalTempFileConstants.getLocalTempDirName();
    }

    @Override
    public void doPreCheck() {
        createTempDir();
    }

    public boolean createTempDir() {

        boolean tempDirExists = false;

        final File tempDir = new File(LocalTempFileConstants.getLocalTempDirName());
        tempDir.deleteOnExit();

        if (tempDir.exists()) {
            log.info("tempDir exists: " + tempDir.getAbsolutePath());
            tempDirExists = true;
        } else {

            log.info("tempDir does not exist, attempting to create dir: " + tempDir.getAbsolutePath());

            if (tempDir.mkdir()) {
                log.info("tempDir created: " + tempDir.getAbsolutePath());
                tempDirExists = true;
            } else {
                log.info("tempDir could not be created: " + tempDir.getAbsolutePath());
            }
        }

        return tempDirExists;
    }
}
