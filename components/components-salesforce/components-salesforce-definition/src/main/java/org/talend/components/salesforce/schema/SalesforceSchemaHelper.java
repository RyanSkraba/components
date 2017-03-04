package org.talend.components.salesforce.schema;

import java.io.IOException;

/**
 * Created by pavlo.fandych on 1/30/2017.
 */

public interface SalesforceSchemaHelper<T> {

    public T guessSchema(String soqlQuery) throws IOException;

	public String guessQuery(T t, String entityName);
}