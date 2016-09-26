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
package com.veracloud.jton.serialization;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import com.veracloud.jton.JtonArray;
import com.veracloud.jton.JtonElement;
import com.veracloud.jton.JtonObject;

public class Test2 {

  public static JtonElement convert(JtonElement elem) {
    JtonObject result = new JtonObject();

    if (elem.isJtonObject()) {
      JtonObject obj = elem.getAsJtonObject();
      convertChild(result, "", obj);
    } else if (elem.isJtonArray()) {
      JtonArray array = elem.getAsJtonArray();
      convertChild(result, "", array);
    }

    return result;
  }

  private static void convertChild(JtonObject entity, String keyOfParent, JtonObject elem) {
    for (Map.Entry<String, JtonElement> entry : elem.getAsJtonObject().entrySet()) {
      String key = keyOfParent + "/" + entry.getKey();
      JtonElement child = entry.getValue();
      if (child.isJtonNull() || child.isJtonPrimitive()) {
        entity.add(key, child);
      } else if (child.isJtonObject()) {
        convertChild(entity, key, child.getAsJtonObject());
      } else if (child.isJtonArray()) {
        convertChild(entity, key, child.getAsJtonArray());
      }
    }
  }

  private static void convertChild(JtonObject entity, String keyOfParent, JtonArray array) {
    for (int i = 0, n = array.size(); i < n; i++) {
      String iKey = keyOfParent + "/" + i;
      JtonElement item = array.get(i);
      if (item.isJtonNull() || item.isJtonPrimitive()) {
        entity.add(iKey, item);
      } else if (item.isJtonObject()) {
        convertChild(entity, iKey, item.getAsJtonObject());
      } else if (item.isJtonArray()) {
        convertChild(entity, iKey, item.getAsJtonArray());
      }
    }
  }

  public static void main(String[] args) throws IOException, SerializationException {
    String content = new String(Files.readAllBytes(Paths.get("test-files/test-data-01.json")));
    JtonElement data = JsonSerializer.parse(content);

    System.out.println(data.toString(2));
    System.out.println(convert(data).toString(2));
  }

}
