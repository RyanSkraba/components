package org.talend.components.filedelimited.runtime;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.avro.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.common.EncodingTypeProperties;
import org.talend.components.filedelimited.tfileoutputdelimited.TFileOutputDelimitedProperties;
import com.talend.csv.CSVWriter;

public class FileOutputDelimitedRuntime {

    private transient static final Logger LOGGER = LoggerFactory.getLogger(FileOutputDelimitedRuntime.class);

    private TFileOutputDelimitedProperties props;

    boolean useStream;

    String fileName;

    String fullName = null;

    String extension = null;

    String directory = null;

    boolean isFileGenerated;

    File file;

    File zipFile;

    private ZipOutputStream zipOut;

    protected String encoding;

    protected String fieldSeparator;

    protected String rowSeparator;

    protected char escapeChar;

    protected char textEnclosureChar;

    Writer writer;

    StringWriter strWriter;

    OutputStreamWriter streamWriter;

    private int splitedFileNo = 0;

    public FileOutputDelimitedRuntime(TFileOutputDelimitedProperties props) {
        this.props = props;
        this.useStream = props.targetIsStream.getValue();
        this.encoding = getEncoding();
        setFieldSeparator(props.fieldSeparator.getValue());
        setRowSeparator(props.rowSeparator.getValue());
        if (props.csvOptions.getValue()) {
            setEscapeAndTextEnclosure(props.escapeChar.getValue(), props.textEnclosure.getValue());
        }
        init();
    }

    public void init() {
        if (!useStream) {
            fileName = (new File(props.fileName.getStringValue())).getAbsolutePath().replace("\\", "/");
            if ((fileName.indexOf("/") != -1)) {
                if (fileName.lastIndexOf(".") < fileName.lastIndexOf("/")) {
                    fullName = fileName;
                    extension = "";
                } else {
                    fullName = fileName.substring(0, fileName.lastIndexOf("."));
                    extension = fileName.substring(fileName.lastIndexOf("."));
                }
                directory = fileName.substring(0, fileName.lastIndexOf("/"));
            } else {
                if (fileName.lastIndexOf(".") != -1) {
                    fullName = fileName.substring(0, fileName.lastIndexOf("."));
                    extension = fileName.substring(fileName.lastIndexOf("."));
                } else {
                    fullName = fileName;
                    extension = "";
                }
                directory = "";
            }
            this.file = new File(fileName);
            if (props.append.getValue() && file.exists()) {
                isFileGenerated = false;
            }
            if (props.creatDirIfNotExist.getValue() && directory != null && directory.trim().length() != 0) {
                File dir = new File(directory);
                if (!dir.exists()) {
                    LOGGER.debug(" - Creating directory '" + dir.getPath() + "'.");
                    dir.mkdirs();
                    LOGGER.debug(" - The directory '" + dir.getPath() + "' has been created successfully.");
                }
            }
        }
    }

