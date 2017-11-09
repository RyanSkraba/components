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

import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.avro.generic.GenericData.Record;
import org.apache.avro.generic.IndexedRecord;
import org.slf4j.Logger;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.WriterWithFeedback;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.google.drive.GoogleDriveComponentProperties.AccessMethod;
import org.talend.components.google.drive.create.GoogleDriveCreateDefinition;
import org.talend.components.google.drive.put.GoogleDrivePutProperties;
import org.talend.components.google.drive.runtime.utils.GoogleDrivePutParameters;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;

import com.google.api.services.drive.model.File;

public class GoogleDrivePutWriter implements WriterWithFeedback {

    private GoogleDrivePutProperties properties;

    private GoogleDriveWriteOperation writeOperation;

    private RuntimeContainer container;

    private final List<IndexedRecord> successfulWrites = new ArrayList<>();

    private final List<IndexedRecord> rejectedWrites = new ArrayList<>();

    private GoogleDriveUtils utils;

    private Result result;

    private File sentFile;

    private transient static final Logger LOG = getLogger(GoogleDrivePutWriter.class);

    private static final I18nMessages messages = GlobalI18N.getI18nMessageProvider().getI18nMessages(GoogleDrivePutWriter.class);

    public GoogleDrivePutWriter(GoogleDriveWriteOperation writeOperation, GoogleDrivePutProperties properties,
            RuntimeContainer container) {
        this.container = container;
        this.properties = properties;
        this.writeOperation = writeOperation;
    }

    @Override
    public void open(String uId) throws IOException {
        result = new Result(uId);
        try {
            utils = ((GoogleDriveSink) getWriteOperation().getSink()).getDriveUtils();
        } catch (GeneralSecurityException e) {
            LOG.error(e.getMessage());
            result.toMap().put(GoogleDriveCreateDefinition.RETURN_ERROR_MESSAGE, e.getMessage());
            throw new ComponentException(e);
        }
    }

    @Override
    public void write(Object object) throws IOException {
        if (object == null) {
            return;
        }
        IndexedRecord input = (IndexedRecord) object;
        Object data = input.get(0);
        LOG.debug("data [{}] {}.", data.getClass().getCanonicalName(), data.toString());
        byte[] bytes = null;
        if (data instanceof byte[]) {
            bytes = (byte[]) data;
        } else {
            bytes = data.toString().getBytes();
        }
        //
        String destinationFolderId = properties.destinationFolderAccessMethod.getValue().equals(AccessMethod.Id)
                ? properties.destinationFolder.getValue()
                : utils.getFolderId(properties.destinationFolder.getValue(), false);
        GoogleDrivePutParameters p = new GoogleDrivePutParameters(destinationFolderId, properties.fileName.getValue(),
                properties.overwrite.getValue(), bytes);
        sentFile = utils.putResource(p);
        //
        IndexedRecord record = new Record(properties.schemaMain.schema.getValue());
        record.put(0, bytes);
        // TODO should return this values in outOfBandRecord
        record.put(1, sentFile.getParents().get(0));
        record.put(2, sentFile.getId());
        successfulWrites.clear();
        result.successCount++;
        result.totalCount++;
        successfulWrites.add(record);
    }

    @Override
    public Result close() throws IOException {
        return result;
    }

    @Override
    public WriteOperation getWriteOperation() {
        return writeOperation;
    }

    @Override
    public List<IndexedRecord> getSuccessfulWrites() {
        return Collections.unmodifiableList(successfulWrites);
    }

    @Override
    public List<IndexedRecord> getRejectedWrites() {
        return Collections.unmodifiableList(rejectedWrites);
    }

}
