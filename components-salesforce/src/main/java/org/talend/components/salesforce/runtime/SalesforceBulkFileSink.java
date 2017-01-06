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

import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.common.runtime.BulkFileSink;

public class SalesforceBulkFileSink extends BulkFileSink {

    @Override
    public WriteOperation<?> createWriteOperation() {
        return new SalesforceBulkFileWriteOperation(this);
    }
}
