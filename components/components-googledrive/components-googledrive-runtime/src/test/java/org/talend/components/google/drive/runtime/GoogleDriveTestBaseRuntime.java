package org.talend.components.google.drive.runtime;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.talend.components.google.drive.connection.GoogleDriveConnectionProperties.OAuthMethod.AccessToken;
import static org.talend.components.google.drive.connection.GoogleDriveConnectionProperties.OAuthMethod.InstalledApplicationWithIdAndSecret;
import static org.talend.components.google.drive.connection.GoogleDriveConnectionProperties.OAuthMethod.InstalledApplicationWithJSON;

import java.io.File;
import java.util.ArrayList;

import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.container.DefaultComponentRuntimeContainerImpl;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.google.drive.GoogleDriveProvideConnectionProperties;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.FileList;

public class GoogleDriveTestBaseRuntime {

    protected String TEST_CONTAINER = "test-container";

    protected GoogleDriveSourceOrSink sourceOrSink;

    protected GoogleDriveSource source;

    protected GoogleDriveSink sink;

    protected String APPLICATION_NAME = "IT_APP";

    protected File DATA_STORE_DIR;

    protected String CLIENT_ID = "google-drive-client-id";

    protected String CLIENT_SECRET = "google-drive-secret";

    protected NetHttpTransport HTTP_TRANSPORT;

    protected String FOLDER_ROOT = "root";

    protected RuntimeContainer container = null;

    protected Drive drive;

    protected String FOLDER_CREATE_ID = "create-id";

    protected String FOLDER_CREATE = "create";

    protected String FOLDER_DELETE_ID = "delete-id";

    protected String FOLDER_DELETE = "delete";

    protected FileList emptyFileList;

    protected transient static final Logger LOG = LoggerFactory.getLogger(GoogleDriveTestBaseRuntime.class);

    @Before
    public void setUp() throws Exception {
        container = new DefaultComponentRuntimeContainerImpl() {

            @Override
            public String getCurrentComponentId() {
                return TEST_CONTAINER;
            }
        };
        //
        DATA_STORE_DIR = new File(getClass().getClassLoader().getResource("./").toURI().getPath());
        HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        //
        drive = mock(Drive.class, RETURNS_DEEP_STUBS);
        sourceOrSink = spy(GoogleDriveSourceOrSink.class);
        source = spy(GoogleDriveSource.class);
        sink = spy(GoogleDriveSink.class);
        doReturn(drive).when(sourceOrSink).getDriveService();
        doReturn(drive).when(source).getDriveService();
        doReturn(drive).when(sink).getDriveService();
        //
        emptyFileList = new FileList();
        emptyFileList.setFiles(new ArrayList<com.google.api.services.drive.model.File>());
    }

    public GoogleDriveProvideConnectionProperties setupConnectionWithAccessToken(
            GoogleDriveProvideConnectionProperties properties) {
        properties.getConnectionProperties().applicationName.setValue("IT");
        properties.getConnectionProperties().oAuthMethod.setValue(AccessToken);
        properties.getConnectionProperties().accessToken.setValue(":token:");

        return properties;
    }

    public GoogleDriveProvideConnectionProperties setupConnectionWithInstalledApplicationWithIdAndSecret(
            GoogleDriveProvideConnectionProperties properties) {
        properties.getConnectionProperties().applicationName.setValue("IT");
        properties.getConnectionProperties().oAuthMethod.setValue(InstalledApplicationWithIdAndSecret);
        properties.getConnectionProperties().clientId.setValue(CLIENT_ID);
        properties.getConnectionProperties().clientSecret.setValue(CLIENT_SECRET);
        properties.getConnectionProperties().datastorePath.setValue("target");

        return properties;
    }

    public GoogleDriveProvideConnectionProperties setupConnectionWithInstalledApplicationWithJson(
            GoogleDriveProvideConnectionProperties properties) {
        properties.getConnectionProperties().applicationName.setValue("IT");
        properties.getConnectionProperties().oAuthMethod.setValue(InstalledApplicationWithJSON);
        properties.getConnectionProperties().clientSecretFile.setValue("secretFile");
        properties.getConnectionProperties().datastorePath.setValue("target");

        return properties;
    }

    protected FileList createFolderFileList(String folderId, boolean createDuplicate) {
        FileList fileList = new FileList();
        java.util.List<com.google.api.services.drive.model.File> files = new ArrayList<>();
        com.google.api.services.drive.model.File file = new com.google.api.services.drive.model.File();
        file.setId(folderId);
        files.add(file);
        //
        if (createDuplicate) {
            com.google.api.services.drive.model.File fileDup = new com.google.api.services.drive.model.File();
            fileDup.setId(folderId);
            files.add(fileDup);
        }
        fileList.setFiles(files);

        return fileList;
    }

}
