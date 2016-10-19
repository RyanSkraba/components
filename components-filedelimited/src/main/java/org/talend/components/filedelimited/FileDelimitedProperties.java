package org.talend.components.filedelimited;

import static org.talend.components.common.EncodingTypeProperties.ENCODING_TYPE_CUSTOM;
import static org.talend.components.common.EncodingTypeProperties.ENCODING_TYPE_ISO_8859_15;
import static org.talend.components.common.EncodingTypeProperties.ENCODING_TYPE_UTF_8;
import static org.talend.daikon.properties.presentation.Widget.widget;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.apache.avro.Schema;
import org.apache.commons.lang3.StringUtils;
import org.talend.components.api.component.Connector;
import org.talend.components.api.component.ISchemaListener;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.common.EncodingTypeProperties;
import org.talend.components.common.FixedConnectorsComponentProperties;
import org.talend.components.common.SchemaProperties;
import org.talend.components.filedelimited.runtime.FileDelimitedSource;
import org.talend.daikon.properties.PresentationItem;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;
import org.talend.daikon.properties.service.Repository;

public class FileDelimitedProperties extends FixedConnectorsComponentProperties {

    public static final String FORM_WIZARD = "Wizard";

    public Property<Object> fileName = PropertyFactory.newProperty(Object.class, "fileName").setRequired();

    public ISchemaListener schemaListener;

    public SchemaProperties main = new SchemaProperties("main") {

        public void afterSchema() {
            if (schemaListener != null) {
                schemaListener.afterSchema();
            }
        }
    };

    public Property<Boolean> csvOptions = PropertyFactory.newBoolean("csvOptions");

    public Property<String> rowSeparator = PropertyFactory.newString("rowSeparator").setRequired();

    public Property<String> fieldSeparator = PropertyFactory.newString("fieldSeparator").setRequired();

    public Property<String> escapeChar = PropertyFactory.newString("escapeChar").setRequired();

    public Property<String> textEnclosure = PropertyFactory.newString("textEnclosure").setRequired();

    public Property<Boolean> advancedSeparator = PropertyFactory.newBoolean("advancedSeparator");

    public Property<String> thousandsSeparator = PropertyFactory.newString("thousandsSeparator").setRequired();

    public Property<String> decimalSeparator = PropertyFactory.newString("decimalSeparator").setRequired();

    public EncodingTypeProperties encoding = new EncodingTypeProperties("encoding");

    protected transient PropertyPathConnector MAIN_CONNECTOR = new PropertyPathConnector(Connector.MAIN_NAME, "main");

    // For wizard

    public Property<Integer> header = PropertyFactory.newInteger("header");

    public Property<Integer> footer = PropertyFactory.newInteger("footer");

    public Property<Integer> limit = PropertyFactory.newInteger("limit");

    public Property<Boolean> removeEmptyRow = PropertyFactory.newBoolean("removeEmptyRow");

    private String repositoryLocation;

    public Property<String> name = PropertyFactory.newString("name").setRequired();

    public PresentationItem preview = new PresentationItem("preview", "Preview");

    public Property<String> previewTable = PropertyFactory.newString("previewTable");

    public FileDelimitedProperties(String name) {
        super(name);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
        rowSeparator.setValue("\\n");
        fieldSeparator.setValue(";");
        escapeChar.setValue("\\\"");
        textEnclosure.setValue("\\\"");
        thousandsSeparator.setValue(",");
        decimalSeparator.setValue(".");
        header.setValue(0);
        footer.setValue(0);
        removeEmptyRow.setValue(true);
        encoding.encodingType.setPossibleValues(Arrays.asList(ENCODING_TYPE_UTF_8, "UTF-16", "UTF-16LE", "UTF-16BE", "UTF-7",
                "ISO-8859-1", "ISO-8859-2", "ISO-8859-3", "ISO-8859-4", "ISO-8859-5", "ISO-8859-6", "ISO-8859-7", "ISO-8859-8",
                "ISO-8859-9", "ISO-8859-10", ENCODING_TYPE_ISO_8859_15, "windows-1252", "BIG5", "GB18030", "GB2312", "EUC_CN",
                ENCODING_TYPE_CUSTOM));
        encoding.encodingType.setValue(ENCODING_TYPE_UTF_8);

    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = Form.create(this, Form.MAIN);
        mainForm.addRow(main.getForm(Form.REFERENCE));
        mainForm.addRow(widget(fileName).setWidgetType(Widget.FILE_WIDGET_TYPE));

        Form advancedForm = Form.create(this, Form.ADVANCED);
        advancedForm.addRow(advancedSeparator);
        advancedForm.addRow(thousandsSeparator);
        advancedForm.addColumn(decimalSeparator);
        advancedForm.addRow(encoding.getForm(Form.MAIN));

        Form wizardForm = Form.create(this, FORM_WIZARD);
        wizardForm.addRow(name);
        wizardForm.addRow(widget(fileName).setWidgetType(Widget.FILE_WIDGET_TYPE));
        wizardForm.addRow(encoding.getForm(Form.MAIN));
        wizardForm.addRow(rowSeparator);
        wizardForm.addColumn(fieldSeparator);
        wizardForm.addRow(csvOptions);
        wizardForm.addRow(escapeChar);
        wizardForm.addColumn(textEnclosure);
        wizardForm.addRow(header);
        wizardForm.addColumn(footer);
        wizardForm.addRow(removeEmptyRow);
        wizardForm.addRow(limit);
        wizardForm.addRow(widget(preview).setLongRunning(true).setWidgetType(Widget.BUTTON_WIDGET_TYPE));
        // TODO need add "Schema" to edit schema
        // wizardForm.addColumn(main.getForm(Form.REFERENCE));
        wizardForm.addRow(widget(previewTable).setWidgetType(Widget.JSON_TABLE_WIDGET_TYPE));
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
        if (form != null) {
            if (Form.MAIN.equals(form.getName()) || FORM_WIZARD.equals(form.getName())) {
                if (form.getWidget(csvOptions.getName()) != null) {
                    form.getWidget(escapeChar.getName()).setHidden(!csvOptions.getValue());
                    form.getWidget(textEnclosure.getName()).setHidden(!csvOptions.getValue());
                }
            }
            if (form.getName().equals(Form.ADVANCED)) {
                if (form.getWidget(advancedSeparator.getName()) != null) {
                    form.getWidget(thousandsSeparator.getName()).setHidden(!advancedSeparator.getValue());
                    form.getWidget(decimalSeparator.getName()).setHidden(!advancedSeparator.getValue());
                }
            }
        }
    }

