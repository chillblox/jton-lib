package com.arkasoft.jton.serialization;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import com.arkasoft.jton.JtonArray;
import com.arkasoft.jton.JtonObject;
import com.arkasoft.jton.JtonPrimitive;

public class Test {

	public static void main(String[] args) throws IOException, SerializationException {
	
		JtonObject o = new JtonObject();
		o.add("lala", 100);
		o.add("lilo", new BigDecimal("10000000000000000000000.9999999999999999"));
		o.add("lalala", new BigInteger("10000000000000000000000"));
		o.add("date", new Date());
		o.add("sqldate", new java.sql.Date(new Date().getTime()));
		o.add("sqltime", new java.sql.Time(new Date().getTime()));
		o.add("sqltstamp", new java.sql.Timestamp(new Date().getTime()));
		o.add("roger", "Roger Goudarzi");
		
		JtonArray arr = new JtonArray();
		arr.add(new JtonPrimitive(5));
		arr.add(new JtonPrimitive("Hello"));
		o.add("array", arr);
		
		o.add("object", o.deepCopy());

		JsonSerializer s = new JsonSerializer();
		s.writeObject(o, System.out);
//		
		System.out.println();
//		
//		JtonElement e = s.readObject(new StringReader(o.toString()));
//		e = s.readObject(new StringReader(e.toString()));
//		System.out.println(e);
//		
//		System.out.println(o.get("lilo").getAsDouble());
//		System.out.println(o.get("lilo").getAsBigDecimal());
		
		XmlSerializer xml = new XmlSerializer();
		xml.writeObject(o, System.out);
	}
	
}
