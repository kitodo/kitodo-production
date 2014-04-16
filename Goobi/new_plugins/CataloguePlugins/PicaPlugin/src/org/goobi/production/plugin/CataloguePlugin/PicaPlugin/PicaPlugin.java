package org.goobi.production.plugin.CataloguePlugin.PicaPlugin;

import java.util.HashMap;
import java.util.Map;

import net.xeoh.plugins.base.annotations.PluginImplementation;
import ugh.dl.Fileformat;
import de.unigoettingen.sub.search.opac.ConfigOpac;
import de.unigoettingen.sub.search.opac.ConfigOpacCatalogue;

@PluginImplementation
public class PicaPlugin {
	private static String tempDir;
	private static String configDir;

	public void configure(Map<String, String> configuration) {
		configDir = configuration.get("configDir");
		tempDir = configuration.get("tempDir");
	}

	// @see org.goobi.production.plugin.CataloguePlugin#find(String, long)
	public Object find(String query, long timeout) {
		ConfigOpacCatalogue coc = ConfigOpac.getCatalogueByName(catalogue);
	}

	public static String getConfigDir() {
		return configDir;
	}

	// @see org.goobi.production.plugin.UnspecificPlugin#getDescription()
	public static String getDescription() {
		return "The PICA plugin can be used to access PICA library catalogue systems.";
	}

	// @see org.goobi.production.plugin.CataloguePlugin#getHit(Object, long, long)
	public static Map<String, Object> getHit(Object searchResult, long index, long timeout) {
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

	public static String getTempDir() {
		return tempDir;
	}

	// @see org.goobi.production.plugin.UnspecificPlugin#getTitle()
	public static String getTitle() {
		return "PICA Catalogue Plugin";
	}

	private String catalogue;

	// @see org.goobi.production.plugin.CataloguePlugin#supportsCatalogue(String)
	public supportsCatalogue(String catalogue) {

	}

	// @see org.goobi.production.plugin.CataloguePlugin#useCatalogue(String)
	public void useCatalogue(String catalogue) {
		this.catalogue = catalogue;
	}
}
