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
package org.talend.components.azurestorage.blob.runtime;

import java.util.Map;

import org.talend.components.api.component.runtime.AbstractBoundedReader;
import org.talend.components.api.component.runtime.BoundedSource;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.azurestorage.AzureStorageProvideConnectionProperties;
import org.talend.components.azurestorage.tazurestorageconnection.TAzureStorageConnectionProperties;

public abstract class AzureStorageReader<T> extends AbstractBoundedReader<T> implements AzureStorageProvideConnectionProperties {

    protected transient TAzureStorageConnectionProperties connection;

    protected int dataCount;

    protected RuntimeContainer runtime;

    protected AzureStorageReader(RuntimeContainer container, BoundedSource source) {
        super(source);
        this.runtime = container;
    }

    @Override
    public TAzureStorageConnectionProperties getConnectionProperties() {
        if (connection == null) {
            connection = ((AzureStorageSourceOrSink) getCurrentSource()).getConnectionProperties();
        }
        return connection;
    }

    @Override
    public Map<String, Object> getReturnValues() {
        Result res = new Result();
        res.totalCount = dataCount;
        Map<String, Object> resultMap = res.toMap();
        return resultMap;
    }
}
