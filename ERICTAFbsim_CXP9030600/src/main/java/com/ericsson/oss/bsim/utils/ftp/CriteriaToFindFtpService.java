/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2014 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.bsim.utils.ftp;

import com.ericsson.cifwk.taf.data.HostType;
import com.ericsson.oss.bsim.batch.data.model.NetworkType;
import com.ericsson.oss.bsim.data.model.FtpServiceType;
import com.ericsson.oss.bsim.utils.network.IpVersion;

/**
 * @author eannpaa
 */
public class CriteriaToFindFtpService {

    private final NetworkType networkType;

    private final FtpServiceType ftpServiceType;

    private final HostType hostType;

    private final IpVersion ipVersion;

    private CriteriaToFindFtpService(final Builder builder) {
        networkType = builder.networkType;
        ftpServiceType = builder.ftpServiceType;
        hostType = builder.hostType;
        ipVersion = builder.ipVersion != null ? builder.ipVersion : IpVersion.IPv4;
    }

    public NetworkType getNetworkType() {
        return networkType;
    }

    public FtpServiceType getFtpServiceType() {
        return ftpServiceType;
    }

    public HostType getHostType() {
        return hostType;
    }

    public IpVersion getIpVersion() {
        return ipVersion;
    }

    public static class Builder {

        private NetworkType networkType;

        private FtpServiceType ftpServiceType;

        private HostType hostType;

        private IpVersion ipVersion;

        public Builder byNetworkType(final NetworkType networkType) {
            this.networkType = networkType;
            return this;
        }

        public Builder byFtpServiceType(final FtpServiceType ftpServiceType) {
            this.ftpServiceType = ftpServiceType;
            return this;
        }

        public Builder checkIfIpIsSetOnHost(final HostType hostType) {
            this.hostType = hostType;
            return this;
        }

        public Builder byIPv4() {
            ipVersion = IpVersion.IPv4;
            return this;
        }

        public Builder byIPv6() {
            ipVersion = IpVersion.IPv6;
            return this;
        }

        public CriteriaToFindFtpService build() {
            return new CriteriaToFindFtpService(this);
        }
    }
}
