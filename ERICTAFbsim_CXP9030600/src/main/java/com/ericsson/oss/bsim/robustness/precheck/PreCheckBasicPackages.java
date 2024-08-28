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
import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;
import com.ericsson.cifwk.taf.tools.cli.Shell;
import com.ericsson.cifwk.taf.tools.cli.handlers.impl.RemoteObjectHandler;
import com.ericsson.oss.bsim.data.model.NodeType;
import com.ericsson.oss.bsim.getters.api.BsimApiGetter;
import com.ericsson.oss.bsim.utils.file.LocalTempFileConstants;

/**
 * @author xriskas
 */

public class PreCheckBasicPackages implements IBsimPreChecker {

    private static final String BASIC_PACKAGE_ID = "successfully imported";

    private static final String DESCRIPTION = "Basic Upgrade packages name: ";

    private static final String BASIC_PACKAGE_ALREADY_IMPORTED = "already imported";

    private static final String Basic_Package_LOCAL_FOLDER_LOCATION = new File("").getAbsolutePath().concat(
            File.separator + LocalTempFileConstants.getLocalTempDirName() + File.separator);

    private static final String Basic_Package_REMOTE_FOLDER_LOCATION = "/BPUP_packages/";

    private static final String Basic_Package_cloud_FOLDER_LOCATION = "/var/opt/ericsson/nms_smo_srv/smo_file_store/Delivery/";

    private final String[] basicPackages = { "CXP9011610_C3PO_wcdmaupdrade.zip", "m_CXP102051_1_R4D25_ltebasic_new.zip", "CXP102051%23_R13A_lteupgrade.zip",
            "m_CXP9023291_R4JA41_wcdmaupgrade.zip", "m_CXP9023290_R4A_wcdmabasic.zip", "m_CXP9030569%23_R7HE_ltebasic.zip",
            "new_CXP102051_1_R4D25_ltebasic1.zip", "new_CXP9030569%23_R7HE_ltebasic2.zip", "new_CXP9023290_R4A_wcdmabasic1.zip" };

    private RemoteObjectHandler fileHandler;

    private RemoteObjectHandler masterFileHandler;

    private String response;

    private final Logger logger = Logger.getLogger(PreCheckBasicPackages.class);

    private static boolean isComplete = false;

    private static CLICommandHelper cLICommandHelper = BsimApiGetter.getCLICommandHelper(BsimApiGetter.getHostMaster());

    int count = 0;

    @Override
    public boolean doPreCheck(final NodeType nodeType) {
        return checkBasicPackages();
    }

    @Override
    @Test(groups = { "common.precheck", "common.precheck" }, alwaysRun = true)
    public void doPreCheck() {
        assertTrue(checkBasicPackages());

    }

