/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2014 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.bsim.test.verification;

import org.apache.log4j.Logger;

import com.ericsson.oss.bsim.data.model.BsimNodeData;
import com.ericsson.oss.bsim.operators.BsimOperator;

public class ScramblingCodeVerification implements IBSIMVerification {

    private final BsimNodeData nodeData;

    private final Logger log = Logger.getLogger(ScramblingCodeVerification.class);

    public ScramblingCodeVerification(final BsimNodeData nodeData) {

        this.nodeData = nodeData;
    }

    @Override
    public boolean doVerification(final BsimOperator bsimOperator) {
        int count = 0;
        final int maximumCount = 5;

        do {
            if (bsimOperator.checkSCValuesByCmd(nodeData.getNodeName(), nodeData.getPlanName(), nodeData.CriticalData.getRncName())) {

                return true;
            }
            count++;
            try {
                Thread.sleep(3000);
            } catch (final InterruptedException e) {
            }

        } while (count < maximumCount);
        log.error("Scrambling code values not found");
        return false;
    }
}
