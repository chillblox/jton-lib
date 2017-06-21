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
import java.util.Date;

import com.veracloud.jton.serialization.JsonSerializer;
import com.veracloud.jton.serialization.SerializationException;

/**
 * A class representing an element of Jton. It could either be a
 * {@link JtonObject}, a {@link JtonArray}, a {@link JtonPrimitive} or a
 * {@link JtonNull}.
 */
public abstract class JtonElement {

  /**
   * Returns a deep copy of this element. Immutable elements like primitives and
   * nulls are not copied.
   */
  public abstract JtonElement deepCopy();

  // -----------------------------------------------------------------------
  // TYPE CHECKING
  // -----------------------------------------------------------------------

  /**
   * Provides check for verifying if this element is a {@link JtonArray} or not.
   *
   * @return true if this element is of type {@link JtonArray}, false otherwise.
   */
  public boolean isJtonArray() {
    return this instanceof JtonArray;
  }

  /**
   * Provides check for verifying if this element is a {@link JtonObject} or
   * not.
   *
   * @return true if this element is of type {@link JtonObject}, false
   *         otherwise.
   */
  public boolean isJtonObject() {
    return this instanceof JtonObject;
  }

  /**
   * Provides check for verifying if this element is a {@link JtonPrimitive} or
   * not.
   *
   * @return true if this element is of type {@link JtonPrimitive}, false
   *         otherwise.
   */
  public boolean isJtonPrimitive() {
    return this instanceof JtonPrimitive;
  }

  /**
   * Provides check for verifying if this element represents a {@link JtonNull}
   * value or not.
   *
   * @return true if this element is of type {@link JtonNull}, false otherwise.
   */
  public boolean isJtonNull() {
    return this instanceof JtonNull;
  }

  /**
   * Provides check for verifying if this element is a {@link JtonTransient} or
   * not.
   * 
   * @return true if this element is transient, false otherwise.
   */
  public boolean isTransient() {
    return false;
  }

  // -----------------------------------------------------------------------
  // TYPE CASTING
  // -----------------------------------------------------------------------

  /**
   * Convenience method to get this element as a {@link JtonObject}. If the
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
    throw new IllegalStateException("This is not a JtonObject: " + this);
  }

  public JtonObject getAsJtonObject(JtonObject fallback) {
    try {
      return isJtonObject() ? getAsJtonObject() : fallback;
    } catch (ClassCastException e) {
      return fallback;
    }
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
    throw new IllegalStateException("This is not a JtonArray: " + this);
  }

  public JtonArray getAsJtonArray(JtonArray fallback) {
    try {
      return isJtonArray() ? getAsJtonArray() : fallback;
    } catch (ClassCastException e) {
      return fallback;
    }
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
    throw new IllegalStateException("This is not a JtonPrimitive: " + this);
  }

  public JtonPrimitive getAsJtonPrimitive(JtonPrimitive fallback) {
    try {
      return isJtonPrimitive() ? getAsJtonPrimitive() : fallback;
    } catch (ClassCastException e) {
      return fallback;
    }
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
   */
  //@formatter:off
//  public JtonNull getAsJtonNull() {
//    if (isJtonNull()) {
//      return (JtonNull) this;
//    }
//    throw new IllegalStateException("This is not a JtonNull: " + this);
//  }
//
//  public JtonNull getAsJtonNull(JtonNull fallback) {
//    try {
//      return isJtonPrimitive() ? getAsJtonNull() : fallback;
//    } catch (ClassCastException e) {
//      return fallback;
//    }
//  }
  //@formatter:on

