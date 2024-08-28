package com.ericsson.oss.bsim.robustness.precheck;

import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.log4j.Logger;
import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.tools.cli.handlers.impl.RemoteObjectHandler;
import com.ericsson.cifwk.taf.tools.http.HttpResponse;
import com.ericsson.cifwk.taf.tools.http.HttpTool;
import com.ericsson.cifwk.taf.tools.http.HttpToolBuilder;
import com.ericsson.cifwk.taf.tools.http.constants.ContentType;
import com.ericsson.cifwk.taf.utils.FileFinder;
import com.ericsson.oss.bsim.data.model.NodeType;
import com.ericsson.oss.bsim.getters.api.BsimApiGetter;
import com.ericsson.oss.bsim.utils.file.LocalTempFileConstants;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;
import com.google.common.net.HttpHeaders;

/**
 * @author xriskas
 */

public class PreCheckInstallLicense implements IBsimPreChecker {

    private static final String IMPORT_LICENSE_ID = "ImportLicenses";

    private static final String DESCRIPTION = "Install License name: ";

    private static final String INSTALL_LICENSE_ALREADY_IMPORTED = "already imported";

    private static final String INSTALL_LICENSE_LOCAL_FOLDER_LOCATION = new File("").getAbsolutePath().concat(
            File.separator + LocalTempFileConstants.getLocalTempDirName() + File.separator);

    private static final String INSTALL_LICENSE_REMOTE_FOLDER_LOCATION = "/installLicense/";

    private final String[] installLicenses = { "D821781432_160330_182532.zip", "G2RBS_W_258.zip" };

    private RemoteObjectHandler fileHandler;

    private HttpResponse response;

    private final Logger logger = Logger.getLogger(PreCheckInstallLicense.class);

    private static boolean isComplete = false;

    private HttpResponse postResponse;

    private final Host serverHost = HostGroup.getMasterServer();

    private final HttpTool tool = HttpToolBuilder.newBuilder(serverHost).build();

    private String uri;

    public static final String ACCEPT = "Accept";

    @Override
    public boolean doPreCheck(final NodeType nodeType) {
        return checkInstallLicenses();
    }

    @Override
    @Test(groups = { "common.precheck", "common.precheck" }, alwaysRun = true)
    public void doPreCheck() {
        assertTrue(checkInstallLicenses());

    }

    /**
     * <p>
     * Main method of test case. Checks if Install License have been uploaded to SHM. If not, copies Install License from remote server and
     * uploads them to SHM
     * </p>
     * <p>
     * Conditional complexity in method due to providing verbose logging. This logging should enable testers to pinpoint the cause of a test
     * failure easier.
     * </p>
     * 
     * @return boolean
     */
    public boolean checkInstallLicenses() {

        if (!isComplete) {
            final Host ftpServer = getFtpServerHost();
            logger.info("Checking of the ftp server Host is correct.. HostName : " + ftpServer.getHostname());
            fileHandler = BsimApiGetter.getRemoteFileHandler(ftpServer);
            logger.info("Checking if the instance of Remote File Handler is correct.. User : " + fileHandler.getUser());
            for (final String installLicense : installLicenses) {
                final String installLicenseDescription = DESCRIPTION + installLicense;

                final String localFile = INSTALL_LICENSE_LOCAL_FOLDER_LOCATION + installLicense;
                final String remoteFile = INSTALL_LICENSE_REMOTE_FOLDER_LOCATION + installLicense;
                logger.info("Local and Remote file : " + localFile + "    " + remoteFile);
                if (!checkIfInstallLicenseIsPresentLocally(localFile)) {
                    if (checkIfInstallLicenseIsPresentOnFtpServer(remoteFile)) {
                        if (!transferLicenseFromFtpServer(remoteFile, localFile)) {
                            logger.error("Install License failed to be transferred from remote server");
                            return false;
                        }
                    } else {
                        logger.error("Install License are not present on remote server. Put Install License on remote server in /installLicense");
                        return false;
                    }

                }
                response = importvalidInstallLicense(installLicense, installLicenseDescription);
                if (response == null) {
                    logger.error("Received invalid response from server. Test failure");
                    return false;
                }
                logger.info("Response is   :" + response.getBody());
                if (response.getBody().contains(INSTALL_LICENSE_ALREADY_IMPORTED)) {
                    logger.info(installLicense + " has already being imported");
                } else if (response.getBody().contains(IMPORT_LICENSE_ID)) {
                    logger.info(installLicense + " has been successfully imported");
                } else {
                    logger.error(installLicense + " has not being imported. Test failure");
                    return false;
                }
                deleteInstallLicenseFromLocal(installLicense, localFile);
            }
            isComplete = true;
            logger.info("PreCheckInstallLicense tests have passed. Install Licenses have been uploaded to SHM");
        }
        return true;

    }

    /**
     * @param installLicense
     * @param localFile
     *        <p>
     *        Method deletes Install Licenses files from local repository to ensure they are not pushed to remote git repository
     *        </p>
     */
    private void deleteInstallLicenseFromLocal(final String installLicense, final String localFile) {

        final Path path = Paths.get(localFile);
        try {
            if (!Files.deleteIfExists(path)) {
                logger.warn("Install Licenses must be deleted from local file system and not be pushed to central TAF repository (due to size of files)");
            } else {
                logger.info(installLicense + " has been deleted from repository");
            }
        } catch (final IOException e) {
            logger.warn("Delete Install Licenses failed, delete manually");
            e.printStackTrace();
        }
    }

    private boolean checkIfInstallLicenseIsPresentOnFtpServer(final String installLicense) {
        logger.info("Install Licenses->>>> " + installLicense);
        logger.info("Value to be returned->>>>> " + fileHandler.remoteFileExists(installLicense));
        return fileHandler.remoteFileExists(installLicense);

    }

    private boolean transferLicenseFromFtpServer(final String remoteFile, final String localFile) {
        logger.info("Transferring " + remoteFile + " to " + localFile + "...");
        return fileHandler.copyRemoteFileToLocal(remoteFile, localFile);

    }

    private boolean checkIfInstallLicenseIsPresentLocally(final String installLicense) {
        return new File(installLicense).exists();
    }

    public Host getFtpServerHost() {

        return DataHandler.getHostByName("ftpserver");

    }

    @Override
    public String getCheckDescription() {
        return "Checking Install Licenses...";
    }

    public HttpResponse importvalidInstallLicense(final String filename, final String hiddenDescription) {
        try {
            logger.info("Install Licenses are statring to upload on SHM ::-----");

            uri = (String) DataHandler.getAttribute("InstallLicenseuri");
            logger.info("During import Licenses ");
            final File file = new File(FileFinder.findFile(filename).get(0));

            logger.info("Uploading " + filename + " to SHM..." + file);

            CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
            postResponse = tool.request().authenticate(serverHost.getUser(), serverHost.getPass()).header(HttpHeaders.ACCEPT, ACCEPT)
                    .contentType(ContentType.MULTIPART_FORM_DATA).body("hiddenDescription", hiddenDescription).file("uploadfile", file).post(uri);
        } catch (final Exception ex) {
            logger.error("Exception caught while uploading file on SHM is ", ex);

        }

        return postResponse;
    }

}
