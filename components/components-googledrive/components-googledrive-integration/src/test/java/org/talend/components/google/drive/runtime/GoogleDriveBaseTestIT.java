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

import static org.junit.Assert.*;
import static org.slf4j.LoggerFactory.getLogger;
import static org.talend.components.google.drive.runtime.GoogleDriveRuntime.getStudioName;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.avro.generic.IndexedRecord;
import org.junit.Assume;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.google.drive.connection.GoogleDriveConnectionProperties;
import org.talend.components.google.drive.connection.GoogleDriveConnectionProperties.OAuthMethod;
import org.talend.components.google.drive.create.GoogleDriveCreateDefinition;
import org.talend.components.google.drive.create.GoogleDriveCreateProperties;
import org.talend.components.google.drive.list.GoogleDriveListProperties;
import org.talend.components.google.drive.list.GoogleDriveListProperties.ListMode;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;

public class GoogleDriveBaseTestIT {

    public static final String GOOGLEDRIVE_SERVICE_ACCOUNT_PROP = "org.talend.components.google.drive.service_account_file";

    public static final String DRIVE_ROOT = "root";

    public static final String TEST_NAME = "GoogleDrivesockettimeout-test";

    public static String GOOGLEDRIVE_SERVICE_ACCOUNT_FILE;

    protected String testTS;

    protected GoogleDriveSource source;

    protected GoogleDriveSink sink;

    protected GoogleDriveConnectionProperties connectionProperties;

    protected GoogleDriveCreateProperties createProperties;

    protected GoogleDriveCreateRuntime createRuntime;

    protected GoogleDriveListProperties listDriveProperties;

    protected RuntimeContainer container = new RuntimeContainer() {

        private Map<String, Object> map = new HashMap<>();

        @Override
        public Object getComponentData(String componentId, String key) {
            return map.get(componentId + "_" + key);
        }

        @Override
        public void setComponentData(String componentId, String key, Object data) {
            map.put(componentId + "_" + key, data);
        }

        @Override
        public String getCurrentComponentId() {
            return "GoogleDrive-ITs";
        }

        @Override
        public Object getGlobalData(String key) {
            return null;
        }

    };

    protected transient static final Logger LOG = getLogger(GoogleDriveBaseTestIT.class);

    static {
        GOOGLEDRIVE_SERVICE_ACCOUNT_FILE = System.getProperty(GOOGLEDRIVE_SERVICE_ACCOUNT_PROP);
    }

    @ClassRule
    public static final TestRule DISABLE_IF_MISSING = new DisableIfMissingConfig(GOOGLEDRIVE_SERVICE_ACCOUNT_PROP);

    /**
     * Sometimes, we may have some SocketTimeOut exceptions, in that case we don't consider that the test has failed. It
     * won't break the whole build.
     */
    @Rule
    public TestRule skipRule = new TestRule() {

        public Statement apply(final Statement base, Description desc) {
            return new Statement() {

                public void evaluate() throws Throwable {
                    try {
                        base.evaluate();
                    } catch (SocketTimeoutException ex) {
                        LOG.error("{} caught during test execution.", ex.getMessage());
                        // If called with an expression evaluating to false, the test will halt and be ignored.
                        Assume.assumeTrue(false);
                    }
                }
            };
        }
    };

    @Before
    public void setUp() throws Exception {
        testTS = String.valueOf(new Date().getTime());
        //
        source = new GoogleDriveSource();
        sink = new GoogleDriveSink();
        connectionProperties = new GoogleDriveConnectionProperties(TEST_NAME);
        connectionProperties.setupProperties();
        connectionProperties.setupLayout();
        connectionProperties.applicationName.setValue("TCOMP-IT");
        connectionProperties.oAuthMethod.setValue(OAuthMethod.ServiceAccount);
        connectionProperties.serviceAccountFile.setValue(GOOGLEDRIVE_SERVICE_ACCOUNT_FILE);
        connectionProperties.afterOAuthMethod();
        //
        createProperties = new GoogleDriveCreateProperties(TEST_NAME);
        createProperties.init();
        createProperties.setupProperties();
        createProperties.connection = connectionProperties;
        createProperties.parentFolder.setValue(DRIVE_ROOT);
        createRuntime = new GoogleDriveCreateRuntime();
        //
        listDriveProperties = new GoogleDriveListProperties(TEST_NAME);
        listDriveProperties.init();
        listDriveProperties.setupProperties();
        listDriveProperties.connection = connectionProperties;
        listDriveProperties.setupLayout();
        listDriveProperties.folder.setValue(DRIVE_ROOT);
        listDriveProperties.includeSubDirectories.setValue(true);
        listDriveProperties.listMode.setValue(ListMode.Both);
    }

    protected void createFolder(String parent, String folder) throws GeneralSecurityException, IOException {
        createProperties.parentFolder.setValue(parent);
        createProperties.newFolder.setValue(folder);
        createRuntime.initialize(container, createProperties);
        createRuntime.runAtDriver(container);
        assertNotNull(container.getComponentData(container.getCurrentComponentId(),
                getStudioName(GoogleDriveCreateDefinition.RETURN_NEW_FOLDER_ID)));
    }

    protected void createFolderAtRoot(String folder) throws GeneralSecurityException, IOException {
        createFolder(DRIVE_ROOT, folder);
    }

    protected boolean resourceExists(String start, String resourceName) throws IOException {
        boolean found = false;
        listDriveProperties.folder.setValue(start);
        ValidationResult vr = source.initialize(container, listDriveProperties);
        assertEquals(Result.OK, vr.getStatus());
        GoogleDriveListReader reader = (GoogleDriveListReader) source.createReader(container);
        assertNotNull(reader);
        assertTrue(reader.start());
        IndexedRecord res = reader.getCurrent();
        assertNotNull(res);
        if (resourceName.equals(res.get(1))) {
            found = true;
        }
        while (reader.advance()) {
            res = reader.getCurrent();
            assertNotNull(res);
            if (resourceName.equals(res.get(1))) {
                found = true;
            }
        }
        return found;
    }

}
