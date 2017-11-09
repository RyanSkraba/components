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
import static java.util.Arrays.asList;
import static org.talend.components.google.drive.GoogleDriveMimeTypes.MIME_TYPE_FOLDER;
import static org.talend.components.google.drive.runtime.GoogleDriveConstants.DRIVE_ROOT_FOLDER;
import static org.talend.components.google.drive.runtime.GoogleDriveConstants.QUERY_NOTTRASHED_NAME_NOTMIME_INPARENTS;
import static org.talend.components.google.drive.runtime.GoogleDriveConstants.Q_AND;
import static org.talend.components.google.drive.runtime.GoogleDriveConstants.Q_IN_PARENTS;
import static org.talend.components.google.drive.runtime.GoogleDriveConstants.Q_MIME_FOLDER;
import static org.talend.components.google.drive.runtime.GoogleDriveConstants.Q_MIME_NOT_FOLDER;
import static org.talend.components.google.drive.runtime.GoogleDriveConstants.Q_NAME;
import static org.talend.components.google.drive.runtime.GoogleDriveConstants.Q_NOT_TRASHED;
import static org.talend.components.google.drive.runtime.GoogleDriveConstants.ROOT_FOLDER_SEPARATOR;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.google.drive.GoogleDriveMimeTypes;
import org.talend.components.google.drive.runtime.utils.GoogleDriveGetParameters;
import org.talend.components.google.drive.runtime.utils.GoogleDriveGetResult;
import org.talend.components.google.drive.runtime.utils.GoogleDrivePutParameters;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;

import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

public class GoogleDriveUtils {

    private static final Logger LOG = LoggerFactory.getLogger(GoogleDriveUtils.class);

    private static final I18nMessages messages = GlobalI18N.getI18nMessageProvider().getI18nMessages(GoogleDriveUtils.class);

    private Drive drive;

    private static final String FILE_TYPE = "file";

    private static final String FOLDER_TYPE = "folder";

    private static final String FILEFOLDER_TYPE = "filefolder";

    private static final String PATH_SEPARATOR = "/";

    public GoogleDriveUtils(Drive drive) {
        this.drive = drive;
    }

    public List<String> getExplodedPath(String resourcePath) {
        LOG.debug("[getExplodedPath] resourcePath :`{}`.", resourcePath);
        String path = resourcePath.trim();
        path = path.startsWith(PATH_SEPARATOR) ? path.substring(1) : path;
        path = path.endsWith(PATH_SEPARATOR) ? path.substring(0, path.length() - 1) : path;
        return asList(path.split(PATH_SEPARATOR));
    }

    public File getMetadata(String id, String metadata) throws IOException {
        LOG.debug("[getMetadata] id={}, metadata={}.", id, metadata);
        return drive.files().get(id).setFields(metadata).execute();
    }

    public String getResourceId(String query, String resourceName, String type) throws IOException {
        LOG.debug("[getResourceId] Searching for {} named {} with `{}`.", type, resourceName, query);
        FileList files = drive.files().list().setQ(query).execute();
        if (files.getFiles().size() > 1) {
            String errorMsg = messages.getMessage(format("error.%s.more.than.one", type), resourceName);
            LOG.error(errorMsg);
            throw new IOException(errorMsg);
        } else if (files.getFiles().isEmpty()) {
            String errorMsg = messages.getMessage(format("error.%s.inexistant", type), resourceName);
            LOG.error(errorMsg);
            throw new IOException(errorMsg);
        }
        LOG.debug("[getResourceId] Found `{}` [{}].", resourceName, files.getFiles().get(0).getId());

        return files.getFiles().get(0).getId();
    }

    public List<String> checkPath(int pathLevel, List<String> path, String folderName, String parentId, boolean searchInTrash)
            throws IOException {
        List<String> result = new ArrayList<>();
        LOG.debug("[checkPath] (pathLevel = [{}], path = [{}], folderName = [{}], parentId = [{}], searchInTrash = [{}]).",
                pathLevel, path, folderName, parentId, searchInTrash);
        String query = format(Q_NAME, folderName) + Q_AND + //
                format(Q_IN_PARENTS, parentId) + Q_AND + //
                Q_MIME_FOLDER + //
                (searchInTrash ? "" : Q_AND + Q_NOT_TRASHED);
        LOG.debug("[checkPath] Query({}).", query);
        FileList files = drive.files().list().setQ(query).execute();
        if (files.getFiles().isEmpty()) {
            return Collections.emptyList();
        }
        int idx = pathLevel + 1;
        if (files.getFiles().size() > 1) {
            for (File f : files.getFiles()) {
                if (idx == path.size()) {
                    result.add(f.getId());// last level, give up...
                } else {
                    result.addAll(checkPath(idx, path, path.get(idx), f.getId(), searchInTrash));
                }
            }
        } else {
            File f = files.getFiles().get(0);
            String currentId = f.getId();
            if (idx == path.size()) {
                result.add(currentId);

            } else {
                String next = path.get(idx);
                result.addAll(checkPath(idx, path, next, currentId, searchInTrash));
            }
        }
        LOG.debug("[checkPath] `{}` => [{}].", String.join("/", path), result);

        return result;
    }