    @Override
    protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputComponent) {
        if (isOutputComponent) {
            return Collections.singleton(MAIN_CONNECTOR);
        }
        return Collections.emptySet();
    }

    public void afterCsvOptions() {
        refreshLayout(getForm(Form.MAIN));
        refreshLayout(getForm(Form.ADVANCED));
        if (getForm(FORM_WIZARD) != null) {
            refreshLayout(getForm(FORM_WIZARD));
        }
    }

    public void afterAdvancedSeparator() {
        refreshLayout(getForm(Form.ADVANCED));
    }

    public void setSchemaListener(ISchemaListener schemaListener) {
        this.schemaListener = schemaListener;
    }

    public ValidationResult validateName() throws Exception {
        if (name.getValue() != null) {
            getForm(FORM_WIZARD).setAllowFinish(true);
        } else {
            getForm(FORM_WIZARD).setAllowFinish(false);
        }
        return ValidationResult.OK;
    }

    public ValidationResult afterFormFinishWizard(Repository<Properties> repo) throws Exception {
        // Clear the preview data when form is finished
        previewTable.setValue("{\"data\":[]}");
        String connRepLocation = repo.storeProperties(this, this.getName(), repositoryLocation, null);
        repo.storeProperties(this, this.name.getValue(), connRepLocation, "main.schema");
        return ValidationResult.OK;
    }

    public FileDelimitedProperties setRepositoryLocation(String location) {
        repositoryLocation = location;
        return this;
    }

    public ValidationResult validatePreview() {
        ValidationResult vr = new ValidationResult();
        try {
            if (!StringUtils.isEmpty(fileName.getStringValue())) {
                File file = new File(fileName.getStringValue());
                if (!file.exists() || !file.isFile()) {
                    throw new IOException("File \"" + fileName.getStringValue() + "\" is not found.");
                }
                Map<String, Schema> dataAndSchema = FileDelimitedSource.previewData(null, this, 200);
                for (String jsonData : dataAndSchema.keySet()) {
                    Schema guessedSchema = dataAndSchema.get(jsonData);
                    previewTable.setValue(jsonData);
                    if (guessedSchema.getFields().size() > 0) {
                        main.schema.setValue(guessedSchema);
                    } else {
                        main.schema.setValue(SchemaProperties.EMPTY_SCHEMA);
                    }
                }
                if (main.schema.getValue() != null && main.schema.getValue().getFields().size() > 0) {
                    getForm(FORM_WIZARD).setAllowFinish(true);
                } else {
                    getForm(FORM_WIZARD).setAllowFinish(false);
                }
            }
        } catch (Exception e) {
            // Reset preview table when get exception
            previewTable.setValue("{\"data\":[]}");
            String errorMessage = e.getMessage();
            if (e instanceof UnsupportedEncodingException) {
                errorMessage = "java.io.UnsupportedEncodingException: " + errorMessage;
            }
            vr.setMessage(errorMessage);
            vr.setStatus(ValidationResult.Result.ERROR);
            getForm(FORM_WIZARD).setAllowFinish(false);
        }
        return vr;
    }

    public void afterPreview() {
        refreshLayout(getForm(FORM_WIZARD));
    }

}
