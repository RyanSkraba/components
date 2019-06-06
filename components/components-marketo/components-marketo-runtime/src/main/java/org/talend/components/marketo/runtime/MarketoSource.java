package org.talend.components.marketo.runtime;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.api.component.runtime.BoundedSource;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.marketo.MarketoUtils;
import org.talend.components.marketo.tmarketobulkexec.TMarketoBulkExecProperties;
import org.talend.components.marketo.tmarketobulkexec.TMarketoBulkExecProperties.BulkImportTo;
import org.talend.components.marketo.tmarketocampaign.TMarketoCampaignProperties;
import org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties.APIMode;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.LeadSelector;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.ListParam;
import org.talend.components.marketo.wizard.MarketoComponentWizardBaseProperties.CustomObjectAction;
import org.talend.components.marketo.wizard.MarketoComponentWizardBaseProperties.InputOperation;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;
import org.talend.daikon.properties.ValidationResultMutable;

import static org.talend.components.marketo.MarketoConstants.REST_API_LIMIT;

public class MarketoSource extends MarketoSourceOrSink implements BoundedSource {

    private transient static final Logger LOG = LoggerFactory.getLogger(MarketoSource.class);

    private static final I18nMessages messages = GlobalI18N.getI18nMessageProvider().getI18nMessages(MarketoSource.class);

    public MarketoSource() {
    }

    @Override
    public List<? extends BoundedSource> splitIntoBundles(long desiredBundleSizeBytes, RuntimeContainer adaptor)
            throws Exception {
        List<BoundedSource> list = new ArrayList<>();
        list.add(this);
        return list;
    }

    @Override
    public long getEstimatedSizeBytes(RuntimeContainer adaptor) {
        return 0;
    }

    @Override
    public boolean producesSortedKeys(RuntimeContainer adaptor) {
        return false;
    }

    public boolean isInvalidDate(String datetime) {
        try {
            MarketoUtils.parseDateString(datetime);
            return false;
        } catch (ParseException e) {
            return true;
        }
    }

