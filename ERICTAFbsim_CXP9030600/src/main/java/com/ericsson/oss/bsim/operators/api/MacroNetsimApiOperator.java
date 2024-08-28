/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2014 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.bsim.operators.api;

import com.ericsson.oss.bsim.data.model.BsimNodeData;
import com.ericsson.oss.bsim.data.model.NodeType;

/**
 * @author egavhug
 */
public class MacroNetsimApiOperator extends NetsimApiOperator {

    public String createMacroPort(final BsimNodeData nodeData) {

        final String portName = getPortName(nodeData);
        final String portIPAddress = getPortIPAddress(nodeData);

        return createMacroPort(portName, portIPAddress);

    }

    /**
     * Create an LTE/WCDMA Node in Netsim
     * 
     * @param simulationName
     * @param nodeName
     * @return
     */
    public String createNodeInNetsim(final String simulationName, final BsimNodeData nodeData) {
        String nodeName = null;
        String[] createNodeCommands = new String[13];
        if (nodeData.getNodeType() == NodeType.DG2) {
            nodeName = nodeData.getDG2NodeNameForNetsim();
        } else if (nodeData.getNodeType() == NodeType.BasebandT) {
            nodeName = nodeData.getNodeName();
        } else {
            nodeName = nodeData.getNodeNameForNetsim();
        }
        createNodeCommands = generateCreatNodeCommand(createNodeCommands, simulationName, nodeData);

        log.info("Creating Node " + nodeName + " in simulation " + simulationName);

        final String outputFromCommandLine = sshNetsimHandler.executeCommand(createNodeCommands);
        final String errorMessage = "Node " + nodeName + " creation failed!";
        final String successfulMessage = "Node " + nodeName + " created in netsim successfully";

        return outputToReturnToTestCase(outputFromCommandLine, errorMessage, successfulMessage);

    }

    private String getPortName(final BsimNodeData nodeData) {

        final String portName = nodeData.getNodeType().toString();
        if (portName.equals("DG2") || portName.equals("BasebandT")) {
            return "NETCONF_PROT_SSH";
        }
        if (nodeData.isIPv6()) {
            return portName + "_IPv6";
        } else {
            return portName + "_IPv4";
        }
    }

    private String getPortIPAddress(final BsimNodeData nodeData) {

        final String ipAddress = nodeData.CriticalData.getIpAddress();
        int lastSeperator = 0;
        if (nodeData.isIPv6()) {
            lastSeperator = ipAddress.lastIndexOf(":");
        } else {
            lastSeperator = ipAddress.lastIndexOf(".");
        }

        return ipAddress.substring(0, lastSeperator + 1) + "1";
    }

    public String createMacroPort(final String portName, final String ipAddress) {

        final String[] createPort = new String[7];

        createPort[0] = ".configure .config1 newport";
        createPort[1] = ".select configuration";
        createPort[2] = ".config1 newport";
        createPort[3] = ".config add port " + portName + " iiop_prot netsim";
        createPort[4] = ".config1 portaddr " + portName;
        createPort[5] = ".config port address " + portName + " nehttpd " + ipAddress + " 56834 56836 no_value";
        createPort[6] = ".config save";

        log.info("Creating Macro Port in Netsim ==> " + portName);

        final String outputFromCommandLine = sshNetsimHandler.executeCommand(createPort);
        final String errorMessage = "Port creation failed!";
        final String successfulMessage = "Port created in netsim successfully";

        return outputToReturnToTestCase(outputFromCommandLine, errorMessage, successfulMessage);

    }

    public String[] generateCreatNodeCommand(final String[] createNodeArrayofCommands, final String simulationName, final BsimNodeData nodeData) {

        final boolean isBasebandTnode = nodeData.getNodeType() == NodeType.BasebandT;
        final String ipOffset = getIPOffset(nodeData.CriticalData.getIpAddress(), nodeData.isIPv6());
        log.info("IP Offset of IP address is: ==> " + ipOffset);
        createNodeArrayofCommands[0] = ".open " + simulationName;
        createNodeArrayofCommands[1] = ".createne checkport " + getPortName(nodeData);
        if (nodeData.getNodeType() == NodeType.DG2) {
            createNodeArrayofCommands[2] = ".new simne -auto 1 " + nodeData.getDG2NodeNameForNetsim();
        } else if (isBasebandTnode) {
            createNodeArrayofCommands[2] = ".new simne -auto 1 " + nodeData.getNodeName() + " no_value";
        } else {
            createNodeArrayofCommands[2] = ".new simne -auto 1 " + nodeData.getNodeNameForNetsim();
        }
        createNodeArrayofCommands[3] = ".set netype " + nodeData.CriticalData.getNetsimMimVersion();
        createNodeArrayofCommands[4] = ".set port " + getPortName(nodeData);
        if (nodeData.getNodeType() != NodeType.DG2 && !isBasebandTnode) {
            createNodeArrayofCommands[5] = ".createne subaddr " + ipOffset + " subaddr no_value";

            createNodeArrayofCommands[6] = ".set taggedaddr subaddr " + ipOffset + " 1";
        } else {
            createNodeArrayofCommands[5] = ".createne subaddr " + nodeData.CriticalData.getIpAddress() + " subaddr no_value";

            createNodeArrayofCommands[6] = ".set taggedaddr subaddr " + nodeData.CriticalData.getIpAddress();
        }
        createNodeArrayofCommands[7] = ".set ssliop no no_value";
        createNodeArrayofCommands[8] = "useattributecharacteristics:switch=\"off\";";
        createNodeArrayofCommands[9] = ".set save";
        createNodeArrayofCommands[10] = ".start";
        createNodeArrayofCommands[11] = "setswinstallvariables:createCVFiles=true;";
        createNodeArrayofCommands[12] = ".stop";
        return createNodeArrayofCommands;

    }
}

