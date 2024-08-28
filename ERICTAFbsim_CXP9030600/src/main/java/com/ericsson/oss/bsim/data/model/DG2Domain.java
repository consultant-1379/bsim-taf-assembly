
/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.bsim.data.model;

/**
 * @author xsonaro
 */
public enum DG2Domain {

    LRAN("LRAN"),
    WRAN("WRAN"),
    W_L_RAN("W-L-RAN"),
    TRANSPORT("Transport"),
    GRAN("GRAN"),
    L_G_RAN("L-G-RAN"),
    W_G_RAN("W-G-RAN"),
    W_L_G_RAN("W-L-G-RAN");

    private String domain;

    private DG2Domain(final String domain) {

        this.domain = domain;
    }

    @Override
    public String toString() {

        return domain;
    }

    public static DG2Domain dg2DomainTypeFromString(final String domain) {

        if ("LRAN".equals(domain)) {
            return LRAN;
        } else if ("WRAN".equals(domain)) {
            return WRAN;
        } else if ("W-L-RAN".equals(domain)) {
            return W_L_RAN;
        } else if ("GRAN".equals(domain)) {
            return GRAN;
        } else if ("Transport".equals(domain)) {
            return TRANSPORT;
        } else if ("W-G-RAN".equals(domain)) {
            return W_G_RAN;
        } else if ("L-G-RAN".equals(domain)) {
            return L_G_RAN;
        } else if ("W-L-G-RAN".equals(domain)) {
            return W_L_G_RAN;
        } else {
            return null;
        }
    }

}

