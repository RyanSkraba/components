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

package org.talend.components.localio.fixed;

import java.net.MalformedURLException;
import java.net.URL;

import org.talend.components.api.component.runtime.DependenciesReader;
import org.talend.components.api.component.runtime.JarRuntimeInfo;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.common.dataset.DatasetProperties;
import org.talend.components.common.datastore.DatastoreDefinition;
import org.talend.components.localio.LocalIOComponentFamilyDefinition;
import org.talend.components.localio.devnull.DevNullOutputDefinition;
import org.talend.daikon.definition.DefinitionImageType;
import org.talend.daikon.definition.I18nDefinition;
import org.talend.daikon.runtime.RuntimeInfo;

/**
 * A datastore that does not require any configuration or persistent resources, used to coordinate the fixed datasets.
 */
public class FixedDatastoreDefinition extends I18nDefinition implements DatastoreDefinition<FixedDatastoreProperties> {

    public static final String RUNTIME = "org.talend.components.localio.runtime.fixed.FixedDatastoreRuntime";

    public static final String NAME = "FixedDatastore";

    public static final boolean IS_CLASSLOADER_REUSABLE = true;

    public FixedDatastoreDefinition() {
        super(NAME);
    }

    @Override
    public Class<FixedDatastoreProperties> getPropertiesClass() {
        return FixedDatastoreProperties.class;
    }

    @Deprecated
    @Override
    public String getImagePath() {
        return null;
    }

    @Override
    public String getImagePath(DefinitionImageType type) {
        return null;
    }

    @Override
    public String getIconKey() {
        return "flow-source-o";
    }

    @Override
    public DatasetProperties createDatasetProperties(FixedDatastoreProperties storeProp) {
        FixedDatasetProperties setProp = new FixedDatasetProperties(FixedDatasetDefinition.NAME);
        setProp.init();
        setProp.setDatastoreProperties(storeProp);
        return setProp;
    }

    @Override
    public String getInputCompDefinitionName() {
        return FixedInputDefinition.NAME;
    }

    @Override
    public String getOutputCompDefinitionName() {
        // There is no output component for this datastore.
        return DevNullOutputDefinition.NAME;
    }

    @Override
    public RuntimeInfo getRuntimeInfo(FixedDatastoreProperties properties) {
        try {
            return new JarRuntimeInfo(new URL(LocalIOComponentFamilyDefinition.MAVEN_DEFAULT_RUNTIME_URI),
                    DependenciesReader.computeDependenciesFilePath(LocalIOComponentFamilyDefinition.MAVEN_GROUP_ID,
                            LocalIOComponentFamilyDefinition.MAVEN_DEFAULT_RUNTIME_ARTIFACT_ID),
                    RUNTIME, IS_CLASSLOADER_REUSABLE);
        } catch (MalformedURLException e) {
            throw new ComponentException(e);
        }
    }
}
