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
 * created by dmytro.chmyga on Nov 22, 2016
 */
public class LowerCaseFunction implements Function<String, String> {

    @Override
    public String getValue(String o) {
        if (o == null) {
            return null;
        }
        return o.toLowerCase();
    }

    public String getStringPresentation() {
        return ".toLowerCase()";
    }

}
