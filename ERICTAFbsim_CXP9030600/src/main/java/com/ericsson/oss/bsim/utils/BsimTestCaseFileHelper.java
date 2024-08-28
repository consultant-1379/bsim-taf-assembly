package com.ericsson.oss.bsim.utils;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;

import com.ericsson.cifwk.taf.utils.FileFinder;

public class BsimTestCaseFileHelper {

    /**
     * Generic method to invoke groovy method with arguments
     * 
     * @param searchTerm
     *        the term to search file, it could be the prefix of the files
     *        or a keyword of files
     * @param localDirFilterKeyword
     *        part of full name of the local directory which stores the
     *        resource files. It should be the path in the .jar package so
     *        that it is able to be applied to CDB job
     * @return a file name and file path map of the search files
     */
    public static LinkedHashMap<String, String> searchFilesInWorkspace(final String searchTerm, final String localDirFilterKeyword) {

        final LinkedHashMap<String, String> fileNamePathMap = new LinkedHashMap<String, String>();

        final List<String> searchedFiles = FileFinder.findFile(searchTerm);
        for (final String fullFilePath : searchedFiles) {
            // System.out.println(fullFilePath);
            final String dirKeyword = localDirFilterKeyword.replace("/", File.separator).replace("\\", File.separator);
            if (fullFilePath.toLowerCase().contains(dirKeyword)) {
                final int idx = fullFilePath.lastIndexOf(File.separator);
                final String fileName = idx >= 0 ? fullFilePath.substring(idx + 1) : fullFilePath;
                fileNamePathMap.put(fileName, fullFilePath);
            }
        }

        return fileNamePathMap;
    }

}
