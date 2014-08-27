package de.sub.goobi.metadaten;

public abstract class RenderableGroupableMetadatum extends RenderableMetadatum implements RenderableGroupedMetadatum {
	private final RenderableMetadataGroup container;

	public RenderableGroupableMetadatum(RenderableMetadataGroup container) {
		this.container = container;
	}

	@Override
	public boolean isFirst() {
		return container != null && container.getMembers().iterator().next().equals(this);
	}

}
