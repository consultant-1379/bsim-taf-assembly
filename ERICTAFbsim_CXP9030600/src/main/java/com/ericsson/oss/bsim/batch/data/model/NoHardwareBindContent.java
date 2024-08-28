/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2014 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.bsim.batch.data.model;

import java.util.LinkedHashMap;

public class NoHardwareBindContent {

    private LinkedHashMap<String, String> siteInstallTemplateAttrs;

    private String siteInstallTemplateName;

    private String saveToLocation;

    public LinkedHashMap<String, String> getSiteInstallTemplateAttrs() {
        return siteInstallTemplateAttrs;
    }

    public void setSiteInstallTemplateAttrs(final LinkedHashMap<String, String> siteInstallTemplateAttrs) {
        this.siteInstallTemplateAttrs = siteInstallTemplateAttrs;
    }

    public String getSiteInstallTemplateName() {
        return siteInstallTemplateName;
    }

    public void setSiteInstallTemplateName(final String siteInstallTemplateName) {
        this.siteInstallTemplateName = siteInstallTemplateName;
    }

    public String getSaveToLocation() {
        return saveToLocation;
    }

    public void setSaveToLocation(final String saveToLocation) {
        this.saveToLocation = saveToLocation;
    }

}
