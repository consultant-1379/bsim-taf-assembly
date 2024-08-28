/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2014 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.bsim.test.verification;

import com.ericsson.oss.bsim.operators.BsimOperator;

/**
 * Interface to be used for verification tasks
 * 
 * @author ebrimah
 */
public interface IBSIMVerification {

    boolean doVerification(final BsimOperator bsimOperator);
}
