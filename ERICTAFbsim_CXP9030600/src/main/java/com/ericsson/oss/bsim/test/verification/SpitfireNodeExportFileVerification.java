/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2016 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.bsim.test.verification;

import org.apache.log4j.Logger;

import com.ericsson.oss.bsim.data.model.BsimNodeData;
import com.ericsson.oss.bsim.operators.BsimOperator;

/**
 * @author xnehmis
 */
public class SpitfireNodeExportFileVerification implements IBSIMVerification {

    private final BsimNodeData nodeData;

    private final Logger log = Logger.getLogger(SpitfireNodeExportFileVerification.class);

    public SpitfireNodeExportFileVerification(final BsimNodeData nodeData) {

        this.nodeData = nodeData;
    }

    @Override
    public boolean doVerification(final BsimOperator bsimOperator) {

        int count = 0;
        final int maximumCount = 5;

        do {
            if (bsimOperator.checkExportFileSpitfire(nodeData.getNodeType(), nodeData.getNodeName())) {
                return true;
            }

            count++;

            try {
                Thread.sleep(3000);
            } catch (final InterruptedException e) {
            }

        } while (count < maximumCount);
        log.error("Export Files not found");
        return false;

    }
}
