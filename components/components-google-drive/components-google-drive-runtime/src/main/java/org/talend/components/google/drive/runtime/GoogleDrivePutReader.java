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
import java.nio.file.Paths;
import java.util.Map;

import org.apache.avro.generic.GenericData.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.google.drive.GoogleDriveComponentProperties.AccessMethod;
import org.talend.components.google.drive.put.GoogleDrivePutDefinition;
import org.talend.components.google.drive.put.GoogleDrivePutProperties;
import org.talend.components.google.drive.runtime.utils.GoogleDrivePutParameters;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;

import com.google.api.services.drive.model.File;

public class GoogleDrivePutReader extends GoogleDriveReader {

    private GoogleDrivePutProperties properties;

    private File sentFile;

    private transient static final Logger LOG = LoggerFactory.getLogger(GoogleDriveDeleteReader.class);

    private static final I18nMessages messages = GlobalI18N.getI18nMessageProvider()
            .getI18nMessages(GoogleDriveDeleteReader.class);

    public GoogleDrivePutReader(RuntimeContainer container, GoogleDriveSource source, GoogleDrivePutProperties properties) {
        super(container, source);
        this.properties = properties;
    }

    @Override
    public boolean start() throws IOException {
        super.start();

        String localFilePath = properties.localFilePath.getValue();
        String destinationFolderId = properties.destinationFolderAccessMethod.getValue().equals(AccessMethod.Id)
                ? properties.destinationFolder.getValue()
                : utils.getFolderId(properties.destinationFolder.getValue(), false);

        GoogleDrivePutParameters p = new GoogleDrivePutParameters(destinationFolderId, properties.fileName.getValue(),
                properties.overwrite.getValue(), localFilePath);
        sentFile = utils.putResource(p);
        record = new Record(properties.schemaMain.schema.getValue());
        record.put(0, java.nio.file.Files.readAllBytes(Paths.get(localFilePath)));
        record.put(1, sentFile.getParents().get(0));
        record.put(2, sentFile.getId());
        result.totalCount++;
        result.successCount++;

        return true;
    }

    @Override
    public Map<String, Object> getReturnValues() {
        Map<String, Object> r = result.toMap();
        r.put(GoogleDrivePutDefinition.RETURN_FILE_ID, sentFile.getId());
        r.put(GoogleDrivePutDefinition.RETURN_PARENT_FOLDER_ID, sentFile.getParents().get(0));

        return r;
    }

}
