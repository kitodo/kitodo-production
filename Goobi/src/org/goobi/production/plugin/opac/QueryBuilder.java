package org.goobi.production.plugin.opac;

public class QueryBuilder {

	public static String buildSimpleFieldedQuery(String field, String query) {
		StringBuilder result = new StringBuilder(2 * query.length());
		String prefix = field.concat(":");
		for (int index = 0; index < query.length(); index++) {
			int codePoint = query.charAt(index);
			boolean addField = true;
			boolean stringLiteral = false;
			switch (codePoint) {
			case ' ':
				if (!stringLiteral)
					addField = true;
				result.appendCodePoint(codePoint);
				break;
			case '"':
				if (stringLiteral && addField)
					result.append(prefix);
				stringLiteral = !stringLiteral;
				addField = stringLiteral;
				result.appendCodePoint(codePoint);
				break;
			case '(':
				if (!stringLiteral)
					addField = true;
				result.appendCodePoint(codePoint);
				break;
			case ')':
				if (!stringLiteral)
					addField = true;
				result.appendCodePoint(codePoint);
				break;
			case '-':
				result.appendCodePoint(codePoint);
				if (addField)
					result.append(prefix);
				addField = false;
				break;
			case '|':
				if (!stringLiteral)
					addField = true;
				result.appendCodePoint(codePoint);
				break;
			default:
				if (addField)
					result.append(prefix);
				result.appendCodePoint(codePoint);
				break;
			}

		}
		return null;
	}
}
