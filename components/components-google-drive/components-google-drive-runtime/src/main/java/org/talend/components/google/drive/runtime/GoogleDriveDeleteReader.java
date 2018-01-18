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
import java.util.Map;

import org.apache.avro.generic.GenericData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.google.drive.GoogleDriveComponentProperties.AccessMethod;
import org.talend.components.google.drive.delete.GoogleDriveDeleteDefinition;
import org.talend.components.google.drive.delete.GoogleDriveDeleteProperties;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;

public class GoogleDriveDeleteReader extends GoogleDriveReader {

    private GoogleDriveDeleteProperties properties;

    private String fileId;

    private transient static final Logger LOG = LoggerFactory.getLogger(GoogleDriveDeleteReader.class);

    private static final I18nMessages messages = GlobalI18N.getI18nMessageProvider()
            .getI18nMessages(GoogleDriveDeleteReader.class);

    public GoogleDriveDeleteReader(RuntimeContainer container, GoogleDriveSource source, GoogleDriveDeleteProperties properties) {
        super(container, source);
        this.properties = properties;
    }

    @Override
    public boolean start() throws IOException {
        super.start();
        //
        if (properties.deleteMode.getValue().equals(AccessMethod.Name)) {
            fileId = utils.deleteResourceByName(properties.file.getValue(), properties.useTrash.getValue());
        } else {
            fileId = utils.deleteResourceById(properties.file.getValue(), properties.useTrash.getValue());
        }
        /* feeding result and record */
        record = new GenericData.Record(properties.schemaMain.schema.getValue());
        record.put(0, fileId);
        result.totalCount++;
        result.successCount++;

        return true;
    }

    @Override
    public Map<String, Object> getReturnValues() {
        Map<String, Object> r = result.toMap();
        r.put(GoogleDriveDeleteDefinition.RETURN_FILE_ID, fileId);

        return r;
    }
}
