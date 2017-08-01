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
package org.talend.components.marketo.tmarketoinput;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.talend.components.marketo.wizard.MarketoComponentWizardBaseProperties.InputOperation.CustomObject;
import static org.talend.components.marketo.wizard.MarketoComponentWizardBaseProperties.InputOperation.getLead;
import static org.talend.components.marketo.wizard.MarketoComponentWizardBaseProperties.InputOperation.getLeadActivity;
import static org.talend.components.marketo.wizard.MarketoComponentWizardBaseProperties.InputOperation.getLeadChanges;
import static org.talend.components.marketo.wizard.MarketoComponentWizardBaseProperties.InputOperation.getMultipleLeads;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.marketo.MarketoConstants;
import org.talend.components.marketo.MarketoTestBase;
import org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties.APIMode;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.IncludeExcludeFieldsREST;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.IncludeExcludeFieldsSOAP;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.LeadKeyTypeREST;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.LeadKeyTypeSOAP;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.LeadSelector;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.ListParam;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.StandardAction;
import org.talend.components.marketo.wizard.MarketoComponentWizardBaseProperties;
import org.talend.components.marketo.wizard.MarketoComponentWizardBaseProperties.CustomObjectAction;
import org.talend.components.marketo.wizard.MarketoComponentWizardBaseProperties.InputOperation;
import org.talend.daikon.properties.ValidationResult.Result;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.serialize.PostDeserializeSetup;

public class TMarketoInputPropertiesTest extends MarketoTestBase {

    private TMarketoInputProperties props;

    @Before
    public final void setup() {
        props = new TMarketoInputProperties("test");
        props.connection.setupProperties();
        props.schemaInput.setupProperties();
        props.setupProperties();
        props.connection.setupLayout();
        props.schemaInput.setupLayout();
        props.setupLayout();
        props.refreshLayout(props.getForm(Form.MAIN));
    }

