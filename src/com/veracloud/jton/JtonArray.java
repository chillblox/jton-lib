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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.stream.Stream;

/**
 * A class representing an array type in Json. An array is a list of
 * {@link JtonElement}s each of which can be of a different type. This is an
 * ordered list, meaning that the order in which elements are added is
 * preserved.
 */
public final class JtonArray extends JtonElement implements List<JtonElement> {

  public static JtonArray create(List<Map<String, Object>> list) {
    JtonArray result = new JtonArray();
    for (Map<String, Object> item : list) {
      result.add(new JtonObject(item));
    }
    return result;
  }

  // ---

  private final List<JtonElement> elements;

  /**
   * Creates an empty JsonArray.
   */
  public JtonArray() {
    elements = new ArrayList<JtonElement>();
  }
  
  public JtonArray(List<JtonElement> elements) {
    this.elements = elements;
  }
  
  public JtonArray(Stream<JtonElement> stream) {
    this(Arrays.asList(stream.toArray(JtonElement[]::new)));
  }

  @Override
  public JtonArray deepCopy() {
    JtonArray result = new JtonArray();
    for (JtonElement element : elements) {
      result.add(element.deepCopy());
    }
    return result;
  }

  public boolean addObject(Object value) {
    if (value == this) {
      throw new IllegalArgumentException("cyclic reference");
    }

    if (value == null) {
      value = JtonNull.INSTANCE;
      return add((JtonElement) value);
    } else if (value instanceof JtonElement) {
      return add((JtonElement) value);
    } else {
      return add(JtonObject.createJsonElement(value));
    }
  }

  public boolean add(Boolean bool) {
    return add(new JtonPrimitive(bool));
  }
  
  public void add(int index, Boolean bool) {
    add(index, new JtonPrimitive(bool));
  }

  public JtonElement set(int index, Boolean bool) {
    return set(index, new JtonPrimitive(bool));
  }

  public boolean add(Number number) {
    return add(new JtonPrimitive(number));
  }
  
  public void add(int index, Number number) {
    add(index, new JtonPrimitive(number));
  }

  public JtonElement set(int index, Number number) {
    return set(index, new JtonPrimitive(number));
  }

  public boolean add(String string) {
    return add(new JtonPrimitive(string));
  }
  
  public void add(int index, String string) {
    add(index, new JtonPrimitive(string));
  }

  public JtonElement set(int index, String string) {
    return set(index, new JtonPrimitive(string));
  }

  public boolean add(Character c) {
    return add(new JtonPrimitive(c));
  }
  
  public void add(int index, Character c) {
    add(index, new JtonPrimitive(c));
  }

  public JtonElement set(int index, Character c) {
    return set(index, new JtonPrimitive(c));
  }

  public boolean add(Date date) {
    return add(new JtonPrimitive(date));
  }
  
  public void add(int index, Date date) {
    add(index, new JtonPrimitive(date));
  }

  public JtonElement set(int index, Date date) {
    return set(index, new JtonPrimitive(date));
  }

  /**
   * Adds the specified element to self.
   *
   * @param element
   *          the element that needs to be added to the array.
   * @return
   */
  @Override
  public boolean add(JtonElement element) {
    if (element == null) {
      element = JtonNull.INSTANCE;
    }
    return elements.add(element);
  }

  /**
   * Adds all the elements of the specified array to self.
   *
   * @param array
   *          the array whose elements need to be added to the array.
   */
  public void addAll(JtonArray array) {
    elements.addAll(array.elements);
  }

  /**
   * Replaces the element at the specified position in this array with the
   * specified element. Element can be {@code null}.
   * 
   * @param index
   *          index of the element to replace
   * @param element
   *          element to be stored at the specified position
   * @return the element previously at the specified position
   */
  @Override
  public JtonElement set(int index, JtonElement element) {
    if (index >= elements.size()) {
      for (int i = elements.size(); i <= index; i++) {
        elements.add(JtonNull.INSTANCE);
      }
    }
    return elements.set(index, element);
  }

  /**
   * Removes the first occurrence of the specified element from this array, if
   * it is present. If the array does not contain the element, it is unchanged.
   * 
   * @param element
   *          element to be removed from this array, if present
   * @return true if this array contained the specified element, false otherwise
   */
  public boolean remove(JtonElement element) {
    return elements.remove(element);
  }

  /**
   * Removes the element at the specified position in this array. Shifts any
   * subsequent elements to the left (subtracts one from their indices). Returns
   * the element that was removed from the array.
   * 
   * @param index
   *          index the index of the element to be removed
   * @return the element previously at the specified position
   * @throws IndexOutOfBoundsException
   *           if the specified index is outside the array bounds
   */
  @Override
  public JtonElement remove(int index) {
    return elements.remove(index);
  }

