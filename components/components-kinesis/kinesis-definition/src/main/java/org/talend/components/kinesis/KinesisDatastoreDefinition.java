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

package org.talend.components.kinesis;

import java.net.MalformedURLException;
import java.net.URL;

import org.talend.components.api.component.runtime.DependenciesReader;
import org.talend.components.api.component.runtime.JarRuntimeInfo;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.common.dataset.DatasetProperties;
import org.talend.components.common.datastore.DatastoreDefinition;
import org.talend.components.kinesis.input.KinesisInputDefinition;
import org.talend.daikon.definition.DefinitionImageType;
import org.talend.daikon.definition.I18nDefinition;
import org.talend.daikon.runtime.RuntimeInfo;

public class KinesisDatastoreDefinition extends I18nDefinition
        implements DatastoreDefinition<KinesisDatastoreProperties> {

    public static final String RUNTIME = "org.talend.components.kinesis.runtime.KinesisDatastoreRuntime";

    public static final String NAME = KinesisComponentFamilyDefinition.NAME + "Datastore";

    public KinesisDatastoreDefinition() {
        super(NAME);
    }

    @Override
    public Class<KinesisDatastoreProperties> getPropertiesClass() {
        return KinesisDatastoreProperties.class;
    }

    @Override
    public RuntimeInfo getRuntimeInfo(KinesisDatastoreProperties properties) {
        try {
            return new JarRuntimeInfo(new URL("mvn:org.talend.components/kinesis-runtime"),
                    DependenciesReader.computeDependenciesFilePath("org.talend.components", "kinesis-runtime"),
                    RUNTIME);
        } catch (MalformedURLException e) {
            throw new ComponentException(e);
        }
    }

    @Deprecated
    @Override
    public String getImagePath() {
        return NAME + "_icon32.png";
    }

    @Override
    public String getImagePath(DefinitionImageType type) {
        switch (type) {
        case PALETTE_ICON_32X32:
            return NAME + "_icon32.png";
        case SVG_ICON:
            return NAME + ".svg";
        }
        return null;
    }

    @Override
    public String getIconKey() {
        return "AWS-kinesis";
    }

    @Override
    public DatasetProperties createDatasetProperties(KinesisDatastoreProperties storeProp) {
        KinesisDatasetProperties setProp = new KinesisDatasetProperties(KinesisDatasetDefinition.NAME);
        setProp.init();
        setProp.setDatastoreProperties(storeProp);
        return setProp;
    }

    @Override
    public String getInputCompDefinitionName() {
        return KinesisInputDefinition.NAME;
    }

    @Override
    public String getOutputCompDefinitionName() {
        // no output component now
        return null;
    }

}
