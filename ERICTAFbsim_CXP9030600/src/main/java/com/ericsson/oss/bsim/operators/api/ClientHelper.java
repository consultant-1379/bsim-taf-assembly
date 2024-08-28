package com.ericsson.oss.bsim.operators.api;

import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.osgi.client.ApiClient;
import com.ericsson.oss.cex.operator.CEXOperator;

public class ClientHelper extends CEXOperator {

    private static ApiClient apiClient;

    public ClientHelper(final Host rcHost) {
        super(rcHost);
    }

    /**
     * Get OSGI Client
     *
     * @return osgi client
     */
    public static ApiClient getClient() {
        return apiClient;
    }

    /**
     * set client to be used in GroovyTestOperators.
     *
     * @param client
     */
    public void setClient(final ApiClient client) {
        apiClient = client;
    }
}
