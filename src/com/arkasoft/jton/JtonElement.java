/*
 * Copyright (C) 2008 Google Inc.
 *
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

package com.arkasoft.jton;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import com.arkasoft.jton.internal.Streams;
import com.arkasoft.jton.stream.JsonWriter;

/**
 * A class representing an element of Json. It could either be a
 * {@link JtonObject}, a {@link JtonArray}, a {@link JtonPrimitive} or a
 * {@link JtonNull}.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public abstract class JtonElement {
	/**
	 * Returns a deep copy of this element. Immutable elements like primitives and
	 * nulls are not copied.
	 */
	abstract JtonElement deepCopy();

	/**
	 * provides check for verifying if this element is an array or not.
	 *
	 * @return true if this element is of type {@link JtonArray}, false otherwise.
	 */
	public boolean isJtonArray() {
		return this instanceof JtonArray;
	}

	/**
	 * provides check for verifying if this element is a Json object or not.
	 *
	 * @return true if this element is of type {@link JtonObject}, false
	 *         otherwise.
	 */
	public boolean isJtonObject() {
		return this instanceof JtonObject;
	}

	/**
	 * provides check for verifying if this element is a primitive or not.
	 *
	 * @return true if this element is of type {@link JtonPrimitive}, false
	 *         otherwise.
	 */
	public boolean isJtonPrimitive() {
		return this instanceof JtonPrimitive;
	}

	/**
	 * provides check for verifying if this element represents a null value or
	 * not.
	 *
	 * @return true if this element is of type {@link JtonNull}, false otherwise.
	 * @since 1.2
	 */
	public boolean isJtonNull() {
		return this instanceof JtonNull;
	}
	
	/**
	 * provides check for verifying if this element is a transient or not.
	 * 
	 * @return true if this element is transient, false otherwise.
	 */
	public boolean isTransient() {
		return false;
	}

	/**
	 * convenience method to get this element as a {@link JtonObject}. If the
	 * element is of some other type, a {@link IllegalStateException} will result.
	 * Hence it is best to use this method after ensuring that this element is of
	 * the desired type by calling {@link #isJtonObject()} first.
	 *
	 * @return get this element as a {@link JtonObject}.
	 * @throws IllegalStateException
	 *           if the element is of another type.
	 */
	public JtonObject getAsJtonObject() {
		if (isJtonObject()) {
			return (JtonObject) this;
		}
		throw new IllegalStateException("Not a JTON Object: " + this);
	}

	public JtonObject getAsJtonObject(JtonObject fallback) {
		return isJtonObject() ? getAsJtonObject() : fallback;
	}

	/**
	 * convenience method to get this element as a {@link JtonArray}. If the
	 * element is of some other type, a {@link IllegalStateException} will result.
	 * Hence it is best to use this method after ensuring that this element is of
	 * the desired type by calling {@link #isJtonArray()} first.
	 *
	 * @return get this element as a {@link JtonArray}.
	 * @throws IllegalStateException
	 *           if the element is of another type.
	 */
	public JtonArray getAsJtonArray() {
		if (isJtonArray()) {
			return (JtonArray) this;
		}
		throw new IllegalStateException("This is not a JSON Array.");
	}

	public JtonArray getAsJtonArray(JtonArray fallback) {
		return isJtonArray() ? getAsJtonArray() : fallback;
	}

	/**
	 * convenience method to get this element as a {@link JtonPrimitive}. If the
	 * element is of some other type, a {@link IllegalStateException} will result.
	 * Hence it is best to use this method after ensuring that this element is of
	 * the desired type by calling {@link #isJtonPrimitive()} first.
	 *
	 * @return get this element as a {@link JtonPrimitive}.
	 * @throws IllegalStateException
	 *           if the element is of another type.
	 */
	public JtonPrimitive getAsJtonPrimitive() {
		if (isJtonPrimitive()) {
			return (JtonPrimitive) this;
		}
		throw new IllegalStateException("This is not a JSON Primitive.");
	}

	/**
	 * convenience method to get this element as a {@link JtonNull}. If the
	 * element is of some other type, a {@link IllegalStateException} will result.
	 * Hence it is best to use this method after ensuring that this element is of
	 * the desired type by calling {@link #isJtonNull()} first.
	 *
	 * @return get this element as a {@link JtonNull}.
	 * @throws IllegalStateException
	 *           if the element is of another type.
	 * @since 1.2
	 */
	public JtonNull getAsJtonNull() {
		if (isJtonNull()) {
			return (JtonNull) this;
		}
		throw new IllegalStateException("This is not a JTON Null.");
	}

	/**
	 * convenience method to get this element as a boolean value.
	 *
	 * @return get this element as a primitive boolean value.
	 * @throws ClassCastException
	 *           if the element is of not a {@link JtonPrimitive} and is not a
	 *           valid boolean value.
	 * @throws IllegalStateException
	 *           if the element is of the type {@link JtonArray} but contains more
	 *           than a single element.
	 */
	public boolean getAsBoolean() {
		throw new UnsupportedOperationException(getClass().getSimpleName());
	}

	public boolean getAsBoolean(boolean fallback) {
		try {
			return isJtonPrimitive() ? getAsBoolean() : fallback;
		} catch (ClassCastException e) {
			return fallback;
		}
	}

	/**
	 * convenience method to get this element as a {@link Boolean} value.
	 *
	 * @return get this element as a {@link Boolean} value.
	 * @throws ClassCastException
	 *           if the element is of not a {@link JtonPrimitive} and is not a
	 *           valid boolean value.
	 * @throws IllegalStateException
	 *           if the element is of the type {@link JtonArray} but contains more
	 *           than a single element.
	 */
	@Deprecated
	Boolean getAsBooleanWrapper() {
		throw new UnsupportedOperationException(getClass().getSimpleName());
	}

	/**
	 * convenience method to get this element as a {@link Number}.
	 *
	 * @return get this element as a {@link Number}.
	 * @throws ClassCastException
	 *           if the element is of not a {@link JtonPrimitive} and is not a
	 *           valid number.
	 * @throws IllegalStateException
	 *           if the element is of the type {@link JtonArray} but contains more
	 *           than a single element.
	 */
	public Number getAsNumber() {
		throw new UnsupportedOperationException(getClass().getSimpleName());
	}

	/**
	 * convenience method to get this element as a string value.
	 *
	 * @return get this element as a string value.
	 * @throws ClassCastException
	 *           if the element is of not a {@link JtonPrimitive} and is not a
	 *           valid string value.
	 * @throws IllegalStateException
	 *           if the element is of the type {@link JtonArray} but contains more
	 *           than a single element.
	 */
	public String getAsString() {
		throw new UnsupportedOperationException(getClass().getSimpleName());
	}

	public String getAsString(String fallback) {
		try {
			return isJtonPrimitive() ? getAsString() : fallback;
		} catch (ClassCastException e) {
			return fallback;
		}
	}

	/**
	 * convenience method to get this element as a primitive double value.
	 *
	 * @return get this element as a primitive double value.
	 * @throws ClassCastException
	 *           if the element is of not a {@link JtonPrimitive} and is not a
	 *           valid double value.
	 * @throws IllegalStateException
	 *           if the element is of the type {@link JtonArray} but contains more
	 *           than a single element.
	 */
	public double getAsDouble() {
		throw new UnsupportedOperationException(getClass().getSimpleName());
	}

	public double getAsDouble(double fallback) {
		try {
			return isJtonPrimitive() ? getAsDouble() : fallback;
		} catch (ClassCastException e) {
			return fallback;
		}
	}

	/**
	 * convenience method to get this element as a primitive float value.
	 *
	 * @return get this element as a primitive float value.
	 * @throws ClassCastException
	 *           if the element is of not a {@link JtonPrimitive} and is not a
	 *           valid float value.
	 * @throws IllegalStateException
	 *           if the element is of the type {@link JtonArray} but contains more
	 *           than a single element.
	 */
	public float getAsFloat() {
		throw new UnsupportedOperationException(getClass().getSimpleName());
	}

	public float getAsFloat(float fallback) {
		try {
			return isJtonPrimitive() ? getAsFloat() : fallback;
		} catch (ClassCastException e) {
			return fallback;
		}
	}

	/**
	 * convenience method to get this element as a primitive long value.
	 *
	 * @return get this element as a primitive long value.
	 * @throws ClassCastException
	 *           if the element is of not a {@link JtonPrimitive} and is not a
	 *           valid long value.
	 * @throws IllegalStateException
	 *           if the element is of the type {@link JtonArray} but contains more
	 *           than a single element.
	 */
	public long getAsLong() {
		throw new UnsupportedOperationException(getClass().getSimpleName());
	}

	public long getAsLong(long fallback) {
		try {
			return isJtonPrimitive() ? getAsLong() : fallback;
		} catch (ClassCastException e) {
			return fallback;
		}
	}

	/**
	 * convenience method to get this element as a primitive integer value.
	 *
	 * @return get this element as a primitive integer value.
	 * @throws ClassCastException
	 *           if the element is of not a {@link JtonPrimitive} and is not a
	 *           valid integer value.
	 * @throws IllegalStateException
	 *           if the element is of the type {@link JtonArray} but contains more
	 *           than a single element.
	 */
	public int getAsInt() {
		throw new UnsupportedOperationException(getClass().getSimpleName());
	}

	public int getAsInt(int fallback) {
		try {
			return isJtonPrimitive() ? getAsInt() : fallback;
		} catch (ClassCastException e) {
			return fallback;
		}
	}

	/**
	 * convenience method to get this element as a primitive byte value.
	 *
	 * @return get this element as a primitive byte value.
	 * @throws ClassCastException
	 *           if the element is of not a {@link JtonPrimitive} and is not a
	 *           valid byte value.
	 * @throws IllegalStateException
	 *           if the element is of the type {@link JtonArray} but contains more
	 *           than a single element.
	 * @since 1.3
	 */
	public byte getAsByte() {
		throw new UnsupportedOperationException(getClass().getSimpleName());
	}

	public byte getAsByte(byte fallback) {
		try {
			return isJtonPrimitive() ? getAsByte() : fallback;
		} catch (ClassCastException e) {
			return fallback;
		}
	}

	/**
	 * convenience method to get this element as a primitive character value.
	 *
	 * @return get this element as a primitive char value.
	 * @throws ClassCastException
	 *           if the element is of not a {@link JtonPrimitive} and is not a
	 *           valid char value.
	 * @throws IllegalStateException
	 *           if the element is of the type {@link JtonArray} but contains more
	 *           than a single element.
	 * @since 1.3
	 */
	public char getAsCharacter() {
		throw new UnsupportedOperationException(getClass().getSimpleName());
	}

	public char getAsCharacter(char fallback) {
		try {
			return isJtonPrimitive() ? getAsCharacter() : fallback;
		} catch (ClassCastException e) {
			return fallback;
		}
	}

	/**
	 * convenience method to get this element as a {@link BigDecimal}.
	 *
	 * @return get this element as a {@link BigDecimal}.
	 * @throws ClassCastException
	 *           if the element is of not a {@link JtonPrimitive}. * @throws
	 *           NumberFormatException if the element is not a valid
	 *           {@link BigDecimal}.
	 * @throws IllegalStateException
	 *           if the element is of the type {@link JtonArray} but contains more
	 *           than a single element.
	 * @since 1.2
	 */
	public BigDecimal getAsBigDecimal() {
		throw new UnsupportedOperationException(getClass().getSimpleName());
	}

	public BigDecimal getAsBigDecimal(BigDecimal fallback) {
		try {
			return isJtonPrimitive() ? getAsBigDecimal() : fallback;
		} catch (ClassCastException e) {
			return fallback;
		}
	}

	/**
	 * convenience method to get this element as a {@link BigInteger}.
	 *
	 * @return get this element as a {@link BigInteger}.
	 * @throws ClassCastException
	 *           if the element is of not a {@link JtonPrimitive}.
	 * @throws NumberFormatException
	 *           if the element is not a valid {@link BigInteger}.
	 * @throws IllegalStateException
	 *           if the element is of the type {@link JtonArray} but contains more
	 *           than a single element.
	 * @since 1.2
	 */
	public BigInteger getAsBigInteger() {
		throw new UnsupportedOperationException(getClass().getSimpleName());
	}
	
	public BigInteger getAsBigInteger(BigInteger fallback) {
		try {
			return isJtonPrimitive() ? getAsBigInteger() : fallback;
		} catch (ClassCastException e) {
			return fallback;
		}
	}

	/**
	 * convenience method to get this element as a primitive short value.
	 *
	 * @return get this element as a primitive short value.
	 * @throws ClassCastException
	 *           if the element is of not a {@link JtonPrimitive} and is not a
	 *           valid short value.
	 * @throws IllegalStateException
	 *           if the element is of the type {@link JtonArray} but contains more
	 *           than a single element.
	 */
	public short getAsShort() {
		throw new UnsupportedOperationException(getClass().getSimpleName());
	}
	
	public short getAsShort(short fallback) {
		try {
			return isJtonPrimitive() ? getAsShort() : fallback;
		} catch (ClassCastException e) {
			return fallback;
		}
	}

	public Date getAsDate() {
		throw new UnsupportedOperationException(getClass().getSimpleName());
	}
	
	public Date getAsDate(Date fallback) {
		try {
			return isJtonPrimitive() ? getAsDate() : fallback;
		} catch (ClassCastException e) {
			return fallback;
		}
	}

	public java.sql.Date getAsSqlDate() {
		throw new UnsupportedOperationException(getClass().getSimpleName());
	}
	
	public java.sql.Date getAsSqlDate(java.sql.Date fallback) {
		try {
			return isJtonPrimitive() ? getAsSqlDate() : fallback;
		} catch (ClassCastException e) {
			return fallback;
		}
	}

	public java.sql.Time getAsSqlTime() {
		throw new UnsupportedOperationException(getClass().getSimpleName());
	}
	
	public java.sql.Time getAsSqlTime(java.sql.Time fallback) {
		try {
			return isJtonPrimitive() ? getAsSqlTime() : fallback;
		} catch (ClassCastException e) {
			return fallback;
		}
	}

	public java.sql.Timestamp getAsSqlTimestamp() {
		throw new UnsupportedOperationException(getClass().getSimpleName());
	}
	
	public java.sql.Timestamp getAsSqlTimestamp(java.sql.Timestamp fallback) {
		try {
			return isJtonPrimitive() ? getAsSqlTimestamp() : fallback;
		} catch (ClassCastException e) {
			return fallback;
		}
	}

	public Object getValue() {
		throw new UnsupportedOperationException(getClass().getSimpleName());
	}
	
	public Object getValue(Object fallback) {
		return isJtonPrimitive() ? getValue() : fallback;
	}

	/**
	 * Returns a String representation of this element.
	 */
	@Override
	public String toString() {
		try {
			StringWriter stringWriter = new StringWriter();
			JsonWriter jsonWriter = new JsonWriter(stringWriter);
			jsonWriter.setLenient(true);
			Streams.write(this, jsonWriter);
			return stringWriter.toString();
		} catch (IOException e) {
			throw new AssertionError(e);
		}
	}
}
