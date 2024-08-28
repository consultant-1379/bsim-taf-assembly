package com.ericsson.oss.bsim.batch.data.model;

/**
 * Defines supported Network Types
 *
 * @author emarfay
 */
public enum NetworkType {

    LTE("Lran"),
    WCDMA("Utran"),
    GERAN("Geran"),
    CORE("Core"),
    RADIO("Radio");

    private final String asAttribute;

    private NetworkType(final String asAttribute) {
        this.asAttribute = asAttribute;
    }

    public String asAttribute() {
        return this.asAttribute;
    }
}
