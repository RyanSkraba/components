// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.snowflake.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.apache.avro.Schema;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.wizard.ComponentWizard;
import org.talend.components.api.wizard.ComponentWizardDefinition;
import org.talend.components.api.wizard.WizardNameComparator;
import org.talend.components.service.spring.SpringTestApp;
import org.talend.components.snowflake.SnowflakeConnectionProperties;
import org.talend.components.snowflake.SnowflakeConnectionWizardDefinition;
import org.talend.components.snowflake.SnowflakeTableListProperties;
import org.talend.components.snowflake.SnowflakeTableProperties;
import org.talend.components.snowflake.runtime.SnowflakeSourceOrSink;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.PresentationItem;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.service.Repository;
import org.talend.daikon.properties.test.PropertiesTestUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = SpringTestApp.class)
public class SpringSnowflakeTestIT extends SnowflakeRuntimeIT {

    @Inject
    ComponentService componentService;

    @Override
    public ComponentService getComponentService() {
        return componentService;
    }

    @Test
    public void testFamily() {
        ComponentDefinition cd = getComponentService().getComponentDefinition("tSnowflakeConnection");
        assertEquals(1, cd.getFamilies().length);
        assertEquals("Cloud/Snowflake", cd.getFamilies()[0]);
    }

    @Test
    public void testWizard() throws Throwable {
        final List<RepoProps> repoProps = new ArrayList<>();

        Repository repo = new TestRepository(repoProps);
        getComponentService().setRepository(repo);

        Set<ComponentWizardDefinition> wizards = getComponentService().getTopLevelComponentWizards();
        int connectionWizardNumber = 0;
        ComponentWizardDefinition wizardDef = null;
        for (ComponentWizardDefinition wizardDefinition : wizards) {
            if (wizardDefinition instanceof SnowflakeConnectionWizardDefinition) {
                wizardDef = wizardDefinition;
                connectionWizardNumber++;
            }
        }
        assertEquals(1, connectionWizardNumber);
        assertEquals("Snowflake Connection", wizardDef.getMenuItemName());
        ComponentWizard connectionWizard = getComponentService()
                .getComponentWizard(SnowflakeConnectionWizardDefinition.COMPONENT_WIZARD_NAME, "nodeSnowflake");
        assertNotNull(connectionWizard);
        assertEquals("nodeSnowflake", connectionWizard.getRepositoryLocation());
        List<Form> forms = connectionWizard.getForms();
        Form connFormWizard = forms.get(0);
        assertEquals("Wizard", connFormWizard.getName());
        assertFalse(connFormWizard.isAllowBack());
        assertFalse(connFormWizard.isAllowForward());
        assertFalse(connFormWizard.isAllowFinish());
        // Main from SnowflakeTableListProperties
        assertEquals("Main", forms.get(1).getName());
        assertEquals("Snowflake Connection Settings", connFormWizard.getTitle());
        assertEquals("Complete these fields in order to connect to your Snowflake account.", connFormWizard.getSubtitle());

        SnowflakeConnectionProperties connProps = (SnowflakeConnectionProperties) connFormWizard.getProperties();

        Form advancedForm = connProps.getForm(Form.ADVANCED);
        assertTrue(
                ((PresentationItem) connFormWizard.getWidget("advanced").getContent()).getFormtoShow() + " should be == to "
                        + advancedForm,
                ((PresentationItem) connFormWizard.getWidget("advanced").getContent()).getFormtoShow() == advancedForm);

        assertEquals("Name", connProps.getProperty("name").getDisplayName());
        connProps.name.setValue("connName");
        setupProps(connProps);
        Form userPassword = (Form) connFormWizard.getWidget("userPassword").getContent();
        Property passwordSe = (Property) userPassword.getWidget("password").getContent();
        assertEquals("Password", passwordSe.getDisplayName());
        NamedThing nameProp = connFormWizard.getWidget("name").getContent(); //$NON-NLS-1$
        assertEquals("Name", nameProp.getDisplayName());
        PropertiesTestUtils.checkAndValidate(getComponentService(), connFormWizard, "testConnection", connProps);
        assertTrue(connFormWizard.isAllowForward());

        Form modForm = forms.get(1);
        SnowflakeTableListProperties mlProps = (SnowflakeTableListProperties) modForm.getProperties();
        assertFalse(modForm.isCallAfterFormBack());
        assertFalse(modForm.isCallAfterFormNext());
        assertTrue(modForm.isCallAfterFormFinish());
        assertTrue(modForm.isCallBeforeFormPresent());
        assertFalse(modForm.isAllowBack());
        assertFalse(modForm.isAllowForward());
        assertFalse(modForm.isAllowFinish());
        mlProps = (SnowflakeTableListProperties) getComponentService().beforeFormPresent(modForm.getName(), mlProps);
        assertTrue(modForm.isAllowBack());
        assertFalse(modForm.isAllowForward());
        assertTrue(modForm.isAllowFinish());
        List<NamedThing> all = mlProps.selectedTableNames.getValue();
        assertNull(all);
        List<NamedThing> possibleValues = (List<NamedThing>) mlProps.selectedTableNames.getPossibleValues();
        LOGGER.info("possibleValues: " + possibleValues);
        assertEquals(1, possibleValues.size());
        List<NamedThing> selected = new ArrayList<>();
        selected.add(possibleValues.get(0));

        mlProps.selectedTableNames.setValue(selected);
        getComponentService().afterFormFinish(modForm.getName(), mlProps);
        LOGGER.debug(repoProps.toString());
        assertEquals(2, repoProps.size());
        int i = 0;
        for (RepoProps rp : repoProps) {
            if (i == 0) {
                assertEquals("connName", rp.name);
                SnowflakeConnectionProperties storedConnProps = (SnowflakeConnectionProperties) rp.props;
                assertEquals(USER, storedConnProps.userPassword.userId.getValue());
                assertEquals(PASSWORD, storedConnProps.userPassword.password.getValue());
            } else {
                SnowflakeTableProperties storedModule = (SnowflakeTableProperties) rp.props;
                assertEquals(selected.get(i - 1).getName(), storedModule.tableName.getValue());
                assertTrue(rp.schema.getFields().size() == NUM_COLUMNS);
                assertThat(storedModule.main.schema.getStringValue(), Matchers.is(rp.schema.toString()));
            }
            i++;
        }
    }

