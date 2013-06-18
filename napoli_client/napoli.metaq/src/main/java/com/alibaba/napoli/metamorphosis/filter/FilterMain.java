package com.alibaba.napoli.metamorphosis.filter;

import java.util.HashMap;
import java.util.Map;

public class FilterMain {

	public static void main(String[] args) throws Exception {

		Map<SimpleString, Identifier> identifierMap = new HashMap<SimpleString, Identifier>();
		FilterParser parser = new FilterParser();
		Operator result = (Operator) parser.parse(new SimpleString(
				"-12345 = -1 * 12345"), identifierMap);
//		System.out.println("result="+result);
//		System.out.println(result.apply());
//		result = (Operator) parser.parse(new SimpleString(
//				"-123452 = -1 * 12345"), identifierMap);
//		System.out.println(result.apply());
//		result = (Operator) parser.parse(new SimpleString(
//				"Status IN ('new', 'cleared', 'acknowledged')"), identifierMap);
//		Identifier a = identifierMap.get(new SimpleString("Status"));
//		a.setValue(new SimpleString("new"));
//		System.out.println(result.apply());
//
//		result = (Operator) parser.parse(new SimpleString(
//				"Status IN ('new2', 'cleared', 'acknowledged')"), identifierMap);
//		System.out.println("result="+result);
//		a = identifierMap.get(new SimpleString("Status"));
//		a.setValue(new SimpleString("new"));
//		System.out.println(result.apply());
//		
//		result = (Operator)parser.parse(new SimpleString("average = -95.7"), identifierMap);
////		a = identifierMap.get(new SimpleString("average"));
////		a.setValue(new SimpleString("-95.7"));
////		System.out.println(result.apply());
//		System.out.println("result="+result);
//		
//		
//		result = (Operator)parser.parse(new SimpleString("weight > 2500"), identifierMap);
//        identifierMap.get(new SimpleString("weight")).setValue(new Integer(3000));
//        System.out.println(result.apply());
        
        
        result = (Operator)parser.parse(new SimpleString("a='true'"),
                identifierMap);
//identifierMap.get(new SimpleString("a")).setValue(new SimpleString("category1"));
//        Identifier i = identifierMap.get(new SimpleString("a"));
//        i.
identifierMap.get(new SimpleString("a")).setValue(new SimpleString("true"));
System.out.println(result.apply());
	}
}
