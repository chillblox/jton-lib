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
package com.veracloud.jton;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import javax.xml.bind.DatatypeConverter;

import com.veracloud.jton.internal.$Gson$Preconditions;
import com.veracloud.jton.internal.LazilyParsedNumber;

/**
 * A class representing a Json primitive value. A primitive value is either a
 * String, a Java primitive, or a Java primitive wrapper type.
 */
public class JtonPrimitive extends JtonElement {

  /**
   * Primitive types.
   */
  private static final Class<?>[] PRIMITIVE_TYPES = {
      Integer.class, int.class,
      Long.class, long.class,
      Short.class, short.class,
      Float.class, float.class,
      Double.class, double.class,
      Byte.class, byte.class,
      Boolean.class, boolean.class,
      Character.class, char.class
  };

  // ---

  /**
   * Primitive value.
   */
  private Object value;
  private transient Object _value;

  /**
   * Flag indicating that the primitive value is transient.
   */
  private transient boolean jtonTransient = false;

  /**
   * Create a primitive using the specified Object. It must be an instance of
   * {@link Number}, a {@link Date}, a Java primitive type, or a String.
   *
   * @param primitive
   *          the value to create the primitive with.
   */
  public JtonPrimitive(Object primitive) {
    setPrimitiveValue(primitive);
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
    setPrimitiveValue(primitive);
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
  public Object getPrimitiveValue() {
    return (jtonTransient) ? _value : value;
  }

  final void setPrimitiveValue(Object primitive) {
    if (isTransient()) {
      this._value = primitive;
    } else if (primitive instanceof Character) {
      // convert characters to strings since in JSON, characters are represented
      // as a single character string
      char c = ((Character) primitive).charValue();
      this.value = String.valueOf(c);
    } else {
      $Gson$Preconditions.checkArgument(isPrimitiveOrStringOrNumberOrDate(primitive));
      this.value = primitive;
    }
  }

  /**
   * Check whether this primitive contains a boolean value.
   *
   * @return true if this primitive contains a boolean value, false otherwise.
   */
  public boolean isBoolean() {
    return getPrimitiveValue() instanceof Boolean;
  }

  /**
   * convenience method to get this element as a boolean value.
   *
   * @return get this element as a primitive boolean value.
   */
  @Override
  public boolean getAsBoolean() {
    if (isBoolean()) {
      return (Boolean) getPrimitiveValue();
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
    return getPrimitiveValue() instanceof Number;
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
    Object value = getPrimitiveValue();
    return value instanceof String ? new LazilyParsedNumber((String) value) : (Number) value;
  }

  /**
   * Check whether this primitive contains a String value.
   *
   * @return true if this primitive contains a String value, false otherwise.
   */
  public boolean isString() {
    return getPrimitiveValue() instanceof String;
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
      return String.valueOf(getAsBoolean());
    } else if (isDate()) {
      Date d = getAsDate();
      Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
      c.setTime(d);
      if (d instanceof java.sql.Date) {
        return DatatypeConverter.printDate(c);
      } else if (d instanceof java.sql.Time) {
        return DatatypeConverter.printTime(c);
      } else {
        return DatatypeConverter.printDateTime(c);
      }
    } else {
      return (String) getPrimitiveValue();
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
    Object value = getPrimitiveValue();
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
    Object value = getPrimitiveValue();
    return value instanceof BigInteger ? (BigInteger) value : new BigInteger(value.toString());
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

  /**
   * convenience method to get this element as a primitive byte.
   *
   * @return get this element as a primitive byte.
   * @throws NumberFormatException
   *           if the value contained is not a valid integer.
   */
  @Override
  public byte getAsByte() {
    return isNumber() ? getAsNumber().byteValue() : Byte.parseByte(getAsString());
  }

  /**
   * convenience method to get this element as a primitive char.
   *
   * @return get this element as a primitive char.
   */
  @Override
  public char getAsChar() {
    return getAsString().charAt(0);
  }

  /**
   * Check whether this primitive contains a Date value.
   *
   * @return true if this primitive contains a Date value, false otherwise.
   */
  public boolean isDate() {
    return getPrimitiveValue() instanceof Date;
  }

  /**
   * 
   * @throws IllegalArgumentException
   */
  @Override
  public Date getAsDate() {
    return isDate() ? (Date) getPrimitiveValue() : DatatypeConverter.parseDateTime(getAsString()).getTime();
  }

  /**
   * Check whether this primitive contains a SQL Date value.
   *
   * @return true if this primitive contains a SQL Date value, false otherwise.
   */
  public boolean isSqlDate() {
    return getPrimitiveValue() instanceof java.sql.Date;
  }

  @Override
  public java.sql.Date getAsSqlDate() {
    return isSqlDate() ? (java.sql.Date) getPrimitiveValue()
        : new java.sql.Date(DatatypeConverter.parseDate(getAsString()).getTime().getTime());
  }

  /**
   * Check whether this primitive contains a SQL Time value.
   *
   * @return true if this primitive contains a SQL Time value, false otherwise.
   */
  public boolean isSqlTime() {
    return getPrimitiveValue() instanceof java.sql.Time;
  }

  @Override
  public java.sql.Time getAsSqlTime() {
    return isSqlTime() ? (java.sql.Time) getPrimitiveValue()
        : new java.sql.Time(DatatypeConverter.parseDate(getAsString()).getTime().getTime());
  }

  /**
   * Check whether this primitive contains a SQL Timestamp value.
   *
   * @return true if this primitive contains a SQL Timestamp value, false
   *         otherwise.
   */
  public boolean isSqlTimestamp() {
    return getPrimitiveValue() instanceof java.sql.Timestamp;
  }

  @Override
  public java.sql.Timestamp getAsSqlTimestamp() {
    return isSqlTimestamp() ? (java.sql.Timestamp) getPrimitiveValue()
        : new java.sql.Timestamp(DatatypeConverter.parseDate(getAsString()).getTime().getTime());
  }

  private static boolean isPrimitiveOrStringOrNumberOrDate(Object target) {
    if (target instanceof String || target instanceof Number || target instanceof Date) {
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
    Object _value = getPrimitiveValue();
    if (_value == null) {
      return 31;
    }
    // Using recommended hashing algorithm from
    // Effective Java for longs and doubles
    if (isIntegral(this)) {
      long value = getAsNumber().longValue();
      return (int) (value ^ (value >>> 32));
    }
    if (_value instanceof Number) {
      long value = Double.doubleToLongBits(getAsNumber().doubleValue());
      return (int) (value ^ (value >>> 32));
    }
    return _value.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    Object value = getPrimitiveValue();
    JtonPrimitive other = (JtonPrimitive) obj;
    if (value == null) {
      return other.getPrimitiveValue() == null;
    }
    if (isIntegral(this) && isIntegral(other)) {
      return getAsNumber().longValue() == other.getAsNumber().longValue();
    }
    if (value instanceof Number && other.getPrimitiveValue() instanceof Number) {
      double a = getAsNumber().doubleValue();
      // Java standard types other than double return true for two NaN. So, need
      // special handling for double.
      double b = other.getAsNumber().doubleValue();
      return a == b || (Double.isNaN(a) && Double.isNaN(b));
    }
    return value.equals(other.getPrimitiveValue());
  }

  /**
   * Returns true if the specified number is an integral type (Long, Integer,
   * Short, Byte, BigInteger)
   */
  private static boolean isIntegral(JtonPrimitive primitive) {
    if (primitive.getPrimitiveValue() instanceof Number) {
      Number number = (Number) primitive.getPrimitiveValue();
      return number instanceof BigInteger || number instanceof Long || number instanceof Integer
          || number instanceof Short || number instanceof Byte;
    }
    return false;
  }
}
