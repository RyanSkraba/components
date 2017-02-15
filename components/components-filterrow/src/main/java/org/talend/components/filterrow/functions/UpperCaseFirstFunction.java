// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.filterrow.functions;

/**
 * created by dmytro.chmyga on Dec 22, 2016
 */
public class UpperCaseFirstFunction implements Function<Character, String> {

    @Override
    public Character getValue(String o) {
        if (o == null) {
            return null;
        }
        return o.toUpperCase().charAt(0);
    }

    @Override
    public String getStringPresentation() {
        return ".toUpperCase().charAt(0)";
    }

}
