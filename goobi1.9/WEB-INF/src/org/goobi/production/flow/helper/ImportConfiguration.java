package org.goobi.production.flow.helper;
/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 * 			- http://digiverso.com 
 * 			- http://www.intranda.com
 * 
 * Copyright 2010, intranda GmbH, Göttingen
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.goobi.production.api.property.xmlbasedprovider.Status;
import org.goobi.production.api.property.xmlbasedprovider.impl.PropertyTemplate;
import org.goobi.production.api.property.xmlbasedprovider.impl.XMLBasedPropertyTemplateProvider;
import org.jdom.JDOMException;

import de.sub.goobi.Beans.Prozess;
import de.sub.goobi.Beans.Vorlage;
import de.sub.goobi.Beans.Werkstueck;
import de.sub.goobi.Beans.Property.IGoobiEntity;
import de.sub.goobi.config.ConfigMain;

public class ImportConfiguration {
	
	private static final Logger logger = Logger.getLogger(ImportConfiguration.class);

	
	private Prozess template;
	private List<PropertyTemplate> processProperties;
	private List<PropertyTemplate> templateProperties;
	private List<PropertyTemplate> workProperties;

	
	
	public ImportConfiguration(Prozess template) {
		this.template =template;
		this.processProperties=getDefaultProperties(template);
		if (template.getVorlagenSize()> 0) {
			this.templateProperties =getDefaultProperties(template.getVorlagenList().get(0));
		} else {
			Vorlage v = new Vorlage();
			v.setProzess(template);
			Set<Vorlage> temp = new HashSet<Vorlage>();
			temp.add(v);
			template.setVorlagen(temp);
			this.templateProperties =getDefaultProperties(v);
		}
		if (template.getWerkstueckeSize() > 0) {
			this.workProperties = getDefaultProperties(template.getWerkstueckeList().get(0));
		} else {
			Werkstueck w = new Werkstueck();
			w.setProzess(template);
			Set<Werkstueck> work = new HashSet<Werkstueck>();
			work.add(w);
			template.setWerkstuecke(work);
			this.workProperties = getDefaultProperties(w);
		}
		
		
	}



	public int getSize() {
		return this.processProperties.size();
	}
	
	
	private List<PropertyTemplate> getDefaultProperties(IGoobiEntity inEntity) {
		List<PropertyTemplate> defProps = new ArrayList<PropertyTemplate>();
		try {
			XMLBasedPropertyTemplateProvider instance = XMLBasedPropertyTemplateProvider.getInstance(inEntity);
			instance.setFilepath(ConfigMain.getParameter("KonfigurationVerzeichnis"));

			Status status = inEntity.getStatus();

			defProps = instance.getTemplates(status, inEntity);

		} catch (IOException e) {
			logger.error("templateProviderOfflineFileNotFound", e);
		} catch (JDOMException e) {
			logger.error(e);
		}
		return defProps;
	}






	public Prozess getTemplate() {
		return this.template;
	}






	public void setTemplate(Prozess template) {
		this.template = template;
	}






	public List<PropertyTemplate> getProcessProperties() {
		return this.processProperties;
	}






	public void setProcessProperties(List<PropertyTemplate> processProperties) {
		this.processProperties = processProperties;
	}






	public List<PropertyTemplate> getTemplateProperties() {
		return this.templateProperties;
	}






	public void setTemplateProperties(List<PropertyTemplate> templateProperties) {
		this.templateProperties = templateProperties;
	}






	public List<PropertyTemplate> getWorkProperties() {
		return this.workProperties;
	}






	public void setWorkProperties(List<PropertyTemplate> workProperties) {
		this.workProperties = workProperties;
	}

}
