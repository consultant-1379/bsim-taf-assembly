/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2016 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.bsim.data.model;

/**
 * @author xnehmis
 */
public enum SpitfireDomain {

    TRANSPORT("Transport");

    private String transport;

    private SpitfireDomain(final String transport) {
        this.transport = transport;
    }

    public String getTransport() {
        return transport;
    }

    public void setTransport(final String transport) {
        this.transport = transport;
    }

    public static SpitfireDomain spitfireDomainTypeFromString(final String domain) {

        if (domain.equalsIgnoreCase("transport")) {
            return TRANSPORT;
        } else {
            return null;
        }
    }

    @Override
    public String toString() {

        return transport;
    }
}
