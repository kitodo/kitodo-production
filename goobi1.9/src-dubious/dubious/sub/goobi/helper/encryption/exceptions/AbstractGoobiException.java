package dubious.sub.goobi.helper.encryption.exceptions;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 * 			- http://digiverso.com 
 * 			- http://www.intranda.com
 * 
 * Copyright 2011, intranda GmbH, Göttingen
 * 
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */import java.io.PrintStream;

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

	@Override
	public void printStackTrace() {
		super.printStackTrace();
		if (this.exception != null) {
           myLogger.error("%%%% wrapped exception: ");
			this.exception.printStackTrace();
		}
	}

	@Override
	public void printStackTrace(PrintStream printStream) {
		super.printStackTrace(printStream);
		if (this.exception != null) {
           myLogger.error("%%%% wrapped exception: ");
			this.exception.printStackTrace(printStream);
		}
	}

	@Override
	public String toString() {
		if (this.exception != null) {
			return super.toString() + " wraps: [" + this.exception.toString() + "]";
		} else {
			return super.toString();
		}
	}


}
