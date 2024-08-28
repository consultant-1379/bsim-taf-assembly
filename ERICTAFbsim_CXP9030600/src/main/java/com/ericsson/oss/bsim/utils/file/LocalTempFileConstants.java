/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2014 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.bsim.utils.file;

/**
 * Holds list of constant properties to be used when working with local temporary files
 */
public final class LocalTempFileConstants {

    /**
     * temp directory name for writing local temporary files
     *
     * @return
     */
    public static String getLocalTempDirName() {
        // hardcode for moment but using property instead of constant allows us to improve implementation
        // and read this from system property/resource etc... if ever required in future.
        return "BsimTempDir";
    }

}
