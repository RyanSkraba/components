package org.talend.components.dataprep.util;

import java.util.HashSet;
import java.util.Set;

import org.apache.avro.Schema;
import org.junit.Assert;
import org.junit.Test;
import org.talend.components.dataprep.connection.DataPrepField;
import org.talend.components.dataprep.runtime.DataPrepAvroRegistry;
import org.talend.daikon.avro.AvroRegistry;

public class NameUtilTest {

    private String[] wrongNames = { "P1_Vente_Qté", "P1_Vente_PVEscpteNet", "P1_Vente_PVEscpteBrut", "TVA", "CA HT",
            "Numéro de ticket", "Article unique", "N° référence", "Désignation", "Photo n°", "Date_PV", "éà", "+" };

    @Test
    public void testCorrectInAvroRegistry() {
        DataPrepField[] fields = new DataPrepField[wrongNames.length];

        int i = 0;
        for (String wrongName : wrongNames) {
            fields[i] = new DataPrepField(wrongName, "string", null);
            i++;
        }

        AvroRegistry avroRegistry = DataPrepAvroRegistry.getDataPrepInstance();
        Schema schema = avroRegistry.inferSchema(fields);
        Assert.assertNotNull(schema);

        Set<String> set = new HashSet<String>();
        for (Schema.Field field : schema.getFields()) {
            String name = field.name();
            Assert.assertTrue("too many underline", countUnderLine(name) <= (name.length() / 2));
            set.add(name);
        }
        Assert.assertEquals("after the correct, we miss some one or create a duplicated one, please check the NameUtil class",
                wrongNames.length, set.size());
    }

    @Test
    public void testCorrect() {
        Set<String> previousNames = new HashSet<String>();
        int i = 0;
        for (String each : wrongNames) {
            String name = NameUtil.correct(each, i++, previousNames);
            Assert.assertTrue("too many underline", countUnderLine(name) <= (name.length() / 2));
            previousNames.add(name);
        }
        Assert.assertEquals("after the correct, we miss some one or create a duplicated one, please check the NameUtil class",
                wrongNames.length, previousNames.size());
    }

    private int countUnderLine(String str) {
        int result = 0;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '_') {
                result++;
            }
        }
        return result;
    }

}