  // -----------------------------------------------------------------------
  // PRIMITIVE DATA TYPES
  // -----------------------------------------------------------------------

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
    } catch (Throwable t) {
      return fallback;
    }
  }

  public Boolean getAsBoolean(Boolean fallback) {
    try {
      return isJtonPrimitive() ? getAsBoolean() : fallback;
    } catch (Throwable t) {
      return fallback;
    }
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

  public Number getAsNumber(Number fallback) {
    try {
      return isJtonPrimitive() ? getAsNumber() : fallback;
    } catch (Throwable t) {
      return fallback;
    }
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
    } catch (Throwable t) {
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
    } catch (Throwable t) {
      return fallback;
    }
  }

  public Double getAsDouble(Double fallback) {
    try {
      return isJtonPrimitive() ? getAsDouble() : fallback;
    } catch (Throwable t) {
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
    } catch (Throwable t) {
      return fallback;
    }
  }

  public Float getAsFloat(Float fallback) {
    try {
      return isJtonPrimitive() ? getAsFloat() : fallback;
    } catch (Throwable t) {
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
    } catch (Throwable t) {
      return fallback;
    }
  }

  public Long getAsLong(Long fallback) {
    try {
      return isJtonPrimitive() ? getAsLong() : fallback;
    } catch (Throwable t) {
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

  public int getAsInteger(int fallback) {
    try {
      return isJtonPrimitive() ? getAsInt() : fallback;
    } catch (Throwable t) {
      return fallback;
    }
  }
  
  public Integer getAsInteger(Integer fallback) {
    try {
      return isJtonPrimitive() ? getAsInt() : fallback;
    } catch (Throwable t) {
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
   */
  public byte getAsByte() {
    throw new UnsupportedOperationException(getClass().getSimpleName());
  }

  public byte getAsByte(byte fallback) {
    try {
      return isJtonPrimitive() ? getAsByte() : fallback;
    } catch (Throwable t) {
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
   */
  public char getAsChar() {
    throw new UnsupportedOperationException(getClass().getSimpleName());
  }

  public char getAsChar(char fallback) {
    try {
      return isJtonPrimitive() ? getAsChar() : fallback;
    } catch (Throwable t) {
      return fallback;
    }
  }

  public Character getAsCharacter(Character fallback) {
    try {
      return isJtonPrimitive() ? getAsChar() : fallback;
    } catch (Throwable t) {
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
   */
  public BigDecimal getAsBigDecimal() {
    throw new UnsupportedOperationException(getClass().getSimpleName());
  }

  public BigDecimal getAsBigDecimal(BigDecimal fallback) {
    try {
      return isJtonPrimitive() ? getAsBigDecimal() : fallback;
    } catch (Throwable t) {
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
   */
  public BigInteger getAsBigInteger() {
    throw new UnsupportedOperationException(getClass().getSimpleName());
  }

  public BigInteger getAsBigInteger(BigInteger fallback) {
    try {
      return isJtonPrimitive() ? getAsBigInteger() : fallback;
    } catch (Throwable t) {
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
    } catch (Throwable t) {
      return fallback;
    }
  }

  public Short getAsShort(Short fallback) {
    try {
      return isJtonPrimitive() ? getAsShort() : fallback;
    } catch (Throwable t) {
      return fallback;
    }
  }

  public Date getAsDate() {
    throw new UnsupportedOperationException(getClass().getSimpleName());
  }

  public Date getAsDate(Date fallback) {
    try {
      return isJtonPrimitive() ? getAsDate() : fallback;
    } catch (Throwable t) {
      return fallback;
    }
  }

  public java.sql.Date getAsSqlDate() {
    throw new UnsupportedOperationException(getClass().getSimpleName());
  }

  public java.sql.Date getAsSqlDate(java.sql.Date fallback) {
    try {
      return isJtonPrimitive() ? getAsSqlDate() : fallback;
    } catch (Throwable t) {
      return fallback;
    }
  }

  public java.sql.Time getAsSqlTime() {
    throw new UnsupportedOperationException(getClass().getSimpleName());
  }

  public java.sql.Time getAsSqlTime(java.sql.Time fallback) {
    try {
      return isJtonPrimitive() ? getAsSqlTime() : fallback;
    } catch (Throwable t) {
      return fallback;
    }
  }

  public java.sql.Timestamp getAsSqlTimestamp() {
    throw new UnsupportedOperationException(getClass().getSimpleName());
  }

  public java.sql.Timestamp getAsSqlTimestamp(java.sql.Timestamp fallback) {
    try {
      return isJtonPrimitive() ? getAsSqlTimestamp() : fallback;
    } catch (Throwable t) {
      return fallback;
    }
  }

  // -----------------------------------------------------------------------
  // PRIMITIVE VALUE
  // -----------------------------------------------------------------------

  public Object getPrimitiveValue() {
    throw new UnsupportedOperationException(getClass().getSimpleName());
  }

  public Object getPrimitiveValue(Object fallback) {
    try {
      return isJtonPrimitive() ? getPrimitiveValue() : fallback;
    } catch (Throwable t) {
      return fallback;
    }
  }

  // -----------------------------------------------------------------------
  // SERIALIZATION
  // -----------------------------------------------------------------------

  /**
   * Returns a String representation of this element.
   */
  @Override
  public String toString() {
    try {
      return JsonSerializer.toString(this, true);
    } catch (JtonIOException e) {
      throw new AssertionError(e);
    } catch (SerializationException e) {
      throw new AssertionError(e);
    }
  }

  /**
   * Returns a String representation of this element.
   */
  public String toString(int intentFactor) {
    try {
      return JsonSerializer.toString(this, true, intentFactor);
    } catch (JtonIOException e) {
      throw new AssertionError(e);
    } catch (SerializationException e) {
      throw new AssertionError(e);
    }
  }
}
