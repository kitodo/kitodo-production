package org.goobi.production.plugin.CataloguePlugin.PicaPlugin;

import java.util.HashMap;
import java.util.Map;

import net.xeoh.plugins.base.annotations.PluginImplementation;
import ugh.dl.Fileformat;

@PluginImplementation
public class PicaPlugin {
	// @see org.goobi.production.plugin.CataloguePlugin#find(String, long)
	public static Object find(String query, long timeout) {

	}

	// @see org.goobi.production.plugin.UnspecificPlugin#getDescription()
	public static String getDescription() {
		return "The PICA plugin can be used to access PICA library catalogue systems.";
	}

	// @see org.goobi.production.plugin.CataloguePlugin#getHit(Object, int, long)
	public static Map<String, Object> getHit(Object searchResult, int index, long timeout) {
		// get hit from hit list
		String author = null; // TODO
		String bibliographicCitation = null; // TODO
		String docType = null; // TODO
		Fileformat fileformat = null; // TODO
		String title = null; // TODO
		
		// return hit
		Map<String, Object> result = new HashMap<String, Object>(7);
		result.put("bibliographicCitation", bibliographicCitation);
		result.put("creator", author);
		result.put("docType", docType);
		result.put("fileformat", fileformat);
		result.put("title", title);
		return result;
	}

	// @see org.goobi.production.plugin.CataloguePlugin#getNumberOfHits(Object, long)
	public static long getNumberOfHits(Object searchResult, long timeout) {

	}

	// @see org.goobi.production.plugin.UnspecificPlugin#getTitle()
	public static String getTitle() {
		return "PICA Catalogue Plugin";
	}

	// @see org.goobi.production.plugin.CataloguePlugin#supportsCatalogue(String)
	public boolean supportsCatalogue(String catalogue) {

	}

	// @see org.goobi.production.plugin.CataloguePlugin#useCatalogue(String)
	public void useCatalogue(String catalogue) {

	}
}
