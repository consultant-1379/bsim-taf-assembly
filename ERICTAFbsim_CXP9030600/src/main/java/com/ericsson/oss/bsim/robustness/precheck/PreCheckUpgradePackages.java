package com.ericsson.oss.bsim.robustness.precheck;

import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.log4j.Logger;
import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.tools.cli.handlers.impl.RemoteObjectHandler;
import com.ericsson.cifwk.taf.tools.http.HttpResponse;
import com.ericsson.oss.bsim.data.model.NodeType;
import com.ericsson.oss.bsim.getters.api.BsimApiGetter;
import com.ericsson.oss.bsim.operators.api.ImportAndUpgradePackageRestOperator;
import com.ericsson.oss.bsim.utils.file.LocalTempFileConstants;

/**
 * @author ejomclo
 *         <p style="color:red">
 *         <b> Important: The following needs to be set in OssHost.properties for test case to work</b>
 *         </p>
 *         <p>
 *         host.gateway.node.shmgui.tunnel=1
 *         </p>
 *         <p>
 *         host.gateway.node.shmgui.ip=192.168.0.12
 *         </p>
 *         <p>
 *         host.gateway.node.shmgui.port.http=50503
 *         </p>
 *         <p>
 *         host.gateway.node.shmgui.user.nmsadm.pass=nms27511
 *         </p>
 *         </br>
 *         <p>
 *         host.ftpserver.ip=atrcxb2101.athtem.eei.ericsson.se (server holding upgrade Packages)
 *         </p>
 *         <p>
 *         host.ftpserver.user.root.pass=shroot
 *         </p>
 *         <p>
 *         host.ftpserver.user.root.type=admin
 *         </p>
 *         <p>
 *         host.ftpserver.port.ssh=22
 *         </p>
 *         <p>
 *         host.ftpserver.type=PEER
 *         </p>
 */

public class PreCheckUpgradePackages implements IBsimPreChecker {

    private static final String UPGRADE_PACKAGE_ID = "upgradePackageId";

    private static final String DESCRIPTION = "Pico upgrade package name: ";

    private static final String UPGRADE_PACKAGE_ALREADY_IMPORTED = "upgrade package has already been imported";

    private static final String UPGRADE_PACKAGES_LOCAL_FOLDER_LOCATION = new File("").getAbsolutePath().concat(
            File.separator + LocalTempFileConstants.getLocalTempDirName() + File.separator);

    private static final String UPGRADE_PACKAGES_REMOTE_FOLDER_LOCATION = "/upgradePackages/";

    private final String[] upgradePackages = { "1_19089-CXP9023306_1_X_AK_PKZIPV2R04.zip", "1_19089-CXP9024371_1_X_B_PKZIPV2R04.zip",
            "1_19089-CXP9024371_1_X_F_PKZIPV2R04.zip", "CXP9024418_2-R3EG.zip", "CXP9024418_2-R5HS.zip", "CXP9024418_2-R5LF.zip" };

    private ImportAndUpgradePackageRestOperator importAndUpgradePackageRestOperator;

    private RemoteObjectHandler fileHandler;

    private HttpResponse response;

    private final Logger logger = Logger.getLogger(PreCheckUpgradePackages.class);

    private static boolean isComplete = false;

    @Override
    public boolean doPreCheck(final NodeType nodeType) {
        return checkUpgradePackages();
    }

    @Override
    @Test(groups = { "common.precheck", "common.precheck" }, alwaysRun = true)
    public void doPreCheck() {
        assertTrue(checkUpgradePackages());

    }

