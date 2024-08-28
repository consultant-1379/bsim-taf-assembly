/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2014 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.bsim.test.verification;

import org.apache.log4j.Logger;

import com.ericsson.oss.bsim.data.model.BsimNodeData;
import com.ericsson.oss.bsim.operators.BsimOperator;

public class NodeAddedVerification implements IBSIMVerification {

    private final BsimNodeData nodeData;

    private final Logger log = Logger.getLogger(NodeAddedVerification.class);

    /**
     * @param nodeData
     */
    public NodeAddedVerification(final BsimNodeData nodeData) {
        this.nodeData = nodeData;
    }

    @Override
    public boolean doVerification(final BsimOperator bsimOperator) {
        int count = 0;
        final int maximumCount = 60;
        boolean isFinalExecutionOfcheckNodeByCmdMethod = false;
        do {
            if (count == maximumCount - 1) {
                isFinalExecutionOfcheckNodeByCmdMethod = true;
            }
            if (bsimOperator.checkNodeByCmd(nodeData.getNodeName(), isFinalExecutionOfcheckNodeByCmdMethod)) {

                return true;
            }
            count++;
            try {
                Thread.sleep(3000);
            } catch (final InterruptedException e) {
            }

        } while (count < maximumCount);
        log.error("Node not found in CS");
        return false;
    }
}