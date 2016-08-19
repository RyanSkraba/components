package org.talend.components.fileinput.runtime;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.talend.components.fileinput.function.CSVReader;
import org.talend.components.fileinput.function.FileInputDelimited;
import org.talend.components.fileinput.tFileInputDelimited.TFileInputDelimitedProperties;

public class FileInputRuntime {

	TFileInputDelimitedProperties properties;

	FileInputDelimited fileInputDelimited = null;

	CSVReader csvReader = null;

	String fileName = properties.filename.getStringValue();

	String fieldSeparator = properties.fieldSeparator.getStringValue();

	String rowSeparator = properties.rowSeparator.getStringValue();

	String escapeChar = properties.escapeChar.getStringValue();

	String textEnclosure = properties.textEnclosure.getStringValue();

	boolean csvOptions = properties.csvOptions.getValue();

	boolean uncompress = properties.uncompress.getValue();

	boolean dieOnError = properties.dieOnError.getValue();

	boolean splitRecord = properties.splitRecord.getValue();

	boolean random = properties.random.getValue();

	boolean removeEmptyRow = properties.removeEmptyRow.getValue();

	String encoding = properties.encodingType.getStringValue();

	Integer nbRandom = -1;

	Integer head = (properties.head.getValue() == null) ? -1 : properties.head.getValue();

	Integer foot = (properties.foot.getValue() == null || uncompress) ? -1 : properties.head.getValue();

	Integer limit = (properties.limit.getValue() == null) ? -1 : properties.head.getValue();

	public FileInputDelimited fileRead(boolean uncompress) throws IOException {

		if (random) {
			nbRandom = properties.nbRandom.getValue();
			if (nbRandom == null) {
				nbRandom = 0;
			}
		}
		if (uncompress) {
			nbRandom = -1;
		}

		if (uncompress) {
			Object filename = fileName;
			ZipInputStream zipInputStream = null;
			if (filename instanceof InputStream) {
				zipInputStream = new ZipInputStream(new BufferedInputStream((InputStream) filename));
			} else {
				try {
					zipInputStream = new ZipInputStream(
							new BufferedInputStream(new FileInputStream(String.valueOf(filename))));
				} catch (IOException e) {
					if (dieOnError) {
						throw e;
					} else {
						System.err.println(e.getMessage());
					}
				}
			}

			ZipEntry zipEntry = null;
			while (true) {
				zipEntry = zipInputStream.getNextEntry();
				if (zipEntry == null) {
					break;
				} else if (zipEntry.isDirectory()) {
					continue;
				}

				fileInputDelimited = new FileInputDelimited(zipInputStream, encoding, fieldSeparator, rowSeparator,
						removeEmptyRow, head, foot, limit, nbRandom, splitRecord);
			}

			if (csvOptions) {
				if (filename instanceof InputStream) {
					zipInputStream = new ZipInputStream(new BufferedInputStream((InputStream) filename));
				} else {
					try {
						zipInputStream = new ZipInputStream(
								new BufferedInputStream(new FileInputStream(String.valueOf(filename))));
					} catch (IOException e) {
						if (dieOnError) {
							throw e;
						} else {
							System.err.println(e.getMessage());
						}
					}
				}

				while (true) {
					zipEntry = zipInputStream.getNextEntry();
					if (zipEntry == null) {
						break;
					} else if (zipEntry.isDirectory()) {
						continue;
					}

					csvReader = new CSVReader(zipInputStream, getFieldSeparator(fieldSeparator), encoding);
				}
			}
		} else {
			Object filename = fileName;
			if (filename instanceof java.io.InputStream) {
				try {
					checkFooterAndRandom(foot, nbRandom);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			fileInputDelimited = new FileInputDelimited(fileName, encoding, fieldSeparator, rowSeparator,
					removeEmptyRow, head, foot, limit, nbRandom, splitRecord);
		}

		return fileInputDelimited;
	}

	private void checkFooterAndRandom(Integer foot, Integer nbRandom) throws Exception {

		if (foot > 0 || nbRandom > 0) {
			try {
				throw new Exception("When the input source is a stream,footer and random shouldn't be bigger than 0.");
			} catch (IOException e) {
				if (dieOnError) {
					throw e;
				} else {
					System.err.println(e.getMessage());
				}
			}
		}
	}

	private char getFieldSeparator(String fieldSeparator) {
		char fSeparator;
		if (fieldSeparator.length() > 0) {
			char[] fieldSeparat = fieldSeparator.toCharArray();
			fSeparator = fieldSeparat[0];
		} else {
			throw new IllegalArgumentException("Field Separator must be assigned a char.");
		}

		return fSeparator;
	}
}
