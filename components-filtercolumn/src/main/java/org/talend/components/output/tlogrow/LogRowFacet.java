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
import org.talend.components.api.facet.SimpleOutputFacetV2;

import com.google.cloud.dataflow.sdk.transforms.DoFn;

// TODO slice the component into a write component and an output compoenent
public class LogRowFacet extends SimpleOutputFacetV2 {

    private static final Logger LOG = LoggerFactory.getLogger(LogRowFacet.class);

    @Override
    public void setUp(DoFn<Map<String, Object>, Void>.Context context) {
    }

    @Override
    public void execute(Map<String, Object> inputValue) throws Exception {
        System.out.println(inputValue);
    }

    @Override
    public void tearDown(DoFn<Map<String, Object>, Void>.Context context) {
    }

}
