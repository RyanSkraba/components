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
public class MatchesFunction implements Function<Boolean, String> {

    private final String pattern;

    public MatchesFunction(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public Boolean getValue(String o) {
        if (pattern == null || o == null) {
            return false;
        }
        return o.matches(pattern);
    }

    @Override
    public String getStringPresentation() {
        return ".matches(" + pattern + ")";
    }

}
