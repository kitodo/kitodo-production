/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
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
