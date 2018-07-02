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
package org.talend.components.simplefileio.runtime;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.talend.components.test.RecordSetUtil.getEmptyTestData;
import static org.talend.components.test.RecordSetUtil.getSimpleTestData;
import static org.talend.components.test.RecordSetUtil.writeCsvFile;
import static org.talend.components.test.RecordSetUtil.writeRandomAvroFile;
import static org.talend.components.test.RecordSetUtil.writeRandomCsvFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.generic.IndexedRecord;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.fs.Path;
import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.talend.components.common.dataset.DatasetDefinition;
import org.talend.components.simplefileio.ExcelFormat;
import org.talend.components.simplefileio.SimpleFileIODatasetDefinition;
import org.talend.components.simplefileio.SimpleFileIODatasetProperties;
import org.talend.components.simplefileio.SimpleFileIOFormat;
import org.talend.components.simplefileio.local.EncodingType;
import org.talend.components.test.MiniDfsResource;
import org.talend.components.test.RecordSet;
import org.talend.daikon.java8.Consumer;
import org.talend.daikon.runtime.RuntimeUtil;

/**
 * Unit tests for {@link SimpleFileIODatasetRuntime}.
 */
public class SimpleFileIODatasetRuntimeTest {

    @Rule
    public MiniDfsResource mini = new MiniDfsResource();

    /**
     * Instance to test. Definitions are immutable.
     */
    private final DatasetDefinition<?> def = new SimpleFileIODatasetDefinition();

    static {
        RuntimeUtil.registerMavenUrlHandler();
    }

    /**
     * @return the properties for this dataset, fully initialized with the default values.
     */
    public static SimpleFileIODatasetProperties createDatasetProperties() {
        // Configure the dataset.
        SimpleFileIODatasetProperties datasetProps = new SimpleFileIODatasetProperties(null);
        datasetProps.init();
        datasetProps.setDatastoreProperties(SimpleFileIODatastoreRuntimeTest.createDatastoreProperties());
        return datasetProps;
    }

    @Test
    public void testGetSchema() throws Exception {
        writeRandomAvroFile(mini.getFs(), "/user/test/input.avro", getSimpleTestData(0));
        String fileSpec = mini.getFs().getUri().resolve("/user/test/input.avro").toString();

        // Configure the component.
        SimpleFileIODatasetProperties props = createDatasetProperties();
        props.format.setValue(SimpleFileIOFormat.AVRO);
        props.path.setValue(fileSpec);

        // Create the runtime.
        SimpleFileIODatasetRuntime runtime = new SimpleFileIODatasetRuntime();
        runtime.initialize(null, props);

        // Attempt to get a sample using the runtime methods.
        Schema actual = runtime.getSchema();

        assertThat(actual, notNullValue());
        // TODO(rskraba): check the schema with the input file.
    }

    @Test
    public void testGetSchemaEmptyCsvFile() throws Exception {
        writeRandomCsvFile(mini.getFs(), "/user/test/empty.csv", getEmptyTestData(), "UTF-8");
        String fileSpec = mini.getFs().getUri().resolve("/user/test/empty.csv").toString();

        // Configure the component.
        SimpleFileIODatasetProperties props = createDatasetProperties();
        props.format.setValue(SimpleFileIOFormat.CSV);
        props.path.setValue(fileSpec);

        // Create the runtime.
        SimpleFileIODatasetRuntime runtime = new SimpleFileIODatasetRuntime();
        runtime.initialize(null, props);

        // Attempt to get a sample using the runtime methods.
        Schema actual = runtime.getSchema();

        assertThat(actual, notNullValue());
        // TODO(rskraba): check the schema with the input file.
    }

    @Test
    public void testGetSampleCsv() throws Exception {
        RecordSet rs = getSimpleTestData(0);
        writeRandomCsvFile(mini.getFs(), "/user/test/input5.csv", rs, "UTF-8");
        String fileSpec = mini.getFs().getUri().resolve("/user/test/input5.csv").toString();

        // Configure the component.
        SimpleFileIODatasetProperties props = createDatasetProperties();
        props.format.setValue(SimpleFileIOFormat.CSV);
        props.path.setValue(fileSpec);

        final List<IndexedRecord> actual = getSample(props,Integer.MAX_VALUE);

        // Check the expected values match.
        assertThat(actual, hasSize(9));
        // assertThat(actual, (Matcher) equalTo(rs.getAllData()));
    }
    
