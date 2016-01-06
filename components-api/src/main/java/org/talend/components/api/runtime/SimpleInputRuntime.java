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
package org.talend.components.api.runtime;

import java.util.Map;

import org.talend.components.api.facet.SimpleInputFacet;

import com.google.cloud.dataflow.sdk.values.PCollection;

public class SimpleInputRuntime<Entity> implements FrameworkRuntime {

    SimpleInputFacet facet;

    PCollection<Map<String, Object>> outputObject;

    /**
     * Retrieve or generate input data and put them into the main flow compatible with the current Framework
     */
    public void genericExecute() throws Exception {
        // TODO extract the connection phase from the execution phase
        facet.connection();
        // facet.execute(output);
        // outputObject = sc.parallelize(output.getMainOutput());
        facet.tearDown();
    }

    /**
     * Retrieve the main output for tor the current framework
     *
     * @return
     */
    public PCollection<Map<String, Object>> getMainOutput() {
        return outputObject;
    }

}
