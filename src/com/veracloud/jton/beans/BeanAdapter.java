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
package com.veracloud.jton.beans;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Exposes Java bean properties of an object via the {@link Map} interface. A
 * call to {@link Map#get(Object)} invokes the getter for the corresponding
 * property, and a call to {@link Map#put(Object, Object)} invokes the
 * property's setter.
 * <p>
 * Properties may provide multiple setters; the appropriate setter to invoke is
 * determined by the type of the value being set. If the value is {@code null},
 * the return type of the getter method is used.
 * <p>
 * Getter methods must be named "getProperty" where "property" is the property
 * name. If there is no "get" method, then an "isProperty" method can also be
 * used. Setter methods (if present) must be named "setProperty".
 * <p>
 * Getter and setter methods are checked before straight fields named "property"
 * in order to support proper data encapsulation. And only <code>public</code>
 * and non-<code>static</code> methods and fields can be accessed.
 */
public class BeanAdapter implements Map<String, Object> {
	private Object bean;
	private boolean ignoreReadOnlyProperties;

	public static final String GET_PREFIX = "get";
	public static final String IS_PREFIX = "is";
	public static final String SET_PREFIX = "set";

	private static final String ENUM_VALUE_OF_METHOD_NAME = "valueOf";

	private static final String ILLEGAL_ACCESS_EXCEPTION_MESSAGE_FORMAT =
			"Unable to access property \"%s\" for type %s.";
	private static final String ENUM_COERCION_EXECPTION_MESSAGE =
			"Unable to coerce %s (\"%s\") to %s.\nValid enum constants - %s";

	/**
	 * Creates a new bean dictionary.
	 *
	 * @param bean
	 *          The bean object to wrap.
	 */
	public BeanAdapter(Object bean) {
		this(bean, false);
	}

	/**
	 * Creates a new bean dictionary which can ignore readonly fields (that is,
	 * straight fields marked as <code>final</code> or bean properties where there
	 * is a "get" method but no corresponding "set" method).
	 *
	 * @param bean
	 *          The bean object to wrap.
	 */
	public BeanAdapter(Object bean, boolean ignoreReadOnlyProperties) {
		if (bean == null) {
			throw new IllegalArgumentException();
		}

		this.bean = bean;
		this.ignoreReadOnlyProperties = ignoreReadOnlyProperties;
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsKey(Object _key) {
		final String key = (String) _key;

		if (key == null) {
			throw new IllegalArgumentException("key is null.");
		}

		if (key.length() == 0) {
			throw new IllegalArgumentException("key is empty.");
		}

		boolean containsKey = (getGetterMethod(key) != null);

		if (!containsKey) {
			containsKey = (getField(key) != null);
		}

		return containsKey;
	}

	@Override
	public boolean containsValue(Object value) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object get(Object _key) {
		final String key = (String) _key;

		if (key == null) {
			throw new IllegalArgumentException("key is null.");
		}

		if (key.length() == 0) {
			throw new IllegalArgumentException("key is empty.");
		}

		Object value = null;

		Method getterMethod = getGetterMethod(key);

		if (getterMethod == null) {
			Field field = getField(key);

			if (field != null) {
				try {
					value = field.get(bean);
				} catch (IllegalAccessException exception) {
					throw new RuntimeException(String.format(ILLEGAL_ACCESS_EXCEPTION_MESSAGE_FORMAT,
							key, bean.getClass().getName()), exception);
				}
			}
		} else {
			try {
				value = getterMethod.invoke(bean, new Object[] {});
			} catch (IllegalAccessException exception) {
				throw new RuntimeException(String.format(ILLEGAL_ACCESS_EXCEPTION_MESSAGE_FORMAT,
						key, bean.getClass().getName()), exception);
			} catch (InvocationTargetException exception) {
				throw new RuntimeException(String.format("Error getting property \"%s\" for type %s.",
						key, bean.getClass().getName()), exception.getCause());
			}
		}

		return value;
	}

	@Override
	public Object put(String key, Object value) {
		if (key == null) {
			throw new IllegalArgumentException("key is null.");
		}

		if (key.length() == 0) {
			throw new IllegalArgumentException("key is empty.");
		}

		Method setterMethod = null;
		Object valueUpdated = value;

		if (valueUpdated != null) {
			// Get the setter method for the value type
			setterMethod = getSetterMethod(key, valueUpdated.getClass());
		}

		if (setterMethod == null) {
			// Get the property type and attempt to coerce the value to it
			Class<?> propertyType = getType(key);

			if (propertyType != null) {
				setterMethod = getSetterMethod(key, propertyType);
				valueUpdated = coerce(valueUpdated, propertyType);
			}
		}

		if (setterMethod == null) {
			Field field = getField(key);

			if (field == null) {
				throw new PropertyNotFoundException("Property \"" + key + "\""
						+ " does not exist or is read-only.");
			}

			Class<?> fieldType = field.getType();
			if (valueUpdated != null) {
				Class<?> valueType = valueUpdated.getClass();
				if (!fieldType.isAssignableFrom(valueType)) {
					valueUpdated = coerce(valueUpdated, fieldType);
				}
			}

			try {
				field.set(bean, valueUpdated);
			} catch (IllegalAccessException exception) {
				throw new RuntimeException(String.format(ILLEGAL_ACCESS_EXCEPTION_MESSAGE_FORMAT,
						key, bean.getClass().getName()), exception);
			}
		} else {
			try {
				setterMethod.invoke(bean, new Object[] { valueUpdated });
			} catch (IllegalAccessException exception) {
				throw new RuntimeException(String.format(ILLEGAL_ACCESS_EXCEPTION_MESSAGE_FORMAT,
						key, bean.getClass().getName()), exception);
			} catch (InvocationTargetException exception) {
				throw new RuntimeException(String.format("Error setting property \"%s\" for type %s to value \"%s\"",
						key, bean.getClass().getName(), "" + valueUpdated), exception.getCause());
			}

		}

		Object previousValue = null;

		return previousValue;
	}

	@Override
	public Object remove(Object key) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void putAll(Map<? extends String, ? extends Object> m) {
		for (Map.Entry<? extends String, ? extends Object> entry : m.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}

	public void putAll(Map<? extends String, ? extends Object> m, boolean ignoreErrors) {
		for (Map.Entry<? extends String, ? extends Object> entry : m.entrySet()) {
			try {
				put(entry.getKey(), entry.getValue());
			} catch (PropertyNotFoundException ex) {
				if (!ignoreErrors) {
					throw ex;
				}

				// TODO log error
			}
		}
	}

	/**
	 * Tests the read-only state of a property.
	 *
	 * @param key
	 *          The property name.
	 *
	 * @return <tt>true</tt> if the property is read-only; <tt>false</tt>,
	 *         otherwise.
	 */
	public boolean isReadOnly(String key) {
		return isReadOnly(bean.getClass(), key);
	}

	/**
	 * Returns the type of a property.
	 *
	 * @param key
	 *          The property name.
	 *
	 * @see #getType(Class, String)
	 */
	public Class<?> getType(String key) {
		return getType(bean.getClass(), key);
	}

	/**
	 * Returns the generic type of a property.
	 *
	 * @param key
	 *          The property name.
	 *
	 * @see #getGenericType(Class, String)
	 */
	public Type getGenericType(String key) {
		return getGenericType(bean.getClass(), key);
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<String> keySet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Object> values() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<java.util.Map.Entry<String, Object>> entrySet() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Returns the getter method for a property.
	 *
	 * @param key
	 *          The property name.
	 *
	 * @return The getter method, or <tt>null</tt> if the method does not exist.
	 */
	private Method getGetterMethod(String key) {
		return getGetterMethod(bean.getClass(), key);
	}

	/**
	 * Returns the setter method for a property.
	 *
	 * @param key
	 *          The property name.
	 *
	 * @return The getter method, or <tt>null</tt> if the method does not exist.
	 */
	private Method getSetterMethod(String key, Class<?> valueType) {
		return getSetterMethod(bean.getClass(), key, valueType);
	}

	/**
	 * Returns the public, non-static field for a property. Note that fields will
	 * only be consulted for bean properties after bean methods.
	 *
	 * @param fieldName
	 *          The property name
	 *
	 * @return The field, or <tt>null</tt> if the field does not exist, or is
	 *         non-public or static
	 */
	private Field getField(String fieldName) {
		if (fieldName == null) {
			throw new IllegalArgumentException("fieldName is null.");
		}

		return getField(bean.getClass(), fieldName);
	}

	/**
	 * Tests the read-only state of a property. Note that if no such property
	 * exists, this method will return <tt>true</tt> (it will <u>not</u> throw an
	 * exception).
	 *
	 * @param beanClass
	 *          The bean class.
	 *
	 * @param key
	 *          The property name.
	 *
	 * @return <tt>true</tt> if the property is read-only; <tt>false</tt>,
	 *         otherwise.
	 */
	public static boolean isReadOnly(Class<?> beanClass, String key) {
		if (beanClass == null) {
			throw new IllegalArgumentException("beanClass is null.");
		}

		if (key == null) {
			throw new IllegalArgumentException("key is null.");
		}

		if (key.length() == 0) {
			throw new IllegalArgumentException("key is empty.");
		}

		boolean isReadOnly = true;

		Method getterMethod = getGetterMethod(beanClass, key);
		if (getterMethod == null) {
			Field field = getField(beanClass, key);
			if (field != null) {
				isReadOnly = ((field.getModifiers() & Modifier.FINAL) != 0);
			}
		} else {
			Method setterMethod = getSetterMethod(beanClass, key, getType(beanClass, key));
			isReadOnly = (setterMethod == null);
		}

		return isReadOnly;
	}

	/**
	 * Returns the type of a property.
	 *
	 * @param beanClass
	 *          The bean class.
	 *
	 * @param key
	 *          The property name.
	 *
	 * @return The type of the property, or <tt>null</tt> if no such bean property
	 *         exists.
	 */
	public static Class<?> getType(Class<?> beanClass, String key) {
		if (beanClass == null) {
			throw new IllegalArgumentException("beanClass is null.");
		}

		if (key == null) {
			throw new IllegalArgumentException("key is null.");
		}

		if (key.length() == 0) {
			throw new IllegalArgumentException("key is empty.");
		}

		Class<?> type = null;

		Method getterMethod = getGetterMethod(beanClass, key);

		if (getterMethod == null) {
			Field field = getField(beanClass, key);

			if (field != null) {
				type = field.getType();
			}
		} else {
			type = getterMethod.getReturnType();
		}

		return type;
	}

	/**
	 * Returns the generic type of a property.
	 *
	 * @param beanClass
	 *          The bean class.
	 *
	 * @param key
	 *          The property name.
	 *
	 * @return The generic type of the property, or <tt>null</tt> if no such bean
	 *         property exists. If the type is a generic, an instance of
	 *         {@link java.lang.reflect.ParameterizedType} will be returned.
	 *         Otherwise, an instance of {@link java.lang.Class} will be returned.
	 */
	public static Type getGenericType(Class<?> beanClass, String key) {
		if (beanClass == null) {
			throw new IllegalArgumentException("beanClass is null.");
		}

		if (key == null) {
			throw new IllegalArgumentException("key is null.");
		}

		if (key.length() == 0) {
			throw new IllegalArgumentException("key is empty.");
		}

		Type genericType = null;

		Method getterMethod = getGetterMethod(beanClass, key);

		if (getterMethod == null) {
			Field field = getField(beanClass, key);

			if (field != null) {
				genericType = field.getGenericType();
			}
		} else {
			genericType = getterMethod.getGenericReturnType();
		}

		return genericType;
	}

	/**
	 * Returns the public, non-static fields for a property. Note that fields will
	 * only be consulted for bean properties after bean methods.
	 *
	 * @param beanClass
	 *          The bean class.
	 *
	 * @param key
	 *          The property name.
	 *
	 * @return The field, or <tt>null</tt> if the field does not exist, or is
	 *         non-public or static.
	 */
	public static Field getField(Class<?> beanClass, String key) {
		if (beanClass == null) {
			throw new IllegalArgumentException("beanClass is null.");
		}

		if (key == null) {
			throw new IllegalArgumentException("key is null.");
		}

		if (key.length() == 0) {
			throw new IllegalArgumentException("key is empty.");
		}

		Field field = null;

		try {
			field = beanClass.getField(key);

			int modifiers = field.getModifiers();

			// Exclude non-public and static fields
			if ((modifiers & Modifier.PUBLIC) == 0 || (modifiers & Modifier.STATIC) > 0) {
				field = null;
			}
		} catch (NoSuchFieldException exception) {
			// No-op
		}

		return field;
	}

	/**
	 * Returns the getter method for a property.
	 *
	 * @param beanClass
	 *          The bean class.
	 *
	 * @param key
	 *          The property name.
	 *
	 * @return The getter method, or <tt>null</tt> if the method does not exist.
	 */
	public static Method getGetterMethod(final Class<?> beanClass, final String key) {
		if (beanClass == null) {
			throw new IllegalArgumentException("beanClass is null.");
		}

		if (key == null) {
			throw new IllegalArgumentException("key is null.");
		}

		if (key.length() == 0) {
			throw new IllegalArgumentException("key is empty.");
		}

		// Upper-case the first letter
		String keyUpdated = Character.toUpperCase(key.charAt(0)) + key.substring(1);
		Method getterMethod = null;

		try {
			getterMethod = beanClass.getMethod(GET_PREFIX + keyUpdated);
		} catch (NoSuchMethodException exception) {
			// No-op
		}

		if (getterMethod == null) {
			try {
				getterMethod = beanClass.getMethod(IS_PREFIX + keyUpdated);
			} catch (NoSuchMethodException exception) {
				// No-op
			}
		}

		return getterMethod;
	}

	/**
	 * Returns the setter method for a property.
	 *
	 * @param beanClass
	 *          The bean class.
	 *
	 * @param key
	 *          The property name.
	 *
	 * @return The getter method, or <tt>null</tt> if the method does not exist.
	 */
	public static Method getSetterMethod(final Class<?> beanClass, final String key, final Class<?> valueType) {
		if (beanClass == null) {
			throw new IllegalArgumentException("beanClass is null.");
		}

		if (key == null) {
			throw new IllegalArgumentException("key is null.");
		}

		if (key.length() == 0) {
			throw new IllegalArgumentException("key is empty.");
		}

		Method setterMethod = null;

		if (valueType != null) {
			// Upper-case the first letter and prepend the "set" prefix to
			// determine the method name
			String keyUpdated = Character.toUpperCase(key.charAt(0)) + key.substring(1);
			final String methodName = SET_PREFIX + keyUpdated;

			try {
				setterMethod = beanClass.getMethod(methodName, valueType);
			} catch (NoSuchMethodException exception) {
				// No-op
			}

			if (setterMethod == null) {
				// Look for a match on the value's super type
				Class<?> superType = valueType.getSuperclass();
				setterMethod = getSetterMethod(beanClass, keyUpdated, superType);
			}

			if (setterMethod == null) {
				// If value type is a primitive wrapper, look for a method
				// signature with the corresponding primitive type
				try {
					Field primitiveTypeField = valueType.getField("TYPE");
					Class<?> primitiveValueType = (Class<?>) primitiveTypeField.get(null);

					try {
						setterMethod = beanClass.getMethod(methodName, primitiveValueType);
					} catch (NoSuchMethodException exception) {
						// No-op
					}
				} catch (NoSuchFieldException exception) {
					// No-op
				} catch (IllegalAccessException exception) {
					throw new RuntimeException(String.format(ILLEGAL_ACCESS_EXCEPTION_MESSAGE_FORMAT,
							keyUpdated, beanClass.getName()), exception);
				}
			}

			if (setterMethod == null) {
				// Walk the interface graph to find a matching method
				Class<?>[] interfaces = valueType.getInterfaces();

				int i = 0, n = interfaces.length;
				while (setterMethod == null
						&& i < n) {
					Class<?> interfaceType = interfaces[i++];
					setterMethod = getSetterMethod(beanClass, keyUpdated, interfaceType);
				}
			}
		}

		return setterMethod;
	}

	/**
	 * Coerces a value to a given type.
	 *
	 * @param value
	 * @param type
	 *
	 * @return The coerced value.
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
						coercedValue = ((Number) value).byteValue();
					} else {
						coercedValue = Byte.parseByte(value.toString());
					}
				} else if (type == Short.class
						|| type == Short.TYPE) {
					if (value instanceof Number) {
						coercedValue = ((Number) value).shortValue();
					} else {
						coercedValue = Short.parseShort(value.toString());
					}
				} else if (type == Integer.class
						|| type == Integer.TYPE) {
					if (value instanceof Number) {
						coercedValue = ((Number) value).intValue();
					} else {
						coercedValue = Integer.parseInt(value.toString());
					}
				} else if (type == Long.class
						|| type == Long.TYPE) {
					if (value instanceof Number) {
						coercedValue = ((Number) value).longValue();
					} else {
						coercedValue = Long.parseLong(value.toString());
					}
				} else if (type == Float.class
						|| type == Float.TYPE) {
					if (value instanceof Number) {
						coercedValue = ((Number) value).floatValue();
					} else {
						coercedValue = Float.parseFloat(value.toString());
					}
				} else if (type == Double.class
						|| type == Double.TYPE) {
					if (value instanceof Number) {
						coercedValue = ((Number) value).doubleValue();
					} else {
						coercedValue = Double.parseDouble(value.toString());
					}
				} else if (type == BigInteger.class) {
					if (value instanceof Number) {
						coercedValue = new BigInteger(((Number) value).toString());
					} else {
						coercedValue = new BigInteger(value.toString());
					}
				} else if (type == BigDecimal.class) {
					if (value instanceof Number) {
						coercedValue = new BigDecimal(((Number) value).toString());
					} else {
						coercedValue = new BigDecimal(value.toString());
					}
				} else {
					throw new IllegalArgumentException("Unable to coerce " + value.getClass().getName()
							+ " to " + type + ".");
				}
			}
		}

		return (T) coercedValue;
	}

}
