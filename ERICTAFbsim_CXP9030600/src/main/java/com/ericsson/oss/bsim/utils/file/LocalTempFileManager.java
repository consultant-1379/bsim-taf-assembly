/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2014 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.bsim.utils.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.ericsson.cifwk.taf.assertions.TafAsserts;

/**
 * Can be used for temporary file methods such as creating temporary files
 */
public class LocalTempFileManager {

    private final Logger log;

    final File fileDirectory;

    public LocalTempFileManager() {
        log = Logger.getLogger(LocalTempFileManager.class);
        fileDirectory = new File(LocalTempFileConstants.getLocalTempDirName());
    }

    /**
     * Creates a temporary file that will be deleted on exit in the BsimTempDir directory that will be deleted on exit with the content
     * specified
     *
     * @param fileNamePrefix
     *        Prefix to be added before the timestamp to create the temp file name (e.g. "myScriptName")
     * @param fileNameSuffix
     *        Suffix to be appended after the temp name (e.g the file extension ".xml")
     * @param fileContent
     *        The contents to be written to the file
     * @return
     * @throws IOException
     */
    public File createTempFile(final String fileNamePrefix, final String fileNameSuffix, final String fileContent) throws IOException {

        TafAsserts.assertNotNull(fileNamePrefix);
        TafAsserts.assertNotNull(fileNameSuffix);
        TafAsserts.assertNotNull(fileContent);

        final File tempFileHandler = createTempFileHandler(fileNamePrefix, fileNameSuffix, fileDirectory);

        log.info("Temp file to be created " + tempFileHandler.getAbsolutePath());

        writeContentToFile(fileContent.getBytes(), tempFileHandler);

        log.info("Temp file created " + tempFileHandler.getAbsolutePath());

        return tempFileHandler;
    }

    public File createTempFileHandler(final String fileNamePrefix, final String fileNameSuffix) {
        TafAsserts.assertNotNull(fileNamePrefix);
        TafAsserts.assertNotNull(fileNameSuffix);
        return createTempFileHandler(fileNamePrefix, fileNameSuffix, fileDirectory);
    }

    private void writeContentToFile(final byte[] contentInBytes, final File tempFileHandler) throws FileNotFoundException, IOException {
        final FileOutputStream fileOutputStream = new FileOutputStream(tempFileHandler);

        fileOutputStream.write(contentInBytes);
        fileOutputStream.flush();
        fileOutputStream.close();
    }

    private File createTempFileHandler(final String fileNamePrefix, final String fileNameSuffix, final File fileDirectory) {

        final StringBuilder pathName = new StringBuilder();
        pathName.append(fileDirectory).append(File.separator).append(fileNamePrefix).append(System.currentTimeMillis()).append(fileNameSuffix);

        final File tempFile = new File(pathName.toString());
        tempFile.deleteOnExit();
        return tempFile;
    }
}
