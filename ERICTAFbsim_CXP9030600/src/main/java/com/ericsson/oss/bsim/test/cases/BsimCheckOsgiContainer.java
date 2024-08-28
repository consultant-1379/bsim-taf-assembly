package com.ericsson.oss.bsim.test.cases;

import java.io.IOException;

import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TestCase;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.osgi.client.ApiClient;
import com.ericsson.cifwk.taf.osgi.client.ContainerNotReadyException;
import com.ericsson.oss.bsim.operators.api.ClientHelper;

public class BsimCheckOsgiContainer extends TorTestCaseHelper implements TestCase {

    @Test
    public void testFoo() throws ContainerNotReadyException, IOException {

        final ApiClient client = ClientHelper.getClient();
        final String respVal = client.invoke("BsimOsgiUtility", "getMimversions").getValue();
        System.out.println(respVal);

    }

}
