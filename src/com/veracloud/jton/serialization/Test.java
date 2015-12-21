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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import com.veracloud.jton.JtonArray;
import com.veracloud.jton.JtonElement;
import com.veracloud.jton.JtonNull;
import com.veracloud.jton.JtonObject;
import com.veracloud.jton.JtonPrimitive;

public class Test {

	public static void main(String[] args) throws IOException, SerializationException {
	
		JtonObject o = new JtonObject();
		o.add("null", JtonNull.INSTANCE);
		o.add("lala", 100);
		o.add("lilo", new BigDecimal("10000000000000000000000.9999999999999999"));
		o.add("lalala", new BigInteger("10000000000000000000000"));
		o.add("date", new Date());
		o.add("sqldate", new java.sql.Date(new Date().getTime()));
		o.add("sqltime", new java.sql.Time(new Date().getTime()));
		o.add("sqltstamp", new java.sql.Timestamp(new Date().getTime()));
		o.add("roger", "Roger Goudarzi");
		
		o.add("object", o.deepCopy());
		
		JtonArray arr = new JtonArray();
		arr.add(JtonNull.INSTANCE);
		arr.add(new JtonPrimitive(5));
		arr.add(new JtonPrimitive("Hello"));
		arr.add(o.deepCopy());
		arr.add(o.deepCopy());
		o.add("array", arr);

		JsonSerializer s = new JsonSerializer();
		s.setAlwaysDelimitMapKeys(true);
		s.setIntentFactor(3);
		s.writeObject(o, System.out);
		
		System.out.println();
	
		JtonElement e = s.readObject(new StringReader(o.toString()));
		e = s.readObject(new StringReader(e.toString()));
		System.out.println(e);
		
		XmlSerializer xml = new XmlSerializer();
		xml.writeObject(o, System.out);
		
		StringWriter w = new StringWriter();
		xml.writeObject(o, w);
		
		System.out.println();
		
		xml.writeObject(xml.readObject(new StringReader(w.toString())), System.out);
	}
	
}
