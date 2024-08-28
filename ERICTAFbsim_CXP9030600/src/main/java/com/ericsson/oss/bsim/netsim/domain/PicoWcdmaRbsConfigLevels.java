/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2014 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.bsim.netsim.domain;

/**
 * @author egavhug
 */
public enum PicoWcdmaRbsConfigLevels implements RbsConfigLevel {

    UNDEFINED(0),
    INTEGRATION_NOT_STARTED(9),
    INTEGRATION_ONGOING(10),
    INTEGRATION_COMPLETE(11),
    READY_FOR_SERVICE(12),
    AI_RNW_CONFIGURATION_FAILED(13),
    OSS_CONFIGURATION_SUCCESSFUL(14),
    OSS_CONFIGURATION_FAILED(15),
    OSS_MIB_SYNCHRONIZATION_SUCCESSFUL(16);

    private int rbsConfigLevel;

    PicoWcdmaRbsConfigLevels(final int rbsConfigLevel) {
        this.rbsConfigLevel = rbsConfigLevel;
    }

    @Override
    public Integer getRbsConfigLevel() {
        return this.rbsConfigLevel;
    }

}

