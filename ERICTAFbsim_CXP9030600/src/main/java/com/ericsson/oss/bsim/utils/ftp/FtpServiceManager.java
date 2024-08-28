/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2014 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.bsim.utils.ftp;

import static com.ericsson.oss.bsim.utils.ftp.FtpServiceFinder.findFtpServiceFdnsBy;
import static com.ericsson.oss.bsim.utils.ftp.FtpServiceFinder.findIpFrom;
import static com.ericsson.oss.bsim.utils.network.helper.IpAddressHelper.formatToReducedRepresentation;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.HostType;
import com.ericsson.oss.bsim.getters.api.BsimApiGetter;
import com.ericsson.oss.bsim.utils.network.IpAddress;

/**
 * @author eannpaa
 */
public class FtpServiceManager {

    private final Map<HostType, String> cacheNetworkInterface = new HashMap<>();

    public IpAddress findFtpServiceBy(final CriteriaToFindFtpService criteria) throws FtpServiceNotFoundException {
        for (final String fdn : findFtpServiceFdnsBy(criteria.getNetworkType(), criteria.getFtpServiceType())) {
            final IpAddress ipFtpServer = findIpFrom(fdn);
            if (ipMatchesCriteria(ipFtpServer, criteria)) {
                return ipFtpServer;
            }
        }
        throw new FtpServiceNotFoundException();
    }

    private boolean ipMatchesCriteria(final IpAddress ip, final CriteriaToFindFtpService criteria) {
        if (ip == null) {
            return false;
        }
        final boolean matchesIpVersion = ip.getVersion() == criteria.getIpVersion();
        return matchesIpVersion && isIpSetOnHost(ip, criteria.getHostType());
    }

    private boolean isIpSetOnHost(final IpAddress ip, final HostType hostType) {
        if (hostType == null) {
            return true;
        }

        String networkInterface = cacheNetworkInterface.get(hostType);

        if (StringUtils.isBlank(networkInterface)) {
            final Host masterHost = DataHandler.getHostByType(hostType);
            networkInterface = BsimApiGetter.getCLICommandHelper(masterHost).simpleExec("ifconfig -a");
            cacheNetworkInterface.put(hostType, networkInterface);
        }

        return StringUtils.contains(networkInterface, formatToReducedRepresentation(ip.getValue()));
    }

    public static FtpServiceManager getInstance() {
        return new FtpServiceManager();
    }

    public static CriteriaToFindFtpService.Builder builderCriteria() {
        return new CriteriaToFindFtpService.Builder();
    }
}
