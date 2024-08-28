package com.ericsson.oss.bsim.netsim.domain;

public enum DG2ConfigLevel implements RbsConfigLevel {

    SITE_CONFIG_COMPLETE(1),
    OSS_CONFIGURATION_SUCCESSFUL(2),
    INTEGRATION_COMPLETE(3),
    READY_FOR_SERVICE(4),
    OSS_ACTIVATING_CONFIGURATION(5),
    OSS_CONFIGURATION_FAILED(6),
    ACTIVATIN_FEATURES(7),
    ACTIVATIN_FEATURES_FAILED(8),
    OSS_CONTROL_NODE_CONN(9),
    OSS_CONTROL_NODE_CONN_FAILED(10),
    UNLOCKING_CELLS(11),
    UNLOCKING_CELLS_FAILED(12),
    RAN_INTEGRATION_WAS_CANCELLED(13);

    private int rbsConfigLevel;

    DG2ConfigLevel(final int rbsConfigLevel) {
        this.rbsConfigLevel = rbsConfigLevel;
    }

    @Override
    public Integer getRbsConfigLevel() {
        return this.rbsConfigLevel;
    }

}

