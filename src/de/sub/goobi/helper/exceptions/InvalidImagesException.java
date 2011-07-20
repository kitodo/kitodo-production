package de.sub.goobi.helper.exceptions;

public class InvalidImagesException extends AbstractGoobiException {
	private static final long serialVersionUID = -2677207359216957351L;
	
	public InvalidImagesException(Exception e) {
		super(e);
	}

	public InvalidImagesException(String string) {
		super(string);
	}
}
