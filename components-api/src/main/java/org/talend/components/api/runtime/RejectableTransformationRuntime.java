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

import org.talend.components.api.facet.ExtractionFacet;

public class RejectableTransformationRuntime extends ExtractionRuntime<Map<String, Object>> {

    public RejectableTransformationRuntime(ExtractionFacet<Map<String, Object>> facet) {
        super(facet);
    }

}
