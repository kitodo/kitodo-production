/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */
package org.goobi.production.plugin.CataloguePlugin.PicaPlugin;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.*;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.log4j.Logger;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

/**
 * Resolve rules are used to import data from a related record into a PICA
 * field.
 * <p>
 * Example: If PICA field 028C subfield 9 is present, take the value from that
 * field and look it up in the catalogue by a request on search field 12. In the
 * result, select field 003U subfield a and store its value as subfield g in the
 * same field of the main record. Configuration:
 *
 * <pre>
 * {@code
 * <catalogue title="…">
 *     <!-- ... -->
 *     <resolve tag="028C" subtag="9" searchField="12">
 *         <map tag="003U" subtag="a" asSubtag="g" />
 *     </resolve>
 * }
 * </pre>
 *
 * Example field on a record:
 *
 * <pre>
 * {@code
 * <field tag="028C">
 *     <subfield code="d">Dirk</subfield>
 *     <subfield code="a">Fahlenkamp</subfield>
 *     <subfield code="9">69749487X</subfield>
 *     <subfield code="8">Fahlenkamp, Dirk *1952-*</subfield>
 * </field>
 * }
 * </pre>
 *
 * Example result after successful resolving:
 *
 * <pre>
 * {@code
 * <field tag="028C">
 *     <subfield code="d">Dirk</subfield>
 *     <subfield code="a">Fahlenkamp</subfield>
 *     <subfield code="9">69749487X</subfield>
 *     <subfield code="8">Fahlenkamp, Dirk *1952-*</subfield>
 *     <subfield code="g">http://d-nb.info/gnd/172559227</subfield>
 * </field>
 * }
 * </pre>
 *
 * @author Matthias Ronge
 */
class ResolveRule {
	private static final Logger logger = Logger.getLogger(ResolveRule.class);

	/**
	 * Returns an identifier for a resolve rule.
	 *
	 * @param tag
	 *            PICA field tag
	 * @param subtag
	 *            PICA subfield code
	 * @return an identifier for a resolve rule
	 */
	static String getIdentifier(String tag, String subtag) {
		return tag + '.' + subtag;
	}

	/**
	 * Creates a new resolve rule from the XML configuration. The attributes
	 * {@code tag} and {@code subtag} are required. The attributes
	 * {@code searchField} is optional, defaults to 12. The attribute
	 * {@code substring} is optional as well; if it is missing, no substring
	 * will be created.
	 *
	 * @param config
	 *            configuration for the resolve rule from
	 */
	@SuppressWarnings("unchecked")
	public ResolveRule(HierarchicalConfiguration config) {
		tag = config.getString("[@tag]");
		assert tag != null : "Missing mandatory attribute tag in element <resolve> of catalogue configuration";
		assert tag.length() == 4 : "Attribute tag in element <resolve> is not 4 characters long";

		subtag = config.getString("[@subtag]");
		assert subtag != null : "Missing mandatory attribute subtag in element <resolve> of catalogue configuration";
		assert subtag.length() == 1 : "Attribute subtag in element <resolve> is not 1 character long";

		searchField = config.getInt("[@searchField]", 12);

		String substringRegex = config.getString("[@substring]", null);
		substring = substringRegex != null ? Pattern.compile(substringRegex) : null;

		mapping = new HashMap<>();
		for (HierarchicalConfiguration map : (List<HierarchicalConfiguration>) config.configurationsAt("map")) {
			MapRule e = new MapRule(map);
			mapping.put(getIdentifier(e.tag, e.subtag), e);
		}
		assert !mapping.isEmpty() : "Element <resolve> does not contain any <map> element";
	}

	/**
	 * PICA field tag of a field which may have a subfield with an ID to
	 * resolve. Typical values match the pattern <code>\d{3}[@-Z]</code>. An
	 * example value is "028C" for an author.
	 */
	String tag;

	/**
	 * PICA subfield code of the subfield that has the ID to resolve. Typical
	 * values match the pattern <code>[0-9a-z]</code>. An example value is "9"
	 * for the PPN of the person record for the person.
	 */
	String subtag;

	/**
	 * Catalogue search field to resolve the ID on. Typical values are in range
	 * 1—9999. An example value is 12 for PPN search.
	 */
	int searchField;

	/**
	 * Allows to look up a substring from the field only. If the pattern
	 * contains a match group (identified by parentheses) the value from that
	 * group is used. If not, the region returned by the find() operation is
	 * used.
	 */
	Pattern substring;

	/**
	 * Import mapping fields and sub-fields to import.
	 */
	Map<String, MapRule> mapping;

	/**
	 * Returns an identifier for the resolve rule.
	 *
	 * @return an identifier for the resolve rule
	 */
	String getIdentifier() {
		return getIdentifier(tag, subtag);
	}

