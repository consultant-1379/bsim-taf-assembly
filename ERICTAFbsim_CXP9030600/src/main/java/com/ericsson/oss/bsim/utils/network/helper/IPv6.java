/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2014 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.bsim.utils.network.helper;

import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.compile;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.ericsson.oss.bsim.utils.network.IpAddress;

/**
 * @author eannpaa
 */
class IPv6 {

    private static final int RADIX_16 = 16;

    private static final Pattern IP_GROUP = compile(
            "^([0-9a-f]{4}):([0-9a-f:]{4}):([0-9a-f]{4}):([0-9a-f:]{4}):([0-9a-f]{4}):([0-9a-f:]{4}):([0-9a-f:]{4}):([0-9a-f]{4})$", CASE_INSENSITIVE);

    static List<IpAddress> parse(final String source) {
        final Pattern pattern = compile(".*\"(([0-9a-f]{0,4}:){2,7}([0-9a-f]{1,4}))\".*", Pattern.CASE_INSENSITIVE);
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
        return new IpAddress(String.format("%s:ffff", networkFrom(ip)));
    }

    static IpAddress nextAdjacentIpFrom(final IpAddress ip) {
        return new IpAddress(String.format("%s:%s", networkFrom(ip), Integer.toHexString(hostFrom(ip) + 1)));
    }

    private static Comparator<IpAddress> ipComparator() {
        return new Comparator<IpAddress>() {
            @Override
            /*
             * If IPs have same network compares by host else compares by network.
             * In both cases transform IP in hexadecimal value.
             */
            public int compare(final IpAddress ip1, final IpAddress ip2) {
                if (isSameNetwork(ip1, ip2)) {
                    return toNumber(ip1.getValue()).compareTo(toNumber(ip2.getValue()));
                }
                return toNumber(networkFrom(ip1)).compareTo(toNumber(networkFrom(ip2)));
            }

            private BigInteger toNumber(final String value) {
                return new BigInteger(value.replace(":", ""), RADIX_16);
            }
        };
    }

    static String formatToFullRepresentation(final String ip) {
        final String[] tokens = ip.split(":");

        for (int i = 0; i < tokens.length; i++) {
            if (StringUtils.isBlank(tokens[i])) {
                tokens[i] = produceGroups(9 - tokens.length);
            } else {
                tokens[i] = StringUtils.leftPad(tokens[i], 4, "0");
            }
        }

        return StringUtils.join(tokens, ":");
    }

    static String formatToReducedRepresentation(final String ip) {
        return ip.replaceAll("\\b0+", "").replaceAll(":{3,}", "::");
    }

    private static String networkFrom(final IpAddress ip) {
        return IP_GROUP.matcher(ip.getValue()).replaceAll("$1:$2:$3:$4:$5:$6:$7");
    }

    private static int hostFrom(final IpAddress ip) {
        return Integer.valueOf(IP_GROUP.matcher(ip.getValue()).replaceAll("$8"), RADIX_16);
    }

    private static String produceGroups(final int size) {
        final StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < size; i++) {
            buffer.append(":0000");
        }
        return buffer.toString().replaceFirst(":", "");
    }
}
