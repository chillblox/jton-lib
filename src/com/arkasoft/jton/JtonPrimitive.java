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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;

import javax.xml.bind.DatatypeConverter;

import com.arkasoft.jton.internal.$Gson$Preconditions;
import com.arkasoft.jton.internal.LazilyParsedNumber;

/**
 * A class representing a Json primitive value. A primitive value is either a
 * String, a Java primitive, or a Java primitive wrapper type.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public final class JtonPrimitive extends JtonElement {

	private static final Class<?>[] PRIMITIVE_TYPES = {
			int.class, long.class, short.class, float.class, double.class, byte.class, boolean.class, char.class,
			Integer.class, Long.class, Short.class, Float.class, Double.class, Byte.class, Boolean.class, Character.class
	};

	private boolean jtonTransient = false;

	private Object value;

//	/**
//	 * Create a primitive containing a boolean value.
//	 *
//	 * @param bool
//	 *          the value to create the primitive with.
//	 */
//	public JtonPrimitive(Boolean bool) {
//		setValue(bool);
//	}
//
//	/**
//	 * Create a primitive containing a {@link Number}.
//	 *
//	 * @param number
//	 *          the value to create the primitive with.
//	 */
//	public JtonPrimitive(Number number) {
//		setValue(number);
//	}
//
//	/**
//	 * Create a primitive containing a String value.
//	 *
//	 * @param string
//	 *          the value to create the primitive with.
//	 */
//	public JtonPrimitive(String string) {
//		setValue(string);
//	}
//
//	/**
//	 * Create a primitive containing a character. The character is turned into a
//	 * one character String since Json only supports String.
//	 *
//	 * @param c
//	 *          the value to create the primitive with.
//	 */
//	public JtonPrimitive(Character c) {
//		setValue(c);
//	}
//
//	/**
//	 * Create a primitive containing a {@link Date}.
//	 *
//	 * @param c
//	 *          the value to create the primitive with.
//	 */
//	public JtonPrimitive(Date c) {
//		setValue(c);
//	}

	/**
	 * Create a primitive using the specified Object. It must be an instance of
	 * {@link Number}, a {@link Date}, a Java primitive type, or a String.
	 *
	 * @param primitive
	 *          the value to create the primitive with.
	 */
	public JtonPrimitive(Object primitive) {
		setValue(primitive);
	}

	/**
	 * Create a primitive using the specified Object. It must be an instance of
	 * {@link Number}, a {@link Date}, a Java primitive type, or a String;
	 * otherwise {@code jtonTransient} parameter must be set to {@code true}.
	 * 
	 * @param primitive
	 *          the value to create the primitive with.
	 * @param jtonTransient
	 *          whether this primitive value is transient or not.
	 */
	JtonPrimitive(Object primitive, boolean jtonTransient) {
		this.jtonTransient = jtonTransient;
		setValue(primitive);
	}

	@Override
	public JtonPrimitive deepCopy() {
		if (!isTransient()) {
			return new JtonPrimitive(value);
		}
		return this;
	}

	@Override
	public boolean isTransient() {
		return jtonTransient;
	}

	@Override
	public Object getValue() {
		return value;
	}

	void setValue(Object primitive) {
		if (isTransient()) {
			this.value = primitive;
		} else if (primitive instanceof Character) {
			// convert characters to strings since in JSON, characters are represented
			// as a single character string
			char c = ((Character) primitive).charValue();
			this.value = String.valueOf(c);
		} else {
			$Gson$Preconditions.checkArgument(primitive instanceof Number 
					|| primitive instanceof Date 
					|| isPrimitiveOrString(primitive));
			this.value = primitive;
		}
	}

	/**
	 * Check whether this primitive contains a boolean value.
	 *
	 * @return true if this primitive contains a boolean value, false otherwise.
	 */
	public boolean isBoolean() {
		return value instanceof Boolean;
	}

	/**
	 * convenience method to get this element as a {@link Boolean}.
	 *
	 * @return get this element as a {@link Boolean}.
	 */
	@Override
	Boolean getAsBooleanWrapper() {
		return (Boolean) value;
	}

	/**
	 * convenience method to get this element as a boolean value.
	 *
	 * @return get this element as a primitive boolean value.
	 */
	@Override
	public boolean getAsBoolean() {
		if (isBoolean()) {
			return getAsBooleanWrapper().booleanValue();
		} else {
			// Check to see if the value as a String is "true" in any case.
			return Boolean.parseBoolean(getAsString());
		}
	}

	/**
	 * Check whether this primitive contains a Number.
	 *
	 * @return true if this primitive contains a Number, false otherwise.
	 */
	public boolean isNumber() {
		return value instanceof Number;
	}

	/**
	 * convenience method to get this element as a Number.
	 *
	 * @return get this element as a Number.
	 * @throws NumberFormatException
	 *           if the value contained is not a valid Number.
	 */
	@Override
	public Number getAsNumber() {
		return value instanceof String ? new LazilyParsedNumber((String) value) : (Number) value;
	}

	/**
	 * Check whether this primitive contains a String value.
	 *
	 * @return true if this primitive contains a String value, false otherwise.
	 */
	public boolean isString() {
		return value instanceof String;
	}

	/**
	 * convenience method to get this element as a String.
	 *
	 * @return get this element as a String.
	 */
	@Override
	public String getAsString() {
		if (isNumber()) {
			return getAsNumber().toString();
		} else if (isBoolean()) {
			return getAsBooleanWrapper().toString();
		} else if (isDate()) {
			Date d = getAsDate();
			Calendar c = Calendar.getInstance();
			c.setTime(d);
			if (d instanceof java.sql.Date) {
				return DatatypeConverter.printDate(c);
			} else if (d instanceof java.sql.Time) {
				return DatatypeConverter.printTime(c);
			} else {
				return DatatypeConverter.printDateTime(c);
			}
		} else {
			return (String) value;
		}
	}

	/**
	 * convenience method to get this element as a primitive double.
	 *
	 * @return get this element as a primitive double.
	 * @throws NumberFormatException
	 *           if the value contained is not a valid double.
	 */
	@Override
	public double getAsDouble() {
		return isNumber() ? getAsNumber().doubleValue() : Double.parseDouble(getAsString());
	}

	/**
	 * convenience method to get this element as a {@link BigDecimal}.
	 *
	 * @return get this element as a {@link BigDecimal}.
	 * @throws NumberFormatException
	 *           if the value contained is not a valid {@link BigDecimal}.
	 */
	@Override
	public BigDecimal getAsBigDecimal() {
		return value instanceof BigDecimal ? (BigDecimal) value : new BigDecimal(value.toString());
	}

	/**
	 * convenience method to get this element as a {@link BigInteger}.
	 *
	 * @return get this element as a {@link BigInteger}.
	 * @throws NumberFormatException
	 *           if the value contained is not a valid {@link BigInteger}.
	 */
	@Override
	public BigInteger getAsBigInteger() {
		return value instanceof BigInteger ?
				(BigInteger) value : new BigInteger(value.toString());
	}

	/**
	 * convenience method to get this element as a float.
	 *
	 * @return get this element as a float.
	 * @throws NumberFormatException
	 *           if the value contained is not a valid float.
	 */
	@Override
	public float getAsFloat() {
		return isNumber() ? getAsNumber().floatValue() : Float.parseFloat(getAsString());
	}

	/**
	 * convenience method to get this element as a primitive long.
	 *
	 * @return get this element as a primitive long.
	 * @throws NumberFormatException
	 *           if the value contained is not a valid long.
	 */
	@Override
	public long getAsLong() {
		return isNumber() ? getAsNumber().longValue() : Long.parseLong(getAsString());
	}

	/**
	 * convenience method to get this element as a primitive short.
	 *
	 * @return get this element as a primitive short.
	 * @throws NumberFormatException
	 *           if the value contained is not a valid short value.
	 */
	@Override
	public short getAsShort() {
		return isNumber() ? getAsNumber().shortValue() : Short.parseShort(getAsString());
	}

	/**
	 * convenience method to get this element as a primitive integer.
	 *
	 * @return get this element as a primitive integer.
	 * @throws NumberFormatException
	 *           if the value contained is not a valid integer.
	 */
	@Override
	public int getAsInt() {
		return isNumber() ? getAsNumber().intValue() : Integer.parseInt(getAsString());
	}

	@Override
	public byte getAsByte() {
		return isNumber() ? getAsNumber().byteValue() : Byte.parseByte(getAsString());
	}

	@Override
	public char getAsCharacter() {
		return getAsString().charAt(0);
	}

	public boolean isDate() {
		return value instanceof Date;
	}

	public boolean isSqlDate() {
		return value instanceof java.sql.Date;
	}

	public boolean isSqlTime() {
		return value instanceof java.sql.Time;
	}

	public boolean isSqlTimestamp() {
		return value instanceof java.sql.Timestamp;
	}

	/**
	 * 
	 * @throws IllegalArgumentException
	 */
	@Override
	public Date getAsDate() {
		return isDate() ? (Date) value : DatatypeConverter.parseDateTime(getAsString()).getTime();
	}

	@Override
	public java.sql.Date getAsSqlDate() {
		return isSqlDate() ? (java.sql.Date) value : new java.sql.Date(DatatypeConverter.parseDate(getAsString()).getTime().getTime());
	}

	@Override
	public java.sql.Time getAsSqlTime() {
		return isSqlTime() ? (java.sql.Time) value : new java.sql.Time(DatatypeConverter.parseDate(getAsString()).getTime().getTime());
	}

	@Override
	public java.sql.Timestamp getAsSqlTimestamp() {
		return isSqlTimestamp() ? (java.sql.Timestamp) value : new java.sql.Timestamp(DatatypeConverter.parseDate(getAsString()).getTime().getTime());
	}

	private static boolean isPrimitiveOrString(Object target) {
		if (target instanceof String) {
			return true;
		}

		Class<?> classOfPrimitive = target.getClass();
		for (Class<?> standardPrimitive : PRIMITIVE_TYPES) {
			if (standardPrimitive.isAssignableFrom(classOfPrimitive)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		if (value == null) {
			return 31;
		}
		// Using recommended hashing algorithm from Effective Java for longs and
		// doubles
		if (isIntegral(this)) {
			long value = getAsNumber().longValue();
			return (int) (value ^ (value >>> 32));
		}
		if (value instanceof Number) {
			long value = Double.doubleToLongBits(getAsNumber().doubleValue());
			return (int) (value ^ (value >>> 32));
		}
		return value.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		JtonPrimitive other = (JtonPrimitive) obj;
		if (value == null) {
			return other.value == null;
		}
		if (isIntegral(this) && isIntegral(other)) {
			return getAsNumber().longValue() == other.getAsNumber().longValue();
		}
		if (value instanceof Number && other.value instanceof Number) {
			double a = getAsNumber().doubleValue();
			// Java standard types other than double return true for two NaN. So, need
			// special handling for double.
			double b = other.getAsNumber().doubleValue();
			return a == b || (Double.isNaN(a) && Double.isNaN(b));
		}
		return value.equals(other.value);
	}

	/**
	 * Returns true if the specified number is an integral type (Long, Integer,
	 * Short, Byte, BigInteger)
	 */
	private static boolean isIntegral(JtonPrimitive primitive) {
		if (primitive.value instanceof Number) {
			Number number = (Number) primitive.value;
			return number instanceof BigInteger || number instanceof Long || number instanceof Integer
					|| number instanceof Short || number instanceof Byte;
		}
		return false;
	}
}
