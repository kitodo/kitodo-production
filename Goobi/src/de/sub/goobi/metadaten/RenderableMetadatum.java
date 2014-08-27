package de.sub.goobi.metadaten;

import java.util.HashMap;

import ugh.dl.MetadataType;

public abstract class RenderableMetadatum {

	protected HashMap<String, String> labels;

	protected String language;

	public static RenderableGroupedMetadatum create(MetadataType metadataType,
			RenderableMetadataGroup renderableMetadataGroup) {
		return new RenderableEdit(metadataType, renderableMetadataGroup);
	}

	public String getLabel() {
		return labels.get(language);
	}

	public boolean isReadonly() {
		return false;
	}

	void setLanguage(String language) {
		this.language = language;
	}
}
