package com.ericsson.oss.bsim.cli.test.configuration;

public enum BsimCliTestConfigurationType {

    CREATE_NODE(true, true, "NODE", false, false, false),
    CREATE_BATCH(true, true, "BATCH", false, false, false),
    CREATE_WITH_FILE_NODE(true, true, "NODE", true, true, false),
    CREATE_WITH_FILE_BATCH(true, true, "BATCH", true, true, false),
    UPDATE(true, true, "NODE", true, true, true),
    UPDATE_WITH_FILE(true, true, "NODE", true, true, true),
    DELETE_NODE(true, true, "NODE", false, false, true),
    DELETE_BATCH(true, true, "BATCH", false, false, true),
    DELETE_WITH_FILE_NODE(true, true, "NODE", true, false, true),
    DELETE_WITH_FILE_BATCH(true, true, "BATCH", true, false, true),
    READ_NODE(true, false, "NODE", false, false, true),
    READ_BATCH(true, false, "BATCH", false, false, true),
    SHOW_BATCH(true, true, "BATCH", false, false, true),
    SHOW_WITH_FILE_BATCH(true, true, "BATCH", true, true, true),
    HELP(false, false, "", false, false, true);

    private boolean addBatch;

    private boolean bind;

    private boolean transferInputFile;

    private boolean outputFileGenerated;

    private boolean validateByString;

    private String bindType;

    private BsimCliTestConfigurationType(
            final boolean addBatch,
            final boolean bind,
            final String bindType,
            final boolean transferInputFile,
            final boolean outputFileGenerated,
            final boolean validateByString) {

        this.addBatch = addBatch;
        this.bind = bind;
        this.bindType = bindType;
        this.transferInputFile = transferInputFile;
        this.outputFileGenerated = outputFileGenerated;
        this.validateByString = validateByString;
    }

    /**
     * Will return true or false if a batch should be manually added
     * for the configuration type
     * 
     * @return
     */
    public boolean isAddBatch() {
        return addBatch;
    }

    /**
     * Will return true or false if a binding of a node needs to happen
     * for the configuration type
     * 
     * @return
     */
    public boolean isBind() {
        return bind;
    }

    /**
     * Will return the bind type. Batch or Node
     * 
     * @return
     */
    public String isBindType() {
        return bindType;
    }

    /**
     * Will return true or false if an input file will need to be transfered
     * for the configuration type
     * 
     * @return
     */
    public boolean isTransferInputFile() {
        return transferInputFile;
    }

    /**
     * Will return true or false if an output file will need to be transfered
     * for the configuration type
     * 
     * @return
     */
    public boolean isOutputFileGenerated() {
        return outputFileGenerated;
    }

    /**
     * Will return true or false if a validation string is required
     * for the configuration type
     * 
     * @return
     */
    public boolean isValidateByString() {
        return validateByString;
    }

}
