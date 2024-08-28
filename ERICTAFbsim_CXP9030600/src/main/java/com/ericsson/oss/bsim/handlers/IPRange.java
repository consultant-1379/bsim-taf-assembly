/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2014 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.bsim.handlers;

import com.ericsson.cifwk.taf.data.Host;

/**
 * @author ecilosh
 */
public class IPRange {

    private Host host;

    private String startingIP;

    private int size;

    private boolean isIPv6;

    public Host getHost() {
        return host;
    }

    public void setHost(final Host host) {
        this.host = host;
    }

    public String getStartingIP() {
        return startingIP;
    }

    public void setStartingIP(final String startingIP) {
        this.startingIP = startingIP;
    }

    public int getSize() {
        return size;
    }

    public void setSize(final int size) {
        this.size = size;
    }

    public boolean isIPv6() {
        return isIPv6;
    }

    public void setIPv6(final boolean isIPv6) {
        this.isIPv6 = isIPv6;
    }

}
