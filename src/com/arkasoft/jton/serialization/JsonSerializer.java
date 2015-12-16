/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.arkasoft.jton.serialization;

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
import java.lang.reflect.Type;
import java.nio.charset.Charset;

import com.arkasoft.jton.JtonArray;
import com.arkasoft.jton.JtonElement;
import com.arkasoft.jton.JtonIOException;
import com.arkasoft.jton.JtonNull;
import com.arkasoft.jton.JtonObject;
import com.arkasoft.jton.JtonPrimitive;
import com.arkasoft.jton.internal.LazilyParsedNumber;

/**
 * Implementation of the {@link Serializer} interface that reads data from and
 * writes data to a JavaScript Object Notation (JSON) file.
 */
public class JsonSerializer implements Serializer<JtonElement> {
	public static final String DEFAULT_CHARSET_NAME = "UTF-8";
	public static final String JSON_EXTENSION = "json";
	public static final String MIME_TYPE = "application/json";
	public static final int BUFFER_SIZE = 2048;

	// ---------------------------

	/** Character set used to encode/decode the JSON data */
	private Charset charset;

	/** A flag indicating that map keys should always be quote-delimited. */
	private boolean alwaysDelimitMapKeys = false;

	/** The number of spaces to add to each level of indentation. */
	private int indentFactor = 0;

	private int c = -1;

	public JsonSerializer() {
		this(Charset.forName(DEFAULT_CHARSET_NAME));
	}

	public JsonSerializer(Charset charset) {
		if (charset == null) {
			throw new IllegalArgumentException("charset is null.");
		}

		this.charset = charset;
	}

	/**
	 * Returns the character set used to encode/decode the JSON data.
	 */
	public Charset getCharset() {
		return charset;
	}

	/**
	 * Returns a flag indicating whether or not map keys will always be
	 * quote-delimited.
	 */
	public boolean getAlwaysDelimitMapKeys() {
		return alwaysDelimitMapKeys;
	}

	/**
	 * Sets a flag indicating that map keys should always be quote-delimited.
	 *
	 * @param alwaysDelimitMapKeys
	 *          <tt>true</tt> to bound map keys in double quotes; <tt>false</tt>
	 *          to only quote-delimit keys as necessary.
	 */
	public void setAlwaysDelimitMapKeys(boolean alwaysDelimitMapKeys) {
		this.alwaysDelimitMapKeys = alwaysDelimitMapKeys;
	}

	/**
	 * Get the number of spaces to add to each level of indentation.
	 */
	public int getIntentFactor() {
		return indentFactor;
	}

	/**
	 * Sets the number of spaces to add to each level of indentation.
	 * 
	 * @param intent
	 *          the number of spaces to add to each level of indentation.
	 */
	public void setIntentFactor(int intent) {
		this.indentFactor = intent;
	}

	/**
	 * Reads data from a JSON stream.
	 *
	 * @param inputStream
	 *          The input stream from which data will be read.
	 *
	 * @see #readObject(Reader)
	 */
	@Override
	public JtonElement readObject(InputStream inputStream) throws IOException, SerializationException {
		if (inputStream == null) {
			throw new IllegalArgumentException("inputStream is null.");
		}

		Reader reader = new BufferedReader(new InputStreamReader(inputStream, charset), BUFFER_SIZE);
		return readObject(reader);
	}

	/**
	 * Reads data from a JSON stream.
	 *
	 * @param reader
	 *          The reader from which data will be read.
	 *
	 * @return One of the following types, depending on the content of the stream
	 *         and the value of {@link #getType()}:
	 *
	 *         <ul>
	 *         <li>pivot.collections.Dictionary</li>
	 *         <li>pivot.collections.List</li>
	 *         <li>java.lang.String</li>
	 *         <li>java.lang.Number</li>
	 *         <li>java.lang.Boolean</li>
	 *         <li><tt>null</tt></li>
	 *         <li>A JavaBean object</li>
	 *         </ul>
	 */
	public JtonElement readObject(Reader reader) throws IOException, SerializationException {
		if (reader == null) {
			throw new IllegalArgumentException("reader is null.");
		}

		// Move to the first character
		LineNumberReader lineNumberReader = new LineNumberReader(reader);
		c = lineNumberReader.read();

		// Ignore BOM (if present)
		if (c == 0xFEFF) {
			c = lineNumberReader.read();
		}

		// Read the root value
		JtonElement object;
		try {
			object = readValue(lineNumberReader);
		} catch (SerializationException exception) {
			System.err.println("An error occurred while processing input at line number " + (lineNumberReader.getLineNumber() + 1));

			throw exception;
		}

		return object;
	}

