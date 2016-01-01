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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.veracloud.jton.JtonArray;
import com.veracloud.jton.JtonElement;
import com.veracloud.jton.JtonIOException;
import com.veracloud.jton.JtonNull;
import com.veracloud.jton.JtonObject;
import com.veracloud.jton.JtonPrimitive;
import com.veracloud.jton.internal.LazilyParsedNumber;

public class XmlSerializer implements Serializer<JtonObject> {
	private Charset charset = null;
	private String localName = null;

	public static final String DEFAULT_LOCALNAME = "jton-object";

	public static final String XMLNS_ATTRIBUTE_PREFIX = "xmlns";

	public static final String DEFAULT_CHARSET_NAME = "UTF-8";
	public static final String XML_EXTENSION = "xml";
	public static final String MIME_TYPE = "text/xml";
	public static final int BUFFER_SIZE = 2048;

	public XmlSerializer() {
		this(Charset.forName(DEFAULT_CHARSET_NAME));
	}

	public XmlSerializer(Charset charset) {
		if (charset == null) {
			throw new IllegalArgumentException("charset is null.");
		}

		this.charset = charset;
	}

	public Charset getCharset() {
		return charset;
	}

	@Override
	public JtonObject readObject(InputStream inputStream)
			throws IOException, SerializationException {
		if (inputStream == null) {
			throw new IllegalArgumentException("inputStream is null.");
		}

		Reader reader = new BufferedReader(new InputStreamReader(inputStream, charset), BUFFER_SIZE);
		JtonObject element = readObject(reader);

		return element;
	}

	public JtonObject readObject(Reader reader)
			throws IOException, SerializationException {
		if (reader == null) {
			throw new IllegalArgumentException("reader is null.");
		}

		// Parse the XML stream
		XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
		xmlInputFactory.setProperty("javax.xml.stream.isCoalescing", true);

		Element document = null;

		try {
			XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(reader);

			Element current = null;

			while (xmlStreamReader.hasNext()) {
				int event = xmlStreamReader.next();

				switch (event) {
				case XMLStreamConstants.CHARACTERS: {
					if (!xmlStreamReader.isWhiteSpace()) {
						if (current != null) {
							current.text = xmlStreamReader.getText();
						}
					}
					break;
				}
				case XMLStreamConstants.START_ELEMENT: {
					// Create the element
					String prefix = xmlStreamReader.getPrefix();
					if (prefix != null && prefix.length() == 0) {
						prefix = null;
					}

					String localName = xmlStreamReader.getLocalName();

					Element element = new Element(localName);

					// Get the element's attributes
					for (int i = 0, n = xmlStreamReader.getAttributeCount(); i < n; i++) {
						String attributePrefix = xmlStreamReader.getAttributePrefix(i);
						if (attributePrefix != null && attributePrefix.length() == 0) {
							attributePrefix = null;
						}

						String attributeLocalName = xmlStreamReader.getAttributeLocalName(i);

						if ("type".equalsIgnoreCase(attributeLocalName)) {
							element.type = xmlStreamReader.getAttributeValue(i);
							break;
						}
					}

					if (current == null) {
						document = element;
					} else {
						current.add(element);
					}

					current = element;

					break;
				}
				case XMLStreamConstants.END_ELEMENT: {

					// Move up the stack
					if (current != null) {
						current = current.parent;
					}

					break;
				}
				default: {
					break;
				}
				}
			}
		} catch (XMLStreamException exception) {
			throw new SerializationException(exception);
		}

		return (JtonObject) document.toJton();
	}

