package com.ericsson.oss.bsim.robustness;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.oss.bsim.data.model.BsimNodeData;
import com.ericsson.oss.bsim.robustness.validation.IBSIMValidator;
import com.ericsson.oss.bsim.robustness.validation.MimVersionValidator;
import com.ericsson.oss.bsim.robustness.validation.TemplateExistenceValidator;

/**
 * The <code>AddNodeValidationManager</code> class is is part of robustness improvement feature. It used to manage the
 * all validations of test data for a single test case.
 * <p>
 * Normally, it validation of test data is failed, the test case will failed directly without do the execution of test case.
 * <p>
 * It saves time and make it easier to know the problem of the test case or test data in test case level.
 *
 * @author exuuguu
 */
public class AddNodeValidationManager {

    List<IBSIMValidator> validatorList = new ArrayList<IBSIMValidator>();

    /**
     * In the constructor, the validatorList is initialized. Every validator in the list will be executed one by one
     * when run doAllValidations process.
     */
    public AddNodeValidationManager() {

        // add all the expected validators to the validatorList
        validatorList.add(new MimVersionValidator());
        validatorList.add(new TemplateExistenceValidator());
    }

    /**
     * Do all the validations against the validators in the validatorList. If all the validations are passed, then it is
     * successful. Otherwise, the validation will be failed only if one of the single validation is failed.
     *
     * @return <code>true</code> if all the validations are successful; <code>false</code> otherwise.
     */
    public boolean doAllValidations(final BsimNodeData nodeData) {

        boolean isValidated = true;

        for (final IBSIMValidator validator : validatorList) {
            if (!validator.doValidation(nodeData)) {
                isValidated = false;
            }
        }

        return isValidated;
    }
}