	private JtonElement readValue(Reader reader) throws IOException, SerializationException {
		JtonElement object = null;

		skipWhitespaceAndComments(reader);

		if (c == -1) {
			throw new SerializationException("Unexpected end of input stream.");
		}

		if (c == 'n') {
			object = readNullValue(reader);
		} else if (c == '"' || c == '\'') {
			object = new JtonPrimitive((String) readStringValue(reader));
		} else if (c == '+' || c == '-' || Character.isDigit(c)) {
			object = new JtonPrimitive((Number) readNumberValue(reader));
		} else if (c == 't' || c == 'f') {
			object = new JtonPrimitive((Boolean) readBooleanValue(reader));
		} else if (c == '[') {
			object = readListValue(reader);
		} else if (c == '{') {
			object = readMapValue(reader);
		} else {
			throw new SerializationException("Unexpected character in input stream.");
		}

		return object;
	}

	private void skipWhitespaceAndComments(Reader reader) throws IOException, SerializationException {
		while (c != -1 && (Character.isWhitespace(c) || c == '/')) {
			boolean comment = (c == '/');

			// Read the next character
			c = reader.read();

			if (comment) {
				if (c == '/') {
					// Single-line comment
					while (c != -1 && c != '\n' && c != '\r') {
						c = reader.read();
					}
				} else if (c == '*') {
					// Multi-line comment
					boolean closed = false;

					while (c != -1 && !closed) {
						c = reader.read();

						if (c == '*') {
							c = reader.read();
							closed = (c == '/');
						}
					}

					if (!closed) {
						throw new SerializationException("Unexpected end of input stream.");
					}

					if (c != -1) {
						c = reader.read();
					}
				} else {
					throw new SerializationException("Unexpected character in input stream.");
				}
			}
		}
	}

	private JtonNull readNullValue(Reader reader) throws IOException, SerializationException {
		String nullString = "null";

		int n = nullString.length();
		int i = 0;

		while (c != -1 && i < n) {
			if (nullString.charAt(i) != c) {
				throw new SerializationException("Unexpected character in input stream.");
			}

			c = reader.read();
			i++;
		}

		if (i < n) {
			throw new SerializationException("Incomplete null value in input stream.");
		}

		return JtonNull.INSTANCE;
	}

	private String readString(Reader reader) throws IOException, SerializationException {
		StringBuilder stringBuilder = new StringBuilder();

		// Use the same delimiter to close the string
		int t = c;

		// Move to the next character after the delimiter
		c = reader.read();

		while (c != -1 && c != t) {
			if (!Character.isISOControl(c)) {
				if (c == '\\') {
					c = reader.read();

					if (c == 'b') {
						c = '\b';
					} else if (c == 'f') {
						c = '\f';
					} else if (c == 'n') {
						c = '\n';
					} else if (c == 'r') {
						c = '\r';
					} else if (c == 't') {
						c = '\t';
					} else if (c == 'u') {
						StringBuilder unicodeBuilder = new StringBuilder();
						while (unicodeBuilder.length() < 4) {
							c = reader.read();
							unicodeBuilder.append((char) c);
						}

						String unicode = unicodeBuilder.toString();
						c = (char) Integer.parseInt(unicode, 16);
					} else {
						if (!(c == '\\' || c == '/' || c == '\"' || c == '\'' || c == t)) {
							throw new SerializationException("Unsupported escape sequence in input stream.");
						}
					}
				}

				stringBuilder.append((char) c);
			}

			c = reader.read();
		}

		if (c != t) {
			throw new SerializationException("Unterminated string in input stream.");
		}

		// Move to the next character after the delimiter
		c = reader.read();

		return stringBuilder.toString();
	}

	private Object readStringValue(Reader reader) throws IOException, SerializationException {
		return readString(reader);
	}

	private Object readNumberValue(Reader reader) throws IOException, SerializationException {
		StringBuilder stringBuilder = new StringBuilder();

		while (c != -1 && (Character.isDigit(c) || c == '.' || c == 'e' || c == 'E' || c == '-')) {
			stringBuilder.append((char) c);
			c = reader.read();
		}

		return new LazilyParsedNumber(stringBuilder.toString());
	}

