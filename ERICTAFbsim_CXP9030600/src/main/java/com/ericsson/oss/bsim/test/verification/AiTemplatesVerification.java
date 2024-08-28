/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2014 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.bsim.test.verification;

import org.apache.log4j.Logger;

import com.ericsson.oss.bsim.data.model.BsimNodeData;
import com.ericsson.oss.bsim.operators.BsimOperator;

public class AiTemplatesVerification implements IBSIMVerification {

    private final BsimNodeData nodeData;

    private final Logger log = Logger.getLogger(AiTemplatesVerification.class);

    public AiTemplatesVerification(final BsimNodeData nodeData) {

        this.nodeData = nodeData;
    }

    @Override
    public boolean doVerification(final BsimOperator bsimOperator) {
        int count = 0;
        final int maximumCount = 6;

        do {
            if (bsimOperator.checkAITemplates(nodeData)) {

                return true;
            }
            count++;
            try {
                Thread.sleep(10000);
            } catch (final InterruptedException e) {
            }

        } while (count < maximumCount);
        log.error("AI Templates not found");
        return false;
    }

}
