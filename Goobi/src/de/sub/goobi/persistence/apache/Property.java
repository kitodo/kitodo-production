package de.sub.goobi.persistence.apache;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
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
import java.util.Date;

public class Property {
	
	private int id; 
	private String title;
	private String value;
	private boolean IstObligatorisch;
	private int DatentypenID;
	private String auswahl;
	private Date creationDate;
	private int container;
	
	
	
	
	
	public Property(int id, String title, String value, boolean istObligatorisch, int datentypenID, String auswahl, 
			Date creationDate, int container) {
		this.id = id;
		this.title = title;
		this.value = value;
		this.IstObligatorisch = istObligatorisch;
		this.DatentypenID = datentypenID;
		this.auswahl = auswahl;
		this.creationDate = creationDate;
		this.container = container;
	}
	public int getId() {
		return this.id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getTitle() {
		return this.title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getValue() {
		return this.value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public boolean isIstObligatorisch() {
		return this.IstObligatorisch;
	}
	public void setIstObligatorisch(boolean istObligatorisch) {
		this.IstObligatorisch = istObligatorisch;
	}
	public int getDatentypenID() {
		return this.DatentypenID;
	}
	public void setDatentypenID(int datentypenID) {
		this.DatentypenID = datentypenID;
	}
	public String getAuswahl() {
		return this.auswahl;
	}
	public void setAuswahl(String auswahl) {
		this.auswahl = auswahl;
	}
	public Date getCreationDate() {
		return this.creationDate;
	}
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	public int getContainer() {
		return this.container;
	}
	public void setContainer(int container) {
		this.container = container;
	}

	



}
