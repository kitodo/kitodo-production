/*
 * This file is part of the SUB Commons project.
 * Visit the websites for more information. 
 * 		- http://gdz.sub.uni-goettingen.de 
 * 
 * Copyright 2009, Center for Retrospective Digitization, Göttingen (GDZ),
 * 
 * 
 * Licensed under the Apache License, Version 2.0 (the “License”);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an “AS IS” BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.unigoettingen.sub.commons.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO: Auto-generated Javadoc
/**
 * The Class StringUtils is a simple utility class for advanced (read obscure) string operations
 */
public class StringUtils {

	/**
	 * Returns the index within the given String of the first occurrence of the specified regular expression.
	 * 
	 * @param str the String to search
	 * @param searchStr the regular expression to search for.
	 * 
	 * @return the index of the found expression
	 */
	public static int indexOfRegex (String str, String searchStr) {
		Pattern p = Pattern.compile(searchStr);
		Matcher m = p.matcher(str);
		if (m.find()) {
			return m.start();	
		}
		return -1;
	}

	/**
	 * Returns List of indexes within the given String of all occurrences of the specified character.
	 * 
	 * @param s1 the s1
	 * @param s2 the s2
	 * 
	 * @return the List of indexes
	 */
	public static List<Integer> allIndexOf (String s1, String s2) {
		List<Integer> al = new ArrayList<Integer>();
		StringBuffer sb = new StringBuffer(s1);
		Integer base = 0;
		while (sb.indexOf(s2) >= 0) {
			Integer pos = sb.indexOf(s2);
			al.add(pos + base);
			base += sb.delete(0, pos + s2.length()).length();
		}
		return al;
	}
	
	/**
	 * Index of occurance.
	 * 
	 * @param s1 the s1
	 * @param s2 the s2
	 * @param i the i
	 * 
	 * @return the int
	 */
	public static int indexOfOccurance (String s1, String s2, Integer i) {
		ArrayList<Integer> al = new ArrayList<Integer>();
		al.addAll(allIndexOf(s1, s2));
		if (al.size() <= i - 1) {
			return al.get(i - i);
		} else {
			return -1;
		}
	}
	
	/**
	 * Tests if the given string starts with the specified prefix or ends with the specified suffix.
	 * 
	 * @param in the string to test
	 * @param str the pre- or suffix
	 * 
	 * @return true, if successful
	 */
	public static boolean startsOrEndsWith(String in, String str) {
		if (in.startsWith(str)) {
			return true;
		}
		if (in.endsWith(str)) {
			return true;
		}
		return false;
	}

	/**
	 * Tests if the given string ends with the specified suffix.
	 * 
	 * @param in the string to test
	 * @param str the suffix
	 * 
	 * @return true, if successful
	 * 
	 * @deprecated
	 */
	@Deprecated
	public static boolean endsWith(String in, String str) {
		char[] c = str.toCharArray();
		for (int i = 0; i < c.length; i++) {
			if (in.endsWith(String.valueOf(c[i]))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Escape a regular expression.
	 * 
	 * @param str the string containing a regular expression
	 * 
	 * @return the escaped string
	 */
	public static String escapeRegex (String str) {
		str = str.replaceAll("\\*", "\\\\*");
		str = str.replaceAll("\\.", "\\\\.");
		str = str.replaceAll("\\?", "\\\\?");
		str = str.replaceAll("\\+", "\\\\+");
		//str = str.replaceAll("\\", "\\\\");
		str = str.replaceAll("\\$", "\\\\$");
		str = str.replaceAll("\\|", "\\\\|");
		str = str.replaceAll("\\{", "\\\\{");
		str = str.replaceAll("\\}", "\\\\}");
		str = str.replaceAll("\\[", "\\\\[");
		str = str.replaceAll("\\]", "\\\\]");
		return str;
	}
	
	/**
	 * Returns true if and only if the given string contains the specified string, ignoring the case aof each string.
	 * 
	 * @param in the string to test
	 * @param contains the string to search for
	 * 
	 * @return true, if successful
	 */
	public static boolean containsIgnoreCase (String in, String contains) {
		return in.toLowerCase().contains(contains.toLowerCase());
		
	}
	
}
