package org.talend.components.filedelimited;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ErrorCollector;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.service.internal.ComponentServiceImpl;
import org.talend.components.api.test.AbstractComponentTest;
import org.talend.components.api.test.SimpleComponentRegistry;
import org.talend.components.filedelimited.tFileInputDelimited.TFileInputDelimitedDefinition;
import org.talend.components.filedelimited.tFileOutputDelimited.TFileOutputDelimitedDefinition;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.presentation.Form;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("nls")
public class FileDelimitedTestBasic extends AbstractComponentTest {

    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    private ComponentServiceImpl componentService;

    @Before
    public void initializeComponentRegistryAndService() {
        // reset the component service
        componentService = null;
    }

    @Override
    public ComponentService getComponentService() {
        if (componentService == null) {
            SimpleComponentRegistry testComponentRegistry = new SimpleComponentRegistry();

            testComponentRegistry.addComponent(TFileInputDelimitedDefinition.COMPONENT_NAME, new TFileInputDelimitedDefinition());
            testComponentRegistry.addComponent(TFileInputDelimitedDefinition.COMPONENT_NAME,
                    new TFileOutputDelimitedDefinition());

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
}
