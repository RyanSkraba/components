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
package org.talend.components.kafka;

import org.talend.components.api.AbstractComponentFamilyDefinition;
import org.talend.components.api.ComponentInstaller;
import org.talend.components.api.Constants;
import org.talend.components.kafka.dataset.KafkaDatasetDefinition;
import org.talend.components.kafka.datastore.KafkaDatastoreDefinition;
import org.talend.components.kafka.input.KafkaInputDefinition;
import org.talend.components.kafka.output.KafkaOutputDefinition;

import aQute.bnd.annotation.component.Component;

/**
 * Install all of the definitions provided for the Kafka family of components.
 */
@Component(name = Constants.COMPONENT_INSTALLER_PREFIX + KafkaFamilyDefinition.NAME, provide = ComponentInstaller.class)
public class KafkaFamilyDefinition extends AbstractComponentFamilyDefinition implements ComponentInstaller {

    public static final String NAME = "Kafka";

    public KafkaFamilyDefinition() {
        super(NAME, new KafkaDatastoreDefinition(), new KafkaDatasetDefinition(), new KafkaInputDefinition(),
                new KafkaOutputDefinition());
    }

    public void install(ComponentFrameworkContext ctx) {
        ctx.registerComponentFamilyDefinition(this);
    }
}