	private Object readBooleanValue(Reader reader) throws IOException, SerializationException {
		String text = (c == 't') ? "true" : "false";
		int n = text.length();
		int i = 0;

		while (c != -1 && i < n) {
			if (text.charAt(i) != c) {
				throw new SerializationException("Unexpected character in input stream.");
			}

			c = reader.read();
			i++;
		}

		if (i < n) {
			throw new SerializationException("Incomplete boolean value in input stream.");
		}

		// Get the boolean value
		return Boolean.parseBoolean(text);

		// return BeanUtils.coerce(value, (Class<?>) typeArgument);
	}

	private JtonArray readListValue(Reader reader) throws IOException, SerializationException {
		JtonArray sequence = null;

		// Return the default sequence and item types
		sequence = new JtonArray();

		// Move to the next character after '['
		c = reader.read();
		skipWhitespaceAndComments(reader);

		while (c != -1 && c != ']') {
			sequence.add(readValue(reader));
			skipWhitespaceAndComments(reader);

			if (c == ',') {
				c = reader.read();
				skipWhitespaceAndComments(reader);
			} else if (c == -1) {
				throw new SerializationException("Unexpected end of input stream.");
			} else {
				if (c != ']') {
					throw new SerializationException("Unexpected character in input stream.");
				}
			}
		}

		// Move to the next character after ']'
		c = reader.read();

		return sequence;
	}

	private JtonObject readMapValue(Reader reader) throws IOException, SerializationException {
		JtonObject dictionary = null;
		Type valueType = null;

		// Return the default dictionary and value types
		dictionary = new JtonObject();
		valueType = Object.class;

		// Move to the next character after '{'
		c = reader.read();
		skipWhitespaceAndComments(reader);

		while (c != -1 && c != '}') {
			String key = null;

			if (c == '"' || c == '\'') {
				// The key is a delimited string
				key = readString(reader);
			} else {
				// The key is an undelimited string; it must adhere to Java
				// identifier syntax
				StringBuilder keyBuilder = new StringBuilder();

				if (!Character.isJavaIdentifierStart(c)) {
					throw new SerializationException("Illegal identifier start character.");
				}

				while (c != -1 && c != ':' && !Character.isWhitespace(c)) {
					if (!Character.isJavaIdentifierPart(c)) {
						throw new SerializationException("Illegal identifier character.");
					}

					keyBuilder.append((char) c);
					c = reader.read();
				}

				if (c == -1) {
					throw new SerializationException("Unexpected end of input stream.");
				}

				key = keyBuilder.toString();
			}

			if (key == null || key.length() == 0) {
				throw new SerializationException("\"" + key + "\" is not a valid key.");
			}

			skipWhitespaceAndComments(reader);

			if (c != ':') {
				throw new SerializationException("Unexpected character in input stream.");
			}

			// Move to the first character after ':'
			c = reader.read();

			if (valueType == null) {
				throw new UnsupportedOperationException();
				// // The map is a bean instance; get the generic type of the property
				// Type genericValueType =
				// ((BeanAdapter)dictionary).getGenericType(key);
				//
				// if (genericValueType != null) {
				// // Set the value in the bean
				// dictionary.put(key, readValue(reader, genericValueType));
				// } else {
				// // The property does not exist; ignore this value
				// readValue(reader, Object.class);
				// }
			} else {
				dictionary.add(key, readValue(reader));
			}

			skipWhitespaceAndComments(reader);

			if (c == ',') {
				c = reader.read();
				skipWhitespaceAndComments(reader);
			} else if (c == -1) {
				throw new SerializationException("Unexpected end of input stream.");
			} else {
				if (c != '}') {
					throw new SerializationException("Unexpected character in input stream.");
				}
			}
		}

		// Move to the first character after '}'
		c = reader.read();

		// return (dictionary instanceof BeanAdapter) ?
		// ((BeanAdapter)dictionary).getBean() : dictionary;
		return dictionary;
	}