    @Test
    public void testRefreshLayout() throws Exception {
        String tl_operation = props.inputOperation.getName();
        String tl_mappingInput = props.mappingInput.getName();
        String tl_leadSelectorSOAP = props.leadSelectorSOAP.getName();
        String tl_leadSelectorREST = props.leadSelectorREST.getName();
        String tl_leadKeyTypeSOAP = props.leadKeyTypeSOAP.getName();
        String tl_leadKeyTypeREST = props.leadKeyTypeREST.getName();
        String tl_leadKeyValue = props.leadKeyValue.getName();
        String tl_leadKeyValues = props.leadKeyValues.getName();
        String tl_listParam = props.listParam.getName();
        String tl_listParamValue = props.listParamListName.getName();
        String tl_listParamListId = props.listParamListId.getName();
        String tl_oldestUpdateDate = props.oldestUpdateDate.getName();
        String tl_latestUpdateDate = props.latestUpdateDate.getName();
        String tl_setIncludeTypes = props.setIncludeTypes.getName();
        String tl_setExcludeTypes = props.setExcludeTypes.getName();
        String tl_includeTypes = props.includeTypes.getName();
        String tl_excludeTypes = props.excludeTypes.getName();
        String tl_fieldList = props.fieldList.getName();
        String tl_sinceDateTime = props.sinceDateTime.getName();
        String tl_oldestCreateDate = props.oldestCreateDate.getName();
        String tl_latestCreateDate = props.latestCreateDate.getName();
        String tl_batchSize = props.batchSize.getName();
        String tl_timeout = props.connection.timeout.getName();
        String tl_dieOnError = props.dieOnError.getName();
        // REST API Mode - getLead
        props.refreshLayout(props.getForm(Form.MAIN));
        Form f = props.getForm(Form.MAIN);
        assertTrue(f.getWidget(tl_operation).isVisible());
        assertTrue(f.getWidget(tl_mappingInput).isVisible());
        assertTrue(f.getWidget(tl_dieOnError).isVisible());
        //
        assertTrue(f.getWidget(tl_leadKeyTypeREST).isVisible());
        assertTrue(f.getWidget(tl_leadKeyValue).isVisible());
        //
        assertFalse(f.getWidget(tl_leadSelectorREST).isVisible());
        assertFalse(f.getWidget(tl_leadSelectorSOAP).isVisible());
        assertFalse(f.getWidget(tl_leadKeyTypeSOAP).isVisible());
        assertFalse(f.getWidget(tl_leadKeyValues).isVisible());
        assertFalse(f.getWidget(tl_listParam).isVisible());
        assertFalse(f.getWidget(tl_listParamValue).isVisible());
        assertFalse(f.getWidget(tl_oldestUpdateDate).isVisible());
        assertFalse(f.getWidget(tl_latestUpdateDate).isVisible());
        assertFalse(f.getWidget(tl_setIncludeTypes).isVisible());
        assertFalse(f.getWidget(tl_setExcludeTypes).isVisible());
        assertFalse(f.getWidget(tl_includeTypes).isVisible());
        assertFalse(f.getWidget(tl_excludeTypes).isVisible());
        assertFalse(f.getWidget(tl_fieldList).isVisible());
        assertFalse(f.getWidget(tl_sinceDateTime).isVisible());
        assertFalse(f.getWidget(tl_oldestCreateDate).isVisible());
        assertFalse(f.getWidget(tl_latestCreateDate).isVisible());
        assertFalse(f.getWidget(tl_batchSize).isVisible());
        // REST API Mode - getMultipleLeads - LeadKey
        props.inputOperation.setValue(getMultipleLeads);
        props.refreshLayout(props.getForm(Form.MAIN));
        assertTrue(f.getWidget(tl_operation).isVisible());
        assertTrue(f.getWidget(tl_mappingInput).isVisible());
        assertTrue(f.getWidget(tl_dieOnError).isVisible());
        //
        assertTrue(f.getWidget(tl_leadKeyTypeREST).isVisible());
        assertTrue(f.getWidget(tl_leadSelectorREST).isVisible());
        assertTrue(f.getWidget(tl_leadKeyValues).isVisible());
        //
        assertFalse(f.getWidget(tl_leadSelectorSOAP).isVisible());
        assertFalse(f.getWidget(tl_leadKeyTypeSOAP).isVisible());
        assertFalse(f.getWidget(tl_leadKeyValue).isVisible());
        assertFalse(f.getWidget(tl_listParam).isVisible());
        assertFalse(f.getWidget(tl_listParamValue).isVisible());
        assertFalse(f.getWidget(tl_oldestUpdateDate).isVisible());
        assertFalse(f.getWidget(tl_latestUpdateDate).isVisible());
        assertFalse(f.getWidget(tl_setIncludeTypes).isVisible());
        assertFalse(f.getWidget(tl_setExcludeTypes).isVisible());
        assertFalse(f.getWidget(tl_includeTypes).isVisible());
        assertFalse(f.getWidget(tl_excludeTypes).isVisible());
        assertFalse(f.getWidget(tl_fieldList).isVisible());
        assertFalse(f.getWidget(tl_sinceDateTime).isVisible());
        assertFalse(f.getWidget(tl_oldestCreateDate).isVisible());
        assertFalse(f.getWidget(tl_latestCreateDate).isVisible());
        assertFalse(f.getWidget(tl_batchSize).isVisible());
        // REST API Mode - getMultipleLeads - StaticList
        props.inputOperation.setValue(getMultipleLeads);
        props.leadSelectorREST.setValue(LeadSelector.StaticListSelector);
        props.listParam.setValue(ListParam.STATIC_LIST_NAME);
        props.afterListParam();
        props.refreshLayout(props.getForm(Form.MAIN));
        assertTrue(f.getWidget(tl_operation).isVisible());
        assertTrue(f.getWidget(tl_mappingInput).isVisible());
        assertTrue(f.getWidget(tl_dieOnError).isVisible());
        assertTrue(f.getWidget(tl_leadSelectorREST).isVisible());
        assertTrue(f.getWidget(tl_listParam).isVisible());
        assertTrue(f.getWidget(tl_listParamValue).isVisible());
        assertFalse(f.getWidget(tl_listParamListId).isVisible());
        //
        props.leadSelectorREST.setValue(LeadSelector.StaticListSelector);
        props.listParam.setValue(ListParam.STATIC_LIST_ID);
        props.refreshLayout(props.getForm(Form.MAIN));
        assertTrue(f.getWidget(tl_leadSelectorREST).isVisible());
        assertTrue(f.getWidget(tl_listParam).isVisible());
        assertFalse(f.getWidget(tl_listParamValue).isVisible());
        assertTrue(f.getWidget(tl_listParamListId).isVisible());
        //
        assertFalse(f.getWidget(tl_leadKeyTypeREST).isVisible());
        assertFalse(f.getWidget(tl_leadSelectorSOAP).isVisible());
        assertFalse(f.getWidget(tl_leadKeyTypeSOAP).isVisible());
        assertFalse(f.getWidget(tl_leadKeyValue).isVisible());
        assertFalse(f.getWidget(tl_leadKeyValues).isVisible());
        assertFalse(f.getWidget(tl_oldestUpdateDate).isVisible());
        assertFalse(f.getWidget(tl_latestUpdateDate).isVisible());
        assertFalse(f.getWidget(tl_setIncludeTypes).isVisible());
        assertFalse(f.getWidget(tl_setExcludeTypes).isVisible());
        assertFalse(f.getWidget(tl_includeTypes).isVisible());
        assertFalse(f.getWidget(tl_excludeTypes).isVisible());
        assertFalse(f.getWidget(tl_fieldList).isVisible());
        assertFalse(f.getWidget(tl_sinceDateTime).isVisible());
        assertFalse(f.getWidget(tl_oldestCreateDate).isVisible());
        assertFalse(f.getWidget(tl_latestCreateDate).isVisible());
        assertTrue(f.getWidget(tl_batchSize).isVisible());
        // REST API Mode - getLeadActivity
        props.inputOperation.setValue(getLeadActivity);
        props.refreshLayout(props.getForm(Form.MAIN));
        assertTrue(f.getWidget(tl_operation).isVisible());
        assertTrue(f.getWidget(tl_mappingInput).isVisible());
        assertTrue(f.getWidget(tl_dieOnError).isVisible());
        //
        assertTrue(f.getWidget(tl_setIncludeTypes).isVisible());
        assertTrue(f.getWidget(tl_setExcludeTypes).isVisible());
        assertTrue(f.getWidget(tl_sinceDateTime).isVisible());
        assertTrue(f.getWidget(tl_batchSize).isVisible());
        //
        assertFalse(f.getWidget(tl_leadKeyTypeREST).isVisible());
        assertFalse(f.getWidget(tl_leadSelectorREST).isVisible());
        assertFalse(f.getWidget(tl_leadKeyValues).isVisible());
        assertFalse(f.getWidget(tl_leadSelectorSOAP).isVisible());
        assertFalse(f.getWidget(tl_leadKeyTypeSOAP).isVisible());
        assertFalse(f.getWidget(tl_leadKeyValue).isVisible());
        assertFalse(f.getWidget(tl_listParam).isVisible());
        assertFalse(f.getWidget(tl_listParamValue).isVisible());
        assertFalse(f.getWidget(tl_oldestUpdateDate).isVisible());
        assertFalse(f.getWidget(tl_latestUpdateDate).isVisible());
        assertFalse(f.getWidget(tl_includeTypes).isVisible());
        assertFalse(f.getWidget(tl_excludeTypes).isVisible());
        assertFalse(f.getWidget(tl_fieldList).isVisible());
        assertFalse(f.getWidget(tl_oldestCreateDate).isVisible());
        assertFalse(f.getWidget(tl_latestCreateDate).isVisible());
        // REST API Mode - getLeadChanges
        props.inputOperation.setValue(getLeadChanges);
        props.refreshLayout(props.getForm(Form.MAIN));
        assertTrue(f.getWidget(tl_operation).isVisible());
        assertTrue(f.getWidget(tl_mappingInput).isVisible());
        assertTrue(f.getWidget(tl_dieOnError).isVisible());
        //
        assertTrue(f.getWidget(tl_fieldList).isVisible());
        assertTrue(f.getWidget(tl_sinceDateTime).isVisible());
        assertTrue(f.getWidget(tl_batchSize).isVisible());
        //
        assertFalse(f.getWidget(tl_leadKeyTypeREST).isVisible());
        assertFalse(f.getWidget(tl_leadSelectorREST).isVisible());
        assertFalse(f.getWidget(tl_leadKeyValues).isVisible());
        assertFalse(f.getWidget(tl_leadSelectorSOAP).isVisible());
        assertFalse(f.getWidget(tl_leadKeyTypeSOAP).isVisible());
        assertFalse(f.getWidget(tl_leadKeyValue).isVisible());
        assertFalse(f.getWidget(tl_listParam).isVisible());
        assertFalse(f.getWidget(tl_listParamValue).isVisible());
        assertFalse(f.getWidget(tl_setIncludeTypes).isVisible());
        assertFalse(f.getWidget(tl_setExcludeTypes).isVisible());
        assertFalse(f.getWidget(tl_oldestUpdateDate).isVisible());
        assertFalse(f.getWidget(tl_latestUpdateDate).isVisible());
        assertFalse(f.getWidget(tl_includeTypes).isVisible());
        assertFalse(f.getWidget(tl_excludeTypes).isVisible());
        assertFalse(f.getWidget(tl_oldestCreateDate).isVisible());
        assertFalse(f.getWidget(tl_latestCreateDate).isVisible());
        //
        // SOAP
        //
        // SOAP API Mode - getLead
        props.connection.apiMode.setValue(APIMode.SOAP);
        props.inputOperation.setValue(getLead);
        props.refreshLayout(props.getForm(Form.MAIN));
        assertTrue(f.getWidget(tl_operation).isVisible());
        assertTrue(f.getWidget(tl_mappingInput).isVisible());
        assertTrue(f.getWidget(tl_dieOnError).isVisible());
        //
        assertTrue(f.getWidget(tl_leadKeyTypeSOAP).isVisible());
        assertTrue(f.getWidget(tl_leadKeyValue).isVisible());
        //
        assertFalse(f.getWidget(tl_leadSelectorSOAP).isVisible());
        assertFalse(f.getWidget(tl_leadKeyTypeREST).isVisible());
        assertFalse(f.getWidget(tl_leadSelectorREST).isVisible());
        assertFalse(f.getWidget(tl_leadKeyValues).isVisible());
        assertFalse(f.getWidget(tl_listParam).isVisible());
        assertFalse(f.getWidget(tl_listParamValue).isVisible());
        assertFalse(f.getWidget(tl_oldestUpdateDate).isVisible());
        assertFalse(f.getWidget(tl_latestUpdateDate).isVisible());
        assertFalse(f.getWidget(tl_setIncludeTypes).isVisible());
        assertFalse(f.getWidget(tl_setExcludeTypes).isVisible());
        assertFalse(f.getWidget(tl_includeTypes).isVisible());
        assertFalse(f.getWidget(tl_excludeTypes).isVisible());
        assertFalse(f.getWidget(tl_fieldList).isVisible());
        assertFalse(f.getWidget(tl_sinceDateTime).isVisible());
        assertFalse(f.getWidget(tl_oldestCreateDate).isVisible());
        assertFalse(f.getWidget(tl_latestCreateDate).isVisible());
        assertFalse(f.getWidget(tl_batchSize).isVisible());
        // SOAP API Mode - getMultipleLeads - LeadKey
        props.inputOperation.setValue(getMultipleLeads);
        props.refreshLayout(props.getForm(Form.MAIN));
        assertTrue(f.getWidget(tl_operation).isVisible());
        assertTrue(f.getWidget(tl_mappingInput).isVisible());
        assertTrue(f.getWidget(tl_dieOnError).isVisible());
        //
        assertTrue(f.getWidget(tl_leadKeyTypeSOAP).isVisible());
        assertTrue(f.getWidget(tl_leadSelectorSOAP).isVisible());
        assertTrue(f.getWidget(tl_leadKeyValues).isVisible());
        //
        assertFalse(f.getWidget(tl_leadSelectorREST).isVisible());
        assertFalse(f.getWidget(tl_leadKeyTypeREST).isVisible());
        assertFalse(f.getWidget(tl_leadKeyValue).isVisible());
        assertFalse(f.getWidget(tl_listParam).isVisible());
        assertFalse(f.getWidget(tl_listParamValue).isVisible());
        assertFalse(f.getWidget(tl_oldestUpdateDate).isVisible());
        assertFalse(f.getWidget(tl_latestUpdateDate).isVisible());
        assertFalse(f.getWidget(tl_setIncludeTypes).isVisible());
        assertFalse(f.getWidget(tl_setExcludeTypes).isVisible());
        assertFalse(f.getWidget(tl_includeTypes).isVisible());
        assertFalse(f.getWidget(tl_excludeTypes).isVisible());
        assertFalse(f.getWidget(tl_fieldList).isVisible());
        assertFalse(f.getWidget(tl_sinceDateTime).isVisible());
        assertFalse(f.getWidget(tl_oldestCreateDate).isVisible());
        assertFalse(f.getWidget(tl_latestCreateDate).isVisible());
        assertFalse(f.getWidget(tl_batchSize).isVisible());
        // SOAP API Mode - getMultipleLeads - StaticList
        props.inputOperation.setValue(getMultipleLeads);
        props.leadSelectorSOAP.setValue(LeadSelector.StaticListSelector);
        props.listParam.setValue(ListParam.STATIC_LIST_NAME);
        props.refreshLayout(props.getForm(Form.MAIN));
        assertTrue(f.getWidget(tl_operation).isVisible());
        assertTrue(f.getWidget(tl_mappingInput).isVisible());
        assertTrue(f.getWidget(tl_dieOnError).isVisible());
        //
        assertTrue(f.getWidget(tl_leadSelectorSOAP).isVisible());
        assertTrue(f.getWidget(tl_listParam).isVisible());
        assertTrue(f.getWidget(tl_listParamValue).isVisible());
        assertFalse(f.getWidget(tl_listParamListId).isVisible());
        //
        props.listParam.setValue(ListParam.STATIC_LIST_ID);
        props.refreshLayout(props.getForm(Form.MAIN));
        assertTrue(f.getWidget(tl_leadSelectorSOAP).isVisible());
        assertTrue(f.getWidget(tl_listParam).isVisible());
        assertFalse(f.getWidget(tl_listParamValue).isVisible());
        assertTrue(f.getWidget(tl_listParamListId).isVisible());
        //
        assertFalse(f.getWidget(tl_leadKeyTypeREST).isVisible());
        assertFalse(f.getWidget(tl_leadSelectorREST).isVisible());
        assertFalse(f.getWidget(tl_leadKeyTypeSOAP).isVisible());
        assertFalse(f.getWidget(tl_leadKeyValue).isVisible());
        assertFalse(f.getWidget(tl_leadKeyValues).isVisible());
        assertFalse(f.getWidget(tl_oldestUpdateDate).isVisible());
        assertFalse(f.getWidget(tl_latestUpdateDate).isVisible());
        assertFalse(f.getWidget(tl_setIncludeTypes).isVisible());
        assertFalse(f.getWidget(tl_setExcludeTypes).isVisible());
        assertFalse(f.getWidget(tl_includeTypes).isVisible());
        assertFalse(f.getWidget(tl_excludeTypes).isVisible());
        assertFalse(f.getWidget(tl_fieldList).isVisible());
        assertFalse(f.getWidget(tl_sinceDateTime).isVisible());
        assertFalse(f.getWidget(tl_oldestCreateDate).isVisible());
        assertFalse(f.getWidget(tl_latestCreateDate).isVisible());
        assertTrue(f.getWidget(tl_batchSize).isVisible());
        // SOAP API Mode - getMultipleLeads - LastUpdate
        props.inputOperation.setValue(getMultipleLeads);
        props.leadSelectorSOAP.setValue(LeadSelector.LastUpdateAtSelector);
        props.refreshLayout(props.getForm(Form.MAIN));
        assertTrue(f.getWidget(tl_operation).isVisible());
        assertTrue(f.getWidget(tl_mappingInput).isVisible());
        assertTrue(f.getWidget(tl_dieOnError).isVisible());
        //
        assertTrue(f.getWidget(tl_leadSelectorSOAP).isVisible());
        assertTrue(f.getWidget(tl_oldestUpdateDate).isVisible());
        assertTrue(f.getWidget(tl_latestUpdateDate).isVisible());
        //
        assertFalse(f.getWidget(tl_listParam).isVisible());
        assertFalse(f.getWidget(tl_listParamValue).isVisible());
        assertFalse(f.getWidget(tl_leadKeyTypeREST).isVisible());
        assertFalse(f.getWidget(tl_leadSelectorREST).isVisible());
        assertFalse(f.getWidget(tl_leadKeyTypeSOAP).isVisible());
        assertFalse(f.getWidget(tl_leadKeyValue).isVisible());
        assertFalse(f.getWidget(tl_leadKeyValues).isVisible());
        assertFalse(f.getWidget(tl_setIncludeTypes).isVisible());
        assertFalse(f.getWidget(tl_setExcludeTypes).isVisible());
        assertFalse(f.getWidget(tl_includeTypes).isVisible());
        assertFalse(f.getWidget(tl_excludeTypes).isVisible());
        assertFalse(f.getWidget(tl_fieldList).isVisible());
        assertFalse(f.getWidget(tl_sinceDateTime).isVisible());
        assertFalse(f.getWidget(tl_oldestCreateDate).isVisible());
        assertFalse(f.getWidget(tl_latestCreateDate).isVisible());
        assertTrue(f.getWidget(tl_batchSize).isVisible());

        // SOAP API Mode - getLeadActivity
        props.inputOperation.setValue(getLeadActivity);
        props.refreshLayout(props.getForm(Form.MAIN));
        assertTrue(f.getWidget(tl_operation).isVisible());
        assertTrue(f.getWidget(tl_mappingInput).isVisible());
        assertTrue(f.getWidget(tl_dieOnError).isVisible());
        //
        assertTrue(f.getWidget(tl_setIncludeTypes).isVisible());
        assertTrue(f.getWidget(tl_setExcludeTypes).isVisible());
        assertTrue(f.getWidget(tl_batchSize).isVisible());
        assertTrue(f.getWidget(tl_leadKeyTypeSOAP).isVisible());
        assertTrue(f.getWidget(tl_leadKeyValue).isVisible());
        //
        assertFalse(f.getWidget(tl_sinceDateTime).isVisible());
        assertFalse(f.getWidget(tl_leadKeyTypeREST).isVisible());
        assertFalse(f.getWidget(tl_leadSelectorREST).isVisible());
        assertFalse(f.getWidget(tl_leadKeyValues).isVisible());
        assertFalse(f.getWidget(tl_leadSelectorSOAP).isVisible());
        assertFalse(f.getWidget(tl_listParam).isVisible());
        assertFalse(f.getWidget(tl_listParamValue).isVisible());
        assertFalse(f.getWidget(tl_oldestUpdateDate).isVisible());
        assertFalse(f.getWidget(tl_latestUpdateDate).isVisible());
        assertFalse(f.getWidget(tl_includeTypes).isVisible());
        assertFalse(f.getWidget(tl_excludeTypes).isVisible());
        assertFalse(f.getWidget(tl_fieldList).isVisible());
        assertFalse(f.getWidget(tl_oldestCreateDate).isVisible());
        assertFalse(f.getWidget(tl_latestCreateDate).isVisible());
        // SOAP API Mode - getLeadChanges
        props.inputOperation.setValue(getLeadChanges);
        props.refreshLayout(props.getForm(Form.MAIN));
        assertTrue(f.getWidget(tl_operation).isVisible());
        assertTrue(f.getWidget(tl_mappingInput).isVisible());
        assertTrue(f.getWidget(tl_dieOnError).isVisible());
        //
        assertTrue(f.getWidget(tl_oldestCreateDate).isVisible());
        assertTrue(f.getWidget(tl_latestCreateDate).isVisible());
        assertTrue(f.getWidget(tl_setIncludeTypes).isVisible());
        assertTrue(f.getWidget(tl_setExcludeTypes).isVisible());
        assertTrue(f.getWidget(tl_batchSize).isVisible());
        //
        assertFalse(f.getWidget(tl_fieldList).isVisible());
        assertFalse(f.getWidget(tl_sinceDateTime).isVisible());
        assertFalse(f.getWidget(tl_leadKeyTypeREST).isVisible());
        assertFalse(f.getWidget(tl_leadSelectorREST).isVisible());
        assertFalse(f.getWidget(tl_leadKeyValues).isVisible());
        assertFalse(f.getWidget(tl_leadSelectorSOAP).isVisible());
        assertFalse(f.getWidget(tl_leadKeyTypeSOAP).isVisible());
        assertFalse(f.getWidget(tl_leadKeyValue).isVisible());
        assertFalse(f.getWidget(tl_listParam).isVisible());
        assertFalse(f.getWidget(tl_listParamValue).isVisible());
        assertFalse(f.getWidget(tl_includeTypes).isVisible());
        assertFalse(f.getWidget(tl_excludeTypes).isVisible());
        assertFalse(f.getWidget(tl_oldestUpdateDate).isVisible());
        assertFalse(f.getWidget(tl_latestUpdateDate).isVisible());
    }