    /**
     * <p>
     * Main method of test case. Checks if upgrade packages have been uploaded to SHM. If not, copies upgrade packages from remote server
     * and uploads them to SHM
     * </p>
     * <p>
     * Conditional complexity in method due to providing verbose logging. This logging should enable testers to pinpoint the cause of a test
     * failure easier.
     * </p>
     * 
     * @return boolean
     */
    public boolean checkUpgradePackages() {

        if (!isComplete) {
            importAndUpgradePackageRestOperator = new ImportAndUpgradePackageRestOperator();
            final Host ftpServer = getFtpServerHost();
            logger.info("Checking of the ftp server Host is correct.. HostName : " + ftpServer.getHostname());
            fileHandler = BsimApiGetter.getRemoteFileHandler(ftpServer);
            logger.info("Checking if the instance of Remote File Handler is correct.. User : " + fileHandler.getUser());
            for (final String upgradePackage : upgradePackages) {
                final String upgradePackageDescription = DESCRIPTION + upgradePackage;
                response = importAndUpgradePackageRestOperator.getListOfImportedUpgradePackagesFromShm(upgradePackage, upgradePackageDescription);
                logger.info("Response from server to GET: " + response.getBody());
                if (response.getBody().contains("\"totalCount\":3")) {
                    logger.info("All upgrade packages are present on server. Precheck passed");
                    break;
                }
                if (response == null || !response.getBody().contains(upgradePackageDescription)) {
                    logger.info(upgradePackage + " is not present in SHM. Beginning transfer and upload...");
                    final String localFile = UPGRADE_PACKAGES_LOCAL_FOLDER_LOCATION + upgradePackage;
                    final String remoteFile = UPGRADE_PACKAGES_REMOTE_FOLDER_LOCATION + upgradePackage;
                    logger.info("Local and Remote file : " + localFile + "    " + remoteFile);
                    if (!checkIfUpgradePackadgeIsPresentLocally(localFile)) {
                        if (checkIfUpgradePackadgeIsPresentOnFtpServer(remoteFile)) {
                            if (!transferPackageFromFtpServer(remoteFile, localFile)) {
                                logger.error("Upgrade package failed to be transferred from remote server");
                                return false;
                            }
                        } else {
                            logger.error("Upgrade packages are not present on remote server. Put upgrade packages on remote server in /upgradePackages");
                            return false;
                        }

                    }
                    response = importAndUpgradePackageRestOperator.importvalidupgradepackage(upgradePackage, upgradePackageDescription);
                    if (response == null) {
                        logger.error("Received invalid response from server. Test failure");
                        return false;
                    }
                    logger.info("Response is   :" + response.getBody());
                    if (response.getBody().contains(UPGRADE_PACKAGE_ALREADY_IMPORTED)) {
                        logger.info(upgradePackage + " has already being imported");
                    } else if (response.getBody().contains(UPGRADE_PACKAGE_ID)) {
                        logger.info(upgradePackage + " has been successfully imported");
                    } else {
                        logger.error(upgradePackage + " has not being imported.");
                        if (!retryUpgradePackageImport(upgradePackage, upgradePackageDescription)) {
                            return false;
                        }
                    }
                    deleteUpgradePackageFromLocal(upgradePackage, localFile);
                }
                logger.info(upgradePackage + " has already been uploaded to SHM");
            }
            isComplete = true;
            logger.info("PreCheckUpgradePackages tests have passed. Upgrade packages have been uploaded to SHM");
        }
        return true;

    }
    public boolean retryUpgradePackageImport(final String upgradePackage, final String upgradePackageDescription) {
        logger.info("Import of " + upgradePackage + " failed.Retrying....");
        response = importAndUpgradePackageRestOperator.importvalidupgradepackage(upgradePackage, upgradePackageDescription);
        if (response == null) {
            logger.error("Received invalid response from server. Test failure");
            return false;
        }
        logger.info("Response is   :" + response.getBody());
        if (response.getBody().contains(UPGRADE_PACKAGE_ALREADY_IMPORTED)) {
            logger.info(upgradePackage + " has already being imported");
        } else if (response.getBody().contains(UPGRADE_PACKAGE_ID)) {
            logger.info(upgradePackage + " has been successfully imported");
        } else {
            logger.error(upgradePackage + " has not being imported. Test failure");
            return false;
        }
        return true;
    }
    /**
     * @param upgradePackage
     * @param localFile
     *        <p>
     *        Method deletes upgrade packages files from local repository to ensure they are not pushed to remote git repository
     *        </p>
     */
    private void deleteUpgradePackageFromLocal(final String upgradePackage, final String localFile) {

        final Path path = Paths.get(localFile);
        try {
            if (!Files.deleteIfExists(path)) {
                logger.warn("Upgrade Packages must be deleted from local file system and not be pushed to central TAF repository (due to size of files)");
            } else {
                logger.info(upgradePackage + " has been deleted from repository");
            }
        } catch (final IOException e) {
            logger.warn("Delete upgrade package failed, delete manually");
            e.printStackTrace();
        }
    }

    private boolean checkIfUpgradePackadgeIsPresentOnFtpServer(final String upgradePackage) {
        logger.info("Upgrade package->>>> " + upgradePackage);
        logger.info("Value to be returned->>>>> " + fileHandler.remoteFileExists(upgradePackage));
        return fileHandler.remoteFileExists(upgradePackage);

    }

    private boolean transferPackageFromFtpServer(final String remoteFile, final String localFile) {
        logger.info("Transferring " + remoteFile + " to " + localFile + "...");
        return fileHandler.copyRemoteFileToLocal(remoteFile, localFile);

    }

    private boolean checkIfUpgradePackadgeIsPresentLocally(final String upgradePackage) {
        return new File(upgradePackage).exists();
    }

    public Host getFtpServerHost() {
        // DataHandler.getHostByType(HostType.RC);// workaround for TAF issue with DataHandler.java, do not remove
        // see http://jira-oss.lmera.ericsson.se/i#browse/CIP-5962 for progress on issue
        // can remove only if issue is resolved
        return DataHandler.getHostByName("ftpserver");
        // return DataHandler.getHostByType(HostType.PEER);
    }

    @Override
    public String getCheckDescription() {
        return "Checking upgrade packages...";
    }

}
