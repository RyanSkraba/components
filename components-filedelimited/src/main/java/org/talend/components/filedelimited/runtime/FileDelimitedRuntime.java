package org.talend.components.filedelimited.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.common.EncodingTypeProperties;
import org.talend.components.common.runtime.FileRuntimeHelper;
import org.talend.components.filedelimited.tFileInputDelimited.TFileInputDelimitedProperties;
import org.talend.fileprocess.FileInputDelimited;

import com.talend.csv.CSVReader;

public class FileDelimitedRuntime {

    private transient static final Logger LOG = LoggerFactory.getLogger(FileDelimitedRuntime.class);

    private TFileInputDelimitedProperties props;

    protected Object fileNameOrStream;

    private ZipInputStream zipInputStream;

    private String encoding;

    private int nbRandom = -1;

    private int header;

    private int footer;

    protected int limit;

    private boolean sourceIsStream;

    private char fieldSeparator;

    private char rowSeparator;

    private int totalLine = 0;

    private int lastLine = -1;

    public FileDelimitedRuntime(TFileInputDelimitedProperties props) {
        this.props = props;
    }

    public void init() throws IOException {
        fileNameOrStream = props.fileName.getValue();

        sourceIsStream = fileNameOrStream instanceof InputStream;

        encoding = getEncoding();

        header = (props.header.getValue() == null) ? -1 : props.header.getValue();

        footer = (props.footer.getValue() == null || props.uncompress.getValue()) ? -1 : props.footer.getValue();

        limit = (props.limit.getValue() == null) ? -1 : props.limit.getValue();

        if (props.random.getValue()) {
            nbRandom = (props.nbRandom.getValue() == null || props.uncompress.getValue()) ? -1 : props.nbRandom.getValue();
        }
        if (sourceIsStream) {
            zipInputStream = FileRuntimeHelper.getZipInputStream((InputStream) fileNameOrStream);
        } else {
            zipInputStream = FileRuntimeHelper.getZipInputStream(String.valueOf(fileNameOrStream));
        }

        fieldSeparator = getFieldSeparator();
        rowSeparator = getRowSeparator();
    }

    public FileInputDelimited getFileDelimited() throws IOException {
        FileInputDelimited fileInputDelimited = null;
        if (props.uncompress.getValue()) {
            ZipEntry zipEntry = null;
            if (hashNextEntry()) {
                fileInputDelimited = new FileInputDelimited(zipInputStream, encoding, props.fieldSeparator.getValue(),
                        props.rowSeparator.getValue(), props.removeEmptyRow.getValue(), header, footer, limit, nbRandom,
                        props.splitRecord.getValue());
            }
        } else {
            if (fileNameOrStream instanceof InputStream) {
                checkFooterAndRandom();
                fileInputDelimited = new FileInputDelimited((InputStream) fileNameOrStream, encoding,
                        props.fieldSeparator.getValue(), props.rowSeparator.getValue(), props.removeEmptyRow.getValue(), header,
                        footer, limit, nbRandom, props.splitRecord.getValue());
            } else {
                fileInputDelimited = new FileInputDelimited(String.valueOf(fileNameOrStream), encoding,
                        props.fieldSeparator.getValue(), props.rowSeparator.getValue(), props.removeEmptyRow.getValue(), header,
                        footer, limit, nbRandom, props.splitRecord.getValue());
            }

        }

        return fileInputDelimited;
    }

