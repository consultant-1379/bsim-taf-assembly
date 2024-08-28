package com.ericsson.oss.bsim.batch.data.model;

import java.util.List;

public abstract class MockBsimPicoBatch extends MockBsimBatch {

    private String swUpgradePackageId;

    private String swUpgradePackageLocation;

    private boolean unlockCells;

    @Override
    public abstract RanType getRantype();

    @Override
    public abstract String getRncNameForNetsim();

    @Override
    public abstract String getRncName();

    @Override
    public abstract String getRncFdn();

    @Override
    public abstract String getRncIpAddress();

    @Override
    public abstract List<String> getNodeFdnValues();

    public String getSwUpgradePackageId() {

        return swUpgradePackageId;
    }

    public void setSwUpgradePackageId(final String swUpgradePackageId) {

        this.swUpgradePackageId = swUpgradePackageId;
    }

    public String getSwUpgradePackageLocation() {

        return swUpgradePackageLocation;
    }

    public void setSwUpgradePackageLocation(final String swUpgradePackageLocation) {

        this.swUpgradePackageLocation = swUpgradePackageLocation;
    }

    public boolean isUnlockCells() {

        return unlockCells;
    }

    public void setUnlockCells(final boolean unlockCells) {

        this.unlockCells = unlockCells;
    }

}
