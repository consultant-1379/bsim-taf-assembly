/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2014 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.bsim.utils.network.helper;

import static java.util.regex.Pattern.compile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ericsson.oss.bsim.utils.network.IpAddress;

/**
 * @author eannpaa
 */
class IPv4 {

    private static final Pattern IP_GROUP = compile("(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})");

    static boolean belongsVersion(final String ip) {
        return IP_GROUP.matcher(ip).find();
    }

    static List<IpAddress> parseClassC(final String source) {
        final Pattern pattern = compile(".*\"((19[2-9]|2[0-2][0-3])\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})\".*");
        final Matcher matcher = pattern.matcher(source);

        final List<IpAddress> reservedIps = new ArrayList<>();
        while (matcher.find()) {
            reservedIps.add(new IpAddress(matcher.group(1)));
        }

        Collections.sort(reservedIps, ipComparator());
        return reservedIps;
    }

    static boolean isSameNetwork(final IpAddress ip1, final IpAddress ip2) {
        return networkFrom(ip1).equals(networkFrom(ip2));
    }

    static int calculateHostDistance(final IpAddress origem, final IpAddress target) {
        return !isSameNetwork(origem, target) ? 0 : Math.abs(hostFrom(origem) - hostFrom(target)) - 1;
    }

    static boolean isAdjacent(final IpAddress ip1, final IpAddress ip2) {
        return calculateHostDistance(ip1, ip2) == 0;
    }

    static IpAddress multicastAddressFrom(final IpAddress ip) {
        return new IpAddress(String.format("%s.255", networkFrom(ip)));
    }

    static IpAddress nextAdjacentIpFrom(final IpAddress ip) {
        return new IpAddress(String.format("%s.%s", networkFrom(ip), hostFrom(ip) + 1));
    }

    private static Comparator<IpAddress> ipComparator() {
        return new Comparator<IpAddress>() {
            @Override
            /**
             * If IPs have same network compares by host else compares by network.
             * In both cases transform IP in decimal value.
             */
            public int compare(final IpAddress ip1, final IpAddress ip2) {
                if (isSameNetwork(ip1, ip2)) {
                    return toNumber(ip1.getValue()).compareTo(toNumber(ip2.getValue()));
                }
                return toNumber(networkFrom(ip1)).compareTo(toNumber(networkFrom(ip2)));
            }

            private Long toNumber(final String value) {
                return Long.valueOf(value.replace(".", ""));
            }
        };
    }

    private static String networkFrom(final IpAddress ip) {
        return IP_GROUP.matcher(ip.getValue()).replaceAll("$1.$2.$3");
    }

    private static int hostFrom(final IpAddress ip) {
        return Integer.valueOf(IP_GROUP.matcher(ip.getValue()).replaceAll("$4"));
    }
}
