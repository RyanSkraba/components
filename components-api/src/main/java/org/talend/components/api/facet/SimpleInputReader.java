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
package org.talend.components.api.facet;

import com.google.cloud.dataflow.sdk.io.BoundedSource;

public abstract class SimpleInputReader<InputObject> extends BoundedSource.BoundedReader<InputObject> {

    private final SimpleInputFacet<InputObject> source;

    /**
     * Returns a DatastoreReader with Source and Datastore object set.
     *
     * @param datastore a datastore connection to use.
     */
    public SimpleInputReader(SimpleInputFacet<InputObject> source) {
        this.source = source;
    }

    @Override
    public SimpleInputFacet<InputObject> getCurrentSource() {
        return source;
    }

    @Override
    public SimpleInputFacet<InputObject> splitAtFraction(double fraction) {
        // Not supported.
        return null;
    }

    @Override
    public Double getFractionConsumed() {
        // Not supported.
        return null;
    }
}