    @Test
    public void testGetAllSchemaPropertiesConnectors() {
        assertEquals(Collections.singleton(props.MAIN_CONNECTOR), props.getAllSchemaPropertiesConnectors(true));
        assertEquals(Collections.singleton(props.FLOW_CONNECTOR), props.getAllSchemaPropertiesConnectors(false));
    }

    @Test
    public void testGetSchema() {
        props.afterInputOperation();
        assertEquals(MarketoConstants.getRESTSchemaForGetLeadOrGetMultipleLeads(), props.getSchema(props.MAIN_CONNECTOR, false));
        assertEquals(6, props.getSchemaFields().size());
    }

    @Test
    public void testUpdateSchemaRelated() throws Exception {
        props.inputOperation.setValue(getLead);
        props.updateSchemaRelated();
        props.schemaListener.afterSchema();
        assertEquals(MarketoConstants.getRESTSchemaForGetLeadOrGetMultipleLeads(), props.schemaInput.schema.getValue());
        props.inputOperation.setValue(getMultipleLeads);
        props.updateSchemaRelated();
        assertEquals(MarketoConstants.getRESTSchemaForGetLeadOrGetMultipleLeads(), props.schemaInput.schema.getValue());
        props.inputOperation.setValue(getLeadActivity);
        props.updateSchemaRelated();
        props.afterInputOperation();
        assertEquals(MarketoConstants.getRESTSchemaForGetLeadActivity(), props.schemaInput.schema.getValue());
        props.inputOperation.setValue(getLeadChanges);
        props.updateSchemaRelated();
        props.afterInputOperation();
        assertEquals(MarketoConstants.getRESTSchemaForGetLeadChanges(), props.schemaInput.schema.getValue());

        props.connection.apiMode.setValue(APIMode.SOAP);
        props.inputOperation.setValue(getLead);
        props.afterInputOperation();
        props.updateSchemaRelated();
        assertEquals(MarketoConstants.getSOAPSchemaForGetLeadOrGetMultipleLeads(), props.schemaInput.schema.getValue());
        props.inputOperation.setValue(getMultipleLeads);
        props.updateSchemaRelated();
        assertEquals(MarketoConstants.getSOAPSchemaForGetLeadOrGetMultipleLeads(), props.schemaInput.schema.getValue());
        props.inputOperation.setValue(getLeadActivity);
        props.updateSchemaRelated();
        props.afterInputOperation();
        assertEquals(MarketoConstants.getSOAPSchemaForGetLeadActivity(), props.schemaInput.schema.getValue());
        props.inputOperation.setValue(getLeadChanges);
        props.updateSchemaRelated();
        props.afterInputOperation();
        assertEquals(MarketoConstants.getSOAPSchemaForGetLeadChanges(), props.schemaInput.schema.getValue());
    }

    @Test
    public void testAfterOperationAndAPIMode() throws Exception {
        // REST
        props.afterInputOperation();
        assertEquals(MarketoConstants.getRESTSchemaForGetLeadOrGetMultipleLeads(), props.schemaInput.schema.getValue());
        props.inputOperation.setValue(getMultipleLeads);
        props.afterInputOperation();
        assertEquals(MarketoConstants.getRESTSchemaForGetLeadOrGetMultipleLeads(), props.schemaInput.schema.getValue());
        props.inputOperation.setValue(getLeadActivity);
        props.afterInputOperation();
        assertEquals(MarketoConstants.getRESTSchemaForGetLeadActivity(), props.schemaInput.schema.getValue());
        props.inputOperation.setValue(getLeadChanges);
        props.afterInputOperation();
        assertEquals(MarketoConstants.getRESTSchemaForGetLeadChanges(), props.schemaInput.schema.getValue());
        // SOAP
        props.connection.apiMode.setValue(APIMode.SOAP);
        props.inputOperation.setValue(getLead);
        props.afterInputOperation();
        assertEquals(MarketoConstants.getSOAPSchemaForGetLeadOrGetMultipleLeads(), props.schemaInput.schema.getValue());
        props.inputOperation.setValue(getMultipleLeads);
        props.afterInputOperation();
        assertEquals(MarketoConstants.getSOAPSchemaForGetLeadOrGetMultipleLeads(), props.schemaInput.schema.getValue());
        props.inputOperation.setValue(getLeadActivity);
        props.afterInputOperation();
        assertEquals(MarketoConstants.getSOAPSchemaForGetLeadActivity(), props.schemaInput.schema.getValue());
        props.inputOperation.setValue(getLeadChanges);
        props.afterInputOperation();
        assertEquals(MarketoConstants.getSOAPSchemaForGetLeadChanges(), props.schemaInput.schema.getValue());
        props.connection.apiMode.setValue(APIMode.SOAP);
    }

