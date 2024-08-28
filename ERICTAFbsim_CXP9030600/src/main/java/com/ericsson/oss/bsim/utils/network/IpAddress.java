/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2014 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.bsim.utils.network;

import com.ericsson.oss.bsim.utils.network.helper.IpAddressHelper;

/**
 * @author eannpaa
 */
public class IpAddress {

    private final String value;

    private final IpVersion version;

    public IpAddress(final String value) {
        this.value = IpAddressHelper.format(value);
        version = IpAddressHelper.isIPv4(this) ? IpVersion.IPv4 : IpVersion.IPv6;
    }

    public String getValue() {
        return value;
    }

    public IpVersion getVersion() {
        return version;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (value == null ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IpAddress other = (IpAddress) obj;
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }
}
