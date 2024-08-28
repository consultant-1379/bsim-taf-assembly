package com.ericsson.oss.bsim.robustness.validation;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.ericsson.oss.bsim.data.model.BsimNodeData;
import com.ericsson.oss.bsim.data.model.NodeType;
import com.ericsson.oss.bsim.getters.api.BsimApiGetter;
import com.ericsson.oss.bsim.getters.api.BsimRemoteCommandExecutor;

/**
 * @author exuuguu
 */
public class MimVersionValidator implements IBSIMValidator {

    private static final Logger log = Logger.getLogger(MimVersionValidator.class);

    private final static BsimRemoteCommandExecutor masterRemoteCommandExecutor = BsimApiGetter.getRemoteCommandExecutor(BsimApiGetter.getHostMaster());

    private final static BsimRemoteCommandExecutor netsimRemoteCommandExecutor = BsimApiGetter.getRemoteCommandExecutor(BsimApiGetter.getHostNetsim());

    // for test purpose
    public static void main(final String[] args) {

        printServerAndMimInfo();
    }

    @Override
    public boolean doValidation(final BsimNodeData nodeData) {

        final String ossMimVersionString = nodeData.CriticalData.getOssMimVersion();
        final String netsimMimVersionString = nodeData.CriticalData.getNetsimMimVersion();

        if (!checkOssMimVersion(ossMimVersionString)) {
            return false;
        }

        if (!checkNetsimMimVersion(netsimMimVersionString)) {
            return false;
        }

        return true;
    }

    /**
     * Check whether mim version exists on master server
     * 
     * @param mimVersion
     *        the mim version on oss master server
     * @return <tt>true</tt> if the mim version does exist on the master server; <tt>false</tt> otherwise
     */
    private boolean checkOssMimVersion(final String mimVersion) {

        System.out.println("============OSS Mim Version: " + mimVersion + "============");

        if (mimVersion != null && ossMimVersions.contains(mimVersion)) {
            log.info("The mim version [" + mimVersion + "] does exist in oss master server.");
            return true;
        } else {
            log.info("The mim version [" + mimVersion + "] does not exist in oss master server.");
            return false;
        }

    }

    /**
     * Check whether mim version exists on netsim server
     * 
     * @param mimVersion
     *        the mim version on netsim server
     * @return <tt>true</tt> if the mim version does exist on the netsim server; <tt>false</tt> otherwise
     */
    private boolean checkNetsimMimVersion(final String mimVersion) {

        System.out.println("============Netsim Mim Version: " + mimVersion + "============");
        if (mimVersion != null && netsimMimVersions.contains(mimVersion)) {
            log.info("The mim version [" + mimVersion + "] does exist in netsim server.");
            return true;
        } else {
            log.warn("The mim version [" + mimVersion + "] does not exist in netsim server.");
            return false;
        }
    }

    /**
     * Get oss mim version from server automatically regarding to node version
     * and the first part of mim version for that node version
     * 
     * @param nodeType
     *        the type of node, e.g.: LTE, WCDMA
     * @param ossNodeVersionAndMimInfo
     *        the mapping info between node version and first part of MIM
     *        version, e.g.: O13A-D
     * @return the Oss mim version on the test server consistent with the node
     *         type and node version
     */
    public static String getOssMimVersionByNodeVersion(final NodeType nodeType, final String ossNodeVersionAndMimInfo, final boolean isEndToEnd) {

        final String[] infoArray = ossNodeVersionAndMimInfo.split("-");
        final String ossNodeVersion = infoArray[0];
        final String mimStartKey = infoArray[1];
        String matchedMimVersion = null;

        try {
            // traverse ossMimVersions list and get the matched mim version with
            // the node version
            for (final String tempMimVersion : ossMimVersions) {
                if (tempMimVersion.startsWith(mimStartKey)) {
                    matchedMimVersion = compareAndReturnMimVersion(nodeType, ossNodeVersion, matchedMimVersion, tempMimVersion, isEndToEnd);
                }
            }
        } catch (final Exception ex) {
            log.warn("Error happens when find matched mim version for the node version. Error: " + ex.getMessage());
        }

        return matchedMimVersion;
    }

