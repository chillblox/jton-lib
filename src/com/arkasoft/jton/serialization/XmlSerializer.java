package com.arkasoft.jton.serialization;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.arkasoft.jton.JtonArray;
import com.arkasoft.jton.JtonElement;
import com.arkasoft.jton.JtonObject;
import com.arkasoft.jton.JtonPrimitive;

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

	public JtonObject readObject(Reader reader) throws SerializationException {
		if (reader == null) {
			throw new IllegalArgumentException("reader is null.");
		}

		// Parse the XML stream
		XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
		xmlInputFactory.setProperty("javax.xml.stream.isCoalescing", true);

		JtonObject document = null;

		try {
			XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(reader);

			JtonElement current = null;

			while (xmlStreamReader.hasNext()) {
				int event = xmlStreamReader.next();

				switch (event) {
				case XMLStreamConstants.CHARACTERS: {

					break;
				}
				case XMLStreamConstants.START_ELEMENT: {
					// Create the element
					String prefix = xmlStreamReader.getPrefix();
					if (prefix != null && prefix.length() == 0) {
						prefix = null;
					}

					String localName = xmlStreamReader.getLocalName();
					
					if (current == null) {
						
					}

					break;
				}
				case XMLStreamConstants.END_ELEMENT: {

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

		return document;

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
				xmlStreamWriter.writeStartElement(key);
				xmlStreamWriter.writeCharacters("null");
				xmlStreamWriter.writeEndElement();
			} else if (element.isJtonPrimitive()) {
				xmlStreamWriter.writeStartElement(key);
				String type;

				Object value = element.getValue();
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
				xmlStreamWriter.writeEndElement();

			} else if (element.isJtonArray()) {
				writeArray(key, element.getAsJtonArray(), xmlStreamWriter);
			} else {
				writeObject(key, element.getAsJtonObject(), xmlStreamWriter);
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
				xmlStreamWriter.writeStartElement(key);
				xmlStreamWriter.writeCharacters("null");
				xmlStreamWriter.writeEndElement();
			} else if (element.isJtonPrimitive()) {
				xmlStreamWriter.writeStartElement(key);
				String type;

				Object value = element.getValue();
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
				xmlStreamWriter.writeEndElement();

			} else if (element.isJtonArray()) {
				writeArray(key, element.getAsJtonArray(), xmlStreamWriter);
			} else {
				writeObject(key, element.getAsJtonObject(), xmlStreamWriter);
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

				case '\\':
				case '\"':
				case '\'': {
					stringBuilder.append("\\" + ci);
					break;
				}

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

}
