package org.talend.components.filedelimited.tFileInputDelimited;

import java.util.Collections;
import java.util.Set;

import org.talend.components.api.component.Connector;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.filedelimited.FileDelimitedProperties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

public class TFileInputDelimitedProperties extends FileDelimitedProperties {

    public TFileInputDelimitedProperties(String name) {
        super(name);
    }

    public Property<Integer> header = PropertyFactory.newInteger("header");

    public Property<Integer> footer = PropertyFactory.newInteger("footer");

    public Property<Integer> limit = PropertyFactory.newInteger("limit");

    public Property<Boolean> removeEmptyRow = PropertyFactory.newBoolean("removeEmptyRow");

    public Property<Boolean> uncompress = PropertyFactory.newBoolean("uncompress");

    public Property<Boolean> dieOnError = PropertyFactory.newBoolean("dieOnError");

    // Advanced
    public Property<Boolean> random = PropertyFactory.newBoolean("random");

    public Property<Integer> nbRandom = PropertyFactory.newInteger("nbRandom");

    public Property<Boolean> trimall = PropertyFactory.newBoolean("trimall");

    // TODO add table "TRIMSELECT"

    public Property<Boolean> checkFieldsNum = PropertyFactory.newBoolean("checkFieldsNum");

    public Property<Boolean> checkDate = PropertyFactory.newBoolean("checkDate");

    public Property<Boolean> splitRecord = PropertyFactory.newBoolean("splitRecord");

    public Property<Boolean> enableDecode = PropertyFactory.newBoolean("enableDecode");

    // TODO add table "DECODE_COLS"

    protected transient PropertyPathConnector REJECT_CONNECTOR = new PropertyPathConnector(Connector.REJECT_NAME, "schemaReject");

    @Override
    public void setupProperties() {
        super.setupProperties();
        header.setValue(0);
        footer.setValue(0);
        nbRandom.setValue(10);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = getForm(Form.MAIN);
        mainForm.addRow(csvOptions);
        mainForm.addRow(rowSeparator);
        mainForm.addRow(csvRowSeparator);
        mainForm.addColumn(fieldSeparator);
        mainForm.addRow(escapeChar);
        mainForm.addColumn(textEnclosure);
        mainForm.addRow(header);
        mainForm.addColumn(footer);
        mainForm.addRow(limit);
        mainForm.addRow(removeEmptyRow);
        mainForm.addRow(uncompress);
        mainForm.addRow(dieOnError);

        Form advancedForm = getForm(Form.ADVANCED);

        advancedForm.addRow(random);
        advancedForm.addColumn(nbRandom);
        advancedForm.addRow(trimall);
        // TODO add table "TRIMSELECT"
        advancedForm.addRow(checkFieldsNum);
        advancedForm.addRow(checkDate);
        advancedForm.addRow(splitRecord);
        advancedForm.addRow(enableDecode);
        // TODO add table "DECODE_COLS"

    }

    public void afterUncompress() {
        refreshLayout(getForm(Form.MAIN));
        refreshLayout(getForm(Form.ADVANCED));
    }

    public void afterRandom() {
        refreshLayout(getForm(Form.ADVANCED));
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
        if (form.getName().equals(Form.MAIN)) {
            form.getWidget(footer.getName()).setHidden(uncompress.getValue());
        }
        if (form.getName().equals(Form.ADVANCED)) {
            form.getWidget(random.getName()).setHidden(csvOptions.getValue() || uncompress.getValue());
            form.getWidget(nbRandom.getName()).setHidden(csvOptions.getValue() || uncompress.getValue() || !random.getValue());
            // TODO add table "TRIMSELECT"
            form.getWidget(splitRecord.getName()).setHidden(csvOptions.getValue());
            // TODO add table "DECODE_COLS"
        }
    }

    @Override
    protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputConnection) {
        if (isOutputConnection) {
            return Collections.singleton(MAIN_CONNECTOR);
        } else {
            return Collections.EMPTY_SET;
        }
    }
}