    /**
     * Compare two mim version which have same first part, and return the higher
     * or lower mim version based on the oss node version.
     * <p>
     * For example, compare D.1.188.M.4.7 and D.1.44.M.2.29. If oss node version is O13A, then return the lower version D.1.44.M.2.29. If
     * oss node version is O13B, then return the higher one D.1.188.M.4.7
     * 
     * @param nodeType
     * @param ossNodeVersion
     *        oss node version, e.g.: O13B
     * @param originalMim
     *        the selected mim version
     * @param currentMim
     *        the mim version to be compared with
     * @return the matched mim version based on the node version type
     */
    private static String compareAndReturnMimVersion(
            final NodeType nodeType,
            final String ossNodeVersion,
            final String originalMim,
            final String currentMim,
            final boolean isEndToEnd) {

        if (originalMim == null || originalMim.equals("")) {
            // no need compare for the first one
            return currentMim;
        }

        final String[] arr1 = originalMim.split("\\."); // original
        final String[] arr2 = currentMim.split("\\."); // current
        final String lowerMim;
        final String higherMim;
        // Temporary handling for WCDMA AI
        if (nodeType.equals(NodeType.WCDMA) && isEndToEnd) {
            if (arr1[1].equals(arr2[1])) {
                // if first version number is same, compare second
                if (Integer.parseInt(arr1[2]) < Integer.parseInt(arr2[2])) {
                    lowerMim = originalMim;
                    higherMim = currentMim;
                } else {
                    lowerMim = originalMim;
                    higherMim = currentMim;
                }
            } else {
                // compare first version number
                if (Integer.parseInt(arr1[1]) < Integer.parseInt(arr2[1])) {
                    lowerMim = originalMim;
                    higherMim = currentMim;
                } else {
                    lowerMim = currentMim;
                    higherMim = originalMim;
                }
            }
        } else {
            if (arr1[1].equals(arr2[1])) {
                // if first version number is same, compare second
                if (Integer.parseInt(arr1[2]) < Integer.parseInt(arr2[2])) {
                    lowerMim = originalMim;
                    higherMim = currentMim;
                } else {
                    lowerMim = currentMim;
                    higherMim = originalMim;
                }
            } else {
                // compare first version number
                if (Integer.parseInt(arr1[1]) < Integer.parseInt(arr2[1])) {
                    lowerMim = originalMim;
                    higherMim = currentMim;
                } else {
                    lowerMim = currentMim;
                    higherMim = originalMim;
                }
            }
        }
        if (ossNodeVersion.toUpperCase().contains("A")) {
            // lower Mim for A version node, e.g.: 13A
            return lowerMim;
        } else {
            // higher Mim for B version node, e.g.: 13B
            return higherMim;
        }
    }

    /**
     * get the netsim mim version which is matched with the provided oss mim
     * version
     * This function is extremely fragile when it comes to node integration as it can pick the wrong simulation as assumptions are made
     * about how good a match we need on mim version as exact mim version is not always present for our test cases
     * 
     * @param ossMimVersion
     *        the value of oss mim version
     * @return the netsim mim version based on the provided oss mim version
     */
    public static String getNetsimMimByOssMim(final String ossMimVersion) {

        // if ossMimVersion is null, no need to continue the process any more
        if (ossMimVersion == null) {
            return null;
        }

        final String[] vals = ossMimVersion.split("\\.");
        if (vals.length > 3) {
            // change was made here to add vals[2] and substring to get first char to include in comparison as otherwise, we can pick wrong
            // mim version for netsim OSS-45502
            final String mimKeyFirstTwo = vals[0] + vals[1] + vals[2].substring(0, 1);
            for (final String mimVersion : netsimMimVersions) {
                if (mimVersion.contains(mimKeyFirstTwo)) {

                    final String mimKeyFirstThree = vals[0] + vals[1] + vals[2];
                    final String netsimMimValue = mimVersion.split("[\\s|-]")[2];
                    if (mimKeyFirstThree.length() == netsimMimValue.length()) {
                        return mimVersion;
                    }
                }
            }
        } else {
            log.warn("Invalid oss mim version: " + ossMimVersion);
        }

        return null;
    }

