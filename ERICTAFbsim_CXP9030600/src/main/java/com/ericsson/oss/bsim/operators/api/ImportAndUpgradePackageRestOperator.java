package com.ericsson.oss.bsim.operators.api;

import java.io.File;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;

import org.apache.log4j.Logger;

import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.tools.http.HttpResponse;
import com.ericsson.cifwk.taf.tools.http.HttpTool;
import com.ericsson.cifwk.taf.tools.http.HttpToolBuilder;
import com.ericsson.cifwk.taf.tools.http.constants.ContentType;
import com.ericsson.cifwk.taf.utils.FileFinder;
import com.ericsson.oss.bsim.operators.ImportAndUpgradePackageOperator;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;
import com.google.common.net.HttpHeaders;

/**
 * @author ejomclo
 *         <p>
 *         The purpose of this class is to check and upload pico upgrade packages to SHM
 *         </p>
 */
@Operator
public class ImportAndUpgradePackageRestOperator implements ImportAndUpgradePackageOperator {

    public static final String ACCEPT = "Accept";

    private final Logger logger = Logger.getLogger(ImportAndUpgradePackageRestOperator.class);

    private HttpResponse postResponse;

    private final Host serverHost = HostGroup.getMasterServer();// DataHandler.getHostByName("shmgui");

    private final HttpTool tool = HttpToolBuilder.newBuilder(serverHost).build();

    private String uri;

    @Override
    public HttpResponse importvalidupgradepackage(final String filename, final String hiddenDescription) {
        try {
            logger.info("Upgarde Packages are statring to upload on SHM ::-----");

            uri = (String) DataHandler.getAttribute("Uploadpackageuri");
            logger.info("During import packages ");
            final File file = new File(FileFinder.findFile(filename).get(0));
            // final File file = new File(filename);
            logger.info("Uploading " + filename + " to SHM..." + file);
            // accept all cookies so we can authenticate against SHM reliably
            CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
            postResponse = tool.request().authenticate(serverHost.getUser(), serverHost.getPass()).header(HttpHeaders.ACCEPT, ACCEPT)
                    .contentType(ContentType.MULTIPART_FORM_DATA).body("hiddenDescription", hiddenDescription).file("uploadfile", file).post(uri);
        } catch (final Exception ex) {
            logger.error("Exception caught while uploading file on SHM is ", ex);

        }

        return postResponse;
    }

    /**
     * checkIfUpgradePackadgeIsPresentLocally
     * 
     * @param upgradePackage
     * @param description
     *        <p>
     *        Method gets uploaded package list by get request to SHM http server
     *        </p>
     *        <p>
     *        Url of GET request is https://mashost:{portNumberForServerUnderTest}/oss/shm/v1/upgrade/packages. Can check manually, once
     *        logged in to shm on server under test.
     *        </p>
     * @return HttpResponse
     */
    public HttpResponse getListOfImportedUpgradePackagesFromShm(final String upgradePackage, final String description) {

        uri = (String) DataHandler.getAttribute("listPackagesUri");
        logger.info("here uri is " + uri);
        logger.info("--->>" + serverHost.getIp());
        logger.info("Checking if " + upgradePackage + " has already been imported");
        logger.info("postResponse" + serverHost.getUser());
        try {
            postResponse = tool.request().authenticate(serverHost.getUser(), serverHost.getPass()).get(uri);
            logger.info("response of upgrade packages from SHM is :" + postResponse);
        } catch (final Exception e) {
            logger.info("exception--->>", e);
        }
        return postResponse;

    }

}

