package de.sub.goobi.helper.exceptions;

public class DAOException extends AbstractGoobiException {
	private static final long serialVersionUID = 3174737519370361577L;

	public DAOException(Exception e) {
		super(e);
	}

	public DAOException(String string) {
		super(string);
	}
}
