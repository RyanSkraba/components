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
package org.talend.components.common.format;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.talend.daikon.definition.Definition;
import org.talend.daikon.definition.service.DefinitionRegistryService;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.PropertiesImpl;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

public abstract class FormatPropertiesProvider<V extends Properties> extends PropertiesImpl {

    private static final long serialVersionUID = 7293969314564405598L;

    private static final String FORMAT_PROPERTIES_NAME = "formatProperties";

    @Inject
    public transient DefinitionRegistryService definitionRegistry;

    public final Property<String> format = PropertyFactory.newString("format");

    public V formatProperties;

    private final Class<? extends Definition<V>> definitionClass;

    private transient String previousFormatValue = "";

    private transient Map<String, ? extends Definition<V>> allFormatDefs = new HashMap<>();

    private transient Map<String, V> referenceMemento = new HashMap<>();

    public FormatPropertiesProvider(String name, Class<? extends Definition<V>> definitionClass) {
        super(name);
        this.definitionClass = definitionClass;
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
        initFormatProperty();
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        // create the form and add the combo for formats.
        // The format properties will be setup in refreshLayout
        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addRow(Widget.widget(format).setWidgetType(Widget.ENUMERATION_WIDGET_TYPE));
        refreshLayout(mainForm);
    }

    public void afterFormat() {
        refreshLayout(getForm(Form.MAIN));
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
        if (form.getName().equals(Form.MAIN)) {
            createFormatValues();
            Widget w = form.getWidget(FORMAT_PROPERTIES_NAME);
            if (!allFormatDefs.isEmpty() && w != null) {
                updateProperties();
                form.replaceRow(FORMAT_PROPERTIES_NAME, formatProperties.getForm(Form.MAIN));
            } else if (!allFormatDefs.isEmpty() && w == null) {
                updateProperties();
                form.addRow(formatProperties.getForm(Form.MAIN));
            }
            form.getWidget(format.getName()).setReadonly(allFormatDefs.isEmpty());
        }
    }

    protected Class<? extends Definition<V>> getDefinitionClass() {
        return definitionClass;
    }

    private void initFormatProperty() {
        createFormatValues();
        updateProperties();
    }

    private void createFormatValues() {
        allFormatDefs = getPossibleFormatValues();
        Object[] defsNames = allFormatDefs.keySet().toArray();
        if (defsNames.length > 0) {
            format.setPossibleValues(defsNames);
            if (format.getValue() == null || format.getValue().isEmpty()) {
                format.setStoredValue(defsNames[0]);
            }
        }
    }

    private void updateProperties() {
        String selectedFileFormatDefinitionStr = format.getValue();
        if (!StringUtils.isEmpty(selectedFileFormatDefinitionStr)
                && !StringUtils.equals(previousFormatValue, selectedFileFormatDefinitionStr)) {
            V props = referenceMemento.get(selectedFileFormatDefinitionStr);
            if (props == null) {
                Definition<V> fileFormatDefinition = getDefinitionRegistry().getDefinitionsMapByType(getDefinitionClass())
                        .get(selectedFileFormatDefinitionStr);
                props = getDefinitionRegistry().createProperties(fileFormatDefinition, FORMAT_PROPERTIES_NAME);
                referenceMemento.put(selectedFileFormatDefinitionStr, props);
            }
            formatProperties = props;
        } else if (!StringUtils.isEmpty(selectedFileFormatDefinitionStr)
                && StringUtils.equals(previousFormatValue, selectedFileFormatDefinitionStr)) {
            // After deserialization we already have formatProperties, but we need to put them to referenceMemento
            referenceMemento.put(selectedFileFormatDefinitionStr, (V) formatProperties);
        }
        previousFormatValue = selectedFileFormatDefinitionStr;
    }

    protected DefinitionRegistryService getDefinitionRegistry() {
        return definitionRegistry;
    }

    protected Map<String, ? extends Definition<V>> getPossibleFormatValues() {
        DefinitionRegistryService registry = getDefinitionRegistry();
        if (registry == null) {
            return Collections.emptyMap();
        }
        return registry.getDefinitionsMapByType(getDefinitionClass());
    }

    public V getFormatProperties() {
        if (format.getValue() == null || format.getValue().isEmpty())
            return null;
        return (V) formatProperties;
    }

    @Override
    protected boolean acceptUninitializedField(Field f) {
        if (super.acceptUninitializedField(f)) {
            return true;
        }
        // we accept that return field is not initialized after setupProperties.
        return "formatProperties".equals(f.getName());
    }

}