    @Test
    public void testGetSampleCsv_header() throws Exception {
        RecordSet rs = getSimpleTestData(0);
        writeRandomCsvFile(mini.getFs(), "/user/test/input4.csv", rs, "UTF-8");
        String fileSpec = mini.getFs().getUri().resolve("/user/test/input4.csv").toString();

        // Configure the component.
        SimpleFileIODatasetProperties props = createDatasetProperties();
        props.format.setValue(SimpleFileIOFormat.CSV);
        props.path.setValue(fileSpec);
        props.setHeaderLine.setValue(true);
        props.headerLine.setValue(3);

        final List<IndexedRecord> actual = getSample(props,100);

        assertThat(actual, hasSize(7));
    }
    
    @Test
    public void testGetSampleCsv_encoding() throws Exception {
        RecordSet rs = getSimpleTestData(0);
        writeRandomCsvFile(mini.getFs(), "/user/test/input3.csv", rs, "GBK");
        String fileSpec = mini.getFs().getUri().resolve("/user/test/input3.csv").toString();

        // Configure the component.
        SimpleFileIODatasetProperties props = createDatasetProperties();
        props.format.setValue(SimpleFileIOFormat.CSV);
        props.path.setValue(fileSpec);
        props.encoding.setValue(EncodingType.OTHER);
        props.specificEncoding.setValue("GBK");

        final List<IndexedRecord> actual = getSample(props,100);

        assertThat(actual, hasSize(9));
    }
    
    @Test
    public void testGetSampleCsv_encoding_header() throws Exception {
        RecordSet rs = getSimpleTestData(0);
        writeRandomCsvFile(mini.getFs(), "/user/test/input2.csv", rs, "GBK");
        String fileSpec = mini.getFs().getUri().resolve("/user/test/input2.csv").toString();

        // Configure the component.
        SimpleFileIODatasetProperties props = createDatasetProperties();
        props.format.setValue(SimpleFileIOFormat.CSV);
        props.path.setValue(fileSpec);
        props.encoding.setValue(EncodingType.OTHER);
        props.specificEncoding.setValue("GBK");
        props.setHeaderLine.setValue(true);
        props.headerLine.setValue(1);

        final List<IndexedRecord> actual = getSample(props,100);

        assertThat(actual, hasSize(9));
    }
    
    @Test
    public void testGetSampleCsv_textEnclosure() throws Exception {
        String content = "\"wang;wei\";Beijing;100\n\"gao\nyan\";Beijing;99\ndabao;Beijing;98\n";
        writeCsvFile(mini.getFs(), "/user/test/input1.csv", content, "UTF-8");
        String fileSpec = mini.getFs().getUri().resolve("/user/test/input1.csv").toString();
  
        // Configure the component.
        SimpleFileIODatasetProperties props = createDatasetProperties();
        props.format.setValue(SimpleFileIOFormat.CSV);
        props.path.setValue(fileSpec);
        props.textEnclosureCharacter.setValue("\"");
  
        // Create the runtime.
        SimpleFileIODatasetRuntime runtime = new SimpleFileIODatasetRuntime();
        runtime.initialize(null, props);
  
        // Attempt to get a sample using the runtime methods.
        final List<IndexedRecord> actual = new ArrayList<>();
        runtime.getSample(100, new Consumer<IndexedRecord>() {
  
            @Override
            public void accept(IndexedRecord ir) {
                assertThat(ir.getSchema().getFields(), hasSize(3));
                actual.add(ir);
            }
        });
  
        assertThat(actual, hasSize(2));
    }
    
