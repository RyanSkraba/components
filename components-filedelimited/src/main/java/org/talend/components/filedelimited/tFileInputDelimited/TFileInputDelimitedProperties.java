package org.talend.components.filedelimited.tFileInputDelimited;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.avro.Schema;
import org.talend.components.api.component.Connector;
import org.talend.components.api.component.ISchemaListener;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.common.ValuesTrimPropertis;
import org.talend.components.filedelimited.DecodeTable;
import org.talend.components.filedelimited.FileDelimitedProperties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

import static org.talend.daikon.properties.presentation.Widget.widget;

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

    public ValuesTrimPropertis trimColumns = new ValuesTrimPropertis("trimColumns");

    public Property<Boolean> checkFieldsNum = PropertyFactory.newBoolean("checkFieldsNum");

    public Property<Boolean> checkDate = PropertyFactory.newBoolean("checkDate");

    public Property<Boolean> splitRecord = PropertyFactory.newBoolean("splitRecord");

    public Property<Boolean> enableDecode = PropertyFactory.newBoolean("enableDecode");

    public DecodeTable decodeTable = new DecodeTable("decodeTable");

    protected transient PropertyPathConnector REJECT_CONNECTOR = new PropertyPathConnector(Connector.REJECT_NAME, "schemaReject");

    @Override
    public void setupProperties() {
        super.setupProperties();
        header.setValue(0);
        footer.setValue(0);
        nbRandom.setValue(10);
        removeEmptyRow.setValue(true);
        setSchemaListener(new ISchemaListener() {

            @Override
            public void afterSchema() {
                List<String> fieldsName = getFieldNames(main.schema);
                trimColumns.setFieldNames(fieldsName);
                trimColumns.beforeTrimTable();
                beforeDecodeTable();
            }
        });
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = getForm(Form.MAIN);
        mainForm.addRow(csvOptions);
        mainForm.addRow(rowSeparator);
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
        advancedForm.addRow(trimColumns.getForm(Form.MAIN));
        advancedForm.addRow(checkFieldsNum);
        advancedForm.addRow(checkDate);
        advancedForm.addRow(splitRecord);
        advancedForm.addRow(enableDecode);
        advancedForm.addRow(widget(decodeTable).setWidgetType(Widget.TABLE_WIDGET_TYPE));

    }

    public void afterUncompress() {
        refreshLayout(getForm(Form.MAIN));
        refreshLayout(getForm(Form.ADVANCED));
    }

    public void afterRandom() {
        refreshLayout(getForm(Form.ADVANCED));
    }

    public void afterEnableDecode() {
        refreshLayout(getForm(Form.ADVANCED));
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
        if (form != null) {
            if (form.getName().equals(Form.MAIN)) {
                form.getWidget(footer.getName()).setHidden(uncompress.getValue());
            }
            if (form.getName().equals(Form.ADVANCED)) {
                form.getWidget(random.getName()).setHidden(csvOptions.getValue() || uncompress.getValue());
                form.getWidget(nbRandom.getName())
                        .setHidden(csvOptions.getValue() || uncompress.getValue() || !random.getValue());
                // TODO add table "TRIMSELECT"
                form.getWidget(splitRecord.getName()).setHidden(csvOptions.getValue());
                form.getWidget(decodeTable.getName()).setHidden(!enableDecode.getValue());
            }
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

    protected List<String> getFieldNames(Property schema) {
        Schema s = (Schema) schema.getValue();
        List<String> fieldNames = new ArrayList<>();
        for (Schema.Field f : s.getFields()) {
            fieldNames.add(f.name());
        }
        return fieldNames;
    }

    public void beforeDecodeTable() {
        List<String> fieldNames = trimColumns.getFieldNames();
        if (fieldNames != null && fieldNames.size() > 0) {
            decodeTable.columnName.setValue(fieldNames);
            List<Boolean> decodeValueList = new ArrayList<>();
            for (int i = fieldNames.size(); i > 0; i--) {
                decodeValueList.add(Boolean.FALSE);
            }
            decodeTable.decode.setValue(decodeValueList);
        }
    }
}
