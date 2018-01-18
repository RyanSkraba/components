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
import java.util.List;
import java.util.Map;

import org.apache.avro.generic.GenericData.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.google.drive.GoogleDriveComponentProperties.AccessMethod;
import org.talend.components.google.drive.copy.GoogleDriveCopyDefinition;
import org.talend.components.google.drive.copy.GoogleDriveCopyProperties;
import org.talend.components.google.drive.copy.GoogleDriveCopyProperties.CopyMode;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;

public class GoogleDriveCopyReader extends GoogleDriveReader {

    private final GoogleDriveCopyProperties properties;

    private String sourceId = "";

    private String destinationId = "";

    private transient static final Logger LOG = LoggerFactory.getLogger(GoogleDriveCopyReader.class);

    private static final I18nMessages messages = GlobalI18N.getI18nMessageProvider().getI18nMessages(GoogleDriveCopyReader.class);

    public GoogleDriveCopyReader(RuntimeContainer container, GoogleDriveSource source, GoogleDriveCopyProperties properties) {
        super(container, source);
        this.properties = properties;
    }

    @Override
    public boolean start() throws IOException {
        super.start();

        CopyMode copyMode = properties.copyMode.getValue();
        String source = properties.source.getValue();
        String destinationFolder = properties.destinationFolder.getValue();
        String newName = properties.rename.getValue() ? properties.newName.getValue() : "";
        boolean deleteSourceFile = properties.deleteSourceFile.getValue();

        /* check for destination folder */
        String destinationFolderId = properties.destinationFolderAccessMethod.getValue().equals(AccessMethod.Id)
                ? destinationFolder
                : utils.getFolderId(destinationFolder, false);
        /* work on a fileName */
        if (CopyMode.File.equals(copyMode)) {
            /* check for managed resource */
            sourceId = properties.sourceAccessMethod.getValue().equals(AccessMethod.Id) ? source : utils.getFileId(source);
            destinationId = utils.copyFile(sourceId, destinationFolderId, newName, deleteSourceFile);
        } else {/* work on a folder */
            /* check for managed resource */
            sourceId = properties.sourceAccessMethod.getValue().equals(AccessMethod.Id) ? source
                    : utils.getFolderId(source, false);
            if (newName.isEmpty()) {
                List<String> paths = utils.getExplodedPath(source);
                newName = paths.get(paths.size() - 1);
            }
            destinationId = utils.copyFolder(sourceId, destinationFolderId, newName);
        }
        //
        record = new Record(properties.schemaMain.schema.getValue());
        record.put(0, sourceId);
        record.put(1, destinationId);
        result.totalCount++;
        result.successCount++;

        return true;
    }

    @Override
    public Map<String, Object> getReturnValues() {
        Map<String, Object> res = result.toMap();
        res.put(GoogleDriveCopyDefinition.RETURN_SOURCE_ID, sourceId);
        res.put(GoogleDriveCopyDefinition.RETURN_DESTINATION_ID, destinationId);

        return res;
    }
}
