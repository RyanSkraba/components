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

package org.talend.components.pubsub;

import java.net.MalformedURLException;
import java.net.URL;

import org.talend.components.api.component.runtime.DependenciesReader;
import org.talend.components.api.component.runtime.JarRuntimeInfo;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.common.dataset.DatasetProperties;
import org.talend.components.common.datastore.DatastoreDefinition;
import org.talend.components.pubsub.input.PubSubInputDefinition;
import org.talend.components.pubsub.output.PubSubOutputDefinition;
import org.talend.daikon.definition.DefinitionImageType;
import org.talend.daikon.definition.I18nDefinition;
import org.talend.daikon.runtime.RuntimeInfo;


public class PubSubDatastoreDefinition extends I18nDefinition implements DatastoreDefinition<PubSubDatastoreProperties> {

    public static final String RUNTIME = "org.talend.components.pubsub.runtime.PubSubDatastoreRuntime";

    public static final String NAME = PubSubComponentFamilyDefinition.NAME + "Datastore";

    public PubSubDatastoreDefinition() {
        super(NAME);
    }

    @Override
    public Class<PubSubDatastoreProperties> getPropertiesClass() {
        return PubSubDatastoreProperties.class;
    }

    @Override
    public RuntimeInfo getRuntimeInfo(PubSubDatastoreProperties properties) {
        try {
            return new JarRuntimeInfo(new URL("mvn:org.talend.components/pubsub-runtime"),
                    DependenciesReader.computeDependenciesFilePath("org.talend.components", "pubsub-runtime"), RUNTIME);
        } catch (MalformedURLException e) {
            throw new ComponentException(e);
        }
    }

    @Deprecated
    @Override
    public String getImagePath(){
        return NAME + "_icon32.png";
    }

    @Override
    public String getImagePath(DefinitionImageType type) {
        switch (type) {
            case PALETTE_ICON_32X32:
                return NAME + "_icon32.png";
        }
        return null;
    }

    @Override
    public String getIconKey() {
        return "pubsub";
    }

    @Override
    public DatasetProperties createDatasetProperties(PubSubDatastoreProperties storeProp) {
        PubSubDatasetProperties setProp = new PubSubDatasetProperties(PubSubDatasetDefinition.NAME);
        setProp.init();
        setProp.setDatastoreProperties(storeProp);
        return setProp;
    }

    @Override
    public String getInputCompDefinitionName() {
        return PubSubInputDefinition.NAME;
    }

    @Override
    public String getOutputCompDefinitionName() {
        return PubSubOutputDefinition.NAME;
    }

}
