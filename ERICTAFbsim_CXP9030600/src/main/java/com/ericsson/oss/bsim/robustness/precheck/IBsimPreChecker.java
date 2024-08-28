package com.ericsson.oss.bsim.robustness.precheck;

import com.ericsson.oss.bsim.data.model.NodeType;

public interface IBsimPreChecker {

	boolean doPreCheck(final NodeType nodeType);

	public String getCheckDescription();
	
	void doPreCheck();
}
