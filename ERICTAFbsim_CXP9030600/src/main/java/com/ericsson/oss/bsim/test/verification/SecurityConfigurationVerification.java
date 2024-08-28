/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2014 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.bsim.test.verification;

import org.apache.log4j.Logger;

import com.ericsson.oss.bsim.data.model.BsimNodeData;
import com.ericsson.oss.bsim.data.model.NodeType;
import com.ericsson.oss.bsim.operators.BsimOperator;

public class SecurityConfigurationVerification implements IBSIMVerification {

    private final BsimNodeData nodeData;

    private final Logger log = Logger.getLogger(SecurityConfigurationVerification.class);

    public SecurityConfigurationVerification(final BsimNodeData nodeData) {

        this.nodeData = nodeData;
    }

    @Override
    public boolean doVerification(final BsimOperator bsimOperator) {
        int count = 0;
        final int maximumCount = 5;

        do {
            // OSS-93001 CMPv2 TAF: Modify Micro/Macro TAF TCs to include Security
            log.info("node type::::" + nodeData.getNodeType().equals(NodeType.WCDMA) + "-------------" + nodeData.getNodeType());
            if (nodeData.getNodeType().equals(NodeType.WCDMA) || nodeData.getNodeType().equals(NodeType.MICRO_WCDMA)) {
                if (bsimOperator.checkNetworkConfiguration(nodeData.getNodeType(), nodeData.getNodeName(), nodeData.CriticalData.getRncName(), "Trusted")
                        && bsimOperator.checkIscfFileWCDMA(nodeData)) {

                    return true;
                }
            } else {
                if (bsimOperator.checkNetworkConfiguration(nodeData.getNodeType(), nodeData.getNodeName(), nodeData.CriticalData.getRncName(), "Trusted")
                        && bsimOperator.checkIscfFile(nodeData)) {

                    return true;
                }
            }
            count++;
            try {
                Thread.sleep(3000);
            } catch (final InterruptedException e) {
            }

        } while (count < maximumCount);
        log.error("Security files not found");
        return false;
    }
}