    @Test
    public void testAfterLeadSelectorSOAP() throws Exception {
        props.afterLeadSelectorSOAP();
        assertEquals(LeadSelector.LeadKeySelector, props.leadSelectorSOAP.getValue());
        props.afterLeadSelectorSOAP();
        props.leadSelectorSOAP.setValue(LeadSelector.LastUpdateAtSelector);
        assertEquals(LeadSelector.LastUpdateAtSelector, props.leadSelectorSOAP.getValue());
    }

    @Test
    public void testAfterLeadSelectorREST() throws Exception {
        assertEquals(LeadSelector.LeadKeySelector, props.leadSelectorREST.getValue());
        props.afterLeadSelectorREST();
        props.leadSelectorREST.setValue(LeadSelector.LastUpdateAtSelector);
        assertEquals(LeadSelector.LastUpdateAtSelector, props.leadSelectorREST.getValue());
    }

    @Test
    public void testAfterSetIncludeTypes() throws Exception {
        props.inputOperation.setValue(getLeadActivity);
        assertFalse(props.setIncludeTypes.getValue());
        assertFalse(props.getForm(Form.MAIN).getWidget(props.includeTypes.getName()).isVisible());
        props.setIncludeTypes.setValue(true);
        props.afterSetIncludeTypes();
        assertTrue(props.setIncludeTypes.getValue());
        assertTrue(props.getForm(Form.MAIN).getWidget(props.includeTypes.getName()).isVisible());
    }

    @Test
    public void testAfterSetExcludeTypes() throws Exception {
        props.inputOperation.setValue(getLeadActivity);
        assertFalse(props.setExcludeTypes.getValue());
        assertFalse(props.getForm(Form.MAIN).getWidget(props.excludeTypes.getName()).isVisible());
        props.setExcludeTypes.setValue(true);
        props.afterSetExcludeTypes();
        assertTrue(props.setExcludeTypes.getValue());
        assertTrue(props.getForm(Form.MAIN).getWidget(props.excludeTypes.getName()).isVisible());
    }

