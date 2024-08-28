/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2014 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.bsim.handlers;

/**
 * @author ecilosh
 */
public interface SiteHandler {

    /**
     * Get any site
     */
    String getSite();

    /**
     * Create a Site given the Site name
     * 
     * @param siteName
     *        The name of the Site to be created
     */
    void createSite(String siteName);

    /**
     * Delete a Site given the Site name
     * 
     * @param siteName
     *        The name of the Site to be deleted
     */
    void deleteSite(String siteName);

    /**
     * Execute a check to see if the Site exists
     * 
     * @param siteName
     *        The name of the Site you wish to check exists.
     * @return True if the Site exists
     */
    boolean siteExists(String siteName);
}
