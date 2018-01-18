// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.marklogic.runtime.input;

import java.io.IOException;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.Instant;
import org.talend.components.api.component.runtime.AbstractBoundedReader;
import org.talend.components.api.component.runtime.BoundedSource;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.marklogic.connection.MarkLogicConnection;
import org.talend.components.marklogic.exceptions.MarkLogicErrorCode;
import org.talend.components.marklogic.exceptions.MarkLogicException;
import org.talend.components.marklogic.runtime.input.strategies.DocContentReader;
import org.talend.components.marklogic.tmarklogicinput.MarkLogicInputProperties;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.admin.QueryOptionsManager;
import com.marklogic.client.document.DocumentManager;
import com.marklogic.client.io.Format;
import com.marklogic.client.io.SearchHandle;
import com.marklogic.client.io.StringHandle;
import com.marklogic.client.query.MatchDocumentSummary;
import com.marklogic.client.query.QueryManager;
import com.marklogic.client.query.StringQueryDefinition;

public class MarkLogicCriteriaReader extends AbstractBoundedReader<IndexedRecord> {

    private RuntimeContainer container;

    private Setting settings;

    private DatabaseClient connectionClient;

    private IndexedRecord current;

    private ResultWithLongNB result;

    private Schema.Field docContentField;

    private DocumentManager docManager;

    private SearchHandle searchHandle;

    private long matchedDocuments;

    private int maxRetrieve;

    private long pageSize;

    private long documentCounter;

    private int pageCounter;

    private MatchDocumentSummary[] currentPage;

    DocContentReader docContentReader;

    private QueryManager queryManager;

    private StringQueryDefinition stringQueryDefinition;

    public MarkLogicCriteriaReader(BoundedSource source, RuntimeContainer container, ComponentProperties inputProperties) {
        super(source);
        this.container = container;
        this.settings = prepareSettings(inputProperties);
    }

    protected Setting prepareSettings(ComponentProperties inputProperties) {
        MarkLogicInputProperties properties = (MarkLogicInputProperties) inputProperties;
        return new Setting(
                properties.datasetProperties.main.schema.getValue(),
                properties.datasetProperties.criteria.getValue(),
                properties.maxRetrieve.getValue(), properties.datasetProperties.pageSize.getValue(),
                properties.datasetProperties.useQueryOption.getValue(), properties.datasetProperties.queryLiteralType.getValue(),
                properties.datasetProperties.queryOptionName.getValue(), properties.datasetProperties.queryOptionLiterals.getValue(),
                properties.datasetProperties.getDatastoreProperties().isReferencedConnectionUsed());
    }

    @Override
    public boolean start() throws IOException {
        MarkLogicConnection connection = (MarkLogicConnection) getCurrentSource();
        result = new ResultWithLongNB();
        connectionClient = connection.connect(container);
        if (connectionClient == null) {
            return false;
        }
        docManager = connectionClient.newDocumentManager();

        boolean isDocContentFieldPresent = (settings.outputSchema.getFields().size() >= 2);
        if (isDocContentFieldPresent) {
            docContentField = settings.outputSchema.getFields().get(1);
        }
        docContentReader = new DocContentReader(docManager, settings.outputSchema, docContentField);
        if (settings.useQueryOption && StringUtils.isNotEmpty(settings.queryOptionName)) {
            prepareQueryOption();
        }
        queryManager = connectionClient.newQueryManager();
        stringQueryDefinition = (settings.useQueryOption) ?
                queryManager.newStringDefinition(settings.queryOptionName) : queryManager.newStringDefinition();

        stringQueryDefinition.setCriteria(settings.criteria);

        searchHandle = queryManager.search(stringQueryDefinition, new SearchHandle());


        matchedDocuments = searchHandle.getTotalResults();

        pageSize = (settings.pageSize <= 0) ? matchedDocuments : settings.pageSize;
        maxRetrieve = settings.maxRetrieve; //if < 0 - it will be ignored
        queryManager.setPageLength(pageSize);
        documentCounter = 1;

        readNextPage();

        return (matchedDocuments > 0);
    }

    private void prepareQueryOption() {
        QueryOptionsManager qryOptMgr = connectionClient.newServerConfigManager().newQueryOptionsManager();
        if (StringUtils.isNotEmpty(settings.queryOptionLiterals)) {
            StringHandle strHandle = new StringHandle();
            switch (settings.queryLiteralType) {
                case "JSON": {
                    strHandle.withFormat(Format.JSON);
                    break;
                }
                case "XML": {
                    strHandle.withFormat(Format.XML);
                    break;
                }
            }

            strHandle.set(settings.queryOptionLiterals);
            qryOptMgr.writeOptions(settings.queryOptionName, strHandle);
        }
    }

    @Override
    public boolean advance() throws IOException {
        if (pageCounter >= pageSize) {
            readNextPage();
            pageCounter = 0;
        }
        return maxRetrieve > 0 ?
                (documentCounter <= maxRetrieve) && (documentCounter <= matchedDocuments) :
                documentCounter <= matchedDocuments;
    }

    private void readNextPage() {
        queryManager.search(stringQueryDefinition, searchHandle, documentCounter);
        currentPage = searchHandle.getMatchResults();
    }

    @Override
    public IndexedRecord getCurrent() throws NoSuchElementException {
        ++documentCounter;
        MatchDocumentSummary currentSummary = currentPage[pageCounter];
        current = new GenericData.Record(settings.outputSchema);
        try {
            String docId = currentSummary.getUri();
            current = docContentReader.readDocument(docId);

            result.totalCountLong++;
            result.successCountLong++;
            pageCounter++;
            return current;
        } catch (Exception e) {
            throw new MarkLogicException(new MarkLogicErrorCode("Can't read document from MarkLogic database"), e);
        }
    }

    @Override
    public Instant getCurrentTimestamp() throws NoSuchElementException {
        return Instant.now();
    }

    @Override
    public void close() throws IOException {
        if (!settings.isReferencedConnectionUsed) {
            connectionClient.release();
        }
    }

    @Override
    public Map<String, Object> getReturnValues() {
        return result.toMap();
    }

    protected class Setting {

        private final Schema outputSchema;

        private final String criteria;

        private final int maxRetrieve;

        private final int pageSize;

        private final boolean useQueryOption;

        private final String queryLiteralType;

        private final String queryOptionLiterals;

        private final String queryOptionName;

        private final boolean isReferencedConnectionUsed;

        public Setting(Schema outputSchema, String criteria, int maxRetrieve, int pageSize,
                boolean useQueryOption, String queryLiteralType, String queryOptionName, String queryOptionLiterals, boolean isReferencedConnectionUsed) {
            this.outputSchema = outputSchema;
            this.criteria = criteria;
            this.maxRetrieve = maxRetrieve;
            this.pageSize = pageSize;
            this.useQueryOption = useQueryOption;
            this.queryLiteralType = queryLiteralType;
            this.queryOptionLiterals = queryOptionLiterals;
            this.queryOptionName = queryOptionName;

            this.isReferencedConnectionUsed = isReferencedConnectionUsed;
        }
    }
}