    private static String ossInstallationInfo;

    private static final LinkedList<String> ossMimVersions = new LinkedList<String>();

    private static final LinkedList<String> netsimMimVersions = new LinkedList<String>();

    private static final List<String> netsimMimVersionsDG2 = new ArrayList<String>();

    private static final List<String> netsimMimVersionsBasebandT = new ArrayList<String>();

    private static final List<String> netsimMimVersionsSpitfire = new ArrayList<String>();

    /**
     * This static initiator is called at the first time of using this validator
     * class
     */
    static {

        loadVersionInfoOfInstalledOSS();
        loadOSSMimVersions();
        loadNetsimMimVersions();
        loadNetsimMimVersionsDG2();
        loadNetsimMimVersionsSpitfire();
    }

    public static void printServerAndMimInfo() {

        System.out.println("========= Print out OSS Installation info=========");
        System.out.println(ossInstallationInfo);

        System.out.println("========= Print out mim version on master server=========");
        for (final String mimVersion : ossMimVersions) {
            System.out.println(mimVersion);
        }
        System.out.println("========= Print out mim version on netsim server=========");
        for (final String mimVersion : netsimMimVersions) {
            System.out.println(mimVersion);
        }
    }

    /**
     * 
     */
    private static List<String> loadNetsimMimVersionsDG2() {
        // TODO Auto-generated method stub (Dec 3, 2015:10:21:56 AM by xsidmeh)

        final String command = "ls -ls /netsim/inst/nedatabases/LTE.MSRBS-V2.*.simcmd";
        final String outputStr = netsimRemoteCommandExecutor.simpleExec(command);
        // System.out.println(outputStr);

        final Pattern pattern = Pattern.compile(".*/(.*)\\.simcmd");
        try (final Scanner scanner = new Scanner(outputStr)) {
            String line = null;
            Matcher m = null;

            while (scanner.hasNext()) {
                if ((line = scanner.nextLine()) != null) {
                    m = pattern.matcher(line);
                    if (m.find()) {
                        final String netsimMim = m.group(1);
                        // cache the netsim mim
                        netsimMimVersionsDG2.add(netsimMim.replace(".", " "));
                    }
                }
            }
        } catch (final Exception ex) {
            log.error(ex.toString());
        }
        return netsimMimVersionsDG2;
    }

    private static List<String> loadNetsimMimVersionsBasebandT() {
        // TODO Auto-generated method stub (Dec 3, 2015:10:21:56 AM by xsidmeh)

        final String command = "ls -ls /netsim/inst/nedatabases/GSM.TCU04.*.simcmd";
        final String outputStr = netsimRemoteCommandExecutor.simpleExec(command);
        // System.out.println(outputStr);

        final Pattern pattern = Pattern.compile(".*/(.*)\\.simcmd");
        try (final Scanner scanner = new Scanner(outputStr)) {
            String line = null;
            Matcher m = null;

            while (scanner.hasNext()) {
                if ((line = scanner.nextLine()) != null) {
                    m = pattern.matcher(line);
                    if (m.find()) {
                        final String netsimMim = m.group(1);
                        // cache the netsim mim
                        netsimMimVersionsBasebandT.add(netsimMim.replace(".", " "));
                    }
                }
            }
        } catch (final Exception ex) {
            log.error(ex.toString());
        }
        return netsimMimVersionsBasebandT;
    }

    public static String getDG2NetsimNEType(final String nodeVersion) {
        String NeType = null;
        final List<String> netsimVersionList = loadNetsimMimVersionsDG2();
        for (final String netype : netsimVersionList) {
            if (netype.contains(nodeVersion)) {
                NeType = netype;
                break;
            }
        }
        return NeType;
    }

