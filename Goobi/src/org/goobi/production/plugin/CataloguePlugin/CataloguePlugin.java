/**
 * This file is part of the Goobi Application - a Workflow tool for the support
 * of mass digitization.
 * 
 * (c) 2014 Goobi. Digitalisieren im Verein e.V. <contact@goobi.org>
 * 
 * Visit the websites for more information.
 *     		- http://www.kitodo.org/en/
 *     		- https://github.com/goobi
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Linking this library statically or dynamically with other modules is making a
 * combined work based on this library. Thus, the terms and conditions of the
 * GNU General Public License cover the whole combination. As a special
 * exception, the copyright holders of this library give you permission to link
 * this library with independent modules to produce an executable, regardless of
 * the license terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided that you also
 * meet, for each linked independent module, the terms and conditions of the
 * license of that module. An independent module is a module which is not
 * derived from or based on this library. If you modify this library, you may
 * extend this exception to your version of the library, but you are not obliged
 * to do so. If you do not wish to do so, delete this exception statement from
 * your version.
 */
package org.goobi.production.plugin.CataloguePlugin;

import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.goobi.production.constants.Parameters;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.PluginLoader;
import org.goobi.production.plugin.UnspecificPlugin;

import ugh.dl.Fileformat;
import ugh.dl.Prefs;
import de.sub.goobi.config.ConfigMain;
import de.unigoettingen.sub.search.opac.ConfigOpac;
import de.unigoettingen.sub.search.opac.ConfigOpacDoctype;

