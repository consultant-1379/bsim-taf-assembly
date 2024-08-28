package com.ericsson.oss.bsim.operators;

import com.ericsson.cifwk.taf.ApiOperator;

public interface BsimBindOperator extends ApiOperator {

    public String bindNode(String nodeName, String serialNumber);

    public String bindMicroWCDMANode(String nodeName, String serialNumber);

    public String bindMacroWCDMANode(String nodeName, String serialNumber);

    public String noHWbindDG2Node(String nodeName, String saveAs, String siteInstallationTemplate, String OAMIpAddress);

    public String bindRadioNode(String nodeName, String saveAs, String siteInstallationTemplate, String OAMIpAddress, String serialNumber);

    /**
     * @param nodeName
     * @param saveAs
     * @param siteInstallationTemplate
     * @param OAMIpAddress
     * @param serialNumber
     * @return
     */
    String bindSpiteFireNode(String nodeName, String saveAs, String siteInstallationTemplate, String OAMIpAddress, String serialNumber);

}

