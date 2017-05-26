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
package org.talend.components.azurestorage.tazurestorageconnection;

import static org.talend.daikon.properties.presentation.Widget.widget;
import static org.talend.daikon.properties.property.PropertyFactory.newEnum;
import static org.talend.daikon.properties.property.PropertyFactory.newString;

import java.util.EnumSet;
import java.util.List;

import org.talend.components.api.properties.ComponentPropertiesImpl;
import org.talend.components.api.properties.ComponentReferenceProperties;
import org.talend.components.azurestorage.AzureStorageProvideConnectionProperties;
import org.talend.components.azurestorage.blob.runtime.AzureStorageSourceOrSink;
import org.talend.components.azurestorage.queue.runtime.AzureStorageQueueSourceOrSink;
import org.talend.components.azurestorage.table.runtime.AzureStorageTableSourceOrSink;
import org.talend.daikon.NamedThing;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.properties.PresentationItem;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;
import org.talend.daikon.properties.service.Repository;

/**
 * Class TAzureStorageConnectionProperties.
 */
public class TAzureStorageConnectionProperties extends ComponentPropertiesImpl
        implements AzureStorageProvideConnectionProperties {

    private static final long serialVersionUID = 5588521568261191377L;

    // Only for the wizard use
    public Property<String> name = newString("name").setRequired();

    public static final String FORM_WIZARD = "Wizard";

    private String repositoryLocation;

    private static final I18nMessages i18nMessages = GlobalI18N.getI18nMessageProvider()
            .getI18nMessages(TAzureStorageConnectionProperties.class);
    //

    public enum Protocol {
        HTTP,
        HTTPS
    }

    /** accountKey - The Azure Storage Account Key. */
    public Property<String> accountKey = newString("accountKey").setRequired()
            .setFlags(EnumSet.of(Property.Flags.ENCRYPT, Property.Flags.SUPPRESS_LOGGING));

    /** accountName - The Azure Storage Account Name. */
    public Property<String> accountName = PropertyFactory.newString("accountName"); //$NON-NLS-1$

    public Property<Boolean> useSharedAccessSignature = PropertyFactory.newBoolean("useSharedAccessSignature");

    public Property<String> sharedAccessSignature = PropertyFactory.newString("sharedAccessSignature");//$NON-NLS-1$

    public Property<Protocol> protocol = newEnum("protocol", Protocol.class).setRequired(); //$NON-NLS-1$

    public ComponentReferenceProperties<TAzureStorageConnectionProperties> referencedComponent = new ComponentReferenceProperties<>(
            "referencedComponent", TAzureStorageConnectionDefinition.COMPONENT_NAME);

    public PresentationItem testConnection = new PresentationItem("testConnection", "Test connection");

    public List<NamedThing> BlobSchema = null, QueueSchema = null, TableSchema = null;

    public TAzureStorageConnectionProperties(String name) {
        super(name);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
        protocol.setValue(Protocol.HTTPS);
        accountName.setValue("");
        accountKey.setValue("");
        useSharedAccessSignature.setValue(false);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addRow(accountName);
        mainForm.addRow(widget(accountKey).setWidgetType(Widget.HIDDEN_TEXT_WIDGET_TYPE));
        mainForm.addRow(protocol);
        mainForm.addRow(useSharedAccessSignature);
        mainForm.addRow(sharedAccessSignature);

        Form refForm = Form.create(this, Form.REFERENCE);
        Widget compListWidget = widget(referencedComponent).setWidgetType(Widget.COMPONENT_REFERENCE_WIDGET_TYPE);
        // referencedComponent.referenceType.setValue(TAzureStorageConnectionDefinition.COMPONENT_NAME);
        refForm.addRow(compListWidget);
        refForm.addRow(mainForm);

        Form wizardForm = Form.create(this, FORM_WIZARD);
        wizardForm.addRow(name);
        wizardForm.addRow(accountName);
        wizardForm.addRow(widget(accountKey).setWidgetType(Widget.HIDDEN_TEXT_WIDGET_TYPE));
        wizardForm.addRow(protocol);
        wizardForm.addRow(useSharedAccessSignature);
        wizardForm.addRow(sharedAccessSignature);
        wizardForm.addColumn(widget(testConnection).setLongRunning(true).setWidgetType(Widget.BUTTON_WIDGET_TYPE));

    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);

        String refComponentIdValue = getReferencedComponentId();
        boolean useOtherConnection = refComponentIdValue != null
                && refComponentIdValue.startsWith(TAzureStorageConnectionDefinition.COMPONENT_NAME);
        if (form.getName().equals(Form.MAIN) || form.getName().equals(FORM_WIZARD)) {
            form.getWidget(accountName).setHidden(useOtherConnection);
            form.getWidget(accountKey).setHidden(useOtherConnection);
            form.getWidget(protocol).setHidden(useOtherConnection);
            form.getWidget(useSharedAccessSignature.getName()).setHidden(useOtherConnection);
            form.getWidget(sharedAccessSignature.getName()).setHidden(useOtherConnection);
            boolean useSAS = useSharedAccessSignature.getValue();
            if (!useOtherConnection) {
                form.getWidget(accountName).setHidden(useSAS);
                form.getWidget(accountKey).setHidden(useSAS);
                form.getWidget(protocol).setHidden(useSAS);
                form.getWidget(sharedAccessSignature.getName()).setHidden(!useSAS);
            }
        }
    }

    public void afterReferencedComponent() {
        refreshLayout(getForm(Form.MAIN));
        refreshLayout(getForm(Form.REFERENCE));
    }

    public void afterUseSharedAccessSignature() {
        refreshLayout(getForm(Form.MAIN));
        refreshLayout(getForm(Form.REFERENCE));
        refreshLayout(getForm(FORM_WIZARD));
    }

    public ValidationResult validateTestConnection() throws Exception {
        try {
            
            ValidationResult vr = AzureStorageSourceOrSink.validateConnection(this);
            if (ValidationResult.Result.OK != vr.getStatus()) {
                return vr;
            }

            if (useSharedAccessSignature.getValue()) {
                String[] SAS = sharedAccessSignature.getValue().split("&");
                boolean allowedBlob = true, allowedQueue = true, allowedtable = true;
                for (String string : SAS) {
                    if (string.startsWith("ss=")) {
                        allowedBlob = string.contains("b") ? true : false;
                        allowedQueue = string.contains("q") ? true : false;
                        allowedtable = string.contains("t") ? true : false;
                        break;
                    }
                }
                if (allowedBlob) {
                    BlobSchema = AzureStorageSourceOrSink.getSchemaNames(null, this);
                }
                if (allowedQueue) {
                    QueueSchema = AzureStorageQueueSourceOrSink.getSchemaNames(null, this);
                }
                if (allowedtable) {
                    TableSchema = AzureStorageTableSourceOrSink.getSchemaNames(null, this);
                }
            } else {
                BlobSchema = AzureStorageSourceOrSink.getSchemaNames(null, this);
                QueueSchema = AzureStorageQueueSourceOrSink.getSchemaNames(null, this);
                TableSchema = AzureStorageTableSourceOrSink.getSchemaNames(null, this);
            }
        } catch (Exception e) {
            String errorMessage = e.getLocalizedMessage() + '\n';
            Throwable throwable = e.getCause();
            while (throwable != null) {
                errorMessage += throwable.getLocalizedMessage() + '\n';
                throwable = throwable.getCause();
            }

            getForm(FORM_WIZARD).setAllowForward(false);
            return new ValidationResult(Result.ERROR, errorMessage);
        }

        getForm(FORM_WIZARD).setAllowForward(true);
        getForm(FORM_WIZARD).setAllowFinish(true);
        return new ValidationResult(Result.OK, i18nMessages.getMessage("message.success"));
    }

    public ValidationResult afterFormFinishWizard(Repository<Properties> repo) throws Exception {
        ValidationResult vr = AzureStorageSourceOrSink.validateConnection(this);
        if (vr.getStatus() != ValidationResult.Result.OK) {
            return vr;
        }

        return ValidationResult.OK;
    }

    @Override
    public TAzureStorageConnectionProperties getConnectionProperties() {
        return this;
    }

    public String getReferencedComponentId() {
        return referencedComponent.componentInstanceId.getValue();
    }

    public TAzureStorageConnectionProperties getReferencedConnectionProperties() {
        TAzureStorageConnectionProperties refProps = referencedComponent.getReference();
        if (refProps != null) {
            return refProps;
        }
        return null;
    }

    public TAzureStorageConnectionProperties setRepositoryLocation(String repositoryLocation) {
        this.repositoryLocation = repositoryLocation;
        return this;
    }
}