	@Override
	public void writeObject(JtonObject object, OutputStream outputStream)
			throws IOException, SerializationException {
		if (outputStream == null) {
			throw new IllegalArgumentException("outputStream is null.");
		}

		Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream, charset), BUFFER_SIZE);
		writeObject(object, writer);
		writer.flush();
	}

	public void writeObject(JtonObject object, Writer writer)
			throws IOException, SerializationException {
		if (writer == null) {
			throw new IllegalArgumentException("writer is null.");
		}

		XMLOutputFactory output = XMLOutputFactory.newInstance();
		try {
			XMLStreamWriter xmlStreamWriter = output.createXMLStreamWriter(writer);
			xmlStreamWriter.writeStartDocument();
			writeObject(this.localName != null ? this.localName : DEFAULT_LOCALNAME, object, xmlStreamWriter);
			xmlStreamWriter.writeEndDocument();
		} catch (XMLStreamException exception) {
			throw new SerializationException(exception);
		}
	}

	private void writeObject(String localName, JtonObject object, XMLStreamWriter xmlStreamWriter)
			throws XMLStreamException, SerializationException {

		if (object.size() == 0) {
			xmlStreamWriter.writeEmptyElement(localName);
		} else {
			xmlStreamWriter.writeStartElement(localName);
		}

		// Write out the child nodes
		for (Map.Entry<String, JtonElement> entry : object.entrySet()) {
			String key = entry.getKey();
			JtonElement element = entry.getValue();

			// ---
			// boolean identifier = true;
			// StringBuilder keyStringBuilder = new StringBuilder();
			//
			// for (int j = 0, n = key.length(); j < n; j++) {
			// char cj = key.charAt(j);
			// identifier &= Character.isJavaIdentifierPart(cj);
			//
			// if (cj == '"') {
			// keyStringBuilder.append('\\');
			// }
			//
			// keyStringBuilder.append(cj);
			// }
			//
			// key = keyStringBuilder.toString();
			// ---

			if (element.isJtonNull()) {
				xmlStreamWriter.writeEmptyElement(key);
				xmlStreamWriter.writeAttribute("type", "null");
			} else {
				if (element.isJtonPrimitive()) {
					xmlStreamWriter.writeStartElement(key);
					String type;

					Object value = element.getPrimitiveValue();
					if (value instanceof Boolean) {
						type = "bool";
					} else if (value instanceof Integer) {
						type = "int";
					} else if (value instanceof BigInteger) {
						type = "bigint";
					} else if (value instanceof com.veracloud.jton.internal.LazilyParsedNumber) {
						type = "number";
					} else if (value instanceof java.sql.Date) {
						type = "sqldate";
					} else if (value instanceof java.sql.Time) {
						type = "sqltime";
					} else if (value instanceof java.sql.Timestamp) {
						type = "sqltstamp";
					} else {
						type = value.getClass().getSimpleName().toLowerCase();
					}

					xmlStreamWriter.writeAttribute("type", type);
					writeTextNode(element.getAsJtonPrimitive(), xmlStreamWriter);

				} else if (element.isJtonArray()) {
					writeArray(key, element.getAsJtonArray(), xmlStreamWriter);
				} else {
					writeObject(key, element.getAsJtonObject(), xmlStreamWriter);
				}

				xmlStreamWriter.writeEndElement();
			}
		}

	}

	private void writeArray(String key, JtonArray object, XMLStreamWriter xmlStreamWriter)
			throws XMLStreamException, SerializationException {
		if (object.size() == 0) {
			xmlStreamWriter.writeEmptyElement(key);
		}

		// Write out the child nodes
		for (JtonElement element : object) {
			if (element.isJtonNull()) {
				xmlStreamWriter.writeEmptyElement(key);
				xmlStreamWriter.writeAttribute("type", "null");
			} else {
				if (element.isJtonPrimitive()) {
					xmlStreamWriter.writeStartElement(key);
					String type;

					Object value = element.getPrimitiveValue();
					if (value instanceof Boolean) {
						type = "bool";
					} else if (value instanceof Integer) {
						type = "int";
					} else if (value instanceof BigInteger) {
						type = "bigint";
					} else if (value instanceof java.sql.Date) {
						type = "sqldate";
					} else if (value instanceof java.sql.Time) {
						type = "sqltime";
					} else if (value instanceof java.sql.Timestamp) {
						type = "sqltstamp";
					} else {
						type = value.getClass().getSimpleName().toLowerCase();
					}

					xmlStreamWriter.writeAttribute("type", type);
					writeTextNode(element.getAsJtonPrimitive(), xmlStreamWriter);

				} else if (element.isJtonArray()) {
					writeArray(key, element.getAsJtonArray(), xmlStreamWriter);
				} else {
					writeObject(key, element.getAsJtonObject(), xmlStreamWriter);
				}

				xmlStreamWriter.writeEndElement();
			}
		}
	}

	private void writeTextNode(JtonPrimitive object, XMLStreamWriter xmlStreamWriter)
			throws XMLStreamException, SerializationException {
		if (object.isString()) {
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

				// XXX case '\\':
				// XXX case '\"':
				// XXX case '\'':
				// {
				// stringBuilder.append("\\" + ci);
				// break;
				// }

				default: {
					if (charset.name().startsWith("UTF")
							|| ci <= 0xFF) {
						stringBuilder.append(ci);
					} else {
						stringBuilder.append("\\u");
						stringBuilder.append(String.format("%04x", (short) ci));
					}
				}
				}

			}
			xmlStreamWriter.writeCharacters(stringBuilder.toString());
		} else if (object.isNumber()) {
			Number number = object.getAsNumber();

			if (number instanceof Float) {
				Float f = (Float) number;
				if (f.isNaN()
						|| f.isInfinite()) {
					throw new SerializationException(number + " is not a valid value.");
				}
			} else if (number instanceof Double) {
				Double d = (Double) number;
				if (d.isNaN()
						|| d.isInfinite()) {
					throw new SerializationException(number + " is not a valid value.");
				}
			}

			xmlStreamWriter.writeCharacters(number.toString());
		} else if (object.isBoolean()) {
			xmlStreamWriter.writeCharacters(String.valueOf(object.getAsBoolean()));
		} else if (object.isDate()) {
			xmlStreamWriter.writeCharacters(object.getAsString());
		} else if (object.isSqlDate()) {
			xmlStreamWriter.writeCharacters(object.getAsString());
		} else if (object.isSqlTime()) {
			xmlStreamWriter.writeCharacters(object.getAsString());
		} else if (object.isSqlTimestamp()) {
			xmlStreamWriter.writeCharacters(object.getAsString());
		}
	}

	@Override
	public String getMIMEType(JtonObject object) {
		return MIME_TYPE + "; charset=" + charset.name();
	}

	@SuppressWarnings("serial")
	private class Element extends ArrayList<Element> {
		Element parent = null;
		String type = null;
		String text = null;

		private final String name;

		private final Map<String, Boolean> members = new HashMap<String, Boolean>();

		public Element(String name) {
			this.name = name;
		}

		@Override
		public boolean add(Element element) {
			if (element.parent != null) {
				return false;
			}
			if (super.add(element)) {
				element.parent = this;
				String prop = element.name;
				if (members.containsKey(prop)) {
					if (!members.get(prop)) {
						members.put(prop, Boolean.TRUE);
					}
				} else {
					members.put(prop, Boolean.FALSE);
				}
				return true;
			}
			return false;
		}

		JtonElement toJton() throws SerializationException {
			if (type == null) { // handle NULL
				JtonObject me = new JtonObject();
				for (Element e : this) {
					if (members.get(e.name)) {
						JtonArray arr = me.get(e.name).getAsJtonArray(null);
						if (arr == null) {
							me.add(e.name, arr = new JtonArray());
						}
						arr.add(e.toJton());
					} else {
						me.add(e.name, e.toJton());
					}
				}
				return me;
			} else {
				try {
					if ("null".equals(type)) {
						return JtonNull.INSTANCE;
					} else if ("string".equals(type)) {
						return new JtonPrimitive(text);
					} else if ("char".equals(type)) {
						return new JtonPrimitive(Character.valueOf(text.charAt(0)));
					} else if ("byte".equals(type)) {
						return new JtonPrimitive(Byte.valueOf(text));
					} else if ("short".equals(type)) {
						return new JtonPrimitive(Short.valueOf(text));
					} else if ("int".equals(type)) {
						return new JtonPrimitive(Integer.valueOf(text));
					} else if ("long".equals(type)) {
						return new JtonPrimitive(Long.valueOf(text));
					} else if ("float".equals(type)) {
						return new JtonPrimitive(Float.valueOf(text));
					} else if ("double".equals(type)) {
						return new JtonPrimitive(Double.valueOf(text));
					} else if ("bigint".equals(type)) {
						return new JtonPrimitive(new BigInteger(text));
					} else if ("bigdecimal".equals(type)) {
						return new JtonPrimitive(new BigDecimal(text));
					} else if ("number".equals(type)) {
						return new JtonPrimitive(new LazilyParsedNumber(text));
					} else if ("date".equals(type)) {
						return new JtonPrimitive(DatatypeConverter.parseDateTime(text).getTime());
					} else if ("sqldate".equals(type)) {
						return new JtonPrimitive(new java.sql.Date(DatatypeConverter.parseDate(text).getTime().getTime()));
					} else if ("sqltime".equals(type)) {
						return new JtonPrimitive(new java.sql.Time(DatatypeConverter.parseDate(text).getTime().getTime()));
					} else if ("sqltstamp".equals(type)) {
						return new JtonPrimitive(new java.sql.Timestamp(DatatypeConverter.parseDate(text).getTime().getTime()));
					} else {
						throw new SerializationException("Unknown type: " + type);
					}
				} catch (Exception e) {
					throw new SerializationException(e.getMessage(), e);
				}
			}
		}
	}

	//
	// Static helpers
	//

	/**
	 * Converts a XML value to a Java object.
	 *
	 * @param xml
	 *          The XML value.
	 *
	 * @return The parsed object.
	 */
	public static JtonObject parse(String xml) throws SerializationException {
		XmlSerializer xmlSerializer = new XmlSerializer();

		try {
			return xmlSerializer.readObject(new StringReader(xml));
		} catch (IOException exception) {
			throw new JtonIOException(exception);
		}
	}

	/**
	 * Converts a object to a XML string representation.
	 *
	 * @param object
	 *          The object to convert.
	 *
	 * @return The resulting XML string.
	 */
	public static String toString(JtonObject object) throws SerializationException {
		XmlSerializer xmlSerializer = new XmlSerializer();

		StringWriter writer = new StringWriter();

		try {
			xmlSerializer.writeObject(object, writer);
		} catch (IOException exception) {
			throw new JtonIOException(exception);
		}

		return writer.toString();
	}

}