  /**
   * Returns true if this array contains the specified element.
   * 
   * @return true if this array contains the specified element.
   * @param element
   *          whose presence in this array is to be tested
   */
  public boolean contains(JtonElement element) {
    return elements.contains(element);
  }

  /**
   * Returns the number of elements in the array.
   *
   * @return the number of elements in the array.
   */
  @Override
  public int size() {
    return elements.size();
  }

  /**
   * Returns an iterator to navigate the elemetns of the array. Since the array
   * is an ordered list, the iterator navigates the elements in the order they
   * were inserted.
   *
   * @return an iterator to navigate the elements of the array.
   */
  @Override
  public Iterator<JtonElement> iterator() {
    return elements.iterator();
  }

  /**
   * Returns the ith element of the array.
   *
   * @param i
   *          the index of the element that is being sought.
   * @return the element present at the ith index.
   */
  @Override
  public JtonElement get(int i) {
    if (i < elements.size()) {
      return elements.get(i);
    } else {
      return JtonNull.INSTANCE;
    }
  }

  /**
   * convenience method to get this array as a {@link Number} if it contains a
   * single element.
   *
   * @return get this element as a number if it is single element array.
   * @throws ClassCastException
   *           if the element in the array is of not a {@link JtonPrimitive} and
   *           is not a valid Number.
   * @throws IllegalStateException
   *           if the array has more than one element.
   */
  // @Override
  // public Number getAsNumber() {
  // if (elements.size() == 1) {
  // return elements.get(0).getAsNumber();
  // }
  // throw new IllegalStateException();
  // }

  /**
   * convenience method to get this array as a {@link String} if it contains a
   * single element.
   *
   * @return get this element as a String if it is single element array.
   * @throws ClassCastException
   *           if the element in the array is of not a {@link JtonPrimitive} and
   *           is not a valid String.
   * @throws IllegalStateException
   *           if the array has more than one element.
   */
  // @Override
  // public String getAsString() {
  // if (elements.size() == 1) {
  // return elements.get(0).getAsString();
  // }
  // throw new IllegalStateException();
  // }

  /**
   * convenience method to get this array as a double if it contains a single
   * element.
   *
   * @return get this element as a double if it is single element array.
   * @throws ClassCastException
   *           if the element in the array is of not a {@link JtonPrimitive} and
   *           is not a valid double.
   * @throws IllegalStateException
   *           if the array has more than one element.
   */
  // @Override
  // public double getAsDouble() {
  // if (elements.size() == 1) {
  // return elements.get(0).getAsDouble();
  // }
  // throw new IllegalStateException();
  // }

  /**
   * convenience method to get this array as a {@link BigDecimal} if it contains
   * a single element.
   *
   * @return get this element as a {@link BigDecimal} if it is single element
   *         array.
   * @throws ClassCastException
   *           if the element in the array is of not a {@link JtonPrimitive}.
   * @throws NumberFormatException
   *           if the element at index 0 is not a valid {@link BigDecimal}.
   * @throws IllegalStateException
   *           if the array has more than one element.
   * @since 1.2
   */
  // @Override
  // public BigDecimal getAsBigDecimal() {
  // if (elements.size() == 1) {
  // return elements.get(0).getAsBigDecimal();
  // }
  // throw new IllegalStateException();
  // }

  /**
   * convenience method to get this array as a {@link BigInteger} if it contains
   * a single element.
   *
   * @return get this element as a {@link BigInteger} if it is single element
   *         array.
   * @throws ClassCastException
   *           if the element in the array is of not a {@link JtonPrimitive}.
   * @throws NumberFormatException
   *           if the element at index 0 is not a valid {@link BigInteger}.
   * @throws IllegalStateException
   *           if the array has more than one element.
   * @since 1.2
   */
  // @Override
  // public BigInteger getAsBigInteger() {
  // if (elements.size() == 1) {
  // return elements.get(0).getAsBigInteger();
  // }
  // throw new IllegalStateException();
  // }

  /**
   * convenience method to get this array as a float if it contains a single
   * element.
   *
   * @return get this element as a float if it is single element array.
   * @throws ClassCastException
   *           if the element in the array is of not a {@link JtonPrimitive} and
   *           is not a valid float.
   * @throws IllegalStateException
   *           if the array has more than one element.
   */
  // @Override
  // public float getAsFloat() {
  // if (elements.size() == 1) {
  // return elements.get(0).getAsFloat();
  // }
  // throw new IllegalStateException();
  // }

