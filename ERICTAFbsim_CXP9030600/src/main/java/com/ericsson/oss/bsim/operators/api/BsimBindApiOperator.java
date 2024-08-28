package com.ericsson.oss.bsim.operators.api;

import com.ericsson.oss.bsim.operators.BsimBindOperator;

public class BsimBindApiOperator implements BsimBindOperator {

    private final BsimAddNodeApiOperator addNodeOperator = new BsimAddNodeApiOperator();

    @Override
    public String bindNode(final String nodeName, final String serialNumber) {
        return addNodeOperator.invokeGroovyMethodOnArgs("BindExecutor", "executeHardwareBindOnMacro", nodeName, serialNumber);
    }

    @Override
    public String bindMicroWCDMANode(final String nodeName, final String serialNumber) {
        return addNodeOperator.invokeGroovyMethodOnArgs("BindExecutor", "executeHardwareBindOnMicroWCDMA", nodeName, serialNumber);
    }

    @Override
    public String bindMacroWCDMANode(final String nodeName, final String serialNumber) {
        return addNodeOperator.invokeGroovyMethodOnArgs("BindExecutor", "executeHardwareBindOnMicroWCDMA", nodeName, serialNumber);
    }

    @Override
    public String noHWbindDG2Node(final String nodeName, final String saveAs, final String siteInstallationTemplate, final String OAMIpAddress) {
        return addNodeOperator.invokeGroovyMethodOnArgs("BsimAddNodeOperator", "executeNoHardwareBindDG2", nodeName, saveAs, siteInstallationTemplate,
                OAMIpAddress);
    }

    @Override
    public String bindRadioNode(
            final String nodeName,
            final String saveAs,
            final String siteInstallationTemplate,
            final String OAMIpAddress,
            final String serialNumber) {
        return addNodeOperator.invokeGroovyMethodOnArgs("BsimAddNodeOperator", "executeBindRadioNode", nodeName, saveAs, siteInstallationTemplate,
                OAMIpAddress, serialNumber);
    }

    @Override
    public String bindSpiteFireNode(
            final String nodeName,
            final String saveAs,
            final String siteInstallationTemplate,
            final String OAMIpAddress,
            final String serialNumber) {
        return addNodeOperator.invokeGroovyMethodOnArgs("BsimAddNodeOperator", "executeBindSpitFireNode", nodeName, saveAs, siteInstallationTemplate,
                OAMIpAddress, serialNumber);
    }

}

