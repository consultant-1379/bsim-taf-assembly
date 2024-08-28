package com.ericsson.oss.bsim.operators.api;

public interface IClusterOperator {

    boolean createCluster(String clusterType, String elementIds);

    boolean editCluster(String clusterType, String elementIds);

    void deleteCluster();

    String getElementIds(String clusterType, int noOfRbs, int noOfErbs, int noOfRnc, int noOfPrbs, int noOfPerbs);

    boolean verifyChangesInDomain(String clusterType, String elementIds);

    boolean verifyClusterDeletedInDomain();

}
