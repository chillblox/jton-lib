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

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.veracloud.jton.internal.LinkedTreeMap;

/**
 * A class representing an object type in Json. An object consists of name-value
 * pairs where names are strings, and values are any other type of
 * {@link JtonElement}. This allows for a creating a tree of JsonElements. The
 * member elements of this object are maintained in order they were added.
 */
@XmlRootElement(name = "item")
@XmlAccessorType(XmlAccessType.FIELD)
public class JtonObject extends JtonElement implements Map<String, JtonElement> {
  
  private final Map<String, JtonElement> members;
  
  public JtonObject() {
    members = new LinkedTreeMap<String, JtonElement>();
  }

  public JtonObject(Map<String, Object> map) {
    this();
    
    if (map != null) {
      for (Map.Entry<String, Object> entry : map.entrySet()) {
        add(entry.getKey(), entry.getValue());
      }
    }
  }

  @Override
  public JtonObject deepCopy() {
    JtonObject result = new JtonObject();
    for (Map.Entry<String, JtonElement> entry : members.entrySet()) {
      result.add(entry.getKey(), entry.getValue().deepCopy());
    }
    return result;
  }

  /**
   * Convenience method to add a member. The specified value is converted to a
   * JtonPrimitive at runtime.
   * 
   * @param property
   *          name of the member.
   * @param value
   *          the value associated with the member.
   */
  @Deprecated
  public JtonObject add(String property, Object value) {
    return set(property, value);
  }

  public JtonObject set(String property, Object value) {
    if (value == this) {
      throw new IllegalArgumentException("cyclic reference");
    }

    if (value == null || value == JtonNull.INSTANCE) {
//      value = JtonNull.INSTANCE;
//      members.put(property, (JtonElement) value);
    } else if (value instanceof JtonElement) {
      members.put(property, (JtonElement) value);
    } else {
      members.put(property, createJsonElement(value));
    }

    return this;
  }

  public JtonObject set(String property, Stream<JtonElement> stream) {
    return set(property, new JtonArray(stream));
  }

  /**
   * Convenience method to add a member. The specified value is converted to a
   * JtonPrimitive id {@code jtonTransient} is {@code false} otherwise not.
   * 
   * @param property
   *          name of the member.
   * @param value
   *          the value associated with the member.
   * @param jtonTransient
   *          if {@code false} the is converted to a JtonPrimitive; otherwise
   *          the value will be added as it is.
   * @return
   */
  @Deprecated
  public JtonObject add(String property, Object value, boolean jtonTransient) {
    return set(property, value, jtonTransient);
  }

  public JtonObject set(String property, Object value, boolean jtonTransient) {
    if (value == this) {
      throw new IllegalArgumentException("cyclic reference");
    }

    return add(property, createJsonElement(value, jtonTransient));
  }

  /**
   * Removes a member, which is a name-value pair.
   *
   * @param property
   *          name of the member.
   * @param value
   *          the member object.
   */
  public JtonElement remove(String key) {
    return members.remove(key);
  }

  /**
   * Creates the proper {@link JtonElement} object from the given {@code value}
   * object.
   *
   * @param value
   *          the object to generate the {@link JtonElement} for
   * @return a {@link JtonPrimitive} if the {@code value} is not null, otherwise
   *         a {@link JtonNull}
   */
  static JtonElement createJsonElement(Object value) {
    return createJsonElement(value, false);
  }

  static JtonElement createJsonElement(Object value, boolean jtonTransient) {
    return (value == null && !jtonTransient) ? JtonNull.INSTANCE
        : new JtonPrimitive(value, jtonTransient);
  }

  /**
   * Returns a set of members of this object. The set is ordered, and the order
   * is in which the elements were added.
   *
   * @return a set of members of this object.
   */
  @Override
  public Set<Map.Entry<String, JtonElement>> entrySet() {
    return members.entrySet();
  }

  /**
   * Convenience method to check if a member with the specified name is present
   * in this object.
   *
   * @param memberName
   *          name of the member that is being checked for presence.
   * @return true if there is a member with the specified name, false otherwise.
   */
  @Override
  public boolean has(String memberName) {
    return members.containsKey(memberName);
  }

  /**
   * Returns the member with the specified name.
   *
   * @param memberName
   *          name of the member that is being requested.
   * @return the member matching the name; {@link JtonNull} if no such member
   *         exists.
   */
  @Override
  public JtonElement get(String memberName) {
    if (members.containsKey(memberName))
      return members.get(memberName);
    else
      return JtonNull.INSTANCE;
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
  public final boolean containsKey(Object key) {
    return members.containsKey(key);
  }

  @Override
  @Deprecated
  public final boolean containsValue(Object value) {
    return members.containsValue(value);
  }

  @Override
  @Deprecated
  public final JtonElement get(Object key) {
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
  public final JtonElement remove(Object key) {
    return members.remove(key);
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