  /**
   * convenience method to get this array as a long if it contains a single
   * element.
   *
   * @return get this element as a long if it is single element array.
   * @throws ClassCastException
   *           if the element in the array is of not a {@link JtonPrimitive} and
   *           is not a valid long.
   * @throws IllegalStateException
   *           if the array has more than one element.
   */
  // @Override
  // public long getAsLong() {
  // if (elements.size() == 1) {
  // return elements.get(0).getAsLong();
  // }
  // throw new IllegalStateException();
  // }

  /**
   * convenience method to get this array as an integer if it contains a single
   * element.
   *
   * @return get this element as an integer if it is single element array.
   * @throws ClassCastException
   *           if the element in the array is of not a {@link JtonPrimitive} and
   *           is not a valid integer.
   * @throws IllegalStateException
   *           if the array has more than one element.
   */
  // @Override
  // public int getAsInt() {
  // if (elements.size() == 1) {
  // return elements.get(0).getAsInt();
  // }
  // throw new IllegalStateException();
  // }

  // @Override
  // public byte getAsByte() {
  // if (elements.size() == 1) {
  // return elements.get(0).getAsByte();
  // }
  // throw new IllegalStateException();
  // }

  // @Override
  // public char getAsCharacter() {
  // if (elements.size() == 1) {
  // return elements.get(0).getAsCharacter();
  // }
  // throw new IllegalStateException();
  // }

  /**
   * convenience method to get this array as a primitive short if it contains a
   * single element.
   *
   * @return get this element as a primitive short if it is single element
   *         array.
   * @throws ClassCastException
   *           if the element in the array is of not a {@link JtonPrimitive} and
   *           is not a valid short.
   * @throws IllegalStateException
   *           if the array has more than one element.
   */
  // @Override
  // public short getAsShort() {
  // if (elements.size() == 1) {
  // return elements.get(0).getAsShort();
  // }
  // throw new IllegalStateException();
  // }

  /**
   * convenience method to get this array as a boolean if it contains a single
   * element.
   *
   * @return get this element as a boolean if it is single element array.
   * @throws ClassCastException
   *           if the element in the array is of not a {@link JtonPrimitive} and
   *           is not a valid boolean.
   * @throws IllegalStateException
   *           if the array has more than one element.
   */
  // @Override
  // public boolean getAsBoolean() {
  // if (elements.size() == 1) {
  // return elements.get(0).getAsBoolean();
  // }
  // throw new IllegalStateException();
  // }

  @Override
  public boolean equals(Object o) {
    return (o == this) || (o instanceof JtonArray && ((JtonArray) o).elements.equals(elements));
  }

  @Override
  public int hashCode() {
    return elements.hashCode();
  }

  @Override
  public boolean isEmpty() {
    return elements.isEmpty();
  }

  @Override
  @Deprecated
  public boolean contains(Object o) {
    return elements.contains(o);
  }

  @Override
  public Object[] toArray() {
    return elements.toArray();
  }

  @Override
  public <T> T[] toArray(T[] a) {
    return elements.toArray(a);
  }

  @Override
  @Deprecated
  public boolean remove(Object o) {
    return elements.remove(o);
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    return elements.containsAll(c);
  }

  @Override
  public boolean addAll(Collection<? extends JtonElement> c) {
    return elements.addAll(c);
  }

  @Override
  public boolean addAll(int index, Collection<? extends JtonElement> c) {
    return elements.addAll(index, c);
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    return elements.removeAll(c);
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    return elements.retainAll(c);
  }

  @Override
  public void clear() {
    elements.clear();
  }

  @Override
  public void add(int index, JtonElement element) {
    elements.add(index, element);
  }

  @Override
  @Deprecated
  public int indexOf(Object o) {
    return elements.indexOf(o);
  }

  @Override
  public int indexOf(JtonElement e) {
    return elements.indexOf(e);
  }

  @Override
  @Deprecated
  public int lastIndexOf(Object o) {
    return elements.lastIndexOf(o);
  }
  
  @Override
  public int lastIndexOf(JtonElement o) {
    return elements.lastIndexOf(o);
  }

  @Override
  public ListIterator<JtonElement> listIterator() {
    return elements.listIterator();
  }

  @Override
  public ListIterator<JtonElement> listIterator(int index) {
    return elements.listIterator(index);
  }

  @Override
  public List<JtonElement> subList(int fromIndex, int toIndex) {
    return elements.subList(fromIndex, toIndex);
  }

  @Override
  public Stream<JtonElement> stream() {
    return elements.stream();
  }
}