    public CSVWriter getCsvWriter() throws IOException {
        com.talend.csv.CSVWriter csvWriter = null;
        if (!useStream) {
            if (!props.split.getValue()) {
                if (props.compress.getValue() && !props.append.getValue()) {// compress the dest file
                    file = new File(fileName);
                    String zipName = fullName + ".zip";
                    zipFile = new File(zipName);

                    if (zipFile.exists()) {
                        zipFile.delete();
                    }
                    zipOut = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipName)));
                    zipOut.putNextEntry(new ZipEntry(file.getName()));
                    this.streamWriter = new OutputStreamWriter(zipOut, encoding);
                } else {
                    if (!props.append.getValue()) {
                        File fileToDelete = new File(fileName);
                        if (fileToDelete.exists()) {
                            fileToDelete.delete();
                        }
                    }
                    streamWriter = new OutputStreamWriter(new FileOutputStream(fileName, props.append.getValue()), encoding);
                }
            } else {
                file = new File(fullName + splitedFileNo + extension);

                if (!props.append.getValue()) {
                    if (file.exists()) {
                        file.delete();
                    }
                }
                streamWriter = new OutputStreamWriter(
                        new FileOutputStream(fullName + splitedFileNo + extension, props.append.getValue()), encoding);
                splitedFileNo++;
            }
            if (props.rowMode.getValue()) {
                this.writer = new BufferedOutput(streamWriter);
                this.strWriter = new StringWriter();
                csvWriter = new CSVWriter(strWriter);
            } else {
                csvWriter = new CSVWriter(new BufferedWriter(streamWriter));
            }
        } else {
            if (props.compress.getValue()) {
                // compress the dest output stream
                zipOut = new ZipOutputStream(new BufferedOutputStream((OutputStream) props.fileName.getValue()));
                zipOut.putNextEntry(new ZipEntry("TalendOutputDelimited"));
                streamWriter = new OutputStreamWriter(zipOut, encoding);

            } else {
                streamWriter = new OutputStreamWriter((OutputStream) props.fileName.getValue(), encoding);
            }
            if (props.rowMode.getValue()) {
                this.writer = new BufferedOutput(streamWriter);
                StringWriter strWriter = new StringWriter();
                csvWriter = new com.talend.csv.CSVWriter(strWriter);
            } else {
                BufferedWriter bufferWriter = new BufferedWriter(streamWriter);
                csvWriter = new com.talend.csv.CSVWriter(bufferWriter);
            }
        }

        csvWriter.setSeparator(fieldSeparator.charAt(0));
        if (!props.useOsRowSeparator.getValue()) {
            csvWriter.setLineEnd(rowSeparator);
        } else {
            if (!"\r\n".equals(rowSeparator) && rowSeparator.charAt(0) != '\r' && rowSeparator.charAt(0) != '\n') {
                csvWriter.setLineEnd(rowSeparator);
            }
        }
        // Header can't be enclosed with text enclosed char
        // This would set after write header
        if (!props.includeHeader.getValue()) {
            csvWriter.setEscapeChar(escapeChar);
            csvWriter.setQuoteChar(textEnclosureChar);
            csvWriter.setQuoteStatus(com.talend.csv.CSVWriter.QuoteStatus.FORCE);
        }
        return csvWriter;
    }

    public Writer getWriter() throws IOException {
        if (!props.targetIsStream.getValue()) {
            if (!props.split.getValue()) {
                if (props.compress.getValue() && !props.append.getValue()) {// compress the dest file
                    file = new File(fileName);
                    String zipName = fullName + ".zip";
                    zipFile = new File(zipName);

                    if (zipFile.exists()) {
                        zipFile.delete();
                    }
                    zipOut = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipName)));
                    zipOut.putNextEntry(new ZipEntry(file.getName()));
                    streamWriter = new OutputStreamWriter(zipOut, encoding);
                } else {
                    if (!props.append.getValue()) {
                        File fileToDelete = new File(fileName);
                        if (fileToDelete.exists()) {
                            fileToDelete.delete();
                        }
                    }
                    streamWriter = new OutputStreamWriter(new FileOutputStream(fileName, props.append.getValue()), encoding);
                }
            } else {
                file = new File(fullName + splitedFileNo + extension);
                if (!props.append.getValue()) {
                    if (file.exists()) {
                        file.delete();
                    }
                }
                streamWriter = new OutputStreamWriter(
                        new FileOutputStream(fullName + splitedFileNo + extension, props.append.getValue()), encoding);
                splitedFileNo++;
            }
        } else {
            if (props.compress.getValue()) {
                // compress the dest output stream
                zipOut = new ZipOutputStream(new BufferedOutputStream((OutputStream) props.fileName.getValue()));
                zipOut.putNextEntry(new ZipEntry("TalendOutputDelimited"));
                streamWriter = new OutputStreamWriter(zipOut, encoding);
            } else {
                streamWriter = new OutputStreamWriter((OutputStream) props.fileName.getValue(), encoding);
            }
        }
        if (props.rowMode.getValue()) {
            writer = new BufferedOutput(streamWriter);
        } else {
            writer = new BufferedWriter(streamWriter);
        }
        return writer;
    }

    // This schema should be schema of IndexRecord
    public void writeHeader(Writer writer, Schema schema) throws IOException {
        if (props.includeHeader.getValue()) {
            // TODO support PARALLEL ? need recheck with code of javajet
            // If the target is stream, would write header directly
            if (props.targetIsStream.getValue() || (file != null && file.length() == 0)
                    || (zipFile != null && zipFile.length() == 0)) {
                for (Schema.Field field : schema.getFields()) {
                    writer.write(field.name());
                    if (field.pos() != (schema.getFields().size() - 1)) {
                        writer.write(fieldSeparator);
                    }
                }
                writer.write(rowSeparator);
            }
        }
    }

    public void writeHeader(CSVWriter csvWriter, Schema schema) throws IOException {
        if (props.includeHeader.getValue()) {
            csvWriter.writeNext(getHeaders(schema));
            if (props.rowMode.getValue()) {
                writer.write(strWriter.getBuffer().toString());
                strWriter.getBuffer().delete(0, strWriter.getBuffer().length());
            }
            // Because of header should not be included "escapeChar" and "textEnclosureChar"
            // So need set them after header is written
            csvWriter.setEscapeChar(escapeChar);
            csvWriter.setQuoteChar(textEnclosureChar);
            csvWriter.setQuoteStatus(com.talend.csv.CSVWriter.QuoteStatus.FORCE);
        }
    }

    public String[] getHeaders(Schema schema) {
        List<Schema.Field> fields = schema.getFields();
        String[] headers = new String[fields.size()];
        for (int i = 0; i < headers.length; i++) {
            headers[i] = fields.get(i).name();
        }
        return headers;
    }

    private void setFieldSeparator(String fieldSeparator) {
        if (props.csvOptions.getValue()) {
            if (fieldSeparator != null && fieldSeparator.length() > 0) {
                this.fieldSeparator = String.valueOf(fieldSeparator.toCharArray()[0]);
            } else {
                throw new IllegalArgumentException("Field Separator must be assigned a char.");
            }
        } else {
            this.fieldSeparator = fieldSeparator;
        }

    }

    public void setEscapeAndTextEnclosure(String escapeChar, String textEnclosure) throws IllegalArgumentException {
        if (escapeChar.length() <= 0) {
            throw new IllegalArgumentException("Escape Char must be assigned a char.");
        }

        if ("".equals(textEnclosure))
            textEnclosure = "\0";
        char textEnclosurewriter[] = null;

        if (textEnclosure.length() > 0) {
            textEnclosurewriter = textEnclosure.toCharArray();
        } else {
            throw new IllegalArgumentException("Text Enclosure must be assigned a char.");
        }

        this.textEnclosureChar = textEnclosurewriter[0];

        if (("\\").equals(escapeChar)) {
            this.escapeChar = '\\';
        } else if (escapeChar.equals(textEnclosure)) {
            this.escapeChar = this.textEnclosureChar;
        } else {
            // the default escape mode is double escape
            this.escapeChar = this.textEnclosureChar;
        }

    }

    private void setRowSeparator(String rowSeparator) {
        if (props.csvOptions.getValue()) {
            if ("\r\n".equals(rowSeparator)) {
                this.rowSeparator = rowSeparator;
            }
            if (rowSeparator.length() > 0) {
                this.rowSeparator = String.valueOf(rowSeparator.toCharArray()[0]);
            } else {
                throw new IllegalArgumentException("Row Separator must be assigned a char.");
            }
        } else {
            this.rowSeparator = rowSeparator;
        }
    }

    private String getEncoding() {
        if (EncodingTypeProperties.ENCODING_TYPE_CUSTOM.equals(props.encoding.encodingType.getValue())) {
            return props.encoding.customEncoding.getValue();
        }
        return props.encoding.encodingType.getValue();
    }

}