    /**
     * <p>
     * Main method of test case. Checks if Basic packages are imported. If not, copies basic packages from remote server and import them.
     * </p>
     * <p>
     * Conditional complexity in method due to providing verbose logging. This logging should enable testers to pinpoint the cause of a test
     * failure easier.
     * </p>
     * 
     * @return boolean
     */
    public boolean checkBasicPackages() {

        if (!isComplete) {
            final Host ftpServer = getFtpServerHost();
            logger.info("Checking of the ftp server Host is correct.. HostName : " + ftpServer.getHostname());
            fileHandler = BsimApiGetter.getRemoteFileHandler(ftpServer);
            masterFileHandler = BsimApiGetter.getMasterHostFileHandler();

            logger.info("Checking if the instance of Remote File Handler is correct.. User : " + fileHandler.getUser());
            for (final String basicPackage : basicPackages) {
                final String basicpackageDescription = DESCRIPTION + basicPackage;

                final String localFile = Basic_Package_LOCAL_FOLDER_LOCATION + basicPackage;
                final String remoteFile = Basic_Package_REMOTE_FOLDER_LOCATION + basicPackage;
                final String cloudFile = Basic_Package_cloud_FOLDER_LOCATION + basicPackage;

                logger.info("Local file : " + localFile + " Remote file" + remoteFile + " cloud file " + cloudFile);

                if (checkIfBasicPackageIsPresentOnFtpServer(remoteFile)) {
                    if (!checkIfBasicPackageIsPresentinworkspace(localFile)) {
                        if (transferPackageFromFtpServer(remoteFile, localFile)) {
                            if (transferBasicPackageToCloud(localFile, cloudFile)) {
                                logger.info("Packages transferred to Local server Succesfully.");
                            } else {
                                logger.error("Basic packages failed to be transferred to Local server");
                                return false;
                            }
                        } else {
                            logger.error("Basic packages failed to be transferred from remote server");
                            return false;
                        }
                    } else {
                        if (transferBasicPackageToCloud(localFile, cloudFile)) {
                            logger.info("Packages transferred to Local server Succesfully.");
                        } else {
                            logger.error("Basic packages failed to be transferred to Local server");
                            return false;
                        }
                    }

                } else {
                    logger.error("Basic packages are not present on remote server. Put Basic packages on remote server in /BPUP_packages");
                    return false;
                }

                response = importvalidBasicpackages(basicPackage, basicpackageDescription);
                logger.info("Response is   :" + response);
                if (response.contains(BASIC_PACKAGE_ALREADY_IMPORTED)) {
                    logger.info(basicPackage + " has already being imported");
                    deletePackageFromLocal(basicPackage);
                } else if (response.contains(BASIC_PACKAGE_ID)) {
                    logger.info(basicPackage + " has been successfully imported");
                } else {
                    logger.error(basicPackage + " has not being imported. Test failure");
                    return false;
                }
                deleteBasicPackageFromLocal(basicPackage, localFile);
            }
            isComplete = true;
            logger.info("PreCheckBasicPackage tests have passed. Basic Packages have been uploaded to SHM");
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
    private void deleteBasicPackageFromLocal(final String basicPackage, final String localFile) {

        final Path path = Paths.get(localFile);
        try {
            if (!Files.deleteIfExists(path)) {
                logger.warn("Basic Packages must be deleted from local file system and not be pushed to central TAF repository (due to size of files)");
            } else {
                logger.info(basicPackage + " has been deleted from repository");
            }
        } catch (final IOException e) {
            logger.warn("Delete Package failed, delete manually");
            e.printStackTrace();
        }
    }

    private boolean checkIfBasicPackageIsPresentOnFtpServer(final String basicpackage) {
        logger.info("Basic Package->>>> " + basicpackage);
        logger.info("Value to be returned->>>>> " + fileHandler.remoteFileExists(basicpackage));
        return fileHandler.remoteFileExists(basicpackage);

    }

    private boolean checkIfBasicPackageIsPresentinworkspace(final String installLicense) {
        logger.info("Basic Package-->>>> " + installLicense);
        logger.info("Value to be returned->>>>> " + fileHandler.remoteFileExists(installLicense));
        return fileHandler.remoteFileExists(installLicense);

    }

    private boolean transferPackageFromFtpServer(final String remoteFile, final String localFile) {
        logger.info("Transferring " + remoteFile + " to " + localFile + "...");
        return fileHandler.copyRemoteFileToLocal(remoteFile, localFile);

    }

    private boolean transferBasicPackageToCloud(final String localFile, final String cloudFile) {
        logger.info("Transferring " + localFile + " to " + cloudFile + "...");
        // return remoteFilesHandler.transferLocalFilesToRemote(masterFileHandler, cloudFile, java.util.Arrays.asList(localFile));
        return masterFileHandler.copyLocalFileToRemote(localFile, cloudFile);

    }

    public Host getFtpServerHost() {

        return DataHandler.getHostByName("ftpserver");

    }

    @Override
    public String getCheckDescription() {
        return "Checking Basic Package-...";
    }

    Shell shell;

    public String importvalidBasicpackages(final String filename, final String hiddenDescription) {
        shell = cLICommandHelper.openShell();
        shell.writeln("cd /var/opt/ericsson/nms_smo_srv/smo_file_store/Delivery/");
        if (count == 0) {
            runprerequisiteCommand();
            count++;
        }
        shell.writeln("smotool import " + filename);
        final String output = shell.expect("imported", 540);
        shell.disconnect();
        return output;
    }

    public void runprerequisiteCommand() {
        shell.writeln("smotool _setmainftpservice -networktype Lran -type SwStore -name l-sws-nedssv4");
        shell.writeln("smotool _setmainftpservice -networktype Utran -type SwStore -name w-sws-nedssv4");
    }

    public void deletePackageFromLocal(final String basicPackage) {
	logger.info(basicPackage + " has been deleted from Cloud server");
        cLICommandHelper.simpleExec("rm -rf /var/opt/ericsson/nms_smo_srv/smo_file_store/Delivery/" + basicPackage);
    }
}
