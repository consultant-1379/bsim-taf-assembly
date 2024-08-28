/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2014 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.bsim.utils.network;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

/**
 * @author eannpaa
 */
public class IpAddressManager {

    private static final int HEAD = 0;

    private final List<IpAddressRange> freeRanges;

    public IpAddressManager(final List<IpAddressRange> freeRanges) {
        this.freeRanges = new ArrayList<>(freeRanges);
    }

    public IpAddress getIpAddress() {
        return findIpByVersion(null);
    }

    public IpAddress getIpAddressByVersion(final IpVersion version) {
        return findIpByVersion(version);
    }

    public Set<IpAddress> getIpAddressByQuantity(final int quantityRequested) {
        return findIpsByQuantityAndVersion(quantityRequested, null);
    }

    public Set<IpAddress> getIpAddressByQuantityAndVersion(final int quantityRequested, final IpVersion ipVersion) {
        return findIpsByQuantityAndVersion(quantityRequested, ipVersion);
    }

    public boolean ipRangeIsEmpty() {
        return freeRanges.isEmpty();
    }

    public Set<IpAddress> findIpsByQuantityAndVersion(final int quantityRequested, final IpVersion ipVersion) {
        if (numberOfPossibleIpsBy(ipVersion) < quantityRequested) {
            throw new IllegalArgumentException("cannot create this quantity IPs");
        }

        final Set<IpAddress> ipAddresses = new HashSet<>();
        for (int i = 0; i < quantityRequested; i++) {
            ipAddresses.add(findIpByVersion(ipVersion));
        }
        return ipAddresses;
    }

    private IpAddress findIpByVersion(final IpVersion ipVersion) {
        if (ipRangeIsEmpty()) {
            throw new IllegalStateException("IP range is empty");
        }

        final IpAddressRange range = findRangeBy(ipVersion);
        if (range == null) {
            throw new IllegalArgumentException(String.format("cannot create IP version: %s", ipVersion));
        }

        final IpAddress ip = range.nextIpAddress();

        if (range.isEmpty()) {
            freeRanges.remove(range);
        }

        return ip;
    }

    private IpAddressRange findRangeBy(final IpVersion ipVersion) {
        if (ipVersion == null) {
            return freeRanges.get(HEAD);
        }

        return (IpAddressRange) CollectionUtils.find(freeRanges, new Predicate() {
            @Override
            public boolean evaluate(final Object range) {
                return ((IpAddressRange) range).is(ipVersion);
            }
        });
    }

    private int numberOfPossibleIpsBy(final IpVersion ipVersion) {
        int numberOfPossibleIps = 0;
        for (final IpAddressRange range : freeRanges) {
            if (ipVersion == null || range.is(ipVersion)) {
                numberOfPossibleIps += range.getSize();
            }
        }
        return numberOfPossibleIps;
    }

    public static IpAddressManager getInstance() throws IpNotFoundException {
        final List<IpAddressRange> freeRanges = IpAddressFinder.findFreeIpAddressRange();
        if (freeRanges.isEmpty()) {
            throw new IpNotFoundException("finder did not find free ips");
        }
        return new IpAddressManager(freeRanges);
    }
}
