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
package org.talend.components.kafka.input;

import java.util.Collections;
import java.util.Set;

import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.kafka.KafkaConfTableProperties;
import org.talend.components.kafka.KafkaIOBasedProperties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.EnumProperty;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

public class KafkaInputProperties extends KafkaIOBasedProperties {

    public Property<String> groupID = PropertyFactory.newString("groupID");

    public EnumProperty<OffsetType> autoOffsetReset = PropertyFactory.newEnum("autoOffsetReset", OffsetType.class);

    public Property<Boolean> useMaxReadTime = PropertyFactory.newBoolean("useMaxReadTime", false);

    // Max duration(Millions) from start receiving
    public Property<Long> maxReadTime = PropertyFactory.newProperty(Long.class, "maxReadTime");

    public Property<Boolean> useMaxNumRecords = PropertyFactory.newBoolean("useMaxNumRecords", false);

    public Property<Long> maxNumRecords = PropertyFactory.newProperty(Long.class, "maxNumRecords");

    public KafkaConfTableProperties configurations = new KafkaConfTableProperties("configurations");

    // auto commit offset CAUSE problem for KafkaIO?
    // stop after maximum time waiting between message DO NOT supported by KafkaIO!

    public KafkaInputProperties(String name) {
        super(name);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
        autoOffsetReset.setValue(OffsetType.LATEST);
        maxReadTime.setValue(600000L);
        maxNumRecords.setValue(5000L);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form main = new Form(this, Form.MAIN);
        main.addRow(groupID);
        main.addRow(autoOffsetReset);
        main.addRow(useMaxReadTime).addColumn(maxReadTime);
        main.addRow(useMaxNumRecords).addColumn(maxNumRecords);
        main.addRow(Widget.widget(configurations).setWidgetType(Widget.TABLE_WIDGET_TYPE));
    }

    public void afterUseMaxReadTime() {
        refreshLayout(getForm(Form.MAIN));
    }

    public void afterUseMaxNumRecords() {
        refreshLayout(getForm(Form.MAIN));
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
        if (form.getName().equals(Form.MAIN)) {
            form.getWidget(maxReadTime).setVisible(useMaxReadTime);
            form.getWidget(maxNumRecords).setVisible(useMaxNumRecords);
        }
    }

    @Override
    protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputConnection) {
        if (isOutputConnection) {
            return Collections.singleton(MAIN_CONNECTOR);
        } else {
            return Collections.EMPTY_SET;
        }
    }

    // sync with org.apache.kafka.clients.consumer.OffsetResetStrategy
    public enum OffsetType {
        LATEST,
        EARLIEST
    }
}
