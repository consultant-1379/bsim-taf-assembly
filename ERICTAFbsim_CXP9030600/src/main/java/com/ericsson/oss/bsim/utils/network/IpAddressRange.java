/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2014 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.bsim.utils.network;

import static com.ericsson.oss.bsim.utils.network.helper.IpAddressHelper.nextAdjacentIpFrom;

/**
 * @author eannpaa
 */
public class IpAddressRange {

    private IpAddress startingIp;

    private int size;

    public IpAddressRange(final IpAddress startingIp, final int size) {
        this.startingIp = startingIp;
        this.size = size;
    }

    public int getSize() {
        return size;
    }

    public boolean is(final IpVersion ipVersion) {
        return startingIp.getVersion() == ipVersion;
    }

    public IpAddress nextIpAddress() {
        if (isEmpty()) {
            throw new IllegalStateException("range is empty");
        }
        return nextAdjacentIp();
    }

    public boolean isEmpty() {
        return size < 1;
    }

    private IpAddress nextAdjacentIp() {
        final IpAddress nextIp = startingIp;
        startingIp = nextAdjacentIpFrom(startingIp);
        size--;
        return nextIp;
    }
}
