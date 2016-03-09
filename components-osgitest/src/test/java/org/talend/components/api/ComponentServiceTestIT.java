// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.api;

import static org.junit.Assert.*;

import java.util.Dictionary;
import java.util.Hashtable;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.service.ComponentServiceAbstractForIT;
import org.talend.components.api.service.testcomponent.TestComponentDefinition;
import org.talend.components.api.service.testcomponent.TestComponentWizardDefinition;
import org.talend.components.api.wizard.ComponentWizardDefinition;

/**
 * created by sgandon on 7 sept. 2015 Detailled comment
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class ComponentServiceTestIT extends ComponentServiceAbstractForIT {

    @Inject
    private ComponentService osgiCompService;

    @Configuration
    public Option[] config() {
        try {
            return PaxExamOptions.getOptions();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    @Before
    public void setupComponentService() {
        BundleContext bundleContext = FrameworkUtil.getBundle(getClass()).getBundleContext();
        final Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put("component.name", Constants.COMPONENT_BEAN_PREFIX + TestComponentDefinition.COMPONENT_NAME);
        bundleContext.registerService(ComponentDefinition.class, new TestComponentDefinition(), props);
        props.put("component.name", Constants.COMPONENT_WIZARD_BEAN_PREFIX + TestComponentWizardDefinition.COMPONENT_WIZARD_NAME);
        bundleContext.registerService(ComponentWizardDefinition.class, new TestComponentWizardDefinition(), props);
    }

    @Test
    public void exampleOnHowToGetTheServiceUsingOsgiApis() {
        // inside eclipse the bundle context can be retrieved from the Activator.start method or using the FrameworkUtil
        // class.
        BundleContext bundleContext = FrameworkUtil.getBundle(getClass()).getBundleContext();
        ServiceReference<ComponentService> compServiceRef = bundleContext.getServiceReference(ComponentService.class);
        if (compServiceRef != null) {
            ComponentService compService = bundleContext.getService(compServiceRef);
            assertNotNull(compService);
        } else {
            fail("Failed to retreive the Component service");
        }
    }

    @Override
    public ComponentService getComponentService() {
        return osgiCompService;
    }

}
