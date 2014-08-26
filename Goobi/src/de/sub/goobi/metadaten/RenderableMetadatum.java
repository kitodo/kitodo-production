package de.sub.goobi.metadaten;

import java.util.HashMap;

import ugh.dl.MetadataType;

public abstract class RenderableMetadatum {

	protected HashMap<String, String> labels;

	public static RenderableGroupedMetadatum create(MetadataType metadataType) {
		// TODO implement different types of inputs here
		return new RenderableEdit(metadataType);
	}

	protected String language;

	public boolean isReadonly() {
		return false;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getLabel() {
		return labels.get(language);
	}
}
