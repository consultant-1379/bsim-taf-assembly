/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2014 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.bsim.utils.ftp.helper;

import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.join;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.ericsson.oss.bsim.batch.data.model.NetworkType;
import com.ericsson.oss.bsim.data.model.FtpServiceType;

/**
 * @author eannpaa
 */
public class Command {

    private static final String BASE = "/opt/ericsson/nms_cif_cs/etc/unsupported/bin/cstest -s ONRM_CS";

    public static FilterFtpService filterFtpService() {
        return new FilterFtpService();
    }

    public static FindIpAddress findIpAddress() {
        return new FindIpAddress();
    }

    private static String createCommand(final String flag, final String args) {
        return join(Arrays.asList(BASE, flag, args), " ");
    }

    public static class FilterFtpService {

        private static final String FLAG = "lt FtpService -f";

        private String networkType;

        private String ftpServiceType;

        public FilterFtpService byNetworkType(final NetworkType networkType) {
            this.networkType = "\\$.networkType==" + networkType.asAttribute();
            return this;
        }

        public FilterFtpService byFtpServiceType(final FtpServiceType ftpServiceType) {
            this.ftpServiceType = "\\$.typeName==" + ftpServiceType.asAttribute();
            return this;
        }

        private String arguments() {
            final List<String> args = new ArrayList<>();

            if (!isEmpty(networkType)) {
                args.add(networkType);
            }

            if (!isEmpty(ftpServiceType)) {
                args.add(ftpServiceType);
            }

            return String.format("\"%s\"", join(args, " and "));
        }

        public String build() {
            return createCommand(FLAG, arguments());
        }
    }

    public static class FindIpAddress {

        private static final String FLAG = "la";

        private String ftpServerFdn;

        public FindIpAddress byFtpServerFdn(final String ftpServerFdn) {
            this.ftpServerFdn = ftpServerFdn.substring(0, ftpServerFdn.lastIndexOf(","));
            return this;
        }

        private String arguments() {
            return String.format("%s ipAddress", ftpServerFdn);
        }

        public String build() {
            return createCommand(FLAG, arguments());
        }
    }
}