	/**
	 * Writes data to a JSON stream.
	 *
	 * @param object
	 *
	 * @param outputStream
	 *          The output stream to which data will be written.
	 *
	 * @see #writeObject(Object, Writer)
	 */
	@Override
	public void writeObject(JtonElement object, OutputStream outputStream) throws IOException, SerializationException {
		if (outputStream == null) {
			throw new IllegalArgumentException("outputStream is null.");
		}

		Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream, charset), BUFFER_SIZE);
		writeObject(object, writer);
		writer.flush();
	}

	/**
	 * Writes data to a JSON stream.
	 *
	 * @param object
	 *          The object to serialize. Must be one of the following types:
	 *
	 *          <ul>
	 *          <li>pivot.collections.Map</li>
	 *          <li>pivot.collections.List</li>
	 *          <li>java.lang.String</li>
	 *          <li>java.lang.Number</li>
	 *          <li>java.lang.Boolean</li>
	 *          <li><tt>null</tt></li>
	 *          </ul>
	 *
	 * @param writer
	 *          The writer to which data will be written.
	 */
	public void writeObject(JtonElement object, Writer writer) throws IOException, SerializationException {
		writeObject(object, writer, 0);
	}

	private void writeObject(JtonElement object, Writer writer, int level) throws IOException, SerializationException {
		if (writer == null) {
			throw new IllegalArgumentException("writer is null.");
		}

		if (object.isJtonNull()) {
			writer.append("null");
		} else if (object.isJtonPrimitive()) {
			JtonPrimitive o = object.getAsJtonPrimitive();
			if (o.isString()) {
				String string = object.getAsString();
				StringBuilder stringBuilder = new StringBuilder();

				for (int i = 0, n = string.length(); i < n; i++) {
					char ci = string.charAt(i);

					switch (ci) {
					case '\t': {
						stringBuilder.append("\\t");
						break;
					}

					case '\n': {
						stringBuilder.append("\\n");
						break;
					}

					case '\\':
					case '\"':
					// XXX case '\'':
					{
						stringBuilder.append("\\" + ci);
						break;
					}

					default: {
						if (charset.name().startsWith("UTF") || ci <= 0xFF) {
							stringBuilder.append(ci);
						} else {
							stringBuilder.append("\\u");
							stringBuilder.append(String.format("%04x", (short) ci));
						}
					}
					}

				}

				writer.append("\"" + stringBuilder.toString() + "\"");

			} else if (o.isNumber()) {
				Number number = object.getAsNumber();

				if (number instanceof Float) {
					Float f = (Float) number;
					if (f.isNaN() || f.isInfinite()) {
						throw new SerializationException(number + " is not a valid value.");
					}
				} else if (number instanceof Double) {
					Double d = (Double) number;
					if (d.isNaN() || d.isInfinite()) {
						throw new SerializationException(number + " is not a valid value.");
					}
				}

				writer.append(number.toString());
			} else if (o.isBoolean()) {
				writer.append(String.valueOf(object.getAsBoolean()));
			} else if (o.isDate()) {
				writer.append("\"" + object.getAsString() + "\"");
			} else if (o.isSqlDate()) {
				writer.append("\"" + object.getAsString() + "\"");
			} else if (o.isSqlTime()) {
				writer.append("\"" + object.getAsString() + "\"");
			} else if (o.isSqlTimestamp()) {
				writer.append("\"" + object.getAsString() + "\"");
			}

		} else if (object.isJtonArray()) {
			JtonArray list = object.getAsJtonArray();
			writer.append("[");

			if (indentFactor > 0) {
				final String childPadding = padding((level + 1) * indentFactor);

				int i = 0;
				for (JtonElement item : list) {
					if (i > 0) {
						writer.append(',');
					}

					writer.append('\n');
					writer.append(childPadding);

					writeObject(item, writer, level + 1);
					i++;
				}

				if(list.size() > 0) {
					writer.append('\n');
					writer.append(padding(level * indentFactor));
				}
				
				writer.append("]");
				
			} else {
				int i = 0;
				for (JtonElement item : list) {
					if (i > 0) {
						writer.append(", ");
					}

					writeObject(item, writer, level++);
					i++;
				}

				writer.append("]");
			}

		} else {

			JtonObject map = object.getAsJtonObject();
			writer.append("{");

			if (indentFactor > 0) {
				final String childPadding = padding((level + 1) * indentFactor);

				int i = 0;
				for (String key : map.keySet()) {
					JtonElement value = map.get(key);
					if (value.isTransient()) {
						continue;
					}

					boolean identifier = true;
					StringBuilder keyStringBuilder = new StringBuilder();

					for (int j = 0, n = key.length(); j < n; j++) {
						char cj = key.charAt(j);
						identifier &= Character.isJavaIdentifierPart(cj);

						if (cj == '"') {
							keyStringBuilder.append('\\');
						}

						keyStringBuilder.append(cj);
					}

					key = keyStringBuilder.toString();

					if (i > 0) {
						writer.append(", ");
					}

					writer.append('\n');
					writer.append(childPadding);

					// Write the key
					if (!identifier || alwaysDelimitMapKeys) {
						writer.append('"');
					}

					writer.append(key);

					if (!identifier || alwaysDelimitMapKeys) {
						writer.append('"');
					}

					writer.append(": ");

					// Write the value
					writeObject(value, writer, level + 1);

					i++;
				}

				if(map.size() > 0) {
					writer.append('\n');
					writer.append(padding(level * indentFactor));
				}
				
				writer.append("}");

			} else {

				int i = 0;
				for (String key : map.keySet()) {
					JtonElement value = map.get(key);
					if (value.isTransient()) {
						continue;
					}

					boolean identifier = true;
					StringBuilder keyStringBuilder = new StringBuilder();

					for (int j = 0, n = key.length(); j < n; j++) {
						char cj = key.charAt(j);
						identifier &= Character.isJavaIdentifierPart(cj);

						if (cj == '"') {
							keyStringBuilder.append('\\');
						}

						keyStringBuilder.append(cj);
					}

					key = keyStringBuilder.toString();

					if (i > 0) {
						writer.append(", ");
					}

					// Write the key
					if (!identifier || alwaysDelimitMapKeys) {
						writer.append('"');
					}

					writer.append(key);

					if (!identifier || alwaysDelimitMapKeys) {
						writer.append('"');
					}

					writer.append(": ");

					// Write the value
					writeObject(value, writer, level++);

					i++;
				}

				writer.append("}");
			}
		}

		writer.flush();
	}

	private String padding(int padding) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < padding; i++) {
			sb.append(' ');
		}
		return sb.toString();
	}

	@Override
	public String getMIMEType(JtonElement object) {
		return MIME_TYPE + "; charset=" + charset.name();
	}

	//
	// Static helpers
	//

	/**
	 * Converts a JSON value to a Java object.
	 *
	 * @param json
	 *          The JSON value.
	 *
	 * @return The parsed object.
	 */
	public static JtonElement parse(String json) throws SerializationException {
		JsonSerializer jsonSerializer = new JsonSerializer();

		try {
			return jsonSerializer.readObject(new StringReader(json));
		} catch (IOException exception) {
			throw new JtonIOException(exception);
		}
	}

	/**
	 * Converts a object to a JSON string representation.
	 *
	 * @param value
	 *          The object to convert.
	 *
	 * @param alwaysDelimitMapKeys
	 *          A flag indicating whether or not map keys will always be
	 *          quote-delimited.
	 *
	 * @return The resulting JSON string.
	 */
	public static String toString(JtonElement value, boolean alwaysDelimitMapKeys) throws SerializationException {
		JsonSerializer jsonSerializer = new JsonSerializer();
		jsonSerializer.setAlwaysDelimitMapKeys(alwaysDelimitMapKeys);

		StringWriter writer = new StringWriter();

		try {
			jsonSerializer.writeObject(value, writer);
		} catch (IOException exception) {
			throw new JtonIOException(exception);
		}

		return writer.toString();
	}

	/**
	 * Converts a object to a JSON string representation.
	 * 
	 * @param value
	 *          The object to convert.
	 * @param alwaysDelimitMapKeys
	 *          A flag indicating whether or not map keys will always be
	 *          quote-delimited.
	 * @param intentFactor
	 *          The number of spaces to add to each level of indentation.
	 * @return The resulting JSON string.
	 * @throws SerializationException
	 */
	public static String toString(JtonElement value, boolean alwaysDelimitMapKeys, int intentFactor) throws SerializationException {
		JsonSerializer jsonSerializer = new JsonSerializer();
		jsonSerializer.setAlwaysDelimitMapKeys(alwaysDelimitMapKeys);
		jsonSerializer.setIntentFactor(intentFactor);

		StringWriter writer = new StringWriter();

		try {
			jsonSerializer.writeObject(value, writer);
		} catch (IOException exception) {
			throw new JtonIOException(exception);
		}

		return writer.toString();
	}
}