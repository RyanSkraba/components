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
package org.talend.components.filedelimited.wizard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.avro.Schema;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.test.ComponentTestUtils;
import org.talend.components.api.wizard.ComponentWizard;
import org.talend.components.api.wizard.ComponentWizardDefinition;
import org.talend.components.api.wizard.WizardImageType;
import org.talend.components.common.EncodingTypeProperties;
import org.talend.components.filedelimited.FileDelimitedProperties;
import org.talend.components.filedelimited.FileDelimitedTestBasic;
import org.talend.components.filedelimited.tfileinputdelimited.TFileInputDelimitedDefinition;
import org.talend.components.filedelimited.tfileinputdelimited.TFileInputDelimitedProperties;
import org.talend.components.filedelimited.tfileoutputdelimited.TFileOutputDelimitedDefinition;
import org.talend.components.filedelimited.tfileoutputdelimited.TFileOutputDelimitedProperties;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.service.Repository;
import org.talend.daikon.properties.test.PropertiesTestUtils;

/**
 * Created by Talend on 2016-08-22.
 */
public class FileDelimitedWizardTestIT extends FileDelimitedTestBasic {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileDelimitedWizardTestIT.class);

    public FileDelimitedWizardTestIT() {
        super();
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
        assertEquals("file delimited", wizardDef.getMenuItemName());
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

        FileDelimitedProperties wizardProps = (FileDelimitedProperties) formWizard.getProperties();

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

    protected void testWizardProperties() throws Throwable {
        FileDelimitedProperties props = (FileDelimitedProperties) new FileDelimitedProperties("wizard").init();
        Form wizardForm = props.getForm(FileDelimitedProperties.FORM_WIZARD);
        ComponentTestUtils.checkSerialize(props, errorCollector);
        LOGGER.debug(wizardForm.toString());
        assertEquals(FileDelimitedProperties.FORM_WIZARD, wizardForm.getName());
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
        FileDelimitedProperties props = (FileDelimitedProperties) new FileDelimitedProperties("wizard").init();
        props.rowSeparator.setValue("\n");
        Form wizardForm = props.getForm(FileDelimitedProperties.FORM_WIZARD);
        // File name is empty means you can edit the schema manually
        props.fileName.setValue("");

        props = (FileDelimitedProperties) PropertiesTestUtils.checkAndValidate(getComponentService(), wizardForm, "preview",
                props);
        LOGGER.debug("File is not specified, you can edit the schema manual!");
        assertEquals(ValidationResult.Result.OK, props.getValidationResult().getStatus());
        // File is not exist
        props.fileName.setValue(resources + "/not_exist_file.csv");
        props = (FileDelimitedProperties) PropertiesTestUtils.checkAndValidate(getComponentService(), wizardForm, "preview",
                props);
        LOGGER.debug(props.getValidationResult().getMessage());
        assertEquals(ValidationResult.Result.ERROR, props.getValidationResult().getStatus());
        // File is exist
        props.name.setValue("test_wizard");
        props.fileName.setValue(resources + "/test_input_delimited.csv");
        props = (FileDelimitedProperties) PropertiesTestUtils.checkAndValidate(getComponentService(), wizardForm, "preview",
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
        props = (FileDelimitedProperties) PropertiesTestUtils.checkAndValidate(getComponentService(), wizardForm, "preview",
                props);
        assertEquals("{\"data\":[]}", props.previewTable.getValue());
        assertNotNull(props.main.schema.getValue());
        assertEquals(0, props.main.schema.getValue().getFields().size());
        assertEquals(ValidationResult.Result.OK, props.getValidationResult().getStatus());

        // Data is empty
        props.fileName.setValue(resources + "/wizard_data_empty.csv");
        props.header.setValue(1);
        props = (FileDelimitedProperties) PropertiesTestUtils.checkAndValidate(getComponentService(), wizardForm, "preview",
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

    @Test
    public void testWizardSupportComponents() {
        // This control the "Property Type" showed in component
        TFileInputDelimitedProperties input = (TFileInputDelimitedProperties) new TFileInputDelimitedDefinition()
                .createProperties();
        TFileOutputDelimitedProperties output = (TFileOutputDelimitedProperties) new TFileOutputDelimitedDefinition()
                .createProperties();
        List<ComponentWizard> wizards = getComponentService().getComponentWizardsForProperties(input, null);
        assertTrue(wizards.get(0) instanceof FileDelimitedWizard);
        wizards = getComponentService().getComponentWizardsForProperties(output, null);
        assertTrue(wizards.get(0) instanceof FileDelimitedWizard);
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
