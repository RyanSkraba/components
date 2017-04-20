// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.components.netsuite;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Hold NetSuite API version numbers.
 */
public class NetSuiteVersion {

    private static final Pattern VERSION_PATTERN = Pattern.compile(
            "((\\d+)\\.(\\d+))(\\.(\\d+))?"
    );

    private static final Pattern ENDPOINT_URL_VERSION_PATTERN = Pattern.compile(
            ".+\\/NetSuitePort_((\\d+)_(\\d+))(_(\\d+))?"
    );

    /** First number of major version (year). */
    private int majorYear;

    /** Second number of major version (release). */
    private int majorRelease;

    /** Minor or patch version, can be <code>-1</code> if minor or patch version not specified. */
    private int minor;

    public NetSuiteVersion(int majorYear, int majorRelease) {
        this(majorYear, majorRelease, -1);
    }

    public NetSuiteVersion(int majorYear, int majorRelease, int minor) {
        this.majorYear = majorYear;
        this.majorRelease = majorRelease;
        this.minor = minor;
    }

    public int getMajorYear() {
        return majorYear;
    }

    public int getMajorRelease() {
        return majorRelease;
    }

    public int getMinor() {
        return minor;
    }

    public NetSuiteVersion getMajor() {
        return new NetSuiteVersion(majorYear, majorRelease);
    }

    public String getMajorAsString() {
        return getMajorAsString("_");
    }

    public String getMajorAsString(String separator) {
        return String.format("%d%s%d", majorYear, separator, majorRelease);
    }

    public String getAsString() {
        return getAsString("_");
    }

    public String getAsString(String separator) {
        if (minor == -1) {
            return getMajorAsString(separator);
        }
        return String.format("%d%s%d%s%d", majorYear, separator, majorRelease, separator, minor);
    }

    public boolean isSameMajor(NetSuiteVersion thatVersion) {
        return this.majorYear == thatVersion.majorYear && this.majorRelease == thatVersion.majorRelease;
    }

    /**
     * Parse version.
     *
     * @param versionString version string
     * @return version object
     * @throws IllegalArgumentException if version couldn't be parsed
     */
    public static NetSuiteVersion parseVersion(String versionString) {
        Matcher matcher = VERSION_PATTERN.matcher(versionString);
        if (matcher.matches()) {
            String sValue1 = matcher.group(2);
            String sValue2 = matcher.group(3);
            String sValue3 = matcher.group(5);
            try {
                int value1 = Integer.parseInt(sValue1);
                int value2 = Integer.parseInt(sValue2);
                int value3 = sValue3 != null ? Integer.parseInt(sValue3) : -1;
                return new NetSuiteVersion(value1, value2, value3);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Failed to parse NetSuite API version: " + versionString);
            }
        } else {
            throw new IllegalArgumentException("Failed to parse NetSuite API version: " + versionString);
        }
    }

    /**
     * Detect version from NetSuite web service endpoint URL.
     *
     * @param nsEndpointUrl endpoint URL
     * @return version object
     * @throws IllegalArgumentException if version couldn't be detected
     */
    public static NetSuiteVersion detectVersion(String nsEndpointUrl) {
        Matcher matcher = ENDPOINT_URL_VERSION_PATTERN.matcher(nsEndpointUrl);
        if (matcher.matches()) {
            String sValue1 = matcher.group(2);
            String sValue2 = matcher.group(3);
            String sValue3 = matcher.group(5);
            try {
                int value1 = Integer.parseInt(sValue1);
                int value2 = Integer.parseInt(sValue2);
                int value3 = sValue3 != null ? Integer.parseInt(sValue3) : -1;
                return new NetSuiteVersion(value1, value2, value3);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Failed to detect NetSuite API version: " + nsEndpointUrl);
            }
        } else {
            throw new IllegalArgumentException("Failed to detect NetSuite API version: " + nsEndpointUrl);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        NetSuiteVersion version = (NetSuiteVersion) o;
        return majorYear == version.majorYear && majorRelease == version.majorRelease && minor == version.minor;
    }

    @Override
    public int hashCode() {
        return Objects.hash(majorYear, majorRelease, minor);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NetSuiteVersion{");
        sb.append("majorYear=").append(majorYear);
        sb.append(", majorRelease=").append(majorRelease);
        sb.append(", minor=").append(minor);
        sb.append('}');
        return sb.toString();
    }
}
