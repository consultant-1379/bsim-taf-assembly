/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2014 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.bsim.handlers;

/**
 * @author ecilosh
 */
public interface SubNetworkHandler {

    /**
     * Get any SubNetwork
     */
    String getSubNetwork();

    /**
     * Create a SubNetwork given the SubNetwork name
     * 
     * @param subNetworkName
     *        The name of the SubNetwork to be created
     */
    void createSubNetwork(String subNetworkName);

    /**
     * Delete a SubNetwork given the SubNetwork name
     * 
     * @param subNetworkName
     *        The name of the SubNetwork to be deleted
     */
    void deleteSubNetwork(String subNetworkName);

    /**
     * Execute a check to see if the SubNetwork exists
     * 
     * @param subNetworkName
     *        The name of the SubNetwork you wish to check exists.
     * @return True if the SubNetwork exists
     */
    boolean subNetworkExists(String subNetworkName);

}
