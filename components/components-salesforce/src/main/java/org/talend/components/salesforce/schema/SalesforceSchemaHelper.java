package org.talend.components.salesforce.schema;

import java.io.IOException;

import com.sforce.ws.ConnectionException;

/**
 * Created by pavlo.fandych on 1/30/2017.
 */

public interface SalesforceSchemaHelper<T> {

    public T guessSchema(String soqlQuery) throws ConnectionException, IOException;
}