	/**
	 * Copy data from resolved record to corresponding element, as defined by
	 * mapping.
	 *
	 * @param in
	 *            input data
	 * @param resultField
	 *            element to write to
	 */
	private void applyMapping(Element in, Element resultField) {
		boolean debugEnabledAndNoMappingRuleMatches = logger.isDebugEnabled();
		Set<String> fieldsInResult = logger.isDebugEnabled() ? new TreeSet<String>() : null;
		for (Element field : new GetChildElements(in)) {
			if (field.getNodeName().equals("field")) {
				String tag = field.getAttributeNode("tag").getTextContent();
				for (Element subfield : new GetChildElements(field)) {
					if (subfield.getNodeName().equals("subfield")) {
						String subtag = subfield.getAttributeNode("code").getTextContent();
						String mappingID = ResolveRule.getIdentifier(tag, subtag);
						if (mapping.containsKey(mappingID)) {
							debugEnabledAndNoMappingRuleMatches = false;
							Element newChild = resultField.getOwnerDocument().createElement("subfield");
							newChild.setAttribute("code", mapping.get(mappingID).asSubtag);
							newChild.setTextContent(subfield.getTextContent());
							resultField.appendChild(newChild);
						} else if (debugEnabledAndNoMappingRuleMatches) {
							fieldsInResult.add(mappingID);
						}
					}
				}
			} else {
				applyMapping(field, resultField);
			}
		}
		if (debugEnabledAndNoMappingRuleMatches) {
			StringBuilder debugLogMessage = new StringBuilder(13 * mapping.size() + 8 * fieldsInResult.size() + 71);
			debugLogMessage.append("No import mapping for any of the result fields ");
			boolean addAComma = false;
			for (String fieldInResult : fieldsInResult) {
				debugLogMessage.append(fieldInResult);
				if (addAComma) {
					debugLogMessage.append(", ");
				} else {
					addAComma = true;
				}
			}
			debugLogMessage.append(". Configured mappings: ");
			addAComma = false;
			for (Entry<String, MapRule> mappingEntry : mapping.entrySet()) {
				debugLogMessage.append(mappingEntry.getValue());
				if (addAComma) {
					debugLogMessage.append(", ");
				} else {
					addAComma = true;
				}
			}
			debugLogMessage.append('.');
			logger.debug(debugLogMessage);
		}
	}

	/**
	 * Applies the substring on the input, if any.
	 *
	 * @param input
	 *            String to apply the substring on
	 * @return substring if found, or input otherwise
	 */
	private String applySubstring(String input) {
		if (substring == null)
			return input;
		Matcher matcher = substring.matcher(input);
		if (!matcher.find()) {
			logger.warn("Pattern /" + substring.pattern() + "/ was not found on input string \"" + input + '"');
			return input;
		} else {
			String matchGroup1 = matcher.group(1);
			if (matchGroup1 != null) {
				if(logger.isDebugEnabled()) {
					logger.debug("Using substring from match group 1: " + matchGroup1);
				}
				return matchGroup1;
			} else {
				String found = matcher.group();
				if(logger.isDebugEnabled()) {
					logger.debug("Using found substring: " + found);
				}
				return found;
			}
		}
	}

	/**
	 * Execute the resolve rule.
	 *
	 * @param subfield
	 *            subfield to take the ID to resolve from
	 * @param client
	 *            catalogue client to use for catalogue look-up
	 * @param out
	 *            field to write the result to
	 * @throws IOException
	 *             an IO exception from the parser, possibly from a byte stream
	 *             or character stream supplied by the application.
	 * @throws ParserConfigurationException
	 *             if a parser cannot be created which satisfies the requested
	 *             configuration.
	 * @throws SAXException
	 *             if any SAX errors occur during processing
	 */
	void execute(Element subfield, CatalogueClient client, Element out) throws IOException, SAXException, ParserConfigurationException {
		assert tag.equals(out.getAttributeNode("tag").getTextContent()) : "Attempt to call the rule on the wrong field.";
		assert subtag.equals(subfield.getAttributeNode("code").getTextContent()) : "Attempt to call the rule on the wrong subfield.";

		String queryString = applySubstring(subfield.getFirstChild().getTextContent());
		Query query = new Query(queryString, Integer.toString(searchField));
		int numberOfHits = client.getNumberOfHits(query);
		if (numberOfHits == 1) {
			Node result = client.retrievePicaNode(query, 1);
			if (result instanceof Element) {
				applyMapping((Element) result, out);
			}
		} else {
			logger.warn("Could not apply resolve rule " + this.toString() + " for query string \"" + queryString
					+ "\": Number of hits is " + numberOfHits);
		}
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder(80);
		result.append("<resolve tag=\"");
		result.append(tag);
		result.append("\" subtag=\"");
		result.append(subtag);
		if (substring != null) {
			result.append("\" substring=\"");
			result.append(substring.pattern());
		}
		result.append("\" searchField=\"");
		result.append(searchField);
		result.append("\">");
		return result.toString();
	}
}
