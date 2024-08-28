package com.ericsson.oss.bsim.test.cases;

import javax.inject.Inject;

import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TestCase;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.DataDriven;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.oss.bsim.data.model.BsimNodeData;
import com.ericsson.oss.bsim.operators.api.BsimAddNodeApiOperator;
import com.ericsson.oss.cex.operator.CEXOperator;

public class AddLTEMacroNode extends TorTestCaseHelper implements TestCase {

    @Inject
    private CEXOperator operator;

    @Inject
    private BsimAddNodeApiOperator addNodeOperator;

    @Test(groups = { "KGB, CDB, GAT, Feature" })
    @Context(context = { Context.API })
    @TestId(id = "OSS-34386_Func_1", title = "Add and Auto-integrate LTE Macro Node using BSIM")
    @DataDriven(name = "LTETestDataSource")
    public void addLTEMacroNode(
            @Input("isEndtoEnd") final boolean isEndtoEnd,
            @Input("numberOfNodes") final int numberOfNodes,
            @Input("bsimNodeData") final BsimNodeData bsimNodeData) {

        setTestStep("Execute add node operation on bsimNodeData");
        try {
            addNodeOperator.setClient(operator.getOsgiClient());
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
        addNodeOperator.addNode(bsimNodeData);
        addNodeOperator.executeAddNodeCommandOnBsimServer(String.valueOf(1));

    }

}
