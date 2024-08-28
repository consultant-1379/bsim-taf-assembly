/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.bsim.test.verification;

import org.apache.log4j.Logger;

import com.ericsson.oss.bsim.data.model.BsimNodeData;
import com.ericsson.oss.bsim.operators.BsimOperator;

/**
 * @author xsonaro
 */
public class DG2AIFiles implements IBSIMVerification {

    private final BsimNodeData nodeData;

    private final Logger log = Logger.getLogger(DG2AIFiles.class);

    public DG2AIFiles(final BsimNodeData nodeData) {
        this.nodeData = nodeData;

    }

    @Override
    public boolean doVerification(final BsimOperator bsimOperator) {
        // TODO Auto-generated method stub (Oct 12, 2015:3:16:18 PM by xsonaro)

        int count = 0;
        final int maximumCount = 5;

        do {
            if (bsimOperator.checkDG2AIFiles(nodeData)) {

                return true;
            }
            count++;
            try {
                Thread.sleep(3000);
            } catch (final InterruptedException e) {
            }

        } while (count < maximumCount);
        log.error("AI Templates not found");
        return false;
    }

}
