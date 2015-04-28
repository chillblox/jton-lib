package com.arkasoft.jton.serialization.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Locale;

public class BeanUtils {
	
  private static final String ENUM_VALUE_OF_METHOD_NAME = "valueOf";
	
  private static final String ILLEGAL_ACCESS_EXCEPTION_MESSAGE_FORMAT =
      "Unable to access property \"%s\" for type %s.";
  private static final String ENUM_COERCION_EXECPTION_MESSAGE =
      "Unable to coerce %s (\"%s\") to %s.\nValid enum constants - %s";
  
  /**
   * Coerces a value to a given type.
   *
   * @param value
   * @param type
   *
   * @return
   * The coerced value.
   */
  @SuppressWarnings("unchecked")
  public static <T> T coerce(Object value, Class<? extends T> type) {
      if (type == null) {
          throw new IllegalArgumentException();
      }

      Object coercedValue;

      if (value == null) {
          // Null values can only be coerced to null
          coercedValue = null;
      } else {
          if (type.isAssignableFrom(value.getClass())) {
              // Value doesn't need coercion
              coercedValue = value;
          } else if (type.isEnum()) {
              // Find and invoke the valueOf(String) method using an upper
              // case conversion of the supplied Object's toString() value
              try {
                  String valueString = value.toString().toUpperCase(Locale.ENGLISH);
                  Method valueOfMethod = type.getMethod(ENUM_VALUE_OF_METHOD_NAME, String.class);
                  coercedValue = valueOfMethod.invoke(null, valueString);
              }
              // Nothing to be gained by handling the getMethod() & invoke()
              // Exceptions separately
              catch (IllegalAccessException e) {
                  throw new IllegalArgumentException(String.format(
                      ENUM_COERCION_EXECPTION_MESSAGE, value.getClass().getName(), value, type,
                      Arrays.toString(type.getEnumConstants())), e);
              } catch (InvocationTargetException e) {
                  throw new IllegalArgumentException(String.format(
                      ENUM_COERCION_EXECPTION_MESSAGE, value.getClass().getName(), value, type,
                      Arrays.toString(type.getEnumConstants())), e);
              } catch (SecurityException e) {
                  throw new IllegalArgumentException(String.format(
                      ENUM_COERCION_EXECPTION_MESSAGE, value.getClass().getName(), value, type,
                      Arrays.toString(type.getEnumConstants())), e);
              } catch (NoSuchMethodException e) {
                  throw new IllegalArgumentException(String.format(
                      ENUM_COERCION_EXECPTION_MESSAGE, value.getClass().getName(), value, type,
                      Arrays.toString(type.getEnumConstants())), e);
              }
          } else {
              // Coerce the value to the requested type
              if (type == String.class) {
                  coercedValue = value.toString();
              } else if (type == Boolean.class
                  || type == Boolean.TYPE) {
                  coercedValue = Boolean.parseBoolean(value.toString());
              } else if (type == Character.class
                  || type == Character.TYPE) {
                  coercedValue = value.toString().charAt(0);
              } else if (type == Byte.class
                  || type == Byte.TYPE) {
                  if (value instanceof Number) {
                      coercedValue = ((Number)value).byteValue();
                  } else {
                      coercedValue = Byte.parseByte(value.toString());
                  }
              } else if (type == Short.class
                  || type == Short.TYPE) {
                  if (value instanceof Number) {
                      coercedValue = ((Number)value).shortValue();
                  } else {
                      coercedValue = Short.parseShort(value.toString());
                  }
              } else if (type == Integer.class
                  || type == Integer.TYPE) {
                  if (value instanceof Number) {
                      coercedValue = ((Number)value).intValue();
                  } else {
                      coercedValue = Integer.parseInt(value.toString());
                  }
              } else if (type == Long.class
                  || type == Long.TYPE) {
                  if (value instanceof Number) {
                      coercedValue = ((Number)value).longValue();
                  } else {
                      coercedValue = Long.parseLong(value.toString());
                  }
              } else if (type == Float.class
                  || type == Float.TYPE) {
                  if (value instanceof Number) {
                      coercedValue = ((Number)value).floatValue();
                  } else {
                      coercedValue = Float.parseFloat(value.toString());
                  }
              } else if (type == Double.class
                  || type == Double.TYPE) {
                  if (value instanceof Number) {
                      coercedValue = ((Number)value).doubleValue();
                  } else {
                      coercedValue = Double.parseDouble(value.toString());
                  }
              } else if (type == BigInteger.class) {
                  if (value instanceof Number) {
                      coercedValue = new BigInteger(((Number)value).toString());
                  } else {
                      coercedValue = new BigInteger(value.toString());
                  }
              } else if (type == BigDecimal.class) {
                  if (value instanceof Number) {
                      coercedValue = new BigDecimal(((Number)value).toString());
                  } else {
                      coercedValue = new BigDecimal(value.toString());
                  }
              } else {
                  throw new IllegalArgumentException("Unable to coerce " + value.getClass().getName()
                      + " to " + type + ".");
              }
          }
      }

      return (T)coercedValue;
  }
}
