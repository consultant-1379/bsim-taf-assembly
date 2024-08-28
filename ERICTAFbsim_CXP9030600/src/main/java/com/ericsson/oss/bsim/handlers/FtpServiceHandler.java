/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2014 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.bsim.handlers;

import com.ericsson.oss.bsim.data.model.FtpServiceType;
import com.ericsson.oss.bsim.data.model.NodeType;

/**
 * @author ecilosh
 */
public interface FtpServiceHandler {

    /**
     * Get FTP service based on the Node Type, Node Version and FTP type..
     * 
     * @param nodeType
     *        The node type of the mim
     * @param isIPv6
     *        The ip addressing scheme being used
     * @param ftpType
     *        The type of FTP service to be retrieved.
     * @return The value of the FtpService required.
     */
    String getFtpService(final NodeType nodeType, final boolean isIPv6, final FtpServiceType ftpServiceType);
}
