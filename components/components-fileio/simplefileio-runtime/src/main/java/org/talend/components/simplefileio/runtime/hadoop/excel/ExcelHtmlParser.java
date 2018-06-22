package org.talend.components.simplefileio.runtime.hadoop.excel;

import java.io.InputStream;
import java.util.List;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.html.HtmlParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExcelHtmlParser {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExcelHtmlParser.class);

  // TODO make it stream and apply limit for performance, but how to do for
  // data-stream?
  public static List<List<String>> getRows(InputStream rawContent, String encoding, long limit) {
    SimpleValuesContentHandler valuesContentHandler = new SimpleValuesContentHandler(-1, limit);

    HtmlParser htmlParser = new HtmlParser();
    Metadata metadata = new Metadata();
    metadata.add(Metadata.CONTENT_ENCODING, encoding);
    try {
      htmlParser.parse(rawContent, valuesContentHandler, metadata, new ParseContext());
    } catch (Exception e) {
      LOGGER.debug("Failed to parse the excel html format document.", e);
    }

    return valuesContentHandler.getValues();
  }
}
