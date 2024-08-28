
package com.ericsson.oss.bsim.test.cases;

import java.io.IOException;

import javax.inject.Inject;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TestCase;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.UserType;
import com.ericsson.cifwk.taf.guice.OperatorRegistry;
import com.ericsson.cifwk.taf.osgi.client.ApiClient;
import com.ericsson.cifwk.taf.osgi.client.ContainerNotReadyException;
import com.ericsson.cifwk.taf.tools.cli.CLI;
import com.ericsson.cifwk.taf.tools.cli.Shell;
import com.ericsson.cifwk.taf.tools.cli.Terminal;
import com.ericsson.cifwk.taf.utils.cluster.PortForwardingUtility;
import com.ericsson.oss.bsim.getters.api.BsimApiGetter;
import com.ericsson.oss.bsim.getters.api.BsimRemoteCommandExecutor;
import com.ericsson.oss.bsim.operators.api.OsgiClientOperator;

public class BsimXJenkins extends TorTestCaseHelper implements TestCase {

    @Inject
    OperatorRegistry<OsgiClientOperator> operators;

    @BeforeSuite
    public void prepareCEX() throws ContainerNotReadyException, IOException {

        // test start CEX and get Osgi container ready
        operators.provide(OsgiClientOperator.class).startCex();
    }

    @Test
    public void testFoo() throws ContainerNotReadyException, IOException {

        // test Api Client
        final ApiClient client = operators.provide(OsgiClientOperator.class).getClient();
        final String respVal = client.invoke("BsimOsgiUtility", "getMimversions").getValue();
        System.out.println(respVal);

    }

    @AfterSuite
    public void closeCEX() {

        // test close Osgi container
        operators.provide(OsgiClientOperator.class).close();
    }

    public static void main(final String[] args) {

        testPortFaward();
    }

    public static void testPortFaward() {

        // use PortForward api
        final PortForwardingUtility utility = new PortForwardingUtility();
        final Host gateway = BsimApiGetter.getGateway();
        final String targetIp = "192.168.0.5";
        final String targetPort = "9999";
        final String gatewayIp = gateway.getIp();
        final String gatewayRootPassword = gateway.getPass(UserType.ADMIN);
        utility.configureForwarding(targetIp, targetPort, gatewayIp, gatewayRootPassword);

        // // use remote executor
        // final Host gateway = BsimApiGetter.getGateway();
        // final SshRemoteCommandExecutor executor = new
        // SshRemoteCommandExecutor(gateway);
        // final String respVal =
        // executor.simplExec("/sbin/iptables -t nat -L PREROUTING -n");
        // System.out.println(respVal);
    }

    public static void testCLI() {

        final String cmd = String.format("ls -ltr %1$s | grep  -c %2$s", "/opt/ericsson/bin", "cex");
        String returnVal = "";

        final CLI cliExecutor = new CLI(BsimApiGetter.getHostMaster());
        Shell shell = cliExecutor.openShell(Terminal.VT100);
        shell = cliExecutor.executeCommand(Terminal.VT100, cmd);
        returnVal = shell.read();
        shell.disconnect();

        System.out.println(returnVal);
    }

    public static void testBsimCmd() {

        final BsimRemoteCommandExecutor executor = new BsimRemoteCommandExecutor(BsimApiGetter.getHostMaster());
        final String respVal = executor.simpleExec("cd /opt/ericsson/nms_umts_bsim_cli/bin/; bsim.sh -h");
        System.out.println(respVal);
    }
}
