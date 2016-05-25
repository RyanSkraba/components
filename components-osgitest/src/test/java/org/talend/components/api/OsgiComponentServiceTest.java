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

import org.junit.BeforeClass;
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
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.service.ComponentServiceTest;
import org.talend.components.api.service.testcomponent.TestComponentInstaller;

/**
 * created by sgandon on 7 sept. 2015 Detailled comment
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class OsgiComponentServiceTest extends ComponentServiceTest {

    @Inject
    private ComponentService osgiCompService;

    // Ensure that the TestComponentInstaller is registered before the osgiCompService is created.
    @BeforeClass
    public static void installTestComponents() {
        BundleContext bundleContext = FrameworkUtil.getBundle(ComponentServiceTest.class).getBundleContext();
        final Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put("component.name", "test");
        bundleContext.registerService(ComponentInstaller.class, new TestComponentInstaller(), props);
    }

    @Configuration
    public Option[] config() {
        try {
            return ComponentsPaxExamOptions.getOptions();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
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
            fail("Failed to retrieve the Component service");
        }
    }

    @Override
    public ComponentService getComponentService() {
        return osgiCompService;
    }

}