    @Test
    public final void testEnums() {
        // inoputOperation
        assertEquals(getLead, InputOperation.valueOf("getLead"));
        assertEquals(getLeadActivity, InputOperation.valueOf("getLeadActivity"));
        assertEquals(getLeadChanges, InputOperation.valueOf("getLeadChanges"));
        assertEquals(getMultipleLeads, InputOperation.valueOf("getMultipleLeads"));
        // LeadSelector
        assertEquals(LeadSelector.LeadKeySelector, LeadSelector.valueOf("LeadKeySelector"));
        assertEquals(LeadSelector.StaticListSelector, LeadSelector.valueOf("StaticListSelector"));
        assertEquals(LeadSelector.LastUpdateAtSelector, LeadSelector.valueOf("LastUpdateAtSelector"));
        // IncludeExcludeFieldsSOAP
        assertEquals(IncludeExcludeFieldsSOAP.VisitWebpage, IncludeExcludeFieldsSOAP.valueOf("VisitWebpage"));
        assertEquals(IncludeExcludeFieldsSOAP.FillOutForm, IncludeExcludeFieldsSOAP.valueOf("FillOutForm"));
        assertEquals(IncludeExcludeFieldsSOAP.ClickLink, IncludeExcludeFieldsSOAP.valueOf("ClickLink"));
        assertEquals(IncludeExcludeFieldsSOAP.RegisterForEvent, IncludeExcludeFieldsSOAP.valueOf("RegisterForEvent"));
        assertEquals(IncludeExcludeFieldsSOAP.AttendEvent, IncludeExcludeFieldsSOAP.valueOf("AttendEvent"));
        assertEquals(IncludeExcludeFieldsSOAP.SendEmail, IncludeExcludeFieldsSOAP.valueOf("SendEmail"));
        assertEquals(IncludeExcludeFieldsSOAP.EmailDelivered, IncludeExcludeFieldsSOAP.valueOf("EmailDelivered"));
        assertEquals(IncludeExcludeFieldsSOAP.EmailBounced, IncludeExcludeFieldsSOAP.valueOf("EmailBounced"));
        assertEquals(IncludeExcludeFieldsSOAP.UnsubscribeEmail, IncludeExcludeFieldsSOAP.valueOf("UnsubscribeEmail"));
        assertEquals(IncludeExcludeFieldsSOAP.OpenEmail, IncludeExcludeFieldsSOAP.valueOf("OpenEmail"));
        assertEquals(IncludeExcludeFieldsSOAP.ClickEmail, IncludeExcludeFieldsSOAP.valueOf("ClickEmail"));
        assertEquals(IncludeExcludeFieldsSOAP.NewLead, IncludeExcludeFieldsSOAP.valueOf("NewLead"));
        assertEquals(IncludeExcludeFieldsSOAP.ChangeDataValue, IncludeExcludeFieldsSOAP.valueOf("ChangeDataValue"));
        assertEquals(IncludeExcludeFieldsSOAP.LeadAssigned, IncludeExcludeFieldsSOAP.valueOf("LeadAssigned"));
        assertEquals(IncludeExcludeFieldsSOAP.NewSFDCOpprtnty, IncludeExcludeFieldsSOAP.valueOf("NewSFDCOpprtnty"));
        assertEquals(IncludeExcludeFieldsSOAP.Wait, IncludeExcludeFieldsSOAP.valueOf("Wait"));
        assertEquals(IncludeExcludeFieldsSOAP.RunSubflow, IncludeExcludeFieldsSOAP.valueOf("RunSubflow"));
        assertEquals(IncludeExcludeFieldsSOAP.RemoveFromFlow, IncludeExcludeFieldsSOAP.valueOf("RemoveFromFlow"));
        assertEquals(IncludeExcludeFieldsSOAP.PushLeadToSales, IncludeExcludeFieldsSOAP.valueOf("PushLeadToSales"));
        assertEquals(IncludeExcludeFieldsSOAP.CreateTask, IncludeExcludeFieldsSOAP.valueOf("CreateTask"));
        assertEquals(IncludeExcludeFieldsSOAP.ConvertLead, IncludeExcludeFieldsSOAP.valueOf("ConvertLead"));
        assertEquals(IncludeExcludeFieldsSOAP.ChangeScore, IncludeExcludeFieldsSOAP.valueOf("ChangeScore"));
        assertEquals(IncludeExcludeFieldsSOAP.ChangeOwner, IncludeExcludeFieldsSOAP.valueOf("ChangeOwner"));
        assertEquals(IncludeExcludeFieldsSOAP.AddToList, IncludeExcludeFieldsSOAP.valueOf("AddToList"));
        assertEquals(IncludeExcludeFieldsSOAP.RemoveFromList, IncludeExcludeFieldsSOAP.valueOf("RemoveFromList"));
        assertEquals(IncludeExcludeFieldsSOAP.SFDCActivity, IncludeExcludeFieldsSOAP.valueOf("SFDCActivity"));
        assertEquals(IncludeExcludeFieldsSOAP.EmailBouncedSoft, IncludeExcludeFieldsSOAP.valueOf("EmailBouncedSoft"));
        assertEquals(IncludeExcludeFieldsSOAP.PushLeadUpdatesToSales, IncludeExcludeFieldsSOAP.valueOf("PushLeadUpdatesToSales"));
        assertEquals(IncludeExcludeFieldsSOAP.DeleteLeadFromSales, IncludeExcludeFieldsSOAP.valueOf("DeleteLeadFromSales"));
        assertEquals(IncludeExcludeFieldsSOAP.SFDCActivityUpdated, IncludeExcludeFieldsSOAP.valueOf("SFDCActivityUpdated"));
        assertEquals(IncludeExcludeFieldsSOAP.SFDCMergeLeads, IncludeExcludeFieldsSOAP.valueOf("SFDCMergeLeads"));
        assertEquals(IncludeExcludeFieldsSOAP.MergeLeads, IncludeExcludeFieldsSOAP.valueOf("MergeLeads"));
        assertEquals(IncludeExcludeFieldsSOAP.ResolveConflicts, IncludeExcludeFieldsSOAP.valueOf("ResolveConflicts"));
        assertEquals(IncludeExcludeFieldsSOAP.AssocWithOpprtntyInSales,
                IncludeExcludeFieldsSOAP.valueOf("AssocWithOpprtntyInSales"));
        assertEquals(IncludeExcludeFieldsSOAP.DissocFromOpprtntyInSales,
                IncludeExcludeFieldsSOAP.valueOf("DissocFromOpprtntyInSales"));
        assertEquals(IncludeExcludeFieldsSOAP.UpdateOpprtntyInSales, IncludeExcludeFieldsSOAP.valueOf("UpdateOpprtntyInSales"));
        assertEquals(IncludeExcludeFieldsSOAP.DeleteLead, IncludeExcludeFieldsSOAP.valueOf("DeleteLead"));
        assertEquals(IncludeExcludeFieldsSOAP.SendAlert, IncludeExcludeFieldsSOAP.valueOf("SendAlert"));
        assertEquals(IncludeExcludeFieldsSOAP.SendSalesEmail, IncludeExcludeFieldsSOAP.valueOf("SendSalesEmail"));
        assertEquals(IncludeExcludeFieldsSOAP.OpenSalesEmail, IncludeExcludeFieldsSOAP.valueOf("OpenSalesEmail"));
        assertEquals(IncludeExcludeFieldsSOAP.ClickSalesEmail, IncludeExcludeFieldsSOAP.valueOf("ClickSalesEmail"));
        assertEquals(IncludeExcludeFieldsSOAP.AddtoSFDCCampaign, IncludeExcludeFieldsSOAP.valueOf("AddtoSFDCCampaign"));
        assertEquals(IncludeExcludeFieldsSOAP.RemoveFromSFDCCampaign, IncludeExcludeFieldsSOAP.valueOf("RemoveFromSFDCCampaign"));
        assertEquals(IncludeExcludeFieldsSOAP.ChangeStatusInSFDCCampaign,
                IncludeExcludeFieldsSOAP.valueOf("ChangeStatusInSFDCCampaign"));
        assertEquals(IncludeExcludeFieldsSOAP.ReceiveSalesEmail, IncludeExcludeFieldsSOAP.valueOf("ReceiveSalesEmail"));
        assertEquals(IncludeExcludeFieldsSOAP.InterestingMoment, IncludeExcludeFieldsSOAP.valueOf("InterestingMoment"));
        assertEquals(IncludeExcludeFieldsSOAP.RequestCampaign, IncludeExcludeFieldsSOAP.valueOf("RequestCampaign"));
        assertEquals(IncludeExcludeFieldsSOAP.SalesEmailBounced, IncludeExcludeFieldsSOAP.valueOf("SalesEmailBounced"));
        assertEquals(IncludeExcludeFieldsSOAP.ChangeLeadPartition, IncludeExcludeFieldsSOAP.valueOf("ChangeLeadPartition"));
        assertEquals(IncludeExcludeFieldsSOAP.ChangeRevenueStage, IncludeExcludeFieldsSOAP.valueOf("ChangeRevenueStage"));
        assertEquals(IncludeExcludeFieldsSOAP.ChangeRevenueStageManually,
                IncludeExcludeFieldsSOAP.valueOf("ChangeRevenueStageManually"));
        assertEquals(IncludeExcludeFieldsSOAP.ComputeDataValue, IncludeExcludeFieldsSOAP.valueOf("ComputeDataValue"));
        assertEquals(IncludeExcludeFieldsSOAP.ChangeStatusInProgression,
                IncludeExcludeFieldsSOAP.valueOf("ChangeStatusInProgression"));
        assertEquals(IncludeExcludeFieldsSOAP.ChangeFieldInProgram, IncludeExcludeFieldsSOAP.valueOf("ChangeFieldInProgram"));
        assertEquals(IncludeExcludeFieldsSOAP.EnrichWithDatacom, IncludeExcludeFieldsSOAP.valueOf("EnrichWithDatacom"));
        assertEquals(IncludeExcludeFieldsSOAP.ChangeSegment, IncludeExcludeFieldsSOAP.valueOf("ChangeSegment"));
        assertEquals(IncludeExcludeFieldsSOAP.ComputeSegmentation, IncludeExcludeFieldsSOAP.valueOf("ComputeSegmentation"));
        assertEquals(IncludeExcludeFieldsSOAP.ResolveRuleset, IncludeExcludeFieldsSOAP.valueOf("ResolveRuleset"));
        assertEquals(IncludeExcludeFieldsSOAP.SmartCampaignTest, IncludeExcludeFieldsSOAP.valueOf("SmartCampaignTest"));
        assertEquals(IncludeExcludeFieldsSOAP.SmartCampaignTestTrigger,
                IncludeExcludeFieldsSOAP.valueOf("SmartCampaignTestTrigger"));
        // IncludeExcludeFieldsREST
        assertEquals(IncludeExcludeFieldsREST.VisitWebpage, IncludeExcludeFieldsREST.valueOf("VisitWebpage"));
        assertEquals(IncludeExcludeFieldsREST.VisitWebpage, IncludeExcludeFieldsREST.valueOf(1));
        assertEquals(1, IncludeExcludeFieldsREST.VisitWebpage.fieldVal);
        assertEquals(IncludeExcludeFieldsREST.FillOutForm, IncludeExcludeFieldsREST.valueOf("FillOutForm"));
        assertEquals(IncludeExcludeFieldsREST.FillOutForm, IncludeExcludeFieldsREST.valueOf(2));
        assertEquals(2, IncludeExcludeFieldsREST.FillOutForm.fieldVal);
        assertEquals(IncludeExcludeFieldsREST.ClickLink, IncludeExcludeFieldsREST.valueOf("ClickLink"));
        assertEquals(IncludeExcludeFieldsREST.ClickLink, IncludeExcludeFieldsREST.valueOf(3));
        assertEquals(3, IncludeExcludeFieldsREST.ClickLink.fieldVal);
        assertEquals(IncludeExcludeFieldsREST.SendEmail, IncludeExcludeFieldsREST.valueOf("SendEmail"));
        assertEquals(IncludeExcludeFieldsREST.SendEmail, IncludeExcludeFieldsREST.valueOf(6));
        assertEquals(6, IncludeExcludeFieldsREST.SendEmail.fieldVal);
        assertEquals(IncludeExcludeFieldsREST.EmailDelivered, IncludeExcludeFieldsREST.valueOf("EmailDelivered"));
        assertEquals(IncludeExcludeFieldsREST.EmailDelivered, IncludeExcludeFieldsREST.valueOf(7));
        assertEquals(7, IncludeExcludeFieldsREST.EmailDelivered.fieldVal);
        assertEquals(IncludeExcludeFieldsREST.EmailBounced, IncludeExcludeFieldsREST.valueOf("EmailBounced"));
        assertEquals(IncludeExcludeFieldsREST.EmailBounced, IncludeExcludeFieldsREST.valueOf(8));
        assertEquals(8, IncludeExcludeFieldsREST.EmailBounced.fieldVal);
        assertEquals(IncludeExcludeFieldsREST.UnsubscribeEmail, IncludeExcludeFieldsREST.valueOf("UnsubscribeEmail"));
        assertEquals(IncludeExcludeFieldsREST.UnsubscribeEmail, IncludeExcludeFieldsREST.valueOf(9));
        assertEquals(9, IncludeExcludeFieldsREST.UnsubscribeEmail.fieldVal);
        assertEquals(IncludeExcludeFieldsREST.OpenEmail, IncludeExcludeFieldsREST.valueOf("OpenEmail"));
        assertEquals(IncludeExcludeFieldsREST.OpenEmail, IncludeExcludeFieldsREST.valueOf(10));
        assertEquals(10, IncludeExcludeFieldsREST.OpenEmail.fieldVal);
        assertEquals(IncludeExcludeFieldsREST.ClickEmail, IncludeExcludeFieldsREST.valueOf("ClickEmail"));
        assertEquals(IncludeExcludeFieldsREST.ClickEmail, IncludeExcludeFieldsREST.valueOf(11));
        assertEquals(11, IncludeExcludeFieldsREST.ClickEmail.fieldVal);
        assertEquals(IncludeExcludeFieldsREST.NewLead, IncludeExcludeFieldsREST.valueOf("NewLead"));
        assertEquals(IncludeExcludeFieldsREST.NewLead, IncludeExcludeFieldsREST.valueOf(12));
        assertEquals(12, IncludeExcludeFieldsREST.NewLead.fieldVal);
        assertEquals(IncludeExcludeFieldsREST.ChangeDataValue, IncludeExcludeFieldsREST.valueOf("ChangeDataValue"));
        assertEquals(IncludeExcludeFieldsREST.ChangeDataValue, IncludeExcludeFieldsREST.valueOf(13));
        assertEquals(13, IncludeExcludeFieldsREST.ChangeDataValue.fieldVal);
        assertEquals(IncludeExcludeFieldsREST.SyncLeadToSFDC, IncludeExcludeFieldsREST.valueOf("SyncLeadToSFDC"));
        assertEquals(IncludeExcludeFieldsREST.SyncLeadToSFDC, IncludeExcludeFieldsREST.valueOf(19));
        assertEquals(19, IncludeExcludeFieldsREST.SyncLeadToSFDC.fieldVal);
        assertEquals(IncludeExcludeFieldsREST.ConvertLead, IncludeExcludeFieldsREST.valueOf("ConvertLead"));
        assertEquals(IncludeExcludeFieldsREST.ConvertLead, IncludeExcludeFieldsREST.valueOf(21));
        assertEquals(21, IncludeExcludeFieldsREST.ConvertLead.fieldVal);
        assertEquals(IncludeExcludeFieldsREST.ChangeScore, IncludeExcludeFieldsREST.valueOf("ChangeScore"));
        assertEquals(IncludeExcludeFieldsREST.ChangeScore, IncludeExcludeFieldsREST.valueOf(22));
        assertEquals(22, IncludeExcludeFieldsREST.ChangeScore.fieldVal);
        assertEquals(IncludeExcludeFieldsREST.ChangeOwner, IncludeExcludeFieldsREST.valueOf("ChangeOwner"));
        assertEquals(IncludeExcludeFieldsREST.ChangeOwner, IncludeExcludeFieldsREST.valueOf(23));
        assertEquals(23, IncludeExcludeFieldsREST.ChangeOwner.fieldVal);
        assertEquals(IncludeExcludeFieldsREST.AddToList, IncludeExcludeFieldsREST.valueOf("AddToList"));
        assertEquals(IncludeExcludeFieldsREST.AddToList, IncludeExcludeFieldsREST.valueOf(24));
        assertEquals(24, IncludeExcludeFieldsREST.AddToList.fieldVal);
        assertEquals(IncludeExcludeFieldsREST.RemoveFromList, IncludeExcludeFieldsREST.valueOf("RemoveFromList"));
        assertEquals(IncludeExcludeFieldsREST.RemoveFromList, IncludeExcludeFieldsREST.valueOf(25));
        assertEquals(25, IncludeExcludeFieldsREST.RemoveFromList.fieldVal);
        assertEquals(IncludeExcludeFieldsREST.SFDCActivity, IncludeExcludeFieldsREST.valueOf("SFDCActivity"));
        assertEquals(IncludeExcludeFieldsREST.SFDCActivity, IncludeExcludeFieldsREST.valueOf(26));
        assertEquals(26, IncludeExcludeFieldsREST.SFDCActivity.fieldVal);
        assertEquals(IncludeExcludeFieldsREST.EmailBouncedSoft, IncludeExcludeFieldsREST.valueOf("EmailBouncedSoft"));
        assertEquals(IncludeExcludeFieldsREST.EmailBouncedSoft, IncludeExcludeFieldsREST.valueOf(27));
        assertEquals(27, IncludeExcludeFieldsREST.EmailBouncedSoft.fieldVal);
        assertEquals(IncludeExcludeFieldsREST.DeleteLeadFromSFDC, IncludeExcludeFieldsREST.valueOf("DeleteLeadFromSFDC"));
        assertEquals(IncludeExcludeFieldsREST.DeleteLeadFromSFDC, IncludeExcludeFieldsREST.valueOf(29));
        assertEquals(29, IncludeExcludeFieldsREST.DeleteLeadFromSFDC.fieldVal);
        assertEquals(IncludeExcludeFieldsREST.SFDCActivityUpdated, IncludeExcludeFieldsREST.valueOf("SFDCActivityUpdated"));
        assertEquals(IncludeExcludeFieldsREST.SFDCActivityUpdated, IncludeExcludeFieldsREST.valueOf(30));
        assertEquals(30, IncludeExcludeFieldsREST.SFDCActivityUpdated.fieldVal);
        assertEquals(IncludeExcludeFieldsREST.MergeLeads, IncludeExcludeFieldsREST.valueOf("MergeLeads"));
        assertEquals(IncludeExcludeFieldsREST.MergeLeads, IncludeExcludeFieldsREST.valueOf(32));
        assertEquals(32, IncludeExcludeFieldsREST.MergeLeads.fieldVal);
        assertEquals(IncludeExcludeFieldsREST.AddToOpportunity, IncludeExcludeFieldsREST.valueOf("AddToOpportunity"));
        assertEquals(IncludeExcludeFieldsREST.AddToOpportunity, IncludeExcludeFieldsREST.valueOf(34));
        assertEquals(34, IncludeExcludeFieldsREST.AddToOpportunity.fieldVal);
        assertEquals(IncludeExcludeFieldsREST.RemoveFromOpportunity, IncludeExcludeFieldsREST.valueOf("RemoveFromOpportunity"));
        assertEquals(IncludeExcludeFieldsREST.RemoveFromOpportunity, IncludeExcludeFieldsREST.valueOf(35));
        assertEquals(35, IncludeExcludeFieldsREST.RemoveFromOpportunity.fieldVal);
        assertEquals(IncludeExcludeFieldsREST.UpdateOpportunity, IncludeExcludeFieldsREST.valueOf("UpdateOpportunity"));
        assertEquals(IncludeExcludeFieldsREST.UpdateOpportunity, IncludeExcludeFieldsREST.valueOf(36));
        assertEquals(36, IncludeExcludeFieldsREST.UpdateOpportunity.fieldVal);
        assertEquals(IncludeExcludeFieldsREST.DeleteLead, IncludeExcludeFieldsREST.valueOf("DeleteLead"));
        assertEquals(IncludeExcludeFieldsREST.DeleteLead, IncludeExcludeFieldsREST.valueOf(37));
        assertEquals(37, IncludeExcludeFieldsREST.DeleteLead.fieldVal);
        assertEquals(IncludeExcludeFieldsREST.SendAlert, IncludeExcludeFieldsREST.valueOf("SendAlert"));
        assertEquals(IncludeExcludeFieldsREST.SendAlert, IncludeExcludeFieldsREST.valueOf(38));
        assertEquals(38, IncludeExcludeFieldsREST.SendAlert.fieldVal);
        assertEquals(IncludeExcludeFieldsREST.SendSalesEmail, IncludeExcludeFieldsREST.valueOf("SendSalesEmail"));
        assertEquals(IncludeExcludeFieldsREST.SendSalesEmail, IncludeExcludeFieldsREST.valueOf(39));
        assertEquals(39, IncludeExcludeFieldsREST.SendSalesEmail.fieldVal);
        assertEquals(IncludeExcludeFieldsREST.OpenSalesEmail, IncludeExcludeFieldsREST.valueOf("OpenSalesEmail"));
        assertEquals(IncludeExcludeFieldsREST.OpenSalesEmail, IncludeExcludeFieldsREST.valueOf(40));
        assertEquals(40, IncludeExcludeFieldsREST.OpenSalesEmail.fieldVal);
        assertEquals(IncludeExcludeFieldsREST.ClickSalesEmail, IncludeExcludeFieldsREST.valueOf("ClickSalesEmail"));
        assertEquals(IncludeExcludeFieldsREST.ClickSalesEmail, IncludeExcludeFieldsREST.valueOf(41));
        assertEquals(41, IncludeExcludeFieldsREST.ClickSalesEmail.fieldVal);
        assertEquals(IncludeExcludeFieldsREST.AddToSFDCCampaign, IncludeExcludeFieldsREST.valueOf("AddToSFDCCampaign"));
        assertEquals(IncludeExcludeFieldsREST.AddToSFDCCampaign, IncludeExcludeFieldsREST.valueOf(42));
        assertEquals(42, IncludeExcludeFieldsREST.AddToSFDCCampaign.fieldVal);
        assertEquals(IncludeExcludeFieldsREST.RemoveFromSFDCCampaign, IncludeExcludeFieldsREST.valueOf("RemoveFromSFDCCampaign"));
        assertEquals(IncludeExcludeFieldsREST.RemoveFromSFDCCampaign, IncludeExcludeFieldsREST.valueOf(43));
        assertEquals(43, IncludeExcludeFieldsREST.RemoveFromSFDCCampaign.fieldVal);
        assertEquals(IncludeExcludeFieldsREST.ChangeStatusInSFDCCampaign,
                IncludeExcludeFieldsREST.valueOf("ChangeStatusInSFDCCampaign"));
        assertEquals(IncludeExcludeFieldsREST.ChangeStatusInSFDCCampaign, IncludeExcludeFieldsREST.valueOf(44));
        assertEquals(44, IncludeExcludeFieldsREST.ChangeStatusInSFDCCampaign.fieldVal);
        assertEquals(IncludeExcludeFieldsREST.ReceiveSalesEmail, IncludeExcludeFieldsREST.valueOf("ReceiveSalesEmail"));
        assertEquals(IncludeExcludeFieldsREST.ReceiveSalesEmail, IncludeExcludeFieldsREST.valueOf(45));
        assertEquals(45, IncludeExcludeFieldsREST.ReceiveSalesEmail.fieldVal);
        assertEquals(IncludeExcludeFieldsREST.InterestingMoment, IncludeExcludeFieldsREST.valueOf("InterestingMoment"));
        assertEquals(IncludeExcludeFieldsREST.InterestingMoment, IncludeExcludeFieldsREST.valueOf(46));
        assertEquals(46, IncludeExcludeFieldsREST.InterestingMoment.fieldVal);
        assertEquals(IncludeExcludeFieldsREST.RequestCampaign, IncludeExcludeFieldsREST.valueOf("RequestCampaign"));
        assertEquals(IncludeExcludeFieldsREST.RequestCampaign, IncludeExcludeFieldsREST.valueOf(47));
        assertEquals(47, IncludeExcludeFieldsREST.RequestCampaign.fieldVal);
        assertEquals(IncludeExcludeFieldsREST.SalesEmailBounced, IncludeExcludeFieldsREST.valueOf("SalesEmailBounced"));
        assertEquals(IncludeExcludeFieldsREST.SalesEmailBounced, IncludeExcludeFieldsREST.valueOf(48));
        assertEquals(48, IncludeExcludeFieldsREST.SalesEmailBounced.fieldVal);
        assertEquals(IncludeExcludeFieldsREST.ChangeLeadPartition, IncludeExcludeFieldsREST.valueOf("ChangeLeadPartition"));
        assertEquals(IncludeExcludeFieldsREST.ChangeLeadPartition, IncludeExcludeFieldsREST.valueOf(100));
        assertEquals(100, IncludeExcludeFieldsREST.ChangeLeadPartition.fieldVal);
        assertEquals(IncludeExcludeFieldsREST.ChangeRevenueStage, IncludeExcludeFieldsREST.valueOf("ChangeRevenueStage"));
        assertEquals(IncludeExcludeFieldsREST.ChangeRevenueStage, IncludeExcludeFieldsREST.valueOf(101));
        assertEquals(101, IncludeExcludeFieldsREST.ChangeRevenueStage.fieldVal);
        assertEquals(IncludeExcludeFieldsREST.ChangeRevenueStageManually,
                IncludeExcludeFieldsREST.valueOf("ChangeRevenueStageManually"));
        assertEquals(IncludeExcludeFieldsREST.ChangeRevenueStageManually, IncludeExcludeFieldsREST.valueOf(102));
        assertEquals(102, IncludeExcludeFieldsREST.ChangeRevenueStageManually.fieldVal);
        assertEquals(IncludeExcludeFieldsREST.ChangeStatusInProgression,
                IncludeExcludeFieldsREST.valueOf("ChangeStatusInProgression"));
        assertEquals(IncludeExcludeFieldsREST.ChangeStatusInProgression, IncludeExcludeFieldsREST.valueOf(104));
        assertEquals(104, IncludeExcludeFieldsREST.ChangeStatusInProgression.fieldVal);
        assertEquals(IncludeExcludeFieldsREST.EnrichWithDataCom, IncludeExcludeFieldsREST.valueOf("EnrichWithDataCom"));
        assertEquals(IncludeExcludeFieldsREST.EnrichWithDataCom, IncludeExcludeFieldsREST.valueOf(106));
        assertEquals(106, IncludeExcludeFieldsREST.EnrichWithDataCom.fieldVal);
        assertEquals(IncludeExcludeFieldsREST.ChangeSegment, IncludeExcludeFieldsREST.valueOf("ChangeSegment"));
        assertEquals(IncludeExcludeFieldsREST.ChangeSegment, IncludeExcludeFieldsREST.valueOf(108));
        assertEquals(108, IncludeExcludeFieldsREST.ChangeSegment.fieldVal);
        assertEquals(IncludeExcludeFieldsREST.CallWebhook, IncludeExcludeFieldsREST.valueOf("CallWebhook"));
        assertEquals(IncludeExcludeFieldsREST.CallWebhook, IncludeExcludeFieldsREST.valueOf(110));
        assertEquals(110, IncludeExcludeFieldsREST.CallWebhook.fieldVal);
        assertEquals(IncludeExcludeFieldsREST.SentForwardToFriendEmail,
                IncludeExcludeFieldsREST.valueOf("SentForwardToFriendEmail"));
        assertEquals(IncludeExcludeFieldsREST.SentForwardToFriendEmail, IncludeExcludeFieldsREST.valueOf(111));
        assertEquals(111, IncludeExcludeFieldsREST.SentForwardToFriendEmail.fieldVal);
        assertEquals(IncludeExcludeFieldsREST.ReceivedForwardToFriendEmail,
                IncludeExcludeFieldsREST.valueOf("ReceivedForwardToFriendEmail"));
        assertEquals(IncludeExcludeFieldsREST.ReceivedForwardToFriendEmail, IncludeExcludeFieldsREST.valueOf(112));
        assertEquals(112, IncludeExcludeFieldsREST.ReceivedForwardToFriendEmail.fieldVal);
        assertEquals(IncludeExcludeFieldsREST.AddToNurture, IncludeExcludeFieldsREST.valueOf("AddToNurture"));
        assertEquals(IncludeExcludeFieldsREST.AddToNurture, IncludeExcludeFieldsREST.valueOf(113));
        assertEquals(113, IncludeExcludeFieldsREST.AddToNurture.fieldVal);
        assertEquals(IncludeExcludeFieldsREST.ChangeNurtureTrack, IncludeExcludeFieldsREST.valueOf("ChangeNurtureTrack"));
        assertEquals(IncludeExcludeFieldsREST.ChangeNurtureTrack, IncludeExcludeFieldsREST.valueOf(114));
        assertEquals(114, IncludeExcludeFieldsREST.ChangeNurtureTrack.fieldVal);
        assertEquals(IncludeExcludeFieldsREST.ChangeNurtureCadence, IncludeExcludeFieldsREST.valueOf("ChangeNurtureCadence"));
        assertEquals(IncludeExcludeFieldsREST.ChangeNurtureCadence, IncludeExcludeFieldsREST.valueOf(115));
        assertEquals(115, IncludeExcludeFieldsREST.ChangeNurtureCadence.fieldVal);
        assertEquals(IncludeExcludeFieldsREST.ShareContent, IncludeExcludeFieldsREST.valueOf("ShareContent"));
        assertEquals(IncludeExcludeFieldsREST.ShareContent, IncludeExcludeFieldsREST.valueOf(400));
        assertEquals(400, IncludeExcludeFieldsREST.ShareContent.fieldVal);
        assertEquals(IncludeExcludeFieldsREST.VoteInPoll, IncludeExcludeFieldsREST.valueOf("VoteInPoll"));
        assertEquals(IncludeExcludeFieldsREST.VoteInPoll, IncludeExcludeFieldsREST.valueOf(401));
        assertEquals(401, IncludeExcludeFieldsREST.VoteInPoll.fieldVal);
        assertEquals(IncludeExcludeFieldsREST.ClickSharedLink, IncludeExcludeFieldsREST.valueOf("ClickSharedLink"));
        assertEquals(IncludeExcludeFieldsREST.ClickSharedLink, IncludeExcludeFieldsREST.valueOf(405));
        assertEquals(405, IncludeExcludeFieldsREST.ClickSharedLink.fieldVal);
        // ListParm
        assertEquals(ListParam.STATIC_LIST_ID, ListParam.valueOf("STATIC_LIST_ID"));
        assertEquals(ListParam.STATIC_LIST_NAME, ListParam.valueOf("STATIC_LIST_NAME"));
        //
        assertEquals(LeadKeyTypeREST.id, LeadKeyTypeREST.valueOf("id"));
        assertEquals(LeadKeyTypeREST.cookie, LeadKeyTypeREST.valueOf("cookie"));
        assertEquals(LeadKeyTypeREST.email, LeadKeyTypeREST.valueOf("email"));
        assertEquals(LeadKeyTypeREST.twitterId, LeadKeyTypeREST.valueOf("twitterId"));
        assertEquals(LeadKeyTypeREST.facebookId, LeadKeyTypeREST.valueOf("facebookId"));
        assertEquals(LeadKeyTypeREST.linkedInId, LeadKeyTypeREST.valueOf("linkedInId"));
        assertEquals(LeadKeyTypeREST.sfdcAccountId, LeadKeyTypeREST.valueOf("sfdcAccountId"));
        assertEquals(LeadKeyTypeREST.sfdcContactId, LeadKeyTypeREST.valueOf("sfdcContactId"));
        assertEquals(LeadKeyTypeREST.sfdcLeadId, LeadKeyTypeREST.valueOf("sfdcLeadId"));
        assertEquals(LeadKeyTypeREST.sfdcLeadOwnerId, LeadKeyTypeREST.valueOf("sfdcLeadOwnerId"));
        assertEquals(LeadKeyTypeREST.sfdcOpptyId, LeadKeyTypeREST.valueOf("sfdcOpptyId"));
        //
        assertEquals(LeadKeyTypeSOAP.IDNUM, LeadKeyTypeSOAP.valueOf("IDNUM"));
        assertEquals(LeadKeyTypeSOAP.COOKIE, LeadKeyTypeSOAP.valueOf("COOKIE"));
        assertEquals(LeadKeyTypeSOAP.EMAIL, LeadKeyTypeSOAP.valueOf("EMAIL"));
        assertEquals(LeadKeyTypeSOAP.LEADOWNEREMAIL, LeadKeyTypeSOAP.valueOf("LEADOWNEREMAIL"));
        assertEquals(LeadKeyTypeSOAP.SFDCACCOUNTID, LeadKeyTypeSOAP.valueOf("SFDCACCOUNTID"));
        assertEquals(LeadKeyTypeSOAP.SFDCCONTACTID, LeadKeyTypeSOAP.valueOf("SFDCCONTACTID"));
        assertEquals(LeadKeyTypeSOAP.SFDCLEADID, LeadKeyTypeSOAP.valueOf("SFDCLEADID"));
        assertEquals(LeadKeyTypeSOAP.SFDCLEADOWNERID, LeadKeyTypeSOAP.valueOf("SFDCLEADOWNERID"));
        assertEquals(LeadKeyTypeSOAP.SFDCOPPTYID, LeadKeyTypeSOAP.valueOf("SFDCOPPTYID"));
        //
        assertEquals(StandardAction.describe, StandardAction.valueOf("describe"));
        assertEquals(StandardAction.get, StandardAction.valueOf("get"));
    }

