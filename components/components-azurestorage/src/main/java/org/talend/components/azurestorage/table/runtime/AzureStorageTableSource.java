// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.comç
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.azurestorage.table.runtime;

import java.util.List;

import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.api.component.runtime.BoundedSource;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.azurestorage.table.tazurestorageinputtable.TAzureStorageInputTableProperties;
import org.talend.daikon.properties.ValidationResult;

public class AzureStorageTableSource extends AzureStorageTableSourceOrSink implements BoundedSource {

    private static final long serialVersionUID = -2453758634165235002L;

    @SuppressWarnings("rawtypes")
    @Override
    public BoundedReader createReader(RuntimeContainer container) {
        if (properties instanceof TAzureStorageInputTableProperties) {
            return new AzureStorageTableReader(container, this, (TAzureStorageInputTableProperties) properties);
        }
        return null;
    }

    @Override
    public ValidationResult validate(RuntimeContainer container) {
        ValidationResult vr = super.validate(container);
        if (vr == ValidationResult.OK) {
            // TODO check combinedFilter...
        }
        return ValidationResult.OK;
    }

    @Override
    public List<? extends BoundedSource> splitIntoBundles(long desiredBundleSizeBytes, RuntimeContainer adaptor)
            throws Exception {
        return null;
    }

    @Override
    public long getEstimatedSizeBytes(RuntimeContainer adaptor) {
        return 0;
    }

    @Override
    public boolean producesSortedKeys(RuntimeContainer adaptor) {
        return false;
    }
}
