// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.ops4j.pax.exam.CoreOptions.composite;
import static org.ops4j.pax.exam.CoreOptions.linkBundle;
import static org.ops4j.pax.exam.CoreOptions.options;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.service.ComponentService;
import org.talend.daikon.definition.Definition;
import org.talend.daikon.definition.service.DefinitionRegistryService;

/**
 * created by sgandon on 7 sept. 2015 Detailled comment
 */
@RunWith(DisablablePaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class OsgiComponentServiceTest {

    static final Logger LOG = LoggerFactory.getLogger(OsgiComponentServiceTest.class);

    @Inject
    BundleContext bc;

    @Configuration
    public Option[] config() {
        try {
            return options(composite(ComponentsPaxExamOptions.getOptions())//
                    , linkBundle("org.ops4j.pax.swissbox-pax-swissbox-property")//
                    , linkBundle("org.ops4j.pax.url-pax-url-commons")//
                    , linkBundle("org.ops4j.pax.url-pax-url-link")//
                    , linkBundle("org.ops4j.pax.url-pax-url-classpath")//
            // , mavenBundle("org.talend.components", "components-api-full-example")//
            );
        } catch (Exception e) {
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

    @Test
    public void testRegistryServiceDynamicLoadOfComponent() throws BundleException, MalformedURLException, URISyntaxException {
        // inside eclipse the bundle context can be retrieved from the Activator.start method or using the FrameworkUtil
        // class.
        BundleContext bundleContext = FrameworkUtil.getBundle(getClass()).getBundleContext();
        ServiceReference<DefinitionRegistryService> compServiceRef = bundleContext
                .getServiceReference(DefinitionRegistryService.class);
        if (compServiceRef != null) {
            DefinitionRegistryService defService = bundleContext.getService(compServiceRef);
            assertNotNull(defService);
            assertEquals(0, defService.getDefinitionsMapByType(Definition.class).size());
            installNewComponentBundle(bundleContext);
            assertEquals(1, defService.getDefinitionsMapByType(Definition.class).size());
        } else {
            fail("Failed to retrieve the Component service");
        }
    }

    private void installNewComponentBundle(BundleContext bundleContext)
            throws BundleException, MalformedURLException, URISyntaxException {
        String exampleUrl = new URL("link:classpath:org.talend.components-osgi-test-component.link").toURI().toString();
        Bundle bundle = bundleContext.installBundle(exampleUrl);
        bundle.start();
    }
}
