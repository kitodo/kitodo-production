package org.goobi.production.plugin.CataloguePlugin.PicaPlugin;


class FindResult {
	private final GetOpac catalogueAccessor;
	private final ConfigOpacCatalogue configuration;
	private final long hits;
	private final Query query;

	FindResult(ConfigOpacCatalogue configuration, GetOpac catalogueAccessor, Query query,
			long hits) {
		this.configuration = configuration;
		this.catalogueAccessor = catalogueAccessor;
		this.query = query;
		this.hits = hits;
	}

	GetOpac getCatalogueAccessor() {
		return catalogueAccessor;
	}

	ConfigOpacCatalogue getConfiguration() {
		return configuration;
	}

	long getHits() {
		return hits;
	}

	Query getQuery() {
		return query;
	}

	@Override
	public int hashCode() {
		throw new UnsupportedOperationException(
				"The classes Catalogue, ConfigOpacCatalogue, GetOpac and Query do not implement equals() and hashCode()—implementing hashCode() here would not work correctly");
	}

	@Override
	public boolean equals(Object obj) {
		throw new UnsupportedOperationException(
				"The classes Catalogue, ConfigOpacCatalogue, GetOpac and Query do not implement equals() and hashCode()—implementing equals() here would not work correctly");
	}
}
