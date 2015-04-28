package com.arkasoft.jton.serialization;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import com.arkasoft.jton.JtonArray;
import com.arkasoft.jton.JtonElement;
import com.arkasoft.jton.JtonNull;
import com.arkasoft.jton.JtonObject;
import com.arkasoft.jton.JtonPrimitive;

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