    @Test
    public void testGetSampleCsv_escape() throws Exception {
        String content = "wang\\;wei;Beijing;100\ngaoyan;Beijing;99\ndabao;Beijing;98\n";
        writeCsvFile(mini.getFs(), "/user/test/input6.csv", content, "UTF-8");
        String fileSpec = mini.getFs().getUri().resolve("/user/test/input6.csv").toString();
  
        // Configure the component.
        SimpleFileIODatasetProperties props = createDatasetProperties();
        props.format.setValue(SimpleFileIOFormat.CSV);
        props.path.setValue(fileSpec);
        props.escapeCharacter.setValue("\\");
  
        // Create the runtime.
        SimpleFileIODatasetRuntime runtime = new SimpleFileIODatasetRuntime();
        runtime.initialize(null, props);
  
        // Attempt to get a sample using the runtime methods.
        final List<IndexedRecord> actual = new ArrayList<>();
        runtime.getSample(100, new Consumer<IndexedRecord>() {
  
            @Override
            public void accept(IndexedRecord ir) {
                assertThat(ir.getSchema().getFields(), hasSize(3));
                actual.add(ir);
            }
        });
  
        assertThat(actual, hasSize(2));
    }
    
    @Test
    public void testGetSampleCsv_textEnclosureAndEscape() throws Exception {
        String content = "\"wa\\\"ng;wei\";Bei\\\"jing;100\n\"gao\nyan\";Bei\\\"jing;99\ndabao;Bei\\\"jing;98\n";
        writeCsvFile(mini.getFs(), "/user/test/input7.csv", content, "UTF-8");
        String fileSpec = mini.getFs().getUri().resolve("/user/test/input7.csv").toString();
  
        // Configure the component.
        SimpleFileIODatasetProperties props = createDatasetProperties();
        props.format.setValue(SimpleFileIOFormat.CSV);
        props.path.setValue(fileSpec);
        props.textEnclosureCharacter.setValue("\"");
        props.escapeCharacter.setValue("\\");
  
        // Create the runtime.
        SimpleFileIODatasetRuntime runtime = new SimpleFileIODatasetRuntime();
        runtime.initialize(null, props);
  
        // Attempt to get a sample using the runtime methods.
        final List<IndexedRecord> actual = new ArrayList<>();
        runtime.getSample(100, new Consumer<IndexedRecord>() {
  
            @Override
            public void accept(IndexedRecord ir) {
                assertThat(ir.getSchema().getFields(), hasSize(3));
                actual.add(ir);
            }
        });
  
        assertThat(actual, hasSize(2));
    }

    @Test
    public void testGetSampleCsv_multipleSources() throws Exception {
        RecordSet rs1 = getSimpleTestData(0);
        writeRandomCsvFile(mini.getFs(), "/user/test/input/part-00000", rs1, "UTF-8");
        RecordSet rs2 = getSimpleTestData(100);
        writeRandomCsvFile(mini.getFs(), "/user/test/input/part-00001", rs2, "UTF-8");
        RecordSet rs3 = getSimpleTestData(100);
        writeRandomCsvFile(mini.getFs(), "/user/test/input/part-00002", rs3, "UTF-8");
        String fileSpec = mini.getFs().getUri().resolve("/user/test/input/").toString();

        // Configure the component.
        SimpleFileIODatasetProperties props = createDatasetProperties();
        props.format.setValue(SimpleFileIOFormat.CSV);
        props.path.setValue(fileSpec);

        // Create the runtime.
        SimpleFileIODatasetRuntime runtime = new SimpleFileIODatasetRuntime();
        runtime.initialize(null, props);

        // Attempt to get a sample using the runtime methods.
        {
            final List<IndexedRecord> actual = new ArrayList<>();
            runtime.getSample(15, new Consumer<IndexedRecord>() {

                @Override
                public void accept(IndexedRecord ir) {
                    actual.add(ir);
                }
            });

            // Check that the result was limited (15 out of 30 records)
            assertThat(actual, hasSize(15));
        }

        // Run it again to verify that the static state is not retained.
        {
            final List<IndexedRecord> actual = new ArrayList<>();
            runtime.getSample(15, new Consumer<IndexedRecord>() {

                @Override
                public void accept(IndexedRecord ir) {
                    actual.add(ir);
                }
            });
            assertThat(actual, hasSize(15));
        }
    }

    @Test
    public void testGetSampleAvro() throws Exception {
        RecordSet rs = getSimpleTestData(0);
        writeRandomAvroFile(mini.getFs(), "/user/test/input.avro", rs);
        String fileSpec = mini.getFs().getUri().resolve("/user/test/input.avro").toString();

        // Configure the component.
        SimpleFileIODatasetProperties props = createDatasetProperties();
        props.format.setValue(SimpleFileIOFormat.AVRO);
        props.path.setValue(fileSpec);

        final List<IndexedRecord> actual = getSample(props,100);

        // Check the expected values.
        assertThat(actual, (Matcher) equalTo(rs.getAllData()));
    }
    
