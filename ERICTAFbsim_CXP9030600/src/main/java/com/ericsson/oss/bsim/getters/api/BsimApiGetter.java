package com.ericsson.oss.bsim.getters.api;

import java.util.List;

import org.apache.log4j.Logger;

import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.handlers.netsim.implementation.SshNetsimHandler;
import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;
import com.ericsson.cifwk.taf.tools.cli.handlers.impl.RemoteObjectHandler;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;
import com.ericsson.cifwk.taf.data.UserType;

public class BsimApiGetter {

    private static final Host masterHost = HostGroup.getOssmaster();// DataHandler.getHostByName("ossmaster");

    private static final Host masterRootHost = DataHandler.getHostByName("ossmasterRoot");

    private static final List<Host> netsimHost = HostGroup.getAllNetsims();// DataHandler.getHostByName("netsim");

    private static final Host infraServerHost = HostGroup.getOmsrvm();/* DataHandler.getHostByName("omsrvm"); */

    private static final Host gateway = HostGroup.getGateway();// DataHandler.getHostByName("gateway");//

    private static final Host omsasHost = HostGroup.getOmsas();// DataHandler.getHostByName("omsas");//

    private static Logger log = Logger.getLogger(BsimApiGetter.class);

    public static Host getHostMaster() {

        return masterHost;
    }

    public static Host getHostMasterRoot() {
        return masterRootHost;
    }

    public static Host getHostOmsas() {

        return omsasHost;
    }

    public static Host getHostNetsim() {
//New Netsim Host
	final Host newNetsimTestHost = HostGroup.getAllNetsims().get(0);
        newNetsimTestHost.addUser("netsim", "netsim", UserType.OPER);
        return newNetsimTestHost;
        //return netsimHost.get(0);
	
    //Host obj2 = new Host();
    //obj2.setUser("netsim");
    //obj2.setPass("netsim");
	//log.info("id and pwd are " + obj2.getUser() + ": " + obj2.getPass());
    //return obj2;
	//final List<Host> netsimHost = HostGroup.getAllNetsims();
        //final Host obj2 = netsimHost.get(0);
        //obj2.setUser("netsim");
        //obj2.setPass("netsim");
        //log.info("id and pwd are " + obj2.getUser() + ": " + obj2.getPass());
        //return obj2;
    }

    public static Host getHostInfraServer() {

        return infraServerHost;
    }

    public static Host getGateway() {

        return gateway;
    }

    // public static BsimRemoteCommandExecutor getSshMasterHostExecutor() {
    //
    // return getRemoteCommandExecutor(masterHost);
    // }
    //
    // public static BsimRemoteCommandExecutor getSshMasterRootExecutor() {
    //
    // return getRemoteCommandExecutor(masterRootHost);
    // }
    //
    // public static BsimRemoteCommandExecutor
    // getNetsimSshRemoteCommandExecutor() {
    //
    // return getRemoteCommandExecutor(netsimHost);
    // }
    //
    // public static BsimRemoteCommandExecutor getSshInfrServerExecutor() {
    //
    // return getRemoteCommandExecutor(infraServerHost);
    // }

    public static BsimRemoteCommandExecutor getRemoteCommandExecutor(final Host host) {

        return new BsimRemoteCommandExecutor(host);
    }

    public static RemoteObjectHandler getMasterHostFileHandler() {

        return getRemoteFileHandler(masterHost);
    }

    public static RemoteObjectHandler getMasterRootFileHandler() {

        return getRemoteFileHandler(masterRootHost);
    }

    public static RemoteObjectHandler getInfrServerFileHandler() {

        return getRemoteFileHandler(infraServerHost);
    }

    public static RemoteObjectHandler getNetsimRemoteFileHandler() {

        return getRemoteFileHandler(getHostNetsim());
    }

    public static RemoteObjectHandler getOmsasServerFileHandler() {

        return getRemoteFileHandler(omsasHost);
    }

    public static RemoteObjectHandler getGatewayFileHandler() {

        return getRemoteFileHandler(gateway);
    }

    public static RemoteObjectHandler getRemoteFileHandler(final Host host) {

        log.info("Executing getRemoteFileHandler...");
        if (host.getHostname().trim().equalsIgnoreCase("ossmaster")) {

            log.info("User is : " + host.getUser());
            //log.info("pass1 is : " + host.getPass());
            return new RemoteObjectHandler(host, HostGroup.getOssmaster().getNmsadmUser());
        } else if (host.getHostname().trim().equalsIgnoreCase("netsim")) {
            log.info("User is : " + host.getUser());
           // log.info("pass2 is : " + host.getPass());
            return new RemoteObjectHandler(host);
        } else if (host.getHostname().trim().equalsIgnoreCase("gateway")) {
            log.info("Usern is : " + host.getUser());
           // log.info("passn is : " + host.getPass());
            return new RemoteObjectHandler(host);
        }

        else {
            log.info("User is : " + host.getUser());
            //log.info("pass3 is : " + host.getPass());
            return new RemoteObjectHandler(host, HostGroup.getOssmaster().getRootUser());
        }

    }

    public static SshNetsimHandler getSshNetsimHandler() {

        return getSshNetsimHandler(getHostNetsim());
    }

    public static SshNetsimHandler getSshNetsimHandler(final Host netsimHost) {

        return new SshNetsimHandler(netsimHost);
    }

    public static CLICommandHelper getCLICommandHelper(final Host host) {

        log.info("Executing getCLICommandHelper...");
        if (host.getHostname().trim().equalsIgnoreCase("ossmaster")) {

            log.info("User is : " + host.getUser());
            //log.info("pass4 is : " + host.getPass());
            return new CLICommandHelper(host, HostGroup.getOssmaster().getNmsadmUser());
        } else if (host.getHostname().trim().equalsIgnoreCase("netsim")) {
            log.info("User is : " + host.getUser());
           // log.info("pass5 is : " + host.getPass());
            return new CLICommandHelper(host);
        } else {
            log.info("User is : " + host.getUser());
           // log.info("pass6 is : " + host.getPass());
            return new CLICommandHelper(host, HostGroup.getOssmaster().getRootUser());
        }

        /*
         * log.info("Executing getCLICommandHelper...");
         * return new CLICommandHelper(host, HostGroup.getOssmaster().getNmsadmUser());
         */

    }

}
