package org.goobi.production.plugin.CataloguePlugin.PicaPlugin;

import java.util.*;
import java.util.regex.Pattern;

import org.apache.commons.configuration.HierarchicalConfiguration;

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
 * &lt;catalogue title="…">
 *     &lt;!-- ... -->
 *     &lt;resolve tag="028C" subtag="9" searchField="12">
 *         &lt;map tag="003U" subtag="a" asSubtag="g" />
 *     &lt;/resolve>
 * </pre>
 * 
 * Example field on a record:
 * 
 * <pre>
 * &lt;field tag="028C">
 *     &lt;subfield code="d">Dirk&lt;/subfield>
 *     &lt;subfield code="a">Fahlenkamp&lt;/subfield>
 *     &lt;subfield code="9">69749487X&lt;/subfield>
 *     &lt;subfield code="8">Fahlenkamp, Dirk *1952-*&lt;/subfield>
 * &lt;/field>
 * </pre>
 * 
 * Example result after successful resolving:
 * 
 * <pre>
 * &lt;field tag="028C">
 *     &lt;subfield code="d">Dirk&lt;/subfield>
 *     &lt;subfield code="a">Fahlenkamp&lt;/subfield>
 *     &lt;subfield code="9">69749487X&lt;/subfield>
 *     &lt;subfield code="8">Fahlenkamp, Dirk *1952-*&lt;/subfield>
 *     &lt;subfield code="g">http://d-nb.info/gnd/172559227&lt;/subfield>
 * &lt;/field>
 * </pre>
 * 
 * @author Matthias Ronge
 */
class ResolveRule {
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
		tag = config.getString("tag");
		subtag = config.getString("subtag");
		searchField = config.getInt("searchField", 12);
		String substringRegex = config.getString("substring", null);
		substring = substringRegex != null ? Pattern.compile(substringRegex) : null;
		mapping = new LinkedList<>();
		for (HierarchicalConfiguration map : (List<HierarchicalConfiguration>) config.configurationsAt("map")) {
			mapping.add(new MapRule(map));
		}
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
	Collection<MapRule> mapping;
}
