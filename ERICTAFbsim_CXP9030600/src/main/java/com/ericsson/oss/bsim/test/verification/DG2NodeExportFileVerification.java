/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2014 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.bsim.test.verification;

import org.apache.log4j.Logger;

import com.ericsson.oss.bsim.data.model.BsimNodeData;
import com.ericsson.oss.bsim.operators.BsimOperator;

public class DG2NodeExportFileVerification implements IBSIMVerification {

    private final BsimNodeData nodeData;

    private final Logger log = Logger.getLogger(DG2NodeExportFileVerification.class);

    public DG2NodeExportFileVerification(final BsimNodeData nodeData) {

        this.nodeData = nodeData;
    }

    @Override
    public boolean doVerification(final BsimOperator bsimOperator) {
        int count = 0;
        final int maximumCount = 5;

        do {
            if (bsimOperator.checkExportFileDG2(nodeData.getNodeType(), nodeData.getNodeName(), nodeData.CriticalData.getRncName())) {

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
