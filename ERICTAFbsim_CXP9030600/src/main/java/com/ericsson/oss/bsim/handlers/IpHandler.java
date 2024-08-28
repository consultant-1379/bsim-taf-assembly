/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2014 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.bsim.handlers;

import java.util.List;

import com.ericsson.cifwk.taf.data.Host;

/**
 * The IpHandler contains functionality that will be related to ip addressing only.
 * 
 * @author ecilosh
 */
public interface IpHandler {

    /**
     * This method run on a particular group of hosts will return a list of available IP ranges that are free.
     * 
     * @param host
     * @return
     */
    List<IPRange> getFreeIpRanges(List<Host> hosts);

}
