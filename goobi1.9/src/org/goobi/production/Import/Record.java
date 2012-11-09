package org.goobi.production.Import;
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
 */
import java.util.ArrayList;
import java.util.List;

public class Record {

//	private List<Prozesseigenschaft> processProperties = new ArrayList<Prozesseigenschaft>();
//	private List<Werkstueckeigenschaft> workProperties = new ArrayList<Werkstueckeigenschaft>();
//	private List<Vorlageeigenschaft> templateProperties = new ArrayList<Vorlageeigenschaft>();
	
	private List<String> collections = new ArrayList<String>();
	private String data = "";
	private String id = "";

	/**
	 * @param data
	 *            the data to set
	 */
	public void setData(String data) {
		this.data = data;
	}

	/**
	 * @return the data
	 */
	public String getData() {
		return this.data;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return this.id;
	}

	public void setCollections(List<String> collections) {
		this.collections = collections;
	}

	public List<String> getCollections() {
		return this.collections;
	}

//	public List<Prozesseigenschaft> getProcessProperties() {
//		return this.processProperties;
//	}
//	public void setProcessProperties(List<Prozesseigenschaft> processProperties) {
//		this.processProperties = processProperties;
//	}
//	public List<Werkstueckeigenschaft> getWorkProperties() {
//		return this.workProperties;
//	}
//	public void setWorkProperties(List<Werkstueckeigenschaft> workProperties) {
//		this.workProperties = workProperties;
//	}
//	public List<Vorlageeigenschaft> getTemplateProperties() {
//		return this.templateProperties;
//	}
//	public void setTemplateProperties(List<Vorlageeigenschaft> templateProperties) {
//		this.templateProperties = templateProperties;
//	}
	
}
