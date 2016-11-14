package org.talend.components.kafka.output;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.kafka.KafkaConfTableProperties;
import org.talend.components.kafka.KafkaIOBasedProperties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.EnumProperty;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

public class KafkaOutputProperties extends KafkaIOBasedProperties {

    public Property<Boolean> useCompress = PropertyFactory.newBoolean("useCompress", false);

    public EnumProperty<CompressType> compressType = PropertyFactory.newEnum("compressType", CompressType.class);

    public KafkaConfTableProperties configurations = new KafkaConfTableProperties("configurations");

    public KafkaOutputProperties(String name) {
        super(name);
    }

    public void afterUseCompress() {
        refreshLayout(getForm(Form.MAIN));
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
        if (Form.MAIN.equals(form.getName())) {
            form.getWidget(compressType).setVisible(useCompress);
        }
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addRow(useCompress).addColumn(compressType);
        mainForm.addRow(Widget.widget(configurations).setWidgetType(Widget.TABLE_WIDGET_TYPE));
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
}
