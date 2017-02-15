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
package org.talend.components.api.testcomponent;

import static org.talend.daikon.properties.presentation.Widget.*;
import static org.talend.daikon.properties.property.PropertyFactory.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.avro.Schema;
import org.talend.components.api.component.Connector;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.api.properties.ComponentPropertiesImpl;
import org.talend.components.api.properties.ComponentReferenceProperties;
import org.talend.components.api.properties.ComponentReferencePropertiesEnclosing;
import org.talend.components.api.testcomponent.nestedprop.NestedComponentProperties;
import org.talend.components.api.testcomponent.nestedprop.inherited.InheritedComponentProperties;
import org.talend.daikon.properties.PresentationItem;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.service.Repository;

public class TestComponentProperties extends ComponentPropertiesImpl implements ComponentReferencePropertiesEnclosing {

    public static final String USER_ID_PROP_NAME = "userId"; //$NON-NLS-1$

    public Form mainForm;

    public Form restoreForm;

    public Property<Schema> mainOutput = newSchema("mainOutput");

    public PresentationItem testPI = new PresentationItem("testPI", "testPI display name");

    public Property<String> userId = newProperty(USER_ID_PROP_NAME).setRequired();

    public Property<String> password = newProperty("password").setRequired()
            .setFlags(EnumSet.of(Property.Flags.ENCRYPT, Property.Flags.SUPPRESS_LOGGING));

    public Property<String> nameList = newProperty("nameList");

    public Property<String> nameListRef = newProperty("nameListRef");

    public Property<Integer> integer = newInteger("integer");

    public Property<Integer> decimal = newInteger("decimal");

    public Property<Date> date = newDate("date");

    public Property<Date> dateTime = newDate("dateTime");

    // Used in testing refreshLayout
    public Property<Boolean> suppressDate = newBoolean("suppressDate");

    public Property<String> initLater = null;

    public NestedComponentProperties nestedInitLater = null;

    public NestedComponentProperties nestedProps = new NestedComponentProperties("nestedProps");

    public ComponentPropertiesWithDefinedI18N nestedProp2 = new ComponentPropertiesWithDefinedI18N("nestedProp2");

    public InheritedComponentProperties nestedProp3 = new InheritedComponentProperties("nestedProp3");

    public ComponentReferenceProperties referencedComponent = new ComponentReferenceProperties("referencedComponent",
            TestComponentDefinition.COMPONENT_NAME);

    public static final String TESTCOMPONENT = "TestComponent";

    public TestComponentProperties(String name) {
        super(name);
    }

    public ValidationResult beforeNameList() {
        List values = new ArrayList<>();
        Collections.addAll(values, new String[] { "name1", "name2", "name3" });
        nameList.setPossibleValues(values);
        return ValidationResult.OK;
    }

    public void beforeNameListRef() {
        List values = new ArrayList<>();
        Collections.addAll(values, new String[] { "namer1", "namer2", "namer3" });
        nameListRef.setPossibleValues(values);
    }

    public ValidationResult afterFormFinishMain(Repository<Properties> repo) {
        return new ValidationResult().setStatus(Result.ERROR);
    }

    public ValidationResult afterInteger() {
        return new ValidationResult().setStatus(Result.WARNING);
    }

    @Override
    public void afterReferencedComponent() {
        refreshLayout(getForm(Form.MAIN));
        refreshLayout(getForm(Form.REFERENCE));
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
        initLater = newProperty("initLater");
        nestedInitLater = new NestedComponentProperties("nestedInitLater");
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form form = Form.create(this, Form.MAIN);
        mainForm = form;
        form.addRow(userId);
        form.addRow(widget(password).setWidgetType(Widget.HIDDEN_TEXT_WIDGET_TYPE));
        form.addRow(testPI);
        form.addRow(widget(nameList).setWidgetType(Widget.NAME_SELECTION_AREA_WIDGET_TYPE));
        form.addRow(widget(nameListRef).setWidgetType(Widget.NAME_SELECTION_REFERENCE_WIDGET_TYPE));

        form = Form.create(this, "restoreTest");
        restoreForm = form;
        form.addRow(userId);
        form.addRow(nameList);
        form.addRow(integer);
        form.addRow(decimal);
        form.addRow(date);
        form.addRow(dateTime);
        form.addRow(nestedProps.getForm(Form.MAIN));

        Form refForm = new Form(this, Form.REFERENCE);
        Widget compListWidget = widget(referencedComponent).setWidgetType(Widget.COMPONENT_REFERENCE_WIDGET_TYPE);
        refForm.addRow(compListWidget);
        refForm.addRow(mainForm);
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
        if (form.getName().equals("restoreTest")) {
            if (suppressDate.getValue()) {
                form.getWidget("date").setHidden(true);
            }
        }
    }

    @Override
    public Schema getSchema(Connector connector, boolean isOutputConnection) {
        if (connector instanceof PropertyPathConnector) {
            Property<?> property = getValuedProperty(((PropertyPathConnector) connector).getPropertyPath());
            Object value = property.getValue();
            return value != null && Schema.class.isAssignableFrom(value.getClass()) ? (Schema) property.getValue() : null;
        } else {// not a connector handled by this class
            return null;
        }
    }

    @Override
    public Set<Connector> getAvailableConnectors(Set<? extends Connector> existingConnections, boolean isOutput) {
        HashSet<Connector> filteredConnectors = new HashSet<>(getAllConnectors());
        filteredConnectors.removeAll(existingConnections);
        return filteredConnectors;
    }

    public Set<? extends Connector> getAllConnectors() {
        return Collections.singleton(new PropertyPathConnector(Connector.MAIN_NAME, "mainOutput"));
    }

}
