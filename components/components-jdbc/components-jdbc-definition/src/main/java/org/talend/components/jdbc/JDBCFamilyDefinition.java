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
package org.talend.components.jdbc;

import java.io.IOException;
import java.io.InputStream;

import org.talend.components.api.AbstractComponentFamilyDefinition;
import org.talend.components.api.ComponentInstaller;
import org.talend.components.api.Constants;
import org.talend.components.jdbc.dataprep.JDBCInputDefinition;
import org.talend.components.jdbc.dataset.JDBCDatasetDefinition;
import org.talend.components.jdbc.datastore.JDBCDatastoreDefinition;
import org.talend.components.jdbc.datastream.JDBCOutputDefinition;
import org.talend.components.jdbc.tjdbcclose.TJDBCCloseDefinition;
import org.talend.components.jdbc.tjdbccommit.TJDBCCommitDefinition;
import org.talend.components.jdbc.tjdbcconnection.TJDBCConnectionDefinition;
import org.talend.components.jdbc.tjdbcinput.TJDBCInputDefinition;
import org.talend.components.jdbc.tjdbcoutput.TJDBCOutputDefinition;
import org.talend.components.jdbc.tjdbcrollback.TJDBCRollbackDefinition;
import org.talend.components.jdbc.tjdbcrow.TJDBCRowDefinition;
import org.talend.components.jdbc.tjdbcsp.TJDBCSPDefinition;
import org.talend.components.jdbc.wizard.JDBCConnectionEditWizardDefinition;
import org.talend.components.jdbc.wizard.JDBCConnectionWizardDefinition;

import com.google.auto.service.AutoService;

import aQute.bnd.annotation.component.Component;

/**
 * Install all of the definitions provided for the JDBC family of components.
 */
@AutoService(ComponentInstaller.class)
@Component(name = Constants.COMPONENT_INSTALLER_PREFIX + JDBCFamilyDefinition.NAME, provide = ComponentInstaller.class)
public class JDBCFamilyDefinition extends AbstractComponentFamilyDefinition implements ComponentInstaller {

    public static final String NAME = "Jdbc";

    // DI Runtime informations
    public static final String RUNTIME_DI_MAVEN_GROUP_ID = "runtime.di.maven.groupId";

    public static final String RUNTIME_DI_MAVEN_ARTIFACT_ID = "runtime.di.maven.artifactId";

    public static final String RUNTIME_DI_MAVEN_VERSION = "runtime.di.maven.version";

    // Beam Runtime informations
    public static final String RUNTIME_BEAM_MAVEN_GROUP_ID = "runtime.beam.maven.groupId";

    public static final String RUNTIME_BEAM_MAVEN_ARTIFACT_ID = "runtime.beam.maven.artifactId";

    public static final String RUNTIME_BEAM_MAVEN_VERSION = "runtime.beam.maven.version";

    private static final java.util.Properties runtimeProperties;

    /**
     * Bootstrap runtime information form the <code>runtime.properties</code> file.
     * This file should be in src/main/runtime folder of the definition part of a component
     * and should contains the runtime properties defined above
     */
    static {
        ClassLoader loader = JDBCFamilyDefinition.class.getClassLoader();
        runtimeProperties = new java.util.Properties();
        try (InputStream resourceStream = loader
                .getResourceAsStream("META-INF/runtime/org.talend.components/components-jdbc-definition/runtime.properties")) {
            runtimeProperties.load(resourceStream);
        } catch (IOException e) {
            throw new RuntimeException("Error while bootstraping runtime properties", e);
        }
    }

    public JDBCFamilyDefinition() {
        super(NAME,
                // Components
                new TJDBCCloseDefinition(), new TJDBCCommitDefinition(), new TJDBCConnectionDefinition(),
                new TJDBCInputDefinition(), new TJDBCOutputDefinition(), new TJDBCRollbackDefinition(), new TJDBCRowDefinition(),
                new TJDBCSPDefinition(),
                // Component wizards
                new JDBCConnectionWizardDefinition(), new JDBCConnectionEditWizardDefinition(),
                // Datastore, Dataset and the component
                new JDBCDatastoreDefinition(), new JDBCDatasetDefinition(), new JDBCInputDefinition(),
                new JDBCOutputDefinition());
    }

    @Override
    public void install(ComponentFrameworkContext ctx) {
        ctx.registerComponentFamilyDefinition(this);
    }

    public static String getDIRuntimeGroupId() {
        return runtimeProperties.getProperty(RUNTIME_DI_MAVEN_GROUP_ID);
    }

    public static String getDIRuntimeArtifactId() {
        return runtimeProperties.getProperty(RUNTIME_DI_MAVEN_ARTIFACT_ID);
    }

    public static String getDIRuntimeVersion() {
        return runtimeProperties.getProperty(RUNTIME_DI_MAVEN_VERSION);
    }

    public static String getDIRuntimeMavenURI() {
        return "mvn:" + getDIRuntimeGroupId() + "/" + getDIRuntimeArtifactId() + "/" + getDIRuntimeVersion();
    }

    public static String getBeamRuntimeArtifactId() {
        return runtimeProperties.getProperty(RUNTIME_BEAM_MAVEN_ARTIFACT_ID);
    }

    public static String getBeamRuntimeGroupId() {
        return runtimeProperties.getProperty(RUNTIME_BEAM_MAVEN_GROUP_ID);
    }

    public static String getBeamRuntimeVersion() {
        return runtimeProperties.getProperty(RUNTIME_BEAM_MAVEN_VERSION);
    }

    public static String getBeamRuntimeMavenURI() {
        return "mvn:" + getDIRuntimeGroupId() + "/" + getBeamRuntimeArtifactId() + "/" + getBeamRuntimeVersion();
    }

}
