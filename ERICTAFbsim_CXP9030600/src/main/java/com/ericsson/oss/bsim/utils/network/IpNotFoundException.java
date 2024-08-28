/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2014 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.bsim.utils.network;

/**
 * @author eannpaa
 */
public class IpNotFoundException extends Exception {

    private static final long serialVersionUID = 2036278897623029646L;

    public IpNotFoundException() {
    }

    public IpNotFoundException(final String message) {
        super(message);
    }
}
