/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2014 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.bsim.test.verification;

import org.apache.log4j.Logger;

import com.ericsson.oss.bsim.data.model.BsimNodeData;
import com.ericsson.oss.bsim.operators.BsimOperator;

public class BcgFilesVerification implements IBSIMVerification {

    private final BsimNodeData nodeData;

    private final Logger log = Logger.getLogger(BcgFilesVerification.class);

    public BcgFilesVerification(final BsimNodeData nodeData) {

        this.nodeData = nodeData;
    }

    @Override
    public boolean doVerification(final BsimOperator bsimOperator) {
        int count = 0;
        final int maximumCount = 5;

        do {
            if (bsimOperator.checkBcgFiles(nodeData)) {

                return true;
            }
            count++;
            try {
                Thread.sleep(30000);
            } catch (final InterruptedException e) {
            }

        } while (count < maximumCount);
        log.error("BCG Files not found");
        return false;
    }

}
