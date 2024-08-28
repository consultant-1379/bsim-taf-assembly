/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2014 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.bsim.handlers.templates;

/**
 * Handler to help execute commands using the template utility service.
 * 
 * @author ecilosh
 */
public interface TemplateHandler {

    /**
     * Execute a check to see if the template exists on the OSS master server
     * 
     * @param templateName
     *        The Template name.
     * @return True if the Template name exists.
     */
    boolean templateExists(String templateName);

    /**
     * Install a template on the OSS master server that exists in the local template/non_delivered directory.
     * 
     * @param templateName
     *        The Template name. This must also be the name of the file without the xml extension
     * @return True if the Template is successfully installed.
     */
    boolean installTemplate(String templateName);

    /**
     * Uninstall a template that exists on the OSS master server
     * 
     * @param templateName
     *        The Template name
     * @return True if the Template is successfully uninstalled.
     */
    boolean uninstallTemplate(String templateName);
}