    @Override
    public ValidationResult validate(RuntimeContainer container) {
        ValidationResultMutable vr = new ValidationResultMutable(super.validate(container));
        if (vr.getStatus().equals(Result.ERROR)) {
            return vr;
        }

        if (properties instanceof TMarketoInputProperties) {
            TMarketoInputProperties p = (TMarketoInputProperties) properties;
            boolean useSOAP = APIMode.SOAP.equals(properties.getConnectionProperties().apiMode.getValue());
            // Validate dynamic schema if needed
            Boolean isDynamic = AvroUtils.isIncludeAllFields(p.schemaInput.schema.getValue());
            if (useSOAP) { // no dynamic schema for SOAP !
                if (isDynamic) {
                    vr.setStatus(Result.ERROR);
                    vr.setMessage(messages.getMessage("error.validation.soap.dynamicschema"));
                    return vr;
                }
            }
            ////////////
            // Leads
            ////////////
            // getLead
            if (p.inputOperation.getValue().equals(InputOperation.getLead)) {
                if (p.leadKeyValue.getValue().isEmpty()) {
                    vr.setStatus(Result.ERROR);
                    vr.setMessage(messages.getMessage("error.validation.leadkeyvalue"));
                    return vr;
                }
            }
            // getMultipleLeads
            if (p.inputOperation.getValue().equals(InputOperation.getMultipleLeads)) {
                LeadSelector sel;
                if (useSOAP) {
                    sel = p.leadSelectorSOAP.getValue();
                } else {
                    sel = p.leadSelectorREST.getValue();
                }
                switch (sel) {
                case LeadKeySelector:
                    if (p.leadKeyValues.getValue().isEmpty()) {
                        vr.setStatus(Result.ERROR);
                        vr.setMessage(messages.getMessage("error.validation.leadkeyvalues"));
                        return vr;
                    }
                    break;
                case StaticListSelector:
                    if (ListParam.STATIC_LIST_NAME.equals(p.listParam.getValue())) {

                        if (p.listParamListName.getValue().isEmpty()) {
                            vr.setStatus(Result.ERROR);
                            vr.setMessage(messages.getMessage("error.validation.listparamvalue"));
                            return vr;
                        }
                    } else {
                        if (p.listParamListId.getValue() == null) {
                            vr.setStatus(Result.ERROR);
                            vr.setMessage(messages.getMessage("error.validation.listparamvalue"));
                            return vr;
                        }
                    }
                    break;
                case LastUpdateAtSelector:
                    if (p.oldestUpdateDate.getValue().isEmpty() || p.latestUpdateDate.getValue().isEmpty()
                            || isInvalidDate(p.oldestUpdateDate.getValue()) || isInvalidDate(p.latestUpdateDate.getValue())) {
                        vr.setStatus(Result.ERROR);
                        vr.setMessage(messages.getMessage("error.validation.updatedates"));
                        return vr;
                    }
                    break;
                }
            }
            // getLeadActivity
            if (p.inputOperation.getValue().equals(InputOperation.getLeadActivity)) {
                if (isDynamic) {
                    vr.setStatus(Result.ERROR);
                    vr.setMessage(messages.getMessage("error.validation.operation.dynamicschema"));
                    return vr;
                }
                if (useSOAP) {
                    if (p.leadKeyValue.getValue().isEmpty()) {
                        vr.setStatus(Result.ERROR);
                        vr.setMessage(messages.getMessage("error.validation.leadkeyvalue"));
                        return vr;
                    }
                } else {
                    if (p.sinceDateTime.getValue().isEmpty() || isInvalidDate(p.sinceDateTime.getValue())) {
                        vr.setStatus(Result.ERROR);
                        vr.setMessage(messages.getMessage("error.validation.sincedatetime"));
                        return vr;
                    }
                }
            }
            // getLeadChanges
            if (p.inputOperation.getValue().equals(InputOperation.getLeadChanges)) {
                if (isDynamic) {
                    vr.setStatus(Result.ERROR);
                    vr.setMessage(messages.getMessage("error.validation.operation.dynamicschema"));
                    return vr;
                }
                if (useSOAP) {
                    if (p.oldestCreateDate.getValue().isEmpty() || p.latestCreateDate.getValue().isEmpty()
                            || isInvalidDate(p.oldestCreateDate.getValue()) || isInvalidDate(p.latestCreateDate.getValue())) {
                        vr.setStatus(Result.ERROR);
                        vr.setMessage(messages.getMessage("error.validation.createdates"));
                        return vr;
                    }
                } else {
                    if (p.sinceDateTime.getValue().isEmpty() || isInvalidDate(p.sinceDateTime.getValue())) {
                        vr.setStatus(Result.ERROR);
                        vr.setMessage(messages.getMessage("error.validation.sincedatetime"));
                        return vr;
                    }
                    if (StringUtils.isEmpty(p.fieldList.getValue())) {
                        vr.setStatus(Result.ERROR);
                        vr.setMessage(messages.getMessage("error.validation.fieldlist"));
                        return vr;
                    }
                }
            }
            /////////////////////
            // Custom Objects
            /////////////////////
            if (p.inputOperation.getValue().equals(InputOperation.CustomObject)) {
                // get Action
                if (p.customObjectAction.getValue().equals(CustomObjectAction.get)) {
                    if (p.customObjectName.getValue().isEmpty()) {
                        vr.setStatus(Result.ERROR);
                        vr.setMessage(messages.getMessage("error.validation.customobject.customobjectname"));
                        return vr;
                    }
                    if (p.useCompoundKey.getValue()) {
                        if (p.compoundKey.size() == 0) {
                            vr.setStatus(Result.ERROR);
                            vr.setMessage(messages.getMessage("error.validation.customobject.compoundkey"));
                            return vr;
                        }
                    } else {
                        // filterType & filterValue
                        if (p.customObjectFilterType.getValue().isEmpty()) {
                            vr.setStatus(Result.ERROR);
                            vr.setMessage(messages.getMessage("error.validation.customobject.filtertype"));
                            return vr;
                        }
                        if (p.customObjectFilterValues.getValue().isEmpty()) {
                            vr.setStatus(Result.ERROR);
                            vr.setMessage(messages.getMessage("error.validation.customobject.filtervalues"));
                            return vr;
                        }
                    }
                }
                // list no checking...
                if (p.customObjectAction.getValue().equals(CustomObjectAction.list)) {
                    if (isDynamic) {
                        vr.setStatus(Result.ERROR);
                        vr.setMessage(messages.getMessage("error.validation.operation.dynamicschema"));
                        return vr;
                    }
                }
                // describe
                if (p.customObjectAction.getValue().equals(CustomObjectAction.describe)) {
                    if (isDynamic) {
                        vr.setStatus(Result.ERROR);
                        vr.setMessage(messages.getMessage("error.validation.operation.dynamicschema"));
                        return vr;
                    }
                    if (p.customObjectName.getValue().isEmpty()) {
                        vr.setStatus(Result.ERROR);
                        vr.setMessage(messages.getMessage("error.validation.customobject.customobjectname"));
                        return vr;
                    }
                }
            }
            if (p.batchSize.getValue() < 1 || p.batchSize.getValue() > REST_API_LIMIT) {
                p.batchSize.setValue(REST_API_LIMIT);
                LOG.info(messages.getMessage("error.validation.batchSize.range", REST_API_LIMIT));
            }
        }
        // BulkExec
        if (properties instanceof TMarketoBulkExecProperties) {
            TMarketoBulkExecProperties p = (TMarketoBulkExecProperties) properties;
            if (StringUtils.isEmpty(p.bulkFilePath.getValue())) {
                vr.setStatus(Result.ERROR);
                vr.setMessage(messages.getMessage("error.validation.sink.bulk.bulkfilepath"));
                return vr;
            }
            Path tmpPath = Paths.get(p.bulkFilePath.getValue());
            if (!Files.exists(tmpPath)) {
                vr.setStatus(Result.ERROR);
                vr.setMessage(messages.getMessage("error.validation.sink.bulk.bulkfilepath.notexists"));
                return vr;
            }
            if (StringUtils.isEmpty(p.logDownloadPath.getValue())) {
                vr.setStatus(Result.ERROR);
                vr.setMessage(messages.getMessage("error.validation.sink.bulk.logdownloadpath"));
                return vr;
            }
            tmpPath = Paths.get(p.logDownloadPath.getValue());
            if (!Files.isDirectory(tmpPath)) {
                vr.setStatus(Result.ERROR);
                vr.setMessage(messages.getMessage("error.validation.sink.bulk.logdownloadpath"));
                return vr;
            }
            if (p.bulkImportTo.getValue().equals(BulkImportTo.Leads)) {
                if (StringUtils.isEmpty(p.lookupField.getValue().name())) {
                    vr.setStatus(Result.ERROR);
                    vr.setMessage(messages.getMessage("error.validation.bulk.lookupfield"));
                    return vr;
                }
            } else {
                if (StringUtils.isEmpty(p.customObjectName.getValue())) {
                    vr.setStatus(Result.ERROR);
                    vr.setMessage(messages.getMessage("error.validation.customobject.customobjectname"));
                    return vr;
                }
            }
        }
        // Campaign
        if (properties instanceof TMarketoCampaignProperties) {
            TMarketoCampaignProperties p = (TMarketoCampaignProperties) properties;
            if (p.batchSize.getValue() < 1 || p.batchSize.getValue() > REST_API_LIMIT) {
                p.batchSize.setValue(REST_API_LIMIT);
                LOG.info(messages.getMessage("error.validation.batchSize.range", REST_API_LIMIT));
            }
        }
        //
        return vr;
    }

    @Override
    public BoundedReader createReader(RuntimeContainer adaptor) {
        if (properties instanceof TMarketoInputProperties) {
            return new MarketoInputReader(adaptor, this, (TMarketoInputProperties) properties);
        }
        if (properties instanceof TMarketoBulkExecProperties) {
            return new MarketoBulkExecReader(adaptor, this, (TMarketoBulkExecProperties) properties);
        }
        if (properties instanceof TMarketoCampaignProperties) {
            return new MarketoCampaignReader(adaptor, this, (TMarketoCampaignProperties) properties);
        }
        return null;
    }
}
