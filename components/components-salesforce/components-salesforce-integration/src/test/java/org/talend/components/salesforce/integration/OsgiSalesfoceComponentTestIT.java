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
package org.talend.components.salesforce.integration;

import static org.ops4j.pax.exam.CoreOptions.*;

import java.util.Arrays;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.talend.components.api.ComponentsPaxExamOptions;
import org.talend.components.api.service.ComponentService;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class OsgiSalesfoceComponentTestIT extends SalesforceComponentTestIT {

    @Inject
    ComponentService osgiCompService;

    @Inject
    BundleContext bc;

    @Configuration
    public Option[] config() {
        return getSalesforcePaxExamOption();
    }

    static public Option[] getSalesforcePaxExamOption() {
        return options(composite(ComponentsPaxExamOptions.getOptions()), //
                linkBundle("commons-lang-commons-lang"),//
                linkBundle("org.talend.components-components-common-bundle"), //
                linkBundle("org.talend.components-components-common-tests").noStart(), //
                linkBundle("org.talend.components-components-common-oauth-bundle"), //
                linkBundle("org.talend.components-components-salesforce-definition-bundle"), //
                linkBundle("org.talend.components-components-salesforce-runtime-bundle"), //
                linkBundle("commons-beanutils-commons-beanutils"), //
                linkBundle("org.apache.servicemix.bundles-org.apache.servicemix.bundles.commons-collections"), //
                propagateSystemProperties("salesforce.user", "salesforce.password", "salesforce.key"));
    }

    @Override
    public ComponentService getComponentService() {
        return osgiCompService;
    }

    @Test
    public void showbundleContext() throws InvalidSyntaxException {
        System.out.println(" CLASS IS LOCATED :" + this.getClass().getResource(""));
        System.out.println(" ALL BUNDLES" + Arrays.toString(bc.getBundles()));
        ServiceReference<?>[] allServiceReferences = bc.getAllServiceReferences(null, null);
        System.out.println("ALL SERVICES : " + Arrays.toString(allServiceReferences));
        Bundle[] bundles = bc.getBundles();
        for (Bundle bnd : bundles) {
            System.out.println("Bundle :" + bnd.toString());
            if (bnd.getSymbolicName().equals("org.talend.components.api.service")) {
                System.out.println("Bundle :" + bnd.toString());
            }
        }
        System.out.println("XXX");
    }
}