    @Test
    public void testGetSampleExcelHtml() throws Exception {
        String fileSpec = sourceFilePrepare("sales-force.html");

        // Configure the component.
        SimpleFileIODatasetProperties props = createDatasetProperties();
        props.path.setValue(fileSpec);
        props.format.setValue(SimpleFileIOFormat.EXCEL);
        props.excelFormat.setValue(ExcelFormat.HTML);

        final List<IndexedRecord> actual = getSample(props,100);

        assertThat(actual, hasSize(100));
        List<Field> fields = actual.get(0).getSchema().getFields();
        assertThat(fields, hasSize(7));
        assertThat("UID", equalTo(fields.get(0).name()));
        assertThat("Hire_Date", equalTo(fields.get(6).name()));
        
        assertThat("000001", equalTo(actual.get(0).get(0)));
        assertThat("France", equalTo(actual.get(0).get(5)));
    }

    private List<IndexedRecord> getSample(SimpleFileIODatasetProperties props, int limit) {
      // Create the runtime.
      SimpleFileIODatasetRuntime runtime = new SimpleFileIODatasetRuntime();
      runtime.initialize(null, props);

      // Attempt to get a sample using the runtime methods.
      final List<IndexedRecord> actual = new ArrayList<>();
      runtime.getSample(limit, new Consumer<IndexedRecord>() {

          @Override
          public void accept(IndexedRecord ir) {
              actual.add(ir);
          }
      });
      return actual;
    }

    private String sourceFilePrepare(String filename) throws IOException {
        InputStream in = getClass().getResourceAsStream(filename);
        try (OutputStream inOnMinFS = mini.getFs().create(new Path("/user/test/" + filename))) {
            inOnMinFS.write(IOUtils.toByteArray(in));
        }
        String fileSpec = mini.getFs().getUri().resolve("/user/test/" + filename).toString();
        return fileSpec;
    }
    
    @Test
    public void testGetSampleExcelHtml_header() throws Exception {
        String fileSpec = sourceFilePrepare("sales-force.html");

        // Configure the component.
        SimpleFileIODatasetProperties props = createDatasetProperties();
        props.path.setValue(fileSpec);
        props.format.setValue(SimpleFileIOFormat.EXCEL);
        props.excelFormat.setValue(ExcelFormat.HTML);
        props.setHeaderLine.setValue(true);
        props.headerLine.setValue(900);

        final List<IndexedRecord> actual = getSample(props,100);

        assertThat(actual, hasSize(47));
        List<Field> fields = actual.get(0).getSchema().getFields();
        assertThat(fields, hasSize(7));
        assertThat("field0", equalTo(fields.get(0).name()));
        assertThat("field6", equalTo(fields.get(6).name()));
        
        assertThat("000931", equalTo(actual.get(0).get(0)));
        assertThat("", equalTo(actual.get(0).get(5)));
    }
    
    @Test
    public void testGetSampleExcelHtml_header_footer() throws Exception {
        String fileSpec = sourceFilePrepare("sales-force.html");
        
        // Configure the component.
        SimpleFileIODatasetProperties props = createDatasetProperties();
        props.path.setValue(fileSpec);
        props.format.setValue(SimpleFileIOFormat.EXCEL);
        props.excelFormat.setValue(ExcelFormat.HTML);
        props.setHeaderLine.setValue(true);
        props.headerLine.setValue(900);
        props.setFooterLine.setValue(true);
        props.footerLine.setValue(1);

        final List<IndexedRecord> actual = getSample(props,100);

        assertThat(actual, hasSize(46));
        List<Field> fields = actual.get(0).getSchema().getFields();
        assertThat(fields, hasSize(7));
        assertThat("field0", equalTo(fields.get(0).name()));
        assertThat("field6", equalTo(fields.get(6).name()));
        
        assertThat("000931", equalTo(actual.get(0).get(0)));
        assertThat("", equalTo(actual.get(0).get(5)));
    }
    
