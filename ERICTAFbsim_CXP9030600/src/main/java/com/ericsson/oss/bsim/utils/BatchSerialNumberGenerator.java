package com.ericsson.oss.bsim.utils;

import java.util.ArrayList;

public class BatchSerialNumberGenerator {

    /**
     * Method will generate a required number of node serials for a batch from a four
     * digit prefix and return the serials as one string
     * 
     * @param fourDigitPrefix
     * @param batchIndex
     * @param batchSize
     * @return
     */
    public String generateSerialNumbersString(final String fourDigitPrefix, final int batchIndex, final int batchSize) {

        final StringBuilder sb = new StringBuilder();

        final String batchID = batchIndex < 10 ? "B0" + String.valueOf(batchIndex) : "B" + String.valueOf(batchIndex);

        for (int i = 0; i < batchSize; i++) {
            if (i < 10) {
                sb.append(fourDigitPrefix).append(batchID).append("N00" + i + ",");
            } else if (i < 100) {
                sb.append(fourDigitPrefix).append(batchID).append("N0" + i + ",");
            } else if (i < 1000) {
                sb.append(fourDigitPrefix).append(batchID).append("N" + i + ",");
            }

        }

        return sb.substring(0, sb.lastIndexOf(","));
    }

    /**
     * Method will generate a required number of node serials for a batch from a four
     * digit prefix and return the serials as a list
     * 
     * @param fourDigitPrefix
     * @param batchIndex
     * @param batchSize
     * @return
     */
    public ArrayList<String> generateSerialNumbersList(final String fourDigitPrefix, final int batchIndex, final int batchSize) {

        final String batchID = batchIndex < 10 ? "B0" + String.valueOf(batchIndex) : "B" + String.valueOf(batchIndex);

        final ArrayList<String> serialList = new ArrayList<String>();

        for (int i = 0; i < batchSize; i++) {
            if (i < 10) {
                serialList.add(fourDigitPrefix + batchID + "N00" + i);
            } else if (i < 100) {
                serialList.add(fourDigitPrefix + batchID + "N0" + i);
            } else if (i < 1000) {
                serialList.add(fourDigitPrefix + batchID + "N" + i);
            }

        }

        return serialList;
    }

}
