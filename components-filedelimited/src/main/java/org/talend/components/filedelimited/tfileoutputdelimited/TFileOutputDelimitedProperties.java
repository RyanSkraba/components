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
package org.talend.components.filedelimited.tfileoutputdelimited;

import java.util.Collections;
import java.util.Set;

import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.common.EncodingTypeProperties;
import org.talend.components.filedelimited.FileDelimitedProperties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

public class TFileOutputDelimitedProperties extends FileDelimitedProperties {

    public TFileOutputDelimitedProperties(String name) {
        super(name);
    }

    public Property<Boolean> targetIsStream = PropertyFactory.newBoolean("targetIsStream");

    public Property<Boolean> useOsRowSeparator = PropertyFactory.newBoolean("useOsRowSeparator");

    public Property<Boolean> append = PropertyFactory.newBoolean("append");

    public Property<Boolean> includeHeader = PropertyFactory.newBoolean("includeHeader");

    public Property<Boolean> compress = PropertyFactory.newBoolean("compress");

    // Advanced

    public Property<Boolean> creatDirIfNotExist = PropertyFactory.newBoolean("creatDirIfNotExist");

    public Property<Boolean> split = PropertyFactory.newBoolean("split");

    public Property<Integer> splitEvery = PropertyFactory.newInteger("splitEvery");

    public Property<Boolean> flushOnRow = PropertyFactory.newBoolean("flushOnRow");

    public Property<Integer> flushOnRowNum = PropertyFactory.newInteger("flushOnRowNum");

    public Property<Boolean> rowMode = PropertyFactory.newBoolean("rowMode");

    public Property<Boolean> deleteEmptyFile = PropertyFactory.newBoolean("deleteEmptyFile");

    @Override
    public void setupProperties() {
        super.setupProperties();
        useOsRowSeparator.setValue(true);
        creatDirIfNotExist.setValue(true);
        splitEvery.setValue(1000);
        flushOnRowNum.setValue(1);
        encoding.encodingType.setPossibleValues(encoding.getDefaultEncodings());
        encoding.encodingType.setValue(EncodingTypeProperties.ENCODING_TYPE_ISO_8859_15);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = getForm(Form.MAIN);
        mainForm.addRow(targetIsStream);
        mainForm.addRow(csvOptions);
        mainForm.addRow(useOsRowSeparator);
        mainForm.addRow(rowSeparator);
        mainForm.addColumn(fieldSeparator);
        mainForm.addRow(escapeChar);
        mainForm.addColumn(textEnclosure);
        mainForm.addRow(append);
        mainForm.addRow(includeHeader);
        mainForm.addRow(compress);

        Form advancedForm = getForm(Form.ADVANCED);
        advancedForm.addRow(creatDirIfNotExist);
        advancedForm.addRow(split);
        advancedForm.addColumn(splitEvery);
        advancedForm.addRow(flushOnRow);
        advancedForm.addColumn(flushOnRowNum);
        advancedForm.addRow(rowMode);
        advancedForm.addRow(deleteEmptyFile);
    }

    public void afterCompress() {
        refreshLayout(getForm(Form.MAIN));
    }

    public void afterSplit() {
        refreshLayout(getForm(Form.MAIN));
        refreshLayout(getForm(Form.ADVANCED));
    }

    public void afterAppend() {
        refreshLayout(getForm(Form.MAIN));
    }

    public void afterTargetIsStream() {
        refreshLayout(getForm(Form.ADVANCED));
    }

    public void afterFlushOnRow() {
        refreshLayout(getForm(Form.ADVANCED));
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
        if (form != null) {
            if (form.getName().equals(Form.MAIN)) {
                form.getWidget(useOsRowSeparator.getName()).setHidden(!csvOptions.getValue());
                form.getWidget(compress.getName()).setHidden(append.getValue() || split.getValue());
            }
            if (form.getName().equals(Form.ADVANCED)) {
                form.getWidget(creatDirIfNotExist.getName()).setHidden(targetIsStream.getValue());
                form.getWidget(split.getName()).setHidden(targetIsStream.getValue());
                form.getWidget(splitEvery.getName()).setHidden(targetIsStream.getValue() || !split.getValue());
                form.getWidget(flushOnRowNum.getName()).setHidden(!flushOnRow.getValue());
                form.getWidget(deleteEmptyFile.getName()).setHidden(targetIsStream.getValue());
            }
        }
    }

    @Override
    protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputConnection) {
        return Collections.singleton(MAIN_CONNECTOR);
    }

}
