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

package org.talend.components.elasticsearch;

import org.osgi.service.component.annotations.Component;
import org.talend.components.api.AbstractComponentFamilyDefinition;
import org.talend.components.api.ComponentInstaller;
import org.talend.components.api.Constants;
import org.talend.components.elasticsearch.input.ElasticsearchInputDefinition;
import org.talend.components.elasticsearch.output.ElasticsearchOutputDefinition;

import com.google.auto.service.AutoService;

/**
 * Install all of the definitions provided for the Elasticsearch family of components.
 */
@AutoService(ComponentInstaller.class)
@Component(name = Constants.COMPONENT_INSTALLER_PREFIX
        + ElasticsearchComponentFamilyDefinition.NAME, service = ComponentInstaller.class)
public class ElasticsearchComponentFamilyDefinition extends AbstractComponentFamilyDefinition implements ComponentInstaller {

    public static final String NAME = "Elasticsearch";

    public static final String MAVEN_GROUP_ID = "org.talend.components";

    public static final String MAVEN_RUNTIME_ARTIFACT_ID = "elasticsearch-runtime_2_4";

    public static final String MAVEN_RUNTIME_URI = "mvn:" + MAVEN_GROUP_ID + "/" + MAVEN_RUNTIME_ARTIFACT_ID;

    public ElasticsearchComponentFamilyDefinition() {
        super(NAME, new ElasticsearchDatastoreDefinition(), new ElasticsearchDatasetDefinition(),
                new ElasticsearchInputDefinition(), new ElasticsearchOutputDefinition());
    }

    public void install(ComponentFrameworkContext ctx) {
        ctx.registerComponentFamilyDefinition(this);
    }
}
