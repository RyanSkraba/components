// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
/**
 *
 */
package org.talend.components.snowflake.tsnowflakeinput;

import static org.talend.daikon.properties.presentation.Widget.widget;
import static org.talend.daikon.properties.property.PropertyFactory.newBoolean;
import static org.talend.daikon.properties.property.PropertyFactory.newProperty;

import java.util.Collections;
import java.util.Set;

import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.common.ComponentConstants;
import org.talend.components.snowflake.SnowflakeGuessSchemaProperties;
import org.talend.daikon.properties.PresentationItem;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TSnowflakeInputProperties extends SnowflakeGuessSchemaProperties {

    public Property<String> condition = newProperty("condition"); //$NON-NLS-1$

    public Property<Boolean> manualQuery = newBoolean("manualQuery"); //$NON-NLS-1$

    public transient PresentationItem guessSchema = new PresentationItem("guessSchema");

    public Property<String> query = newProperty("query"); //$NON-NLS-1$

    public Property<Boolean> convertColumnsAndTableToUppercase = newBoolean("convertColumnsAndTableToUppercase", true);

    public TSnowflakeInputProperties(@JsonProperty("name") String name) {
        super(name);
    }

    @Override
    protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputConnection) {
        if (isOutputConnection) {
            return Collections.singleton(MAIN_CONNECTOR);
        } else {
            return Collections.emptySet();
        }
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
        manualQuery.setValue(false);
        query.setTaggedValue(ComponentConstants.LINE_SEPARATOR_REPLACED_TO, " ");
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = getForm(Form.MAIN);
        mainForm.addRow(manualQuery);
        mainForm.addColumn(Widget.widget(guessSchema).setWidgetType(Widget.BUTTON_WIDGET_TYPE).setLongRunning(true));
        mainForm.addRow(condition);
        mainForm.addRow(widget(query).setWidgetType(Widget.TEXT_AREA_WIDGET_TYPE));

        Form advancedForm = getForm(Form.ADVANCED);
        advancedForm.addRow(convertColumnsAndTableToUppercase);
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
        if (form.getName().equals(Form.MAIN)) {
            form.getWidget(guessSchema.getName()).setHidden(!manualQuery.getValue());
            form.getWidget(query.getName()).setHidden(!manualQuery.getValue());
            form.getWidget(condition.getName()).setHidden(manualQuery.getValue());
        } else if (form.getName().equals(Form.ADVANCED)) {
            form.getWidget(convertColumnsAndTableToUppercase.getName()).setHidden(manualQuery.getValue());
        }
    }

    @Override
    public ValidationResult validateGuessSchema() {
        return super.validateGuessSchema();
    }

    public void afterManualQuery() {
        refreshLayout(getForm(Form.MAIN));
    }

    public void afterGuessSchema() {
        refreshLayout(getForm(Form.MAIN));
    }

    @Override
    public String getQuery() {
        return query.getValue();
    }

}
