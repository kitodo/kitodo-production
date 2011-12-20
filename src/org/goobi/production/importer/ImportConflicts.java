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

package org.goobi.production.importer;
public class ImportConflicts {
	/**
	 * simple POJO class
	 * @author Igor Toker
	 */
	
	private String storeidentifier;
	private String property;
	private String goobiValue;
	private String productionValue;
	
	
	public ImportConflicts(String storeidentifier, String property, String goobiValue, String productionValue) {
		super();
		this.storeidentifier = storeidentifier;
		this.property = property;
		this.goobiValue = goobiValue;
		this.productionValue = productionValue;
	}
	
	public void setStoreidentifier(String storeidentifier) {
		this.storeidentifier = storeidentifier;
	}
	public String getStoreidentifier() {
		return storeidentifier;
	}
	public void setProperty(String property) {
		this.property = property;
	}
	public String getProperty() {
		return property;
	}
	public void setGoobiValue(String goobiValue) {
		this.goobiValue = goobiValue;
	}
	public String getGoobiValue() {
		return goobiValue;
	}
	public void setProductionValue(String productionValue) {
		this.productionValue = productionValue;
	}
	public String getProductionValue() {
		return productionValue;
	}
	
}
