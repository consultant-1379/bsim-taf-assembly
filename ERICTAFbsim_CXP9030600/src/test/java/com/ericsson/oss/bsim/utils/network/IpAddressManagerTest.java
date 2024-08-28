/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2014 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.bsim.utils.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

/**
 * @author eannpaa
 */
public class IpAddressManagerTest {

    @Test
    public void shouldDeliverOneIPv4() {
        final IpAddressManager ipManager = new IpAddressManager(buildIPv4RangeBySize(1));
        assertEquals(new IpAddress("192.168.160.1"), ipManager.getIpAddress());
        assertTrue(ipManager.ipRangeIsEmpty());
    }

    @Test
    public void shouldDeliverOneIPv6() {
        final IpAddressManager ipManager = new IpAddressManager(buildIPv6RangeBySize(1));
        assertEquals(new IpAddress("2001:1b70:82a1:103::64:240"), ipManager.getIpAddress());
        assertTrue(ipManager.ipRangeIsEmpty());
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowAnExceptionWhenIpRangeIsEmpty() {
        final IpAddressManager ipManager = new IpAddressManager(buildIPv4RangeBySize(1));
        ipManager.getIpAddress();
        ipManager.getIpAddress();
    }

    @Test
    public void shouldDeliverManyIPv4() {
        final IpAddressManager ipManager = new IpAddressManager(buildIPv4RangeBySize(3));
        assertEquals(3, ipManager.getIpAddressByQuantity(3).size());
        assertTrue(ipManager.ipRangeIsEmpty());
    }

    @Test
    public void shouldDeliverManyIPv6() {
        final IpAddressManager ipManager = new IpAddressManager(buildIPv6RangeBySize(3));
        assertEquals(3, ipManager.getIpAddressByQuantity(3).size());
        assertTrue(ipManager.ipRangeIsEmpty());
    }

    @Test
    public void shouldDeliverManyIPv4FromDifferentRanges() {
        final IpAddressManager ipManager = new IpAddressManager(buildIPv4Range());
        assertEquals(5, ipManager.getIpAddressByQuantity(5).size());
        assertTrue(ipManager.ipRangeIsEmpty());
    }

    @Test
    public void shouldDeliverManyIPv6FromDifferentRanges() {
        final IpAddressManager ipManager = new IpAddressManager(buildIPv6Range());
        assertEquals(5, ipManager.getIpAddressByQuantity(5).size());
        assertTrue(ipManager.ipRangeIsEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowAnExceptionWhenGetIPv6IntoIPv4Range() {
        final IpAddressManager ipManager = new IpAddressManager(buildIPv4RangeBySize(1));
        assertFalse(ipManager.ipRangeIsEmpty());
        ipManager.getIpAddressByVersion(IpVersion.IPv6);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowAnExceptionWhenGetIPv4IntoIPv6Range() {
        final IpAddressManager ipManager = new IpAddressManager(buildIPv6RangeBySize(3));
        ipManager.getIpAddressByQuantityAndVersion(3, IpVersion.IPv4);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowAnExceptionWhenTryDeliverMoreThanPossible() {
        final IpAddressManager ipManager = new IpAddressManager(buildIPv4RangeBySize(1));
        ipManager.getIpAddress();
        ipManager.getIpAddress();
    }

    private static List<IpAddressRange> buildIPv4RangeBySize(final int size) {
        return Arrays.asList(new IpAddressRange(new IpAddress("192.168.160.1"), size));
    }

    private static List<IpAddressRange> buildIPv4Range() {
        return Arrays.asList(new IpAddressRange(new IpAddress("192.168.160.1"), 2), new IpAddressRange(new IpAddress("192.168.161.10"), 3));
    }

    private static List<IpAddressRange> buildIPv6RangeBySize(final int size) {
        return Arrays.asList(new IpAddressRange(new IpAddress("2001:1b70:82a1:103::64:240"), size));
    }

    private static List<IpAddressRange> buildIPv6Range() {
        return Arrays.asList(new IpAddressRange(new IpAddress("2001:1b70:82a1:0103::64:240"), 2), new IpAddressRange(
                new IpAddress("2001:1b70:82a1:103::64:280"), 3));
    }
}
