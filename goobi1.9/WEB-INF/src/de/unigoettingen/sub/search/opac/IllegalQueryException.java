package de.unigoettingen.sub.search.opac;

//Eingefügt cm 8.5.2007
@SuppressWarnings("serial")
public class IllegalQueryException extends Exception {
	public IllegalQueryException () {
		super();
	}

	public IllegalQueryException (String str) {
		super(str);
	}
	
}
