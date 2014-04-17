package org.goobi.production.plugin.CataloguePlugin.PicaPlugin;


class FindResult {
	private final Catalogue catalogue;
	private final GetOpac catalogueAccessor;
	private final ConfigOpacCatalogue configuration;
	private final long hits;
	private final Query query;

	FindResult(ConfigOpacCatalogue configuration, Catalogue catalogue, GetOpac catalogueAccessor, Query query,
			long hits) {
		this.configuration = configuration;
		this.catalogue = catalogue;
		this.catalogueAccessor = catalogueAccessor;
		this.query = query;
		this.hits = hits;
	}

	private Catalogue getCatalogue() {
		return catalogue;
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
