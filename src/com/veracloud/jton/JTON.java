package com.veracloud.jton;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains utility methods for working with JTON and JSON-like data structures.
 * 
 * @author ggeorg
 */
public class JTON {

  /**
   * Returns the value at a given path.
   * 
   * @param root
   *          the root object
   * @param path
   *          the path to the value as a {@code JavaScript} path
   * @return the value at the given path
   */
  public static JtonElement get(JtonObject root, String path) {
    if (root == null) {
      throw new IllegalArgumentException("'root' is null");
    }

    if (path == null) {
      throw new IllegalArgumentException("'path' is null");
    }

    return get(root, parse(path));
  }

  public static JtonElement get(JtonObject root, List<String> path) {
    JtonElement e = root;

    for (int i = 0, n = path.size(); i < n; i++) {
      String prop = path.get(i);

      if (e.isJtonObject()) {
        e = e.get(prop);
      } else if (e.isJtonArray() && prop.startsWith("[")) {
        e = e.get(Integer.parseInt(prop.substring(1).trim()));
      } else {
        return JtonNull.INSTANCE;
      }
    }

    return e;
  }

  /**
   * Sets the value at a given path.
   * 
   * @param root
   *          the root object
   * @param path
   *          the path to the value as a {@code JavaScript} path
   * @param value
   *          the value to set
   */
  public static JtonObject set(JtonObject root, String path, Object value) {
    if (root == null) {
      throw new IllegalArgumentException("'root' is null");
    }

    if (path == null) {
      throw new IllegalArgumentException("'path' is null");
    }

    return set(root, parse(path), value);
  }

  public static JtonObject set(JtonObject root, List<String> path, Object value) {
    JtonElement e = root;

    final String lastPath = path.get(path.size() - 1);

    for (int i = 0, n = path.size() - 1; i < n; i++) {
      String prop = path.get(i);

      JtonElement parent = e;

      if (e.isJtonObject()) {
        e = e.get(prop);
      } else if (e.isJtonArray()) {
        if (prop.startsWith("[")) {
          e = e.get(Integer.parseInt(prop.substring(1).trim()));
        } else {
          throw new IllegalArgumentException("expecting array index: " + prop);
        }
      }

      if (e.isJtonNull() || e.isJtonPrimitive()) {
        String nextProp = path.get(i + 1);
        if (parent.isJtonObject()) {
          if (nextProp.startsWith("[")) {
            parent.getAsJtonObject().set(prop, e = new JtonArray());
          } else {
            parent.getAsJtonObject().set(prop, e = new JtonObject());
          }
        } else if (parent.isJtonArray()) {
          int index = Integer.parseInt(prop.substring(1).trim());
          if (nextProp.startsWith("[")) {
            parent.getAsJtonArray().set(index, e = new JtonArray());
          } else {
            parent.getAsJtonArray().set(index, e = new JtonObject());
          }
        }
      }
    }

    JtonElement _value = value instanceof JtonElement ? (JtonPrimitive) value : new JtonPrimitive(value);
    if (e.isJtonObject()) {
      e.getAsJtonObject().set(lastPath, _value);
    } else if (e.isJtonArray()) {
      if (lastPath.startsWith("[")) {
        e.getAsJtonArray().set(Integer.parseInt(lastPath.substring(1).trim()), _value);
      } else {
        throw new IllegalArgumentException("expecting array index: " + lastPath);
      }
    }

    return root;
  }

  public static List<String> parse(String path) {
    if (path == null) {
      throw new IllegalArgumentException("path is null.");
    }

    ArrayList<String> keys = new ArrayList<String>();

    int i = 0;
    int n = path.length();

    while (i < n) {
      char c = path.charAt(i++);

      StringBuilder identifierBuilder = new StringBuilder();

      boolean bracketed = (c == '[');
      if (bracketed && i < n) {
        identifierBuilder.append(c);

        c = path.charAt(i++);

        char quote = Character.UNASSIGNED;

        boolean quoted = (c == '"'
            || c == '\'');
        if (quoted
            && i < n) {
          quote = c;
          c = path.charAt(i++);
        }

        while (i <= n
            && bracketed) {
          bracketed = quoted || (c != ']');

          if (bracketed) {
            if (c == quote) {
              if (i < n) {
                c = path.charAt(i++);
                quoted = (c == quote);
              }
            }

            if (quoted || c != ']') {
              if (Character.isISOControl(c)) {
                throw new IllegalArgumentException("Illegal identifier character.");
              }

              identifierBuilder.append(c);

              if (i < n) {
                c = path.charAt(i++);
              }
            }
          }
        }

        if (quoted) {
          throw new IllegalArgumentException("Unterminated quoted identifier.");
        }

        if (bracketed) {
          throw new IllegalArgumentException("Unterminated bracketed identifier.");
        }

        if (i < n) {
          c = path.charAt(i);

          if (c == '.') {
            i++;
          }
        }
      } else {
        while (i <= n
            && c != '.'
            && c != '[') {
          if (!Character.isJavaIdentifierPart(c)) {
            throw new IllegalArgumentException("Illegal identifier character.");
          }

          identifierBuilder.append(c);

          if (i < n) {
            c = path.charAt(i);
          }

          i++;
        }

        if (c == '[') {
          i--;
        }
      }

      if (c == '.'
          && i == n) {
        throw new IllegalArgumentException("Path cannot end with a '.' character.");
      }

      if (identifierBuilder.length() == 0) {
        throw new IllegalArgumentException("Missing identifier.");
      }

      keys.add(identifierBuilder.toString().trim());
    }

    return keys;
  }

  public static void main(String[] args) {
    JtonObject msg = new JtonObject();
    System.out.println(JTON.set(msg, "lala.lala[   8 ].list", true).toString(3));

    System.out.println(JTON.get(msg, "lala.lala[   8 ]"));
  }
}
