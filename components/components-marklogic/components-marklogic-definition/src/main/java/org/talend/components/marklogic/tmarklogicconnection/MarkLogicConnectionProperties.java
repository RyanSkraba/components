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
package org.talend.components.marklogic.tmarklogicconnection;

import static org.talend.daikon.properties.presentation.Widget.widget;
import static org.talend.daikon.properties.property.PropertyFactory.newInteger;
import static org.talend.daikon.properties.property.PropertyFactory.newString;

import java.util.EnumSet;

import org.talend.components.api.properties.ComponentPropertiesImpl;
import org.talend.components.api.properties.ComponentReferenceProperties;
import org.talend.components.common.datastore.DatastoreProperties;
import org.talend.components.common.datastore.runtime.DatastoreRuntime;
import org.talend.components.marklogic.MarkLogicProvideConnectionProperties;
import org.talend.components.marklogic.RuntimeInfoProvider;
import org.talend.components.marklogic.data.MarkLogicDatastoreDefinition;
import org.talend.daikon.properties.PresentationItem;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;
import org.talend.daikon.properties.ValidationResultMutable;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.StringProperty;
import org.talend.daikon.properties.service.Repository;
import org.talend.daikon.sandbox.SandboxInstanceFactory;
import org.talend.daikon.sandbox.SandboxedInstance;

public class MarkLogicConnectionProperties extends ComponentPropertiesImpl implements MarkLogicProvideConnectionProperties, DatastoreProperties {

    public final ComponentReferenceProperties<MarkLogicConnectionProperties> referencedComponent = new ComponentReferenceProperties<>(
            "referencedComponent", MarkLogicConnectionDefinition.COMPONENT_NAME);

    public StringProperty host = newString("host");

    public Property<Integer> port = newInteger("port", 8000);

    public StringProperty database = newString("database");

    public Property<String> username = newString("username");

    public Property<String> password = newString("password")
            .setFlags(EnumSet.of(Property.Flags.ENCRYPT, Property.Flags.SUPPRESS_LOGGING));

    public Property<String> authentication = newString("authentication");

    //for wizzard usage
    public Property<String> name = newString("name").setRequired();

    private String repositoryLocation;

    public static final String WIZARD = "wizardForm";

    public PresentationItem testConnection = new PresentationItem("testConnection");

    public MarkLogicConnectionProperties(String name) {
        super(name);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
        host.setRequired();
        host.setValue("127.0.0.1");
        port.setRequired();
        port.setValue(8000);
        database.setRequired();
        database.setValue("Documents");
        username.setRequired();
        password.setRequired();
        authentication.setPossibleValues("DIGEST", "BASIC");
        authentication.setValue("DIGEST");

    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form wizardForm = Form.create(this, WIZARD);
        wizardForm.addRow(name);
        wizardForm.addRow(host);
        wizardForm.addRow(port);
        wizardForm.addRow(database);
        wizardForm.addRow(username);
        wizardForm.addColumn(password);
        wizardForm.addColumn(widget(authentication).setWidgetType(Widget.ENUMERATION_WIDGET_TYPE));
        wizardForm.addColumn(widget(testConnection).setLongRunning(true).setWidgetType(Widget.BUTTON_WIDGET_TYPE));
        refreshLayout(wizardForm);

        Form mainForm = new Form(this, Form.MAIN);

        mainForm.addRow(host);
        mainForm.addColumn(port);
        mainForm.addRow(database);
        mainForm.addRow(username);
        mainForm.addColumn(widget(password).setWidgetType(Widget.HIDDEN_TEXT_WIDGET_TYPE));

        mainForm.addRow(widget(authentication).setWidgetType(Widget.ENUMERATION_WIDGET_TYPE));

        Form refForm = Form.create(this, Form.REFERENCE);
        Widget compListWidget = widget(referencedComponent).setWidgetType(Widget.COMPONENT_REFERENCE_WIDGET_TYPE);
        refForm.addRow(compListWidget);
        refForm.addRow(mainForm);
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
        boolean refConnectionUsed = isReferencedConnectionUsed();

        if (form.getName().equals(Form.MAIN)) {
            form.getWidget(host).setHidden(refConnectionUsed);
            form.getWidget(port).setHidden(refConnectionUsed);
            form.getWidget(database).setHidden(refConnectionUsed);
            form.getWidget(username).setHidden(refConnectionUsed);
            form.getWidget(password).setHidden(refConnectionUsed);
            form.getWidget(authentication).setHidden(refConnectionUsed);
        }
    }

    public boolean isReferencedConnectionUsed() {
        String refComponentId = referencedComponent.componentInstanceId.getStringValue();
        return refComponentId != null && refComponentId.startsWith(MarkLogicConnectionDefinition.COMPONENT_NAME);
    }

    public void afterReferencedComponent() {
        refreshLayout(getForm(Form.MAIN));
        refreshLayout(getForm(Form.REFERENCE));
    }

    public String getReferencedComponentId() {
        return referencedComponent.componentInstanceId.getValue();
    }

    @Override
    public MarkLogicConnectionProperties getConnectionProperties() {
        return isReferencedConnectionUsed() ? referencedComponent.getReference() : this;
    }

    public void beforeFormPresentWizardForm() throws Exception {
        setupLayout();
    }

    public ValidationResult afterFormFinishWizardForm(Repository<Properties> repo) throws Exception {
        repo.storeProperties(this, name.getStringValue(), repositoryLocation, null);
        return ValidationResult.OK;
    }

    public ValidationResult validateTestConnection() {
        ValidationResult vr;
        try (SandboxedInstance sandbox = SandboxInstanceFactory.createSandboxedInstance(
                RuntimeInfoProvider.getCommonRuntimeInfo(MarkLogicDatastoreDefinition.DATASTORE_RUNTIME),
                MarkLogicConnectionProperties.class.getClassLoader(), false)) {

            DatastoreRuntime<MarkLogicConnectionProperties> datastoreRuntime = (DatastoreRuntime<MarkLogicConnectionProperties>) sandbox
                    .getInstance();
            datastoreRuntime.initialize(null, this);
            ValidationResultMutable vrm = new ValidationResultMutable(datastoreRuntime.doHealthChecks(null).iterator().next());
            if (vrm.getStatus() == ValidationResult.Result.OK) {
                vrm.setMessage(getI18nMessage("messages.connectionSuccessful"));
                getForm(WIZARD).setAllowFinish(true);
            } else {
                getForm(WIZARD).setAllowFinish(false);
            }
            vr = vrm;
        } catch (Exception e) {
            vr = new ValidationResult(Result.ERROR, e.getMessage());
        }
        return vr;
    }

    public String getRepositoryLocation() {
        return repositoryLocation;
    }

    public MarkLogicConnectionProperties setRepositoryLocation(String location) {
        repositoryLocation = location;
        return this;
    }
}
