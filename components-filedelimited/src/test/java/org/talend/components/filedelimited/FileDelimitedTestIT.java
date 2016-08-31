package org.talend.components.filedelimited;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.test.ComponentTestUtils;
import org.talend.components.api.wizard.ComponentWizard;
import org.talend.components.api.wizard.ComponentWizardDefinition;
import org.talend.components.api.wizard.WizardImageType;
import org.talend.components.filedelimited.tFileInputDelimited.TFileInputDelimitedDefinition;
import org.talend.components.filedelimited.tFileOutputDelimited.TFileOutputDelimitedDefinition;
import org.talend.components.filedelimited.wizard.FileDelimitedWizard;
import org.talend.components.filedelimited.wizard.FileDelimitedWizardDefinition;
import org.talend.components.filedelimited.wizard.FileDelimitedWizardProperties;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.service.Repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * Created by Talend on 2016-08-22.
 */
public class FileDelimitedTestIT extends FileDelimitedTestBasic {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileDelimitedTestIT.class);

    public static Schema SCHEMA_QUERY_ACCOUNT = SchemaBuilder.builder().record("Schema").fields() //
            .name("Id").type().stringType().noDefault() //
            .name("Name").type().stringType().noDefault() //
            .name("Age").type().intType().noDefault().endRecord();

    public FileDelimitedTestIT() {
        super();
    }

    @Test
    public void testGetProps() {
        // Input delimited
        ComponentProperties input = new TFileInputDelimitedDefinition().createProperties();
        Form inputForm = input.getForm(Form.MAIN);
        ComponentTestUtils.checkSerialize(input, errorCollector);
        LOGGER.debug(inputForm.toString());
        LOGGER.debug(input.toString());
        assertEquals(Form.MAIN, inputForm.getName());

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
        Form encoding = (Form) formWizard.getWidget("encoding").getContent();
        assertEquals("Main", encoding.getDisplayName());
        Property encodingType = (Property) encoding.getWidget("encodingType").getContent();
        assertEquals("Encoding", encodingType.getDisplayName());

        wizardProps.main.schema.setValue(SCHEMA_QUERY_ACCOUNT);
        ValidationResult result = wizardProps.afterFormFinishWizard(repo);
        assertEquals(ValidationResult.OK, result);

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
