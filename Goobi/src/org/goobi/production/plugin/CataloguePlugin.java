package org.goobi.production.plugin;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

import org.goobi.production.enums.PluginType;

public class CataloguePlugin extends UnspecificPlugin {

	private final Method find, getHit_Object_int_long, getHit_Object_int_Collection_long, getNumberOfHits;

	public CataloguePlugin(Object implementation) throws SecurityException, NoSuchMethodException {
		super(implementation);
		find = getDeclaredMethod("find", new Class[] { String.class, long.class }, Object.class);
		getHit_Object_int_long = getDeclaredMethod("getHit", new Class[] { Object.class, int.class, long.class },
				Map.class);
		getHit_Object_int_Collection_long = getDeclaredMethod("getHit", new Class[] { Object.class, int.class,
				Collection.class, long.class }, Map.class);
		getNumberOfHits = getDeclaredMethod("getNumberOfHits", new Class[] { Object.class, long.class }, long.class);
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
	public Map<String, Object> getHit(Object searchResult, int index, long timeout) {
		return invokeQuietly(plugin, getHit_Object_int_long, new Object[] { searchResult, index, timeout }, Map.class);
	}

	/**
	 * The function getHit() shall return the hit identified by its index. The
	 * hit shall contain at most the fields named. This method shall be favoured
	 * on both sides. It may unburden the database from looking up relations
	 * that are never processed in the application, and it may unburden the
	 * application from keeping large records in memory (i.e. the full OCR data
	 * from a book) which is never used. The method shall ensure that it returns
	 * after the given timeout and shall throw a
	 * javax.persistence.QueryTimeoutException in that case. The method may
	 * throw exceptions.
	 * 
	 * @param searchResult
	 *            an object identifying the search result
	 * @param index
	 *            zero-based index of the object to retrieve
	 * @param fields
	 *            fields to return in the result
	 * @param timeout
	 *            timeout in milliseconds
	 * @return A map with the fields of the hit
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> getHit(Object searchResult, int index, Collection<String> fields, long timeout) {
		return invokeQuietly(plugin, getHit_Object_int_Collection_long, new Object[] { searchResult, index, fields,
				timeout }, Map.class);
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
}
