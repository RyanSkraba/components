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

import static java.lang.String.format;
import static org.talend.components.google.drive.GoogleDriveMimeTypes.MIME_TYPE_FOLDER;
import static org.talend.components.google.drive.runtime.GoogleDriveConstants.QUERY_MIME_FOLDER;
import static org.talend.components.google.drive.runtime.GoogleDriveConstants.Q_AND;
import static org.talend.components.google.drive.runtime.GoogleDriveConstants.Q_IN_PARENTS;
import static org.talend.components.google.drive.runtime.GoogleDriveConstants.Q_NOT_TRASHED;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.google.drive.GoogleDriveComponentProperties.AccessMethod;
import org.talend.components.google.drive.list.GoogleDriveListProperties;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;

import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

public class GoogleDriveListReader extends GoogleDriveReader {

    public static final String FIELDS_SELECTION = "files(id,name,mimeType,modifiedTime,kind,size,parents,trashed,webViewLink),nextPageToken";

    private GoogleDriveListProperties properties;

    private List<File> searchResults;

    private int searchIdx;

    private int searchCount;

    private boolean includeSubDirectories;

    private boolean includeTrashedFiles;

    private String listModeStr;

    private String folderName;

    private Files.List request;

    private String query;

    private String folderId;

    private List<String> subFolders;

    private static final Logger LOG = LoggerFactory.getLogger(GoogleDriveListReader.class);

    private static final I18nMessages messages = GlobalI18N.getI18nMessageProvider().getI18nMessages(GoogleDriveListReader.class);

    public GoogleDriveListReader(RuntimeContainer container, GoogleDriveSource source, GoogleDriveListProperties properties) {
        super(container, source);
        this.properties = properties;
        //
        includeSubDirectories = properties.includeSubDirectories.getValue();
        includeTrashedFiles = properties.includeTrashedFiles.getValue();
        listModeStr = properties.listMode.getValue().name().toUpperCase();
        folderName = properties.folder.getValue().trim();
        //
        String qTrash = includeTrashedFiles ? "" : Q_AND + Q_NOT_TRASHED;
        query = Q_IN_PARENTS + ("DIRECTORIES".equals(listModeStr) ? QUERY_MIME_FOLDER : "") + qTrash;
        subFolders = new ArrayList<>();
        searchResults = new ArrayList<>();
    }

    @Override
    public boolean start() throws IOException {
        super.start();
        /* build query string */
        request = drive.files().list();
        request.setFields(FIELDS_SELECTION);
        //
        if (properties.folderAccessMethod.getValue().equals(AccessMethod.Id)) {
            subFolders.add(folderName);
        } else {
            subFolders = utils.getFolderIds(folderName, includeTrashedFiles);
        }
        LOG.debug("[start] subFolders = {}.", subFolders);
        if (subFolders.size() == 0) {
            LOG.warn(messages.getMessage("error.folder.inexistant", folderName));
            return false;
        }
        if (subFolders.size() > 1) {
            LOG.warn(messages.getMessage("error.folder.more.than.one", folderName));
        }
        return processFolder();
    }

    @Override
    public boolean advance() throws IOException {
        return hasNext();
    }

    @Override
    public IndexedRecord getCurrent() throws NoSuchElementException {
        record = convertSearchResultToIndexedRecord(searchResults.get(searchIdx));
        result.successCount++;

        return record;
    }

    private boolean hasNext() throws IOException {
        boolean next = (searchIdx + 1) < searchCount;
        if (next) {
            searchIdx++;
        } else {
            while (!next && !subFolders.isEmpty()) {
                next = processFolder();
            }
            if (next) {
                searchIdx = 0;
            }
        }

        return next;
    }

    private boolean canAddSubFolder(String mimeType) {
        return MIME_TYPE_FOLDER.equals(mimeType) && includeSubDirectories;
    }

    private boolean canAddFile(String mimeType) {
        return "BOTH".equals(listModeStr) || ("FILES".equals(listModeStr) && !MIME_TYPE_FOLDER.equals(mimeType))
                || ("DIRECTORIES".equals(listModeStr) && MIME_TYPE_FOLDER.equals(mimeType));
    }

    private boolean processFolder() throws IOException {
        if (folderId == null && !subFolders.isEmpty()) {
            folderId = subFolders.get(0);
            subFolders.remove(0);
            request.setQ(format(query, folderId));
            LOG.debug("query = {} {}.", query, folderId);
        }
        searchResults.clear();
        FileList files = request.execute();
        for (File file : files.getFiles()) {
            if (canAddSubFolder(file.getMimeType())) {
                subFolders.add(file.getId());
            }
            if (canAddFile(file.getMimeType())) {
                searchResults.add(file);
                result.totalCount++;
            }
        }
        request.setPageToken(files.getNextPageToken());
        searchCount = searchResults.size();
        // finished for folderId
        if (StringUtils.isEmpty(request.getPageToken()) || searchCount == 0) {
            folderId = null;
        }

        return searchCount > 0;
    }

    private IndexedRecord convertSearchResultToIndexedRecord(File file) {
        Schema s = properties.getSchema();
        // Main record
        IndexedRecord main = new GenericData.Record(s);
        main.put(0, file.getId());
        main.put(1, file.getName());
        main.put(2, file.getMimeType());
        main.put(3, file.getModifiedTime().getValue());
        main.put(4, file.getSize());
        main.put(5, file.getKind());
        main.put(6, file.getTrashed());
        main.put(7, file.getParents().toString()); // TODO This should be a List<String>
        main.put(8, file.getWebViewLink());

        return main;
    }
}
