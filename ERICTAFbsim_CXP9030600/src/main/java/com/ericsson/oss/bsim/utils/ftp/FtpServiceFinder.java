/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2014 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.bsim.utils.ftp;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.oss.bsim.batch.data.model.NetworkType;
import com.ericsson.oss.bsim.data.model.FtpServiceType;
import com.ericsson.oss.bsim.getters.api.BsimApiGetter;
import com.ericsson.oss.bsim.utils.ftp.helper.Command;
import com.ericsson.oss.bsim.utils.network.IpAddress;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;

/**
 * @author eannpaa
 */
class FtpServiceFinder {

    private static final Pattern IP_POSITION = Pattern.compile(".*\"(.*)\".*");

    static List<String> findFtpServiceFdnsBy(final NetworkType networkType, final FtpServiceType ftpServiceType) {
        final List<String> fdns = new ArrayList<>();
        final Scanner scanner = new Scanner(execute(Command.filterFtpService().byNetworkType(networkType).byFtpServiceType(ftpServiceType).build()));
        while (scanner.hasNextLine()) {
            fdns.add(scanner.nextLine());
        }
        scanner.close();

        return fdns;
    }

    static IpAddress findIpFrom(final String fdn) {
        final String output = execute(Command.findIpAddress().byFtpServerFdn(fdn).build());
        final Matcher matcher = IP_POSITION.matcher(output);

        return matcher.find() ? new IpAddress(matcher.group(1)) : null;
    }

    private static String execute(final String command) {
        final Host masterHost = HostGroup.getOssmaster(); // DataHandler.getHostByName("ossmaster");
        final String output = BsimApiGetter.getCLICommandHelper(masterHost).simpleExec(command);

        return isValid(output) ? output : StringUtils.EMPTY;
    }

    private static boolean isValid(final String output) {
        return !StringUtils.startsWith(output, "exception");
    }
}
