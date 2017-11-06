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
package org.talend.components.processing.definition.typeconverter;

import org.apache.avro.Schema;
import org.talend.components.api.component.Connector;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.common.FixedConnectorsComponentProperties;
import org.talend.daikon.converter.Converter;
import org.talend.daikon.converter.TypeConverter;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.PropertiesImpl;
import org.talend.daikon.properties.PropertiesList;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

public class TypeConverterProperties extends FixedConnectorsComponentProperties implements Serializable {

    /**
     * Output types enum
     */
    public enum TypeConverterOutputTypes {
        Boolean(Schema.Type.BOOLEAN, Boolean.class),
        Decimal(Schema.Type.BYTES, BigDecimal.class),
        Double(Schema.Type.DOUBLE, Double.class),
        Float(Schema.Type.FLOAT, Float.class),
        Integer(Schema.Type.INT, Integer.class),
        Long(Schema.Type.LONG, Long.class),
        String(Schema.Type.STRING, String.class),
        Date(Schema.Type.INT, LocalDate.class),
        Time(Schema.Type.INT, LocalTime.class),
        DateTime(Schema.Type.LONG, LocalDateTime.class);

        // Avro schema output type
        private Schema.Type targetType;

        private Class targetClass;

        private TypeConverterOutputTypes(Schema.Type targetType, Class targetClass) {

            this.targetType = targetType;
            this.targetClass = targetClass;
        }

        public Schema.Type getTargetType() {

            return this.targetType;
        }

        public Class getTargetClass(){
            return this.targetClass;
        }

        public Converter getConverter(){
            return TypeConverter.as(this.targetClass);
        }

    }

    /**
     * Nested properties
     */
    public static class TypeConverterPropertiesInner extends PropertiesImpl {

        public Property<String> field = PropertyFactory.newString("field").setRequired().setValue("");

        public Property<TypeConverterOutputTypes> outputType = PropertyFactory
                .newEnum("outputType", TypeConverterOutputTypes.class).setRequired();

        public Property<String> outputFormat = PropertyFactory.newString("outputFormat").setValue("");

        public TypeConverterPropertiesInner(String name) {
            super(name);
        }

        @Override
        public void setupLayout() {
            super.setupLayout();
            Form mainForm = new Form(this, Form.MAIN);
            mainForm.addRow(Widget.widget(field).setWidgetType(Widget.DATALIST_WIDGET_TYPE));
            mainForm.addColumn(Widget.widget(outputType).setWidgetType(Widget.ENUMERATION_WIDGET_TYPE));
            mainForm.addColumn(outputFormat);
        }

    }

    public PropertiesList<TypeConverterPropertiesInner> converters = new PropertiesList<>("converters",
            new PropertiesList.NestedPropertiesFactory() {

                @Override
                public Properties createAndInit(String name) {
                    return new TypeConverterPropertiesInner(name).init();
                }
            });

    public transient PropertyPathConnector INCOMING_CONNECTOR = new PropertyPathConnector(Connector.MAIN_NAME, "incoming");

    public transient PropertyPathConnector OUTGOING_CONNECTOR = new PropertyPathConnector(Connector.MAIN_NAME, "outgoing");

    public TypeConverterProperties(String name) {
        super(name);
        converters.init();
    }

    @Override
    protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputConnection) {
        HashSet<PropertyPathConnector> connectors = new HashSet<PropertyPathConnector>();
        if (isOutputConnection) {
            connectors.add(OUTGOING_CONNECTOR);
        } else {
            // input schema
            connectors.add(INCOMING_CONNECTOR);
        }
        return connectors;
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addRow(Widget.widget(converters).setWidgetType(Widget.NESTED_PROPERTIES)
                .setConfigurationValue(Widget.NESTED_PROPERTIES_TYPE_OPTION, "converter"));
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
        setupLayout();
        // Add a default converter
        converters.createAndAddRow();
    }
}