    @Test
    public void testChangeOperationSchema() throws Exception {
        props.inputOperation.setValue(CustomObject);
        props.customObjectAction.setValue(CustomObjectAction.get);
        props.refreshLayout(props.getForm(Form.MAIN));
        props.afterInputOperation();
        assertEquals(MarketoConstants.getCustomObjectRecordSchema(), props.schemaInput.schema.getValue());
        props.inputOperation.setValue(getLeadActivity);
        props.afterCustomObjectAction();
        assertEquals(MarketoConstants.getRESTSchemaForGetLeadActivity(), props.schemaInput.schema.getValue());
        props.inputOperation.setValue(CustomObject);
        props.customObjectAction.setValue(CustomObjectAction.describe);
        props.afterCustomObjectAction();
        assertEquals(MarketoConstants.getCustomObjectDescribeSchema(), props.schemaInput.schema.getValue());
        props.customObjectAction.setValue(CustomObjectAction.list);
        props.afterCustomObjectAction();
        assertEquals(MarketoConstants.getCustomObjectDescribeSchema(), props.schemaInput.schema.getValue());
        props.customObjectAction.setValue(CustomObjectAction.get);
        props.afterCustomObjectAction();
        assertEquals(MarketoConstants.getCustomObjectRecordSchema(), props.schemaInput.schema.getValue());
    }

