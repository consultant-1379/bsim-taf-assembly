package com.ericsson.oss.bsim.batch.data.model;

import java.util.HashMap;

public class LRANBatchConfig {

    MockLRANPicoBatch mockLRANPicoBatch;

    int nodesToBind;

    HashMap<String, String> boundFdns;

    String hardwareIdPrefix;

    int batchSize;

    boolean noHardwareBind;

    HashMap<NoHardwareBindContent, Integer> noHardwareBindContents;

    public MockLRANPicoBatch getMockLRANPicoBatch() {

        return mockLRANPicoBatch;
    }

    public void setMockLRANPicoBatch(final MockLRANPicoBatch mockLRANPicoBatch) {

        this.mockLRANPicoBatch = mockLRANPicoBatch;
    }

    public int getNodesToBind() {

        return nodesToBind;
    }

    public void setNodesToBind(final int nodesToBind) {

        this.nodesToBind = nodesToBind;
    }

    public HashMap<String, String> getBoundFdns() {

        return boundFdns;
    }

    public void setBoundFdns(final HashMap<String, String> boundFdns) {

        this.boundFdns = boundFdns;
    }

    public String getHardwareIdPrefix() {

        return hardwareIdPrefix;
    }

    public void setHardwareIdPrefix(final String hardwareIdPrefix) {

        this.hardwareIdPrefix = hardwareIdPrefix;
    }

    public int getBatchSize() {

        return batchSize;
    }

    public void setBatchSize(final int batchSize) {

        this.batchSize = batchSize;
    }

    public boolean isNoHardwareBind() {

        return noHardwareBind;
    }

    public void setNoHardwareBind(final boolean noHardwareBind) {
        this.noHardwareBind = noHardwareBind;
    }

    public HashMap<NoHardwareBindContent, Integer> getNoHardwareBindconfigs() {
        return noHardwareBindContents;
    }

    public void setNoHardwareBindconfigs(final HashMap<NoHardwareBindContent, Integer> noHardwareBindconfigs) {
        this.noHardwareBindContents = noHardwareBindconfigs;
    }

}
