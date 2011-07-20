package de.sub.goobi.helper.exceptions;

//TODO: What's the licence of this file?
import java.io.PrintStream;

import org.apache.log4j.Logger;

/**
 General purpose application exception.  This class is a modified version
 of an exception class found in Matt Raible's example app, found at
 http://raibledesigns.com/wiki/Wiki.jsp?page=AppFuse

 @author <a href="mailto:nick@systemmobile.com">Nick Heudecker</a>
 */

public abstract class AbstractGoobiException extends Exception {
   private static final Logger myLogger = Logger.getLogger(AbstractGoobiException.class);
   private static final long serialVersionUID = 967941638835011325L;

   protected Exception exception;
   protected boolean fatal;

   public AbstractGoobiException() {
		super();
	}

	public AbstractGoobiException(String message) {
		super(message);
	}

	public AbstractGoobiException(Exception e) {
		this(e, e.getMessage());
	}

	public AbstractGoobiException(Exception e, String message) {
		super(message);
		this.exception = e;
	}

	public AbstractGoobiException(Exception e, String message, boolean fatal) {
		this(e, message);
		setFatal(fatal);
	}

	public boolean isFatal() {
		return this.fatal;
	}

	public void setFatal(boolean fatal) {
		this.fatal = fatal;
	}

	public void printStackTrace() {
		super.printStackTrace();
		if (this.exception != null) {
           myLogger.error("%%%% wrapped exception: ");
			this.exception.printStackTrace();
		}
	}

	public void printStackTrace(PrintStream printStream) {
		super.printStackTrace(printStream);
		if (this.exception != null) {
           myLogger.error("%%%% wrapped exception: ");
			this.exception.printStackTrace(printStream);
		}
	}

	public String toString() {
		if (exception != null) {
			return super.toString() + " wraps: [" + exception.toString() + "]";
		} else {
			return super.toString();
		}
	}


}
