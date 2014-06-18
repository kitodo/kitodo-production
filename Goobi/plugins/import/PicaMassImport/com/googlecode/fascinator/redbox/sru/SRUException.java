/* 
 * The Fascinator - ReDBox/Mint SRU Client - Exception
 * Copyright (C) 2012 Queensland Cyber Infrastructure Foundation (http://www.qcif.edu.au/)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package com.googlecode.fascinator.redbox.sru;

/**
 * Basic Exception for issues that occur inside this package
 * 
 * @author Greg Pendlebury
 */
public class SRUException extends Exception {

	private static final long serialVersionUID = 2142460060117618325L;
	public SRUException(String message) {
        super(message);
    }
    public SRUException(Throwable cause) {
        super(cause);
    }
    public SRUException(String message, Throwable cause) {
        super(message, cause);
    }
}
