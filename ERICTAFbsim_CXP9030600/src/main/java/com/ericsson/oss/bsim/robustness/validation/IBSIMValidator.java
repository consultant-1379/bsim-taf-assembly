
package com.ericsson.oss.bsim.robustness.validation;

import com.ericsson.oss.bsim.data.model.BsimNodeData;

public interface IBSIMValidator {

    boolean doValidation(final BsimNodeData nodeData);
}