    @Test
    public void testGetSampleExcel_emptyrow() throws Exception {
        String fileSpec = sourceFilePrepare("emptyrowexist.xlsx");
        
        // Configure the component.
        SimpleFileIODatasetProperties props = createDatasetProperties();
        props.path.setValue(fileSpec);
        props.format.setValue(SimpleFileIOFormat.EXCEL);
        props.excelFormat.setValue(ExcelFormat.EXCEL2007);

        final List<IndexedRecord> actual = getSample(props,1000);

        assertThat(actual, hasSize(199));
        List<Field> fields = actual.get(0).getSchema().getFields();
        assertThat(fields, hasSize(5));
    }
    
    @Test
    public void testGetSampleExcel2007_TDI_40654() throws Exception {
        String fileSpec = sourceFilePrepare("emptyfield.xlsx");
        
        // Configure the component.
        SimpleFileIODatasetProperties props = createDatasetProperties();
        props.path.setValue(fileSpec);
        props.format.setValue(SimpleFileIOFormat.EXCEL);
        props.excelFormat.setValue(ExcelFormat.EXCEL2007);

        final List<IndexedRecord> actual = getSample(props,100);

        assertThat(actual, hasSize(3));
        
        List<Field> fields = actual.get(0).getSchema().getFields();
        assertThat(fields, hasSize(8));
        assertThat("field0", equalTo(fields.get(0).name()));
        assertThat("field7", equalTo(fields.get(7).name()));
        
        assertThat("", equalTo(actual.get(0).get(0)));
        assertThat("2", equalTo(actual.get(0).get(1)));
        assertThat("false", equalTo(actual.get(0).get(2)));
        assertThat("", equalTo(actual.get(0).get(3)));
        assertThat("3.4", equalTo(actual.get(0).get(4)));
        assertThat("2018-04-26", equalTo(actual.get(0).get(5)));
        assertThat("", equalTo(actual.get(0).get(6)));
        assertThat("TDI-T3_V1", equalTo(actual.get(0).get(7)));
    }
    
    @Test
    public void testGetSampleExcel97_TDI_40654() throws Exception {
        String fileSpec = sourceFilePrepare("emptyfield.xls");
        
        // Configure the component.
        SimpleFileIODatasetProperties props = createDatasetProperties();
        props.path.setValue(fileSpec);
        props.format.setValue(SimpleFileIOFormat.EXCEL);
        props.excelFormat.setValue(ExcelFormat.EXCEL97);

        final List<IndexedRecord> actual = getSample(props,100);

        assertThat(actual, hasSize(3));
        
        List<Field> fields = actual.get(0).getSchema().getFields();
        assertThat(fields, hasSize(8));
        assertThat("field0", equalTo(fields.get(0).name()));
        assertThat("field7", equalTo(fields.get(7).name()));
        
        assertThat("", equalTo(actual.get(0).get(0)));
        assertThat("2", equalTo(actual.get(0).get(1)));
        assertThat("false", equalTo(actual.get(0).get(2)));
        assertThat("", equalTo(actual.get(0).get(3)));
        assertThat("3.4", equalTo(actual.get(0).get(4)));
        assertThat("2018-04-26", equalTo(actual.get(0).get(5)));
        assertThat("", equalTo(actual.get(0).get(6)));
        assertThat("TDI-T3_V1", equalTo(actual.get(0).get(7)));
    }
    
    @Test
    public void testGetSampleExcel97() throws Exception {
        String fileSpec = sourceFilePrepare("basic.xls");
        
        // Configure the component.
        SimpleFileIODatasetProperties props = createDatasetProperties();
        props.path.setValue(fileSpec);
        props.format.setValue(SimpleFileIOFormat.EXCEL);
        props.excelFormat.setValue(ExcelFormat.EXCEL97);
        props.sheet.setValue("Sheet1");

        final List<IndexedRecord> actual = getSample(props,100);

        assertThat(actual, hasSize(2));
        List<Field> fields = actual.get(0).getSchema().getFields();
        assertThat(fields, hasSize(3));
        
        assertThat("2", equalTo(actual.get(0).get(0)));
        assertThat("gaoyan", equalTo(actual.get(0).get(1)));
        assertThat("Shunyi", equalTo(actual.get(0).get(2)));
    }
    
