package com.ericsson.oss.bsim.netsim.domain;

public enum LteRbsConfigLevels implements RbsConfigLevel {

    UNDEFINED(0),
    BASIC_CV(1),  
    // 2 missing in LTE simulation RbsConfiguration ENUM
    SITE_BASIC(3),
    // for LTE, level 4 initiates AI
    SITE_EQUIPMENT(4),
    SITE_CONFIG_COMPLETE(5),
    OSS_ACTIVATING_CONFIGURATION(6),
    OSS_CONFIGURATION_SUCCESSFUL(7),
    OSS_CONFIGURATION_FAILED(8),
    S1_COMPLETE(9),
    INTEGRATION_COMPLETE(10),
    // Skipping node only HW type config levels that OSS doesn't require
    FEATURES_ACTIVATED(19),
    ACTIVATING_FEATURES(20),
    ACTIVATING_FEATURES_FAILED(21),
    CELLS_UNLOCKED(22),
    UNLOCKING_CELLS(23),
    UNLOCKING_CELLS_FAILED(24),
    READY_FOR_SERVICE(25),
    S1_NOT_NEEDED(26),
    GPS_SUCCESSFULLY_MATCHED(27),
    GPS_MISMATCH_ERROR(28),
    GPS_POSITION_UNAVAILABLE(29),
    GPS_WANTED_POSITION_NOT_SET(30),
    GPS_CHECK_POSITION(31);

    private int rbsConfigLevel;

    LteRbsConfigLevels(final int rbsConfigLevel) {
        this.rbsConfigLevel = rbsConfigLevel;
    }

    public Integer getRbsConfigLevel() {
        return this.rbsConfigLevel;
    }

}
