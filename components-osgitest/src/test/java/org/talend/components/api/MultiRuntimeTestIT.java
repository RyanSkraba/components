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

import javax.inject.Inject;

import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.talend.components.api.service.ComponentService;
import org.talend.components.multiruntime.AbstractMultiRuntimeComponentTests;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class MultiRuntimeTestIT extends AbstractMultiRuntimeComponentTests {

    @Inject
    ComponentService osgiCompService;

    @Configuration
    public Option[] config() {

        return PaxExamOptions.getOptions();
        // these debug option do not work, I still don't know how to debug this :, cleanCaches(),
        // vmOption("-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005")
        // , systemTimeout(0)
    }

    @Override
    public ComponentService getComponentService() {
        BundleContext bundleContext = FrameworkUtil.getBundle(getClass()).getBundleContext();
        try {
            ServiceReference<?>[] compServiceRef = bundleContext.getAllServiceReferences(null, null);
            for (ServiceReference<?> serviceRef : compServiceRef) {
                System.out.println("SERVICE REF : " + serviceRef);
                Object service = bundleContext.getService(serviceRef);
                System.out.println("SERVICE : " + service);
            }
        } catch (InvalidSyntaxException e) {
            throw new RuntimeException(e);
        }
        Bundle[] bundles = bundleContext.getBundles();
        for (Bundle bun : bundles) {
            System.out.println("BUNDLE : [" + bun.getSymbolicName() + "], state : " + bun.getState() + ", num services : "
                    + (bun.getServicesInUse() != null ? bun.getServicesInUse().length : "0"));

        }
        return osgiCompService;
    }

}
