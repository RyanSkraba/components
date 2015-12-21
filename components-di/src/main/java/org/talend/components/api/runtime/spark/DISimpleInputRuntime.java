package org.talend.components.api.runtime.spark;

import java.util.List;
import java.util.Map;

import org.talend.components.api.facet.SimpleInputFacet;
import org.talend.components.api.runtime.ReturnObject;
import org.talend.components.api.runtime.SimpleInputRuntime;

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

/**
 * created by pbailly on 18 Dec 2015 Detailled comment
 *
 */
public class DISimpleInputRuntime implements SimpleInputRuntime<List<Map<String, Object>>> {

    ReturnObject output = new ReturnObject();

    SimpleInputFacet facet;

    public DISimpleInputRuntime(SimpleInputFacet facet) {
        this.facet = facet;
    }

    @Override
    public void genericExecute() throws Exception {
        // TODO extract the connection phase from the execution phase
        facet.connection();
        facet.execute(output);
        facet.tearDown();
    }

    @Override
    public List<Map<String, Object>> getMainOutput() {
        return output.getMainOutput();
    }

}