    public List<String> getFolderIds(String folderName, boolean searchInTrash) throws IOException {
        LOG.debug("[getFolderIds] (folderName = [{}], searchInTrash = [{}]).", folderName, searchInTrash);
        List<String> result = new ArrayList<>();
        if (DRIVE_ROOT_FOLDER.equals(folderName) || ROOT_FOLDER_SEPARATOR.equals(folderName) || folderName.isEmpty()) {
            result.add(DRIVE_ROOT_FOLDER);
            return result;
        }
        List<String> path = getExplodedPath(folderName);
        String start = path.get(0);
        result = checkPath(0, path, start, "root", searchInTrash);

        LOG.debug("[getFolderIds] Returning {}.", result);
        return result;
    }

    public String findResourceByName(String resource, String type) throws IOException {
        if (resource.contains(PATH_SEPARATOR)) {
            return findResourceByPath(resource, type);
        } else {
            return findResourceByGlobalSearch(resource, type);
        }
    }

    private String findResourceByGlobalSearch(String resource, String type) throws IOException {
        String query;
        switch (type) {
        case FILE_TYPE:
            query = format(Q_NAME, resource) + Q_AND + Q_MIME_NOT_FOLDER;
            break;
        case FOLDER_TYPE:
            query = format(Q_NAME, resource) + Q_AND + Q_MIME_FOLDER;
            break;
        case FILEFOLDER_TYPE:
            query = format(Q_NAME, resource);
            break;
        default:
            query = "";
        }
        LOG.debug("[findResourceByGlobalSearch] Searching for {} [{}] with `{}`.", resource, type, query);
        return getResourceId(query, resource, type);
    }

    private String findResourceByPath(String resourceName, String type) throws IOException {
        List<String> path = getExplodedPath(resourceName);
        String fileName = path.get(path.size() - 1);
        String parentId = DRIVE_ROOT_FOLDER;
        String query;
        LOG.debug("[findResourceByPath] Searching for {} [{}]. Path: {}; FileName:{}", resourceName, type.toUpperCase(), path,
                fileName);
        switch (type) {
        case FILE_TYPE:
            // We may have the case of "/FileA". So parentId is root and otherwise :
            if (path.size() > 1) {
                parentId = handleCheckPathResult(checkPath(0, path.subList(0, (path.size() - 1)), path.get(0), "root", true));
            }
            query = format(Q_NAME, fileName) + Q_AND + //
                    format(Q_IN_PARENTS, parentId) + Q_AND + //
                    Q_MIME_NOT_FOLDER;
            return getResourceId(query, fileName, FILE_TYPE);
        case FOLDER_TYPE:
            return handleCheckPathResult(checkPath(0, path, path.get(0), "root", true));
        case FILEFOLDER_TYPE:
            // try a full path first...
            LOG.debug("[findResourceByPath] Searching for a full path...");
            List<String> searchPath = checkPath(0, path, path.get(0), "root", true);
            if (searchPath.size() == 1) {
                return searchPath.get(0);
            }
            // try path + fileName
            LOG.debug("[findResourceByPath] Searching for fileName because did not find a full path");
            parentId = handleCheckPathResult(checkPath(0, path.subList(0, (path.size() - 1)), path.get(0), "root", true));
            query = format(Q_NAME, fileName) + Q_AND + //
                    format(Q_IN_PARENTS, parentId) + Q_AND + //
                    Q_MIME_NOT_FOLDER;
            return getResourceId(query, fileName, FILE_TYPE);
        default:
        }
        return null;
    }

