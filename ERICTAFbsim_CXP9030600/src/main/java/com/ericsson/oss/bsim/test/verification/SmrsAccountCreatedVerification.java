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
 * @author eshaosu
 */
public class SmrsAccountCreatedVerification implements IBSIMVerification {

    private final BsimNodeData nodeData;

    private final boolean expectedResult;

    private final Logger log = Logger.getLogger(SmrsAccountCreatedVerification.class);

    public SmrsAccountCreatedVerification(final BsimNodeData nodeData, final boolean expectedResult) {

        this.nodeData = nodeData;
        this.expectedResult = expectedResult;
    }

    @Override
    public boolean doVerification(final BsimOperator bsimOperator) {

        return bsimOperator.checkDoesSmrsAccountExistforNode(nodeData, expectedResult);
    }

}
