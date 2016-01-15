// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.transformation.tfiltercolumn;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.runtime.SimpleTransformationRuntime;

public class FilterColumnRuntime extends SimpleTransformationRuntime {

    private static final Logger LOG = LoggerFactory.getLogger(FilterColumnRuntime.class);

    @Override
    public void execute(Map<String, Object> inputValue) throws Exception {
        // Do the processing... Let's simplify everything : remove just the field "invalid" if there is any
        if (inputValue.containsKey("invalid")) {
            inputValue.remove("invalid");
        }
        addToMainOutput(inputValue);
    }
}
