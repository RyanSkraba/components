package org.talend.components.filedelimited;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.test.ComponentTestUtils;
import org.talend.components.api.wizard.ComponentWizard;
import org.talend.components.api.wizard.ComponentWizardDefinition;
import org.talend.components.api.wizard.WizardImageType;
import org.talend.components.common.EncodingTypeProperties;
import org.talend.components.filedelimited.tFileInputDelimited.TFileInputDelimitedDefinition;
import org.talend.components.filedelimited.tFileInputDelimited.TFileInputDelimitedProperties;
import org.talend.components.filedelimited.tFileOutputDelimited.TFileOutputDelimitedDefinition;
import org.talend.components.filedelimited.wizard.FileDelimitedWizard;
import org.talend.components.filedelimited.wizard.FileDelimitedWizardDefinition;
import org.talend.components.filedelimited.wizard.FileDelimitedWizardProperties;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.service.Repository;
import org.talend.daikon.properties.test.PropertiesTestUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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

        // Wizard properties
        testWizardProperties();

        // Output delimited
        ComponentProperties output = new TFileOutputDelimitedDefinition().createProperties();
        Form outputForm = output.getForm(Form.MAIN);
        ComponentTestUtils.checkSerialize(output, errorCollector);
        LOGGER.debug(outputForm.toString());
        LOGGER.debug(output.toString());
        assertEquals(Form.MAIN, outputForm.getName());

    }

    @Test
    public void testWizard() throws Throwable {
        final List<RepoProps> repoProps = new ArrayList<>();

        Repository repo = new TestRepository(repoProps);
        getComponentService().setRepository(repo);

        Set<ComponentWizardDefinition> wizards = getComponentService().getTopLevelComponentWizards();
        int count = 0;
        ComponentWizardDefinition wizardDef = null;
        for (ComponentWizardDefinition wizardDefinition : wizards) {
            if (wizardDefinition instanceof FileDelimitedWizardDefinition) {
                wizardDef = wizardDefinition;
                count++;
            }
        }
        assertEquals(1, count);
        assertEquals("file delimited (New)", wizardDef.getMenuItemName());
        ComponentWizard wiz = getComponentService().getComponentWizard(FileDelimitedWizardDefinition.COMPONENT_WIZARD_NAME,
                "nodeFileDelimited");
        assertNotNull(wiz);
        assertEquals("nodeFileDelimited", wiz.getRepositoryLocation());
        FileDelimitedWizard swiz = (FileDelimitedWizard) wiz;
        List<Form> forms = wiz.getForms();
        Form formWizard = forms.get(0);
        assertEquals("Wizard", formWizard.getName());
        assertFalse(formWizard.isAllowBack());
        assertFalse(formWizard.isAllowForward());
        assertFalse(formWizard.isAllowFinish());

        assertEquals("Delimited File Settings", formWizard.getTitle());
        assertEquals("", formWizard.getSubtitle());

        FileDelimitedWizardProperties wizardProps = (FileDelimitedWizardProperties) formWizard.getProperties();

        Object image = getComponentService().getWizardPngImage(FileDelimitedWizardDefinition.COMPONENT_WIZARD_NAME,
                WizardImageType.TREE_ICON_16X16);
        assertNotNull(image);
        image = getComponentService().getWizardPngImage(FileDelimitedWizardDefinition.COMPONENT_WIZARD_NAME,
                WizardImageType.WIZARD_BANNER_75X66);
        assertNotNull(image);

        // Check the non-top-level wizard

        assertEquals("Name", wizardProps.getProperty("name").getDisplayName());
        wizardProps.name.setValue("connName");
        setupProps(wizardProps);
        Form encodingForm = (Form) formWizard.getWidget("encoding").getContent();
        assertEquals("Main", encodingForm.getDisplayName());
        Property encodingType = (Property) encodingForm.getWidget("encodingType").getContent();
        assertEquals("Encoding", encodingType.getDisplayName());

        assertFalse(formWizard.getWidget(wizardProps.encoding.getName()).isHidden());
        assertFalse(encodingForm.getWidget(wizardProps.encoding.encodingType.getName()).isHidden());
        assertTrue(encodingForm.getWidget(wizardProps.encoding.customEncoding.getName()).isHidden());
        wizardProps.encoding.encodingType.setValue(EncodingTypeProperties.ENCODING_TYPE_CUSTOM);
        assertTrue(encodingForm.getWidget(wizardProps.encoding.encodingType.getName()).isCallAfter());
        getComponentService().afterProperty(wizardProps.encoding.encodingType.getName(), wizardProps.encoding);
        assertFalse(encodingForm.getWidget(wizardProps.encoding.customEncoding.getName()).isHidden());

        wizardProps.main.schema.setValue(BASIC_SCHEMA);
        ValidationResult result = wizardProps.afterFormFinishWizard(repo);
        assertEquals(ValidationResult.OK, result);

        // TODO Continue when finish the wizard
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
        assertNotNull(input.trimColumns.trimTable.trim.getValue());
        assertEquals(3, input.trimColumns.trimTable.trim.getValue().size());

        // Decode table
        input.enableDecode.setValue(true);
        assertTrue(inputAdvancedForm.getWidget(input.enableDecode.getName()).isCallAfter());
        getComponentService().afterProperty(input.enableDecode.getName(), input);

        assertNotNull(input.decodeTable.columnName.getValue());
        assertEquals(Arrays.asList("Id", "Name", "Age"), input.decodeTable.columnName.getValue());
        assertNotNull(input.decodeTable.decode.getValue());
        assertEquals(3, input.decodeTable.decode.getValue().size());
    }

    protected void testWizardProperties() throws Throwable {
        FileDelimitedWizardProperties props = (FileDelimitedWizardProperties) new FileDelimitedWizardProperties("wizard").init();
        Form wizardForm = props.getForm(FileDelimitedWizardProperties.FORM_WIZARD);
        ComponentTestUtils.checkSerialize(props, errorCollector);
        LOGGER.debug(wizardForm.toString());
        assertEquals(FileDelimitedWizardProperties.FORM_WIZARD, wizardForm.getName());
        assertFalse(wizardForm.isAllowFinish());

        assertFalse(wizardForm.getWidget(props.name.getName()).isHidden());
        assertFalse(wizardForm.getWidget(props.fileName.getName()).isHidden());
        Form encodingForm = wizardForm.getChildForm(props.encoding.getName());
        assertFalse(encodingForm.getWidget(props.encoding.encodingType.getName()).isHidden());
        assertTrue(encodingForm.getWidget(props.encoding.customEncoding.getName()).isHidden());
        assertEquals(EncodingTypeProperties.ENCODING_TYPE_UTF_8, props.encoding.encodingType.getValue());
        assertFalse(wizardForm.getWidget(props.rowSeparator.getName()).isHidden());
        assertEquals("\\n", props.rowSeparator.getValue());
        assertFalse(wizardForm.getWidget(props.fieldSeparator.getName()).isHidden());
        assertEquals(";", props.fieldSeparator.getValue());
        assertFalse(wizardForm.getWidget(props.csvOptions.getName()).isHidden());
        assertFalse(props.csvOptions.getValue());
        assertTrue(wizardForm.getWidget(props.textEnclosure.getName()).isHidden());
        assertTrue(wizardForm.getWidget(props.escapeChar.getName()).isHidden());
        assertFalse(wizardForm.getWidget(props.header.getName()).isHidden());
        assertEquals(0, (int) props.header.getValue());
        assertFalse(wizardForm.getWidget(props.footer.getName()).isHidden());
        assertEquals(0, (int) props.footer.getValue());
        assertFalse(wizardForm.getWidget(props.removeEmptyRow.getName()).isHidden());
        assertTrue(props.removeEmptyRow.getValue());
        assertFalse(wizardForm.getWidget(props.preview.getName()).isHidden());
        assertFalse(wizardForm.getWidget(props.previewTable.getName()).isHidden());

        // Change to CSV mode
        props.csvOptions.setValue(true);
        assertTrue(wizardForm.getWidget(props.csvOptions.getName()).isCallAfter());
        getComponentService().afterProperty(props.csvOptions.getName(), props);
        assertFalse(wizardForm.getWidget(props.escapeChar.getName()).isHidden());
        assertEquals("\\\"", props.escapeChar.getValue());
        assertFalse(wizardForm.getWidget(props.textEnclosure.getName()).isHidden());
        assertEquals("\\\"", props.textEnclosure.getValue());

        // Change name
        props.name.setValue("wizard");
        PropertiesTestUtils.checkAndValidate(getComponentService(), wizardForm, "name", props);
        assertTrue(wizardForm.isAllowFinish());
    }

    @Test
    public void testWizardPreviewData() throws Throwable {
        String resources = getClass().getResource("/runtime/input").getPath();
        FileDelimitedWizardProperties props = (FileDelimitedWizardProperties) new FileDelimitedWizardProperties("wizard").init();
        props.rowSeparator.setValue("\n");
        Form wizardForm = props.getForm(FileDelimitedWizardProperties.FORM_WIZARD);
        // File name is empty means you can edit the schema manually
        props.fileName.setValue("");

        props = (FileDelimitedWizardProperties) PropertiesTestUtils.checkAndValidate(getComponentService(), wizardForm, "preview",
                props);
        LOGGER.debug("File is not specified, you can edit the schema manual!");
        assertEquals(ValidationResult.Result.OK, props.getValidationResult().getStatus());
        // File is not exist
        props.fileName.setValue(resources + "/not_exist_file.csv");
        props = (FileDelimitedWizardProperties) PropertiesTestUtils.checkAndValidate(getComponentService(), wizardForm, "preview",
                props);
        LOGGER.debug(props.getValidationResult().getMessage());
        assertEquals(ValidationResult.Result.ERROR, props.getValidationResult().getStatus());
        // File is exist
        props.name.setValue("test_wizard");
        props.fileName.setValue(resources + "/test_input_delimited.csv");
        props = (FileDelimitedWizardProperties) PropertiesTestUtils.checkAndValidate(getComponentService(), wizardForm, "preview",
                props);
        assertNotNull(props.main.schema.getValue());
        assertEquals(19, props.main.schema.getValue().getFields().size());
        assertEquals(0, props.main.schema.getValue().getField("Column0").pos());
        assertEquals(4, props.main.schema.getValue().getField("Column4").pos());
        assertEquals(7, props.main.schema.getValue().getField("Column7").pos());
        assertEquals(9, props.main.schema.getValue().getField("Column9").pos());
        assertEquals(11, props.main.schema.getValue().getField("Column11").pos());
        assertEquals(18, props.main.schema.getValue().getField("Column18").pos());
        assertEquals(ValidationResult.Result.OK, props.getValidationResult().getStatus());

        resources = getClass().getResource("/runtime/wizard").getPath();

        // File is empty
        props.fileName.setValue(resources + "/wizard_file_empty.csv");
        props = (FileDelimitedWizardProperties) PropertiesTestUtils.checkAndValidate(getComponentService(), wizardForm, "preview",
                props);
        assertEquals("{\"data\":[]}", props.previewTable.getValue());
        assertNotNull(props.main.schema.getValue());
        assertEquals(0, props.main.schema.getValue().getFields().size());
        assertEquals(ValidationResult.Result.OK, props.getValidationResult().getStatus());

        // Data is empty
        props.fileName.setValue(resources + "/wizard_data_empty.csv");
        props.header.setValue(1);
        props = (FileDelimitedWizardProperties) PropertiesTestUtils.checkAndValidate(getComponentService(), wizardForm, "preview",
                props);
        assertEquals(
                "{\"columnNames\":[\"TestBoolean\",\"TestByte\",\"TestBytes\",\"TestChar\",\"TestDate\",\"TestDouble\",\"TestFloat\",\"TestBigDecimal\",\"TestInteger\",\"TestLong\",\"TestObject\"],\"data\":[]}",
                props.previewTable.getValue());
        assertNotNull(props.main.schema.getValue());
        assertEquals(11, props.main.schema.getValue().getFields().size());
        assertEquals(0, props.main.schema.getValue().getField("TestBoolean").pos());
        assertEquals(4, props.main.schema.getValue().getField("TestDate").pos());
        assertEquals(7, props.main.schema.getValue().getField("TestBigDecimal").pos());
        assertEquals(9, props.main.schema.getValue().getField("TestLong").pos());
        assertEquals(ValidationResult.Result.OK, props.getValidationResult().getStatus());

    }

    static class RepoProps {

        Properties props;

        String name;

        String repoLocation;

        Schema schema;

        String schemaPropertyName;

        RepoProps(Properties props, String name, String repoLocation, String schemaPropertyName) {
            this.props = props;
            this.name = name;
            this.repoLocation = repoLocation;
            this.schemaPropertyName = schemaPropertyName;
            if (schemaPropertyName != null) {
                this.schema = (Schema) props.getValuedProperty(schemaPropertyName).getValue();
            }
        }

        @Override
        public String toString() {
            return "RepoProps: " + repoLocation + "/" + name + " props: " + props;
        }
    }

    class TestRepository implements Repository {

        private int locationNum;

        public String componentIdToCheck;

        public ComponentProperties properties;

        public List<RepoProps> repoProps;

        TestRepository(List<RepoProps> repoProps) {
            this.repoProps = repoProps;
        }

        @Override
        public String storeProperties(Properties properties, String name, String repositoryLocation, String schemaPropertyName) {
            RepoProps rp = new RepoProps(properties, name, repositoryLocation, schemaPropertyName);
            repoProps.add(rp);
            LOGGER.debug(rp.toString());
            return repositoryLocation + ++locationNum;
        }
    }
}
