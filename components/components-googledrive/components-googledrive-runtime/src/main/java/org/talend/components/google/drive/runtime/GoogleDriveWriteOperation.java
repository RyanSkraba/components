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
package org.talend.components.google.drive.runtime;

import java.util.Map;

import org.talend.components.api.component.runtime.Sink;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.google.drive.put.GoogleDrivePutProperties;
import org.talend.daikon.properties.ValidationResult.Result;

public class GoogleDriveWriteOperation implements WriteOperation<Result> {

    private GoogleDriveSink sink;

    public GoogleDriveWriteOperation(GoogleDriveSink sink) {
        this.sink = sink;
    }

    @Override
    public void initialize(RuntimeContainer container) {
    }

    @Override
    public Map<String, Object> finalize(Iterable<Result> writerResults, RuntimeContainer container) {
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Writer<Result> createWriter(RuntimeContainer container) {
        return new GoogleDrivePutWriter(this, (GoogleDrivePutProperties) sink.getProperties(), container);
    }

    @Override
    public Sink getSink() {
        return sink;
    }

}
