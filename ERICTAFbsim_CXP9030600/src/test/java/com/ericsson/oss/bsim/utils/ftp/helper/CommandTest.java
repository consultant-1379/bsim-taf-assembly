/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2014 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.bsim.utils.ftp.helper;

import static com.ericsson.oss.bsim.batch.data.model.NetworkType.CORE;
import static com.ericsson.oss.bsim.data.model.FtpServiceType.AUTOINTEGRATION;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * @author eannpaa
 */
public class CommandTest {

    @Test
    public void shouldBuildCommandToFilterFtpServiceByNetworkType() {
        final String command = "/opt/ericsson/nms_cif_cs/etc/unsupported/bin/cstest -s ONRM_CS lt FtpService -f \"\\$.networkType==Core\"";
        assertEquals(command, Command.filterFtpService().byNetworkType(CORE).build());
    }

    @Test
    public void shouldBuildCommandToFilterFtpServiceByServiceType() {
        final String command = "/opt/ericsson/nms_cif_cs/etc/unsupported/bin/cstest -s ONRM_CS lt FtpService -f \"\\$.typeName==autoIntegration\"";
        assertEquals(command, Command.filterFtpService().byFtpServiceType(AUTOINTEGRATION).build());
    }

    @Test
    public void shouldBuildCommandToFilterFtpServiceByNetworkTypeAndServiceType() {
        final String command = "/opt/ericsson/nms_cif_cs/etc/unsupported/bin/cstest -s ONRM_CS lt FtpService -f \"\\$.networkType==Core and \\$.typeName==autoIntegration\"";
        assertEquals(command, Command.filterFtpService().byNetworkType(CORE).byFtpServiceType(AUTOINTEGRATION).build());
        assertEquals(command, Command.filterFtpService().byFtpServiceType(AUTOINTEGRATION).byNetworkType(CORE).build());
    }

    @Test
    public void shouldBuildCommandToFindIpAddressByFtpServerFtn() {
        final String command = "/opt/ericsson/nms_cif_cs/etc/unsupported/bin/cstest -s ONRM_CS la SubNetwork=ONRM_ROOT_MO,FtpServer=SMRSSLAVE-WRAN-nedssv4 ipAddress";
        assertEquals(command, Command.findIpAddress().byFtpServerFdn("SubNetwork=ONRM_ROOT_MO,FtpServer=SMRSSLAVE-WRAN-nedssv4,FtpService=w-back-nedssv4")
                .build());
    }
}
