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

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.ops4j.pax.exam.CoreOptions.composite;
import static org.ops4j.pax.exam.CoreOptions.linkBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.propagateSystemProperties;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.apache.avro.Schema;
import org.junit.BeforeClass;
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
import org.talend.components.salesforce.SalesforceConnectionProperties;
import org.talend.components.salesforce.runtime.SalesforceSourceOrSink;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.ValidationResult;

/**
 * check static method call to {@link SalesforceSourceOrSink} used by the ESB
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class OsgiSalesforceEsbTestIT extends SalesforceTestBase {

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
                linkBundle("commons-lang-commons-lang"), //
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

    @BeforeClass
    public static void unsetPaxMavenRepo() {
        // we set the pax maven repo to some non existing value cause those tested API should not rely on maven but
        // rather on OSGI only.
        System.setProperty("org.ops4j.pax.url.mvn.localRepository", "");
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

    @Test
    public void checkStaticValidate() {
        SalesforceConnectionProperties props = setupProps(null, !ADD_QUOTES);
        assertEquals(ValidationResult.Result.OK, SalesforceSourceOrSink.validateConnection(props).getStatus());
    }

    @Test
    public void testStaticGetSchemaNames() throws IOException {
        SalesforceConnectionProperties scp = setupProps(null, !ADD_QUOTES);
        List<NamedThing> schemaNames = SalesforceSourceOrSink.getSchemaNames(null, scp);
        assertTrue(schemaNames.size() > 50);
    }

    @Test
    public void testStaticGetSchema() throws IOException {
        SalesforceConnectionProperties scp = setupProps(null, !ADD_QUOTES);
        Schema schema = SalesforceSourceOrSink.getSchema(null, scp, EXISTING_MODULE_NAME);
        assertNotNull(schema);
        assertThat(schema.getFields(), hasSize(greaterThan(10)));
        // assertTrue(schema.getRoot().getChildren().size() > 10);
    }

    @Test
    public void testStaticGetSchemaFail() throws IOException {
        SalesforceConnectionProperties scp = setupProps(null, !ADD_QUOTES);
        try {
            Schema schema = SalesforceSourceOrSink.getSchema(null, scp, "module that does not exist");
            fail("Should have throw an exception when not finding the module");
        } catch (IOException ce) {
            assertTrue(ce.getMessage().contains("does not exist"));
        }
    }

}
