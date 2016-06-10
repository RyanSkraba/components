package org.talend.components.salesforce.runtime;

import org.talend.components.api.component.runtime.Writer;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.common.runtime.BulkFileSink;
import org.talend.components.common.runtime.BulkFileWriteOperation;

public class SalesforceBulkFileWriteOperation extends BulkFileWriteOperation {
    public SalesforceBulkFileWriteOperation(BulkFileSink fileSink) {
        super(fileSink);
    }

    @Override
    public Writer<Result> createWriter(RuntimeContainer adaptor) {
        return new SalesforceBulkFileWriter(this, ((BulkFileSink)getSink()).getBulkFileProperties(), adaptor);
    }
}