    @Test
    public void testValidateInputOperation() throws Exception {
        assertEquals(Result.OK, props.validateInputOperation().getStatus());
        props.connection.apiMode.setValue(APIMode.SOAP);
        props.inputOperation.setValue(getLeadActivity);
        assertEquals(Result.OK, props.validateInputOperation().getStatus());
        props.inputOperation.setValue(CustomObject);
        props.connection.apiMode.setValue(APIMode.SOAP);
        assertEquals(Result.ERROR, props.validateInputOperation().getStatus());
    }

    @Test
    public void testTDI38475() throws Exception {
        assertEquals(MarketoConstants.getRESTSchemaForGetLeadOrGetMultipleLeads(), props.schemaInput.schema.getValue());
        props.connection.apiMode.setValue(APIMode.SOAP);
        props.afterInputOperation();
        props.refreshLayout(props.getForm(Form.ADVANCED));
        assertEquals(MarketoConstants.getSOAPSchemaForGetLeadOrGetMultipleLeads(), props.schemaInput.schema.getValue());
        props.refreshLayout(props.getForm(Form.MAIN));
        assertEquals(MarketoConstants.getSOAPSchemaForGetLeadOrGetMultipleLeads(), props.schemaInput.schema.getValue());
        props.connection.apiMode.setValue(APIMode.SOAP);
        props.afterInputOperation();
        assertEquals(MarketoConstants.getSOAPSchemaForGetLeadOrGetMultipleLeads(), props.schemaInput.schema.getValue());
        props.connection.apiMode.setValue(APIMode.REST);
        props.afterInputOperation();
        assertEquals(MarketoConstants.getRESTSchemaForGetLeadOrGetMultipleLeads(), props.schemaInput.schema.getValue());
    }

