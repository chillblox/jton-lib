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

import com.arkasoft.jton.internal.LinkedTreeMap;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * A class representing an object type in Json. An object consists of name-value
 * pairs where names are strings, and values are any other type of
 * {@link JtonElement}. This allows for a creating a tree of JsonElements. The
 * member elements of this object are maintained in order they were added.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public final class JtonObject extends JtonElement implements
		Map<String, JtonElement> {
	private final LinkedTreeMap<String, JtonElement> members = new LinkedTreeMap<String, JtonElement>();

	@Override
	public JtonObject deepCopy() {
		JtonObject result = new JtonObject();
		for (Map.Entry<String, JtonElement> entry : members.entrySet()) {
			result.add(entry.getKey(), entry.getValue().deepCopy());
		}
		return result;
	}

	/**
	 * Adds a member, which is a name-value pair, to self. The name must be a
	 * String, but the value can be an arbitrary JsonElement, thereby allowing
	 * you to build a full tree of JsonElements rooted at this node.
	 *
	 * @param property
	 *            name of the member.
	 * @param value
	 *            the member object.
	 */
	public void add(String property, JtonElement value) {
		if (value == null) {
			value = JtonNull.INSTANCE;
		}
		members.put(property, value);
	}

	/**
	 * Removes the {@code property} from this {@link JtonObject}.
	 *
	 * @param property
	 *            name of the member that should be removed.
	 * @return the {@link JtonElement} object that is being removed.
	 * @since 1.3
	 */
	public JtonElement remove(String property) {
		return members.remove(property);
	}

	/**
	 * Convenience method to add a primitive member. The specified value is
	 * converted to a JsonPrimitive of String.
	 *
	 * @param property
	 *            name of the member.
	 * @param value
	 *            the string value associated with the member.
	 */
	public void add(String property, String value) {
		add(property, createJsonElement(value));
	}

	/**
	 * Convenience method to add a primitive member. The specified value is
	 * converted to a JsonPrimitive of Number.
	 *
	 * @param property
	 *            name of the member.
	 * @param value
	 *            the number value associated with the member.
	 */
	public void add(String property, Number value) {
		add(property, createJsonElement(value));
	}

	/**
	 * Convenience method to add a boolean member. The specified value is
	 * converted to a JsonPrimitive of Boolean.
	 *
	 * @param property
	 *            name of the member.
	 * @param value
	 *            the number value associated with the member.
	 */
	public void add(String property, Boolean value) {
		add(property, createJsonElement(value));
	}

	/**
	 * Convenience method to add a char member. The specified value is converted
	 * to a JsonPrimitive of Character.
	 *
	 * @param property
	 *            name of the member.
	 * @param value
	 *            the number value associated with the member.
	 */
	public void add(String property, Character value) {
		add(property, createJsonElement(value));
	}

	/**
	 * Convenience method to add a {@link Date} member. The specified value is
	 * converted to a JtonPrimitive of Date.
	 * 
	 * @param property
	 *            name of the member.
	 * @param value
	 *            the date value associated with the member.
	 */
	public void add(String property, Date value) {
		add(property, createJsonElement(value));
	}

	/**
	 * Convenience method to add a member. The specified value is converted to a
	 * JtonPrimitive at runtime.
	 * 
	 * @param property
	 *            name of the member.
	 * @param value
	 *            the value associated with the member.
	 */
	public void add(String property, Object value) {
		if (value == null) {
			value = JtonNull.INSTANCE;
			members.put(property, (JtonElement) value);
		} else if (value instanceof JtonElement) {
			members.put(property, (JtonElement) value);
		} else {
			add(property, createJsonElement(value));
		}
	}

	/**
	 * Convenience method to add a member. The specified value is converted to a
	 * JtonPrimitive id {@code jtonTransient} is {@code false} otherwise not.
	 * 
	 * @param property
	 *            name of the member.
	 * @param value
	 *            the value associated with the member.
	 * @param jtonTransient
	 *            if {@code false} the is converted to a JtonPrimitive;
	 *            otherwise the value will be added as it is.
	 */
	public void add(String property, Object value, boolean jtonTransient) {
		add(property, createJsonElement(value, jtonTransient));
	}

	/**
	 * Creates the proper {@link JtonElement} object from the given
	 * {@code value} object.
	 *
	 * @param value
	 *            the object to generate the {@link JtonElement} for
	 * @return a {@link JtonPrimitive} if the {@code value} is not null,
	 *         otherwise a {@link JtonNull}
	 */
	private JtonElement createJsonElement(Object value) {
		return createJsonElement(value, false);
	}

	private JtonElement createJsonElement(Object value, boolean jtonTransient) {
		return value == null && !jtonTransient ? JtonNull.INSTANCE
				: new JtonPrimitive(value, jtonTransient);
	}

	/**
	 * Returns a set of members of this object. The set is ordered, and the
	 * order is in which the elements were added.
	 *
	 * @return a set of members of this object.
	 */
	@Override
	public Set<Map.Entry<String, JtonElement>> entrySet() {
		return members.entrySet();
	}

	/**
	 * Convenience method to check if a member with the specified name is
	 * present in this object.
	 *
	 * @param memberName
	 *            name of the member that is being checked for presence.
	 * @return true if there is a member with the specified name, false
	 *         otherwise.
	 */
	public boolean has(String memberName) {
		return members.containsKey(memberName);
	}

	/**
	 * Returns the member with the specified name.
	 *
	 * @param memberName
	 *            name of the member that is being requested.
	 * @return the member matching the name. Null if no such member exists.
	 */
	public JtonElement get(String memberName) {
		if (members.containsKey(memberName))
			return members.get(memberName);
		else
			return JtonNull.INSTANCE;
	}

	/**
	 * Convenience method to get the specified member as a JsonPrimitive
	 * element.
	 *
	 * @param memberName
	 *            name of the member being requested.
	 * @return the JsonPrimitive corresponding to the specified member.
	 */
	public JtonPrimitive getAsJtonPrimitive(String memberName) {
		return (JtonPrimitive) members.get(memberName);
	}

	/**
	 * Convenience method to get the specified member as a JsonArray.
	 *
	 * @param memberName
	 *            name of the member being requested.
	 * @return the JsonArray corresponding to the specified member.
	 */
	public JtonArray getAsJtonArray(String memberName) {
		return (JtonArray) members.get(memberName);
	}

	/**
	 * Convenience method to get the specified member as a JsonObject.
	 *
	 * @param memberName
	 *            name of the member being requested.
	 * @return the JsonObject corresponding to the specified member.
	 */
	public JtonObject getAsJtonObject(String memberName) {
		return (JtonObject) members.get(memberName);
	}

	@Override
	public boolean equals(Object o) {
		return (o == this)
				|| (o instanceof JtonObject && ((JtonObject) o).members
						.equals(members));
	}

	@Override
	public int hashCode() {
		return members.hashCode();
	}

	@Override
	public int size() {
		return members.size();
	}

	@Override
	public boolean isEmpty() {
		return members.isEmpty();
	}

	@Override
	@Deprecated
	public boolean containsKey(Object key) {
		return members.containsKey(key);
	}

	@Override
	@Deprecated
	public boolean containsValue(Object value) {
		return members.containsValue(value);
	}

	@Override
	@Deprecated
	public JtonElement get(Object key) {
		return members.get(key);
	}

	@Override
	@Deprecated
	public JtonElement put(String key, JtonElement value) {
		add(key, value);
		return value;
	}

	@Override
	@Deprecated
	public JtonElement remove(Object key) {
		return remove((String) key);
	}

	@Override
	public void putAll(Map<? extends String, ? extends JtonElement> m) {
		for (Map.Entry<? extends String, ? extends JtonElement> entry : m
				.entrySet()) {
			add(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public void clear() {
		members.clear();
	}

	@Override
	public Set<String> keySet() {
		return members.keySet();
	}

	@Override
	public Collection<JtonElement> values() {
		return members.values();
	}
}
