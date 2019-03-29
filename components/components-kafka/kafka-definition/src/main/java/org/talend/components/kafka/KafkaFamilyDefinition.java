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
package org.talend.components.kafka;

import org.osgi.service.component.annotations.Component;
import org.talend.components.api.AbstractComponentFamilyDefinition;
import org.talend.components.api.ComponentInstaller;
import org.talend.components.api.Constants;
import org.talend.components.kafka.dataset.KafkaDatasetDefinition;
import org.talend.components.kafka.datastore.KafkaDatastoreDefinition;
import org.talend.components.kafka.input.KafkaInputDefinition;
import org.talend.components.kafka.output.KafkaOutputDefinition;

import com.google.auto.service.AutoService;

/**
 * Install all of the definitions provided for the Kafka family of components.
 */
@AutoService(ComponentInstaller.class)
@Component(name = Constants.COMPONENT_INSTALLER_PREFIX + KafkaFamilyDefinition.NAME, service = ComponentInstaller.class)
public class KafkaFamilyDefinition extends AbstractComponentFamilyDefinition implements ComponentInstaller {

    public static final String NAME = "Kafka";

    public static final String MAVEN_GROUP_ID = "org.talend.components";

    public static final String MAVEN_DEFAULT_RUNTIME_ARTIFACT_ID = "kafka-runtime";

    public static final String MAVEN_DEFAULT_RUNTIME_URI = "mvn:" + MAVEN_GROUP_ID + "/" + MAVEN_DEFAULT_RUNTIME_ARTIFACT_ID;

    public KafkaFamilyDefinition() {
        super(NAME, new KafkaDatastoreDefinition(), new KafkaDatasetDefinition(), new KafkaInputDefinition(),
                new KafkaOutputDefinition());
    }

    public void install(ComponentFrameworkContext ctx) {
        ctx.registerComponentFamilyDefinition(this);
    }
}
