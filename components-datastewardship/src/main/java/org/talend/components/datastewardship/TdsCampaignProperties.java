// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.datastewardship;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.talend.daikon.properties.property.PropertyFactory.newProperty;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.talend.components.datastewardship.avro.CampaignAvroRegistry;
import org.talend.components.datastewardship.common.CampaignDetail;
import org.talend.components.datastewardship.common.CampaignDetail.RecordField;
import org.talend.components.datastewardship.common.CampaignType;
import org.talend.components.datastewardship.common.TdsConstants;
import org.talend.components.datastewardship.common.TdsUtils;
import org.talend.components.datastewardship.connection.TdsConnection;
import org.talend.components.datastewardship.tdatastewardshiptaskinput.TDataStewardshipTaskInputProperties;
import org.talend.components.datastewardship.tdatastewardshiptaskoutput.TDataStewardshipTaskOutputProperties;
import org.talend.daikon.avro.AvroRegistry;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.EnumProperty;
import org.talend.daikon.properties.property.Property;

/**
 * TDS Campaign {@link Properties}
 */
public abstract class TdsCampaignProperties extends TdsProperties {

    /**
     * Campaign name
     */
    public Property<String> campaignName = newProperty("campaignName").setRequired(); //$NON-NLS-1$

    /**
     * Campaign label
     */
    public Property<String> campaignLabel = newProperty("campaignLabel"); //$NON-NLS-1$

    /**
     * Campaign type
     */
    public EnumProperty<CampaignType> campaignType = new EnumProperty<CampaignType>(CampaignType.class, "campaignType"); //$NON-NLS-1$

    /**
     * 
     * @param name
     * @param widgetType
     */
    public TdsCampaignProperties(String name) {
        super(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = getForm(Form.MAIN);
        mainForm.addRow(Widget.widget(campaignName).setWidgetType(TdsConstants.WIDGET_TYPE_CAMPAIGN_CHOOSER)); // $NON-NLS-1$
        mainForm.addRow(campaignLabel);
        mainForm.addColumn(campaignType);
    }

    public ValidationResult afterCampaignName() {
        if (!isRequiredFieldRight()) {
            return new ValidationResult().setStatus(ValidationResult.Result.ERROR)
                    .setMessage(getI18nMessage("error.missingRequiredFields")); //$NON-NLS-1$
        } else {
            ValidationResult validationResult = ValidationResult.OK;
            try {
                CampaignDetail campaign = fetchCampaignDetail();
                // basic info
                campaignLabel.setValue(TdsUtils.addQuotes(campaign.getLabel()));
                campaignType.setValue(CampaignType.valueOf(campaign.getType()));
                // related properties
                setupRelatedProperties(campaign);
                // schema
                AvroRegistry avroRegistry = CampaignAvroRegistry.getCampaignInstance();
                schema.schema.setValue(avroRegistry.inferSchema(getSchemaFields(campaign)));
            } catch (Exception e) {
                validationResult = new ValidationResult().setStatus(Result.ERROR)
                        .setMessage(getI18nMessage("error.campaignIsNotFetched", e.getMessage())); //$NON-NLS-1$
            }
            return validationResult;
        }
    }

    protected abstract void setupRelatedProperties(CampaignDetail campaign);

    protected abstract RecordField[] getSchemaFields(CampaignDetail campaign);

    private boolean isRequiredFieldRight() {
        return !isEmpty(connection.url.getStringValue()) && !isEmpty(connection.username.getStringValue())
                && !isEmpty(connection.password.getStringValue()) && !isEmpty(campaignName.getStringValue());
    }

    private CampaignDetail fetchCampaignDetail() throws Exception {
        CampaignDetail campaign = new CampaignDetail();
        String url = connection.url.getStringValue();
        String username = connection.username.getStringValue();
        String password = connection.password.getStringValue();
        TdsConnection tdsConn = new TdsConnection(url, username, password);
        String resource = TdsUtils.getCampaignDetailResource(campaignName.getStringValue());
        String response = tdsConn.get(resource);

        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(response);
        JSONArray states = (JSONArray) jsonObject.get("states"); // $NON-NLS-1$
        JSONObject recordStructure = (JSONObject) jsonObject.get("recordStructure"); // $NON-NLS-1$
        JSONArray fields = (JSONArray) recordStructure.get("fields"); // $NON-NLS-1$

        campaign = new CampaignDetail();
        campaign.setName((String) jsonObject.get("name")); // $NON-NLS-1$
        campaign.setLabel((String) jsonObject.get("label")); // $NON-NLS-1$
        campaign.setType((String) jsonObject.get("taskType")); // $NON-NLS-1$
        campaign.setCreationDate(TdsUtils.formatDate((Long) jsonObject.get("creationDate"))); // $NON-NLS-1$
        for (Object obj : states) {
            JSONObject state = (JSONObject) obj;
            campaign.getStates().add((String) state.get("name")); // $NON-NLS-1$
        }
        for (Object obj : fields) {
            JSONObject field = (JSONObject) obj;
            String name = (String) field.get("name"); // $NON-NLS-1$
            String type = (String) ((JSONObject) field.get("type")).get("frontType"); // $NON-NLS-1$ // $NON-NLS-2$
            boolean mandatory = (boolean) field.get("mandatory"); // $NON-NLS-1$
            campaign.getFields().add(new CampaignDetail.RecordField(name, type, mandatory, true));
        }
        return campaign;
    }

}
