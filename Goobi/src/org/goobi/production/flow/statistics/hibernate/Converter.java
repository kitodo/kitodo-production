package org.goobi.production.flow.statistics.hibernate;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.kitodo.org
 *     		- https://github.com/goobi/goobi-production
 * 		    - http://gdz.sub.uni-goettingen.de
 * 			- http://www.intranda.com
 * 			- http://digiverso.com 
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */

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
					.error("Class statistics.hibernate.Converter Error, can't get FacesContext");
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
		this.myObject = obj;
	}

	/**
	 * 
	 * @return Integer if possible
	 */
	protected Integer getInteger() {
		if (this.myObject instanceof Integer) {
			return (Integer) this.myObject;
		} else if (this.myObject instanceof Double) {
			return ((Double) this.myObject).intValue();
		} else if (this.myObject instanceof String) {
			return Integer.parseInt((String) this.myObject);
		} else if (this.myObject instanceof Long) {
			return ((Long) this.myObject).intValue();
		} else {
			throw new NumberFormatException();
		}
	}

	/**
	 * 
	 * @return Double if possible
	 */
	protected Double getDouble() {
		if (this.myObject instanceof Integer) {

			return new Double(((Integer) this.myObject).intValue());
		} else if (this.myObject instanceof Double) {

			return (Double) this.myObject;
		} else if (this.myObject instanceof String) {

			return Double.parseDouble((String) this.myObject);
		} else if (this.myObject instanceof Long) {
			return ((Long) this.myObject).doubleValue();
		} else {
			throw new NumberFormatException();
		}
	}

	/**
	 * 
	 * @return String, fall back is toString() method
	 */
	protected String getString() {
		if (this.myObject instanceof Date) {
			return this.sdf.format(this.myObject);
		} else {
			return this.myObject.toString();

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
