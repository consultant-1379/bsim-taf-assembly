/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2014 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.bsim.utils.ftp;

/**
 * @author eannpaa
 */
public class FtpServiceNotFoundException extends Exception {

    private static final long serialVersionUID = 1392753705457253329L;

    public FtpServiceNotFoundException() {
    }

    public FtpServiceNotFoundException(final String message) {
        super(message);
    }
}