/**
 * The class CataloguePlugin is a redirection class that takes a library
 * catalogue access plugin implementation object as argument. The plugin
 * implementation class can be a POJO that can be compiled without the necessity
 * to link it against the Production code, thus making it possible to provide
 * proprietary plugins that do not violate the GPL. The plugin class must
 * however implement the following public methods which it is checked for upon
 * instantiation. If one of the methods is missing a NoSuchMethodException will
 * be thrown.
 * 
 * <p>
 * <code>void configure(Map)</code><br>
 * See {@link UnspecificPlugin}.
 * </p>
 * 
 * <p>
 * <code>Object find(String, long)</code><br>
 * The function find() is to perform a search request in a library catalogue.
 * See {@link QueryBuilder} for the semantics of the query. It may return an
 * arbitrary Object identifying (or—at the implementor’s choice—containing) the
 * search result. The method shall return null if the search performed normally,
 * but didn’t yield any result. The method shall ensure that it returns after
 * the given timeout and shall throw a javax.persistence.QueryTimeoutException
 * if it was cancelled by the timer. The method may throw exceptions.
 * </p>
 * 
 * <p>
 * <code>String getDescription()</code><br>
 * See {@link UnspecificPlugin}.
 * </p>
 * 
 * <p>
 * <code>Map getHit(Object, long, long)</code><br>
 * The function getHit() shall return the hit identified by its index. The hit
 * shall be a Map&lt;String, Object&gt; with the fields described in {@link Hit}
 * populated. The method shall ensure that it returns after the given timeout
 * and shall throw a javax.persistence.QueryTimeoutException if it was cancelled
 * by the timer. The method may throw exceptions.
 * </p>
 * 
 * <p>
 * <code>long getNumberOfHits(Object, long)</code><br>
 * The function getNumberOfHits() shall return the number of hits scored by the
 * search represented by the given object. If the object isn’t the result of a
 * call to the find() function, the behaviour of the function may be undefined.
 * The method shall ensure that it returns after the given timeout and shall
 * throw a javax.persistence.QueryTimeoutException if it was cancelled by the
 * timer. The method may throw exceptions.
 * </p>
 * 
 * <p>
 * <code>String getTitle()</code><br>
 * See {@link UnspecificPlugin}.
 * </p>
 * 
 * <p>
 * <code>void setPreferences(Prefs)</code><br>
 * The method setPreferences() is called before the first search request to the
 * plug-in and passes the UGH preferences the plugin shall use.
 * </p>
 * 
 * <p>
 * <code>boolean supportsCatalogue(String)</code><br>
 * The function supportsCatalogue() shall return whether the plug-in has
 * sufficient knowledge to query a catalogue identified by the given String
 * literal or not.
 * </p>
 * 
 * <p>
 * <code>void useCatalogue(String)</code><br>
 * The function useCatalogue() is called before the first search request to the
 * plug-in and shall tell it to use the catalogue connection identified by the
 * given String literal. If the plugin doesn’t support the given catalogue
 * (supportsCatalogue() would return false) the behaviour may be unspecified.
 * </p>
 * 
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public class CataloguePlugin extends UnspecificPlugin {

	/**
	 * The constant field THIRTY_MINUTES holds the milliseconds value
	 * representing 30 minutes. This is the default for catalogue operations
	 * timeout. Note that on large, database-backed catalogues, searches for
	 * common title terms may take more than 15 minutes, so 30 minutes is a fair
	 * deal for an internal default value here.
	 */
	private static final long THIRTY_MINUTES = TimeUnit.MILLISECONDS.convert(30, TimeUnit.MINUTES);

	/**
	 * The field find holds a Method reference to the method find() of the
	 * plug-in implementation class.
	 */
	private final Method find;

	/**
	 * The field getHit holds a Method reference to the method getHit() of the
	 * plug-in implementation class.
	 */
	private final Method getHit;

	/**
	 * The field getNumberOfHits holds a Method reference to the method
	 * getNumberOfHits() of the plug-in implementation class.
	 */
	private final Method getNumberOfHits;

	/**
	 * The field setPreferences holds a Method reference to the method
	 * setPreferences() of the plug-in implementation class.
	 */
	final private Method setPreferences;

	/**
	 * The field supportsCatalogue holds a Method reference to the method
	 * supportsCatalogue() of the plug-in implementation class.
	 */
	private final Method supportsCatalogue;
	
	private final Method getSupportedCatalogues;
	
	private final Method getAllConfigDocTypes;

	/**
	 * The field useCatalogue holds a Method reference to the method
	 * useCatalogue() of the plug-in implementation class.
	 */
	private final Method useCatalogue;

	/**
	 * CataloguePlugin constructor. The constructor takes a reference to the
	 * plug-in implementation class, saves it in the final field plugin and
	 * inspects the class for existence of the methods configure,
	 * getDescription, getTitle, find, getHit, getNumberOfHits, setPreferences,
	 * supportsCatalogue and useCatalogue.
	 * 
	 * @param implementation
	 *            plug-in implementation class
	 * @throws SecurityException
	 *             If a security manager, is present and an invocation of its
	 *             method checkMemberAccess(this, Member.DECLARED) denies access
	 *             to the declared method or if the caller’s class loader is not
	 *             the same as or an ancestor of the class loader for the
	 *             current class and invocation of the security manager’s
	 *             checkPackageAccess() denies access to the package of this
	 *             class.
	 * @throws NoSuchMethodException
	 *             if a required method is not found on the plug-in
	 */
	public CataloguePlugin(Object implementation) throws SecurityException, NoSuchMethodException {
		super(implementation);
		find = getDeclaredMethod("find", new Class[] { String.class, long.class }, Object.class);
		getHit = getDeclaredMethod("getHit", new Class[] { Object.class, long.class, long.class }, Map.class);
		getNumberOfHits = getDeclaredMethod("getNumberOfHits", new Class[] { Object.class, long.class }, long.class);
		setPreferences = getDeclaredMethod("setPreferences", Prefs.class, Void.TYPE);
		supportsCatalogue = getDeclaredMethod("supportsCatalogue", String.class, boolean.class);
		getSupportedCatalogues = getDeclaredMethod("getSupportedCatalogues", Object.class, List.class);
		getAllConfigDocTypes = getDeclaredMethod("getAllConfigDocTypes", Object.class, List.class);
		useCatalogue = getDeclaredMethod("useCatalogue", String.class, Void.TYPE);
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
	 * The function getFirstHit() returns at random the first hit the catalogue
	 * plugin returns for a given query. Making use of this utility method only
	 * makes sense if there is only one result expected, which usually is the
	 * case if a valid identifier is looked up in its respective column.
	 * 
	 * @param catalogue
	 *            catalogue in question
	 * @param query
	 *            Query string
	 * @param preferences
	 * @return UGH preferences
	 */
	public static Fileformat getFirstHit(String catalogue, String query, Prefs preferences) {
		try {
			CataloguePlugin plugin = PluginLoader.getCataloguePluginForCatalogue(catalogue);
			plugin.setPreferences(preferences);
			plugin.useCatalogue(catalogue);
			Object searchResult = plugin.find(query, getTimeout());
			long hits = plugin.getNumberOfHits(searchResult, getTimeout());
			if (hits > 0)
				return plugin.getHit(searchResult, 0, getTimeout()).getFileformat();
			else
				return null;
		} catch (RuntimeException e) {
			return null;
		}
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
		if (searchResult == null)
			return 0;
		return invokeQuietly(plugin, getNumberOfHits, new Object[] { searchResult, timeout }, long.class);
	}

	/**
	 * The function getTimeout() returns the timeout to be used in catalogue
	 * access. This defaults to thirty minutes if no catalogue timeout is set in
	 * the configuration
	 * 
	 * Note that on large, database-backed catalogues, searches for common title
	 * terms may take more than 15 minutes, so 30 minutes may be a fair avarage
	 * between that and the moment the sun will swallow up the earth.
	 * 
	 * @return the timeout for catalogue access
	 */
	public static long getTimeout() {
		return ConfigMain.getLongParameter(Parameters.CATALOGUE_TIMEOUT, THIRTY_MINUTES);
	}

	/**
	 * The function getType() returns the PluginType.Opac as it corresponds to
	 * this class.
	 * 
	 * @see org.goobi.production.plugin.UnspecificPlugin#getType()
	 */
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

	@SuppressWarnings("unchecked")
	public List<String> getSupportedCatalogues(Object dummyObject){
		return invokeQuietly(plugin, getSupportedCatalogues, Object.class, List.class);
	}
	
	public List<ConfigOpacDoctype> getAllConfigDocTypes(Object dummyObject){
		List<ConfigOpacDoctype> result = new ArrayList<ConfigOpacDoctype>();
		for(Object obj : invokeQuietly(plugin, getAllConfigDocTypes, Object.class, List.class)){
			try {
				result.add(ConfigOpac.getDoctypeByName((String)obj));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return result;
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
	 */
	public void useCatalogue(String catalogue) {
		invokeQuietly(plugin, useCatalogue, catalogue, null);
	}
}
