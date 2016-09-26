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
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.veracloud.jton.JtonElement;
import com.veracloud.jton.JtonObject;

public class Test3 {

	public static void main(String[] args) throws IOException, SerializationException {
		String content = new String(Files.readAllBytes(Paths.get("test-files/test-data-01.json")));
		JtonElement data = JsonSerializer.parse(content);
		
		JtonObject o = new JtonObject();
		o.add("data", data);

		XmlSerializer xml = new XmlSerializer();
		xml.writeObject(o, System.out);
		
		StringWriter w = new StringWriter();
		xml.writeObject(o, w);
		
		System.out.println();
		
		xml.writeObject(xml.readObject(new StringReader(w.toString())), System.out);
	}
	
}
