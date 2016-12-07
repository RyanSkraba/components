// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.components.simplefileio;

import java.net.MalformedURLException;
import java.net.URL;

import org.talend.components.api.component.runtime.DependenciesReader;
import org.talend.components.api.component.runtime.JarRuntimeInfo;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.common.dataset.DatasetProperties;
import org.talend.components.common.datastore.DatastoreDefinition;
import org.talend.components.simplefileio.input.SimpleFileIoInputDefinition;
import org.talend.components.simplefileio.output.SimpleFileIoOutputDefinition;
import org.talend.daikon.definition.I18nDefinition;
import org.talend.daikon.runtime.RuntimeInfo;

public class SimpleFileIoDatastoreDefinition extends I18nDefinition implements
        DatastoreDefinition<SimpleFileIoDatastoreProperties> {

    public static final String RUNTIME = "org.talend.components.simplefileio.runtime.SimpleFileIoDatastoreRuntime";

    public static final String NAME = SimpleFileIoComponentFamilyDefinition.NAME + "Datastore";

    public SimpleFileIoDatastoreDefinition() {
        super(NAME);
    }

    @Override
    public Class<SimpleFileIoDatastoreProperties> getPropertiesClass() {
        return SimpleFileIoDatastoreProperties.class;
    }

    @Override
    public RuntimeInfo getRuntimeInfo(SimpleFileIoDatastoreProperties properties, Object ctx) {
        try {
            return new JarRuntimeInfo(new URL("mvn:org.talend.components/simplefileio-runtime"),
                    DependenciesReader.computeDependenciesFilePath("org.talend.components", "simplefileio-runtime"), RUNTIME);
        } catch (MalformedURLException e) {
            throw new ComponentException(e);
        }
    }

    @Override
    public String getImagePath() {
        return NAME + "_icon32.png";
    }

    @Override
    public DatasetProperties createDatasetProperties(SimpleFileIoDatastoreProperties storeProp) {
        SimpleFileIoDatasetProperties setProp = new SimpleFileIoDatasetProperties(SimpleFileIoDatasetDefinition.NAME);
        setProp.init();
        setProp.setDatastoreProperties(storeProp);
        return setProp;
    }

    @Override
    public String getInputCompDefinitionName() {
        return SimpleFileIoInputDefinition.NAME;
    }

    @Override
    public String getOutputCompDefinitionName() {
        return SimpleFileIoOutputDefinition.NAME;
    }
}
