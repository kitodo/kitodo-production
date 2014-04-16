package org.goobi.production.plugin.CataloguePlugin;

import java.util.Map;

import ugh.dl.Fileformat;

public class Hit {

	private final Map<String, Object> data;

	public Hit(Map<String, Object> data) {
		this.data = data;
	}

	public String getAuthor() {
		return getAs("creator", String.class);
	}

	public String getBibliographicCitation() {
		return getAs("bibliographicCitation", String.class);
	}

	public String getDocType() {
		return getAs("type", String.class);
	}

	public Fileformat getFileformat() {
		return getAs("fileformat", Fileformat.class);
	}

	public String getTitle() {
		return getAs("title", String.class);
	}

	@SuppressWarnings("unchecked")
	private <T> T getAs(String key, Class<T> type) {
		Object value = data.get(key);
		if (value == null || type.isAssignableFrom(value.getClass()))
			return (T) value;
		else
			throw new ClassCastException("Bad content type of field " + key + " (" + value.getClass().getName()
					+ "), must be " + type.getName());
	}
}
