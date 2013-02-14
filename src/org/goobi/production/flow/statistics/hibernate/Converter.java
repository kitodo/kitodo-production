/*
 * This file is part of the Goobi Application - a Workflow tool for the support of
 * mass digitization.
 *
 * Visit the websites for more information.
 *     - http://gdz.sub.uni-goettingen.de
 *     - http://www.goobi.org
 *     - http://launchpad.net/goobi-production
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You
 * should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 */

package org.goobi.production.flow.statistics.hibernate;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.goobi.production.flow.statistics.StatisticsManager;

/**
 * class helps to convert results returned from Projections or Queries, 
 * where data types don't match the target data type 
 * 
 * @author Wulf Riebensahm
 * @version 23.05.2009
 */
class Converter {
	private static final Logger logger = Logger.getLogger(Converter.class);

	Object myObject = null;

	SimpleDateFormat sdf;

	/**
	 * constructor retrieves current locale and uses it for formatting data 
	 */
	private Converter() {
		try {
			this.sdf = new SimpleDateFormat("yyyy.MM.dd",
					new DateFormatSymbols(StatisticsManager.getLocale()));
		} catch (NullPointerException e) {
			logger
					.error("Class statistics.hibernate.converter Error, can't get FacesContext");
		}
	}

	/**
	 * constructor (parameterless constructor is set to private)
	 * 
	 * @param Object
	 *            which will get converted
	 */
	protected Converter(Object obj) {
		this();
		if (obj == null) {
			throw new NullPointerException();
		}
		myObject = obj;
	}

	/**
	 * 
	 * @return Integer if possible
	 */
	protected Integer getInteger() {
		if (myObject instanceof Integer) {
			return (Integer) myObject;
		} else if (myObject instanceof Double) {
			return ((Double) myObject).intValue();
		} else if (myObject instanceof String) {
			return Integer.parseInt((String) myObject);
		} else {
			throw new NumberFormatException();
		}
	}

	/**
	 * 
	 * @return Double if possible
	 */
	protected Double getDouble() {
		if (myObject instanceof Integer) {

			return new Double(((Integer) myObject).intValue());
		} else if (myObject instanceof Double) {

			return (Double) myObject;
		} else if (myObject instanceof String) {

			return Double.parseDouble((String) myObject);
		} else {
			throw new NumberFormatException();
		}
	}

	/**
	 * 
	 * @return String, fall back is toString() method
	 */
	protected String getString() {
		if (myObject instanceof Date) {
			return sdf.format(myObject);
		} else {
			return myObject.toString();

		}
	}

	/**
	 * 
	 * @return Double value of GB, calculated on the basis
	 * of Bytes
	 */
	protected Double getGB() {
		return getDouble() / (1024 * 1024 * 1024);

	}

}
