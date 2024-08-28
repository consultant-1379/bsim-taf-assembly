/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2014 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.bsim.utils.network.helper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ericsson.oss.bsim.utils.network.IpAddress;
import com.ericsson.oss.bsim.utils.network.IpVersion;

/**
 * This class contains helper methods to handle with IPs. It works with IPv4 and IPv6.
 *
 * @author eannpaa
 */
public class IpAddressHelper {

    public static IpAddress nextAdjacentIpFrom(final IpAddress ip) {
        return isIPv4(ip) ? IPv4.nextAdjacentIpFrom(ip) : IPv6.nextAdjacentIpFrom(ip);
    }

    public static boolean isSameNetwork(final IpAddress ip1, final IpAddress ip2) {
        return isIPv4(ip1) && isIPv4(ip2) ? IPv4.isSameNetwork(ip1, ip2) : IPv6.isSameNetwork(ip1, ip2);
    }

    public static IpAddress multicastAddressFrom(final IpAddress ip) {
        return isIPv4(ip) ? IPv4.multicastAddressFrom(ip) : IPv6.multicastAddressFrom(ip);
    }

    public static int calculateHostDistance(final IpAddress origem, final IpAddress target) {
        return isIPv4(origem) && isIPv4(target) ? IPv4.calculateHostDistance(origem, target) : IPv6.calculateHostDistance(origem, target);
    }

    public static boolean isAdjacent(final IpAddress origem, final IpAddress target) {
        return isIPv4(origem) ? IPv4.isAdjacent(origem, target) : IPv6.isAdjacent(origem, target);
    }

    @SuppressWarnings("serial")
    public static Map<IpVersion, List<IpAddress>> parse(final String source) {
        return new HashMap<IpVersion, List<IpAddress>>() {
            {
                put(IpVersion.IPv4, IPv4.parseClassC(source));
                put(IpVersion.IPv6, IPv6.parse(source));
            }
        };
    }

    public static String format(final String ip) {
        return IPv4.belongsVersion(ip) ? ip : IPv6.formatToFullRepresentation(ip);
    }

    public static String formatToReducedRepresentation(final String ip) {
        return IPv4.belongsVersion(ip) ? ip : IPv6.formatToReducedRepresentation(ip);
    }

    public static boolean isIPv4(final IpAddress ip) {
        return IPv4.belongsVersion(ip.getValue());
    }
}
