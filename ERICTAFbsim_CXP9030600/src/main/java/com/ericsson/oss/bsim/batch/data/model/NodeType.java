package com.ericsson.oss.bsim.batch.data.model;

/**
 * Defines supported Node types and associated network
 */
public enum NodeType {

    Rbs(new NetworkType[] { NetworkType.WCDMA }, "RBS_NODE_MODEL"), //$NON-NLS-1$
    ERbs(new NetworkType[] { NetworkType.LTE }, "ERBS_NODE_MODEL"), //$NON-NLS-1$
    Stn(new NetworkType[] { NetworkType.LTE, NetworkType.WCDMA, NetworkType.GERAN }, "STN_NODE_MODEL"), //$NON-NLS-1$
    Bts(new NetworkType[] { NetworkType.GERAN }, "BTS_NODE_MODEL"),
    Pico(new NetworkType[] { NetworkType.LTE, NetworkType.WCDMA, NetworkType.CORE }, "ECIM_Top"),
    LANSwitch(new NetworkType[] { NetworkType.GERAN }, "LANS_NODE_MODEL"),
    // Adding nodetype GERAN for GRAN
    DG2(new NetworkType[] { NetworkType.LTE, NetworkType.WCDMA, NetworkType.CORE, NetworkType.GERAN }, "ECIM_Top");

    private final NetworkType[] networkTypes;

    private final String model;

    private NodeType(final NetworkType[] networkTypes, final String model) {
        this.networkTypes = networkTypes;
        this.model = model;
    }

    public static NodeType getNodeTypeByModel(final String modelName) {
        for (final NodeType nodeType : NodeType.values()) {
            if (nodeType.model.equals(modelName)) {
                return nodeType;
            }
        }
        return null;
    }

    public NetworkType[] networkTypes() {
        return this.networkTypes;
    }

    public String nodeModel() {
        return this.model;
    }

    public static String getMimScope(final NodeType... nodeTypes) {
        final StringBuilder mimScope = new StringBuilder();
        for (int i = 0; i < nodeTypes.length; i++) {
            mimScope.append(nodeTypes[i].nodeModel());
            if (i != nodeTypes.length - 1) {
                mimScope.append(":");
            }
        }
        return mimScope.toString();
    }

    // ADding same method as that in java code

    public static String getNodeTypeString(final NodeType nodeType) {
        if (nodeType == DG2) {
            // return "RadioNode";
            return "Baseband";
        } else {
            return nodeType.toString();
        }
    }

}
