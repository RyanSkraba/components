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
package org.talend.components.filedelimited;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.IndexedRecord;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ErrorCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.container.DefaultComponentRuntimeContainerImpl;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.exception.DataRejectException;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.service.common.DefinitionRegistry;
import org.talend.components.api.service.common.ComponentServiceImpl;
import org.talend.components.api.test.AbstractComponentTest;
import org.talend.components.api.test.ComponentTestUtils;
import org.talend.components.common.EncodingTypeProperties;
import org.talend.components.filedelimited.runtime.FileDelimitedSink;
import org.talend.components.filedelimited.runtime.FileDelimitedSource;
import org.talend.components.filedelimited.runtime.FileDelimitedWriteOperation;
import org.talend.components.filedelimited.runtime.FileDelimitedWriter;
import org.talend.components.filedelimited.tfileinputdelimited.TFileInputDelimitedDefinition;
import org.talend.components.filedelimited.tfileinputdelimited.TFileInputDelimitedProperties;
import org.talend.components.filedelimited.tfileoutputdelimited.TFileOutputDelimitedDefinition;
import org.talend.components.filedelimited.tfileoutputdelimited.TFileOutputDelimitedProperties;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.presentation.Form;

@SuppressWarnings("nls")
public class FileDelimitedTestBasic extends AbstractComponentTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileDelimitedTestBasic.class);

    protected RuntimeContainer adaptor;

    public static Schema BASIC_SCHEMA = getBasicSchema("yyyy-MM-dd'T'HH:mm:ss");

    public static Schema BASIC_OUTPUT_SCHEMA = getBasicSchema("yyyy-MM-dd'T'HH:mm:ss.SSS");

    public static Schema BASIC_DYNAMIC_SCHEMA = new org.apache.avro.Schema.Parser().parse(
            "{\"type\":\"record\",\"name\":\"MAIN\",\"fields\":[],\"di.table.name\":\"MAIN\",\"di.table.label\":\"MAIN\",\"di.dynamic.column.comment\":\"\",\"di.dynamic.column.name\":\"test_dynamic\",\"di.column.talendType\":\"id_Dynamic\",\"talend.field.pattern\":\"yyyy-MM-dd'T'HH:mm:ss\",\"di.column.isNullable\":\"true\",\"talend.field.scale\":\"0\",\"talend.field.dbColumnName\":\"test_dynamic\",\"di.column.relatedEntity\":\"\",\"di.column.relationshipType\":\"\",\"di.dynamic.column.position\":\"0\",\"include-all-fields\":\"true\"}");

    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    private ComponentServiceImpl componentService;

    public FileDelimitedTestBasic() {
        adaptor = new DefaultComponentRuntimeContainerImpl();
    }

    @Before
    public void initializeComponentRegistryAndService() {
        // reset the component service
        componentService = null;
    }

    @Override
    public ComponentService getComponentService() {
        if (componentService == null) {
            DefinitionRegistry testComponentRegistry = new DefinitionRegistry();

            testComponentRegistry.registerComponentFamilyDefinition(new FileDelimitedFamilyDefinition());
            componentService = new ComponentServiceImpl(testComponentRegistry);
        }
        return componentService;
    }

    protected ComponentProperties checkAndAfter(Form form, String propName, ComponentProperties props) throws Throwable {
        assertTrue(form.getWidget(propName).isCallAfter());
        ComponentProperties afterProperty = (ComponentProperties) getComponentService().afterProperty(propName, props);
        assertEquals(
                "ComponentProperties after failed[" + props.getClass().getCanonicalName() + "/after"
                        + StringUtils.capitalize(propName) + "] :" + afterProperty.getValidationResult().getMessage(),
                ValidationResult.Result.OK, afterProperty.getValidationResult().getStatus());
        return afterProperty;
    }

    static public FileDelimitedProperties setupProps(FileDelimitedProperties props) {
        if (props == null) {
            props = (FileDelimitedProperties) new FileDelimitedProperties("foo").init();
        }
        return props;
    }

    protected List<IndexedRecord> readRows(FileDelimitedProperties inputProps) throws IOException {
        FileDelimitedSource source = new FileDelimitedSource();
        source.initialize(null, inputProps);
        source.validate(null);
        BoundedReader<IndexedRecord> reader = source.createReader(null);
        boolean hasRecord = reader.start();
        List<IndexedRecord> rows = new ArrayList<>();
        while (hasRecord) {
            org.apache.avro.generic.IndexedRecord unenforced = reader.getCurrent();
            rows.add(unenforced);
            hasRecord = reader.advance();
        }
        reader.close();
        return rows;
    }

    protected Date parseToDate(String pattern, String strDate) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.parse(strDate);
    }

    protected TFileInputDelimitedProperties createInputProperties(Object file, boolean isCsvMode) {
        TFileInputDelimitedProperties properties = (TFileInputDelimitedProperties) new TFileInputDelimitedDefinition()
                .createProperties().init();
        properties.fileName.setValue(file);
        properties.rowSeparator.setValue("\n");
        if (isCsvMode) {
            properties.csvOptions.setValue(true);
            properties.escapeChar.setValue("\"");
            properties.textEnclosure.setValue("\"");
        }
        properties.header.setValue(1);
        properties.main.schema.setValue(BASIC_SCHEMA);
        ComponentTestUtils.checkSerialize(properties, errorCollector);
        return properties;
    }

    protected TFileOutputDelimitedProperties createOutputProperties(Object file, boolean isCsvMode) {
        TFileOutputDelimitedProperties properties = (TFileOutputDelimitedProperties) new TFileOutputDelimitedDefinition()
                .createProperties().init();
        properties.fileName.setValue(file);
        properties.rowSeparator.setValue("\n");
        if (isCsvMode) {
            properties.csvOptions.setValue(true);
            properties.escapeChar.setValue("\"");
            properties.textEnclosure.setValue("\"");
        }
        properties.main.schema.setValue(BASIC_OUTPUT_SCHEMA);
        ComponentTestUtils.checkSerialize(properties, errorCollector);
        return properties;
    }

    protected FileDelimitedProperties createWizaredProperties(TFileInputDelimitedProperties properties) {
        FileDelimitedProperties wizardProperties = new FileDelimitedProperties("wizard");
        wizardProperties.init();
        wizardProperties.copyValuesFrom(properties);
        return wizardProperties;
    }

    // Return the records are not rejected
    protected List<IndexedRecord> printLogRecords(List<IndexedRecord> records) {
        List<IndexedRecord> successRecords = new ArrayList<>();
        if (records != null) {
            StringBuffer sb = new StringBuffer();
            for (int index = 0; index < records.size(); index++) {
                try {
                    IndexedRecord record = records.get(index);
                    assertNotNull(record.getSchema());
                    int columnSize = record.getSchema().getFields().size();
                    for (int i = 0; i < columnSize; i++) {
                        sb.append(record.get(i));
                        if (i != columnSize - 1) {
                            sb.append(" - ");
                        }
                    }

                    LOGGER.debug("Row " + (index + 1) + " :" + sb.toString());
                    successRecords.add(record);
                } catch (DataRejectException dre) {
                    LOGGER.debug("Row " + (index + 1) + " :" + dre.getRejectInfo().get("errorMessage"));
                }
                sb.delete(0, sb.length());
            }
        } else {
            LOGGER.debug("Records list is empty!");
        }
        return successRecords;
    }

    // Returns the rows written (having been re-read so they have their Ids)
    protected Result doWriteRows(TFileOutputDelimitedProperties props, List<IndexedRecord> outputRows) throws Exception {
        FileDelimitedSink sink = new FileDelimitedSink();
        sink.initialize(adaptor, props);
        sink.validate(adaptor);
        FileDelimitedWriteOperation writeOperation = sink.createWriteOperation();
        FileDelimitedWriter delimitedWriter = writeOperation.createWriter(adaptor);
        Result result;
        delimitedWriter.open("foo");
        try {
            for (IndexedRecord row : outputRows) {
                delimitedWriter.write(row);
            }
        } finally {
            result = delimitedWriter.close();
        }
        return result;
    }

    protected String getEncoding(EncodingTypeProperties encodingProps) {
        if (encodingProps != null) {
            if (EncodingTypeProperties.ENCODING_TYPE_CUSTOM.equals(encodingProps.encodingType)) {
                return encodingProps.customEncoding.getValue();
            }
            return encodingProps.encodingType.getValue();
        }
        return null;
    }

    public static Schema getBasicSchema(String pattern) {
        return SchemaBuilder.builder().record("Schema").fields() //
                .name("TestBoolean").type().booleanType().noDefault() //
                .name("TestByte").type(AvroUtils._byte()).noDefault() //
                .name("TestBytes").type(AvroUtils._bytes()).noDefault() //
                .name("TestChar").type(AvroUtils._character()).noDefault() //
                .name("TestDate").prop(SchemaConstants.TALEND_COLUMN_PATTERN, pattern)//
                .type(AvroUtils._date()).noDefault() //
                .name("TestDouble").type().doubleType().noDefault() //
                .name("TestFloat").type().floatType().noDefault() //
                .name("TestBigDecimal").type(AvroUtils._decimal()).noDefault()//
                .name("TestInteger").type().intType().noDefault() //
                .name("TestLong").type().longType().noDefault() //
                .name("TestObject").type(AvroUtils._bytes()).noDefault().endRecord();
    }

}
