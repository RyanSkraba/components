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
package org.talend.components.output.tlogrow;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.runtime.SimpleOutputRuntime;

// TODO slice the component into a write component and an output compoenent
public class LogRowFacet extends SimpleOutputRuntime<Map<String, Object>> {

    private static final Logger LOG = LoggerFactory.getLogger(LogRowFacet.class);

    @Override
    public void setUp(ComponentProperties comp) {
    }

    @Override
    public void execute(Map<String, Object> inputValue) throws Exception {
        System.out.println("got results!");
        System.out.println(inputValue);
    }

    @Override
    public void tearDown() {
        // TODO Auto-generated method stub

    }

}
