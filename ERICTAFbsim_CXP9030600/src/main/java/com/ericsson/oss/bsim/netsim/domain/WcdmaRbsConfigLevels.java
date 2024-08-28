package com.ericsson.oss.bsim.netsim.domain;

public enum WcdmaRbsConfigLevels implements RbsConfigLevel {

    BASIC_CV(1),
    CABINET_EQUIPMENT(2),
    SITE_BASIC(3),
    // for WCDMA MACRO, level 4 initiates AI
    SITE_EXTERNAL_HW(4),
    OSS_ACTIVATING_CONFIGURATION(5),
    OSS_CONFIGURATION_SUCCESSFUL(6),
    OSS_CONFIGURATION_FAILED(7),
    UNDEFINED(0),
    GPS_SUCCESSFULLY_MATCHED(8),
    GPS_WANTED_POSITION_NOT_SET(9),
    GPS_MISMATCH_ERROR(10),
    GPS_POSITION_UNAVAILABLE(11),
    INTEGRATION_COMPLETE(12),
    // for WCDMA MICRO, level 13 initiates AI
    SITE_CONFIG_COMPLETE(13),
    READY_FOR_SERVICE(14),
    UNLOCKING_CELLS(15),
    CELLS_UNLOCKED(16),
    UNLOCKING_CELLS_FAILED(17),
    GPS_CHECK_POSITION(18);

    private int rbsConfigLevel;

    WcdmaRbsConfigLevels(final int rbsConfigLevel) {
        this.rbsConfigLevel = rbsConfigLevel;
    }

    @Override
    public Integer getRbsConfigLevel() {
        return this.rbsConfigLevel;
    }

}
