//package com.ericsson.oss.bsim.operators.api;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Scanner;
//
//import javax.inject.Singleton;
//
//import org.apache.log4j.Logger;
//
//import com.ericsson.cifwk.taf.ApiOperator;
//import com.ericsson.cifwk.taf.annotations.Operator;
//import com.ericsson.cifwk.taf.data.DataHandler;
//import com.ericsson.cifwk.taf.data.Host;
//import com.ericsson.cifwk.taf.handlers.implementation.SshRemoteCommandExecutor;
//import com.ericsson.cifwk.taf.osgi.client.ApiClient;
//import com.ericsson.cifwk.taf.osgi.client.ApiContainerClient;
//import com.ericsson.cifwk.taf.osgi.client.ContainerNotReadyException;
//import com.ericsson.cifwk.taf.osgi.client.JavaApi;
//import com.ericsson.cifwk.taf.utils.FileFinder;
//import com.ericsson.oss.bsim.getters.api.BsimApiGetter;
//
//@Operator
//@Singleton
//public class OsgiApiOperatorV1 implements ApiOperator, OsgiClientOperator {
//
//	private static Logger log = Logger.getLogger(OsgiApiOperatorV1.class);
//
//	private static final Host masterHost = DataHandler
//			.getHostByName("ossmaster");
//
//	private static final Long CEX_START_TIME = 50000L;
//
//	private static final String CEX_CONFIG = DataHandler.getAttribute(
//			"cex_config").toString();
//
//	private static final String CEX_SCRIPT = DataHandler.getAttribute(
//			"cex_script").toString();
//
//	private static final String XDISPLAY = DataHandler.getAttribute("xdisplay")
//			.toString();
//
//	private static ApiClient client;
//
//	@Override
//	public synchronized ApiClient getClient() {
//
//		return getOsgiClient();
//	}
//
//	@Override
//	public synchronized void startCex() {
//		try {
//			prepareCex();
//		} catch (ContainerNotReadyException | IOException e) {
//			throw new RuntimeException("Cannot construct OSGi client", e);
//		}
//
//	}
//
//	@Override
//	public void close() {
//		// TODO Auto-generated method stub
//
//	}
//
//	public static ApiClient getOsgiClient() {
//		if (client == null) {
//			final String endPoint = "http://" + masterHost.getIp() + ":"
//					+ ApiContainerClient.getDataPort()
//					+ ApiContainerClient.AGENT_URI;
//			client = JavaApi.createApiClient(endPoint);
//			log.info(endPoint);
//		}
//
//		return client;
//	}
//
//	public static void main(final String[] args)
//			throws ContainerNotReadyException, IOException {
//
//		prepareCex();
//	}
//
//	public static void prepareCex() throws ContainerNotReadyException,
//			IOException {
//
//		// check status of osgi container. If not available, launch CEX and also
//		// deploy plugin's automatically
//		if (!getOsgiClient().isAlive()) {
//			OsgiApiOperatorV1.launchCEX();
//		}
//
//		// deploy osgi remote parts, our groovy files
//		OsgiApiOperatorV1.registerOSGIRemoteParts();
//	}
//
//	private static void launchCEX() throws ContainerNotReadyException {
//
//		// update the role/group of the config file so that nmsadm account can
//		// update it
//		checkAndUpdateConfigFile();
//
//		log.info("Cannot start communication with the client. Will start the container now");
//		final ApiContainerClient osgiContainer = new ApiContainerClient(
//				masterHost, CEX_SCRIPT, CEX_CONFIG);
//		osgiContainer.prepare(DataHandler.getAttribute("xdisplay").toString(),
//				CEX_START_TIME);
//	}
//
//	private static void checkAndUpdateConfigFile() {
//
//		final SshRemoteCommandExecutor rootExecutor = BsimApiGetter
//				.getSshMasterRootExecutor();
//
//		final String updateCommand = "chown nmsadm:nms " + CEX_CONFIG;
//		rootExecutor.simplExec(updateCommand);
//		rootExecutor.disconnect();
//		log.info("Change the owner of file [" + CEX_CONFIG + "] to nmsadm:nms ");
//
//		// final SshRemoteCommandExecutor rootExecutor =
//		// BsimApiGetter.getSshMasterRootExecutor();
//		// final String checkCommand = "ls -l " + CEX_CONFIG;
//		// final String resp = rootExecutor.simplExec(checkCommand, false);
//		// log.info("The check result of the owner of CEX config file:\r\n" +
//		// resp);
//		// final List<String> vals = Arrays.asList(resp.split("\\s+"));
//		// if (vals.contains("nmsadm") && vals.contains("nms")) {
//		// log.info("The config file [" + CEX_CONFIG + "] has the right owner");
//		// }
//		// else {
//		// final String updateCommand = "chown nmsadm:nms " + CEX_CONFIG;
//		// rootExecutor.simplExec(updateCommand);
//		// log.info("Change the owner of fie [" + CEX_CONFIG +
//		// "] to nmsadm:nms ");
//		// }
//	}
//
//	private static void registerOSGIRemoteParts() throws IOException {
//
//		final String workingDir = System.getProperty("user.dir");
//		System.out.println("Current working directory =====> " + workingDir);
//
//		final LinkedHashMap<String, String> groovyFiles = new LinkedHashMap<String, String>();
//		final List<String> rawGroovyFiles = FileFinder.findFile(".groovy");
//		for (final String fullFilePath : rawGroovyFiles) {
//			System.out.println(fullFilePath);
//			// filter files
//			if (fullFilePath.toLowerCase().contains(
//					".jar/scripts/".replace("/", File.separator))) {
//				final int idx1 = fullFilePath.lastIndexOf("\\");
//				final int idx2 = fullFilePath.lastIndexOf("/");
//				final int idx = idx1 > idx2 ? idx1 : idx2;
//				final String fileName = idx >= 0 ? fullFilePath
//						.substring(idx + 1) : fullFilePath;
//				groovyFiles.put(fileName, fullFilePath);
//			}
//		}
//
//		if (groovyFiles.size() > 0) {
//			for (final String resource : groovyFiles.values()) {
//				// get the class name of groovy file
//				final String fileName = resource.substring(resource
//						.lastIndexOf(File.separator) + 1);
//				final String remotePart = fileName.substring(0,
//						fileName.indexOf(".groovy"));
//
//				// register groovy class to Osgi container
//				if (getOsgiClient().register(readResource(resource)).getValue()
//						.equals(remotePart)) {
//					log.info("Deploy OSGI remote part " + remotePart
//							+ " successfully.");
//				} else {
//					throw new IOException("Cannot deploy OSGi remote part: "
//							+ remotePart);
//				}
//			}
//		}
//	}
//
//	private static String readResource(final String path) throws IOException {
//
//		// final ClassLoader classLoader =
//		// OsgiApiOperator.class.getClassLoader();
//		// final InputStream in = classLoader.getResourceAsStream(path);
//		final InputStream in = new FileInputStream(path);
//		Scanner scanner = null;
//		try {
//			scanner = new Scanner(in);
//			return scanner.useDelimiter("\\A").next();
//		} finally {
//			if (scanner != null) {
//				scanner.close();
//			}
//		}
//	}
//
// }
