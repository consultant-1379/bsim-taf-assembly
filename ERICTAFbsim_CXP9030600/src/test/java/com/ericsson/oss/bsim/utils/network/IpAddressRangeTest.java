/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2014 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.bsim.utils.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * @author eannpaa
 */
public class IpAddressRangeTest {

    @Test
    public void shouldDeliverOneIpAddressVersion4() {
        final IpAddress startingIp = new IpAddress("192.168.160.1");
        final IpAddressRange ipRange = new IpAddressRange(startingIp, 1);

        assertEquals(startingIp, ipRange.nextIpAddress());
        assertTrue(ipRange.isEmpty());
    }

    @Test
    public void shouldDeliverSequenceIpAddressVersion4() {
        final IpAddress startingIp = new IpAddress("192.168.160.1");
        final IpAddressRange ipRange = new IpAddressRange(startingIp, 2);

        assertEquals(startingIp, ipRange.nextIpAddress());
        assertEquals(new IpAddress("192.168.160.2"), ipRange.nextIpAddress());
        assertTrue(ipRange.isEmpty());
    }

    @Test
    public void shouldDeliverOneIpAddressVersion6() {
        final IpAddress startingIp = new IpAddress("2001:1b70:82a1:0103::64:240");
        final IpAddressRange ipRange = new IpAddressRange(startingIp, 1);

        assertEquals(startingIp, ipRange.nextIpAddress());
        assertTrue(ipRange.isEmpty());
    }

    @Test
    public void shouldDeliverSequenceIpAddressVersion6() {
        final IpAddress startingIp = new IpAddress("2001:1b70:82a1:103::64:240");
        final IpAddressRange ipRange = new IpAddressRange(startingIp, 2);

        assertEquals(startingIp, ipRange.nextIpAddress());
        assertEquals(new IpAddress("2001:1b70:82a1:103::64:241"), ipRange.nextIpAddress());
        assertTrue(ipRange.isEmpty());
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowAnExceptionWhenIpRangeIsEmpty() {
        final IpAddressRange ipRange = new IpAddressRange(new IpAddress("192.168.160.1"), 1);
        ipRange.nextIpAddress();
        ipRange.nextIpAddress();
    }
}