    public static String getBasebandTNetsimNEType(final String nodeVersion) {
        String NeType = null;
        final List<String> netsimVersionList = loadNetsimMimVersionsBasebandT();
        for (final String netype : netsimVersionList) {
            if (netype.contains(nodeVersion)) {
                NeType = netype;
                break;
            }
        }
        return NeType;
    }

    public static String getSpitfireNetsimNEType(final String nodeVersion) {

        String NeType = null;
        final List<String> netsimVersionList = loadNetsimMimVersionsSpitfire();
        for (final String netype : netsimVersionList) {
            if (netype.contains(nodeVersion)) {
                NeType = netype;
                break;
            }
        }
        return NeType;

    }

    private static List<String> loadNetsimMimVersionsSpitfire() {

        final String command = "ls -ls /netsim/inst/nedatabases/LTE.MSRBS-V2.*.simcmd";
        final String outputStr = netsimRemoteCommandExecutor.simpleExec(command);
        // System.out.println(outputStr);

        final Pattern pattern = Pattern.compile(".*/(.*)\\.simcmd");
        try (final Scanner scanner = new Scanner(outputStr)) {
            String line = null;
            Matcher m = null;

            while (scanner.hasNext()) {
                if ((line = scanner.nextLine()) != null) {
                    m = pattern.matcher(line);
                    if (m.find()) {
                        final String netsimMim = m.group(1);
                        // cache the netsim mim
                        netsimMimVersionsDG2.add(netsimMim.replace(".", " "));
                    }
                }
            }
        } catch (final Exception ex) {
            log.error(ex.toString());
        }
        return netsimMimVersionsSpitfire;

    }

    /**
     * Load and store version information of installed OSS
     */
    private static void loadVersionInfoOfInstalledOSS() {

        final String command = "/opt/ericsson/sck/bin/ist_run -v";
        ossInstallationInfo = masterRemoteCommandExecutor.simpleExec(command);

    }

    /**
     * Load OSS MIM version from OSS master server and store them for the whole
     * test run.
     */
    private static void loadOSSMimVersions() {

        final String command = "ls /opt/ericsson/nms_umts_wranmom/dat/*RBS_NODE_MODEL*xml | grep -v TDD";
        final String outputStr = masterRemoteCommandExecutor.simpleExec(command);
        // System.out.println(outputStr);

        final Pattern pattern = Pattern.compile("RBS_NODE_MODEL_v(.+)\\.xml");
        try (final Scanner scanner = new Scanner(outputStr)) {
            String line = null;
            Matcher m = null;

            while (scanner.hasNext()) {
                if ((line = scanner.nextLine()) != null) {
                    m = pattern.matcher(line);
                    if (m.find()) {
                        final String mimVersion = m.group(1);
                        // cache the oss mim
                        ossMimVersions.add(mimVersion.replace("_", "."));
                    }
                }
            }
        } catch (final Exception ex) {
            log.error(ex.toString());
        }
    }

    /**
     * Load Netsim MIM version from Netsim server and store them for the whole
     * test run.
     */
    private static void loadNetsimMimVersions() {

        final String command = "ls -ls /netsim/inst/nedatabases/LTE.ERBS.*.simcmd;ls -ls /netsim/inst/nedatabases/WCDMA.RBS.*.simcmd";
        final String outputStr = netsimRemoteCommandExecutor.simpleExec(command);
        // System.out.println(outputStr);

        final Pattern pattern = Pattern.compile(".*/(.*)\\.simcmd");
        try (final Scanner scanner = new Scanner(outputStr)) {
            String line = null;
            Matcher m = null;

            while (scanner.hasNext()) {
                if ((line = scanner.nextLine()) != null) {
                    m = pattern.matcher(line);
                    if (m.find()) {
                        final String netsimMim = m.group(1);
                        // cache the netsim mim
                        netsimMimVersions.add(netsimMim.replace(".", " "));
                    }
                }
            }
        } catch (final Exception ex) {
            log.error(ex.toString());
        }
    }

}

