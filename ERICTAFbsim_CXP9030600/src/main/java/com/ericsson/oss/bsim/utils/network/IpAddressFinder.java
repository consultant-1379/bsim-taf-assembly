/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2014 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.bsim.utils.network;

import static com.ericsson.oss.bsim.utils.network.helper.IpAddressHelper.calculateHostDistance;
import static com.ericsson.oss.bsim.utils.network.helper.IpAddressHelper.isAdjacent;
import static com.ericsson.oss.bsim.utils.network.helper.IpAddressHelper.isSameNetwork;
import static com.ericsson.oss.bsim.utils.network.helper.IpAddressHelper.multicastAddressFrom;
import static com.ericsson.oss.bsim.utils.network.helper.IpAddressHelper.nextAdjacentIpFrom;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;
import com.ericsson.oss.bsim.getters.api.BsimApiGetter;
import com.ericsson.oss.bsim.utils.network.helper.IpAddressHelper;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;

/**
 * This class is responsable to find free IP address range from reserved IPs.
 * For the mechanism to work, is required find reserved IPs to calculate free range between those reserved IPs.
 * 
 * @author eannpaa
 */
class IpAddressFinder {

    static List<IpAddressRange> findFreeIpAddressRange() {
        final List<IpAddressRange> freeRanges = new ArrayList<>();

        for (final Entry<IpVersion, List<IpAddress>> entry : IpAddressHelper.parse(getReservedIps()).entrySet()) {
            freeRanges.addAll(findFreeIpAddressRange(entry.getValue()));
        }
        return freeRanges;
    }

    private static List<IpAddressRange> findFreeIpAddressRange(final List<IpAddress> reservedIps) {
        final List<IpAddressRange> freeRanges = new ArrayList<>();
        if (reservedIps.isEmpty()) {
            return freeRanges;
        }

        IpAddress headIp, nextIp;

        for (int i = 0; i < reservedIps.size() - 1; i++) {
            headIp = reservedIps.get(i);
            nextIp = reservedIps.get(i + 1);

            if (!isSameNetwork(headIp, nextIp)) {
                nextIp = multicastAddressFrom(headIp);
            }

            tryAddNewRange(freeRanges, headIp, nextIp);
        }

        // calculates range to last IP using its multicast address
        headIp = reservedIps.get(reservedIps.size() - 1);
        tryAddNewRange(freeRanges, headIp, multicastAddressFrom(headIp));

        return freeRanges;
    }

    private static void tryAddNewRange(final List<IpAddressRange> freeRanges, final IpAddress headIp, final IpAddress nextIp) {
        if (!isAdjacent(headIp, nextIp)) {
            freeRanges.add(new IpAddressRange(nextAdjacentIpFrom(headIp), calculateHostDistance(headIp, nextIp)));
        }
    }

    private static String getReservedIps() {
        final Host masterHost = HostGroup.getOssmaster(); // DataHandler.getHostByName("ossmaster");
        final CLICommandHelper executor = BsimApiGetter.getCLICommandHelper(masterHost);
        return executor.simpleExec("/opt/ericsson/nms_cif_cs/etc/unsupported/bin/cstest -s Seg_masterservice_CS lt MeContext -an ipAddress");
    }
}
