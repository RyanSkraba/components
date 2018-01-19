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
package org.talend.components.common.config.jdbc;

/**
 * DBMS type
 */
public class DbmsType {

    /**
     * Used to specify that value wansn't defined. Can be used for positive integer variables
     */
    static final int UNDEFINED = -1;

    /**
     * DBMS type name
     */
    private final String typeName;

    /**
     * Specify whether it is default type for type mapping
     */
    private final boolean isDefault;

    private final int defaultLength;

    private final int defaultPrecision;

    private final boolean ignoreLength;

    private final boolean ignorePrecision;

    /**
     * Precision before length
     */
    private final boolean preBeforeLength;

    /**
     * Constructs DBMS type
     * 
     * @param typeName
     * @param isDefault
     * @param defaultLength
     * @param defaultPrecision
     * @param ignoreLength
     * @param ignorePrecision
     * @param preBeforeLength
     */
    public DbmsType(String typeName, boolean isDefault, int defaultLength, int defaultPrecision, boolean ignoreLength,
            boolean ignorePrecision, boolean preBeforeLength) {
        this.typeName = typeName;
        this.isDefault = isDefault;
        this.defaultLength = defaultLength;
        this.defaultPrecision = defaultPrecision;
        this.ignoreLength = ignoreLength;
        this.ignorePrecision = ignorePrecision;
        this.preBeforeLength = preBeforeLength;
    }

    /**
     * @return DB type name
     */
    public String getName() {
        return typeName;
    }

    /**
     * @return the isDefault
     */
    public boolean isDefault() {
        return isDefault;
    }

    /**
     * @return the defaultLength
     */
    public int getDefaultLength() {
        return defaultLength;
    }

    /**
     * @return the defaultPrecision
     */
    public int getDefaultPrecision() {
        return defaultPrecision;
    }

    /**
     * @return the ignoreLength
     */
    public boolean isIgnoreLength() {
        return ignoreLength;
    }

    /**
     * @return the ignorePrecision
     */
    public boolean isIgnorePrecision() {
        return ignorePrecision;
    }

    /**
     * @return the preBeforeLength
     */
    public boolean isPreBeforeLength() {
        return preBeforeLength;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("dbType type=").append(getName()).append(" ");
        sb.append("defaut=").append(isDefault()).append(" ");
        sb.append("defaultLength=").append(getDefaultLength()).append(" ");
        sb.append("defautPrecision=").append(getDefaultPrecision()).append(" ");
        sb.append("ignoreLength=").append(isIgnoreLength()).append(" ");
        sb.append("ignorePrecision=").append(isIgnorePrecision()).append(" ");
        sb.append("preBeforeLength=").append(isPreBeforeLength()).append(" ");
        return sb.toString();
    }

}
