/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.veracloud.jton.serialization;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.veracloud.jton.JtonArray;
import com.veracloud.jton.JtonElement;
import com.veracloud.jton.JtonObject;
import com.veracloud.jton.internal.LazilyParsedNumber;

public class CSVSerializer implements Serializer<JtonArray> {
	private Charset charset;

	private ArrayList<String> keys = new ArrayList<String>();

	private boolean writeKeys = false;

	private int c = -1;

	public static final String DEFAULT_CHARSET_NAME = "ISO-8859-1";

	public static final String CSV_EXTENSION = "csv";
	public static final String MIME_TYPE = "text/csv";
	public static final int BUFFER_SIZE = 2048;

	public CSVSerializer() {
		this(Charset.forName(DEFAULT_CHARSET_NAME));
	}

	public CSVSerializer(Charset charset) {
		if (charset == null) {
			throw new IllegalArgumentException("charset is null.");
		}

		this.charset = charset;
	}

	/**
	 * Returns the character set used to encode/decode the CSV data.
	 */
	public Charset getCharset() {
		return charset;
	}

	/**
	 * Returns the keys that will be read or written by this serializer.
	 */
	public List<String> getKeys() {
		return keys;
	}

	/**
	 * Sets the keys that will be read or written by this serializer.
	 * 
	 * @param keys
	 */
	public void setKeys(Collection<String> keys) {
		if (keys == null) {
			throw new IllegalArgumentException();
		}

		this.keys = new ArrayList<String>(keys);
	}

	/**
	 * Sets the keys that will be read or written by this serializer.
	 * 
	 * @param keys
	 */
	public void setKeys(String... keys) {
		if (keys == null) {
			throw new IllegalArgumentException();
		}
		setKeys(Arrays.asList(keys));
	}

	/**
	 * Returns the serializer's write keys flag.
	 */
	public boolean getWriteKeys() {
		return writeKeys;
	}

	/**
	 * Sets the serializer's write keys flag.
	 * 
	 * @param writeKeys
	 *            If <tt>true</tt>, the first line of the output will contain
	 *            the keys. Otherwise, the first line will contain the first
	 *            line of data.
	 */
	public void setWriteKeys(boolean writeKeys) {
		this.writeKeys = writeKeys;
	}

	/**
	 * Reads values from a comma-separated value stream.
	 * 
	 * @param inputStream
	 *            The input stream from which data will be read.
	 * 
	 * @see #readObject(Reader)
	 */
	@Override
	public JtonArray readObject(InputStream inputStream) throws IOException, SerializationException {
		if (inputStream == null) {
			throw new IllegalArgumentException("inputStream is null.");
		}

		Reader reader = new BufferedReader(new InputStreamReader(inputStream, charset), BUFFER_SIZE);
		return readObject(reader);
	}

	/**
	 * Reads values from a comma-separated value stream.
	 * 
	 * @param reader
	 *            The reader from which data will be read.
	 * 
	 * @return A list containing the data read from the CSV file. The list items
	 *         are instances of Dictionary<String, Object> populated by mapping
	 *         columns in the CSV file to keys in the key sequence.
	 *         <p>
	 *         If no keys have been specified when this method is called, they
	 *         are assumed to be defined in the first line of the file.
	 */
	public JtonArray readObject(Reader reader) throws IOException, SerializationException {
		if (reader == null) {
			throw new IllegalArgumentException("reader is null.");
		}

		LineNumberReader lineNumberReader = new LineNumberReader(reader);

		if (keys.size() == 0) {
			// Read keys from first line
			String line = lineNumberReader.readLine();
			if (line == null) {
				throw new SerializationException("Could not read keys from input.");
			}

			String[] keysLocal = line.split(",");
			this.keys = new ArrayList<String>(keysLocal.length);

			for (int i = 0; i < keysLocal.length; i++) {
				String key = keysLocal[i];
				this.keys.add(key.trim());
			}
		}

		// Create the list and notify the listeners
		JtonArray items = new JtonArray();

		// Move to the first character
		c = lineNumberReader.read();

		// Ignore BOM (if present)
		if (c == 0xFEFF) {
			c = lineNumberReader.read();
		}

		try {
			while (c != -1) {
				JtonObject item = readItem(lineNumberReader);
				while (item != null) {
					items.add(item);

					// Move to next line
					while (c != -1 && (c == '\r' || c == '\n')) {
						c = lineNumberReader.read();
					}

					// Read the next item
					item = readItem(lineNumberReader);
				}
			}
		} catch (SerializationException exception) {
			System.err.println("An error occurred while processing input at line number " + (lineNumberReader.getLineNumber() + 1));

			throw exception;
		}

		return items;
	}

	private JtonObject readItem(Reader reader) throws IOException, SerializationException {
		JtonObject item = null;

		if (c != -1) {
			// Instantiate the item
			item = new JtonObject();

			// Add values to the item
			for (int i = 0, n = keys.size(); i < n; i++) {
				String key = keys.get(i);
				Object value = readValue(reader);

				if (c == '\r' || c == '\n') {
					if (i < n - 1) {
						throw new SerializationException("Line data is incomplete.");
					}

					// Move to next char; if LF, move again
					c = reader.read();

					if (c == '\n') {
						c = reader.read();
					}
				}

				item.add(key, value);
			}
		}

		return item;
	}

