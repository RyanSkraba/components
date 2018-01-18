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
import org.talend.components.google.drive.create.GoogleDriveCreateDefinition;
import org.talend.components.google.drive.create.GoogleDriveCreateProperties;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;

public class GoogleDriveCreateReader extends GoogleDriveReader {

    private GoogleDriveCreateProperties properties;

    private String parentFolderId;

    private String newFolderId;

    private transient static final Logger LOG = LoggerFactory.getLogger(GoogleDriveCreateReader.class);

    private static final I18nMessages messages = GlobalI18N.getI18nMessageProvider()
            .getI18nMessages(GoogleDriveCreateReader.class);

    public GoogleDriveCreateReader(RuntimeContainer container, GoogleDriveSource source, GoogleDriveCreateProperties properties) {
        super(container, source);
        this.properties = properties;
    }

    @Override
    public boolean start() throws IOException {
        super.start();

        String parentFolder = properties.parentFolder.getValue();
        parentFolderId = properties.parentFolderAccessMethod.getValue().equals(AccessMethod.Id) ? parentFolder
                : utils.getFolderId(parentFolder, false);
        newFolderId = utils.createFolder(parentFolderId, properties.newFolder.getValue());
        /* feeding record */
        record = new GenericData.Record(properties.schemaMain.schema.getValue());
        record.put(0, parentFolderId);
        record.put(1, newFolderId);
        result.totalCount++;
        result.successCount++;

        return true;
    }

    @Override
    public Map<String, Object> getReturnValues() {
        Map<String, Object> r = result.toMap();
        r.put(GoogleDriveCreateDefinition.RETURN_PARENT_FOLDER_ID, parentFolderId);
        r.put(GoogleDriveCreateDefinition.RETURN_NEW_FOLDER_ID, newFolderId);

        return r;
    }
}
