package org.goobi.production.plugin.CataloguePlugin;

import java.lang.reflect.Method;
import java.util.Map;

import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.UnspecificPlugin;

import ugh.dl.Prefs;

public class CataloguePlugin extends UnspecificPlugin {

	private final Method find;
	private final Method getHit;
	private final Method getNumberOfHits;
	final private Method setPreferences;
	private final Method supportsCatalogue;
	private final Method useCatalogue;

	public CataloguePlugin(Object implementation) throws SecurityException, NoSuchMethodException {
		super(implementation);
		find = getDeclaredMethod("find", new Class[] { String.class, long.class }, Object.class);
		getHit = getDeclaredMethod("getHit", new Class[] { Object.class, long.class, long.class }, Map.class);
		getNumberOfHits = getDeclaredMethod("getNumberOfHits", new Class[] { Object.class, long.class }, long.class);
		setPreferences = getDeclaredMethod("setPreferences", new Class[] { Prefs.class }, null);
		supportsCatalogue = getDeclaredMethod("supportsCatalogue", new Class[] { String.class }, boolean.class);
		useCatalogue = getDeclaredMethod("useCatalogue", new Class[] { String.class }, null);
	}

	/**
	 * The function find() is intended to send a search request to a library
	 * catalogue and to return an Object identifying (or—at the implementor’s
	 * choice—containing) the search result. The method shall return null if the
	 * search didn’t yield any result. The method shall ensure that it returns
	 * after the given timeout and shall throw a
	 * javax.persistence.QueryTimeoutException in that case. The method may
	 * throw exceptions.
	 * 
	 * @param query
	 *            Query string
	 * @param timeout
	 *            timeout in milliseconds
	 * @return an object identifying the result set
	 */
	public Object find(String query, long timeout) {
		return invokeQuietly(plugin, find, new Object[] { query, timeout }, Object.class);
	}

	/**
	 * The function getHit() shall return the hit identified by its index. The
	 * method shall ensure that it returns after the given timeout and shall
	 * throw a javax.persistence.QueryTimeoutException in that case. The method
	 * may throw exceptions.
	 * 
	 * @param searchResult
	 *            an object identifying the search result
	 * @param index
	 *            zero-based index of the object to retrieve
	 * @param timeout
	 *            timeout in milliseconds
	 * @return A map with the fields of the hit
	 */
	@SuppressWarnings("unchecked")
	public Hit getHit(Object searchResult, long index, long timeout) {
		Map<String, Object> data = invokeQuietly(plugin, getHit, new Object[] { searchResult, index, timeout },
				Map.class);
		return new Hit(data);
	}

	/**
	 * The function getNumberOfHits() shall return the hits scored by the search
	 * represented by the given object. If the object isn’t the result of a call
	 * to the find() function, the behaviour may be undefined.
	 * 
	 * @param searchResult
	 *            search result object whose number of hits is to retrieve
	 * @return the number of hits in this search
	 */
	public long getNumberOfHits(Object searchResult, long timeout) {
		return invokeQuietly(plugin, getNumberOfHits, searchResult, long.class);
	}

	@Override
	public PluginType getType() {
		return PluginType.Opac;
	}

	/**
	 * The method setPreferences() must be used to set the UGH preferences the
	 * plugin shall use.
	 * 
	 * @param preferences
	 *            UGH preferences
	 * @see de.sub.goobi.beans.Regelsatz#getPreferences()
	 */
	public void setPreferences(Prefs preferences) {
		invokeQuietly(plugin, setPreferences, preferences, null);
	}

	/**
	 * The function supportsCatalogue() returns whether the plugin has
	 * sufficient knowledge to query a catalogue identified by the given String
	 * literal.
	 * 
	 * @param catalogue
	 *            catalogue in question
	 * @return whether the plugin supports that catalogue
	 */
	public boolean supportsCatalogue(String catalogue) {
		return invokeQuietly(plugin, supportsCatalogue, catalogue, boolean.class);
	}

	/**
	 * The function useCatalogue() shall tell the plugin to use a catalogue
	 * connection identified by the given String literal. If the plugin doesn’t
	 * support the given catalogue (supportsCatalogue() would return false) the
	 * behaviour is unspecified (throwing an unchecked exception is a good
	 * option).
	 * 
	 * @param catalogue
	 *            catalogue in question
	 * @return whether the plugin supports that catalogue
	 */
	public void useCatalogue(String catalogue) {
		invokeQuietly(plugin, useCatalogue, catalogue, null);
	}
}