    private String handleCheckPathResult(List<String> path) throws IOException {
        if (path.size() == 1) {
            return path.get(0);
        }
        String errorMsg = "";
        if (path.size() > 1) {
            errorMsg = messages.getMessage("error.folder.more.than.one", path);
        } else {
            errorMsg = messages.getMessage("error.folder.inexistant", path);
        }
        LOG.error(errorMsg);
        throw new IOException(errorMsg);
    }

    /**
     * @param folderName searched folder name
     * @param searchInTrash include folders in trash
     * @return the folder ID value
     * @throws IOException when the folder doesn't exist or there are more than one folder named like {@code folderName}
     */
    public String getFolderId(String folderName, boolean searchInTrash) throws IOException {
        if (DRIVE_ROOT_FOLDER.equals(folderName) || ROOT_FOLDER_SEPARATOR.equals(folderName) || folderName.isEmpty()) {
            return DRIVE_ROOT_FOLDER;
        }
        return findResourceByName(folderName, FOLDER_TYPE);
    }

    /**
     * @param fileName searched fileName name
     * @return the fileName ID value
     * @throws IOException when the fileName doesn't exist or there are more than one fileName named like {@code fileName}
     */
    public String getFileId(String fileName) throws IOException {
        return findResourceByName(fileName, FILE_TYPE);
    }

    /**
     * @param fileOrFolderName searched fileName name or folder
     * @return fileName ID value
     * @throws IOException when the fileName/folder doesn't exist or there are more than one fileName/folder named like
     * {@code fileOrFolderName}
     */
    public String getFileOrFolderId(String fileOrFolderName) throws IOException {
        return findResourceByName(fileOrFolderName, FILEFOLDER_TYPE);
    }

    /**
     * Create a folder in the specified parent folder
     *
     * @param parentFolderId folder ID where to create folderName
     * @param folderName new folder's name
     * @return folder ID value
     * @throws IOException when operation fails
     */
    public String createFolder(String parentFolderId, String folderName) throws IOException {
        File createdFolder = new File();
        createdFolder.setName(folderName);
        createdFolder.setMimeType(MIME_TYPE_FOLDER);
        createdFolder.setParents(Collections.singletonList(parentFolderId));

        return drive.files().create(createdFolder).setFields("id").execute().getId();
    }

    /**
     * @param fileId ID of the fileName to copy
     * @param destinationFolderId folder ID where to copy the fileId
     * @param newName if not empty rename copy to this name
     * @param deleteOriginal remove original fileName (aka mv)
     * @return copied fileName ID
     * @throws IOException when copy fails
     */
    public String copyFile(String fileId, String destinationFolderId, String newName, boolean deleteOriginal) throws IOException {
        LOG.debug("[copyFile] fileId: {}; destinationFolderId: {}, newName: {}; deleteOriginal: {}.", fileId, destinationFolderId,
                newName, deleteOriginal);
        File copy = new File();
        copy.setParents(Collections.singletonList(destinationFolderId));
        if (!newName.isEmpty()) {
            copy.setName(newName);
        }
        File resultFile = drive.files().copy(fileId, copy).setFields("id, parents").execute();
        String copiedResourceId = resultFile.getId();
        if (deleteOriginal) {
            drive.files().delete(fileId).execute();
        }

        return copiedResourceId;
    }

    /**
     * @param sourceFolderId source folder ID
     * @param destinationFolderId folder ID where to copy the sourceFolderId's content
     * @param newName folder name to assign
     * @return created folder ID
     * @throws IOException when operation fails
     */
    public String copyFolder(String sourceFolderId, String destinationFolderId, String newName) throws IOException {
        LOG.debug("[copyFolder] sourceFolderId: {}; destinationFolderId: {}; newName: {}", sourceFolderId, destinationFolderId,
                newName);
        // create a new folder
        String newFolderId = createFolder(destinationFolderId, newName);
        // Make a recursive copy of all files/folders inside the source folder
        String query = format(Q_IN_PARENTS, sourceFolderId) + Q_AND + Q_NOT_TRASHED;
        FileList originals = drive.files().list().setQ(query).execute();
        LOG.debug("[copyFolder] Searching for copy {}", query);
        for (File file : originals.getFiles()) {
            if (file.getMimeType().equals(MIME_TYPE_FOLDER)) {
                copyFolder(file.getId(), newFolderId, file.getName());
            } else {
                copyFile(file.getId(), newFolderId, file.getName(), false);
            }
        }

        return newFolderId;
    }