	private Object readValue(Reader reader) throws IOException, SerializationException {
		Object value = null;

		// Read the next value from this line, returning null if there are
		// no more values on the line
		if (c != -1 && (c != '\r' && c != '\n')) {
			// Read the value
			StringBuilder valueBuilder = new StringBuilder();

			// Values may be bounded in quotes; the double-quote character is
			// escaped by two successive occurrences
			boolean quoted = (c == '"');
			if (quoted) {
				c = reader.read();
			}

			while (c != -1 && (quoted || (c != ',' && c != '\r' && c != '\n'))) {
				if (c == '"') {
					if (!quoted) {
						throw new SerializationException("Dangling quote.");
					}

					c = reader.read();

					if (c != '"' && (c != ',' && c != '\r' && c != '\n' && c != -1)) {
						throw new SerializationException("Prematurely terminated quote.");
					}

					quoted &= (c == '"');
				}

				if (c != -1 && (quoted || (c != ',' && c != '\r' && c != '\n'))) {
					valueBuilder.append((char) c);
					c = reader.read();
				}
			}

			if (quoted) {
				throw new SerializationException("Unterminated string.");
			}

			String string = valueBuilder.toString();

			// Trim the value
			if (string != null) {
				string = string.trim();
			}

			if ("null".equals(string)) {
				value = null;
			} else if ("true".equals(string) || "false".equals(string)) {
				value = Boolean.valueOf(string);
			} else if (string.length() > 0) {
				char c = string.charAt(0);
				if (c == '+' || c == '-' || Character.isDigit(c)) {
					value = new LazilyParsedNumber(string);
				} else {
					value = string;
				}
			} else {
				value = string;
			}

			// Move to the next character after ',' (don't automatically advance
			// to
			// the next line)
			if (c == ',') {
				c = reader.read();
			}
		}

		return value;
	}

	/**
	 * Writes values to a comma-separated value stream.
	 * 
	 * @param object
	 * 
	 * @param outputStream
	 *            The output stream to which data will be written.
	 * 
	 * @see #writeObject(List, Writer)
	 */
	@Override
	public void writeObject(JtonArray items, OutputStream outputStream) throws IOException, SerializationException {
		if (items == null) {
			throw new IllegalArgumentException("items is null.");
		}

		if (outputStream == null) {
			throw new IllegalArgumentException("outputStream is null.");
		}

		Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream, charset), BUFFER_SIZE);

		writeObject(items, writer);
	}

	/**
	 * Writes values to a comma-separated value stream.
	 * 
	 * @param items
	 *            A list containing the data to write to the CSV file. List
	 *            items must be instances of Dictionary<String, Object>. The
	 *            dictionary values will be written out in the order specified
	 *            by the key sequence.
	 * 
	 * @param writer
	 *            The writer to which data will be written.
	 */
	private void writeObject(JtonArray items, Writer writer) throws IOException {
		if (items == null) {
			throw new IllegalArgumentException("items is null.");
		}

		if (writer == null) {
			throw new IllegalArgumentException("writer is null.");
		}

		if (writeKeys) {
			// Write keys as first line
			for (int i = 0, n = keys.size(); i < n; i++) {
				String key = keys.get(i);

				if (i > 0) {
					writer.append(",");
				}

				writer.append(key);
			}

			writer.append("\r\n");
		}

		for (JtonElement item : items) {
			if (item.isJtonObject()) {
				JtonObject itemDictionary = item.getAsJtonObject();

				int i = 0;
				for (String key : keys) {
					JtonElement value = itemDictionary.get(key);

					if (i++ > 0) {
						writer.append(",");
					}

					String string = value.getAsString(null);
					if (value != null) {
						if (string == null) {
							// empty field for null value
							string = "";
						}
						if (string.indexOf(',') >= 0 || string.indexOf('"') >= 0 || string.indexOf('\r') >= 0 || string.indexOf('\n') >= 0) {
							writer.append('"');

							if (string.indexOf('"') == -1) {
								writer.append(string);
							} else {
								writer.append(string.replace("\"", "\"\""));
							}

							writer.append('"');
						} else {
							writer.append(string);
						}
					}
				}
			}
			writer.append("\r\n");
		}

		writer.flush();
	}

	@Override
	public String getMIMEType(JtonArray object) {
		return MIME_TYPE + "; charset=" + charset.name();
	}

	public static void main(String[] args) throws Exception {
		JtonArray a = new JtonArray();
		JtonObject o = new JtonObject();
		a.add(o);

		o.add("date", new Date());

		StringWriter w = new StringWriter();

		CSVSerializer csv = new CSVSerializer();
		csv.setKeys(o.keySet());
		// csv.setWriteKeys(true);
		// csv.writeObject(a, System.out);

		csv.writeObject(a, w);

		System.out.println(w.toString());

		JtonArray aa = csv.readObject(new StringReader(w.toString()));
		System.out.println(aa.get(0).getAsJtonObject().get("date").getAsDate());

	}

}
