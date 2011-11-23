package org.goobi.production.flow.helper;

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
