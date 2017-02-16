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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MatchesFunction implements Function<Boolean, String> {

    private final String pattern;

    private final Pattern p;

    public MatchesFunction(String pattern) {
        this.pattern = pattern;
        if (pattern != null) {
            p = Pattern.compile(this.pattern);
        } else {
            p = null;
        }
    }

    @Override
    public Boolean getValue(String o) {
        if (pattern == null || o == null) {
            return false;
        }
        Matcher m = p.matcher(o);
        return m.matches();
    }

    @Override
    public String getStringPresentation() {
        return ".matches(" + pattern + ")";
    }

}