    public CSVReader getCsvReader() throws IOException {

        String[] row = null;
        int currentLine = 0;
        CSVReader csvReader = null;

        if (props.uncompress.getValue()) {
            if (hashNextEntry()) {
                csvReader = new CSVReader(zipInputStream, getFieldSeparator(), encoding);
            } else {
                return null;
            }
        } else {
            if (fileNameOrStream instanceof InputStream) {
                checkFooter();
                csvReader = new CSVReader((java.io.InputStream) fileNameOrStream, fieldSeparator, encoding);
            } else {
                csvReader = new CSVReader(new java.io.BufferedReader(
                        new java.io.InputStreamReader(new java.io.FileInputStream(String.valueOf(fileNameOrStream)), encoding)),
                        fieldSeparator);
            }
        }

        csvReader.setTrimWhitespace(false);
        if ((rowSeparator != '\n') && (rowSeparator != '\r')) {
            csvReader.setLineEnd("" + rowSeparator);
        }
        csvReader.setQuoteChar('"');
        csvReader.setEscapeChar(csvReader.getQuoteChar());
        if (footer > 0) {
            for (totalLine = 0; totalLine < header; totalLine++) {
                csvReader.readNext();
            }
            // TODO check csv option setting
            csvReader.setSkipEmptyRecords(true);
            while (csvReader.readNext()) {

                row = csvReader.getValues();
                // empty line when row separator is '\n'
                if (!(row.length == 1 && ("\015").equals(row[0]))) {
                    totalLine++;
                }
            }
            int lastLineTemp = totalLine - footer < 0 ? 0 : totalLine - footer;
            if (lastLine > 0) {
                lastLine = lastLine < lastLineTemp ? lastLine : lastLineTemp;
            } else {
                lastLine = lastLineTemp;
            }
            csvReader.close();

            if (sourceIsStream) {
                csvReader = new CSVReader((InputStream) fileNameOrStream, fieldSeparator, encoding);
            } else {
                csvReader = new CSVReader(new java.io.BufferedReader(
                        new java.io.InputStreamReader(new java.io.FileInputStream(String.valueOf(sourceIsStream)), encoding)),
                        fieldSeparator);
            }
            csvReader.setTrimWhitespace(false);
            if ((rowSeparator != '\n') && (rowSeparator != '\r')) {
                csvReader.setLineEnd("" + rowSeparator);
            }
            csvReader.setQuoteChar('"');
            csvReader.setEscapeChar(csvReader.getQuoteChar());
        }
        if (limit != 0) {
            // TODO <%= header %><%=hasDynamic?"-1":""%>
            for (currentLine = 0; currentLine < header; currentLine++) {
                csvReader.readNext();
            }
        }
        csvReader.setSkipEmptyRecords(true);
        return csvReader;
    }

    private char getFieldSeparator() {
        String fieldSeparator = props.fieldSeparator.getValue();
        char fSeparator;
        if (fieldSeparator != null && fieldSeparator.length() > 0) {
            fSeparator = fieldSeparator.toCharArray()[0];
        } else {
            throw new IllegalArgumentException("Field Separator must be assigned a char.");
        }

        return fSeparator;
    }

    private char getRowSeparator() {
        String rowSeparator = props.rowSeparator.getValue();
        char separatorChar;
        if (rowSeparator != null && rowSeparator.length() > 0) {
            separatorChar = rowSeparator.toCharArray()[0];
        } else {
            throw new IllegalArgumentException("Row Separator must be assigned a char.");
        }
        return separatorChar;
    }

    private String getEncoding() {
        if (EncodingTypeProperties.ENCODING_TYPE_CUSTOM.equals(props.encoding.encodingType.getValue())) {
            return props.encoding.customEncoding.getValue();
        }
        return props.encoding.encodingType.getValue();
    }

    protected boolean hashNextEntry() throws IOException {
        if (zipInputStream == null) {
            return false;
        }
        ZipEntry zipEntry = null;
        while (true) {
            zipEntry = zipInputStream.getNextEntry();
            if (zipEntry == null) {
                return false;
            } else if (zipEntry.isDirectory()) {
                continue;
            }
            return true;
        }
    }

    private void checkFooterAndRandom() throws IOException {

        if (footer > 0 || nbRandom > 0) {
            throw new IOException("When the input source is a stream,footer and random shouldn't be bigger than 0.");
        }
    }

    public void checkFooter() throws IOException {
        if (footer > 0) {
            throw new IOException("When the input source is a stream,footer shouldn't be bigger than 0.");
        }
    }

}
