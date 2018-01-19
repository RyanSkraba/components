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

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.avro.generic.IndexedRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.AbstractBoundedReader;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.google.drive.copy.GoogleDriveCopyDefinition;

import com.google.api.services.drive.Drive;

public abstract class GoogleDriveReader extends AbstractBoundedReader<IndexedRecord> {

    protected Drive drive;

    protected GoogleDriveUtils utils;

    protected GoogleDriveSource source;

    protected RuntimeContainer container;

    protected Result result;

    protected IndexedRecord record;

    private transient static final Logger LOG = LoggerFactory.getLogger(GoogleDriveReader.class);

    public GoogleDriveReader(RuntimeContainer container, GoogleDriveSource source) {
        super(source);
        this.source = source;
        this.container = container;
        result = new Result();
    }

    @Override
    public boolean start() throws IOException {
        try {
            drive = source.getDriveService();
            utils = source.getDriveUtils();
        } catch (GeneralSecurityException e) {
            LOG.error(e.getMessage());
            // TODO manage this correctly
            result.toMap().put(GoogleDriveCopyDefinition.RETURN_ERROR_MESSAGE, e.getMessage());
            throw new ComponentException(e);
        }

        return true;
    }

    @Override
    public boolean advance() throws IOException {
        return false;
    }

    @Override
    public IndexedRecord getCurrent() throws NoSuchElementException {
        return record;
    }

    @Override
    public Map<String, Object> getReturnValues() {
        return result.toMap();
    }

}
