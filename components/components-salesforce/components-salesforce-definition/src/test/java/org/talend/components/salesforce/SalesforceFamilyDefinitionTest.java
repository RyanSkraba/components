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

package org.talend.components.salesforce;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;

import java.util.Arrays;
import java.util.List;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;
import org.talend.components.api.ComponentInstaller;
import org.talend.components.common.CommonTags;
import org.talend.components.salesforce.dataprep.SalesforceInputDefinition;
import org.talend.components.salesforce.dataset.SalesforceDatasetDefinition;
import org.talend.components.salesforce.datastore.SalesforceDatastoreDefinition;
import org.talend.components.salesforce.tsalesforcebulkexec.TSalesforceBulkExecDefinition;
import org.talend.components.salesforce.tsalesforceconnection.TSalesforceConnectionDefinition;
import org.talend.components.salesforce.tsalesforcegetdeleted.TSalesforceGetDeletedDefinition;
import org.talend.components.salesforce.tsalesforcegetservertimestamp.TSalesforceGetServerTimestampDefinition;
import org.talend.components.salesforce.tsalesforcegetupdated.TSalesforceGetUpdatedDefinition;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputDefinition;
import org.talend.components.salesforce.tsalesforceoutput.TSalesforceOutputDefinition;
import org.talend.components.salesforce.tsalesforceoutputbulk.TSalesforceOutputBulkDefinition;
import org.talend.components.salesforce.tsalesforceoutputbulkexec.TSalesforceOutputBulkExecDefinition;
import org.talend.daikon.definition.Definition;
import org.talend.daikon.i18n.tag.TagImpl;

import com.google.common.collect.Lists;

/**
 *
 */
public class SalesforceFamilyDefinitionTest {

    private SalesforceFamilyDefinition familyDefinition = new SalesforceFamilyDefinition();

    private ComponentInstaller.ComponentFrameworkContext frameworkContext =
            Mockito.mock(ComponentInstaller.ComponentFrameworkContext.class);

    @Test
    public void testName() {
        assertEquals("Salesforce", familyDefinition.getName());
    }

    @Test
    public void testDefinitions() {
        List<Matcher> matchers = Arrays.<Matcher>asList(
                Matchers.isA(TSalesforceConnectionDefinition.class),
                Matchers.isA(SalesforceConnectionWizardDefinition.class),
                Matchers.isA(SalesforceModuleWizardDefinition.class),
                Matchers.isA(TSalesforceInputDefinition.class),
                Matchers.isA(TSalesforceOutputDefinition.class),
                Matchers.isA(TSalesforceOutputBulkDefinition.class),
                Matchers.isA(TSalesforceBulkExecDefinition.class),
                Matchers.isA(TSalesforceOutputBulkExecDefinition.class),
                Matchers.isA(TSalesforceGetServerTimestampDefinition.class),
                Matchers.isA(TSalesforceGetUpdatedDefinition.class),
                Matchers.isA(TSalesforceGetDeletedDefinition.class),
                Matchers.isA(SalesforceDatastoreDefinition.class),
                Matchers.isA(SalesforceDatasetDefinition.class),
                Matchers.isA(SalesforceInputDefinition.class)
        );
        List<? extends Definition> definitions = Lists.newArrayList(familyDefinition.getDefinitions());
        assertEquals(matchers.size(), definitions.size());
        for (Matcher m : matchers) {
            assertThat(definitions, hasItem(m));
        }
    }

    @Test
    public void testTags() {
        List<? extends Definition> definitions = Lists.newArrayList(familyDefinition.getDefinitions());
        for (Definition definition : definitions) {
            if (definition instanceof SalesforceDefinition) {
                SalesforceDefinition d = (SalesforceDefinition) definition;
                assertThat(d.doGetTags(), containsInAnyOrder(
                        new TagImpl("salesforce", CommonTags.CLOUD_TAG),
                        new TagImpl("salesforce", CommonTags.BUSINESS_TAG)
                ));
            }
        }
    }

    @Test
    public void testFamilies() {
        List<? extends Definition> definitions = Lists.newArrayList(familyDefinition.getDefinitions());
        for (Definition definition : definitions) {
            if (definition instanceof SalesforceDefinition) {
                SalesforceDefinition d = (SalesforceDefinition) definition;
                assertThat(d.getFamilies(), arrayContainingInAnyOrder("Business/Salesforce", "Cloud/Salesforce"));
            }
        }
    }

    @Test
    public void testInstall() {
        familyDefinition.install(frameworkContext);
        Mockito.verify(frameworkContext, times(1))
                .registerComponentFamilyDefinition(any(SalesforceFamilyDefinition.class));
    }

}