    @Test
    public void testCompoundKey() throws Exception {
        props.refreshLayout(props.getForm(Form.MAIN));
        Form f = props.getForm(Form.MAIN);
        assertFalse(f.getWidget(props.customObjectAction).isVisible());
        assertFalse(f.getWidget(props.customObjectName).isVisible());
        assertFalse(f.getWidget(props.customObjectNames).isVisible());
        assertFalse(f.getWidget(props.customObjectFilterType).isVisible());
        assertFalse(f.getWidget(props.customObjectFilterValues).isVisible());
        assertFalse(f.getWidget(props.useCompoundKey).isVisible());
        assertFalse(f.getWidget(props.compoundKey).isVisible());
        assertFalse(f.getWidget(props.fetchCustomObjectSchema).isVisible());
        assertFalse(f.getWidget(props.fetchCompoundKey).isVisible());

        props.inputOperation.setValue(CustomObject);
        props.customObjectAction.setValue(CustomObjectAction.get);
        props.afterInputOperation();
        props.refreshLayout(props.getForm(Form.MAIN));
        assertEquals(MarketoConstants.getCustomObjectRecordSchema(), props.schemaInput.schema.getValue());
        assertTrue(f.getWidget(props.customObjectAction).isVisible());
        assertTrue(f.getWidget(props.customObjectName).isVisible());
        assertFalse(f.getWidget(props.customObjectNames).isVisible());
        assertTrue(f.getWidget(props.customObjectFilterType).isVisible());
        assertTrue(f.getWidget(props.customObjectFilterValues).isVisible());
        assertTrue(f.getWidget(props.useCompoundKey).isVisible());
        assertFalse(f.getWidget(props.compoundKey).isVisible());
        assertTrue(f.getWidget(props.fetchCustomObjectSchema).isVisible());
        assertFalse(f.getWidget(props.fetchCompoundKey).isVisible());

        props.useCompoundKey.setValue(true);
        props.afterUseCompoundKey();
        props.refreshLayout(props.getForm(Form.MAIN));
        assertTrue(f.getWidget(props.customObjectAction).isVisible());
        assertTrue(f.getWidget(props.customObjectName).isVisible());
        assertFalse(f.getWidget(props.customObjectNames).isVisible());
        assertFalse(f.getWidget(props.customObjectFilterType).isVisible());
        assertFalse(f.getWidget(props.customObjectFilterValues).isVisible());
        assertTrue(f.getWidget(props.useCompoundKey).isVisible());
        assertTrue(f.getWidget(props.compoundKey).isVisible());
        assertTrue(f.getWidget(props.fetchCustomObjectSchema).isVisible());
        assertTrue(f.getWidget(props.fetchCompoundKey).isVisible());
    }

    @Test
    public void testBeforeInputOperation() throws Exception {
        props.beforeInputOperation();
        assertEquals(Arrays.asList(getLead, getMultipleLeads, getLeadActivity, getLeadChanges, CustomObject),
                props.inputOperation.getPossibleValues());
        props.connection.apiMode.setValue(APIMode.SOAP);
        props.beforeInputOperation();
        assertEquals(Arrays.asList(getLead, getMultipleLeads, getLeadActivity, getLeadChanges),
                props.inputOperation.getPossibleValues());
    }

    @Test
    public void testGetVersionNumber() throws Exception {
        assertTrue(props.getVersionNumber() > 0);
    }

    @Test
    public void testPostDeserialize() throws Exception {
        MarketoComponentWizardBaseProperties mprops = mock(MarketoComponentWizardBaseProperties.class);
        when(mprops.postDeserialize(eq(0), any(PostDeserializeSetup.class), eq(false))).thenReturn(true);
        assertFalse(props.postDeserialize(0, null, false));
    }

    @Test
    public void testValidateFetchCustomObjectSchema() throws Exception {
        try (SandboxedInstanceTestFixture sandboxedInstanceTestFixture = new SandboxedInstanceTestFixture()) {
            sandboxedInstanceTestFixture.setUp();
            props.inputOperation.setValue(CustomObject);
            props.customObjectAction.setValue(CustomObjectAction.get);
            props.refreshLayout(props.getForm(Form.MAIN));
            props.afterInputOperation();
            props.customObjectName.setValue("car_c");
            assertEquals(Result.OK, props.validateFetchCustomObjectSchema().getStatus());
            props.afterFetchCustomObjectSchema();
            assertEquals(CO_CARC_SCHEMA, props.schemaInput.schema.getValue());
            props.customObjectName.setValue("car_null");
            assertEquals(Result.ERROR, props.validateFetchCustomObjectSchema().getStatus());
            props.customObjectName.setValue("car_except");
            assertEquals(Result.ERROR, props.validateFetchCustomObjectSchema().getStatus());
        }
    }

    @Test
    public void testValidateFetchCompoundKey() throws Exception {
        try (SandboxedInstanceTestFixture sandboxedInstanceTestFixture = new SandboxedInstanceTestFixture()) {
            sandboxedInstanceTestFixture.setUp();
            props.customObjectName.setValue("car_c");
            assertEquals(Result.OK, props.validateFetchCompoundKey().getStatus());
            props.afterFetchCompoundKey();
            assertEquals(Arrays.asList("brand", "model"), props.compoundKey.keyName.getValue());
            props.customObjectName.setValue("car_null");
            assertEquals(Result.ERROR, props.validateFetchCompoundKey().getStatus());
            props.customObjectName.setValue("car_except");
            assertEquals(Result.ERROR, props.validateFetchCompoundKey().getStatus());
        }
    }

    @Test
    public void testLeadActivityVisibility() throws Exception {
        String tl_setExcludeTypes = props.setExcludeTypes.getName();
        String tl_excludeTypes = props.excludeTypes.getName();
        String tl_setIncludeTypes = props.setIncludeTypes.getName();
        String tl_includeTypes = props.includeTypes.getName();
        props.refreshLayout(props.getForm(Form.MAIN));
        Form f = props.getForm(Form.MAIN);
        // leadActivity SOAP
        props.connection.apiMode.setValue(APIMode.SOAP);
        props.inputOperation.setValue(InputOperation.getLeadActivity);
        props.refreshLayout(f);
        assertTrue(f.getWidget(tl_setExcludeTypes).isVisible());
        assertTrue(f.getWidget(tl_setIncludeTypes).isVisible());
        assertFalse(f.getWidget(tl_excludeTypes).isVisible());
        assertFalse(f.getWidget(tl_includeTypes).isVisible());
        props.setExcludeTypes.setValue(true);
        props.refreshLayout(f);
        assertTrue(f.getWidget(tl_setExcludeTypes).isVisible());
        assertTrue(f.getWidget(tl_setIncludeTypes).isVisible());
        assertTrue(f.getWidget(tl_excludeTypes).isVisible());
        assertFalse(f.getWidget(tl_includeTypes).isVisible());
        props.setIncludeTypes.setValue(true);
        props.refreshLayout(f);
        assertTrue(f.getWidget(tl_setExcludeTypes).isVisible());
        assertTrue(f.getWidget(tl_setIncludeTypes).isVisible());
        assertTrue(f.getWidget(tl_excludeTypes).isVisible());
        assertTrue(f.getWidget(tl_includeTypes).isVisible());
        // leadActivity REST
        props.connection.apiMode.setValue(APIMode.REST);
        props.inputOperation.setValue(InputOperation.getLeadActivity);
        props.refreshLayout(f);
        props.setExcludeTypes.setValue(false);
        props.setIncludeTypes.setValue(false);
        props.refreshLayout(f);
        assertTrue(f.getWidget(tl_setExcludeTypes).isVisible());
        assertTrue(f.getWidget(tl_setIncludeTypes).isVisible());
        assertFalse(f.getWidget(tl_excludeTypes).isVisible());
        assertFalse(f.getWidget(tl_includeTypes).isVisible());
        props.setExcludeTypes.setValue(true);
        props.refreshLayout(f);
        assertTrue(f.getWidget(tl_setExcludeTypes).isVisible());
        assertFalse(f.getWidget(tl_setIncludeTypes).isVisible());
        assertTrue(f.getWidget(tl_excludeTypes).isVisible());
        assertFalse(f.getWidget(tl_includeTypes).isVisible());
        props.setExcludeTypes.setValue(false);
        props.setIncludeTypes.setValue(true);
        props.refreshLayout(f);
        assertFalse(f.getWidget(tl_setExcludeTypes).isVisible());
        assertTrue(f.getWidget(tl_setIncludeTypes).isVisible());
        assertFalse(f.getWidget(tl_excludeTypes).isVisible());
        assertTrue(f.getWidget(tl_includeTypes).isVisible());
    }

    @Test
    public void testLeadChangeVisibility() throws Exception {
        String tl_setExcludeTypes = props.setExcludeTypes.getName();
        String tl_excludeTypes = props.excludeTypes.getName();
        String tl_setIncludeTypes = props.setIncludeTypes.getName();
        String tl_includeTypes = props.includeTypes.getName();
        props.refreshLayout(props.getForm(Form.MAIN));
        Form f = props.getForm(Form.MAIN);
        // leadChanges SOAP
        props.connection.apiMode.setValue(APIMode.SOAP);
        props.inputOperation.setValue(InputOperation.getLeadChanges);
        props.setExcludeTypes.setValue(false);
        props.setIncludeTypes.setValue(false);
        props.refreshLayout(f);
        assertTrue(f.getWidget(tl_setExcludeTypes).isVisible());
        assertTrue(f.getWidget(tl_setIncludeTypes).isVisible());
        assertFalse(f.getWidget(tl_excludeTypes).isVisible());
        assertFalse(f.getWidget(tl_includeTypes).isVisible());
        props.setExcludeTypes.setValue(true);
        props.refreshLayout(f);
        assertTrue(f.getWidget(tl_setExcludeTypes).isVisible());
        assertTrue(f.getWidget(tl_setIncludeTypes).isVisible());
        assertTrue(f.getWidget(tl_excludeTypes).isVisible());
        assertFalse(f.getWidget(tl_includeTypes).isVisible());
        props.setIncludeTypes.setValue(true);
        props.refreshLayout(f);
        assertTrue(f.getWidget(tl_setExcludeTypes).isVisible());
        assertTrue(f.getWidget(tl_setIncludeTypes).isVisible());
        assertTrue(f.getWidget(tl_excludeTypes).isVisible());
        assertTrue(f.getWidget(tl_includeTypes).isVisible());
        // leadChanges REST
        props.connection.apiMode.setValue(APIMode.REST);
        props.inputOperation.setValue(InputOperation.getLeadChanges);
        props.refreshLayout(f);
        assertFalse(f.getWidget(tl_setExcludeTypes).isVisible());
        assertFalse(f.getWidget(tl_setIncludeTypes).isVisible());
        assertFalse(f.getWidget(tl_excludeTypes).isVisible());
        assertFalse(f.getWidget(tl_includeTypes).isVisible());
    }
}
