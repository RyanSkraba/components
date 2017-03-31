// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%${symbol_escape}features${symbol_escape}org.talend.rcp.branding.%PRODUCTNAME%${symbol_escape}%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package ${package};

/**
 * Enumeration of standard string delimiters
 * 
 * This shows possible values for dropdown list UI element i18n message for it
 * should be placed in TableInputProperties.properties file
 */
public enum StringDelimiter {
    SEMICOLON(";"),
    COLON(":"),
    COMMA(",");

    private String delimiter;

    private StringDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public String getDelimiter() {
        return this.delimiter;
    }
}
