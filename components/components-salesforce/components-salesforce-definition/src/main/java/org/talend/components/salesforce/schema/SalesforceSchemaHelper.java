package org.talend.components.salesforce.schema;

import java.io.IOException;

/**
 * Created by pavlo.fandych on 1/30/2017.
 */
public interface SalesforceSchemaHelper<T> {

    T guessSchema(String soqlQuery) throws IOException;

    String guessQuery(T t, String entityName);
}