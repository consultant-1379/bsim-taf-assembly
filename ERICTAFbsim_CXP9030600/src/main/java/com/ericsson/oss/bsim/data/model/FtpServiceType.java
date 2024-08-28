/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2014 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.bsim.data.model;

/**
 * @author ecilosh
 */
public enum FtpServiceType {
    BACKUP("BackupStore"),
    SOFTWARE("SwStore"),
    LICENSE("LicenseKeyStore"),
    CONFIG("ConfigStore"),
    AUTOINTEGRATION("autoIntegration");

    private final String asAttribute;

    private FtpServiceType(final String asAttribute) {
        this.asAttribute = asAttribute;
    }

    public String asAttribute() {
        return this.asAttribute;
    }
}
