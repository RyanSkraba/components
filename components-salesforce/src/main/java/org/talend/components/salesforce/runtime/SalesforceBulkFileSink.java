package org.talend.components.salesforce.runtime;

import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.common.runtime.BulkFileSink;

public class SalesforceBulkFileSink extends BulkFileSink {

    @Override
    public WriteOperation<?> createWriteOperation() {
        return new SalesforceBulkFileWriteOperation(this);
    }
}
