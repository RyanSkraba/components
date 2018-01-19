package org.talend.components.jdbc.query;

import org.talend.components.jdbc.CommonUtils;
import org.talend.components.jdbc.runtime.setting.AllSetting;

public class QueryUtils {

    public static String generateNewQuery(final String dbType, final String databaseDisplayed/* not used now */,
            final String dbschemaDisplayed/* not used now */,
            final String tableDisplayed/* "mytable" or context.mytable or more complex */,
            final AllSetting setting/*
                                     * all values in it already be converted to real value, for example, context.id is changed to "myid"
                                     * already
                                     */) {
        final String realDbType = CommonUtils.getRealDBType(setting, dbType);

        IQueryGenerator generator = GenerateQueryFactory.getGenerator(realDbType);
        generator.setParameters(databaseDisplayed, dbschemaDisplayed, tableDisplayed, setting);
        return generator.generateQuery();
    }

}