    private String removeResource(String resourceId, boolean useTrash) throws IOException {
        if (useTrash) {
            drive.files().update(resourceId, new File().setTrashed(true)).execute();
        } else {
            drive.files().delete(resourceId).execute();
        }
        return resourceId;
    }

    public String deleteResourceByName(String resourceName, boolean useTrash) throws IOException {
        String resourceId = getFileOrFolderId(resourceName);
        if (useTrash) {
            LOG.info(messages.getMessage("message.trashing.resource", resourceName, resourceId));
        } else {
            LOG.info(messages.getMessage("message.deleting.resource", resourceName, resourceId));
        }
        return removeResource(resourceId, useTrash);
    }

    public String deleteResourceById(String resourceId, boolean useTrash) throws IOException {
        if (useTrash) {
            LOG.info(messages.getMessage("message.trashing.resource", resourceId, resourceId));
        } else {
            LOG.info(messages.getMessage("message.deleting.resource", resourceId, resourceId));
        }
        return removeResource(resourceId, useTrash);
    }

    public GoogleDriveGetResult getResource(GoogleDriveGetParameters parameters) throws IOException {
        String fileId = parameters.getResourceId();
        File file = getMetadata(fileId, "id,mimeType,fileExtension");
        String fileMimeType = file.getMimeType();
        String outputFileExt = "." + file.getFileExtension();
        LOG.debug("[getResource] Found fileName `{}` [id: {}, mime: {}, ext: {}]", parameters.getResourceId(), fileId,
                fileMimeType, file.getFileExtension());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        /* Google Apps types */
        if (GoogleDriveMimeTypes.GOOGLE_DRIVE_APPS.contains(fileMimeType)) {
            String exportFormat = parameters.getMimeType().get(fileMimeType).getMimeType();
            outputFileExt = parameters.getMimeType().get(fileMimeType).getExtension();
            drive.files().export(fileId, exportFormat).executeMediaAndDownloadTo(outputStream);
        } else { /* Standard fileName */
            drive.files().get(fileId).executeMediaAndDownloadTo(outputStream);
        }
        byte[] content = outputStream.toByteArray();
        if (parameters.isStoreToLocal()) {
            String localFile = parameters.getOutputFileName();
            if (parameters.isAddExt()) {
                localFile = localFile + ((localFile.endsWith(outputFileExt)) ? "" : outputFileExt);
            }
            LOG.info(messages.getMessage("message.writing.resource", parameters.getResourceId(), localFile));
            try (FileOutputStream fout = new FileOutputStream(localFile)) {
                fout.write(content);
                fout.close();
            }
        }

        return new GoogleDriveGetResult(fileId, content);
    }

    public File putResource(GoogleDrivePutParameters parameters) throws IOException {
        String folderId = parameters.getDestinationFolderId();
        File putFile = new File();
        putFile.setParents(Collections.singletonList(folderId));
        Files.List fileRequest = drive.files().list()
                .setQ(format(QUERY_NOTTRASHED_NAME_NOTMIME_INPARENTS, parameters.getResourceName(), MIME_TYPE_FOLDER, folderId));
        LOG.debug("[putResource] `{}` Exists in `{}` ? with `{}`.", parameters.getResourceName(),
                parameters.getDestinationFolderId(), fileRequest.getQ());
        FileList existingFiles = fileRequest.execute();
        if (existingFiles.getFiles().size() > 1) {
            throw new IOException(messages.getMessage("error.file.more.than.one", parameters.getResourceName()));
        }
        if (existingFiles.getFiles().size() == 1) {
            if (!parameters.isOverwriteIfExist()) {
                throw new IOException(messages.getMessage("error.file.already.exist", parameters.getResourceName()));
            }
            LOG.debug("[putResource] {} will be overwritten...", parameters.getResourceName());
            drive.files().delete(existingFiles.getFiles().get(0).getId()).execute();
        }
        putFile.setName(parameters.getResourceName());
        String metadata = "id,parents,name";
        if (!StringUtils.isEmpty(parameters.getFromLocalFilePath())) {
            // Reading content from local fileName
            FileContent fContent = new FileContent(null, new java.io.File(parameters.getFromLocalFilePath()));
            putFile = drive.files().create(putFile, fContent).setFields(metadata).execute();
            //

        } else if (parameters.getFromBytes() != null) {
            AbstractInputStreamContent content = new ByteArrayContent(null, parameters.getFromBytes());
            putFile = drive.files().create(putFile, content).setFields(metadata).execute();
        }
        return putFile;
    }

}