    @Test
    public void testModuleWizard() throws Throwable {
        ComponentWizard connectionWizard = getComponentService()
                .getComponentWizard(SnowflakeConnectionWizardDefinition.COMPONENT_WIZARD_NAME, "nodeSnowflake");
        List<Form> forms = connectionWizard.getForms();
        Form connFormWizard = forms.get(0);
        SnowflakeConnectionProperties connProps = (SnowflakeConnectionProperties) connFormWizard.getProperties();

        ComponentWizard[] subWizards = getComponentService().getComponentWizardsForProperties(connProps, "location")
                .toArray(new ComponentWizard[2]);
        Arrays.sort(subWizards, new WizardNameComparator());
        assertEquals(2, subWizards.length);

        assertTrue(subWizards[0].getDefinition().isTopLevel());
        assertEquals("Snowflake Connection", subWizards[0].getDefinition().getMenuItemName());

        // Edit connection wizard - we copy the connection properties, as we present the UI, so we use the
        // connection properties object created by the new wizard
        assertFalse(connProps == subWizards[1].getForms().get(0).getProperties());
        // will remove the edit wizard in future, no need to test more about it

        // Add module wizard - we refer to the existing connection properties as we don't present the UI
        // for them.
        // assertTrue(connProps == ((SnowflakeTableListProperties) subWizards[2].getForms().get(0).getProperties())
        // .getConnectionProperties());
        // assertFalse(subWizards[2].getDefinition().isTopLevel());
        // assertEquals("Snowflake Tables", subWizards[2].getDefinition().getMenuItemName());
    }

    @Test
    public void testLogin() throws Throwable {
        SnowflakeConnectionProperties props = (SnowflakeConnectionProperties) setupProps(null);
        LOGGER.debug(String.valueOf(props));
        Form f = props.getForm(SnowflakeConnectionProperties.FORM_WIZARD);
        props = (SnowflakeConnectionProperties) PropertiesTestUtils.checkAndValidate(getComponentService(), f, "testConnection",
                props);
        LOGGER.debug(props.getValidationResult().toString());
        assertEquals(ValidationResult.Result.OK, props.getValidationResult().getStatus());
    }

    @Test
    public void testLoginFail() throws Throwable {
        SnowflakeConnectionProperties props = (SnowflakeConnectionProperties) setupProps(null);
        props.userPassword.userId.setValue("blah");
        Form f = props.getForm(SnowflakeConnectionProperties.FORM_WIZARD);
        props = (SnowflakeConnectionProperties) PropertiesTestUtils.checkAndValidate(getComponentService(), f, "testConnection",
                props);
        LOGGER.debug(props.getValidationResult().toString());
        assertEquals(ValidationResult.Result.ERROR, props.getValidationResult().getStatus());
    }

    @Test
    public void testGetSchema() throws IOException {
        SnowflakeConnectionProperties scp = (SnowflakeConnectionProperties) setupProps(null);
        SnowflakeSourceOrSink ss = new SnowflakeSourceOrSink();
        ss.initialize(null, scp);
        Schema schema = ss.getEndpointSchema(null, testTable);
        assertNotNull(schema);
        assertThat(schema.getFields(), Matchers.hasSize(NUM_COLUMNS));
    }

}
