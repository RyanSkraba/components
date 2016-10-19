package org.talend.components.filedelimited;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.test.ComponentTestUtils;
import org.talend.components.common.EncodingTypeProperties;
import org.talend.components.filedelimited.tfileinputdelimited.TFileInputDelimitedDefinition;
import org.talend.components.filedelimited.tfileinputdelimited.TFileInputDelimitedProperties;
import org.talend.components.filedelimited.tfileoutputdelimited.TFileOutputDelimitedDefinition;
import org.talend.daikon.properties.presentation.Form;

/**
 * Created by Talend on 2016-08-22.
 */
public class FileDelimitedTestIT extends FileDelimitedTestBasic {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileDelimitedTestIT.class);

    public static Schema BASIC_SCHEMA = SchemaBuilder.builder().record("Schema").fields() //
            .name("Id").type().stringType().noDefault() //
            .name("Name").type().stringType().noDefault() //
            .name("Age").type().intType().noDefault().endRecord();

    public FileDelimitedTestIT() {
        super();
    }

    @Test
    public void testFamily() {
        ComponentDefinition cdInput = getComponentService().getComponentDefinition(TFileInputDelimitedDefinition.COMPONENT_NAME);
        assertEquals(1, cdInput.getFamilies().length);
        assertEquals("File/Input", cdInput.getFamilies()[0]);

        ComponentDefinition cdOutput = getComponentService()
                .getComponentDefinition(TFileOutputDelimitedDefinition.COMPONENT_NAME);
        assertEquals(1, cdOutput.getFamilies().length);
        assertEquals("File/Output", cdOutput.getFamilies()[0]);
    }

    @Test
    public void testGetProps() throws Throwable {
        // Input properties
        testInputProperties();

        // Output delimited
        ComponentProperties output = new TFileOutputDelimitedDefinition().createProperties();
        Form outputForm = output.getForm(Form.MAIN);
        ComponentTestUtils.checkSerialize(output, errorCollector);
        LOGGER.debug(outputForm.toString());
        LOGGER.debug(output.toString());
        assertEquals(Form.MAIN, outputForm.getName());

    }

    protected void testInputProperties() throws Throwable {

        TFileInputDelimitedProperties input = (TFileInputDelimitedProperties) new TFileInputDelimitedDefinition()
                .createProperties();
        Form inputMainForm = input.getForm(Form.MAIN);
        ComponentTestUtils.checkSerialize(input, errorCollector);
        LOGGER.debug(inputMainForm.toString());
        assertEquals(Form.MAIN, inputMainForm.getName());

        // Default properties
        assertFalse(input.csvOptions.getValue());
        assertFalse(inputMainForm.getWidget(input.rowSeparator.getName()).isHidden());
        assertEquals("\\n", input.rowSeparator.getValue());
        assertFalse(inputMainForm.getWidget(input.fieldSeparator.getName()).isHidden());
        assertEquals(";", input.fieldSeparator.getValue());
        assertTrue(inputMainForm.getWidget(input.escapeChar.getName()).isHidden());
        assertTrue(inputMainForm.getWidget(input.textEnclosure.getName()).isHidden());
        assertFalse(inputMainForm.getWidget(input.header.getName()).isHidden());
        assertEquals(0, (int) input.header.getValue());
        assertFalse(inputMainForm.getWidget(input.footer.getName()).isHidden());
        assertEquals(0, (int) input.footer.getValue());
        assertFalse(inputMainForm.getWidget(input.limit.getName()).isHidden());
        assertNull(input.limit.getValue());
        assertFalse(inputMainForm.getWidget(input.removeEmptyRow.getName()).isHidden());
        assertTrue(input.removeEmptyRow.getValue());
        assertFalse(inputMainForm.getWidget(input.dieOnError.getName()).isHidden());
        assertFalse(input.dieOnError.getValue());

        Form inputAdvancedForm = input.getForm(Form.ADVANCED);
        assertFalse(inputAdvancedForm.getWidget(input.advancedSeparator.getName()).isHidden());
        assertFalse(input.advancedSeparator.getValue());
        assertTrue(inputAdvancedForm.getWidget(input.thousandsSeparator.getName()).isHidden());
        assertTrue(inputAdvancedForm.getWidget(input.decimalSeparator.getName()).isHidden());
        assertFalse(inputAdvancedForm.getWidget(input.random.getName()).isHidden());
        assertFalse(input.random.getValue());
        assertTrue(inputAdvancedForm.getWidget(input.nbRandom.getName()).isHidden());
        Form trimForm = inputAdvancedForm.getChildForm(input.trimColumns.getName());
        assertFalse(trimForm.getWidget(input.trimColumns.trimAll.getName()).isHidden());
        assertFalse(input.trimColumns.trimAll.getValue());
        assertFalse(trimForm.getWidget(input.trimColumns.trimTable.getName()).isHidden());
        assertNull(input.trimColumns.trimTable.trim.getValue());
        assertFalse(inputAdvancedForm.getWidget(input.checkFieldsNum.getName()).isHidden());
        assertFalse(input.checkFieldsNum.getValue());
        assertFalse(inputAdvancedForm.getWidget(input.checkDate.getName()).isHidden());
        assertFalse(input.checkDate.getValue());
        Form encodingForm = inputAdvancedForm.getChildForm(input.encoding.getName());
        assertFalse(encodingForm.getWidget(input.encoding.encodingType.getName()).isHidden());
        assertTrue(encodingForm.getWidget(input.encoding.customEncoding.getName()).isHidden());
        assertEquals(EncodingTypeProperties.ENCODING_TYPE_ISO_8859_15, input.encoding.encodingType.getValue());
        assertFalse(inputAdvancedForm.getWidget(input.splitRecord.getName()).isHidden());
        assertFalse(input.splitRecord.getValue());
        assertFalse(inputAdvancedForm.getWidget(input.enableDecode.getName()).isHidden());
        assertFalse(input.enableDecode.getValue());
        assertTrue(inputAdvancedForm.getWidget(input.decodeTable.getName()).isHidden());

        // Use uncompress
        input.uncompress.setValue(true);
        assertTrue(inputMainForm.getWidget(input.uncompress.getName()).isCallAfter());
        getComponentService().afterProperty(input.uncompress.getName(), input);
        assertTrue(inputMainForm.getWidget(input.footer.getName()).isHidden());
        assertTrue(inputAdvancedForm.getWidget(input.random.getName()).isHidden());
        assertTrue(inputAdvancedForm.getWidget(input.nbRandom.getName()).isHidden());
        input.uncompress.setValue(false);
        assertTrue(inputMainForm.getWidget(input.uncompress.getName()).isCallAfter());
        getComponentService().afterProperty(input.uncompress.getName(), input);

        // Use random
        assertFalse(inputAdvancedForm.getWidget(input.random.getName()).isHidden());
        input.random.setValue(true);
        assertTrue(inputAdvancedForm.getWidget(input.random.getName()).isCallAfter());
        getComponentService().afterProperty(input.random.getName(), input);
        assertFalse(inputAdvancedForm.getWidget(input.nbRandom.getName()).isHidden());
        assertEquals(10, (int) input.nbRandom.getValue());

        // Change to CSV mode
        input.csvOptions.setValue(true);
        assertTrue(inputMainForm.getWidget(input.csvOptions.getName()).isCallAfter());
        getComponentService().afterProperty(input.csvOptions.getName(), input);
        assertFalse(inputMainForm.getWidget(input.rowSeparator.getName()).isHidden());
        assertFalse(inputMainForm.getWidget(input.escapeChar.getName()).isHidden());
        assertFalse(inputMainForm.getWidget(input.textEnclosure.getName()).isHidden());
        assertTrue(inputAdvancedForm.getWidget(input.random.getName()).isHidden());
        assertTrue(inputAdvancedForm.getWidget(input.splitRecord.getName()).isHidden());

        // Change to advanced separator
        input.advancedSeparator.setValue(true);
        assertTrue(inputAdvancedForm.getWidget(input.advancedSeparator.getName()).isCallAfter());
        getComponentService().afterProperty(input.advancedSeparator.getName(), input);
        assertFalse(inputAdvancedForm.getWidget(input.thousandsSeparator.getName()).isHidden());
        assertEquals(",", input.thousandsSeparator.getValue());
        assertFalse(inputAdvancedForm.getWidget(input.decimalSeparator.getName()).isHidden());
        assertEquals(".", input.decimalSeparator.getValue());

        // Schema change
        input.main.schema.setValue(BASIC_SCHEMA);
        input.schemaListener.afterSchema();
        Form schemaForm = inputMainForm.getChildForm(input.main.getName());
        assertTrue(schemaForm.getWidget(input.main.schema.getName()).isCallAfter());
        getComponentService().afterProperty(input.main.schema.getName(), input.main);

        // Trim table
        input.trimColumns.trimAll.setValue(true);
        assertTrue(trimForm.getWidget(input.trimColumns.trimAll.getName()).isCallAfter());
        getComponentService().afterProperty(input.trimColumns.trimAll.getName(), input.trimColumns);

        assertNotNull(input.trimColumns.trimTable.columnName.getValue());
        assertEquals(Arrays.asList("Id", "Name", "Age"), input.trimColumns.trimTable.columnName.getValue());

        // Decode table
        input.enableDecode.setValue(true);
        assertTrue(inputAdvancedForm.getWidget(input.enableDecode.getName()).isCallAfter());
        getComponentService().afterProperty(input.enableDecode.getName(), input);

        assertNotNull(input.decodeTable.columnName.getValue());
        assertEquals(Arrays.asList("Id", "Name", "Age"), input.decodeTable.columnName.getValue());
    }
}
