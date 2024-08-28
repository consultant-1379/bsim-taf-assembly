package com.ericsson.oss.bsim.operators.api;

import com.ericsson.cifwk.taf.ApiOperator;
import com.ericsson.cifwk.taf.osgi.client.ApiClient;

public class BsimRetrieveServerDataOperator implements ApiOperator {

    private final ApiClient client = ClientHelper.getClient();

    public String getFirstAvailableRnc() {

        return client.invoke("MoDetailsRetriever", "getFirstAvailableRnc").getValue();
    }

    public String getFirstSubnetworkByNetworkType(final String networkType) {

        return client.invoke("BsimOsgiUtility", "getFirstSubnetworkByNetworkType", networkType).getValue();
    }

    public String getFirstFtpServiceByFtpAndNetworkType(final String ftpType, final String networkType) {

        return client.invoke("BsimOsgiUtility", "getFirstFtpServiceByFtpAndNetworkType", ftpType, networkType).getValue();
    }

    public String getAvailableSiteName() {

        return client.invoke("MoDetailsRetriever", "getAvailableSiteName").getValue();
    }

    public String loadScProfile(final String nodeName) {

        return client.invoke("BsimOsgiUtility", "loadScProfile", "").getValue();
    }
}
