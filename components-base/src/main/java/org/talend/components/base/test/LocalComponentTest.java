package org.talend.components.base.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.talend.components.base.ComponentProperties;
import org.talend.components.base.ComponentService;
import org.talend.components.base.properties.presentation.Form;
import org.talend.components.base.test.testcomponent.TestComponentDefinition;
import org.talend.components.base.test.testcomponent.TestComponentProperties;

@RunWith(SpringJUnit4ClassRunner.class) @ContextConfiguration(classes = { ComponentService.class,
        TestComponentDefinition.class }, loader = AnnotationConfigContextLoader.class) public class LocalComponentTest {

    @Autowired protected ComponentService componentService;

    public LocalComponentTest() {
    }

    @org.junit.Test public void testGetProps() {
        ComponentProperties props = componentService.getComponentProperties(TestComponentDefinition.COMPONENT_NAME);
        Form f = props.getForm(TestComponentProperties.TESTCOMPONENT);
        System.out.println(f);
        System.out.println(props);
    }

}
