/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2014 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.bsim.handlers;

import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.oss.bsim.data.model.NodeType;

/**
 * @author ecilosh
 */
public interface MimHandler {

    /**
     * Get Mim version based on the Node Type, Node Version and Host server.
     * 
     * @param nodeType
     *        The node type of the mim
     * @param nodeVersion
     *        The node type of the mim
     * @param host
     *        The host server to find the Mim version on.
     * @return The value of the Mimversion required.
     */
    String getMimVersion(final NodeType nodeType, final String nodeVersion, final Host host);

}
