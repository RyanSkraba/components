// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
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
