package org.talend.components.marketo.runtime;

import org.apache.commons.lang3.StringUtils;
import org.talend.components.api.component.runtime.Sink;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.marketo.MarketoComponentProperties;
import org.talend.components.marketo.tmarketocampaign.TMarketoCampaignProperties;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.InputOperation;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.LeadSelector;
import org.talend.components.marketo.tmarketolistoperation.TMarketoListOperationProperties;
import org.talend.components.marketo.tmarketooutput.TMarketoOutputProperties;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;
import org.talend.daikon.properties.ValidationResultMutable;

public class MarketoSink extends MarketoSourceOrSink implements Sink {

    private static final I18nMessages messages = GlobalI18N.getI18nMessageProvider().getI18nMessages(MarketoSink.class);

    @Override
    public WriteOperation<?> createWriteOperation() {
        return new MarketoWriteOperation(this);
    }

    @Override
    public ValidationResult validate(RuntimeContainer container) {
        ValidationResultMutable vr = new ValidationResultMutable(super.validate(container));
        if (vr.getStatus().equals(Result.ERROR)) {
            return vr;
        }
        // output
        if (properties instanceof TMarketoOutputProperties) {
            switch (((TMarketoOutputProperties) properties).outputOperation.getValue()) {
            case syncLead:
                break;
            case syncMultipleLeads:
                break;
            case deleteLeads:
                break;
            case syncCustomObjects:
                if (StringUtils.isEmpty(((TMarketoOutputProperties) properties).customObjectName.getValue())) {
                    vr.setStatus(Result.ERROR);
                    vr.setMessage(messages.getMessage("error.validation.customobject.customobjectname"));
                    return vr;
                }
                if (StringUtils.isEmpty(((TMarketoOutputProperties) properties).customObjectDedupeBy.getValue())) {
                    // TODO check API if required. Not sure...
                }
                break;
            case deleteCustomObjects:
                if (StringUtils.isEmpty(((TMarketoOutputProperties) properties).customObjectName.getValue())) {
                    vr.setStatus(Result.ERROR);
                    vr.setMessage(messages.getMessage("error.validation.customobject.customobjectname"));
                    return vr;
                }
                break;
            }
        }
        // check list operations
        if (properties instanceof TMarketoListOperationProperties) {
            // nothing to check for now.
        }
        // check getMultipleLeads with an input
        if (properties instanceof TMarketoInputProperties) {
            // operation must be getMultipleLeads
            if (!((TMarketoInputProperties) properties).inputOperation.getValue().equals(InputOperation.getMultipleLeads)) {
                vr.setStatus(Result.ERROR);
                vr.setMessage(messages.getMessage("error.validation.sink.getmultipleleads.only"));
                return vr;
            }
            // lead selector must be LeadKeySelector
            LeadSelector selector;
            if (properties.getApiMode().equals(MarketoComponentProperties.APIMode.SOAP)) {
                selector = ((TMarketoInputProperties) properties).leadSelectorSOAP.getValue();
            } else {
                selector = ((TMarketoInputProperties) properties).leadSelectorREST.getValue();
            }
            if (!selector.equals(LeadSelector.LeadKeySelector)) {
                vr.setStatus(Result.ERROR);
                vr.setMessage(messages.getMessage("error.validation.sink.leadkeyselector.only"));
                return vr;
            }
            // lead key values must be defined
            if (StringUtils.isEmpty(((TMarketoInputProperties) properties).leadKeyValues.getValue())) {
                vr.setStatus(Result.ERROR);
                vr.setMessage(messages.getMessage("error.validation.leadkeyvalues"));
                return vr;
            }
        }
        // Campaign
        if (properties instanceof TMarketoCampaignProperties) {
            TMarketoCampaignProperties p = (TMarketoCampaignProperties) properties;
            switch (p.campaignAction.getValue()) {
            case schedule:
            case trigger:
                if (StringUtils.isEmpty(p.campaignId.getStringValue())) {
                    vr.setStatus(Result.ERROR);
                    vr.setMessage(messages.getMessage("error.validation.campaign.byid"));
                    return vr;
                }
                break;
            }
        }

        return ValidationResult.OK;
    }

}
