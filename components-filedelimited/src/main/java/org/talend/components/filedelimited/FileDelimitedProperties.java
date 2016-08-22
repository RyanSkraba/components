package org.talend.components.filedelimited;

import java.util.Collections;
import java.util.Set;

import org.talend.components.api.component.Connector;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.common.EncodingTypeProperties;
import org.talend.components.common.FixedConnectorsComponentProperties;
import org.talend.components.common.SchemaProperties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

import static org.talend.daikon.properties.presentation.Widget.widget;
import static org.talend.daikon.properties.property.PropertyFactory.newEnum;

public class FileDelimitedProperties extends FixedConnectorsComponentProperties {

    public Property<String> fileName = PropertyFactory.newString("fileName");

    public SchemaProperties schema = new SchemaProperties("schema");

    public Property<Boolean> csvOptions = PropertyFactory.newBoolean("csvOptions");

    public enum CSVRowSeparator {
        LF,
        CR,
        CRLF
    }

    public Property<CSVRowSeparator> csvRowSeparator = newEnum("csvRowSeparator", CSVRowSeparator.class);

    public Property<String> rowSeparator = PropertyFactory.newString("rowSeparator");

    public Property<String> fieldSeparator = PropertyFactory.newString("fieldSeparator");

    public Property<String> escapeChar = PropertyFactory.newString("escapeChar");

    public Property<String> textEnclosure = PropertyFactory.newString("textEnclosure");

    public Property<Boolean> advancedSeparator = PropertyFactory.newBoolean("advancedSeparator");

    public Property<String> thousandsSeparator = PropertyFactory.newString("thousandsSeparator");

    public Property<String> decimalSeparator = PropertyFactory.newString("decimalSeparator");

    public EncodingTypeProperties encodingType = new EncodingTypeProperties("encodingType");

    protected transient PropertyPathConnector MAIN_CONNECTOR = new PropertyPathConnector(Connector.MAIN_NAME, "schema");

    public FileDelimitedProperties(String name) {
        super(name);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
        rowSeparator.setValue("\\n");
        fieldSeparator.setValue(";");
        escapeChar.setValue("\"\"");
        textEnclosure.setValue("\"\"");
        thousandsSeparator.setValue(",");
        decimalSeparator.setValue(".");
        encodingType.encodingType.setPossibleValues(encodingType.getDefaultEncodings());
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = Form.create(this, Form.MAIN);
        mainForm.addRow(schema.getForm(Form.REFERENCE));
        mainForm.addRow(widget(fileName).setWidgetType(Widget.FILE_WIDGET_TYPE));

        Form advancedForm = Form.create(this, Form.ADVANCED);
        advancedForm.addRow(advancedSeparator);
        advancedForm.addRow(thousandsSeparator);
        advancedForm.addColumn(decimalSeparator);
        advancedForm.addRow(encodingType.getForm(Form.MAIN));
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
        if (form.getName().equals(Form.MAIN)) {
            form.getWidget(rowSeparator.getName()).setHidden(csvOptions.getValue());
            form.getWidget(csvRowSeparator.getName()).setHidden(!csvOptions.getValue());
            form.getWidget(escapeChar.getName()).setHidden(!csvOptions.getValue());
            form.getWidget(textEnclosure.getName()).setHidden(!csvOptions.getValue());
        }
        if (form.getName().equals(Form.ADVANCED)) {
            form.getWidget(thousandsSeparator.getName()).setHidden(!advancedSeparator.getValue());
            form.getWidget(decimalSeparator.getName()).setHidden(!advancedSeparator.getValue());
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
    }

    public void afterAdvancedSeparator() {
        refreshLayout(getForm(Form.ADVANCED));
    }

}