    @Test
    public void testGetSampleExcel() throws Exception {
        String fileSpec = sourceFilePrepare("basic.xlsx");
        
        // Configure the component.
        SimpleFileIODatasetProperties props = createDatasetProperties();
        props.path.setValue(fileSpec);
        props.format.setValue(SimpleFileIOFormat.EXCEL);
        props.sheet.setValue("Sheet1");

        final List<IndexedRecord> actual = getSample(props,100);

        assertThat(actual, hasSize(2));
        List<Field> fields = actual.get(0).getSchema().getFields();
        assertThat(fields, hasSize(3));
        
        assertThat("2", equalTo(actual.get(0).get(0)));
        assertThat("gaoyan", equalTo(actual.get(0).get(1)));
        assertThat("Shunyi", equalTo(actual.get(0).get(2)));
    }
    
    @Test
    public void testGetSampleExcel_no_sheet() throws Exception {
        String fileSpec = sourceFilePrepare("basic.xlsx");

        // Configure the component.
        SimpleFileIODatasetProperties props = createDatasetProperties();
        props.path.setValue(fileSpec);
        props.format.setValue(SimpleFileIOFormat.EXCEL);

        final List<IndexedRecord> actual = getSample(props,100);

        assertThat(actual, hasSize(2));
        List<Field> fields = actual.get(0).getSchema().getFields();
        assertThat(fields, hasSize(3));
        
        assertThat("2", equalTo(actual.get(0).get(0)));
        assertThat("gaoyan", equalTo(actual.get(0).get(1)));
        assertThat("Shunyi", equalTo(actual.get(0).get(2)));
    }
    
    @Test
    public void testGetSampleExcel_header() throws Exception {
        String fileSpec = sourceFilePrepare("basic.xlsx");

        // Configure the component.
        SimpleFileIODatasetProperties props = createDatasetProperties();
        props.path.setValue(fileSpec);
        props.format.setValue(SimpleFileIOFormat.EXCEL);
        props.sheet.setValue("Sheet1");
        props.setHeaderLine.setValue(true);

        final List<IndexedRecord> actual = getSample(props,100);

        assertThat(actual, hasSize(2));
        List<Field> fields = actual.get(0).getSchema().getFields();
        assertThat(fields, hasSize(3));
        
        assertThat("2", equalTo(actual.get(0).get(0)));
        assertThat("gaoyan", equalTo(actual.get(0).get(1)));
        assertThat("Shunyi", equalTo(actual.get(0).get(2)));
    }
    
    @Test
    public void testGetSampleExcel_footer() throws Exception {
        String fileSpec = sourceFilePrepare("basic.xlsx");

        // Configure the component.
        SimpleFileIODatasetProperties props = createDatasetProperties();
        props.path.setValue(fileSpec);
        props.format.setValue(SimpleFileIOFormat.EXCEL);
        props.sheet.setValue("Sheet1");
        props.setHeaderLine.setValue(false);
        props.setFooterLine.setValue(true);
        props.footerLine.setValue(1);

        final List<IndexedRecord> actual = getSample(props,100);

        assertThat(actual, hasSize(2));
        List<Field> fields = actual.get(0).getSchema().getFields();
        assertThat(fields, hasSize(3));
        
        assertThat("1", equalTo(actual.get(0).get(0)));
        assertThat("wangwei", equalTo(actual.get(0).get(1)));
        assertThat("Shunyi", equalTo(actual.get(0).get(2)));
    }
    
    @Test
    public void testGetSampleExcel_header_footer() throws Exception {
        String fileSpec = sourceFilePrepare("basic.xlsx");

        // Configure the component.
        SimpleFileIODatasetProperties props = createDatasetProperties();
        props.path.setValue(fileSpec);
        props.format.setValue(SimpleFileIOFormat.EXCEL);
        props.sheet.setValue("Sheet1");
        props.setHeaderLine.setValue(true);
        props.setFooterLine.setValue(true);
        props.footerLine.setValue(1);

        final List<IndexedRecord> actual = getSample(props,100);

        assertThat(actual, hasSize(1));
        List<Field> fields = actual.get(0).getSchema().getFields();
        assertThat(fields, hasSize(3));
        
        assertThat("2", equalTo(actual.get(0).get(0)));
        assertThat("gaoyan", equalTo(actual.get(0).get(1)));
        assertThat("Shunyi", equalTo(actual.get(0).get(2)));
    }

}
