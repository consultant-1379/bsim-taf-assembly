/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2014 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.bsim.test.verification;

import org.apache.log4j.Logger;

import com.ericsson.oss.bsim.data.model.BsimNodeData;
import com.ericsson.oss.bsim.operators.BsimOperator;

public class PlanCreatedVerification implements IBSIMVerification {

    private final BsimNodeData nodeData;

    private final Logger log = Logger.getLogger(PlanCreatedVerification.class);

    public PlanCreatedVerification(final BsimNodeData nodeData) {

        this.nodeData = nodeData;
    }

    @Override
    public boolean doVerification(final BsimOperator bsimOperator) {
        int count = 0;
        final int maximumCount = 2;

        do {
            if (bsimOperator.checkNodeInPlan(nodeData.getNodeFdn(), nodeData.getPlanName())) {

                return true;
            }
            count++;
            try {
                Thread.sleep(3000);
            } catch (final InterruptedException e) {
            }

        } while (count < maximumCount);
        log.error("Plan not found");
        return false;
    }

}
