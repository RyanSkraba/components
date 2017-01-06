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
package org.talend.components.kafka.output;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.avro.Schema;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.kafka.KafkaConfTableProperties;
import org.talend.components.kafka.KafkaIOBasedProperties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.EnumProperty;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

public class KafkaOutputProperties extends KafkaIOBasedProperties {

    public Property<PartitionType> partitionType = PropertyFactory.newEnum("partitionType", PartitionType.class);

    public Property<String> keyColumn = PropertyFactory.newString("keyColumn");

    public Property<Boolean> useCompress = PropertyFactory.newBoolean("useCompress", false);

    public EnumProperty<CompressType> compressType = PropertyFactory.newEnum("compressType", CompressType.class);

    public KafkaConfTableProperties configurations = new KafkaConfTableProperties("configurations");

    public KafkaOutputProperties(String name) {
        super(name);
    }

    public void afterUseCompress() {
        refreshLayout(getForm(Form.MAIN));
    }

    public void afterPartitionType() {
        refreshLayout(getForm(Form.MAIN));
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
        partitionType.setValue(PartitionType.ROUND_ROBIN);
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
        if (Form.MAIN.equals(form.getName())) {
            form.getWidget(compressType).setVisible(useCompress);
            form.getWidget(keyColumn).setVisible(partitionType.getValue() == PartitionType.COLUMN);
        }
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addRow(partitionType).addColumn(keyColumn);
        mainForm.addRow(useCompress).addColumn(compressType);
        mainForm.addRow(Widget.widget(configurations).setWidgetType(Widget.TABLE_WIDGET_TYPE));
    }

    /**
     * set possible value to keyColumn based on dataset's schema
     */
    public void beforeKeyColumn() {
        List<Schema.Field> fields = getDatasetProperties().main.schema.getValue().getFields();
        List<String> columnNames = new ArrayList<>();
        for (Schema.Field field : fields) {
            columnNames.add(field.name());
        }
        keyColumn.setPossibleValues(columnNames);
    }

    @Override
    protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputConnection) {
        HashSet<PropertyPathConnector> connectors = new HashSet<>();
        if (isOutputConnection) {
            return Collections.EMPTY_SET;
        } else {
            connectors.add(MAIN_CONNECTOR);
        }
        return connectors;
    }

    public enum CompressType {
        GZIP,
        SNAPPY
    }

    public enum PartitionType {
        // no key provided, kafka produce use round-robin as default partition strategy
        ROUND_ROBIN,
        // use the value of one column in the record as the key, and kafka will use this value to calculate partition
        COLUMN
    }
}
